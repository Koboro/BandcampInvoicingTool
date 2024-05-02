import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContractTest {

    @Test
    fun testCalculateRemainingExpenses_SalesDoNotPayOffExpenses() {

        val contract = Contract(
            25f,
            0f
        )

        val result = contract.calculateOutstandingExpenses(200, 100)

        assertThat(result).isEqualTo(50)
    }

    @Test
    fun testCalculateRemainingExpenses_SalesMakeBreakEven_0Returned() {

        val contract = Contract(
            50f,
            0f
        )


        val result = contract.calculateOutstandingExpenses(200, 100)

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun testCalculateRemainingExpenses_SalesPayOffAllExpensesAndMore_0Returned() {

        val contract = Contract(
            50f,
            0f
        )


        val result = contract.calculateOutstandingExpenses(300, 100)

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun testCalculateArtistPayout_NoOutstandingExpenses_CorrectValueReturned() {

        val contract = Contract(
            50f,
            25f
        )

        val result = contract.calculateArtistPayout(20, 0)

        assertThat(result).isEqualTo(15)
    }

    @Test
    fun testCalculateArtistPayout_SalesDontReachBreakEven_CorrectValueReturned() {

        val contract = Contract(
            50f,
            25f
        )

        val result = contract.calculateArtistPayout(20, 20)

        assertThat(result).isEqualTo(10)
    }

    @Test
    fun testCalculateArtistPayout_SalesBreakEvenAndAbove_CorrectValueReturned() {

        val contract = Contract(
            50f,
            25f
        )

        val result = contract.calculateArtistPayout(20, 8)

        //16 to break even, split 50 to label
        //of the 4 remaining after break even, 1 to label and 3 to artist
        assertThat(result).isEqualTo(11)
    }

    @Test
    fun testCalculateArtistPayout_SalesBreakEvenAndAbove_CorrectValueReturned_2() {

        val contract = Contract(
            50f,
            0f
        )

        val result = contract.calculateArtistPayout(1_000, 250)

        //500 to break even, split 250 to label
        //of the 500 remaining after break even, all 500 to artist
        assertThat(result).isEqualTo(750)
    }
}