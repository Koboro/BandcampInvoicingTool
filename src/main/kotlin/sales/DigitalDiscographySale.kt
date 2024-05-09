package sales

import java.time.LocalDate

data class DigitalDiscographySale(
    val itemName: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val dateTime: LocalDate
): SaleItem