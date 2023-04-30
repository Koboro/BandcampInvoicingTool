package csv

import sales.SaleItem
import java.io.File

class BandcampSalesReportReader(
    private val bandcampSalesReportLineParser: BandcampSalesReportLineParser
) {

    fun read(location: String): List<SaleItem> {
        return File(location).inputStream().use {
            val reader = it.bufferedReader(Charsets.UTF_16)

            val headerIndices = HeaderIndices.from(reader.readLine())

            val sales = mutableListOf<SaleItem>()
            reader.forEachLine { line -> bandcampSalesReportLineParser.parse(line, headerIndices)?.let { saleItem -> sales.add(saleItem) } }

            sales
        }
    }
}