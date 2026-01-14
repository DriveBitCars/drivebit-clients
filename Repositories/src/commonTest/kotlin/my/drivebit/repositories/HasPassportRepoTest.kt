package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Document
import my.drivebit.network.services.Documents
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeDocuments : Documents {
    var documents: List<Document> = emptyList()
    var shouldThrow = false

    override suspend fun getDocuments(): List<Document> {
        if (shouldThrow) {
            throw Exception("Network error")
        }
        return documents
    }

    override suspend fun uploadDocument(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Document = throw NotImplementedError()
}

private class FakeCarEnumsRepository : CarEnumsRepository {
    var documentTypes: List<EnumItem> = listOf(EnumItem(number = 1, name = "Passport", translate = "Паспорт"))

    override suspend fun getAllDocumentTypes(): List<EnumItem> = documentTypes

    override suspend fun getEnums(): my.drivebit.network.services.CarEnumsResponse = throw NotImplementedError()

    override suspend fun getColorByName(name: String): EnumItem = throw NotImplementedError()

    override suspend fun getBodyTypeByName(name: String): EnumItem = throw NotImplementedError()

    override suspend fun getStatusByName(name: String): EnumItem = throw NotImplementedError()

    override suspend fun getEngineTypeByName(name: String): EnumItem = throw NotImplementedError()

    override suspend fun getTransmissionTypeByName(name: String): EnumItem = throw NotImplementedError()

    override suspend fun getDriveTypeByName(name: String): EnumItem = throw NotImplementedError()

    override suspend fun getAllColors(): List<EnumItem> = throw NotImplementedError()

    override suspend fun getAllBodyTypes(): List<EnumItem> = throw NotImplementedError()

    override suspend fun getAllStatuses(): List<EnumItem> = throw NotImplementedError()

    override suspend fun getAllEngineTypes(): List<EnumItem> = throw NotImplementedError()

    override suspend fun getAllTransmissionTypes(): List<EnumItem> = throw NotImplementedError()

    override suspend fun getAllDriveTypes(): List<EnumItem> = throw NotImplementedError()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HasPassportRepoTest {
    @Test
    fun `hasPasport should return true when passport document exists by type Passport`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Driver License",
                                type = "Passport",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertTrue(result)
        }

    @Test
    fun `hasPasport should return false when document type does not match Passport enum`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Passport",
                                type = "document",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertFalse(result)
        }

    @Test
    fun `hasPasport should return false when Passport enum is not found`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Passport",
                                type = "Passport",
                            ),
                        )
                }
            val fakeCarEnumsRepository =
                FakeCarEnumsRepository().apply {
                    documentTypes = emptyList()
                }
            val repo = HasPassportRepoImpl(fakeDocuments, fakeCarEnumsRepository)

            val result = repo.hasPasport()

            assertFalse(result)
        }

    @Test
    fun `hasPasport should return true when passport type is Passport case insensitive`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Document",
                                type = "passport",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertTrue(result)
        }

    @Test
    fun `hasPasport should return false when no passport document exists`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Driver License",
                                type = "license",
                            ),
                            Document(
                                id = "2",
                                name = "Insurance",
                                type = "insurance",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertFalse(result)
        }

    @Test
    fun `hasPasport should return false when documents list is empty`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents = emptyList()
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertFalse(result)
        }

    @Test
    fun `hasPasport should return false when Documents service throws exception`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    shouldThrow = true
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertFalse(result)
        }

    @Test
    fun `hasPasport should be case insensitive for type Passport`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Document",
                                type = "PASSPORT",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertTrue(result)
        }

    @Test
    fun `hasPasport should return true when passport is in multiple documents`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Driver License",
                                type = "license",
                            ),
                            Document(
                                id = "2",
                                name = "Passport",
                                type = "Passport",
                            ),
                            Document(
                                id = "3",
                                name = "Insurance",
                                type = "insurance",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments, FakeCarEnumsRepository())

            val result = repo.hasPasport()

            assertTrue(result)
        }
}
