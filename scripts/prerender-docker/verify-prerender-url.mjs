#!/usr/bin/env node
/**
 * Smoke-test: Prerender отдаёт HTML для целевого URL (по умолчанию drivebit.ru).
 * Запуск: из каталога scripts/prerender-docker при поднятом `docker compose up`.
 *
 * Env:
 *   PRERENDER_BASE — база сервиса (default http://127.0.0.1:3000)
 *   PRERENDER_TARGET_URL — страница для рендера (default https://drivebit.ru/)
 *   PRERENDER_VERIFY_TIMEOUT_MS — таймаут fetch (default 180000)
 */

const base = (process.env.PRERENDER_BASE || "http://127.0.0.1:3000").replace(/\/$/, "");
let target = (process.env.PRERENDER_TARGET_URL || "https://drivebit.ru/").trim();
const timeoutMs = Number(process.env.PRERENDER_VERIFY_TIMEOUT_MS || 180000);

if (!/^https?:\/\//i.test(target)) {
  console.error("verify-prerender-url: PRERENDER_TARGET_URL must be absolute http(s) URL, got:", target);
  process.exit(1);
}

const requestUrl = `${base}/${target}`;

async function main() {
  console.log("GET", requestUrl);
  const res = await fetch(requestUrl, {
    signal: AbortSignal.timeout(timeoutMs),
    headers: { Accept: "text/html,*/*" },
  });

  const text = await res.text();
  const len = text.length;

  if (!res.ok) {
    console.error("verify-prerender-url: HTTP", res.status, len, "bytes");
    process.exit(1);
  }

  if (len < 200) {
    console.error("verify-prerender-url: body too short:", len);
    process.exit(1);
  }

  const lower = text.toLowerCase();
  if (!lower.includes("drivebit")) {
    console.error("verify-prerender-url: expected 'DriveBit' in HTML body");
    process.exit(1);
  }

  const hasPrerenderMeta =
    text.includes("x-prerender-render-id") || text.includes("x-prerender-render-at");
  if (!hasPrerenderMeta) {
    console.warn("verify-prerender-url: no x-prerender meta (optional for static-ish HTML)");
  }

  console.log("verify-prerender-url: OK", res.status, len, "bytes");
}

main().catch((e) => {
  console.error("verify-prerender-url:", e.message || e);
  process.exit(1);
});
