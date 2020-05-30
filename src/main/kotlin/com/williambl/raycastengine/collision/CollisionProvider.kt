package com.williambl.raycastengine.collision

import com.williambl.raycastengine.util.math.Vec2d

interface CollisionProvider {

    val quadTree: AABBQuadTree

    fun getAABBsAt(position: Vec2d): List<AxisAlignedBoundingBox> {
        return quadTree[position]
    }

    fun getAABBsAt(x: Double, y: Double): List<AxisAlignedBoundingBox> {
        return quadTree[x, y]
    }

    fun addAABB(element: AxisAlignedBoundingBox) {
        quadTree.add(element)
    }

    fun removeAABB(element: AxisAlignedBoundingBox) {
        quadTree.remove(element)
    }
}