package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.viewmodels.MyCityViewModel
import my.drivebit.web.isCityHomePath
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun CityDisplay() {
    val myCityViewModel: MyCityViewModel = koinInject()
    val navigationController = LocalNavigationController.current
    val navigationState: NavigationState = koinInject()
    val currentPath by navigationState.currentPath.collectAsState()
    val cityName by myCityViewModel.myCity.collectAsState("")

    LaunchedEffect(Unit) {
        myCityViewModel.refresh()
    }

    LaunchedEffect(currentPath) {
        if (isCityHomePath(currentPath)) {
            myCityViewModel.refresh()
        }
    }

    if (cityName.isNotEmpty()) {
        Div({
            classes("drivebit-header-city")
            onClick {
                navigationController?.navigateTo("/my-city-selection")
            }
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(6.px)
                cursor("pointer")
                color(CSSColors.Blue)
                fontSize(14.px)
                fontWeight("500")
                property("white-space", "nowrap")
            }
        }) {
            Img(
                src = "/images/chrome/icon-location.svg",
                alt = "",
                attrs = {
                    style {
                        width(16.px)
                        height(16.px)
                        property("flex-shrink", "0")
                    }
                },
            )
            Span {
                Text(cityName)
            }
        }
    }
}
