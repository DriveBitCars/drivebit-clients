# Воронка оплаты: диагностика и observability

> Проверено: 2026-07-25 · источники: docker logs / Postgres на `157.22.252.70`, Yandex Metrika counter `105947907`, pages-dev e2e

## Вердикт

Жалоба «клиенты не могут оплатить» (чат ~15:46 MSK): до AlfaBank запросы **не доходят**. С 16.07 не было регистраций платежей; на момент проверки **5** `Confirmed` броней без строк в `Payments`. Брони «здоровые» (дедлайн открыт, кнопка в чате есть) — проблема на пути клик → API / сессия, не в банке.

**С чего начать:** смотреть цели Метрики `pay_click` → `pay_redirect` / `pay_fail` и permanent-логи `PaymentController` («Запрос ссылки на оплату…»).

## Факты (инцидент 2026-07-25)

| Факт | Истотус | Статус |
|------|---------|--------|
| API `/api/health` healthy | prod backend | ✅ |
| Последний успешный платёж: PaymentId 106, 2026-07-16 | Postgres `Payments` | ✅ |
| 0 новых `Payments` за ≥7 дней | Postgres | ✅ |
| 5× `Confirmed` без `Payments` (в т.ч. `87306748…` Chery, подтверждена ~15:00 MSK) | Postgres | ✅ |
| В чате ActionType=1 «Можно оплатить» | `Messages.MessageActionBlockJson` | ✅ |
| Нет логов «Запрос в банк…» после 16.07 | `ApplicationLogs` IsPermanent | ✅ |
| Metrika: 0 pageviews `/payment*` за день инцидента | Metrika API | ✅ |
| `PaymentStatusCheckBackgroundService` отключён (не блокирует выдачу ссылки) | backend logs | ✅ |
| Шум: Telegram proxy timeouts, `Token refresh failed` | docker logs | ✅ |

## Observability (задеплоено на clients trunk)

PR: [#317](https://github.com/DriveBitCars/drivebit-clients/pull/317) · commit `b4e73abe`

### Цели Яндекс.Метрики (counter 105947907)

| Goal id | Имя | Когда |
|---------|-----|--------|
| `pay_click` | Клик оплатить | клик / старт checkout |
| `pay_redirect` | Редирект в банк | получен `paymentUrl` |
| `pay_fail` | Ошибка оплаты API | fail ответа API |
| `pay_already_paid` | Уже оплачено | HTTP 208 / already paid |
| `payment_success` | Страница успешной оплаты | `/payment-success` |
| `payment_failure` | Страница неуспешной оплаты | `/payment-failure` |

Параметры события: `source` (`chat` \| `my_bookings` \| `payment_link` \| `car_detail`), `kind` (`full` \| `prepay`), `bookingId`, опционально `message` (до 120 символов).

Код: `Utils/.../PaymentFunnelAnalytics.kt`, `DrivebitWeb/.../YandexMetrica.kt`, wiring в chat / my-bookings / payment-link / car-detail.

### Как читать воронку

| Паттерн | Интерпретация |
|---------|----------------|
| Нет `pay_click` | UI/кнопка/сессия — клик не происходит |
| Есть `pay_click`, нет `pay_redirect`/`pay_fail` | обрыв до ответа (сеть / вкладка / crash) |
| `pay_fail` | смотреть `message` + backend ApplicationLogs |
| `pay_redirect` без `payment_success` | ушёл в банк, не вернулся / отказ банка |
| `payment_failure` | returnUrl fail от банка |

## Backend logs (отдельный PR)

PR: [DriveBitCars/drivebitbackend#9](https://github.com/DriveBitCars/drivebitbackend/pull/9) · ветка `feat/payment-attempt-logging`

Permanent-логи в `PaymentController.GenerateBookingPaymentAsync`:

- попытка без авторизации
- запрос ссылки (`BookingId`, `UserId`, `Kind`)
- ошибка выдачи
- уже оплачено
- ссылка выдана

## Не брать / оговорки

- Firetiger в сессии не использовался (auth отклонён) — опирались на SSH + DB + Metrika.
- Headless e2e на pages-dev не доказывает доставку `reachGoal` в Metrika (скрипты аналитики часто режутся); факт вставки goal на `/payment-success` проверен в HTML.
- `dev.drivebit.ru` в Playwright давал `ERR_CERT_AUTHORITY_INVALID` — e2e шёл на `https://dev.drivebit.my`.
- Backend PR #9 на момент записи мог быть ещё не смержен — без деплоя бэка permanent-логи в prod не появятся.

## История правок

| Дата | Что изменили |
|------|----------------|
| 2026-07-25 | первичная запись: инцидент + Metrika funnel + backend PR |
