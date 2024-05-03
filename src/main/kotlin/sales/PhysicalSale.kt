package sales

import java.time.LocalDate

class PhysicalSale(
    val packageName: String,
    override val value: Int,
    override val dateTime: LocalDate,
    override val catNo: String
): CategorisedSaleItem