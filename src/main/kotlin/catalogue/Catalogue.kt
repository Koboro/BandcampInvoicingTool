package catalogue

import payout.Payout
import sales.*
import java.time.LocalDate

class Catalogue(private val releases: MutableList<Release> = mutableListOf()) {

    fun addReleaseToCatalog(release: Release) {
        releases.add(release)
    }

    fun provideSalesData(salesReport: SalesReport) {

        salesReport.sales.map { saleItem ->
            when (saleItem) {
                is ReleaseSale -> applyReleaseSale(saleItem)
                is TrackSale -> applyTrackSale(saleItem)
                is DigitalDiscographySale -> applyDigitalDiscographySale(saleItem)
                is PhysicalSale -> applyPhysicalSale(saleItem)
                else -> throw Exception("Unrecognised sale type")
            }
        }
    }

    fun calculatePayouts(from: LocalDate, to: LocalDate): List<Payout> {
        return releases.flatMap { it.calculatePayout(from, to) }
    }

    private fun applyReleaseSale(releaseSale: ReleaseSale) {
        findRelease(releaseSale.catNo).addSale(releaseSale)
    }

    private fun applyTrackSale(trackSale: TrackSale) {
        val track: Track = findRelease(trackSale.catNo).findTrack(trackSale.trackName)

        track.addSale(Sale(trackSale.netValue, trackSale.dateTime, SaleType.TRACK))
    }

    private fun applyDigitalDiscographySale(digitalDiscographySale: DigitalDiscographySale) {

        if (digitalDiscographySale.itemName.contains("full digital discography")) {
            releases.getBundleSplit(digitalDiscographySale.dateTime)
                .calculateShares(digitalDiscographySale.netValue)
                .forEach { findRelease(it.key).addDigitalDiscographySale(it.value, digitalDiscographySale.dateTime) }
        } else {
            // Only supports digital discography bundles right now
            throw Exception("No appropriate actions for bundle item")
        }
    }

    private fun applyPhysicalSale(physicalSale: PhysicalSale) {
        try {
            findRelease(physicalSale.catNo)
        } catch (e: Exception) {

            println("Physical sale with no associated release found. Package name: \"${physicalSale.packageName}\". Ignoring...")
            return
        }.addSale(physicalSale)
    }

    private fun findRelease(catNo: String): Release = releases.find { it.catNo == catNo }
        ?: throw Exception("No release with $catNo found in catalogue")
}