package my.drivebit.mobile.screens.main.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.delay
import my.drivebit.mobile.screens.main.LeaveReviewScreen
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.canShowSignContractAsRenter
import my.drivebit.network.services.contractDownloadPageUrl
import my.drivebit.network.services.prepaymentButtonLabel
import my.drivebit.network.services.renterFullOrBalanceAmountRub
import my.drivebit.network.services.renterFullOrBalancePaymentLabel
import my.drivebit.network.services.statusAllowsContractDownload
import my.drivebit.network.services.statusAllowsRenterPayment
import my.drivebit.ui.icons.Icons
import my.drivebit.ui.theme.DrivebitTheme
import my.drivebit.viewmodels.ChatPayEffect
import my.drivebit.viewmodels.MyBookingsAsRenterViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

private const val MOBILE_PAYMENT_SUCCESS_URL = "https://drivebit.ru/payment-success"
private const val MOBILE_PAYMENT_FAIL_URL = "https://drivebit.ru/payment-failure"

object TripsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 2u,
                title = "Поездки",
                icon = rememberVectorPainter(Icons.TripIcon),
            )
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MyBookingsAsRenterViewModel = koinInject()
        val bookings by viewModel.bookings.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        val isPaying by viewModel.isPaying.collectAsState()
        val actionInProgress by viewModel.actionInProgress.collectAsState()
        val uriHandler = LocalUriHandler.current
        var payInfo by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            viewModel.payEffects.collect { effect ->
                when (effect) {
                    is ChatPayEffect.OpenCheckout -> uriHandler.openUri(effect.url)
                    is ChatPayEffect.ShowInfo -> payInfo = effect.text
                }
            }
        }

        LaunchedEffect(payInfo) {
            val msg = payInfo
            if (msg != null) {
                delay(6_000)
                payInfo = null
            }
        }

        LaunchedEffect(Unit) {
            viewModel.loadBookings()
            while (true) {
                delay(30_000)
                viewModel.refreshBookings()
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Text(
                text = "Мои бронирования",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            payInfo?.let { info ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            when {
                isLoading && bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                bookings.isEmpty() && error != null -> {
                    Text(
                        text = error ?: "Ошибка",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                bookings.isEmpty() -> {
                    Text(
                        text = "У вас пока нет бронирований",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    if (error != null) {
                        Text(
                            text = error ?: "Ошибка",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(bookings, key = { it.id }) { booking ->
                            BookingItemCard(
                                booking = booking,
                                isPaying = isPaying,
                                isActionInProgress = booking.id in actionInProgress,
                                onLeaveReview = {
                                    navigator.push(LeaveReviewScreen(carId = booking.carId))
                                },
                                onPay = {
                                    viewModel.payBooking(
                                        bookingId = booking.id,
                                        returnUrl = MOBILE_PAYMENT_SUCCESS_URL,
                                        failUrl = MOBILE_PAYMENT_FAIL_URL,
                                    )
                                },
                                onPrepay = {
                                    viewModel.prepayBooking(
                                        bookingId = booking.id,
                                        returnUrl = MOBILE_PAYMENT_SUCCESS_URL,
                                        failUrl = MOBILE_PAYMENT_FAIL_URL,
                                    )
                                },
                                onSignContract = { viewModel.signContractAsRenter(booking.id) },
                                onDownloadContract = {
                                    uriHandler.openUri(contractDownloadPageUrl(booking.id))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BookingItemCard(
    booking: BookingDTO,
    isPaying: Boolean = false,
    isActionInProgress: Boolean = false,
    onLeaveReview: () -> Unit,
    onPay: () -> Unit = {},
    onPrepay: () -> Unit = {},
    onSignContract: () -> Unit = {},
    onDownloadContract: () -> Unit = {},
) {
    val carName =
        listOfNotNull(booking.carBrandName, booking.carModelName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Автомобиль" }
    val dateRange = "${booking.startAt.take(10)} — ${booking.endAt.take(10)}"
    val canLeaveReview = booking.status.equals("Completed", ignoreCase = true)
    val canPay = booking.statusAllowsRenterPayment()
    val canDownloadContract = booking.statusAllowsContractDownload()
    val canSignContract = booking.canShowSignContractAsRenter()
    val fullPaymentLabel =
        "${booking.renterFullOrBalancePaymentLabel()} (${booking.renterFullOrBalanceAmountRub()} ₽)"
    val showActions = canPay || booking.canPayPrepayment || canLeaveReview || canDownloadContract || canSignContract

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = carName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${booking.totalAmount.toInt()} ₽",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = booking.statusTranslate?.takeIf { it.isNotBlank() } ?: booking.status,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (showActions) {
                Spacer(modifier = Modifier.height(12.dp))
                if (booking.canPayPrepayment) {
                    Button(
                        onClick = onPrepay,
                        enabled = !isPaying,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isPaying) "Загрузка…" else booking.prepaymentButtonLabel())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (canPay) {
                    Button(
                        onClick = onPay,
                        enabled = !isPaying,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isPaying) "Загрузка…" else fullPaymentLabel)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (canDownloadContract) {
                    Button(
                        onClick = onDownloadContract,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Договор")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (canSignContract) {
                    Button(
                        onClick = onSignContract,
                        enabled = !isActionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isActionInProgress) "Подписание..." else "Подписать договор")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (canLeaveReview) {
                    Button(
                        onClick = onLeaveReview,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Оставить отзыв")
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun TripsTabPreview() {
    DrivebitTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Text(
                text = "Мои бронирования",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            BookingItemCard(
                booking =
                    BookingDTO(
                        id = "1",
                        carId = "car-1",
                        carBrandName = "Toyota",
                        carModelName = "Camry",
                        renterId = "r1",
                        ownerId = "o1",
                        startAt = "2025-02-20T10:00:00Z",
                        endAt = "2025-02-22T10:00:00Z",
                        totalAmount = 5000.0,
                        status = "Confirmed",
                        createdAt = "2025-02-17T12:00:00Z",
                    ),
                onLeaveReview = {},
            )
        }
    }
}
