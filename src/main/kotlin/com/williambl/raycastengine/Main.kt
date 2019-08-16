package com.williambl.raycastengine

import com.williambl.raycastengine.events.InputListener
import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Player
import com.williambl.raycastengine.render.Renderer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL


object Main {

    var window: Long = 0
    var windowHeight: Int = 480
    var windowWidth: Int = 640
    var windowTitle: String = "Raycaster"

    var world = World(arrayOf(
            arrayOf(1, 1, 1, 1, 1, 1, 1, 2, 2, 2),
            arrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 2),
            arrayOf(1, 0, 0, 0, 0, 0, 0, 2, 0, 2),
            arrayOf(1, 0, 0, 0, 0, 0, 0, 2, 0, 2),
            arrayOf(1, 0, 0, 0, 0, 0, 0, 2, 0, 2),
            arrayOf(1, 0, 0, 0, 0, 0, 0, 2, 0, 2),
            arrayOf(1, 0, 1, 1, 1, 0, 2, 2, 0, 2),
            arrayOf(1, 0, 1, 0, 0, 0, 2, 0, 0, 2),
            arrayOf(1, 0, 1, 0, 0, 0, 0, 0, 0, 2),
            arrayOf(1, 1, 1, 2, 2, 2, 2, 2, 2, 2)
    ))

    var player = Player(2.0, 2.0)
    var renderer = Renderer()

    var tickables: Array<Tickable> = arrayOf(
            renderer
    )
    var inputListeners: Array<InputListener> = arrayOf(
            player
    )
    var startupListeners: Array<StartupListener> = arrayOf(
            world
    )

    @JvmStatic
    fun main(args: Array<String>) {
        init()

        while (true) {
            loop()
        }
    }

    private fun init() {
        initGLFW()
        initGL()

        initInputListeners()
        initStartupListeners()
    }

    private fun initGLFW() {
        //Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        // Configure our window
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL)
        if (window == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)
    }

    private fun initGL() {
        GL.createCapabilities()
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, windowWidth.toDouble(), 0.0, windowHeight.toDouble(), 1.0, -1.0)
        glMatrixMode(GL_MODELVIEW)
    }

    private fun initInputListeners() {
        inputListeners.forEach {
            it.attachInputCallbacks()
        }
    }

    private fun initStartupListeners() {
        startupListeners.forEach {
            it.onStart()
        }
    }

    private fun tickTickables() {
        tickables.forEach {
            it.tick()
        }
    }

    private fun loop() {
        // Clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents()

        tickTickables()

        // Swap the color buffers
        glfwSwapBuffers(window)
    }

}