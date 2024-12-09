import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.time_wallet_3.view.TimeLogs.ViewLogsScreen
import com.example.time_wallet_3.viewmodel.viewmodel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewLogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    @Test
    fun testFloatingActionButtonNavigatesToCreateLog() {
        // Arrange
        val mockViewModel = mockk<viewmodel>(relaxed = true) // Use MockK for mocking
        val navController = rememberNavController()

        composeTestRule.setContent {
            ViewLogsScreen(navController = navController, viewModel = mockViewModel)
        }

        // Act
        composeTestRule.onNodeWithText("+").performClick()

        // Assert
        val currentDestination = navController.currentBackStackEntry?.destination?.route
        assert(currentDestination == "create_log") {
            "Expected destination 'create_log' but found $currentDestination"
        }
    }
}
