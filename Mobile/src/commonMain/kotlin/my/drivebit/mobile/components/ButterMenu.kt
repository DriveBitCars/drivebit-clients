package my.drivebit.mobile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import my.drivebit.mobile.screens.profile.ProfileScreen
import my.drivebit.viewmodels.ButterState
import my.drivebit.viewmodels.ButterViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ButterMenu() {
    val butterViewModel: ButterViewModel = koinScreenModel()
    val state by butterViewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    when (val currentState = state) {
        is ButterState.Idle -> {}
        is ButterState.Opened -> {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                ) {
                    currentState.model.forEach { item ->
                        val onClick =
                            when (item.text) {
                                "Мой профиль" -> {
                                    {
                                        item.onClick()
                                        navigator.push(ProfileScreen())
                                    }
                                }
                                else -> item.onClick
                            }

                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier =
                                Modifier
                                    .clickable { onClick() }
                                    .padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
