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
import my.drivebit.network.parseResponse
import my.drivebit.network.services.Car
import my.drivebit.network.utils.UrlSanitizer

interface Photo {
    suspend fun getAvatar(): AvatarResponse

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
)

class PhotoImpl(
    private val httpClient: HttpClient,
    private val carService: Car? = null,
) : Photo {
    private fun ensureHttpsUrl(url: String): String = UrlSanitizer.ensureHttpsUrl(url)

    override suspend fun getAvatar(): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response = httpClient.get(url)
        val avatarResponse: AvatarResponse = response.parseResponse()
        return avatarResponse.copy(url = ensureHttpsUrl(avatarResponse.url))
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

    override suspend fun getCarPhotos(carId: String): List<CarPhotoResponse> =
        runCatching {
            val url = "${DEFAULT_BASE_URL}Photo/car/$carId"
            val response = httpClient.get(url)
            val photos: List<CarPhotoResponse> = response.parseResponse()
            photos.map { photo ->
                photo.copy(url = ensureHttpsUrl(photo.url))
            }
        }.getOrElse { e ->
            if (carService != null) {
                val car = carService.getCar(carId)
                car.photos.map { photo ->
                    CarPhotoResponse(
                        id = photo.id,
                        url = ensureHttpsUrl(photo.url),
                        uploadDate = photo.uploadDate,
                    )
                }
            } else {
                throw e
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
        return photos.map { photo ->
            photo.copy(url = ensureHttpsUrl(photo.url))
        }
    }

    override suspend fun deleteCarPhoto(photoId: Int) {
        val url = "${DEFAULT_BASE_URL}Photo/$photoId"
        httpClient.delete(url)
    }
}
