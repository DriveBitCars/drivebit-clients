---
name: test-end-to-end-dev
description: >-
  Run commit-push-tests, merge to trunk, wait for GitHub Pages deploy to
  dev.drivebit.ru, then verify main web flows in the browser (desktop + mobile)
  with screenshots, console error checks, and Hawk Garage
  (https://garage.hawk.so/). Use when the user says /test-end-to-end-dev,
  /test-ene-to-end-dev, «проверь на pages-dev», «e2e на деве», «на мобильном»,
  or asks to commit-push then validate GitHub Pages / pages-dev.
---

# Test end-to-end on pages-dev

**REQUIRED:** Do the work yourself. Never hand the user a manual checklist.

Pipeline:

1. **`/commit-push-tests`** (stage new files → commit → push → PR → CI green)
2. **Merge PR into `trunk`** (Pages deploys only from `trunk`)
3. **Wait for GitHub Pages deploy** (`Deploy to GitHub Pages` → `dev.drivebit.ru`)
4. **Browser e2e** on pages-dev: **desktop + mobile** main flows + screenshots
5. **Console errors** — capture and fail on unexpected JS/`pageerror` / `console.error`
6. **Hawk Garage** — check https://garage.hawk.so/ for new errors from the e2e window

This skill does **not** create a GitHub Release or run production `deploy.yml`.
For prod ship use `drivebit-ship-release`.

## Triggers

- `/test-end-to-end-dev`, `/test-ene-to-end-dev`
- «проверь на pages-dev / GitHub Pages»
- «commit-push, дождись pages, открой дев в браузере»
- «проверь на мобильном» / mobile viewport e2e on pages-dev

## URLs & workflows

| Item | Value |
|------|-------|
| Pages-dev site | `https://dev.drivebit.ru` (often **301 →** `https://dev.drivebit.my`) |
| Canonical live base | `https://dev.drivebit.my` (use for browser e2e after redirect) |
| Fallback Pages URL | `https://drivebitcars.github.io/drivebit-clients/` |
| Pages workflow | `.github/workflows/github-pages.yml` (`Deploy to GitHub Pages`) |
| Pages trigger | push to `trunk` or `workflow_dispatch` |
| Prod site | `https://drivebit.ru` (out of scope here) |
| Hawk errors UI | https://garage.hawk.so/ |
| Home filters regression doc | `docs/home-filters-url-regression.md` |

## Progress checklist

```
Progress:
- [ ] 1. Run commit-push-tests to CI green
- [ ] 2. Merge PR into trunk (no release)
- [ ] 3. Wait for Pages deploy success
- [ ] 4. Browser smoke desktop on pages-dev
- [ ] 5. Browser smoke + key flows on mobile viewport
- [ ] 6. Screenshots (desktop + mobile)
- [ ] 7. Console: no unexpected pageerror / console.error
- [ ] 8. Hawk Garage: no new errors from this e2e window (https://garage.hawk.so/)
- [ ] 9. Report PR + Pages run + screenshot + console + Hawk evidence
```

## 1. Commit → push → CI

Follow project skill **`commit-push-tests`** exactly:

- Stage **all** new task files (no leftover `??`)
- Commit, push feature branch, ensure PR, `gh pr checks --watch` → exit **0**

Do not claim green without fresh check output.

## 2. Merge to trunk (required for Pages)

GitHub Pages deploys **only** after `trunk` updates (not from the feature branch alone).

```bash
gh pr merge <PR_NUMBER> --merge --delete-branch
git checkout trunk && git pull origin trunk
```

If merge is blocked, fix blockers; do not skip to browser checks on stale Pages.

## 3. Wait for Pages deploy

```bash
gh run list --workflow=github-pages.yml --branch trunk --limit 3
gh run watch <RUN_ID> --exit-status
```

Exit code **0** required. Typical duration: several minutes after merge.

Optional HTTP gate before browser work:

```bash
curl -sI "https://dev.drivebit.ru/moskva" | head -5
curl -sI "https://dev.drivebit.my/moskva" | head -5
```

Expect HTTP 200 on the final host (`.ru` may 301 to `.my`; allow short retry if CDN is still propagating).

## 4. Browser e2e on pages-dev

Base URL: start from **`https://dev.drivebit.ru`**, follow redirects; interact on the live host (usually **`https://dev.drivebit.my`**).

Prefer **cursor-ide-browser** MCP when available. If missing, use **Playwright** (`devices["iPhone 14"]` / `Pixel 7`) — still required to produce real screenshots. `scripts/verify-prod-smoke.mjs` / `scripts/e2e-pages-dev-inp.mjs` are optional helpers only.

### Required page checks (desktop **and** mobile)

| Path | What must be true |
|------|-------------------|
| `/moskva` | HTTP 200; hero / city home visible; `#root` has content or static hero shell present |
| `/moskva/search` | Search UI loads (results, loading, or error — not blank white); script may be `appCompose.js` or `composeApp.js` |
| `/bmw` or `/search/toyota` | Brand search shell loads |
| `/login` or `/login-by-phone` | Auth shell loads |
| `/contacts` | Contacts page loads |
| `/profile` (optional) | Redirects to login or shows profile shell |

### Interactions (main functionality)

1. Open `/moskva` — wait for paint
2. Click hero CTA **«Найти автомобиль»** (if present) → land on `/{city}/search`
3. On search: confirm list/loading/error UI visible (not empty document)
4. Open `/login-by-phone` — confirm form/shell visible
5. Header nav: **Контакты** / **Сдать авто** if present

### Mobile viewport (required)

Always run a **mobile** pass, not only desktop.

| Item | Value |
|------|-------|
| Primary device | Playwright `devices["iPhone 14"]` (or equivalent ~390×844) |
| Secondary (optional) | `devices["Pixel 7"]` smoke on `/moskva` + one nearby/filter URL |
| Locale | `ru-RU` |

Mobile-specific notes:

- Home trip filters on mobile are often **static HTML**: `button.drivebit-filter-btn` (+ `.is-selected`), not Compose `.universal-button`
- Nearby **Карта / Список / N км** remain Compose `.universal-button`
- Scroll chips into view (`scrollIntoView({ inline: "center" })`) before click
- Dismiss cookie banner («Понятно») if present
- Hero CTA may be `#drivebit-hero-search-btn`

If the change touches **home filters / URL / pagination / nearby**, also run the mobile filter checklist below (or full doc checklist).

### Home filters regression (when relevant)

When the PR touches city-home filters, URL-first search, pagination, or nearby — run both desktop and mobile against the checklist in **`docs/home-filters-url-regression.md`**:

| Area | Must pass |
|------|-----------|
| Select each filter chip | URL slug + pressed state + cars/map |
| Deselect (toggle) | back to `/{city}`, pressed «Все» |
| Refresh | slug / `poblizosti?lat&lon` survive reload |
| Pagination | «Вперёд» → `?page=2`; deeplink; «Назад» |
| Nearby | markers; List mode grid + «Показано»; radius → `radiusKm` in URL |

Known slug: Минивэн → `/moskva/arenda-minivena-bez-voditelya` (not `arenda-avto-minivena-…`).

Save evidence under `tmp/e2e-*-regress/` or `tmp/e2e-mobile-home-filters/` and update the doc date/results if you ran the checklist.

### Screenshots (required)

**Desktop** — at least:

1. `/moskva` first viewport
2. `/moskva/search` (or post-CTA search URL)
3. One brand search page (`/bmw` or `/search/...`)
4. Auth shell (`/login-by-phone`)

**Mobile** — at least:

1. `/moskva` first viewport (iPhone-class)
2. `/moskva/search` or post-CTA search
3. One brand or contacts page
4. If filters/nearby were in scope: one filter selected + nearby map **or** List

Keep images for the final report.

### Console errors (required)

On **every** e2e page (desktop and mobile), collect:

- Playwright / browser **`pageerror`** (uncaught exceptions)
- **`console` messages** of type `error` (and treat `pageerror` as hard failures)

Wire listeners **before** `goto`:

```js
page.on("pageerror", (e) => pageErrors.push(String(e?.message || e)));
page.on("console", (msg) => {
  if (msg.type() === "error") consoleErrors.push(msg.text());
});
```

**Fail the skill** if any unexpected `pageerror` or `console.error` appears during the smoke.

Allowlist only clearly known noise (document each allowed string in the report), for example:

- Third-party blocked in headless (Metrika / Callibri / Jivo network failures) when the app shell still works
- Broken remote image 404 noise that does not break UI

Do **not** allowlist app/Compose/Kotlin/JS exceptions, blank `#root`, or Hawk catcher init failures on pages-dev.

Include console/`pageerror` lists in the final report (empty list = pass).

### Hawk Garage (required)

After browser e2e (or overlapping it), open **https://garage.hawk.so/** and check the DriveBit project for errors tied to this run:

1. Prefer environment **`development`** (pages-dev: `dev.drivebit.ru` / `dev.drivebit.my` — see `HawkErrorTracking.kt` `hawkEnvironment`)
2. Look at events from **now − ~15–30 minutes** (cover the e2e window)
3. Fail if **new** errors appeared that match the flows you just exercised (home, search, auth, contacts, filters)
4. Note event titles / counts / links in the report

If Garage requires login and you cannot authenticate, say so explicitly in the report and still fail closed on **browser console/`pageerror`** evidence — do not claim “no Hawk errors” without opening Garage or an API equivalent.

Optional: correlate that `window.__drivebitHawk` exists on pages-dev after load (Hawk catcher initialized); absence alone is not a pass for Garage.

### Fail the skill if

- Pages deploy not green
- Key URL not HTTP 200
- City home or search is blank / broken shell
- Hero→search navigation fails when the button exists
- **Mobile pass skipped** (desktop-only is not enough)
- **Unexpected console `error` or `pageerror` during e2e**
- **New relevant errors in Hawk Garage** for the e2e window (or Garage not checked without an explicit auth blocker note)
- You only ran commit-push-tests without Pages + browser proof

## 5. Report

Return:

- Branch + commit SHAs
- PR URL + merge result
- Pages workflow run URL + conclusion
- Pages-dev base URL used (note `.ru` → `.my` if redirected)
- Short pass/fail per checked path (**desktop and mobile**)
- Screenshot paths / attachments (both viewports)
- Console / `pageerror` summary (empty = clean)
- Hawk Garage check: URL https://garage.hawk.so/, environment, time window, new errors yes/no (+ links if any)
- Known limitations (analytics blocked in headless, broken image 404 noise, Garage auth blocker, etc.)

## Red flags — STOP

- Claiming Pages-dev OK without deploy watch exit 0
- Testing **prod** `drivebit.ru` instead of **dev** (unless user asked)
- Skipping screenshots
- Skipping **mobile** viewport
- Skipping **console error** collection
- Skipping **https://garage.hawk.so/** (or claiming clean Hawk without looking)
- Merging then releasing/prod-deploy under this skill
- Leaving new skill/task files untracked

## Related

- `commit-push-tests` — stage/commit/push/CI only
- `drivebit-ship-release` — merge + GitHub Release + prod deploy + prod smoke
- `scripts/verify-prod-smoke.mjs` — reusable smoke ideas; pass pages-dev base if using as helper
- `scripts/e2e-pages-dev-inp.mjs` — optional INP/deferral-oriented smoke
- `docs/home-filters-url-regression.md` — URL-first home filters checklist + last results
- `AppHeader/.../HawkErrorTracking.kt` — Hawk token/env mapping for pages-dev → `development`
