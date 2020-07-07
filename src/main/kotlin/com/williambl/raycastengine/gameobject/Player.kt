package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.render.DefaultWorldRenderer
import com.williambl.raycastengine.util.raytrace.RaytraceModeType
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import com.williambl.raycastengine.world.DefaultWorld
import kotlin.math.cos
import kotlin.math.sin


open class Player(x: Double = 0.0, y: Double = 0.0) : Camera(x, y), Tickable {

    val worldRenderer: DefaultWorldRenderer by lazy { DefaultWorldRenderer(world as DefaultWorld, this) }

    override fun tick() {
        if (Main.inputManager.isPressed("forward"))
            forward()
        if (Main.inputManager.isPressed("backward"))
            backward()
        if (Main.inputManager.isPressed("left"))
            left()
        if (Main.inputManager.isPressed("right"))
            right()
        if (Main.inputManager.isPressed("interact"))
            interact()

        worldRenderer.tick()
    }

    val moveSpeed = 0.075
    val rotSpeed = 0.075

    private fun forward() {
        val moveVec = Pair(dir.first*moveSpeed, dir.second*moveSpeed)
        if (Main.world.map[(x+moveVec.first).toInt()][y.toInt()] == 0)
            x += moveVec.first
        if (Main.world.map[x.toInt()][(y+moveVec.second).toInt()] == 0)
            y += moveVec.second
    }

    private fun backward() {
        val moveVec = Pair(dir.first*moveSpeed, dir.second*moveSpeed)
        if (Main.world.map[(x-moveVec.first).toInt()][y.toInt()] == 0)
            x -= moveVec.first
        if (Main.world.map[x.toInt()][(y-moveVec.second).toInt()] == 0)
            y -= moveVec.second
    }

    private fun left() {
        val dirX = dir.first * cos(rotSpeed) - dir.second * sin(rotSpeed)
        val dirY = dir.first * sin(rotSpeed) + dir.second * cos(rotSpeed)
        dir = Pair(dirX, dirY)
        val planeX = plane.first * cos(rotSpeed) - plane.second * sin(rotSpeed)
        val planeY = plane.first * sin(rotSpeed) + plane.second * cos(rotSpeed)
        plane = Pair(planeX, planeY)
    }

    private fun right() {
        val dirX = dir.first * cos(-rotSpeed) - dir.second * sin(-rotSpeed)
        val dirY = dir.first * sin(-rotSpeed) + dir.second * cos(-rotSpeed)
        dir = Pair(dirX, dirY)
        val planeX = plane.first * cos(-rotSpeed) - plane.second * sin(-rotSpeed)
        val planeY = plane.first * sin(-rotSpeed) + plane.second * cos(-rotSpeed)
        plane = Pair(planeX, planeY)
    }

    val raytraceMode = RaytraceModeType.AABBS.and(RaytraceModeType.TILES)

    private fun interact() {
        val result = world.rayTrace(dir.first, dir.second, x, y, raytraceMode)
        when (result.result) {
            is RaytraceResult.AABBRaytraceResultType -> result.result.result
            is RaytraceResult.TileRaytraceResultType -> (world as DefaultWorld).getAABBsAt(result.x.toDouble() + 0.5, result.y.toDouble() + 0.5)
            else -> listOf()
        }.filter { it.owner is Interactable }.forEach { (it.owner as Interactable).interact(this, result) }
    }
}