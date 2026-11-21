(function () {
    function qs(id) {
        return document.getElementById(id);
    }

    function appBase() {
        return (typeof window !== "undefined" && window.__DRIVEBIT_BASE__) || "";
    }

    function appPathname(pathname) {
        var base = appBase();
        var path = pathname || "/";
        if (base && (path === base || path === base + "/")) return "/";
        if (base && path.indexOf(base + "/") === 0) return path.slice(base.length) || "/";
        return path;
    }

    function withAppBase(url) {
        var base = appBase();
        if (!base || !url || url.charAt(0) !== "/" || url.indexOf(base) === 0) return url;
        return base + url;
    }

    function normalizePath(pathname) {
        var path = appPathname(pathname || "/");
        if (path.length > 1 && path.endsWith("/")) {
            path = path.slice(0, -1);
        }
        return path || "/";
    }

    function scrollActiveButtonIntoView(nav, activeTitle) {
        var scroll = nav.querySelector(".drivebit-filters-scroll");
        var buttons = nav.querySelectorAll(".drivebit-filter-btn");
        var activeBtn = null;
        for (var i = 0; i < buttons.length; i++) {
            if ((buttons[i].getAttribute("data-filter-title") || "") === activeTitle) {
                activeBtn = buttons[i];
                break;
            }
        }
        if (!scroll || !activeBtn) return;
        var maxScroll = Math.max(0, scroll.scrollWidth - scroll.clientWidth);
        var scrollRect = scroll.getBoundingClientRect();
        var btnRect = activeBtn.getBoundingClientRect();
        var delta =
            btnRect.left + btnRect.width / 2 - (scrollRect.left + scrollRect.width / 2);
        var target = scroll.scrollLeft + delta;
        scroll.scrollTo({
            left: Math.max(0, Math.min(target, maxScroll)),
            behavior: "smooth",
        });
    }

    function headerOffsetPx() {
        var header = document.querySelector(".drivebit-app-header");
        if (!header) return 72;
        return Math.ceil(header.getBoundingClientRect().height) || 72;
    }

    /** Scroll page to the cars list below the hero (home / city landing). */
    function scrollToCarsList() {
        var target =
            document.getElementById("drivebit-cars") ||
            document.querySelector(".drivebit-cars-section-title") ||
            document.querySelector("#drivebit-cars-grid-static") ||
            document.querySelector(".drivebit-cars-grid-mounted");
        if (!target) return;
        var top =
            target.getBoundingClientRect().top +
            (window.pageYOffset || document.documentElement.scrollTop) -
            headerOffsetPx() -
            8;
        window.scrollTo({ top: Math.max(0, top), behavior: "smooth" });
    }

    function syncHeroBackground(nav, activeTitle) {
        var heroBg = qs("drivebit-hero-bg");
        if (!heroBg || !nav) return;
        var buttons = nav.querySelectorAll(".drivebit-filter-btn");
        for (var i = 0; i < buttons.length; i++) {
            var btn = buttons[i];
            if ((btn.getAttribute("data-filter-title") || "") !== activeTitle) continue;
            var bg = btn.getAttribute("data-hero-bg");
            if (bg && heroBg.getAttribute("src") !== bg) {
                heroBg.setAttribute("src", bg);
            }
            return;
        }
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
        scrollActiveButtonIntoView(nav, activeTitle);
        syncHeroBackground(nav, activeTitle);
    }

    // cityNameToSlug aliases for filters that also have longer SEO path segments
    var SHORT_TITLE_BY_SLUG = {
        komfort: "Комфорт",
        biznes: "Бизнес",
        premium: "Премиум",
        ekonom: "Эконом",
        vnedorozhnik: "Внедорожник",
        miniven: "Минивэн",
        "v-krym": "В Крым",
        belarus: "Беларусь",
        abkhaziya: "Абхазия",
    };

    function titleFromPath(pathname) {
        var path = normalizePath(pathname);
        var nav = qs("drivebit-filters-static");
        if (!nav) return "Поблизости";
        var pathParts = path.split("/").filter(Boolean);
        var segment = pathParts.length > 1 ? pathParts[pathParts.length - 1] : "";
        var buttons = nav.querySelectorAll(".drivebit-filter-btn");
        var defaultTitle =
            (buttons[0] && buttons[0].getAttribute("data-filter-title")) || "Поблизости";
        // City root (/moskva) — keep first category selected (no "Все" chip in Figma 1440)
        if (!segment || segment === pathParts[0]) {
            return defaultTitle;
        }
        for (var i = 0; i < buttons.length; i++) {
            var btn = buttons[i];
            var filterPath = btn.getAttribute("data-filter-path");
            if (filterPath && normalizePath(filterPath) === path) {
                return btn.getAttribute("data-filter-title") || defaultTitle;
            }
            if (
                segment &&
                filterPath &&
                normalizePath(filterPath).split("/").pop() === segment
            ) {
                return btn.getAttribute("data-filter-title") || defaultTitle;
            }
        }
        var aliasTitle = SHORT_TITLE_BY_SLUG[segment];
        if (aliasTitle) {
            for (var j = 0; j < buttons.length; j++) {
                if ((buttons[j].getAttribute("data-filter-title") || "") === aliasTitle) {
                    return aliasTitle;
                }
            }
        }
        return defaultTitle;
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
                setPressedState(nav, title);
                window.dispatchEvent(
                    new CustomEvent("drivebit-filter-select", {
                        detail: { title: title, path: path, url: url },
                    })
                );
                // After filter applies on the same page — scroll to the cars list
                window.setTimeout(scrollToCarsList, 50);
                return;
            }

            // Full navigation: land on cars block when target page has one
            var next = withAppBase(url);
            if (next.indexOf("#") === -1) next += "#drivebit-cars";
            window.location.href = next;
        });

        window.addEventListener("popstate", syncFromLocation);
        window.addEventListener("drivebit-filter-path-changed", syncFromLocation);

        // Land on cars block when opened via #drivebit-cars (full page nav from filter click)
        if (window.location.hash === "#drivebit-cars") {
            window.setTimeout(scrollToCarsList, 100);
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initFilters);
    } else {
        initFilters();
    }
})();
