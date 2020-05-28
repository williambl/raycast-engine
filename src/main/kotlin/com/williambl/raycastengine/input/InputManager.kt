package com.williambl.raycastengine.input

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.InputListener
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback

class InputManager: InputListener {

    val keybinds = listOf(
            Keybind("forward", GLFW_KEY_W),
            Keybind("backward", GLFW_KEY_S),
            Keybind("left", GLFW_KEY_A),
            Keybind("right", GLFW_KEY_D),
            Keybind("interact", GLFW_KEY_SPACE)
    )

    override fun attachInputCallbacks() {
        glfwSetKeyCallback(Main.window, GLFWKeyCallback.create { _, key, _, action, _ ->
            when (action) {
                GLFW_PRESS -> keybinds.filter { it.key == key }.forEach { it.down() }
                GLFW_RELEASE -> keybinds.filter { it.key == key }.forEach { it.up() }
            }
        })
    }

    fun isPressed(name: String): Boolean {
        return keybinds.find { it.name == name }?.pressed ?: false
    }
}