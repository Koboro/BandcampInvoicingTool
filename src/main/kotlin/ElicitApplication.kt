import catalogue.*
import csv.BandcampSalesReportLineParser
import csv.BandcampSalesReportReader
import payout.ItemDetails
import sales.SalesReport
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.Month

val Elicit_Standard_Contract = SimpleContract(100f, 50f, 100f, 50f)

fun main() {
    val lunar = Release(
        catNo = "ELCT01",
        digitalPriceMap = mapOf(LocalDate.of(2023, Month.OCTOBER, 14) to 700),
        tracks = setOf(
            Track("Lunar", Split.singleSplit("Kessler")),
            Track("Hiyah", Split.singleSplit("Kessler")),
            Track("F**k Off With Your Horoscopes", Split.singleSplit("Kessler")),
            Track("Black Sky", Split.singleSplit("Kessler")),
            Track("F**k Off Your Horoscopes", Split.singleSplit("Kessler"))

        ),
        contract = Elicit_Standard_Contract,
        expenses = mutableListOf(
            Expense(500_00, "artwork"),
            Expense(209_00, "mastering"),
            Expense(1538_10, "press")
        ),
    )
    val connected_01 = Release(
        catNo = "ELCTVA01",
        digitalPriceMap = mapOf(LocalDate.of(2023, Month.DECEMBER, 21) to 9_99),
        tracks = setOf(
            Track("Feels (Early Hours Mix)", Split.evenSplit("Bex","LOOR")),
            Track("Delos", Split.singleSplit("Destrata")),
            Track("Tyd", Split.singleSplit("Leese")),
            Track("Save The Number", Split.singleSplit("Qant")),
            Track("Leech", Split.evenSplit("SSSLIP","Dom Carlo")),
            Track("Proto", Split.singleSplit("Portway")),
            Track("Un territorio o una substancia", Split.singleSplit("DJ LOUI FROM JUPITER4")),
            Track("Ukiyo", Split.singleSplit("Kenshō")),
            Track("Duhh", Split.singleSplit("Jason Code")),
            Track("Brain Displacement", Split.singleSplit("Kessler")),
            Track("Whut 4", Split.singleSplit("Joey")),
            Track("Call Of The Sea", Split.singleSplit("FTL"))


        ),
        contract = Elicit_Standard_Contract,
        expenses = mutableListOf(
            Expense(400_00, "artwork"),
            Expense(360_00,"Mastering"),


        ),
    )
    val tenSixty = Release(
        catNo = "ELCT02",
        digitalPriceMap = mapOf(LocalDate.of(2024, Month.JUNE, 13) to 700),
        tracks = setOf(
            Track("Inpression", Split.singleSplit("Joey")),
            Track("In For A Dime", Split.singleSplit("Joey")),
            Track("Rendr", Split.singleSplit("Joey")),
            Track("Test Me", Split.singleSplit("Joey")),
            Track("Hand It Over", Split.singleSplit("Joey")),
            Track("Hand It Over (Octoptic Remix)", Split.singleSplit("Octoptic")),

        ),
        contract = Elicit_Standard_Contract,
        expenses = mutableListOf(
            Expense(360_00, "artwork"),
            Expense(150_00,"mastering")


        ),
    )
    val hypa = Release(
        catNo = "ELCT03",
        digitalPriceMap = mapOf(LocalDate.of(2024, Month.JULY, 18) to 650),
        tracks = setOf(
            Track("Hypa", Split.evenSplit("Aloka", "Dave N.A.")),
            Track("Red Line", Split.singleSplit("Dave N.A.")),
            Track("Cicada", Split.singleSplit("Aloka")),
            Track("BBQ & Vodka", Split.evenSplit("Aloka","Dave N.A.")),

        ),
        contract = Elicit_Standard_Contract,
        expenses = mutableListOf(
            Expense(300_00, "artwork"),
            Expense(122_47, "mastering"),
            Expense(1538_10, "press"),
            Expense(100, "Ad spend")

        ),
    )
    val transientCurse = Release(
        catNo = "ELCT04",
        digitalPriceMap = mapOf(LocalDate.of(2024, Month.SEPTEMBER, 23) to 700),
        tracks = setOf(
            Track("Transient Curse", Split.singleSplit("Mathis Ruffing")),
            Track("Azul Nube", Split.singleSplit("Mathis Ruffing")),
            Track("Silt Strider", Split.singleSplit("Mathis Ruffing")),
            Track("Outbound", Split.singleSplit("Mathis Ruffing")),

        ),
        contract = Elicit_Standard_Contract,
        expenses = mutableListOf(
            Expense(300_00, "artwork"),
            Expense(100_00, "mastering"),
        ),
    )
    val Connected_02 = Release(
        catNo = "ELCTVA02",
        digitalPriceMap = mapOf(LocalDate.of(2025, Month.JANUARY, 14) to 9_99),
        tracks = setOf(
            Track("Raster", Split.singleSplit("Sam Link")),
            Track("Spooky Emote", Split.singleSplit("Joey")),
            Track("Altered States", Split.singleSplit("Martini")),
            Track("Side Mission", Split.singleSplit("Dom Carlo")),
            Track("AOAC", Split.singleSplit("Nouveau Monica")),
            Track("Arekstep", Split.singleSplit("Murteza")),
            Track("Pull Up", Split.singleSplit("Shai FM")),
            Track("Hurting My Feet", Split.singleSplit("Cardozo")),
            Track("3zN", Split.singleSplit("Portway")),
            Track("Seoul City Drift", Split.singleSplit("Dual Monitor")),
            Track("unexpctd item in the bubbling area", Split.singleSplit("Pépe")),
            Track("Changing Pages", Split.singleSplit("Ziyiz")),


            ),
        contract = Elicit_Standard_Contract,
        expenses = mutableListOf(
            Expense(300_00, "artwork"),
            Expense(300_00, "mastering"),
        ),)
val catalogue=Catalogue()
    // Add as many releases to your catalogue as you wish
    catalogue.addReleaseToCatalog(lunar)
    catalogue.addReleaseToCatalog(connected_01)
    catalogue.addReleaseToCatalog(tenSixty)
    catalogue.addReleaseToCatalog(hypa)
    catalogue.addReleaseToCatalog(transientCurse)
    catalogue.addReleaseToCatalog(Connected_02)


    /*
    Step 2 - Read your Bandcamp sales data
     */

    val reader = BandcampSalesReportReader(BandcampSalesReportLineParser())
    // This is an example location, replace with your actual Bandcamp sales report CSV location
    val salesReportData = reader.read("/Users/lee/Downloads/20230701-20250122_bandcamp_raw_data_Elicit-Records.csv")
    val salesReport = SalesReport(salesReportData)

    // Once the catalogue has been loaded with the sales data you may now perform any calculations on it!
    catalogue.provideSalesData(salesReport)

    /*
    Step 3 - Calculate artist payouts
     */

    // Define the start and end dates of the time period you would like to calculate payouts for
    // The start date is inclusive (will be included in the calculations)
    val dateToGoFrom = LocalDate.of(2023, Month.AUGUST, 22)
    // Then end date is exclusive (will not be included in the calculations - just everything up until this date)
    val dateToGoTo = LocalDate.of(2025, Month.JANUARY, 22)

    val payouts = catalogue.calculatePayouts(dateToGoFrom, dateToGoTo)
    println("Payouts from $dateToGoFrom:")
    val decimalFormat = DecimalFormat("#,###.##")

    println("\nOverview:")
    val bandcampPayouts = payouts
        .filterNot { it.saleInformation.bandcampTransactionId == null }
        .distinctBy { it.saleInformation.bandcampTransactionId + it.itemDetails }
    val totalGrossSaleValueFromBandcamp = bandcampPayouts.sumOf { it.saleInformation.totalGrossValue }
    println("\tBandcamp sales (gross):  ${formatCurrency(totalGrossSaleValueFromBandcamp)}")
    val totalNetSaleValueFromBandcamp = bandcampPayouts.sumOf { it.saleInformation.totalNetValue }
    println("\tBandcamp sales (net): ${formatCurrency(totalNetSaleValueFromBandcamp)}")

    val externalSaleValue = payouts
        .filter { it.saleInformation.bandcampTransactionId == null }
        .sumOf { it.saleInformation.netValue }
    println("\tExternal sales: ${formatCurrency(externalSaleValue)}")


    val totalPayoutValue = payouts.sumOf { it.getTotalPayoutValue() }
    println("\tTotal payouts: ${formatCurrency(totalPayoutValue)}")
    val totalArtistPayouts = payouts.sumOf { it.amount }
    println("\tTotal artist payout: ${formatCurrency(totalArtistPayouts)}")
    val totalLabelPayout = payouts.sumOf { it.getLabelPayoutValue() }
    println("\tTotal label payout: ${formatCurrency(totalLabelPayout)}")
    println()

    val payoutsByArtist = payouts.groupBy { it.artist }.toSortedMap()
    payoutsByArtist.forEach { payoutByArtist ->
        val artistPayout = payoutByArtist.value.sumOf { it.amount }
        val labelRecoupValue = payoutByArtist.value.sumOf { it.getLabelPayoutValue() }.let { if (it > 0) " (${formatCurrency(it)} recouped)" else ""}
        println("\t${payoutByArtist.key} -> ${formatCurrency(artistPayout)}$labelRecoupValue")
    }
    println()

    payouts.groupBy { it.artist }.forEach { byArtistEntry ->
        val totalArtistPayoutValue = byArtistEntry.value.sumOf { it.amount }
        println("Artist: ${byArtistEntry.key} -> ${formatCurrency(totalArtistPayoutValue)}")
        println("\t- - - Breakdown - - -")
        byArtistEntry.value.groupBy { it.itemDetails }.toSortedMap( compareBy<ItemDetails> { it.itemType }.thenBy { it.itemName }).forEach { byItemNameEntry ->
            val totalValueOfItemPayout = byItemNameEntry.value.sumOf { it.amount }
            val netValue = byItemNameEntry.value.sumOf { it.saleInformation.netValue }
            val grossValue = byItemNameEntry.value.sumOf { it.saleInformation.grossValue }
            val totalValueOfLabelRecoup = byItemNameEntry.value.sumOf { it.getLabelPayoutValue() }

            val recoup = if (totalValueOfLabelRecoup > 0) " (${formatCurrency(totalValueOfLabelRecoup)} recouped from expenses)" else ""

            println("\t[${byItemNameEntry.key.itemType}] ${byItemNameEntry.key.itemName} - ${formatCurrency(totalValueOfItemPayout)}" +
                    "\n\t\t${byItemNameEntry.value.size} sale(s): ${formatCurrency(netValue)} net, ${formatCurrency(grossValue)} gross$recoup")
        }
        println("")
    }
}
private fun formatCurrency(value: Int): String {
    val decimalFormat = DecimalFormat("#,###.##")

    return "€${decimalFormat.format(value.toBigDecimal().divide(BigDecimal(100)))}"
}