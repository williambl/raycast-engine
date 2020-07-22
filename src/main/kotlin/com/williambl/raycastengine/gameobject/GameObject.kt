package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.util.network.readUUID
import com.williambl.raycastengine.util.network.writeUUID
import com.williambl.raycastengine.util.synced
import com.williambl.raycastengine.world.World
import io.netty.buffer.ByteBuf
import java.util.*

open class GameObject(xIn: Double = 0.0, yIn: Double = 0.0) {

    var x: Double by synced(xIn, ::id, ByteBuf::writeDouble, ByteBuf::readDouble)
    var y: Double by synced(yIn, ::id, ByteBuf::writeDouble, ByteBuf::readDouble)

    lateinit var world: World

    open var id: UUID = UUID.randomUUID()

    open fun toBytes(byteBuf: ByteBuf) {
        byteBuf.writeUUID(id)
        byteBuf.writeDouble(x)
        byteBuf.writeDouble(y)
    }

    open fun fromBytes(byteBuf: ByteBuf) {
        id = byteBuf.readUUID()
        x = byteBuf.readDouble()
        y = byteBuf.readDouble()
    }

    open fun toJson(json: JsonObject) {
        json["id"] = id.toString()
        json["x"] = x
        json["y"] = y
    }

    open fun fromJson(json: JsonObject) {
        id = UUID.fromString(json["uuid"].toString())
        x = json["x"] as Double
        y = json["y"] as Double
    }
}