import java.time.LocalDateTime

// Shorthand conversion from Pair to Map.Entry
fun <K,V> Pair<K,V>.toEntry() = object: Map.Entry<K,V> {
    override val key: K = first
    override val value: V = second
}

/**
 * Minuses the values from the [this] map against the [other] map. E.g.
 * [this] map {"Apple" -> 3}
 * [other] map {"Apple" -> 2}
 *
 * results in {"Apple" -> 1}
 *
 * @throws NoSuchElementException if key exists in [this] but not [other]
 */
fun Map<String, Int>.minusValues(other: Map<String, Int>): Map<String, Int> = this
    .map { it.key to it.value - other.getValue(it.key) }
    .toMap()

fun <T> Collection<Map<T, Int>>.combineIntMapsWithSummedValues(): Map<T, Int> {
    val totalTrackSales = mutableMapOf<T, Int>()

    this.forEach{ trackSalesMap -> trackSalesMap.forEach {
            totalTrackSales.merge(it.key, it.value, Int::plus)
        } }

    return totalTrackSales
}

fun <T> Collection<Map<T, Float>>.combineFloatMapsWithSummedValues(): Map<T, Float> {
    val totalTrackSales = mutableMapOf<T, Float>()

    this.forEach{ trackSalesMap -> trackSalesMap.forEach {
        totalTrackSales.merge(it.key, it.value, Float::plus)
    } }

    return totalTrackSales
}

fun Pair<Int, LocalDateTime>.isAfterOrDuring(from: LocalDateTime): Boolean =
    this.second.isAfter(from) || this.second.isEqual(from)