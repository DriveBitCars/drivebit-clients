#!/usr/bin/env python3
"""Inject page loader placeholder into /moskva/* static index.html files."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "composeApp" / "src" / "jsMain" / "resources"
LOADER_FRAGMENT = (ROOT / "composeApp" / "seo" / "page-loader.fragment.html").read_text(
    encoding="utf-8"
).strip()

GENERATED_START = "<!-- drivebit-seo-generated-start -->"
GENERATED_END = "<!-- drivebit-seo-generated-end -->"
COMPOSE_APP_PRELOAD = '    <link rel="preload" href="/composeApp.js?v=4" as="script">\n'
COMPOSE_APP_SCRIPT = (
    '    <script type="application/javascript" src="/composeApp.js?v=4"></script>\n'
)

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


def ensure_compose_app_script(html_text: str) -> str:
    if 'src="/composeApp.js' in html_text:
        return html_text
    if COMPOSE_APP_PRELOAD.strip() not in html_text:
        html_text = html_text.replace("</head>", COMPOSE_APP_PRELOAD + "</head>", 1)
    if "</body>" in html_text:
        return html_text.replace("</body>", COMPOSE_APP_SCRIPT + "</body>", 1)
    raise RuntimeError("</body> not found")


def inject_generated_body(html_text: str, generated: str) -> str:
    wrapped = f"{GENERATED_START}\n{generated}\n{GENERATED_END}\n"
    if GENERATED_START in html_text:
        pattern = re.compile(
            rf"{re.escape(GENERATED_START)}.*?{re.escape(GENERATED_END)}",
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
    for path, html_path in PATH_TO_HTML.items():
        if not html_path.is_file():
            print(f"ERROR missing {html_path}", file=sys.stderr)
            return 1
        text = html_path.read_text(encoding="utf-8")
        text = inject_generated_body(text, LOADER_FRAGMENT)
        text = ensure_compose_app_script(text)
        html_path.write_text(text, encoding="utf-8")
        print(f"OK {path} -> {html_path.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
