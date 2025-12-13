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
import my.drivebit.components.UserAvatar
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

@Suppress("UNCHECKED_CAST")
private suspend fun org.w3c.files.File.readAsBytes(): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val file = this@readAsBytes
        val reader = org.w3c.files.FileReader()
        reader.onload = {
            val arrayBuffer = reader.result
            if (arrayBuffer != null) {
                val uint8Array: dynamic = js("new Uint8Array(arrayBuffer)")
                val length = uint8Array.length as Int
                val bytes = ByteArray(length)
                for (i in 0 until length) {
                    bytes[i] = (uint8Array[i] as Number).toInt().toByte()
                }
                continuation.resume(bytes)
            } else {
                continuation.resumeWithException(Exception("Failed to read file"))
            }
        }
        reader.onerror = {
            continuation.resumeWithException(Exception("File read error"))
        }
        reader.readAsArrayBuffer(file)
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
                    val avatarUrl by iconUserViewModel.avatarUrl.collectAsState(null)
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
                                        val fileBytes = file.readAsBytes()
                                        val fileName = file.name
                                        val contentType = file.type.ifBlank { "image/jpeg" }
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

                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                        }
                    }) {
                        Column(gap = 8.px, marginBottom = 24.px) {
                            RowSpaceBetween {
                                avatarUrl?.let {
                                    UserAvatar(
                                        src = it,
                                        size = 200.px,
                                    )
                                }
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
