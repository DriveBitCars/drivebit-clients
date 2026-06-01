package my.drivebit.shell

import androidx.compose.runtime.Composable

@Composable
fun PageWithLogo(content: @Composable () -> Unit) {
    AppWithHeader {
        content()
    }
}
