import assert from "node:assert/strict";
import test from "node:test";
import {
    bootThirdPartyScripts,
    verifyCallibriInstalled,
} from "../vendor/drivebit-third-party-scheduler.mjs";

function createMockEnv(options = {}) {
    const scripts = [];
    const loadListeners = [];
    const interactionListeners = {};
    const headChildren = [];
    const bodyChildren = [];
    let idleCallback = null;
    let setTimeoutCalls = [];
    let timeoutSeq = 0;

    const head = {
        appendChild(node) {
            node.parentNode = head;
            headChildren.push(node);
            scripts.push(node);
        },
        insertBefore(node, ref) {
            node.parentNode = head;
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
            if (selector === 'script[src*="callibri.js"]') {
                return scripts.find((s) => s.src?.includes("callibri.js")) ?? null;
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
        console: {
            log() {},
            warn() {},
        },
        addEventListener(type, fn, opts) {
            if (type === "load") {
                loadListeners.push(fn);
                return;
            }
            if (!interactionListeners[type]) interactionListeners[type] = [];
            interactionListeners[type].push({ fn, opts });
        },
        removeEventListener(type, fn) {
            const list = interactionListeners[type];
            if (!list) return;
            interactionListeners[type] = list.filter((e) => e.fn !== fn);
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
        loadListeners,
        interactionListeners,
        get idleCallback() {
            return idleCallback;
        },
        get setTimeoutCalls() {
            return setTimeoutCalls;
        },
        fireLoad() {
            for (const fn of loadListeners) fn();
        },
        fireInteraction(type) {
            const list = interactionListeners[type] || [];
            for (const { fn } of list.slice()) fn();
        },
        runIdle() {
            if (idleCallback) idleCallback.fn();
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
        captureYmInit() {
            const orig = window.ym;
            window.ym = function () {
                ymCalls.push(Array.from(arguments));
                if (typeof orig === "function") orig.apply(null, arguments);
            };
        },
    };
}

test("bootThirdPartyScripts injects Metrika immediately without idle/setTimeout activation", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.ok(env.metrikaScript(), "Metrika script should be injected synchronously on boot");
    assert.equal(
        env.setTimeoutCalls.filter((c) => c.delay <= 100).length,
        0,
        "Must not use short setTimeout for Metrika activation",
    );
});

test("Metrika init disables webvisor and keeps clickmap", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.ok(Array.isArray(env.window.ym.a), "ym uses call queue");
    const initArgs = env.window.ym.a.find((a) => a[1] === "init");
    assert.ok(initArgs, "ym init must be queued");
    assert.equal(initArgs[2].webvisor, false);
    assert.equal(initArgs[2].clickmap, true);
});

test("Callibri is not injected immediately after Metrika onload", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.ok(env.metrikaScript(), "Metrika tag must exist before Callibri");
    env.metrikaScript().onload?.();
    assert.equal(env.callibriScript(), undefined, "Callibri must wait for idle or interaction");
});

test("Callibri loads on requestIdleCallback after Metrika is ready", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);
    env.metrikaScript().onload?.();

    assert.ok(env.idleCallback, "Should schedule requestIdleCallback for Callibri");
    env.runIdle();

    const callibri = env.callibriScript();
    assert.ok(callibri, "Callibri script should load on idle");
    assert.equal(callibri.defer, true);
    assert.notEqual(callibri.async, true);
});

test("Callibri loads on first pointerdown before idle", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);
    env.metrikaScript().onload?.();

    assert.equal(env.callibriScript(), undefined);
    env.fireInteraction("pointerdown");
    assert.ok(env.callibriScript(), "Callibri loads on first interaction");
});

test("Callibri loads on fallback timeout after Metrika ready", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);
    env.metrikaScript().onload?.();

    const fallback = env.setTimeoutCalls.find((c) => c.delay >= 3000);
    assert.ok(fallback, "Must schedule fallback timeout >= 3s");
    fallback.fn();
    assert.ok(env.callibriScript(), "Callibri loads on fallback timeout");
});

test("Callibri still schedules when Metrika tag.js fails to load", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);

    assert.equal(env.callibriScript(), undefined);
    assert.equal(typeof env.metrikaScript()?.onerror, "function", "Metrika must have onerror");
    env.metrikaScript().onerror?.();

    assert.ok(env.idleCallback, "Callibri deferral must start after Metrika error");
    env.runIdle();
    assert.ok(env.callibriScript(), "Callibri must load even if Metrika fails");
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
    env.runIdle();

    const metrikaCount = env.document.scripts.filter((s) => s.src?.includes("metrika/tag.js")).length;
    const callibriCount = env.document.scripts.filter(
        (s) => s.getAttribute?.("data-drivebit-callibri") === "1",
    ).length;

    bootThirdPartyScripts(env);
    env.metrikaScript()?.onload?.();
    env.runIdle();

    assert.equal(
        env.document.scripts.filter((s) => s.src?.includes("metrika/tag.js")).length,
        metrikaCount,
    );
    assert.equal(
        env.document.scripts.filter((s) => s.getAttribute?.("data-drivebit-callibri") === "1").length,
        callibriCount,
    );
});

test("verifyCallibriInstalled passes official Callibri check callibriInit → undefined", () => {
    const logs = [];
    const env = createMockEnv();
    env.window.console = {
        log: (...args) => logs.push(args.join(" ")),
        warn: (...args) => logs.push("WARN " + args.join(" ")),
    };
    env.window.callibriInit = () => undefined;

    assert.equal(verifyCallibriInstalled(env, 0), true);
    assert.ok(
        logs.some((line) => line.includes("callibriInit() → undefined")),
        "Must log official Callibri success check",
    );
});

test("verifyCallibriInstalled fails when callibriInit is missing after retries exhausted", () => {
    const logs = [];
    const env = createMockEnv();
    env.window.console = {
        log: (...args) => logs.push(args.join(" ")),
        warn: (...args) => logs.push("WARN " + args.join(" ")),
    };

    assert.equal(verifyCallibriInstalled(env, 40), false);
    assert.ok(logs.some((line) => line.includes("НЕ обнаружен")));
});

test("loadCallibri does not duplicate when script already injected", () => {
    const env = createMockEnv();
    bootThirdPartyScripts(env);
    env.metrikaScript()?.onload?.();
    env.runIdle();

    const callibriCount = env.document.scripts.filter((s) =>
        String(s.src || "").includes("callibri.js"),
    ).length;
    assert.equal(callibriCount, 1);

    env.fireInteraction("pointerdown");
    env.runIdle();
    assert.equal(
        env.document.scripts.filter((s) => String(s.src || "").includes("callibri.js")).length,
        1,
        "Must not inject a second Callibri script",
    );
});
