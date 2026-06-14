#!/usr/bin/env node
/**
 * Browser check: stale tab reads fresh refresh token from localStorage (multi-tab fix).
 *
 * Uses Playwright route mocks so we don't need real credentials.
 */
import { chromium } from "playwright";

const baseUrl = (process.argv[2] || "http://127.0.0.1:8080/").replace(/\/?$/, "/");

const MEMORY_REFRESH = "memory-stale-refresh-token";
const STORAGE_REFRESH = "storage-fresh-refresh-token";
const INITIAL_ACCESS = "initial-valid-access-token";

async function waitForServer(url, timeoutMs = 180000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    try {
      const res = await fetch(url, { signal: AbortSignal.timeout(3000) });
      if (res.ok || res.status === 404) {
        return;
      }
    } catch {
      /* retry */
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`Dev server not ready at ${url}`);
}

await waitForServer(baseUrl);

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext();
const staleTab = await context.newPage();
const otherTab = await context.newPage();

let chatCalls = 0;
let trackingRefresh = false;
const refreshTokensSeen = [];
const crossTabEvents = [];

await staleTab.exposeFunction("__recordAuthEvent", () => crossTabEvents.push(Date.now()));
await staleTab.evaluate(() => {
  window.addEventListener("drivebit-auth-changed", () => window.__recordAuthEvent());
});

const mockApi = async (route) => {
  const url = route.request().url();
  if (url.includes("/api/Auth/create-tokens")) {
    const body = JSON.parse(route.request().postData() || "{}");
    if (trackingRefresh) {
      refreshTokensSeen.push(body.refreshToken);
    }
    if (body.refreshToken === STORAGE_REFRESH || body.refreshToken === "recovered-refresh-token") {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({
          accessToken: { token: "recovered-access-token", expiresAt: "2099-01-01T00:00:00Z" },
          refreshToken: {
            token: "recovered-refresh-token",
            userId: "00000000-0000-0000-0000-000000000001",
            expiresAt: "2099-01-01T00:00:00Z",
            createdAt: "2099-01-01T00:00:00Z",
          },
        }),
      });
      return;
    }
    await route.fulfill({
      status: 401,
      contentType: "application/json",
      body: JSON.stringify({ error: "Invalid refresh token" }),
    });
    return;
  }

  if (url.includes("/api/Chat")) {
    chatCalls += 1;
    if (!trackingRefresh) {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: "[]",
      });
      return;
    }
    await route.fulfill({
      status: 401,
      contentType: "application/json",
      body: '"Unauthorized"',
    });
    return;
  }

  await route.continue();
};

await staleTab.route("**/api/**", mockApi);
await otherTab.route("**/api/**", mockApi);

await staleTab.addInitScript(
  ({ access, refresh }) => {
    localStorage.setItem("auth_token", access);
    localStorage.setItem("refresh_token", refresh);
  },
  { access: INITIAL_ACCESS, refresh: MEMORY_REFRESH },
);

await staleTab.goto(`${baseUrl}chats/`, { waitUntil: "networkidle", timeout: 180000 });
await staleTab.waitForFunction(
  () => (document.body?.innerText || "").includes("Входящие"),
  { timeout: 120000 },
);

await otherTab.goto(`${baseUrl}`, { waitUntil: "domcontentloaded", timeout: 60000 });
await otherTab.evaluate(
  ({ access, refresh }) => {
    localStorage.setItem("auth_token", access);
    localStorage.setItem("refresh_token", refresh);
  },
  { access: "other-tab-access", refresh: STORAGE_REFRESH },
);
await staleTab.waitForTimeout(1000);

trackingRefresh = true;
await staleTab.evaluate(() => {
  localStorage.setItem("auth_token", "expired-access-token-for-401");
});

console.log("Waiting for chats poll to trigger stale-tab refresh...");
await staleTab.waitForTimeout(14000);

const ui = await staleTab.evaluate(() => {
  const text = document.body?.innerText || "";
  return {
    hasLoadChatsError: /не удалось загрузить чаты/i.test(text),
    hasUnauthorized: /\bUnauthorized\b/i.test(text),
    hasIncomingHeader: text.includes("Входящие"),
    snippet: text.slice(0, 400),
  };
});

const report = {
  baseUrl,
  crossTabAuthEvents: crossTabEvents.length,
  chatCallsAfterCorrupt: Math.max(0, chatCalls - 1),
  refreshTokensAfterCorrupt: refreshTokensSeen,
  usedStorageRefresh: refreshTokensSeen.includes(STORAGE_REFRESH),
  usedMemoryRefresh: refreshTokensSeen.includes(MEMORY_REFRESH),
  ui,
};

console.log(JSON.stringify(report, null, 2));

const ok =
  refreshTokensSeen.length > 0 &&
  report.usedStorageRefresh &&
  !report.usedMemoryRefresh &&
  !ui.hasLoadChatsError;

console.log(
  ok
    ? "OK: after cross-tab update stale tab refreshed using storage token."
    : "FAIL: stale tab did not recover correctly (see JSON above).",
);

await browser.close();
process.exit(ok ? 0 : 1);
