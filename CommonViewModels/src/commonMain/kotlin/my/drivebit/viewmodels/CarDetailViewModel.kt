package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarAvailability
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.Photo
import my.drivebit.network.services.Review
import my.drivebit.network.services.ReviewListItemDTO
import my.drivebit.utils.parseDisabledDatesFromIsoIntervals
import kotlin.runCatching

data class CarReviewUi(
    val id: String,
    val authorDisplayName: String,
    val stars: Int,
    val text: String?,
    val createdAtDisplay: String?,
)

sealed interface CarDetailState {
    data object Loading : CarDetailState

    data class Success(
        val car: CarDetailResponse,
        val owner: CarOwnerUi? = null,
        val disabledBookingDates: Set<String> = emptySet(),
        val reviews: List<CarReviewUi> = emptyList(),
        val reviewsPage: Int = 1,
        val reviewsTotalPages: Int = 0,
        val reviewsTotalCount: Int = 0,
        val reviewsLoading: Boolean = false,
        val reviewsError: String? = null,
    ) : CarDetailState

    data class Error(
        val message: String,
    ) : CarDetailState
}

data class CarOwnerUi(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val memberSince: String?,
)

interface CarDetailViewModel {
    val state: StateFlow<CarDetailState>

    fun loadReviewsPage(page: Int)
}

private const val CAR_DETAIL_REVIEWS_PAGE_SIZE = 10

class CarDetailViewModelImpl(
    private val carService: Car,
    private val photoService: Photo,
    private val carAvailability: CarAvailability,
    private val reviewService: Review,
    private val carId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarDetailViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<CarDetailState>(CarDetailState.Loading)

    override val state: StateFlow<CarDetailState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { CarDetailState.Loading }
            runCatching {
                carService.getCar(carId)
            }.onSuccess { car ->
                _state.update {
                    CarDetailState.Success(
                        car = car,
                        reviewsLoading = true,
                    )
                }
                loadOwner(
                    ownerId = car.general.owner,
                    ownerName = car.general.ownerName,
                )
                loadBookingBlocks()
                loadReviewsPage(1)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить информацию об автомобиле",
                    )
                _state.update { CarDetailState.Error(errorMessage) }
            }
        }
    }

    private fun loadBookingBlocks() {
        if (carId.isBlank()) {
            return
        }
        viewModelScope.launch {
            val disabled =
                runCatching {
                    val blocks = carAvailability.getBlocks(carId)
                    parseDisabledDatesFromIsoIntervals(blocks.map { it.startAt to it.endAt })
                }.getOrDefault(emptySet())
            _state.update { current ->
                if (current is CarDetailState.Success) {
                    current.copy(disabledBookingDates = disabled)
                } else {
                    current
                }
            }
        }
    }

    private fun loadOwner(
        ownerId: String?,
        ownerName: String?,
    ) {
        if (ownerId.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            val owner =
                runCatching {
                    val avatarFromService = photoService.getAvatarByUserId(ownerId)?.url

                    CarOwnerUi(
                        id = ownerId,
                        name = buildOwnerName(ownerName),
                        avatarUrl = avatarFromService,
                        memberSince = null,
                    )
                }.getOrElse {
                    CarOwnerUi(
                        id = ownerId,
                        name = buildOwnerName(ownerName),
                        avatarUrl = null,
                        memberSince = null,
                    )
                }

            _state.update { current ->
                if (current is CarDetailState.Success) {
                    current.copy(owner = owner)
                } else {
                    current
                }
            }
        }
    }

    private fun buildOwnerName(ownerName: String?): String = ownerName?.takeIf { it.isNotBlank() } ?: "Владелец"

    override fun loadReviewsPage(page: Int) {
        if (carId.isBlank()) {
            return
        }
        val safePage = page.coerceAtLeast(1)
        viewModelScope.launch {
            _state.update { current ->
                if (current is CarDetailState.Success) {
                    current.copy(
                        reviewsLoading = true,
                        reviewsError = null,
                    )
                } else {
                    current
                }
            }
            runCatching {
                reviewService.getReviewsByCarId(
                    carId = carId,
                    page = safePage,
                    pageSize = CAR_DETAIL_REVIEWS_PAGE_SIZE,
                )
            }.onSuccess { dto ->
                _state.update { current ->
                    if (current is CarDetailState.Success) {
                        current.copy(
                            reviews = dto.reviews.map { mapReviewUi(it) },
                            reviewsPage = dto.page,
                            reviewsTotalPages = dto.totalPages,
                            reviewsTotalCount = dto.totalCount,
                            reviewsLoading = false,
                            reviewsError = null,
                        )
                    } else {
                        current
                    }
                }
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить отзывы",
                    )
                _state.update { current ->
                    if (current is CarDetailState.Success) {
                        current.copy(
                            reviewsLoading = false,
                            reviewsError = message,
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun mapReviewUi(dto: ReviewListItemDTO): CarReviewUi =
        CarReviewUi(
            id = dto.id,
            authorDisplayName = dto.authorName?.takeIf { it.isNotBlank() } ?: "Пользователь",
            stars = dto.stars,
            text = dto.text,
            createdAtDisplay = formatReviewDate(dto.createdAt),
        )

    private fun formatReviewDate(iso: String?): String? {
        if (iso.isNullOrBlank()) {
            return null
        }
        val t = iso.indexOf('T')
        return if (t > 0) {
            iso.substring(0, t)
        } else {
            iso
        }
    }
}
