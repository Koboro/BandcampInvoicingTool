package sales

import java.time.LocalDateTime

data class TrackSale(
    val releaseCatNo: String,
    val trackName: String,
    override val value: Int,
    override val dateTime: LocalDateTime
): SaleItem