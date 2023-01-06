package sales

import java.time.LocalDateTime

interface SaleItem {
    val value: Int
    val dateTime: LocalDateTime
}