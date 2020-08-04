package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.render.Renderable
import com.williambl.raycastengine.render.RenderingContext
import com.williambl.raycastengine.render.SpriteRenderer
import com.williambl.raycastengine.util.network.readString
import com.williambl.raycastengine.util.network.writeString
import io.netty.buffer.ByteBuf

open class Sprite(var textureLoc: String = "", x: Double = 0.0, y: Double = 0.0) : GameObject(x, y), Renderable<Sprite> {
    val renderer: SpriteRenderer by lazy { SpriteRenderer(textureLoc) }

    override fun getRenderer(): (Sprite, RenderingContext) -> Unit {
        return renderer::render
    }

    override fun toBytes(byteBuf: ByteBuf) {
        super.toBytes(byteBuf)
        byteBuf.writeString(textureLoc)
    }

    override fun fromBytes(byteBuf: ByteBuf) {
        super.fromBytes(byteBuf)
        textureLoc = byteBuf.readString()
    }

}