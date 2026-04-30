"""Chat channel handler — proxies between phone (WSS) and Hermes OpenAI-compatible API.

Updated for Hermes Agent modern architecture (v2026.04+).
The legacy WebAPI endpoints (/api/sessions/{id}/chat/stream) no longer exist.
Hermes now exposes an OpenAI-compatible API at /v1/chat/completions on port 8642.

This handler:
  1. Maintains local session state (no server-side session creation needed)
  2. POSTs to /v1/chat/completions with streaming enabled
  3. Parses SSE deltas and re-emits as WebSocket envelopes to the phone
"""

from __future__ import annotations

import json
import logging
import uuid
from typing import Any

import aiohttp
from aiohttp import web

logger = logging.getLogger(__name__)

# SSE event type mapping from OpenAI stream format to our envelope protocol
_SSE_TYPE_MAP: dict[str, str] = {
    "content_delta": "chat.delta",
    "delta": "chat.delta",
    "tool_start": "chat.tool.started",
    "tool_started": "chat.tool.started",
    "tool_result": "chat.tool.completed",
    "tool_completed": "chat.tool.completed",
    "content_complete": "chat.completed",
    "complete": "chat.completed",
    "completed": "chat.completed",
    "error": "chat.error",
}


def _make_envelope(
    msg_type: str,
    payload: dict[str, Any],
    msg_id: str | None = None,
) -> str:
    """Build a JSON envelope string for the chat channel."""
    return json.dumps(
        {
            "channel": "chat",
            "type": msg_type,
            "id": msg_id or str(uuid.uuid4()),
            "payload": payload,
        }
    )


class ChatHandler:
    """Handles all ``chat.*`` messages from the phone.

    Uses an ``aiohttp.ClientSession`` to talk to the Hermes OpenAI-compatible API.
    The HTTP session is created lazily and reused across requests.
    """

    def __init__(
        self,
        api_url: str = "http://localhost:8642",
        api_key: str | None = None,
        model: str = "kimi-k2.6",
    ) -> None:
        self._api_url = api_url.rstrip("/")
        self._api_key = api_key or "relay-local-dev"
        self._model = model
        self._http: aiohttp.ClientSession | None = None
        # Local session store: session_id -> {messages: [], profile: str}
        self._sessions: dict[str, dict[str, Any]] = {}

    async def _get_http(self) -> aiohttp.ClientSession:
        """Return (and lazily create) the shared HTTP client session."""
        if self._http is None or self._http.closed:
            headers = {
                "Authorization": f"Bearer {self._api_key}",
                "Content-Type": "application/json",
            }
            self._http = aiohttp.ClientSession(
                timeout=aiohttp.ClientTimeout(total=None, connect=10),
                headers=headers,
            )
        return self._http

    async def close(self) -> None:
        """Shut down the HTTP client session."""
        if self._http is not None and not self._http.closed:
            await self._http.close()
            self._http = None

    # ── Dispatcher ───────────────────────────────────────────────────────

    async def handle(self, ws: web.WebSocketResponse, envelope: dict[str, Any]) -> None:
        """Route an incoming chat-channel envelope to the right handler."""
        msg_type = envelope.get("type", "")
        payload = envelope.get("payload", {})
        msg_id = envelope.get("id")

        if msg_type == "chat.send":
            await self._handle_send(ws, payload, msg_id)
        elif msg_type == "chat.sessions.list":
            await self._handle_sessions_list(ws, payload, msg_id)
        else:
            logger.warning("Unknown chat message type: %s", msg_type)
            await ws.send_str(
                _make_envelope(
                    "chat.error",
                    {"message": f"Unknown chat message type: {msg_type}"},
                    msg_id,
                )
            )

    # ── chat.send → stream response ─────────────────────────────────────

    async def _handle_send(
        self,
        ws: web.WebSocketResponse,
        payload: dict[str, Any],
        msg_id: str | None,
    ) -> None:
        """Process a chat.send message: maintain session locally, stream via OpenAI API."""
        profile = payload.get("profile", "default")
        session_id = payload.get("session_id")
        message = payload.get("message", "")

        if not message:
            await ws.send_str(
                _make_envelope(
                    "chat.error",
                    {"message": "Empty message"},
                    msg_id,
                )
            )
            return

        http = await self._get_http()

        # If no session_id provided, create a new local session
        if not session_id:
            session_id = await self._create_session(ws, profile, msg_id)
            if session_id is None:
                return  # Error already sent

        # Ensure session exists
        if session_id not in self._sessions:
            self._sessions[session_id] = {
                "messages": [],
                "profile": profile,
                "title": message[:40] + "..." if len(message) > 40 else message,
            }

        session = self._sessions[session_id]
        session["messages"].append({"role": "user", "content": message})

        # Build OpenAI-compatible request
        request_body: dict[str, Any] = {
            "model": self._model,
            "messages": session["messages"],
            "stream": True,
            "max_iterations": 90,
        }

        # Include profile if supported
        if profile and profile != "default":
            request_body["profile"] = profile

        logger.info(
            "Streaming chat: session=%s profile=%s message=%s",
            session_id,
            profile,
            message[:80],
        )

        url = f"{self._api_url}/v1/chat/completions"

        try:
            async with http.post(
                url,
                json=request_body,
                headers={"Accept": "text/event-stream"},
            ) as resp:
                if resp.status != 200:
                    body = await resp.text()
                    logger.error(
                        "API returned %d for chat stream: %s",
                        resp.status,
                        body[:200],
                    )
                    await ws.send_str(
                        _make_envelope(
                            "chat.error",
                            {
                                "message": f"API error ({resp.status}): {body[:200]}"
                            },
                            msg_id,
                        )
                    )
                    return

                # Consume SSE stream and build assistant message
                assistant_content = await self._consume_sse_stream(
                    ws, resp, session_id, msg_id
                )

                # Store assistant response in session history
                if assistant_content:
                    session["messages"].append(
                        {"role": "assistant", "content": assistant_content}
                    )

        except aiohttp.ClientError as exc:
            logger.error("HTTP error talking to API: %s", exc)
            await ws.send_str(
                _make_envelope(
                    "chat.error",
                    {"message": f"Cannot reach Hermes API: {exc}"},
                    msg_id,
                )
            )
        except ConnectionResetError:
            logger.warning("WebSocket closed while streaming chat response")

    async def _create_session(
        self,
        ws: web.WebSocketResponse,
        profile: str,
        msg_id: str | None,
    ) -> str | None:
        """Create a new local chat session. Returns session_id or None."""
        session_id = str(uuid.uuid4())
        self._sessions[session_id] = {
            "messages": [],
            "profile": profile,
            "title": "New chat",
            "model": self._model,
        }

        # Notify the phone of the new session
        await ws.send_str(
            _make_envelope(
                "chat.session",
                {
                    "session_id": session_id,
                    "title": "New chat",
                    "model": self._model,
                },
                msg_id,
            )
        )
        logger.info("Created local session %s (profile=%s)", session_id, profile)
        return session_id

    # ── SSE stream consumer ──────────────────────────────────────────────

    async def _consume_sse_stream(
        self,
        ws: web.WebSocketResponse,
        resp: aiohttp.ClientResponse,
        session_id: str,
        msg_id: str | None,
    ) -> str:
        """Read an SSE stream from the OpenAI API and forward events to the phone.

        Returns the full assistant content accumulated from deltas.
        SSE format: lines of ``data: {...}\\n\\n`` with optional ``event:`` lines.
        """
        buffer = ""
        current_event_type: str | None = None
        assistant_content = ""
        tool_calls: list[dict[str, Any]] = []

        async for chunk_bytes in resp.content.iter_any():
            if ws.closed:
                logger.info("WebSocket closed — stopping SSE consumption")
                return assistant_content

            chunk = chunk_bytes.decode("utf-8", errors="replace")
            buffer += chunk

            # Process complete lines
            while "\n" in buffer:
                line, buffer = buffer.split("\n", 1)
                line = line.rstrip("\r")

                if not line:
                    current_event_type = None
                    continue

                if line.startswith("event:"):
                    current_event_type = line[len("event:"):].strip()
                    continue

                if line.startswith("data:"):
                    data_str = line[len("data:"):].strip()
                    if not data_str:
                        continue
                    if data_str == "[DONE]":
                        continue

                    content_delta = await self._emit_sse_event(
                        ws, data_str, session_id, msg_id, current_event_type
                    )
                    if content_delta:
                        assistant_content += content_delta
                    continue

                if line.startswith(":"):
                    continue

                logger.debug("Ignoring unknown SSE line: %s", line[:100])

        return assistant_content

    async def _emit_sse_event(
        self,
        ws: web.WebSocketResponse,
        data_str: str,
        session_id: str,
        msg_id: str | None,
        sse_event_type: str | None,
    ) -> str | None:
        """Parse a single SSE ``data:`` value and send the corresponding WS envelope.

        Returns any content delta for accumulation into the assistant message.
        """
        try:
            data = json.loads(data_str)
        except json.JSONDecodeError:
            logger.debug("Non-JSON SSE data: %s", data_str[:100])
            return None

        # OpenAI streaming format:
        # data: {"choices": [{"delta": {"content": "..."}, "finish_reason": null}]}
        choices = data.get("choices", [])
        if not choices:
            return None

        choice = choices[0]
        delta = choice.get("delta", {})
        finish_reason = choice.get("finish_reason")

        # Extract content delta
        content = delta.get("content", "")
        if content:
            try:
                await ws.send_str(
                    _make_envelope(
                        "chat.delta",
                        {
                            "session_id": session_id,
                            "delta": content,
                        },
                        msg_id,
                    )
                )
            except ConnectionResetError:
                logger.warning("WebSocket closed while sending delta")
                return content
            return content

        # Handle tool calls
        tool_call_delta = delta.get("tool_calls")
        if tool_call_delta:
            # Forward tool call start
            for tc in tool_call_delta:
                if tc.get("id") and tc.get("function", {}).get("name"):
                    try:
                        await ws.send_str(
                            _make_envelope(
                                "chat.tool.started",
                                {
                                    "tool_name": tc["function"]["name"],
                                    "args": tc["function"].get("arguments", {}),
                                },
                                msg_id,
                            )
                        )
                    except ConnectionResetError:
                        logger.warning("WebSocket closed while sending tool start")

        # Handle completion
        if finish_reason is not None:
            try:
                await ws.send_str(
                    _make_envelope(
                        "chat.completed",
                        {
                            "session_id": session_id,
                            "finish_reason": finish_reason,
                        },
                        msg_id,
                    )
                )
            except ConnectionResetError:
                logger.warning("WebSocket closed while sending completion")

        return None

    # ── chat.sessions.list ───────────────────────────────────────────────

    async def _handle_sessions_list(
        self,
        ws: web.WebSocketResponse,
        payload: dict[str, Any],
        msg_id: str | None,
    ) -> None:
        """Return local session list to the phone."""
        profile = payload.get("profile")

        sessions = []
        for sid, sess in self._sessions.items():
            if profile and sess.get("profile") != profile:
                continue
            sessions.append(
                {
                    "id": sid,
                    "title": sess.get("title", "Untitled"),
                    "model": sess.get("model", self._model),
                    "profile": sess.get("profile", "default"),
                    "message_count": len(sess.get("messages", [])),
                }
            )

        # Sort by most recently used (we don't track timestamps, so just reverse)
        sessions.reverse()

        await ws.send_str(
            _make_envelope(
                "chat.sessions",
                {"sessions": sessions},
                msg_id,
            )
        )
        logger.info("Sent %d sessions to phone", len(sessions))
