package com.williambl.raycastengine.gameobject

import io.netty.buffer.Unpooled

class RemotePlayer(x: Double = 0.0, y: Double = 0.0) : Player(x, y) {

    override fun tick() {}

    fun toPlayer(): Player {
        val player = Player()
        val buf = Unpooled.buffer()
        toBytes(buf)
        player.fromBytes(buf)
        buf.release()
        return player
    }
}