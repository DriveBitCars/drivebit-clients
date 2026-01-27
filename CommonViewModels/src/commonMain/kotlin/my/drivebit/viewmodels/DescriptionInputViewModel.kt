package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import my.drivebit.repositories.CarDataRepository

data class DescriptionInputState(
    val description: String,
    val canSubmit: Boolean,
)

interface DescriptionInputViewModel {
    val state: StateFlow<DescriptionInputState>

    fun updateDescription(value: String)

    fun submit(): Boolean
}

class DescriptionInputViewModelImpl(
    private val carDataRepository: CarDataRepository,
) : DescriptionInputViewModel {
    private val _state =
        MutableStateFlow(
            DescriptionInputState(
                description = carDataRepository.getDescription() ?: "",
                canSubmit = true,
            ),
        )

    override val state: StateFlow<DescriptionInputState>
        get() = _state.asStateFlow()

    override fun updateDescription(value: String) {
        _state.update {
            it.copy(
                description = value,
                canSubmit = true,
            )
        }
    }

    override fun submit(): Boolean {
        val value = _state.value.description.trim()
        carDataRepository.saveDescription(value)
        return true
    }
}
