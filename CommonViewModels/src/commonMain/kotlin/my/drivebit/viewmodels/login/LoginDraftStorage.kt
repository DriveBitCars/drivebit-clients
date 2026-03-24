package my.drivebit.viewmodels.login

data class LoginDraft(
    val identifier: String,
    val termsAccepted: Boolean,
)

interface LoginDraftStorage {
    fun save(draft: LoginDraft)

    fun load(): LoginDraft?

    fun clear()
}

object NoOpLoginDraftStorage : LoginDraftStorage {
    override fun save(draft: LoginDraft) {}

    override fun load(): LoginDraft? = null

    override fun clear() {}
}
