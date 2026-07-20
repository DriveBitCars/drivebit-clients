package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import my.drivebit.network.services.TelegramNotifications

sealed interface TelegramLinkUiState {
    data object Loading : TelegramLinkUiState

    data object Unlinked : TelegramLinkUiState

    data class LinkPending(
        val code: String,
        val deepLinkUrl: String,
        val expiresAt: String,
    ) : TelegramLinkUiState

    data class Linked(
        val telegramUsername: String?,
    ) : TelegramLinkUiState

    data class ConfirmUnlink(
        val previous: Linked,
    ) : TelegramLinkUiState

    data class Error(
        val message: String,
        val checkboxChecked: Boolean = false,
    ) : TelegramLinkUiState
}

fun TelegramLinkUiState.isCheckboxChecked(): Boolean =
    when (this) {
        is TelegramLinkUiState.Linked,
        is TelegramLinkUiState.LinkPending,
        is TelegramLinkUiState.ConfirmUnlink,
        -> true
        is TelegramLinkUiState.Error -> checkboxChecked
        else -> false
    }

fun TelegramLinkUiState.isCheckboxEnabled(): Boolean =
    this !is TelegramLinkUiState.Loading

interface TelegramLinkViewModel {
    val uiState: StateFlow<TelegramLinkUiState>

    fun loadStatus()

    fun onCheckboxChanged(checked: Boolean)

    fun confirmUnlink()

    fun cancelUnlink()
}

class TelegramLinkViewModelImpl(
    private val api: TelegramNotifications,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val pollIntervalMs: Long = 2_000L,
) : TelegramLinkViewModel {
    private val _uiState = MutableStateFlow<TelegramLinkUiState>(TelegramLinkUiState.Loading)
    override val uiState: StateFlow<TelegramLinkUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    override fun loadStatus() {
        coroutineScope.launch {
            _uiState.value = TelegramLinkUiState.Loading
            runCatching { api.getLinkStatus() }
                .onSuccess { status ->
                    _uiState.value =
                        if (status.isLinked) {
                            TelegramLinkUiState.Linked(status.telegramUsername)
                        } else {
                            TelegramLinkUiState.Unlinked
                        }
                }.onFailure { e ->
                    _uiState.value =
                        TelegramLinkUiState.Error(
                            message =
                                ErrorHandler.extractErrorMessage(
                                    exception = e,
                                    defaultNetworkError = "Ошибка сети",
                                    defaultGenericError = "Не удалось загрузить статус Telegram",
                                ),
                            checkboxChecked = false,
                        )
                }
        }
    }

    override fun onCheckboxChanged(checked: Boolean) {
        val current = _uiState.value
        when {
            checked && (current is TelegramLinkUiState.Unlinked || current is TelegramLinkUiState.Error) -> {
                coroutineScope.launch {
                    runCatching { api.createLink() }
                        .onSuccess { link ->
                            _uiState.value =
                                TelegramLinkUiState.LinkPending(
                                    code = link.code,
                                    deepLinkUrl = link.deepLinkUrl,
                                    expiresAt = link.expiresAt,
                                )
                            startPolling()
                        }.onFailure { e ->
                            cancelPolling()
                            _uiState.value =
                                TelegramLinkUiState.Error(
                                    message =
                                        ErrorHandler.extractErrorMessage(
                                            exception = e,
                                            defaultNetworkError = "Ошибка сети",
                                            defaultGenericError = "Не удалось создать ссылку Telegram",
                                        ),
                                    checkboxChecked = false,
                                )
                        }
                }
            }
            !checked && current is TelegramLinkUiState.Linked -> {
                cancelPolling()
                _uiState.value = TelegramLinkUiState.ConfirmUnlink(previous = current)
            }
            !checked && current is TelegramLinkUiState.LinkPending -> {
                cancelPolling()
                _uiState.value = TelegramLinkUiState.Unlinked
            }
        }
    }

    override fun confirmUnlink() {
        val current = _uiState.value
        if (current !is TelegramLinkUiState.ConfirmUnlink) return
        coroutineScope.launch {
            cancelPolling()
            runCatching { api.unlink() }
                .onSuccess {
                    _uiState.value = TelegramLinkUiState.Unlinked
                }.onFailure { e ->
                    _uiState.value =
                        TelegramLinkUiState.Error(
                            message =
                                ErrorHandler.extractErrorMessage(
                                    exception = e,
                                    defaultNetworkError = "Ошибка сети",
                                    defaultGenericError = "Не удалось отвязать Telegram",
                                ),
                            checkboxChecked = true,
                        )
                }
        }
    }

    override fun cancelUnlink() {
        val current = _uiState.value
        if (current is TelegramLinkUiState.ConfirmUnlink) {
            _uiState.value = current.previous
        }
    }

    private fun startPolling() {
        cancelPolling()
        pollJob =
            coroutineScope.launch {
                while (isActive) {
                    delay(pollIntervalMs)
                    if (_uiState.value !is TelegramLinkUiState.LinkPending) break
                    runCatching { api.getLinkStatus() }
                        .onSuccess { status ->
                            if (status.isLinked) {
                                cancelPolling()
                                _uiState.value = TelegramLinkUiState.Linked(status.telegramUsername)
                            }
                        }
                }
            }
    }

    private fun cancelPolling() {
        pollJob?.cancel()
        pollJob = null
    }
}
