import sales.RawSaleData
import java.time.LocalDate

data class Track(val name: String, val split: Split) {

    /**
     * Sale values tracked by date where
     * - first -> Integer value representing minor currency value
     * - second -> Time of sale
     */
    val sales: MutableList<Pair<Int, LocalDate>> = mutableListOf()

    fun applySale(saleValue: Int, timeOfSale: LocalDate) {
        sales.add(saleValue to timeOfSale)
    }

    fun getSaleSharesMappedByContributingArtist(): List<RawSaleData> {

        return sales.flatMap { sale ->
            val artistToSaleValue = split.calculateShares(sale.first)

            artistToSaleValue.map {
                RawSaleData(it.key, name, it.value, sale.second)
            }.asSequence()
        }
    }

    fun calculateSalesUntil(until: LocalDate): Map<String, Int> {
        val totalSales = sales
            .filter { it.second.isBefore(until) }
            .sumOf { it.first }

        // Total sales * artist percentage split mapped
        return split.calculateShares(totalSales)
    }

    fun calculateSalesBetween(from: LocalDate, to: LocalDate): Map<String, Int> {

        val totalSales = sales
            .filter { it.isAfterOrDuring(from) }
            .filter { it.isBefore(to) }
            .sumOf { it.first }

        // Total sales * artist percentage split mapped
        return split.calculateShares(totalSales)
    }
}