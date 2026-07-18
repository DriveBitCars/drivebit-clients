# Search pagination (URL-driven) — design

Date: 2026-07-18  
Status: approved in brainstorming (approach 1)  
Scope: searchApp / SearchCore / Utils only — not main map pagination

## Problem

URL-driven search must keep pagination correct:

- Opening `/moskva/search?page=2` must request API page **2** (1-based).
- “Next page” must advance `page` in the URL and reload.
- Changing any filter must reset `page` to **1** so users are not stuck on an empty high page.

Risk: mixing main’s 0-based `CurrentFilters.currentPage` with search’s 1-based URL `page`, or relying on `FiltersCarSearchRepository.setPage` (intentionally a no-op).

## Decision

**Approach 1 — contract in Utils + SearchCore helpers/tests.**

Source of truth remains the full search URL. Page changes and filter changes produce a new URL; ViewModel only `load(url)`.

## URL contract

| Rule | Detail |
|------|--------|
| Query key | `page` |
| Indexing | **1-based**, same as `carService.search(page=…)` |
| Omit default | Do not write `page` when value is 1 |
| Path | Unchanged by page (brand/model stay in path only) |
| Out of scope | `CarSearchMainRepositoryImpl` / `CurrentFilters` 0-based page |

## Data flow

```
URL (pathname + search)
  → SearchViewModel.load(url)
  → parseSearchUrl → SearchFilterSet (page ≥ 1)
  → SearchCarRepository / FiltersCarSearchRepository(filters)
  → API search(page = filters.page)
  → UI Results(totalPages, filters.page)

Next page:
  filters.copy(page = min(page+1, totalPages))
  → buildSearchUrl → history.pushState → load(newUrl)

Filter change:
  applySearchFilterChange(current) { … }
  → forces page = 1
  → buildSearchUrl → pushState → load(newUrl)
```

`FiltersCarSearchRepository.setPage` stays a no-op. Pagination must not depend on it.

## Components

### `applySearchFilterChange` (Utils or SearchCore)

Pure helper used whenever a non-page filter changes:

- Input: current `SearchFilterSet` (or `SearchUrlParts`) + transform of filter fields.
- Output: updated set with **`page = 1`**.
- Must not be used for next/prev page (those only `copy(page = …)`).

### Existing pieces (unchanged roles)

- `parseSearchUrl` / `buildSearchUrl` — encode/decode `page`.
- `SearchViewModel.load` — parse URL, resolve brand/model, call repository with that page.
- Search UI — navigate via `buildSearchUrl` + `pushState`, then refresh from location.

## Error handling

- Invalid/missing `page` → treat as 1.
- `page < 1` after parse → coerce to 1.
- Next page coerced with `totalPages` so UI does not navigate past last page.
- Repository/API errors → existing `SearchUiState.Error`; page in URL unchanged until user navigates again.

## Testing (TDD)

Iron Law: failing test first, then minimal implementation.

### Utils — `SearchUrlTest`

1. Build with `page=2` → URL contains `page=2`.
2. Build with `page=1` → URL does **not** contain `page=`.
3. Round-trip `/moskva/search?page=3` → parse → build → still `page=3`.

### Helper — `applySearchFilterChange`

4. Given `page=3`, change `seatsMin` → result `page=1` (and new seats).
5. Next-page path: `copy(page = 2)` keeps other filters; does **not** go through filter-change helper.

### SearchViewModel / repository

6. `load("/moskva/search?page=2")` → repository factory receives `filters.page == 2`.
7. Repository with `filters.page=2` → `carService.search` / API called with `page=2` (assert explicitly).

### Out of scope for this spec

- Main home list pagination (`CarSearchMainRepositoryImpl`).
- Browser/screenshot e2e (covered by pages-dev skill separately).
- Changing `setPage` on `FiltersCarSearchRepository` to mutate state.

## Success criteria

- Deep link with `?page=N` loads that API page.
- Next page updates URL and results.
- Any filter change clears `page` from URL (back to implicit page 1).
- All listed unit tests green; main map pagination untouched.
