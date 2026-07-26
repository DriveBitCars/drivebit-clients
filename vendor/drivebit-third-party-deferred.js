(function (document, window) {
    var METRIKA_TAG_URL = "https://mc.yandex.ru/metrika/tag.js?id=105947907";
    var METRIKA_COUNTER_ID = 105947907;
    var CALLIBRI_URL = "//cdn.callibri.ru/callibri.js";
    var JIVO_URL = "//code.jivo.ru/widget/MWoBzLXYYF";
    var LOG_PREFIX = "[DriveBit/Callibri]";
    var CALLIBRI_FALLBACK_MS = 4000;
    var INTERACTION_EVENTS = ["pointerdown", "keydown", "scroll", "touchstart"];
    var callibriScheduled = false;
    var callibriFallbackId = null;

    function log() {
        var args = Array.prototype.slice.call(arguments);
        args.unshift(LOG_PREFIX);
        if (window.console && typeof window.console.log === "function") {
            window.console.log.apply(window.console, args);
        }
    }

    function warn() {
        var args = Array.prototype.slice.call(arguments);
        args.unshift(LOG_PREFIX);
        if (window.console && typeof window.console.warn === "function") {
            window.console.warn.apply(window.console, args);
        }
    }

    function isMetrikaTagRequested() {
        for (var j = 0; j < document.scripts.length; j++) {
            if (
                document.scripts[j].src &&
                document.scripts[j].src.indexOf("metrika/tag.js") !== -1
            ) {
                return true;
            }
        }
        return false;
    }

    function findCallibriScript() {
        return (
            document.querySelector("script[data-drivebit-callibri]") ||
            document.querySelector('script[src*="callibri.js"]')
        );
    }

    /**
     * Official Callibri check:
     * https://callibri.ru/help/ustanovka_skripta_callibri/kak_ustanovit_skript_callibri_napryamuyu_v_kod_sayta
     * Console → callibriInit() → undefined means script is installed.
     */
    function verifyCallibriInstalled(attempt) {
        var n = attempt || 0;
        if (typeof window.callibriInit === "function") {
            var result;
            try {
                result = window.callibriInit();
            } catch (e) {
                warn("callibriInit() threw:", e && e.message ? e.message : e);
                return false;
            }
            if (result === undefined) {
                var tag = findCallibriScript();
                log(
                    "OK: Callibri установлен (docs: callibriInit() → undefined)",
                );
                log(
                    "HTML/DOM script:",
                    tag && tag.src ? tag.src : "(не найден)",
                    tag && tag.defer ? "defer=true" : "",
                );
                if (typeof window.ym === "function") {
                    try {
                        window.ym(METRIKA_COUNTER_ID, "getClientID", function (clientId) {
                            log("Yandex Metrika ClientID:", clientId || "(пусто)");
                        });
                    } catch (e) {
                        warn("getClientID failed:", e && e.message ? e.message : e);
                    }
                }
                return true;
            }
            warn("callibriInit() unexpected result:", result);
            return false;
        }
        if (n < 40) {
            window.setTimeout(function () {
                verifyCallibriInstalled(n + 1);
            }, 250);
            return null;
        }
        warn(
            "Callibri НЕ обнаружен: нет callibriInit. Смотрите Network → callibri.js (deferred load)",
        );
        return false;
    }

    function loadMetrika(onTagReady) {
        if (isMetrikaTagRequested()) {
            log("Metrika tag.js already present");
            if (onTagReady) onTagReady();
            return;
        }

        window.ym =
            window.ym ||
            function () {
                (window.ym.a = window.ym.a || []).push(arguments);
            };
        window.ym.l = 1 * new Date();

        var tagScript = null;
        for (var j = 0; j < document.scripts.length; j++) {
            if (document.scripts[j].src === METRIKA_TAG_URL) {
                tagScript = document.scripts[j];
                break;
            }
        }

        if (!tagScript) {
            tagScript = document.createElement("script");
            var firstScript = document.getElementsByTagName("script")[0];
            tagScript.async = true;
            tagScript.src = METRIKA_TAG_URL;
            if (onTagReady) {
                var readyFired = false;
                function fireReady() {
                    if (readyFired) return;
                    readyFired = true;
                    onTagReady();
                }
                tagScript.onload = function () {
                    log("Metrika tag.js loaded");
                    fireReady();
                };
                tagScript.onerror = function () {
                    warn("Metrika tag.js failed; continuing with Callibri deferral");
                    fireReady();
                };
            }
            firstScript.parentNode.insertBefore(tagScript, firstScript);
            log("Metrika: start tag.js immediately (webvisor off for INP)");
        } else if (onTagReady) {
            onTagReady();
        }

        window.ym(METRIKA_COUNTER_ID, "init", {
            ssr: true,
            webvisor: false,
            clickmap: true,
            ecommerce: "dataLayer",
            accurateTrackBounce: true,
            trackLinks: true,
        });
    }

    function ensureCallibri(attempt) {
        var existing = findCallibriScript();
        if (existing) {
            log(
                "Callibri script already in DOM:",
                existing.src || "(inline)",
                existing.defer ? "defer=true" : "",
            );
            verifyCallibriInstalled(0);
            return;
        }
        log("Callibri: dynamic insert (idle/interaction deferred)");
        var script = document.createElement("script");
        script.src = CALLIBRI_URL;
        script.type = "text/javascript";
        script.charset = "utf-8";
        script.defer = true;
        script.setAttribute("data-drivebit-callibri", "1");
        script.onload = function () {
            log("Callibri loaded");
            verifyCallibriInstalled(0);
        };
        script.onerror = function () {
            script.remove();
            warn("callibri.js load error, attempt=", attempt || 0);
            if ((attempt || 0) < 1) {
                window.setTimeout(function () {
                    ensureCallibri((attempt || 0) + 1);
                }, 3000);
            }
        };
        document.head.appendChild(script);
    }

    function scheduleDeferredCallibri() {
        if (callibriScheduled) return;
        callibriScheduled = true;

        var started = false;
        function start() {
            if (started) return;
            started = true;
            cleanup();
            ensureCallibri(0);
        }

        function onInteraction() {
            start();
        }

        function cleanup() {
            for (var i = 0; i < INTERACTION_EVENTS.length; i++) {
                window.removeEventListener(INTERACTION_EVENTS[i], onInteraction);
            }
            if (callibriFallbackId != null) {
                window.clearTimeout(callibriFallbackId);
                callibriFallbackId = null;
            }
        }

        for (var i = 0; i < INTERACTION_EVENTS.length; i++) {
            window.addEventListener(INTERACTION_EVENTS[i], onInteraction, {
                once: true,
                passive: true,
            });
        }

        if (typeof window.requestIdleCallback === "function") {
            window.requestIdleCallback(function () {
                start();
            }, { timeout: CALLIBRI_FALLBACK_MS });
        }

        callibriFallbackId = window.setTimeout(function () {
            start();
        }, CALLIBRI_FALLBACK_MS);
    }

    function loadJivo() {
        var host = window.location.hostname;
        if (host === "localhost" || host === "127.0.0.1") {
            return;
        }
        if (document.querySelector('script[src*="code.jivo.ru"]')) {
            return;
        }
        var script = document.createElement("script");
        script.src = JIVO_URL;
        script.async = true;
        document.head.appendChild(script);
    }

    function afterLoad(fn) {
        if (document.readyState === "complete") {
            fn();
            return;
        }
        window.addEventListener("load", fn, { once: true });
    }

    log("boot: Metrika first (webvisor off), Callibri deferred to idle/interaction");
    loadMetrika(function () {
        scheduleDeferredCallibri();
    });

    afterLoad(function () {
        loadJivo();
    });
})(document, window);
