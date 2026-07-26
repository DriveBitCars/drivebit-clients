#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const DEFER_LOADER =
    '<script src="/vendor/drivebit-third-party-deferred.js" defer></script>';
const MODULE_LOADER =
    '<script type="module" src="/vendor/drivebit-third-party-deferred.js"></script>';

const REQUIRED_FILES = [
    "composeApp/src/jsMain/resources/moskva/index.html",
    "index.html",
    "list-your-car.html",
    "authApp/src/jsMain/resources/auth-app-shell/index.html",
    "carDetailApp/src/jsMain/resources/car-detail/index.html",
];

function walk(dir, out = []) {
    for (const name of fs.readdirSync(dir)) {
        const full = path.join(dir, name);
        const st = fs.statSync(full);
        if (st.isDirectory()) walk(full, out);
        else if (name === "index.html") out.push(full);
    }
    return out;
}

const resources = path.join(root, "composeApp/src/jsMain/resources");
const composePages = walk(resources).filter((file) => {
    const html = fs.readFileSync(file, "utf8");
    return /composeApp\.js|drivebit-third-party-deferred|appCompose\.js/.test(html);
});

const failures = [];

function checkHtml(rel, html) {
    if (html.includes(MODULE_LOADER)) {
        failures.push(`${rel}: still uses type=module`);
    }
    if (!html.includes(DEFER_LOADER)) {
        failures.push(`${rel}: missing defer Metrika loader in head`);
    }
    if (/cdn\.callibri\.ru\/callibri\.js/.test(html)) {
        failures.push(`${rel}: eager Callibri tag must be removed (loader defers it)`);
    }
}

for (const rel of REQUIRED_FILES) {
    checkHtml(rel, fs.readFileSync(path.join(root, rel), "utf8"));
}

for (const file of composePages) {
    checkHtml(path.relative(root, file), fs.readFileSync(file, "utf8"));
}

if (failures.length > 0) {
    console.error("Third-party loader verification failed:");
    for (const failure of failures) {
        console.error(`  - ${failure}`);
    }
    process.exit(1);
}

console.log(
    `OK: Metrika loader present, no eager Callibri on ${composePages.length} composeApp pages + shells`,
);
