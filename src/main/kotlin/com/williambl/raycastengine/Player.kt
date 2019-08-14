package com.williambl.raycastengine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import kotlin.math.cos
import kotlin.math.sin


class Player(var x: Double, var y: Double, var dir: Pair<Double, Double>) {

    fun initKeyCallbacks() {
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

    fun forward() {
        val moveVec = Pair(dir.first*moveSpeed, dir.second*moveSpeed)
        if (!Main.world.map[(x+moveVec.first).toInt()][y.toInt()])
            x += moveVec.first
        if (!Main.world.map[x.toInt()][(y+moveVec.second).toInt()])
            y += moveVec.second
    }

    fun backward() {
        val moveVec = Pair(dir.first*moveSpeed, dir.second*moveSpeed)
        if (!Main.world.map[(x-moveVec.first).toInt()][y.toInt()])
            x -= moveVec.first
        if (!Main.world.map[x.toInt()][(y-moveVec.second).toInt()])
            y -= moveVec.second
    }

    fun left() {
        val dirX = dir.first * cos(rotSpeed) - dir.second * sin(rotSpeed)
        val dirY = dir.first * sin(rotSpeed) + dir.second * cos(rotSpeed)
        dir = Pair(dirX, dirY)
        val planeX = Main.renderer.plane.first * cos(rotSpeed) - Main.renderer.plane.second * sin(rotSpeed)
        val planeY = Main.renderer.plane.first * sin(rotSpeed) + Main.renderer.plane.second * cos(rotSpeed)
        Main.renderer.plane = Pair(planeX, planeY)
    }

    fun right() {
        val dirX = dir.first * cos(-rotSpeed) - dir.second * sin(-rotSpeed)
        val dirY = dir.first * sin(-rotSpeed) + dir.second * cos(-rotSpeed)
        dir = Pair(dirX, dirY)
        val planeX = Main.renderer.plane.first * cos(-rotSpeed) - Main.renderer.plane.second * sin(-rotSpeed)
        val planeY = Main.renderer.plane.first * sin(-rotSpeed) + Main.renderer.plane.second * cos(-rotSpeed)
        Main.renderer.plane = Pair(planeX, planeY)
    }
}