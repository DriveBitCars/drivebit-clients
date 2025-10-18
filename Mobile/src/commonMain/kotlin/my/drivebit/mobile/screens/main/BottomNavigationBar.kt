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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
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
                        painter = painterResource(
                            when (tab) {
                                is SearchTab -> "search_icon.xml"
                                is FavoritesTab -> "favorite_icon.xml"
                                is TripsTab -> "trip_icon.xml"
                                is InboxTab -> "inbox_icon.xml"
                                is MoreTab -> "more_icon.xml"
                                else -> "search_icon.xml"
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
