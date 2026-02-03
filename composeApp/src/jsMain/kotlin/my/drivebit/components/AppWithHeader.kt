package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.viewmodels.ButterViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun AppWithHeader(content: @Composable () -> Unit) {
    val butterViewModel: ButterViewModel = koinInject()

    ResponsiveContainer { isMobile ->
        AppContainer {
            if (isMobile) {
                Div({
                    style {
                        marginBottom(12.px)
                    }
                }) {
                    Hero(isMobile)
                }
            }

            HeaderRow {
                Logo()

                if (!isMobile) {
                    Hero(isMobile)
                }

                Row(
                    alignItems = AlignItems.Center,
                    gap = 12.px,
                ) {
                    CityDisplay()
                    MenuUserButton(butterViewModel::onClick)
                }

                ButterMenu()
            }

            content()
        }
    }
}
