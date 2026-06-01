/**
 * DriveBit app header — auth, city, nav, butter menu, unread banner.
 * Storage keys match StorageImpl / MyCityStorageKeys (localStorage via StorageSettings).
 * API: GET /api/Chat/has-unread, GET /api/Car/my, GET /api/Photo/avatar/my
 */
(function () {
    "use strict";

    var API_BASE = "/api/";
    var TOKEN_KEY = "auth_token";
    var CITY_NAME_KEY = "my_city_name";
    var DEFAULT_CITY = "Москва";
    var POLL_MS = 30000;
    var MOBILE_BP = 768;
    var REDIRECT_PARAM = "redirect";

    var RESERVED = {};
    (
        "city-selection my-city-selection list-your-car verify-otp login-by-phone login-by-mail " +
        "login-by-password signup profile my-cars my-bookings leave-review my-deals chats chat documents " +
        "offer contacts privacy cookies payment payment-success payment-failure download-booking-contract " +
        "car-edit car-sts-upload car-photos-gallery car-photos-upload car-photos car-availability edit-name " +
        "change-email change-phone change-password address-input license-plate-input car-brand-selection " +
        "car-model-selection body-type-selection drive-type-selection engine-type-selection engine-volume-input " +
        "production-year-input seats-count-input trunk-size-selection daily-rate-input description-input " +
        "passport-upload search car-detail"
    )
        .split(/\s+/)
        .forEach(function (s) {
            RESERVED[s] = true;
        });

    var ICONS = {
        user: "/images/menu/user.svg",
        car: "/images/butter/car-icon.svg",
        docs: "/images/butter/docs.svg",
        booking: "/images/butter/booking.svg",
        deals: "/images/butter/deals.svg",
        mail: "/images/butter/mail.svg",
        logout: "/images/butter/logout.svg"
    };

    var TRANSLIT = {
        "\u0430": "a", "\u0431": "b", "\u0432": "v", "\u0433": "g", "\u0434": "d", "\u0435": "e",
        "\u0451": "yo", "\u0436": "zh", "\u0437": "z", "\u0438": "i", "\u0439": "y", "\u043a": "k",
        "\u043b": "l", "\u043c": "m", "\u043d": "n", "\u043e": "o", "\u043f": "p", "\u0440": "r",
        "\u0441": "s", "\u0442": "t", "\u0443": "u", "\u0444": "f", "\u0445": "h", "\u0446": "ts",
        "\u0447": "ch", "\u0448": "sh", "\u0449": "sch", "\u044a": "", "\u044c": "", "\u044b": "y",
        "\u044d": "e", "\u044e": "yu", "\u044f": "ya"
    };

    var menuOpen = false;
    var pollTimer = null;

    function cityNameToSlug(name) {
        var lower = (name || "").trim().toLowerCase();
        var buf = "";
        for (var i = 0; i < lower.length; i++) {
            var ch = lower.charAt(i);
            if (/\s|-|_|\./.test(ch)) {
                buf += " ";
            } else if (TRANSLIT[ch] !== undefined) {
                buf += TRANSLIT[ch];
            } else if (/[a-z0-9]/.test(ch)) {
                buf += ch;
            }
        }
        var parts = buf.trim().split(/\s+/).filter(Boolean);
        var slug = parts
            .map(function (seg) {
                return seg.replace(/[^a-z0-9]/g, "");
            })
            .filter(Boolean)
            .join("-");
        return slug || "city";
    }

    function parseCitySlugFromPath(pathname) {
        var pathOnly = (pathname || "").split("?")[0].split("#")[0];
        var trimmed = pathOnly.replace(/^\/+|\/+$/g, "");
        if (!trimmed) return null;
        var segments = trimmed.split("/").filter(Boolean);
        if (segments.length !== 1 && segments.length !== 2) return null;
        var citySlug = segments[0].toLowerCase();
        if (RESERVED[citySlug]) return null;
        return citySlug;
    }

    function homePathHref() {
        var fromPath = parseCitySlugFromPath(window.location.pathname);
        if (fromPath) return "/" + fromPath;
        var name = localStorage.getItem(CITY_NAME_KEY) || "";
        return "/" + cityNameToSlug(name.trim() || DEFAULT_CITY);
    }

    function getToken() {
        var t = localStorage.getItem(TOKEN_KEY);
        return t && t.length > 0 ? t : null;
    }

    function isLogined() {
        return getToken() !== null;
    }

    function appIsland(pathname) {
        var p = (pathname || window.location.pathname).split("?")[0];
        if (p.indexOf("/car-detail") === 0 || p.indexOf("/car-photos-gallery") === 0) return "carDetail";
        var parts = parseCityPathFilter(p);
        if (parts && parts.filterSlug === "poblizosti") return "nearby";
        if (p.indexOf("/search") === 0) return "search";
        return "main";
    }

    function parseCityPathFilter(pathname) {
        var pathOnly = (pathname || "").split("?")[0].split("#")[0];
        var trimmed = pathOnly.replace(/^\/+|\/+$/g, "");
        if (!trimmed) return null;
        var segments = trimmed.split("/").filter(Boolean);
        if (segments.length !== 1 && segments.length !== 2) return null;
        var citySlug = segments[0].toLowerCase();
        if (RESERVED[citySlug]) return null;
        return { citySlug: citySlug, filterSlug: segments[1] ? segments[1].toLowerCase() : null };
    }

    function shouldUseFullPageNavigation(targetPath) {
        return appIsland(window.location.pathname) !== appIsland(targetPath);
    }

    function navigate(path) {
        if (shouldUseFullPageNavigation(path)) {
            window.location.href = path;
            return;
        }
        window.history.pushState(null, "", path);
        window.dispatchEvent(new PopStateEvent("popstate"));
    }

    function apiFetch(path, options) {
        var headers = { Accept: "application/json" };
        var token = getToken();
        if (token) headers.Authorization = "Bearer " + token;
        return fetch(API_BASE + path, Object.assign({ headers: headers }, options || {}));
    }

    function listYourCarHref() {
        if (isLogined()) return "/list-your-car.html";
        return "/login-by-phone?" + REDIRECT_PARAM + "=" + encodeURIComponent("/list-your-car.html");
    }

    function updateListCarLinks() {
        var href = listYourCarHref();
        document.querySelectorAll("[data-drivebit-nav-list-car]").forEach(function (el) {
            el.setAttribute("href", href);
        });
    }

    function updateLogo() {
        var logo = document.getElementById("drivebit-header-logo");
        if (!logo) return;
        logo.setAttribute("href", homePathHref());
    }

    function updateCity() {
        var el = document.getElementById("drivebit-header-city");
        if (!el) return;
        var name = (localStorage.getItem(CITY_NAME_KEY) || "").trim();
        if (!name) {
            el.hidden = true;
            return;
        }
        el.textContent = name;
        el.hidden = false;
    }

    function updateMobileNavVisibility() {
        var mobile = document.getElementById("drivebit-header-nav-mobile");
        if (!mobile) return;
        if (window.innerWidth <= MOBILE_BP) {
            mobile.hidden = false;
        } else {
            mobile.hidden = true;
        }
    }

    function closeMenu() {
        menuOpen = false;
        var overlay = document.getElementById("drivebit-butter-overlay");
        var menu = document.getElementById("drivebit-butter-menu");
        var btn = document.getElementById("drivebit-header-menu-btn");
        if (overlay) overlay.hidden = true;
        if (menu) menu.hidden = true;
        if (btn) btn.setAttribute("aria-expanded", "false");
    }

    function openMenu(items) {
        var overlay = document.getElementById("drivebit-butter-overlay");
        var menu = document.getElementById("drivebit-butter-menu");
        var btn = document.getElementById("drivebit-header-menu-btn");
        if (!menu) return;
        menu.innerHTML = "";
        items.forEach(function (item) {
            var row = document.createElement(item.href ? "a" : "button");
            row.className = "drivebit-butter-item";
            if (item.href) {
                row.href = item.href;
                row.setAttribute("data-drivebit-menu-path", item.path || item.href);
            } else {
                row.type = "button";
            }
            if (item.icon) {
                var img = document.createElement("img");
                img.src = item.icon;
                img.alt = "";
                row.appendChild(img);
            }
            row.appendChild(document.createTextNode(item.text));
            row.addEventListener("click", function (e) {
                if (item.action) {
                    e.preventDefault();
                    item.action();
                }
                closeMenu();
            });
            menu.appendChild(row);
        });
        menuOpen = true;
        if (overlay) overlay.hidden = false;
        menu.hidden = false;
        if (btn) btn.setAttribute("aria-expanded", "true");
    }

    function buildMenuItems(hasCars) {
        var items = [];
        if (isLogined()) {
            items.push({ text: "Мой профиль", icon: ICONS.user, path: "/profile" });
            items.push({ text: "Входящие", icon: ICONS.mail, path: "/chats" });
            items.push({ text: "Мои документы", icon: ICONS.docs, path: "/documents" });
            items.push({ text: "Мои бронирования", icon: ICONS.booking, path: "/my-bookings" });
            items.push({ text: "Мои сделки", icon: ICONS.deals, path: "/my-deals" });
            if (hasCars) {
                items.push({ text: "Мои авто", icon: ICONS.car, path: "/my-cars" });
            } else {
                items.push({ text: "Сдать авто", icon: ICONS.car, href: listYourCarHref() });
            }
            items.push({
                text: "Выйти",
                icon: ICONS.logout,
                action: function () {
                    localStorage.clear();
                    window.location.reload();
                }
            });
        } else {
            items.push({ text: "Логин", path: "/login-by-phone" });
            items.push({ text: "Регистрация", path: "/login-by-phone" });
        }
        return items.map(function (it) {
            if (it.path && !it.action) {
                return Object.assign({}, it, { href: it.path });
            }
            return it;
        });
    }

    function loadCarMenuOption() {
        if (!isLogined()) return Promise.resolve(false);
        return apiFetch("Car/my")
            .then(function (r) {
                if (!r.ok) return false;
                return r.json();
            })
            .then(function (data) {
                if (Array.isArray(data)) return data.length > 0;
                if (data && Array.isArray(data.items)) return data.items.length > 0;
                return false;
            })
            .catch(function () {
                return false;
            });
    }

    function refreshAvatar() {
        var img = document.getElementById("drivebit-header-avatar");
        if (!img || !isLogined()) {
            if (img) img.hidden = true;
            return Promise.resolve();
        }
        return apiFetch("Photo/avatar/my")
            .then(function (r) {
                if (!r.ok) throw new Error("avatar");
                return r.json();
            })
            .then(function (data) {
                var url = data && data.url;
                if (url) {
                    img.src = url;
                    img.hidden = false;
                } else {
                    img.hidden = true;
                }
            })
            .catch(function () {
                img.hidden = true;
            });
    }

    function refreshUnread() {
        var banner = document.getElementById("drivebit-unread-banner");
        if (!banner) return Promise.resolve();
        if (!isLogined()) {
            banner.hidden = true;
            return Promise.resolve();
        }
        return apiFetch("Chat/has-unread")
            .then(function (r) {
                if (!r.ok) throw new Error("unread");
                return r.json();
            })
            .then(function (data) {
                banner.hidden = !(data && data.hasUnread);
            })
            .catch(function () {
                banner.hidden = true;
            });
    }

    function onMenuButtonClick() {
        if (menuOpen) {
            closeMenu();
            return;
        }
        loadCarMenuOption().then(function (hasCars) {
            openMenu(buildMenuItems(hasCars));
        });
    }

    function bindNavClicks() {
        document.querySelectorAll("[data-drivebit-nav]").forEach(function (el) {
            el.addEventListener("click", function (e) {
                var path = el.getAttribute("data-drivebit-nav");
                if (!path) return;
                e.preventDefault();
                navigate(path);
            });
        });
        document.querySelectorAll("[data-drivebit-nav-list-car]").forEach(function (el) {
            el.addEventListener("click", function (e) {
                var href = el.getAttribute("href") || listYourCarHref();
                if (href.indexOf("/login-by-phone") === 0 || href.indexOf("/list-your-car.html") === 0) {
                    if (shouldUseFullPageNavigation(href.split("?")[0])) return;
                    e.preventDefault();
                    window.location.href = href;
                }
            });
        });
        document.querySelectorAll("#drivebit-butter-menu").forEach(function (menu) {
            menu.addEventListener("click", function (e) {
                var target = e.target.closest("[data-drivebit-menu-path]");
                if (!target) return;
                var path = target.getAttribute("data-drivebit-menu-path");
                if (!path || path.indexOf("http") === 0) return;
                e.preventDefault();
                navigate(path);
            });
        });
    }

    function markRootAsMain() {
        var root = document.getElementById("root");
        if (root && !root.classList.contains("drivebit-app-main")) {
            root.classList.add("drivebit-app-main");
        }
    }

    function refreshAll() {
        updateLogo();
        updateCity();
        updateListCarLinks();
        updateMobileNavVisibility();
        refreshUnread();
        refreshAvatar();
        closeMenu();
    }

    function startPolling() {
        if (pollTimer) clearInterval(pollTimer);
        pollTimer = setInterval(refreshUnread, POLL_MS);
    }

    function init() {
        if (!document.getElementById("drivebit-app-header")) return;

        markRootAsMain();

        var logo = document.getElementById("drivebit-header-logo");
        if (logo) {
            logo.addEventListener("click", function (e) {
                e.preventDefault();
                navigate(homePathHref());
            });
        }

        var city = document.getElementById("drivebit-header-city");
        if (city) {
            city.addEventListener("click", function (e) {
                e.preventDefault();
                navigate("/my-city-selection");
            });
        }

        var menuBtn = document.getElementById("drivebit-header-menu-btn");
        if (menuBtn) menuBtn.addEventListener("click", onMenuButtonClick);

        var overlay = document.getElementById("drivebit-butter-overlay");
        if (overlay) overlay.addEventListener("click", closeMenu);

        var banner = document.getElementById("drivebit-unread-banner");
        if (banner) {
            banner.addEventListener("click", function (e) {
                e.preventDefault();
                navigate("/chats");
            });
        }

        bindNavClicks();
        refreshAll();
        startPolling();

        window.addEventListener("resize", updateMobileNavVisibility);
        window.addEventListener("drivebit-auth-changed", refreshAll);
        window.addEventListener("popstate", function () {
            updateLogo();
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
