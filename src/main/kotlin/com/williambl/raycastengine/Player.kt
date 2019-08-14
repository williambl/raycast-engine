package com.williambl.raycastengine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class Player(var x: Double, var y: Double, var dir: Double) {

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
        val moveVec = Pair(cos(dir * PI / 180)*moveSpeed, sin(dir * PI / 180)*moveSpeed)
        if (!Main.world.map[(x+moveVec.first).toInt()][y.toInt()])
            x += moveVec.first
        if (!Main.world.map[x.toInt()][(y+moveVec.second).toInt()])
            y += moveVec.second
    }

    fun backward() {
        val moveVec = Pair(cos(dir * PI / 180)*moveSpeed, sin(dir * PI / 180)*moveSpeed)
        if (!Main.world.map[(x-moveVec.first).toInt()][y.toInt()])
            x -= moveVec.first
        if (!Main.world.map[x.toInt()][(y-moveVec.second).toInt()])
            y -= moveVec.second
    }

    fun left() {
        dir -= rotSpeed
    }

    fun right() {
        dir += rotSpeed
    }
}