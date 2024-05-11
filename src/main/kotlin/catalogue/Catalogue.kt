package catalogue

import addValues
import payout.*
import sales.*
import java.time.LocalDate

class Catalogue(private val releases: MutableList<Release> = mutableListOf()) {

    private val uncategorisedPhysicalSales: MutableList<UncategorisedPhysicalSale> = mutableListOf()

    private val digitalDiscographySales: MutableList<DigitalDiscographySale> = mutableListOf()

    fun addReleaseToCatalog(release: Release) {
        releases.add(release)
    }

    fun provideSalesData(salesReport: SalesReport) {

        salesReport.sales.map { saleItem ->
            when (saleItem) {
                is CategorisedSaleItem -> applyCategorisedSale(saleItem)
                is UncategorisedSaleItem -> applyUncategorisedSale(saleItem)
            }
        }
    }

    fun calculatePayouts(from: LocalDate, to: LocalDate): List<Payout> {

        val allSalesSortedByDate = (releases.flatMap { it.getAllSales() } + digitalDiscographySales + uncategorisedPhysicalSales)
            .sortedBy { it.date }

        // The cursor date tracks when the last expense values were loaded. For the first cursor we take the very first sale date
        var cursorDate = allSalesSortedByDate.getOrNull(0)?.date
        // If no sales exist yet just return here
            ?: return emptyList()

        val releaseSplits = releases.associate { it.catNo to it.calculateReleaseSplit() }
        val releaseOutstandingExpensesPerArtist = releases.associate {
            val totalDefaultExpenseValue = it.getExpensesValueUpTo(cursorDate)
            val expensesPerArtist = releaseSplits[it.catNo]?.calculateShares(totalDefaultExpenseValue)?.toMutableMap()
                ?: throw Exception("Could not get find release split for ${it.catNo} when attempting to calculate expense split")

            it.catNo to expensesPerArtist
        }.toMutableMap()


        return allSalesSortedByDate.flatMap { saleItem ->
            val dateOfSale = saleItem.date

            // If the date becomes later than the previous update of expense values then load the expense values that have occurred up to this new date for each release and each artist within the release
            if (cursorDate.isBefore(dateOfSale)) {

                releases.forEach {
                    val newExpenses = it.getTotalExpenseValueBetweenDates(cursorDate, dateOfSale)
                    val newExpensesPerArtist = releaseSplits[it.catNo]?.calculateShares(newExpenses)
                        ?: throw Exception("Could not find release split for ${it.catNo} when trying to generate expenses per artist")
                    releaseOutstandingExpensesPerArtist[it.catNo] = releaseOutstandingExpensesPerArtist[it.catNo]?.addValues(newExpensesPerArtist)?.toMutableMap()
                        ?: throw Exception("Could not find outstanding expenses per release for ${it.catNo}")
                }

                // update cursor to this date
                cursorDate = dateOfSale
            }

            when(saleItem) {
                is ReleaseSaleItem -> {
                    val releaseSplit = releaseSplits[saleItem.catNo] ?: throw Exception("")
                    val outstandingExpensesPerArtist = releaseOutstandingExpensesPerArtist[saleItem.catNo] ?: throw Exception("")
                    val contract = findRelease(saleItem.catNo).contract
                    calculatePayoutsForReleaseSale(saleItem, releaseSplit, outstandingExpensesPerArtist, contract)
                }
                is TrackSale -> {
                    val release = findRelease(saleItem.catNo)
                    val trackSplit = release.findTrack(saleItem.trackName).split
                    val outstandingExpensesPerArtist = releaseOutstandingExpensesPerArtist[saleItem.catNo] ?: throw Exception("")
                    val contract = release.contract
                    calculatePayoutsForTrackSale(saleItem, trackSplit, outstandingExpensesPerArtist, contract)
                }
                is UncategorisedSaleItem -> {
                    when (saleItem) {
                        is DigitalDiscographySale -> calculatePayoutsForDigitalDiscographySale(saleItem, releaseSplits, releaseOutstandingExpensesPerArtist)
                        is UncategorisedPhysicalSale -> {
                            val itemDetails = ItemDetails("${saleItem.itemName} - ${saleItem.packageName}", ItemType.PHYSICAL_RELEASE)
                            val saleInformation = SaleInformation(
                                saleItem.bandcampTransactionId,
                                saleItem.date,
                                saleItem.netValue,
                                saleItem.grossValue,
                                saleItem.netValue,
                                saleItem.grossValue,
                                100f
                            )
                            val labelPayout = LabelPayout(saleItem.itemName, saleItem.netValue)
                            listOf(Payout("[LABEL]", 0, itemDetails, saleInformation, labelPayout))
                        }
                        else -> throw Exception("No branch for uncategorised sale item sale when calculating payouts")
                    }
                }
                else -> throw Exception("Invalid sale item value when attempting to calculate payouts ${saleItem.javaClass.simpleName}")
            }
        }
            .filter { it.saleInformation.date.isAfter(from) || it.saleInformation.date.isEqual(from) }
            .filter { it.saleInformation.date.isBefore(to) }
    }

    private fun calculatePayoutsForReleaseSale(
        releaseSale: ReleaseSaleItem,
        releaseSplit: Split,
        outstandingExpensesPerArtist: MutableMap<String, Int>,
        contract: Contract
    ): List<Payout> {
        return releaseSplit.calculateSaleShares(releaseSale.netValue, releaseSale.grossValue).map {

            val bandcampTransactionId = if (releaseSale is BandcampSaleItem) releaseSale.bandcampTransactionId else null
            val itemType = when (releaseSale) {
                is PhysicalSale -> ItemType.PHYSICAL_RELEASE
                is DigitalReleaseSale -> ItemType.DIGITAL_RELEASE
                is ExternalReleaseSale -> ItemType.DIGITAL_RELEASE_EXTERNAL
                else -> throw Exception("No suitable item type for ${releaseSale.javaClass.simpleName}")
            }

            val artist = it.key
            val proportionedNetSaleValue = it.value.first
            val proportionedGrossSaleValue = it.value.second

            val saleInformation = SaleInformation(
                bandcampTransactionId = bandcampTransactionId,
                date = releaseSale.date,
                netValue = proportionedNetSaleValue,
                grossValue = proportionedGrossSaleValue,
                totalNetValue = releaseSale.netValue,
                totalGrossValue = releaseSale.grossValue,
                splitProportion = releaseSplit[artist] ?: throw Exception("No share recognised for artist \"$artist\" for release ${releaseSale.catNo}")
            )

            calculateArtistPayout(
                contract,
                artist,
                releaseSale.catNo,
                releaseSale.catNo,
                saleInformation,
                outstandingExpensesPerArtist,
                itemType
            )
        }
    }

    private fun calculatePayoutsForTrackSale(
        trackSale: TrackSale,
        trackSplit: Split,
        outstandingExpensesPerArtist: MutableMap<String, Int>,
        contract: Contract
    ): List<Payout> {

        return trackSplit.calculateSaleShares(trackSale.netValue, trackSale.grossValue).map {
            val artist = it.key
            val proportionedNetSaleValue = it.value.first
            val proportionedGrossSaleValue = it.value.second

            val saleInformation = SaleInformation(
                bandcampTransactionId = trackSale.bandcampTransactionId,
                date = trackSale.date,
                netValue = proportionedNetSaleValue,
                grossValue = proportionedGrossSaleValue,
                totalNetValue = trackSale.netValue,
                totalGrossValue = trackSale.grossValue,
                splitProportion = trackSplit[artist] ?: throw Exception("No share recognised for artist \"$artist\" for track ${trackSale.trackName}")
            )

            calculateArtistPayout(
                contract,
                artist,
                trackSale.trackName,
                trackSale.catNo,
                saleInformation,
                outstandingExpensesPerArtist,
                ItemType.TRACK
            )
        }
    }

    private fun calculatePayoutsForDigitalDiscographySale(
        digitalDiscographySale: DigitalDiscographySale,
        releaseSplits: Map<String, Split>,
        releaseOutstandingExpensesPerArtist: MutableMap<String, MutableMap<String, Int>>
    ): List<Payout> {
        return releases.getBundleSplit(digitalDiscographySale.date)
            .calculateSaleShares(digitalDiscographySale.netValue, digitalDiscographySale.grossValue)
            .flatMap { releaseSaleShares ->

                val catNo = releaseSaleShares.key
                val proportionedReleaseNetSaleValue = releaseSaleShares.value.first
                val proportionedReleaseGrossSaleValue = releaseSaleShares.value.second

                val releaseSplit = releaseSplits[catNo]
                    ?: throw Exception("Cannot find release split for release sale share")
                val outstandingExpensesPerArtistForRelease = releaseOutstandingExpensesPerArtist[catNo]
                    ?: throw Exception("Cannot find outstanding expenses for release")

                releaseSplit.calculateSaleShares(proportionedReleaseNetSaleValue, proportionedReleaseGrossSaleValue)
                    .map { artistSaleShares ->

                        val artistName = artistSaleShares.key
                        val proportionedArtistNetSaleValue = artistSaleShares.value.first
                        val proportionedArtistGrossSaleValue = artistSaleShares.value.second

                        val saleInformation = SaleInformation(
                            bandcampTransactionId = digitalDiscographySale.bandcampTransactionId,
                            date = digitalDiscographySale.date,
                            netValue = proportionedArtistNetSaleValue,
                            grossValue = proportionedArtistGrossSaleValue,
                            totalNetValue = digitalDiscographySale.netValue,
                            totalGrossValue = digitalDiscographySale.grossValue,
                            splitProportion = releaseSplit[artistName] ?: throw Exception("No share recognised for artist \"$artistName\" for release $catNo")
                        )


                        val contract = findRelease(catNo).contract

                        calculateArtistPayout(
                            contract = contract,
                            artistName = artistName,
                            itemName = "Digital Discography Sale Share",
                            catNo = catNo,
                            saleInformation = saleInformation,
                            outstandingExpensesPerArtist = outstandingExpensesPerArtistForRelease,
                            itemType = ItemType.DIGITAL_DISCOGRAPHY_SHARE
                        )
                }
            }
    }

    private fun calculateArtistPayout(
        contract: Contract,
        artistName: String,
        itemName: String,
        catNo: String,
        saleInformation: SaleInformation,
        outstandingExpensesPerArtist: MutableMap<String, Int>,
        itemType: ItemType
    ): Payout {
        val artistOutstandingExpenses = outstandingExpensesPerArtist[artistName] ?: throw Exception("No expenses recognised for artist \"${artistName}\" for item $catNo")

        val share = contract.calculateSaleShare(saleInformation.netValue, itemType, artistOutstandingExpenses)

        val newExpenseValue = artistOutstandingExpenses - share.labelShare
        outstandingExpensesPerArtist[artistName] = newExpenseValue

        val itemDetails = ItemDetails(itemName, itemType)
        val labelPayout = if (share.labelShare <= 0) null else LabelPayout(catNo, share.labelShare)
        return Payout(artistName, share.artistShare, itemDetails, saleInformation, labelPayout)
    }

    private fun applyCategorisedSale(saleItem: CategorisedSaleItem) {
        when (saleItem) {
            is DigitalReleaseSale -> applyReleaseSale(saleItem)
            is TrackSale -> applyTrackSale(saleItem)
            is CategorisedPhysicalSale -> applyCategorisedPhysicalSale(saleItem)
            else -> throw Exception("Unrecognised categorised sale type ${saleItem.javaClass.simpleName}")
        }
    }

    private fun applyUncategorisedSale(saleItem: UncategorisedSaleItem) {
        when (saleItem) {
            is DigitalDiscographySale -> digitalDiscographySales.add(saleItem)
            is UncategorisedPhysicalSale -> uncategorisedPhysicalSales.add(saleItem)
        }
    }

    private fun applyReleaseSale(digitalReleaseSale: DigitalReleaseSale) {
        findRelease(digitalReleaseSale.catNo).addSale(digitalReleaseSale)
    }

    private fun applyTrackSale(trackSale: TrackSale) {
        val track: Track = findRelease(trackSale.catNo).findTrack(trackSale.trackName)

        track.addSale(trackSale)
    }

    private fun applyCategorisedPhysicalSale(categorisedPhysicalSale: CategorisedPhysicalSale) {
        try {
            findRelease(categorisedPhysicalSale.catNo)
        } catch (e: Exception) {

            println("Physical sale with no associated release found. Package name: \"${categorisedPhysicalSale.packageName}\". Ignoring...")
            return
        }.addSale(categorisedPhysicalSale)
    }

    private fun findRelease(catNo: String): Release = releases.find { it.catNo == catNo }
        ?: throw Exception("No release with $catNo found in catalogue")
}