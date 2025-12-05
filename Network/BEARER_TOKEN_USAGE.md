# Bearer Token Usage - К каким запросам добавляется Bearer токен

## Обзор

В проекте используются два типа `HttpClient`:

1. **`unauthorized`** - HttpClient БЕЗ Bearer токена
2. **`authorized`** - HttpClient С Bearer токеном

## HttpClient с Bearer токеном (`authorized`)

### Конфигурация

HttpClient создается через `createHttpClientWithConfig()` с установленным плагином `Auth`:

```kotlin
install(Auth) {
    bearer {
        sendWithoutRequest { true }  // ⚠️ ВАЖНО: Bearer токен добавляется ко ВСЕМ запросам
        loadTokens { ... }
        refreshTokens { ... }
    }
}
```

**Ключевой момент**: `sendWithoutRequest { true }` означает, что Bearer токен добавляется **ко всем запросам**, которые используют этот HttpClient, независимо от того, требуется ли авторизация или нет.

### Сервисы, использующие `authorized` HttpClient

#### 1. **User Service** (`UserImpl`)

Все запросы в `User` сервисе используют `authorized` HttpClient и **автоматически получают Bearer токен**:

- ✅ **GET `/api/User/user`** - получение данных пользователя
  - Метод: `userGet()`
  - Bearer токен: **ДОБАВЛЯЕТСЯ**

- ✅ **POST `/api/User/user`** - обновление данных пользователя
  - Метод: `updateUser(firstName, lastName)`
  - Bearer токен: **ДОБАВЛЯЕТСЯ**

### Как это работает

1. При создании запроса через `authorized` HttpClient, плагин `Auth` автоматически:
   - Вызывает `loadTokens()` для получения токенов из storage
   - Добавляет заголовок `Authorization: Bearer <access_token>` к запросу
   - Если токен пустой или null, заголовок не добавляется

2. При получении 401 ответа:
   - Плагин автоматически вызывает `refreshTokens()`
   - Обновляет токены через `Auth.createTokens(refreshToken)`
   - Сохраняет новые токены в storage
   - Повторяет оригинальный запрос с новым токеном

## HttpClient БЕЗ Bearer токена (`unauthorized`)

### Сервисы, использующие `unauthorized` HttpClient

#### 1. **Auth Service** (`AuthImpl`)

Все запросы в `Auth` сервисе используют `unauthorized` HttpClient и **НЕ получают Bearer токен**:

- ❌ **POST `/api/Auth/create-otp`** - создание OTP кода
  - Метод: `createOtp(login)`
  - Bearer токен: **НЕ ДОБАВЛЯЕТСЯ**

- ❌ **POST `/api/Auth/verify-otp`** - верификация OTP кода
  - Метод: `verifyOtp(identifier, code)`
  - Bearer токен: **НЕ ДОБАВЛЯЕТСЯ**

- ❌ **POST `/api/Auth/create-tokens`** - обновление токенов (используется при refresh)
  - Метод: `createTokens(refreshToken)`
  - Bearer токен: **НЕ ДОБАВЛЯЕТСЯ**

## Сводная таблица

| Сервис | Метод | Endpoint | HttpClient | Bearer токен |
|--------|-------|----------|------------|--------------|
| **Auth** | `createOtp` | `POST /api/Auth/create-otp` | `unauthorized` | ❌ Нет |
| **Auth** | `verifyOtp` | `POST /api/Auth/verify-otp` | `unauthorized` | ❌ Нет |
| **Auth** | `createTokens` | `POST /api/Auth/create-tokens` | `unauthorized` | ❌ Нет |
| **User** | `userGet` | `GET /api/User/user` | `authorized` | ✅ Да |
| **User** | `updateUser` | `POST /api/User/user` | `authorized` | ✅ Да |

## Важные замечания

1. **Все запросы через `authorized` HttpClient получают Bearer токен**
   - Даже если эндпоинт не требует авторизации, токен будет добавлен
   - Это происходит из-за `sendWithoutRequest { true }`

2. **Токен добавляется только если он не пустой**
   - Если `loadTokens()` возвращает пустой токен, заголовок не добавляется
   - Проверка: `accessToken.isNotBlank()`

3. **Автоматический refresh при 401**
   - Если запрос получает 401, токен автоматически обновляется
   - Запрос повторяется с новым токеном
   - Если refresh не удался, запрос завершается с ошибкой

4. **Логирование**
   - Все операции с токенами логируются в консоль
   - Можно отследить, когда токен загружается, добавляется, обновляется

## Примеры использования

### Запрос с Bearer токеном (автоматически)

```kotlin
// В UserImpl
val response = httpClient.get(url)  // Bearer токен добавится автоматически
```

### Запрос без Bearer токена

```kotlin
// В AuthImpl
val response = httpClient.post(url) {  // Bearer токен НЕ добавится
    setBody(request)
}
```

## Диагностика проблем

Если Bearer токен не добавляется к запросу:

1. Проверьте, что используется правильный HttpClient (`authorized`)
2. Проверьте, что токен сохранен в storage (`storage.getToken()`)
3. Проверьте логи: `🔑 [Auth] loadTokens called`
4. Проверьте, что токен не пустой в логах

Если Bearer токен добавляется к запросам Auth:

1. Это нормально, если используется `authorized` HttpClient
2. Сервер должен игнорировать Bearer токен для публичных эндпоинтов
3. Если это проблема, нужно использовать `unauthorized` HttpClient
