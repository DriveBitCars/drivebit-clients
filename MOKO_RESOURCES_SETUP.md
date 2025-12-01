# Генерация констант для путей к изображениям

## Реализовано решение

В проекте реализован **простой Gradle task** для автоматической генерации констант путей к изображениям.

## Как это работает

### 1. Gradle Task

В `CommonViewModels/build.gradle.kts` добавлен task `generateImageConstants`, который:
- Сканирует папку `composeApp/src/commonMain/resources/images/`
- Генерирует файл `ImagePaths.kt` с константами для всех изображений
- Автоматически запускается перед компиляцией

### 2. Сгенерированный файл

Файл создается в `CommonViewModels/src/commonMain/kotlin/my/drivebit/resources/ImagePaths.kt`:

```kotlin
package my.drivebit.resources

object ImagePaths {
    const val BUTTER_CAR_ICON_SVG = "images/butter/car-icon.svg"
    const val LOGOS_TURO_LOGO_SVG = "images/logos/turo_logo.svg"
    // ... и т.д.
}
```

### 3. Использование

```kotlin
import my.drivebit.resources.ImagePaths

// Вместо строкового пути
iconUrl = "images/butter/car-icon.svg"

// Используем константу
iconUrl = ImagePaths.BUTTER_CAR_ICON_SVG
```

## Преимущества

✅ **Типобезопасность** - IDE подсказывает доступные константы  
✅ **Автодополнение** - не нужно помнить точные пути  
✅ **Рефакторинг** - при переименовании файла константа обновится автоматически  
✅ **Нет опечаток** - компилятор поймает ошибки  
✅ **Простота** - не требует дополнительных зависимостей  

## Запуск генерации вручную

```bash
./gradlew :CommonViewModels:generateImageConstants
```

## Альтернатива: moko-resources

Если в будущем понадобится более продвинутая система управления ресурсами (локализация, цвета, шрифты), можно рассмотреть [moko-resources](https://github.com/icerockdev/moko-resources).

Однако для простой генерации констант путей текущее решение оптимально.

