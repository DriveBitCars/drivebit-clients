(function () {
    if (typeof window.drivebitLoadLeaflet === "function") {
        return;
    }

    window.drivebitLoadLeaflet = function drivebitLoadLeaflet() {
        if (typeof window.L !== "undefined") {
            return Promise.resolve(window.L);
        }
        if (window.__drivebitLeafletPromise) {
            return window.__drivebitLeafletPromise;
        }

        window.__drivebitLeafletPromise = new Promise(function (resolve, reject) {
            if (!document.querySelector('link[data-drivebit-leaflet-css]')) {
                var link = document.createElement("link");
                link.rel = "stylesheet";
                link.href = "/vendor/leaflet/leaflet.css";
                link.setAttribute("data-drivebit-leaflet-css", "1");
                document.head.appendChild(link);
            }

            var script = document.createElement("script");
            script.src = "/vendor/leaflet/leaflet.js";
            script.async = true;
            script.onload = function () {
                if (typeof window.L !== "undefined") {
                    window.L.Icon.Default.mergeOptions({
                        iconUrl: "/vendor/leaflet/images/marker-icon.png",
                        iconRetinaUrl: "/vendor/leaflet/images/marker-icon-2x.png",
                        shadowUrl: "/vendor/leaflet/images/marker-shadow.png",
                    });
                    resolve(window.L);
                } else {
                    reject(new Error("Leaflet failed to initialize"));
                }
            };
            script.onerror = function () {
                reject(new Error("Leaflet script failed to load"));
            };
            document.head.appendChild(script);
        });

        return window.__drivebitLeafletPromise;
    };
})();
