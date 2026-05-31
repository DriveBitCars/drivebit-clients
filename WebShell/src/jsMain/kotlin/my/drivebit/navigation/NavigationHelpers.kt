package my.drivebit.navigation

import androidx.compose.runtime.compositionLocalOf

val LocalNavigationController =
    compositionLocalOf<NavigationController?> {
        null
    }
