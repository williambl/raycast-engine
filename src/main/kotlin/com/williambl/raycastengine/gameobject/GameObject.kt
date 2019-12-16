package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.world.World

open class GameObject(var x: Double, var y: Double) {

    lateinit var world: World
}