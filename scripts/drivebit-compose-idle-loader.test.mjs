import assert from "node:assert/strict";
import test from "node:test";
import {
    bootComposeIdleLoader,
    loadCompose,
} from "../vendor/drivebit-compose-idle-loader.mjs";

function createMockEnv(options = {}) {
    const scripts = [];
    const interactionListeners = {};
    let idleCallback = null;
    let setTimeoutCalls = [];
    let timeoutSeq = 0;

    const head = {
        appendChild(node) {
            scripts.push(node);
        },
    };

    const document = {
        scripts,
        head,
        createElement(tag) {
            if (tag !== "script") throw new Error("unexpected " + tag);
            const attrs = {};
            return {
                src: "",
                type: "",
                defer: false,
                setAttribute(name, value) {
                    attrs[name] = value;
                },
                getAttribute(name) {
                    return attrs[name] ?? null;
                },
            };
        },
        querySelector(selector) {
            if (selector === 'script[data-drivebit-compose="1"]') {
                return scripts.find((s) => s.getAttribute?.("data-drivebit-compose") === "1") ?? null;
            }
            return null;
        },
    };

    const window = {
        addEventListener(type, fn) {
            if (!interactionListeners[type]) interactionListeners[type] = [];
            interactionListeners[type].push(fn);
        },
        removeEventListener(type, fn) {
            const list = interactionListeners[type];
            if (!list) return;
            interactionListeners[type] = list.filter((f) => f !== fn);
        },
        requestIdleCallback(fn, opts) {
            idleCallback = { fn, opts };
        },
        setTimeout(fn, delay) {
            const id = ++timeoutSeq;
            setTimeoutCalls.push({ fn, delay, id });
            return id;
        },
        clearTimeout(id) {
            setTimeoutCalls = setTimeoutCalls.filter((c) => c.id !== id);
        },
    };

    return {
        document,
        window,
        currentScript: options.currentScript ?? {
            getAttribute(name) {
                if (name === "data-src") return options.dataSrc ?? "/composeApp.js?v=4";
                return null;
            },
        },
        get idleCallback() {
            return idleCallback;
        },
        get setTimeoutCalls() {
            return setTimeoutCalls;
        },
        fireInteraction(type) {
            for (const fn of (interactionListeners[type] || []).slice()) fn();
        },
        runIdle() {
            if (idleCallback) idleCallback.fn();
        },
        composeScript() {
            return scripts.find((s) => s.getAttribute?.("data-drivebit-compose") === "1");
        },
    };
}

test("does not inject composeApp.js synchronously on boot", () => {
    const env = createMockEnv();
    bootComposeIdleLoader(env);
    assert.equal(env.composeScript(), undefined);
});

test("loads compose on requestIdleCallback", () => {
    const env = createMockEnv();
    bootComposeIdleLoader(env);
    assert.ok(env.idleCallback);
    env.runIdle();
    const script = env.composeScript();
    assert.ok(script);
    assert.equal(script.src, "/composeApp.js?v=4");
    assert.equal(script.defer, true);
});

test("loads compose on first pointerdown", () => {
    const env = createMockEnv();
    bootComposeIdleLoader(env);
    env.fireInteraction("pointerdown");
    assert.ok(env.composeScript());
});

test("loads compose on fallback timeout", () => {
    const env = createMockEnv();
    bootComposeIdleLoader(env);
    const fallback = env.setTimeoutCalls.find((c) => c.delay >= 2000);
    assert.ok(fallback);
    fallback.fn();
    assert.ok(env.composeScript());
});

test("uses data-src from currentScript", () => {
    const env = createMockEnv({ dataSrc: "/composeApp.js?v=99" });
    bootComposeIdleLoader(env);
    env.runIdle();
    assert.equal(env.composeScript().src, "/composeApp.js?v=99");
});

test("loadCompose is idempotent", () => {
    const env = createMockEnv();
    assert.equal(loadCompose(env), true);
    assert.equal(loadCompose(env), false);
    assert.equal(
        env.document.scripts.filter((s) => s.getAttribute?.("data-drivebit-compose") === "1").length,
        1,
    );
});

test("drivebitLoadCompose forces load", () => {
    const env = createMockEnv();
    bootComposeIdleLoader(env);
    assert.equal(env.composeScript(), undefined);
    env.window.drivebitLoadCompose();
    assert.ok(env.composeScript());
});
