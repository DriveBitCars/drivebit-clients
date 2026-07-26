#!/usr/bin/env node
/**
 * Pages-dev e2e for mobile INP changes (desktop + iPhone).
 * Usage: node scripts/e2e-pages-dev-inp.mjs
 */
import { chromium, devices } from "playwright";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const outDir = path.join(root, "tmp/e2e-inp-pages-dev");
fs.mkdirSync(outDir, { recursive: true });

const START = "https://dev.drivebit.my";
const results = [];

async function dismissCookie(page) {
  const btn = page.getByRole("button", { name: /Понятно/i });
  if ((await btn.count()) > 0) {
    await btn.first().click({ timeout: 3000 }).catch(() => {});
  }
}

async function checkPage(browser, label, contextOptions, checks) {
  const context = await browser.newContext({
    ...contextOptions,
    locale: "ru-RU",
    ignoreHTTPSErrors: true,
  });
  const page = await context.newPage();
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));

  const entry = { label, checks: {}, errors, base: null };
  try {
    await page.goto(START + "/moskva", { waitUntil: "domcontentloaded", timeout: 60000 });
    entry.base = page.url();
    await dismissCookie(page);

    for (const check of checks) {
      await check.run(page, entry);
    }
  } catch (e) {
    entry.fatal = e.message;
  }

  await context.close();
  results.push(entry);
  return entry;
}

async function main() {
  const browser = await chromium.launch({ headless: true });

  const desktopChecks = [
    {
      name: "moskva-home",
      async run(page, entry) {
        await page.goto(entry.base.replace(/\/$/, "") + "", { waitUntil: "load", timeout: 60000 }).catch(() => {});
        // already on moskva
        const report = await page.evaluate(() => {
          const scripts = Array.from(document.scripts).map((s) => s.src);
          return {
            hasIdle: scripts.some((s) => /drivebit-compose-idle-loader/.test(s)),
            hasEagerCompose: scripts.some(
              (s) => /composeApp\.js/.test(s) && !/idle-loader/.test(s),
            ),
            hasEagerCallibri: scripts.some((s) => /callibri\.js/.test(s)),
            hasHero: !!document.getElementById("drivebit-hero-static") || !!document.querySelector(".drivebit-hero"),
            hasFilters: document.querySelectorAll("button.drivebit-filter-btn").length,
            webvisorOff: !/webvisor:\s*true/.test(
              Array.from(document.scripts)
                .map((s) => s.textContent || "")
                .join("\n"),
            ),
          };
        });
        // Wait briefly — compose may inject later; initially no eager callibri
        await page.waitForTimeout(500);
        const early = await page.evaluate(() => ({
          callibriEarly: Array.from(document.scripts).some((s) => /callibri\.js/.test(s.src)),
          composeEarly: Array.from(document.scripts).some(
            (s) => /composeApp\.js/.test(s.src) && s.getAttribute("data-drivebit-compose") === "1",
          ),
        }));
        await page.screenshot({ path: path.join(outDir, "desktop-moskva.png"), fullPage: false });
        entry.checks.moskva = { ...report, ...early, ok: report.hasIdle && report.hasHero };
      },
    },
    {
      name: "hero-search",
      async run(page, entry) {
        const btn = page.locator("#drivebit-hero-search-btn").or(page.getByRole("button", { name: /Найти автомобиль/i }));
        if ((await btn.count()) > 0) {
          await btn.first().click();
          await page.waitForURL(/\/[^/]+\/search/, { timeout: 20000 });
        }
        const onSearch = /\/search/.test(page.url());
        await page.waitForTimeout(3000);
        const searchUi = await page.evaluate(() => {
          const root = document.getElementById("root");
          return {
            url: location.pathname,
            hasAppCompose: Array.from(document.scripts).some((s) => /appCompose\.js/.test(s.src)),
            rootLen: root?.innerHTML?.length ?? 0,
            blank: !root || (root.innerHTML.trim().length === 0 && document.body.innerText.trim().length < 40),
          };
        });
        await page.screenshot({ path: path.join(outDir, "desktop-search.png"), fullPage: false });
        entry.checks.search = { onSearch, ...searchUi, ok: onSearch && !searchUi.blank };
      },
    },
    {
      name: "bmw",
      async run(page, entry) {
        await page.goto(new URL("/search/bmw", entry.base).href, { waitUntil: "load", timeout: 60000 });
        await page.waitForTimeout(2000);
        const ok = (await page.locator("h1").count()) > 0;
        await page.screenshot({ path: path.join(outDir, "desktop-bmw.png"), fullPage: false });
        entry.checks.bmw = { ok, title: await page.title() };
      },
    },
    {
      name: "login",
      async run(page, entry) {
        await page.goto(new URL("/login-by-phone", entry.base).href, { waitUntil: "load", timeout: 60000 });
        await page.waitForTimeout(2000);
        const body = await page.locator("body").innerText();
        await page.screenshot({ path: path.join(outDir, "desktop-login.png"), fullPage: false });
        entry.checks.login = { ok: body.length > 20, snippet: body.slice(0, 80) };
      },
    },
    {
      name: "contacts",
      async run(page, entry) {
        await page.goto(new URL("/contacts", entry.base).href, { waitUntil: "load", timeout: 60000 });
        await page.waitForTimeout(1500);
        const ok = (await page.locator("body").innerText()).length > 20;
        await page.screenshot({ path: path.join(outDir, "desktop-contacts.png"), fullPage: false });
        entry.checks.contacts = { ok };
      },
    },
    {
      name: "filter-chip",
      async run(page, entry) {
        await page.goto(new URL("/moskva", entry.base).href, { waitUntil: "load", timeout: 60000 });
        await dismissCookie(page);
        await page.waitForTimeout(300);
        const chips = page.locator("button.drivebit-filter-btn");
        const count = await chips.count();
        let navigated = false;
        if (count > 1) {
          const chip = chips.nth(1);
          await chip.scrollIntoViewIfNeeded();
          await chip.click();
          await page.waitForTimeout(1500);
          navigated = !/\/moskva\/?$/.test(new URL(page.url()).pathname);
        }
        entry.checks.filter = { count, navigated, ok: count > 0 };
      },
    },
  ];

  const mobileChecks = [
    {
      name: "moskva-mobile",
      async run(page, entry) {
        await dismissCookie(page);
        const report = await page.evaluate(() => ({
          hasIdle: Array.from(document.scripts).some((s) =>
            /drivebit-compose-idle-loader/.test(s.src),
          ),
          hasHero: !!document.querySelector("#drivebit-hero-static, .drivebit-hero"),
          filters: document.querySelectorAll("button.drivebit-filter-btn").length,
        }));
        await page.screenshot({ path: path.join(outDir, "mobile-moskva.png"), fullPage: false });
        entry.checks.moskva = { ...report, ok: report.hasIdle && report.hasHero };
      },
    },
    {
      name: "search-mobile",
      async run(page, entry) {
        const btn = page.locator("#drivebit-hero-search-btn").or(page.getByRole("button", { name: /Найти автомобиль/i }));
        if ((await btn.count()) > 0) {
          await btn.first().click();
          await page.waitForURL(/\/[^/]+\/search/, { timeout: 20000 }).catch(() => {});
        }
        await page.waitForTimeout(3000);
        const searchUi = await page.evaluate(() => {
          const root = document.getElementById("root");
          return {
            url: location.href,
            rootLen: root?.innerHTML?.length ?? 0,
            blank: document.body.innerText.trim().length < 40,
          };
        });
        await page.screenshot({ path: path.join(outDir, "mobile-search.png"), fullPage: false });
        entry.checks.search = { ...searchUi, ok: /\/search/.test(searchUi.url) && !searchUi.blank };
      },
    },
    {
      name: "bmw-mobile",
      async run(page, entry) {
        await page.goto(new URL("/search/bmw", entry.base).href, { waitUntil: "load", timeout: 60000 });
        await page.waitForTimeout(2000);
        await page.screenshot({ path: path.join(outDir, "mobile-bmw.png"), fullPage: false });
        entry.checks.bmw = { ok: (await page.locator("h1").count()) > 0 };
      },
    },
    {
      name: "contacts-mobile",
      async run(page, entry) {
        await page.goto(new URL("/contacts", entry.base).href, { waitUntil: "load", timeout: 60000 });
        await page.waitForTimeout(1500);
        await page.screenshot({ path: path.join(outDir, "mobile-contacts.png"), fullPage: false });
        entry.checks.contacts = { ok: (await page.locator("body").innerText()).length > 20 };
      },
    },
  ];

  await checkPage(browser, "desktop", { viewport: { width: 1280, height: 800 } }, desktopChecks);
  await checkPage(browser, "mobile", devices["iPhone 14"], mobileChecks);

  await browser.close();

  const reportPath = path.join(outDir, "report.json");
  fs.writeFileSync(reportPath, JSON.stringify(results, null, 2));
  console.log(JSON.stringify(results, null, 2));

  const failed = results.some(
    (r) =>
      r.fatal ||
      Object.values(r.checks || {}).some((c) => c && c.ok === false),
  );
  console.log(failed ? "FAIL" : "OK", "evidence:", outDir);
  process.exit(failed ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
