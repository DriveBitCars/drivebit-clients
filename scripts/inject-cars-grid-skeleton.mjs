#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const fragmentPath = path.join(root, "composeApp/seo/cars-grid-skeleton.fragment.html");
const skeleton = fs.readFileSync(fragmentPath, "utf8").trim();

const CSS_LINK = '    <link rel="stylesheet" href="/vendor/drivebit-cars-grid-skeleton.css"/>';
const SEO_LOADER = /<div class="drivebit-seo-loader"[^>]*><\/div>\s*/g;

const SKIP_BODY =
  /class="[^"]*drivebit-static-info-page|drivebit-info-page|drivebit-payment-result/;

function walk(dir, out = []) {
  for (const name of fs.readdirSync(dir)) {
    const full = path.join(dir, name);
    const st = fs.statSync(full);
    if (st.isDirectory()) walk(full, out);
    else if (name === "index.html") out.push(full);
  }
  return out;
}

function hasComposeApp(html) {
  return /composeApp\.js/.test(html);
}

function injectCss(html) {
  if (html.includes("drivebit-cars-grid-skeleton.css")) return html;
  if (html.includes("drivebit-main-promo.css")) {
    return html.replace(
      '<link rel="stylesheet" href="/vendor/drivebit-main-promo.css"/>',
      `<link rel="stylesheet" href="/vendor/drivebit-main-promo.css"/>\n${CSS_LINK}`,
    );
  }
  if (html.includes("drivebit-city-page.css")) {
    return html.replace(
      '<link rel="stylesheet" href="/vendor/drivebit-city-page.css"/>',
      `<link rel="stylesheet" href="/vendor/drivebit-city-page.css"/>\n${CSS_LINK}`,
    );
  }
  const m = html.match(/<link rel="stylesheet" href="\/vendor\/[^"]+"\/>/g);
  if (!m) throw new Error("no stylesheet links in head");
  const last = m[m.length - 1];
  return html.replace(last, `${last}\n${CSS_LINK}`);
}

function injectSkeleton(html) {
  if (html.includes("drivebit-cars-grid-static")) {
    return html.replace(SEO_LOADER, "");
  }
  let next = html.replace(SEO_LOADER, "");
  if (next.includes("<!-- drivebit-filters-end -->")) {
    next = next.replace(
      "<!-- drivebit-filters-end -->",
      `<!-- drivebit-filters-end -->\n\n${skeleton}\n`,
    );
    return next;
  }
  return next.replace(
    /(\n\s*)<div id="root"([^>]*)><\/div>/,
    `\n\n${skeleton}\n$1<div id="root"$2></div>`,
  );
}

const resources = path.join(root, "composeApp/src/jsMain/resources");
const rootIndex = path.join(root, "index.html");
const files = [...walk(resources), rootIndex].filter((f) => fs.existsSync(f));

let updated = 0;
let skipped = 0;

for (const file of files) {
  let html = fs.readFileSync(file, "utf8");
  if (!hasComposeApp(html)) continue;
  if (SKIP_BODY.test(html)) {
    skipped++;
    continue;
  }
  const before = html;
  html = injectCss(html);
  html = injectSkeleton(html);
  if (html !== before) {
    fs.writeFileSync(file, html);
    updated++;
    console.log("updated:", path.relative(root, file));
  }
}

console.log(`done: ${updated} updated, ${skipped} skipped (info/payment pages)`);
