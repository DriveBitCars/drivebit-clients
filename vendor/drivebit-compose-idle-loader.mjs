const DEFAULT_SRC = "/composeApp.js?v=4";
const FALLBACK_MS = 2000;
const INTERACTION_EVENTS = ["pointerdown", "keydown", "scroll", "touchstart"];

export function resolveComposeSrc(env) {
    const script = env.currentScript;
    if (script && typeof script.getAttribute === "function") {
        const fromAttr = script.getAttribute("data-src");
        if (fromAttr) return fromAttr;
    }
    return env.defaultSrc || DEFAULT_SRC;
}

export function isComposeAlreadyRequested(document, src) {
    const scripts = document.scripts || [];
    for (let i = 0; i < scripts.length; i++) {
        const s = scripts[i];
        if (!s.src) continue;
        if (s.src.indexOf("composeApp.js") !== -1) return true;
        if (src && s.src.indexOf(src.split("?")[0]) !== -1 && s.getAttribute?.("data-drivebit-compose") === "1") {
            return true;
        }
    }
    return !!document.querySelector?.('script[data-drivebit-compose="1"]');
}

export function loadCompose(env) {
    const { document } = env;
    const src = resolveComposeSrc(env);
    if (isComposeAlreadyRequested(document, src)) {
        return false;
    }
    const script = document.createElement("script");
    script.src = src;
    script.type = "application/javascript";
    script.defer = true;
    script.setAttribute("data-drivebit-compose", "1");
    document.head.appendChild(script);
    return true;
}

export function scheduleComposeIdleLoad(env) {
    const { window } = env;
    if (env._composeScheduled) return;
    env._composeScheduled = true;

    let started = false;
    function start() {
        if (started) return;
        started = true;
        cleanup();
        loadCompose(env);
    }

    function onInteraction() {
        start();
    }

    function cleanup() {
        for (let i = 0; i < INTERACTION_EVENTS.length; i++) {
            window.removeEventListener(INTERACTION_EVENTS[i], onInteraction);
        }
        if (env._composeFallbackId != null && typeof window.clearTimeout === "function") {
            window.clearTimeout(env._composeFallbackId);
        }
    }

    for (let i = 0; i < INTERACTION_EVENTS.length; i++) {
        window.addEventListener(INTERACTION_EVENTS[i], onInteraction, { once: true, passive: true });
    }

    if (typeof window.requestIdleCallback === "function") {
        window.requestIdleCallback(function () {
            start();
        }, { timeout: FALLBACK_MS });
    }

    env._composeFallbackId = window.setTimeout(function () {
        start();
    }, FALLBACK_MS);

    window.drivebitLoadCompose = start;
}

export function bootComposeIdleLoader(env) {
    scheduleComposeIdleLoad(env);
}
