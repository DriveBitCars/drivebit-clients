# Swagger Documentation Notes

## Важно: Всегда проверяй Swagger перед реализацией API

### Ссылки на Swagger
- **Backend Server (прямой доступ)**: http://157.22.252.70:5000/swagger/index.html
- **Production (через nginx)**: https://drivebit.ru/api/swagger/index.html

### Ключевые моменты:

1. **Схемы запросов**: Всегда проверяй структуру запросов в разделе "Schemas"
   - Например: `ChangeLoginViaOtpRequest` используется и для смены email, и для смены телефона
   - Оба эндпоинта (`/User/change-email` и `/User/change-phone`) используют поле `newLogin`, а не `newPhone`

2. **Парсинг ошибок**: При ошибках валидации бэкенд возвращает JSON с полями:
   - `errors` - объект с полями и массивом сообщений об ошибках
   - Нужно извлекать только сообщения из `errors`, а не показывать весь JSON

3. **Проверка перед коммитом**:
   - Сверь URL эндпоинта
   - Сверь структуру DTO (имена полей, типы)
   - Сверь структуру ответа

4. **Car create/update (PUT/POST `/Car/my`)**:
   - POST принимает `CreateCarRequest`, PUT — **`UpdateCarRequest`** (другая схема: `carId` в теле, без `id`/`modelId`/`seats`, `additionalProperties: false`).
   - `insurance` — enum `InsuranceTypeEnum`: `OSAGO_Included`, `OSAGO_Unlimited`, `KASKO_Included`, `KASKO_Unlimited`.
   - Клиент для PUT маппит `CarCreateRequest` → `UpdateCarRequest` в `Car.kt`.
   - Поле года в теле: `year` (camelCase).

