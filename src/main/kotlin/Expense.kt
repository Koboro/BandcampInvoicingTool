import java.time.LocalDate

/**
 * Represents an associated release expense, e.g. the cost of mastering a track.
 * This can be used to track label expenditure on a release and be included in payout calculations.
 */
data class Expense(
    /**
     * The value in minor amount - e.g. £1 = `100`
     */
    val value: Int,
    /**
     * An optional description of the expense - e.g. `"Track Mastering"`
     */
    private val description: String,
    /**
     * An optional date associated with the expense.
     * Used in payout calculation to ensure that expense values are applied at the correct point in time.
     * Null value implies this is a general expense, so should be applied to calculations from the very beginning of sales.
     */
    val date: LocalDate? = null
)

fun List<Expense>.getExpenseValueUpTo(until: LocalDate): Int {

    return this.filter { it.date?.isBefore(until) ?: true }.sumOf { it.value }
}

fun List<Expense>.getTotalExpenseValueBetweenDates(from: LocalDate, untilInclusive: LocalDate): Int {

    return this.filter { it.date != null  && it.date.isAfter(from) && (it.date.isEqual(untilInclusive) || it.date.isBefore(untilInclusive)) }.sumOf { it.value }
}