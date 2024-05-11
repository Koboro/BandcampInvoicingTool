package catalogue

import payout.ItemType

/**
 * Class representing a simple agreed percentage split between the label and artists, depending on whether the item sold
 * was digital or physical.
 *
 * @param labelPercentageOnDigitalBeforeBreakEven The percentage of revenue from digital sales that goes to the label before break-even is achieved on sales
 * @param labelPercentageOnDigitalAfterBreakEven The percentage of revenue from digital sales that goes to the label after break-even is achieved on sales
 * @param labelPercentageOnPhysicalBeforeBreakEven The percentage of revenue from physical sales that goes to the label before break-even is achieved on sales
 * @param labelPercentageOnPhysicalAfterBreakEven The percentage of revenue from physical sales that goes to the label after break-even is achieved on sales
 */
data class SimpleContract(
    val labelPercentageOnDigitalBeforeBreakEven: Float,
    val labelPercentageOnDigitalAfterBreakEven: Float,
    val labelPercentageOnPhysicalBeforeBreakEven: Float,
    val labelPercentageOnPhysicalAfterBreakEven: Float,
): Contract {
    init {

        if (labelPercentageOnDigitalBeforeBreakEven > 100 || labelPercentageOnDigitalBeforeBreakEven < 0) {
            throw IllegalArgumentException("labelPercentageBeforeBreakEven has invalid value of $labelPercentageOnDigitalBeforeBreakEven. Must be between 0 and 100.")
        }

        if (labelPercentageOnDigitalAfterBreakEven > 100 || labelPercentageOnDigitalAfterBreakEven < 0) {
            throw IllegalArgumentException("labelPercentageAfterBreakEven has invalid value of $labelPercentageOnDigitalAfterBreakEven. Must be between 0 and 100.")
        }
    }

    override fun calculateSaleShare(saleValue: Int, itemType: ItemType, outstandingExpenses: Int): Share {
        return when(itemType) {

            ItemType.DIGITAL_RELEASE, ItemType.DIGITAL_RELEASE_EXTERNAL, ItemType.TRACK, ItemType.DIGITAL_DISCOGRAPHY_SHARE ->
                calculateArtistPayout(
                    saleValue,
                    outstandingExpenses,
                    labelPercentageOnDigitalBeforeBreakEven,
                    labelPercentageOnDigitalAfterBreakEven
                )

            ItemType.PHYSICAL_RELEASE ->
                calculateArtistPayout(
                    saleValue,
                    outstandingExpenses,
                    labelPercentageOnPhysicalBeforeBreakEven,
                    labelPercentageOnPhysicalAfterBreakEven
                )
        }
    }

    private fun calculateArtistPayout(
        saleValue: Int,
        outstandingExpenses: Int,
        labelPercentageBeforeBreakEven: Float,
        labelPercentageAfterBreakEven: Float
    ): Share {

        //Not a fully necessary branch, but just avoids unnecessary calculations when there are no outstanding expenses
        if (outstandingExpenses > 0) {
            val artistShareValue = calculateArtistShareValueForUnsettledSales(
                saleValue,
                outstandingExpenses,
                labelPercentageBeforeBreakEven,
                labelPercentageAfterBreakEven
            )

            return Share(artistShareValue, saleValue - artistShareValue)
        }

        val labelShareValue = (saleValue * (labelPercentageAfterBreakEven / 100)).toInt()
        return Share(saleValue - labelShareValue, labelShareValue)
    }

    private fun calculateArtistShareValueForUnsettledSales(
        totalSales: Int,
        outstandingExpenses: Int,
        labelPercentageBeforeBreakEven: Float,
        labelPercentageAfterBreakEven: Float
    ): Int {
        val artistPercentageBeforeBreakEven = 100f - labelPercentageBeforeBreakEven
        val artistPercentageAfterBreakEven = 100f - labelPercentageAfterBreakEven

        val valueRequiredToBreakEven = (outstandingExpenses / (labelPercentageBeforeBreakEven / 100)).toInt()

        if (valueRequiredToBreakEven > totalSales) {
            return (totalSales * (artistPercentageBeforeBreakEven / 100)).toInt()
        }

        val beforeBreakEven = (valueRequiredToBreakEven * (artistPercentageBeforeBreakEven / 100)).toInt()

        val remaining = totalSales - valueRequiredToBreakEven

        val afterBreakEven = (remaining * (artistPercentageAfterBreakEven / 100)).toInt()

        return beforeBreakEven + afterBreakEven
    }
}