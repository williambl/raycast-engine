package com.williambl.raycastengine.gameobject

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.RenderTickable
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.network.ClientNetworkManager
import com.williambl.raycastengine.render.DefaultWorldRenderer
import com.williambl.raycastengine.render.PlayerRenderer
import com.williambl.raycastengine.render.Renderable
import com.williambl.raycastengine.render.RenderingContext
import com.williambl.raycastengine.util.network.writeDoublePair
import com.williambl.raycastengine.util.raytrace.RaytraceModeType
import com.williambl.raycastengine.util.raytrace.RaytraceResult
import com.williambl.raycastengine.world.DefaultWorld
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlin.math.cos
import kotlin.math.sin


class Player(x: Double = 0.0, y: Double = 0.0, isLocal: Boolean = false) : Camera(x, y), Tickable, RenderTickable, Renderable<Player> {

    init {
        if (isLocal) {
            id = Main.myId
            Main.renderTickables.add(this)
        }
    }

    val worldRenderer: DefaultWorldRenderer by lazy { DefaultWorldRenderer(world as DefaultWorld, this) }

    val renderer: PlayerRenderer by lazy { PlayerRenderer() }

    override fun tick() {
        if (id == Main.myId) {
            if (Main.inputManager.isPressed("forward"))
                forward()
            if (Main.inputManager.isPressed("backward"))
                backward()
            if (Main.inputManager.isPressed("left"))
                left()
            if (Main.inputManager.isPressed("right"))
                right()
            if (Main.inputManager.isPressed("interact")) {
                if (world.isClient)
                    ClientNetworkManager.sendPacketToServer("interact", Unpooled.buffer())
                interact()
            }

            if (world.isClient) {
                ClientNetworkManager.sendPacketToServer("move", Unpooled.buffer().writeDouble(x).writeDouble(y).writeDoublePair(dir).writeDoublePair(plane))
            }
        }
    }

    override fun renderTick() {
        if (id == Main.myId)
            worldRenderer.renderTick()
    }

    val moveSpeed = 0.075
    val rotSpeed = 0.075

    private fun forward() {
        val moveVec = Pair(dir.first * moveSpeed, dir.second * moveSpeed)
        if (Main.world.map[(x + moveVec.first).toInt()][y.toInt()] == 0)
            x += moveVec.first
        if (Main.world.map[x.toInt()][(y + moveVec.second).toInt()] == 0)
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

    fun interact() {
        val result = world.rayTrace(dir.first, dir.second, x, y, raytraceMode)
        when (result.result) {
            is RaytraceResult.AABBRaytraceResultType -> result.result.result
            is RaytraceResult.TileRaytraceResultType -> (world as DefaultWorld).getAABBsAt(result.x.toDouble() + 0.5, result.y.toDouble() + 0.5)
            else -> listOf()
        }.filter { it.owner is Interactable }.forEach { (it.owner as Interactable).interact(this, result) }
    }

    override fun getRenderer(): (Player, RenderingContext) -> Unit {
        if (id == Main.myId)
            return { _, _ -> }
        return renderer::render
    }

    override fun fromJson(json: JsonObject) {
        super.fromJson(json)
        if (id == Main.myId)
            Main.renderTickables.add(this)
    }

    override fun fromBytes(byteBuf: ByteBuf) {
        super.fromBytes(byteBuf)
        if (id == Main.myId)
            Main.renderTickables.add(this)
    }
}