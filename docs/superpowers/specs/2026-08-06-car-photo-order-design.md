# Car photo order (owner) — design

Date: 2026-08-06  
Status: approved in brainstorming  
Backend: `PUT /Photo/car/my/{carId}/order` (`ReorderCarPhotosRequest.photoIds`)

## Problem

Owners can upload and delete car photos, but cannot choose which photo is first (cover) or the gallery order. The backend already stores `sortOrder` and exposes a reorder endpoint; the client ignores both.

## Goals

- Let the car owner reorder photos on the existing `CarPhotosPage` with ↑ / ↓.
- Persist order via `PUT /Photo/car/my/{carId}/order` with the full `photoIds` list.
- Optimistic UI: swap locally first, rollback on failure.
- Ensure public/search/detail lists respect `sortOrder` when present.
- First photo in order = cover (badge «Главное» on owner grid).

## Non-goals

- Drag-and-drop (possible later on the same API).
- Separate «photo order» screen.
- Admin `PUT /Admin/cars/{carId}/photos/order`.
- Changing upload/delete flows beyond living next to reorder.
- Reordering on the renter-facing gallery UI (read-only order from API).

## Decision

**↑ / ↓ on each photo + optimistic `movePhoto` in ViewModel**

| Piece | Choice |
|-------|--------|
| Interaction | ↑ / ↓ buttons on each tile (not ⋮ menu) |
| Persistence | Immediate `PUT` after each move |
| UI update | Optimistic swap; rollback + error message on failure |
| Cover | Index 0 / lowest `sortOrder` = «Главное» |
| Concurrency | Block further moves while `isReordering` |

### Rejected alternatives

- «Сделать главным» only — cannot fine-tune middle positions.
- Drag-and-drop now — same API, heavier Compose Web work; defer.

## Architecture

```text
CarPhotosPage
  → CarPhotosViewModel.movePhoto(carId, photoId, Up|Down)
      → optimistic reorder in Success.photos
      → Photo.reorderCarPhotos(carId, photoIds)
      → apply server list OR keep optimistic; on failure rollback
```

### Network / models

- Add `sortOrder: Int = 0` to `CarPhotoResponse` and `CarPhotoItem`.
- After fetch (owner list and public car photos), sort by `sortOrder` ascending as a defensive client rule.
- `Photo.reorderCarPhotos(carId, photoIds: List<Int>): List<CarPhotoResponse>`
  - `PUT ${DEFAULT_BASE_URL}Photo/car/my/{carId}/order`
  - Body: `{ "photoIds": [...] }` (all photo ids for that car, no duplicates).
- Sanitize URLs on the response the same way as `getCarPhotos`.

### ViewModel

- Extend `CarPhotosState.Success` with `isReordering: Boolean = false`.
- Reuse existing `uploadError` for reorder failures (same slot already used by delete).
- `movePhoto(carId, photoId, direction)`:
  - no-op if not `Success`, if reordering, or if move would go past ends;
  - snapshot previous list;
  - swap with neighbor; renumber local `sortOrder` to `1..N`;
  - set `isReordering = true`, clear `uploadError`;
  - call `reorderCarPhotos` with full id list in new order;
  - on success: set photos from response (sorted by `sortOrder`), `isReordering = false`;
  - on failure: restore snapshot, `isReordering = false`, set `uploadError` via `ErrorHandler`.

UI does not own order state; it only calls `movePhoto` / `deletePhoto` / `uploadPhotos`.

### UI (`CarPhotosPage`)

- On each photo tile: ↑ and ↓ overlays (separate from ⋮ delete menu).
- Disable/hide ↑ on first, ↓ on last; disable both while `isReordering`.
- Badge «Главное» on the first photo.
- Show reorder error in the same error area used for upload failures.

### Public surfaces

- Search cards, car detail gallery, and owner previews already use list order / `previewUrl()`.
- After decoding photos, sort by `sortOrder` so cover stays correct if the API ever returns unsorted.

## Error handling

- Empty / duplicate / incomplete `photoIds` are backend validation errors — client always sends the full current Success id list, so these should not occur in normal use.
- Network / 4xx / 5xx → rollback + user-visible message.
- Ignore extra clicks while `isReordering`.

## Tests

- `CarPhotosViewModel`: move up/down changes order; edge no-ops; API failure restores previous order; concurrent move ignored while reordering.
- Network: request body serializes `photoIds`; DTO parses `sortOrder`.
- Optional: unit for sort-by-`sortOrder` helper if extracted.

## Files to change

- `Network/.../Photo.kt` — model `sortOrder`, `reorderCarPhotos`
- `Network/.../Car.kt` — `CarPhotoItem.sortOrder` + defensive sort where lists are mapped
- `CommonViewModels/.../CarPhotosViewModel.kt` — `movePhoto`, state flags
- `CommonViewModels/.../CarPhotosViewModelTest.kt` — reorder scenarios
- `DrivebitWeb/.../CarPhotosPage.kt` — ↑ / ↓ + «Главное»
- Network tests for serialization / `sortOrder` as needed

## Out of scope follow-ups

- Drag-and-drop reorder on the same endpoint.
- Keyboard accessibility polish beyond button focus.
