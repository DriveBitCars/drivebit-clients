#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const DEFER_SCRIPT =
    '<script src="/vendor/drivebit-third-party-deferred.js" defer></script>';
const MODULE_SCRIPT =
    '<script type="module" src="/vendor/drivebit-third-party-deferred.js"></script>';

function walk(dir, out = []) {
    for (const name of fs.readdirSync(dir)) {
        const full = path.join(dir, name);
        const st = fs.statSync(full);
        if (st.isDirectory()) walk(full, out);
        else if (name === "index.html" || name.endsWith(".html")) out.push(full);
    }
    return out;
}

function injectLoader(html) {
    let next = html;
    if (next.includes(MODULE_SCRIPT)) {
        next = next.replaceAll(MODULE_SCRIPT, DEFER_SCRIPT);
    }
    if (next.includes(DEFER_SCRIPT)) {
        return next;
    }
    if (next.includes("</head>")) {
        return next.replace("</head>", `    ${DEFER_SCRIPT}\n</head>`);
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
for (const file of files) {
    let html = fs.readFileSync(file, "utf8");
    if (!/drivebit-third-party-deferred|composeApp\.js/.test(html) && !extraFiles.includes(file)) {
        continue;
    }
    if (!/drivebit-third-party-deferred|composeApp\.js/.test(html) && extraFiles.includes(file)) {
        // still try shells
    }
    const before = html;
    if (!/drivebit-third-party-deferred/.test(html) && !/composeApp\.js/.test(html)) continue;
    html = injectLoader(html);
    if (html !== before) {
        fs.writeFileSync(file, html);
        updated++;
        console.log("updated:", path.relative(root, file));
    }
}

console.log(`done: ${updated} updated`);
