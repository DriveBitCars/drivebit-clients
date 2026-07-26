#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const IDLE = "drivebit-compose-idle-loader.js";

const MARKETING_MUST_IDLE = [
    "index.html",
    "composeApp/src/jsMain/resources/moskva/index.html",
    "composeApp/src/jsMain/resources/contacts/index.html",
    "composeApp/src/jsMain/resources/offer/index.html",
];

const APP_MUST_EAGER = [
    "composeApp/src/jsMain/resources/moskva/search/index.html",
    "composeApp/src/jsMain/resources/search/bmw/index.html",
    "searchApp/src/jsMain/resources/search-app-shell/index.html",
];

const failures = [];

for (const rel of MARKETING_MUST_IDLE) {
    const html = fs.readFileSync(path.join(root, rel), "utf8");
    if (!html.includes(IDLE)) {
        failures.push(`${rel}: missing compose idle loader`);
    }
    const withoutDataSrc = html.replace(/\sdata-src="\/composeApp\.js[^"]*"/g, "");
    if (/<script[^>]*src="\/composeApp\.js/.test(withoutDataSrc)) {
        failures.push(`${rel}: still has eager composeApp.js script`);
    }
    if (/rel="preload"[^>]*composeApp\.js/.test(html)) {
        failures.push(`${rel}: still preloads composeApp.js`);
    }
}

for (const rel of APP_MUST_EAGER) {
    const html = fs.readFileSync(path.join(root, rel), "utf8");
    if (!/appCompose\.js/.test(html)) {
        failures.push(`${rel}: expected eager appCompose.js`);
    }
    if (html.includes(IDLE)) {
        failures.push(`${rel}: must not use compose idle loader`);
    }
}

if (!fs.existsSync(path.join(root, "vendor/drivebit-compose-idle-loader.js"))) {
    failures.push("vendor/drivebit-compose-idle-loader.js missing");
}

if (failures.length) {
    console.error("Compose idle loader verification failed:");
    for (const f of failures) console.error("  -", f);
    process.exit(1);
}

console.log("OK: marketing shells idle-load composeApp.js; search shells keep appCompose.js");
