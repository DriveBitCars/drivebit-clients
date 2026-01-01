package my.drivebit.viewmodels

object NumberFormatter {
    fun formatDouble(value: Double?): String {
        if (value == null) return ""
        val isWhole = value % 1.0 == 0.0
        return if (isWhole) "${value.toInt()}.0" else value.toString()
    }

    fun formatInt(value: Int?): String = value?.toString() ?: ""
}
