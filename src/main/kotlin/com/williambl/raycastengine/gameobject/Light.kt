package com.williambl.raycastengine.gameobject

class Light(x: Double, y: Double, var strength: Triple<Double, Double, Double>) : GameObject(x, y) {

    constructor(x: Double, y: Double, strength: Double) : this(x, y, Triple(strength, strength, strength))

    constructor(x: Double, y: Double, strengthR: Double, strengthG: Double, strengthB: Double) : this(x, y, Triple(strengthR, strengthG, strengthB))
}