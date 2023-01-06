import sales.ReleaseSale
import java.time.LocalDateTime

data class Release(
    val catNo: String,
    val tracks: Set<Track>,
    private val expenses: MutableList<Expense> = mutableListOf(),
    private val sales: MutableList<ReleaseSale> = mutableListOf()
) {


    fun applySale(sale: ReleaseSale) {
        sales.add(sale)
    }

    fun addExpense(expense: Expense) {
        expenses.add(expense)
    }

    fun calculatePayout(from: LocalDateTime): Map<String, Int> {
        val totalExpenses = expenses.map { it.value }.reduce { acc, value -> acc + value }

        val totalSales = sales
            .filter { it.dateTime.isAfter(from) }
            .map { it.value }
            .reduce { acc, value -> acc + value }

        val totalTrackSales = tracks.map { it.getTotalSalesFrom(from) }

        // Apply sales to expenses
        // Apply all track sales to expenses via artist split
        // Combine values and return
        TODO()
    }

    fun findTrack(trackName: String): Track? = tracks.find { it.name == trackName }
}