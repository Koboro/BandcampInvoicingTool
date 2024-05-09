package payout

/**
 * Represents a single sale payout *per artist* - e.g. a sale on a track with two contributing artists will result in
 * two different [Payout] objects, one for each artist.
 *
 * @param artist the artist name
 * @param amount the payout value amount in minor units
 * @param itemDetails details relating to the item sold, e.g. track name
 * @param saleInformation details relating to the Bandcamp sale
 * @param labelPayout details relating to the labels payout. If no payout is to be given to the label this may be null
 */
data class Payout(
    val artist: String,
    val amount: Int,
    val itemDetails: ItemDetails,
    val saleInformation: SaleInformation,
    val labelPayout: LabelPayout?
){
    fun getTotalPayoutValue(): Int = amount + getLabelPayoutValue()

    fun getLabelPayoutValue(): Int = labelPayout?.amount ?: 0
}