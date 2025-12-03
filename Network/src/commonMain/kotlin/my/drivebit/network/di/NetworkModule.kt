package my.drivebit.network.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import my.drivebit.network.createHttpClientWithConfig
import my.drivebit.network.createPlatformHttpClientEngine
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthImpl
import my.drivebit.network.services.User
import my.drivebit.network.services.UserImpl
import my.drivebit.shared.storage.Storage
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Network module for Koin DI
 * API Base URL: https://api.drivebit.my/api
 * API Documentation: http://213.171.27.185:5000/swagger/index.html
 */
val networkModule =
    module {
        single<HttpClient>(named("unauthorized")) {
            HttpClient(createPlatformHttpClientEngine()) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = false
                        },
                    )
                }
            }
        }
        single<Auth> {
            AuthImpl(get(named("unauthorized")))
        }
        single<HttpClient>(named("authorized")) {
            val storage = get<Storage>()
            val authService = get<Auth>()
            createHttpClientWithConfig(
                getToken = { storage.getToken()!! },
                getRefreshToken = { storage.getRefreshToken()!! },
                saveTokens = { accessToken, refreshToken ->
                    accessToken?.let { storage.saveToken(it) }
                    refreshToken.let { storage.saveRefreshToken(it) }
                },
                authService = authService,
            )
        }
        single<User> {
            UserImpl(get(named("authorized")))
        }
    }
