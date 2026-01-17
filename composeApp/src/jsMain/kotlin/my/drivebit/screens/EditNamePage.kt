package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.components.ActionButton
import my.drivebit.components.ErrorContainer
import my.drivebit.components.PageContainer
import my.drivebit.components.PageWithLogo
import my.drivebit.components.RowButtons
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.network.services.User
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.EditProfileState
import my.drivebit.viewmodels.EditProfileViewModel
import my.drivebit.viewmodels.EditProfileViewModelImpl
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun EditNamePage(currentPath: String = "/edit-name") {
    val userService: User = koinInject()
    val initialFirstName = getUrlParameter("firstName")
    val initialLastName = getUrlParameter("lastName")
    val initialMiddleName = getUrlParameter("middleName")

    val viewModel: EditProfileViewModel =
        remember(initialFirstName, initialLastName, initialMiddleName, userService) {
            EditProfileViewModelImpl(
                userService = userService,
                initialFirstName = initialFirstName,
                initialLastName = initialLastName,
                initialMiddleName = initialMiddleName,
            )
        }

    EditNamePageContent(
        viewModel = viewModel,
    )
}

@Composable
private fun EditNamePageContent(viewModel: EditProfileViewModel) {
    val state by viewModel.state.collectAsState()
    val navigationController = LocalNavigationController.current

    val cancelButtonViewModel = createButtonViewModel()
    val saveButtonViewModel = createButtonViewModel()

    PageWithLogo {
        PageContainer {
            when (val currentState = state) {
                is EditProfileState.Error -> {
                    ErrorContainer {
                        TextError("Ошибка: ${currentState.message}")
                    }
                }

                is EditProfileState.Success -> {
                    LaunchedEffect(Unit) {
                        navigationController?.navigateTo("/profile")
                    }
                }

                is EditProfileState.Initial -> {
                    when {
                        currentState.isLoading -> saveButtonViewModel.setState(ButtonState.Loading)
                        currentState.isButtonEnabled -> saveButtonViewModel.setState(ButtonState.Enabled)
                        else -> saveButtonViewModel.setState(ButtonState.Disabled)
                    }

                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(24.px)
                            maxWidth(400.px)
                        }
                    }) {
                        TextSmartHeader("Редактирование профиля")

                        TextInputField(
                            label = "Имя",
                            value = currentState.firstName,
                            onValueChange = { viewModel.updateFirstName(it) },
                        )

                        TextInputField(
                            label = "Фамилия",
                            value = currentState.lastName,
                            onValueChange = { viewModel.updateLastName(it) },
                        )

                        TextInputField(
                            label = "Отчество",
                            value = currentState.middleName,
                            onValueChange = { viewModel.updateMiddleName(it) },
                        )

                        RowButtons {
                            Div({
                                style {
                                    flex(1)
                                    maxWidth(200.px)
                                }
                            }) {
                                ActionButton(
                                    viewModel = cancelButtonViewModel,
                                    enabledColor = CSSColors.Gray300,
                                    text = "Отмена",
                                    onClick = {
                                        navigationController?.navigateTo("/profile")
                                    },
                                )
                            }

                            Div({
                                style {
                                    flex(1)
                                    maxWidth(200.px)
                                }
                            }) {
                                ActionButton(
                                    viewModel = saveButtonViewModel,
                                    enabledColor = CSSColors.BlueRed,
                                    text = "Сохранить",
                                    onClick = {
                                        viewModel.save()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
