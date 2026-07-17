#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const resourcesDir = path.join(root, "composeApp/src/jsMain/resources");
const blocksPath = path.join(root, "composeApp/seo/landing-blocks.json");
const fragmentPath = path.join(root, "composeApp/seo/search-page-headline.fragment.html");
const cssLink = '    <link rel="stylesheet" href="/vendor/drivebit-search-page.css"/>';

const blocks = JSON.parse(fs.readFileSync(blocksPath, "utf8"));
const fragmentTemplate = fs.readFileSync(fragmentPath, "utf8").trim();

function escapeHtml(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function pathToHtmlFile(routePath) {
  const normalized = routePath.replace(/^\//, "");
  if (!normalized) return null;
  if (normalized === "search") {
    return path.join(resourcesDir, "search/index.html");
  }
  if (normalized.startsWith("search/")) {
    return path.join(resourcesDir, normalized, "index.html");
  }
  if (normalized === "audi" || normalized === "bmw") {
    return path.join(resourcesDir, normalized, "index.html");
  }
  return null;
}

function injectCss(html) {
  if (html.includes("drivebit-search-page.css")) return html;
  if (html.includes("drivebit-cars-grid-skeleton.css")) {
    return html.replace(
      '<link rel="stylesheet" href="/vendor/drivebit-cars-grid-skeleton.css"/>',
      `<link rel="stylesheet" href="/vendor/drivebit-cars-grid-skeleton.css"/>\n${cssLink}`,
    );
  }
  const lastStylesheet = html.match(/<link rel="stylesheet" href="\/vendor\/[^"]+"\/>/g)?.at(-1);
  if (!lastStylesheet) throw new Error("No vendor stylesheet link found");
  return html.replace(lastStylesheet, `${lastStylesheet}\n${cssLink}`);
}

function buildHeadlineBlock(headline) {
  return fragmentTemplate.replace("__HEADLINE__", escapeHtml(headline));
}

function injectOrUpdateHeadline(html, headline) {
  const block = buildHeadlineBlock(headline);
  if (html.includes("drivebit-search-headline-start")) {
    return html.replace(
      /<!-- drivebit-search-headline-start -->[\s\S]*?<!-- drivebit-search-headline-end -->/,
      block,
    );
  }
  if (html.includes("<!-- drivebit-app-header-end -->")) {
    return html.replace(
      "<!-- drivebit-app-header-end -->",
      `<!-- drivebit-app-header-end -->\n\n${block}\n`,
    );
  }
  throw new Error("Could not find insertion point for search headline");
}

function createSearchIndexFromTemplate(templateHtml, block) {
  const headline = block.h1 ?? block.h2;
  const title = escapeHtml(block.title ?? headline);
  const description = escapeHtml(block.description ?? "");
  const canonical = "https://drivebit.ru/search";

  return injectOrUpdateHeadline(
    injectCss(templateHtml)
      .replace(/<title>[^<]*<\/title>/i, `<title>${title}</title>`)
      .replace(
        /(<meta\s+name="description"\s+content=")[^"]*(")/i,
        `$1${description}$2`,
      )
      .replace(
        /(<link\s+rel="canonical"\s+href=")[^"]*(")/i,
        `$1${canonical}$2`,
      )
      .replace(
        /(<meta\s+property="og:url"\s+content=")[^"]*(")/i,
        `$1${canonical}$2`,
      )
      .replace(
        /(<meta\s+property="og:title"\s+content=")[^"]*(")/i,
        `$1${title}$2`,
      )
      .replace(
        /(<meta\s+property="og:description"\s+content=")[^"]*(")/i,
        `$1${description}$2`,
      )
      .replace(
        /(<meta\s+name="twitter:url"\s+content=")[^"]*(")/i,
        `$1${canonical}$2`,
      )
      .replace(
        /(<meta\s+name="twitter:title"\s+content=")[^"]*(")/i,
        `$1${title}$2`,
      )
      .replace(
        /(<meta\s+name="twitter:description"\s+content=")[^"]*(")/i,
        `$1${description}$2`,
      )
      .replace(
        /<section class="drivebit-seo-text" aria-label="[^"]*">[\s\S]*?<\/section>/,
        `<section class="drivebit-seo-text" aria-label="${escapeHtml(block.ariaLabel)}">
            <h2>${escapeHtml(headline)}</h2>
            ${block.paragraphs.map((p) => `<p>${escapeHtml(p)}</p>`).join("\n            ")}
        </section>`,
      ),
    headline,
  );
}

const searchRoutes = Object.entries(blocks).filter(
  ([route]) =>
    route === "/search" ||
    route.startsWith("/search/") ||
    route === "/audi" ||
    route === "/bmw",
);
let updated = 0;
let created = 0;

for (const [route, block] of searchRoutes) {
  const headline = block.h1 ?? block.h2;
  const htmlPath = pathToHtmlFile(route);

  if (!htmlPath) {
    console.warn(`Skip unknown route mapping: ${route}`);
    continue;
  }

  if (!fs.existsSync(htmlPath)) {
    if (route !== "/search") {
      console.warn(`Missing HTML for ${route}: ${htmlPath}`);
      continue;
    }
    const templatePath = path.join(resourcesDir, "search/skoda/index.html");
    const templateHtml = fs.readFileSync(templatePath, "utf8");
    const next = createSearchIndexFromTemplate(templateHtml, block);
    fs.mkdirSync(path.dirname(htmlPath), { recursive: true });
    fs.writeFileSync(htmlPath, next);
    created++;
    console.log(`Created ${htmlPath}`);
    continue;
  }

  const html = fs.readFileSync(htmlPath, "utf8");
  const next = injectOrUpdateHeadline(injectCss(html), headline);
  if (next !== html) {
    fs.writeFileSync(htmlPath, next);
    updated++;
    console.log(`Updated ${htmlPath}`);
  }
}

console.log(`Done. Updated ${updated} file(s), created ${created} file(s).`);
