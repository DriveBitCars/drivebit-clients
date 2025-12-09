package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
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
                                            width(80.px)
                                            height(80.px)
                                            borderRadius(50.percent)
                                        }
                                    },
                                )
                                LinkButton(
                                    text = "Изменить",
                                    onClick = {
                                        navigationController?.navigateTo("/edit-avatar")
                                    },
                                )
                            }
                        }

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
