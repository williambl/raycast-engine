package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.util.network.readGameObject
import com.williambl.raycastengine.util.network.writeGameObject
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import io.netty.buffer.Unpooled
import java.nio.charset.Charset

class InteractableSprite(textureLoc: String = "", x: Double = 0.0, y: Double = 0.0) : Sprite(textureLoc, x, y), Interactable, Collidable {
    override fun interact(player: Player, raytraceResult: RaytraceResult<*>) {
        val buf = Unpooled.buffer()
        buf.writeGameObject(this)
        println(buf.toString(0, buf.writerIndex() + 1, Charset.defaultCharset()))
        world.removeGameObject(this)
        world.addGameObject(buf.readGameObject())
    }

    override fun getAABB() = AxisAlignedBoundingBox(x - 0.5, y - 0.5, x + 0.5, y + 0.5, this)
}