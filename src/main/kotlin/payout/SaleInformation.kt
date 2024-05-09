package payout

import java.time.LocalDate

/**
 * @param splitProportion the proportion of the total sale value that was received for this [Payout] as a
 * percentage. e.g., if a track with two contributing artists sells, and they split the sales equally, this value would
 * be `50`
 */
data class SaleInformation(
    val bandcampTransactionId: String,
    val date: LocalDate,
    val netValue: Int,
    val grossValue: Int,
    val splitProportion: Float
)