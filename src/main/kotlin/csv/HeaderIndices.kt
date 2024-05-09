package csv

data class HeaderIndices(
    val itemTypeIndex: Int,
    val itemNameIndex: Int,
    val catNoIndex: Int,
    val dateIndex: Int,
    val netAmountIndex: Int,
    val packageNameIndex: Int,
    val subTotalIndex: Int,
    val additionalFanContributionIndex: Int,
    val bandcampTransactionIdIndex: Int,
    val totalHeaders: Int
) {
    companion object {
        private const val HEADER_ITEM_TYPE = "item type"
        private const val HEADER_ITEM_NAME = "item name"
        private const val HEADER_CAT_NO = "catalog number"
        private const val HEADER_DATE = "date"
        private const val HEADER_NET_AMOUNT = "net amount"
        private const val HEADER_PACKAGE_NAME = "package"

        private const val HEADER_SUB_TOTAL = "sub total"
        private const val HEADER_ADDITIONAL_FAN_CONTRIBUTION = "additional fan contribution"
        private const val HEADER_BANDCAMP_TRANSACTION_ID = "bandcamp transaction id"

        fun from(headerLine: String): HeaderIndices {
            val headerIndicesMap = headerLine
                .replace("\u0000", "") // TODO better way to achieve this?
                .replace("�", "")
                .split(",")
                .mapIndexed { index, columnHeader -> columnHeader.trim().lowercase() to index }
                .toMap()

            return HeaderIndices(
                itemTypeIndex = getHeaderIndex(headerIndicesMap, HEADER_ITEM_TYPE) ?: throw Exception("Could not create header indices. No column for item type"),
                itemNameIndex = getHeaderIndex(headerIndicesMap, HEADER_ITEM_NAME) ?: throw Exception("Could not create header indices. No column for item name"),
                catNoIndex = getHeaderIndex(headerIndicesMap, HEADER_CAT_NO) ?: throw Exception("Could not create header indices. No column for category number"),
                dateIndex = getHeaderIndex(headerIndicesMap, HEADER_DATE) ?: throw Exception("Could not create header indices. No column for date"),
                netAmountIndex = getHeaderIndex(headerIndicesMap, HEADER_NET_AMOUNT) ?: throw Exception("Could not create header indices. No column for net amount"),
                packageNameIndex = getHeaderIndex(headerIndicesMap, HEADER_PACKAGE_NAME) ?: throw Exception("Could not create header indices. No column for package"),
                subTotalIndex = getHeaderIndex(headerIndicesMap, HEADER_SUB_TOTAL) ?: throw Exception("Could not create header indices. No column for sub total"),
                additionalFanContributionIndex = getHeaderIndex(headerIndicesMap, HEADER_ADDITIONAL_FAN_CONTRIBUTION) ?: throw Exception("Could not create header indices. No column for additional fan contribution"),
                bandcampTransactionIdIndex = getHeaderIndex(headerIndicesMap, HEADER_BANDCAMP_TRANSACTION_ID) ?: throw Exception("Could not create header indices. No column for bandcamp transaction ID"),
                totalHeaders = headerIndicesMap.size
            )
        }

        private fun getHeaderIndex(headerIndices: Map<String, Int>, header: String): Int {
            return headerIndices[header] ?: throw Exception("Could not create header indices. No column for \"$header\"")
        }
    }
}