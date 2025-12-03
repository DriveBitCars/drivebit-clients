package my.drivebit.clients

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import my.drivebit.mobile.screens.main.MainScreen
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.ui.theme.DrivebitTheme
import org.koin.compose.KoinApplication

@Composable
actual fun App() {
    DrivebitTheme {
        KoinApplication(application = {
            modules(
                storageModule,
            )
        }) {
            Box(modifier = Modifier.fillMaxSize()) {
                Navigator(MainScreen())
            }
        }
    }
}
