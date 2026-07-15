(function () {
    var CITY_SLUGS = {
        "kaliningrad": "Калининград",
        "krasnogorsk": "Красногорск",
        "lyubertsy": "Люберцы",
        "avtodoroga-moskva-astrahany-selo-sheremetevo": "Автодорога Москва-Астрахань (село Шереметьево)",
        "zelenograd": "Зеленоград",
        "moskva": "Москва",
        "rostov-na-donu": "Ростов-на-Дону",
        "rostovka": "Ростовка",
        "ufa": "Уфа",
        "ekaterinburg": "Екатеринбург",
        "mineralnye-vody": "Минеральные Воды",
        "serpuhov": "Серпухов"
    };
    var CITY_FORMS = {
        "Москва": [
            "Москве",
            "Москвы"
        ],
        "Красногорск": [
            "Красногорске",
            "Красногорска"
        ],
        "Зеленоград": [
            "Зеленограде",
            "Зеленограда"
        ],
        "Люберцы": [
            "Люберцах",
            "Люберец"
        ],
        "Минеральные Воды": [
            "Минеральных Водах",
            "Минеральных Вод"
        ]
    };
    var FILTER_SLUGS = {
        "poblizosti": "Поблизости",
        "arenda-avto-v-krym": "В Крым",
        "arenda-avto-v-belarus": "Беларусь",
        "arenda-avto-v-abkhaziyu": "Абхазия",
        "arenda-vnedorozhnika-bez-voditelya": "Внедорожник",
        "arenda-minivena-bez-voditelya": "Минивэн",
        "arenda-avto-ekonom-klassa-bez-voditelya": "Эконом",
        "arenda-avto-komfort-klassa-bez-voditelya": "Комфорт",
        "arenda-avto-premium-klassa-bez-voditelya": "Премиум"
    };
    var GENITIVE_FILTERS = ["В Крым", "Беларусь", "Абхазия"];
    var SEO_OPTIMIZED_MOSKVA_FILTER_SLUGS = ["arenda-avto-v-krym", "arenda-avto-v-belarus", "arenda-avto-v-abkhaziyu", "poblizosti", "arenda-vnedorozhnika-bez-voditelya", "arenda-minivena-bez-voditelya", "arenda-avto-ekonom-klassa-bez-voditelya", "arenda-avto-komfort-klassa-bez-voditelya", "arenda-avto-premium-klassa-bez-voditelya"];
    var SEO_PAGE_TITLE_SUFFIX = " через сервис DriveBit";
    var SEO_PAGE_DESCRIPTION_SUFFIX = ". Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!";
    var RESERVED = ["create-car", "city-selection", "my-city-selection", "list-your-car", "verify-otp", "login-by-phone", "login-by-mail", "login-by-password", "signup", "profile", "my-cars", "my-bookings", "leave-review", "my-deals", "chats", "chat", "documents", "offer", "contacts", "privacy", "cookies", "payment", "payment-success", "payment-failure", "download-booking-contract", "car-edit", "car-sts-upload", "car-photos-gallery", "car-photos-upload", "car-photos", "edit-name", "change-email", "change-phone", "change-password", "address-input", "license-plate-input", "car-brand-selection", "car-model-selection", "body-type-selection", "drive-type-selection", "engine-type-selection", "engine-volume-input", "production-year-input", "seats-count-input", "trunk-size-selection", "daily-rate-input", "description-input", "passport-upload", "search", "car-detail"];
    var CITY_FILTER_RENT_SUFFIX = "Аренда у собственников, прозрачные условия и поддержка 24/7.";
    var DEFAULT_CITY_PAGE_DESCRIPTION = "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7.";

    function cityForms(name) {
        var key = (name || "").trim();
        if (CITY_FORMS[key]) return CITY_FORMS[key];
        if (!key) return ["", ""];
        var last = key.slice(-1);
        var stem;
        if (key.slice(-2) === "ия") {
            stem = key.slice(0, -2);
            return [stem + "ии", stem + "ии"];
        }
        if (last === "а") {
            stem = key.slice(0, -1);
            return [stem + "е", stem + "ы"];
        }
        if (last === "я") {
            stem = key.slice(0, -1);
            return [stem + "е", stem + "и"];
        }
        if (last === "ь") {
            stem = key.slice(0, -1);
            return [stem + "и", stem + "и"];
        }
        if (last === "й") {
            stem = key.slice(0, -1);
            return [stem + "е", stem + "я"];
        }
        return [key + "е", key.slice(0, -1) + "а"];
    }

    function filterTitleFromSlug(filterSlug) {
        if (!filterSlug) return "Все";
        return FILTER_SLUGS[filterSlug.toLowerCase()] || "Все";
    }

    function isSeoOptimizedPage(citySlug, filterSlug) {
        var city = (citySlug || "").trim().toLowerCase();
        var filter = filterSlug ? filterSlug.trim().toLowerCase() : null;
        if (city === "moskva" && !filter) return true;
        if (city === "moskva" && SEO_OPTIMIZED_MOSKVA_FILTER_SLUGS.indexOf(filter) >= 0) return true;
        if (city === "krasnogorsk" && !filter) return true;
        return false;
    }

    function seoPageHeadline(cityName, filterTitle) {
        var trimmedCity = (cityName || "").trim();
        if (!trimmedCity) return "Аренда авто у частных владельцев";
        var forms = cityForms(trimmedCity);
        var prep = forms[0];
        var gen = forms[1];
        switch (filterTitle) {
            case "В Крым":
                return "Аренда авто для поездки в Крым из " + gen;
            case "Беларусь":
                return "Аренда авто для поездки в Беларусь из " + gen;
            case "Абхазия":
                return "Аренда авто для поездки в Абхазию из " + gen;
            case "Поблизости":
                return "Аренда авто на карте в " + prep;
            case "Внедорожник":
                return "Аренда внедорожника без водителя в " + prep;
            case "Минивэн":
                return "Аренда минивэна без водителя в " + prep;
            case "Эконом":
                return "Аренда автомобиля эконом-класса без водителя в " + prep;
            case "Комфорт":
                return "Аренда авто комфорт-класса без водителя в " + prep;
            case "Премиум":
                return "Аренда автомобиля премиум-класса без водителя в " + prep;
            default:
                return "Аренда авто у частных владельцев в " + prep;
        }
    }

    function seoPageTitle(cityName, filterTitle) {
        var headline = seoPageHeadline(cityName, filterTitle);
        if (filterTitle === "Поблизости") {
            var prep = cityForms((cityName || "").trim())[0];
            return "Аренда авто на карте в " + prep + " - аренда автомобиля поблизости" + SEO_PAGE_TITLE_SUFFIX;
        }
        if (filterTitle === "Комфорт") {
            var comfortPrep = cityForms((cityName || "").trim())[0];
            return "Аренда автомобиля комфорт-класса в " + comfortPrep + SEO_PAGE_TITLE_SUFFIX;
        }
        return headline + SEO_PAGE_TITLE_SUFFIX;
    }

    function seoPageDescription(cityName, filterTitle) {
        return seoPageTitle(cityName, filterTitle) + SEO_PAGE_DESCRIPTION_SUFFIX;
    }

    function cityPageDescription(cityName, filterTitle) {
        var trimmedCity = (cityName || "").trim();
        if (!trimmedCity) return DEFAULT_CITY_PAGE_DESCRIPTION;
        var forms = cityForms(trimmedCity);
        var prep = forms[0];
        var gen = forms[1];
        switch (filterTitle) {
            case "Беларусь":
                return "Подберите автомобиль для поездки в Беларусь из " + gen + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Абхазия":
                return "Подберите автомобиль для поездки в Абхазию из " + gen + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Эконом":
                return "Подберите автомобиль эконом-класса в аренду в " + prep + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Комфорт":
                return "Подберите автомобиль комфорт-класса в аренду в " + prep + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Премиум":
                return "Подберите автомобиль премиум-класса в аренду в " + prep + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Внедорожник":
                return "Подберите внедорожник в аренду в " + prep + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Минивэн":
                return "Подберите минивэн в аренду в " + prep + ". " + CITY_FILTER_RENT_SUFFIX;
            case "Поблизости":
                return "Найдите автомобили поблизости в " + prep + ". Быстрая аренда у собственников, прозрачные условия и поддержка 24/7.";
            case "В Крым":
                return "Подберите автомобиль для поездки в Крым из " + gen + ". " + CITY_FILTER_RENT_SUFFIX;
            default:
                return "Аренда авто в " + prep + " у собственников. Дешевле проката, полная страховка и поддержка 24/7.";
        }
    }

    function heroBannerHeadline(cityName, filterTitle) {
        var base = "Арендуй авто у частных владельцев";
        var trimmedCity = (cityName || "").trim();
        if (!trimmedCity) return base;
        var forms = cityForms(trimmedCity);
        var phrase = GENITIVE_FILTERS.indexOf(filterTitle) >= 0
            ? "из " + forms[1]
            : "в " + forms[0];
        return base + " " + phrase;
    }

    function pageHeadline(citySlug, filterSlug, cityName, filterTitle) {
        if (isSeoOptimizedPage(citySlug, filterSlug)) {
            return seoPageHeadline(cityName, filterTitle);
        }
        return heroBannerHeadline(cityName, filterTitle);
    }

    function pageTitle(citySlug, filterSlug, cityName, filterTitle) {
        if (isSeoOptimizedPage(citySlug, filterSlug)) {
            return seoPageTitle(cityName, filterTitle);
        }
        return heroBannerHeadline(cityName, filterTitle) + " - DriveBit";
    }

    function pageDescription(citySlug, filterSlug, cityName, filterTitle) {
        if (isSeoOptimizedPage(citySlug, filterSlug)) {
            return seoPageDescription(cityName, filterTitle);
        }
        return cityPageDescription(cityName, filterTitle);
    }

    function parseCityPath(pathname) {
        var trimmed = (pathname || "").replace(/\/$/, "").replace(/^\//, "");
        if (!trimmed) return null;
        var segments = trimmed.split("/").filter(Boolean);
        if (segments.length !== 1 && segments.length !== 2) return null;
        var citySlug = segments[0].toLowerCase();
        if (RESERVED.indexOf(citySlug) >= 0) return null;
        return {
            citySlug: citySlug,
            filterSlug: segments[1] ? segments[1].toLowerCase() : null
        };
    }

    window.drivebitCityPageMeta = function (pathname) {
        var cityPath = parseCityPath(pathname);
        if (!cityPath) return null;
        var cityName = CITY_SLUGS[cityPath.citySlug];
        if (!cityName) return null;
        var filterTitle = filterTitleFromSlug(cityPath.filterSlug);
        var path = "/" + cityPath.citySlug + (cityPath.filterSlug ? "/" + cityPath.filterSlug : "");
        var cityNamePrep = cityForms(cityName)[0];
        return {
            title: pageTitle(cityPath.citySlug, cityPath.filterSlug, cityName, filterTitle),
            description: pageDescription(cityPath.citySlug, cityPath.filterSlug, cityName, filterTitle),
            headline: pageHeadline(cityPath.citySlug, cityPath.filterSlug, cityName, filterTitle),
            cityNamePrep: cityNamePrep,
            path: path
        };
    };

    window.drivebitCitySlugs = Object.keys(CITY_SLUGS);
})();
