package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.render.Texture
import com.williambl.raycastengine.util.raytrace.RaytraceResult

class InteractableSprite(texture: Texture, x: Double, y: Double) : Sprite(texture, x, y), Interactable, Collidable {
    override fun interact(player: Player, raytraceResult: RaytraceResult<*>) {
        println("hello world! ${System.currentTimeMillis().toString().substring(10)}")
    }

    override fun getAABB() = AxisAlignedBoundingBox(x - 0.5, y - 0.5, x + 0.5, y + 0.5, this)
}