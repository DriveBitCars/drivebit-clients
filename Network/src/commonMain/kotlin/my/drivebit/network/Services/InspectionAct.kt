package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.consumeResponse
import my.drivebit.network.parseResponse

@Serializable
enum class InspectionActType {
    Handover,
    Return,
}

val supportedInspectionActTypes: List<InspectionActType> =
    listOf(InspectionActType.Handover, InspectionActType.Return)

fun inspectionActPagePath(
    bookingId: String,
    type: InspectionActType,
): String = "/inspection-act?bookingId=$bookingId&type=${type.name}"

fun inspectionActPageUrl(
    bookingId: String,
    type: InspectionActType,
): String = "https://drivebit.ru${inspectionActPagePath(bookingId, type)}"

const val INSPECTION_ACT_PHOTO_THUMBNAIL_PX = 200

fun inspectionActPhotoPagePath(photoUrl: String): String =
    "/inspection-act/photo?url=${photoUrl.encodeURLQueryComponent(encodeFull = true)}"

fun isInspectionActPhotoPath(path: String): Boolean {
    val normalized = path.substringBefore("?").removeSuffix("/").ifEmpty { "/" }
    return normalized == "/inspection-act/photo"
}

fun inspectionActTitle(type: InspectionActType): String =
    when (type) {
        InspectionActType.Handover -> "Акт передачи"
        InspectionActType.Return -> "Акт возврата"
    }

fun inspectionActStatusLabel(status: InspectionActStatus): String =
    when (status) {
        InspectionActStatus.None -> "Не создан"
        InspectionActStatus.Draft -> "Черновик"
        InspectionActStatus.AwaitingOwner -> "Ожидает владельца"
        InspectionActStatus.AwaitingRenter -> "Ожидает арендатора"
        InspectionActStatus.SignedByBoth -> "Подписан обеими сторонами"
    }

fun inspectionPhotoKindLabel(kind: InspectionPhotoKind): String =
    when (kind) {
        InspectionPhotoKind.Car -> "Автомобиль"
        InspectionPhotoKind.Dashboard -> "Приборная панель"
        InspectionPhotoKind.Other -> "Другое"
    }

const val INSPECTION_ACT_FUEL_LABEL = "Топливо, %"
const val INSPECTION_ACT_MILEAGE_LABEL = "Пробег, км"

enum class InspectionActViewerRole {
    Owner,
    Renter,
}

fun resolveInspectionActViewerRole(
    currentUserId: String,
    ownerId: String,
    renterId: String,
): InspectionActViewerRole? =
    when (currentUserId) {
        ownerId -> InspectionActViewerRole.Owner
        renterId -> InspectionActViewerRole.Renter
        else -> null
    }

fun BookingInspectionActDto.canCurrentUserEditMetrics(role: InspectionActViewerRole): Boolean =
    when (role) {
        InspectionActViewerRole.Owner -> canEditOwnerFields
        InspectionActViewerRole.Renter -> canEditRenterFields
    }

fun BookingInspectionActDto.canCurrentUserEditComment(role: InspectionActViewerRole): Boolean =
    when (role) {
        InspectionActViewerRole.Owner -> canEditOwnerFields
        InspectionActViewerRole.Renter -> canEditRenterFields
    }

fun BookingInspectionActDto.canCurrentUserUploadPhotos(role: InspectionActViewerRole): Boolean =
    canCurrentUserEditComment(role)

fun BookingInspectionActDto.canCurrentUserSign(role: InspectionActViewerRole): Boolean =
    when (role) {
        // Backend sets canSignAsOwner only after fuel/mileage are saved; owner still needs the
        // button so metrics can be persisted on sign.
        InspectionActViewerRole.Owner -> canEditOwnerFields || canSignAsOwner
        InspectionActViewerRole.Renter -> canSignAsRenter
    }

fun BookingInspectionActDto.hasCurrentUserSigned(role: InspectionActViewerRole): Boolean =
    when (role) {
        InspectionActViewerRole.Owner -> isSignedByOwner
        InspectionActViewerRole.Renter -> isSignedByRenter
    }

fun BookingInspectionActDto.currentUserSignStatusMessage(role: InspectionActViewerRole): String? =
    when {
        isFullySigned -> null
        canCurrentUserSign(role) -> null
        hasCurrentUserSigned(role) -> "Вы подписали акт"
        else -> null
    }

fun BookingInspectionActDto.counterpartySignStatusMessage(role: InspectionActViewerRole): String? =
    when (role) {
        InspectionActViewerRole.Owner ->
            when {
                isFullySigned -> null
                isSignedByRenter -> "Арендатор подписал"
                else -> "Ожидаем подпись арендатора"
            }
        InspectionActViewerRole.Renter ->
            when {
                isFullySigned -> null
                isSignedByOwner -> "Владелец подписал"
                else -> "Ожидаем подпись владельца"
            }
    }

fun BookingInspectionActDto.commentInputFor(role: InspectionActViewerRole): String =
    when (role) {
        InspectionActViewerRole.Owner -> ownerComment.orEmpty()
        InspectionActViewerRole.Renter -> renterComment.orEmpty()
    }

fun BookingInspectionActPhotoDto.canCurrentUserDelete(
    role: InspectionActViewerRole,
    ownerId: String,
    renterId: String,
    canEditOwnerFields: Boolean,
    canEditRenterFields: Boolean,
): Boolean =
    when (role) {
        InspectionActViewerRole.Owner -> authorId == ownerId && canEditOwnerFields
        InspectionActViewerRole.Renter -> authorId == renterId && canEditRenterFields
    }

fun BookingInspectionActDto.hasRequiredPhotosForCurrentUser(
    role: InspectionActViewerRole,
    ownerId: String,
    renterId: String,
): Boolean {
    val authorId =
        when (role) {
            InspectionActViewerRole.Owner -> ownerId
            InspectionActViewerRole.Renter -> renterId
        }
    val mine = photos.orEmpty().filter { it.authorId == authorId }
    return mine.any { it.kind == InspectionPhotoKind.Car } &&
        mine.any { it.kind == InspectionPhotoKind.Dashboard }
}

@Serializable
enum class InspectionActStatus {
    None,
    Draft,
    AwaitingOwner,
    AwaitingRenter,
    SignedByBoth,
}

@Serializable
enum class InspectionPhotoKind {
    Car,
    Dashboard,
    Other,
}

interface InspectionAct {
    suspend fun get(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto

    suspend fun openOrCreate(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto

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

    suspend fun deletePhoto(
        bookingId: String,
        type: InspectionActType,
        photoId: String,
    )

    suspend fun signAsOwner(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto

    suspend fun signAsRenter(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto

    suspend fun download(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDownloadDto
}

@Serializable
data class BookingInspectionActDto(
    @JsonNames("id", "Id") val id: String,
    @JsonNames("bookingId", "BookingId") val bookingId: String,
    @JsonNames("type", "Type") val type: InspectionActType,
    @JsonNames("actNumber", "ActNumber") val actNumber: Long,
    @JsonNames("fuelRemaining", "FuelRemaining") val fuelRemaining: Int? = null,
    @JsonNames("mileage", "Mileage") val mileage: Int? = null,
    @JsonNames("ownerComment", "OwnerComment") val ownerComment: String? = null,
    @JsonNames("renterComment", "RenterComment") val renterComment: String? = null,
    @JsonNames("signedByOwnerAt", "SignedByOwnerAt") val signedByOwnerAt: String? = null,
    @JsonNames("signedByRenterAt", "SignedByRenterAt") val signedByRenterAt: String? = null,
    @JsonNames("isSignedByOwner", "IsSignedByOwner") val isSignedByOwner: Boolean = false,
    @JsonNames("isSignedByRenter", "IsSignedByRenter") val isSignedByRenter: Boolean = false,
    @JsonNames("isFullySigned", "IsFullySigned") val isFullySigned: Boolean = false,
    @JsonNames("hasPdf", "HasPdf") val hasPdf: Boolean = false,
    @JsonNames("status", "Status") val status: InspectionActStatus,
    @JsonNames("canEditOwnerFields", "CanEditOwnerFields") val canEditOwnerFields: Boolean = false,
    @JsonNames("canEditRenterFields", "CanEditRenterFields") val canEditRenterFields: Boolean = false,
    @JsonNames("canSignAsOwner", "CanSignAsOwner") val canSignAsOwner: Boolean = false,
    @JsonNames("canSignAsRenter", "CanSignAsRenter") val canSignAsRenter: Boolean = false,
    @JsonNames("createdAt", "CreatedAt") val createdAt: String,
    @JsonNames("updatedAt", "UpdatedAt") val updatedAt: String,
    @JsonNames("photos", "Photos") val photos: List<BookingInspectionActPhotoDto>? = null,
)

@Serializable
data class BookingInspectionActPhotoDto(
    @JsonNames("id", "Id") val id: String,
    @JsonNames("authorId", "AuthorId") val authorId: String,
    @JsonNames("authorName", "AuthorName") val authorName: String? = null,
    @JsonNames("kind", "Kind") val kind: InspectionPhotoKind,
    @JsonNames("fileName", "FileName") val fileName: String? = null,
    @JsonNames("url", "Url") val url: String? = null,
    @JsonNames("uploadedAt", "UploadedAt") val uploadedAt: String,
)

@Serializable
data class BookingInspectionActDownloadDto(
    @JsonNames("actId", "ActId") val actId: String,
    @JsonNames("actNumber", "ActNumber") val actNumber: Long,
    @JsonNames("type", "Type") val type: InspectionActType,
    @JsonNames("fileName", "FileName") val fileName: String? = null,
    @JsonNames("downloadUrl", "DownloadUrl") val downloadUrl: String? = null,
    @JsonNames("urlExpiresAt", "UrlExpiresAt") val urlExpiresAt: String,
)

@Serializable
data class UpdateInspectionMetricsRequest(
    @JsonNames("fuelRemaining", "FuelRemaining") val fuelRemaining: Int,
    @JsonNames("mileage", "Mileage") val mileage: Int,
)

@Serializable
data class UpdateInspectionCommentRequest(
    @JsonNames("comment", "Comment") val comment: String? = null,
)

class InspectionActImpl(
    private val httpClient: HttpClient,
) : InspectionAct {
    override suspend fun get(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto = httpClient.get(actUrl(bookingId, type)).parseResponse()

    override suspend fun openOrCreate(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto = httpClient.post(actUrl(bookingId, type)).parseResponse()

    override suspend fun updateMetrics(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionMetricsRequest,
    ): BookingInspectionActDto =
        httpClient
            .put("${actUrl(bookingId, type)}/metrics") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.parseResponse()

    override suspend fun updateComment(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionCommentRequest,
    ): BookingInspectionActDto =
        httpClient
            .put("${actUrl(bookingId, type)}/comment") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.parseResponse()

    override suspend fun uploadPhoto(
        bookingId: String,
        type: InspectionActType,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        kind: InspectionPhotoKind,
    ): BookingInspectionActPhotoDto =
        httpClient
            .post("${actUrl(bookingId, type)}/photos") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "File",
                                fileBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                },
                            )
                            append("Kind", kind.name)
                        },
                    ),
                )
            }.parseResponse()

    override suspend fun deletePhoto(
        bookingId: String,
        type: InspectionActType,
        photoId: String,
    ) {
        httpClient.delete("${actUrl(bookingId, type)}/photos/$photoId").consumeResponse()
    }

    override suspend fun signAsOwner(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto = httpClient.post("${actUrl(bookingId, type)}/sign-as-owner").parseResponse()

    override suspend fun signAsRenter(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto = httpClient.post("${actUrl(bookingId, type)}/sign-as-renter").parseResponse()

    override suspend fun download(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDownloadDto = httpClient.get("${actUrl(bookingId, type)}/download").parseResponse()

    private fun actUrl(
        bookingId: String,
        type: InspectionActType,
    ): String {
        require(bookingId.isNotBlank()) { "Booking ID cannot be empty" }
        return "${DEFAULT_BASE_URL}Booking/$bookingId/inspection-acts/${type.name}"
    }
}
