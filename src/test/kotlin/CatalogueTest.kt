import catalogue.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sales.DigitalReleaseSale
import sales.TrackSale
import java.time.LocalDate

class CatalogueTest {

    companion object {
        private const val CAT_NO = "test-cat-no"

        private const val ARTIST_1_NAME = "test-artist-name-1"
        private const val ARTIST_2_NAME = "test-artist-name-2"
    }

    private lateinit var catalogue: Catalogue

    @BeforeEach
    fun setUp() {
        catalogue = Catalogue()
    }

    @Test
    fun testCalculatePayout_HappyPath_BeforeExpensesPaid_CorrectAmount() {
        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val expense = Expense(20_00, "test-expense-1")

        val simpleTrack1 = Track("track-1", Split.singleSplit(ARTIST_1_NAME))

        val release = Release(CAT_NO, mapOf(LocalDate.now().minusDays(1) to 20_00), setOf(simpleTrack1), contract, mutableListOf(expense))

        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 20_00, 20_00, LocalDate.now().minusDays(1)))
    }

    @Test
    fun testCalculatePayout_HappyPath_PayoutCalculatedCorrectly() {
        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val expense = Expense(10_00, "test-expense-1")

        val simpleTrack1 = Track("track-1", Split.singleSplit(ARTIST_1_NAME))

        val release = Release(
            catNo =  CAT_NO,
            digitalPriceMap = mapOf(LocalDate.now().minusDays(2) to 5_00),
            tracks = setOf(simpleTrack1),
            contract = contract,
            expenses = mutableListOf(expense)
        )

        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 5_00, 5_00, LocalDate.now().minusDays(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 5_00, 5_00, LocalDate.now().minusDays(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 5_00, 5_00, LocalDate.now().minusDays(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 10_00, 10_00, LocalDate.now().minusDays(1)))

        val payouts = Catalogue(mutableListOf(release)).calculatePayouts(LocalDate.now().minusDays(3), LocalDate.now())
        assertThat(payouts).hasSize(4)
        assertThat(payouts.sumOf { it.getLabelPayoutValue() }).isEqualTo(expense.value)

        assertThat(payouts[0].amount).isEqualTo(250)
        assertThat(payouts[0].getLabelPayoutValue()).isEqualTo(250)
        assertThat(payouts[1].amount).isEqualTo(250)
        assertThat(payouts[1].getLabelPayoutValue()).isEqualTo(250)
        assertThat(payouts[2].amount).isEqualTo(250)
        assertThat(payouts[2].getLabelPayoutValue()).isEqualTo(250)
        assertThat(payouts[3].amount).isEqualTo(750)
        assertThat(payouts[3].getLabelPayoutValue()).isEqualTo(250)
    }

    @Test
    fun testCalculatePayout_Complex1_PayoutCalculatedCorrectly() {
        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val expense = Expense(400, "test-expense-1")

        val soloTrackName = "track-1"
        val soloTrack = Track(soloTrackName, Split.singleSplit(ARTIST_1_NAME))
        val collaborationTrack = Track("track-2", Split.evenSplit(ARTIST_1_NAME, ARTIST_2_NAME))

        val release = Release(
            catNo =  CAT_NO,
            digitalPriceMap = mapOf(LocalDate.now().minusDays(2) to 400),
            tracks = setOf(soloTrack, collaborationTrack),
            contract=  contract,
            expenses = mutableListOf(expense)
        )

        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, LocalDate.now().minusDays(1)))
        release.addSale(TrackSale(CAT_NO,"track-1", "n/a", 200, 200, LocalDate.now().minusDays(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, LocalDate.now().minusDays(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, LocalDate.now().minusDays(1)))

        val payouts = Catalogue(mutableListOf(release)).calculatePayouts(LocalDate.now().minusDays(3), LocalDate.now())

        assertThat(payouts.sumOf { it.getLabelPayoutValue() }).isEqualTo(400)
        assertThat(payouts.filter { it.artist == ARTIST_1_NAME }.sumOf { it.amount }).isEqualTo(800)
        assertThat(payouts.filter { it.artist == ARTIST_2_NAME }.sumOf { it.amount }).isEqualTo(200)
    }

    @Test
    fun testCalculatePayout_ComplexFromASpecificDate_PayoutCalculatedCorrectly() {
        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val expense = Expense(400, "test-expense-1")

        val soloTrackName = "track-1"
        val soloTrack = Track(soloTrackName, Split.singleSplit(ARTIST_1_NAME))
        val collaborationTrack = Track("track-2", Split.evenSplit(ARTIST_1_NAME, ARTIST_2_NAME))

        val release = Release(
            catNo =  CAT_NO,
            digitalPriceMap = mapOf(LocalDate.now().minusDays(2) to 400),
            tracks = setOf(soloTrack, collaborationTrack),
            contract=  contract,
            expenses = mutableListOf(expense)
        )

        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, LocalDate.now().minusDays(2)))
        release.addSale(TrackSale(CAT_NO,"track-1", "n/a", 200, 200, LocalDate.now().minusDays(2)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, LocalDate.now().minusDays(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, LocalDate.now().minusDays(1)))

        val payouts = Catalogue(mutableListOf(release)).calculatePayouts(LocalDate.now().minusDays(1), LocalDate.now())
        assertThat(payouts.filter { it.artist == ARTIST_1_NAME }.sumOf { it.amount }).isEqualTo(550)
        assertThat(payouts.filter { it.artist == ARTIST_2_NAME }.sumOf { it.amount }).isEqualTo(150)
    }

    @Test
    fun testCalculatePayout_DelayedExpenseIncluded_PayoutCalculatedCorrectly() {
        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val expense = Expense(100, "delayed-expense", day(2))

        val simpleTrack1 = Track("track-1", Split.singleSplit(ARTIST_1_NAME))

        val release = Release(
            catNo =  CAT_NO,
            digitalPriceMap = mapOf(day(0) to 200),
            tracks = setOf(simpleTrack1),
            contract = contract,
            expenses = mutableListOf(expense)
        )

        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, day(3)))

        val payouts = Catalogue(mutableListOf(release)).calculatePayouts(day(1), LocalDate.now())
        assertThat(payouts).hasSize(1)
        assertThat(payouts.sumOf { it.amount }).isEqualTo(300)
    }

    @Test
    fun testCalculatePayout_DelayedExpenseAfterFirstSaleIncluded_PayoutCalculatedCorrectly() {
        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val expense = Expense(100, "delayed-expense", day(2))

        val simpleTrack1 = Track("track-1", Split.singleSplit(ARTIST_1_NAME))

        val release = Release(
            catNo =  CAT_NO,
            digitalPriceMap = mapOf(day(0) to 200),
            tracks = setOf(simpleTrack1),
            contract = contract,
            expenses = mutableListOf(expense)
        )

        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, day(1)))
        release.addSale(DigitalReleaseSale(CAT_NO, "n/a", 400, 400, day(3)))

        val payouts = Catalogue(mutableListOf(release)).calculatePayouts(day(3), LocalDate.now())
        assertThat(payouts).hasSize(1)
        assertThat(payouts.sumOf { it.amount }).isEqualTo(300)
    }

    private fun day(day: Long): LocalDate {
        return LocalDate.now().minusDays(100).plusDays(day)
    }

    private fun getComplexCompilationRelease(): Release {

        val track1 = Track("Rumble", Split.customSplit("Skrillex" to 40f, "Fred Again..." to 60f))
        val track2 = Track("Big Ol' Collab Track", Split.evenSplit("Kendrick", "J Cole", "Schoolboy Q"))
        val track3 = Track("Mayonnaise", Split.singleSplit("Bob Dylan"))

        val expenses: MutableList<Expense> = mutableListOf(
            Expense(60_00, "Track Mastering"),
            Expense(100_00, "Artwork")
        )

        return Release(
            catNo = "COMPLEX001",
            tracks = setOf(track1, track2, track3),
            digitalPriceMap = mapOf(LocalDate.now().minusDays(1) to 10_00),
            contract = SimpleContract(
            0f,
            0f,
                0f,
                0f),
            expenses = expenses

        )
    }
}