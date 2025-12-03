package my.drivebit.clients

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import my.drivebit.clients.demo.ProfileScreenTest
import my.drivebit.ui.theme.DrivebitTheme

@Composable
fun AppDemo() {
    DrivebitTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Navigator(ProfileScreenTest())
        }
    }
}
