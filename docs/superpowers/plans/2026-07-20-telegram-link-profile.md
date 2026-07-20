# Telegram Link in Profile Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let logged-in users link/unlink Telegram for notifications from web Profile via checkbox, deep link button, fallback code, and unlink confirm.

**Architecture:** New authorized Network service `TelegramNotifications` wraps `GET/POST Notifications/Telegram/link*` and `POST .../unlink`. Dedicated `TelegramLinkViewModel` owns status, link-pending (code + deepLink), poll-to-linked, and unlink confirm. `ProfilePage` renders a lazy-loaded block; no logic in Compose beyond intents and displaying state.

**Tech Stack:** Kotlin Multiplatform, Ktor MockEngine, kotlinx-coroutines-test, kotlin.test, Koin, Compose HTML (`DrivebitWeb` / `profileApp`)

**Spec:** `docs/superpowers/specs/2026-07-20-telegram-link-profile-design.md`

## Global Constraints

- Web Profile only — Network + CommonViewModels + DrivebitWeb `ProfilePage` (+ DI)
- Checkbox copy: `Получать уведомления в телеграм`
- Button copy: `Привязать телеграм`
- Fallback copy: `Если кнопка не работает, то отправьте код {code} телеграм боту @drivebit_bot` with link `https://t.me/drivebit_bot`
- Unlink popup: `Вы перестанете получать уведомления в телеграм` — **Ок** / **Отмена**
- `unlink` must treat HTTP **204** as success (`consumeResponse`)
- Iron Law: failing test first, then minimal implementation
- No comments unless required for clarity in plan samples
- Stage new project files with `git add` as created; commit only at each task’s commit step

---

## File structure

| File | Role |
|------|------|
| `Network/.../services/TelegramNotifications.kt` | Interface, DTOs, `TelegramNotificationsImpl` |
| `Network/.../di/NetworkModule.kt` | Register `single<TelegramNotifications>` |
| `Network/.../TelegramNotificationsTest.kt` | MockEngine tests for status/link/unlink |
| `CommonViewModels/.../TelegramLinkViewModel.kt` | States, intents, poll |
| `CommonViewModels/.../TelegramLinkViewModelTest.kt` | ViewModel unit tests |
| `CommonViewModels/.../di/ViewModelsModule.kt` | `factory` or `single` for ViewModel |
| `DrivebitWeb/.../screens/ProfilePage.kt` | Telegram block UI |
| `DrivebitWeb/.../components/TelegramUnlinkConfirmDialog.kt` (optional extract) | Confirm overlay |

---

### Task 1: Network — `TelegramNotifications` service

**Files:**
- Create: `Network/src/commonMain/kotlin/my/drivebit/network/services/TelegramNotifications.kt`
- Create: `Network/src/commonTest/kotlin/my/drivebit/network/services/TelegramNotificationsTest.kt`
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/di/NetworkModule.kt`

**Interfaces:**
- Consumes: `HttpClient` (authorized), `DEFAULT_BASE_URL`, `parseResponse`, `consumeResponse`
- Produces:
  - `interface TelegramNotifications { suspend fun getLinkStatus(): TelegramBindingStatus; suspend fun createLink(): TelegramLinkStart; suspend fun unlink() }`
  - `@Serializable data class TelegramBindingStatus(val isLinked: Boolean, val telegramUsername: String? = null, val linkedAt: String? = null)`
  - `@Serializable data class TelegramLinkStart(val token: String, val code: String, val deepLinkUrl: String, val expiresAt: String)`

- [ ] **Step 1: Write the failing tests**

Create `TelegramNotificationsTest.kt`:

```kotlin
package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TelegramNotificationsTest {
    @Test
    fun `getLinkStatus parses isLinked false`() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.encodedPath.endsWith("/Notifications/Telegram/link/status"))
                    respond(
                        content = """{"isLinked":false}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val api = TelegramNotificationsImpl(HttpClient(engine))
            val status = api.getLinkStatus()
            assertFalse(status.isLinked)
        }

    @Test
    fun `createLink parses code and deepLinkUrl`() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.encodedPath.endsWith("/Notifications/Telegram/link"))
                    respond(
                        content =
                            """
                            {
                              "token":"tok",
                              "code":"489366",
                              "deepLinkUrl":"https://t.me/drivebit_bot?start=tok",
                              "expiresAt":"2026-07-20T12:00:00Z"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val api = TelegramNotificationsImpl(HttpClient(engine))
            val link = api.createLink()
            assertEquals("489366", link.code)
            assertEquals("https://t.me/drivebit_bot?start=tok", link.deepLinkUrl)
            assertEquals("tok", link.token)
        }

    @Test
    fun `unlink succeeds on HTTP 204`() =
        runTest {
            var called = false
            val engine =
                MockEngine { request ->
                    called = true
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.encodedPath.endsWith("/Notifications/Telegram/unlink"))
                    respond(
                        content = "",
                        status = HttpStatusCode.NoContent,
                    )
                }
            val api = TelegramNotificationsImpl(HttpClient(engine))
            api.unlink()
            assertTrue(called)
        }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :Network:cleanTest :Network:allTests --tests "my.drivebit.network.services.TelegramNotificationsTest"`

Expected: FAIL (unresolved `TelegramNotificationsImpl` / types)

- [ ] **Step 3: Minimal implementation**

Create `TelegramNotifications.kt`:

```kotlin
package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.consumeResponse
import my.drivebit.network.parseResponse

interface TelegramNotifications {
    suspend fun getLinkStatus(): TelegramBindingStatus

    suspend fun createLink(): TelegramLinkStart

    suspend fun unlink()
}

@Serializable
data class TelegramBindingStatus(
    val isLinked: Boolean,
    val telegramUsername: String? = null,
    val linkedAt: String? = null,
)

@Serializable
data class TelegramLinkStart(
    val token: String,
    val code: String,
    val deepLinkUrl: String,
    val expiresAt: String,
)

class TelegramNotificationsImpl(
    private val httpClient: HttpClient,
) : TelegramNotifications {
    override suspend fun getLinkStatus(): TelegramBindingStatus {
        val response = httpClient.get("${DEFAULT_BASE_URL}Notifications/Telegram/link/status")
        return response.parseResponse()
    }

    override suspend fun createLink(): TelegramLinkStart {
        val response = httpClient.post("${DEFAULT_BASE_URL}Notifications/Telegram/link")
        return response.parseResponse()
    }

    override suspend fun unlink() {
        val response = httpClient.post("${DEFAULT_BASE_URL}Notifications/Telegram/unlink")
        response.consumeResponse()
    }
}
```

Register in `NetworkModule.kt` (after `User` single is fine):

```kotlin
import my.drivebit.network.services.TelegramNotifications
import my.drivebit.network.services.TelegramNotificationsImpl

// inside module { ... }
single<TelegramNotifications> {
    TelegramNotificationsImpl(get(named("authorized")))
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :Network:allTests --tests "my.drivebit.network.services.TelegramNotificationsTest"`

Expected: PASS (exit 0)

- [ ] **Step 5: Commit**

```bash
git add \
  Network/src/commonMain/kotlin/my/drivebit/network/services/TelegramNotifications.kt \
  Network/src/commonTest/kotlin/my/drivebit/network/services/TelegramNotificationsTest.kt \
  Network/src/commonMain/kotlin/my/drivebit/network/di/NetworkModule.kt
git commit -m "$(cat <<'EOF'
Add TelegramNotifications Network client for link status and unlink.

EOF
)"
```

---

### Task 2: `TelegramLinkViewModel` (TDD)

**Files:**
- Create: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/TelegramLinkViewModel.kt`
- Create: `CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/TelegramLinkViewModelTest.kt`
- Modify: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/di/ViewModelsModule.kt`

**Interfaces:**
- Consumes: `TelegramNotifications` from Task 1
- Produces:
  - `sealed interface TelegramLinkUiState` with `Loading`, `Unlinked`, `LinkPending(code, deepLinkUrl, expiresAt)`, `Linked(telegramUsername: String?)`, `ConfirmUnlink(previous: Linked)`, `Error(message: String)` (Error may wrap display over last successful shape via separate fields if needed — keep checkbox derived helpers on state)
  - `interface TelegramLinkViewModel { val uiState: StateFlow<TelegramLinkUiState>; fun loadStatus(); fun onCheckboxChanged(checked: Boolean); fun confirmUnlink(); fun cancelUnlink() }`
  - Constructor: `TelegramLinkViewModelImpl(api, scope, pollIntervalMs = 2000L)` — inject `CoroutineScope` + `StandardTestDispatcher` in tests; poll only while `LinkPending`

Helper on states for UI:

```kotlin
fun TelegramLinkUiState.isCheckboxChecked(): Boolean =
    when (this) {
        is TelegramLinkUiState.Linked,
        is TelegramLinkUiState.LinkPending,
        is TelegramLinkUiState.ConfirmUnlink,
        -> true
        else -> false
    }

fun TelegramLinkUiState.isCheckboxEnabled(): Boolean =
    this !is TelegramLinkUiState.Loading
```

- [ ] **Step 1: Write the failing tests**

Create `TelegramLinkViewModelTest.kt` with a fake:

```kotlin
package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.TelegramBindingStatus
import my.drivebit.network.services.TelegramLinkStart
import my.drivebit.network.services.TelegramNotifications
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeTelegramNotifications : TelegramNotifications {
    var status: TelegramBindingStatus = TelegramBindingStatus(isLinked = false)
    var linkResult: TelegramLinkStart =
        TelegramLinkStart(
            token = "t",
            code = "123456",
            deepLinkUrl = "https://t.me/drivebit_bot?start=t",
            expiresAt = "2099-01-01T00:00:00Z",
        )
    var createLinkError: Throwable? = null
    var unlinkCalls = 0
    var statusCalls = 0

    override suspend fun getLinkStatus(): TelegramBindingStatus {
        statusCalls++
        return status
    }

    override suspend fun createLink(): TelegramLinkStart {
        createLinkError?.let { throw it }
        return linkResult
    }

    override suspend fun unlink() {
        unlinkCalls++
        status = TelegramBindingStatus(isLinked = false)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramLinkViewModelTest {
    @Test
    fun `loadStatus sets Unlinked when not linked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            assertIs<TelegramLinkUiState.Unlinked>(vm.uiState.value)
        }

    @Test
    fun `loadStatus sets Linked when isLinked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true, telegramUsername = "u")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            val linked = assertIs<TelegramLinkUiState.Linked>(vm.uiState.value)
            assertEquals("u", linked.telegramUsername)
        }

    @Test
    fun `toggle on creates link and enters LinkPending`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            vm.onCheckboxChanged(true)
            advanceUntilIdle()
            val pending = assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
            assertEquals("123456", pending.code)
            assertEquals("https://t.me/drivebit_bot?start=t", pending.deepLinkUrl)
        }

    @Test
    fun `poll moves LinkPending to Linked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            vm.onCheckboxChanged(true)
            advanceUntilIdle()
            assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
            api.status = TelegramBindingStatus(isLinked = true, telegramUsername = "bot_user")
            advanceTimeBy(2_000L)
            advanceUntilIdle()
            assertIs<TelegramLinkUiState.Linked>(vm.uiState.value)
        }

    @Test
    fun `toggle off shows ConfirmUnlink without calling unlink`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true)
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            vm.onCheckboxChanged(false)
            advanceUntilIdle()
            assertIs<TelegramLinkUiState.ConfirmUnlink>(vm.uiState.value)
            assertEquals(0, api.unlinkCalls)
        }

    @Test
    fun `cancelUnlink restores Linked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true, telegramUsername = "u")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            vm.onCheckboxChanged(false)
            advanceUntilIdle()
            vm.cancelUnlink()
            advanceUntilIdle()
            assertIs<TelegramLinkUiState.Linked>(vm.uiState.value)
            assertEquals(0, api.unlinkCalls)
        }

    @Test
    fun `confirmUnlink calls unlink and becomes Unlinked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true)
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            vm.onCheckboxChanged(false)
            advanceUntilIdle()
            vm.confirmUnlink()
            advanceUntilIdle()
            assertEquals(1, api.unlinkCalls)
            assertIs<TelegramLinkUiState.Unlinked>(vm.uiState.value)
        }

    @Test
    fun `createLink failure returns to Unlinked with Error`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val api =
                FakeTelegramNotifications().apply {
                    createLinkError = NetworkException(HttpStatusCode.ServiceUnavailable, "Telegram bot is not configured")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = CoroutineScope(dispatcher),
                    pollIntervalMs = 2_000L,
                )
            vm.loadStatus()
            advanceUntilIdle()
            vm.onCheckboxChanged(true)
            advanceUntilIdle()
            val err = assertIs<TelegramLinkUiState.Error>(vm.uiState.value)
            assertTrue(err.message.isNotBlank())
            assertEquals(false, err.checkboxChecked)
        }
}
```

`Error` shape:

```kotlin
data class Error(
    val message: String,
    val checkboxChecked: Boolean = false,
) : TelegramLinkUiState
```

Failed `createLink` sets `checkboxChecked = false`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :CommonViewModels:cleanTest :CommonViewModels:allTests --tests "my.drivebit.viewmodels.TelegramLinkViewModelTest"`

Expected: FAIL (types missing)

- [ ] **Step 3: Minimal ViewModel implementation**

Create `TelegramLinkViewModel.kt` implementing:

- `loadStatus()` → Loading then Linked/Unlinked/Error
- `onCheckboxChanged(true)` from Unlinked/Error → createLink → LinkPending; start poll job (`while` delay `pollIntervalMs`, get status; if linked → Linked and cancel job)
- `onCheckboxChanged(false)` from Linked → ConfirmUnlink(previous)
- `onCheckboxChanged(false)` from LinkPending → cancel poll, stay or go Unlinked without API (product: user abandoned pending — go Unlinked, cancel poll; do not call unlink)
- `cancelUnlink()` → restore `previous`
- `confirmUnlink()` → unlink → Unlinked; on failure → Error with `checkboxChecked = true` (or back to Linked + message)
- Use `ErrorHandler.extractErrorMessage` like `EditProfileViewModelImpl` if available in module

Cancel poll on Linked, Unlinked, ConfirmUnlink path, and ViewModel clear if any.

- [ ] **Step 4: Register DI**

In `ViewModelsModule.kt`:

```kotlin
import my.drivebit.network.services.TelegramNotifications
import my.drivebit.viewmodels.TelegramLinkViewModel
import my.drivebit.viewmodels.TelegramLinkViewModelImpl

factory<TelegramLinkViewModel> {
    TelegramLinkViewModelImpl(
        api = get<TelegramNotifications>(),
    )
}
```

Default scope inside impl: `CoroutineScope(SupervisorJob() + Dispatchers.Default)` like other VMs.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :CommonViewModels:allTests --tests "my.drivebit.viewmodels.TelegramLinkViewModelTest"`

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add \
  CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/TelegramLinkViewModel.kt \
  CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/TelegramLinkViewModelTest.kt \
  CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/di/ViewModelsModule.kt
git commit -m "$(cat <<'EOF'
Add TelegramLinkViewModel for profile link and unlink flow.

EOF
)"
```

---

### Task 3: Profile UI — checkbox, button, code, confirm

**Files:**
- Modify: `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/ProfilePage.kt`
- Create (optional): `DrivebitWeb/src/jsMain/kotlin/my/drivebit/components/TelegramUnlinkConfirmDialog.kt`

**Interfaces:**
- Consumes: `TelegramLinkViewModel`, `TelegramLinkUiState`, helpers `isCheckboxChecked` / `isCheckboxEnabled`
- Produces: visible Telegram block on Profile success state only

- [ ] **Step 1: Wire ViewModel on Profile**

Inside `ProfileState.Success` column (after password row), inject:

```kotlin
val telegramLinkViewModel: TelegramLinkViewModel = koinInject()
val telegramState by telegramLinkViewModel.uiState.collectAsState()

LaunchedEffect(Unit) {
    telegramLinkViewModel.loadStatus()
}
```

- [ ] **Step 2: Render checkbox + pending UI**

Pattern after `TermsConsentCheckbox` (native `InputType.Checkbox`):

- Label: `Получать уведомления в телеграм`
- `checked = telegramState.isCheckboxChecked()` (or `Error.checkboxChecked`)
- `disabled` when `!telegramState.isCheckboxEnabled()`
- `onAcceptedChange` → `telegramLinkViewModel.onCheckboxChanged(checked)`

When `telegramState is LinkPending`:

- `LinkButton("Привязать телеграм") { window.open(pending.deepLinkUrl, "_blank") }`
- Instruction `Div`/`Text` with code and clickable `A` to `https://t.me/drivebit_bot` (label `@drivebit_bot`)

When `Error`: `TextError(message)` under the block.

When `ConfirmUnlink`: show overlay/dialog:

- Text: `Вы перестанете получать уведомления в телеграм`
- Buttons: `Ок` → `confirmUnlink()`, `Отмена` → `cancelUnlink()`

Simple full-screen/semi-opaque `Div` overlay is enough (match existing dialog density if any; otherwise minimal).

- [ ] **Step 3: Manual / compile check**

Run: `./gradlew :DrivebitWeb:compileKotlinJs :CommonViewModels:allTests :Network:allTests --tests "*Telegram*"`

Expected: compile OK; Telegram unit tests PASS

- [ ] **Step 4: Commit**

```bash
git add \
  DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/ProfilePage.kt \
  DrivebitWeb/src/jsMain/kotlin/my/drivebit/components/TelegramUnlinkConfirmDialog.kt
git commit -m "$(cat <<'EOF'
Show Telegram link controls on Profile page.

EOF
)"
```

(Omit dialog path from `git add` if inlined into `ProfilePage`.)

---

### Task 4: Spec self-check + branch hygiene

**Files:** none required beyond fixes if gaps found

- [ ] **Step 1: Spec coverage checklist**

Confirm each spec row has code:

| Spec item | Task |
|-----------|------|
| Lazy GET status | Task 2 + 3 `LaunchedEffect` |
| Checkbox ↔ isLinked | Task 2/3 |
| POST link → button + code + bot link | Task 2/3 |
| Poll to Linked | Task 2 |
| Unlink confirm Ok/Cancel | Task 2/3 |
| unlink 204 | Task 1 |
| Webhook out of scope | — |

- [ ] **Step 2: Fix any gap found in Step 1** (commit with focused message if needed)

- [ ] **Step 3: Final verification**

Run: `./gradlew :Network:allTests --tests "my.drivebit.network.services.TelegramNotificationsTest" :CommonViewModels:allTests --tests "my.drivebit.viewmodels.TelegramLinkViewModelTest"`

Expected: PASS, exit 0

---

## Self-review (plan vs spec)

1. **Spec coverage:** status / link / unlink / UI copy / confirm / lazy load / poll / 204 — all mapped to Tasks 1–3. Webhook/mobile explicitly out of scope.
2. **Placeholders:** none; concrete paths, signatures, test bodies, Gradle commands.
3. **Type consistency:** `TelegramBindingStatus` / `TelegramLinkStart` / `TelegramLinkUiState.*` names aligned across tasks; `unlink()` void; poll via `pollIntervalMs`.

---

## Execution handoff

Plan complete and saved to `docs/superpowers/plans/2026-07-20-telegram-link-profile.md`. Two execution options:

**1. Subagent-Driven (recommended)** — fresh subagent per task, review between tasks  
**2. Inline Execution** — execute tasks in this session with checkpoints  

Which approach?
