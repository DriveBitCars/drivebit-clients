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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.delay
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.canShowSignContractAsOwner
import my.drivebit.ui.icons.Icons
import my.drivebit.ui.theme.DrivebitTheme
import my.drivebit.viewmodels.MyBookingsAsOwnerViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

object InboxTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 3u,
                title = "Мои сделки",
                icon = rememberVectorPainter(Icons.DealsIcon),
            )
        }

    @Composable
    override fun Content() {
        val viewModel: MyBookingsAsOwnerViewModel = koinInject()
        val bookings by viewModel.bookings.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        val actionInProgress by viewModel.actionInProgress.collectAsState()

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
                text = "Мои сделки",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
                        text = "У вас пока нет заявок на аренду",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(bookings, key = { it.id }) { booking ->
                            DealItemCard(
                                booking = booking,
                                isActionInProgress = booking.id in actionInProgress,
                                onConfirm = { viewModel.confirmBooking(booking.id) },
                                onDecline = { viewModel.declineBooking(booking.id) },
                                onSignContract = { viewModel.signContractAsOwner(booking.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DealItemCard(
    booking: BookingDTO,
    isActionInProgress: Boolean,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    onSignContract: () -> Unit = {},
) {
    val renterName = booking.renterName?.takeIf { it.isNotBlank() } ?: "Арендатор"
    val carName =
        listOfNotNull(booking.carBrandName, booking.carModelName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Автомобиль" }
    val dateRange = "${booking.startAt.take(10)} — ${booking.endAt.take(10)}"
    val canConfirmOrDecline =
        booking.status.equals("Pending", ignoreCase = true) ||
            booking.status.equals("AwaitingOwnerConfirmation", ignoreCase = true)
    val canSignContract = booking.canShowSignContractAsOwner()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = renterName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = carName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            if (canSignContract) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSignContract,
                    enabled = !isActionInProgress,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isActionInProgress) "Подписание..." else "Подписать договор")
                }
            }
            if (canConfirmOrDecline) {
                Spacer(modifier = Modifier.height(12.dp))
                if (isActionInProgress) {
                    Text(
                        text = "Обработка...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Подтвердить")
                        }
                        OutlinedButton(
                            onClick = onDecline,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Отклонить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun InboxTabPreview() {
    DrivebitTheme {
        InboxTab.Content()
    }
}
