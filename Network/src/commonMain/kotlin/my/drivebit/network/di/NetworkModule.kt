package my.drivebit.network.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import my.drivebit.network.createHttpClientWithConfig
import my.drivebit.network.createPlatformHttpClientEngine
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthImpl
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingImpl
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarImpl
import my.drivebit.network.services.Dadata
import my.drivebit.network.services.DadataImpl
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.DictionaryImpl
import my.drivebit.network.services.Documents
import my.drivebit.network.services.DocumentsImpl
import my.drivebit.network.services.Photo
import my.drivebit.network.services.PhotoImpl
import my.drivebit.network.services.User
import my.drivebit.network.services.UserImpl
import my.drivebit.shared.storage.Storage
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Network module for Koin DI
 * API Base URL: https://drivebit.my/api/
 * Backend Server: 155.212.170.94:5000
 * API Documentation: http://155.212.170.94:5000/swagger/index.html
 * Production API Documentation: https://drivebit.my/api/swagger/index.html
 */
val networkModule =
    module {
        single<HttpClient>(named("unauthorized")) {
            println("🔧 [NetworkModule] Creating unauthorized HttpClient (NO Bearer token will be added)")
            HttpClient(createPlatformHttpClientEngine()) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = false
                            explicitNulls = true
                        },
                    )
                }
                // ⚠️ ВАЖНО: НЕ устанавливаем плагин Auth - это гарантирует, что Bearer токен НЕ будет добавляться
            }.also {
                println("🔧 [NetworkModule] Unauthorized HttpClient created successfully (NO Auth plugin installed)")
            }
        }
        single<Auth> {
            AuthImpl(get(named("unauthorized")))
        }
        single<HttpClient>(named("authorized")) {
            println("🔧 [NetworkModule] Creating authorized HttpClient...")
            val storage = get<Storage>()
            val authService = get<Auth>()

            val currentToken = storage.getToken()
            val currentRefreshToken = storage.getRefreshToken()
            println("🔧 [NetworkModule] Current tokens in storage:")
            println(
                "   - Access token: ${if (currentToken != null) "present (${currentToken.take(20)}...)" else "null"}",
            )
            println(
                "   - Refresh token: ${if (currentRefreshToken != null) {
                    "present (${currentRefreshToken.take(
                        20,
                    )}...)"
                } else {
                    "null"
                }}",
            )

            createHttpClientWithConfig(
                getToken = {
                    val token = storage.getToken() ?: ""
                    println(
                        "🔧 [NetworkModule] getToken called: ${if (token.isNotEmpty()) "token present" else "empty token"}",
                    )
                    token
                },
                getRefreshToken = {
                    val refreshToken = storage.getRefreshToken() ?: ""
                    println(
                        "🔧 [NetworkModule] getRefreshToken called: ${if (refreshToken.isNotEmpty()) "refresh token present" else "empty refresh token"}",
                    )
                    refreshToken
                },
                saveTokens = { accessToken, refreshToken ->
                    println("🔧 [NetworkModule] saveTokens called")
                    println(
                        "   - Access token: ${if (accessToken != null) {
                            "present (${accessToken.take(
                                20,
                            )}...)"
                        } else {
                            "null"
                        }}",
                    )
                    println("   - Refresh token: ${refreshToken.take(20)}...")
                    accessToken?.let { storage.saveToken(it) }
                    refreshToken.let { storage.saveRefreshToken(it) }
                    println("🔧 [NetworkModule] Tokens saved to storage")
                },
                authService = authService,
            ).also {
                println("🔧 [NetworkModule] Authorized HttpClient created successfully")
            }
        }
        single<User> {
            UserImpl(get(named("authorized")))
        }
        single<Car> {
            CarImpl(get(named("authorized")))
        }
        single<Photo> {
            PhotoImpl(get(named("authorized")), get<Car>())
        }
        single<Dadata> {
            DadataImpl(get(named("unauthorized")))
        }
        single<Dictionary> {
            DictionaryImpl(get(named("unauthorized")))
        }
        single<Booking> {
            BookingImpl(
                unauthorizedHttpClient = get(named("unauthorized")),
                authorizedHttpClient = get(named("authorized")),
            )
        }
        single<Documents> {
            DocumentsImpl(get(named("authorized")))
        }
    }
