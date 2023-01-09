package csv

data class HeaderIndices(
    val itemTypeIndex: Int,
    val itemNameIndex: Int,
    val catNoIndex: Int,
    val dateIndex: Int,
    val netAmountIndex: Int
) {
    companion object {
        private const val HEADER_ITEM_TYPE = "item type"
        private const val HEADER_ITEM_NAME = "item name"
        private const val HEADER_CAT_NO = "catalog number"
        private const val HEADER_DATE = "date"
        private const val HEADER_NET_AMOUNT = "net amount"

        fun from(headerLine: String): HeaderIndices {
            val headerIndicesMap = headerLine
                .replace("\u0000", "") // TODO better way to achieve this?
                .replace("�", "")
                .split(",")
                .mapIndexed { index, columnHeader -> columnHeader.trim().lowercase() to index }
                .toMap()

            return HeaderIndices(
                itemTypeIndex = headerIndicesMap[HEADER_ITEM_TYPE] ?: throw Exception("Could not create header indices. No column for item type"),
                itemNameIndex = headerIndicesMap[HEADER_ITEM_NAME] ?: throw Exception("Could not create header indices. No column for item name"),
                catNoIndex = headerIndicesMap[HEADER_CAT_NO] ?: throw Exception("Could not create header indices. No column for category number"),
                dateIndex = headerIndicesMap[HEADER_DATE] ?: throw Exception("Could not create header indices. No column for date"),
                netAmountIndex = headerIndicesMap[HEADER_NET_AMOUNT] ?: throw Exception("Could not create header indices. No column for net amount")
            )
        }
    }
}