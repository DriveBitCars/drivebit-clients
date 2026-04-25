# Network Module

Модуль для работы с REST API через Ktor.

## API Configuration

- **Base URL (клиент)**: `https://drivebit.ru/api/` (`DEFAULT_BASE_URL` в коде)
- **Upstream бэкенда (nginx / DNS)**: `api.drivebit.ru:5000`
- **API Documentation (через nginx)**: https://drivebit.ru/api/swagger/index.html

## Структура

- `HttpClientFactory` - создание HttpClient для разных платформ с настроенным base URL
- `NetworkModule` - модуль Koin для dependency injection
- `services/Auth` - сервис аутентификации

## Стили

В проекте используется единая система стилей:

- **ColorsDriveBit** (`UI-Components/src/commonMain/kotlin/my/drivebit/ui/theme/Theme.kt`) - основные цвета темы
- **CSSColors** (`composeApp/src/jsMain/kotlin/my/drivebit/design/CSSColors.kt`) - CSS-версии цветов для веб-платформы
  - `CSSColorValue` версии для использования в Compose Web стилях
  - `String` версии (например, `BlueString`, `Gray300String`) для использования в `setProperty()` и других DOM операциях
- **CSSTypography** (`composeApp/src/jsMain/kotlin/my/drivebit/design/CSSTypography.kt`) - типографика для веб-платформы

**Важно**: При работе с DOM элементами (например, `HTMLInputElement.style.setProperty()`) используйте строковые версии цветов из `CSSColors` (например, `CSSColors.BlueString`) вместо хардкода hex-значений.

## Использование

### Добавление в проект

В `build.gradle.kts` модуля, где нужно использовать Network:

```kotlin
dependencies {
    implementation(project(":Network"))
}
```

### Подключение Koin модуля

В вашем DI модуле:

```kotlin
val appModule = module {
    includes(networkModule)
}
```

### Пример использования

```kotlin
@Serializable
data class User(
    val id: Int,
    val name: String
)

class UserService(private val httpClient: HttpClient) {
    suspend fun getUser(id: Int): User {
        return httpClient.get("$DEFAULT_BASE_URL/users/$id").body()
    }

    suspend fun createUser(user: User): User {
        return httpClient.post("$DEFAULT_BASE_URL/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }
}
```

