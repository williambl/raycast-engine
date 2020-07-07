package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.readUUID
import com.williambl.raycastengine.world.World
import com.williambl.raycastengine.writeUUID
import io.netty.buffer.ByteBuf
import java.util.*

open class GameObject(var x: Double = 0.0, var y: Double = 0.0) {

    lateinit var world: World

    open var id: UUID = UUID.randomUUID()

    var isDirty = false

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
}