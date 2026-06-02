(function () {
    function afterLoad(fn) {
        if (document.readyState === "complete") {
            fn();
            return;
        }
        window.addEventListener("load", fn, { once: true });
    }

    function whenIdle(fn) {
        if (typeof window.requestIdleCallback === "function") {
            window.requestIdleCallback(fn, { timeout: 3000 });
        } else {
            window.setTimeout(fn, 1);
        }
    }

    function loadMetrika() {
        if (window.ym) {
            return;
        }
        (function (m, e, t, r, i, k, a) {
            m[i] =
                m[i] ||
                function () {
                    (m[i].a = m[i].a || []).push(arguments);
                };
            m[i].l = 1 * new Date();
            for (var j = 0; j < document.scripts.length; j++) {
                if (document.scripts[j].src === r) {
                    return;
                }
            }
            k = e.createElement(t);
            a = e.getElementsByTagName(t)[0];
            k.async = 1;
            k.src = r;
            a.parentNode.insertBefore(k, a);
        })(window, document, "script", "https://mc.yandex.ru/metrika/tag.js?id=105947907", "ym");

        window.ym(105947907, "init", {
            ssr: true,
            webvisor: true,
            clickmap: true,
            ecommerce: "dataLayer",
            accurateTrackBounce: true,
            trackLinks: true,
        });
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
        script.src = "//code.jivo.ru/widget/MWoBzLXYYF";
        script.async = true;
        document.head.appendChild(script);
    }

    function loadCallibri(attempt) {
        if (document.querySelector("script[data-drivebit-callibri]")) {
            return;
        }
        var script = document.createElement("script");
        script.src = "https://cdn.callibri.ru/callibri.js";
        script.type = "text/javascript";
        script.charset = "utf-8";
        script.async = true;
        script.setAttribute("data-drivebit-callibri", "1");
        script.onerror = function () {
            script.remove();
            if ((attempt || 0) < 1) {
                window.setTimeout(function () {
                    loadCallibri((attempt || 0) + 1);
                }, 3000);
            }
        };
        document.body.appendChild(script);
    }

    afterLoad(function () {
        whenIdle(function () {
            loadMetrika();
            loadJivo();
            loadCallibri(0);
        });
    });
})();
