package sales

import java.time.LocalDate

data class DigitalDiscographySale(
    val itemName: String,
    override val value: Int,
    override val dateTime: LocalDate
): SaleItem