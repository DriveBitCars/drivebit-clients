package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.CarAvailability
import my.drivebit.network.services.CarAvailabilityBlock
import my.drivebit.network.services.CreateAvailabilityBlockRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockCarAvailability : CarAvailability {
    var blocks: List<CarAvailabilityBlock> = emptyList()
    var shouldThrowOnLoad = false
    var shouldThrowOnCreate = false
    var shouldThrowOnDelete = false
    var createCalls = 0
    var deleteCalls = 0
    var lastCreateRequest: CreateAvailabilityBlockRequest? = null
    var lastDeletedBlockId: String? = null

    override suspend fun getBlocks(
        carId: String,
        from: String?,
        to: String?,
    ): List<CarAvailabilityBlock> {
        if (shouldThrowOnLoad) {
            throw NetworkException(HttpStatusCode.InternalServerError, "Load failed")
        }
        return blocks
    }

    override suspend fun createBlock(request: CreateAvailabilityBlockRequest): CarAvailabilityBlock {
        createCalls++
        lastCreateRequest = request
        if (shouldThrowOnCreate) {
            throw NetworkException(HttpStatusCode.BadRequest, "Overlap with booking")
        }
        val block =
            CarAvailabilityBlock(
                id = "new-block-id",
                startAt = request.startAt,
                endAt = request.endAt,
                blockType = "Maintenance",
                blockTypeTranslate = "Техническое обслуживание",
            )
        blocks = blocks + block
        return block
    }

    override suspend fun deleteBlock(blockId: String) {
        deleteCalls++
        lastDeletedBlockId = blockId
        if (shouldThrowOnDelete) {
            throw NetworkException(HttpStatusCode.InternalServerError, "Delete failed")
        }
        blocks = blocks.filter { it.id != blockId }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarAvailabilityViewModelTest {
    @Test
    fun `loadBlocks should populate blocks and disabled dates`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mockService =
                MockCarAvailability().apply {
                    blocks =
                        listOf(
                            CarAvailabilityBlock(
                                id = "block-1",
                                startAt = "2026-06-10T00:00:00Z",
                                endAt = "2026-06-12T00:00:00Z",
                                blockTypeTranslate = "Техническое обслуживание",
                            ),
                        )
                }
            val viewModel =
                CarAvailabilityViewModelImpl(
                    carId = "car-1",
                    carAvailability = mockService,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarAvailabilityState.Success>(state)
            assertEquals(1, state.blocks.size)
            assertTrue(state.disabledDates.contains("2026-06-10"))
            assertTrue(state.disabledDates.contains("2026-06-11"))
        }

    @Test
    fun `createBlock should send request and reload list`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mockService = MockCarAvailability()
            val viewModel =
                CarAvailabilityViewModelImpl(
                    carId = "car-1",
                    carAvailability = mockService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            viewModel.updateSelectedStartDate("2026-06-15")
            viewModel.updateSelectedEndDate("2026-06-16")
            viewModel.createBlock()
            advanceUntilIdle()

            assertEquals(1, mockService.createCalls)
            assertEquals("car-1", mockService.lastCreateRequest?.carId)
            assertEquals("Maintenance", mockService.lastCreateRequest?.blockType)
            val success = viewModel.state.value as CarAvailabilityState.Success
            assertEquals(1, success.blocks.size)
            assertNull(success.selectedStartDate)
            assertNull(success.selectedEndDate)
        }

    @Test
    fun `createBlock should show error when dates not selected`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mockService = MockCarAvailability()
            val viewModel =
                CarAvailabilityViewModelImpl(
                    carId = "car-1",
                    carAvailability = mockService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            viewModel.createBlock()
            advanceUntilIdle()

            assertEquals(0, mockService.createCalls)
            val success = viewModel.state.value as CarAvailabilityState.Success
            assertEquals("Выберите даты для блокировки", success.formError)
        }

    @Test
    fun `deleteBlock should call service and reload`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mockService =
                MockCarAvailability().apply {
                    blocks =
                        listOf(
                            CarAvailabilityBlock(
                                id = "block-1",
                                startAt = "2026-06-10T00:00:00Z",
                                endAt = "2026-06-11T00:00:00Z",
                            ),
                        )
                }
            val viewModel =
                CarAvailabilityViewModelImpl(
                    carId = "car-1",
                    carAvailability = mockService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            viewModel.deleteBlock("block-1")
            advanceUntilIdle()

            assertEquals(1, mockService.deleteCalls)
            assertEquals("block-1", mockService.lastDeletedBlockId)
            val success = viewModel.state.value as CarAvailabilityState.Success
            assertTrue(success.blocks.isEmpty())
        }

    @Test
    fun `createBlock should propagate backend error`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mockService =
                MockCarAvailability().apply {
                    shouldThrowOnCreate = true
                }
            val viewModel =
                CarAvailabilityViewModelImpl(
                    carId = "car-1",
                    carAvailability = mockService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            viewModel.updateSelectedStartDate("2026-06-15")
            viewModel.createBlock()
            advanceUntilIdle()

            val success = viewModel.state.value as CarAvailabilityState.Success
            assertEquals("Overlap with booking", success.formError)
        }
}
