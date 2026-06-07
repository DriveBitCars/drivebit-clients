# Static SEO HTML (explicit duplication)

Landing pages live as **full `index.html` files** in `composeApp/src/jsMain/resources/`.
There is **no** Python/Gradle/CI step that generates or rewrites them on build or deploy.

## Why static pages

Search crawlers read the initial HTML response. The root `index.html` has a generic title/description;
`city-meta-bootstrap.js` updates meta in the browser, but that is too late for many bots.
Each public URL that must rank (city landings, filters, search brands, etc.) needs its own committed
`index.html` with unique `<title>`, `<meta name="description">`, canonical, and og/twitter tags.

**Do not** add deploy-time generation for city SEO pages — add or update files in the repo instead.

## New city landing (`/{city}`)

Example: `/lyubertsy`, `/zelenograd`, `/kaliningrad`, `/krasnogorsk`.

1. **Preferred:** run the local helper (writes into `composeApp/src/jsMain/resources/{city}/index.html`):
   ```bash
   node scripts/generate-city-seo-pages.mjs
   # or one city:
   node scripts/generate-city-seo-pages.mjs composeApp/src/jsMain/resources lyubertsy
   ```
   Meta text comes from `vendor/city-meta-bootstrap.js` (same rules as in-app). Template: `moskva/index.html`.

2. **Manual:** copy `moskva/index.html` → `{city}/index.html`, replace paths (`/moskva` → `/{city}`), title, description, h1, SEO block.

3. Add the path to `composeApp/src/jsMain/resources/sitemap.xml` if not already listed.

4. Add an entry to [`scripts/print-city-meta-table.mjs`](../../scripts/print-city-meta-table.mjs) `STATIC_HTML` when verifying meta tables.

Filter subpages (`/{city}/{filter}`) follow the same pattern as [`moskva/kanikuly`](../src/jsMain/resources/moskva/kanikuly/index.html) when needed.

## When you change shared head assets (fonts, defer, leaflet loader)

1. Edit [`head-common.reference.html`](head-common.reference.html) as the checklist.
2. Apply the same `<link>` / `<script>` block to every page that needs it (moskva, search, offer, …).

## Copy-paste fragments (optional)

Files like `app-header.fragment.html`, `filters.fragment.html`, `cars-grid-skeleton.fragment.html` are **reference snippets** only.
Paste into the target `index.html` by hand when updating header/filters/hero/cars grid placeholder.

For bulk cars-grid skeleton rollout on listing pages, run `node scripts/inject-cars-grid-skeleton.mjs` from the repo root.

## New `/search/{brand}` page

1. Copy an existing `search/bmw/index.html`.
2. Update title, description, canonical, SEO text.
3. Add an entry to [`landing-blocks.json`](landing-blocks.json) for in-app meta.

## New `/moskva/{filter}` page

1. Copy `moskva/kanikuly/index.html` (or closest filter).
2. Update hero image preload, h1, filter `aria-pressed`, SEO block.
