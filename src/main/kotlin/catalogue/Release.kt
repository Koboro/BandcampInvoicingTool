package catalogue

import addValues
import combineFloatMapsWithSummedValues
import payout.*
import sales.PhysicalSale
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
    val digitalPriceMap: Map<LocalDate, Int>,
    private val tracks: Set<Track>,
    private val contract: Contract,
    private val expenses: MutableList<Expense> = mutableListOf(),
    /**
     * The date at which sales stopped. May be kept null if sales were not stopped / still ongoing
     */
    private val salesStopDate: LocalDate? = null
) {

    private val sales: MutableList<Sale> = mutableListOf()

    fun calculatePayout(from: LocalDate, to: LocalDate = LocalDate.now()): List<Payout> {

        return generateTotalPayout()
            .filter { it.saleInformation.date.isAfter(from) || it.saleInformation.date.isEqual(from) }
            .filter { it.saleInformation.date.isBefore(to) }
    }

    fun addExternalSale(value: Int, date: LocalDate) {
        addSale(Sale("N/A (external)", value, value, date, SaleType.RELEASE))
    }

    internal fun addSale(saleItem: SaleItem) {
        when (saleItem) {
            is ReleaseSale -> {
                sales.add(Sale(
                    bandcampTransactionId = saleItem.bandcampTransactionId,
                    netValue = saleItem.netValue,
                    grossValue = saleItem.grossValue,
                    date = saleItem.dateTime,
                    saleType = SaleType.RELEASE)
                )
            }
            is TrackSale -> {
                try {
                    findTrack(saleItem.trackName)
                } catch (e: Exception) {
                    throw Exception("Could not apply track sale.", e)
                }.addSale(Sale(
                    bandcampTransactionId = saleItem.bandcampTransactionId,
                    netValue = saleItem.netValue,
                    grossValue = saleItem.grossValue,
                    date = saleItem.dateTime,
                    saleType = SaleType.TRACK)
                )
            }
            is PhysicalSale -> {
                sales.add(Sale(
                    bandcampTransactionId = saleItem.bandcampTransactionId,
                    netValue = saleItem.netValue,
                    grossValue = saleItem.grossValue,
                    date = saleItem.dateTime,
                    saleType = SaleType.PHYSICAL)
                )
            }
            else -> throw Exception("Unrecognised sale item.")
        }
    }

    internal fun addDigitalDiscographySale(value: Int, date: LocalDate) {
        addSale(Sale(
            bandcampTransactionId = "N/A (digital discography)",
            netValue = value,
            grossValue = value, // TODO: Gross value?
            date = date,
            saleType = SaleType.DIGITAL_DISCOGRAPHY)
        )
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


    private fun addSale(sale: Sale) {
        sales.add(sale)
    }

    private fun generateTotalPayout(): List<Payout> {

        val trackSales: List<ArtistProportionedSale> = tracks.flatMap { it.getSaleSharesMappedByContributingArtist() }
        val releaseSales: List<ArtistProportionedSale> = sales.map { ArtistProportionedSale(
            artist = null,
            itemName = catNo,
            bandcampTransactionId = it.bandcampTransactionId,
            netValue = it.netValue,
            grossValue = it.grossValue,
            date = it.date,
            saleType = it.saleType)
        }
        val allSalesSortedByDate: List<ArtistProportionedSale> = (trackSales + releaseSales)
            .sortedBy { it.date }
        // The cursor date tracks when the last expense values were loaded. For the first cursor we take the very first sale date
        var cursorDate = allSalesSortedByDate.getOrNull(0)?.date
        // If no sales exist yet just return here
            ?: return emptyList()

        val totalDefaultExpenseValue = expenses.getExpenseValueUpTo(cursorDate)
        val releaseSplit = calculateReleaseSplit()
        var outstandingExpensesPerArtist = releaseSplit.calculateShares(totalDefaultExpenseValue).toMutableMap()

        return allSalesSortedByDate.flatMap { artistProportionedSale ->

            val valueOfSale = artistProportionedSale.netValue
            val dateOfSale = artistProportionedSale.date

            // If the date becomes later than the previous update of expense values then load the expense values that have occurred up to this new date
            if (cursorDate.isBefore(dateOfSale)) {
                val newExpenses = expenses.getTotalExpenseValueBetweenDates(cursorDate, dateOfSale)
                outstandingExpensesPerArtist = outstandingExpensesPerArtist.addValues(releaseSplit.calculateShares(newExpenses)).toMutableMap()

                // update cursor to this date
                cursorDate = dateOfSale
            }

            if (artistProportionedSale.artist == null) {
                return@flatMap releaseSplit.calculateShares(valueOfSale).map {
                    val artistName = it.key
                    val splitProportion = releaseSplit[artistName] ?: throw Exception("No share recognised for artist \"$artistName\" for release $catNo")
                    val saleInformation = SaleInformation(
                        bandcampTransactionId = artistProportionedSale.bandcampTransactionId,
                        netValue = it.value,
                        grossValue = artistProportionedSale.grossValue,
                        date = dateOfSale,
                        splitProportion = splitProportion
                    )
                    calculateArtistPayout(artistName, catNo, saleInformation, outstandingExpensesPerArtist, artistProportionedSale.saleType)
                }

            } else {
                val artistName = artistProportionedSale.artist
                val itemName = artistProportionedSale.itemName
                val splitProportion = findSplitProportionForTrack(itemName, artistName)
                val saleInformation = SaleInformation(
                    bandcampTransactionId = artistProportionedSale.bandcampTransactionId,
                    netValue = valueOfSale,
                    grossValue = artistProportionedSale.grossValue,
                    date = dateOfSale,
                    splitProportion = splitProportion
                )

                return@flatMap listOf(calculateArtistPayout(artistName, itemName, saleInformation, outstandingExpensesPerArtist, artistProportionedSale.saleType))
            }
        }
    }

    private fun calculateArtistPayout(
        artistName: String,
        itemName: String,
        saleInformation: SaleInformation,
        outstandingExpensesPerArtist: MutableMap<String, Int>,
        saleType: SaleType
    ): Payout {
        val artistOutstandingExpenses = outstandingExpensesPerArtist[artistName] ?: throw Exception("No expenses recognised for artist \"${artistName}\" for release $catNo")

        val itemType = when (saleType) {
            SaleType.RELEASE -> ItemType.RELEASE
            SaleType.DIGITAL_DISCOGRAPHY -> ItemType.DIGITAL_DISCOGRAPHY_SHARE
            SaleType.TRACK -> ItemType.TRACK
            SaleType.PHYSICAL -> ItemType.PHYSICAL
        }

        val share = contract.calculateSaleShare(saleInformation.netValue, itemType, artistOutstandingExpenses)

        val newExpenseValue = artistOutstandingExpenses - share.labelShare
        outstandingExpensesPerArtist[artistName] = newExpenseValue

        val itemDetails = ItemDetails(itemName, itemType)
        val labelPayout = if (share.labelShare <= 0) null else LabelPayout(catNo, share.labelShare)
        return Payout(artistName, share.artistShare, itemDetails, saleInformation, labelPayout)
    }

    private fun findSplitProportionForTrack(trackName: String, artistName: String): Float = tracks
        .first { it.name == trackName }
        .split[artistName]
        ?: throw Exception("Could not find share for $artistName on track $trackName")

    private fun calculateReleaseSplit(): Split {

        val totalTracks = tracks.size

        val splitMap = tracks.map { it.split }
            .combineFloatMapsWithSummedValues()
            .map { it.key to (it.value / totalTracks) }
            .toTypedArray()

        return Split.customSplit(splits = splitMap)
    }
}