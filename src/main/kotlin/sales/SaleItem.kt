package sales

import java.time.LocalDate

interface SaleItem {
    val value: Int
    val dateTime: LocalDate
}