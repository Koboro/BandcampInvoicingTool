package sales

import java.time.LocalDate

data class DigitalReleaseSale(
    override val catNo: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val date: LocalDate
): ReleaseSaleItem, BandcampSaleItem