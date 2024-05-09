package catalogue

data class Track(val name: String, val split: Split) {

    /**
     * Sale values tracked by date where
     * - first -> Integer value representing minor currency value
     * - second -> Time of sale
     */
    internal val sales: MutableList<Sale> = mutableListOf()

    internal fun addSale(sale: Sale) {
        sales.add(sale)
    }

    internal fun getSaleSharesMappedByContributingArtist(): List<ArtistProportionedSale> {

        return sales.flatMap { sale ->
            val artistToSaleValue = split.calculateShares(sale.netValue)

            artistToSaleValue.map {
                ArtistProportionedSale(
                    artist = it.key,
                    itemName = name,
                    bandcampTransactionId = sale.bandcampTransactionId,
                    netValue = it.value,
                    grossValue = sale.grossValue,
                    date = sale.date,
                    saleType = sale.saleType)
            }.asSequence()
        }
    }
}