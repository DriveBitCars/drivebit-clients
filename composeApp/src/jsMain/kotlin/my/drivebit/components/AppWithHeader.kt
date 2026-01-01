package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.viewmodels.ButterViewModel
import org.koin.compose.koinInject

@Composable
fun AppWithHeader(content: @Composable () -> Unit) {
    val butterViewModel: ButterViewModel = koinInject()

    AppContainer {
        HeaderRow {
            Logo()

            MenuUserButton(butterViewModel::onClick)

            ButterMenu()
        }

        content()
    }
}
