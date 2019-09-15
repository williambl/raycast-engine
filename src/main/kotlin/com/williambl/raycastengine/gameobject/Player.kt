package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.render.WorldRenderer
import kotlin.math.cos
import kotlin.math.sin


class Player(x: Double, y: Double) : Camera(x, y), Tickable {

    val worldRenderer: WorldRenderer by lazy { WorldRenderer(world, this) }

    override fun tick() {
        if (Main.inputManager.shouldGoForward)
            forward()
        if (Main.inputManager.shouldGoBackward)
            backward()
        if (Main.inputManager.shouldGoLeft)
            left()
        if (Main.inputManager.shouldGoRight)
            right()

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
}