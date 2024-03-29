package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.Tickable
import kotlin.random.Random

class MovingSprite(textureLoc: String, x: Double, y: Double) : Sprite(textureLoc, x, y), Tickable {

    val rand = Random(345323 + x.toInt() + y.toInt())

    override fun tick() {
        val moveX = rand.nextDouble(0.2) - 0.1
        val moveY = rand.nextDouble(0.2) - 0.1

        if (Main.world.map[(x + moveX).toInt()][y.toInt()] == 0)
            x += moveX
        if (Main.world.map[x.toInt()][(y + moveY).toInt()] == 0)
            y += moveY
    }
}