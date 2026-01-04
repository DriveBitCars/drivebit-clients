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
    private fun ensureHttpsUrl(url: String): String {
        val sanitizedUrl = url.replace(" ", "%20")

        val isHttp = sanitizedUrl.startsWith("http://")
        val isHttps = sanitizedUrl.startsWith("https://")
        if (!isHttp && !isHttps) {
            return sanitizedUrl
        }

        val withoutScheme = sanitizedUrl.substringAfter("://")
        val host = withoutScheme.substringBefore("/").substringBefore(":")

        val isLocalAddress =
            host == "localhost" ||
                host == "127.0.0.1" ||
                host.startsWith("10.") ||
                host.startsWith("192.168.") ||
                (host.startsWith("172.") && host.split(".").getOrNull(1)?.toIntOrNull()?.let { it in 16..31 } == true)

        val isProductionMinIO = host == "155.212.170.94" && sanitizedUrl.contains(":9000")
        if (isProductionMinIO) {
            val path = sanitizedUrl.substringAfter(":9000")
            val normalizedPath = if (path.startsWith("/")) path else "/$path"
            println("🖼️ [Photo] Преобразование URL: $sanitizedUrl -> $normalizedPath")
            return normalizedPath
        }

        if (isLocalAddress) {
            return sanitizedUrl
        }

        return if (isHttp) sanitizedUrl.replaceFirst("http://", "https://") else sanitizedUrl
    }
    override suspend fun getAvatar(): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response = httpClient.get(url)
        return response.parseResponse()
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
        return try {
            val url = "${DEFAULT_BASE_URL}Photo/car/$carId"
            val response = httpClient.get(url)
            val photos: List<CarPhotoResponse> = response.parseResponse()
            photos.map { photo ->
                val originalUrl = photo.url
                val convertedUrl = ensureHttpsUrl(originalUrl)
                println("🖼️ [Photo] getCarPhotos: $originalUrl -> $convertedUrl")
                photo.copy(url = convertedUrl)
            }
        } catch (e: Exception) {
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
