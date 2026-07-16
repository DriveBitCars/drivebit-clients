#!/usr/bin/env node
import { chromium } from "playwright";

const base = process.argv[2] || "http://127.0.0.1:8080";
const url = `${base.replace(/\/$/, "")}/moskva`;

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 390, height: 844 } });
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));

await page.goto(url, { waitUntil: "load", timeout: 60000 });
await page.waitForTimeout(8000);

const report = await page.evaluate(() => ({
  hasComposeApp: !!document.querySelector('script[src*="composeApp.js"]'),
  hasThirdPartyLoader: !!document.querySelector('script[src*="drivebit-third-party-deferred"]'),
  hasRoot: !!document.getElementById("root"),
  hasLoader: !!document.querySelector(".drivebit-seo-loader"),
  hasLegacyNav: !!document.querySelector(".drivebit-seo-nav"),
  hasLegacyCars: document.querySelectorAll(".drivebit-seo-car-card").length,
  rootLen: document.getElementById("root")?.innerHTML?.length ?? 0,
}));

console.log(JSON.stringify({ url, ...report, errors }, null, 2));
const ok =
  report.hasComposeApp &&
  report.hasThirdPartyLoader &&
  report.hasRoot &&
  report.hasLoader &&
  !report.hasLegacyNav &&
  report.hasLegacyCars === 0 &&
  report.rootLen > 0 &&
  errors.length === 0;
console.log(ok ? "OK: /moskva loader + compose mount" : "FAIL");
await browser.close();
process.exit(ok ? 0 : 1);
