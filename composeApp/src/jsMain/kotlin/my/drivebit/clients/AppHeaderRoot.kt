package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.navigation.Navigation
import my.drivebit.shell.StandaloneAppHeader
import my.drivebit.web.koin.WebKoinHost

@Composable
fun AppHeaderRoot() {
    WebKoinHost {
        Navigation { _ ->
            StandaloneAppHeader()
        }
    }
}
