import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const distDir = path.resolve(process.argv[2] ?? path.join(root, "composeApp/build/dist/js/productionExecutable"));
const templatePath = path.join(distDir, "index.html");

if (!fs.existsSync(templatePath)) {
  console.error(`Missing template: ${templatePath}`);
  process.exit(1);
}

const template = fs.readFileSync(templatePath, "utf8");

global.window = {};
await import(`file://${path.join(root, "vendor/city-meta-bootstrap.js")}`);

const SKIP_CITY_SLUGS = new Set(["moskva"]);

function escapeHtml(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function applyMeta(html, meta) {
  const canonicalUrl = `https://drivebit.ru${meta.path}`;
  const title = escapeHtml(meta.title);
  const description = escapeHtml(meta.description);

  return html
    .replace(/<title>[^<]*<\/title>/i, `<title>${title}</title>`)
    .replace(
      /(<meta\s+name="description"\s+content=")[^"]*(")/i,
      `$1${description}$2`,
    )
    .replace(
      /(<link\s+rel="canonical"\s+href=")[^"]*(")/i,
      `$1${canonicalUrl}$2`,
    )
    .replace(
      /(<meta\s+property="og:url"\s+content=")[^"]*(")/i,
      `$1${canonicalUrl}$2`,
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
      `$1${canonicalUrl}$2`,
    )
    .replace(
      /(<meta\s+name="twitter:title"\s+content=")[^"]*(")/i,
      `$1${title}$2`,
    )
    .replace(
      /(<meta\s+name="twitter:description"\s+content=")[^"]*(")/i,
      `$1${description}$2`,
    );
}

const citySlugs = global.window.drivebitCitySlugs ?? [];

let generated = 0;
for (const citySlug of citySlugs) {
  if (SKIP_CITY_SLUGS.has(citySlug)) continue;

  const meta = global.window.drivebitCityPageMeta(`/${citySlug}`);
  if (!meta?.title || !meta?.description) {
    console.warn(`Skip ${citySlug}: no meta`);
    continue;
  }

  const outDir = path.join(distDir, citySlug);
  const outPath = path.join(outDir, "index.html");
  if (fs.existsSync(outPath)) {
    console.log(`Skip ${citySlug}: already has index.html`);
    continue;
  }

  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(outPath, applyMeta(template, meta));
  generated += 1;
  console.log(`Generated ${outPath}`);
  console.log(`  title: ${meta.title}`);
}

console.log(`City SEO pages generated: ${generated}`);
