package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import my.drivebit.components.AppContainer
import my.drivebit.components.ButterMenu
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.Logo
import my.drivebit.components.MenuUserButton
import my.drivebit.components.filterButton
import my.drivebit.navigation.Navigation
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.ProfilePage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Suppress("FunctionName")
actual fun App() {
    KoinApplication(application = {
        modules(
            storageModule,
            webModule,
            commonViewModelsModule,
        )
    }) {
        Navigation { currentPath ->
            when {
                currentPath.startsWith("/verify-otp") -> {
                    OtpVerificationPage()
                }
                currentPath == "/login-by-phone" -> {
                    LoginPage(viewModelQualifier = named("phone"))
                }
                currentPath == "/login-by-mail" -> {
                    LoginPage(viewModelQualifier = named("email"))
                }
                currentPath == "/profile" -> {
                    ProfilePage()
                }
                currentPath.startsWith("/edit-name") -> {
                    EditNamePage(currentPath)
                }
                else -> {
                    HomePage()
                }
            }
        }
    }
}

@Composable
fun HomePage() {
    val filterViewModel: FiltersViewModel = koinInject()
    val butterViewModel: ButterViewModel = koinInject()

    val state = filterViewModel.state.collectAsState()
    val filters = state.value.filters
    val selected = state.value.selected

    AppContainer {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(20.px)
            }
        }) {
            Logo()

            MenuUserButton(butterViewModel::onClick)

            ButterMenu()
        }

        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            FilterBackgroundImage(
                backgroundIconUrl = "images/searchbackground/car${filter.backgroundIcon}.jpg",
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
}
