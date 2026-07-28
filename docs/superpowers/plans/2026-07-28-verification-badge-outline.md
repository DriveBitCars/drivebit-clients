# Verification Badge Outline Blue Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle public verification badges from gray StatusTag fill to brand-blue outline (variant D).

**Architecture:** Keep a single shared style token in `UI-Components` (color + border width) covered by a unit test; apply it in Compose Multiplatform `VerificationBadge` and mirror the same values in Compose Web `VerificationBadge` (DrivebitWeb cannot share Modifier/`dp` with DOM CSS).

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Compose HTML, kotlin.test, ColorsDriveBit / CSSColors

**Spec:** `docs/superpowers/specs/2026-07-28-verification-badge-outline-design.md`

## Global Constraints

- Background: transparent (no Gray300 fill)
- Text + border: `ColorsDriveBit.Blue` / `CSSColors.Blue` (`#2962FF`)
- Border width: `1.5.dp` / `1.5.px` solid
- Radius `6`, padding `4×8`, font `12` / weight `600`, uppercase — unchanged
- All badge types share one style (no per-type colors)
- Do not change `VerificationLabels` copy
- Iron Law: failing test first, then minimal implementation
- No comments unless required
- Stage new project files with `git add` as created; commit only at each task’s commit step

---

## File structure

| File | Role |
|------|------|
| `UI-Components/.../theme/VerificationBadgeStyle.kt` | Shared tokens: Blue content/border, border width 1.5 |
| `UI-Components/.../VerificationBadgeStyleTest.kt` | Unit test for tokens |
| `UI-Components/build.gradle.kts` | Enable `commonTest` + kotlin.test |
| `UI-Components/.../VerificationBadge.kt` | Mobile/shared Compose: outline Blue, no gray fill |
| `DrivebitWeb/.../VerificationBadge.kt` | Web DOM: CSS outline Blue, no gray fill |

---

### Task 1: Style tokens + Mobile/shared Compose badge

**Files:**
- Create: `UI-Components/src/commonMain/kotlin/my/drivebit/ui/theme/VerificationBadgeStyle.kt`
- Create: `UI-Components/src/commonTest/kotlin/my/drivebit/ui/theme/VerificationBadgeStyleTest.kt`
- Modify: `UI-Components/build.gradle.kts` — add `commonTest` with `kotlin("test")`
- Modify: `UI-Components/src/commonMain/kotlin/my/drivebit/ui/components/VerificationBadge.kt`

**Interfaces:**
- Consumes: `ColorsDriveBit.Blue`
- Produces: `VerificationBadgeStyle.contentColor`, `VerificationBadgeStyle.borderColor`, `VerificationBadgeStyle.borderWidthDp`

- [ ] **Step 1: Enable commonTest in UI-Components**

In `UI-Components/build.gradle.kts`, inside `sourceSets { ... }`, after `jsMain.dependencies`, add:

```kotlin
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
```

- [ ] **Step 2: Write the failing test**

Create `UI-Components/src/commonTest/kotlin/my/drivebit/ui/theme/VerificationBadgeStyleTest.kt`:

```kotlin
package my.drivebit.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class VerificationBadgeStyleTest {
    @Test
    fun `uses brand blue for content and border`() {
        assertEquals(ColorsDriveBit.Blue, VerificationBadgeStyle.contentColor)
        assertEquals(ColorsDriveBit.Blue, VerificationBadgeStyle.borderColor)
        assertEquals(1.5f, VerificationBadgeStyle.borderWidthDp)
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :UI-Components:jsTest --tests "my.drivebit.ui.theme.VerificationBadgeStyleTest"`

Expected: FAIL (unresolved `VerificationBadgeStyle` or similar)

- [ ] **Step 4: Add style tokens**

Create `UI-Components/src/commonMain/kotlin/my/drivebit/ui/theme/VerificationBadgeStyle.kt`:

```kotlin
package my.drivebit.ui.theme

import androidx.compose.ui.graphics.Color

object VerificationBadgeStyle {
    val contentColor: Color = ColorsDriveBit.Blue
    val borderColor: Color = ColorsDriveBit.Blue
    const val borderWidthDp: Float = 1.5f
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :UI-Components:jsTest --tests "my.drivebit.ui.theme.VerificationBadgeStyleTest"`

Expected: PASS / BUILD SUCCESSFUL

- [ ] **Step 6: Update Compose VerificationBadge**

Replace body of `VerificationBadge` in `UI-Components/.../VerificationBadge.kt` with:

```kotlin
@Composable
fun VerificationBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier =
            modifier
                .border(
                    width = VerificationBadgeStyle.borderWidthDp.dp,
                    color = VerificationBadgeStyle.borderColor,
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        color = VerificationBadgeStyle.contentColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
```

Add imports:

```kotlin
import androidx.compose.foundation.border
import my.drivebit.ui.theme.VerificationBadgeStyle
```

Remove unused `background` import and `ColorsDriveBit` import if unused.

`VerificationBadgeRow` stays unchanged.

- [ ] **Step 7: Commit**

```bash
git add \
  UI-Components/build.gradle.kts \
  UI-Components/src/commonMain/kotlin/my/drivebit/ui/theme/VerificationBadgeStyle.kt \
  UI-Components/src/commonTest/kotlin/my/drivebit/ui/theme/VerificationBadgeStyleTest.kt \
  UI-Components/src/commonMain/kotlin/my/drivebit/ui/components/VerificationBadge.kt
git commit -m "$(cat <<'EOF'
feat(ui): outline blue verification badges on Compose

EOF
)"
```

---

### Task 2: DrivebitWeb Compose HTML badge

**Files:**
- Modify: `DrivebitWeb/src/jsMain/kotlin/my/drivebit/components/VerificationBadge.kt`

**Interfaces:**
- Consumes: `CSSColors.Blue` (same hex as `ColorsDriveBit.Blue` / tokens)
- Produces: DOM badge with transparent fill, Blue text, `1.5px` solid Blue border

- [ ] **Step 1: Update Web VerificationBadge styles**

Replace `VerificationBadge` style block so it matches variant D:

```kotlin
@Composable
fun VerificationBadge(text: String) {
    val displayText = text.uppercase()
    Span({
        style {
            padding(4.px, 8.px)
            borderRadius(6.px)
            border(1.5.px, LineStyle.Solid, CSSColors.Blue)
            color(CSSColors.Blue)
            fontSize(12.px)
            fontWeight("600")
        }
    }) {
        Text(displayText)
    }
}
```

Add imports:

```kotlin
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.border
```

Remove `backgroundColor` usage (no gray fill). Do not set a solid background.

`VerificationBadgeRow` stays unchanged.

- [ ] **Step 2: Compile DrivebitWeb (narrow)**

Run: `./gradlew :DrivebitWeb:compileKotlinJs`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add DrivebitWeb/src/jsMain/kotlin/my/drivebit/components/VerificationBadge.kt
git commit -m "$(cat <<'EOF'
feat(web): outline blue verification badges

EOF
)"
```

---

## Spec coverage (self-review)

| Spec item | Task |
|-----------|------|
| Transparent background | 1 + 2 |
| Blue text + Blue border | 1 tokens + Compose + Web |
| Border 1.5 | 1 tokens + Compose + Web |
| Radius/padding/font unchanged | 1 + 2 |
| Single style for all types | both components keep one style |
| No label copy changes | no `VerificationLabels` edits |
| Web + Mobile | Task 1 Mobile, Task 2 Web |

Placeholder scan: none. Type names consistent: `VerificationBadgeStyle.contentColor` / `borderColor` / `borderWidthDp`.
