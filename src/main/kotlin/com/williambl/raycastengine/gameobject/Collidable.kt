package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.collision.AxisAlignedBoundingBox

interface Collidable {
    fun getAABB(): AxisAlignedBoundingBox
}