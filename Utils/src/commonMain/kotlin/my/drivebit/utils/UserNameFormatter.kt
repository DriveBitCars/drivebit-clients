package my.drivebit.utils

object UserNameFormatter {
    fun formatDisplayName(
        firstName: String?,
        middleName: String?,
        lastName: String?,
        defaultName: String = "Имя",
    ): String {
        val hasName =
            (firstName?.isNotBlank() == true) || (middleName?.isNotBlank() == true) || (lastName?.isNotBlank() == true)
        if (!hasName) {
            return defaultName
        }

        return buildString {
            firstName?.takeIf { it.isNotBlank() }?.let { append(it.trim()) }
            middleName?.takeIf { it.isNotBlank() }?.let {
                append(" ${it.trim()}")
            }
            lastName?.takeIf { it.isNotBlank() }?.let {
                append(" ${it.trim().first()}.")
            }
        }.trim()
    }
}
