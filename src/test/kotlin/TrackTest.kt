import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TrackTest {

    companion object {
        private const val TRACK_NAME = "test-track-name"

        private const val ARTIST_1_NAME = "test-artist-name-1"
        private const val ARTIST_2_NAME = "test-artist-name-2"
        private const val ARTIST_3_NAME = "test-artist-name-3"
    }

    @Test
    fun testCalculateTotalSalesFrom_HappyPathWithSingleArtist_EachArtistGetsHalfOfTotal() {

        val track = Track(TRACK_NAME, Split.singleSplit(ARTIST_1_NAME))

        val saleValue = 10
        val timeOfSale = LocalDate.now()

        track.applySale(saleValue, timeOfSale)


        val sales: Map<String, Int> = track.calculateSalesFrom(timeOfSale.minusDays(1))

        assertThat(sales).containsOnly(
            (ARTIST_1_NAME to saleValue).toEntry()
        )
    }

    @Test
    fun testCalculateTotalSalesFrom_HappyPathWithEvenSplitBetweenTwoArtists_EachArtistGetsHalfOfTotal() {

        val track = Track(TRACK_NAME, Split.evenSplit(ARTIST_1_NAME, ARTIST_2_NAME))

        val timeOfSale = LocalDate.now()

        track.applySale(10, timeOfSale)


        val sales: Map<String, Int> = track.calculateSalesFrom(timeOfSale.minusDays(1))


        assertThat(sales).containsOnly(
            (ARTIST_1_NAME to 5).toEntry(), 
            (ARTIST_2_NAME to 5).toEntry()
        )
    }

    @Test
    fun testCalculateTotalSalesFrom_HappyPathWithEvenSplitBetweenThreeArtists_EachArtistGetsAThirdRoundingToNearestInteger() {

        val track = Track(TRACK_NAME, Split.evenSplit(ARTIST_1_NAME, ARTIST_2_NAME, ARTIST_3_NAME))

        val timeOfSale = LocalDate.now()

        track.applySale(20, timeOfSale)


        val sales: Map<String, Int> = track.calculateSalesFrom(timeOfSale.minusDays(1))


        assertThat(sales).containsOnly(
            (ARTIST_1_NAME to 7).toEntry(),
            (ARTIST_2_NAME to 7).toEntry(),
            (ARTIST_3_NAME to 7).toEntry()
        )
    }

    @Test
    fun testCalculateTotalSalesFrom_FromIsSameAsTimeOfSale_SaleIsIncludedInTotal() {

        val track = Track(TRACK_NAME, Split.singleSplit(ARTIST_1_NAME))

        val saleValue = 10
        val timeOfSale = LocalDate.now()

        track.applySale(saleValue, timeOfSale)


        val sales: Map<String, Int> = track.calculateSalesFrom(timeOfSale)


        assertThat(sales).containsOnly((ARTIST_1_NAME to saleValue).toEntry())
    }

    @Test
    fun testCalculateTotalSalesFrom_FromIsLaterThanSaleDate_SaleIsExcludedFromTotal() {

        val track = Track(TRACK_NAME, Split.singleSplit(ARTIST_1_NAME))

        val timeOfSale = LocalDate.now()

        track.applySale(10, timeOfSale)


        val sales: Map<String, Int> = track.calculateSalesFrom(timeOfSale.plusDays(1))


        assertThat(sales).containsOnly((ARTIST_1_NAME to 0).toEntry())
    }
}

