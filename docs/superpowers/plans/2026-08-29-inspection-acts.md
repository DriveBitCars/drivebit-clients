# Inspection Acts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline execution in this session). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать на Web и Mobile полный сценарий актов осмотра автомобиля: передача/возврат, метрики, комментарии, фотографии, подписи и PDF.

**Architecture:** Добавить общий `InspectionAct` Network-сервис с DTO, enum и multipart-операциями. Добавить общий `InspectionActViewModel`, который владеет состоянием акта, черновиками полей, разрешениями, блокировкой повторных действий и PDF-эффектом. Web и Mobile получают отдельные Compose-экраны, но используют один сервис и одну ViewModel; кнопки входа добавляются в существующие карточки бронирований.

**Tech Stack:** Kotlin Multiplatform, Ktor client, kotlinx.serialization, Kotlin Coroutines/Flow, Koin, Compose Multiplatform, Compose HTML (`DrivebitWeb`), Voyager (`Mobile`), kotlin.test / coroutines-test.

**Spec:** `docs/superpowers/specs/2026-08-29-inspection-acts-design.md`

## Global Constraints

- Endpoint’ы пользователя: `/Booking/{bookingId}/inspection-acts/{type}` и суффиксы `metrics`, `comment`, `photos`, `photos/{photoId}`, `sign-as-owner`, `sign-as-renter`, `download`
- Типы акта: `Handover`, `Return`; типы фотографии: `Car`, `Dashboard`, `Other`
- Разрешения редактирования и подписания брать из `canEditOwnerFields`, `canEditRenterFields`, `canSignAsOwner`, `canSignAsRenter`
- Состояние и черновики формы хранятся в ViewModel; UI не использует `remember { mutableStateOf(...) }` для бизнес-состояния
- Ответ успешной mutation-операции заменяет текущий DTO акта
- Ошибка сохраняет последний корректный акт и показывает извлечённое сообщение бэкенда
- Повторная одинаковая операция блокируется до завершения текущего запроса
- Тест сначала должен падать, затем production-код делает его зелёным
- Новые project-файлы сразу добавлять в staging; commit/push выполнять только по отдельной просьбе пользователя
- Не запускать `./gradlew clean`

---

## File structure

| Файл | Ответственность |
|------|----------------|
| `Network/src/commonMain/kotlin/my/drivebit/network/services/InspectionAct.kt` | DTO, enum, request-модели, `InspectionAct` и HTTP-реализация |
| `Network/src/commonMain/kotlin/my/drivebit/network/di/NetworkModule.kt` | Koin binding `InspectionAct` |
| `Network/src/commonTest/kotlin/my/drivebit/network/services/InspectionActTest.kt` | MockEngine-тесты URL, методов, JSON, multipart и DTO |
| `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/InspectionActViewModel.kt` | StateFlow-состояние, черновики, intents, ошибки и PDF effect |
| `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/di/ViewModelsModule.kt` | Factory ViewModel с `bookingId` и `InspectionActType` |
| `CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/InspectionActViewModelTest.kt` | TDD-покрытие полного сценария ViewModel |
| `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/InspectionActPage.kt` | Web-страница акта, DOM file picker и временная PDF-ссылка |
| `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/MyBookingsPage.kt` | Кнопки актов для роли арендатора |
| `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/MyDealsPage.kt` | Кнопки актов для роли владельца |
| `WebShell/src/jsMain/kotlin/my/drivebit/navigation/AccountBundlePaths.kt` | Web account-route для акта |
| `accountApp/src/jsMain/kotlin/my/drivebit/clients/AccountAppContent.kt` | Рендер `InspectionActPage` по route |
| `WebShell/src/jsTest/kotlin/my/drivebit/navigation/InspectionActRouteTest.kt` | Проверка account-route и URL helper |
| `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/main/InspectionActScreen.kt` | Mobile Voyager screen и Compose UI |
| `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/main/tabs/TripsTab.kt` | Кнопки актов арендатора |
| `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/main/tabs/InboxTab.kt` | Кнопки актов владельца |

---

### Task 1: Network DTO и контракт сервиса

**Files:**
- Create: `Network/src/commonTest/kotlin/my/drivebit/network/services/InspectionActTest.kt`
- Create: `Network/src/commonMain/kotlin/my/drivebit/network/services/InspectionAct.kt`

**Interfaces:**
- Produces:

```kotlin
@Serializable
enum class InspectionActType { Handover, Return }

val supportedInspectionActTypes: List<InspectionActType> =
    listOf(InspectionActType.Handover, InspectionActType.Return)

@Serializable
enum class InspectionActStatus { None, Draft, AwaitingOwner, AwaitingRenter, SignedByBoth }

@Serializable
enum class InspectionPhotoKind { Car, Dashboard, Other }

interface InspectionAct {
    suspend fun get(bookingId: String, type: InspectionActType): BookingInspectionActDto
    suspend fun openOrCreate(bookingId: String, type: InspectionActType): BookingInspectionActDto
    suspend fun updateMetrics(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionMetricsRequest,
    ): BookingInspectionActDto
    suspend fun updateComment(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionCommentRequest,
    ): BookingInspectionActDto
    suspend fun uploadPhoto(
        bookingId: String,
        type: InspectionActType,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        kind: InspectionPhotoKind,
    ): BookingInspectionActPhotoDto
    suspend fun deletePhoto(bookingId: String, type: InspectionActType, photoId: String)
    suspend fun signAsOwner(bookingId: String, type: InspectionActType): BookingInspectionActDto
    suspend fun signAsRenter(bookingId: String, type: InspectionActType): BookingInspectionActDto
    suspend fun download(bookingId: String, type: InspectionActType): BookingInspectionActDownloadDto
}
```

- `BookingInspectionActDto` fields: `id`, `bookingId`, `type`, `actNumber`,
  nullable `fuelRemaining`, nullable `mileage`, nullable `ownerComment`,
  nullable `renterComment`, nullable signing timestamps, `isSignedByOwner`,
  `isSignedByRenter`, `isFullySigned`, `hasPdf`, `status`, `canEditOwnerFields`,
  `canEditRenterFields`, `canSignAsOwner`, `canSignAsRenter`, `createdAt`,
  `updatedAt`, nullable `photos`.
- `BookingInspectionActPhotoDto` fields: `id`, `authorId`, nullable
  `authorName`, `kind`, nullable `fileName`, nullable `url`, `uploadedAt`.
- `BookingInspectionActDownloadDto` fields: `actId`, `actNumber`, `type`,
  nullable `fileName`, nullable `downloadUrl`, `urlExpiresAt`.
- Request models:

```kotlin
@Serializable
data class UpdateInspectionMetricsRequest(
    val fuelRemaining: Int,
    val mileage: Int,
)

@Serializable
data class UpdateInspectionCommentRequest(
    val comment: String? = null,
)
```

- Each DTO property accepts the backend’s camelCase and PascalCase names with
  `@JsonNames` where existing Network models use that compatibility pattern.

- [ ] **Step 1: Write the failing DTO tests**

In `InspectionActTest.kt`, add tests that decode a complete JSON fixture and
assert every permission, signature, metric, comment, status, type and photo
field. Add a test that decodes a download response with nullable `downloadUrl`.

```kotlin
@Test
fun `inspection act decodes permissions photos and signing state`() {
    val act = json.decodeFromString(
        BookingInspectionActDto.serializer(),
        """
        {
          "id":"11111111-1111-1111-1111-111111111111",
          "bookingId":"22222222-2222-2222-2222-222222222222",
          "type":"Handover",
          "actNumber":42,
          "fuelRemaining":75,
          "mileage":120500,
          "ownerComment":"Есть царапина на двери",
          "renterComment":"Принял",
          "signedByOwnerAt":"2026-08-29T09:00:00Z",
          "signedByRenterAt":null,
          "isSignedByOwner":true,
          "isSignedByRenter":false,
          "isFullySigned":false,
          "hasPdf":false,
          "status":"AwaitingRenter",
          "canEditOwnerFields":false,
          "canEditRenterFields":true,
          "canSignAsOwner":false,
          "canSignAsRenter":true,
          "createdAt":"2026-08-29T08:00:00Z",
          "updatedAt":"2026-08-29T09:00:00Z",
          "photos":[
            {
              "id":"33333333-3333-3333-3333-333333333333",
              "authorId":"44444444-4444-4444-4444-444444444444",
              "authorName":"Владелец",
              "kind":"Dashboard",
              "fileName":"dashboard.jpg",
              "url":"/publicbct/inspection/dashboard.jpg",
              "uploadedAt":"2026-08-29T08:30:00Z"
            }
          ]
        }
        """.trimIndent(),
    )

    assertEquals(InspectionActType.Handover, act.type)
    assertEquals(InspectionActStatus.AwaitingRenter, act.status)
    assertEquals(75, act.fuelRemaining)
    assertEquals(120500, act.mileage)
    assertEquals(true, act.isSignedByOwner)
    assertEquals(true, act.canEditRenterFields)
    assertEquals(1, act.photos?.size)
    assertEquals(InspectionPhotoKind.Dashboard, act.photos?.single()?.kind)
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```bash
./gradlew :Network:jsTest
```

Expected: FAIL because the DTOs and service do not exist.

- [ ] **Step 3: Implement DTOs, enum and request models**

Create `InspectionAct.kt` with `@Serializable` models and `@JsonNames` aliases.
Keep nullable backend values nullable. Use `String` for photo and act UUIDs,
matching `BookingDTO` and the existing document APIs. Add
`supportedInspectionActTypes` as the single ordered source for the two booking
card actions used by Web and Mobile.

- [ ] **Step 4: Run the focused test and verify it passes**

Run the same `:Network:jsTest` command. Expected: PASS.

---

### Task 2: Network HTTP operations and DI

**Files:**
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/services/InspectionAct.kt`
- Modify: `Network/src/commonMain/kotlin/my/drivebit/network/di/NetworkModule.kt`
- Modify: `Network/src/commonTest/kotlin/my/drivebit/network/services/InspectionActTest.kt`

**Interfaces:**
- Consumes the `InspectionAct` interface and DTOs from Task 1.
- Produces `InspectionActImpl(authorizedHttpClient: HttpClient)`.
- Uses `${DEFAULT_BASE_URL}Booking/{bookingId}/inspection-acts/{type}` with
  `type.name` in the URL.

- [ ] **Step 1: Add failing MockEngine tests for URL and HTTP verb**

Cover these calls in separate tests:

```text
GET    /Booking/booking-1/inspection-acts/Handover
POST   /Booking/booking-1/inspection-acts/Return
PUT    .../Handover/metrics       body {"fuelRemaining":75,"mileage":120500}
PUT    .../Handover/comment       body {"comment":"Принял"}
POST   .../Return/photos           multipart File + Kind=Car
DELETE  .../Return/photos/photo-1
POST   .../Handover/sign-as-owner
POST   .../Return/sign-as-renter
GET    .../Handover/download
```

The MockEngine must assert `request.method`, `request.url.encodedPath`, and
JSON bodies for JSON operations. For multipart, assert the request content
contains `File`, the original filename, and `Kind` with the enum string.

- [ ] **Step 2: Run the focused tests and verify they fail**

Run:

```bash
./gradlew :Network:jsTest
```

Expected: FAIL because `InspectionActImpl` operations are not implemented.

- [ ] **Step 3: Implement HTTP methods**

Use the authorized Ktor client, `parseResponse()` for DTO responses, and
`consumeResponse()` for delete. Use `MultiPartFormDataContent` with:

```kotlin
append(
    "File",
    fileBytes,
    Headers.build {
        append(HttpHeaders.ContentType, contentType)
        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
    },
)
append("Kind", kind.name)
```

Validate non-blank `bookingId` and throw `IllegalArgumentException` before a
request. Convert the download URL with the same MinIO/browser URL helper used
by `BookingContractDownloadDto` when the UI needs to open it.

- [ ] **Step 4: Register the service in Koin**

Add the import and binding next to `Booking`, `Documents`, and `Chat`:

```kotlin
single<InspectionAct> {
    InspectionActImpl(get(named("authorized")))
}
```

- [ ] **Step 5: Run Network tests**

Run:

```bash
./gradlew :Network:jsTest
```

Expected: PASS with all nine operations covered.

---

### Task 3: ViewModel state and behavior

**Files:**
- Create: `CommonViewModels/src/commonTest/kotlin/my/drivebit/viewmodels/InspectionActViewModelTest.kt`
- Create: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/InspectionActViewModel.kt`
- Modify: `CommonViewModels/src/commonMain/kotlin/my/drivebit/viewmodels/di/ViewModelsModule.kt`

**Interfaces:**
- Consumes `InspectionAct` from Task 2.
- Produces:

```kotlin
sealed interface InspectionActUiState {
    data object Idle : InspectionActUiState
    data object Loading : InspectionActUiState
    data class Ready(
        val act: BookingInspectionActDto,
        val fuelInput: String,
        val mileageInput: String,
        val commentInput: String,
    ) : InspectionActUiState
    data class Error(
        val message: String,
        val previousAct: BookingInspectionActDto? = null,
    ) : InspectionActUiState
}

enum class InspectionActAction {
    OpenOrCreate,
    UpdateMetrics,
    UpdateComment,
    UploadPhoto,
    DeletePhoto,
    SignAsOwner,
    SignAsRenter,
    DownloadPdf,
}

sealed interface InspectionActEffect {
    data class OpenPdf(val download: BookingInspectionActDownloadDto) : InspectionActEffect
}

interface InspectionActViewModel {
    val state: StateFlow<InspectionActUiState>
    val error: StateFlow<String?>
    val actionsInProgress: StateFlow<Set<InspectionActAction>>
    val effects: SharedFlow<InspectionActEffect>

    fun load()
    fun setFuelInput(value: String)
    fun setMileageInput(value: String)
    fun setCommentInput(value: String)
    fun saveMetrics()
    fun saveComment()
    fun uploadPhoto(bytes: ByteArray, fileName: String, contentType: String, kind: InspectionPhotoKind)
    fun deletePhoto(photoId: String)
    fun signAsOwner()
    fun signAsRenter()
    fun downloadPdf()
}
```

- [ ] **Step 1: Write failing ViewModel tests**

Add tests with a fake `InspectionAct` implementation for:

1. `load()` emits `Loading`, then `Ready` with DTO values copied into
   ViewModel-owned string inputs.
2. `saveMetrics()` sends parsed integer values and replaces the DTO with the
   service response.
3. `saveComment()` sends the current comment and preserves nullable blank
   comments as `null`.
4. `uploadPhoto()` forwards bytes, filename, content type and kind.
5. `deletePhoto()` calls the photo endpoint and updates the returned act after
   reloading it with `get()`.
6. `signAsOwner()` and `signAsRenter()` call only their matching endpoint and
   update state.
7. `downloadPdf()` emits `InspectionActEffect.OpenPdf`.
8. A second call while the same action is active is ignored.
9. A failed mutation keeps the previous DTO and sets a non-null error.
10. Blank `bookingId` is rejected before the fake service is called.

Example first test:

```kotlin
@Test
fun `load emits ready state with DTO values in form inputs`() = runTest {
    val api = FakeInspectionAct().apply { nextAct = sampleAct() }
    val vm = InspectionActViewModelImpl(
        inspectionAct = api,
        bookingId = "booking-1",
        type = InspectionActType.Handover,
        coroutineScope = backgroundScope,
    )

    vm.load()
    advanceUntilIdle()

    val ready = vm.state.value as InspectionActUiState.Ready
    assertEquals("75", ready.fuelInput)
    assertEquals("120500", ready.mileageInput)
    assertEquals("Есть царапина", ready.commentInput)
    assertEquals(InspectionActStatus.Draft, ready.act.status)
}
```

- [ ] **Step 2: Run ViewModel tests and verify they fail**

Run:

```bash
./gradlew :CommonViewModels:jsTest
```

Expected: FAIL because the ViewModel and fake contract do not exist.

- [ ] **Step 3: Implement state and intents**

Use `CoroutineScope`, `MutableStateFlow`, `MutableSharedFlow`, and
`safeLaunchWithErrorHandler` as in the booking ViewModels. Store `bookingId` and
`InspectionActType` in the constructor. `load()` calls `openOrCreate()` so the
screen can work with a new draft; `get()` is used after deletion to refresh the
complete DTO including photos.

Use `canEdit*` and `canSign*` only for exposing UI state; do not recreate
booking-status rules. Parse metrics with `toIntOrNull()` and report a localized
validation error without making a request when either value is invalid.

- [ ] **Step 4: Register the parameterized ViewModel**

Add imports and a Koin factory:

```kotlin
factory<InspectionActViewModel> { (bookingId: String, type: InspectionActType) ->
    InspectionActViewModelImpl(
        inspectionAct = get(),
        bookingId = bookingId,
        type = type,
    )
}
```

- [ ] **Step 5: Run ViewModel tests**

Run the focused command again. Expected: PASS.

---

### Task 4: Web route and inspection-act page

**Files:**
- Create: `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/InspectionActPage.kt`
- Create: `WebShell/src/jsTest/kotlin/my/drivebit/navigation/InspectionActRouteTest.kt`
- Modify: `WebShell/src/jsMain/kotlin/my/drivebit/navigation/AccountBundlePaths.kt`
- Modify: `accountApp/src/jsMain/kotlin/my/drivebit/clients/AccountAppContent.kt`

**Interfaces:**
- Route: `/inspection-act?bookingId=<id>&type=Handover|Return`.
- Produces `inspectionActPageUrl(bookingId: String, type: InspectionActType): String`
  in `WebShell/src/jsMain/kotlin/my/drivebit/navigation/AccountBundlePaths.kt`.
- `InspectionActPage` obtains the parameterized
  `InspectionActViewModel` from Koin and calls `load()` once.

- [ ] **Step 1: Write failing route tests**

Test that `/inspection-act` and `/inspection-act/anything` are account-bundle
paths, the route list contains `inspection-act`, and the URL helper encodes
booking ID/type correctly:

```kotlin
@Test
fun `inspection act is an account bundle route`() {
    assertTrue(isAccountBundlePath("/inspection-act"))
    assertTrue(isAccountBundlePath("/inspection-act/anything"))
}

@Test
fun `inspection act URL includes encoded booking and type`() {
    assertEquals(
        "/inspection-act?bookingId=booking-1&type=Handover",
        inspectionActPageUrl("booking-1", InspectionActType.Handover),
    )
}
```

- [ ] **Step 2: Run route tests and verify they fail**

Run:

```bash
./gradlew :WebShell:jsTest
```

Expected: FAIL because the route and helper do not exist.

- [ ] **Step 3: Add route registration**

Add `/inspection-act` checks to `isAccountBundlePath`, add
`"inspection-act"` to `accountBundleShellRoutes`, import
`InspectionActPage`, and render it in `AccountAppContent` only when the user is
logged in. Parse `bookingId` and `type`; invalid/missing values render the
existing error style without a network request.

- [ ] **Step 4: Implement Web UI**

Render:

- localized title from `Handover`/`Return`;
- number and localized status;
- fuel/mileage inputs and «Сохранить» controlled by `canEditOwnerFields`;
- owner/renter comment fields controlled by their permission flags;
- photo kind selector and DOM `<input type="file">` for one or more images;
- photos grouped/rendered with kind and delete action;
- owner/renter signed indicators;
- current-role sign button controlled by `canSignAsOwner`/`canSignAsRenter`;
- «Скачать PDF» when `hasPdf` is true.

Use ViewModel setters for every input value. On PDF effect, open the converted
temporary URL in a new browser tab. Keep upload progress and errors in the
ViewModel state; do not introduce page-level business state.

- [ ] **Step 5: Run route tests**

Run the focused route test. Expected: PASS.

---

### Task 5: Web booking entry points

**Files:**
- Modify: `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/MyBookingsPage.kt`
- Modify: `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/MyDealsPage.kt`

**Interfaces:**
- Both private booking-card composables receive:

```kotlin
onOpenInspectionAct: (InspectionActType) -> Unit
```

- [ ] **Step 1: Add Web callbacks and buttons**

Pass `onOpenInspectionAct` from `MyBookingsPage` and `MyDealsPage` to their
cards. Navigate to `inspectionActPageUrl(booking.id, type)` for both
`Handover` and `Return`. Include the buttons in the existing action row and
preserve current payment, contract and review actions.

- [ ] **Step 2: Run compilation and tests**

Run:

```bash
./gradlew :DrivebitWeb:jsTest
```

Expected: PASS.

---

### Task 6: Mobile inspection-act screen

**Files:**
- Create: `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/main/InspectionActScreen.kt`

**Interfaces:**
- `data class InspectionActScreen(val bookingId: String, val type: InspectionActType) : Screen`.
- Reuse the existing `rememberDocumentImagePicker(onPicked: (ByteArray, String,
  String) -> Unit)` contract from
  `Mobile/src/commonMain/kotlin/my/drivebit/mobile/documents/DocumentImagePicker.kt`.

- [ ] **Step 1: Implement the Mobile screen and reuse the existing picker**

Implement `inspectionActTitle` and `InspectionActScreen`. The shared ViewModel
tests in Task 3 cover the title-independent business behavior; the screen is a
presentation layer. Obtain the
parameterized ViewModel with `koinInject { parametersOf(bookingId, type) }`,
call `load()` from `LaunchedEffect`, and collect state with `collectAsState()`.
Render the same sections as Web with Material 3 components. Use ViewModel-owned
input strings, permission flags, action states, and errors. Call the existing
`rememberDocumentImagePicker` and forward its bytes, filename and content type
to `uploadPhoto`; the selected kind is the ViewModel-owned form selection.
Use `LocalUriHandler` for the PDF URL. Do not add `remember` state for act data
or form values.

- [ ] **Step 2: Run Mobile compilation**

Run:

```bash
./gradlew :Mobile:compileKotlinMultiplatform
```

Expected: PASS; no native picker files need modification because
the existing callback contract already supplies the required upload data.

---

### Task 7: Mobile booking entry points

**Files:**
- Modify: `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/main/tabs/TripsTab.kt`
- Modify: `Mobile/src/commonMain/kotlin/my/drivebit/mobile/screens/main/tabs/InboxTab.kt`

**Interfaces:**
- `BookingItemCard` and `DealItemCard` receive:

```kotlin
onOpenInspectionAct: (InspectionActType) -> Unit = {}
```

- [ ] **Step 1: Add navigation buttons**

In `TripsTab`, navigate with:

```kotlin
navigator.push(InspectionActScreen(booking.id, InspectionActType.Handover))
navigator.push(InspectionActScreen(booking.id, InspectionActType.Return))
```

Pass matching callbacks into `BookingItemCard`. Do the same in `InboxTab` for
`DealItemCard`. Show both buttons in the existing action section without
removing payment, contract, confirmation or decline actions.

- [ ] **Step 2: Run Mobile compilation**

Run:

```bash
./gradlew :Mobile:compileKotlinMultiplatform
```

Expected: PASS.

---

### Task 8: Full verification and cleanup

**Files:**
- Modify any files identified by compiler diagnostics from Tasks 1–7.
- Modify the design/plan only if implementation details require a documented
  decision change.

- [ ] **Step 1: Run focused Network and ViewModel checks**

```bash
./gradlew :Network:jsTest
./gradlew :CommonViewModels:jsTest
```

Expected: both commands exit with code 0.

- [ ] **Step 2: Run Web and Mobile checks**

```bash
./gradlew :WebShell:jsTest
./gradlew :DrivebitWeb:jsTest
./gradlew :Mobile:compileKotlinMultiplatform
```

Expected: all commands exit with code 0.

- [ ] **Step 3: Run repository verification**

```bash
./gradlew check
```

Expected: exit code 0 with no failing task.

- [ ] **Step 4: Review changed files and staging**

Run:

```bash
git status --short
git diff --check
```

Expected: no whitespace errors; unrelated pre-existing `tmp/e2e-*` artifacts
remain untouched. Do not commit or push until the user explicitly requests it.
