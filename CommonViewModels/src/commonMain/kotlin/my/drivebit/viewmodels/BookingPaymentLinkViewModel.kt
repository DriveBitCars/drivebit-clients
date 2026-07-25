package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.network.services.BookingCheckoutKind
import my.drivebit.network.services.PayBookingResult
import my.drivebit.network.services.Payment

sealed interface BookingPaymentLinkUiState {
    data object Idle : BookingPaymentLinkUiState

    data object Loading : BookingPaymentLinkUiState

    data class OpenCheckout(
        val url: String,
    ) : BookingPaymentLinkUiState

    data class FinishedWithMessage(
        val text: String,
        val alreadyPaid: Boolean = false,
    ) : BookingPaymentLinkUiState
}

interface BookingPaymentLinkViewModel {
    val uiState: StateFlow<BookingPaymentLinkUiState>

    fun startCheckout(
        returnUrl: String,
        failUrl: String,
        kind: BookingCheckoutKind = BookingCheckoutKind.FullOrBalance,
    )
}

class BookingPaymentLinkViewModelImpl(
    private val payment: Payment,
    private val bookingId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BookingPaymentLinkViewModel {
    private val _uiState = MutableStateFlow<BookingPaymentLinkUiState>(BookingPaymentLinkUiState.Idle)
    override val uiState: StateFlow<BookingPaymentLinkUiState> = _uiState.asStateFlow()

    private var payJob: Job? = null

    override fun startCheckout(
        returnUrl: String,
        failUrl: String,
        kind: BookingCheckoutKind,
    ) {
        if (bookingId.isBlank()) {
            _uiState.value = BookingPaymentLinkUiState.FinishedWithMessage("Не указан номер бронирования")
            return
        }
        if (payJob?.isActive == true) return
        _uiState.value = BookingPaymentLinkUiState.Loading
        payJob =
            coroutineScope.launch {
                when (
                    val result =
                        when (kind) {
                            BookingCheckoutKind.Prepayment ->
                                payment.registerBookingPrepayment(bookingId, returnUrl, failUrl)
                            BookingCheckoutKind.FullOrBalance ->
                                payment.registerBookingPayment(bookingId, returnUrl, failUrl)
                        }
                ) {
                    is PayBookingResult.Redirect ->
                        _uiState.value = BookingPaymentLinkUiState.OpenCheckout(result.url)
                    is PayBookingResult.AlreadyPaid -> {
                        val text =
                            result.message?.takeIf { it.isNotBlank() }
                                ?: "Оплата уже выполнена"
                        _uiState.value =
                            BookingPaymentLinkUiState.FinishedWithMessage(
                                text = text,
                                alreadyPaid = true,
                            )
                    }
                    is PayBookingResult.Failed ->
                        _uiState.value =
                            BookingPaymentLinkUiState.FinishedWithMessage(
                                text = result.message,
                                alreadyPaid = false,
                            )
                }
            }
    }
}
