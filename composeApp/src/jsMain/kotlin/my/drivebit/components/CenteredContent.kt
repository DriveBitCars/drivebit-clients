package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import org.jetbrains.compose.web.css.*

@Composable
fun CenteredContent(
    padding: CSSSizeValue<out CSSUnit.px> = 40.px,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = {
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            textAlign("center")
            padding(padding)
        },
    ) {
        content()
    }
}
