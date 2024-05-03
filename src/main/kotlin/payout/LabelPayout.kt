package payout

/**
 * LabelPayout represents the value of a sale taken by the label in a [Payout].
 *
 * The amount taken by a sale is determined in the [catalogue.Contract].
 * If the [catalogue.Contract.labelPercentageBeforeBreakEven] and [catalogue.Contract.labelPercentageAfterBreakEven] are different then
 * the amount taken by the label will depend on if there are any outstanding expenses associated with the release.
 *
 * @param itemName the item associated with the contract, defining the artist/label split. This is not always the same
 * as the [ItemDetails.itemName], e.g., if [ItemDetails.itemType] is a track, this will be the release name.
 * @param amount the amount in minor units
 */
data class LabelPayout(
    val itemName: String,
    val amount: Int
)