package com.williambl.raycastengine.world

import com.beust.klaxon.JsonObject

interface WorldFileInterpreter {
    fun interpretWorldFile(json: JsonObject): World
}