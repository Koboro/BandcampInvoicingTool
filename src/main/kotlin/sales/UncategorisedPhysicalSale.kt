package sales

import java.time.LocalDate

class UncategorisedPhysicalSale(
    override val packageName: String,
    override val itemName: String,
    override val bandcampTransactionId: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val date: LocalDate
): UncategorisedSaleItem, PhysicalSale, BandcampSaleItem, SaleItem