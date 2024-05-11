package sales

import java.time.LocalDate

class ExternalReleaseSale(
    override val catNo: String,
    override val netValue: Int,
    override val grossValue: Int,
    override val date: LocalDate
): ReleaseSaleItem, SaleItem