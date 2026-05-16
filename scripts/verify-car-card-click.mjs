#!/usr/bin/env node
import { chromium } from "playwright";

const baseUrl = process.argv[2] || "http://127.0.0.1:8081/";

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));

await page.goto(baseUrl, { waitUntil: "networkidle", timeout: 120000 });
await page.waitForTimeout(5000);

const beforePath = await page.evaluate(() => window.location.pathname);
const card = page.locator('img[src*="/publicbct/"]').first();
const cardCount = await page.locator('img[src*="/publicbct/"]').count();

if (cardCount === 0) {
  console.log(JSON.stringify({ ok: false, reason: "no car images on page", beforePath, errors }, null, 2));
  await browser.close();
  process.exit(1);
}

await card.click({ timeout: 10000 });
await page.waitForTimeout(1500);

const after = await page.evaluate(() => ({
  pathname: window.location.pathname,
  search: window.location.search,
  href: window.location.href,
}));

const ok =
  after.pathname === "/car-detail" &&
  after.search.includes("id=") &&
  after.pathname !== beforePath;

console.log(
  JSON.stringify(
    {
      ok,
      beforePath,
      after,
      cardCount,
      errors: errors.slice(0, 5),
    },
    null,
    2,
  ),
);
console.log(ok ? "OK: клик открыл /car-detail" : "FAIL: переход на карточку не сработал");

await browser.close();
process.exit(ok ? 0 : 1);
