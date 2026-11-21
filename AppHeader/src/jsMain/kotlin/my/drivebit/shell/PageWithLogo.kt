package my.drivebit.shell

import androidx.compose.runtime.Composable

@Composable
fun PageWithLogo(
    fullBleed: Boolean = false,
    content: @Composable () -> Unit,
) = PageContentShell(fullBleed = fullBleed, content = content)
