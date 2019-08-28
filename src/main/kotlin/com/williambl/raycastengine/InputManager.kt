package com.williambl.raycastengine

import com.williambl.raycastengine.events.InputListener
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback

class InputManager: InputListener {
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
}