# Network Module

Модуль для работы с REST API через Ktor.

## API Configuration

- **Base URL**: `https://api.drivebit.my/api`
- **API Documentation**: [https://api.drivebit.my:5000/swagger](https://api.drivebit.my:5000/swagger)

## Структура

- `HttpClientFactory` - создание HttpClient для разных платформ
- `ApiClient` - базовый клиент для HTTP запросов
- `NetworkModule` - модуль Koin для dependency injection

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

class UserService(private val apiClient: ApiClient) {
    suspend fun getUser(id: Int): User {
        return apiClient.get<User>("/users/$id")
    }
    
    suspend fun createUser(user: User): User {
        return apiClient.post<User>("/users", user)
    }
}
```

