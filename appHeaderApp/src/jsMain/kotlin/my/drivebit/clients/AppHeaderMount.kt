package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.navigation.Navigation
import my.drivebit.shell.StandaloneAppHeader

@Composable
fun AppHeaderMount() {
    Navigation { _ ->
        StandaloneAppHeader()
    }
}
