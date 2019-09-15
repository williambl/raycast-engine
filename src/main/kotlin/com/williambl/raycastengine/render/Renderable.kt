package com.williambl.raycastengine.render

import com.williambl.raycastengine.gameobject.GameObject

interface Renderable<in T: GameObject> {
    fun getRenderer(): ((T, RenderingContext) -> Unit)
}
