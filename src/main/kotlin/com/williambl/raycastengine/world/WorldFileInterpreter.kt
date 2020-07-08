package com.williambl.raycastengine.world

import com.beust.klaxon.JsonObject
import io.netty.buffer.ByteBuf

//TODO: turn this into a proper serialization thing
interface WorldFileInterpreter {
    fun interpretWorldFile(json: JsonObject): World
    fun fromBytes(buf: ByteBuf): World
}