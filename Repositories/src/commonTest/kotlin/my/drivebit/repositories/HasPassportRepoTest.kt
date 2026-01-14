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
}

@OptIn(ExperimentalCoroutinesApi::class)
class HasPassportRepoTest {
    @Test
    fun `hasPasport should return true when passport document exists by type`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Driver License",
                                type = "passport",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments)

            val result = repo.hasPasport()

            assertTrue(result)
        }

    @Test
    fun `hasPasport should return true when passport document exists by name`() =
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
            val repo = HasPassportRepoImpl(fakeDocuments)

            val result = repo.hasPasport()

            assertTrue(result)
        }

    @Test
    fun `hasPasport should return true when passport document exists in Russian`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Паспорт",
                                type = "document",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments)

            val result = repo.hasPasport()

            assertTrue(result)
        }

    @Test
    fun `hasPasport should return true when passport type is in Russian`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "Document",
                                type = "паспорт",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments)

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
            val repo = HasPassportRepoImpl(fakeDocuments)

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
            val repo = HasPassportRepoImpl(fakeDocuments)

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
            val repo = HasPassportRepoImpl(fakeDocuments)

            val result = repo.hasPasport()

            assertFalse(result)
        }

    @Test
    fun `hasPasport should be case insensitive`() =
        runTest {
            val fakeDocuments =
                FakeDocuments().apply {
                    documents =
                        listOf(
                            Document(
                                id = "1",
                                name = "PASSPORT",
                                type = "DOCUMENT",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments)

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
                                type = "document",
                            ),
                            Document(
                                id = "3",
                                name = "Insurance",
                                type = "insurance",
                            ),
                        )
                }
            val repo = HasPassportRepoImpl(fakeDocuments)

            val result = repo.hasPasport()

            assertTrue(result)
        }
}
