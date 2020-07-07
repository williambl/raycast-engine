package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.world.World
import java.util.*

open class GameObject(var x: Double, var y: Double) {

    lateinit var world: World

    open var id: UUID = UUID.randomUUID()
}