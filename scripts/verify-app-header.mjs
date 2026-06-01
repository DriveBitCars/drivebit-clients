#!/usr/bin/env node
import { chromium } from "playwright";
import { writeFileSync } from "fs";

const localBase = (process.argv[2] || "http://127.0.0.1:8765").replace(/\/$/, "");
const prodBase = "https://drivebit.ru";
const path = process.argv[3] || "/moskva";

async function collectHeader(page) {
  await page.waitForFunction(
    () => {
      const mount = document.getElementById("drivebit-app-header");
      const root = document.getElementById("root");
      const mountHasHeader =
        mount &&
        (mount.querySelector(".app-container") ||
          mount.querySelector(".turo-logo") ||
          mount.innerHTML.length > 200);
      const rootHasHeader =
        root &&
        (root.querySelector(".app-container") ||
          root.querySelector(".turo-logo"));
      return mountHasHeader || rootHasHeader;
    },
    { timeout: 45000 },
  );

  return page.evaluate(() => {
    const containers = [
      ...document.querySelectorAll("#drivebit-app-header .app-container"),
      ...document.querySelectorAll("#root .app-container"),
    ];
    const headerContainers = containers.filter((el) => {
      const row = el.querySelector(".turo-logo")?.closest("div");
      return !!el.querySelector(".turo-logo");
    });
    const container = headerContainers[0] || containers[0];

    const logo = document.querySelector(".turo-logo");
    const cityEl = [...document.querySelectorAll(".app-container span")].find((el) => {
      const t = (el.textContent || "").trim();
      return t === "Москва" || t === "Москве";
    });
    const navTexts = [...document.querySelectorAll(".app-container span")]
      .map((s) => (s.textContent || "").trim())
      .filter((t) => ["Аренда авто", "Сдать авто", "Контакты"].includes(t));
    const burger = document.querySelector('img[alt="Menu"]');
    const avatars = document.querySelectorAll('img[src*="user"], img[src*="avatar"]');

    const box = container?.getBoundingClientRect();
    const logoBox = logo?.getBoundingClientRect();

    const styles = (el) => {
      if (!el) return null;
      const cs = getComputedStyle(el);
      return {
        color: cs.color,
        fontSize: cs.fontSize,
        fontWeight: cs.fontWeight,
        display: cs.display,
      };
    };

    return {
      headerMountCount: document.querySelectorAll("#drivebit-app-header .app-container").length,
      rootHeaderCount: document.querySelectorAll("#root > div .app-container, #root .app-container").length,
      logoCount: document.querySelectorAll(".turo-logo").length,
      hasLogo: !!logo,
      cityText: cityEl?.textContent?.trim() || null,
      cityColor: cityEl ? getComputedStyle(cityEl).color : null,
      navTexts: [...new Set(navTexts)],
      hasBurger: !!burger,
      avatarImgCount: avatars.length,
      containerWidth: box?.width ?? 0,
      logoWidth: logoBox?.width ?? 0,
      cityStyle: styles(cityEl),
      pageTitle: document.title,
      rootContentLen: document.getElementById("root")?.innerHTML?.length ?? 0,
      scripts: [...document.querySelectorAll("script[src]")].map((s) => s.getAttribute("src")),
    };
  });
}

async function probeMenu(page) {
  const burger = page.locator('img[alt="Menu"]').first();
  if ((await burger.count()) === 0) return { menuOpened: false, reason: "no-burger" };
  await burger.click({ timeout: 5000 });
  await page.waitForTimeout(500);
  const menuVisible = await page.locator(".butter-menu-container").isVisible().catch(() => false);
  return { menuOpened: menuVisible };
}

async function screenshotHeader(page, outPath) {
  const el =
    (await page.$("#drivebit-app-header .app-container")) ||
    (await page.$("#root .app-container"));
  if (el) {
    await el.screenshot({ path: outPath });
    return true;
  }
  await page.screenshot({ path: outPath, fullPage: false });
  return false;
}

const browser = await chromium.launch({ headless: true });
const viewports = [
  { name: "desktop", width: 1280, height: 800 },
  { name: "mobile", width: 390, height: 844 },
];

const results = { local: {}, prod: {}, checks: [], errors: [] };

for (const vp of viewports) {
  for (const [label, base] of [
    ["local", localBase],
    ["prod", prodBase],
  ]) {
    const page = await browser.newPage({ viewport: { width: vp.width, height: vp.height } });
    const errors = [];
    page.on("pageerror", (e) => errors.push(e.message));
    const url = `${base}${path}`;
    try {
      await page.goto(url, { waitUntil: "domcontentloaded", timeout: 90000 });
      await page.waitForTimeout(label === "prod" ? 6000 : 8000);
      const header = await collectHeader(page);
      const menu =
        label === "local" && vp.name === "desktop"
          ? await probeMenu(page)
          : { skipped: true };
      await screenshotHeader(page, `/tmp/drivebit-header-${label}-${vp.name}.png`);
      results[label][vp.name] = { url, header, menu, errors };
    } catch (e) {
      results[label][vp.name] = { url, error: String(e), errors };
      results.errors.push(`${label}/${vp.name}: ${e}`);
    } finally {
      await page.close();
    }
  }
}

await browser.close();

function rgbSimilar(a, b) {
  if (!a || !b) return a === b;
  const pa = a.match(/\d+/g)?.map(Number) || [];
  const pb = b.match(/\d+/g)?.map(Number) || [];
  if (pa.length < 3 || pb.length < 3) return a === b;
  return pa.slice(0, 3).every((v, i) => Math.abs(v - pb[i]) <= 8);
}

const checks = [];
const localD = results.local.desktop?.header;
const prodD = results.prod.desktop?.header;
const localM = results.local.mobile?.header;
const prodM = results.prod.mobile?.header;

if (localD && prodD) {
  checks.push({
    name: "logo-present",
    ok: localD.hasLogo && prodD.hasLogo,
    local: localD.hasLogo,
    prod: prodD.hasLogo,
  });
  checks.push({
    name: "nav-links",
    ok:
      ["Аренда авто", "Сдать авто", "Контакты"].every((l) => localD.navTexts.includes(l)) &&
      ["Аренда авто", "Сдать авто", "Контакты"].every((l) => prodD.navTexts.includes(l)),
    local: localD.navTexts,
    prod: prodD.navTexts,
  });
  checks.push({
    name: "city-moscow",
    ok:
      (localD.cityText === "Москва" || localD.cityText === "Москве") &&
      (prodD.cityText === "Москва" || prodD.cityText === "Москве"),
    local: localD.cityText,
    prod: prodD.cityText,
  });
  checks.push({
    name: "compose-root-mounted-local",
    ok: (localD.rootContentLen ?? 0) > 5000,
    local: localD.rootContentLen,
    prod: prodD.rootContentLen,
  });
  checks.push({
    name: "city-color-purple",
    ok: rgbSimilar(localD.cityColor, prodD.cityColor) || localD.cityColor?.includes("107, 70, 193"),
    local: localD.cityColor,
    prod: prodD.cityColor,
  });
  checks.push({
    name: "burger-menu",
    ok: localD.hasBurger && prodD.hasBurger,
    local: localD.hasBurger,
    prod: prodD.hasBurger,
  });
  checks.push({
    name: "single-header-local",
    ok: localD.logoCount <= 2,
    local: { logoCount: localD.logoCount, mount: localD.headerMountCount, root: localD.rootHeaderCount },
  });
  checks.push({
    name: "menu-opens-local",
    ok: results.local.desktop?.menu?.menuOpened === true,
    detail: results.local.desktop?.menu,
  });
}

if (localM && prodM) {
  checks.push({
    name: "mobile-nav-present",
    ok: localM.navTexts.length >= 2 && prodM.navTexts.length >= 2,
    local: localM.navTexts,
    prod: prodM.navTexts,
  });
}

const report = { path, results, checks, allOk: checks.every((c) => c.ok) && results.errors.length === 0 };
writeFileSync("/tmp/verify-app-header-report.json", JSON.stringify(report, null, 2));
console.log(JSON.stringify(report, null, 2));
console.log(report.allOk ? "\nOK: header matches production checks" : "\nFAIL: see checks");
process.exit(report.allOk ? 0 : 1);
