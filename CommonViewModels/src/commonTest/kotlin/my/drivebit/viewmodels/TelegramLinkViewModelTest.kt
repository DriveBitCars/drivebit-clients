package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.TelegramBindingStatus
import my.drivebit.network.services.TelegramLinkStart
import my.drivebit.network.services.TelegramNotifications
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
    var unlinkError: Throwable? = null
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
        unlinkError?.let { throw it }
        status = TelegramBindingStatus(isLinked = false)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramLinkViewModelTest {
    @Test
    fun `loadStatus sets Unlinked when not linked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                assertIs<TelegramLinkUiState.Unlinked>(vm.uiState.value)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `loadStatus sets Linked when isLinked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true, telegramUsername = "u")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                val linked = assertIs<TelegramLinkUiState.Linked>(vm.uiState.value)
                assertEquals("u", linked.telegramUsername)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `toggle on creates link and enters LinkPending`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(true)
                runCurrent()
                val pending = assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
                assertEquals("123456", pending.code)
                assertEquals("https://t.me/drivebit_bot?start=t", pending.deepLinkUrl)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `poll moves LinkPending to Linked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(true)
                runCurrent()
                assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
                api.status = TelegramBindingStatus(isLinked = true, telegramUsername = "bot_user")
                advanceTimeBy(2_000L)
                runCurrent()
                assertIs<TelegramLinkUiState.Linked>(vm.uiState.value)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `toggle off shows ConfirmUnlink without calling unlink`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true)
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(false)
                runCurrent()
                assertIs<TelegramLinkUiState.ConfirmUnlink>(vm.uiState.value)
                assertEquals(0, api.unlinkCalls)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `cancelUnlink restores Linked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true, telegramUsername = "u")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(false)
                runCurrent()
                vm.cancelUnlink()
                runCurrent()
                assertIs<TelegramLinkUiState.Linked>(vm.uiState.value)
                assertEquals(0, api.unlinkCalls)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `confirmUnlink calls unlink and becomes Unlinked`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true)
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(false)
                runCurrent()
                vm.confirmUnlink()
                runCurrent()
                assertEquals(1, api.unlinkCalls)
                assertIs<TelegramLinkUiState.Unlinked>(vm.uiState.value)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `toggle off from LinkPending abandons and stops polling`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api = FakeTelegramNotifications()
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(true)
                runCurrent()
                assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
                val statusCallsBeforeAbandon = api.statusCalls
                vm.onCheckboxChanged(false)
                runCurrent()
                assertIs<TelegramLinkUiState.Unlinked>(vm.uiState.value)
                assertEquals(0, api.unlinkCalls)
                api.status = TelegramBindingStatus(isLinked = true, telegramUsername = "bot_user")
                advanceTimeBy(2_000L)
                runCurrent()
                assertIs<TelegramLinkUiState.Unlinked>(vm.uiState.value)
                assertEquals(statusCallsBeforeAbandon, api.statusCalls)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `createLink failure returns to Unlinked with Error`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    createLinkError = NetworkException(HttpStatusCode.ServiceUnavailable, "Telegram bot is not configured")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(true)
                runCurrent()
                val err = assertIs<TelegramLinkUiState.Error>(vm.uiState.value)
                assertTrue(err.message.isNotBlank())
                assertEquals(false, err.checkboxChecked)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `confirmUnlink failure stays retryable via checkbox off`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    status = TelegramBindingStatus(isLinked = true, telegramUsername = "u")
                    unlinkError = NetworkException(HttpStatusCode.ServiceUnavailable, "unlink failed")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(false)
                runCurrent()
                vm.confirmUnlink()
                runCurrent()
                val err = assertIs<TelegramLinkUiState.Error>(vm.uiState.value)
                assertEquals(true, err.checkboxChecked)
                assertEquals("u", err.restoreLinked?.telegramUsername)
                assertEquals(1, api.unlinkCalls)

                vm.onCheckboxChanged(false)
                runCurrent()
                val confirm = assertIs<TelegramLinkUiState.ConfirmUnlink>(vm.uiState.value)
                assertEquals("u", confirm.previous.telegramUsername)
            } finally {
                scope.cancel()
            }
        }

    @Test
    fun `poll stops when expiresAt has passed`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val scope = CoroutineScope(Job() + dispatcher)
            val api =
                FakeTelegramNotifications().apply {
                    linkResult =
                        linkResult.copy(expiresAt = "2020-01-01T00:00:00Z")
                }
            val vm =
                TelegramLinkViewModelImpl(
                    api = api,
                    coroutineScope = scope,
                    pollIntervalMs = 2_000L,
                )
            try {
                vm.loadStatus()
                runCurrent()
                vm.onCheckboxChanged(true)
                runCurrent()
                assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
                val statusCallsAfterCreate = api.statusCalls

                advanceTimeBy(2_000L)
                runCurrent()
                assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
                assertEquals(statusCallsAfterCreate, api.statusCalls)

                advanceTimeBy(10_000L)
                runCurrent()
                assertIs<TelegramLinkUiState.LinkPending>(vm.uiState.value)
                assertEquals(statusCallsAfterCreate, api.statusCalls)
            } finally {
                scope.cancel()
            }
        }
}
