package com.williambl.raycastengine.util.raytrace

enum class RaytraceModeType(override val flag: Int) : RaytraceMode {
    TILES(1),
    AABBS(2);

}

interface RaytraceMode {
    val flag: Int

    fun matches(other: RaytraceMode): Boolean {
        return (this.flag and other.flag) != 0
    }

    fun and(other: RaytraceMode): RaytraceMode {
        return CombinationRaytraceMode(this.flag or other.flag)
    }
}

class CombinationRaytraceMode(override val flag: Int) : RaytraceMode