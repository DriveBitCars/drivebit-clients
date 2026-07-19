import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { describe, it } from "node:test";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const searchIndex = path.join(root, "composeApp/src/jsMain/resources/search/index.html");

describe("search/index.html redirect stub", () => {
  it("points bare /search to /moskva/search (not a SPA shell)", () => {
    const html = fs.readFileSync(searchIndex, "utf8");
    assert.match(html, /moskva\/search/);
    assert.match(html, /location\.replace\('\/moskva\/search'/);
    assert.doesNotMatch(html, /appCompose\.js/);
    assert.doesNotMatch(html, /id="root"/);
  });
});
