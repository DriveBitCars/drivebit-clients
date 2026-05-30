#!/usr/bin/env python3
"""SEO blocks — reads composeApp/seo/landing-blocks.json (single source of truth)."""

from __future__ import annotations

import html
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BLOCKS_FILE = ROOT / "composeApp" / "seo" / "landing-blocks.json"


def load_blocks() -> dict[str, dict]:
    return json.loads(BLOCKS_FILE.read_text(encoding="utf-8"))


def _render_section(section: dict) -> str:
    parts: list[str] = []
    parts.append(f"            <h2>{html.escape(section['heading'])}</h2>")
    for paragraph in section.get("paragraphs") or []:
        parts.append(f"            <p>{html.escape(paragraph)}</p>")
    bullets = section.get("bullets") or []
    if bullets:
        parts.append("            <ul>")
        for bullet in bullets:
            parts.append(f"                <li>{html.escape(bullet)}</li>")
        parts.append("            </ul>")
    table = section.get("table")
    if table:
        parts.append("            <table>")
        parts.append("                <thead><tr>")
        for header in table.get("headers") or []:
            parts.append(f"                    <th>{html.escape(header)}</th>")
        parts.append("                </tr></thead>")
        parts.append("                <tbody>")
        for row in table.get("rows") or []:
            parts.append("                    <tr>")
            for cell in row:
                parts.append(f"                        <td>{html.escape(cell)}</td>")
            parts.append("                    </tr>")
        parts.append("                </tbody>")
        parts.append("            </table>")
    return "\n".join(parts)


def render_block(path: str) -> str:
    block = load_blocks()[path]
    sections = block.get("sections")
    if sections:
        inner = "\n".join(_render_section(section) for section in sections)
    else:
        ps = "".join(f"\n            <p>{html.escape(p)}</p>" for p in block["paragraphs"])
        inner = f"            <h2>{html.escape(block['h2'])}</h2>{ps}"
    return f"""    <div class="drivebit-seo-shell">
        <section class="drivebit-seo-text" aria-label="{html.escape(block['ariaLabel'])}">
{inner}
        </section>
    </div>

"""
