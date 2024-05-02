/**
 * Class representing an agreed percentage split between the label and artists.
 * @param labelPercentageBeforeBreakEven The percentage of revenue that goes to the label before break-even is achieved on sales
 * @param labelPercentageAfterBreakEven The percentage of revenue that goes to the label after break-even is achieved on sales
 */
data class Contract(
    val labelPercentageBeforeBreakEven: Float,
    val labelPercentageAfterBreakEven: Float
) {
    init {

        if (labelPercentageBeforeBreakEven > 100 || labelPercentageBeforeBreakEven < 0) {
            throw IllegalArgumentException("labelPercentageBeforeBreakEven has invalid value of $labelPercentageBeforeBreakEven. Must be between 0 and 100.")
        }

        if (labelPercentageAfterBreakEven > 100 || labelPercentageAfterBreakEven < 0) {
            throw IllegalArgumentException("labelPercentageAfterBreakEven has invalid value of $labelPercentageAfterBreakEven. Must be between 0 and 100.")
        }
    }

    fun calculateOutstandingExpenses(totalSales: Int, totalExpenses: Int): Int {
        val valueRequiredToBreakEven = (totalExpenses / (labelPercentageBeforeBreakEven / 100)).toInt()

        if (totalSales >= valueRequiredToBreakEven) {
            return 0
        }

        val labelPayout = (totalSales * (labelPercentageBeforeBreakEven / 100)).toInt()
        return totalExpenses - labelPayout
    }

    fun calculateArtistPayout(totalSales: Int, outstandingExpenses: Int): Int {

        //Not a fully necessary branch, but just avoids unnecessary calculations when there are no outstanding expenses
        if (outstandingExpenses > 0) {
            return calculateArtistPayoutForUnsettledSales(totalSales, outstandingExpenses)
        }

        val labelPayout = (totalSales * (labelPercentageAfterBreakEven / 100)).toInt()
        return totalSales - labelPayout
    }

    private fun calculateArtistPayoutForUnsettledSales(totalSales: Int, outstandingExpenses: Int): Int {
        val valueRequiredToBreakEven = (outstandingExpenses / (labelPercentageBeforeBreakEven / 100)).toInt()

        if (valueRequiredToBreakEven > totalSales) {
            return (totalSales * (labelPercentageBeforeBreakEven / 100)).toInt()
        }

        val beforeBreakEven = (valueRequiredToBreakEven * (artistPercentageBeforeBreakEven() / 100)).toInt()

        val remaining = totalSales - valueRequiredToBreakEven

        val afterBreakEven = (remaining * (artistPercentageAfterBreakEven() / 100)).toInt()

        return beforeBreakEven + afterBreakEven
    }

    private fun artistPercentageBeforeBreakEven(): Float = 100f - labelPercentageBeforeBreakEven

    private fun artistPercentageAfterBreakEven(): Float = 100f - labelPercentageAfterBreakEven
}