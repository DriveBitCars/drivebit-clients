---
name: drivebit-ship-release
description: Use when the user asks to ship, release, or deploy DriveBit clients work — commit all changes, push, wait for CI, merge, and publish a GitHub release that triggers production deploy.
---

# DriveBit Ship & Release

End-to-end delivery for **drivebit-clients**: commit → push → PR → CI green → merge → GitHub release → deploy watch.

**REQUIRED:** Do the work yourself. Never give the user a checklist to run manually.

## When to Use

- User says: «закомить», «запушить», «зарелизить», «дождись тестов», «ship», «release»
- Feature work is done and should reach production via GitHub Release

**When NOT to use:** mid-implementation, question-only tasks, or when user only wants a local commit without release.

## Project Rules

| Rule | Value |
|------|-------|
| Default base branch | `trunk` |
| Feature branch | `cursor/<short-description>-30da` (lowercase) |
| Never push directly to | `trunk` |
| Release trigger | `release: published` → `deploy.yml` |
| Version tag | `v3.16.N` (increment patch from latest release) |

## Workflow

### 1. Pre-flight

```bash
git status
git branch --show-current
```

- On `trunk` with changes → create feature branch first
- Stage **only** files for this task — exclude unrelated local edits
- Optional quick check before push: `./gradlew :ChangedModule:jsTest` (CI is source of truth)

### 2. Commit

Stage explicitly (never `git add -A` blindly):

```bash
git add <paths...>
git commit -m "<clear value-first message>"
```

Multiple logical groups → up to 2–3 commits max.

### 3. Push

```bash
git push -u origin <branch>
```

On network failure: retry up to 4× with backoff 4s, 8s, 16s, 32s.

### 4. Pull Request

- Create or update PR into `trunk` (Cloud Agent: ManagePullRequest tool; local: `gh pr create`)
- Body: summary, what changed, verification run
- Mark ready for review if still draft

### 5. Wait for CI

```bash
gh pr checks <PR_NUMBER> --watch
```

Exit code **0** required before merge. Typical workflows: `Fast Check`, `WASM Test`, `Test Avatar Proxy`.

**If CI fails (max 3 fix cycles):**
1. Read failing job log: `gh run view <run-id> --log-failed`
2. Reproduce narrowly (e.g. `./gradlew :Network:jsTest`, not full `./gradlew check`)
3. Fix, commit, push, re-watch

Do not merge with red CI. Do not claim green without fresh `gh pr checks` output.

### 6. Merge

```bash
gh pr ready <PR_NUMBER>   # if draft
gh pr merge <PR_NUMBER> --merge --delete-branch
git checkout trunk && git pull origin trunk
```

### 7. Release

```bash
gh release list --limit 1
```

Next tag = patch bump (e.g. `v3.16.15` → `v3.16.16`).

```bash
gh release create v3.16.NN --target trunk \
  --title "v3.16.NN — <short Russian title>" \
  --notes "## Изменения

- <bullet points>

PR: #<number>"
```

### 8. Wait for Deploy

```bash
gh run list --workflow=deploy.yml --limit 1
gh run watch <run-id> --exit-status
```

Deploy takes ~10–12 min. Success = `conclusion: success`.

### 9. Post-deploy prod smoke (required)

**Do not announce «на проде» until this step passes.**

Run automated smoke against production:

```bash
npm install   # if node_modules missing locally
npm run verify:prod-smoke
```

Default base URL: `https://drivebit.ru`. Exit code **0** required.

**What the smoke test checks:**

| Check | Gate |
|-------|------|
| HTTP 200 | `/moskva`, `/search`, `/login`, `/contacts`, `/list-your-car.html` |
| Compose mount | `#root` has content on `/moskva` and `/search` |
| Third-party loader | `drivebit-third-party-deferred.js` in HTML on all key pages |
| Navigation | Hero button «Найти автомобиль» opens `/search` |
| Vendor assets | `/vendor/drivebit-third-party-deferred.js` served (classic `defer`, not `type=module`) |
| JS errors | No `pageerror` on any checked page |

**Analytics note:** Metrika/Callibri may not load in headless Playwright (ad-block / external scripts). The smoke script reports `analytics` info but does not fail on it. For analytics-only releases, additionally spot-check in a real browser: Network tab → `metrika/tag.js` before `callibri.js`.

**Optional manual spot-check** (browser or `cursor-ide-browser`):

1. https://drivebit.ru/moskva — hero, filters, car grid render
2. Click «Найти автомобиль» → `/search` with results UI
3. Header links: «Контакты», «Сдать авто»
4. https://drivebit.ru/login — auth shell loads (may redirect if already logged in)

Report in final message: release URL + deploy run URL + smoke test output summary.

## Quick Reference

| Step | Command / gate |
|------|----------------|
| Branch | `cursor/<name>-30da` off `trunk` |
| Push | `git push -u origin <branch>` |
| CI | `gh pr checks --watch` → exit 0 |
| Merge | `gh pr merge --merge` |
| Release | `gh release create v3.16.NN --target trunk` |
| Deploy | `gh run watch` on latest `deploy.yml` |
| Prod smoke | `npm run verify:prod-smoke` → exit 0 |

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Committing on `trunk` | Branch first |
| `git add -A` sweeps junk | Stage paths explicitly |
| Merge before CI green | Wait for `gh pr checks --watch` |
| Skip deploy watch | Release ≠ deployed until `deploy.yml` succeeds |
| Skip prod smoke | Deploy success ≠ site works; run `npm run verify:prod-smoke` |
| Wrong version | Always read `gh release list --limit 1` first |
| Broad local `./gradlew check` for CI debug | Run only the failing module/task from the log |

## Red Flags — STOP

- Announcing «готово» / «на проде» without deploy run exit code 0
- Announcing «на проде» without `npm run verify:prod-smoke` exit code 0
- Pushing to `trunk` directly (except emergency hotfix per `.cursorrules`)
- Creating release before merge to `trunk`
- Leaving unrelated files in the commit

## Related Skills

- **ce-commit** — commit message quality
- **ce-commit-push-pr** — PR description depth
- **verification-before-completion** — no success claims without command output
