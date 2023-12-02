import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

class Split private constructor(splitEntries: Set<Pair<String, Float>>) : HashMap<String, Float>() {

    companion object {

        fun singleSplit(artist: String) = Split(setOf(artist to 100f))

        fun evenSplit(vararg artists: String): Split {
            val percentageEach = 100f / artists.size

            val entries = artists.map { it to percentageEach }.toSet()
            return Split(entries)
        }

        fun customSplit(vararg splits: Pair<String, Float>) = Split(splits.toSet())
    }

    init {
        val total = splitEntries
            .map { it.second }
            .sum()
            .toBigDecimal()
            // Float calculations can result in slightly inconsistent / non-round numbers. For the purposes of this check any extras past 2 decimal places can be considered acceptable inconsistency and can be removed and ignored
            .setScale(2, RoundingMode.HALF_EVEN)

        if (total > BigDecimal(100) || total < BigDecimal(99)) {
            throw Exception("Cannot create split. Total split values not equal to 100")
        }

        putAll(splitEntries)
    }

    /**
     * Calculates the shares to the nearest integer value based on the split values
     *
     * TODO rounding
     */
    fun calculateShares(value: Int): Map<String, Int> = map { it.key to (value * (it.value / 100)).roundToInt() }
        .toMap()
}