(function (document, window) {
    var METRIKA_TAG_URL = "https://mc.yandex.ru/metrika/tag.js?id=105947907";
    var METRIKA_COUNTER_ID = 105947907;
    var CALLIBRI_URL = "https://cdn.callibri.ru/callibri.js";
    var JIVO_URL = "//code.jivo.ru/widget/MWoBzLXYYF";

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

    function loadMetrika(onTagReady) {
        if (isMetrikaTagRequested()) {
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

    function loadCallibri(attempt) {
        if (document.querySelector("script[data-drivebit-callibri]")) {
            return;
        }
        if (document.querySelector('script[src*="callibri.js"]')) {
            return;
        }
        var script = document.createElement("script");
        script.src = CALLIBRI_URL;
        script.type = "text/javascript";
        script.charset = "utf-8";
        script.defer = true;
        script.setAttribute("data-drivebit-callibri", "1");
        script.onerror = function () {
            script.remove();
            if ((attempt || 0) < 1) {
                window.setTimeout(function () {
                    loadCallibri((attempt || 0) + 1);
                }, 3000);
            }
        };
        document.head.appendChild(script);
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

    loadMetrika(function () {
        loadCallibri(0);
    });

    afterLoad(function () {
        loadJivo();
    });
})(document, window);
