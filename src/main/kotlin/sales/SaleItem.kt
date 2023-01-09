package sales

import java.time.LocalDate

interface SaleItem {
    val catNo: String
    val value: Int
    val dateTime: LocalDate
}