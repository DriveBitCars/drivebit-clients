package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.components.AppContainer
import my.drivebit.components.Column
import my.drivebit.components.ErrorText
import my.drivebit.components.Loader
import my.drivebit.components.Logo
import my.drivebit.components.RowSpaceBetween
import my.drivebit.components.SmallBodyBlack
import my.drivebit.components.SmallBodyGray
import my.drivebit.components.SmartHeader
import my.drivebit.components.Spacer
import my.drivebit.components.TextLinkButton
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.mapIso8601ToMonthYearString
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

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

    AppContainer {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(20.px)
            }
        }) {
            Logo()
        }

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
                        ErrorText("Ошибка: ${currentState.message}")
                    }
                }

                is ProfileState.Success -> {
                    val user = currentState.user

                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                        }
                    }) {
                        Column(gap = 8.px, marginBottom = 8.px) {
                            RowSpaceBetween {
                                SmartHeader(
                                    buildString {
                                        val hasName = user.firstName != null || user.lastName != null
                                        if (hasName) {
                                            user.firstName?.let { append(it) }
                                            user.middleName?.let {
                                                if (it.isNotEmpty()) {
                                                    append(" $it")
                                                }
                                            }
                                            user.lastName?.let {
                                                if (it.isNotEmpty()) {
                                                    append(" ${it.first()}.")
                                                }
                                            }
                                        } else {
                                            append("Имя")
                                        }
                                    }.trim(),
                                )

                                if (user.firstName != null || user.lastName != null) {
                                    TextLinkButton(
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
                        }
                        user.createdAt.let { createdAt ->
                            val formattedDate = mapIso8601ToMonthYearString(createdAt)
                            SmallBodyGray("Присоединился $formattedDate")
                        }

                        Spacer(16.px)

                        val phone = user.phone
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                justifyContent(JustifyContent.SpaceBetween)
                            }
                        }) {
                            SmallBodyGray("Номер телефона")
                            if (phone != null && phone.isNotBlank()) {
                                SmallBodyBlack(phone)
                            } else {
                                TextLinkButton(
                                    text = "Подтвердить",
                                    onClick = {},
                                )
                            }
                        }

                        Spacer(16.px)

                        user.email?.let { email ->
                        }
                    }
                }
            }
        }
    }
}
