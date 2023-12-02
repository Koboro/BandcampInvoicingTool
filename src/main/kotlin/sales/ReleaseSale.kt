package sales

import java.time.LocalDate

data class ReleaseSale(
    override val catNo: String,
    override val value: Int,
    override val dateTime: LocalDate
): CategorisedSaleItem