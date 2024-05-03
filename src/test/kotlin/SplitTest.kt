import catalogue.Split
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SplitTest {

    companion object {
        private const val ARTIST_1_NAME = "test-artist-name-1"
        private const val ARTIST_2_NAME = "test-artist-name-2"
        private const val ARTIST_3_NAME = "test-artist-name-3"
    }

    @Test
    fun testCalculateShares() {
        val split = Split.evenSplit(ARTIST_1_NAME, ARTIST_2_NAME, ARTIST_3_NAME)

        val result = split.calculateShares(10)

        assertThat(result)
            .hasSize(3)
            .containsOnlyKeys(ARTIST_1_NAME, ARTIST_2_NAME, ARTIST_3_NAME)
            .containsValues(3, 3, 3)
    }
}