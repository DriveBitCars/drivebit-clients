package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.navigation.NavigationControllerProvider
import my.drivebit.shell.HeaderInteractiveCompose
import my.drivebit.shell.HtmlUnreadBannerSync

@Composable
fun AppHeaderRoot() {
    NavigationControllerProvider {
        HtmlUnreadBannerSync()
        HeaderInteractiveCompose()
    }
}
