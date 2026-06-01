package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.resources.ImagePaths
import my.drivebit.shared.storage.Storage
import my.drivebit.web.homePathHref
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.koin.compose.koinInject

@Composable
fun Logo() {
    val navigationController = LocalNavigationController.current!!
    val storage: Storage = koinInject()

    Div({
        onClick {
            navigationController.navigateTo(homePathHref(storage))
        }
        style {
            cursor("pointer")
        }
    }) {
        Img(
            src = ImagePaths.LOGOS_TURO_LOGO_SVG,
            alt = "Drive bit Logo",
            attrs = {
                style {
                    width(120.px)
                    height(40.px)
                    property("object-fit", "contain")
                    property("transition", "all 0.3s ease")
                    property("max-width", "120px")
                    property("max-height", "40px")
                }
                classes("turo-logo")
            },
        )
    }
}
