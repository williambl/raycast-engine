package com.williambl.raycastengine

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

class WorldLoader(val worldFile: String) {

    fun load(): World {
        val json = Parser.default().parse(this::class.java.getResource(worldFile).path) as JsonObject

        val array = json.array<JsonArray<Int>>("map")

        return World(array!!.map { it.toIntArray() }.toTypedArray())
    }

}