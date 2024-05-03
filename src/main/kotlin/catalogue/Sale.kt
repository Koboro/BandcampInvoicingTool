package catalogue

import java.time.LocalDate

internal data class Sale(
    val value: Int,
    val date: LocalDate,
    val saleType: SaleType
)

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