# SearchApp UI Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace searchApp text MVP with old SearchPage visual parity (dates, chips, overlays, CarsGrid, PaginationBar) while keeping URL-driven SearchCore state.

**Architecture:** Add DrivebitWeb-stack deps like `carDetailApp`, bootstrap via `WebKoinHost`, map API results to `Network.CarItem`, convert PaginationBar’s 0-based index ↔ URL 1-based `page`, and wire every filter/date/page/reset through `applySearchFilterChange` / `buildSearchUrl` / `pushState` / `load(url)`.

**Tech Stack:** Kotlin/JS Compose HTML, Koin (`WebKoinHost`), SearchCore, DrivebitWeb components, Network `CarItem`, Utils SearchUrl

**Spec:** `docs/superpowers/specs/2026-07-18-searchapp-ui-parity-design.md`

## Global Constraints

- URL is source of truth for filters/page — no Storage-backed `SearchFiltersRepository` as filter state
- Visual parity with deleted SearchPage (H1, dates, chips + overlays, CarsGrid, PaginationBar)
- URL/API `page` is **1-based**; `PaginationBar` is **0-based**
- Non-page filter changes use `applySearchFilterChange` → `page = 1`
- Storage may appear transitively via `WebKoinHost`; must not own search filters
- Prefer `Network.CarItem` for grid (not parallel text cards)
- Iron Law: failing test first for pure helpers / mapping; UI port may use characterization + compile

---

## File structure

| File | Role |
|------|------|
| `Utils/.../SearchPagination.kt` (new) | `urlPageToUiIndex` / `uiIndexToUrlPage` |
| `Utils/.../SearchPaginationTest.kt` (new) | TDD for page conversion |
| `SearchCore/build.gradle.kts` | Add `:Network` |
| `SearchCore/.../SearchCarRepository.kt` | `SearchCarsResult.cars: List<CarItem>`; drop `SearchCarCard` if unused |
| `SearchCore/.../HttpSearchCarsApi.kt` | Deserialize `CarDTOPagedResult` / map to `CarItem` |
| `SearchCore/.../*Test.kt` | Update fixtures to `CarItem` |
| `searchApp/build.gradle.kts` | Deps like carDetailApp + SearchCore + Network |
| `searchApp/.../SearchApp.kt` | Full UI port + URL navigation helpers |
| `searchApp/.../main.kt` | `WebKoinHost` + SearchViewModel binding with real resolvers |
| `searchApp/.../SearchNavigation.kt` (optional new) | `filtersToSearchUrl` / `navigateSearchFilters` |

Reference old UI: `git show 002faa45:DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/SearchPage.kt`

---

### Task 1: Pagination index helpers (TDD)

**Files:**
- Create: `Utils/src/commonMain/kotlin/my/drivebit/utils/SearchPagination.kt`
- Create: `Utils/src/commonTest/kotlin/my/drivebit/utils/SearchPaginationTest.kt`

**Interfaces:**
- Produces:

```kotlin
fun urlPageToUiIndex(page: Int): Int // 1-based → 0-based, coerce page ≥ 1
fun uiIndexToUrlPage(uiIndex: Int): Int // 0-based → 1-based, coerce uiIndex ≥ 0
```

- [ ] **Step 1: Write failing tests**

```kotlin
package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchPaginationTest {
    @Test
    fun `url page 1 maps to ui index 0`() {
        assertEquals(0, urlPageToUiIndex(1))
        assertEquals(0, urlPageToUiIndex(0))
        assertEquals(0, urlPageToUiIndex(-3))
    }

    @Test
    fun `url page N maps to ui index N-1`() {
        assertEquals(1, urlPageToUiIndex(2))
        assertEquals(4, urlPageToUiIndex(5))
    }

    @Test
    fun `ui index maps back to url page`() {
        assertEquals(1, uiIndexToUrlPage(0))
        assertEquals(3, uiIndexToUrlPage(2))
        assertEquals(1, uiIndexToUrlPage(-1))
    }
}
```

- [ ] **Step 2: Run — expect FAIL**

Run: `./gradlew :Utils:testDebugUnitTest --tests "my.drivebit.utils.SearchPaginationTest"`

Expected: unresolved reference / FAIL.

- [ ] **Step 3: Implement**

```kotlin
package my.drivebit.utils

fun urlPageToUiIndex(page: Int): Int = page.coerceAtLeast(1) - 1

fun uiIndexToUrlPage(uiIndex: Int): Int = uiIndex.coerceAtLeast(0) + 1
```

- [ ] **Step 4: Run — expect PASS**

Same Gradle command → PASS.

- [ ] **Step 5: Commit**

```bash
git add Utils/src/commonMain/kotlin/my/drivebit/utils/SearchPagination.kt \
  Utils/src/commonTest/kotlin/my/drivebit/utils/SearchPaginationTest.kt
git commit -m "$(cat <<'EOF'
feat(utils): map search URL page to PaginationBar index

EOF
)"
```

---

### Task 2: SearchCore results use `CarItem`

**Files:**
- Modify: `SearchCore/build.gradle.kts` — `commonMain` `implementation(project(":Network"))`
- Modify: `SearchCore/src/commonMain/kotlin/my/drivebit/search/SearchCarRepository.kt`
- Modify: `SearchCore/src/commonMain/kotlin/my/drivebit/search/HttpSearchCarsApi.kt`
- Modify: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchCarRepositoryTest.kt`
- Modify: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchViewModelTest.kt`

**Interfaces:**
- Consumes: `my.drivebit.network.services.CarItem`, `CarDTOPagedResult`, `CarGeneral`, `CarAddress`, `CarPhotoItem`
- Produces: `SearchCarsResult(cars: List<CarItem>, totalCount: Int, totalPages: Int)`
- Removes: `SearchCarCard` (replace all usages)

- [ ] **Step 1: Failing / broken compile by changing type first in tests**

In `SearchCarRepositoryTest` and `SearchViewModelTest`, replace `SearchCarCard(...)` with a minimal `CarItem` fixture:

```kotlin
fun testCarItem(id: String = "1", brand: String = "BMW", model: String = "X5") =
    CarItem(
        id = id,
        year = 2020,
        price = 5000.0,
        photos = emptyList(),
        general =
            CarGeneral(
                brandName = brand,
                modelName = model,
                seats = 5,
                address = CarAddress(), // use real defaults from Network CarAddress
            ),
    )
```

Assert `result.cars[0].general.brandName` instead of `.title` where needed.

If `CarAddress()` needs fields, copy a minimal valid address from Network tests / `CarItemPhotosTest`.

- [ ] **Step 2: Change production types**

In `SearchCarRepository.kt`:

```kotlin
import my.drivebit.network.services.CarItem

data class SearchCarsResult(
    val cars: List<CarItem> = emptyList(),
    val totalCount: Int = 0,
    val totalPages: Int = 0,
)
```

Delete `SearchCarCard`.

- [ ] **Step 3: Rewrite `HttpSearchCarsApi` to use Network DTO**

Prefer:

```kotlin
val paged: CarDTOPagedResult = httpClient.get(url) { ... }.body()
return SearchCarsResult(
    cars = paged.items,
    totalCount = paged.totalCount,
    totalPages = paged.totalPages,
)
```

Remove private `CarDto*` duplicates if Network types cover them. Keep `resolveSearchCityId` as-is.

- [ ] **Step 4: Run tests**

```bash
./gradlew :SearchCore:testDebugUnitTest --tests "my.drivebit.search.*"
```

Expected: PASS (exit 0).

- [ ] **Step 5: Commit**

```bash
git add SearchCore/
git commit -m "$(cat <<'EOF'
feat(search): return Network CarItem from search API mapping

EOF
)"
```

---

### Task 3: searchApp Gradle dependencies

**Files:**
- Modify: `searchApp/build.gradle.kts`

**Interfaces:**
- Produces: compile-time access to DrivebitWeb components + `WebKoinHost`

- [ ] **Step 1: Mirror carDetailApp deps + keep SearchCore/Ktor**

In `jsMain.dependencies`:

```kotlin
implementation(project(":SearchCore"))
implementation(project(":Utils"))
implementation(project(":DrivebitWeb"))
implementation(project(":WebShell"))
implementation(project(":AppHeader"))
implementation(project(":Storage")) // transitive need for WebKoinHost modules
implementation(project(":Repositories"))
implementation(project(":CommonViewModels"))
implementation(project(":Network"))
implementation(compose.html.core)
implementation(compose.runtime)
implementation(libs.koin.core)
implementation(libs.koin.compose)
implementation(libs.koinComposeViewmodelJs)
implementation(libs.kotlinx.coroutines.core)
implementation(libs.kotlinx.serialization.json)
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.js)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
```

Also copy `jsProcessResources` vendor copy from `carDetailApp` if SearchApp needs shared CSS from `/vendor` at runtime (static HTML already references `/vendor/...` from Pages root — usually OK). If compile/runtime missing assets locally, add the same `from(rootProject.../vendor)` block as carDetailApp.

- [ ] **Step 2: Compile dry-run**

```bash
./gradlew :searchApp:compileKotlinJs
```

Expected: may fail until UI/main updated — OK if only missing symbols in SearchApp; fix dep errors now.

- [ ] **Step 3: Commit**

```bash
git add searchApp/build.gradle.kts
git commit -m "$(cat <<'EOF'
build(searchApp): add DrivebitWeb stack deps for UI parity

EOF
)"
```

---

### Task 4: URL navigation helpers for SearchFilterSet

**Files:**
- Create: `searchApp/src/jsMain/kotlin/my/drivebit/search/SearchNavigation.kt`
- Optional test in Utils if logic is pure — prefer keeping `filtersToParts` pure in SearchCore/Utils if easy; otherwise jsMain helper only.

**Interfaces:**
- Produces:

```kotlin
fun SearchFilterSet.toSearchUrlParts(): SearchUrlParts
fun navigateSearchFilters(filters: SearchFilterSet) // pushState(buildSearchUrl(...))
```

- [ ] **Step 1: Implement `toSearchUrlParts`**

Map all fields currently in `SearchApp.navigateSearch` (`citySlug` default `moskva` when no brand, brand/model slugs, dates, rates, drive/body, seats, years, mileage, page).

Brand path: if `brandSlug` / `brandName` set, prefer existing `searchPathForBrandName` / slug helpers from DrivebitWeb/Utils so bmw/audi stay short.

- [ ] **Step 2: `navigateSearchFilters`**

```kotlin
fun navigateSearchFilters(filters: SearchFilterSet) {
    val url = buildSearchUrl(filters.toSearchUrlParts())
    window.history.pushState(null, "", url)
}
```

- [ ] **Step 3: Compile**

`./gradlew :searchApp:compileKotlinJs` — may still fail on unfinished SearchApp; helpers must typecheck.

- [ ] **Step 4: Commit**

```bash
git add searchApp/src/jsMain/kotlin/my/drivebit/search/SearchNavigation.kt
git commit -m "$(cat <<'EOF'
feat(searchApp): centralize filter set to search URL navigation

EOF
)"
```

---

### Task 5: Bootstrap Koin + real brand/model resolvers

**Files:**
- Modify: `searchApp/src/jsMain/kotlin/main.kt`

**Interfaces:**
- Consumes: `WebKoinHost`, `BrandSlugResolver`, SearchCore `SearchViewModel`, `HttpSearchCarsApi` or Network `Car` service
- Produces: injectable `SearchViewModel` with non-null resolvers

- [ ] **Step 1: Replace custom `startKoin` with WebKoinHost pattern**

```kotlin
fun main() {
    renderComposable(rootElementId = "root") {
        WebKoinHost {
            // ensure SearchViewModel single is available — see Step 2
            SearchApp()
        }
    }
}
```

- [ ] **Step 2: Register SearchViewModel**

After Koin starts (module load or `loadKoinModules`):

```kotlin
single {
    val brandSlugResolver: BrandSlugResolver = get()
    SearchViewModel(
        repositoryFactory = { filters ->
            SearchCarRepositoryImpl(
                api = HttpSearchCarsApi(get()), // or adapter over Network Car
                filters = filters,
                cityIdResolver = ::resolveSearchCityId,
            )
        },
        brandResolver = { slug ->
            brandSlugResolver.resolve(slug)?.let { it.id to it.name }
        },
        modelResolver = { brandId, modelSlug ->
            // use CarModelViewModel / existing repository API used by BrandModelFilter
            // look up how BrandModelFilter resolves models; wire the same service
            ...
        },
    )
}
```

Inspect `BrandSlugResolver.ResolvedBrand` and model list APIs in CommonViewModels/Repositories; reuse the same lookup BrandModelFilter uses. If model resolution is heavy, implement minimal slug→id via existing model service used by `CarModelViewModel`.

- [ ] **Step 3: Compile**

`./gradlew :searchApp:compileKotlinJs`

- [ ] **Step 4: Commit**

```bash
git add searchApp/src/jsMain/kotlin/main.kt
git commit -m "$(cat <<'EOF'
feat(searchApp): boot WebKoinHost with real brand resolvers

EOF
)"
```

---

### Task 6: Port SearchPage UI into SearchApp

**Files:**
- Modify: `searchApp/src/jsMain/kotlin/my/drivebit/search/SearchApp.kt` (replace MVP)
- Possibly create: `searchApp/src/jsMain/kotlin/my/drivebit/search/SearchPageContent.kt` if file > ~400 lines

**Interfaces:**
- Consumes: DrivebitWeb components listed in spec; `SearchViewModel.state`; helpers from Tasks 1 & 4
- Produces: visual parity UI

- [ ] **Step 1: Recover reference**

```bash
git show 002faa45:DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/SearchPage.kt > /tmp/SearchPage.ref.kt
```

Use as layout reference — do not restore Storage repo calls.

- [ ] **Step 2: Structure**

Keep existing URL `locationHref` + `popstate` + `viewModel.load(locationHref)`.

Render from `SearchUiState`:

- Loading → H1 + `Loader`
- Error → H1 + `TextError`
- Results → full column:
  - `SearchPageHeadline(resolveSearchPageHeadline(...))`
  - `SearchDateRangeSelector` → on change `navigateSearchFilters(applySearchFilterChange(filters) { it.copy(startDate=...) })` then refresh `locationHref`
  - Filter chips + overlays (Price/Brand/Drive/Body/Seats/Year/Mileage) → same URL pattern
  - `CarsGrid(cars = s.result.cars, carHref = { buildCarDetailUrl(it.id, s.filters.startDate, s.filters.endDate) })`
  - `PaginationBar(currentPage = urlPageToUiIndex(s.filters.page), totalPages = s.result.totalPages, totalCount = s.result.totalCount, pageSize = 9) { ui -> navigateSearchFilters(s.filters.copy(page = uiIndexToUrlPage(ui))); locationHref = ... }`
  - Empty → “Автомобили не найдены”
- Keep root class `drivebit-cars-grid-mounted` (CarsGrid also adds it — redundant OK)

Overlay show flags: local `remember { mutableStateOf(false) }` only.

Reset chip: clear filters to city-only `SearchFilterSet(citySlug = ...)` via URL (no brand path).

Brand overlay: on brand select, set brandSlug/name/id and navigate (path may change to `/bmw` etc.).

- [ ] **Step 3: Prefer `#root`-only content**

Do **not** require `AppWithHeader` if static HTML already has header (match current search HTML shell). If double headers appear, skip `AppWithHeader`.

- [ ] **Step 4: Compile**

```bash
./gradlew :searchApp:compileKotlinJs
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add searchApp/src/jsMain/kotlin/my/drivebit/search/
git commit -m "$(cat <<'EOF'
feat(searchApp): restore SearchPage UI on URL-driven state

EOF
)"
```

---

### Task 7: Verification

**Files:** none (commands only)

- [ ] **Step 1: Module checks**

```bash
./gradlew :Utils:testDebugUnitTest --tests "my.drivebit.utils.SearchPaginationTest" \
  :SearchCore:testDebugUnitTest \
  :searchApp:compileKotlinJs
```

Expected: exit 0.

- [ ] **Step 2: Sanity grep**

Confirm SearchApp does not call deleted `SearchFiltersRepository` / `viewModel.updateDailyRate*` patterns.

- [ ] **Step 3: Manual note for pages-dev**

After merge (separate e2e skill): `/moskva/search` shows cards + chips; API `158835`; skeleton hidden.

- [ ] **Step 4: Commit only if fixes required**

---

## Spec coverage

| Spec item | Task |
|-----------|------|
| DrivebitWeb deps / carDetailApp pattern | 3, 5 |
| URL-driven filters | 4, 6 |
| CarItem mapping | 2 |
| Pagination 0↔1 | 1, 6 |
| H1, dates, chips, overlays, grid, pagination | 6 |
| Real brand resolvers | 5 |
| No Storage filter source of truth | 5–6 constraints |
| Skeleton hidden | 6 (`CarsGrid` / mounted class) |

## Self-review notes

- Plan prefers Network `CarDTOPagedResult` over bespoke DTOs to avoid double mapping.
- `WebKoinHost` pulls Storage modules — acceptable transitively; filters stay URL-only.
- Model resolver wiring left as “inspect BrandModelFilter” — implementer must follow existing API, not invent a new dictionary client.
