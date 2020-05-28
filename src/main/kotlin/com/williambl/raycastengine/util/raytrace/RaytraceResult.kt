package com.williambl.raycastengine.util.raytrace

import com.williambl.raycastengine.gameobject.Collidable

data class RaytraceResult<T : RaytraceResult.RaytraceResultType<out Any>>(val x: Int, val y: Int, val perpWallDist: Double, val side: RaytraceSide, val result: T) {

    enum class RaytraceSide {
        NORTHSOUTH,
        EASTWEST
    }

    abstract class RaytraceResultType<T>(val result: T)
    class TileRaytraceResultType(result: Int) : RaytraceResultType<Int>(result)
    class ColliderRaytraceResultType(result: Collidable) : RaytraceResultType<Collidable>(result)
}