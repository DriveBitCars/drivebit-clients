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
            val documentTypes = carEnumsRepository.getAllDocumentTypes()
            val passportEnum =
                documentTypes.find { it.name.equals("Passport", ignoreCase = true) }
                    ?: return false

            val docs = documents.getDocuments()
            docs.any { doc ->
                doc.type?.equals(passportEnum.name, ignoreCase = true) == true
            }
        }.getOrDefault(false)
}
