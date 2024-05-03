package csv

import sales.*
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
            // Ignore payouts & pending sales
            "payout", "pending sale" -> return null
        }

        val netSaleValue = splitLine[headerIndices.netAmountIndex].replace(".", "").toInt()
        // Gets only the date, ignore time
        val dateString = splitLine[headerIndices.dateIndex].split(" ")[0]
        val date = LocalDate.parse(dateString, dateTimeFormatter)

        return when (itemType) {
            "album" -> {
                val catNo = splitLine[headerIndices.catNoIndex]
                ReleaseSale(catNo, netSaleValue, date)
            }
            "track" -> {
                val catNo = splitLine[headerIndices.catNoIndex]
                val trackName = splitLine[headerIndices.itemNameIndex]
                TrackSale(catNo, trackName, netSaleValue, date)
            }
            "bundle" -> {
                val bundleName = splitLine[headerIndices.itemNameIndex]

                if (bundleName.startsWith("full digital discography"))
                    DigitalDiscographySale(bundleName, netSaleValue, date)
                else
                    throw Exception("Unrecognised bundle \"${bundleName}\"")
            }
            "package" -> {
                val catNo = splitLine[headerIndices.catNoIndex]
                val packageName = splitLine[headerIndices.packageName]

                PhysicalSale(packageName, netSaleValue, date, catNo)
            }
            else -> throw Exception("Unrecognised item type.")
        }
    }
}