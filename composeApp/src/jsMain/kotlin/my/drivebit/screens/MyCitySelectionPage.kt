package my.drivebit.screens

import androidx.compose.runtime.Composable
import my.drivebit.navigation.LocalNavigationController

@Composable
fun MyCitySelectionPage() {
    val navigationController = LocalNavigationController.current
    CitySelectionPage(
        mode =
            CitySelectionMode.ForMyCity(
                onCitySelected = {
                    navigationController?.navigateTo("/")
                },
            ),
    )
}
