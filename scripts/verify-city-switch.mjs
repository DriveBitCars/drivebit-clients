#!/usr/bin/env node
import { chromium } from "playwright";

const baseUrl = process.argv[2] || "http://127.0.0.1:8081";

function carListRequests(urls) {
  return urls.filter((u) => /Car\/list\/filtered\/\d+/.test(u));
}

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
const requests = [];
const errors = [];
page.on("request", (req) => {
  if (req.url().includes("/api/")) requests.push(req.url());
});
page.on("pageerror", (e) => errors.push(e.message));

await page.goto(`${baseUrl}/ufa`, { waitUntil: "networkidle", timeout: 120000 });
await page.waitForTimeout(6000);

const ufaState = await page.evaluate(() => ({
  path: window.location.pathname,
  bodyClass: document.body.className,
  text: document.body.innerText,
  rootKids: document.getElementById("root")?.childElementCount ?? 0,
}));
const ufaCars = carListRequests(requests);
const ufaHasGeely = ufaState.text.includes("Geely");

await page.getByRole("link", { name: "Уфа" }).click({ timeout: 15000 }).catch(async () => {
  await page.locator("text=Уфа").first().click({ timeout: 15000 });
});
await page.waitForURL(/my-city-selection/, { timeout: 15000 });
await page.waitForTimeout(2000);

const requestsBeforeMoscow = requests.length;
await page.getByText("Москва", { exact: true }).first().click({ timeout: 15000 });
await page.waitForURL(/\/moskva/, { timeout: 15000 });
await page.waitForTimeout(6000);

const moscowState = await page.evaluate(() => ({
  path: window.location.pathname,
  text: document.body.innerText,
}));
const moscowCarsAfterSwitch = carListRequests(requests.slice(requestsBeforeMoscow));

const moscowHasCars =
  (await page.locator('img[src*="/publicbct/"]').count()) > 0 ||
  /\d+₽/.test(moscowState.text);

const ok =
  ufaHasGeely &&
  ufaCars.some((u) => u.includes("158836")) &&
  moscowState.path === "/moskva" &&
  moscowCarsAfterSwitch.some((u) => u.includes("158835")) &&
  moscowHasCars;

console.log(
  JSON.stringify(
    {
      ok,
      ufa: { hasGeely: ufaHasGeely, carRequests: ufaCars, composeReady: ufaState.rootKids > 0, rootKids: ufaState.rootKids },
      moscow: { path: moscowState.path, carRequestsAfterSwitch: moscowCarsAfterSwitch, hasCars: moscowHasCars },
      errors: errors.slice(0, 5),
    },
    null,
    2,
  ),
);

await browser.close();
process.exit(ok ? 0 : 1);
