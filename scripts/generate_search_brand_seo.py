#!/usr/bin/env python3
"""Build /search/{brand} static SEO pages and landing-blocks.json entries."""

from __future__ import annotations

import html
import json
import os
import subprocess
import sys
import urllib.parse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BLOCKS_FILE = ROOT / "composeApp" / "seo" / "landing-blocks.json"
RESOURCES = ROOT / "composeApp" / "src" / "jsMain" / "resources"
API_BASE = os.environ.get("DRIVEBIT_API_BASE", "https://drivebit.ru/api").rstrip("/")
SITE_BASE = "https://drivebit.ru"
PAGE_SIZE = 12
MOSCOW_NAME = "Москва"
MOSCOW_CITY_ID_FALLBACK = "158835"

GENERATED_START = "<!-- drivebit-seo-generated-start -->"
GENERATED_END = "<!-- drivebit-seo-generated-end -->"
JSON_LD_MARKER = "<!-- drivebit-seo-jsonld -->"
MAIN_PROMO_FRAGMENT = (ROOT / "composeApp" / "seo" / "main-promo.fragment.html").read_text(
    encoding="utf-8"
)

TRANSLIT = {
    "а": "a", "б": "b", "в": "v", "г": "g", "д": "d", "е": "e", "ё": "yo",
    "ж": "zh", "з": "z", "и": "i", "й": "y", "к": "k", "л": "l", "м": "m",
    "н": "n", "о": "o", "п": "p", "р": "r", "с": "s", "т": "t", "у": "u",
    "ф": "f", "х": "h", "ц": "ts", "ч": "ch", "ш": "sh", "щ": "sch",
    "ъ": "", "ы": "y", "ь": "", "э": "e", "ю": "yu", "я": "ya",
}


def name_to_slug(name: str) -> str:
    buf: list[str] = []
    for ch in name.strip().lower():
        if ch.isspace() or ch in "-_.":
            buf.append(" ")
        elif ch in "()":
            buf.append(" ")
        elif ch.isascii() and ch.isalnum():
            buf.append(ch)
        elif ch in TRANSLIT:
            buf.append(TRANSLIT[ch])
        elif ch.isascii():
            buf.append(ch)
    segments = []
    for word in "".join(buf).split():
        cleaned = "".join(c for c in word if c.isalnum())
        if cleaned:
            segments.append(cleaned)
    slug = "-".join(segments).lower().strip("-")
    return slug or "brand"


def http_get_json(url: str, retries: int = 3) -> object:
    last_error: Exception | None = None
    for attempt in range(retries):
        try:
            out = subprocess.check_output(
                [
                    "curl",
                    "-sS",
                    "--max-time",
                    "20",
                    "-H",
                    "User-Agent: DrivebitSearchBrandSeo/1.0",
                    url,
                ],
                text=True,
            )
            return json.loads(out)
        except (subprocess.CalledProcessError, json.JSONDecodeError) as exc:
            last_error = exc
    raise RuntimeError(f"Failed to fetch {url}") from last_error


def append_query(url: str, params: list[tuple[str, str]]) -> str:
    if not params:
        return url
    sep = "&" if "?" in url else "?"
    return url + sep + urllib.parse.urlencode(params)


def sanitize_photo_url(raw: str | None) -> str:
    if not raw:
        return f"{SITE_BASE}/images/logos/logo.png"
    if raw.startswith("/publicbct/"):
        return f"{SITE_BASE}{raw}"
    if "/publicbct/" in raw:
        return f"{SITE_BASE}/publicbct/{raw.split('/publicbct/', 1)[1]}"
    return f"{SITE_BASE}/publicbct/{raw.lstrip('/')}"


def min_daily_price(car: dict) -> int | None:
    rates = [
        car.get("dailyRate"),
        car.get("dailyRate4Days"),
        car.get("dailyRate7Days"),
        car.get("dailyRate14Days"),
        car.get("dailyRate21Days"),
    ]
    vals = [int(r) for r in rates if isinstance(r, (int, float)) and r > 0]
    return min(vals) if vals else None


def find_moscow_city_id(cities: list[dict]) -> str:
    for city in cities:
        if (city.get("name") or "").strip() == MOSCOW_NAME:
            return str(city["id"])
    raise RuntimeError("Moscow city id not found")


def build_seo_block(brand_name: str, slug: str) -> dict:
    brand_lower = brand_name.lower()
    title = f"Аренда {brand_name} в Москве без водителя - DriveBit"
    description = (
        f"Аренда {brand_name} в Москве у собственников на DriveBit. "
        f"Сравните {brand_lower} по цене, выберите даты и забронируйте онлайн. "
        "Дешевле проката, полная страховка, поддержка 24/7."
    )
    return {
        "ariaLabel": f"Аренда {brand_name} в Москве",
        "h2": f"Аренда {brand_name} в Москве без водителя",
        "title": title,
        "description": description,
        "navLabel": brand_name,
        "brandId": None,
        "brandName": brand_name,
        "brandSlug": slug,
        "paragraphs": [
            (
                f"На DriveBit можно взять {brand_name} в аренду в Москве напрямую у владельцев: "
                f"аренда {brand_lower} в москве, аренда {brand_lower} без водителя, "
                f"посуточная аренда {brand_lower} и аренда автомобиля {brand_lower} недорого."
            ),
            (
                f"Сравните доступные {brand_name} по цене и комплектации, укажите даты поездки "
                "и забронируйте подходящий автомобиль онлайн без переплаты классическому прокату."
            ),
        ],
    }


def fetch_cars(city_id: str, brand_id: int) -> list[dict]:
    url = append_query(
        f"{API_BASE}/Car/list/filtered/{city_id}",
        [("page", "1"), ("pageSize", str(PAGE_SIZE)), ("BrandId", str(brand_id))],
    )
    data = http_get_json(url)
    if not isinstance(data, dict):
        raise RuntimeError(f"Unexpected response from {url}")
    return data.get("items") or []


def render_nav(paths: list[tuple[str, str]], current_path: str) -> str:
    lines = [
        '    <nav class="drivebit-seo-nav" aria-label="Аренда по маркам автомобилей">',
        "        <ul>",
    ]
    for path, label in paths:
        active = ' aria-current="page"' if path == current_path else ""
        lines.append(f'            <li><a href="{path}"{active}>{html.escape(label)}</a></li>')
    lines.extend(["        </ul>", "    </nav>", ""])
    return "\n".join(lines)


def render_car_card(car: dict) -> str:
    general = car.get("general") or {}
    brand = general.get("brandName") or ""
    model = general.get("modelName") or ""
    year = general.get("year") or car.get("year")
    title = " ".join(p for p in (brand, model) if p).strip() or "Автомобиль"
    price = min_daily_price(car)
    price_text = f"{price}₽ / сутки" if price else "Цена по запросу"
    car_id = car.get("id") or ""
    detail_url = f"{SITE_BASE}/car-detail?id={urllib.parse.quote(str(car_id))}"
    photos = general.get("photos") or car.get("photos") or []
    photo_url = sanitize_photo_url((photos[0] or {}).get("url") if photos else None)
    city = (general.get("address") or {}).get("city") or "Москва"
    alt = html.escape(f"{title} {year or ''}".strip())
    return f"""        <article class="drivebit-seo-car-card">
            <a class="drivebit-seo-car-link" href="{html.escape(detail_url)}">
                <img class="drivebit-seo-car-image" src="{html.escape(photo_url)}" alt="{alt}" loading="lazy" width="320" height="200">
                <h3 class="drivebit-seo-car-title">{html.escape(title)}</h3>
                <p class="drivebit-seo-car-meta">{html.escape(str(year) if year else "")} · {html.escape(city)}</p>
                <p class="drivebit-seo-car-price">{html.escape(price_text)}</p>
            </a>
        </article>"""


def render_cars_shell(cars: list[dict], heading: str) -> str:
    cards = "\n".join(render_car_card(car) for car in cars)
    return f"""    <div class="drivebit-seo-cars">
        <h2 class="drivebit-seo-cars-heading">{html.escape(heading)}</h2>
        <div class="drivebit-seo-cars-grid">
{cards}
        </div>
    </div>
"""


def build_json_ld(path: str, block: dict, cars: list[dict]) -> dict:
    page_url = f"{SITE_BASE}{path}"
    brand_name = block["brandName"]
    items = []
    for idx, car in enumerate(cars, start=1):
        general = car.get("general") or {}
        name = " ".join(
            p for p in (general.get("brandName"), general.get("modelName")) if p
        ).strip() or "Автомобиль"
        car_id = car.get("id") or ""
        price = min_daily_price(car)
        photos = general.get("photos") or []
        image = sanitize_photo_url((photos[0] or {}).get("url") if photos else None)
        items.append(
            {
                "@type": "ListItem",
                "position": idx,
                "item": {
                    "@type": "Product",
                    "name": name,
                    "url": f"{SITE_BASE}/car-detail?id={car_id}",
                    "image": image,
                    "offers": {
                        "@type": "Offer",
                        "priceCurrency": "RUB",
                        "price": price or 0,
                        "availability": "https://schema.org/InStock",
                        "url": f"{SITE_BASE}/car-detail?id={car_id}",
                    },
                },
            }
        )
    return {
        "@context": "https://schema.org",
        "@graph": [
            {
                "@type": "BreadcrumbList",
                "itemListElement": [
                    {"@type": "ListItem", "position": 1, "name": "DriveBit", "item": f"{SITE_BASE}/"},
                    {"@type": "ListItem", "position": 2, "name": "Поиск", "item": f"{SITE_BASE}/search"},
                    {"@type": "ListItem", "position": 3, "name": brand_name, "item": page_url},
                ],
            },
            {
                "@type": "AutoRental",
                "name": f"DriveBit — аренда {brand_name}",
                "url": page_url,
                "areaServed": "Москва",
                "provider": {"@type": "Organization", "name": "DriveBit", "url": SITE_BASE},
            },
            {
                "@type": "ItemList",
                "name": block["h2"],
                "numberOfItems": len(items),
                "itemListElement": items,
            },
        ],
    }


def render_page_html(path: str, block: dict, cars: list[dict], nav_html: str, json_ld: dict) -> str:
    page_url = f"{SITE_BASE}{path}"
    title = html.escape(block["title"])
    description = html.escape(block["description"])
    aria = html.escape(block["ariaLabel"])
    h2 = html.escape(block["h2"])
    paragraphs = "".join(f"            <p>{html.escape(p)}</p>\n" for p in block["paragraphs"])
    cars_html = render_cars_shell(cars, f"Автомобили {block['brandName']}")
    json_ld_text = json.dumps(json_ld, ensure_ascii=False, indent=2)
    return f"""<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <meta name="description" content="{description}">
    <meta name="robots" content="noindex, follow">
    <meta name="yandex-verification" content="c19b1a1a78585234" />
    <link rel="canonical" href="{page_url}">

    <meta property="og:type" content="website">
    <meta property="og:title" content="{title}">
    <meta property="og:description" content="{description}">
    <meta property="og:url" content="{page_url}">
    <meta property="og:image" content="{SITE_BASE}/images/logos/logo.png">
    <meta property="og:locale" content="ru_RU">
    <meta property="og:site_name" content="DriveBit">

    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:title" content="{title}">
    <meta name="twitter:description" content="{description}">
    <meta name="twitter:image" content="{SITE_BASE}/images/logos/logo.png">
    <meta name="twitter:url" content="{page_url}">

    <link rel="icon" type="image/svg+xml" href="/images/logos/turo_logo.svg">
    <link rel="alternate icon" href="/images/logos/turo_logo.svg">
    <link rel="apple-touch-icon" href="/images/logos/turo_logo.svg">
    <link rel="preload" href="/searchApp.js?v=4" as="script">
    <link rel="stylesheet" href="/vendor/drivebit-static-layout.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-footer.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-seo-text.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-main-promo.css"/>
    {JSON_LD_MARKER}
    <script type="application/ld+json">
{json_ld_text}
    </script>
</head>
<body>
    <div id="root"></div>

{GENERATED_START}
{nav_html}{cars_html}{GENERATED_END}
{MAIN_PROMO_FRAGMENT}
    <div class="drivebit-seo-shell">
        <section class="drivebit-seo-text" aria-label="{aria}">
            <h2>{h2}</h2>
{paragraphs}        </section>
    </div>

<div class="drivebit-footer-shell">
        <footer class="drivebit-site-footer">
            <a class="drivebit-footer-phone" id="drivebit-footer-phone" href="tel:+74958775051">+7(495) 877-50-51</a>
            <div class="drivebit-footer-bottom">
                <span class="drivebit-footer-copy">© DriveBit Аренда автомобилей от собственников</span>
                <a class="drivebit-footer-link drivebit-footer-link--offer" href="/offer">Оферта</a>
                <a class="drivebit-footer-link drivebit-footer-link--cookies" href="/cookies">Cookies</a>
                <a class="drivebit-footer-link drivebit-footer-link--privacy" href="/privacy">Политика конфиденциальности</a>
            </div>
        </footer>
    </div>

    <script type="application/javascript" src="/searchApp.js?v=4"></script>
</body>
</html>
"""


def main() -> int:
    blocks = json.loads(BLOCKS_FILE.read_text(encoding="utf-8"))
    blocks = {k: v for k, v in blocks.items() if not k.startswith("/search/")}

    blocks["/search"] = {
        "ariaLabel": "Поиск автомобилей DriveBit",
        "h2": "Поиск автомобилей для аренды в Москве",
        "title": "Поиск автомобилей - DriveBit",
        "description": (
            "Расширенный поиск аренды автомобилей в Москве по марке, цене, типу кузова и приводу. "
            "Сравните предложения собственников на DriveBit."
        ),
        "navLabel": "Все марки",
        "paragraphs": [
            "Используйте поиск DriveBit, чтобы подобрать автомобиль по марке, цене, типу кузова, приводу и количеству мест.",
            "Выберите марку в каталоге или откройте готовую подборку — например, аренда BMW, Toyota, Skoda и других брендов в Москве.",
        ],
    }

    try:
        cities = http_get_json(f"{API_BASE}/Dictionary/cities/all")
        city_id = find_moscow_city_id(cities)
    except RuntimeError:
        city_id = MOSCOW_CITY_ID_FALLBACK
        print(f"WARN using fallback Moscow city id {city_id}", file=sys.stderr)
    brands = http_get_json(f"{API_BASE}/Dictionary/cars/brands/existing")
    if not isinstance(brands, list):
        raise RuntimeError("brands/existing returned non-list")

    published: list[tuple[str, str, dict, list[dict]]] = []
    errors: list[str] = []

    for brand in brands:
        brand_id = int(brand["id"])
        brand_name = str(brand["name"])
        slug = name_to_slug(brand_name)
        path = f"/search/{slug}"

        try:
            cars = fetch_cars(city_id, brand_id)
        except Exception as exc:
            errors.append(f"{path}: {exc}")
            continue

        if not cars:
            continue

        block = build_seo_block(brand_name, slug)
        block["brandId"] = brand_id
        blocks[path] = block
        published.append((path, brand_name, block, cars))

    nav_paths = [("/search", "Все марки")] + [
        (path, name) for path, name, _, _ in sorted(published, key=lambda item: item[1].lower())
    ]

    for path, brand_name, block, cars in published:
        nav_html = render_nav(nav_paths, path)
        json_ld = build_json_ld(path, block, cars)
        html_text = render_page_html(path, block, cars, nav_html, json_ld)
        slug = path.removeprefix("/search/")
        out_dir = RESOURCES / "search" / slug
        out_dir.mkdir(parents=True, exist_ok=True)
        (out_dir / "index.html").write_text(html_text, encoding="utf-8")
        print(f"OK {path} ({len(cars)} cars)")

    BLOCKS_FILE.write_text(
        json.dumps(blocks, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    if errors:
        for err in errors:
            print(f"WARN {err}", file=sys.stderr)

    if not published:
        print("ERROR: no brand pages generated", file=sys.stderr)
        return 1

    print(f"Published {len(published)} brand search pages")
    return 0


if __name__ == "__main__":
    sys.exit(main())
