#!/usr/bin/env python3
"""Build /search/{brand} static SEO pages and landing-blocks.json entries."""

from __future__ import annotations

import html
import json
import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BLOCKS_FILE = ROOT / "composeApp" / "seo" / "landing-blocks.json"
RESOURCES = ROOT / "composeApp" / "src" / "jsMain" / "resources"
API_BASE = os.environ.get("DRIVEBIT_API_BASE", "https://drivebit.ru/api").rstrip("/")
SITE_BASE = "https://drivebit.ru"
MOSCOW_NAME = "Москва"
MOSCOW_CITY_ID_FALLBACK = "158835"

GENERATED_START = "<!-- drivebit-seo-generated-start -->"
GENERATED_END = "<!-- drivebit-seo-generated-end -->"
LOADER_FRAGMENT = (ROOT / "composeApp" / "seo" / "page-loader.fragment.html").read_text(
    encoding="utf-8"
).strip()
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
    for _attempt in range(retries):
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


def render_page_html(path: str, block: dict) -> str:
    page_url = f"{SITE_BASE}{path}"
    title = html.escape(block["title"])
    description = html.escape(block["description"])
    aria = html.escape(block["ariaLabel"])
    h2 = html.escape(block["h2"])
    paragraphs = "".join(f"            <p>{html.escape(p)}</p>\n" for p in block["paragraphs"])
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
    <link rel="preload" href="/composeApp.js?v=4" as="script">
    <link rel="stylesheet" href="/vendor/drivebit-static-layout.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-footer.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-seo-text.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-main-promo.css"/>
</head>
<body>
    <div id="root"></div>

{GENERATED_START}
{LOADER_FRAGMENT}
{GENERATED_END}
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

    <script type="application/javascript" src="/composeApp.js?v=4"></script>
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

    brands = http_get_json(f"{API_BASE}/Dictionary/cars/brands/existing")
    if not isinstance(brands, list):
        raise RuntimeError("brands/existing returned non-list")

    published = 0
    for brand in brands:
        brand_id = int(brand["id"])
        brand_name = str(brand["name"])
        slug = name_to_slug(brand_name)
        path = f"/search/{slug}"

        block = build_seo_block(brand_name, slug)
        block["brandId"] = brand_id
        blocks[path] = block

        html_text = render_page_html(path, block)
        out_dir = RESOURCES / "search" / slug
        out_dir.mkdir(parents=True, exist_ok=True)
        (out_dir / "index.html").write_text(html_text, encoding="utf-8")
        published += 1
        print(f"OK {path}")

    BLOCKS_FILE.write_text(
        json.dumps(blocks, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    if published == 0:
        print("ERROR: no brand pages generated", file=sys.stderr)
        return 1

    print(f"Published {published} brand search pages")
    return 0


if __name__ == "__main__":
    sys.exit(main())
