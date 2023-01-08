package sales

import java.time.LocalDateTime

interface SaleItem {
    val catNo: String
    val value: Int
    val dateTime: LocalDateTime
}