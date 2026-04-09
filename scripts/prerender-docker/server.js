#!/usr/bin/env node

const prerender = require("prerender");

const extraFlags = (process.env.CHROME_EXTRA_FLAGS ||
  "--no-sandbox,--disable-dev-shm-usage,--disable-setuid-sandbox,--disable-gpu,--window-size=1920,1080")
  .split(",")
  .map((s) => s.trim())
  .filter(Boolean);

const server = prerender({
  port: process.env.PORT ? Number(process.env.PORT) : 3000,
  chromeLocation: process.env.CHROME_LOCATION || "/usr/bin/chromium",
  extraChromeFlags: extraFlags,
});

server.use(prerender.sendPrerenderHeader());
server.use(prerender.browserForceRestart());
server.use(prerender.addMetaTags());
server.use(prerender.removeScriptTags());
server.use(prerender.httpHeaders());

server.start();
