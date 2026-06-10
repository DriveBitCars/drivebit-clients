#!/usr/bin/env node
import { chromium } from "playwright";

const baseUrl = process.argv[2] || "http://127.0.0.1:8081/";

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext();
const page = await context.newPage({ viewport: { width: 1280, height: 900 } });
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));

await page.goto(baseUrl, { waitUntil: "networkidle", timeout: 120000 });
await page.waitForTimeout(5000);

const cardLink = page.locator('a[href*="/car-detail?id="]').first();
const linkCount = await page.locator('a[href*="/car-detail?id="]').count();

if (linkCount === 0) {
  console.log(JSON.stringify({ ok: false, reason: "no car detail links on page", linkCount, errors }, null, 2));
  await browser.close();
  process.exit(1);
}

const href = await cardLink.getAttribute("href");
const imgPointerEvents = await cardLink.locator("img").first().evaluate((img) => getComputedStyle(img).pointerEvents);

const [newPage] = await Promise.all([
  context.waitForEvent("page"),
  cardLink.click({ button: "middle" }),
]);

await newPage.waitForLoadState("domcontentloaded", { timeout: 30000 });
await newPage.waitForTimeout(1500);

const newTabUrl = newPage.url();
const ok =
  href != null &&
  href.includes("/car-detail") &&
  href.includes("id=") &&
  imgPointerEvents === "none" &&
  newTabUrl.includes("/car-detail") &&
  newTabUrl.includes("id=");

console.log(
  JSON.stringify(
    {
      ok,
      linkCount,
      href,
      imgPointerEvents,
      newTabUrl,
      errors: errors.slice(0, 5),
    },
    null,
    2,
  ),
);
console.log(ok ? "OK: карточка открывается в новой вкладке по ссылке" : "FAIL: открытие в новой вкладке не сработало");

await browser.close();
process.exit(ok ? 0 : 1);
