package sales

import java.time.LocalDate

interface SaleItem {
    val netValue: Int
    val grossValue: Int
    val date: LocalDate
}