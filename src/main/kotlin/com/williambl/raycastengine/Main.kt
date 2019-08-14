package com.williambl.raycastengine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL


object Main {

    var window: Long = 0
    var world = World(arrayOf(
            arrayOf(true, true, true, true, true, true, true, true, true, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, false, false, false, false, false, false, false, false, true),
            arrayOf(true, true, true, true, true, true, true, true, true, true)
    ))

    var player = Player(2.0, 2.0)
    var renderer = Renderer()

    @JvmStatic
    fun main(args: Array<String>) {
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        // Configure our window
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(500, 300, "Hello World!", NULL, NULL)
        if (window == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)

        GL.createCapabilities()
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, 500.0, 0.0, 300.0, 1.0, -1.0)
        glMatrixMode(GL_MODELVIEW)

        player.initKeyCallbacks()

        val loop = fun () {
            // Clear the framebuffer
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()

            // Render the world
            renderer.render(world, player)

            println(player.x.toString() + ", " + player.y.toString() + ", " + player.dir.toString())

            // Swap the color buffers
            glfwSwapBuffers(window)
        }

        while (true) {
            loop()
        }
    }
}