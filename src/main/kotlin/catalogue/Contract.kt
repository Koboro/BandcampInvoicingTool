package catalogue

import payout.ItemType

/**
 * Interface that defines how sale values are shared between the artist and the label.
 *
 * Custom definitions may be created for complex agreements, or the [SimpleContract] can be used for general use.
 */
interface Contract {
    fun calculateSaleShare(saleValue: Int, itemType: ItemType, outstandingExpenses: Int): Share
}