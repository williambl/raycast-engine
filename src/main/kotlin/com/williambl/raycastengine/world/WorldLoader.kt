package com.williambl.raycastengine.world

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.williambl.raycastengine.Main

/**
 * The World Loader handles [World Serializers][WorldSerializer], and the loading of JSON-worlds from disk.
 */
object WorldLoader {

    val worldSerializerClasses: Map<String, Class<out WorldSerializer>> = Main.reflections.getSubTypesOf(WorldSerializer::class.java).map {
        it.name to it
    }.toMap()

    fun getSerializer(className: String): WorldSerializer {
        val clazz = (worldSerializerClasses[className] ?: DefaultWorld.Serializer::class.java)
        return clazz.kotlin.objectInstance ?: clazz.getDeclaredConstructor().newInstance()
    }

    fun load(worldFile: String): World {
        val json = Parser.default().parse(this::class.java.getResourceAsStream(worldFile).reader()) as JsonObject
        return getSerializer(json.string("interpreter") ?: "").fromJson(json)
    }

}