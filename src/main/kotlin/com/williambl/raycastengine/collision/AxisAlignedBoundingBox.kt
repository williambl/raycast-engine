package com.williambl.raycastengine.collision

import com.williambl.raycastengine.gameobject.Collidable
import com.williambl.raycastengine.util.math.Vec2d

open class AxisAlignedBoundingBox(val minX: Double, val minY: Double, val maxX: Double, val maxY: Double, val owner: Collidable?) {
    fun collidesWith(point: Vec2d): Boolean {
        return point.x in minX..maxX && point.y in minY..maxY
    }

    fun collidesWith(x: Double, y: Double): Boolean {
        return x in minX..maxX && y in minY..maxY
    }

    fun collidesWith(other: AxisAlignedBoundingBox): Boolean {
        return this.minX <= other.maxX
                && this.maxX >= other.minX
                && this.maxY >= other.minY
                && this.minY <= other.maxY
    }

    fun fitsWithin(other: AxisAlignedBoundingBox): Boolean {
        return other.collidesWith(Vec2d(minX, minY)) && other.collidesWith(Vec2d(maxX, maxY))
    }
}