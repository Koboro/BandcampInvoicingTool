class Split private constructor(splitEntries: Set<Pair<String, Float>>) : HashMap<String, Float>() {

    companion object {

        fun singleSplit(artist: String) = Split(setOf(artist to 100f))

        fun evenSplit(vararg artists: String): Split {
            val percentageEach = 100f / artists.size

            val entries = artists.map { it to percentageEach }.toSet()
            return Split(entries)
        }

        fun customSplit(vararg splits: Pair<String, Float>) = Split(splits.toSet())
    }

    init {
        val total = splitEntries
            .map { it.second }
            .reduce { acc, next -> acc + next}

        if (total > 100 || total < 99) {
            throw Exception("Cannot create split. Total split values not equal to 100")
        }

        putAll(splitEntries)
    }
}