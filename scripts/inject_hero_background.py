#!/usr/bin/env python3
"""Inject static hero background img + CSS/preload into moskva index.html files."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "composeApp" / "src" / "jsMain" / "resources"
FRAGMENT_TEMPLATE = (ROOT / "composeApp" / "seo" / "hero-background.fragment.html").read_text(
    encoding="utf-8"
).strip()

HERO_START = "<!-- drivebit-hero-start -->"
HERO_END = "<!-- drivebit-hero-end -->"
CITY_PAGE_CSS = '<link rel="stylesheet" href="/vendor/drivebit-city-page.css"/>'
INTER_FONT = (
    '    <link rel="preconnect" href="https://fonts.googleapis.com">\n'
    '    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>\n'
    '    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">\n'
)
HERO_SEARCH_JS = '<script src="/vendor/drivebit-hero-search.js" defer></script>'
ROOT_MARKER = '<div id="root">'

PATH_TO_BACKGROUND: dict[str, str] = {
    "/": "/images/searchbackground/car0.jpg",
    "/moskva": "/images/searchbackground/car0.jpg",
    "/moskva/poblizosti": "/images/searchbackground/car1.jpg",
    "/moskva/puteshestviya": "/images/searchbackground/car2.jpg",
    "/moskva/za-gorod": "/images/searchbackground/car3.jpg",
    "/moskva/k-rodnym": "/images/searchbackground/car4.jpg",
    "/moskva/komandirovki": "/images/searchbackground/car5.jpg",
    "/moskva/kanikuly": "/images/searchbackground/car6.jpg",
    "/moskva/meropriyatie": "/images/searchbackground/car7.jpg",
    "/moskva/pereezd": "/images/searchbackground/car8.jpg",
}

PATH_TO_HEADLINE: dict[str, str] = {
    "/": "Аренда авто у частных владельцев в Москве",
    "/moskva": "Аренда авто у частных владельцев в Москве",
    "/moskva/poblizosti": "Аренда авто на карте в Москве",
    "/moskva/puteshestviya": "Аренда авто для путешествий по России из Москвы",
    "/moskva/za-gorod": "Аренда авто для поездки в другой город из Москвы",
    "/moskva/k-rodnym": "Арендуй авто у частных владельцев в Москве",
    "/moskva/komandirovki": "Арендуй авто у частных владельцев в Москве",
    "/moskva/kanikuly": "Арендуй авто у частных владельцев в Москве",
    "/moskva/meropriyatie": "Арендуй авто у частных владельцев в Москве",
    "/moskva/pereezd": "Арендуй авто у частных владельцев в Москве",
}

PATH_TO_HTML: dict[str, Path] = {
    "/": ROOT / "index.html",
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


def hero_block(background_url: str, headline: str) -> str:
    return (
        FRAGMENT_TEMPLATE.replace("__BACKGROUND_URL__", background_url).replace(
            "__HEADLINE__", headline
        )
    )


def inject_head_assets(html: str, background_url: str) -> str:
    if "drivebit-city-page.css" not in html:
        marker = '<link rel="stylesheet" href="/vendor/drivebit-static-layout.css"/>'
        if marker in html:
            html = html.replace(marker, f"{marker}\n    {CITY_PAGE_CSS}", 1)
        else:
            marker = '<link rel="stylesheet" href="/vendor/leaflet/leaflet.css"/>'
            if marker in html:
                html = html.replace(marker, f"{marker}\n    {CITY_PAGE_CSS}", 1)

    if "fonts.googleapis.com/css2?family=Inter" not in html:
        compose_preload = '<link rel="preload" href="/composeApp.js'
        idx = html.find(compose_preload)
        if idx >= 0:
            line_end = html.find("\n", idx)
            if line_end >= 0:
                html = html[: line_end + 1] + INTER_FONT + html[line_end + 1 :]

    preload = f'    <link rel="preload" href="{background_url}" as="image"/>\n'
    if f'href="{background_url}" as="image"' not in html:
        compose_preload = '<link rel="preload" href="/composeApp.js'
        idx = html.find(compose_preload)
        if idx >= 0:
            line_end = html.find("\n", idx)
            if line_end >= 0:
                html = html[: line_end + 1] + preload + html[line_end + 1 :]

    if "drivebit-hero-search.js" not in html:
        if "</body>" in html:
            html = html.replace("</body>", f"    {HERO_SEARCH_JS}\n</body>", 1)
    return html


def inject_hero_block(html: str, background_url: str, headline: str) -> str:
    block = hero_block(background_url, headline)
    if HERO_START in html:
        pattern = re.compile(
            rf"{re.escape(HERO_START)}.*?{re.escape(HERO_END)}",
            re.DOTALL,
        )
        return pattern.sub(block, html, count=1)
    if ROOT_MARKER in html:
        return html.replace(ROOT_MARKER, f"{block}\n\n    {ROOT_MARKER}", 1)
    raise RuntimeError("root marker not found")


def process(path: Path, background_url: str, headline: str) -> bool:
    text = path.read_text(encoding="utf-8")
    new = inject_head_assets(inject_hero_block(text, background_url, headline), background_url)
    if new != text:
        path.write_text(new, encoding="utf-8")
        return True
    return False


def main() -> int:
    updated: list[Path] = []
    for route, html_path in PATH_TO_HTML.items():
        if not html_path.is_file():
            print(f"ERROR missing {html_path}")
            return 1
        bg = PATH_TO_BACKGROUND[route]
        headline = PATH_TO_HEADLINE[route]
        if process(html_path, bg, headline):
            updated.append(html_path)
            print(f"OK {route} -> {html_path.relative_to(ROOT)} ({bg})")
        else:
            print(f"skip {route} (unchanged)")
    print(f"updated {len(updated)} file(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
