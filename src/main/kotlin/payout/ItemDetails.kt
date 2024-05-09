package payout

/**
 * Details relating to the item sold for a [Payout].
 *
 * @param itemName the item name, such as release category number, or single track title
 * @param itemType the item type

 */
data class ItemDetails(
    val itemName: String,
    val itemType: ItemType
)