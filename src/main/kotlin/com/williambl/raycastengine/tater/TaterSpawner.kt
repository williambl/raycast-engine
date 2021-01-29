package com.williambl.raycastengine.tater

import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import kotlin.random.Random

class TaterSpawner : GameObject(0.0, 0.0), Tickable {
    var timer = 0

    override fun tick() {
        timer++
        if (timer % 60 == 0) {
            world.addGameObject(TaterNPC("/tater.png", "/irritater.png", Random.nextDouble(2.0, 8.0), Random.nextDouble(2.0, 8.0)))
        }
    }
}