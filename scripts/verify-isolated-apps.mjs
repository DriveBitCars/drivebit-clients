#!/usr/bin/env node
import { chromium } from "playwright";

const base = (process.argv[2] || "http://127.0.0.1:8080").replace(/\/$/, "");

const cases = [
  {
    name: "home-moskva",
    path: "/moskva",
    expectNotScript: "composeApp.js",
    ready: async (page) => {
      await page.waitForSelector(".drivebit-seo-cars", { timeout: 60000 });
      const count = await page.locator(".drivebit-seo-car-card").count();
      if (count === 0) throw new Error("no static car cards on /moskva");
    },
  },
  {
    name: "poblizosti-nearbyApp",
    path: "/moskva/poblizosti",
    expectScript: "nearbyApp.js",
    expectNotScript: "composeApp.js",
    ready: async (page) => {
      await page.waitForSelector(".leaflet-container", { timeout: 60000 });
      await page.waitForFunction(
        () => document.querySelectorAll(".leaflet-tile-loaded").length > 0,
        { timeout: 60000 },
      );
    },
  },
  {
    name: "search-root",
    path: "/search",
    expectScript: "searchApp.js",
    expectNotScript: "composeApp.js",
    ready: async (page) => {
      await page.waitForSelector('text=Аренда авто', { timeout: 60000 });
    },
  },
  {
    name: "search-bmw",
    path: "/search/bmw",
    expectScript: "searchApp.js",
    expectNotScript: "composeApp.js",
    ready: async (page) => {
      await page.waitForSelector('img[src*="/publicbct/"], .drivebit-seo-cars', { timeout: 60000 });
    },
  },
];

const browser = await chromium.launch({ headless: true });
const results = [];

for (const testCase of cases) {
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  page.on("console", (msg) => {
    if (msg.type() === "error") errors.push(msg.text());
  });

  let status = "pass";
  let detail = "";

  try {
    const response = await page.goto(`${base}${testCase.path}`, {
      waitUntil: "domcontentloaded",
      timeout: 120000,
    });
    if (!response || response.status() >= 400) {
      throw new Error(`HTTP ${response?.status() ?? "no response"}`);
    }

    await page.waitForTimeout(2000);
    await testCase.ready(page);

    const scripts = await page.evaluate(() =>
      Array.from(document.querySelectorAll("script[src]")).map((s) => s.getAttribute("src") || ""),
    );

    const hasExpected = testCase.expectScript
      ? scripts.some((s) => s.includes(testCase.expectScript))
      : true;
    const hasForbidden = testCase.expectNotScript
      ? scripts.some((s) => s.includes(testCase.expectNotScript))
      : false;

    if (testCase.expectScript && !hasExpected) {
      throw new Error(`missing script ${testCase.expectScript}; got ${scripts.join(", ")}`);
    }
    if (hasForbidden) {
      throw new Error(`forbidden script ${testCase.expectNotScript} present`);
    }

    detail = `scripts ok; errors=${errors.length}`;
  } catch (e) {
    status = "fail";
    detail = e.message;
  }

  results.push({ name: testCase.name, path: testCase.path, status, detail, errors: errors.slice(0, 5) });
  await page.close();
}

// Navigation: moskva -> Pobлизosti (full reload to nearbyApp)
{
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  let status = "pass";
  let detail = "";
  try {
    await page.goto(`${base}/moskva`, { waitUntil: "domcontentloaded", timeout: 120000 });
    await page.waitForSelector(".drivebit-seo-nav a[href='/moskva/poblizosti']", { timeout: 60000 });
    await page.locator(".drivebit-seo-nav a[href='/moskva/poblizosti']").first().click();
    await page.waitForURL("**/moskva/poblizosti**", { timeout: 30000 });
    await page.waitForSelector(".leaflet-container", { timeout: 60000 });
    const scripts = await page.evaluate(() =>
      Array.from(document.querySelectorAll("script[src]")).map((s) => s.getAttribute("src") || ""),
    );
    if (!scripts.some((s) => s.includes("nearbyApp.js"))) {
      throw new Error("after filter click, nearbyApp.js not loaded");
    }
    detail = "navigated to poblizosti with map";
  } catch (e) {
    status = "fail";
    detail = e.message;
  }
  results.push({
    name: "nav-moskva-to-poblizosti",
    path: "/moskva -> /moskva/poblizosti",
    status,
    detail,
    errors: errors.slice(0, 5),
  });
  await page.close();
}

// Car card click from search -> car-detail
{
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  let status = "pass";
  let detail = "";
  try {
    await page.goto(`${base}/search/bmw`, { waitUntil: "domcontentloaded", timeout: 120000 });
    await page.waitForTimeout(5000);
    const card = page.locator('img[src*="/publicbct/"]').first();
    if ((await card.count()) === 0) {
      throw new Error("no car cards on search/bmw");
    }
    await card.click({ timeout: 10000 });
    await page.waitForURL("**/car-detail**", { timeout: 30000 });
    const scripts = await page.evaluate(() =>
      Array.from(document.querySelectorAll("script[src]")).map((s) => s.getAttribute("src") || ""),
    );
    if (!scripts.some((s) => s.includes("carDetail.js"))) {
      throw new Error("car-detail page missing carDetail.js");
    }
    detail = page.url();
  } catch (e) {
    status = "fail";
    detail = e.message;
  }
  results.push({
    name: "search-to-car-detail",
    path: "/search/bmw -> /car-detail",
    status,
    detail,
    errors: errors.slice(0, 5),
  });
  await page.close();
}

await browser.close();

const failed = results.filter((r) => r.status === "fail");
console.log(JSON.stringify({ base, results, failed: failed.length }, null, 2));
console.log(failed.length === 0 ? "OK: all isolated-app checks passed" : "FAIL: see results above");
process.exit(failed.length === 0 ? 0 : 1);
