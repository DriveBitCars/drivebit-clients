package my.drivebit.viewmodels.login

import kotlinx.coroutines.flow.StateFlow
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.ValidatorViewModel

interface LoginMviViewModel {
    val uiState: StateFlow<LoginUiState>

    fun handleIntent(intent: LoginIntent)

    val validatorViewModel: ValidatorViewModel

    fun asAuthFormViewModel(): AuthFormViewModel

    fun clearDraftStorage()
}
