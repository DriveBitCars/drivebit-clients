package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import my.drivebit.components.Column
import my.drivebit.components.ErrorContainer
import my.drivebit.components.LinkButton
import my.drivebit.components.Loader
import my.drivebit.components.PageContainer
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.RowSpaceBetween
import my.drivebit.components.Spacer
import my.drivebit.components.TelegramUnlinkConfirmDialog
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.components.UserAvatar
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.UserNameFormatter
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.mapIso8601ToMonthYearString
import my.drivebit.viewmodels.AvatarUploadState
import my.drivebit.viewmodels.AvatarUploadViewModel
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel
import my.drivebit.viewmodels.TelegramLinkUiState
import my.drivebit.viewmodels.TelegramLinkViewModel
import my.drivebit.viewmodels.isCheckboxChecked
import my.drivebit.viewmodels.isCheckboxEnabled
import my.drivebit.web.homePathHref
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text
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
    val storage: Storage = koinInject()

    LaunchedEffect(state) {
        if (
            state is ProfileState.Error &&
            (state as ProfileState.Error).message == "Не авторизован" &&
            !storage.isLogined()
        ) {
            window.location.href = homePathHref(storage)
        }
    }

    PageWithLogo {
        PageContainer {
            when (val currentState = state) {
                is ProfileState.Loading -> {
                    Loader()
                }

                is ProfileState.Error -> {
                    ErrorContainer {
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
                                    runCatching {
                                        val fileBytes = file.readAsBytes()
                                        val fileName = file.name
                                        val contentType = file.type.ifBlank { "image/jpeg" }
                                        avatarUploadViewModel.uploadAvatar(fileBytes, fileName, contentType)
                                    }
                                }
                            }
                        }
                        inputElement?.addEventListener("change", changeHandler)
                        onDispose {
                            inputElement?.removeEventListener("change", changeHandler)
                        }
                    }

                    Column {
                        Column(gap = 8.px, marginBottom = 24.px) {
                            RowSpaceBetween {
                                avatarUrl?.let {
                                    UserAvatar(
                                        src = it,
                                        size = 200.px,
                                    )
                                }
                                Column(gap = 8.px) {
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

                        Spacer(16.px)

                        RowSpaceBetween {
                            TextSmallBodyBlack("Пароль")
                            LinkButton("Изменить") {
                                val login = (user.email ?: user.phone).orEmpty()
                                val path =
                                    if (login.isNotBlank()) {
                                        "/change-password?login=${login.encodeUrlParameter()}"
                                    } else {
                                        "/change-password"
                                    }
                                navigationController?.navigateTo(path)
                            }
                        }

                        Spacer(16.px)

                        val telegramLinkViewModel: TelegramLinkViewModel = koinInject()
                        val telegramState by telegramLinkViewModel.uiState.collectAsState()

                        LaunchedEffect(Unit) {
                            telegramLinkViewModel.loadStatus()
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.FlexStart)
                                gap(12.px)
                                marginTop(8.px)
                            }
                        }) {
                            Input(
                                type = InputType.Checkbox,
                                attrs = {
                                    id("telegram-notifications-checkbox")
                                    checked(telegramState.isCheckboxChecked())
                                    if (!telegramState.isCheckboxEnabled()) {
                                        disabled()
                                    }
                                    onInput { event ->
                                        val checked =
                                            (event.target as org.w3c.dom.HTMLInputElement).checked
                                        telegramLinkViewModel.onCheckboxChanged(checked)
                                    }
                                    style {
                                        width(20.px)
                                        height(20.px)
                                        marginTop(2.px)
                                        cursor(
                                            if (telegramState.isCheckboxEnabled()) {
                                                "pointer"
                                            } else {
                                                "not-allowed"
                                            },
                                        )
                                        flexShrink(0)
                                    }
                                },
                            )
                            Label(
                                forId = "telegram-notifications-checkbox",
                                attrs = {
                                    style {
                                        cursor(
                                            if (telegramState.isCheckboxEnabled()) {
                                                "pointer"
                                            } else {
                                                "not-allowed"
                                            },
                                        )
                                        flexShrink(1)
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.sm)
                                        color(CSSColors.Black)
                                        lineHeight("1.45")
                                    }
                                },
                            ) {
                                Text("Получать уведомления в телеграм")
                            }
                        }

                        when (val currentTelegramState = telegramState) {
                            is TelegramLinkUiState.LinkPending -> {
                                Spacer(8.px)
                                LinkButton("Привязать телеграм") {
                                    window.open(currentTelegramState.deepLinkUrl, "_blank")
                                }
                                Div({
                                    style {
                                        marginTop(8.px)
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.sm)
                                        color(CSSColors.Black)
                                        lineHeight("1.45")
                                    }
                                }) {
                                    Text(
                                        "Если кнопка не работает, то отправьте код ${currentTelegramState.code} телеграм боту ",
                                    )
                                    A(attrs = {
                                        attr("href", "https://t.me/drivebit_bot")
                                        attr("target", "_blank")
                                        attr("rel", "noopener noreferrer")
                                        style {
                                            color(CSSColors.Blue)
                                            property("text-decoration", "none")
                                            cursor("pointer")
                                        }
                                    }) {
                                        Text("@drivebit_bot")
                                    }
                                }
                            }

                            is TelegramLinkUiState.Error -> {
                                Spacer(8.px)
                                TextError(currentTelegramState.message)
                            }

                            else -> Unit
                        }

                        if (telegramState is TelegramLinkUiState.ConfirmUnlink) {
                            TelegramUnlinkConfirmDialog(
                                onConfirm = { telegramLinkViewModel.confirmUnlink() },
                                onCancel = { telegramLinkViewModel.cancelUnlink() },
                            )
                        }
                    }
                }
            }
        }
    }
}
