package my.drivebit.viewmodels.login

sealed interface LoginIntent {
    data class InputChanged(
        val raw: String,
    ) : LoginIntent

    data class TermsChanged(
        val accepted: Boolean,
    ) : LoginIntent

    data object Submit : LoginIntent

    data object ClearAuthError : LoginIntent

    data object RestoreDraft : LoginIntent
}
