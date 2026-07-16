#!/usr/bin/env node
/**
 * Injects third-party scripts to match Callibri docs:
 * https://callibri.ru/help/ustanovka_skripta_callibri/kak_ustanovit_skript_callibri_napryamuyu_v_kod_sayta
 *
 * - Metrika loader in <head> (defer) — starts immediately, no setTimeout delay
 * - Callibri exact snippet before </body> with defer (visible in View Source)
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const DEFER_SCRIPT =
    '<script src="/vendor/drivebit-third-party-deferred.js" defer></script>';
const MODULE_SCRIPT =
    '<script type="module" src="/vendor/drivebit-third-party-deferred.js"></script>';
/** Exact snippet from Callibri documentation */
const CALLIBRI_SCRIPT =
    '<script src="//cdn.callibri.ru/callibri.js" type="text/javascript" charset="utf-8" defer></script>';

const CALLIBRI_ANY =
    /[ \t]*<script[^>]*cdn\.callibri\.ru\/callibri\.js[^>]*>\s*<\/script>\s*/gi;

function walk(dir, out = []) {
    for (const name of fs.readdirSync(dir)) {
        const full = path.join(dir, name);
        const st = fs.statSync(full);
        if (st.isDirectory()) walk(full, out);
        else if (name === "index.html" || name.endsWith(".html")) out.push(full);
    }
    return out;
}

function injectScripts(html) {
    let next = html;

    if (next.includes(MODULE_SCRIPT)) {
        next = next.replaceAll(MODULE_SCRIPT, DEFER_SCRIPT);
    }

    // Metrika loader must stay in <head> so it runs before body Callibri (defer order)
    if (!next.includes(DEFER_SCRIPT)) {
        if (!next.includes("</head>")) {
            throw new Error("no </head> in HTML");
        }
        next = next.replace("</head>", `    ${DEFER_SCRIPT}\n</head>`);
    }

    // Remove any existing Callibri tags (head or elsewhere) — re-place before </body>
    next = next.replace(CALLIBRI_ANY, "");

    if (!next.includes("</body>")) {
        throw new Error("no </body> in HTML");
    }

    // Docs: add script before closing </body>
    next = next.replace(
        "</body>",
        `    <!-- Callibri: official snippet before </body> -->\n    ${CALLIBRI_SCRIPT}\n</body>`,
    );

    return next;
}

const resources = path.join(root, "composeApp/src/jsMain/resources");
const extraFiles = [
    path.join(root, "index.html"),
    path.join(root, "list-your-car.html"),
    path.join(root, "authApp/src/jsMain/resources/auth-app-shell/index.html"),
    path.join(root, "carDetailApp/src/jsMain/resources/car-detail/index.html"),
];
const files = [...new Set([...walk(resources), ...extraFiles])].filter((f) =>
    fs.existsSync(f),
);

let updated = 0;
for (const file of files) {
    let html = fs.readFileSync(file, "utf8");
    if (!/drivebit-third-party-deferred|composeApp\.js/.test(html) && !extraFiles.includes(file)) {
        continue;
    }
    if (!/drivebit-third-party-deferred/.test(html) && !/composeApp\.js/.test(html)) continue;
    const before = html;
    html = injectScripts(html);
    if (html !== before) {
        fs.writeFileSync(file, html);
        updated++;
        console.log("updated:", path.relative(root, file));
    }
}

console.log(`done: ${updated} updated`);
