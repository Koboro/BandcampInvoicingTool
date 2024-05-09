import catalogue.SimpleContract
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import payout.ItemType

class SimpleContractTest {

    @Test
    fun testCalculateArtistPayout_NoOutstandingExpenses_CorrectValueReturned() {

        val contract = SimpleContract(
            50f,
            25f,
            0f,0f
        )

        val result = contract.calculateSaleShare(20, ItemType.TRACK, 0)

        assertThat(result.artistShare).isEqualTo(15)
    }

    @Test
    fun testCalculateArtistPayout_SalesDontReachBreakEven_CorrectValueReturned() {

        val contract = SimpleContract(
            50f,
            25f,
            0f,0f
        )

        val result = contract.calculateSaleShare(20, ItemType.TRACK, 20)

        assertThat(result.artistShare).isEqualTo(10)
    }

    @Test
    fun testCalculateArtistPayout_SalesBreakEvenAndAbove_CorrectValueReturned() {

        val contract = SimpleContract(
            50f,
            25f,
            0f,0f
        )

        val result = contract.calculateSaleShare(20, ItemType.TRACK, 8)

        //16 to break even, split 50 to label
        //of the 4 remaining after break even, 1 to label and 3 to artist
        assertThat(result.artistShare).isEqualTo(11)
    }

    @Test
    fun testCalculateArtistPayout_SalesBreakEvenAndAbove_CorrectValueReturned_2() {

        val contract = SimpleContract(
            50f,
            0f,
            0f,0f
        )

        val result = contract.calculateSaleShare(1_000, ItemType.TRACK, 250)

        //500 to break even, split 250 to label
        //of the 500 remaining after break even, all 500 to artist
        assertThat(result.artistShare).isEqualTo(750)
    }
}