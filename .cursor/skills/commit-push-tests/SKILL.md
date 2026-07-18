---
name: commit-push-tests
description: >-
  Stage all new project files, commit, push the feature branch, and wait until
  CI tests are green. Use when the user says /commit-push-tests, /commi-push-tests,
  «закоммить и запушь», «дождись тестов на CI», or asks to commit-push-verify CI
  without merge/release.
---

# Commit → Push → Wait for CI tests

**REQUIRED:** Do the work yourself. Never hand the user a manual checklist.

This skill stops at **green CI**. It does **not** merge, release, or deploy.
For full ship (merge + release + prod), use `drivebit-ship-release`.

## Triggers

- `/commit-push-tests`, `/commi-push-tests`
- «добавь новые файлы в гит, пушь, дождись CI»
- «закоммить / запушь / проверь что тесты на CI прошли» (без релиза)

## Project rules

| Rule | Value |
|------|-------|
| Default base | `trunk` |
| Never push to | `trunk` |
| Feature branch | create if on `trunk` |
| Stage new files | always — no leftover `??` for task files |

## Workflow

Copy and track:

```
Progress:
- [ ] 1. Pre-flight (branch + status)
- [ ] 2. Stage ALL new task files (`git add`)
- [ ] 3. Commit
- [ ] 4. Push
- [ ] 5. Ensure PR exists (create if missing)
- [ ] 6. Wait for CI green
- [ ] 7. Report PR URL + checks evidence
```

### 1. Pre-flight

```bash
git status
git branch --show-current
git log -5 --oneline
```

- On `trunk` with changes → create feature branch first (`feat/...` or `cursor/...`)
- Note unrelated dirty files; do **not** include them unless user asked

### 2. Stage all new files

**Always** stage newly created project files for this work:

```bash
git status -u --short
git add <every new/modified path belonging to the task>
```

Must include: new modules, sources, tests, resources, Gradle/`settings.gradle.kts`, project skills/rules created for the task.

Exclude: secrets, `.env`, accidental local junk, unrelated edits.

After staging: `git status` must show **no `??`** for task files.

### 3. Commit

Follow the committing-changes-with-git user rules (HEREDOC message, no `--no-verify`, no amend unless allowed).

```bash
git commit -m "$(cat <<'EOF'
<concise value-first message>

EOF
)"
```

### 4. Push

```bash
git push -u origin HEAD
```

Network failure: retry up to 4× with backoff 4s / 8s / 16s / 32s.

### 5. Pull request

If no PR for this branch:

```bash
gh pr create --base trunk --title "..." --body "$(cat <<'EOF'
## Summary
- ...

## Test plan
- [ ] CI green on this PR

EOF
)"
```

If PR exists, reuse it.

### 6. Wait for CI

```bash
gh pr checks --watch
```

or:

```bash
gh pr checks <PR_NUMBER> --watch
```

**Exit code 0 required.** Do not claim green without fresh `gh pr checks` output.

**If CI fails (max 3 fix cycles):**
1. Identify failing job (`gh pr checks` / `gh run view --log-failed`)
2. Reproduce **narrowly** (not full monorepo `./gradlew check` first)
3. Fix → stage new files → commit → push → watch again

### 7. Report

Return:
- Branch name
- Commit SHA(s)
- PR URL
- CI status evidence (all checks pass / exit 0)

## Red flags — STOP

- Claiming CI green without fresh watch output
- Leaving new task files untracked (`??`)
- Pushing to `trunk`
- Merging / releasing under this skill (use `drivebit-ship-release`)
- Committing secrets or unrelated dirty files

## Related

- `drivebit-ship-release` — merge + release + deploy + prod smoke
- `verification-before-completion` — no success claims without command output
- Project rule `git-stage-new-files` — stage new files as soon as created
