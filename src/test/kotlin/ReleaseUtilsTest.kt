import catalogue.Contract
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month



class ReleaseUtilsTest {

    companion object {
        private const val RELEASE_1_CATNO = "TEST001"
        private const val RELEASE_2_CATNO = "TEST002"
        private const val RELEASE_3_CATNO = "TEST003"
    }

    @Test
    fun testGetBundleSplit_OnlyFirstReleaseAvailable_SplitOnlyIncludesFirstRelease() {

        val split = generateTestReleases()
            .getBundleSplit(LocalDate.of(2001, Month.JANUARY, 1))

        assertThat(split)
            .containsOnlyKeys(RELEASE_1_CATNO)
            .containsValue(100f)
    }

    @Test
    fun testGetBundleSplit_OnlyFirstTwoReleaseAvailable_SplitHasCorrectValues() {

        val split = generateTestReleases()
            .getBundleSplit(LocalDate.of(2005, Month.JANUARY, 1))
            // To integer for the purposes of assertions
            .mapValues { it.value.toInt() }

        assertThat(split)
            .containsOnly(
                entry(RELEASE_1_CATNO, 33),
                entry(RELEASE_2_CATNO, 66),
            )
    }

    @Test
    fun testGetBundleSplit_AllReleaseAvailable_SplitHasCorrectValues() {

        val split = generateTestReleases()
            .getBundleSplit(LocalDate.of(2007, Month.JANUARY, 1))
            // To integer for the purposes of assertions
            .mapValues { it.value.toInt() }

        assertThat(split)
            .containsOnly(
                entry(RELEASE_1_CATNO, 25),
                entry(RELEASE_2_CATNO, 50),
                entry(RELEASE_3_CATNO, 25)
            )
    }

    @Test
    fun testGetBundleSplit_AllReleaseAvailableButRelease1HasChangesPrice_SplitHasCorrectValues() {

        val split = generateTestReleases()
            .getBundleSplit(LocalDate.of(2010, Month.JANUARY, 1))
            // To integer for the purposes of assertions
            .mapValues { it.value.toInt() }

        assertThat(split)
            .containsOnly(
                entry(RELEASE_1_CATNO, 14),
                entry(RELEASE_2_CATNO, 57),
                entry(RELEASE_3_CATNO, 28)
            )
    }

    @Test
    fun testGetBundleSplit_AllReleaseAvailableButAllReleasesHaveChangedPrice_SplitHasCorrectValues() {

        val split = generateTestReleases()
            .getBundleSplit(LocalDate.of(2017, Month.JANUARY, 1))
            // To integer for the purposes of assertions
            .mapValues { it.value.toInt() }

        assertThat(split)
            .containsOnly(
                entry(RELEASE_1_CATNO, 14),
                entry(RELEASE_2_CATNO, 28),
                entry(RELEASE_3_CATNO, 57)
            )
    }

    @Test
    fun testGetBundleSplit_Release1HasStoppedSelling_SplitHasCorrectValues() {

        val split = generateTestReleases()
            .getBundleSplit(LocalDate.of(2020, Month.JANUARY, 1))
            // To integer for the purposes of assertions
            .mapValues { it.value.toInt() }

        assertThat(split)
            .containsOnly(
                entry(RELEASE_2_CATNO, 33),
                entry(RELEASE_3_CATNO, 66)
            )
    }

    fun generateTestReleases() = setOf(
        Release(
            catNo = RELEASE_1_CATNO,
            prices = mapOf(
                // Originally £10 on release in 2000
                Pair(LocalDate.of(2000, Month.JANUARY, 1), 10_00),
                // Went down to £5 in 2010
                Pair(LocalDate.of(2010, Month.JANUARY, 1), 5_00)
            ),
            tracks = setOf(),
            contract = Contract(0f, 0f),
            salesStopDate = LocalDate.of(2020, Month.JANUARY, 1)
        ),

        Release(
            catNo = RELEASE_2_CATNO,
            prices = mapOf(
                // Originally £20 on release in 2005
                Pair(LocalDate.of(2005, Month.JANUARY, 1), 20_00),
                // Went down to £10 in 2015
                Pair(LocalDate.of(2015, Month.JANUARY, 1), 10_00)
            ),
            tracks = setOf(),
            contract = Contract(0f, 0f)
        ),

        Release(
            catNo = RELEASE_3_CATNO,
            prices = mapOf(
                // Originally £10 on release in 2007
                Pair(LocalDate.of(2007, Month.JANUARY, 1), 10_00),
                // Went up to £20 in 2017
                Pair(LocalDate.of(2017, Month.JANUARY, 1), 20_00)
            ),
            tracks = setOf(),
            contract = Contract(0f, 0f)
        )
    )
}