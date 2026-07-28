# Verification badge outline blue — design

Date: 2026-07-28  
Status: approved in brainstorming (variant **D**)  
Mockup: `tmp/verification-badge-mockups/all-variants.png`

## Problem

Public verification badges use StatusTag gray (`Gray300` bg / `Gray600` text). They look muted and not on-brand.

## Goals

- Make badges more colorful using DriveBit brand blue.
- Keep size/typography unchanged so layout does not shift.
- Same look on Web and Mobile (`VerificationBadge` in both places).

## Non-goals

- Changing badge label copy (`VerificationLabels`).
- Per-type colors (variant C).
- Soft fill (A) or solid fill (B).
- New color tokens in `ColorsDriveBit` (reuse existing `Blue`).
- Prod release under this doc (ship separately).

## Decision

**Variant D — Outline blue**

| Property | Value |
|----------|--------|
| Background | transparent |
| Text | `ColorsDriveBit.Blue` / `CSSColors.Blue` (`#2962FF`) |
| Border | `1.5.dp` / `1.5.px` solid Blue |
| Corner radius | `6.dp` / `6.px` |
| Padding | horizontal `8`, vertical `4` |
| Font | `12` / weight `600`, uppercase |

All badge types share this single style.

## Files to change

- `UI-Components/.../VerificationBadge.kt` — Compose: border + Blue text, no gray fill
- `DrivebitWeb/.../VerificationBadge.kt` — Compose Web: same via CSS border + Blue

## Tests

- Prefer a small UI/style unit or screenshot-less assert where the project already tests components; otherwise update any existing VerificationBadge-related tests.
- Manual/visual: car-detail owner row shows outline blue pill with label text unchanged.

## Out of scope follow-ups

- Soft blue fill (A) or solid (B) if outline proves too weak on noisy screens.
