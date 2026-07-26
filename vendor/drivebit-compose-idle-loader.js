(function (document, window) {
    var DEFAULT_SRC = "/composeApp.js?v=4";
    var FALLBACK_MS = 2000;
    var INTERACTION_EVENTS = ["pointerdown", "keydown", "scroll", "touchstart"];
    var scheduled = false;
    var fallbackId = null;
    var currentScript = document.currentScript;

    function resolveSrc() {
        if (currentScript && currentScript.getAttribute) {
            var fromAttr = currentScript.getAttribute("data-src");
            if (fromAttr) return fromAttr;
        }
        return DEFAULT_SRC;
    }

    function alreadyRequested(src) {
        for (var i = 0; i < document.scripts.length; i++) {
            var s = document.scripts[i];
            if (s.src && s.src.indexOf("composeApp.js") !== -1 && s.getAttribute("data-drivebit-compose") === "1") {
                return true;
            }
        }
        if (document.querySelector('script[data-drivebit-compose="1"]')) return true;
        return false;
    }

    function loadCompose() {
        var src = resolveSrc();
        if (alreadyRequested(src)) return;
        var script = document.createElement("script");
        script.src = src;
        script.type = "application/javascript";
        script.defer = true;
        script.setAttribute("data-drivebit-compose", "1");
        document.head.appendChild(script);
    }

    function schedule() {
        if (scheduled) return;
        scheduled = true;

        var started = false;
        function start() {
            if (started) return;
            started = true;
            cleanup();
            loadCompose();
        }

        function onInteraction() {
            start();
        }

        function cleanup() {
            for (var i = 0; i < INTERACTION_EVENTS.length; i++) {
                window.removeEventListener(INTERACTION_EVENTS[i], onInteraction);
            }
            if (fallbackId != null) {
                window.clearTimeout(fallbackId);
                fallbackId = null;
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
            }, { timeout: FALLBACK_MS });
        }

        fallbackId = window.setTimeout(function () {
            start();
        }, FALLBACK_MS);

        window.drivebitLoadCompose = start;
    }

    schedule();
})(document, window);
