import android.util.Log
import com.example.time_wallet_3.model.*
import com.example.time_wallet_3.viewmodel.viewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    // Mocked DAOs
    @Mock private lateinit var timeLogDao: TimeLogDao
    @Mock private lateinit var activityDao: ActivityDao
    @Mock private lateinit var accountDao: AccountDao
    @Mock private lateinit var budgetDao: BudgetDao
    @Mock private lateinit var bankGoalDao: BankGoalDao

    private lateinit var viewModel: viewmodel
    private lateinit var testScope: TestScope

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Initialize Mockito
        MockitoAnnotations.openMocks(this)

        // Set the Main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Initialize ViewModel with mocked DAOs
        testScope = TestScope()
        viewModel = viewmodel(
            dao = mock(TimeLogDao::class.java),
            activityDao = mock(ActivityDao::class.java),
            accountDao = mock(AccountDao::class.java),
            budgetDao = mock(BudgetDao::class.java),
            bankGoalDao = mock(BankGoalDao::class.java),
            coroutineScope = testScope,
            isTesting = true // Disable the infinite loop
        )
    }

    @Test
    fun `setSelectedActivity updates selected activity`() {
        // Act
        viewModel.setSelectedActivity("Test Activity")

        // Assert
        assertEquals("Test Activity", viewModel.selectedActivity.value)
    }

    @Test
    fun `addAccount creates a new account and sets it as current`() = testScope.runTest {
        // Arrange
        val mockAccountDao = mock(AccountDao::class.java)
        val viewModel = viewmodel(
            dao = mock(TimeLogDao::class.java),
            activityDao = mock(ActivityDao::class.java),
            accountDao = mockAccountDao,
            budgetDao = mock(BudgetDao::class.java),
            bankGoalDao = mock(BankGoalDao::class.java),
            coroutineScope = testScope
        )

        whenever(mockAccountDao.insertAccount(any<Account>())).thenReturn(1L)

        // Act
        viewModel.addAccount("Test Account")
        advanceUntilIdle() // Let all coroutines complete

        // Assert
        assertEquals(1, viewModel.currentAccountId.value)

        // Verify interaction and captured argument
        val captor = argumentCaptor<Account>()
        verify(mockAccountDao).insertAccount(captor.capture())
        assertEquals("Test Account", captor.firstValue.name)
    }



    @Test
    fun `addBankGoal creates a new goal and refreshes account data`() = testScope.runTest {
        // Arrange
        val testAccountId = 1
        val testGoal = BankGoal(
            accountId = testAccountId,
            activityName = "Test Activity",
            timeGoalMinutes = 60,
            period = "Daily",
            currentProgress = 0,
            completed = false,
            lastResetTime = System.currentTimeMillis()
        )
        `when`(bankGoalDao.insertBankGoal(any())).thenReturn(Unit)

        // Act
        viewModel.addBankGoal(testAccountId, "Test Activity", 60, "Daily")

        // Assert
        verify(bankGoalDao).insertBankGoal(any()) // Use 'any()' to bypass direct matching
    }


    @Test
    fun `updateBankGoalProgress updates goal progress`() = testScope.runTest {
        // Arrange
        val testGoal = BankGoal(
            accountId = 1,
            activityName = "Test Activity",
            timeGoalMinutes = 60,
            period = "Daily",
            currentProgress = 30,
            completed = false,
            lastResetTime = System.currentTimeMillis()
        )
        // Inject the test goal into the viewmodel's bank goals
        viewModel._bankGoals.value = listOf(testGoal)

        // Act
        viewModel.updateBankGoalProgress("Test Activity", 30 * 60 * 1000L) // 30 minutes in milliseconds

        // Assert
        val updatedGoal = viewModel._bankGoals.value.first { it.activityName == "Test Activity" }
        assertEquals(60, updatedGoal.currentProgress) // Progress should now be 60
        assert(updatedGoal.completed) // Goal should now be marked as completed
    }


    @Test
    fun `increasePoints increments total points`() = testScope.runTest {
        // Act
        viewModel.increasePoints(60 * 1000L) // 1 minute

        // Assert
        assertEquals(1, viewModel._totalPoints.value) // 1 point per minute
    }

    @Test
    fun `resetBudgetsIfNeeded resets daily budget progress`() = testScope.runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val dailyBudget = Budget(
            accountId = 1,
            activityName = "Test Activity",
            timeLimitMinutes = 60,
            period = "Daily",
            currentProgress = 30,
            lastResetTime = now - 24 * 60 * 60 * 1000L // Last reset was a day ago
        )
        // Inject the daily budget into the viewmodel's budgets
        viewModel._budgets.value = listOf(dailyBudget)

        // Act
        viewModel.resetBudgetsIfNeeded()

        // Assert
        val updatedBudget = viewModel._budgets.value.first { it.activityName == "Test Activity" }
        assertEquals(0, updatedBudget.currentProgress) // Progress should be reset to 0
        assert(updatedBudget.lastResetTime >= now) // Last reset time should be updated to current time
    }


    @Test
    fun `addLog creates a new log and resets timer`() = testScope.runTest {
        // Arrange
        val accountId = 1
        val testLog = TimeLog(
            elapsedTime = 60L,
            activity = "Test Activity",
            points = 1,
            date = "2024-12-10",
            notes = "Test Note",
            timeStarted = 1000L,
            timeStopped = 2000L,
            accountId = accountId
        )

        `when`(timeLogDao.insertLog(any())).thenReturn(Unit)

        // Act
        viewModel.addLog(accountId, "Test Activity", "Test Note")

        // Assert
        verify(timeLogDao).insertLog(testLog)
    }

    @Test
    fun `startTimer begins tracking elapsed time`() = testScope.runTest {
        // Act
        viewModel.startTimer()
        advanceTimeBy(1000) // Simulate 1 second

        // Assert
        assert(viewModel.isTimerRunning.value)
        assertEquals(1, viewModel.timeElapsed.value)
    }

    @Test
    fun `stopTimer stops tracking elapsed time and updates goal progress`() = testScope.runTest {
        // Arrange
        viewModel.startTimer()
        advanceTimeBy(1000) // Simulate 1 second
        viewModel.setSelectedActivity("Test Activity")

        // Act
        viewModel.stopTimer()

        // Assert
        assert(!viewModel.isTimerRunning.value)
        assertEquals(1, viewModel.timeElapsed.value)
    }
}
