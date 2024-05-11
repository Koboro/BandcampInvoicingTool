package sales

import java.time.LocalDate

class CategorisedPhysicalSale(
    override val packageName: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val date: LocalDate,
    override val catNo: String
): ReleaseSaleItem, PhysicalSale, BandcampSaleItem