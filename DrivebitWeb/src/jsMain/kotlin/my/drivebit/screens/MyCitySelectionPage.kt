package my.drivebit.screens

import androidx.compose.runtime.Composable
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.cityNameToSlug

@Composable
fun MyCitySelectionPage() {
    val navigationController = LocalNavigationController.current
    CitySelectionPage(
        mode =
            CitySelectionMode.ForMyCity(
                onCitySelected = { city ->
                    navigationController?.navigateTo("/${cityNameToSlug(city.name)}")
                },
            ),
    )
}
