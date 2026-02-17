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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import my.drivebit.network.services.BookingDTO
import my.drivebit.ui.icons.Icons
import my.drivebit.ui.theme.DrivebitTheme
import my.drivebit.viewmodels.MyBookingsAsRenterViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

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
        val viewModel: MyBookingsAsRenterViewModel = koinInject()
        val bookings by viewModel.bookings.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadBookings()
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
            Spacer(modifier = Modifier.height(16.dp))
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(bookings, key = { it.id }) { booking ->
                            BookingItemCard(booking = booking)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BookingItemCard(booking: BookingDTO) {
    val carName =
        listOfNotNull(booking.carBrandName, booking.carModelName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Автомобиль" }
    val dateRange = "${booking.startAt.take(10)} — ${booking.endAt.take(10)}"

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
                text = booking.status,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            )
        }
    }
}
