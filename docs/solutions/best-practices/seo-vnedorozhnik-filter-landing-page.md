---
title: SEO landing page for body-type filter (Внедорожник / SUV)
date: 2026-07-02
last_updated: 2026-07-02
category: best-practices
module: composeApp
problem_type: best_practice
component: seo
severity: low
applies_when:
  - Adding a new Moscow filter button that filters by body type, not trip purpose
  - Replacing an existing trip filter with a long-tail SEO URL (e.g. arenda-vnedorozhnika-bez-voditelya)
  - Content spec lives in Google Doc + spreadsheet (title, description, H1, full SEO text)
tags:
  - seo
  - vnedorozhnik
  - suv
  - landing-page
  - filter-button
  - moskva
related_releases:
  - v3.16.8
  - v3.16.9
related_prs:
  - https://github.com/DriveBitCars/drivebit-clients/pull/254
  - https://github.com/DriveBitCars/drivebit-clients/pull/255
---

# SEO landing page for body-type filter (Внедорожник / SUV)

## Context

Нужно было заменить кнопку фильтра **«Мероприятие»** на **«Внедорожник»** с фильтрацией по типу кузова `SUV`, выделить отдельный SEO-URL и положить в статический HTML полный текст из контент-спека (Google Doc + таблица SEO).

Первая итерация (v3.16.8) добавила страницу и укороченный SEO-блок. Вторая (v3.16.9) дополнила полный текст перед футером — таблица премиум-класса, списки, все разделы из документа.

**Целевой URL:** `https://drivebit.ru/moskva/arenda-vnedorozhnika-bez-voditelya`

**Мета из спеки:**

| Поле | Значение |
|------|----------|
| Title | Аренда внедорожника без водителя в Москве через сервис DriveBit |
| Description | Аренда внедорожника без водителя в Москве через сервис DriveBit. Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга! |
| H1 | Аренда внедорожника без водителя в Москве |

## Guidance

### 1. Фильтр по типу кузова (Kotlin)

В `SuggestedFiltersCatalog.kt` заменить старый trip-filter на body-type filter:

```kotlin
FilterSuggestion(
    name = "Внедорожник",
    shortName = "Внедорожник",
    iconUrl = "/images/filter-main/direction.svg",
    bodyTypes = listOf(
        EnumItem(number = 0, name = "SUV", translate = "Внедорожник"),
    ),
),
```

`CarSearchRepository` автоматически передаёт `bodyTypes = ["SUV"]` при `currentTaskShortName == "Внедорожник"`.

### 2. Кастомный SEO-slug в URL

Обычные фильтры slug'ятся через `cityNameToSlug` (`Путешествия` → `puteshestviya`). Для внедорожника нужен длинный slug из спеки.

`DrivebitWeb/.../CityPathRouting.kt`:

```kotlin
private val CUSTOM_FILTER_PATH_SEGMENTS = mapOf(
    "Внедорожник" to "arenda-vnedorozhnika-bez-voditelya",
)
```

Обратное сопоставление — в `filterTitleFromPathSegment`.

### 3. SEO meta (Kotlin + JS)

- `Utils/.../CityDeclensionUtils.kt` — добавить `"Внедорожник"` в `seoPageHeadline`, slug в `SEO_OPTIMIZED_MOSKVA_FILTER_SLUGS`
- `DrivebitWeb/.../MetaTags.kt` — явная запись для `/moskva/arenda-vnedorozhnika-bez-voditelya`
- `vendor/city-meta-bootstrap.js` — зеркалить slug и headline для статических страниц

### 4. Контент: `landing-blocks.json` + статический HTML

**Источник правды для текста:** `composeApp/seo/landing-blocks.json` → ключ `/moskva/arenda-vnedorozhnika-bez-voditelya`.

Структура с `sections` (как у `/moskva/puteshestviya`): заголовки, параграфы, `bullets`, `table`.

Полный SEO-блок в `index.html` генерируется скриптом:

```bash
python3 -c "
from pathlib import Path
import re
from scripts.seo_blocks import render_block
path = '/moskva/arenda-vnedorozhnika-bez-voditelya'
seo_html = render_block(path)
index_path = Path('composeApp/src/jsMain/resources/moskva/arenda-vnedorozhnika-bez-voditelya/index.html')
text = index_path.read_text(encoding='utf-8')
new_text, count = re.subn(
    r'<div class=\"drivebit-seo-shell\">[\s\S]*?</div>\s*\n',
    seo_html, text, count=1,
)
index_path.write_text(new_text, encoding='utf-8')
"
```

Блок **обязан** стоять в `index.html` перед `drivebit-footer-shell` — краулеры читают initial HTML, а не только runtime-обновление через `SeoBlocks.kt`.

### 5. Статические HTML и кнопки фильтров

Во всех `index.html` (корень, `moskva/*`, другие города, `composeApp/seo/filters.fragment.html`):

- `data-filter-title="Внедорожник"`
- `data-filter-path="/moskva/arenda-vnedorozhnika-bez-voditelya"` (для Москвы; для других городов — `/{city}/arenda-vnedorozhnika-bez-voditelya`)

Удалить `moskva/meropriyatie/` — страница больше не используется.

### 6. Sitemap и CI

- `composeApp/src/jsMain/resources/sitemap.xml` — новый `<loc>`
- `.github/workflows/deploy.yml` — slug `arenda-vnedorozhnika-bez-voditelya` в проверке Moscow SEO pages
- `scripts/print-city-meta-table.mjs` — путь к `index.html`

### 7. Тесты

- `CityPathRoutingTest` — custom slug mapping
- `CarSearchRepositoryTest` — `bodyTypes = listOf("SUV")` вместо старых полей «Мероприятие» (yearMin, dailyPriceMax)

## Why This Matters

- **SEO:** длинный URL и полный статический текст индексируются без ожидания JS.
- **Фильтрация:** кнопка реально отбирает SUV через API (`BodyType=SUV`).
- **Единый источник:** `landing-blocks.json` питает и runtime (`SeoBlocks`), и генерацию static HTML (`scripts/seo_blocks.py`).

## When to Apply

- Новая категория с отдельным long-tail URL (не `cityNameToSlug(shortName)`).
- Фильтр по `bodyTypes` / другим полям каталога, а не по trip-purpose из `SuggestedFiltersCatalog`.
- Контент приходит из Google Doc — сначала `sections` в `landing-blocks.json`, затем `render_block` в `index.html`.

## Examples

**Проверка на проде после релиза:**

```bash
curl -sL "https://drivebit.ru/moskva/arenda-vnedorozhnika-bez-voditelya" | rg "Пробег и дополнительные расходы|Как арендовать внедорожник|<table>"
```

Ожидается: 15× `<h2>`, 1× `<table>`, 6× `<ul>`, блок `drivebit-seo-shell` выше `drivebit-footer-shell`.

## Files touched (reference)

| Область | Файлы |
|---------|--------|
| Filter catalog | `Repositories/.../SuggestedFiltersCatalog.kt` |
| Routing | `DrivebitWeb/.../CityPathRouting.kt`, `FiltersViewModel.kt` |
| Meta | `MetaTags.kt`, `CityDeclensionUtils.kt`, `city-meta-bootstrap.js` |
| Content | `composeApp/seo/landing-blocks.json` |
| Static page | `composeApp/src/jsMain/resources/moskva/arenda-vnedorozhnika-bez-voditelya/index.html` |
| Bulk UI | все `index.html` с filter row, `filters.fragment.html` |
| Infra | `sitemap.xml`, `deploy.yml`, `print-city-meta-table.mjs` |

## Related

- [composeApp/seo/README.md](../../../composeApp/seo/README.md) — общие правила static SEO
- [scripts/seo_blocks.py](../../../scripts/seo_blocks.py) — рендер SEO из JSON
