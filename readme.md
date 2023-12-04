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

