package my.drivebit.viewmodels

import my.drivebit.shared.storage.Storage

interface ButterViewModel {
    val state: List<ButterModel>
}

data class ButterModel(
    val iconUrl: String? = null,
    val text: String,
    val onClick: () -> Unit
)

private val login = ButterModel(
    text = "Логин",
    onClick = TODO()
)

private val registr = ButterModel(
        text = "Регистрация",
        onClick = TODO()
    )

private val beCameAHost = ButterModel(
    iconUrl = "images/butter/car-icon.svg",
    text = "Сдать авто",
    onClick = TODO()
)

class ButterViewModelImpl(
    private val storage: Storage
) : ButterViewModel {
    override val state: List<ButterModel> =
            buildList {
                if (storage.isLogined().not()) {
                    add(login)
                    add(registr)
                }
                add(beCameAHost)
            }
    }



