package my.drivebit.mobile.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import my.drivebit.mobile.screens.main.tabs.FavoritesTab
import my.drivebit.mobile.screens.main.tabs.InboxTab
import my.drivebit.mobile.screens.main.tabs.MoreTab
import my.drivebit.mobile.screens.main.tabs.SearchTab
import my.drivebit.mobile.screens.main.tabs.TripsTab
import my.drivebit.ui.theme.ColorsDriveBit
import my.drivebit.ui.theme.DrivebitTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BottomNavigationBar(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs =
        listOf(
            SearchTab,
            FavoritesTab,
            TripsTab,
            InboxTab,
            MoreTab,
        )

    BottomAppBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val isSelected = currentTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onTabSelected(tab) },
                ) {
                    Image(
                        modifier = Modifier.size(32.dp),
                        painter = rememberAsyncImagePainter(
                            model = when (tab) {
                                is SearchTab -> "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg"
                                is FavoritesTab -> "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg"
                                is TripsTab -> "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg"
                                is InboxTab -> "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg"
                                is MoreTab -> "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg"
                                else -> "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg"
                            }
                        ),
                        contentDescription = tab.options.title,
                        colorFilter = ColorFilter.tint(
                            if (isSelected) ColorsDriveBit.BlueRed else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                    )
                    Text(
                        text = tab.options.title,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) ColorsDriveBit.BlueRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun BottomNavigationBarPreview() {
    DrivebitTheme {
        BottomNavigationBar(
            currentTab = SearchTab,
            onTabSelected = { },
        )
    }
}
