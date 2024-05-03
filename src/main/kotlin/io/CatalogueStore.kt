package io

import catalogue.Split
import catalogue.Catalogue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.time.LocalDate

class CatalogueStore {

    companion object {
        private const val FILE_TYPE = "bcif"
        private const val PATH = "catalogue.$FILE_TYPE"
    }

    fun saveCatalogue(catalogue: Catalogue) {
        val json = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()
            .toJson(catalogue)

        File(PATH).printWriter().use { it.println(json) }
    }

    fun readCatalogue(): Catalogue {
        val json = File(PATH).readText()

        return Gson().newBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(Split::class.java, SplitAdapter())
            .create()
            .fromJson(json, Catalogue::class.java)
    }
}