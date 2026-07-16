const METRIKA_TAG_URL = "https://mc.yandex.ru/metrika/tag.js?id=105947907";
const METRIKA_COUNTER_ID = 105947907;
const CALLIBRI_URL = "https://cdn.callibri.ru/callibri.js";
const JIVO_URL = "//code.jivo.ru/widget/MWoBzLXYYF";

function isMetrikaTagRequested(document) {
    for (let j = 0; j < document.scripts.length; j++) {
        if (document.scripts[j].src && document.scripts[j].src.indexOf("metrika/tag.js") !== -1) {
            return true;
        }
    }
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

export function loadCallibri(env, attempt) {
    const { document, window } = env;
    if (document.querySelector("script[data-drivebit-callibri]")) {
        return;
    }
    const script = document.createElement("script");
    script.src = CALLIBRI_URL;
    script.type = "text/javascript";
    script.charset = "utf-8";
    script.defer = true;
    script.setAttribute("data-drivebit-callibri", "1");
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
    loadMetrika(env, function () {
        loadCallibri(env, 0);
    });

    afterLoad(env, function () {
        loadJivo(env);
    });
}
