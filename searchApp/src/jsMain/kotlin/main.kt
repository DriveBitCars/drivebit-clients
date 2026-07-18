package my.drivebit.search

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.renderComposable
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
    startKoin {
        modules(
            module {
                single { httpClient }
                single<SearchCarsApi> { HttpSearchCarsApi(get()) }
                single {
                    SearchViewModel(
                        repositoryFactory = { filters ->
                            SearchCarRepositoryImpl(
                                api = get(),
                                filters = filters,
                                cityIdResolver = ::resolveSearchCityId,
                            )
                        },
                        brandResolver = { null },
                        modelResolver = { _, _ -> null },
                    )
                }
            },
        )
    }
    renderComposable(rootElementId = "root") {
        SearchApp()
    }
}
