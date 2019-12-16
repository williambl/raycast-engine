package com.williambl.raycastengine

import com.williambl.raycastengine.events.InputListener
import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.reflections.Reflections


object Main {

    var window: Long = 0
    var windowHeight: Int = 480
    var windowWidth: Int = 640
    var windowTitle: String = "Raycaster"

    val reflections = Reflections("")

    val gameObjectClasses: Map<String, Class<out GameObject>> = reflections.getSubTypesOf(GameObject::class.java).map {
        it.name to it
    }.toMap()

    lateinit var world: World

    var inputManager = InputManager()

    var tickables: ArrayList<Tickable> = arrayListOf()

    var inputListeners: ArrayList<InputListener> = arrayListOf(
            inputManager
    )

    var startupListeners: ArrayList<StartupListener> = arrayListOf()

    @JvmStatic
    fun main(args: Array<String>) {
        init(args)

        while (!glfwWindowShouldClose(window)) {
            loop()
        }

        cleanup()
    }

    private fun init(args: Array<String>) {
        initGLFW()
        initGL()

        world = WorldLoader(args.getOrElse(0) { "/world.json" }).load()
        tickables.add(world)
        startupListeners.add(world)
        initInputListeners()
        initStartupListeners()
    }

    private fun cleanup() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private fun initGLFW() {
        //Create an error callback to tell us if something goes wrong
        GLFWErrorCallback.createPrint(System.err).set()

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