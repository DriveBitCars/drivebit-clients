package my.drivebit.utils

data class SearchDateRangeUpdate(
    val startDate: String?,
    val endDate: String?,
)

/**
 * Search dates are applied only as a complete «с»+«по» pair (or cleared together).
 * A partial draft (only start or only end) must not navigate / reload search.
 */
fun resolveSearchDateRangeNavigation(
    appliedStart: String?,
    appliedEnd: String?,
    draftStart: String?,
    draftEnd: String?,
): SearchDateRangeUpdate? {
    val draftComplete = draftStart != null && draftEnd != null
    val draftCleared = draftStart == null && draftEnd == null
    val appliedHasRange = appliedStart != null || appliedEnd != null

    return when {
        draftComplete -> {
            val update = SearchDateRangeUpdate(startDate = draftStart, endDate = draftEnd)
            if (update.startDate == appliedStart && update.endDate == appliedEnd) null else update
        }
        draftCleared && appliedHasRange -> SearchDateRangeUpdate(startDate = null, endDate = null)
        else -> null
    }
}
