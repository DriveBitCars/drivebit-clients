#!/usr/bin/env python3
"""Generate static HTML info pages from legal .txt sources."""

from __future__ import annotations

import html
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "composeApp" / "src" / "jsMain" / "resources"

HEADER_FOOTER = """
    <!-- drivebit-app-header-start -->
    <header id="drivebit-app-header" class="drivebit-app-header">
        <a id="drivebit-unread-banner" class="drivebit-unread-banner" href="/chats" aria-live="polite">У Вас есть непрочитанные сообщения</a>
        <div class="drivebit-app-header-inner">
            <a class="drivebit-header-logo" href="/moskva">
                <img src="/images/logos/turo_logo.svg" alt="Drive bit Logo" width="120" height="40" class="turo-logo">
            </a>
            <nav id="drivebit-header-nav-desktop" class="drivebit-header-nav" aria-label="Основная навигация">
                <a href="/search">Аренда авто</a>
                <a href="/list-your-car.html">Сдать авто</a>
                <a href="/contacts">Контакты</a>
            </nav>
            <div id="drivebit-header-compose" class="drivebit-header-actions"></div>
        </div>
    </header>
    <nav id="drivebit-header-nav-mobile" class="drivebit-header-nav-mobile" aria-label="Основная навигация">
        <a href="/search">Аренда авто</a>
        <a href="/list-your-car.html">Сдать авто</a>
        <a href="/contacts">Контакты</a>
    </nav>
    <!-- drivebit-app-header-end -->
"""

FOOTER = """
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
"""


def normalize_offer(text: str) -> str:
    t = text.strip()
    if t.startswith("# "):
        t = t.removeprefix("# ")
    t = t.replace("ОФЕРТАна", "ОФЕРТА на")
    t = t.replace("»Дата", "» Дата")
    t = t.replace("г.1.", "г. 1.")
    t = t.replace("документа1.1.", "документа 1.1.")
    t = t.replace(").ВАЖНО:", ").\n\nВАЖНО:")
    t = t.replace("ПРОСТЫМИ СЛОВАМИ:", "\n\nПРОСТЫМИ СЛОВАМИ:")
    t = re.sub(r"---", "\n\n---\n\n", t)
    return t


def linkify_line(line: str) -> str:
    escaped = html.escape(line)
    escaped = re.sub(
        r"(https://drivebit\.ru)",
        r'<a href="\1" target="_blank" rel="noopener noreferrer">\1</a>',
        escaped,
    )
    escaped = re.sub(
        r"info@drivebit\.ru",
        r'<a href="mailto:info@drivebit.ru">info@drivebit.ru</a>',
        escaped,
    )
    return escaped


def privacy_body_from_txt(text: str) -> str:
    lines = text.strip().splitlines()
    parts: list[str] = []
    parts.append('<p class="drivebit-info-page__subtitle">(Политика обработки персональных данных)</p>')

    i = 3
    while i < len(lines) and lines[i].strip().startswith("Дата"):
        parts.append(f'<p class="drivebit-info-page__tight">{linkify_line(lines[i].strip())}</p>')
        i += 1

    paragraph: list[str] = []

    def flush_paragraph(section_end: bool = False) -> None:
        nonlocal paragraph
        if not paragraph:
            return
        cls = "drivebit-info-page__section-end" if section_end else ""
        class_attr = f' class="{cls}"' if cls else ""
        parts.append(f"<p{class_attr}>{linkify_line(' '.join(paragraph))}</p>")
        paragraph = []

    while i < len(lines):
        line = lines[i].strip()
        i += 1
        if not line:
            flush_paragraph(section_end=True)
            continue
        if re.match(r"^\d+\.\s", line) and not re.match(r"^\d+\.\d+\.", line):
            flush_paragraph(section_end=True)
            parts.append(f"<h2>{linkify_line(line)}</h2>")
            continue
        if line.startswith("—"):
            flush_paragraph()
            parts.append(f'<p class="drivebit-info-page__tight">{linkify_line(line)}</p>')
            continue
        if re.match(r"^\d+\.\d+\.", line):
            flush_paragraph()
            parts.append(f'<p class="drivebit-info-page__tight">{linkify_line(line)}</p>')
            continue
        paragraph.append(line)

    flush_paragraph()
    body = "\n        ".join(parts)
    body = body.replace(
        "cookie и аналогичные технологии",
        '<a href="/cookies">cookie</a> и аналогичные технологии',
        1,
    )
    return body


def build_page(
    *,
    title: str,
    description: str,
    path: str,
    page_id: str,
    h1: str,
    main_body: str,
) -> str:
    canonical = f"https://drivebit.ru{path}"
    return f"""<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{html.escape(title)}</title>
    <meta name="description" content="{html.escape(description)}">
    <meta name="robots" content="index, follow">
    <meta name="yandex-verification" content="c19b1a1a78585234" />
    <link rel="canonical" href="{canonical}">

    <meta property="og:type" content="website">
    <meta property="og:title" content="{html.escape(title)}">
    <meta property="og:description" content="{html.escape(description)}">
    <meta property="og:url" content="{canonical}">
    <meta property="og:image" content="https://drivebit.ru/images/logos/logo.png">
    <meta property="og:locale" content="ru_RU">
    <meta property="og:site_name" content="DriveBit">

    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:title" content="{html.escape(title)}">
    <meta name="twitter:description" content="{html.escape(description)}">
    <meta name="twitter:image" content="https://drivebit.ru/images/logos/logo.png">
    <meta name="twitter:url" content="{canonical}">

    <link rel="icon" type="image/svg+xml" href="/images/logos/turo_logo.svg">
    <link rel="alternate icon" href="/images/logos/turo_logo.svg">
    <link rel="apple-touch-icon" href="/images/logos/turo_logo.svg">
    <link rel="preload" href="/composeApp.js?v=4" as="script">
    <link rel="stylesheet" href="/vendor/drivebit-static-layout.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-header.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-footer.css"/>
    <link rel="stylesheet" href="/vendor/drivebit-info-page.css"/>
</head>
<body class="drivebit-static-info-page">
{HEADER_FOOTER}

    <main class="drivebit-info-page" id="{page_id}">
        <a class="drivebit-info-page__back" href="/moskva">← На главную</a>
        <h1>{html.escape(h1)}</h1>
        {main_body}
    </main>

    <div id="root" aria-hidden="true"></div>

{FOOTER}
</body>
</html>
"""


def main() -> int:
    privacy_txt = (RES / "privacy.txt").read_text(encoding="utf-8")
    privacy_html = build_page(
        title="Политика конфиденциальности - DriveBit",
        description="Политика обработки персональных данных платформы DriveBit.",
        path="/privacy",
        page_id="drivebit-privacy-page",
        h1="Политика конфиденциальности",
        main_body="        " + privacy_body_from_txt(privacy_txt),
    )
    privacy_path = RES / "privacy" / "index.html"
    privacy_path.parent.mkdir(parents=True, exist_ok=True)
    privacy_path.write_text(privacy_html, encoding="utf-8")
    print(f"OK {privacy_path.relative_to(ROOT)}")

    offer_txt = normalize_offer((RES / "offer.txt").read_text(encoding="utf-8"))
    offer_pre = html.escape(offer_txt)
    offer_body = f'        <pre class="drivebit-info-page__legal-text">{offer_pre}</pre>'
    offer_html = build_page(
        title="Публичная оферта - DriveBit",
        description="Публичная оферта на оказание агентских услуг платформы DriveBit.",
        path="/offer",
        page_id="drivebit-offer-page",
        h1="Публичная оферта",
        main_body=offer_body,
    )
    offer_path = RES / "offer" / "index.html"
    offer_path.parent.mkdir(parents=True, exist_ok=True)
    offer_path.write_text(offer_html, encoding="utf-8")
    print(f"OK {offer_path.relative_to(ROOT)}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
