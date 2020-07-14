package com.williambl.raycastengine.world

import com.beust.klaxon.JsonObject
import io.netty.buffer.ByteBuf
import java.util.*

interface WorldSerializer {
    fun fromJson(json: JsonObject): World
    fun fromBytes(buf: ByteBuf): World
    fun toJson(world: World, json: JsonObject)
    fun toBytes(world: World, buf: ByteBuf, destinationId: UUID?)
}