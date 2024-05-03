import sales.RawSaleData
import sales.ReleaseSale
import sales.SaleItem
import sales.TrackSale
import java.text.DecimalFormat
import java.time.LocalDate
import kotlin.text.StringBuilder

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

    fun calculatePayout(from: LocalDate, to: LocalDate = LocalDate.now()): List<Payout> {

        return generateTotalPayout()
            .filter { it.date.isAfter(from) || it.date.isEqual(from) }
            .filter { it.date.isBefore(to) }
    }

    private fun generateTotalPayout(): List<Payout> {

        val trackSales: List<RawSaleData> = tracks.flatMap { it.getSaleSharesMappedByContributingArtist() }
        val releaseSales: List<RawSaleData> = sales.map { RawSaleData(null, catNo, it.first, it.second) }
        val allSalesSortedByDate: List<RawSaleData> = (trackSales + releaseSales)
            .sortedBy { it.date }
        // The cursor date tracks when the last expense values were loaded. For the first cursor we take the very first sale date
        var cursorDate = allSalesSortedByDate.getOrNull(0)?.date
            // If no sales exist yet just return here
            ?: return emptyList()

        val totalDefaultExpenseValue = expenses.getExpenseValueUpTo(cursorDate)
        val releaseSplit = calculateReleaseSplit()
        var outstandingExpensesPerArtist = releaseSplit.calculateShares(totalDefaultExpenseValue).toMutableMap()

        return allSalesSortedByDate.flatMap {rawSaleData ->

            val valueOfSale = rawSaleData.value
            val dateOfSale = rawSaleData.date

            // If the date becomes later than the previous update of expense values then load the expense values that have occurred up to this new date
            if (cursorDate.isBefore(dateOfSale)) {
                val newExpenses = expenses.getTotalExpenseValueBetweenDates(cursorDate, dateOfSale)
                outstandingExpensesPerArtist = outstandingExpensesPerArtist.addValues(releaseSplit.calculateShares(newExpenses)).toMutableMap()

                // update cursor to this date
                cursorDate = dateOfSale
            }

            if (rawSaleData.artist == null) {
                return@flatMap releaseSplit.calculateShares(valueOfSale).map {
                    val itemName = generateReleaseItemNateForPayout(it.key, rawSaleData.itemName, releaseSplit)
                    calculateArtistPayout(it.key, itemName, it.value, dateOfSale, outstandingExpensesPerArtist)
                }

            } else {
                return@flatMap listOf(calculateArtistPayout(rawSaleData.artist, "[Track] ${rawSaleData.itemName}", valueOfSale, dateOfSale, outstandingExpensesPerArtist))
            }
        }
    }

    private fun generateReleaseItemNateForPayout(artistName: String, itemName: String, releaseSplit: Split): String {
        val builder = StringBuilder( "[Release] $itemName")

        val share = releaseSplit[artistName] ?: throw Exception("No share recognised for artist \"$artistName\" for release $catNo")
        if (share < 100f) {
            val decimalFormat = DecimalFormat("###.#")
           builder.append( " (${decimalFormat.format(share.toBigDecimal())}% share of total sale)")
        }

        return builder.toString()
    }

    private fun calculateArtistPayout(artistName: String, itemName: String, valueOfSale: Int, date: LocalDate, outstandingExpensesPerArtist: MutableMap<String, Int>): Payout {
        val artistOutstandingExpenses = outstandingExpensesPerArtist[artistName] ?: throw Exception("No expenses recognised for artist \"${artistName}\" for release $catNo")
        val artistPayout = contract.calculateArtistPayout(valueOfSale, artistOutstandingExpenses)
        val labelRecoupedValue = valueOfSale - artistPayout

        val newExpenseValue = artistOutstandingExpenses - labelRecoupedValue
        outstandingExpensesPerArtist[artistName] = newExpenseValue

        return Payout(artistName, itemName, artistPayout, labelRecoupedValue, date)
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

    private fun calculateReleaseSplit(): Split {

        val totalTracks = tracks.size

        val splitMap = tracks.map { it.split }
            .combineFloatMapsWithSummedValues()
            .map { it.key to (it.value / totalTracks) }
            .toTypedArray()

        return Split.customSplit(splits = splitMap)
    }
}