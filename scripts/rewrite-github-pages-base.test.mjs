import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const script = path.join(root, "scripts/rewrite-github-pages-base.mjs");

test("rewrites root-absolute assets and injects DRIVEBIT_BASE", () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "pages-base-"));
  const htmlPath = path.join(dir, "index.html");
  fs.writeFileSync(
    htmlPath,
    `<!DOCTYPE html><html><head><title>t</title>
<link rel="stylesheet" href="/vendor/drivebit-fonts.css"/>
<script src="/composeApp.js"></script>
</head><body><img src="/images/logos/logo.png" alt=""></body></html>`,
  );

  const result = spawnSync("node", [script, dir, "/drivebit-clients"], {
    encoding: "utf8",
  });
  assert.equal(result.status, 0, result.stderr || result.stdout);

  const out = fs.readFileSync(htmlPath, "utf8");
  assert.match(out, /window\.__DRIVEBIT_BASE__="\/drivebit-clients"/);
  assert.match(out, /href="\/drivebit-clients\/vendor\/drivebit-fonts\.css"/);
  assert.match(out, /src="\/drivebit-clients\/composeApp\.js"/);
  assert.match(out, /src="\/drivebit-clients\/images\/logos\/logo\.png"/);
});
