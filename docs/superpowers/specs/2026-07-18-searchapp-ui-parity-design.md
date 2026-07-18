# SearchApp UI parity (DrivebitWeb components) — design

Date: 2026-07-18  
Status: approved in brainstorming (approach 2)  
Scope: visual parity of old SearchPage inside URL-driven `searchApp`

## Problem

`searchApp` shows a text-only MVP while the polished SearchPage UI (chips, dates, `CarsGrid`, overlays) still lives in DrivebitWeb. Users see raw titles/prices instead of the previous search experience.

## Goals

- Visual parity with deleted `SearchPage` (H1, dates, filter chips + overlays, car cards, pagination).
- Keep **URL as source of truth** for filters/page (`buildSearchUrl` / `parseSearchUrl` / `applySearchFilterChange` / `load(url)`).
- Do **not** restore Storage-backed `SearchFiltersRepository` as filter state.

## Non-goals

- Redesigning search visuals beyond old SearchPage.
- Production release / prod deploy (pages-dev via existing e2e skill only if asked).
- Extracting a separate shared search-UI module (deferred; may follow later).

## Decision

**Approach 2** — same pattern as `carDetailApp`: depend on DrivebitWeb (+ WebShell, AppHeader, Network, CommonViewModels, Repositories as needed for filter VMs/resolvers). Rewrite SearchPage wiring from repo/VM updates to URL navigation. SearchCore remains the search state machine.

## Architecture

```
URL (path + query)
  → SearchViewModel.load
  → SearchFilterSet + API (city id, filters, page 1-based)
  → SearchUiState.Results(cars: CarItem-compatible, totalPages, filters)

UI event (filter/date/page/reset)
  → applySearchFilterChange / copy(page)
  → buildSearchUrl → history.pushState → locationHref → load
```

Overlay open/close flags stay in Compose `remember` (UI-only).

## Module dependencies

`searchApp` adds (as needed for UI + filter VMs):

- `:DrivebitWeb`, `:WebShell`, `:AppHeader`
- `:Network`, `:CommonViewModels`, `:Repositories` (filter/dictionary VMs and resolvers)
- Existing: `:SearchCore`, `:Utils`

Constraint: Storage may appear transitively; it must **not** become the source of truth for search filters.

## Data & pagination

- Enrich `HttpSearchCarsApi` mapping to fields required by `CarItem` / `CarItemSmall` (photos, year, dailyRate tiers). Prefer reusing Network DTOs/`CarItem` over parallel `SearchCarCard` where practical.
- Car detail links: `buildCarDetailUrl(id, startDate, endDate)` from current URL filters.
- URL/API page: **1-based**.
- `PaginationBar`: **0-based** — `currentPage = filters.page - 1`; on change → `page = uiPage + 1` in URL.
- Any non-page filter change: `applySearchFilterChange` → `page = 1`.

## UI structure (parity checklist)

1. Dynamic H1 via `resolveSearchPageHeadline`
2. `SearchDateRangeSelector`
3. Filter chips row + `FiltersResetChip`
4. Results: `CarsGrid` / Loader / TextError / empty copy
5. `PaginationBar`
6. Overlays: Price, BrandModel, DriveType, BodyType, Seats, Year, Mileage

Shell: `AppWithHeader` if required for parity with old page; otherwise keep static HTML header and only replace `#root` content (match other split bundles’ established pattern during implementation).

## Brand / city resolvers

Replace stubs in `searchApp` Koin with real `BrandSlugResolver` / model resolution and city id mapping already fixed (`resolveSearchCityId` → live ids). Brand path updates use existing short-path helpers (`/bmw`, `/audi`, `/search/...`).

## Error handling

- Load failures → `SearchUiState.Error` + `TextError` (or equivalent).
- Empty results → “Автомобили не найдены” (old copy).
- Invalid URL page → coerce to ≥ 1 (existing contract).

## Testing (TDD where logic is pure)

- City/filter URL round-trips already covered; extend if new query keys appear.
- Unit: page UI index ↔ URL page conversion helper if extracted.
- Unit: filter apply resets page (existing `applySearchFilterChange` tests).
- Compile `:searchApp:compileKotlinJs`; optional focused SearchCore tests for enriched mapping.
- Manual / pages-dev: `/moskva/search` shows cards + chips; filter updates URL; next page works.

## Success criteria

- `/moskva/search` looks like old SearchPage (cards, chips, dates, pagination), not text MVP.
- Filter/date/page changes update URL and reload results.
- Skeleton stays hidden (`drivebit-cars-grid-mounted` or equivalent).
- No Storage-backed filter repo as source of truth.
