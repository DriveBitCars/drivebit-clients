# Работа с динамическими Car Enums

## Обзор

Система для работы с enum'ами автомобилей, которые подгружаются с сервера. Используется парадигма **fast fail** - enum'ы должны обязательно прийти с бэкенда, иначе приложение выбрасывает исключение.

## Архитектура

1. **Network Module** (`Dictionary.kt`) - API запросы для получения enum'ов
2. **Repositories Module** (`CarEnumsRepository.kt`) - репозиторий для загрузки enum'ов
3. **CarEnumsHelper** - удобные методы для работы с enum'ами в UI

## Парадигма Fast Fail

- Enum'ы **обязательно** должны прийти с бэкенда
- Если загрузка не удалась - выбрасывается `CarEnumsLoadException`
- Enum'ы загружаются из сети при каждом запросе, без кэширования
- Все списки enum'ов должны быть не пустыми, иначе выбрасывается исключение
- Если enum не найден по номеру - выбрасывается исключение

## Использование

### 1. Получение enum'ов через репозиторий

```kotlin
class MyViewModel(
    private val carEnumsRepository: CarEnumsRepository,
) : ViewModel() {
    suspend fun loadEnums() {
        try {
            val enums = carEnumsRepository.getEnums()
            // Используем enum'ы
        } catch (e: CarEnumsLoadException) {
            // Обработка ошибки - enum'ы обязательны, приложение не может работать
            // Показать ошибку пользователю или завершить работу экрана
        }
    }
    
    suspend fun getColorTranslate(colorName: String): String {
        try {
            return carEnumsHelper.getColorTranslate(colorName)
        } catch (e: CarEnumsLoadException) {
            // Enum не загружен или не найден
            throw e
        }
    }
}
```

### 2. Использование CarEnumsHelper

```kotlin
class MyViewModel(
    private val carEnumsHelper: CarEnumsHelper,
) : ViewModel() {
    suspend fun displayColor(colorName: String): String {
        try {
            return carEnumsHelper.getColorTranslate(colorName)
        } catch (e: CarEnumsLoadException) {
            // Обработка ошибки
            throw e
        }
    }
    
    suspend fun loadAllColors(): List<EnumItem> {
        val colors = carEnumsHelper.getAllColors()
        // colors содержит name (для отправки на бэк) и translate (для отображения)
        return colors
    }
}
```

### 3. Обновление enum'ов

```kotlin
suspend fun refreshEnums() {
    try {
        carEnumsRepository.getEnums()
    } catch (e: CarEnumsLoadException) {
        // Обработка ошибки загрузки
        throw e
    }
}
```

### 4. Обработка ошибок

```kotlin
sealed interface EnumsState {
    data object Loading : EnumsState
    data class Loaded(val enums: CarEnumsResponse) : EnumsState
    data class Error(val exception: CarEnumsLoadException) : EnumsState
}

class MyViewModel(
    private val carEnumsRepository: CarEnumsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<EnumsState>(EnumsState.Loading)
    val state = _state.asStateFlow()
    
    init {
        loadEnums()
    }
    
    suspend fun loadEnums() {
        _state.value = EnumsState.Loading
        try {
            val enums = carEnumsRepository.getEnums()
            _state.value = EnumsState.Loaded(enums)
        } catch (e: CarEnumsLoadException) {
            _state.value = EnumsState.Error(e)
        }
    }
}
```

## Доступные enum'ы

- `CarColorEnum` - цвета автомобилей
- `BodyTypeEnum` - типы кузова
- `CarStatusEnum` - статусы автомобилей
- `EngineTypeEnum` - типы двигателей
- `TransmissionTypeEnum` - типы трансмиссии
- `DriveTypeEnum` - типы привода
- `SteeringWheelSideEnum` - сторона руля
- `SeatsHeatingEnum` - подогрев сидений
- `SeatsVentilationEnum` - вентиляция сидений
- `SeatsMassageEnum` - массаж сидений
- `ClimateControlEnum` - климат-контроль
- `DriveAssistantsEnum` - системы помощи водителю
- `AlarmSystemEnum` - сигнализация
- `MultimediaSystemEnum` - мультимедиа система
- `CarRoofTypeEnum` - тип крыши
- `MultimediaSystemOptionsEnum` - опции мультимедиа
- `ParkingAssistancesEnum` - системы парковки

## Структура EnumItem

```kotlin
data class EnumItem(
    val number: Int,      // Внутреннее числовое значение (не используется в операциях)
    val name: String,     // Английское название - отправляется на бэкенд
    val translate: String // Локализованное название - показывается пользователю
)
```

## Работа с enum'ами

- **Для отображения**: используйте `translate` (например, "Белый", "Седан")
- **Для отправки на бэкенд**: используйте `name` (например, "White", "Sedan")
- **Поиск**: используйте методы `getColorByName()`, `getBodyTypeByName()` и т.д. по `name`
- **Выбор из списка**: используйте `getAllColors()`, `getAllBodyTypes()` и т.д. - показывайте `translate`, сохраняйте `name`

### Пример использования

```kotlin
// Получение списка цветов для выбора
val colors = carEnumsHelper.getAllColors()
// В UI показываем translate: "Белый", "Чёрный"
// При выборе сохраняем name: "White", "Black"

// Получение перевода для отображения (когда приходит name с бэка)
val translate = carEnumsHelper.getColorTranslate("White") // Вернет "Белый"

// Отправка на бэкенд - используем name
val request = CreateCarRequest(color = "White") // name, не translate
```

## Исключения

### CarEnumsLoadException

Выбрасывается в следующих случаях:
- Не удалось загрузить enum'ы с сервера
- Один из обязательных списков enum'ов пустой
- Enum не найден по номеру

```kotlin
class CarEnumsLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

## Загрузка из сети

- Enum'ы всегда загружаются из сети при каждом запросе
- Нет кэширования - каждый раз делается запрос к API
- Это гарантирует актуальность данных и соответствие парадигме fast fail

## DI настройка

Репозиторий уже настроен в `RepositoriesModule`. Для использования в ViewModel:

```kotlin
class MyViewModel(
    private val carEnumsRepository: CarEnumsRepository, // Автоматически инжектится через Koin
) : ViewModel() {
    // ...
}
```

Или используйте `CarEnumsHelper`:

```kotlin
val repositoriesModule = module {
    // ... другие репозитории
    
    factory<CarEnumsHelper> {
        CarEnumsHelper(get<CarEnumsRepository>())
    }
}
```
