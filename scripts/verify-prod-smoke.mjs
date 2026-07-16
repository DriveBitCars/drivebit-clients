#!/usr/bin/env node
/**
 * Post-deploy smoke test for drivebit.ru main functionality.
 * Usage: node scripts/verify-prod-smoke.mjs [baseUrl]
 */
import { chromium } from "playwright";

const base = (process.argv[2] || "https://drivebit.ru").replace(/\/$/, "");

const PAGES = [
  { path: "/moskva", name: "city landing" },
  { path: "/search", name: "search" },
  { path: "/login", name: "login shell" },
  { path: "/contacts", name: "contacts" },
  { path: "/list-your-car.html", name: "list your car" },
];

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });
const pageErrors = [];
page.on("pageerror", (e) => pageErrors.push({ url: page.url(), message: e.message }));

const results = [];

for (const { path, name } of PAGES) {
  const url = `${base}${path}`;
  const response = await page.goto(url, { waitUntil: "load", timeout: 60000 });
  await page.waitForTimeout(path === "/moskva" ? 8000 : 4000);

  const report = await page.evaluate(() => ({
    hasComposeApp: !!document.querySelector('script[src*="composeApp.js"]'),
    hasThirdPartyLoader: !!document.querySelector('script[src*="drivebit-third-party-deferred"]'),
    hasRoot: !!document.getElementById("root"),
    rootLen: document.getElementById("root")?.innerHTML?.length ?? 0,
    title: document.title,
  }));

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
  await page.waitForURL(/\/search/, { timeout: 15000 });
}
const navigationOk = /\/search/.test(page.url());

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
  if (item.path !== "/list-your-car.html" && !item.hasComposeApp && item.name !== "list your car") {
    if (!item.hasComposeApp) failures.push(`${item.name}: missing composeApp.js`);
  }
  if (item.name === "city landing" || item.name === "search") {
    if (!item.hasRoot || item.rootLen <= 0) failures.push(`${item.name}: compose root empty`);
  }
}

if (!navigationOk) failures.push("navigation: hero search button did not open /search");

const vendorScheduler = await fetch(`${base}/vendor/drivebit-third-party-scheduler.mjs`).then(
  (r) => r.ok,
).catch(() => false);
if (!vendorScheduler) failures.push("vendor: drivebit-third-party-scheduler.mjs not served");

const analytics = {
  ...thirdParty,
  vendorScheduler,
  note:
    "Metrika/Callibri may be blocked in headless; verify order manually in browser DevTools if needed.",
};

for (const err of pageErrors) {
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
