package catalogue

import combineFloatMapsWithSummedValues
import sales.*
import java.time.LocalDate

data class Release(
    val catNo: String,
    /**
     * The date -> price map. If the price of the release is always the same then this only requires a single entry.
     * Initial entry should be on the date at which sales began (NOT ANY TIME BEFORE) - including pre-release. This information is used to calculate discography bundle payouts.
     */
    val digitalPriceMap: Map<LocalDate, Int>,
    private val tracks: Set<Track>,
    internal val contract: Contract,
    private val expenses: MutableList<Expense> = mutableListOf(),
    /**
     * The date at which sales stopped. May be kept null if sales were not stopped / still ongoing
     */
    private val salesStopDate: LocalDate? = null
) {

    private val sales: MutableList<ReleaseSaleItem> = mutableListOf()

    fun getAllSales(): List<SaleItem> = (sales + tracks.flatMap { it.sales })

    fun getExpensesValueUpTo(until: LocalDate): Int = expenses.getExpenseValueUpTo(until)

    fun getTotalExpenseValueBetweenDates(from: LocalDate, untilInclusive: LocalDate): Int = expenses.getTotalExpenseValueBetweenDates(from, untilInclusive)

    fun addExternalSale(value: Int, date: LocalDate) {
        sales.add(ExternalReleaseSale(catNo, value, value, date))
    }

    fun calculateReleaseSplit(): Split {

        val totalTracks = tracks.size

        val splitMap = tracks.map { it.split }
            .combineFloatMapsWithSummedValues()
            .map { it.key to (it.value / totalTracks) }
            .toTypedArray()

        return Split.customSplit(splits = splitMap)
    }

    internal fun addSale(saleItem: SaleItem) {
        when (saleItem) {
            is DigitalReleaseSale -> sales.add(saleItem)
            is CategorisedPhysicalSale -> sales.add(saleItem)
            is TrackSale -> {
                try {
                    findTrack(saleItem.trackName)
                } catch (e: Exception) {
                    throw Exception("Could not apply track sale.", e)
                }.addSale(saleItem)
            }
            else -> throw Exception("Unrecognised sale item.")
        }
    }

    internal fun priceOnDate(date: LocalDate): Int {

        if (digitalPriceMap.isEmpty()) return 0

        val priceDateIterator = digitalPriceMap.keys.asSequence()
            .sorted()
            .iterator()

        var priceDate = priceDateIterator.next()
        while (priceDateIterator.hasNext()) {
            val temp = priceDateIterator.next()
            if (temp.isBefore(date) || temp.isEqual(date))
                priceDate = temp
        }

        return digitalPriceMap[priceDate] ?: throw Exception("Cannot find price for $catNo on date $priceDate")
    }

    internal fun wasActivelySellingOn(date: LocalDate): Boolean {
        if (digitalPriceMap.isEmpty()) return false

        val salesStartDate = digitalPriceMap.keys.asSequence().min()
        return (salesStartDate == date || salesStartDate.isBefore(date)) && salesStopDate?.isAfter(date) ?: true
    }

    internal fun findTrack(trackName: String): Track = tracks.find { it.name == trackName }
        ?: throw IllegalArgumentException("Release $catNo does not contain track with name $trackName")
}