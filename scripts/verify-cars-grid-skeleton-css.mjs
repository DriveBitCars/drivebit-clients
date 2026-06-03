#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { chromium } from "playwright";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const fixtureDir = path.join(root, ".tmp-skeleton-css-fixture");
const skeletonCss = fs.readFileSync(path.join(root, "vendor/drivebit-cars-grid-skeleton.css"), "utf8");
const fragment = fs.readFileSync(
  path.join(root, "composeApp/seo/cars-grid-skeleton.fragment.html"),
  "utf8",
);

fs.mkdirSync(fixtureDir, { recursive: true });
fs.writeFileSync(
  path.join(fixtureDir, "index.html"),
  `<!DOCTYPE html>
<html lang="ru"><head>
<link rel="stylesheet" href="./drivebit-cars-grid-skeleton.css"/>
</head><body>
${fragment}
<div id="root"></div>
<script>
setTimeout(() => {
  const g = document.createElement('div');
  g.className = 'drivebit-cars-grid-mounted';
  g.textContent = 'loaded';
  document.getElementById('root').appendChild(g);
}, 50);
</script>
</body></html>`,
);
fs.writeFileSync(path.join(fixtureDir, "drivebit-cars-grid-skeleton.css"), skeletonCss);

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage();
await page.goto(`file://${path.join(fixtureDir, "index.html")}`);
await page.waitForTimeout(200);
const visible = await page.evaluate(() => {
  const s = document.getElementById("drivebit-cars-grid-static");
  return !!(s && getComputedStyle(s).display !== "none");
});
await browser.close();

if (visible) {
  console.error("FAIL: HTML skeleton still visible after drivebit-cars-grid-mounted");
  process.exit(1);
}
console.log("OK: CSS hides HTML skeleton when drivebit-cars-grid-mounted is in #root");
