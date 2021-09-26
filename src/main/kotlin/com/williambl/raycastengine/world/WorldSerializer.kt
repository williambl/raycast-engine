package com.williambl.raycastengine.world

import com.beust.klaxon.JsonObject
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * A World Serializer is responsible for the serialization of [worlds][World] to and from JSON and Binary formats. This
 * is needed for saving and loading worlds to disk (by the [WorldLoader]), and for syncing the game state over the
 * network.
 */
interface WorldSerializer {
    fun fromJson(json: JsonObject): World
    fun fromBytes(buf: ByteBuf): World
    fun toJson(world: World, json: JsonObject)
    fun toBytes(world: World, buf: ByteBuf, destinationId: UUID?)
}