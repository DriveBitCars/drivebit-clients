# Демо-приложение для Maestro тестов

## Описание

Отдельное демо-приложение создано для тестирования экрана профиля через Maestro без влияния на production код. Демо-приложение использует моки и сразу открывает экран профиля.

**Важно:** Production код полностью изолирован от моков и не содержит никаких упоминаний о тестовых данных.

## Структура

### Production код (не содержит моков)
- `composeApp/src/androidMain/` - основной код приложения
- `Mobile/src/commonMain/` - общий код для мобильных платформ
- `CommonViewModels/src/commonMain/` - ViewModels без моков
- `composeApp/src/jsMain/` - веб-версия приложения (без моков)

### Демо код (полностью изолирован в androidMaestro)
- `composeApp/src/androidMaestro/` - демо-приложение для Maestro тестов
  - `kotlin/my/drivebit/clients/demo/MainActivity.kt` - точка входа демо-приложения
  - `kotlin/my/drivebit/clients/AppDemo.kt` - демо-версия App
  - `kotlin/my/drivebit/clients/demo/ProfileScreenTest.kt` - тестовый экран профиля с моком
  - `AndroidManifest.xml` - отдельный манифест для демо-приложения

## Моки

### MockProfileViewModelForTest
Расположен в `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/profile/ProfileScreenTest.kt`

**Моковые данные:**
- Имя: Иван Петрович Иванов
- Телефон: +7(912)742-88-27
- Email: ivan.ivanov@example.com
- Дата регистрации: 2024-01-15T10:30:00Z

## Сборка демо-приложения

```bash
./gradlew :composeApp:assembleMaestro
```

APK будет создан в: `composeApp/build/outputs/apk/maestro/`

## Запуск Maestro теста

1. Установите APK на устройство/эмулятор:
```bash
adb install composeApp/build/outputs/apk/maestro/composeApp-maestro.apk
```

2. Запустите Maestro тест:
```bash
maestro test maestro/test-profile-screen.yaml
```

## Maestro тест

Файл: `maestro/test-profile-screen.yaml`

Тест проверяет:
- ✅ Отображение заголовка "Мой профиль"
- ✅ Наличие кнопки "Редактировать профиль"
- ✅ Секции "ПРОВЕРЕННАЯ ИНФОРМАЦИЯ" и "ОТЗЫВЫ ОТ ХОСТОВ"
- ✅ Отображение моковых данных пользователя
- ✅ Делает скриншот для анализа

## Изоляция от production кода

✅ Production код (`androidMain`) не содержит моков
✅ Демо код (`androidMaestro`) полностью изолирован
✅ Моки находятся только в тестовых файлах
✅ Разные MainActivity для production и demo

## Анализ скриншота

После запуска теста скриншот сохраняется как `profile-screen-test.png` в директории Maestro.

Проверьте:
1. Корректность отображения всех элементов UI
2. Правильность форматирования данных
3. Расположение элементов на экране
4. Соответствие дизайну
