package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.InputFieldType
import my.drivebit.viewmodels.ValidatorViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun InputField(
    authFormViewModel: AuthFormViewModel,
    validatorViewModel: ValidatorViewModel,
    inputValue: MutableState<String>,
    onFormattedValueCommitted: ((String) -> Unit)? = null,
) {
    inputFieldInternal(
        viewModel = validatorViewModel,
        label = authFormViewModel.fieldLabel,
        inputType =
            when (authFormViewModel.inputType) {
                InputFieldType.Phone -> InputType.Tel
                InputFieldType.Email -> InputType.Email
            },
        autocomplete = authFormViewModel.autocomplete,
        inputName = authFormViewModel.inputName,
        inputValue = inputValue,
        onFormattedValueCommitted = onFormattedValueCommitted,
    )
}

@Composable
private fun inputFieldInternal(
    viewModel: ValidatorViewModel,
    label: String,
    inputType: InputType<*>,
    autocomplete: String,
    inputName: String,
    inputValue: MutableState<String>,
    onFormattedValueCommitted: ((String) -> Unit)?,
) {
    val value by inputValue
    val validationState by viewModel.validationState.collectAsState()
    val validationError =
        when (val state = validationState) {
            is my.drivebit.viewmodels.ValidationState.Error -> state.message
            else -> null
        }

    Column(gap = 8.px) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Gray600)
            }
        }) {
            Text(label)
        }
        Input(
            type = inputType,
            attrs = {
                attr("autocomplete", autocomplete)
                attr("name", inputName)
                value(value)
                onInput { event ->
                    val newValue = (event.target as org.w3c.dom.HTMLInputElement).value
                    val formattedValue = viewModel.formatInput(newValue)
                    inputValue.value = formattedValue
                    if (onFormattedValueCommitted != null) {
                        onFormattedValueCommitted(formattedValue)
                    } else {
                        viewModel.validateInput(formattedValue)
                    }
                }
                style {
                    width(100.percent)
                    padding(12.px, 16.px)
                    borderRadius(8.px)
                    property("box-sizing", "border-box")
                    val hasError = validationError != null
                    val borderColor =
                        if (hasError) {
                            CSSColors.RedString
                        } else {
                            CSSColors.Gray300String
                        }
                    property("border", "1px solid $borderColor")
                    property("font-size", "16px")
                    property("outline", "none")
                    property("transition", "border-color 0.2s ease")
                }
                onFocus {
                    val hasError = validationError != null
                    val borderColor =
                        if (hasError) {
                            CSSColors.RedString
                        } else {
                            CSSColors.BlueString
                        }
                    (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                        "border-color",
                        borderColor,
                    )
                }
                onBlur {
                    if (onFormattedValueCommitted != null) {
                        onFormattedValueCommitted(value)
                    } else {
                        viewModel.validateInput(value)
                    }
                    val hasError = validationError != null
                    val borderColor =
                        if (hasError) {
                            CSSColors.RedString
                        } else {
                            CSSColors.Gray300String
                        }
                    (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                        "border-color",
                        borderColor,
                    )
                }
                onKeyDown { event ->
                    val inputElement = event.target as org.w3c.dom.HTMLInputElement
                    if (event.key == "Backspace" &&
                        inputElement.selectionStart == 0 &&
                        inputElement.selectionEnd == 0
                    ) {
                        event.preventDefault()
                    }
                }
            },
        )
        if (validationError != null) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Red)
                }
            }) {
                Text(validationError)
            }
        }
    }
}
