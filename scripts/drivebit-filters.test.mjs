import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import vm from "node:vm";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const source = fs.readFileSync(path.join(root, "vendor/drivebit-filters.js"), "utf8");

function button(title, filterPath, pressed = false) {
    const attributes = {
        "data-filter-title": title,
        "data-filter-path": filterPath,
        "aria-pressed": pressed ? "true" : "false",
    };
    const classes = new Set(pressed ? ["is-selected"] : []);
    return {
        getAttribute: (name) => attributes[name] ?? null,
        setAttribute: (name, value) => {
            attributes[name] = value;
        },
        classList: {
            toggle: (name, enabled) => enabled ? classes.add(name) : classes.delete(name),
        },
    };
}

test("selects SEO filter from a non-Moscow deep link", () => {
    const buttons = [
        button("Все", "/moskva", true),
        button("Абхазия", "/moskva/arenda-avto-v-abkhaziyu"),
    ];
    const nav = {
        querySelector: () => null,
        querySelectorAll: (selector) => selector === ".drivebit-filter-btn" ? buttons : [],
        addEventListener() {},
    };
    const context = {
        document: {
            readyState: "interactive",
            getElementById: (id) => id === "drivebit-filters-static" ? nav : null,
        },
        window: {
            location: {
                pathname: "/rostov-na-donu/arenda-avto-v-abkhaziyu",
            },
            addEventListener() {},
        },
    };

    vm.runInNewContext(source, context);

    assert.equal(buttons[0].getAttribute("aria-pressed"), "false");
    assert.equal(buttons[1].getAttribute("aria-pressed"), "true");
});
