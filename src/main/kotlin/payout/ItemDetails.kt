package payout

import java.text.DecimalFormat

/**
 * Details relating to the item sold for a [Payout].
 *
 * @param itemName the item name, such as release category number, or single track title
 * @param itemType the item type
 * @param share the percentage of the total item sale that ws received for this [Payout]. e.g., if a track with two
 * contributing artists sells, and they split the sales equally, this value would be `50`.
 */
data class ItemDetails(
    val itemName: String,
    val itemType: ItemType,
    val share: Float
) {
    override fun toString(): String {
        val builder = StringBuilder( "[$itemType] $itemName")

        if (share < 100f) {
            val decimalFormat = DecimalFormat("###.#")
            builder.append( " (${decimalFormat.format(share.toBigDecimal())}% share)")
        }

        return builder.toString()
    }
}