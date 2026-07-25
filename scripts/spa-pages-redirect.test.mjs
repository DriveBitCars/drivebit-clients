import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { describe, it } from "node:test";
import vm from "node:vm";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const indexHtml = fs.readFileSync(path.join(root, "index.html"), "utf8");
const redirectScript = indexHtml.match(
  /<script>\s*(\(function \(\) \{\s*var l = window\.location;[\s\S]*?\}\)\(\);)\s*<\/script>/,
)?.[1];

describe("GitHub Pages SPA redirect", () => {
  it("preserves every query parameter after a deep-link refresh", () => {
    assert.ok(redirectScript);
    let replacedUrl = null;

    vm.runInNewContext(redirectScript, {
      window: {
        location: {
          search:
            "?/rostov-na-donu/poblizosti&lat=47.195612&lon=39.633356&radiusKm=5",
          hash: "",
        },
        history: {
          replaceState: (_state, _title, url) => {
            replacedUrl = url;
          },
        },
      },
    });

    assert.equal(
      replacedUrl,
      "/rostov-na-donu/poblizosti?lat=47.195612&lon=39.633356&radiusKm=5",
    );
  });
});
