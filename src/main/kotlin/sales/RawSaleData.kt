package sales

import java.time.LocalDate

data class RawSaleData(
    // Null value indicates this is for a release
    val artist: String?,
    val itemName: String,
    val value: Int,
    val date: LocalDate
)