#!/usr/bin/env node
/**
 * Prefix root-absolute asset/nav URLs with the GitHub Pages project base path,
 * inject window.__DRIVEBIT_BASE__, and patch Image.src / setAttribute / history /
 * Location so Compose and static redirects resolve under the project site.
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
    else out.push(full);
  }
  return out;
}

function withBaseUrl(url) {
  if (
    !url ||
    url === base ||
    url.startsWith(`${base}/`) ||
    url.startsWith("//") ||
    url.startsWith("http") ||
    url.startsWith("data:") ||
    url.startsWith("blob:") ||
    url.startsWith("#")
  ) {
    return url;
  }
  if (url.startsWith("/")) return `${base}${url}`;
  return url;
}

function rewriteCssUrls(text) {
  return text.replace(/url\(\s*(['"]?)(\/[^)'"]+)\1\s*\)/g, (match, quote, url) => {
    const next = withBaseUrl(url);
    if (next === url) return match;
    const q = quote || "";
    return `url(${q}${next}${q})`;
  });
}

function rewriteHtml(text) {
  const inject = `<script>window.__DRIVEBIT_BASE__="${base}";</script>
<script>
(function (base) {
  if (!base) return;
  function withBase(u) {
    if (typeof u !== "string" || !u) return u;
    if (u.charAt(0) !== "/" || u.indexOf(base) === 0 || u.indexOf("//") === 0) return u;
    if (u.indexOf("http") === 0 || u.indexOf("data:") === 0 || u.indexOf("blob:") === 0) return u;
    return base + u;
  }
  try {
    var desc = Object.getOwnPropertyDescriptor(HTMLImageElement.prototype, "src");
    if (desc && desc.set) {
      Object.defineProperty(HTMLImageElement.prototype, "src", {
        configurable: true,
        enumerable: !!desc.enumerable,
        get: desc.get,
        set: function (v) { desc.set.call(this, withBase(v)); }
      });
    }
  } catch (e) {}
  try {
    var origSet = Element.prototype.setAttribute;
    Element.prototype.setAttribute = function (name, value) {
      var n = String(name || "").toLowerCase();
      if ((n === "src" || n === "href" || n === "data-src" || n === "data-hero-bg") && typeof value === "string") {
        return origSet.call(this, name, withBase(value));
      }
      return origSet.call(this, name, value);
    };
  } catch (e2) {}
  try {
    var origPush = history.pushState.bind(history);
    var origReplace = history.replaceState.bind(history);
    history.pushState = function (state, title, url) {
      return origPush(state, title, typeof url === "string" ? withBase(url) : url);
    };
    history.replaceState = function (state, title, url) {
      return origReplace(state, title, typeof url === "string" ? withBase(url) : url);
    };
  } catch (e3) {}
  try {
    var locDesc = Object.getOwnPropertyDescriptor(Location.prototype, "href");
    if (locDesc && locDesc.set) {
      Object.defineProperty(Location.prototype, "href", {
        configurable: true,
        enumerable: !!locDesc.enumerable,
        get: locDesc.get,
        set: function (v) { locDesc.set.call(this, withBase(v)); }
      });
    }
  } catch (e4) {}
  try {
    var origAssign = Location.prototype.assign;
    Location.prototype.assign = function (url) {
      return origAssign.call(this, typeof url === "string" ? withBase(url) : url);
    };
    var origLocReplace = Location.prototype.replace;
    Location.prototype.replace = function (url) {
      return origLocReplace.call(this, typeof url === "string" ? withBase(url) : url);
    };
  } catch (e5) {}
})(window.__DRIVEBIT_BASE__);
</script>
`;
  let next = text;
  if (!next.includes('window.__DRIVEBIT_BASE__="')) {
    next = next.replace(/(<head[^>]*>)/i, `$1\n${inject}`);
  }
  next = next.replace(
    /\b(href|src|data-src|data-hero-bg|action)=(["'])(\/[^"']*)\2/g,
    (match, attr, quote, url) => {
      const rewritten = withBaseUrl(url);
      if (rewritten === url) return match;
      return `${attr}=${quote}${rewritten}${quote}`;
    },
  );
  next = next.replace(
    /(http-equiv=["']refresh["'][^>]*content=["'][^"']*url=)(\/[^"'\s>]+)/gi,
    (match, prefix, url) => {
      const rewritten = withBaseUrl(url);
      if (rewritten === url) return match;
      return `${prefix}${rewritten}`;
    },
  );
  next = next.replace(
    /(content=["'][^"']*url=)(\/[^"'\s>]+)([^>]*http-equiv=["']refresh["'])/gi,
    (match, prefix, url, suffix) => {
      const rewritten = withBaseUrl(url);
      if (rewritten === url) return match;
      return `${prefix}${rewritten}${suffix}`;
    },
  );
  next = rewriteCssUrls(next);
  return next;
}

const files = walk(distDir);
let htmlCount = 0;
let cssCount = 0;
for (const file of files) {
  if (/\.html?$/i.test(file)) {
    const before = fs.readFileSync(file, "utf8");
    const after = rewriteHtml(before);
    if (after !== before) {
      fs.writeFileSync(file, after);
      htmlCount++;
    }
  } else if (/\.css$/i.test(file)) {
    const before = fs.readFileSync(file, "utf8");
    const after = rewriteCssUrls(before);
    if (after !== before) {
      fs.writeFileSync(file, after);
      cssCount++;
    }
  }
}
console.log(
  `Rewrote ${htmlCount} HTML and ${cssCount} CSS file(s) for base ${base}`,
);
