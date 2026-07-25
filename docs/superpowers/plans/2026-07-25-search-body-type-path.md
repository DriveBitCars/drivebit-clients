# Search body-type SEO paths — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Body type without brand uses `/search/{bodySlug}` (like brands); with brand, body stays in query; city URLs unchanged.

**Architecture:** Fixed body-slug table in Utils; `parseSearchUrl` / `buildSearchUrl` prefer brand path then body path then city; static `search/{slug}/index.html` + `landing-blocks.json`; SEO + SearchUrl + ViewModel tests.

**Tech Stack:** Kotlin Multiplatform (Utils, SearchCore, searchApp), static HTML under `composeApp/src/jsMain/resources/search/`, JSON SEO blocks.

## Global Constraints

- Do not change `CityPathRouting` home long-tail behavior.
- Body slugs = lowercase API enum names: sedan, hatchback, crossover, suv, minivan (extend if enum grows).
- Sedan SEO from sheet; others by same short template.
- TDD: failing test before production code.
- Never push to trunk; ship via PR + `/test-end-to-end-dev`.

---

### Task 1: BodyTypeSearchPathUtils + tests

**Files:**
- Create: `Utils/src/commonMain/kotlin/my/drivebit/utils/BodyTypeSearchPathUtils.kt`
- Create: `Utils/src/commonTest/kotlin/my/drivebit/utils/BodyTypeSearchPathUtilsTest.kt`

**Produces:**
- `data class BodyTypePathInfo(val slug: String, val apiName: String, val label: String)`
- `fun bodyTypePathInfoBySlug(slug: String): BodyTypePathInfo?`
- `fun bodyTypePathInfoByApiName(apiName: String): BodyTypePathInfo?`
- `fun pathForBodyTypeApiName(apiName: String): String?` → `/search/{slug}`
- `fun isBodyTypeSearchSlug(slug: String): Boolean`

- [ ] Failing tests for slug map (sedan/SUV/…), pathFor, not-a-brand collision samples
- [ ] Implement table + helpers
- [ ] `./gradlew :Utils:jvmTest --tests '*BodyTypeSearchPathUtilsTest*'` green

### Task 2: SearchUrl parse/build

**Files:**
- Modify: `Utils/src/commonMain/kotlin/my/drivebit/utils/SearchUrl.kt`
- Modify: `Utils/src/commonTest/kotlin/my/drivebit/utils/SearchUrlTest.kt`

- [ ] Failing tests: parse `/search/sedan`; build body-only; brand+body query; city unchanged; city+body query round-trip; `/search/bmw/x5` still brand+model
- [ ] Implement parse/build priority per design
- [ ] Utils tests green

### Task 3: toSearchUrlParts city default with body

**Files:**
- Modify: `searchApp/src/jsMain/kotlin/my/drivebit/search/SearchNavigation.kt`
- Add tests in Utils or SearchCore for filter→URL mapping if pure Kotlin helper extracted; otherwise cover via SearchUrlParts construction in SearchUrlTest / thin helper in Utils

Prefer: keep logic in `buildSearchUrl` so callers that set `citySlug=moskva` + `bodyType` without brand still emit body path (build prefers body over city when no brand). That avoids JS-only gaps.

- [ ] Test: `SearchUrlParts(citySlug="moskva", bodyType="Sedan", bodyTypeLabel="Седан")` → `/search/sedan`
- [ ] Implement in `buildSearchUrl`
- [ ] Green

### Task 4: SearchViewModel load body path

**Files:**
- Modify: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SearchViewModelTest.kt`
- Modify: `SearchCore/.../SearchViewModel.kt` only if resolve needs label fill from path (parse should already set bodyType)

- [ ] Test `load("/search/sedan")` → bodyType Sedan
- [ ] Test brand+body query still works
- [ ] Green

### Task 5: Static pages + landing-blocks + SeoBlocksTest

**Files:**
- Create: `composeApp/src/jsMain/resources/search/{sedan,hatchback,crossover,suv,minivan}/index.html`
- Modify: `composeApp/seo/landing-blocks.json`
- Modify: `DrivebitWeb/src/jsTest/kotlin/my/drivebit/navigation/SeoBlocksTest.kt`

- [ ] Sedan fixture SEO test
- [ ] HTML pages + JSON entries
- [ ] Stage all new files

### Task 6: Ship + e2e corners

- [ ] Feature branch from trunk, commit-push-tests, merge, Pages, browser desktop+mobile
- [ ] Corners: body path; body→brand; brand→body; city search intact; home vnedorozhnik landing; clear brand keeps body path; `/search/sedan?page=2`; unknown `/search/toyota` still brand
