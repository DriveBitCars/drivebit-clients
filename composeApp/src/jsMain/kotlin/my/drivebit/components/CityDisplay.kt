package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.viewmodels.MyCityViewModel
import org.koin.compose.koinInject

@Composable
fun CityDisplay() {
    val myCityViewModel: MyCityViewModel = koinInject()
    val navigationController = LocalNavigationController.current
    val navigationState: NavigationState = koinInject()
    val currentPath by navigationState.currentPath.collectAsState()
    val cityName by myCityViewModel.myCity.collectAsState("")

    LaunchedEffect(currentPath) {
        if (currentPath == "/" || currentPath.isEmpty()) {
            myCityViewModel.refresh()
        }
    }

    if (cityName.isNotEmpty()) {
        LinkButton(
            text = cityName,
            onClick = {
                navigationController?.navigateTo("/my-city-selection")
            },
        )
    }
}
