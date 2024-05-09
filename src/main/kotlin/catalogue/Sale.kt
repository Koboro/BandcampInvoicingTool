package catalogue

import java.time.LocalDate

internal data class Sale(
    val bandcampTransactionId: String,
    val netValue: Int,
    val grossValue: Int,
    val date: LocalDate,
    val saleType: SaleType
)

internal data class ArtistProportionedSale(
    // Null value indicates this is for a release
    val artist: String?,
    val itemName: String,
    val bandcampTransactionId: String,
    val netValue: Int,
    val grossValue: Int,
    val date: LocalDate,
    val saleType: SaleType
)

internal enum class SaleType {
    TRACK,
    RELEASE,
    PHYSICAL,
    DIGITAL_DISCOGRAPHY
}