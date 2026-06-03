# Static SEO HTML (explicit duplication)

Landing pages live as **full `index.html` files** in `composeApp/src/jsMain/resources/`.
There is **no** Python/Gradle step that rewrites them on build or deploy.

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
