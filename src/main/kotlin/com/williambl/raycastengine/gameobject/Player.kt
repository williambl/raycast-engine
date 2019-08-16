package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.InputListener
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import kotlin.math.cos
import kotlin.math.sin


class Player(var x: Double, var y: Double): InputListener {

    var plane = Pair(0.0, 0.66)
    var dir = Pair(-1.0, 0.0)

    override fun attachInputCallbacks() {
        glfwSetKeyCallback(Main.window, GLFWKeyCallback.create { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_W)
                forward()
            if (key == GLFW_KEY_S)
                backward()
            if (key == GLFW_KEY_A)
                left()
            if (key == GLFW_KEY_D)
                right()
        })
    }

    val moveSpeed = 0.1
    val rotSpeed = 0.1

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