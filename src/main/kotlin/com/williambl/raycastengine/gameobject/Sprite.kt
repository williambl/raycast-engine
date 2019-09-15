package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.render.Renderable
import com.williambl.raycastengine.render.RenderingContext
import com.williambl.raycastengine.render.SpriteRenderer
import com.williambl.raycastengine.render.Texture

open class Sprite(val texture: Texture, x: Double, y: Double) : GameObject(x, y), Renderable<Sprite> {
    val renderer: SpriteRenderer = SpriteRenderer()

    override fun getRenderer(): (Sprite, RenderingContext) -> Unit {
        return renderer::render
    }

}