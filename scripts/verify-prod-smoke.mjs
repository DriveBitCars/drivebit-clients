#!/usr/bin/env node
/**
 * Post-deploy smoke test for drivebit.ru main functionality.
 * Usage: node scripts/verify-prod-smoke.mjs [baseUrl]
 */
import { chromium } from "playwright";

const base = (process.argv[2] || "https://drivebit.ru").replace(/\/$/, "");

const PAGES = [
  { path: "/moskva", name: "city landing" },
  { path: "/moskva/search", name: "search" },
  { path: "/search/bmw", name: "brand search" },
  { path: "/search/bmw/x5", name: "brand model search" },
  { path: "/login", name: "login shell" },
  { path: "/contacts", name: "contacts" },
  { path: "/list-your-car.html", name: "list your car" },
];

/** Pages that must embed Callibri the same way as city landing. */
const CALLIBRI_REQUIRED = new Set([
  "city landing",
  "search",
  "brand search",
  "brand model search",
  "contacts",
  "login shell",
]);

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });
const pageErrors = [];
page.on("pageerror", (e) => pageErrors.push({ url: page.url(), message: e.message }));

const results = [];

for (const { path, name } of PAGES) {
  const url = `${base}${path}`;
  const response = await page.goto(url, { waitUntil: "load", timeout: 60000 });
  await page.waitForTimeout(path === "/moskva" ? 8000 : 5000);

  const report = await page.evaluate(() => {
    const scripts = Array.from(document.scripts);
    const hasComposeBundle = scripts.some(
      (s) =>
        /composeApp\.js/.test(s.src) ||
        /appCompose\.js/.test(s.src) ||
        /searchApp\.js/.test(s.src) ||
        /drivebit-compose-idle-loader/.test(s.src),
    );
    return {
      hasComposeApp: hasComposeBundle,
      hasThirdPartyLoader: scripts.some((s) => /drivebit-third-party-deferred/.test(s.src)),
      hasCallibriScript: scripts.some((s) => /callibri\.js/.test(s.src)),
      hasRoot: !!document.getElementById("root"),
      rootLen: document.getElementById("root")?.innerHTML?.length ?? 0,
      title: document.title,
    };
  });

  results.push({
    name,
    path,
    url,
    status: response?.status() ?? 0,
    ...report,
  });
}

await page.goto(`${base}/moskva`, { waitUntil: "load", timeout: 60000 });
await page.waitForTimeout(5000);
const searchButton = page.getByRole("button", { name: "Найти автомобиль" });
const searchNavOk = (await searchButton.count()) > 0;
if (searchNavOk) {
  await searchButton.click();
  await page.waitForURL(/\/[^/]+\/search/, { timeout: 15000 });
}
const navigationOk = /\/[^/]+\/search/.test(page.url());

await page.goto(`${base}/moskva`, { waitUntil: "load", timeout: 60000 });
await page.waitForTimeout(12000);
const thirdParty = await page.evaluate(() => {
  const resources = performance.getEntriesByType("resource");
  const metrika = resources.find((r) => /mc\.yandex\.ru\/metrika\/tag\.js/.test(r.name));
  const callibri = resources.find((r) => /callibri/.test(r.name));
  const hasYm = typeof window.ym === "function";
  const metrikaInDom = Array.from(document.scripts).some((s) => /metrika\/tag\.js/.test(s.src));
  const callibriInDom = Array.from(document.scripts).some((s) => /callibri/.test(s.src));
  return {
    hasYm,
    metrikaInDom,
    callibriInDom,
    metrikaStart: metrika?.startTime ?? null,
    callibriStart: callibri?.startTime ?? null,
    metrikaBeforeCallibri:
      metrika && callibri ? metrika.startTime <= callibri.startTime : metrikaInDom && !callibriInDom,
  };
});

const failures = [];

for (const item of results) {
  if (item.status !== 200) failures.push(`${item.name}: HTTP ${item.status}`);
  if (!item.hasThirdPartyLoader) failures.push(`${item.name}: missing third-party loader`);
  if (CALLIBRI_REQUIRED.has(item.name) && !item.hasCallibriScript) {
    failures.push(
      `${item.name}: Callibri not injected after deferred load (cdn.callibri.ru/callibri.js)`,
    );
  }
  if (item.path !== "/list-your-car.html") {
    if (!item.hasComposeApp) failures.push(`${item.name}: missing compose/search app bundle`);
  }
  if (
    item.name === "city landing" ||
    item.name === "search" ||
    item.name === "brand search" ||
    item.name === "brand model search"
  ) {
    if (!item.hasRoot || item.rootLen <= 0) failures.push(`${item.name}: compose root empty`);
  }
}

if (!navigationOk) failures.push("navigation: hero search button did not open /{city}/search");

const vendorDeferred = await fetch(`${base}/vendor/drivebit-third-party-deferred.js`)
  .then((r) => r.ok)
  .catch(() => false);
if (!vendorDeferred) failures.push("vendor: drivebit-third-party-deferred.js not served");

const vendorScheduler = await fetch(`${base}/vendor/drivebit-third-party-scheduler.mjs`)
  .then((r) => r.ok)
  .catch(() => false);
if (!vendorScheduler) failures.push("vendor: drivebit-third-party-scheduler.mjs not served");

const analytics = {
  ...thirdParty,
  vendorDeferred,
  vendorScheduler,
  note:
    "Callibri script tag in HTML is a hard gate. Metrika/Callibri network load may be blocked in headless; verify callibriInit() in a real browser if needed.",
};

for (const err of pageErrors) {
  // Callibri widget may throw benign errors in headless Chromium
  if (/Cannot set properties of undefined \(setting 'groups'\)/.test(err.message)) {
    continue;
  }
  failures.push(`js error on ${err.url}: ${err.message}`);
}

const report = {
  base,
  pages: results,
  navigationOk,
  analytics,
  pageErrors,
  ok: failures.length === 0,
  failures,
};

console.log(JSON.stringify(report, null, 2));
await browser.close();
process.exit(report.ok ? 0 : 1);
