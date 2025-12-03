# UI-Components Module

Модуль переиспользуемых UI компонентов для Drivebit приложения.

## Компоненты

### ApplicationTopBar
Универсальная верхняя панель с заголовком и опциональной кнопкой "Назад".

**Параметры:**
- `title: String` - заголовок панели
- `onBackClick: (() -> Unit)?` - опциональный обработчик кнопки "Назад"
- `modifier: Modifier` - модификатор для настройки внешнего вида

**Использование:**
```kotlin
ApplicationTopBar(
    title = "Screen Title",
    onBackClick = { navigator.pop() }
)
```

### Loader
Простой черно-белый индикатор загрузки для использования на любых экранах.

**Параметры:**
- `modifier: Modifier` - модификатор для настройки внешнего вида (по умолчанию `fillMaxSize()`)
- `size: Dp` - размер индикатора (по умолчанию `48.dp`)
- `color: Color` - цвет индикатора (по умолчанию `Color.Black`)

**Использование:**
```kotlin
Loader()

// С кастомным размером
Loader(size = 32.dp)

// С кастомным цветом
Loader(color = Color.Gray)
```

## Зависимости

- Compose Multiplatform для UI
- Material3 для дизайна
- Material Icons для иконок

## Интеграция

Модуль может быть использован в любом другом модуле приложения:

```kotlin
// В build.gradle.kts
implementation(project(":UI-Components"))

// В коде
import my.drivebit.ui.components.ApplicationTopBar
```

## Сборка

```bash
./gradlew :UI-Components:assembleDebug
```

## Архитектура

Модуль спроектирован как независимый компонент без внешних зависимостей, что позволяет легко переиспользовать его в разных частях приложения.
