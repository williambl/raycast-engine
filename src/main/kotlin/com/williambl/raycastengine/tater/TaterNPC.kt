package com.williambl.raycastengine.tater

import com.beust.klaxon.JsonObject
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
import com.williambl.raycastengine.util.network.readString
import com.williambl.raycastengine.util.network.writeString
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import com.williambl.raycastengine.util.synced
import imgui.ImGui
import io.netty.buffer.ByteBuf
import kotlin.math.roundToInt
import kotlin.random.Random

class TaterNPC(textureLoc: String = "", private var deadTextureLoc: String = "", x: Double = 0.0, y: Double = 0.0) : Sprite(textureLoc, x, y), Interactable, Collidable, Tickable {

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
            if (deadTicks > 120)
                this.world.removeGameObject(this)
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

    override fun toBytes(byteBuf: ByteBuf) {
        super.toBytes(byteBuf)
        byteBuf.writeString(deadTextureLoc)
    }

    override fun fromBytes(byteBuf: ByteBuf) {
        super.fromBytes(byteBuf)
        deadTextureLoc = byteBuf.readString()
    }
}