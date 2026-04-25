# Deployment Guide

CI deploys the **Compose JS browser** build to a Linux host over SSH. Deploy is **atomic**: each run uploads to a new directory under `releases/`, then switches a `current` symlink and reloads nginx (no empty-site window).

## Prerequisites

1. **Ubuntu/Debian** server with **nginx** and SSH access for the GitHub Actions user.
2. **One-time layout** (see below): nginx SPA `root` must be **`/var/www/drivebit-clients/current`** (symlink), not the flat `/var/www/drivebit-clients` directory.

## GitHub Secrets

**Repository → Settings → Secrets and variables → Actions**

### Required

- `SERVER_HOST` — server IP or hostname (e.g. front VPS). After the 2026 front migration, set this to **`80.249.146.3`** (or the DNS name that points there).
- `SERVER_USER` — SSH user (often `root`).
- `SERVER_SSH_KEY` — private key for that user.

### Optional

- `SERVER_PORT` — SSH port (default `22`).
- `SERVER_DOMAIN` — used only in success log messages.

No extra secrets are required for atomic paths; `DEPLOY_ROOT` is fixed in [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) as `/var/www/drivebit-clients`.

## Server layout (atomic)

```
/var/www/drivebit-clients/
  current -> releases/<run-id>   # symlink; nginx root points here
  releases/
    <github_run_id>-<run_attempt>/   # one directory per deploy
    ...
```

Workflow steps:

1. Create `releases/${{ github.run_id }}-${{ github.run_attempt }}/`.
2. SCP build artifacts into that directory only (never delete the whole web root).
3. Flatten nested `composeApp/build/...` if present; `chmod`/`chown` that directory.
4. `ln -sfn releases/<id> /var/www/drivebit-clients/current` and `nginx -t` + `reload`.
5. Prune: keep the active release plus the **5** newest other release directories (configurable later via repo variable if needed).

## One-time migration (existing “flat” `/var/www/drivebit-clients`)

If production still has `root /var/www/drivebit-clients` and files live **directly** in that directory:

1. On the server, run the repo script (as root), or do the same by hand:

   ```bash
   sudo bash scripts/bootstrap-atomic-frontend.sh
   ```

   From a clone, copy the script to the server, or paste its commands. It moves loose files into `releases/bootstrap` and sets `current`.

2. In every nginx `server` block that serves the SPA, set:

   ```nginx
   root /var/www/drivebit-clients/current;
   ```

3. `sudo nginx -t && sudo systemctl reload nginx`.

4. Point GitHub **`SERVER_HOST`** at this machine and run a release or **workflow_dispatch**.

Until step 2 is done, switching `current` will not change what nginx serves.

## API upstream

If `location /api/` is missing, the workflow inserts `proxy_pass http://api.drivebit.ru:5000/;` (backend resolved via DNS). To use another upstream, add or edit `location /api/` in nginx on the front server and do not rely on the auto-inserted block.

## Triggers

- **Release published** (`release: published`), or  
- **workflow_dispatch** (manual).

## Build (local or CI)

```bash
./gradlew :composeApp:jsBrowserDistribution
```

Artifacts: `composeApp/build/dist/js/productionExecutable/`.

## Manual copy (emergency)

```bash
REL=/var/www/drivebit-clients/releases/manual-$(date +%Y%m%d%H%M%S)
ssh user@host "mkdir -p $REL"
scp -r composeApp/build/dist/js/productionExecutable/* "user@host:$REL/"
ssh user@host "cd /var/www/drivebit-clients && ln -sfn $REL current && sudo nginx -t && sudo systemctl reload nginx"
```

(`ln -sfn` target should be relative `releases/...` if you match CI; adjust to your layout.)

## Troubleshooting

1. **404 or old site after deploy** — nginx `root` still without `/current`, or `current` broken symlink.
2. **SSH failed** — check `SERVER_HOST`, key, firewall.
3. **GitHub Actions** — open the failed job; the debug step prints `releases/`, `readlink current`, and the release dir for this run.

## Security

- Protect SSH keys; prefer deploy user with write access only under `/var/www/drivebit-clients`.
- Use TLS (Let’s Encrypt) on nginx.
- Review `location /api/` upstream and MinIO proxy headers for your environment.
