package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import my.drivebit.components.AppContainer
import my.drivebit.components.ButterMenu
import my.drivebit.components.CenteredContent
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.HeaderRow
import my.drivebit.components.Logo
import my.drivebit.components.MenuUserButton
import my.drivebit.components.TextSmartHeader
import my.drivebit.components.filterButton
import my.drivebit.navigation.Navigation
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.resources.ImagePaths
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.ListYourCarPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.ProfilePage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Img
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Suppress("FunctionName")
actual fun App() {
    KoinApplication(application = {
        modules(
            storageModule,
            repositoriesModule,
            webModule,
            commonViewModelsModule,
        )
    }) {
        Navigation { currentPath ->
            when {
                currentPath == "/list-your-car" -> {
                    ListYourCarPage()
                }
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
                currentPath == "/change-email" -> {
                    ChangeEmailPage()
                }
                currentPath == "/change-phone" -> {
                    ChangePhonePage()
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
        HeaderRow {
            Logo()

            MenuUserButton(butterViewModel::onClick)

            ButterMenu()
        }

        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            FilterBackgroundImage(
                backgroundIconUrl = filter.backgroundIcon,
            )
        }

        FilterButtonsRow {
            filters.forEach { filter ->
                filterButton(
                    filter = filter,
                    isSelected = filter.title == selected,
                    onClick = { filterViewModel.onSelect(filter.title) },
                )
            }
        }

        CenteredContent {
            Img(
                src = ImagePaths.FIX_SVG,
                alt = "Coming soon",
                attrs = {
                    style {
                        property("max-width", "600px")
                        width(100.percent)
                        property("height", "auto")
                        marginBottom(20.px)
                    }
                },
            )
            TextSmartHeader("скоро...")
        }
    }
}
