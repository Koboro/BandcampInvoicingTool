import sales.ReleaseSale
import sales.SaleItem
import sales.TrackSale
import java.time.LocalDate

data class Release(
    val catNo: String,
    private val tracks: Set<Track>,
    private val contract: Contract,
    private val expenses: MutableList<Expense> = mutableListOf(),
    private val sales: MutableList<Pair<Int, LocalDate>> = mutableListOf()
) {

    fun applySale(saleItem: SaleItem) {
        when (saleItem) {
            is ReleaseSale -> {
                sales.add(saleItem.value to saleItem.dateTime)
            }
            is TrackSale -> {
                try {
                    findTrack(saleItem.trackName)
                } catch (e: Exception) {
                    throw Exception("Could not apply track sale.", e)
                }.applySale(saleItem.value, saleItem.dateTime)
            }
            else -> throw Exception("Unrecognised sale item.")
        }
    }

    fun addExpense(expense: Expense) {
        expenses.add(expense)
    }

    fun calculatePayout(from: LocalDate): Map<String, Int> {

        val outstandingExpensesPerArtist = calculateOutstandingExpensesPerArtist(from)
        val sales = calculateSalesFrom(from)

        return sales.map {
            val payout = contract.calculateArtistPayout(it.value, outstandingExpensesPerArtist[it.key]
                ?: throw Exception("No expense listed for artist when trying to calculate artist payout"))
            it.key to payout
        }.toMap()
    }

    fun findTrack(trackName: String): Track = tracks.find { it.name == trackName }
        ?: throw IllegalArgumentException("Release $catNo does not contain track with name $trackName")

    private fun calculateSalesFrom(from: LocalDate): Map<String, Int> {
        val totalReleaseSales = sales
            .filter { it.isAfterOrDuring(from) }
            .sumOf { it.first }

        val releaseShares = calculateReleaseSplit()
            .calculateShares(totalReleaseSales)

        val trackShares = calculateTrackSalesFrom(from)

        return listOf(releaseShares, trackShares).combineIntMapsWithSummedValues()
    }

    private fun getTotalExpenses() = expenses.sumOf { it.value }

    private fun calculateReleaseSplit(): Split {

        val totalTracks = tracks.size

        val splitMap = tracks.map { it.split }
            .combineFloatMapsWithSummedValues()
            .map { it.key to (it.value / totalTracks) }
            .toTypedArray()

        return Split.customSplit(splits = splitMap)
    }

    private fun calculateOutstandingExpensesPerArtist(until: LocalDate): Map<String, Int> {
        val releaseSalesValue = sales
            .filter { it.second.isBefore(until) }
            .sumOf { it.first }

        val releaseSalesPerArtist = calculateReleaseSplit().calculateShares(releaseSalesValue)
        val trackSalesPerArtist = calculateTrackSalesUntil(until)

        val totalSalesPerArtist = listOf(releaseSalesPerArtist, trackSalesPerArtist).combineIntMapsWithSummedValues()
        val expensesPerArtist = calculateExpensesPerArtist()

        return totalSalesPerArtist.map {
            val outstandingExpenses = contract.calculateOutstandingExpenses(it.value, expensesPerArtist[it.key]
                ?: throw Exception("No expense listed for artist when trying to calculate outstanding expenses."))
            it.key to outstandingExpenses
        }.toMap()
    }

    private fun calculateExpensesPerArtist(): Map<String, Int> = calculateReleaseSplit()
        .calculateShares(getTotalExpenses())

    private fun calculateTrackSalesUntil(until: LocalDate): Map<String, Int> = tracks
        .map { it.calculateSalesUntil(until) }
        .combineIntMapsWithSummedValues()

    private fun calculateTrackSalesFrom(from: LocalDate): Map<String, Int> = tracks
            .map { it.calculateSalesFrom(from) }
            .combineIntMapsWithSummedValues()
}