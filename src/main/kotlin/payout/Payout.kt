package payout

import java.time.LocalDate

/**
 * Represents a single sale payout *per artist* - e.g. a sale on a track with two contributing artists will result in
 * two different [Payout] objects, one for each artist.
 *
 * @param artist the artist name
 * @param amount the payout value amount in minor units
 * @param date the date at which the payout occurs
 * @param itemDetails details relating to the item sold, e.g. track name
 * @param labelPayout details relating to the labels payout. If no payout is to be given to the label this may be null
 */
data class Payout(
    val artist: String,
    val amount: Int,
    val date: LocalDate,
    val itemDetails: ItemDetails,
    val labelPayout: LabelPayout?
){
    fun getTotalPayoutValue(): Int = amount + getLabelPayoutValue()

    fun getLabelPayoutValue(): Int = labelPayout?.amount ?: 0
}