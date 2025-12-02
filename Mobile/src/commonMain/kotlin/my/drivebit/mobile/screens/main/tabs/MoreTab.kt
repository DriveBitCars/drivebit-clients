package my.drivebit.mobile.screens.main.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import my.drivebit.mobile.components.ButterMenu
import my.drivebit.ui.icons.Icons
import my.drivebit.ui.theme.DrivebitTheme
import my.drivebit.viewmodels.ButterViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

object MoreTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 4u,
                title = "Еще",
                icon = rememberVectorPainter(Icons.MoreIcon),
            )
        }

    @Composable
    override fun Content() {
        val butterViewModel: ButterViewModel = koinScreenModel()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Еще",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Button(onClick = { butterViewModel.onClick() }) {
                    Text("Открыть меню")
                }
            }

            ButterMenu()
        }
    }
}

@Composable
@Preview
fun MoreTabPreview() {
    DrivebitTheme {
        MoreTab.Content()
    }
}
