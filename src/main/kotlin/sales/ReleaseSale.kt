package sales

import java.time.LocalDate

data class ReleaseSale(
    override val catNo: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val dateTime: LocalDate
): CategorisedSaleItem