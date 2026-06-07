import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const resourcesDir = path.resolve(
  process.argv[2] ?? path.join(root, "composeApp/src/jsMain/resources"),
);
const templatePath = path.join(resourcesDir, "moskva/index.html");
const citySlugs = (process.argv[3] ?? "lyubertsy,zelenograd,kaliningrad,krasnogorsk").split(",");

if (!fs.existsSync(templatePath)) {
  console.error(`Missing template: ${templatePath}`);
  process.exit(1);
}

const template = fs.readFileSync(templatePath, "utf8");

global.window = {};
await import(`file://${path.join(root, "vendor/city-meta-bootstrap.js")}`);

function escapeHtml(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function applyCityPage(templateHtml, citySlug, meta) {
  const canonicalUrl = `https://drivebit.ru${meta.path}`;
  const title = escapeHtml(meta.title);
  const description = escapeHtml(meta.description);
  const headline = escapeHtml(meta.headline);

  return templateHtml
    .replaceAll("/moskva", `/${citySlug}`)
    .replaceAll("https://drivebit.ru/moskva", canonicalUrl)
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
    )
    .replace(
      /(<h1[^>]*id="drivebit-hero-headline"[^>]*>)[^<]*(<\/h1>)/i,
      `$1${headline}$2`,
    )
    .replace(
      /(<section class="drivebit-seo-text" aria-label=")[^"]*(")/i,
      `$1${headline}$2`,
    )
    .replace(
      /(<h2>)Аренда автомобиля в Москве у частных владельцев(<\/h2>)/i,
      `$1Аренда автомобиля в ${escapeHtml(meta.cityNamePrep)} у частных владельцев$2`,
    )
    .replace(/в Москве/g, `в ${meta.cityNamePrep}`);
}

let generated = 0;
for (const citySlug of citySlugs.map((slug) => slug.trim()).filter(Boolean)) {
  if (citySlug === "moskva") continue;

  const meta = global.window.drivebitCityPageMeta(`/${citySlug}`);
  if (!meta?.title || !meta?.description || !meta?.headline) {
    console.warn(`Skip ${citySlug}: no meta`);
    continue;
  }

  const outDir = path.join(resourcesDir, citySlug);
  const outPath = path.join(outDir, "index.html");
  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(outPath, applyCityPage(template, citySlug, meta));
  generated += 1;
  console.log(`Wrote ${outPath}`);
  console.log(`  title: ${meta.title}`);
}

console.log(`City SEO pages written: ${generated}`);
