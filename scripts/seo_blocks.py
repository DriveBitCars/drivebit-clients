#!/usr/bin/env python3
"""SEO blocks — reads composeApp/seo/landing-blocks.json (single source of truth)."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BLOCKS_FILE = ROOT / "composeApp" / "seo" / "landing-blocks.json"


def load_blocks() -> dict[str, dict]:
    return json.loads(BLOCKS_FILE.read_text(encoding="utf-8"))


def render_block(path: str) -> str:
    block = load_blocks()[path]
    ps = "".join(f"\n            <p>{p}</p>" for p in block["paragraphs"])
    return f"""    <div class="drivebit-seo-shell">
        <section class="drivebit-seo-text" aria-label="{block['ariaLabel']}">
            <h2>{block['h2']}</h2>{ps}
        </section>
    </div>

"""
