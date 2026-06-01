package my.drivebit.shell

import androidx.compose.runtime.Composable
import my.drivebit.components.AppContainer
import my.drivebit.components.ButterMenu
import my.drivebit.components.CityDisplay
import my.drivebit.components.MenuUserButton
import my.drivebit.components.ResponsiveContainer
import my.drivebit.viewmodels.ButterViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun HeaderInteractiveCompose() {
    val butterViewModel: ButterViewModel = koinInject()
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(12.px)
            property("position", "relative")
        }
    }) {
        CityDisplay()
        MenuUserButton(butterViewModel::onClick)
    }
    ButterMenu()
}

@Composable
fun PageContentShell(content: @Composable () -> Unit) {
    ResponsiveContainer { isMobile ->
        AppContainer(isMobile = isMobile) {
            content()
        }
    }
}

@Composable
fun AppWithHeader(content: @Composable () -> Unit) = PageContentShell(content)
