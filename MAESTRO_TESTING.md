# Maestro Testing Guide

## Установка Maestro

```bash
curl -Ls "https://get.maestro.mobile.dev" | bash
```

## Как Maestro находит кнопки

Maestro может кликать на кнопки используя:

1. **Текст кнопки** - Maestro ищет по видимому тексту
2. **Test Tags** - `Modifier.testTag("button-id")` в Compose
3. **Accessibility Labels** - `contentDescription` в Compose

## Добавление Test Tags в Compose

Для Android/iOS используйте `Modifier.testTag()`:

```kotlin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier

Button(
    onClick = { },
    modifier = Modifier.testTag("menu-button")
) {
    Text("Menu")
}
```

## Примеры тестов Maestro

### Тест клика на кнопку меню

```yaml
appId: my.drivebit.clients
---
- launchApp
- tapOn: "Menu"  # Клик по тексту "Menu"
- assertVisible: "Логин"
```

### Тест с использованием testTag

```yaml
appId: my.drivebit.clients
---
- launchApp
- tapOn: 
    id: "menu-button"  # Использует testTag
- assertVisible: "Логин"
```

## Запуск тестов

```bash
maestro test maestro/test-menu-button.yaml
```

