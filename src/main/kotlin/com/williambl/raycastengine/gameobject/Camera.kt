package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.util.network.readDoublePair
import com.williambl.raycastengine.util.network.writeDoublePair
import io.netty.buffer.ByteBuf

open class Camera(x: Double = 0.0, y: Double = 0.0) : GameObject(x, y) {

    var plane = Pair(0.0, 0.66)
    var dir = Pair(-1.0, 0.0)

    override fun toBytes(byteBuf: ByteBuf) {
        super.toBytes(byteBuf)
        byteBuf.writeDoublePair(plane)
        byteBuf.writeDoublePair(dir)
    }

    override fun fromBytes(byteBuf: ByteBuf) {
        super.fromBytes(byteBuf)
        plane = byteBuf.readDoublePair()
        dir = byteBuf.readDoublePair()
    }
}