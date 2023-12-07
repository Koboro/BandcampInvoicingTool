import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import sales.ReleaseSale
import java.time.LocalDate

class ReleaseTest {

    companion object {
        private const val CAT_NO = "test-cat-no"

        private const val ARTIST_1_NAME = "test-artist-name-1"
    }

    @Test
    fun testCalculatePayout_HappyPath_BeforeExpensesPaid_CorrectAmount() {
        val contract = Contract(
            50f,
            0f
        )

        val expense = Expense(20_00, "test-expense-1")

        val simpleTrack1 = mock<Track> {
            on { calculateSalesBetween(any(), any()) } doReturn mapOf(ARTIST_1_NAME to 0)
        }

        val release = Release(CAT_NO, mapOf(LocalDate.now().minusDays(1) to 20_00), setOf(simpleTrack1), contract, mutableListOf(expense))

        release.applySale(ReleaseSale(CAT_NO, 20_00, LocalDate.now().minusDays(1)))
    }
}