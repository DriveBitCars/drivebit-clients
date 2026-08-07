# Car Photo Order Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let car owners reorder photos on `CarPhotosPage` with ↑ / ↓, persisted via `PUT /Photo/car/my/{carId}/order`, with optimistic UI and public lists respecting `sortOrder`.

**Architecture:** Add `sortOrder` to photo DTOs and a `Photo.reorderCarPhotos` network call. `CarPhotosViewModel.movePhoto` swaps optimistically, sends the full `photoIds` list, then applies the server response or rolls back. Owner UI adds ↑ / ↓ and a «Главное» badge; search/detail sanitize paths sort photos by `sortOrder`.

**Tech Stack:** Kotlin Multiplatform, Ktor client, kotlinx.serialization, Compose HTML (`DrivebitWeb`), kotlin.test / coroutines-test

**Spec:** `docs/superpowers/specs/2026-08-06-car-photo-order-design.md`

## Global Constraints

- Endpoint: `PUT ${DEFAULT_BASE_URL}Photo/car/my/{carId}/order` with body `{ "photoIds": [...] }` (full list, no duplicates)
- First photo after order = cover; badge copy exactly `Главное`
- Optimistic swap; on failure restore previous list and set `uploadError`
- Block further moves while `isReordering == true`
- Admin reorder endpoint is out of scope
- Iron Law: failing test first, then minimal implementation
- No comments unless required
- Stage new project files with `git add` as created; commit only at each task’s commit step
- Do not run `./gradlew clean`

---

## File structure

| File | Role |
|------|------|
| `Network/.../Photo.kt` | `sortOrder` on `CarPhotoResponse`, `ReorderCarPhotosRequest`, `reorderCarPhotos`, sort after fetch |
| `Network/.../Car.kt` | `sortOrder` on `CarPhotoItem`; sort in `sanitizeCarItems` / `sanitizeCarDetail` |
| `SearchCore/.../SanitizeSearchCarPhotoUrls.kt` | Sort photos by `sortOrder` after sanitize |
| `Network/.../CarPhotoSortOrderTest.kt` | DTO decode + sort helper tests |
| `Network/.../ReorderCarPhotosTest.kt` | MockEngine PUT path/body/response |
| `CommonViewModels/.../CarPhotosViewModel.kt` | `PhotoMoveDirection`, `movePhoto`, `isReordering` |
| `CommonViewModels/.../CarPhotosViewModelTest.kt` | Reorder scenarios + mock method |
| Other `Photo` test fakes | Stub `reorderCarPhotos` |
| `DrivebitWeb/.../CarPhotosPage.kt` | ↑ / ↓ + «Главное» |

---

### Task 1: Photo DTOs — `sortOrder` + sort helper

**Files:**
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/services/Photo.kt`
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/services/Car.kt` (`CarPhotoItem`)
- Create: `Network/src/commonTest/kotlin/my/drivebit/network/services/CarPhotoSortOrderTest.kt`
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/services/Photo.kt` — sort in `getCarPhotos` / upload mapping; pass `sortOrder` in carService fallback

**Interfaces:**
- Consumes: existing `CarPhotoResponse`, `CarPhotoItem`
- Produces: `sortOrder: Int = 0` on both; `fun <T> List<T>.sortedByPhotoSortOrder(selector)` or two extensions `List<CarPhotoResponse>.sortedBySortOrder()` / `List<CarPhotoItem>.sortedBySortOrder()`

- [ ] **Step 1: Write the failing test**

Create `Network/src/commonTest/kotlin/my/drivebit/network/services/CarPhotoSortOrderTest.kt`:

```kotlin
package my.drivebit.network.services

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CarPhotoSortOrderTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Test
    fun `CarPhotoResponse decodes sortOrder`() {
        val photo =
            json.decodeFromString(
                CarPhotoResponse.serializer(),
                """{"id":1,"url":"https://cdn.example/a.jpg","uploadDate":"2026-01-01","sortOrder":3}""",
            )
        assertEquals(3, photo.sortOrder)
    }

    @Test
    fun `CarPhotoItem decodes sortOrder`() {
        val photo =
            json.decodeFromString(
                CarPhotoItem.serializer(),
                """{"id":1,"url":"https://cdn.example/a.jpg","uploadDate":"2026-01-01","sortOrder":2}""",
            )
        assertEquals(2, photo.sortOrder)
    }

    @Test
    fun `sortedBySortOrder orders CarPhotoResponse ascending`() {
        val photos =
            listOf(
                CarPhotoResponse(id = 1, url = "a", uploadDate = "d", sortOrder = 3),
                CarPhotoResponse(id = 2, url = "b", uploadDate = "d", sortOrder = 1),
                CarPhotoResponse(id = 3, url = "c", uploadDate = "d", sortOrder = 2),
            )
        assertEquals(listOf(2, 3, 1), photos.sortedBySortOrder().map { it.id })
    }

    @Test
    fun `sortedBySortOrder orders CarPhotoItem ascending`() {
        val photos =
            listOf(
                CarPhotoItem(id = 1, url = "a", uploadDate = "d", sortOrder = 2),
                CarPhotoItem(id = 2, url = "b", uploadDate = "d", sortOrder = 1),
            )
        assertEquals(listOf(2, 1), photos.sortedBySortOrder().map { it.id })
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :Network:jsTest --tests "my.drivebit.network.services.CarPhotoSortOrderTest"`

Expected: FAIL (unresolved `sortOrder` / `sortedBySortOrder`)

- [ ] **Step 3: Minimal implementation**

In `CarPhotoResponse` and `CarPhotoItem`, add:

```kotlin
val sortOrder: Int = 0,
```

In `Photo.kt` (same file as `CarPhotoResponse`), add:

```kotlin
fun List<CarPhotoResponse>.sortedBySortOrder(): List<CarPhotoResponse> = sortedBy { it.sortOrder }

fun List<CarPhotoItem>.sortedBySortOrder(): List<CarPhotoItem> = sortedBy { it.sortOrder }
```

(`CarPhotoItem` extension may live in `Car.kt` next to the data class if imports are cleaner — either is fine; tests import `my.drivebit.network.services`.)

In `getCarPhotos`, after sanitize map:

```kotlin
photos.map { photo -> photo.withSanitizedUrls(::ensureHttpsUrl) }.sortedBySortOrder()
```

In the `carService` fallback `CarPhotoResponse(...)`, pass `sortOrder = photo.sortOrder`, then `.sortedBySortOrder()` on the mapped list.

In `uploadCarPhotos` return: `.map { ... }.sortedBySortOrder()`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :Network:jsTest --tests "my.drivebit.network.services.CarPhotoSortOrderTest"`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add Network/src/commonMain/kotlin/my/drivebit/network/services/Photo.kt \
  Network/src/commonMain/kotlin/my/drivebit/network/services/Car.kt \
  Network/src/commonTest/kotlin/my/drivebit/network/services/CarPhotoSortOrderTest.kt
git commit -m "$(cat <<'EOF'
feat(photos): decode sortOrder and sort photo lists

EOF
)"
```

---

### Task 2: Network — `reorderCarPhotos`

**Files:**
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/services/Photo.kt`
- Create: `Network/src/commonTest/kotlin/my/drivebit/network/services/ReorderCarPhotosTest.kt`
- Modify Photo test fakes (compile fix as soon as interface grows):
  - `CommonViewModels/src/commonTest/.../CarPhotosViewModelTest.kt` (`MockPhotoServiceForCarPhotos`)
  - `CommonViewModels/src/commonTest/.../CarDetailViewModelTest.kt` (`MockPhotoServiceForDetail`)
  - `CommonViewModels/src/commonTest/.../AvatarUploadViewModelTest.kt` (`MockPhoto`)
  - `Repositories/src/commonTest/.../AvatarRepositoryTest.kt` (`FakePhoto`)

**Interfaces:**
- Consumes: `HttpClient`, `DEFAULT_BASE_URL`, `parseResponse`, `withSanitizedUrls`, `sortedBySortOrder`
- Produces:

```kotlin
@Serializable
data class ReorderCarPhotosRequest(
    val photoIds: List<Int>,
)

// on Photo:
suspend fun reorderCarPhotos(carId: String, photoIds: List<Int>): List<CarPhotoResponse>
```

- [ ] **Step 1: Write the failing test**

Create `Network/src/commonTest/kotlin/my/drivebit/network/services/ReorderCarPhotosTest.kt`:

```kotlin
package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.readText
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReorderCarPhotosTest {
    @Test
    fun `reorderCarPhotos puts photoIds and returns sorted photos`() =
        runTest {
            var capturedMethod: HttpMethod? = null
            var capturedPath: String? = null
            var capturedBody: String? = null
            val carId = "11111111-1111-1111-1111-111111111111"
            val mockEngine =
                MockEngine { request ->
                    capturedMethod = request.method
                    capturedPath = request.url.encodedPath
                    capturedBody = request.body.toByteReadChannel().readText()
                    respond(
                        content =
                            """
                            [
                              {"id":2,"url":"https://cdn.example/b.jpg","uploadDate":"2026-01-01","sortOrder":1,"thumbnailUrl":null},
                              {"id":1,"url":"https://cdn.example/a.jpg","uploadDate":"2026-01-01","sortOrder":2,"thumbnailUrl":null}
                            ]
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val photo = PhotoImpl(HttpClient(mockEngine))

            val result = photo.reorderCarPhotos(carId, listOf(2, 1))

            assertEquals(HttpMethod.Put, capturedMethod)
            assertTrue(capturedPath!!.endsWith("/Photo/car/my/$carId/order"))
            val bodyJson = Json { ignoreUnknownKeys = true }.decodeFromString(ReorderCarPhotosRequest.serializer(), capturedBody!!)
            assertEquals(listOf(2, 1), bodyJson.photoIds)
            assertEquals(listOf(2, 1), result.map { it.id })
            assertEquals(listOf(1, 2), result.map { it.sortOrder })
        }
}
```

Body capture: use Ktor MockEngine `OutgoingContent` reading. If `toByteReadChannel().readText()` fails to compile, use:

```kotlin
import io.ktor.http.content.TextContent
// ...
capturedBody = (request.body as TextContent).text
```

Do not drop the `photoIds` body assertion.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :Network:jsTest --tests "my.drivebit.network.services.ReorderCarPhotosTest"`

Expected: FAIL (unresolved `reorderCarPhotos` / `ReorderCarPhotosRequest`)

- [ ] **Step 3: Implement API + stub fakes**

Add to `Photo` interface and `PhotoImpl`:

```kotlin
@Serializable
data class ReorderCarPhotosRequest(
    val photoIds: List<Int>,
)

// interface
suspend fun reorderCarPhotos(
    carId: String,
    photoIds: List<Int>,
): List<CarPhotoResponse>

// PhotoImpl
override suspend fun reorderCarPhotos(
    carId: String,
    photoIds: List<Int>,
): List<CarPhotoResponse> {
    if (carId.isBlank()) {
        throw IllegalArgumentException("Car ID cannot be empty")
    }
    val url = "${DEFAULT_BASE_URL}Photo/car/my/$carId/order"
    val response =
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(ReorderCarPhotosRequest(photoIds = photoIds))
        }
    val photos: List<CarPhotoResponse> = response.parseResponse()
    return photos.map { it.withSanitizedUrls(::ensureHttpsUrl) }.sortedBySortOrder()
}
```

Add imports: `put`, `contentType`, `ContentType`.

Stub on every `Photo` fake:

```kotlin
override suspend fun reorderCarPhotos(
    carId: String,
    photoIds: List<Int>,
): List<CarPhotoResponse> = emptyList()
```

For `MockPhotoServiceForCarPhotos`, implement for Task 3 readiness:

```kotlin
var reorderShouldFail = false
var lastReorderPhotoIds: List<Int>? = null

override suspend fun reorderCarPhotos(
    carId: String,
    photoIds: List<Int>,
): List<CarPhotoResponse> {
    lastReorderPhotoIds = photoIds
    if (shouldThrowNetworkException) {
        throw NetworkException(networkExceptionStatusCode, errorMessage)
    }
    if (shouldThrowError || reorderShouldFail) {
        throw Exception(errorMessage)
    }
    photos =
        photoIds.mapIndexed { index, id ->
            val existing = photos.first { it.id == id }
            existing.copy(sortOrder = index + 1)
        }
    return photos
}
```

- [ ] **Step 4: Run tests**

Run:

```bash
./gradlew :Network:jsTest --tests "my.drivebit.network.services.ReorderCarPhotosTest" \
  :CommonViewModels:jsTest --tests "my.drivebit.viewmodels.CarPhotosViewModelTest" \
  :CommonViewModels:jsTest --tests "my.drivebit.viewmodels.AvatarUploadViewModelTest" \
  :Repositories:jsTest --tests "my.drivebit.repositories.AvatarRepositoryTest"
```

Expected: PASS (existing tests still green; new reorder test green)

- [ ] **Step 5: Commit**

```bash
git add Network/src/commonMain/kotlin/my/drivebit/network/services/Photo.kt \
  Network/src/commonTest/kotlin/my/drivebit/network/services/ReorderCarPhotosTest.kt \
  CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/CarPhotosViewModelTest.kt \
  CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/CarDetailViewModelTest.kt \
  CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/AvatarUploadViewModelTest.kt \
  Repositories/src/commonTest/kotlin/my/drivebit/repositories/AvatarRepositoryTest.kt
git commit -m "$(cat <<'EOF'
feat(photos): add reorderCarPhotos PUT client

EOF
)"
```

---

### Task 3: ViewModel — optimistic `movePhoto`

**Files:**
- Modify: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/CarPhotosViewModel.kt`
- Modify: `CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/CarPhotosViewModelTest.kt`

**Interfaces:**
- Consumes: `Photo.reorderCarPhotos`, `CarPhotosState.Success`
- Produces:

```kotlin
enum class PhotoMoveDirection { Up, Down }

// CarPhotosState.Success
val isReordering: Boolean = false

// CarPhotosViewModel
fun movePhoto(carId: String, photoId: Int, direction: PhotoMoveDirection)
```

- [ ] **Step 1: Write failing tests**

Append to `CarPhotosViewModelTest.kt` (give mock photos explicit `sortOrder` 1 and 2 in the default list):

```kotlin
@Test
fun `movePhoto Down swaps with next and calls reorder with full ids`() =
    runTest(StandardTestDispatcher()) {
        val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
        val mock = MockPhotoServiceForCarPhotos()
        val vm = CarPhotosViewModelImpl(mock, testScope)

        vm.loadPhotos("car-1")
        advanceUntilIdle()

        vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Down)
        advanceUntilIdle()

        val state = assertIs<CarPhotosState.Success>(vm.state.value)
        assertEquals(listOf(2, 1), state.photos.map { it.id })
        assertEquals(listOf(2, 1), mock.lastReorderPhotoIds)
        assertFalse(state.isReordering)
        assertEquals(null, state.uploadError)
    }

@Test
fun `movePhoto Up on first photo is no-op`() =
    runTest(StandardTestDispatcher()) {
        val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
        val mock = MockPhotoServiceForCarPhotos()
        val vm = CarPhotosViewModelImpl(mock, testScope)

        vm.loadPhotos("car-1")
        advanceUntilIdle()

        vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Up)
        advanceUntilIdle()

        assertEquals(null, mock.lastReorderPhotoIds)
        val state = assertIs<CarPhotosState.Success>(vm.state.value)
        assertEquals(listOf(1, 2), state.photos.map { it.id })
    }

@Test
fun `movePhoto rolls back and sets uploadError when reorder fails`() =
    runTest(StandardTestDispatcher()) {
        val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
        val mock =
            MockPhotoServiceForCarPhotos().apply {
                shouldThrowError = true
                errorMessage = ""
            }
        val vm = CarPhotosViewModelImpl(mock, testScope)

        mock.shouldThrowError = false
        vm.loadPhotos("car-1")
        advanceUntilIdle()
        mock.shouldThrowError = true

        vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Down)
        advanceUntilIdle()

        val state = assertIs<CarPhotosState.Success>(vm.state.value)
        assertEquals(listOf(1, 2), state.photos.map { it.id })
        assertEquals("Не удалось изменить порядок фотографий", state.uploadError)
        assertFalse(state.isReordering)
    }

@Test
fun `movePhoto ignored while isReordering`() =
    runTest(StandardTestDispatcher()) {
        val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val mock =
            object : MockPhotoServiceForCarPhotos() {
                override suspend fun reorderCarPhotos(
                    carId: String,
                    photoIds: List<Int>,
                ): List<CarPhotoResponse> {
                    lastReorderPhotoIds = photoIds
                    gate.await()
                    return photoIds.mapIndexed { index, id ->
                        photos.first { it.id == id }.copy(sortOrder = index + 1)
                    }.also { photos = it }
                }
            }
        val vm = CarPhotosViewModelImpl(mock, testScope)

        vm.loadPhotos("car-1")
        advanceUntilIdle()

        vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Down)
        testScheduler.runCurrent()
        val mid = assertIs<CarPhotosState.Success>(vm.state.value)
        assertTrue(mid.isReordering)

        vm.movePhoto("car-1", photoId = 2, direction = PhotoMoveDirection.Down)
        testScheduler.runCurrent()
        assertEquals(listOf(2, 1), mock.lastReorderPhotoIds)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(assertIs<CarPhotosState.Success>(vm.state.value).isReordering)
    }
```

Also update default mock photos to include `sortOrder = 1` / `2`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :CommonViewModels:jsTest --tests "my.drivebit.viewmodels.CarPhotosViewModelTest"`

Expected: FAIL (unresolved `movePhoto` / `PhotoMoveDirection` / `isReordering`)

- [ ] **Step 3: Implement ViewModel**

```kotlin
enum class PhotoMoveDirection {
    Up,
    Down,
}

// Success:
data class Success(
    val photos: List<CarPhotoResponse>,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val isReordering: Boolean = false,
)

fun movePhoto(
    carId: String,
    photoId: Int,
    direction: PhotoMoveDirection,
)

// Impl sketch:
override fun movePhoto(
    carId: String,
    photoId: Int,
    direction: PhotoMoveDirection,
) {
    val current = _state.value
    if (current !is CarPhotosState.Success || current.isReordering) return
    val index = current.photos.indexOfFirst { it.id == photoId }
    if (index < 0) return
    val swapWith =
        when (direction) {
            PhotoMoveDirection.Up -> index - 1
            PhotoMoveDirection.Down -> index + 1
        }
    if (swapWith !in current.photos.indices) return

    val previous = current.photos
    val reordered = previous.toMutableList().apply {
        val tmp = this[index]
        this[index] = this[swapWith]
        this[swapWith] = tmp
    }.mapIndexed { i, photo -> photo.copy(sortOrder = i + 1) }

    coroutineScope.launch {
        _state.value =
            current.copy(
                photos = reordered,
                isReordering = true,
                uploadError = null,
            )
        runCatching {
            photoService.reorderCarPhotos(carId, reordered.map { it.id })
        }.onSuccess { serverPhotos ->
            _state.update {
                when (it) {
                    is CarPhotosState.Success ->
                        it.copy(
                            photos = serverPhotos.sortedBySortOrder(),
                            isReordering = false,
                            uploadError = null,
                        )
                    else -> it
                }
            }
        }.onFailure { e ->
            val message =
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось изменить порядок фотографий",
                )
            _state.update {
                when (it) {
                    is CarPhotosState.Success ->
                        it.copy(
                            photos = previous,
                            isReordering = false,
                            uploadError = message,
                        )
                    else -> it
                }
            }
        }
    }
}
```

Import `sortedBySortOrder` from `my.drivebit.network.services`.

Ensure `loadPhotos` stores `getCarPhotos` result already sorted (Task 1).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :CommonViewModels:jsTest --tests "my.drivebit.viewmodels.CarPhotosViewModelTest"`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/CarPhotosViewModel.kt \
  CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/CarPhotosViewModelTest.kt
git commit -m "$(cat <<'EOF'
feat(photos): optimistic movePhoto reorder in ViewModel

EOF
)"
```

---

### Task 4: Public/search lists sort by `sortOrder`

**Files:**
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/services/Car.kt` — `sanitizeCarItems`, `sanitizeCarDetail`
- Modify: `SearchCore/src/commonMain/kotlin/my/drivebit/search/SanitizeSearchCarPhotoUrls.kt`
- Modify: `SearchCore/src/commonTest/kotlin/my/drivebit/search/SanitizeSearchCarPhotoUrlsTest.kt`

**Interfaces:**
- Consumes: `List<CarPhotoItem>.sortedBySortOrder()`
- Produces: sanitized car/search items with photos ordered by `sortOrder`

- [ ] **Step 1: Write failing test**

In `SanitizeSearchCarPhotoUrlsTest.kt` add:

```kotlin
@Test
fun sanitizeSearchCarPhotoUrls_sortsPhotosBySortOrder() {
    val photosOutOfOrder =
        listOf(
            CarPhotoItem(id = 10, url = "/publicbct/a.jpg", uploadDate = "2026-01-01", sortOrder = 2),
            CarPhotoItem(id = 11, url = "/publicbct/b.jpg", uploadDate = "2026-01-01", sortOrder = 1),
        )
    val car =
        CarItem(
            id = "1",
            year = 2020,
            price = 5000.0,
            photos = photosOutOfOrder,
            general =
                CarGeneral(
                    brandName = "BMW",
                    modelName = "X5",
                    seats = 5,
                    address = CarAddress(),
                    photos = photosOutOfOrder,
                ),
        )

    val sanitized = sanitizeSearchCarPhotoUrls(listOf(car)).single()

    assertEquals(listOf(11, 10), sanitized.photos.map { it.id })
    assertEquals(listOf(11, 10), sanitized.general.photos.map { it.id })
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :SearchCore:jsTest --tests "my.drivebit.search.SanitizeSearchCarPhotoUrlsTest"`

Expected: FAIL (order not sorted)

- [ ] **Step 3: Implement sorting**

In `sanitizeCarItems` / `sanitizeCarDetail`:

```kotlin
photos = allPhotos.map { it.withSanitizedUrls() }.sortedBySortOrder(),
// and for general.photos:
photos = photosFromGeneral.map { it.withSanitizedUrls() }.sortedBySortOrder(),
```

In `sanitizeSearchCarPhotoUrls`, after `withSanitizedUrls()`, apply `.sortedBySortOrder()` to both photo lists the same way.

- [ ] **Step 4: Run tests**

Run:

```bash
./gradlew :SearchCore:jsTest --tests "my.drivebit.search.SanitizeSearchCarPhotoUrlsTest" \
  :Network:jsTest --tests "my.drivebit.network.services.CarItemPhotosTest"
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add Network/src/commonMain/kotlin/my/drivebit/network/services/Car.kt \
  SearchCore/src/commonMain/kotlin/my/drivebit/search/SanitizeSearchCarPhotoUrls.kt \
  SearchCore/src/commonTest/kotlin/my/drivebit/search/SanitizeSearchCarPhotoUrlsTest.kt
git commit -m "$(cat <<'EOF'
feat(photos): sort public and search photo lists by sortOrder

EOF
)"
```

---

### Task 5: Owner UI — ↑ / ↓ and «Главное»

**Files:**
- Modify: `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/CarPhotosPage.kt`

**Interfaces:**
- Consumes: `CarPhotosViewModel.movePhoto`, `PhotoMoveDirection`, `Success.isReordering`, `Success.uploadError`
- Produces: owner grid controls (no new public API)

- [ ] **Step 1: Add controls on each photo tile**

Inside the existing `currentState.photos.forEachIndexed { index, photo -> ... }` (switch from `forEach` if needed):

- Overlay column at bottom-left (or top-left under the menu) with two small circular buttons `↑` / `↓`.
- `↑` enabled only when `index > 0 && !currentState.isReordering`
- `↓` enabled only when `index < currentState.photos.lastIndex && !currentState.isReordering`
- `onClick` → `viewModel.movePhoto(carIdParam, photo.id, PhotoMoveDirection.Up/Down)`
- Style consistently with the existing ⋮ button (white circle, gray border, small shadow).
- If `index == 0`, show a small badge over the image: text `Главное`, dark semi-transparent background, white 12px text, top-left corner.

Keep the ⋮ delete menu unchanged. Keep existing `TextError(currentState.uploadError)` for reorder failures.

Example badge block:

```kotlin
if (index == 0) {
    Div({
        style {
            position(Position.Absolute)
            top(8.px)
            left(8.px)
            backgroundColor(rgba(0, 0, 0, 0.65))
            color(CSSColors.White)
            fontSize(12.px)
            padding(4.px, 8.px)
            borderRadius(6.px)
        }
    }) {
        Text("Главное")
    }
}
```

- [ ] **Step 2: Compile Web**

Run: `./gradlew :DrivebitWeb:compileKotlinJs`

Expected: exit code 0

- [ ] **Step 3: Commit**

```bash
git add DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/CarPhotosPage.kt
git commit -m "$(cat <<'EOF'
feat(photos): add up/down controls and main badge on owner grid

EOF
)"
```

---

## Spec coverage checklist

| Spec requirement | Task |
|------------------|------|
| `sortOrder` on DTOs | 1 |
| `PUT …/order` + `{ photoIds }` | 2 |
| Optimistic `movePhoto` + rollback + `uploadError` | 3 |
| `isReordering` blocks parallel moves | 3 |
| Public/search sort by `sortOrder` | 4 |
| ↑ / ↓ + «Главное» on `CarPhotosPage` | 5 |
| No admin endpoint / no DnD / no separate screen | n/a (non-goals) |

## Self-review notes

- No TBD placeholders; body capture in Task 2 may need a one-line Ktor API adjust — keep the `photoIds` assertion.
- `PhotoMoveDirection` / `reorderCarPhotos` / `isReordering` names are consistent across tasks.
- Task 2 updates all `Photo` fakes so the tree compiles before Task 3.
