import java.time.LocalDateTime

data class Track(val name: String, val split: Split) {

    /**
     * Sale values tracked by date where
     * - first -> Integer value representing minor currency value
     * - second -> Time of sale
     */
    private val sales: MutableList<Pair<Int, LocalDateTime>> = mutableListOf()

    fun applySale(saleValue: Int, timeOfSale: LocalDateTime) {
        sales.add(saleValue to timeOfSale)
    }

    fun getAllTimeSales(): Map<String, Int> {
        val totalAllTimeSales = sales.sumOf { it.first }

        // Total sales * artist percentage split mapped
        return split.calculateShares(totalAllTimeSales)
    }

    fun calculateSalesUntil(until: LocalDateTime): Map<String, Int> {
        val totalSales = sales
            .filter { it.second.isBefore(until) }
            .sumOf { it.first }

        // Total sales * artist percentage split mapped
        return split.calculateShares(totalSales)
    }

    fun calculateSalesFrom(from: LocalDateTime): Map<String, Int> {

        val totalSales = sales
            .filter { it.isAfterOrDuring(from) }
            .sumOf { it.first }

        // Total sales * artist percentage split mapped
        return split.calculateShares(totalSales)
    }
}