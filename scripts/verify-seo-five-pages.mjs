#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const goldenPath = path.join(root, "scripts/seo-five-pages-golden.json");
const blocksPath = path.join(root, "composeApp/seo/landing-blocks.json");
const bootstrapPath = path.join(root, "vendor/city-meta-bootstrap.js");

const STATIC_HTML = {
  "/moskva": "composeApp/src/jsMain/resources/moskva/index.html",
  "/moskva/puteshestviya": "composeApp/src/jsMain/resources/moskva/puteshestviya/index.html",
  "/moskva/arenda-avto-premium-klassa-bez-voditelya": "composeApp/src/jsMain/resources/moskva/arenda-avto-premium-klassa-bez-voditelya/index.html",
  "/moskva/poblizosti": "composeApp/src/jsMain/resources/moskva/poblizosti/index.html",
};

const DESCRIPTION_SUFFIX =
  ". Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!";
const TITLE_MARKER = " через сервис DriveBit";
const LEGACY_TITLE_SUFFIX = " - DriveBit";

const golden = JSON.parse(fs.readFileSync(goldenPath, "utf8"));
const blocks = JSON.parse(fs.readFileSync(blocksPath, "utf8"));
const paths = Object.keys(golden);

global.window = {};
await import(`file://${bootstrapPath}`);

const errors = [];

function fail(message) {
  errors.push(message);
}

function metaFromStaticHtml(relativePath) {
  const html = fs.readFileSync(path.join(root, relativePath), "utf8");
  const title = html.match(/<title>([^<]*)<\/title>/i)?.[1]?.trim() ?? "";
  const description =
    html.match(/<meta\s+name="description"\s+content="([^"]*)"/i)?.[1]?.trim() ?? "";
  return { title, description };
}

function extractSeoShell(relativePath) {
  const html = fs.readFileSync(path.join(root, relativePath), "utf8");
  const match = html.match(/<div class="drivebit-seo-shell">[\s\S]*?<\/div>\s*\n/);
  return match?.[0]?.trim() ?? "";
}

for (const urlPath of paths) {
  const expected = golden[urlPath];

  const jsMeta = global.window.drivebitCityPageMeta(urlPath);
  if (!jsMeta) {
    fail(`${urlPath}: drivebitCityPageMeta returned null`);
    continue;
  }

  if (jsMeta.title !== expected.title) {
    fail(`${urlPath}: bootstrap title mismatch\n  expected: ${expected.title}\n  actual:   ${jsMeta.title}`);
  }
  if (jsMeta.description !== expected.description) {
    fail(`${urlPath}: bootstrap description mismatch\n  expected: ${expected.description}\n  actual:   ${jsMeta.description}`);
  }

  if (!jsMeta.title.includes(TITLE_MARKER)) {
    fail(`${urlPath}: title missing "${TITLE_MARKER.trim()}"`);
  }
  if (jsMeta.title.includes(LEGACY_TITLE_SUFFIX)) {
    fail(`${urlPath}: title still uses legacy suffix "${LEGACY_TITLE_SUFFIX}"`);
  }
  if (!jsMeta.description.endsWith(DESCRIPTION_SUFFIX)) {
    fail(`${urlPath}: description missing required suffix`);
  }

  const staticFile = STATIC_HTML[urlPath];
  if (staticFile) {
    const staticMeta = metaFromStaticHtml(staticFile);
    if (staticMeta.title !== expected.title) {
      fail(`${urlPath}: static title mismatch\n  expected: ${expected.title}\n  actual:   ${staticMeta.title}`);
    }
    if (staticMeta.description !== expected.description) {
      fail(`${urlPath}: static description mismatch\n  expected: ${expected.description}\n  actual:   ${staticMeta.description}`);
    }

    const staticShell = extractSeoShell(staticFile);
    if (!staticShell.includes("drivebit-seo-text")) {
      fail(`${urlPath}: static HTML missing drivebit-seo-text shell`);
    }
    if (urlPath === "/moskva") {
      const html = fs.readFileSync(path.join(root, staticFile), "utf8");
      if (!html.includes("drivebit-cars-grid-static")) {
        fail(`${urlPath}: static HTML missing drivebit-cars-grid-static skeleton`);
      }
      if (!html.includes("drivebit-cars-grid-skeleton.css")) {
        fail(`${urlPath}: static HTML missing drivebit-cars-grid-skeleton.css link`);
      }
    }
  }
}

for (const urlPath of paths) {
  const block = blocks[urlPath];
  if (!block) {
    fail(`${urlPath}: missing landing-blocks.json entry`);
    continue;
  }
  if (!block.paragraphs?.length && !block.sections?.length) {
    fail(`${urlPath}: landing block has no paragraphs or sections`);
  }
}

const puteshestviya = blocks["/moskva/puteshestviya"];
if (!puteshestviya?.sections?.length) {
  fail("/moskva/puteshestviya: expected sections in landing-blocks.json");
} else {
  const hasTable = puteshestviya.sections.some((section) => section.table?.rows?.length);
  if (!hasTable) {
    fail("/moskva/puteshestviya: expected car-class table in sections");
  }
}

const liveBase = process.argv.includes("--live")
  ? process.argv[process.argv.indexOf("--live") + 1]
  : null;

if (liveBase) {
  for (const urlPath of paths) {
    const expected = golden[urlPath];
    const response = await fetch(`${liveBase.replace(/\/$/, "")}${urlPath}`);
    if (!response.ok) {
      fail(`${urlPath}: live fetch failed with ${response.status}`);
      continue;
    }
    const html = await response.text();
    const title = html.match(/<title>([^<]*)<\/title>/i)?.[1]?.trim() ?? "";
    const description =
      html.match(/<meta\s+name="description"\s+content="([^"]*)"/i)?.[1]?.trim() ?? "";
    if (title !== expected.title) {
      fail(`${urlPath}: live title mismatch\n  expected: ${expected.title}\n  actual:   ${title}`);
    }
    if (description !== expected.description) {
      fail(`${urlPath}: live description mismatch\n  expected: ${expected.description}\n  actual:   ${description}`);
    }
  }
}

if (errors.length) {
  console.error(`SEO five-pages verification failed (${errors.length} issue(s)):\n`);
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(`OK: verified ${paths.length} SEO pages (static/bootstrap/blocks${liveBase ? "/live" : ""})`);
