package com.williambl.raycastengine.world

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.getObjectFromJson
import com.williambl.raycastengine.render.Texture

class DefaultWorldFileInterpreter: WorldFileInterpreter {

    override fun interpretWorldFile(json: JsonObject): World {
        val mapArray = json.array<JsonArray<Int>>("map")

        val world = DefaultWorld(mapArray!!.map { it.toIntArray() }.toTypedArray())

        val worldProperties = json.obj("worldProperties")
        if (worldProperties != null) {
            val floorColor = worldProperties.array<Float>("floorColor")
            val skyColor = worldProperties.array<Float>("skyColor")
            if (floorColor != null)
                world.floorColor = Triple(floorColor[0], floorColor[1], floorColor[2])
            if (skyColor != null)
                world.skyColor = Triple(skyColor[0], skyColor[1], skyColor[2])
        }

        world.wallTextures = json.array<String>("wallTextures")!!.map {
            Texture(it)
        }.toTypedArray()

        //TODO: change this so it works out ctor itself
        val gameObjects = json.array<JsonObject>("gameObjects")
        if (gameObjects != null) {
            for (gameObjectRepresentation in gameObjects) {
                val args = gameObjectRepresentation.array<JsonObject>("args")
                val gameObject = getObjectFromJson(gameObjectRepresentation) as GameObject?
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