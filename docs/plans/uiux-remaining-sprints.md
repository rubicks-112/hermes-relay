# Hermes Relay Android — UI/UX Handoff

> Saved: 2026-05-01
> Context: post-chat-polish, pre-next-sprint
> Repo: `rubicks-112/hermes-relay` @ `e16f7fb`

---

## ✅ COMPLETED — Chat Polish Swarm

Commit `e16f7fb` on `main`. Built and pushed.

### Files Changed
| File | Fix |
|---|---|
| `MarkdownContent.kt` | Inline code background now visible (`tertiaryContainer` tint) |
| `MarkdownContent.kt` | Code-block copy button no longer overlaps first line |
| `MessageBubble.kt` | Long-press copy removed — `SelectionContainer` works cleanly |
| `MessageBubble.kt` | Action-bubble accent bar stretches with content |
| `MessageBubble.kt` | Timestamp only shows on `isLastInGroup` |
| `MessageBubble.kt` | Bubble corners fixed — tail only on last message, flat edges on grouped |
| `ChatScreen.kt` | LazyColumn key/composable mismatch fixed (pre-filter empty messages) |
| `ChatScreen.kt` | Scroll resets to bottom on session switch |
| `ChatScreen.kt` | Tool calls visually grouped with parent bubble (shared `Column`) |
| `ChatScreen.kt` | Suggestion chips send immediately |
| `ChatScreen.kt` | Date separators use solid Surface with elevation |
| `ChatScreen.kt` | Inline autocomplete dismiss-by-tap-outside |
| `ChatScreen.kt` | Base64 decode flag matches encoder (`NO_WRAP`) |
| `ChatScreen.kt` | "Load more" header button for older history |
| `ThinkingBlock.kt` | Thinking blocks render as markdown |
| `CommandPalette.kt` | Auto-focus + keyboard show on open |

---

## 📋 REMAINING — Full Swarm Assessment

### 🔴 CRITICAL (not yet done)

| # | Issue | File | Area | Effort |
|---|---|---|---|---|
| 5 | NavHost zero transitions — instant snap on every screen change | `RelayApp.kt:826` | Nav | Small |
| 6 | Dynamic colors override brand palette on Android 12+ | `Theme.kt:93` | Theme | Small |
| 7 | Sphere intro blocks UI on every cold start (3s, hardcoded dark, not persisted) | `RelayApp.kt:1313` | Nav | Medium |
| 8 | Nested Scaffolds anti-pattern — outer + every sub-screen has its own | `RelayApp.kt:722` + many | Nav | Large |
| 9 | No responsive layout for tablets — bottom nav stretches full width | `RelayApp.kt:754` | Nav | Medium |
| 10 | Nested LazyColumn inside verticalScroll | `BridgeSafetySettingsScreen.kt:169` | Bridge | Small |
| 11 | Touch targets < 48dp (keys, tabs, banner) | `ExtraKeysToolbar.kt`, `TerminalTabBar.kt` | Terminal/Bridge | Medium |
| 12 | Lifecycle observer leak | `BridgeSafetySettingsScreen.kt:98` | Bridge | Small |
| 13 | VerifyStep has no Cancel during pairing timeout | `ConnectionWizard.kt:1585` | Settings | Small |

### 🟠 HIGH (not yet done)

| # | Issue | File | Area | Effort |
|---|---|---|---|---|
| 18 | ChatScreen TopAppBar permanently fixed + color mismatch with radial background | `ChatScreen.kt:715` | Nav | Small |
| 19 | Sub-screens lack navigation-bar padding — gesture nav overlaps content | Many settings screens | Nav | Medium |
| 20 | Deep-link nav lacks back-stack hygiene — duplicates on notification tap | `RelayApp.kt:574` | Nav | Small |
| 21 | TerminalWebView blocks system font scaling | `TerminalWebView.kt:80` | Terminal | Small |
| 22 | VoiceWaveform saveLayer every frame — performance drain | `VoiceWaveform.kt:183` | Voice | Small |
| 23 | Switches lack content descriptions — screen reader silent | `BridgeMasterToggle.kt`, `UnattendedAccessRow.kt` | Bridge | Small |
| 24 | Destructive dialog relies on color alone — accessibility fail | `DestructiveVerbConfirmDialog.kt:190` | Bridge | Small |
| 25 | No paste icon on manual URL fields | `ConnectionWizard.kt` | Settings | Small |
| 26 | ActiveAgentCard lacks connection status tint — stale looks healthy | `SettingsScreen.kt:360` | Settings | Small |

### 🟡 MEDIUM (not yet done)

| # | Issue | File | Area | Effort |
|---|---|---|---|---|
| 30 | No per-message delivery / failed status | `MessageBubble.kt` | Chat | Medium |
| 31 | Command palette search doesn't auto-focus | `CommandPalette.kt:136` | Chat | ✅ Done |
| 34 | ProfileInspector tab switches cut instantly — no crossfade | `ProfileInspectorScreen.kt:228` | Nav | Small |
| 35 | Bottom-nav tab switching has no transition | `RelayApp.kt:811` | Nav | Small |
| 36 | Mic button lacks nav-bar padding | `VoiceModeOverlay.kt:459` | Voice | Small |
| 37 | Search bar focus trap — keyboard stays up after dismiss | `TerminalSearchBar.kt:66` | Terminal | Small |
| 38 | Transcript auto-scroll ignores user | `VoiceModeOverlay.kt:229` | Voice | Small |
| 39 | Info sheet rename hidden by keyboard | `TerminalSessionInfoSheet.kt:302` | Terminal | Small |
| 40 | Countdown formatting breaks > 60 min | `BridgeSafetySummaryCard.kt:117` | Bridge | Small |

### 🟢 LOW / QoL (not yet done)

| # | Issue | File | Area | Effort |
|---|---|---|---|---|
| 41 | Missing pull-to-refresh for older history | `ChatScreen.kt` | Chat | Partial (UI added, no VM method) |
| 42 | Missing reactions, reply-to, swipe actions | `MessageBubble.kt` | Chat | Large |
| 43 | Empty-state suggestions are hardcoded | `ChatScreen.kt:997` | Chat | Medium |
| 44 | No Share action for messages | `MessageBubble.kt` | Chat | Small |
| 47 | Onboarding pager state not saved across process death | `OnboardingScreen.kt:124` | Nav | Small |
| 48 | Splash-to-main transition disjointed | `MainActivity.kt:71` | Nav | Small |
| 49 | Settings sub-screens need scroll-aware TopAppBar | Many | Nav | Medium |
| 50 | Theme lacks surface-tint/elevation differentiation in dark | `Theme.kt:25` | Theme | Small |
| 51 | NotificationCompanion drops rich content | `HermesNotificationCompanion.kt:126` | Bridge | Medium |
| 52 | Error banner not announced by TalkBack | `VoiceModeOverlay.kt:371` | Voice | Small |
| 53 | Tab close icon too small | `TerminalTabBar.kt:168` | Terminal | Small |
| 54 | Unbounded WebView memory per tab | `TerminalScreen.kt:257` | Terminal | Medium |
| 55 | Unbounded DropdownMenu height | `VoiceModeOverlay.kt:166` | Voice | Small |

---

## 🎯 Recommended Next Sprints

### Sprint A: Navigation Smoothness (highest perceived quality)
- #5 NavHost transitions
- #34 ProfileInspector tab crossfade
- #35 Bottom-nav tab transitions
- #18 Chat TopAppBar scroll behavior + color fix
- #49 Settings scroll-aware TopAppBars

### Sprint B: Accessibility Pass (Play Store requirement)
- #11 Touch targets < 48dp
- #23 Switches lack content descriptions
- #24 Destructive dialog color-only
- #52 Error banner not announced
- #19 Sub-screens navigation-bar padding
- #21 TerminalWebView font scaling

### Sprint C: Bridge/Terminal Hardening
- #10 Nested LazyColumn
- #12 Lifecycle observer leak
- #37 Search bar focus trap
- #39 Info sheet keyboard hidden
- #54 Unbounded WebView memory

### Sprint D: Theming & Polish
- #6 Dynamic colors override brand
- #7 Sphere intro persistence
- #50 Surface-tint in dark mode
- #48 Splash-to-main continuity

---

## 🏗️ Architecture Debt (do last)
- #8 Nested Scaffolds refactor → single Scaffold in RelayApp
- #9 Tablet responsive layout → NavigationRail + max-width constraints
- #42 Reactions/reply/swipe → architectural addition

---

## 🔗 Links
- **APK server:** `http://100.110.162.124:8080/`
- **API server:** `http://100.110.162.124:8642/`
- **Relay:** `ws://100.110.162.124:8767/`
- **Fork:** `https://github.com/rubicks-112/hermes-relay`
