#!/usr/bin/env node
/**
 * Replace eager composeApp.js on marketing shells with idle loader.
 * Leaves appCompose.js and other app bundles untouched.
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const IDLE_LOADER =
    '<script src="/vendor/drivebit-compose-idle-loader.js" defer data-src="/composeApp.js?v=4"></script>';

const COMPOSE_SCRIPT_RE =
    /[ \t]*<script[^>]*src="\/composeApp\.js[^"]*"[^>]*>\s*<\/script>/gi;
const COMPOSE_PRELOAD_RE =
    /[ \t]*<link[^>]*rel="preload"[^>]*href="\/composeApp\.js[^"]*"[^>]*>\s*\n?/gi;

function walk(dir, out = []) {
    for (const name of fs.readdirSync(dir)) {
        const full = path.join(dir, name);
        const st = fs.statSync(full);
        if (st.isDirectory()) walk(full, out);
        else if (name === "index.html" || name.endsWith(".html")) out.push(full);
    }
    return out;
}

function transform(html) {
    let next = html;
    next = next.replace(COMPOSE_PRELOAD_RE, "");
    if (!COMPOSE_SCRIPT_RE.test(html)) {
        return html;
    }
    // reset lastIndex after test
    COMPOSE_SCRIPT_RE.lastIndex = 0;
    next = next.replace(COMPOSE_SCRIPT_RE, () => `    ${IDLE_LOADER}`);
    return next;
}

const resources = path.join(root, "composeApp/src/jsMain/resources");
const files = [
    ...walk(resources),
    path.join(root, "index.html"),
    path.join(root, "composeApp/seo/head-common.reference.html"),
].filter((f) => fs.existsSync(f));

let updated = 0;
for (const file of files) {
    const before = fs.readFileSync(file, "utf8");
    if (!/composeApp\.js/.test(before)) continue;
    // Skip if already idle-loaded
    if (before.includes("drivebit-compose-idle-loader.js")) {
        const cleaned = before.replace(COMPOSE_PRELOAD_RE, "").replace(COMPOSE_SCRIPT_RE, "");
        // ensure only idle loader remains — if raw composeApp script still there, transform
        if (/src="\/composeApp\.js/.test(cleaned)) {
            const after = transform(before);
            if (after !== before) {
                fs.writeFileSync(file, after);
                updated++;
                console.log("updated:", path.relative(root, file));
            }
        } else if (cleaned !== before) {
            fs.writeFileSync(file, cleaned);
            updated++;
            console.log("updated:", path.relative(root, file));
        }
        continue;
    }
    const after = transform(before);
    if (after !== before) {
        fs.writeFileSync(file, after);
        updated++;
        console.log("updated:", path.relative(root, file));
    }
}

console.log(`done: ${updated} updated`);
