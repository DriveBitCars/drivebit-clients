---
name: test-end-to-end-dev
description: >-
  Run commit-push-tests, merge to trunk, wait for GitHub Pages deploy to
  dev.drivebit.ru, then verify main web flows in the browser with screenshots.
  Use when the user says /test-end-to-end-dev, /test-ene-to-end-dev,
  «проверь на pages-dev», «e2e на деве», or asks to commit-push then validate
  GitHub Pages / pages-dev.
---

# Test end-to-end on pages-dev

**REQUIRED:** Do the work yourself. Never hand the user a manual checklist.

Pipeline:

1. **`/commit-push-tests`** (stage new files → commit → push → PR → CI green)
2. **Merge PR into `trunk`** (Pages deploys only from `trunk`)
3. **Wait for GitHub Pages deploy** (`Deploy to GitHub Pages` → `dev.drivebit.ru`)
4. **Browser e2e** on pages-dev: main flows + screenshots

This skill does **not** create a GitHub Release or run production `deploy.yml`.
For prod ship use `drivebit-ship-release`.

## Triggers

- `/test-end-to-end-dev`, `/test-ene-to-end-dev`
- «проверь на pages-dev / GitHub Pages»
- «commit-push, дождись pages, открой дев в браузере»

## URLs & workflows

| Item | Value |
|------|-------|
| Pages-dev site | `https://dev.drivebit.ru` |
| Fallback Pages URL | `https://drivebitcars.github.io/drivebit-clients/` |
| Pages workflow | `.github/workflows/github-pages.yml` (`Deploy to GitHub Pages`) |
| Pages trigger | push to `trunk` or `workflow_dispatch` |
| Prod site | `https://drivebit.ru` (out of scope here) |

## Progress checklist

```
Progress:
- [ ] 1. Run commit-push-tests to CI green
- [ ] 2. Merge PR into trunk (no release)
- [ ] 3. Wait for Pages deploy success
- [ ] 4. Browser smoke on https://dev.drivebit.ru
- [ ] 5. Screenshots of key pages
- [ ] 6. Report PR + Pages run + screenshot evidence
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
```

Expect HTTP 200 (allow short retry loop if CDN is still propagating).

## 4. Browser e2e on pages-dev

Base URL: **`https://dev.drivebit.ru`**

Use **cursor-ide-browser** MCP (navigate → snapshot → interact → screenshots).
Playwright/`scripts/verify-prod-smoke.mjs` is optional support, not a substitute for visual screenshots.

### Required page checks

| Path | What must be true |
|------|-------------------|
| `/moskva` | HTTP 200; hero / city home visible; `#root` has content or static hero shell present |
| `/moskva/search` | Search UI loads (results, loading, or error — not blank white); script may be `appCompose.js` or `composeApp.js` depending on branch |
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

### Screenshots (required)

Save at least:

1. `/moskva` first viewport
2. `/moskva/search` (or post-CTA search URL)
3. One brand search page (`/bmw` or `/search/...`)
4. Auth shell (`/login-by-phone`)

Use browser screenshot tools; keep images for the final report. Note any JS `pageerror` in the report.

### Fail the skill if

- Pages deploy not green
- Key URL not HTTP 200
- City home or search is blank / broken shell
- Hero→search navigation fails when the button exists
- You only ran commit-push-tests without Pages + browser proof

## 5. Report

Return:

- Branch + commit SHAs
- PR URL + merge result
- Pages workflow run URL + conclusion
- Pages-dev base URL used
- Short pass/fail per checked path
- Screenshot paths / attachments
- Known limitations (analytics blocked in headless, etc.)

## Red flags — STOP

- Claiming Pages-dev OK without deploy watch exit 0
- Testing **prod** `drivebit.ru` instead of **dev** `dev.drivebit.ru` (unless user asked)
- Skipping screenshots
- Merging then releasing/prod-deploy under this skill
- Leaving new skill/task files untracked

## Related

- `commit-push-tests` — stage/commit/push/CI only
- `drivebit-ship-release` — merge + GitHub Release + prod deploy + prod smoke
- `scripts/verify-prod-smoke.mjs` — reusable smoke ideas; pass `https://dev.drivebit.ru` as base if using it as helper
