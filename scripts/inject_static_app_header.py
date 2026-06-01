#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FRAGMENT = (ROOT / "composeApp/seo/app-header.fragment.html").read_text(encoding="utf-8")
HEADER_CSS = '<link rel="stylesheet" href="/vendor/drivebit-header.css"/>'
BLOCK_START = "<!-- drivebit-app-header-start -->"
BLOCK_END = "<!-- drivebit-app-header-end -->"


def inject_header_css(html: str) -> str:
    if "drivebit-header.css" in html:
        return html
    marker = '<link rel="stylesheet" href="/vendor/drivebit-static-layout.css"/>'
    if marker in html:
        return html.replace(marker, f"{marker}\n    {HEADER_CSS}", 1)
    marker = '<link rel="stylesheet" href="/vendor/leaflet/leaflet.css"/>'
    if marker in html:
        return html.replace(marker, f"{marker}\n    {HEADER_CSS}", 1)
    return html


def inject_header_block(html: str) -> str:
    if BLOCK_START in html:
        start = html.index(BLOCK_START)
        end = html.index(BLOCK_END) + len(BLOCK_END)
        return html[:start] + FRAGMENT.strip() + "\n" + html[end:]

    empty_mount = '<div id="drivebit-app-header"></div>'
    if empty_mount in html:
        return html.replace(empty_mount, FRAGMENT.strip(), 1)

    root_marker = '<div id="root">'
    if root_marker in html:
        return html.replace(root_marker, f"{FRAGMENT.strip()}\n{root_marker}", 1)

    return html


def process(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    new = inject_header_css(inject_header_block(text))
    if new != text:
        path.write_text(new, encoding="utf-8")
        return True
    return False


def main() -> None:
    paths = [ROOT / "index.html"]
    paths += list((ROOT / "composeApp/src/jsMain/resources").rglob("index.html"))
    for rel in (
        "carDetailApp/src/jsMain/resources/car-detail/index.html",
        "carDetailApp/src/jsMain/resources/car-photos-gallery/index.html",
        "myCarsApp/src/jsMain/resources/owner-app-shell/index.html",
        "list-your-car.html",
    ):
        p = ROOT / rel
        if p.is_file():
            paths.append(p)

    updated = [p for p in paths if process(p)]
    for p in updated:
        print("updated", p.relative_to(ROOT))


if __name__ == "__main__":
    main()
