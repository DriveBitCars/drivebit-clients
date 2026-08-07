package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.consumeResponse
import my.drivebit.network.parseResponse
import my.drivebit.utils.extractPathFromApiUrl

interface Photo {
    suspend fun getAvatar(): AvatarResponse

    suspend fun getAvatarByUserId(userId: String): AvatarResponse?

    suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): AvatarResponse

    suspend fun getCarPhotos(carId: String): List<CarPhotoResponse>

    suspend fun uploadCarPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    ): List<CarPhotoResponse>

    suspend fun deleteCarPhoto(photoId: Int)
}

@Serializable
data class AvatarResponse(
    val url: String,
)

@Serializable
data class CarPhotoResponse(
    val id: Int,
    val url: String,
    val uploadDate: String,
    val thumbnailUrl: String? = null,
    val sortOrder: Int = 0,
) {
    fun previewUrl(): String = thumbnailUrl?.trim()?.takeIf { it.isNotEmpty() } ?: url

    fun withSanitizedUrls(sanitize: (String) -> String): CarPhotoResponse =
        copy(
            url = sanitize(url),
            thumbnailUrl =
                thumbnailUrl
                    ?.let(sanitize)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() },
        )
}

fun List<CarPhotoResponse>.sortedBySortOrder(): List<CarPhotoResponse> = sortedBy { it.sortOrder }

class PhotoImpl(
    private val httpClient: HttpClient,
    private val carService: Car? = null,
) : Photo {
    private fun ensureHttpsUrl(url: String): String = extractPathFromApiUrl(url)

    override suspend fun getAvatar(): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response = httpClient.get(url)
        val avatarResponse: AvatarResponse = response.parseResponse()
        return avatarResponse.copy(url = ensureHttpsUrl(avatarResponse.url))
    }

    override suspend fun getAvatarByUserId(userId: String): AvatarResponse? {
        if (userId.isBlank()) return null
        val url = "${DEFAULT_BASE_URL}Photo/avatar/$userId"
        return runCatching {
            val response = httpClient.get(url)
            val avatarResponse: AvatarResponse = response.parseResponse()
            avatarResponse.copy(url = ensureHttpsUrl(avatarResponse.url))
        }.getOrNull()
    }

    override suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response =
            httpClient.post(url) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "file",
                                fileBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                },
                            )
                        },
                    ),
                )
            }
        return response.parseResponse()
    }

    override suspend fun getCarPhotos(carId: String): List<CarPhotoResponse> {
        if (carId.isBlank()) {
            throw IllegalArgumentException("Car ID cannot be empty")
        }
        return runCatching {
            val url = "${DEFAULT_BASE_URL}Photo/car/$carId"
            val response = httpClient.get(url)
            val photos: List<CarPhotoResponse> = response.parseResponse()
            photos.map { photo -> photo.withSanitizedUrls(::ensureHttpsUrl) }.sortedBySortOrder()
        }.getOrElse { e ->
            if (carService != null) {
                val car = carService.getCar(carId)
                car.photos
                    .map { photo ->
                        CarPhotoResponse(
                            id = photo.id,
                            url = ensureHttpsUrl(photo.url),
                            uploadDate = photo.uploadDate,
                            thumbnailUrl = photo.thumbnailUrl?.let { ensureHttpsUrl(it) },
                            sortOrder = photo.sortOrder,
                        )
                    }.sortedBySortOrder()
            } else {
                throw e
            }
        }
    }

    override suspend fun uploadCarPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    ): List<CarPhotoResponse> {
        require(fileBytesList.size == fileNames.size && fileNames.size == contentTypes.size) {
            "fileBytesList, fileNames, and contentTypes must have the same size"
        }

        val url = "${DEFAULT_BASE_URL}Photo/car/my/$carId"
        val response =
            httpClient.post(url) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            fileBytesList.forEachIndexed { index, fileBytes ->
                                append(
                                    "files",
                                    fileBytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, contentTypes[index])
                                        append(HttpHeaders.ContentDisposition, "filename=\"${fileNames[index]}\"")
                                    },
                                )
                            }
                        },
                    ),
                )
            }
        val photos: List<CarPhotoResponse> = response.parseResponse()
        return photos.map { photo -> photo.withSanitizedUrls(::ensureHttpsUrl) }.sortedBySortOrder()
    }

    override suspend fun deleteCarPhoto(photoId: Int) {
        val url = "${DEFAULT_BASE_URL}Photo/$photoId"
        val response = httpClient.delete(url)
        response.consumeResponse()
    }
}
