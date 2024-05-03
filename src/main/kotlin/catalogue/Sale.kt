package catalogue

import java.time.LocalDate

internal data class Sale(
    val value: Int,
    val date: LocalDate,
    val saleType: SaleType
) {
    fun isAfterOrDuring(from: LocalDate): Boolean =
        this.date.isAfter(from) || this.date.isEqual(from)

    fun isBefore(until: LocalDate): Boolean =
        this.date.isBefore(until)
}

internal data class ArtistProportionedSale(
    // Null value indicates this is for a release
    val artist: String?,
    val itemName: String,
    val value: Int,
    val date: LocalDate
)

internal enum class SaleType {
    TRACK,
    RELEASE,
    BUNDLE
}