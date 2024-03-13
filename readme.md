# Bandcamp Invoicing SDK

The Bandcamp Invoicing SDK is a library that provides functionality for calculating artist-label shares.

The library reads data from Bandcamp sales report CSV files.

After providing information about your Bandcamp catalogue, sales data can be applied to calculate
summed artist payout shares for a given timeframe.

## Helpful Features 

###  Recoupable Expense Support
A contract is declared for each release stating the label/artist share before & after break-even.
Expenses can be added when declaring your release. These are included in share calculations, meaning you don't need to 
manage these costs outside of the tool and subtract the values from the artist's payout.

### Custom Splits
Splits can be declared for tracks that indicate the percentage that should be given to contributing artists. Splits are declared _per track_, and therefore it is just as easy to calculate artist shares for albums with a single artist, compilation albums, and collaborations.

### Digital Discography Support
Calculates the cost of the digital discography bundle deals and distributes the value of the sale proportionally between the releases included. E.g., if there were 3 releases for sale at £10, £5, and £5, the discography bundle sale would distribute 50% of the sale to the release worth £10, and 25% to the other two.

## Example application with code comment explanations

```kotlin
import csv.BandcampSalesReportLineParser
import csv.BandcampSalesReportReader
import sales.SalesReport
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.Month
import kotlin.to

fun main() {

    /*
    Step 1 - Define your Catalogue
     */

    // Single split is used when royalties go entirely to a single artist
    val track1 = Track("Antidote", Split.singleSplit("Travis Scott"))
    // Even split is used when royalties are to be evenly split between the contributing artists
    val track3 = Track("Maria I'm Drunk", Split.evenSplit("Travis Scott", "Justin Bieber", "Young Thug"))
    // Custom split is used when you want to define custom royalty percentages
    val track2 = Track("Sicko Mode", Split.customSplit("Travis Scott" to 60f, "Drake" to 40f))

    val expense1 = Expense(100_00, "Digital track mastering")
    val expense2 = Expense(65_50, "Artwork")

    // You can manually define sales that occurred outside of Bandcamp here to ensure the payout calculation includes these
    // It is important to include these as these sales will also be accounted for when working out expenses to be paid off / break even point
    val sales = mutableListOf(
        40_00 to LocalDate.of(2020, Month.MARCH, 15)
    )

    val release = Release(
        // This category number MUST match with the one on the release on Bandcamp
        catNo = "LABEL001",
        // Prices and the date that the price is set. This is used for digital discography calculations
        // The first entry should be the date it went on sale and the price at that time
        prices = mapOf(Pair(LocalDate.of(2020, Month.MARCH, 1), 5_00)),
        // All the tracks
        tracks = setOf(track1, track2, track3),
        // Defines the percentage that the label takes on sales before and after breaking even
        contract =  Contract(60f, 40f),
        // Any associates expenses for the release
        expenses = mutableListOf(expense1, expense2),
        // Any sales outside of Bandcamp that you would like to manually include
        sales = sales,
        // Optional property - important to set if you have removed the release from sale from bandcamp, otherwise this will be factored into digital discography sales
        // Do not set or set this value to `null` if the release is still for sale
        salesStopDate = null
    )

    val catalogue = Catalogue()

    // Add as many releases to your catalogue as you wish
    catalogue.addReleaseToCatalog(release)

    /*
    Step 2 - Read your Bandcamp sales data
     */

    val reader = BandcampSalesReportReader(BandcampSalesReportLineParser())
    // This is an example location, replace with your actual Bandcamp sales report CSV location
    val salesReportData = reader.read("C:\\Users\\user\\Documents\\bandcampsales.csv")
    val salesReport = SalesReport(salesReportData)

    // Once the catalogue has been loaded with the sales data you may now perform any calculations on it!
    catalogue.provideSalesData(salesReport)

    /*
    Step 3 - Calculate artist payouts
     */

    // Define the start and end dates of the time period you would like to calculate payouts for
    // The start date is inclusive (will be included in the calculations)
    val dateToGoFrom = LocalDate.of(2024, Month.FEBRUARY, 1)
    // Then end date is exclusive (will not be included in the calculations - just everything up until this date)
    val dateToGoTo = LocalDate.of(2024, Month.MARCH, 1)

    val payouts = catalogue.calculatePayouts(dateToGoFrom, dateToGoTo)

    /*
     The following code is just an example use of how you can use the data

     This simply prints the artist names on the left & their respective payout on the right, with the payout value formatted to GBP

     Example output for the release above
     "
     Travis Scott -> £41.45
     Drake -> £12.78
     Justin Bieber -> £6.40
     Young Thug -> £6.40
     "
     */

    println("Payouts from $dateToGoFrom:")
    val decimalFormat = DecimalFormat("#,###.##")

    payouts.forEach { entry -> println("\t${entry.key} -> £${decimalFormat.format(entry.value.toBigDecimal().divide(
        BigDecimal(100)
    ))}")}
}

```