package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.util.raytrace.RaytraceResult

interface Interactable {
    fun interact(player: Player, raytraceResult: RaytraceResult<*>)
}