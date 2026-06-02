(function () {
    function qs(id) {
        return document.getElementById(id);
    }

    function normalizePath(pathname) {
        var path = pathname || "/";
        if (path.length > 1 && path.endsWith("/")) {
            path = path.slice(0, -1);
        }
        return path || "/";
    }

    function setPressedState(nav, activeTitle) {
        if (!nav) return;
        var buttons = nav.querySelectorAll(".drivebit-filter-btn");
        buttons.forEach(function (btn) {
            var title = btn.getAttribute("data-filter-title") || "";
            var pressed = title === activeTitle;
            btn.setAttribute("aria-pressed", pressed ? "true" : "false");
            btn.classList.toggle("is-selected", pressed);
        });
    }

    function titleFromPath(pathname) {
        var path = normalizePath(pathname);
        var nav = qs("drivebit-filters-static");
        if (!nav) return "Все";
        var buttons = nav.querySelectorAll(".drivebit-filter-btn");
        for (var i = 0; i < buttons.length; i++) {
            var btn = buttons[i];
            var filterPath = btn.getAttribute("data-filter-path");
            if (filterPath && normalizePath(filterPath) === path) {
                return btn.getAttribute("data-filter-title") || "Все";
            }
        }
        return "Все";
    }

    function initFilters() {
        var nav = qs("drivebit-filters-static");
        if (!nav) return;

        var syncFromLocation = function () {
            setPressedState(nav, titleFromPath(window.location.pathname));
        };

        syncFromLocation();

        nav.addEventListener("click", function (event) {
            var target = event.target;
            if (!(target instanceof Element)) return;
            var btn = target.closest(".drivebit-filter-btn");
            if (!btn || !nav.contains(btn)) return;

            event.preventDefault();
            var title = btn.getAttribute("data-filter-title") || "Все";
            var path = btn.getAttribute("data-filter-path") || "/moskva";
            var search = window.location.search || "";
            var url = path + search;

            if (window.__drivebitFiltersBridgeReady) {
                window.dispatchEvent(
                    new CustomEvent("drivebit-filter-select", {
                        detail: { title: title, path: path, url: url },
                    })
                );
                return;
            }

            window.location.href = url;
        });

        window.addEventListener("popstate", syncFromLocation);
        window.addEventListener("drivebit-filter-path-changed", syncFromLocation);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initFilters);
    } else {
        initFilters();
    }
})();
