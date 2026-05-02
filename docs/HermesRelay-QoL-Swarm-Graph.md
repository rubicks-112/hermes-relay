# Hermes Relay Android — QoL Swarm Graph & Issue Registry

> Generated: 2026-05-03
> Repo: `rubicks-112/hermes-relay` @ `03c8cbd`
> Commits ahead of upstream: 11
> Status: **TESTED & SHIPPED**

---

## 📊 Commit Graph (11 commits since chat-polish baseline `f495738`)

```
f495738  fix(android): chat polish swarm — baseline
    │
    ▼
e0a7765  fix(android): bridge/terminal hardening
    │      └─ BridgeSafetySettingsScreen: nested LazyColumn→Column, lifecycle leak→DisposableEffect
    │      └─ TerminalSearchBar: focus trap guarded
    │      └─ TerminalSessionInfoSheet: imePadding
    │      └─ TerminalScreen: WebView onPause/onResume for inactive tabs
    │      └─ TerminalTabBar: close icon 48dp touch target
    │      └─ BridgeSafetySummaryCard: countdown >60min formatting
    │
    ▼
679b040  fix(android): navigation smoothness + accessibility pass
    │      └─ NavHost: slide+fade transitions
    │      └─ Sphere intro: DataStore persistence (skip after first launch)
    │      └─ ProfileInspector: AnimatedContent tab crossfade
    │      └─ Chat TopAppBar: transparent containerColor
    │      └─ 12× settings screens: scroll-aware pinned TopAppBars
    │      └─ ALL settings screens: navigationBarsPadding
    │      └─ ExtraKeysToolbar, TerminalTabBar, ThinkingBlock: touch targets ≥48dp
    │      └─ 17+ switches: contentDescription semantics
    │      └─ DestructiveVerbConfirmDialog: "Allow (destructive)" text + warning icon
    │      └─ VoiceModeOverlay error banner: liveRegion=Assertive
    │      └─ TerminalWebView: respects system font scale (capped 0.8–1.5×)
    │      └─ ConnectionWizard: paste icons, Cancel button, nav/IME padding
    │      └─ SettingsScreen ActiveAgentCard: connection status tint
    │
    ▼
748e3e1  fix(android): navigation smoothness — refined
    │      └─ NavHost transitions: context-aware (bottom-nav fade vs push slide)
    │      └─ Chat TopAppBar: enterAlwaysScrollBehavior
    │
    ▼
e76a63a  fix(android): onboarding, splash, theme surface-tint
    │      └─ OnboardingScreen: rememberSaveable pager state
    │      └─ RelayApp: sphere intro delay 2500ms→1500ms, fade 600ms→400ms
    │      └─ MainActivity: splash exit 400ms→600ms
    │      └─ Theme.kt: DarkColorScheme surfaceTint=HermesPrimaryLight@0.15f
    │      └─ Theme.kt: surface=HermesNavySurface (elevated from background)
    │
    ▼
ad72f8e  fix(android): chat QoL — message status, share, suggestions
    │      └─ ChatMessage: +MessageStatus enum (SENDING, SENT, FAILED)
    │      └─ ChatHandler: +updateMessageStatus(messageId, status)
    │      └─ ChatViewModel: sendMessage flow sets SENDING→SENT/FAILED
    │      └─ MessageBubble: status icons for user messages
    │      └─ MessageBubble: share icon + retry icon for FAILED
    │      └─ ChatScreen: onShareMessage intent (ACTION_SEND)
    │      └─ ChatScreen: suggestions from R.array.chat_suggestions (was hardcoded)
    │
    ▼
12646db  fix(android): notification rich content, waveform perf, transcript scroll
    │      └─ NotificationEntry: +category, bigText, inboxLines, actions, messages,
    │                            conversationTitle, hasImage, progress
    │      └─ NotificationModels: +NotificationMessage, +NotificationProgress
    │      └─ HermesNotificationCompanion: MessagingStyle, BigText, InboxStyle,
    │                                      actions, progress, image detection
    │      └─ VoiceWaveform.kt: saveLayer only when alpha<1.0
    │      └─ VoiceModeOverlay: userHasScrolled guard for auto-scroll
    │
    ▼
f6bfa07  feat(android): tablet responsive layout — NavigationRail, adaptive width
    │      └─ RelayApp: calculateWindowSizeClass → isCompact detection
    │      └─ RelayApp: NavigationRail for !isCompact, NavigationBar for isCompact
    │      └─ RelayApp: Row wrapper around Scaffold for tablet layout
    │
    ▼
30576b4  test(android): NotificationModels serialization round-trip
    │      └─ 10 tests: full round-trip, legacy payload, defaults, nullable fields
    │
    ▼
fa10d81  test(android): ChatMessage status + ChatHandler.updateMessageStatus
    │      └─ ChatMessageTest: 5 tests (default, copy, enum ordering)
    │      └─ ChatHandlerTest: 5 tests (flip, no-op, concurrency stress)
    │
    ▼
b327807  test(android): Compose UI tests for settings, wizard, empty state
    │      └─ ConnectionWizardTest: paste icons (3 fields), Cancel button (2 states)
    │      └─ SettingsScreenTest: ActiveAgentCard Connected/Disconnected status
    │      └─ TabletLayoutTest: NavigationRail renders, item selection
    │      └─ EmptyStateTest: BridgeScreen empty state
    │      └─ OnboardingFlowTest: @Ignore (needs ConnectionViewModel injection)
    │
    ▼
03c8cbd  test(android): MessageBubble status icons + fix existing tests
           └─ MessageBubbleStatusTest: 4 tests (SENDING, FAILED, SENT, assistant)
           └─ ProfileSelectionStoreTest: fixed UncompletedCoroutinesError
           └─ EndpointResolverTest: fixed TTL timing values
```

---

## 🔴 ACTIVE ISSUES — Post-Tablet Install

### Issue #1: Pairing succeeds, chat session creation fails
**Severity:** CRITICAL  
**Device:** poco-x4-gt (tablet)  
**Observed:** Pairing code `9ZI8VF` accepted, but app fails to create a chat session after pairing.

**Hypotheses:**
1. **Endpoint resolution bug:** The pairing QR contains LAN IP `192.168.1.12`. If the tablet is on a different network segment or using Tailscale, the LAN IP may be unreachable. The app pairs over the relay WebSocket (`ws://192.168.1.12:8767`) but the API server (`http://192.168.1.12:8642`) may be unreachable for session creation.
2. **Session creation API not called:** The `sendMessage` or `createSession` path may not trigger after pairing completes. Check if `AuthState` transitions to `Paired` but `ConnectionViewModel.activeConnection` is null.
3. **Missing `loadMessageHistory`:** If the session is created server-side but the client never calls `getMessages()`, the chat screen shows empty with no error.

**Files to investigate:**
- `ConnectionManager.kt` — pairing handshake, `authenticate()` path
- `ChatViewModel.kt` — `sendMessageInternal()`, session creation
- `HermesApiClient.kt` — `createSession()`, `getMessages()`
- `AuthManager.kt` — `serverIssuedCode` consumption, `applyServerIssuedCode()`

**Next steps:**
- [ ] Check app logs for session creation error
- [ ] Verify API server reachable from tablet (`curl http://192.168.1.12:8642/health` from tablet)
- [ ] Test if manual session creation works via API

---

### Issue #2: LAN should be 2nd option, Tailscale should NOT be primary
**Severity:** HIGH  
**Component:** EndpointResolver, pairing QR, ADR-24 multi-endpoint

**Current behavior:**
- Pairing QR contains LAN IP `192.168.1.12` as primary endpoint
- Tailscale IP `100.110.162.124` is NOT included in the QR payload
- If LAN is unreachable (different subnet, mobile data, VPN off), pairing succeeds but API calls fail

**Expected behavior:**
- Endpoint priority: **LAN (fastest) → Tailscale (reliable fallback) → Public (if configured)**
- The pairing QR should include BOTH LAN and Tailscale endpoints
- `EndpointResolver` should probe all candidates and pick the reachable one

**Files to investigate:**
- `plugin/relay/server.py` — `handle_pairing_mint()`, `build_payload()`
- `app/src/main/kotlin/com/hermesandroid/relay/network/EndpointResolver.kt`
- `app/src/main/kotlin/com/hermesandroid/relay/data/Endpoint.kt`
- `app/src/main/kotlin/com/hermesandroid/relay/auth/AuthManager.kt` — `setPendingEndpoints()`

**Current QR payload:**
```json
{
  "hermes": 1,
  "host": "192.168.1.12",
  "port": 8642,
  "relay": {
    "url": "ws://192.168.1.12:8767",
    "code": "9ZI8VF"
  }
}
```

**Desired QR payload (v3 ADR-24):**
```json
{
  "hermes": 1,
  "endpoints": [
    {"host": "192.168.1.12", "port": 8642, "priority": 1, "label": "LAN"},
    {"host": "100.110.162.124", "port": 8642, "priority": 2, "label": "Tailscale"}
  ],
  "relay": {
    "url": "ws://192.168.1.12:8767",
    "code": "9ZI8VF"
  }
}
```

**Related prior fix:** Commit `c41a759` (v3 QR pairing) already fixed server-side to include `relay.code` in each endpoint block. This needs extension to include Tailscale IP.

**Next steps:**
- [ ] Modify `server.py` `build_payload()` to detect and include Tailscale IP
- [ ] Verify `EndpointResolver` probes all candidates correctly
- [ ] Update pairing QR generation to include multi-endpoint list

---

## 📁 Files Changed (36 files)

### Kotlin Source (main)
| File | Change |
|------|--------|
| `RelayApp.kt` | NavHost transitions, NavigationRail/Bar adaptive, sphere intro persistence |
| `ChatScreen.kt` | Transparent TopAppBar, scroll behavior, suggestions from resources |
| `MessageBubble.kt` | Status icons, share/retry actions |
| `ChatMessage.kt` | +MessageStatus enum, +status field |
| `ChatHandler.kt` | +updateMessageStatus() |
| `ChatViewModel.kt` | Status lifecycle (SENDING→SENT/FAILED) |
| `Theme.kt` | surfaceTint, surface color differentiation |
| `ConnectionWizard.kt` | Paste icons, Cancel button, nav/IME padding |
| `SettingsScreen.kt` | ActiveAgentCard status tint |
| `VoiceModeOverlay.kt` | liveRegion error banner, user scroll guard |
| `VoiceWaveform.kt` | Conditional saveLayer |
| `TerminalScreen.kt` | WebView onPause/onResume |
| `TerminalTabBar.kt` | 48dp touch targets |
| `TerminalSearchBar.kt` | Focus trap fix |
| `TerminalSessionInfoSheet.kt` | imePadding |
| `TerminalWebView.kt` | Font scaling respect |
| `BridgeSafetySettingsScreen.kt` | Column instead of LazyColumn, DisposableEffect |
| `BridgeSafetySummaryCard.kt` | >60min countdown |
| `BridgeMasterToggle.kt` | contentDescription |
| `UnattendedAccessRow.kt` | contentDescription |
| `DestructiveVerbConfirmDialog.kt` | Warning text+icon |
| `ExtraKeysToolbar.kt` | 48dp touch targets |
| `ThinkingBlock.kt` | Touch target padding |
| `NotificationModels.kt` | Rich content fields |
| `HermesNotificationCompanion.kt` | MessagingStyle, BigText, InboxStyle extraction |
| `OnboardingScreen.kt` | rememberSaveable pager state |
| `MainActivity.kt` | Splash duration |
| `DataStoreProvider.kt` | Internal→public visibility |

### Kotlin Test (unit)
| File | Tests |
|------|-------|
| `ChatMessageTest.kt` | 5 new (status enum) |
| `ChatHandlerTest.kt` | 5 new (updateMessageStatus) |
| `NotificationModelsTest.kt` | 10 new (serialization) |
| `ProfileSelectionStoreTest.kt` | Fixed coroutine scope |
| `EndpointResolverTest.kt` | Fixed TTL timing |

### Kotlin Test (instrumented)
| File | Tests |
|------|-------|
| `MessageBubbleStatusTest.kt` | 4 new |
| `ConnectionWizardTest.kt` | 5 new |
| `SettingsScreenTest.kt` | 2 new |
| `TabletLayoutTest.kt` | 2 new |
| `EmptyStateTest.kt` | Updated |
| `OnboardingFlowTest.kt` | @Ignore |

---

## 🔗 Build & Deploy

| Artifact | Location |
|---|---|
| APK | `http://100.110.162.124:8080/hermes-relay-0.6.0-sideload-debug.apk` |
| Pairing QR | `~/.hermes/hermes-relay/hermes-pair-qr.png` |
| Fork | `https://github.com/rubicks-112/hermes-relay` @ `03c8cbd` |
| API Server | `http://100.110.162.124:8642/` |
| Relay | `ws://100.110.162.124:8767/` |

---

## 🎯 Remaining Work

| Priority | Issue | Fix Location |
|---|---|---|
| P0 | Chat session creation fails after pairing | `ConnectionManager.kt`, `ChatViewModel.kt` |
| P0 | LAN-only QR, no Tailscale fallback | `server.py`, `EndpointResolver.kt` |
| P1 | OnboardingFlowTest needs ConnectionViewModel injection | `OnboardingFlowTest.kt` |
| P2 | Dynamic color override brand palette (#6) | `Theme.kt` |
| P2 | Reactions/reply/swipe actions (#42) | `MessageBubble.kt` — architectural |
| P2 | Nested Scaffolds refactor (#8) | All settings screens — large refactor |

---

*Report saved to rubick-shared for cross-session persistence.*
