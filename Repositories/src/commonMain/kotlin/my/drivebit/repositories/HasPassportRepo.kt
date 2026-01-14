package my.drivebit.repositories

import my.drivebit.network.services.Documents

interface HasPassportRepo {
    suspend fun hasPasport(): Boolean
}

internal class HasPassportRepoImpl(
    private val documents: Documents,
) : HasPassportRepo {
    override suspend fun hasPasport(): Boolean =
        try {
            val docs = documents.getDocuments()
            docs.any { doc ->
                doc.type?.lowercase()?.contains("passport", ignoreCase = true) == true ||
                    doc.name?.lowercase()?.contains("passport", ignoreCase = true) == true ||
                    doc.type?.lowercase()?.contains("паспорт", ignoreCase = true) == true ||
                    doc.name?.lowercase()?.contains("паспорт", ignoreCase = true) == true
            }
        } catch (e: Exception) {
            false
        }
}
