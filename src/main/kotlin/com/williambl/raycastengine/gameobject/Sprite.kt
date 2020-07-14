package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.getObjectFromJson
import com.williambl.raycastengine.readJson
import com.williambl.raycastengine.render.Renderable
import com.williambl.raycastengine.render.RenderingContext
import com.williambl.raycastengine.render.SpriteRenderer
import com.williambl.raycastengine.render.Texture
import com.williambl.raycastengine.writeObjectToJson
import com.williambl.raycastengine.writeString
import io.netty.buffer.ByteBuf

open class Sprite(var texture: Texture = Texture(""), x: Double = 0.0, y: Double = 0.0) : GameObject(x, y), Renderable<Sprite> {
    val renderer: SpriteRenderer = SpriteRenderer()

    override fun getRenderer(): (Sprite, RenderingContext) -> Unit {
        return renderer::render
    }

    override fun toBytes(byteBuf: ByteBuf) {
        super.toBytes(byteBuf)
        byteBuf.writeString(writeObjectToJson(Texture::class.java, texture.location).toJsonString())
    }

    override fun fromBytes(byteBuf: ByteBuf) {
        super.fromBytes(byteBuf)
        texture = getObjectFromJson(byteBuf.readJson() as JsonObject) as Texture
    }

}