#!/usr/bin/env node
/**
 * Compare mobile load metrics before/after Tier-1 HTML changes.
 * Usage: node scripts/measure-tier1-perf.mjs --base http://127.0.0.1:8765
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { chromium, devices } from "playwright";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

function parseArgs(argv) {
  let base = "http://127.0.0.1:8765";
  let outDir = path.join(root, ".perf/tier1");
  for (let i = 2; i < argv.length; i++) {
    if (argv[i] === "--base" && argv[i + 1]) base = argv[++i].replace(/\/$/, "");
    else if (argv[i] === "--out" && argv[i + 1]) outDir = path.resolve(argv[++i]);
  }
  return { base, outDir };
}

async function measurePage(page, url) {
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));

  const fcpPromise = page
    .evaluate(() => {
      return new Promise((resolve) => {
        const obs = new PerformanceObserver((list) => {
          for (const e of list.getEntries()) {
            if (e.name === "first-contentful-paint") {
              resolve(e.startTime);
              obs.disconnect();
            }
          }
        });
        obs.observe({ type: "paint", buffered: true });
        setTimeout(() => resolve(null), 30000);
      });
    })
    .catch(() => null);

  await page.goto(url, { waitUntil: "load", timeout: 120000 });
  const fcp = await fcpPromise;

  const data = await page.evaluate(() => {
    const nav = performance.getEntriesByType("navigation")[0];
    const resources = performance.getEntriesByType("resource");
    const blocking = resources.filter((r) => {
      const u = r.name;
      return (
        u.includes("leaflet.js") ||
        u.includes("composeApp.js") ||
        u.includes("fonts.googleapis") ||
        u.includes("mc.yandex") ||
        u.includes("jivo") ||
        u.includes("callibri")
      );
    });
    return {
      ttfb: nav?.responseStart ?? null,
      domContentLoaded: nav?.domContentLoadedEventEnd ?? null,
      load: nav?.loadEventEnd ?? null,
      resourceCount: resources.length,
      earlyThirdParty: blocking.filter((r) => r.startTime < (nav?.domContentLoadedEventEnd ?? Infinity)).length,
      hasLeafletSync: !!document.querySelector('script[src*="leaflet/leaflet.js"]:not([async])'),
      composeDefer: document.querySelector('script[src*="composeApp.js"]')?.defer ?? false,
      hasGoogleFonts: !!document.querySelector('link[href*="fonts.googleapis"]'),
    };
  });

  return { url, fcp, ...data, errors: errors.slice(0, 3) };
}

async function main() {
  const { base, outDir } = parseArgs(process.argv);
  fs.mkdirSync(outDir, { recursive: true });

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ ...devices["iPhone 12"], locale: "ru-RU" });
  const page = await context.newPage();
  const cdp = await context.newCDPSession(page);
  await cdp.send("Network.enable");
  await cdp.send("Network.emulateNetworkConditions", {
    offline: false,
    downloadThroughput: (1.6 * 1024 * 1024) / 8,
    uploadThroughput: (750 * 1024) / 8,
    latency: 150,
  });

  const paths = ["/moskva", "/"];
  const results = [];
  for (const p of paths) {
    console.log(`Measuring ${p}...`);
    results.push(await measurePage(page, base + p));
  }
  await browser.close();

  const payload = { base, generatedAt: new Date().toISOString(), results };
  fs.writeFileSync(path.join(outDir, "after.json"), JSON.stringify(payload, null, 2));

  const baselinePath = path.join(root, ".perf/moskva.json");
  let baseline = null;
  if (fs.existsSync(baselinePath)) {
    baseline = JSON.parse(fs.readFileSync(baselinePath, "utf8"));
  }

  const lines = [
    "# Tier-1 perf measurement",
    "",
    `Generated: ${payload.generatedAt}`,
    `Base: ${base}`,
    "",
    "## After (local Slow 4G)",
    "",
    "| Page | FCP | TTFB | DCL | Load | Early 3rd-party reqs |",
    "|------|-----|------|-----|------|---------------------|",
  ];
  for (const r of results) {
    lines.push(
      `| ${r.url.replace(base, "") || "/"} | ${r.fcp != null ? Math.round(r.fcp) + " ms" : "—"} | ${Math.round(r.ttfb)} ms | ${Math.round(r.domContentLoaded)} ms | ${Math.round(r.load)} ms | ${r.earlyThirdParty} |`,
    );
  }

  if (baseline?.audits) {
    const b = baseline.audits;
    lines.push("", "## Production baseline (2026-05-30, /moskva)", "");
    lines.push(`- Perf: ${baseline.categories?.performance?.score != null ? Math.round(baseline.categories.performance.score * 100) : "?"}`);
    lines.push(`- FCP: ${b["first-contentful-paint"]?.displayValue ?? "?"}`);
    lines.push(`- LCP: ${b["largest-contentful-paint"]?.displayValue ?? "?"}`);
    lines.push(`- TBT: ${b["total-blocking-time"]?.displayValue ?? "?"}`);
  }

  lines.push("", "## HTML checks (/moskva)", "");
  const m = results.find((r) => r.url.endsWith("/moskva"));
  if (m) {
    lines.push(`- composeApp defer: ${m.composeDefer}`);
    lines.push(`- Google Fonts in DOM: ${m.hasGoogleFonts}`);
    lines.push(`- Sync leaflet in DOM: ${m.hasLeafletSync}`);
  }

  const lhPath = path.join(outDir, "moskva-local-after.json");
  if (fs.existsSync(lhPath)) {
    const lh = JSON.parse(fs.readFileSync(lhPath, "utf8"));
    const a = lh.audits;
    lines.push("", "## Lighthouse local /moskva (after Tier-1)", "");
    lines.push(`- Perf: ${Math.round(lh.categories.performance.score * 100)}`);
    lines.push(`- FCP: ${a["first-contentful-paint"].displayValue}`);
    lines.push(`- LCP: ${a["largest-contentful-paint"].displayValue}`);
    lines.push(`- TBT: ${a["total-blocking-time"].displayValue}`);
    lines.push(`- Speed Index: ${a["speed-index"].displayValue}`);
    lines.push(`- CLS: ${a["cumulative-layout-shift"].displayValue}`);
  }

  const report = lines.join("\n");
  fs.writeFileSync(path.join(outDir, "report.md"), report);
  console.log(report);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
