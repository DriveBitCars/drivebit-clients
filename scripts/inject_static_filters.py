#!/usr/bin/env python3
"""Inject static filter row into city SEO HTML pages."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FRAGMENT_TEMPLATE = (ROOT / "composeApp" / "seo" / "filters.fragment.html").read_text(
    encoding="utf-8"
).strip()

FILTERS_START = "<!-- drivebit-filters-start -->"
FILTERS_END = "<!-- drivebit-filters-end -->"
HERO_END = "<!-- drivebit-hero-end -->"
ROOT_MARKER = '<div id="root">'
FILTERS_JS = '<script src="/vendor/drivebit-filters.js" defer></script>'

ACTIVE_KEYS = (
    "ALL",
    "POBLIZOSTI",
    "PUTESHESTVIYA",
    "K_RODNYM",
    "KOMANDIROVKI",
    "ZA_GOROD",
    "KANIKULY",
    "PEREEZD",
    "MEROPRIYATIE",
)

PATH_TO_ACTIVE: dict[str, str] = {
    "/": "ALL",
    "/moskva": "ALL",
    "/moskva/poblizosti": "POBLIZOSTI",
    "/moskva/puteshestviya": "PUTESHESTVIYA",
    "/moskva/k-rodnym": "K_RODNYM",
    "/moskva/komandirovki": "KOMANDIROVKI",
    "/moskva/za-gorod": "ZA_GOROD",
    "/moskva/kanikuly": "KANIKULY",
    "/moskva/pereezd": "PEREEZD",
    "/moskva/meropriyatie": "MEROPRIYATIE",
}

PATH_TO_HTML: dict[str, Path] = {
    "/": ROOT / "index.html",
    "/moskva": ROOT / "composeApp" / "src" / "jsMain" / "resources" / "moskva" / "index.html",
    "/moskva/k-rodnym": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "k-rodnym"
    / "index.html",
    "/moskva/kanikuly": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "kanikuly"
    / "index.html",
    "/moskva/komandirovki": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "komandirovki"
    / "index.html",
    "/moskva/meropriyatie": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "meropriyatie"
    / "index.html",
    "/moskva/pereezd": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "pereezd"
    / "index.html",
    "/moskva/poblizosti": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "poblizosti"
    / "index.html",
    "/moskva/puteshestviya": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "puteshestviya"
    / "index.html",
    "/moskva/za-gorod": ROOT
    / "composeApp"
    / "src"
    / "jsMain"
    / "resources"
    / "moskva"
    / "za-gorod"
    / "index.html",
}


def filters_block(active_key: str) -> str:
    block = FRAGMENT_TEMPLATE
    for key in ACTIVE_KEYS:
        token = f"__ACTIVE_{key}__"
        block = block.replace(token, "true" if key == active_key else "false")
    return block


def inject_filters_block(html: str, active_key: str) -> str:
    block = filters_block(active_key)
    if FILTERS_START in html:
        pattern = re.compile(
            rf"{re.escape(FILTERS_START)}.*?{re.escape(FILTERS_END)}",
            re.DOTALL,
        )
        return pattern.sub(block, html, count=1)
    if HERO_END in html:
        return html.replace(HERO_END, f"{HERO_END}\n\n{block}", 1)
    if ROOT_MARKER in html:
        return html.replace(ROOT_MARKER, f"{block}\n\n    {ROOT_MARKER}", 1)
    raise RuntimeError("hero end or root marker not found")


def inject_script(html: str) -> str:
    if "drivebit-filters.js" in html:
        return html
    if "</body>" in html:
        return html.replace("</body>", f"    {FILTERS_JS}\n</body>", 1)
    return html


def process(path: Path, active_key: str) -> bool:
    text = path.read_text(encoding="utf-8")
    new = inject_script(inject_filters_block(text, active_key))
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
        active = PATH_TO_ACTIVE[route]
        if process(html_path, active):
            updated.append(html_path)
            print(f"OK {route} -> {html_path.relative_to(ROOT)} (active={active})")
        else:
            print(f"skip {route} (unchanged)")
    print(f"updated {len(updated)} file(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
