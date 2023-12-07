package io

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate

class LocalDateAdapter: TypeAdapter<LocalDate>() {

    override fun write(writer: JsonWriter?, localDate: LocalDate?) {
        if (localDate == null) {
            writer?.nullValue()
            return
        }
        writer?.value(localDate.toString())
    }

    override fun read(reader: JsonReader?): LocalDate? {
        if (reader?.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        return LocalDate.parse(reader?.nextString())
    }

}