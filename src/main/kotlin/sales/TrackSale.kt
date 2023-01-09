package sales

import java.time.LocalDate

data class TrackSale(
    override val catNo: String,
    val trackName: String,
    override val value: Int,
    override val dateTime: LocalDate
): SaleItem