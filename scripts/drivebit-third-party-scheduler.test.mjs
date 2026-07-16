import assert from "node:assert/strict";
import test from "node:test";
import { bootThirdPartyScripts } from "../vendor/drivebit-third-party-scheduler.mjs";

const METRIKA_URL = "https://mc.yandex.ru/metrika/tag.js?id=105947907";
const CALLIBRI_URL = "https://cdn.callibri.ru/callibri.js";
const JIVO_URL = "//code.jivo.ru/widget/MWoBzLXYYF";

function createMockEnv(options = {}) {
    const scripts = [];
    const loadListeners = [];
    const headChildren = [];
    const bodyChildren = [];
    let idleCallback = null;
    let setTimeoutCalls = [];

    const head = {
        appendChild(node) {
            headChildren.push(node);
            scripts.push(node);
        },
        insertBefore(node, ref) {
            headChildren.push(node);
            scripts.push(node);
        },
    };

    const body = {
        appendChild(node) {
            bodyChildren.push(node);
            scripts.push(node);
        },
    };

    const document = {
        readyState: options.readyState ?? "interactive",
        scripts,
        head,
        body,
        querySelector(selector) {
            if (selector === "script[data-drivebit-callibri]") {
                return scripts.find((s) => s.getAttribute?.("data-drivebit-callibri") === "1") ?? null;
            }
            if (selector === 'script[src*="code.jivo.ru"]') {
                return scripts.find((s) => s.src?.includes("code.jivo.ru")) ?? null;
            }
            return null;
        },
        createElement(tag) {
            if (tag !== "script") {
                throw new Error(`Unexpected tag: ${tag}`);
            }
            const attrs = {};
            const node = {
                tagName: "SCRIPT",
                src: "",
                type: "",
                charset: "",
                async: false,
                defer: false,
                onload: null,
                onerror: null,
                parentNode: null,
                setAttribute(name, value) {
                    attrs[name] = value;
                },
                getAttribute(name) {
                    return attrs[name] ?? null;
                },
                remove() {
                    const idx = scripts.indexOf(node);
                    if (idx >= 0) scripts.splice(idx, 1);
                },
            };
            return node;
        },
        getElementsByTagName(tag) {
            if (tag === "script") {
                return scripts.length > 0 ? [scripts[0]] : [{ parentNode: head }];
            }
            return [];
        },
    };

    const window = {
        location: { hostname: options.hostname ?? "drivebit.ru" },
        ym: undefined,
        addEventListener(type, fn, opts) {
            if (type === "load") {
                loadListeners.push(fn);
            }
        },
        requestIdleCallback(fn, opts) {
            idleCallback = { fn, opts };
        },
        setTimeout(fn, delay) {
            setTimeoutCalls.push({ fn, delay });
        },
    };

    return {
        document,
        window,
        loadListeners,
        get idleCallback() {
            return idleCallback;
        },
        get setTimeoutCalls() {
            return setTimeoutCalls;
        },
        fireLoad() {
            for (const fn of loadListeners) fn();
        },
        metrikaScript() {
            return scripts.find((s) => s.src?.includes("metrika/tag.js"));
        },
        callibriScript() {
            return scripts.find((s) => s.getAttribute?.("data-drivebit-callibri") === "1");
        },
        jivoScript() {
            return scripts.find((s) => s.src?.includes("code.jivo.ru"));
        },
    };
}

test("bootThirdPartyScripts injects Metrika immediately without idle/setTimeout activation", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.ok(env.metrikaScript(), "Metrika script should be injected synchronously on boot");
    assert.equal(env.idleCallback, null, "Must not use requestIdleCallback for Metrika activation");
    assert.equal(
        env.setTimeoutCalls.filter((c) => c.delay <= 100).length,
        0,
        "Must not use short setTimeout for Metrika activation",
    );
});

test("loadCallibri creates script with defer, not async", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    const metrika = env.metrikaScript();
    assert.ok(metrika, "Metrika must be injected first");
    metrika.onload?.();

    const callibri = env.callibriScript();
    assert.ok(callibri, "Callibri script should be injected after Metrika onload");
    assert.equal(callibri.defer, true);
    assert.notEqual(callibri.async, true);
});

test("Callibri does not start before Metrika tag script is inserted and loaded", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.ok(env.metrikaScript(), "Metrika tag must exist before Callibri");
    assert.equal(env.callibriScript(), undefined, "Callibri must wait for Metrika onload");

    env.metrikaScript().onload?.();
    assert.ok(env.callibriScript(), "Callibri starts only after Metrika onload");
});

test("Jivo is scheduled on window.load, not before", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.equal(env.jivoScript(), undefined, "Jivo must not load before window.load");
    assert.equal(env.loadListeners.length, 1, "Should register window.load listener for Jivo");

    env.fireLoad();
    assert.ok(env.jivoScript(), "Jivo loads after window.load");
});

test("bootThirdPartyScripts is idempotent for Metrika and Callibri", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);
    env.metrikaScript().onload?.();

    const metrikaCount = env.document.scripts.filter((s) => s.src?.includes("metrika/tag.js")).length;
    const callibriCount = env.document.scripts.filter(
        (s) => s.getAttribute?.("data-drivebit-callibri") === "1",
    ).length;

    bootThirdPartyScripts(env);

    assert.equal(
        env.document.scripts.filter((s) => s.src?.includes("metrika/tag.js")).length,
        metrikaCount,
    );
    assert.equal(
        env.document.scripts.filter((s) => s.getAttribute?.("data-drivebit-callibri") === "1").length,
        callibriCount,
    );
});
