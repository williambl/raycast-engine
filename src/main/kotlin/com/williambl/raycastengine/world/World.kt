package com.williambl.raycastengine.world

import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.util.math.Vec2d
import com.williambl.raycastengine.util.raytrace.RaytraceMode
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import kotlin.math.abs

interface World: StartupListener, Tickable {

    val map: Array<IntArray>

    fun addGameObject(gameObject: GameObject)

    fun removeGameObject(gameObject: GameObject)

    fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T>

    fun isTileAir(x: Int, y: Int): Boolean {
        return map[x][y] == 0
    }

    fun rayTrace(from: Vec2d, vec: Vec2d, mode: RaytraceMode = RaytraceMode.TILES): RaytraceResult<*> {
        val dir = vec.normalised()
        return rayTrace(dir.x, dir.y, from.x, from.y, mode)
    }

    fun rayTrace(rayDirX: Double, rayDirY: Double, fromX: Double, fromY: Double, mode: RaytraceMode = RaytraceMode.TILES): RaytraceResult<*> {
        var x = fromX.toInt()
        var y = fromY.toInt()

        // Length of ray from current position to next X or Y wall
        // https://lodev.org/cgtutor/images/raycastdelta.gif
        var sideDistX: Double
        var sideDistY: Double

        // Length of ray from one side to next in map
        val deltaDistX = abs(1 / rayDirX)
        val deltaDistY = abs(1 / rayDirY)

        // Direction to go in X and Y
        val stepX: Int
        val stepY: Int

        var tileResult = 0 // Result of raycast
        var collidableResult: AxisAlignedBoundingBox? = null // Result of raycast
        var side = RaytraceResult.RaytraceSide.NORTHSOUTH // Was the wall N-S or E-W

        // Calculate vector to next square
        if (rayDirX < 0) {
            stepX = -1
            sideDistX = (fromX - x) * deltaDistX
        } else {
            stepX = 1
            sideDistX = (x + 1.0 - fromX) * deltaDistX
        }
        if (rayDirY < 0) {
            stepY = -1
            sideDistY = (fromY - y) * deltaDistY
        } else {
            stepY = 1
            sideDistY = (y + 1.0 - fromY) * deltaDistY
        }

        // Loop to find where the ray hits a wall
        while (tileResult == 0) {
            // Jump to next square
            if (sideDistX < sideDistY) {
                sideDistX += deltaDistX
                x += stepX
                side = RaytraceResult.RaytraceSide.NORTHSOUTH
            } else {
                sideDistY += deltaDistY
                y += stepY
                side = RaytraceResult.RaytraceSide.EASTWEST
            }

            // Check if ray has hit a wall
            if (mode.matches(RaytraceMode.TILES)) {
                try {
                    tileResult = this.map[x][y]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    break
                }
            }
            if (mode.matches(RaytraceMode.AABBS)) {
                //TODO
            }
        }

        // Calculate distance from camera plane to wall
        val perpWallDist = if (side == RaytraceResult.RaytraceSide.NORTHSOUTH) (x - fromX + (1 - stepX) / 2) / rayDirX
        else (y - fromY + (1 - stepY) / 2) / rayDirY

        return RaytraceResult(
                x, y, perpWallDist, side,
                if (collidableResult != null)
                    RaytraceResult.AABBRaytraceResultType(collidableResult)
                else
                    RaytraceResult.TileRaytraceResultType(tileResult)
        )
    }
}