package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import my.drivebit.components.Column
import my.drivebit.components.LinkButton
import my.drivebit.components.Loader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.RowSpaceBetween
import my.drivebit.components.Spacer
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.UserNameFormatter
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.mapIso8601ToMonthYearString
import my.drivebit.viewmodels.AvatarUploadState
import my.drivebit.viewmodels.AvatarUploadViewModel
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.koin.compose.koinInject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val MAX_FILE_SIZE = 2 * 1024 * 1024
private const val AVATAR_SIZE = 800

@Suppress("UNCHECKED_CAST")
private suspend fun org.w3c.files.File.cropFromBottom(): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val file = this@cropFromBottom
        val fileSize: Number = js("file.size") as Number
        if (fileSize.toDouble() > MAX_FILE_SIZE) {
            continuation.resumeWithException(
                Exception("Файл слишком большой. Максимальный размер: ${MAX_FILE_SIZE / 1024 / 1024}MB"),
            )
            return@suspendCancellableCoroutine
        }

        val reader = org.w3c.files.FileReader()
        reader.onload = {
            val dataUrl = reader.result as String
            val img = js("new Image()") as org.w3c.dom.HTMLImageElement
            val onLoadHandler: dynamic = {
                val canvas = js("document.createElement('canvas')") as org.w3c.dom.HTMLCanvasElement
                val ctx = canvas.getContext("2d") as? org.w3c.dom.CanvasRenderingContext2D
                if (ctx != null) {
                    val originalWidth = img.width.toInt()
                    val originalHeight = img.height.toInt()
                    val size = minOf(originalWidth, originalHeight, AVATAR_SIZE)
                    val cropY = maxOf(0, originalHeight - size)

                    canvas.width = size
                    canvas.height = size
                    ctx.drawImage(
                        img,
                        0.0,
                        cropY.toDouble(),
                        originalWidth.toDouble(),
                        size.toDouble(),
                        0.0,
                        0.0,
                        size.toDouble(),
                        size.toDouble(),
                    )

                    val toBlobCallback: dynamic = { blob: org.w3c.files.Blob? ->
                        if (blob != null) {
                            val fileReader = org.w3c.files.FileReader()
                            fileReader.onload = {
                                val result = fileReader.result
                                if (result != null) {
                                    val uint8Array: dynamic = js("new Uint8Array(result)")
                                    val length = uint8Array.length as Int
                                    val bytes = ByteArray(length)
                                    for (i in 0 until length) {
                                        bytes[i] = (uint8Array[i] as Number).toInt().toByte()
                                    }
                                    continuation.resume(bytes)
                                } else {
                                    continuation.resumeWithException(Exception("Failed to crop image"))
                                }
                            }
                            fileReader.onerror = {
                                continuation.resumeWithException(Exception("Failed to read cropped image"))
                            }
                            fileReader.readAsArrayBuffer(blob)
                        } else {
                            continuation.resumeWithException(Exception("Failed to crop image"))
                        }
                    }

                    canvas.toBlob(toBlobCallback, "image/jpeg", 0.9)
                } else {
                    continuation.resumeWithException(Exception("Failed to get canvas context"))
                }
            }
            val onErrorHandler: dynamic = {
                continuation.resumeWithException(Exception("Failed to load image"))
            }
            img.onload = onLoadHandler
            img.onerror = onErrorHandler
            img.src = dataUrl
        }
        reader.onerror = {
            continuation.resumeWithException(Exception("File read error"))
        }
        reader.readAsDataURL(file)
    }

private fun buildEditNameUrlParams(
    firstName: String?,
    lastName: String?,
    middleName: String?,
): String {
    val firstNameParam =
        firstName?.let {
            "?firstName=${it.encodeUrlParameter()}"
        } ?: ""

    val lastNameParam =
        lastName?.let {
            "&lastName=${it.encodeUrlParameter()}"
        } ?: ""

    val middleNameParam =
        middleName?.let {
            "&middleName=${it.encodeUrlParameter()}"
        } ?: ""

    return "$firstNameParam$lastNameParam$middleNameParam"
}

@Composable
fun ProfilePage(viewModel: ProfileViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    val navigationController = LocalNavigationController.current

    PageWithLogo {
        Div({
            style {
                padding(24.px)
                maxWidth(800.px)
                margin(0.px)
                property("margin-left", "auto")
                property("margin-right", "auto")
            }
        }) {
            when (val currentState = state) {
                is ProfileState.Loading -> {
                    Loader()
                }

                is ProfileState.Error -> {
                    Div({
                        style {
                            textAlign("center")
                            padding(24.px)
                        }
                    }) {
                        TextError("Ошибка: ${currentState.message}")
                    }
                }

                is ProfileState.Success -> {
                    val user = currentState.user
                    val iconUserViewModel: IconUserViewModel = koinInject()
                    val avatarUrl by iconUserViewModel.avatarUrl.collectAsState()
                    val avatarUploadViewModel: AvatarUploadViewModel = koinInject()
                    val uploadState by avatarUploadViewModel.state.collectAsState()
                    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

                    val fileInputId = remember { "avatar-file-input-${kotlin.random.Random.nextInt()}" }

                    DisposableEffect(fileInputId) {
                        val inputElement =
                            document.getElementById(
                                fileInputId,
                            ) as? org.w3c.dom.HTMLInputElement
                        val changeHandler: (org.w3c.dom.events.Event) -> Unit = { event ->
                            val input = event.target as? org.w3c.dom.HTMLInputElement
                            val fileList = input?.files
                            val file = fileList?.item(0) as? org.w3c.files.File
                            if (file != null) {
                                coroutineScope.launch {
                                    try {
                                        val fileBytes = file.cropFromBottom()
                                        val fileName = "avatar.jpg"
                                        val contentType = "image/jpeg"
                                        avatarUploadViewModel.uploadAvatar(fileBytes, fileName, contentType)
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        }
                        inputElement?.addEventListener("change", changeHandler)
                        onDispose {
                            inputElement?.removeEventListener("change", changeHandler)
                        }
                    }

                    LaunchedEffect(uploadState) {
                        if (uploadState is AvatarUploadState.Success) {
                        }
                    }

                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                        }
                    }) {
                        Column(gap = 8.px, marginBottom = 24.px) {
                            RowSpaceBetween {
                                Img(
                                    src = avatarUrl,
                                    alt = "User",
                                    attrs = {
                                        style {
                                            width(200.px)
                                            height(200.px)
                                            borderRadius(50.percent)
                                        }
                                    },
                                )
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexDirection(FlexDirection.Column)
                                        gap(8.px)
                                    }
                                }) {
                                    LinkButton(
                                        text =
                                            when (uploadState) {
                                                is AvatarUploadState.Uploading -> "Загрузка..."
                                                else -> "Изменить"
                                            },
                                        onClick = {
                                            val inputElement =
                                                document.getElementById(
                                                    fileInputId,
                                                ) as? org.w3c.dom.HTMLInputElement
                                            inputElement?.click()
                                        },
                                    )
                                    when (val state = uploadState) {
                                        is AvatarUploadState.Error -> {
                                            TextError(state.message)
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }

                        Input(
                            type = InputType.File,
                            attrs = {
                                id(fileInputId)
                                attr("accept", "image/*")
                                style {
                                    display(DisplayStyle.None)
                                }
                            },
                        )

                        Column(gap = 8.px, marginBottom = 8.px) {
                            RowSpaceBetween {
                                TextSmartHeader(
                                    UserNameFormatter.formatDisplayName(
                                        firstName = user.firstName,
                                        middleName = user.middleName,
                                        lastName = user.lastName,
                                    ),
                                )
                                LinkButton(
                                    text = "Изменить",
                                    onClick = {
                                        val params =
                                            buildEditNameUrlParams(
                                                currentState.user.firstName,
                                                currentState.user.lastName,
                                                currentState.user.middleName,
                                            )
                                        navigationController?.navigateTo("/edit-name$params")
                                    },
                                )
                            }
                        }
                        user.createdAt.let { createdAt ->
                            val formattedDate = mapIso8601ToMonthYearString(createdAt)
                            TextSmallBodyBlack("Присоединился $formattedDate")
                        }

                        Spacer(16.px)

                        val phone = user.phone
                        RowSpaceBetween {
                            TextSmallBodyBlack("Номер телефона")
                            val phoneString = phone ?: "Подтвердить"
                            LinkButton(phoneString) {
                                navigationController?.navigateTo("/change-phone")
                            }
                        }

                        Spacer(16.px)

                        RowSpaceBetween {
                            TextSmallBodyBlack("E-mail")

                            val emailString = user.email ?: "Изменить"
                            LinkButton(emailString) {
                                navigationController?.navigateTo("/change-email")
                            }
                        }
                    }
                }
            }
        }
    }
}
