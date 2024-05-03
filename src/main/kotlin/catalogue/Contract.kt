package catalogue

/**
 * Class representing an agreed percentage split between the label and artists.
 * @param labelPercentageOnDigitalBeforeBreakEven The percentage of revenue that goes to the label before break-even is achieved on sales
 * @param labelPercentageOnDigitalAfterBreakEven The percentage of revenue that goes to the label after break-even is achieved on sales
 */
data class Contract(
    val labelPercentageOnDigitalBeforeBreakEven: Float,
    val labelPercentageOnDigitalAfterBreakEven: Float,
    val labelPercentageOnPhysicalBeforeBreakEven: Float,
    val labelPercentageOnPhysicalAfterBreakEven: Float,
) {
    init {

        if (labelPercentageOnDigitalBeforeBreakEven > 100 || labelPercentageOnDigitalBeforeBreakEven < 0) {
            throw IllegalArgumentException("labelPercentageBeforeBreakEven has invalid value of $labelPercentageOnDigitalBeforeBreakEven. Must be between 0 and 100.")
        }

        if (labelPercentageOnDigitalAfterBreakEven > 100 || labelPercentageOnDigitalAfterBreakEven < 0) {
            throw IllegalArgumentException("labelPercentageAfterBreakEven has invalid value of $labelPercentageOnDigitalAfterBreakEven. Must be between 0 and 100.")
        }
    }

    fun calculateOutstandingExpenses(totalSales: Int, totalExpenses: Int): Int {
        val valueRequiredToBreakEven = (totalExpenses / (labelPercentageOnDigitalBeforeBreakEven / 100)).toInt()

        if (totalSales >= valueRequiredToBreakEven) {
            return 0
        }

        val labelPayout = (totalSales * (labelPercentageOnDigitalBeforeBreakEven / 100)).toInt()
        return totalExpenses - labelPayout
    }

    fun calculateArtistPayoutForDigitalSales(totalSales: Int, outstandingExpenses: Int): Int {

        return calculateArtistPayout(
            totalSales,
            outstandingExpenses,
            labelPercentageOnDigitalBeforeBreakEven,
            labelPercentageOnDigitalAfterBreakEven
        )
    }

    fun calculateArtistPayoutForPhysicalSales(totalSales: Int, outstandingExpenses: Int): Int {

        return calculateArtistPayout(
            totalSales,
            outstandingExpenses,
            labelPercentageOnPhysicalBeforeBreakEven,
            labelPercentageOnPhysicalAfterBreakEven
        )
    }

    private fun calculateArtistPayout(
        totalSales: Int,
        outstandingExpenses: Int,
        labelPercentageBeforeBreakEven: Float,
        labelPercentageAfterBreakEven: Float
    ): Int {

        //Not a fully necessary branch, but just avoids unnecessary calculations when there are no outstanding expenses
        if (outstandingExpenses > 0) {
            return calculateArtistPayoutForUnsettledSales(
                totalSales,
                outstandingExpenses,
                labelPercentageBeforeBreakEven,
                labelPercentageAfterBreakEven
            )
        }

        val labelPayout = (totalSales * (labelPercentageAfterBreakEven / 100)).toInt()
        return totalSales - labelPayout
    }

    private fun calculateArtistPayoutForUnsettledSales(
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

    /*private fun calculateArtistPayoutForUnsettledSales(totalSales: Int, outstandingExpenses: Int): Int {
        val valueRequiredToBreakEven = (outstandingExpenses / (labelPercentageOnDigitalBeforeBreakEven / 100)).toInt()

        if (valueRequiredToBreakEven > totalSales) {
            return (totalSales * (labelPercentageOnDigitalBeforeBreakEven / 100)).toInt()
        }

        val beforeBreakEven = (valueRequiredToBreakEven * (artistPercentageBeforeBreakEven() / 100)).toInt()

        val remaining = totalSales - valueRequiredToBreakEven

        val afterBreakEven = (remaining * (artistPercentageAfterBreakEven() / 100)).toInt()

        return beforeBreakEven + afterBreakEven
    }*/

    /*private fun artistPercentageBeforeBreakEven(): Float = 100f - labelPercentageOnDigitalBeforeBreakEven

    private fun artistPercentageAfterBreakEven(): Float = 100f - labelPercentageOnDigitalAfterBreakEven*/
}