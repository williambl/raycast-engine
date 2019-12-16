package com.williambl.raycastengine.world

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.williambl.raycastengine.Main

class WorldLoader(val worldFile: String) {

    val worldFileInterpreterClasses: Map<String, Class<out WorldFileInterpreter>> = Main.reflections.getSubTypesOf(WorldFileInterpreter::class.java).map {
        it.name to it
    }.toMap()

    fun load(): World {
        val json = Parser.default().parse(this::class.java.getResourceAsStream(worldFile).reader()) as JsonObject

        var interpreter = worldFileInterpreterClasses[json.string("interpreter")]
        if (interpreter == null) {
            println(json.string("interpreter") + " is not a valid world interpreter, going with default")
            interpreter = DefaultWorldFileInterpreter::class.java
        }
        return try {
            interpreter.newInstance().interpretWorldFile(json)
        } catch (e: InstantiationException) {
            println(json.string("interpreter") + " is not a valid world interpreter, going with default")
            DefaultWorldFileInterpreter().interpretWorldFile(json)
        } catch (e: IllegalAccessException) {
            println(json.string("interpreter") + " is not a valid world interpreter, going with default")
            DefaultWorldFileInterpreter().interpretWorldFile(json)
        }
    }

}