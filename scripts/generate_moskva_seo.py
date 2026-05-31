#!/usr/bin/env python3
"""Inject car listings, nav links, and JSON-LD into /moskva/* static index.html files."""

from __future__ import annotations

import html
import json
import os
import re
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
MOSCOW_GEO = (55.7558, 37.6173)
NEARBY_RADIUS_KM = 50

GENERATED_START = "<!-- drivebit-seo-generated-start -->"
COMPOSE_APP_PRELOAD = '    <link rel="preload" href="/composeApp.js?v=4" as="script">\n'
COMPOSE_APP_SCRIPT = (
    '    <script type="application/javascript" src="/composeApp.js?v=4"></script>\n'
)
NEARBY_APP_PRELOAD = '    <link rel="preload" href="/nearbyApp.js?v=4" as="script">\n'
NEARBY_APP_SCRIPT = (
    '    <script type="application/javascript" src="/nearbyApp.js?v=4"></script>\n'
)
LEAFLET_CSS = '    <link rel="stylesheet" href="/vendor/leaflet/leaflet.css"/>\n'
LEAFLET_SCRIPT = '    <script src="/vendor/leaflet/leaflet.js"></script>\n'
MAIN_PROMO_FRAGMENT = (ROOT / "composeApp" / "seo" / "main-promo.fragment.html").read_text(
    encoding="utf-8"
)
PROMO_CSS = '    <link rel="stylesheet" href="/vendor/drivebit-main-promo.css"/>'
GENERATED_END = "<!-- drivebit-seo-generated-end -->"
JSON_LD_MARKER = "<!-- drivebit-seo-jsonld -->"

PATH_TO_HTML: dict[str, Path] = {
    "/moskva": RESOURCES / "moskva" / "index.html",
    "/moskva/k-rodnym": RESOURCES / "moskva" / "k-rodnym" / "index.html",
    "/moskva/kanikuly": RESOURCES / "moskva" / "kanikuly" / "index.html",
    "/moskva/komandirovki": RESOURCES / "moskva" / "komandirovki" / "index.html",
    "/moskva/meropriyatie": RESOURCES / "moskva" / "meropriyatie" / "index.html",
    "/moskva/pereezd": RESOURCES / "moskva" / "pereezd" / "index.html",
    "/moskva/poblizosti": RESOURCES / "moskva" / "poblizosti" / "index.html",
    "/moskva/puteshestviya": RESOURCES / "moskva" / "puteshestviya" / "index.html",
    "/moskva/za-gorod": RESOURCES / "moskva" / "za-gorod" / "index.html",
}

MOSKVA_PATHS = [p for p in PATH_TO_HTML if p.startswith("/moskva")]


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
                    "User-Agent: DrivebitSeoGenerator/1.0",
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
        path = raw.split("/publicbct/", 1)[1]
        return f"{SITE_BASE}/publicbct/{path}"
    if raw.startswith("http://") or raw.startswith("https://"):
        return sanitize_photo_url(raw.split("://", 1)[-1].split("/", 1)[-1] if "/publicbct/" in raw else raw)
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
        name = (city.get("name") or "").strip()
        region = (city.get("regionName") or "").strip()
        if name == "Москва" and region in ("Москва", "г. Москва", ""):
            return str(city["id"])
    for city in cities:
        if (city.get("name") or "").strip() == "Москва":
            return str(city["id"])
    raise RuntimeError("Moscow city id not found in Dictionary/cities/all")


def filter_by_short_name(suggested: list[dict], short_name: str) -> dict | None:
    for item in suggested:
        if item.get("shortName") == short_name:
            return item
    return None


def build_search_params(
    filter_short: str | None,
    nearby: bool,
    suggested: list[dict],
) -> list[tuple[str, str]]:
    params: list[tuple[str, str]] = [
        ("page", "1"),
        ("pageSize", str(PAGE_SIZE)),
    ]
    if nearby:
        params.extend(
            [
                ("GeoLat", str(MOSCOW_GEO[0])),
                ("GeoLon", str(MOSCOW_GEO[1])),
                ("RadiusKm", str(NEARBY_RADIUS_KM)),
            ]
        )
        return params

    if not filter_short:
        return params

    filt = filter_by_short_name(suggested, filter_short)
    if filt is None and filter_short != "Поблизости":
        raise RuntimeError(f"Filter shortName not found in API: {filter_short!r}")

    if filt:
        if filt.get("availableMileagePerDayKmMin") is not None:
            params.append(("AvailableMileagePerDayKmMin", str(filt["availableMileagePerDayKmMin"])))
        if filt.get("dailyPriceMin") is not None:
            params.append(("DailyRateMin", str(filt["dailyPriceMin"])))
        if filt.get("dailyPriceMax") is not None:
            params.append(("DailyRateMax", str(filt["dailyPriceMax"])))
        if filt.get("yearMin") is not None:
            params.append(("YearMin", str(filt["yearMin"])))
        if filt.get("yearMax") is not None:
            params.append(("YearMax", str(filt["yearMax"])))
        if filt.get("seatsMin") is not None:
            params.append(("SeatsMin", str(filt["seatsMin"])))
        if filt.get("seatsMax") is not None:
            params.append(("SeatsMax", str(filt["seatsMax"])))
        for body in filt.get("bodyTypes") or []:
            params.append(("BodyType", body.get("name", "")))
        for engine in filt.get("engineTypes") or []:
            params.append(("EngineType", engine.get("name", "")))
        for color in filt.get("colors") or []:
            params.append(("Color", color.get("name", "")))

    return params


def fetch_cars(city_id: str, filter_short: str | None, nearby: bool, suggested: list[dict]) -> list[dict]:
    params = build_search_params(filter_short, nearby, suggested)
    url = append_query(f"{API_BASE}/Car/list/filtered/{city_id}", params)
    data = http_get_json(url)
    if not isinstance(data, dict):
        raise RuntimeError(f"Unexpected car list response for {url}")
    items = data.get("items") or []
    if not isinstance(items, list):
        raise RuntimeError(f"Unexpected items in car list for {url}")
    return items


def render_nav(blocks: dict[str, dict], current_path: str) -> str:
    lines = [
        '    <nav class="drivebit-seo-nav" aria-label="Аренда авто в Москве по целям поездки">',
        "        <ul>",
    ]
    for path in MOSKVA_PATHS:
        block = blocks[path]
        label = block.get("navLabel") or block.get("h2", path)
        href = path if path != "/moskva" else "/moskva"
        active = ' aria-current="page"' if path == current_path else ""
        lines.append(
            f'            <li><a href="{href}"{active}>{html.escape(str(label))}</a></li>'
        )
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


def render_cars_shell(cars: list[dict], h2: str) -> str:
    cards = "\n".join(render_car_card(car) for car in cars)
    return f"""    <div class="drivebit-seo-cars">
        <h2 class="drivebit-seo-cars-heading">{html.escape(h2)}</h2>
        <div class="drivebit-seo-cars-grid">
{cards}
        </div>
    </div>
"""


def build_json_ld(path: str, block: dict, cars: list[dict]) -> dict:
    page_url = f"{SITE_BASE}{path}"
    nav_label = block.get("navLabel") or block.get("h2", "Москва")
    items = []
    for idx, car in enumerate(cars, start=1):
        general = car.get("general") or {}
        brand = general.get("brandName") or ""
        model = general.get("modelName") or ""
        name = " ".join(p for p in (brand, model) if p).strip() or "Автомобиль"
        car_id = car.get("id") or ""
        price = min_daily_price(car)
        photos = general.get("photos") or []
        image = sanitize_photo_url((photos[0] or {}).get("url") if photos else None)
        product = {
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
        }
        items.append(
            {
                "@type": "ListItem",
                "position": idx,
                "item": product,
            }
        )

    graph = [
        {
            "@type": "BreadcrumbList",
            "itemListElement": [
                {
                    "@type": "ListItem",
                    "position": 1,
                    "name": "DriveBit",
                    "item": f"{SITE_BASE}/",
                },
                {
                    "@type": "ListItem",
                    "position": 2,
                    "name": "Москва",
                    "item": f"{SITE_BASE}/moskva",
                },
                {
                    "@type": "ListItem",
                    "position": 3,
                    "name": nav_label,
                    "item": page_url,
                },
            ],
        },
        {
            "@type": "AutoRental",
            "name": f"DriveBit — {nav_label}",
            "url": page_url,
            "areaServed": "Москва",
            "provider": {"@type": "Organization", "name": "DriveBit", "url": SITE_BASE},
        },
        {
            "@type": "ItemList",
            "name": block.get("h2", nav_label),
            "numberOfItems": len(items),
            "itemListElement": items,
        },
    ]
    return {"@context": "https://schema.org", "@graph": graph}


def inject_json_ld(html_text: str, graph: dict) -> str:
    script = (
        f'    <script type="application/ld+json">\n'
        f"{json.dumps(graph, ensure_ascii=False, indent=2)}\n"
        f"    </script>\n"
    )
    block = f"    {JSON_LD_MARKER}\n{script}"
    if JSON_LD_MARKER in html_text:
        pattern = re.compile(
            rf"\s*{re.escape(JSON_LD_MARKER)}.*?</script>\s*",
            re.DOTALL,
        )
        return pattern.sub("\n" + block, html_text, count=1)
    return html_text.replace("</head>", block + "</head>", 1)


def ensure_main_promo(html_text: str) -> str:
    if "<!-- drivebit-main-promo-start -->" in html_text:
        return html_text
    if PROMO_CSS not in html_text:
        html_text = html_text.replace(
            'href="/vendor/drivebit-seo-text.css"/>',
            'href="/vendor/drivebit-seo-text.css"/>\n' + PROMO_CSS,
            1,
        )
    footer_idx = html_text.find('<div class="drivebit-footer-shell">')
    if footer_idx < 0:
        raise RuntimeError("drivebit-footer-shell marker not found")
    generated_end = html_text.find(GENERATED_END)
    if generated_end >= 0:
        line_end = html_text.find("\n", generated_end)
        insert_at = (line_end + 1) if line_end >= 0 else generated_end
    else:
        insert_at = footer_idx
    block = MAIN_PROMO_FRAGMENT if MAIN_PROMO_FRAGMENT.endswith("\n") else MAIN_PROMO_FRAGMENT + "\n"
    return html_text[:insert_at] + block + html_text[insert_at:]


def strip_leaflet(html_text: str) -> str:
    html_text = re.sub(
        r'\s*<link rel="stylesheet" href="/vendor/leaflet/[^"]+"/>?\s*',
        "\n",
        html_text,
    )
    html_text = re.sub(
        r'\s*<script src="/vendor/leaflet/[^"]+"></script>\s*',
        "\n",
        html_text,
    )
    return html_text


def ensure_leaflet(html_text: str) -> str:
    has_css = "/vendor/leaflet/leaflet.css" in html_text
    has_js = 'src="/vendor/leaflet/leaflet.js"' in html_text
    if has_css and has_js:
        return html_text
    anchor = '    <link rel="stylesheet" href="/vendor/drivebit-static-layout.css"/>'
    if not has_css:
        if anchor in html_text:
            html_text = html_text.replace(anchor, LEAFLET_CSS + anchor, 1)
        else:
            html_text = html_text.replace("</head>", LEAFLET_CSS + "</head>", 1)
    if not has_js:
        html_text = html_text.replace("</head>", LEAFLET_SCRIPT + "</head>", 1)
    return html_text


def strip_compose_app_script(html_text: str) -> str:
    html_text = re.sub(
        r'\s*<link rel="preload" href="/composeApp\.js[^"]*" as="script">\s*',
        "\n",
        html_text,
    )
    html_text = re.sub(
        r'\s*<script type="application/javascript" src="/composeApp\.js[^"]*"[^>]*>\s*</script>\s*',
        "\n",
        html_text,
    )
    return html_text


def ensure_compose_app_script(html_text: str) -> str:
    if 'src="/composeApp.js' in html_text:
        return html_text
    if COMPOSE_APP_PRELOAD.strip() not in html_text:
        html_text = html_text.replace("</head>", COMPOSE_APP_PRELOAD + "</head>", 1)
    if "</body>" in html_text:
        return html_text.replace("</body>", COMPOSE_APP_SCRIPT + "</body>", 1)
    raise RuntimeError("</body> not found")


def ensure_nearby_app_script(html_text: str) -> str:
    html_text = strip_compose_app_script(html_text)
    if 'src="/nearbyApp.js' in html_text:
        return html_text
    if NEARBY_APP_PRELOAD.strip() not in html_text:
        html_text = html_text.replace("</head>", NEARBY_APP_PRELOAD + "</head>", 1)
    if "</body>" in html_text:
        return html_text.replace("</body>", NEARBY_APP_SCRIPT + "</body>", 1)
    raise RuntimeError("</body> not found")


def inject_generated_body(html_text: str, generated: str) -> str:
    wrapped = f"\n{GENERATED_START}\n{generated}{GENERATED_END}\n\n"
    if GENERATED_START in html_text:
        pattern = re.compile(
            rf"\n?{re.escape(GENERATED_START)}.*?{re.escape(GENERATED_END)}\n?",
            re.DOTALL,
        )
        return pattern.sub(wrapped, html_text, count=1)
    promo_idx = html_text.find("<!-- drivebit-main-promo-start -->")
    footer_idx = html_text.find('<div class="drivebit-footer-shell">')
    if footer_idx < 0:
        raise RuntimeError("drivebit-footer-shell marker not found")
    insert_at = promo_idx if promo_idx >= 0 else footer_idx
    return html_text[:insert_at] + wrapped + html_text[insert_at:]


def main() -> int:
    blocks = json.loads(BLOCKS_FILE.read_text(encoding="utf-8"))
    cities = http_get_json(f"{API_BASE}/Dictionary/cities/all")
    if not isinstance(cities, list):
        raise RuntimeError("cities/all returned non-list")
    city_id = find_moscow_city_id(cities)
    suggested = http_get_json(f"{API_BASE}/Dictionary/cars/filters/suggested")
    if not isinstance(suggested, list):
        raise RuntimeError("filters/suggested returned non-list")

    errors: list[str] = []
    for path in MOSKVA_PATHS:
        html_path = PATH_TO_HTML[path]
        block = blocks.get(path)
        if not block:
            errors.append(f"Missing block for {path}")
            continue

        filter_short = block.get("filterShortName")
        nearby = bool(block.get("nearby"))
        try:
            cars = fetch_cars(city_id, filter_short, nearby, suggested)
        except (RuntimeError, subprocess.CalledProcessError) as exc:
            errors.append(f"{path}: API error: {exc}")
            continue

        if not cars:
            errors.append(f"{path}: no cars returned from API")
            continue

        nav = render_nav(blocks, path)
        cars_html = render_cars_shell(cars, f"Автомобили — {block.get('navLabel') or block.get('h2', '')}")
        generated = nav + cars_html
        graph = build_json_ld(path, block, cars)

        text = html_path.read_text(encoding="utf-8")
        text = inject_generated_body(text, generated)
        text = inject_json_ld(text, graph)
        text = ensure_main_promo(text)
        if path == "/moskva/poblizosti":
            text = strip_compose_app_script(text)
            text = ensure_leaflet(text)
            text = ensure_nearby_app_script(text)
        else:
            text = strip_leaflet(text)
            text = ensure_compose_app_script(text)
        html_path.write_text(text, encoding="utf-8")
        print(f"OK {path} ({len(cars)} cars) -> {html_path.relative_to(ROOT)}")

    if errors:
        for err in errors:
            print(f"ERROR {err}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
