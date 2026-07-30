/**
 * pages-dev: unauth book → login-by-mail OTP → autoBook create once.
 *
 * CRITICAL: pages-dev JS still posts to prod API (drivebit.ru/api/).
 * This script aborts create POSTs so no real booking is written.
 *
 * Pass criterion: exactly one POST to Booking/my/as-renter
 * (GET list shares the same path — do not count GETs).
 *
 * OTP handshake:
 * 1. Script writes OUT/otp-request.json after email submit
 * 2. Agent fetches DriveBit OTP (mail MCP for mail@antonbutov.com)
 * 3. Agent writes OUT/otp.txt (digits only)
 * 4. Script continues
 *
 * Usage:
 *   OUT_DIR=tmp/e2e-book-once-pages-dev node scripts/e2e-pages-dev-book-once.mjs
 *
 * Keep the node process alive while fetching OTP (background shell / nohup).
 */
import { chromium } from "playwright";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, "..");
const BASE = process.env.BASE_URL || "https://dev.drivebit.my";
const EMAIL = process.env.E2E_LOGIN_EMAIL || "mail@antonbutov.com";
const OUT = path.resolve(ROOT, process.env.OUT_DIR || "tmp/e2e-book-once-pages-dev");
const CREATE_PATH = /Booking\/my\/as-renter/i;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

fs.mkdirSync(OUT, { recursive: true });

function futureBookingDates() {
  // ~UTC+3 business hours, start = now+2d @12:00, end = start+2d
  const now = new Date();
  const start = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate() + 2, 9, 0, 0));
  const end = new Date(start.getTime() + 2 * 24 * 60 * 60 * 1000);
  return {
    startAt: start.toISOString().replace(/\.\d{3}Z$/, ".000Z"),
    endAt: end.toISOString().replace(/\.\d{3}Z$/, ".000Z"),
  };
}

async function dismissCookie(page) {
  const btn = page.getByText("Понятно");
  if ((await btn.count()) > 0) {
    try {
      await btn.first().click({ timeout: 1500 });
    } catch {}
  }
}

async function findCarDetailUrl(page) {
  await page.goto(`${BASE}/moskva/search/`, {
    waitUntil: "domcontentloaded",
    timeout: 45000,
  });
  await sleep(4000);
  await dismissCookie(page);
  const hrefs = await page.evaluate(() =>
    Array.from(document.querySelectorAll("a[href]"))
      .map((a) => a.getAttribute("href") || "")
      .filter((h) => h.includes("car-detail")),
  );
  if (!hrefs.length) throw new Error("No car-detail link on search");
  const h = hrefs[0];
  return h.startsWith("http") ? h : new URL(h, BASE).toString();
}

function log(...args) {
  const line = args.map(String).join(" ");
  console.log(line);
  fs.appendFileSync(path.join(OUT, "book-once-out.txt"), line + "\n");
}

const createCalls = [];
const pageErrors = [];
const consoleErrors = [];
const report = {
  email: EMAIL,
  base: BASE,
  startedAt: new Date().toISOString(),
  steps: [],
  createCalls: [],
  postCreateCount: 0,
  getAsRenterCount: 0,
  pass: false,
};

fs.writeFileSync(path.join(OUT, "book-once-out.txt"), "");
fs.rmSync(path.join(OUT, "otp-request.json"), { force: true });
fs.rmSync(path.join(OUT, "otp.txt"), { force: true });
fs.rmSync(path.join(OUT, "book-once-report.json"), { force: true });

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  viewport: { width: 1280, height: 900 },
  locale: "ru-RU",
});
const page = await context.newPage();
page.on("pageerror", (e) => pageErrors.push(String(e?.message || e)));
page.on("console", (msg) => {
  if (msg.type() === "error") consoleErrors.push(msg.text());
});

// Abort real create (prod API). Record all methods; pass uses POST only.
await page.route(CREATE_PATH, async (route) => {
  const req = route.request();
  createCalls.push({
    at: new Date().toISOString(),
    method: req.method(),
    url: req.url(),
    postData: req.postData(),
  });
  log("[BLOCKED]", req.method(), createCalls.filter((c) => c.method === "POST").length, req.url());
  await route.abort("failed");
});

try {
  await page.goto(`${BASE}/moskva/`, { waitUntil: "domcontentloaded", timeout: 45000 });
  await page.evaluate(() => {
    try {
      localStorage.clear();
      sessionStorage.clear();
    } catch {}
  });
  await context.clearCookies();
  report.steps.push("cleared auth");
  log("cleared auth");

  let carUrl = await findCarDetailUrl(page);
  const { startAt, endAt } = futureBookingDates();
  const u = new URL(carUrl);
  u.searchParams.set("startAt", startAt);
  u.searchParams.set("endAt", endAt);
  carUrl = u.toString();
  report.carUrl = carUrl;
  report.steps.push(`car=${carUrl}`);
  log("car", carUrl);

  await page.goto(carUrl, { waitUntil: "domcontentloaded", timeout: 45000 });
  await sleep(6000);
  await dismissCookie(page);
  await sleep(2000);
  await page.screenshot({ path: path.join(OUT, "01-car-unauth.png"), fullPage: false });

  const bookBtn = page.getByText("Забронировать", { exact: true }).first();
  await bookBtn.waitFor({ state: "visible", timeout: 45000 });
  await bookBtn.scrollIntoViewIfNeeded();
  await bookBtn.click({ timeout: 5000 });
  await sleep(3000);
  report.loginUrl = page.url();
  report.steps.push(`afterBook=${page.url()}`);
  log("afterBook", page.url());
  await page.screenshot({ path: path.join(OUT, "02-after-book-click.png") });

  const params = new URL(page.url()).searchParams.toString();
  await page.goto(`${BASE}/login-by-mail${params ? `?${params}` : ""}`, {
    waitUntil: "domcontentloaded",
    timeout: 45000,
  });
  await sleep(2500);
  report.steps.push(`mailLogin=${page.url()}`);
  log("mailLogin", page.url());
  await page.screenshot({ path: path.join(OUT, "02b-login-by-mail.png") });

  const emailInput = page.locator('input:not([type="checkbox"])').first();
  await emailInput.waitFor({ state: "visible", timeout: 20000 });
  await emailInput.click({ clickCount: 3 });
  await page.keyboard.press("Backspace");
  await emailInput.type(EMAIL, { delay: 25 });
  await emailInput.blur();
  await sleep(600);

  const checkbox = page.locator("#terms-consent-checkbox");
  await checkbox.waitFor({ state: "attached", timeout: 10000 });
  if (!(await checkbox.isChecked())) {
    await checkbox.check({ force: true });
  }
  await sleep(800);

  const continueBtn = page.getByText("Продолжить", { exact: true }).first();
  await continueBtn.waitFor({ state: "visible", timeout: 15000 });
  for (let i = 0; i < 40; i++) {
    const bg = await continueBtn.evaluate((el) => getComputedStyle(el).backgroundColor).catch(() => "");
    if (bg && !/128,\s*128,\s*128|170,\s*170,\s*170|209,\s*213,\s*219|rgb\(0,\s*0,\s*0\)/.test(bg)) break;
    await sleep(250);
  }
  await continueBtn.click({ timeout: 5000 });
  report.steps.push("submitted email");
  log("submitted email");
  fs.writeFileSync(
    path.join(OUT, "otp-request.json"),
    JSON.stringify({ email: EMAIL, requestedAt: new Date().toISOString() }, null, 2),
  );

  try {
    await page.waitForURL(/verify-otp/, { timeout: 45000 });
  } catch {
    await page.screenshot({ path: path.join(OUT, "03-otp-page.png") });
    const body = (await page.locator("body").innerText()).slice(0, 500);
    throw new Error(`Expected OTP page, got ${page.url()} body=${body}`);
  }
  await page.screenshot({ path: path.join(OUT, "03-otp-page.png") });
  report.steps.push(`otpPage=${page.url()}`);
  log("otpPage", page.url());

  let otp = null;
  for (let i = 0; i < 120; i++) {
    const otpPath = path.join(OUT, "otp.txt");
    if (fs.existsSync(otpPath)) {
      otp = fs.readFileSync(otpPath, "utf8").trim();
      if (/^\d{4,8}$/.test(otp)) break;
    }
    await sleep(1000);
  }
  if (!otp || !/^\d{4,8}$/.test(otp)) throw new Error("OTP missing in otp.txt");
  report.steps.push("got otp");
  log("got otp len", otp.length);

  const otpInput = page.locator("input").first();
  await otpInput.fill(otp);
  await page.evaluate((code) => {
    const input = document.querySelector("input");
    if (!input) return;
    const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set;
    setter?.call(input, code);
    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.dispatchEvent(new Event("change", { bubbles: true }));
  }, otp);
  await sleep(300);
  await page.getByText("Подтвердить").first().click({ timeout: 5000 });
  report.steps.push("confirmed otp");
  log("confirmed otp");

  for (let i = 0; i < 50; i++) {
    await sleep(1000);
    const posts = createCalls.filter((c) => c.method === "POST");
    if (/car-detail/.test(page.url()) && posts.length >= 1) break;
  }
  // Window for a second autoBook POST
  await sleep(15000);
  report.afterAuthUrl = page.url();
  report.createCalls = createCalls;
  report.postCreateCount = createCalls.filter((c) => c.method === "POST").length;
  report.getAsRenterCount = createCalls.filter((c) => c.method === "GET").length;
  report.pageErrors = pageErrors.slice(0, 20);
  report.consoleErrors = consoleErrors
    .filter((t) => !/Failed to load resource|404|net::ERR_/i.test(t))
    .slice(0, 30);
  await page.screenshot({ path: path.join(OUT, "04-after-autobook.png") });

  report.pass = report.postCreateCount === 1;
  report.verdict =
    report.pass
      ? "POST createAsRenter exactly once (GETs to same path are list, not create)"
      : `Expected 1 POST create, got ${report.postCreateCount}`;
  report.finishedAt = new Date().toISOString();
  fs.writeFileSync(path.join(OUT, "book-once-report.json"), JSON.stringify(report, null, 2));
  log("RESULT", JSON.stringify(report, null, 2));
  if (!report.pass) process.exit(1);
} catch (e) {
  report.error = String(e?.message || e);
  report.createCalls = createCalls;
  report.postCreateCount = createCalls.filter((c) => c.method === "POST").length;
  report.getAsRenterCount = createCalls.filter((c) => c.method === "GET").length;
  report.pageErrors = pageErrors.slice(0, 20);
  report.consoleErrors = consoleErrors.slice(0, 30);
  report.finishedAt = new Date().toISOString();
  fs.writeFileSync(path.join(OUT, "book-once-report.json"), JSON.stringify(report, null, 2));
  try {
    await page.screenshot({ path: path.join(OUT, "FAIL.png") });
  } catch {}
  log("ERROR", report.error);
  log("RESULT", JSON.stringify(report, null, 2));
  process.exit(1);
} finally {
  await browser.close();
}
