package com.williambl.raycastengine.gameobject

open class Camera(x: Double, y: Double) : GameObject(x, y) {

    var plane = Pair(0.0, 0.66)
    var dir = Pair(-1.0, 0.0)

}