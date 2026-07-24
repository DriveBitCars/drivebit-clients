# Регрессия: URL-first фильтры на home (pages-dev)

> Проверено: 2026-07-24 · источники: Playwright e2e на https://dev.drivebit.my · trunk `d0af315d` (merge #307)

## Вердикт

Фильтры, снятие, refresh и пагинация на city home работают через URL. «Поблизости» полностью OK: карта, geo, радиус, режим **Список** (после фикса #307).

**С чего начать:** чеклист ниже на `https://dev.drivebit.my/moskva` (canonical; `dev.drivebit.ru` → 301).

## URL-контракт

| Параметр | Где | Правило |
|----------|-----|---------|
| filter | path `/{city}/{slug}` | SEO-slug фильтра; без slug = «Все» |
| `startDate` / `endDate` | query | ISO даты; опционально |
| `page` | query | 1-based; на page=1 можно опускать |
| `lat` / `lon` | query | только «Поблизости» |
| `radiusKm` | query | только nearby; default 10, можно опускать |

Примеры:

- `/moskva`
- `/moskva/arenda-avto-v-abkhaziyu`
- `/moskva?page=2`
- `/moskva/poblizosti?lat=55.7558&lon=37.6173`
- `/moskva/poblizosti?lat=55.7558&lon=37.6173&radiusKm=25`

## Чеклист регрессии

### A. Выбор фильтра (chip → URL + pressed + карточки)

| # | Шаг | Ожидание | 2026-07-24 |
|---|-----|----------|------------|
| A1 | `/moskva` | «Все» pressed, есть карточки | ✅ |
| A2 | chip «В Крым» | URL `…/arenda-avto-v-krym`, chip pressed, авто | ✅ |
| A3 | «Беларусь» | `…/arenda-avto-v-belarus` | ✅ |
| A4 | «Абхазия» | `…/arenda-avto-v-abkhaziyu` | ✅ |
| A5 | «Эконом» | `…/arenda-avto-ekonom-klassa-bez-voditelya` | ✅ |
| A6 | «Комфорт» | comfort slug | ✅ |
| A7 | «Бизнес» | business slug | ✅ |
| A8 | «Премиум» | premium slug | ✅ |
| A9 | «Минивэн» | minivan slug | ✅ |
| A10 | «Внедорожник» | vnedorozhnik slug | ✅ |
| A11 | «Поблизости» | `…/poblizosti` (+ lat/lon после geo) | ✅ |

### B. Отжим фильтра (повторный клик по выбранному → «Все»)

| # | Шаг | Ожидание | 2026-07-24 |
|---|-----|----------|------------|
| B1–B10 | повторный клик по каждому выбранному chip | URL `/moskva`, pressed «Все» | ✅ |

### C. Refresh (F5 / reload)

| # | URL | Ожидание | 2026-07-24 |
|---|-----|----------|------------|
| C1 | Абхазия slug | тот же slug, chip, авто | ✅ |
| C2 | Внедорожник slug | то же | ✅ |
| C3 | `poblizosti?lat=&lon=` | chip + карта/маркеры | ✅ |

### D. Пагинация (не nearby)

| # | Шаг | Ожидание | 2026-07-24 |
|---|-----|----------|------------|
| D1 | `/moskva` → «Вперёд» | `?page=2`, «Показано …», другой набор авто | ✅ |
| D2 | deeplink `/moskva?page=2` | сразу page 2 | ✅ |
| D3 | «Назад» | снова page 1 / без `page` | ✅ |

> UI: кнопки **«Вперёд» / «Назад»** (не цифра «2»).

### E. Поблизости

| # | Шаг | Ожидание | 2026-07-24 |
|---|-----|----------|------------|
| E1 | chip / deeplink poblizosti | Leaflet, маркеры (>0) | ✅ (~100) |
| E2 | geo | query `lat`+`lon` (fallback Москва) | ✅ |
| E3 | радиус «25 км» | `radiusKm=25` в URL, карта обновляется | ✅ |
| E4 | «Список» | List selected, сетка авто + «Показано» | ✅ post-#307: «Показано 1–9 из 100», 9 карточек |
| E5 | refresh с lat/lon | карта/состояние сохраняются | ✅ |

**Баг E4 (до #307):** клик «Список» не держался — `koinInject(parametersOf(новые лямбды))` пересоздавал `MapViewModel` на каждый `collectAsState`, режим снова `Map`.

**Фикс (#307):** `remember(initialRadiusKm) { koinScope.get<MapViewModel> { … } }` (и аналогично `MainContentViewModel`).

### F. Smoke pages-dev (skill)

| Path | 2026-07-24 |
|------|------------|
| `/moskva` | ✅ |
| `/moskva/search` | ✅ |
| brand (`/bmw` или search) | ✅ |
| `/login-by-phone` | ✅ |
| `/contacts` | ✅ |

## Артефакты прогона

| Что | Где |
|-----|-----|
| Скриншоты + `report.json` | `tmp/e2e-home-filters-regress/` |
| Скрипт | `tmp/e2e-home-filters-regress/run.mjs` |
| Pages deploy (merge #306 URL-first) | https://github.com/DriveBitCars/drivebit-clients/actions/runs/30084552654 ✅ |
| Pages deploy (merge #307 List fix) | https://github.com/DriveBitCars/drivebit-clients/actions/runs/30089143823 ✅ |
| Postfix screenshots | `tmp/e2e-home-filters-regress/postfix-*.png` |
| Base URL | `https://dev.drivebit.my` |

Известные шумности headless: Jivo, analytics, редкий Leaflet `_leaflet_pos`, 404 картинок.

## Не брать / оговорки

- Не считать pages-dev OK без зелёного `github-pages.yml` на `trunk`.
- Не путать с prod `drivebit.ru` (другой skill: ship-release).
- Пагинация nearby list — отдельная (клиентская нарезка `cars` + `page` в URL); не смешивать с серверной пагинацией «Все».

## История правок

| Дата | Что изменили |
|------|----------------|
| 2026-07-24 | Первичный e2e после PR #306; задокументирован баг «Список»; чеклист A–F |
| 2026-07-24 | После merge #307: E4 ✅ (Список); обновлены SHA / Pages run |
