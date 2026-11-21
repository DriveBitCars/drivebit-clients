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
import my.drivebit.components.ErrorContainer
import my.drivebit.components.Loader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.TelegramUnlinkConfirmDialog
import my.drivebit.components.TextError
import my.drivebit.components.UserAvatar
import my.drivebit.components.VerificationBadgeRow
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
import my.drivebit.viewmodels.VerificationLabels
import my.drivebit.viewmodels.isCheckboxChecked
import my.drivebit.viewmodels.isCheckboxEnabled
import my.drivebit.web.homePathHref
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Span
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

    PageWithLogo(fullBleed = true) {
        Div({
            style {
                width(100.percent)
                minHeight(100.percent)
                property("min-height", "calc(100vh - 150px)")
                backgroundColor(CSSColors.Gray7)
                paddingTop(40.px)
                paddingBottom(48.px)
                paddingLeft(16.px)
                paddingRight(16.px)
                property("box-sizing", "border-box")
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
            }
        }) {
            Div({
                style {
                    width(100.percent)
                    maxWidth(720.px)
                    backgroundColor(CSSColors.White)
                    borderRadius(24.px)
                    padding(32.px)
                    property("box-sizing", "border-box")
                    property("box-shadow", "0 8px 32px rgba(21, 32, 50, 0.06)")
                    property("border", "1px solid ${CSSColors.Gray6String}")
                }
            }) {
                when (val currentState = state) {
                    is ProfileState.Loading -> {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.Center)
                                padding(48.px, 0.px)
                            }
                        }) {
                            Loader()
                        }
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
                                document.getElementById(fileInputId) as? org.w3c.dom.HTMLInputElement
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

                        Input(
                            type = InputType.File,
                            attrs = {
                                id(fileInputId)
                                attr("accept", "image/*")
                                style { display(DisplayStyle.None) }
                            },
                        )

                        // —— Header: avatar + identity ——
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexWrap(FlexWrap.Wrap)
                                alignItems(AlignItems.Center)
                                gap(24.px)
                                marginBottom(28.px)
                            }
                        }) {
                            Div({
                                style {
                                    position(Position.Relative)
                                    flexShrink(0)
                                }
                            }) {
                                avatarUrl?.let {
                                    UserAvatar(src = it, size = 112.px)
                                } ?: Div({
                                    style {
                                        width(112.px)
                                        height(112.px)
                                        borderRadius(50.percent)
                                        backgroundColor(CSSColors.Gray7)
                                        border(1.px, LineStyle.Solid, CSSColors.Gray6)
                                    }
                                })
                            }

                            Div({
                                style {
                                    flexGrow(1)
                                    property("min-width", "200px")
                                    display(DisplayStyle.Flex)
                                    flexDirection(FlexDirection.Column)
                                    gap(10.px)
                                }
                            }) {
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexWrap(FlexWrap.Wrap)
                                        alignItems(AlignItems.Center)
                                        gap(12.px)
                                    }
                                }) {
                                    H1({
                                        style {
                                            applyTypography(CSSTypography.Styles.h3)
                                            property("font-size", "28px")
                                            lineHeight("1.25")
                                            color(CSSColors.Black)
                                            margin(0.px)
                                        }
                                    }) {
                                        Text(
                                            UserNameFormatter.formatDisplayName(
                                                firstName = user.firstName,
                                                middleName = user.middleName,
                                                lastName = user.lastName,
                                            ),
                                        )
                                    }
                                    ProfileTextAction("Изменить имя") {
                                        val params =
                                            buildEditNameUrlParams(
                                                currentState.user.firstName,
                                                currentState.user.lastName,
                                                currentState.user.middleName,
                                            )
                                        navigationController?.navigateTo("/edit-name$params")
                                    }
                                }

                                VerificationBadgeRow(
                                    VerificationLabels.forUser(
                                        isPassportVerified = user.isPassportVerified,
                                        isDriverLicenseVerified = user.isDriverLicenseVerified,
                                    ),
                                )

                                user.createdAt.let { createdAt ->
                                    Span({
                                        style {
                                            applyTypography(CSSTypography.Styles.body)
                                            fontSize(CSSTypography.FontSize.sm)
                                            color(CSSColors.Gray4)
                                        }
                                    }) {
                                        Text("Присоединился ${mapIso8601ToMonthYearString(createdAt)}")
                                    }
                                }

                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexWrap(FlexWrap.Wrap)
                                        gap(10.px)
                                        marginTop(4.px)
                                    }
                                }) {
                                    ProfileGhostButton(
                                        text =
                                            when (uploadState) {
                                                is AvatarUploadState.Uploading -> "Загрузка..."
                                                else -> "Изменить фото"
                                            },
                                        enabled = uploadState !is AvatarUploadState.Uploading,
                                        onClick = {
                                            (
                                                document.getElementById(fileInputId)
                                                    as? org.w3c.dom.HTMLInputElement
                                            )?.click()
                                        },
                                    )
                                }

                                when (val upload = uploadState) {
                                    is AvatarUploadState.Error -> TextError(upload.message)
                                    else -> Unit
                                }
                            }
                        }

                        ProfileSectionTitle("Контактные данные")

                        ProfileSettingsRow(
                            label = "Номер телефона",
                            value = user.phone,
                            actionLabel = if (user.phone.isNullOrBlank()) "Подтвердить" else "Изменить",
                            onAction = { navigationController?.navigateTo("/change-phone") },
                        )
                        ProfileSettingsRow(
                            label = "E-mail",
                            value = user.email,
                            actionLabel = if (user.email.isNullOrBlank()) "Добавить" else "Изменить",
                            onAction = { navigationController?.navigateTo("/change-email") },
                        )
                        ProfileSettingsRow(
                            label = "Пароль",
                            value = "••••••••",
                            actionLabel = "Изменить",
                            onAction = {
                                val login = (user.email ?: user.phone).orEmpty()
                                val path =
                                    if (login.isNotBlank()) {
                                        "/change-password?login=${login.encodeUrlParameter()}"
                                    } else {
                                        "/change-password"
                                    }
                                navigationController?.navigateTo(path)
                            },
                            isLast = true,
                        )

                        ProfileSectionTitle("Уведомления")

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
                                padding(16.px, 0.px)
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
                                        property("accent-color", CSSColors.BlueString)
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
                                        fontSize(CSSTypography.FontSize.base)
                                        color(CSSColors.Black)
                                        lineHeight("1.5")
                                    }
                                },
                            ) {
                                Text("Получать уведомления в Telegram")
                            }
                        }

                        when (val currentTelegramState = telegramState) {
                            is TelegramLinkUiState.LinkPending -> {
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexDirection(FlexDirection.Column)
                                        gap(12.px)
                                        marginBottom(8.px)
                                    }
                                }) {
                                    ProfileGhostButton("Привязать Telegram") {
                                        window.open(currentTelegramState.deepLinkUrl, "_blank")
                                    }
                                    Span({
                                        style {
                                            applyTypography(CSSTypography.Styles.body)
                                            fontSize(CSSTypography.FontSize.sm)
                                            color(CSSColors.Gray4)
                                            lineHeight("1.5")
                                        }
                                    }) {
                                        Text(
                                            "Если кнопка не работает, отправьте код ${currentTelegramState.code} боту ",
                                        )
                                        A(attrs = {
                                            attr("href", "https://t.me/drivebit_bot")
                                            attr("target", "_blank")
                                            attr("rel", "noopener noreferrer")
                                            style {
                                                color(CSSColors.Blue)
                                                property("text-decoration", "underline")
                                                property("text-underline-offset", "2px")
                                                cursor("pointer")
                                            }
                                        }) {
                                            Text("@drivebit_bot")
                                        }
                                    }
                                }
                            }

                            is TelegramLinkUiState.Error -> {
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

@Composable
private fun ProfileSectionTitle(text: String) {
    Div({
        style {
            marginTop(8.px)
            marginBottom(4.px)
            paddingTop(20.px)
            property("border-top", "1px solid ${CSSColors.Gray6String}")
        }
    }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Gray4)
                property("letter-spacing", "0.02em")
                property("text-transform", "uppercase")
            }
        }) {
            Text(text)
        }
    }
}

@Composable
private fun ProfileSettingsRow(
    label: String,
    value: String?,
    actionLabel: String,
    onAction: () -> Unit,
    isLast: Boolean = false,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexWrap(FlexWrap.Wrap)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.SpaceBetween)
            gap(12.px)
            padding(16.px, 0.px)
            if (!isLast) {
                property("border-bottom", "1px solid ${CSSColors.Gray6String}")
            }
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(4.px)
                property("min-width", "0")
                flexGrow(1)
            }
        }) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Gray4)
                }
            }) {
                Text(label)
            }
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(CSSColors.Black)
                    property("word-break", "break-word")
                }
            }) {
                Text(value?.takeIf { it.isNotBlank() } ?: "Не указано")
            }
        }
        ProfileTextAction(actionLabel, onAction)
    }
}

@Composable
private fun ProfileTextAction(
    text: String,
    onClick: () -> Unit,
) {
    Span({
        onClick { onClick() }
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.sm)
            fontWeight(CSSTypography.FontWeight.medium)
            color(CSSColors.Blue)
            cursor("pointer")
            property("white-space", "nowrap")
            property("user-select", "none")
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("text-decoration", "underline")
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("text-decoration", "none")
        }
    }) {
        Text(text)
    }
}

@Composable
private fun ProfileGhostButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button({
        if (!enabled) disabled()
        onClick {
            if (enabled) onClick()
        }
        style {
            height(40.px)
            padding(0.px, 18.px)
            borderRadius(16.px)
            backgroundColor(if (enabled) CSSColors.White else CSSColors.DisabledBg)
            color(if (enabled) CSSColors.Black else CSSColors.Gray5)
            if (enabled) {
                border(1.px, LineStyle.Solid, CSSColors.BorderSubtle)
                cursor("pointer")
            } else {
                border(0.px)
                cursor("not-allowed")
            }
            applyTypography(CSSTypography.Styles.button)
            fontSize(CSSTypography.FontSize.sm)
            property("font-weight", "500")
            property("font-family", "Roboto, sans-serif")
            property("transition", "background-color 0.15s ease")
        }
        onMouseEnter {
            if (enabled) {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.Gray7String,
                )
            }
        }
        onMouseLeave {
            if (enabled) {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.WhiteString,
                )
            }
        }
    }) {
        Text(text)
    }
}
