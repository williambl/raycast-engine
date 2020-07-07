package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.readDoubleTriple
import com.williambl.raycastengine.writeDoubleTriple
import io.netty.buffer.ByteBuf

class Light(x: Double = 0.0, y: Double, var strength: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0)) : GameObject(x, y) {

    constructor(x: Double, y: Double, strength: Double) : this(x, y, Triple(strength, strength, strength))

    constructor(x: Double, y: Double, strengthR: Double, strengthG: Double, strengthB: Double) : this(x, y, Triple(strengthR, strengthG, strengthB))

    override fun toBytes(byteBuf: ByteBuf) {
        super.toBytes(byteBuf)
        byteBuf.writeDoubleTriple(strength)
    }

    override fun fromBytes(byteBuf: ByteBuf) {
        super.fromBytes(byteBuf)
        strength = byteBuf.readDoubleTriple()
    }
}