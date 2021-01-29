package com.williambl.raycastengine.tater

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Collidable
import com.williambl.raycastengine.gameobject.Interactable
import com.williambl.raycastengine.gameobject.Player
import com.williambl.raycastengine.gameobject.Sprite
import com.williambl.raycastengine.render.RenderingContext
import com.williambl.raycastengine.render.SpriteRenderer
import com.williambl.raycastengine.render.Texture
import com.williambl.raycastengine.render.gui.Gui
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import com.williambl.raycastengine.util.synced
import imgui.ImGui
import io.netty.buffer.ByteBuf
import kotlin.math.roundToInt
import kotlin.random.Random

class TaterNPC(textureLoc: String, private val deadTextureLoc: String, x: Double, y: Double) : Sprite(textureLoc, x, y), Interactable, Collidable, Tickable {
    var deadTicks: Int by synced(-1, ::id, ByteBuf::writeInt, ByteBuf::readInt)

    private val deadRenderer: SpriteRenderer by lazy { SpriteRenderer(deadTextureLoc) }

    override fun getRenderer(): (Sprite, RenderingContext) -> Unit {
        return if (deadTicks >= 0) deadRenderer::render else super.getRenderer()
    }

    override fun getAABB(): AxisAlignedBoundingBox = AxisAlignedBoundingBox(x-1.5f, y-1.5f, x+1.5f, y+1.5f, this)

    override fun interact(player: Player, raytraceResult: RaytraceResult<*>) {
        deadTicks++
    }

    override fun tick() {
        if (deadTicks >= 0) {
            deadTicks++
            //if (deadTicks > 20)
                //this.world.removeGameObject(this)
        } else {
            val tryX = x + (Random.nextFloat()-0.5f) * 0.1f
            if (world.isTileAir(tryX.roundToInt(), y.roundToInt())) {
                x = tryX
            }
            val tryY = y + (Random.nextFloat()-0.5f) * 0.1f
            if (world.isTileAir(x.roundToInt(), tryY.roundToInt())) {
                y = tryY
            }
        }
    }
}