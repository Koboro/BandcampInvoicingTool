import sales.BundleSale
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
                is ReleaseSale -> applyReleaseSale(saleItem)
                is TrackSale -> applyTrackSale(saleItem)
                is BundleSale -> applyBundleSale(saleItem)
                else -> throw Exception("Unrecognised sale type")
            }
        }
    }

    fun calculatePayouts(from: LocalDate, to: LocalDate): List<Payout> {
        return releases.flatMap { it.calculatePayout(from, to) }
    }

    private fun applyReleaseSale(releaseSale: ReleaseSale) {
        findRelease(releaseSale.catNo).applySale(releaseSale)
    }

    private fun applyTrackSale(trackSale: TrackSale) {
        val track: Track = findRelease(trackSale.catNo).findTrack(trackSale.trackName)

        track.applySale(trackSale.value, trackSale.dateTime)
    }

    private fun applyBundleSale(bundleSale: BundleSale) {

        if (bundleSale.itemName.contains("full digital discography")) {
            releases.getBundleSplit(bundleSale.dateTime)
                .calculateShares(bundleSale.value)
                .map { ReleaseSale(it.key, it.value, bundleSale.dateTime) }
                .forEach(this::applyReleaseSale)
        } else {
            // Only supports digital discography bundles right now
            throw Exception("No appropriate actions for bundle item")
        }
    }

    private fun findRelease(catNo: String): Release = releases.find { it.catNo == catNo }
        ?: throw Exception("No release applicable for sale")
}