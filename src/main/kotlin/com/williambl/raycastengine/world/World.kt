package com.williambl.raycastengine.world

import com.williambl.raycastengine.collision.AxisAlignedBoundingBox
import com.williambl.raycastengine.collision.CollisionProvider
import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.util.math.Vec2d
import com.williambl.raycastengine.util.raytrace.RaytraceMode
import com.williambl.raycastengine.util.raytrace.RaytraceModeType
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import kotlin.math.abs

/**
 * The world, in which all things in the game take place.
 *
 * A World holds all the [GameObject]s in the game, and handles their ticking.
 *
 * It also handles all [raytracing][rayTrace], and holds the [map] of tiles.
 *
 * Worlds are created by the [WorldLoader] using a [WorldSerializer]. Implementors of World therefore require a custom
 * [WorldSerializer] to create them.
 */
interface World: StartupListener, Tickable {

    val map: Array<IntArray>

    var isClient: Boolean

    val serializer: WorldSerializer

    fun addGameObject(gameObject: GameObject)

    fun removeGameObject(gameObject: GameObject)

    fun <T : Any> getGameObjectsOfType(klass: Class<T>): List<T>

    fun getGameObjects(): List<GameObject>

    fun isTileAir(x: Int, y: Int): Boolean {
        return map[x][y] == 0
    }

    fun rayTrace(from: Vec2d, vec: Vec2d, mode: RaytraceMode = RaytraceModeType.TILES): RaytraceResult<*> {
        val dir = vec.normalised()
        return rayTrace(dir.x, dir.y, from.x, from.y, mode)
    }

    fun rayTrace(rayDirX: Double, rayDirY: Double, fromX: Double, fromY: Double, mode: RaytraceMode = RaytraceModeType.TILES): RaytraceResult<*> {
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
        val aabbResult = mutableListOf<AxisAlignedBoundingBox>() // Result of raycast
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
        while (tileResult == 0 && aabbResult.isEmpty()) {
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
            if (mode.matches(RaytraceModeType.TILES)) {
                try {
                    tileResult = this.map[x][y]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    break
                }
            }
            if (mode.matches(RaytraceModeType.AABBS)) {
                try {
                    this.map[x][y]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    break
                }
                if (this is CollisionProvider) {
                    aabbResult.addAll(this.getAABBsAt(x.toDouble(), y.toDouble()))
                }
            }
        }

        // Calculate distance from camera plane to wall
        val perpWallDist = if (side == RaytraceResult.RaytraceSide.NORTHSOUTH) (x - fromX + (1 - stepX) / 2) / rayDirX
        else (y - fromY + (1 - stepY) / 2) / rayDirY

        return RaytraceResult(
                x, y, perpWallDist, side,
                if (aabbResult.isNotEmpty())
                    RaytraceResult.AABBRaytraceResultType(aabbResult)
                else
                    RaytraceResult.TileRaytraceResultType(tileResult)
        )
    }
}