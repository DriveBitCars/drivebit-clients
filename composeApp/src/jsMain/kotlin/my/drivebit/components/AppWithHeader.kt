package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.viewmodels.ButterViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.koin.compose.koinInject

@Composable
fun AppWithHeader(content: @Composable () -> Unit) {
    val butterViewModel: ButterViewModel = koinInject()

    AppContainer {
        HeaderRow {
            Logo()

            Row(
                alignItems = AlignItems.Center,
            ) {
                CityDisplay()
                MenuUserButton(butterViewModel::onClick)
            }

            ButterMenu()
        }

        content()
    }
}
