#!/usr/bin/env node
/**
 * Prefix root-absolute href/src/data-src/action URLs with the GitHub Pages
 * project base path and inject window.__DRIVEBIT_BASE__ into HTML heads.
 *
 * Usage: node scripts/rewrite-github-pages-base.mjs <distDir> [basePath]
 */
import fs from "fs";
import path from "path";

const distDir = path.resolve(process.argv[2] || "dist");
const base = process.argv[3] || "/drivebit-clients";

function walk(dir, out = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, out);
    else if (/\.html?$/i.test(entry.name)) out.push(full);
  }
  return out;
}

function rewriteHtml(text) {
  const inject = `<script>window.__DRIVEBIT_BASE__="${base}";</script>\n`;
  let next = text;
  if (!next.includes("__DRIVEBIT_BASE__")) {
    next = next.replace(/(<head[^>]*>)/i, `$1\n${inject}`);
  }
  next = next.replace(
    /\b(href|src|data-src|action)=(["'])(\/[^"']*)\2/g,
    (match, attr, quote, url) => {
      if (
        url === base ||
        url.startsWith(`${base}/`) ||
        url.startsWith("//") ||
        url.startsWith("http")
      ) {
        return match;
      }
      return `${attr}=${quote}${base}${url}${quote}`;
    },
  );
  return next;
}

const files = walk(distDir);
for (const file of files) {
  const before = fs.readFileSync(file, "utf8");
  const after = rewriteHtml(before);
  if (after !== before) fs.writeFileSync(file, after);
}
console.log(`Rewrote ${files.length} HTML file(s) for base ${base}`);
