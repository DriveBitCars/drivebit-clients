import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { describe, it } from "node:test";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const cssPath = path.join(root, "vendor/drivebit-cars-grid-skeleton.css");

describe("cars grid static shell hide rules", () => {
  it("hides static skeleton when Compose shows empty search results", () => {
    const css = fs.readFileSync(cssPath, "utf8");
    assert.match(
      css,
      /body:has\(#root \.drivebit-search-results-empty\) #drivebit-cars-grid-static/,
    );
  });
});
