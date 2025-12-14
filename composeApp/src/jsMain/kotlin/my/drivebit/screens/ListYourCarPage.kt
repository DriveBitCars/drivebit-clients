package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun ListYourCarPage() {
    DisposableEffect(Unit) {
        window.location.href = "/list-your-car.html"
        onDispose { }
    }

    Div({
        style {
            width(100.percent)
            property("height", "100vh")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
        }
    }) {
        // Показываем загрузку во время перенаправления
    }
}
