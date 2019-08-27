package com.williambl.raycastengine.gameobject

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.InputListener
import com.williambl.raycastengine.events.Tickable
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import kotlin.math.cos
import kotlin.math.sin


class Player(x: Double, y: Double) : Camera(x, y), InputListener, Tickable {

    var shouldGoForward = false
    var shouldGoBackward = false
    var shouldGoLeft = false
    var shouldGoRight = false

    override fun attachInputCallbacks() {
        glfwSetKeyCallback(Main.window, GLFWKeyCallback.create { window, key, scancode, action, mods ->
            if (action in arrayOf(GLFW_PRESS, GLFW_RELEASE)) {
                if (key == GLFW_KEY_W)
                    shouldGoForward = action == GLFW_PRESS
                if (key == GLFW_KEY_S)
                    shouldGoBackward = action == GLFW_PRESS
                if (key == GLFW_KEY_A)
                    shouldGoLeft = action == GLFW_PRESS
                if (key == GLFW_KEY_D)
                    shouldGoRight = action == GLFW_PRESS
            }
        })
    }

    override fun tick() {
        if (shouldGoForward)
            forward()
        if (shouldGoBackward)
            backward()
        if (shouldGoLeft)
            left()
        if (shouldGoRight)
            right()
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