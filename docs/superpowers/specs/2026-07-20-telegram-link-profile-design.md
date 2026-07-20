# Telegram link in Profile — design

Date: 2026-07-20  
Status: approved in brainstorming (approach 1)  
Scope: web Profile only — Network + CommonViewModels + DrivebitWeb ProfilePage

## Problem

Backend already supports Telegram notifications via account binding (`@drivebit_bot`). Clients have no UI or API wrapper. Users need to link/unlink from Profile so booking notifications can go to Telegram as well as email.

Backend (drivebitbackend `NotificationsController` / `TelegramLinkService`):

| Endpoint | Role |
|----------|------|
| `GET Notifications/Telegram/link/status` | `{ isLinked, telegramUsername?, linkedAt? }` — binding **is** the preference; no separate “enabled” flag |
| `POST Notifications/Telegram/link` | Creates token + 6-digit `code` (TTL **15 min**), `deepLinkUrl = https://t.me/{bot}?start={token}` |
| `POST Notifications/Telegram/unlink` | Removes binding; response **204 NoContent** |
| `POST Notifications/Telegram/webhook` | Bot only — **out of client scope** |

## Decision

**Approach 1 — dedicated Network service + `TelegramLinkViewModel`, UI block on `ProfilePage`.**

Not folding into `ProfileViewModel` (keeps profile load separate). Not putting link logic in Compose.

## UX (from product brief)

Lazy-load status when Profile is shown.

1. **Checkbox** label: `Получать уведомления в телеграм`  
   Initial checked state = `isLinked` from `GET .../link/status`.

2. **Off → on:** call `POST .../link`. On success (**200**):
   - Show button **«Привязать телеграм»** → opens `deepLinkUrl`.
   - Below:  
     `Если кнопка не работает, то отправьте код {code} телеграм боту @drivebit_bot`  
     (`@drivebit_bot` / `https://t.me/drivebit_bot` clickable).
   - Checkbox stays visually on while waiting for bind; poll `status` until `isLinked` or give up after expiry window.
   - On `POST /link` failure: revert checkbox, show error under the block.

3. **On → off:** do **not** call unlink immediately. Show confirm popup:  
   `Вы перестанете получать уведомления в телеграм`  
   - **Ок** → `POST .../unlink` (treat **204** as success) → clear pending link UI, checkbox off.  
   - **Отмена** → close popup, checkbox remains on.

## Architecture

```
ProfilePage
  → TelegramLinkViewModel (intents / StateFlow)
      → TelegramNotifications (Network)
          → GET status / POST link / POST unlink
```

### Network: `TelegramNotifications`

- `getLinkStatus(): TelegramBindingStatus`
- `createLink(): TelegramLinkStart`
- `unlink()` — success on 204 (and 200 if ever returned)
- DTOs mirror backend: `isLinked`, `telegramUsername`, `linkedAt`, `token`, `code`, `deepLinkUrl`, `expiresAt`
- Auth: existing authorized `HttpClient` (same as other `[Authorize]` calls)
- Register in `NetworkModule`

### ViewModel: `TelegramLinkViewModel`

States (sealed / clear UI mapping):

| State | UI |
|-------|-----|
| `Loading` | Checkbox disabled / small loader in block |
| `Unlinked` | Checkbox off; no button/code |
| `LinkPending(code, deepLinkUrl, expiresAt)` | Checkbox on; button + instruction with code |
| `Linked(telegramUsername?)` | Checkbox on; no button/code |
| `ConfirmUnlink` | Popup over current linked UI |
| `Error(message, previous?)` | Message under block; restore previous where possible |

Intents: `loadStatus()`, `onCheckboxChanged(checked)`, `openDeepLink()`, `confirmUnlink()`, `cancelUnlink()`.

- `onCheckboxChanged(true)` → `createLink` → `LinkPending` + start status poll  
- `onCheckboxChanged(false)` when `Linked` → `ConfirmUnlink` (checkbox stays on until confirm)  
- Poll: periodic `getLinkStatus` while `LinkPending`; stop on `isLinked`, on expiry, or on leave

DI: register in `ViewModelsModule`; inject into `ProfilePage` (koin).

### UI: `ProfilePage` only (web)

- Block after phone / email / password rows (same `RowSpaceBetween` / spacing patterns).
- Reuse `TermsConsentCheckbox`-style native checkbox or existing profile controls; simple confirm overlay for unlink (no new design system).
- Open `deepLinkUrl` via `window.open` / location (web).

## Error handling

| Case | Behavior |
|------|----------|
| `status` 401 | Existing profile unauth redirect path; do not show Telegram block as linked |
| `status` / `link` network or 5xx | Error text under block |
| `link` 503 (bot not configured) | Surfaced message; checkbox off |
| `unlink` failure | Stay linked / ConfirmUnlink closed with error; checkbox on |
| Expired pending code | Stay on LinkPending until user toggles again or refresh; optional hint that code expired |

## Testing (TDD)

Iron Law: failing test first, then minimal implementation.

**Network (`commonTest`):**

- Parse status JSON → `isLinked`
- Parse link JSON → `code`, `deepLinkUrl`, `expiresAt`
- `unlink` succeeds on HTTP 204

**ViewModel (`commonTest`):**

- Lazy load → Unlinked / Linked from status
- Toggle on → createLink → LinkPending with code/url
- Poll → Linked when status becomes linked
- Toggle off → ConfirmUnlink; cancel restores Linked without unlink
- Confirm → unlink → Unlinked
- createLink failure → Unlinked + Error

## Out of scope

- Mobile / iOS / Android Compose screens
- Telegram webhook / bot message templates
- Hardcoding bot username beyond product string `@drivebit_bot` in the instruction copy (deep link comes from API)
- Separate notifications settings page
- Changing email notification behavior

## Implementation order

1. Network DTOs + service + tests  
2. ViewModel + tests  
3. Profile UI + DI wiring  
4. Manual check on Profile: link flow + unlink confirm
