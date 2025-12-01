package my.drivebit.navigation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationStateTest {
    private lateinit var navigationState: NavigationState

    @BeforeTest
    fun setUp() {
        navigationState = NavigationState()
    }

    @Test
    fun testInitialPath() {
        val initialPath = navigationState.getCurrentPath()
        assertTrue(initialPath.isNotEmpty(), "Initial path should not be empty")
    }

    @Test
    fun testUpdatePath() {
        navigationState.updatePath("/login")
        val path = navigationState.getCurrentPath()
        assertEquals("/login", path)
    }

    @Test
    fun testGetCurrentPath() {
        navigationState.updatePath("/signup")
        assertEquals("/signup", navigationState.getCurrentPath())
    }

    @Test
    fun testMultiplePathUpdates() {
        navigationState.updatePath("/login")
        assertEquals("/login", navigationState.getCurrentPath())

        navigationState.updatePath("/signup")
        assertEquals("/signup", navigationState.getCurrentPath())

        navigationState.updatePath("/")
        assertEquals("/", navigationState.getCurrentPath())
    }

    @Test
    fun testPathStateUpdates() {
        navigationState.updatePath("/login")
        assertEquals("/login", navigationState.getCurrentPath())

        navigationState.updatePath("/signup")
        assertEquals("/signup", navigationState.getCurrentPath())
    }
}
