package com.williambl.raycastengine.util.raytrace

enum class RaytraceMode(val flag: Int) {
    TILES(1),
    AABBS(2);

    fun matches(other: RaytraceMode): Boolean {
        return (this.flag and other.flag) != 0
    }
}