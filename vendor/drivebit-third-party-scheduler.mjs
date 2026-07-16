const METRIKA_TAG_URL = "https://mc.yandex.ru/metrika/tag.js?id=105947907";
const METRIKA_COUNTER_ID = 105947907;
const CALLIBRI_URL = "//cdn.callibri.ru/callibri.js";
const JIVO_URL = "//code.jivo.ru/widget/MWoBzLXYYF";
const LOG_PREFIX = "[DriveBit/Callibri]";

function log(env) {
    const args = Array.prototype.slice.call(arguments, 1);
    args.unshift(LOG_PREFIX);
    const c = env.window && env.window.console;
    if (c && typeof c.log === "function") {
        c.log.apply(c, args);
    }
}

function warn(env) {
    const args = Array.prototype.slice.call(arguments, 1);
    args.unshift(LOG_PREFIX);
    const c = env.window && env.window.console;
    if (c && typeof c.warn === "function") {
        c.warn.apply(c, args);
    }
}

function isMetrikaTagRequested(document) {
    for (let j = 0; j < document.scripts.length; j++) {
        if (document.scripts[j].src && document.scripts[j].src.indexOf("metrika/tag.js") !== -1) {
            return true;
        }
    }
    return false;
}

function findCallibriScript(document) {
    return (
        document.querySelector("script[data-drivebit-callibri]") ||
        document.querySelector('script[src*="callibri.js"]')
    );
}

/**
 * Official Callibri install check from docs: callibriInit() → undefined
 * @see https://callibri.ru/help/ustanovka_skripta_callibri/kak_ustanovit_skript_callibri_napryamuyu_v_kod_sayta
 */
export function verifyCallibriInstalled(env, attempt) {
    const { window } = env;
    const n = attempt || 0;
    if (typeof window.callibriInit === "function") {
        let result;
        try {
            result = window.callibriInit();
        } catch (e) {
            warn(env, "callibriInit() threw:", e && e.message ? e.message : e);
            return false;
        }
        if (result === undefined) {
            log(env, "OK: Callibri установлен (docs: callibriInit() → undefined)");
            return true;
        }
        warn(env, "callibriInit() unexpected result:", result);
        return false;
    }
    if (n < 40) {
        window.setTimeout(function () {
            verifyCallibriInstalled(env, n + 1);
        }, 250);
        return null;
    }
    warn(env, "Callibri НЕ обнаружен: нет callibriInit");
    return false;
}

export function loadMetrika(env, onTagReady) {
    const { document, window } = env;
    if (isMetrikaTagRequested(document)) {
        onTagReady?.();
        return;
    }

    window.ym =
        window.ym ||
        function () {
            (window.ym.a = window.ym.a || []).push(arguments);
        };
    window.ym.l = 1 * new Date();

    let tagScript = null;
    for (let j = 0; j < document.scripts.length; j++) {
        if (document.scripts[j].src === METRIKA_TAG_URL) {
            tagScript = document.scripts[j];
            break;
        }
    }

    if (!tagScript) {
        tagScript = document.createElement("script");
        const firstScript = document.getElementsByTagName("script")[0];
        tagScript.async = true;
        tagScript.src = METRIKA_TAG_URL;
        if (onTagReady) {
            tagScript.onload = function () {
                onTagReady();
            };
        }
        firstScript.parentNode.insertBefore(tagScript, firstScript);
        log(env, "Metrika: start tag.js immediately (no setTimeout/idle delay)");
    } else if (onTagReady) {
        onTagReady();
    }

    window.ym(METRIKA_COUNTER_ID, "init", {
        ssr: true,
        webvisor: true,
        clickmap: true,
        ecommerce: "dataLayer",
        accurateTrackBounce: true,
        trackLinks: true,
    });
}

/** Prefer static docs tag; dynamic insert is fallback only. */
export function loadCallibri(env, attempt) {
    const { document, window } = env;
    const existing = findCallibriScript(document);
    if (existing) {
        log(env, "Callibri static/docs tag found in DOM:", existing.src || "(inline)");
        verifyCallibriInstalled(env, 0);
        return;
    }
    warn(env, "Static Callibri tag missing — fallback dynamic insert");
    const script = document.createElement("script");
    script.src = CALLIBRI_URL;
    script.type = "text/javascript";
    script.charset = "utf-8";
    script.defer = true;
    script.setAttribute("data-drivebit-callibri", "1");
    script.onload = function () {
        verifyCallibriInstalled(env, 0);
    };
    script.onerror = function () {
        script.remove();
        if ((attempt || 0) < 1) {
            window.setTimeout(function () {
                loadCallibri(env, (attempt || 0) + 1);
            }, 3000);
        }
    };
    document.head.appendChild(script);
}

export function loadJivo(env) {
    const { document, window } = env;
    const host = window.location.hostname;
    if (host === "localhost" || host === "127.0.0.1") {
        return;
    }
    if (document.querySelector('script[src*="code.jivo.ru"]')) {
        return;
    }
    const script = document.createElement("script");
    script.src = JIVO_URL;
    script.async = true;
    document.head.appendChild(script);
}

function afterLoad(env, fn) {
    if (env.document.readyState === "complete") {
        fn();
        return;
    }
    env.window.addEventListener("load", fn, { once: true });
}

export function bootThirdPartyScripts(env) {
    log(env, "boot: Metrika first, then verify Callibri (docs snippet before </body>)");
    loadMetrika(env, function () {
        loadCallibri(env, 0);
    });

    afterLoad(env, function () {
        loadJivo(env);
    });
}
