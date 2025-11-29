package my.drivebit.navigation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationControllerTest {
    private lateinit var navigationState: NavigationState
    private lateinit var navigationController: NavigationController

    @BeforeTest
    fun setUp() {
        navigationState = NavigationState()
        navigationController = NavigationController(navigationState)
    }

    @Test
    fun testNavigateTo() {
        navigationController.navigateTo("/login")

        assertEquals("/login", navigationState.getCurrentPath())
    }

    @Test
    fun testReplacePath() {
        navigationController.navigateTo("/login")
        navigationController.replacePath("/signup")

        assertEquals("/signup", navigationState.getCurrentPath())
    }

    @Test
    fun testMultipleNavigations() {
        navigationController.navigateTo("/login")
        assertEquals("/login", navigationState.getCurrentPath())

        navigationController.navigateTo("/signup")
        assertEquals("/signup", navigationState.getCurrentPath())

        navigationController.navigateTo("/")
        assertEquals("/", navigationState.getCurrentPath())
    }

    @Test
    fun testNavigateToUpdatesState() {
        navigationController.navigateTo("/login")
        val newPath = navigationState.getCurrentPath()

        assertEquals("/login", newPath)
    }

    @Test
    fun testReplacePathUpdatesState() {
        navigationController.navigateTo("/login")
        navigationController.replacePath("/home")

        assertEquals("/home", navigationState.getCurrentPath())
    }

    @Test
    fun testGetCurrentPath() {
        val initialPath = navigationController.getCurrentPath()
        assertTrue(initialPath.isNotEmpty(), "Current path should not be empty")
    }
}
