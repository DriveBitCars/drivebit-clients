package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Document
import my.drivebit.network.services.Documents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModelTest {
    @Test
    fun `uploadDocument deletes existing document of same type before upload`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val documents = FakeDocumentsService()
            val viewModel =
                DocumentsViewModelImpl(
                    documents = documents,
                    coroutineScope = testScope,
                )

            documents.documents =
                listOf(
                    Document(
                        id = 10,
                        type = "PassportMainPageRus",
                        status = "Rejected",
                        uploadDate = "2026-01-01",
                    ),
                )
            viewModel.load()
            advanceUntilIdle()
            assertIs<DocumentsState.Success>(viewModel.state.value)

            viewModel.uploadDocument(
                documentType = "PassportMainPageRus",
                fileBytes = byteArrayOf(1),
                fileName = "passport.jpg",
                contentType = "image/jpeg",
            )
            advanceUntilIdle()

            assertEquals(listOf(10), documents.deletedIds)
            assertEquals(1, documents.uploadCount)
            assertIs<DocumentsState.Success>(viewModel.state.value)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `uploadDocument does not delete when slot is empty`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val documents = FakeDocumentsService()
            val viewModel =
                DocumentsViewModelImpl(
                    documents = documents,
                    coroutineScope = testScope,
                )

            viewModel.load()
            advanceUntilIdle()

            viewModel.uploadDocument(
                documentType = "PassportMainPageRus",
                fileBytes = byteArrayOf(1),
                fileName = "passport.jpg",
                contentType = "image/jpeg",
            )
            advanceUntilIdle()

            assertEquals(emptyList(), documents.deletedIds)
            assertEquals(1, documents.uploadCount)
            testScope.coroutineContext.cancelChildren()
        }
}

private class FakeDocumentsService : Documents {
    var documents: List<Document> = emptyList()
    val deletedIds = mutableListOf<Int>()
    var uploadCount = 0

    override suspend fun getDocuments(): List<Document> = documents

    override suspend fun uploadDocument(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        documentType: String,
        carId: String?,
    ): Document {
        uploadCount++
        val doc =
            Document(
                id = 99,
                type = documentType,
                status = "Pending",
            )
        documents = documents.filter { it.type != documentType } + doc
        return doc
    }

    override suspend fun getDocumentUrl(documentId: Int): String = "https://example.com/$documentId"

    override suspend fun deleteDocument(documentId: Int) {
        deletedIds += documentId
        documents = documents.filter { it.id != documentId }
    }
}
