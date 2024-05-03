package catalogue

import java.time.LocalDate

/**
 * Generates a split representing the distribution split for each album when a bundle is bought.
 *
 * Accounts for whether releases were available at the time of the bundle purchase, and the price
 * of the release at the time of the bundle purchase.
 */
fun Collection<Release>.getBundleSplit(date: LocalDate): Split {
    val pricePerAlbum = asSequence()
        .filter { it.wasActivelySellingOn(date) }
        .associate { it.catNo to it.priceOnDate(date) }

    val total = pricePerAlbum.values.sum()
    val part: Float = 100f / total

    val pairs = pricePerAlbum.map { it.key to it.value * part }
    return Split.customSplit(*pairs.toTypedArray())
}