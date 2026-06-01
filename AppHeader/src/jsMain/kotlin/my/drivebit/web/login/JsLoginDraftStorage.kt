package my.drivebit.web.login

import kotlinx.browser.window
import my.drivebit.viewmodels.login.LoginDraft
import my.drivebit.viewmodels.login.LoginDraftStorage

private const val SESSION_LOGIN_IDENTIFIER = "drivebit_login_identifier"
private const val SESSION_LOGIN_TERMS = "drivebit_login_terms"

class JsLoginDraftStorage : LoginDraftStorage {
    override fun save(draft: LoginDraft) {
        window.sessionStorage.setItem(SESSION_LOGIN_IDENTIFIER, draft.identifier)
        window.sessionStorage.setItem(SESSION_LOGIN_TERMS, draft.termsAccepted.toString())
    }

    override fun load(): LoginDraft? {
        val id = window.sessionStorage.getItem(SESSION_LOGIN_IDENTIFIER) ?: return null
        if (id.isBlank()) return null
        val terms = window.sessionStorage.getItem(SESSION_LOGIN_TERMS)
        return LoginDraft(
            identifier = id,
            termsAccepted = terms == "true",
        )
    }

    override fun clear() {
        window.sessionStorage.removeItem(SESSION_LOGIN_IDENTIFIER)
        window.sessionStorage.removeItem(SESSION_LOGIN_TERMS)
    }
}
