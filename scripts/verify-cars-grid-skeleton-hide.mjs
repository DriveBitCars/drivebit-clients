#!/usr/bin/env node
import { chromium } from "playwright";

const base = (process.argv[2] || "http://127.0.0.1:8765").replace(/\/$/, "");

async function checkSkeletonHidden(page, label) {
  return page.evaluate((tag) => {
    const htmlSkeleton = document.getElementById("drivebit-cars-grid-static");
    const htmlVisible =
      htmlSkeleton &&
      htmlSkeleton.classList.contains("drivebit-cars-grid-compose") === false &&
      getComputedStyle(htmlSkeleton).display !== "none";
    const mounted = document.querySelector("#root .drivebit-cars-grid-mounted");
    const composeShimmer = document.querySelector("#root .drivebit-cars-grid-compose");
    const pagination = document.querySelector("#root .drivebit-pagination-bar");
    const carCards = document.querySelectorAll("#root img, #root [class*='car']").length;
    return {
      tag,
      htmlSkeletonVisible: htmlVisible,
      hasMountedGrid: !!mounted,
      hasComposeShimmer: !!composeShimmer,
      hasPagination: !!pagination,
      rootChildCount: document.getElementById("root")?.children.length ?? 0,
    };
  }, label);
}

async function waitForCarsOrError(page, timeoutMs = 45000) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    const state = await page.evaluate(() => ({
      mounted: !!document.querySelector("#root .drivebit-cars-grid-mounted"),
      shimmer: !!document.querySelector("#root .drivebit-cars-grid-compose"),
      error: !!document.querySelector("#root .drivebit-main-content-error"),
      pagination: !!document.querySelector("#root .drivebit-pagination-bar"),
    }));
    if (state.mounted || state.error || state.pagination) return state;
    await page.waitForTimeout(500);
  }
  return null;
}

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));

const cases = [
  { path: "/moskva", filter: null, name: "moskva-all" },
  { path: "/moskva/k-rodnym", filter: "К родным", name: "k-rodnym" },
];

const results = [];

for (const c of cases) {
  await page.goto(`${base}${c.path}`, { waitUntil: "load", timeout: 60000 });
  if (c.filter) {
    await page.locator(".drivebit-filter-btn", { hasText: c.filter }).click();
    await page.waitForTimeout(500);
  }
  const loaded = await waitForCarsOrError(page);
  await page.waitForTimeout(1500);
  const report = await checkSkeletonHidden(page, c.name);
  results.push({ ...c, loaded, report });
}

await browser.close();

let ok = errors.length === 0;
for (const r of results) {
  const hideOk = !r.report.htmlSkeletonVisible;
  const contentOk = r.loaded?.mounted || r.loaded?.pagination || r.loaded?.error;
  if (!hideOk || !contentOk) ok = false;
  console.log(
    JSON.stringify(
      {
        case: r.name,
        hideOk,
        contentOk,
        loaded: r.loaded,
        report: r.report,
      },
      null,
      2,
    ),
  );
}

if (errors.length) console.error("page errors:", errors);
console.log(ok ? "OK: HTML skeleton hidden after cars load" : "FAIL");
process.exit(ok ? 0 : 1);
