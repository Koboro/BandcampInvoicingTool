package sales

import java.time.LocalDate

class PhysicalSale(
    val packageName: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val dateTime: LocalDate,
    override val catNo: String
): CategorisedSaleItem