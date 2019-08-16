package com.williambl.raycastengine.gameobject

class Light(var x: Double, var y: Double, var strength: Triple<Double, Double, Double>) {

    constructor(x: Double, y: Double, strength: Double) : this(x, y, Triple(strength, strength, strength))
}