package my.drivebit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.first
import my.drivebit.repositories.MyCityRepository
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.cityNameToSlug
import my.drivebit.web.CitySlugResolver
import my.drivebit.web.parseCitySlugFromPath
import org.koin.compose.koinInject

@Composable
fun Navigation(content: @Composable (String) -> Unit) {
    val myCityRepository: MyCityRepository = koinInject()
    val citySlugResolver: CitySlugResolver = koinInject()
    val storage: Storage = koinInject()

    NavigationControllerProvider { currentPath ->
        val navigationController = LocalNavigationController.current

        LaunchedEffect(currentPath) {
            when {
                currentPath.isEmpty() || currentPath == "/" -> {
                    val city = myCityRepository.getSelectedCity.first()
                    val target = "/${cityNameToSlug(city.name)}"
                    navigationController?.replacePath(target)
                }
                else -> {
                    parseCitySlugFromPath(currentPath)?.let { slug ->
                        val city =
                            citySlugResolver.resolve(slug) ?: run {
                                val fb = citySlugResolver.moscowCity()
                                navigationController?.replacePath("/${cityNameToSlug(fb.name)}")
                                myCityRepository.selectCity(fb.id, fb.name)
                                return@LaunchedEffect
                            }
                        val current = myCityRepository.getSelectedCity.first()
                        if (current.id != city.id || current.name != city.name) {
                            myCityRepository.selectCity(city.id, city.name)
                        }
                    }
                }
            }
            MetaTags.updateForPath(currentPath, storage)
        }

        content(currentPath)
    }
}
