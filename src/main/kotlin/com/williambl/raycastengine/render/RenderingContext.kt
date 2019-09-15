package com.williambl.raycastengine.render

import com.williambl.raycastengine.World
import com.williambl.raycastengine.gameobject.Camera

data class RenderingContext(val world: World, val camera: Camera, val width: Int, val height: Int, val zBuffer: Array<Double>, val worldRenderer: WorldRenderer) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RenderingContext

        if (world != other.world) return false
        if (camera != other.camera) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (!zBuffer.contentEquals(other.zBuffer)) return false
        if (worldRenderer != other.worldRenderer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = world.hashCode()
        result = 31 * result + camera.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + zBuffer.contentHashCode()
        result = 31 * result + worldRenderer.hashCode()
        return result
    }
}
