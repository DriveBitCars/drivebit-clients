import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const script = path.join(root, "scripts/rewrite-github-pages-base.mjs");

test("rewrites root-absolute assets, data-hero-bg, CSS urls, and injects base", () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "pages-base-"));
  const htmlPath = path.join(dir, "index.html");
  const cssPath = path.join(dir, "app.css");
  fs.writeFileSync(
    htmlPath,
    `<!DOCTYPE html><html><head><title>t</title>
<link rel="stylesheet" href="/vendor/drivebit-fonts.css"/>
<style>.hero{background:url('/images/backgrounds/hero-bg.jpg')}</style>
<script src="/composeApp.js"></script>
</head><body>
<img id="drivebit-hero-bg" src="/images/searchbackground/car0.jpg" alt="">
<button data-hero-bg="/images/searchbackground/car1.jpg" data-filter-path="/moskva"></button>
<img src="/images/logos/logo.png" alt="">
</body></html>`,
  );
  fs.writeFileSync(
    cssPath,
    `.x{background-image:url(/images/backgrounds/hero-bg.jpg)}
.y{background:url("/images/searchbackground/car0.jpg")}`,
  );

  const result = spawnSync("node", [script, dir, "/drivebit-clients"], {
    encoding: "utf8",
  });
  assert.equal(result.status, 0, result.stderr || result.stdout);

  const out = fs.readFileSync(htmlPath, "utf8");
  assert.match(out, /window\.__DRIVEBIT_BASE__="\/drivebit-clients"/);
  assert.match(out, /HTMLImageElement\.prototype/);
  assert.match(out, /href="\/drivebit-clients\/vendor\/drivebit-fonts\.css"/);
  assert.match(out, /src="\/drivebit-clients\/images\/searchbackground\/car0\.jpg"/);
  assert.match(out, /data-hero-bg="\/drivebit-clients\/images\/searchbackground\/car1\.jpg"/);
  assert.match(out, /url\('\/drivebit-clients\/images\/backgrounds\/hero-bg\.jpg'\)/);
  assert.match(out, /data-filter-path="\/moskva"/);

  const css = fs.readFileSync(cssPath, "utf8");
  assert.match(css, /url\(\/drivebit-clients\/images\/backgrounds\/hero-bg\.jpg\)/);
  assert.match(css, /url\("\/drivebit-clients\/images\/searchbackground\/car0\.jpg"\)/);
});
