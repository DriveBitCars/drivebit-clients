(function () {
    function qs(id) {
        return document.getElementById(id);
    }

    function citySlugFromPath(pathname) {
        var trimmed = (pathname || "").replace(/\/+$/, "").replace(/^\//, "");
        if (!trimmed) return "moskva";
        var segment = trimmed.split("/")[0];
        if (!segment || segment === "search") return "moskva";
        return segment.toLowerCase();
    }

    function buildSearchUrl(startDate, endDate) {
        var params = [];
        if (startDate) params.push("startDate=" + encodeURIComponent(startDate));
        if (endDate) params.push("endDate=" + encodeURIComponent(endDate));
        var base = "/" + citySlugFromPath(window.location.pathname) + "/search";
        return params.length ? base + "?" + params.join("&") : base;
    }

    function formatDateDisplay(isoDate) {
        if (!isoDate) return "";
        var parts = isoDate.split("-");
        if (parts.length !== 3) return isoDate;
        return parts[2] + "." + parts[1] + "." + parts[0];
    }

    function readDatesFromQuery() {
        var params = new URLSearchParams(window.location.search);
        return {
            startDate: params.get("startDate") || "",
            endDate: params.get("endDate") || "",
        };
    }

    function updateDateDisplay(displayEl, isoDate) {
        if (!displayEl) return;
        if (isoDate) {
            displayEl.textContent = formatDateDisplay(isoDate);
            displayEl.classList.remove("is-empty");
        } else {
            displayEl.textContent = "выберите даты";
            displayEl.classList.add("is-empty");
        }
    }

    function applyQueryToDisplays(startDisplay, endDisplay) {
        var dates = readDatesFromQuery();
        updateDateDisplay(startDisplay, dates.startDate);
        updateDateDisplay(endDisplay, dates.endDate);
    }

    function initHeroSearch() {
        var hero = qs("drivebit-hero-static");
        if (!hero) return;

        var startDisplay = qs("drivebit-hero-start-display");
        var endDisplay = qs("drivebit-hero-end-display");
        var startTrigger = qs("drivebit-hero-start-trigger");
        var endTrigger = qs("drivebit-hero-end-trigger");
        var searchBtn = qs("drivebit-hero-search-btn");
        if (!startDisplay || !endDisplay || !searchBtn) return;

        applyQueryToDisplays(startDisplay, endDisplay);

        if (startTrigger) {
            startTrigger.addEventListener("click", function () {
                window.dispatchEvent(new CustomEvent("drivebit-hero-open-start-calendar"));
            });
        }

        if (endTrigger) {
            endTrigger.addEventListener("click", function () {
                window.dispatchEvent(new CustomEvent("drivebit-hero-open-end-calendar"));
            });
        }

        searchBtn.addEventListener("click", function () {
            if (window.__drivebitHeroBridgeReady) {
                window.dispatchEvent(new CustomEvent("drivebit-hero-search"));
                return;
            }
            var dates = readDatesFromQuery();
            window.location.href = buildSearchUrl(dates.startDate, dates.endDate);
        });

        window.addEventListener("drivebit-hero-dates-changed", function () {
            applyQueryToDisplays(startDisplay, endDisplay);
        });

        window.addEventListener("popstate", function () {
            applyQueryToDisplays(startDisplay, endDisplay);
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initHeroSearch);
    } else {
        initHeroSearch();
    }
})();
