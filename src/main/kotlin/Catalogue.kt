import sales.ReleaseSale
import sales.SalesReport
import sales.TrackSale
import java.time.LocalDate

class Catalogue {

    private val releases: MutableList<Release> = mutableListOf()

    fun addReleaseToCatalog(release: Release) {
        releases.add(release)
    }

    /**
     * @param releaseCatNo The cat no. of the release to add an expense to.
     * @param expense the expense to add
     */
    fun addExpense(releaseCatNo: String, expense: Expense) {
        findRelease(releaseCatNo).addExpense(expense)
    }


    fun provideSalesData(salesReport: SalesReport) {

        salesReport.sales.map { saleItem ->
            when (saleItem) {
                is ReleaseSale -> findRelease(saleItem.catNo).applySale(saleItem)
                is TrackSale -> applyTrackSale(saleItem)
                else -> throw Exception("Unrecognised sale type")
            }
        }
    }

    /**
     * @return the mappings of sales values to be distributed {Artist Name -> Sale Value}
     */
    fun calculatePayoutsFromDate(from: LocalDate): Map<String, Int> {
        return releases.map { it.calculatePayout(from) }.combineIntMapsWithSummedValues()
    }

    private fun applyTrackSale(trackSale: TrackSale) {
        val track: Track = findRelease(trackSale.catNo).findTrack(trackSale.trackName)
            ?: throw Exception("No track with name ${trackSale.trackName}")

        track.applySale(trackSale.value, trackSale.dateTime)
    }

    private fun findRelease(catNo: String): Release = releases.find { it.catNo == catNo }
        ?: throw Exception("No release applicable for sale")
}