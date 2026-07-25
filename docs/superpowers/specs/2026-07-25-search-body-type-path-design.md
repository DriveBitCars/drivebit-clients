# Search body-type SEO paths — design

Date: 2026-07-25  
Status: approved in brainstorming (approach 1, scope B)  
Source sheet: [SEO keywords gid=136231017](https://docs.google.com/spreadsheets/d/1K0sQLG3z6SNN4o_fddtV8Xx2vDavQhhJ4S5eJyp8yHM/edit?pli=1&gid=136231017#gid=136231017)

## Problem

Brand search already uses SEO paths (`/search/audi`). Body type on the search screen is only a query (`?bodyType=…`). The sheet asks to create body-type pages in the search section **like brands**. City home long-tails (`/moskva/arenda-vnedorozhnika-bez-voditelya`) must keep working unchanged.

## Goals

- Body type without brand → path `/search/{bodySlug}` (static folder + `index.html` + SEO blocks).
- Brand (and optional model) already in path → body type stays in query (current behavior).
- All body types from the search filter enum, not only sedan from the sheet.
- SEO copy: sedan from the sheet; other types by the same template (no invented long-form «Текст» when the sheet has none).
- **Do not break city logic**; cover with regression tests.
- Corner cases: body then brand/model; brand/model then body; clear brand with body still selected.

## Non-goals

- Changing city-home filter path segments (`arenda-*-bez-voditelya`).
- Comfort/business class pages from the same sheet (not body types).
- Prod release (pages-dev e2e via `/test-end-to-end-dev` after merge).
- Body type under model segment (`/search/bmw/x5/sedan`).

## Decision

**Approach 1 — mirror brands under `/search/{slug}`**

- Known body slugs are a fixed set derived from API enum names (lowercase): e.g. `sedan`, `hatchback`, `crossover`, `suv`, `minivan`, plus any other `BodyTypeEnum` values.
- `/search/{slug}` is parsed as body **only if** `slug` is in that set; otherwise it remains a brand slug (existing behavior).
- Slugs chosen must not collide with brand slugs (`sedan` / `suv` / … are not brands today).

## URL rules

| URL | Meaning |
|-----|---------|
| `/moskva/search` | City search — unchanged |
| `/moskva/search?bodyType=SUV&bodyTypeLabel=…` | City + body in query — still accepted |
| `/moskva/arenda-vnedorozhnika-bez-voditelya` | City home landing — **untouched** |
| `/search/bmw`, `/search/bmw/x5` | Brand / brand+model — unchanged |
| `/search/sedan` | Body only in path (no brand) |
| `/search/bmw?bodyType=Sedan&bodyTypeLabel=Седан` | Brand in path, body in query |
| `/bmw?bodyType=SUV&…` | Legacy short brand + body query — still works |

### Build (`buildSearchUrl` / `toSearchUrlParts`)

Priority:

1. If `brandSlug` present → brand path (`/search/{brand}`[ `/{model}` ]); put `bodyType` / `bodyTypeLabel` in query when set. Do **not** put body in path.
2. Else if `bodyType` resolves to a known body slug → `/search/{bodySlug}`; omit redundant `bodyType*` query keys.
3. Else → `/{citySlug}/search` (default `moskva` when city missing), other filters in query as today.

Selecting a brand while on a body path moves to rule 1 (body → query). Clearing brand while body remains moves to rule 2.

### Parse (`parseSearchUrl`)

1. `/{city}/search` → city (unchanged); body only from query.
2. `/search/{a}/{b}` → brand `a`, model `b`; body from query.
3. `/search/{slug}`:
   - if `slug` ∈ body-slug set → `bodyType` + `bodyTypeLabel` from slug map; `brandSlug` null;
   - else → `brandSlug = slug` (current).
4. Legacy `/{audi|bmw}` → brand as today; body from query.
5. Query `bodyType` still wins / merges when present on brand or city URLs.

`SearchUrlParts` may keep using existing `bodyType` / `bodyTypeLabel` fields (no new path field required if parse fills them from the body slug).

## Static SEO pages

For each known body slug:

1. `composeApp/src/jsMain/resources/search/{slug}/index.html` — copy brand page shell (audi/bmw), unique title/description/canonical/H1.
2. `composeApp/seo/landing-blocks.json` entry for `/search/{slug}`.
3. Sedan meta from sheet:
   - H1: `Аренда седана без водителя в Москве` (drop `[в Москве]` brackets → filled city like brands)
   - Title: `Аренда седана в Москве через сервис DriveBit`
   - Description: sheet text for sedan
4. Other types: same sentence patterns with type noun forms (седана → хэтчбека / кроссовера / внедорожника / минивэна / …). Do not invent extra keyword paragraphs beyond the brand-style short sections already used for thin brand landings.
5. SEO unit test analogous to audi in `SeoBlocksTest` (at least sedan; spot-check one more type).

## City safety (must not break)

- `CityPathRouting` / `CUSTOM_FILTER_PATH_SEGMENTS` / home chips — no behavior change.
- `/{city}/search` parse/build round-trips unchanged when body is absent.
- City search **with** body in query still parses and loads filters (backward compatible).
- New body paths must not be classified as city slugs.
- Existing city SEO landings for Внедорожник / Минивэн remain the home-filter URLs, not `/search/suv`.

## Tests (required)

| Area | Assertions |
|------|------------|
| `BodyTypeSearchPathUtilsTest` (new) | slug ↔ enum name/label; `isBodyTypeSearchSlug`; no overlap with sample brand slugs |
| `SearchUrlTest` | parse `/search/sedan` → body, not brand; build body-only → `/search/sedan` without query dup; brand+body → `/search/bmw?bodyType=…`; **city `/moskva/search` + dates/page unchanged**; city + body query still round-trips |
| `SearchBrandPathRouting` / city tests | existing brand + city path tests still green |
| `SearchViewModelTest` | `load("/search/sedan")` applies body filter; `load("/search/bmw?bodyType=SUV&…")` brand+body; navigation-style corner: body then brand ends with brand path + body query |
| `SeoBlocksTest` | `/search/sedan` landing HTML contains expected Cyrillic phrases from sheet/template |
| `CityPathRoutingTest` | no regressions for `arenda-vnedorozhnika` / `arenda-minivena` / city vs brand |

## E2E (pages-dev, after ship)

Via `/test-end-to-end-dev` after CI + merge to trunk + Pages deploy:

1. Open `/search/sedan` — body filter active, results/API with Sedan.
2. Body then brand/model → URL `/search/{brand}/…?bodyType=…`.
3. Brand/model then body → body stays in query.
4. Smoke city: `/moskva`, `/moskva/search`, one city home body landing still OK.
5. Desktop + mobile screenshots.

## Out of scope follow-ups

- Sitemap entries for every body page (add if deploy/sitemap already lists each brand the same way).
- Comfort/business class search paths from the sheet.
