package my.drivebit.network.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import my.drivebit.network.createHttpClientWithConfig
import my.drivebit.network.createPlatformHttpClientEngine
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthImpl
import my.drivebit.network.services.Photo
import my.drivebit.network.services.PhotoImpl
import my.drivebit.network.services.User
import my.drivebit.network.services.UserImpl
import my.drivebit.shared.storage.Storage
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Network module for Koin DI
 * API Base URL: http://api.drivebit.my:5000/
 * API Documentation:  https://drivebit.my/api/swagger/index.html
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
        single<Photo> {
            PhotoImpl(get(named("authorized")))
        }
    }
