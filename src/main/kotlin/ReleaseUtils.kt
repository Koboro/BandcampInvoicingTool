import java.time.LocalDate

/**
 * Generates a split representing the distribution split for each album when a bundle is bought.
 */
fun Set<Release>.getBundleSplit(date: LocalDate): Split {
    val pricePerAlbum = asSequence()
        .map { Pair(it.catNo, it.priceOnDate(date)) }
        .toMap()

    val total = pricePerAlbum.values.sum()
    val part: Float = 100f / total

    val pairs = pricePerAlbum.map { Pair(it.key, it.value * part) }
    return Split.customSplit(*pairs.toTypedArray())
}