package sales

import java.time.LocalDate

data class BundleSale(
    val itemName: String,
    override val value: Int,
    override val dateTime: LocalDate
): SaleItem