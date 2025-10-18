package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.Logo
import my.drivebit.components.MenuUserButton
import my.drivebit.components.filterButton
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.di.commonViewModelsModule
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Suppress("FunctionName")
actual fun App() {
    KoinApplication(application = {
        modules(
            storageModule,
            commonViewModelsModule,
        )
    }) {
        appContent()
    }
}

@Composable
fun appContent() {
    // DrivebitTheme {
    val filterViewModel: FiltersViewModel = koinInject()
    val state = filterViewModel.state.collectAsState()
    val filters = state.value.filters
    val selected = state.value.selected

    Div({
        style {
            padding(20.px, 40.px)
            fontFamily("system-ui, -apple-system, sans-serif")
            property("min-width", "320px")
            property("max-width", "1200px")
            property("margin", "0 auto")
            property("box-sizing", "border-box")
        }
        classes("app-container")
    }) {
        // Header with logo and menu button
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(20.px)
            }
        }) {
            Logo()

            MenuUserButton {
                // TODO: Implement this
            }
        }

        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            FilterBackgroundImage(
                backgroundIconUrl = "https://antonbutov.github.io/drivebit-clients/images/searchbackground/car${filter.backgroundIcon}.jpg",
            )
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                gap(12.px)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                marginBottom(20.px)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            filters.forEach { filter ->
                filterButton(
                    filter = filter,
                    isSelected = filter.title == selected,
                    onClick = { filterViewModel.onSelect(filter.title) },
                )
            }
        }
    }
    // }
}
