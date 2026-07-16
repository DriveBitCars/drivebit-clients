#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const MODULE_SCRIPT =
    '    <script type="module" src="/vendor/drivebit-third-party-deferred.js"></script>';
const LEGACY_DEFER_SCRIPT =
    '<script src="/vendor/drivebit-third-party-deferred.js" defer></script>';

function walk(dir, out = []) {
    for (const name of fs.readdirSync(dir)) {
        const full = path.join(dir, name);
        const st = fs.statSync(full);
        if (st.isDirectory()) walk(full, out);
        else if (name === "index.html") out.push(full);
    }
    return out;
}

function hasComposeApp(html) {
    return /composeApp\.js/.test(html);
}

function hasThirdPartyLoader(html) {
    return /drivebit-third-party-deferred/.test(html);
}

function injectLoader(html) {
    if (html.includes(LEGACY_DEFER_SCRIPT)) {
        return html.replace(LEGACY_DEFER_SCRIPT, MODULE_SCRIPT.trim());
    }
    if (hasThirdPartyLoader(html)) {
        return html;
    }
    if (html.includes("</head>")) {
        return html.replace("</head>", `${MODULE_SCRIPT}\n</head>`);
    }
    throw new Error("no </head> in HTML");
}

const resources = path.join(root, "composeApp/src/jsMain/resources");
const extraFiles = [
    path.join(root, "index.html"),
    path.join(root, "list-your-car.html"),
    path.join(root, "authApp/src/jsMain/resources/auth-app-shell/index.html"),
    path.join(root, "carDetailApp/src/jsMain/resources/car-detail/index.html"),
];
const files = [...walk(resources), ...extraFiles].filter((f) => fs.existsSync(f));

let updated = 0;
let skipped = 0;
let alreadyPresent = 0;

for (const file of files) {
    let html = fs.readFileSync(file, "utf8");
    if (!hasComposeApp(html) && !hasThirdPartyLoader(html)) {
        if (file.endsWith("index.html") && extraFiles.includes(file)) {
            // auth/car-detail shells may not have composeApp.js in head
        } else if (!extraFiles.includes(file)) {
            continue;
        }
    }
    const before = html;
    html = injectLoader(html);
    if (html === before) {
        if (hasThirdPartyLoader(before)) alreadyPresent++;
        continue;
    }
    fs.writeFileSync(file, html);
    updated++;
    console.log("updated:", path.relative(root, file));
}

console.log(`done: ${updated} updated, ${alreadyPresent} unchanged, ${skipped} skipped`);
