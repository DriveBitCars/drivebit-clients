package my.drivebit.mobile.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.viewmodels.CreateReviewViewModel
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

data class LeaveReviewScreen(
    val carId: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koinScope = currentKoinScope()
        val viewModel: CreateReviewViewModel =
            remember(carId) {
                koinScope.get<CreateReviewViewModel>(parameters = { parametersOf(carId) })
            }
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.success) {
            if (state.success) {
                navigator.pop()
                viewModel.consumeSuccess()
            }
        }

        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = "Отзыв",
                    onBackClick = { navigator.pop() },
                )
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Оцените поездку",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (star in 1..5) {
                        val selected = state.stars == star
                        TextButton(
                            onClick = { viewModel.setStars(star) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = star.toString(),
                                color =
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.text,
                    onValueChange = viewModel::setText,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Комментарий") },
                    minLines = 4,
                    maxLines = 8,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                )
                state.error?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = { viewModel.submit() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Отправить")
                    }
                }
            }
        }
    }
}
