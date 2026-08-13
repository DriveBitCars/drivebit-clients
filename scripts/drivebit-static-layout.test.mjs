import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { describe, it } from "node:test";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const cssPath = path.join(root, "vendor/drivebit-static-layout.css");

describe("static layout page canvas", () => {
  it("opts html into light color-scheme so browsers do not auto-darken the page", () => {
    const css = fs.readFileSync(cssPath, "utf8");
    assert.match(css, /html\s*\{[^}]*color-scheme:\s*light/s);
  });

  it("sets an opaque white html/body background so dark OS themes cannot show through", () => {
    const css = fs.readFileSync(cssPath, "utf8");
    assert.match(css, /html\s*\{[^}]*background:\s*#fff/s);
    assert.match(css, /body\s*\{[^}]*background:\s*#fff/s);
  });
});
