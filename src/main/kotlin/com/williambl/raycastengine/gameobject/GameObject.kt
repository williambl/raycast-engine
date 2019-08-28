package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.World

open class GameObject(var x: Double, var y: Double) {

    lateinit var world: World
}