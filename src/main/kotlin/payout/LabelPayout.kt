package payout

/**
 * LabelPayout represents the value of a sale taken by the label in a [Payout].
 *
 * The amount taken by the label for a sale is determined in the [catalogue.Contract].
 *
 * @param relatedRecoupableItem the item associated with the contract, defining the artist/label split. This is not always the same
 * as the [ItemDetails.itemName], e.g., if [ItemDetails.itemType] is a track, this will be the release name.
 * @param amount the amount in minor units
 */
data class LabelPayout(
    val relatedRecoupableItem: String,
    val amount: Int
)