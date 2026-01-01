package my.drivebit.components

import androidx.compose.runtime.Composable

@Composable
fun PageWithLogo(content: @Composable () -> Unit) {
    AppWithHeader {
        content()
    }
}
