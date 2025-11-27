# Master Flow: Заход в профиль

## Описание
Документ описывает полный flow захода пользователя в профиль на всех платформах (Web, Android, iOS).

## Точки входа

### 1. Web платформа
- **URL**: `/profile`
- **Триггер**: 
  - Клик на "Мой профиль" в ButterMenu
  - Прямой переход по URL `/profile`

### 2. Mobile платформа (Android/iOS)
- **Экран**: `ProfileScreen`
- **Триггер**: 
  - Клик на "Мой профиль" в ButterMenu (вкладка "Еще")
  - Навигация через `navigator.push(ProfileScreen())`

## Flow детали

### Шаг 1: Инициализация экрана
1. **Web**: `ProfilePage` компонент рендерится через роутинг в `App.kt`
2. **Mobile**: `ProfileScreen` создается через Voyager navigation

### Шаг 2: Загрузка данных профиля
1. `ProfileViewModel` создается через DI (Koin)
2. В `init` блоке автоматически вызывается `loadProfile()`
3. `ProfileViewModel` делает запрос к `/User/user` через `UserService`
4. Запрос включает Bearer токен из `Storage` через `AuthInterceptorPlugin`

### Шаг 3: Состояния UI

#### Loading State
- **Web**: CSS spinner (черный круг с анимацией вращения)
- **Mobile**: `Loader` компонент (CircularProgressIndicator)

#### Error State
- Отображается сообщение об ошибке
- Кнопка "Повторить" для повторной попытки загрузки

#### Success State
- Отображается информация о пользователе:
  - Имя (firstName, middleName, lastName)
  - Дата регистрации (createdAt)
  - Номер телефона (phone)
  - Email (email)
  - Фотографии (photos)
- Секция "ПРОВЕРЕННАЯ ИНФОРМАЦИЯ"
- Секция "ОТЗЫВЫ ОТ ХОСТОВ"

### Шаг 4: Навигация обратно
- **Web**: Кнопка "Назад" → `navigationController?.navigateTo("/")`
- **Mobile**: `ApplicationTopBar` с кнопкой назад → `navigator.pop()`

## Компоненты

### ViewModel
- **Файл**: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/ProfileViewModel.kt`
- **Интерфейс**: `ProfileViewModel`
- **Реализация**: `ProfileViewModelImpl`
- **Состояния**: `ProfileState` (Loading, Error, Success)

### Service
- **Файл**: `Network/src/commonMain/kotlin/my/drivebit/network/services/User.kt`
- **Интерфейс**: `User`
- **Реализация**: `UserImpl`
- **Метод**: `userGet(): UserGetResponse`

### UI Components

#### Web
- **Файл**: `composeApp/src/jsMain/kotlin/my/drivebit/screens/ProfilePage.kt`
- **Компонент**: `ProfilePage`
- **Стилизация**: CSS через Compose Web

#### Mobile
- **Файл**: `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/profile/ProfileScreen.kt`
- **Компонент**: `ProfileScreen` (Screen для Voyager)
- **Стилизация**: Material3 через Compose Multiplatform

## Авторизация

### Bearer Token
- Токен хранится в `Storage`
- Добавляется автоматически через `AuthInterceptorPlugin` в `HttpClient`
- Формат заголовка: `Authorization: Bearer {token}`

### Interceptor
- **Файл**: `Network/src/commonMain/kotlin/my/drivebit/network/HttpClientFactory.kt`
- **Плагин**: `AuthInterceptorPlugin`
- **Конфигурация**: Передается `getToken` lambda из DI

## Тестирование

### Unit Tests
- **ProfileViewModelTest**: Тестирует состояния ViewModel
- **UserTest**: Тестирует UserService и обработку ошибок

### Manual Testing
1. Запустить приложение
2. Открыть ButterMenu
3. Кликнуть "Мой профиль"
4. Проверить загрузку данных
5. Проверить отображение информации
6. Проверить навигацию назад

## Известные проблемы и решения

### Проблема: CompositionLocal LocalDensity not present (Web)
**Решение**: Заменен `CircularProgressIndicator` на CSS spinner в `ProfilePage`

### Проблема: Unresolved reference koinScreenModel (Mobile ButterMenu)
**Решение**: Использован `koinInject()` вместо `koinScreenModel()` для не-Screen компонентов

### Проблема: Множественное добавление CSS стилей (Web)
**Решение**: Использован `SideEffect` с проверкой существования стиля перед добавлением в `document.head`

## Зависимости

### Модули
- `CommonViewModels` → `ProfileViewModel`
- `Network` → `UserService`, `AuthInterceptorPlugin`
- `Storage` → Хранение токена
- `UI-Components` → `Loader` компонент

### Библиотеки
- Ktor Client (HTTP запросы)
- Koin (Dependency Injection)
- Voyager (Navigation для Mobile)
- Compose Multiplatform (UI)
