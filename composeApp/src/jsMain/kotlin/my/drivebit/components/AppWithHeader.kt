package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.viewmodels.ButterViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject
import org.w3c.dom.events.Event

@Composable
fun AppWithHeader(content: @Composable () -> Unit) {
    val butterViewModel: ButterViewModel = koinInject()
    var isMobile by remember { mutableStateOf(window.innerWidth <= 768) }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            isMobile = window.innerWidth <= 768
        }
        window.addEventListener("resize", listener)
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    AppContainer {
        if (isMobile) {
            Div({
                style {
                    marginBottom(12.px)
                }
            }) {
                Hero()
            }
        }

        HeaderRow {
            Logo()

            if (!isMobile) {
                Hero()
            }

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
