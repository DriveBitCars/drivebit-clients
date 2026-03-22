package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.CreateReviewRequest
import my.drivebit.network.services.Review

data class CreateReviewState(
    val stars: Int? = null,
    val text: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

interface CreateReviewViewModel {
    val state: StateFlow<CreateReviewState>

    fun setStars(stars: Int)

    fun setText(text: String)

    fun submit()

    fun consumeSuccess()
}

class CreateReviewViewModelImpl(
    private val review: Review,
    private val carId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CreateReviewViewModel {
    private val _state = MutableStateFlow(CreateReviewState())
    override val state: StateFlow<CreateReviewState> = _state.asStateFlow()

    override fun setStars(stars: Int) {
        val clamped = stars.coerceIn(1, 5)
        _state.update { it.copy(stars = clamped, error = null) }
    }

    override fun setText(text: String) {
        val trimmed = if (text.length > 1000) text.take(1000) else text
        _state.update { it.copy(text = trimmed) }
    }

    override fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        val stars =
            current.stars ?: run {
                _state.update { it.copy(error = "Выберите оценку от 1 до 5") }
                return
            }
        coroutineScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                review.createReview(
                    CreateReviewRequest(
                        carId = carId,
                        stars = stars,
                        text = current.text.trim().ifBlank { null },
                    ),
                )
            }.onSuccess {
                _state.update { it.copy(isSubmitting = false, success = true) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error =
                            ErrorHandler.extractErrorMessage(
                                exception = e,
                                defaultNetworkError = "Ошибка сети",
                                defaultGenericError = "Не удалось отправить отзыв",
                            ),
                    )
                }
            }
        }
    }

    override fun consumeSuccess() {
        _state.update { it.copy(success = false) }
    }
}
