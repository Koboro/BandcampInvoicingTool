package io

import Split
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class SplitAdapter: JsonDeserializer<Split> {

    override fun deserialize(element: JsonElement?, type: Type?, context: JsonDeserializationContext?): Split {

        if (element == null) {
            throw Exception("Cannot deserialize Split object - element is null!")
        }

        val splitEntries = element.asJsonObject
            .asMap()
            .map { Pair(it.key, it.value.asFloat) }
            .toTypedArray()

        return Split.customSplit(*splitEntries)
    }

}