# Search Pagination (URL-driven) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Lock 1-based URL `page` for searchApp (deep link, next page, filter→reset page) with TDD, including `applySearchFilterChange`.

**Architecture:** URL remains the source of truth. `parseSearchUrl` / `buildSearchUrl` already encode 1-based `page`. Add a pure helper that forces `page = 1` on any non-page filter change; ViewModel keeps only `load(url)`; SearchApp navigates via `buildSearchUrl` + `pushState`. Do not touch main map (`CarSearchMainRepositoryImpl` / 0-based `CurrentFilters`).

**Tech Stack:** Kotlin Multiplatform, kotlinx-coroutines-test, kotlin.test, Gradle modules `Utils`, `SearchCore`, `searchApp`

**Spec:** `docs/superpowers/specs/2026-07-18-search-pagination-url-design.md`

## Global Constraints

- Search-bundle only: Utils + SearchCore + searchApp — not main map pagination
- URL `page` is **1-based**, same as API `page`
- Omit `page` from query when value is 1
- `FiltersCarSearchRepository.setPage` stays a no-op; do not rely on it
- Iron Law: failing test first, then minimal implementation
- No comments unless required for clarity in plan code samples

---

## File structure

| File | Role |
|------|------|
| `Utils/.../SearchUrl.kt` | Already builds/parses `page`; only change if a new test reveals a gap |
| `Utils/.../SearchUrlTest.kt` | Dedicated page build / omit / round-trip locks |
| `SearchCore/.../SearchCarRepository.kt` | `SearchFilterSet` + `applySearchFilterChange` next to it |
| `SearchCore/.../ApplySearchFilterChangeTest.kt` | Helper unit tests (new) |
| `SearchCore/.../SearchViewModelTest.kt` | `load(?page=2)` → factory gets `filters.page == 2` |
| `SearchCore/.../SearchCarRepositoryTest.kt` | Already asserts API `page=2`; keep / strengthen if needed |
| `searchApp/.../SearchApp.kt` | Use helper for filter chip; keep next-page as `copy(page=…)` only |

---

### Task 1: Lock SearchUrl page build / omit / round-trip

**Files:**
- Modify: `Utils/src/commonTest/kotlin/my/drivebit/utils/SearchUrlTest.kt`
- Modify (only if a test fails): `Utils/src/commonMain/kotlin/my/drivebit/utils/SearchUrl.kt`
- Test: `Utils/src/commonTest/kotlin/my/drivebit/utils/SearchUrlTest.kt`

**Interfaces:**
- Consumes: `buildSearchUrl(SearchUrlParts): String`, `parseSearchUrl(url: String): SearchUrlParts`, `SearchUrlParts.page: Int`
- Produces: locked contract — `page>1` in query, `page=1` omitted, round-trip preserves `page`

- [ ] **Step 1: Write the failing (or characterization) tests**

Add these three tests to `SearchUrlTest` (do not remove existing coverage; these are the explicit spec cases):

```kotlin
@Test
fun `build includes page when greater than 1`() {
    val url = buildSearchUrl(SearchUrlParts(citySlug = "moskva", page = 2))
    assertEquals("/moskva/search?page=2", url)
}

@Test
fun `build omits page query when page is 1`() {
    val url = buildSearchUrl(SearchUrlParts(citySlug = "moskva", page = 1))
    assertEquals("/moskva/search", url)
    assertFalse(url.contains("page="))
}

@Test
fun `round trip preserves page 3 on city search`() {
    val url = buildSearchUrl(SearchUrlParts(citySlug = "moskva", page = 3))
    assertEquals("/moskva/search?page=3", url)
    assertEquals(3, parseSearchUrl(url).page)
    assertEquals(url, buildSearchUrl(parseSearchUrl(url)))
}

@Test
fun `parse invalid or missing page becomes 1`() {
    assertEquals(1, parseSearchUrl("/moskva/search").page)
    assertEquals(1, parseSearchUrl("/moskva/search?page=0").page)
    assertEquals(1, parseSearchUrl("/moskva/search?page=-2").page)
    assertEquals(1, parseSearchUrl("/moskva/search?page=abc").page)
}
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :Utils:cleanTest :Utils:test --tests "my.drivebit.utils.SearchUrlTest"`

Expected: all PASS if current `SearchUrl.kt` is correct (characterization). If any FAIL, go to Step 3; otherwise skip Step 3.

- [ ] **Step 3: Minimal fix only if needed**

In `SearchUrl.kt`, keep:

- parse: `page = params["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1`
- build: `if (parts.page > 1) { queryPairs.add("page" to parts.page.toString()) }`

Re-run Step 2 until PASS.

- [ ] **Step 4: Commit**

```bash
git add Utils/src/commonTest/kotlin/my/drivebit/utils/SearchUrlTest.kt \
  Utils/src/commonMain/kotlin/my/drivebit/utils/SearchUrl.kt
git commit -m "$(cat <<'EOF'
test(utils): lock search URL page encode/decode contract

EOF
)"
```

---

### Task 2: `applySearchFilterChange` helper (TDD)

**Files:**
- Create: `SearchCore/src/commonTest/kotlin/my/drivebit/search/ApplySearchFilterChangeTest.kt`
- Modify: `SearchCore/src/commonMain/kotlin/my/drivebit/search/SearchCarRepository.kt` (add helper after `SearchFilterSet`)
- Test: `SearchCore/src/commonTest/kotlin/my/drivebit/search/ApplySearchFilterChangeTest.kt`

**Interfaces:**
- Consumes: `SearchFilterSet` data class (existing)
- Produces:

```kotlin
inline fun applySearchFilterChange(
    current: SearchFilterSet,
    transform: (SearchFilterSet) -> SearchFilterSet,
): SearchFilterSet
```

Returns `transform(current).copy(page = 1)` always.

- [ ] **Step 1: Write the failing tests**

Create `ApplySearchFilterChangeTest.kt`:

```kotlin
package my.drivebit.search

import kotlin.test.Test
import kotlin.test.assertEquals

class ApplySearchFilterChangeTest {
    @Test
    fun `filter change resets page to 1`() {
        val current =
            SearchFilterSet(
                citySlug = "moskva",
                seatsMin = 2,
                page = 3,
            )
        val updated =
            applySearchFilterChange(current) { it.copy(seatsMin = 5) }
        assertEquals(5, updated.seatsMin)
        assertEquals(1, updated.page)
        assertEquals("moskva", updated.citySlug)
    }

    @Test
    fun `next page uses copy not filter-change helper`() {
        val current =
            SearchFilterSet(
                citySlug = "moskva",
                seatsMin = 5,
                page = 1,
            )
        val next = current.copy(page = 2)
        assertEquals(2, next.page)
        assertEquals(5, next.seatsMin)
        assertEquals("moskva", next.citySlug)
    }
}
```

- [ ] **Step 2: Run tests — expect FAIL**

Run: `./gradlew :SearchCore:cleanTest :SearchCore:test --tests "my.drivebit.search.ApplySearchFilterChangeTest"`

Expected: FAIL — unresolved reference `applySearchFilterChange` (or similar compile error).

- [ ] **Step 3: Minimal implementation**

In `SearchCarRepository.kt`, immediately after the `SearchFilterSet` data class closing brace, add:

```kotlin
inline fun applySearchFilterChange(
    current: SearchFilterSet,
    transform: (SearchFilterSet) -> SearchFilterSet,
): SearchFilterSet = transform(current).copy(page = 1)
```

Do not change `SearchCarRepositoryImpl` in this task.

- [ ] **Step 4: Run tests — expect PASS**

Run: `./gradlew :SearchCore:cleanTest :SearchCore:test --tests "my.drivebit.search.ApplySearchFilterChangeTest"`

Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add SearchCore/src/commonMain/kotlin/my/drivebit/search/SearchCarRepository.kt \
  SearchCore/src/commonTest/kotlin/my/drivebit/search/ApplySearchFilterChangeTest.kt
git commit -m "$(cat <<'EOF'
feat(search): reset page on filter change via helper

EOF
)"
```

---

### Task 3: ViewModel passes URL page into repository factory

**Files:**
- Modify: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchViewModelTest.kt`
- Modify (only if test fails): `SearchCore/src/commonMain/kotlin/my/drivebit/search/SearchViewModel.kt`
- Test: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchViewModelTest.kt`

**Interfaces:**
- Consumes: `SearchViewModel.load(url: String)`, `repositoryFactory: (SearchFilterSet) -> SearchCarRepository`
- Produces: `load("/moskva/search?page=2")` invokes factory with `filters.page == 2`

- [ ] **Step 1: Write the failing test**

Append to `SearchViewModelTest`:

```kotlin
@Test
fun `load url with page 2 passes page to repository factory`() =
    runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var capturedPage: Int? = null
        val vm =
            SearchViewModel(
                repositoryFactory = { filters ->
                    capturedPage = filters.page
                    object : SearchCarRepository {
                        override val results: Flow<SearchCarsResult> =
                            flowOf(SearchCarsResult(totalCount = 0, totalPages = 3))
                    }
                },
                brandResolver = { null },
                modelResolver = { _, _ -> null },
                coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
            )

        vm.load("/moskva/search?page=2")
        advanceUntilIdle()

        assertEquals(2, capturedPage)
        val results = assertIs<SearchUiState.Results>(vm.state.value)
        assertEquals(2, results.filters.page)
    }
```

- [ ] **Step 2: Run test**

Run: `./gradlew :SearchCore:cleanTest :SearchCore:test --tests "my.drivebit.search.SearchViewModelTest"`

Expected: PASS if `resolveFilters` already copies `parts.page` (current code does). If FAIL, Step 3.

- [ ] **Step 3: Minimal fix only if needed**

Ensure `SearchViewModel.resolveFilters` sets `page = parts.page` on `SearchFilterSet` (already present around line 87). Do not add live `setPage` calls.

- [ ] **Step 4: Commit**

```bash
git add SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchViewModelTest.kt \
  SearchCore/src/commonMain/kotlin/my/drivebit/search/SearchViewModel.kt
git commit -m "$(cat <<'EOF'
test(search): assert ViewModel forwards URL page to repository

EOF
)"
```

---

### Task 4: Repository API page assertion (lock)

**Files:**
- Modify: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchCarRepositoryTest.kt` (only if assertion missing)
- Test: same file

**Interfaces:**
- Consumes: `SearchCarRepositoryImpl(api, filters, cityIdResolver)`, `SearchCarsApi.searchCars(..., page: Int, ...)`
- Produces: explicit assert `api.lastPage == 2` when `filters.page == 2`

- [ ] **Step 1: Verify existing test covers spec case 7**

Open `SearchCarRepositoryTest`. Confirm it already has `page = 2` on filters and `assertEquals(2, api.lastPage)`.

If present: no code change — run Step 2 and commit nothing (or a no-op skip).

If missing: add / fix so `filters.page = 2` → `assertEquals(2, api.lastPage)`.

- [ ] **Step 2: Run test**

Run: `./gradlew :SearchCore:cleanTest :SearchCore:test --tests "my.drivebit.search.SearchCarRepositoryTest"`

Expected: PASS.

- [ ] **Step 3: Commit only if you changed the test**

```bash
git add SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchCarRepositoryTest.kt
git commit -m "$(cat <<'EOF'
test(search): assert repository passes filters.page to API

EOF
)"
```

---

### Task 5: Wire SearchApp filter chip through helper

**Files:**
- Modify: `searchApp/src/jsMain/kotlin/my/drivebit/search/SearchApp.kt`

**Interfaces:**
- Consumes: `applySearchFilterChange(current, transform)`, `SearchFilterSet.copy(page = …)` for next page only, existing `navigateSearch`
- Produces: seats chip uses helper (always resets page); next-page button never calls helper

- [ ] **Step 1: Replace seats navigation to use helper**

In `SearchApp.kt`, change the seats button from:

```kotlin
navigateSearch(s.filters.copy(seatsMin = 5, page = 1))
```

to:

```kotlin
navigateSearch(
    applySearchFilterChange(s.filters) { it.copy(seatsMin = 5) },
)
```

Keep next-page as:

```kotlin
val nextPage = (s.filters.page + 1).coerceAtMost(s.result.totalPages)
navigateSearch(s.filters.copy(page = nextPage))
```

Do not call `applySearchFilterChange` for next page.

No import needed if same package `my.drivebit.search`; helper is in that package.

- [ ] **Step 2: Compile searchApp js**

Run: `./gradlew :searchApp:compileKotlinJs`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add searchApp/src/jsMain/kotlin/my/drivebit/search/SearchApp.kt
git commit -m "$(cat <<'EOF'
refactor(searchApp): reset page via applySearchFilterChange

EOF
)"
```

---

### Task 6: Full module verification

**Files:** none (verification only)

- [ ] **Step 1: Run focused checks**

Run:

```bash
./gradlew :Utils:check :SearchCore:check :searchApp:compileKotlinJs
```

Expected: exit code 0, all tests green.

- [ ] **Step 2: Confirm out-of-scope untouched**

```bash
git diff trunk -- Repositories/src/commonMain/kotlin/my/drivebit/repositories/CarSearchRepository.kt | head
```

Expected: no pagination / `setPage` / `CarSearchMainRepositoryImpl` changes from this work (empty or unrelated). If this branch already had earlier search-bundle commits touching `FiltersCarSearchRepository`, that is fine — do **not** change `setPage` from no-op or touch main 0-based page logic in this plan’s commits.

- [ ] **Step 3: Final commit only if verification forced small fixes**

Otherwise no commit.

---

## Spec coverage checklist

| Spec requirement | Task |
|------------------|------|
| Build `page=2` in URL | Task 1 |
| Omit `page` when 1 | Task 1 |
| Round-trip `page=3` | Task 1 |
| Invalid/missing page → 1 | Task 1 |
| `applySearchFilterChange` resets page | Task 2 |
| Next page via `copy`, not helper | Task 2 + Task 5 |
| VM `load(?page=2)` → filters.page=2 | Task 3 |
| Repo API called with page=2 | Task 4 |
| SearchApp filter uses helper | Task 5 |
| Main map untouched | Task 6 |
| `setPage` stays no-op | Global + Task 6 |

## Self-review notes

- No placeholders; signatures match existing `SearchFilterSet` / `SearchUrlParts`.
- Helper lives in SearchCore (same type SearchApp uses); Utils stays URL-only.
- Existing SearchUrl / SearchCarRepository coverage is mostly characterization — still add explicit spec tests where named in the design.
