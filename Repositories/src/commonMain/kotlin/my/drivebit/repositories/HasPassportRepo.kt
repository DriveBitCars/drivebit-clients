package my.drivebit.repositories

import my.drivebit.network.services.Documents

interface HasPassportRepo {
    suspend fun hasPasport(): Boolean
}

internal class HasPassportRepoImpl(
    private val documents: Documents,
    private val carEnumsRepository: CarEnumsRepository,
) : HasPassportRepo {
    override suspend fun hasPasport(): Boolean =
        runCatching {
            val docs = documents.getDocuments()
            docs.any { doc ->
                doc.type?.contains("Passport", ignoreCase = true) == true
            }
        }.getOrDefault(false)
}
