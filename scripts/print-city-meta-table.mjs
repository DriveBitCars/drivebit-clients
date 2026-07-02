import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

const STATIC_HTML = {
  "/moskva": "composeApp/src/jsMain/resources/moskva/index.html",
  "/moskva/k-rodnym": "composeApp/src/jsMain/resources/moskva/k-rodnym/index.html",
  "/moskva/kanikuly": "composeApp/src/jsMain/resources/moskva/kanikuly/index.html",
  "/moskva/komandirovki": "composeApp/src/jsMain/resources/moskva/komandirovki/index.html",
  "/moskva/arenda-vnedorozhnika-bez-voditelya": "composeApp/src/jsMain/resources/moskva/arenda-vnedorozhnika-bez-voditelya/index.html",
  "/moskva/arenda-minivena-bez-voditelya": "composeApp/src/jsMain/resources/moskva/arenda-minivena-bez-voditelya/index.html",
  "/moskva/poblizosti": "composeApp/src/jsMain/resources/moskva/poblizosti/index.html",
  "/moskva/puteshestviya": "composeApp/src/jsMain/resources/moskva/puteshestviya/index.html",
  "/moskva/za-gorod": "composeApp/src/jsMain/resources/moskva/za-gorod/index.html",
  "/lyubertsy": "composeApp/src/jsMain/resources/lyubertsy/index.html",
  "/zelenograd": "composeApp/src/jsMain/resources/zelenograd/index.html",
  "/kaliningrad": "composeApp/src/jsMain/resources/kaliningrad/index.html",
  "/krasnogorsk": "composeApp/src/jsMain/resources/krasnogorsk/index.html",
};

const paths = JSON.parse(fs.readFileSync(process.argv[2] ?? "/tmp/paths.json", "utf8"));

global.window = {};
await import(`file://${path.join(root, "vendor/city-meta-bootstrap.js")}`);

function metaFromStaticHtml(relativePath) {
  const html = fs.readFileSync(path.join(root, relativePath), "utf8");
  const title = html.match(/<title>([^<]*)<\/title>/i)?.[1]?.trim() ?? "";
  const description =
    html.match(/<meta\s+name="description"\s+content="([^"]*)"/i)?.[1]?.trim() ?? "";
  return { title, description };
}

function metaForPath(urlPath) {
  const staticFile = STATIC_HTML[urlPath];
  if (staticFile) return metaFromStaticHtml(staticFile);
  return global.window.drivebitCityPageMeta(urlPath) ?? { title: "", description: "" };
}

const rows = paths.map((urlPath) => {
  const meta = metaForPath(urlPath);
  return {
    url: `http://localhost:8080${urlPath}`,
    title: meta.title ?? "",
    description: meta.description ?? "",
  };
});

const colUrl = Math.max(3, ...rows.map((r) => r.url.length));
const colTitle = Math.max(5, ...rows.map((r) => r.title.length));
const colDesc = Math.max(11, ...rows.map((r) => r.description.length));

const sep = `+-${"-".repeat(colUrl)}-+-${"-".repeat(colTitle)}-+-${"-".repeat(colDesc)}-+`;
const header = `| ${"URL".padEnd(colUrl)} | ${"Title".padEnd(colTitle)} | ${"Description".padEnd(colDesc)} |`;

console.log(sep);
console.log(header);
console.log(sep.replace(/\+/g, "+").replace(/-/g, "-"));
for (const row of rows) {
  console.log(
    `| ${row.url.padEnd(colUrl)} | ${row.title.padEnd(colTitle)} | ${row.description.padEnd(colDesc)} |`,
  );
}
console.log(sep);
