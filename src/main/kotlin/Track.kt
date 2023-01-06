import sales.TrackSale
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class Track(
    val name: String,
    private val split: Split,
    private val sales: MutableList<TrackSale> = mutableListOf()
) {
    fun applySale(sale: TrackSale) {
        sales.add(sale)
    }

    fun getTotalSalesFrom(from: LocalDateTime): Map<String, Int> {

        val totalSales = sales
            .filter { it.dateTime.isAfter(from) }
            .map { it.value }
            .reduce { acc, value -> acc + value }

        // Total sales * artist percentage split mapped
        return split.map { it.key to totalSales * (it.value / 100).roundToInt() }.toMap() // TODO: Rounding
    }
}