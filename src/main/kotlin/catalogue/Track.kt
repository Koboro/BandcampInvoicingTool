package catalogue

import sales.TrackSale

data class Track(internal val name: String, internal val split: Split) {

    internal val sales: MutableList<TrackSale> = mutableListOf()

    internal fun addSale(sale: TrackSale) {
        sales.add(sale)
    }
}