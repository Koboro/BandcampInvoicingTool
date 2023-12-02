import sales.ReleaseSale
import sales.SaleItem
import sales.TrackSale
import java.time.LocalDate

data class Release(
    val catNo: String,
    /**
     * The date -> price map. If the price of the release is always the same then this only requires a single entry.
     * Initial entry should be on the date at which sales began (NOT ANY TIME BEFORE) - including pre-release. This information is used to calculate discography bundle payouts.
     */
    val prices: Map<LocalDate, Int>,
    private val tracks: Set<Track>,
    private val contract: Contract,
    private val expenses: MutableList<Expense> = mutableListOf(),
    /**
     * Can be set on construction to include any sales made that were external to Bandcamp to include these values in payout calculations.
     */
    private val sales: MutableList<Pair<Int, LocalDate>> = mutableListOf(),
    /**
     * The date at which sales stopped. May be kept null if sales were not stopped / still ongoing
     */
    private val salesStopDate: LocalDate? = null
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

    fun calculatePayout(from: LocalDate, to: LocalDate = LocalDate.now()): Map<String, Int> {

        val outstandingExpensesPerArtist = calculateOutstandingExpensesPerArtist(from)
        val sales = calculateSalesBetween(from, to)

        return sales.map {
            val payout = contract.calculateArtistPayout(it.value, outstandingExpensesPerArtist[it.key]
                ?: throw Exception("No expense listed for artist when trying to calculate artist payout"))
            it.key to payout
        }.toMap()
    }

    fun priceOnDate(date: LocalDate): Int {
        val priceDateIterator = prices.keys.asSequence()
            .sorted()
            .iterator()

        var priceDate = priceDateIterator.next()
        while (priceDateIterator.hasNext()) {
            val temp = priceDateIterator.next()
            if (temp.isBefore(date) || temp.isEqual(date))
                priceDate = temp
        }

        return prices[priceDate] ?: throw Exception("Date found was not in price data! (Should not occur)")
    }

    fun wasActivelySellingOn(date: LocalDate): Boolean {
        val salesStartDate = prices.keys.asSequence().min()
        return (salesStartDate == date || salesStartDate.isBefore(date)) && salesStopDate?.isAfter(date) ?: true
    }

    fun findTrack(trackName: String): Track = tracks.find { it.name == trackName }
        ?: throw IllegalArgumentException("Release $catNo does not contain track with name $trackName")

    private fun calculateSalesBetween(from: LocalDate, to: LocalDate): Map<String, Int> {
        val totalReleaseSales = sales
            .filter { it.isAfterOrDuring(from) }
            .filter { it.isBefore(to) }
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