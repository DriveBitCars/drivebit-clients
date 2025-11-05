package my.drivebit.network.di

import my.drivebit.network.api.ApiClient
import my.drivebit.network.createHttpClientWithConfig
import org.koin.dsl.module

/**
 * Network module for Koin DI
 * API Base URL: http://82.146.43.206:5000/api
 * API Documentation: http://82.146.43.206:5000/api/docs
 */
val networkModule =
    module {
        single {
            createHttpClientWithConfig()
        }

        single {
            ApiClient(
                httpClient = get(),
                baseUrl = "http://82.146.43.206:5000/api",
            )
        }
    }
