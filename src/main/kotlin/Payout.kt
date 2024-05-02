import java.time.LocalDate

data class Payout(
    val artist: String,
    val itemName: String,
    val artistPayoutValue: Int,
    val labelRecoupValue: Int,
    val date: LocalDate
)