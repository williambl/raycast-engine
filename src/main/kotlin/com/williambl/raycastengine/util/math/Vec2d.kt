package com.williambl.raycastengine.util.math

import kotlin.math.sqrt

/**
 * An immutable 2-dimensional double-precision vector.
 */
class Vec2d(val x: Double, val y: Double) {

    fun normalised(): Vec2d {
        val length = this.length()
        return Vec2d(x / length, y / length)
    }

    fun length(): Double {
        return sqrt(x * x + y * y)
    }

    operator fun plus(other: Vec2d): Vec2d {
        return Vec2d(x + other.x, y + other.y)
    }

    operator fun minus(other: Vec2d): Vec2d {
        return Vec2d(x + other.x, y + other.y)
    }
}