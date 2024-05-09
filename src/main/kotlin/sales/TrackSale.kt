package sales

import java.time.LocalDate

data class TrackSale(
    override val catNo: String,
    val trackName: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val dateTime: LocalDate
): CategorisedSaleItem