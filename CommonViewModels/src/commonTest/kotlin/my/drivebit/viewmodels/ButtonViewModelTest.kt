package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ButtonViewModelTest {
    @Test
    fun `initial state should be Enabled`() =
        runTest {
            val viewModel = ButtonViewModel()
            assertTrue(viewModel.isEnabled)
            assertFalse(viewModel.isDisabled)
            assertFalse(viewModel.isLoading)
            assertTrue(viewModel.state.value is ButtonState.Enabled)
        }

    @Test
    fun `setState should change state to Disabled`() =
        runTest {
            val viewModel = ButtonViewModel()
            viewModel.setState(ButtonState.Disabled)
            assertFalse(viewModel.isEnabled)
            assertTrue(viewModel.isDisabled)
            assertFalse(viewModel.isLoading)
            assertTrue(viewModel.state.value is ButtonState.Disabled)
        }

    @Test
    fun `setState should change state to Loading`() =
        runTest {
            val viewModel = ButtonViewModel()
            viewModel.setState(ButtonState.Loading)
            assertFalse(viewModel.isEnabled)
            assertFalse(viewModel.isDisabled)
            assertTrue(viewModel.isLoading)
            assertTrue(viewModel.state.value is ButtonState.Loading)
        }

    @Test
    fun `setState should change state back to Enabled`() =
        runTest {
            val viewModel = ButtonViewModel()
            viewModel.setState(ButtonState.Disabled)
            viewModel.setState(ButtonState.Enabled)
            assertTrue(viewModel.isEnabled)
            assertFalse(viewModel.isDisabled)
            assertFalse(viewModel.isLoading)
            assertTrue(viewModel.state.value is ButtonState.Enabled)
        }

    @Test
    fun `state transitions should work correctly`() =
        runTest {
            val viewModel = ButtonViewModel()
            assertTrue(viewModel.state.value is ButtonState.Enabled)

            viewModel.setState(ButtonState.Loading)
            assertTrue(viewModel.state.value is ButtonState.Loading)

            viewModel.setState(ButtonState.Disabled)
            assertTrue(viewModel.state.value is ButtonState.Disabled)

            viewModel.setState(ButtonState.Enabled)
            assertTrue(viewModel.state.value is ButtonState.Enabled)
        }

    @Test
    fun `when disabled clicks should not pass`() =
        runTest {
            val viewModel = ButtonViewModel()
            var clickCount = 0

            viewModel.setState(ButtonState.Disabled)
            assertFalse(viewModel.isEnabled)
            assertTrue(viewModel.isDisabled)

            if (viewModel.isEnabled) {
                clickCount++
            }

            assertEquals(0, clickCount)
        }

    @Test
    fun `when loading clicks should not pass`() =
        runTest {
            val viewModel = ButtonViewModel()
            var clickCount = 0

            viewModel.setState(ButtonState.Loading)
            assertFalse(viewModel.isEnabled)
            assertTrue(viewModel.isLoading)

            if (viewModel.isEnabled) {
                clickCount++
            }

            assertEquals(0, clickCount)
        }
}
