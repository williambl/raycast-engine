package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.readUUID
import com.williambl.raycastengine.world.World
import com.williambl.raycastengine.writeUUID
import io.netty.buffer.ByteBuf
import java.util.*

open class GameObject(var x: Double = 0.0, var y: Double = 0.0) {

    lateinit var world: World

    open var id: UUID = UUID.randomUUID()

    //TODO: some system that auto-sets this
    var isDirty = false

    fun setPos(xIn: Double, yIn: Double) {
        x = xIn
        y = yIn
        isDirty = true
    }

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