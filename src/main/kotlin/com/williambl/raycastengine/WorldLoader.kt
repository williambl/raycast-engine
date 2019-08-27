package com.williambl.raycastengine

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

class WorldLoader(val worldFile: String) {

    fun load(): World {
        val json = Parser.default().parse(this::class.java.getResource(worldFile).path) as JsonObject

        val mapArray = json.array<JsonArray<Int>>("map")

        val world = World(mapArray!!.map { it.toIntArray() }.toTypedArray())

        val gameObjects = json.array<JsonObject>("gameObjects")
        if (gameObjects != null) {
            for (gameObjectRepresentation in gameObjects) {
                val gameObject = world.createGameObject(
                        gameObjectRepresentation.string("class")!!,
                        gameObjectRepresentation.int("constructor") ?: 0,
                        *(gameObjectRepresentation.array<Any>("args")!!.toTypedArray())
                )
                if (gameObject == null) {
                    println(gameObjectRepresentation.string("class") + " is not a valid gameObject, skipping")
                    continue
                }

                world.addGameObject(gameObject)
            }
        }

        return world
    }

}