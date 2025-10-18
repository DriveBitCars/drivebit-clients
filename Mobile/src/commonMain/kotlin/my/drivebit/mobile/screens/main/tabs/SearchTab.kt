package my.drivebit.mobile.screens.main.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.AsyncImage
import drivebitclients.mobile.generated.resources.Res
import my.drivebit.ui.icons.Icons
import my.drivebit.ui.icons.Icons.SearchIcon
import my.drivebit.ui.theme.ColorsDriveBit
import my.drivebit.ui.theme.ColorsDriveBit.Black
import my.drivebit.ui.theme.DrivebitTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 0u,
                title = "Поиск",
                icon = rememberVectorPainter(Icons.SearchIcon),
            )
        }

    @Composable
    override fun Content() {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            AsyncImage(
                model = "https://i.etsystatic.com/47531348/r/il/0174f3/5611268998/il_800x800.5611268998_g62l.jpg",
                contentDescription = null,
            )
            AsyncImage(
                //   modifier = Modifier.size(32.dp),
                model = "https://antonbutov.github.io/drivebit-clients/images/filter-main/car.svg",

                contentDescription = "tab.options.title",
                colorFilter = ColorFilter.tint(
                    ColorsDriveBit.BlueRed
                )
                )
            Text(
                text = "Поиск",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
@Preview
fun SearchTabPreview() {
    DrivebitTheme {
        SearchTab.Content()
    }
}
