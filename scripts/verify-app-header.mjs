#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const RESOURCES = path.join(ROOT, "composeApp", "src", "jsMain", "resources");
const MARKER = "<!-- drivebit-app-header-start -->";
const REQUIRED = [
    MARKER,
    "drivebit-header.css",
    "drivebit-header.js",
    'id="drivebit-app-header"',
    '<div id="root"></div>',
];

function collectHtmlFiles() {
    const files = [
        path.join(ROOT, "index.html"),
        path.join(ROOT, "searchApp", "src", "jsMain", "resources", "search", "index.html"),
        path.join(ROOT, "carDetailApp", "src", "jsMain", "resources", "car-detail", "index.html"),
        path.join(ROOT, "carDetailApp", "src", "jsMain", "resources", "car-photos-gallery", "index.html"),
    ];
    for (const dir of ["moskva", "search"]) {
        const base = path.join(RESOURCES, dir);
        if (!fs.existsSync(base)) continue;
        const walk = (p) => {
            for (const ent of fs.readdirSync(p, { withFileTypes: true })) {
                const full = path.join(p, ent.name);
                if (ent.isDirectory()) walk(full);
                else if (ent.name === "index.html") files.push(full);
            }
        };
        walk(base);
    }
    return files;
}

let failed = 0;
for (const file of collectHtmlFiles()) {
    const html = fs.readFileSync(file, "utf8");
    if (!html.includes('<div id="root"></div>')) continue;
    const rel = path.relative(ROOT, file);
    for (const needle of REQUIRED) {
        if (!html.includes(needle)) {
            console.error(`FAIL ${rel}: missing ${needle}`);
            failed++;
        }
    }
    const headerIdx = html.indexOf(MARKER);
    const rootIdx = html.indexOf('<div id="root"></div>');
    if (headerIdx >= 0 && rootIdx >= 0 && headerIdx > rootIdx) {
        console.error(`FAIL ${rel}: header must appear before #root`);
        failed++;
    }
}

for (const asset of ["drivebit-header.css", "drivebit-header.js"]) {
    if (!fs.existsSync(path.join(ROOT, "vendor", asset))) {
        console.error(`FAIL vendor/${asset} not found`);
        failed++;
    }
}

if (failed > 0) process.exit(1);
console.log("OK app header present in HTML shells");
