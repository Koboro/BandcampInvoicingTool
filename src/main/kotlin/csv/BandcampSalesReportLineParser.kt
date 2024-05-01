package csv

import sales.BundleSale
import sales.ReleaseSale
import sales.SaleItem
import sales.TrackSale
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField

class BandcampSalesReportLineParser {

    private val dateTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
        .appendPattern("/")
        .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
        .appendPattern("/yy")
        .toFormatter()

    fun parse(line: String, headerIndices: HeaderIndices): SaleItem? {
        // Invalid line
        if (!line.contains(",")) return null
        // Line can be empty (except csv delimiters) when sale history ends before listing pending transactions
        if (line.all { it == ',' }) return null

        val formattedLine = line
            .replace("\u0000", "")
            .replace("�", "")

        val splitLine = formattedLine.
            split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

        if (splitLine.size != headerIndices.totalHeaders) {
            println("Error - line does not contain as many values (${splitLine.size}) as there are headers(${headerIndices.totalHeaders}). Manual intervention required for line: $formattedLine")
            return null
        }

        val itemType = splitLine[headerIndices.itemTypeIndex]
        when (itemType) {
            // Merch - todo
            "package" -> return null
            // Ignore payouts
            "payout", "pending sale" -> return null
        }

        val saleValue = splitLine[headerIndices.netAmountIndex].replace(".", "").toInt()
        // Gets only the date, ignore time
        val dateString = splitLine[headerIndices.dateIndex].split(" ")[0]
        val date = LocalDate.parse(dateString, dateTimeFormatter)

        return when (itemType) {
            "album" -> {
                val catNo = splitLine[headerIndices.catNoIndex]
                ReleaseSale(catNo, saleValue, date)
            }
            "track" -> {
                val catNo = splitLine[headerIndices.catNoIndex]
                val trackName = splitLine[headerIndices.itemNameIndex]
                TrackSale(catNo, trackName, saleValue, date)
            }
            "bundle" -> {
                val bundleName = splitLine[headerIndices.itemNameIndex]
                BundleSale(bundleName, saleValue, date)
            }
            else -> throw Exception("Unrecognised item type.")
        }
    }
}