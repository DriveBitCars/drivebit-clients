# Inspection act preserve fields + dashboard photo — Plan

> **For agentic workers:** Implement TDD inline; commit after green tests.

**Goal:** After photo upload keep fuel/mileage/comment; require own Car+Dashboard photos before sign.

**Architecture:** Fix `InspectionActViewModel.applyAct` to preserve local inputs; gate `sign()` with photo helper.

**Tech stack:** Kotlin Multiplatform ViewModel + existing Network DTOs/tests.

---

### Task 1: Failing tests

**Files:**
- Modify: `CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/InspectionActViewModelTest.kt`

- [ ] Test: upload photos does not clear fuel/mileage/comment inputs
- [ ] Test: sign without own Dashboard photo fails and does not call API
- [ ] Run tests — expect FAIL

### Task 2: Preserve inputs in applyAct

**Files:**
- Modify: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/InspectionActViewModel.kt`

- [ ] When applying act, keep previous non-blank inputs if DTO fields empty
- [ ] Run upload test — PASS

### Task 3: Require Car + Dashboard before sign

**Files:**
- Modify: `Network/.../InspectionAct.kt` (helper) + ViewModel `sign()`
- Modify: tests

- [ ] Helper: current user has ≥1 Car and ≥1 Dashboard photo by authorId
- [ ] `sign()` shows error and returns if missing
- [ ] Run all InspectionActViewModel tests — PASS
- [ ] Commit
