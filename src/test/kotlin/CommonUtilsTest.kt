import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommonUtilsTest {


    @Test
    fun testCombineMapsWithSummedValues() {

        val map1 = mapOf(
            "Apple" to 1,
            "Banana" to 4,
            "Orange" to 2,
        )

        val map2 = mapOf(
            "Apple" to 2,
            "Banana" to 1,
            "Orange" to 3,
        )

        val map3 = mapOf(
            "Apple" to 4,
            "Banana" to 2,
            "Orange" to 1,
        )

        val result = listOf(map1, map2, map3).combineIntMapsWithSummedValues()

        assertThat(result).containsExactly(
            ("Apple" to 7).toEntry(),
            ("Banana" to 7).toEntry(),
            ("Orange" to 6).toEntry()
        )
    }

    @Test
    fun testMinusValues() {
        val map1 = mapOf(
            "Apple" to 5,
            "Banana" to 4,
            "Orange" to 2,
        )

        val map2 = mapOf(
            "Apple" to 2,
            "Banana" to 1,
            "Orange" to 3,
        )

        val result = map1.minusValues(map2)

        assertThat(result).containsExactly(
            ("Apple" to 3).toEntry(),
            ("Banana" to 3).toEntry(),
            ("Orange" to -1).toEntry()
        )
    }
}