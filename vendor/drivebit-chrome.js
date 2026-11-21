(function () {
  "use strict";

  var ICON = "/images/chrome/";
  var LOGO_HEADER = "/images/logos/drivebit-logo-header.svg";
  var LOGO_FOOTER = "/images/logos/drivebit-logo-footer.svg";
  var LOGO_WHITE = LOGO_HEADER;

  function qs(sel, root) {
    return (root || document).querySelector(sel);
  }

  function qsa(sel, root) {
    return Array.prototype.slice.call((root || document).querySelectorAll(sel));
  }

  function citySlug() {
    var path = window.location.pathname || "/";
    var parts = path.split("/").filter(Boolean);
    if (parts.length && !/^(search|profile|chats|contacts|offer|cookies|privacy|blog|login|account|my-|car-|list-|auth)/i.test(parts[0])) {
      return parts[0];
    }
    return "moskva";
  }

  function cityHomeHref() {
    return "/" + citySlug();
  }

  function searchHref() {
    return "/" + citySlug() + "/search";
  }

  function swapLogo() {
    qsa(".drivebit-header-logo img, .drivebit-site-menu-header img.turo-logo, img.drivebit-logo-header").forEach(function (img) {
      img.src = LOGO_HEADER;
      img.setAttribute("data-db-logo", "header");
      img.removeAttribute("width");
      img.removeAttribute("height");
      img.style.height = "";
      img.style.width = "";
      img.style.maxHeight = "";
      img.style.maxWidth = "";
      img.style.objectFit = "contain";
      img.style.objectPosition = "left center";
    });
    qsa("img.drivebit-logo-footer, .drivebit-footer-brand img").forEach(function (img) {
      img.src = LOGO_FOOTER;
      img.classList.add("drivebit-logo-footer");
      img.removeAttribute("width");
      img.removeAttribute("height");
      img.style.height = "";
      img.style.width = "";
    });
  }

  function patchDesktopNav() {
    var nav = qs("#drivebit-header-nav-desktop");
    if (!nav) return;
    var links = qsa("a", nav);
    if (links[0]) links[0].textContent = "Арендовать авто";
    var hasBlog = links.some(function (a) {
      return /блог/i.test(a.textContent || "");
    });
    if (!hasBlog) {
      var blog = document.createElement("a");
      blog.href = "/blog";
      blog.textContent = "Блог";
      nav.appendChild(blog);
    }
  }

  function patchHeroDateLabels() {
    qsa(".drivebit-hero-date-label--desktop").forEach(function (el, i) {
      el.textContent = i === 0 ? "Дата от" : "Дата до";
    });
    qsa(".drivebit-hero-date-value.is-empty").forEach(function (el) {
      if (/выберите|дд-мм/i.test(el.textContent || "")) el.textContent = "дд-мм-гггг";
    });
  }

  function ensureCarsSectionTitle() {
    var existing = qs(".drivebit-cars-section-title");
    if (existing) {
      if (!existing.id) existing.id = "drivebit-cars";
      return;
    }
    var grid = qs("#drivebit-cars-grid-static, .drivebit-cars-grid-mounted");
    if (!grid) return;
    if (!qs("#drivebit-hero-static")) return;
    var h2 = document.createElement("h2");
    h2.id = "drivebit-cars";
    h2.className = "drivebit-cars-section-title";
    h2.textContent = "Автомобили в аренду";
    grid.parentNode.insertBefore(h2, grid);
  }

  function claimComposeHeaderSlot() {
    var compose = qs("#drivebit-header-compose");
    if (compose) compose.setAttribute("data-db-chrome-owned", "1");
  }

  function ensureDesktopHeaderActions() {
    var inner = qs(".drivebit-app-header-inner");
    if (!inner) return;
    claimComposeHeaderSlot();
    if (!qs(".drivebit-header-desktop-actions", inner)) {
      var actions = document.createElement("div");
      actions.className = "drivebit-header-desktop-actions";
      actions.setAttribute("data-db-chrome-actions", "1");
      actions.innerHTML =
        '<a class="drivebit-header-city" href="/my-city-selection">' +
        '<img src="' + ICON + 'icon-location.svg" alt="">' +
        "<span data-db-city-label>Москва</span></a>" +
        '<a class="drivebit-header-phone" href="tel:+74958775051">+7 (495) 877-50-51</a>' +
        '<div class="drivebit-master-wrap" data-db-master></div>';
      // Before compose mount point so chrome owns the right side
      var compose = qs("#drivebit-header-compose", inner);
      if (compose) inner.insertBefore(actions, compose);
      else inner.appendChild(actions);
    }
    ensureMasterButton();
  }

  function authToken() {
    try {
      return (window.localStorage && localStorage.getItem("auth_token")) || "";
    } catch (e) {
      return "";
    }
  }

  function isLoggedIn() {
    return !!authToken();
  }

  function cachedFirstName() {
    try {
      return (localStorage.getItem("db_user_first_name") || "").trim();
    } catch (e) {
      return "";
    }
  }

  function setCachedFirstName(name) {
    try {
      if (name) localStorage.setItem("db_user_first_name", name);
      else localStorage.removeItem("db_user_first_name");
    } catch (e) {}
  }

  function logout() {
    try {
      localStorage.removeItem("auth_token");
      localStorage.removeItem("refresh_token");
      localStorage.removeItem("db_user_first_name");
    } catch (e) {}
    window.location.href = cityHomeHref();
  }

  function guestMenuHtml() {
    return (
      '<div class="drivebit-master-menu" role="menu">' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/login-by-phone">Войти</a>' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/login-by-phone">Регистрация</a>' +
      "</div>"
    );
  }

  function authMenuHtml() {
    return (
      '<div class="drivebit-master-menu drivebit-master-menu--auth" role="menu">' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/profile"><img src="/images/butter/user.svg" alt=""><span>Мой профиль</span></a>' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/chats"><img src="/images/butter/mail.svg" alt=""><span>Сообщения</span></a>' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/documents"><img src="/images/butter/docs.svg" alt=""><span>Мои документы</span></a>' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/my-bookings"><img src="/images/butter/booking.svg" alt=""><span>Мои бронирования</span></a>' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/my-deals"><img src="/images/butter/deals.svg" alt=""><span>Мои сделки</span></a>' +
      '<a class="drivebit-master-menu-item" role="menuitem" href="/my-cars"><img src="/images/butter/car-icon.svg" alt=""><span>Мои авто</span></a>' +
      '<button type="button" class="drivebit-master-menu-item drivebit-master-logout" role="menuitem"><img src="/images/butter/logout.svg" alt=""><span>Выйти</span></button>' +
      "</div>"
    );
  }

  function renderMasterTrigger(loggedIn, firstName) {
    if (!loggedIn) {
      return (
        '<button type="button" class="drivebit-header-login drivebit-master-btn" aria-haspopup="true" aria-expanded="false">' +
        "Войти</button>"
      );
    }
    var name = firstName || "";
    return (
      '<button type="button" class="drivebit-header-login drivebit-master-btn drivebit-master-btn--user" aria-haspopup="true" aria-expanded="false">' +
      '<img class="drivebit-master-person" src="' + ICON + 'icon-person-fill.svg" alt="">' +
      '<span class="drivebit-master-name">' +
      name.replace(/</g, "&lt;") +
      "</span></button>"
    );
  }

  function wireMasterWrap(wrap) {
    if (!wrap || wrap.getAttribute("data-db-wired") === "1") return;
    wrap.setAttribute("data-db-wired", "1");
    var hideTimer = null;
    function open() {
      if (hideTimer) {
        clearTimeout(hideTimer);
        hideTimer = null;
      }
      wrap.classList.add("is-open");
      var btn = qs(".drivebit-master-btn", wrap);
      if (btn) btn.setAttribute("aria-expanded", "true");
    }
    function closeSoon() {
      if (hideTimer) clearTimeout(hideTimer);
      hideTimer = setTimeout(function () {
        wrap.classList.remove("is-open");
        var btn = qs(".drivebit-master-btn", wrap);
        if (btn) btn.setAttribute("aria-expanded", "false");
      }, 280);
    }
    wrap.addEventListener("mouseenter", open);
    wrap.addEventListener("mouseleave", closeSoon);
    // Keep open while pointer is on the dropdown itself (covers gap / nested targets)
    var menu = qs(".drivebit-master-menu", wrap);
    if (menu) {
      menu.addEventListener("mouseenter", open);
      menu.addEventListener("mouseleave", closeSoon);
    }
    wrap.addEventListener("focusin", open);
    wrap.addEventListener("focusout", function (e) {
      if (!wrap.contains(e.relatedTarget)) closeSoon();
    });
    wrap.addEventListener("click", function (e) {
      var logoutBtn = e.target.closest(".drivebit-master-logout");
      if (logoutBtn) {
        e.preventDefault();
        logout();
      }
    });
  }

  function applyMasterName(wrap, name) {
    if (!wrap) return;
    var label = qs(".drivebit-master-name", wrap);
    if (!label) return;
    label.textContent = name || "";
  }

  function ensureMasterButton() {
    var actions = qs(".drivebit-header-desktop-actions");
    if (!actions) return;
    var wrap = qs("[data-db-master]", actions);
    if (!wrap) {
      var legacy = qs("a.drivebit-header-login, .drivebit-header-login", actions);
      wrap = document.createElement("div");
      wrap.className = "drivebit-master-wrap";
      wrap.setAttribute("data-db-master", "");
      if (legacy) {
        legacy.replaceWith(wrap);
      } else {
        actions.appendChild(wrap);
      }
    }
    var loggedIn = isLoggedIn();
    var name = loggedIn ? cachedFirstName() : "";
    wrap.innerHTML = renderMasterTrigger(loggedIn, name) + (loggedIn ? authMenuHtml() : guestMenuHtml());
    wrap.setAttribute("data-db-wired", "0");
    wireMasterWrap(wrap);
    syncAuthChrome();
    if (!loggedIn) {
      setCachedFirstName("");
      return;
    }
    // Token has no name — always load from User API (cache is only a fast first paint)
    fetchUserFirstName().then(function (fn) {
      if (!fn) return;
      setCachedFirstName(fn);
      applyMasterName(wrap, fn);
    });
  }

  function pickFirstName(data) {
    if (!data || typeof data !== "object") return "";
    var direct = (data.firstName || data.first_name || "").trim();
    if (direct) return direct;
    var full = (data.name || data.fullName || data.displayName || "").trim();
    if (full) return full.split(/\s+/)[0];
    return "";
  }

  function fetchUserFirstName() {
    var token = authToken();
    if (!token) return Promise.resolve("");
    // Prefer same-origin /api (webpack proxy → drivebit.ru), then absolute URL
    var urls = ["/api/User", "https://drivebit.ru/api/User"];
    return urls.reduce(function (chain, url) {
      return chain.then(function (prev) {
        if (prev) return prev;
        return fetch(url, {
          headers: {
            Authorization: "Bearer " + token,
            Accept: "application/json",
          },
          credentials: "same-origin",
        })
          .then(function (r) {
            if (!r.ok) throw new Error("user " + r.status);
            return r.json();
          })
          .then(pickFirstName)
          .catch(function () {
            return "";
          });
      });
    }, Promise.resolve(""));
  }

  function ensureDestinations() {
    if (qs("#drivebit-destinations")) return;
    if (!qs("#drivebit-hero-static")) return;
    var city = citySlug();
    var items = [
      { title: "На море", href: "/" + city + "/search", img: "/images/destinations/na-more.jpg" },
      { title: "В Крым", href: "/" + city + "/arenda-avto-v-krym", img: "/images/destinations/v-krym.jpg" },
      { title: "В Беларусь", href: "/" + city + "/arenda-avto-v-belarus", img: "/images/destinations/v-belarus.jpg" },
      { title: "В Абхазию", href: "/" + city + "/arenda-avto-v-abkhaziyu", img: "/images/destinations/v-abkhaziyu.jpg" },
      { title: "На Алтай", href: "/" + city + "/search", img: "/images/destinations/na-altay.jpg" },
      { title: "В ДНР и ЛНР", href: "/" + city + "/search", img: "/images/destinations/v-dnr-lnr.jpg" },
    ];
    var section = document.createElement("section");
    section.id = "drivebit-destinations";
    section.className = "drivebit-destinations";
    section.setAttribute("aria-label", "Куда можно поехать");
    var cards = items
      .map(function (it) {
        return (
          '<a class="drivebit-destination-card" href="' +
          it.href +
          '"><img src="' +
          it.img +
          '" alt=""><span>' +
          it.title +
          "</span></a>"
        );
      })
      .join("");
    section.innerHTML =
      '<div class="drivebit-destinations-inner">' +
      '<h2 class="drivebit-destinations-title">Куда можно поехать на арендованном авто</h2>' +
      '<p class="drivebit-destinations-sub">Арендуйте автомобиль для путешествий по стране и не только</p>' +
      '<div class="drivebit-destinations-grid">' +
      cards +
      "</div></div>";
    var filters = qs("#drivebit-filters-static");
    var heroWrap = qs(".drivebit-hero-wrap");
    var anchor = filters && filters.parentNode ? filters : heroWrap;
    if (anchor && anchor.parentNode) {
      // Insert after hero block (filters may be inside hero)
      var insertAfter = heroWrap || filters;
      if (insertAfter && insertAfter.nextSibling) {
        insertAfter.parentNode.insertBefore(section, insertAfter.nextSibling);
      } else if (insertAfter) {
        insertAfter.parentNode.appendChild(section);
      } else {
        document.body.appendChild(section);
      }
    }
  }

  function ensureStyles() {
    function ensureCss(hrefPart, href) {
      if (qs('link[href*="' + hrefPart + '"]')) return;
      var link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = href;
      document.head.appendChild(link);
    }
    ensureCss("drivebit-fonts.css", "/vendor/drivebit-fonts.css");
    ensureCss("drivebit-design-tokens.css", "/vendor/drivebit-design-tokens.css");
    ensureCss("drivebit-typography.css", "/vendor/drivebit-typography.css");
    ensureCss("drivebit-buttons.css", "/vendor/drivebit-buttons.css");
    ensureCss("drivebit-footer.css", "/vendor/drivebit-footer.css");
    ensureCss("drivebit-bottom-nav.css", "/vendor/drivebit-bottom-nav.css");
    ensureCss("drivebit-advantages.css", "/vendor/drivebit-advantages.css");
    if (qs("#drivebit-hero-static")) {
      ensureCss("drivebit-home-redesign.css", "/vendor/drivebit-home-redesign.css");
      ensureCss("drivebit-home-full.css", "/vendor/drivebit-home-full.css");
    }
  }

  /** Move copyright to the end of the footer (mobile stack + desktop full-width row). */
  function normalizeFooterCopy() {
    var footer = qs(".drivebit-site-footer");
    if (!footer) return;
    var copy =
      footer.querySelector(":scope > .drivebit-footer-copy") ||
      footer.querySelector(".drivebit-footer-brand small") ||
      footer.querySelector("small");
    if (!copy) return;
    copy.classList.add("drivebit-footer-copy");
    if (copy.parentElement !== footer) {
      footer.appendChild(copy);
    } else if (footer.lastElementChild !== copy) {
      footer.appendChild(copy);
    }

    var brandP = qs(".drivebit-footer-brand p", footer);
    if (!brandP) return;
    var marker = "Безопасно, удобно, прибыльно";
    var withDot = marker + ".";
    var tagline = brandP.querySelector(".drivebit-footer-tagline");
    if (tagline) {
      // Repair older wraps that left "." outside the block tagline
      var next = tagline.nextSibling;
      while (next && next.nodeType === 3 && !/\S/.test(next.textContent || "")) {
        next = next.nextSibling;
      }
      if (next && next.nodeType === 3 && /^\s*\./.test(next.textContent || "")) {
        tagline.textContent = (tagline.textContent || "").replace(/\.\s*$/, "") + ".";
        next.textContent = (next.textContent || "").replace(/^\s*\./, "");
        if (!/\S/.test(next.textContent || "")) {
          next.parentNode && next.parentNode.removeChild(next);
        }
      } else if (!/\.\s*$/.test((tagline.textContent || "").trim())) {
        tagline.textContent = (tagline.textContent || "").replace(/\s*$/, "") + ".";
      }
    } else {
      var html = brandP.innerHTML;
      // Keep the trailing period inside the tagline span (mobile tagline is display:block)
      if (html.indexOf(withDot) !== -1) {
        brandP.innerHTML = html.replace(
          withDot,
          '<span class="drivebit-footer-tagline">' + withDot + "</span>"
        );
      } else if (html.indexOf(marker) !== -1) {
        brandP.innerHTML = html.replace(
          marker,
          '<span class="drivebit-footer-tagline">' + withDot + "</span>"
        );
      }
    }
  }

  /** Upgrade legacy phone/bottom footer → Figma 3-column site footer on every page */
  function ensureFooter() {
    var shell = qs(".drivebit-footer-shell");
    if (!shell) {
      shell = document.createElement("div");
      shell.className = "drivebit-footer-shell";
      document.body.appendChild(shell);
    }
    if (!shell.querySelector(".drivebit-footer-col")) {
      shell.innerHTML =
        '<footer class="drivebit-site-footer">' +
        '<div class="drivebit-footer-col drivebit-footer-brand">' +
        '<img class="drivebit-logo-footer" src="' +
        LOGO_FOOTER +
        '" alt="DriveBit">' +
        '<p>Платформа, объединяющая арендаторов и собственников автомобилей в Москве. <span class="drivebit-footer-tagline">Безопасно, удобно, прибыльно.</span></p>' +
        "</div>" +
        '<div class="drivebit-footer-col">' +
        "<h4>Контактная информация</h4>" +
        '<a href="tel:+74958775051">+7 (495) 877-50-51</a>' +
        '<a href="mailto:info@drivebit.ru">info@drivebit.ru</a>' +
        "<span>117461, г. Москва, ул. Каховка, д. 33-1</span>" +
        "</div>" +
        '<div class="drivebit-footer-col">' +
        "<h4>Документы</h4>" +
        '<a href="/privacy">Политика конфиденциальности</a>' +
        '<a href="/offer">Оферта</a>' +
        '<a href="/cookies">Cookies</a>' +
        "</div>" +
        '<small class="drivebit-footer-copy">© 2026 DriveBit. Все права защищены.</small>' +
        "</footer>";
    }
    normalizeFooterCopy();
  }

  function enrichCategoryFilters() {
    // SVG from Drivebit redesign images
    var bust = "?v=svg-redesign1";
    var icons = {
      Поблизости: "/images/chrome/categories/nearby.svg" + bust,
      Эконом: "/images/chrome/categories/econom.svg" + bust,
      Комфорт: "/images/chrome/categories/comfort.svg" + bust,
      Бизнес: "/images/chrome/categories/business.svg" + bust,
      Премиум: "/images/chrome/categories/premium.svg" + bust,
      Минивэн: "/images/chrome/categories/minivan.svg" + bust,
      Внедорожник: "/images/chrome/categories/suv.svg" + bust,
    };
    qsa(".drivebit-filter-btn").forEach(function (btn) {
      var title = btn.getAttribute("data-filter-title") || "";
      if (!icons[title]) return;
      var img = btn.querySelector("img.drivebit-filter-icon");
      if (img) {
        img.src = icons[title];
        return;
      }
      img = document.createElement("img");
      img.className = "drivebit-filter-icon";
      img.src = icons[title];
      img.alt = "";
      btn.insertBefore(img, btn.firstChild);
    });
    // Prefer «Поблизости» as default active on home (Figma)
    var nearby = qs('.drivebit-filter-btn[data-filter-title="Поблизости"]');
    var all = qs('.drivebit-filter-btn[data-filter-title="Все"]');
    if (nearby && all && all.getAttribute("aria-pressed") === "true") {
      all.setAttribute("aria-pressed", "false");
      all.classList.remove("is-selected");
      nearby.setAttribute("aria-pressed", "true");
      nearby.classList.add("is-selected");
    }
  }

  function advantagesSectionHtml() {
    var items = [
      ["cheaper.svg", "Стоит дешевле", "Стоимость аренды от частного владельца при длительных сроках значительно дешевле. Экономия может составлять 30–40%."],
      ["cleaner.svg", "Машины чище", "Авто сдаются от случая к случаю, а не постоянно. Собственник значительно лучше следит за состоянием своей машины."],
      ["mileage.svg", "Пробег меньше", "Автомобили не используются в коммерческих целях на постоянной основе, дальние поездки на таких машинах организуются нечасто."],
      ["choice.svg", "Выбор больше", "Так как сервис объединяет разных собственников, выбор машин значительно расширяется."],
      ["docs.svg", "Оформить проще", "Максимально простое оформление аренды — регистрируетесь в сервисе, выбираете автомобиль, подаёте заявку на аренду."],
      ["shield.svg", "Ответственность меньше", "Мы не зарабатываем на штрафах арендаторов. Нет ущерба — нет ответственности."],
    ];
    var cards = items
      .map(function (it) {
        return (
          '<article class="drivebit-adv-card">' +
          '<div class="drivebit-adv-icon"><img src="/images/chrome/advantages/' +
          it[0] +
          '" alt=""></div>' +
          "<h3>" +
          it[1] +
          "</h3><p>" +
          it[2] +
          "</p></article>"
        );
      })
      .join("");
    return (
      '<section class="drivebit-advantages" aria-label="Преимущества DriveBit" data-db-adv-upgraded="1">' +
      '<div class="drivebit-advantages-inner">' +
      '<h2 class="drivebit-advantages-title">Зачем вот это все?</h2>' +
      '<p class="drivebit-advantages-sub">Преимущества аренды авто от частных владельцев</p>' +
      '<div class="drivebit-advantages-grid">' +
      cards +
      "</div></div></section>"
    );
  }

  /** Upgrade legacy flip-chip promo → home-style advantages grid */
  function enrichAdvantageCards() {
    var why = qs(".drivebit-promo-card--why");
    if (why && !qs(".drivebit-advantages[data-db-adv-upgraded], .drivebit-advantages .drivebit-adv-card")) {
      var wrap = document.createElement("div");
      wrap.innerHTML = advantagesSectionHtml();
      var section = wrap.firstElementChild;
      var host = why.parentElement;
      if (host) {
        host.insertBefore(section, why);
        why.remove();
      } else {
        why.replaceWith(section);
      }
      return;
    }
    var map = {
      "Стоит дешевле": "/images/chrome/advantages/cheaper.svg",
      "Машины чище": "/images/chrome/advantages/cleaner.svg",
      "Пробег меньше": "/images/chrome/advantages/mileage.svg",
      "Выбор больше": "/images/chrome/advantages/choice.svg",
      "Оформить проще": "/images/chrome/advantages/docs.svg",
      "Ответственность меньше": "/images/chrome/advantages/shield.svg",
    };
    qsa(".drivebit-promo-chip").forEach(function (chip) {
      if (chip.querySelector(".drivebit-promo-chip-icon")) return;
      var front = qs(".drivebit-promo-chip-face--front", chip);
      var title = ((front && front.textContent) || "").trim();
      var src = map[title];
      if (!src) return;
      var icon = document.createElement("div");
      icon.className = "drivebit-promo-chip-icon";
      icon.innerHTML = '<img src="' + src + '" alt="">';
      var inner = qs(".drivebit-promo-chip-inner", chip) || chip;
      inner.insertBefore(icon, inner.firstChild);
    });
  }

  function buildSiteMenu() {
    if (qs("#drivebit-site-menu")) return qs("#drivebit-site-menu");
    var el = document.createElement("div");
    el.id = "drivebit-site-menu";
    el.className = "drivebit-site-menu";
    el.setAttribute("hidden", "");
    el.innerHTML =
      '<div class="drivebit-site-menu-header">' +
      '<a class="drivebit-header-logo" href="' + cityHomeHref() + '">' +
      '<img class="turo-logo" src="' + LOGO_WHITE + '" alt="DriveBit" width="120" height="21" data-db-logo="white">' +
      "</a>" +
      '<button type="button" class="drivebit-site-menu-close" aria-label="Закрыть меню">' +
      '<img src="' + ICON + 'icon-close.svg" alt="">' +
      "</button>" +
      "</div>" +
      '<nav class="drivebit-site-menu-links" aria-label="Меню">' +
      '<a href="' + searchHref() + '">Арендовать авто</a>' +
      '<a href="/list-your-car.html">Сдать авто</a>' +
      '<a href="/contacts">Контакты</a>' +
      '<a href="/blog">Блог</a>' +
      '<a class="drivebit-site-menu-city" href="/my-city-selection">' +
      '<img src="' + ICON + 'icon-location.svg" alt="">' +
      "<span data-db-city-label>Москва</span></a>" +
      '<a href="tel:+74958775051">+7 (495) 877-50-51</a>' +
      '<a class="drivebit-site-menu-login" href="/login-by-phone">Войти</a>' +
      "</nav>";
    document.body.appendChild(el);
    qs(".drivebit-site-menu-close", el).addEventListener("click", closeSiteMenu);
    return el;
  }

  function setBurgerIcon(open) {
    var src = ICON + (open ? "icon-close.svg" : "icon-burger.svg");
    // Match both alts — after open we set alt=Close, so alt=Menu alone never resets
    qsa(
      "#drivebit-header-compose img[alt='Menu'], " +
        "#drivebit-header-compose img[alt='Close'], " +
        "[data-db-static-burger] img, " +
        ".drivebit-header-menu-btn img"
    ).forEach(function (img) {
      img.setAttribute("src", src);
      img.setAttribute("alt", open ? "Close" : "Menu");
      img.setAttribute("data-db-burger-icon", open ? "close" : "burger");
    });
  }

  function openSiteMenu() {
    var menu = buildSiteMenu();
    syncAuthChrome();
    menu.classList.add("is-open");
    menu.removeAttribute("hidden");
    document.body.classList.add("drivebit-site-menu-open");
    document.body.style.overflow = "hidden";
    setBurgerIcon(true);
    document.dispatchEvent(new CustomEvent("drivebit:site-menu-opened"));
  }

  function closeSiteMenu() {
    var menu = qs("#drivebit-site-menu");
    if (!menu) return;
    menu.classList.remove("is-open");
    menu.setAttribute("hidden", "");
    document.body.classList.remove("drivebit-site-menu-open");
    document.body.style.overflow = "";
    setBurgerIcon(false);
  }

  function profileSheetHtml() {
    // Mobile compact sheet: Сообщения / Мои авто live in bottom nav — omit here
    return (
      '<div class="drivebit-profile-sheet" role="menu">' +
      '<a href="/profile">Мой профиль</a>' +
      '<a href="/documents">Мои документы</a>' +
      '<a href="/my-bookings">Мои бронирования</a>' +
      '<a href="/my-deals">Мои сделки</a>' +
      '<button type="button" class="drivebit-master-logout">Выйти</button>' +
      "</div>"
    );
  }

  function buildProfileSheet() {
    var el = qs("#drivebit-profile-sheet-overlay");
    if (!el) {
      el = document.createElement("div");
      el.id = "drivebit-profile-sheet-overlay";
      el.className = "drivebit-profile-sheet-overlay";
      el.addEventListener("click", function (e) {
        if (e.target === el) closeProfileSheet();
        var logoutBtn = e.target.closest(".drivebit-master-logout");
        if (logoutBtn) {
          e.preventDefault();
          logout();
        }
      });
      document.body.appendChild(el);
    }
    el.innerHTML = profileSheetHtml();
    return el;
  }

  function openProfileSheet() {
    if (!isLoggedIn()) {
      window.location.href = "/login-by-phone";
      return;
    }
    // Prefer native sheet with the same items as desktop master menu
    var sheet = buildProfileSheet();
    sheet.classList.add("is-open");
  }

  function closeProfileSheet() {
    var sheet = qs("#drivebit-profile-sheet-overlay");
    if (sheet) sheet.classList.remove("is-open");
  }

  function activeTab() {
    var p = window.location.pathname || "";
    if (/^\/chats?(?:\/|$)/i.test(p) || /\/messages/i.test(p)) return "messages";
    if (/\/profile|\/documents|\/account/i.test(p)) return "profile";
    if (/\/my-bookings|\/my-deals|\/rentals/i.test(p)) return "rentals";
    if (/\/my-cars|\/owner/i.test(p)) return "cars";
    if (/\/search|filter/i.test(p)) return "search";
    return "search";
  }

  /** Body classes for chats list/thread (Roboto + mobile app shell on /chat). */
  function syncChatsBodyClasses() {
    var p = window.location.pathname || "";
    var isChatsScope = /^\/chats?(?:\/|$)/i.test(p);
    var isChatThread = /^\/chat(?:\/|$)/i.test(p);
    document.body.classList.toggle("drivebit-chats-scope", isChatsScope);
    document.body.classList.toggle("drivebit-chat-thread", isChatThread);
  }

  function buildBottomNav() {
    if (qs("#drivebit-bottom-nav")) return qs("#drivebit-bottom-nav");
    var tab = activeTab();
    var el = document.createElement("nav");
    el.id = "drivebit-bottom-nav";
    el.className = "drivebit-bottom-nav";
    el.setAttribute("aria-label", "Нижняя навигация");
    el.innerHTML =
      '<div class="drivebit-bottom-nav-bar">' +
      '<div class="drivebit-bottom-nav-group">' +
      '<a class="drivebit-bottom-nav-item' + (tab === "messages" ? " is-active" : "") + '" href="/chats" data-tab="messages">' +
      '<img src="' + ICON + 'icon-message-mobile.svg" alt=""><span>Сообщения</span></a>' +
      '<button type="button" class="drivebit-bottom-nav-item' + (tab === "profile" ? " is-active" : "") + '" data-tab="profile" data-db-profile>' +
      '<img src="' + ICON + 'icon-profile-mobile.svg" alt=""><span>Профиль</span></button>' +
      "</div>" +
      '<div class="drivebit-bottom-nav-group">' +
      '<a class="drivebit-bottom-nav-item' + (tab === "rentals" ? " is-active" : "") + '" href="/my-bookings" data-tab="rentals">' +
      '<img src="' + ICON + 'icon-calendar-mobile.svg" alt=""><span>Аренды</span></a>' +
      '<a class="drivebit-bottom-nav-item' + (tab === "cars" ? " is-active" : "") + '" href="/my-cars" data-tab="cars">' +
      '<img src="' + ICON + 'icon-car-mobile.svg" alt=""><span>Мои авто</span></a>' +
      "</div>" +
      "</div>" +
      '<button type="button" class="drivebit-bottom-nav-fab" data-db-search-fab aria-label="Поиск">' +
      '<img src="' + ICON + 'icon-search.svg" alt="">' +
      "</button>";
    document.body.appendChild(el);
    document.body.classList.add("drivebit-bottom-nav-active");

    var profileBtn = qs("[data-db-profile]", el);
    if (profileBtn) {
      profileBtn.addEventListener("click", function (e) {
        e.preventDefault();
        openProfileSheet();
      });
    }

    var fab = qs("[data-db-search-fab]", el);
    if (fab) {
      fab.addEventListener("click", function () {
        window.location.href = searchHref();
      });
    }
    return el;
  }

  var BLUE_NAV_SECTIONS =
    "#drivebit-hero-static, .drivebit-hero-wrap, .drivebit-hero-static, .drivebit-destinations, .drivebit-blog, .drivebit-footer-shell";

  /**
   * Over navy home sections → solid rectangular toolbar.
   * Over light sections → center cutout for the search FAB.
   */
  function syncNavBarShape() {
    if (!window.matchMedia("(max-width: 768px)").matches) {
      document.body.classList.remove("drivebit-nav-cutout");
      return;
    }
    if (!qs("#drivebit-bottom-nav")) return;
    if (!qs(BLUE_NAV_SECTIONS)) {
      // Non-home / no navy bands: cutout when FAB is shown for guests/authed
      document.body.classList.add("drivebit-nav-cutout");
      return;
    }
    var navH = 79;
    var probeY = (window.innerHeight || 0) - navH - 6;
    var overBlue = false;
    if (probeY >= 0) {
      qsa(BLUE_NAV_SECTIONS).forEach(function (el) {
        if (overBlue) return;
        var r = el.getBoundingClientRect();
        if (r.top < probeY && r.bottom > probeY) overBlue = true;
      });
    }
    document.body.classList.toggle("drivebit-nav-cutout", !overBlue);
  }

  function syncAuthChrome() {
    var loggedIn = isLoggedIn();
    document.body.classList.toggle("drivebit-guest", !loggedIn);
    document.body.classList.toggle("drivebit-authed", loggedIn);

    // Site menu: hide "Войти" when authenticated
    qsa(".drivebit-site-menu-login").forEach(function (el) {
      el.hidden = loggedIn;
      el.style.display = loggedIn ? "none" : "";
    });

    // Any leftover plain "Войти" triggers (not the user chip)
    qsa(".drivebit-header-login, .drivebit-master-btn").forEach(function (el) {
      if (el.classList.contains("drivebit-master-btn--user")) return;
      if (loggedIn && (el.textContent || "").trim() === "Войти") {
        el.style.display = "none";
      } else if (!loggedIn) {
        el.style.display = "";
      }
    });
  }

  function syncSearchFab() {
    var fab = qs("[data-db-search-fab]");
    if (!fab) {
      syncNavBarShape();
      return;
    }
    // Guests: only search in the bottom bar — always show FAB
    if (!isLoggedIn()) {
      document.body.classList.remove("drivebit-has-hero");
      fab.classList.add("is-visible");
      syncNavBarShape();
      return;
    }
    var hero = qs("#drivebit-hero-static, .drivebit-hero-wrap, #drivebit-hero-search-btn");
    if (!hero) {
      document.body.classList.remove("drivebit-has-hero");
      fab.classList.add("is-visible");
      syncNavBarShape();
      return;
    }
    document.body.classList.add("drivebit-has-hero");
    var rect = hero.getBoundingClientRect();
    var leaveViewport = rect.bottom < 80;
    if (leaveViewport) fab.classList.add("is-visible");
    else fab.classList.remove("is-visible");
    syncNavBarShape();
  }

  function wireBurgerCapture() {
    document.addEventListener(
      "click",
      function (e) {
        var target = e.target;
        if (!target || !target.closest) return;
        if (window.matchMedia("(min-width: 769px)").matches) return;

        var burger = target.closest("#drivebit-header-compose");
        if (!burger) return;
        // Compose MenuUserButton / any click in header actions on mobile opens site menu
        if (target.closest("a.drivebit-header-city, [data-db-city]")) return;

        e.preventDefault();
        e.stopPropagation();
        if (typeof e.stopImmediatePropagation === "function") e.stopImmediatePropagation();
        var menu = qs("#drivebit-site-menu");
        if (menu && menu.classList.contains("is-open")) closeSiteMenu();
        else openSiteMenu();
      },
      true
    );

    window.addEventListener("drivebit:toggle-site-menu", function () {
      var menu = qs("#drivebit-site-menu");
      if (menu && menu.classList.contains("is-open")) closeSiteMenu();
      else openSiteMenu();
    });

    window.addEventListener("drivebit:open-profile-menu", function () {
      openProfileSheet();
    });

    document.addEventListener("drivebit:site-menu-opened", function () {
      if (typeof window.drivebitCloseButter === "function") window.drivebitCloseButter();
    });
  }

  function hideLegacyMobileNav() {
    var nav = qs("#drivebit-header-nav-mobile");
    if (nav) {
      nav.setAttribute("hidden", "");
      nav.style.display = "none";
    }
  }

  function ensureStaticBurger() {
    var actions = qs("#drivebit-header-compose");
    if (!actions) return;
    var existing = qs("[data-db-static-burger]", actions);
    if (!window.matchMedia("(max-width: 768px)").matches) {
      if (existing) existing.remove();
      return;
    }
    // Compose MenuUserButton owns the burger — drop chrome duplicate if present
    var composeMenu = actions.querySelector(
      ".drivebit-header-compose-inner button, .universal-button, img[alt='Menu']"
    );
    if (composeMenu) {
      if (existing) existing.remove();
      return;
    }
    if (existing) return;
    var btn = document.createElement("button");
    btn.type = "button";
    btn.className = "drivebit-header-menu-btn";
    btn.setAttribute("data-db-static-burger", "1");
    btn.setAttribute("aria-label", "Меню");
    btn.innerHTML = '<img src="' + ICON + 'icon-burger.svg" alt="Menu" width="32" height="32">';
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      openSiteMenu();
    });
    actions.appendChild(btn);
  }

  function reorderHeroFilters() {
    var heroInner = qs(".drivebit-hero-inner");
    var filters = qs("#drivebit-filters-static");
    var toolbar = qs(".drivebit-hero-toolbar");
    if (!heroInner || !filters || !toolbar) return;
    if (filters.getAttribute("data-db-moved") === "1") return;
    heroInner.insertBefore(filters, toolbar);
    filters.setAttribute("data-db-moved", "1");
    filters.style.padding = "0";
    filters.style.margin = "0";
    filters.style.background = "transparent";
    filters.style.maxWidth = "1012px";
    filters.style.width = "100%";
  }

  function ensureNearbySelected() {
    var track = qs("#drivebit-filters-static .drivebit-filters-track");
    if (!track) return;
    var buttons = qsa(".drivebit-filter-btn", track);
    if (!buttons.length) return;
    var any = false;
    for (var i = 0; i < buttons.length; i++) {
      if (buttons[i].classList.contains("is-selected") || buttons[i].getAttribute("aria-pressed") === "true") {
        any = true;
        break;
      }
    }
    if (any) return;
    buttons[0].classList.add("is-selected");
    buttons[0].setAttribute("aria-pressed", "true");
  }

  function syncCityLabel() {
    var cityLink = qs(".drivebit-header-city");
    var label = qs("[data-db-city-label]");
    if (cityLink && label && cityLink.textContent) {
      label.textContent = cityLink.textContent.trim();
    }
  }

  /**
   * Mobile: replay desktop hover lift when a card sits in the upper ~third of the viewport.
   * Active band ≈ 12%–42% of viewport height; one card per group at a time.
   */
  function initScrollFocusCards() {
    var SELECTOR =
      ".drivebit-adv-card, .drivebit-faq-item, .drivebit-howto-card";
    var mq = window.matchMedia("(max-width: 768px)");
    var ticking = false;
    var lastActive = null;

    function cards() {
      return Array.prototype.slice.call(document.querySelectorAll(SELECTOR));
    }

    function clearAll() {
      cards().forEach(function (el) {
        el.classList.remove("is-scroll-active");
      });
      lastActive = null;
    }

    function update() {
      ticking = false;
      if (!mq.matches) {
        clearAll();
        return;
      }
      var vh = window.innerHeight || document.documentElement.clientHeight;
      var bandTop = vh * 0.12;
      var bandBottom = vh * 0.42;
      var bandMid = (bandTop + bandBottom) / 2;
      var best = null;
      var bestDist = Infinity;

      cards().forEach(function (el) {
        var rect = el.getBoundingClientRect();
        if (rect.height < 8) return;
        var mid = rect.top + rect.height / 2;
        if (mid < bandTop || mid > bandBottom) return;
        var dist = Math.abs(mid - bandMid);
        if (dist < bestDist) {
          bestDist = dist;
          best = el;
        }
      });

      if (best === lastActive) return;
      cards().forEach(function (el) {
        el.classList.toggle("is-scroll-active", el === best);
      });
      lastActive = best;
    }

    function onScrollOrResize() {
      if (ticking) return;
      ticking = true;
      window.requestAnimationFrame(update);
    }

    window.addEventListener("scroll", onScrollOrResize, { passive: true });
    window.addEventListener("resize", onScrollOrResize, { passive: true });
    if (typeof mq.addEventListener === "function") {
      mq.addEventListener("change", onScrollOrResize);
    } else if (typeof mq.addListener === "function") {
      mq.addListener(onScrollOrResize);
    }
    update();
    setTimeout(update, 400);
  }

  function init() {
    // Home-only body class — do not apply redesign home layout to contacts/account/etc.
    if (qs("#drivebit-hero-static")) {
      document.body.classList.add("drivebit-home-1440");
      document.body.classList.add("drivebit-has-hero");
    }
    document.body.classList.add("drivebit-chrome-ready");
    syncChatsBodyClasses();
    ensureStyles();
    ensureFooter();
    swapLogo();
    patchDesktopNav();
    patchHeroDateLabels();
    hideLegacyMobileNav();
    ensureDesktopHeaderActions();
    ensureMasterButton();
    ensureStaticBurger();
    reorderHeroFilters();
    enrichCategoryFilters();
    setTimeout(ensureMasterButton, 600);
    ensureDestinations();
    ensureCarsSectionTitle();
    enrichAdvantageCards();
    buildBottomNav();
    buildSiteMenu();
    wireBurgerCapture();
    syncAuthChrome();
    syncSearchFab();
    syncCityLabel();
    ensureNearbySelected();
    initScrollFocusCards();
    setTimeout(ensureNearbySelected, 400);
    setTimeout(patchHeroDateLabels, 800);
    setTimeout(patchHeroDateLabels, 2000);
    setTimeout(ensureFooter, 500);
    window.addEventListener("scroll", syncSearchFab, { passive: true });
    window.addEventListener("resize", function () {
      syncSearchFab();
      swapLogo();
      reorderHeroFilters();
    });
    window.addEventListener("popstate", syncChatsBodyClasses);
    setTimeout(swapLogo, 500);
    setTimeout(ensureStaticBurger, 300);
    setTimeout(ensureStaticBurger, 1200);
    setTimeout(ensureStaticBurger, 2500);
    setTimeout(syncCityLabel, 800);
    setTimeout(ensureCarsSectionTitle, 1200);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
