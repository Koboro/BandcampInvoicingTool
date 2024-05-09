package sales

import java.time.LocalDate

interface SaleItem {
    val bandcampTransactionId: String
    val netValue: Int
    val grossValue: Int
    val dateTime: LocalDate
}