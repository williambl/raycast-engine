package com.williambl.raycastengine

import com.williambl.raycastengine.events.InputListener
import com.williambl.raycastengine.events.RenderTickable
import com.williambl.raycastengine.events.StartupListener
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.gameobject.Player
import com.williambl.raycastengine.input.InputManager
import com.williambl.raycastengine.network.ClientNetworkManager
import com.williambl.raycastengine.network.ServerNetworkManager
import com.williambl.raycastengine.render.RenderSystem
import com.williambl.raycastengine.util.SyncedProperty
import com.williambl.raycastengine.util.network.*
import com.williambl.raycastengine.world.World
import com.williambl.raycastengine.world.WorldLoader
import imgui.ImGui
import imgui.classes.Context
import imgui.impl.gl.ImplGL3
import imgui.impl.glfw.ImplGlfw
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.reflections.Reflections
import uno.glfw.GlfwWindow
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object Main {

    var window: Long = 0
    var windowHeight: Int = 480
    var windowWidth: Int = 640
    var windowTitle: String = "Raycaster"

    lateinit var implGlfw: ImplGlfw
    lateinit var implGl3: ImplGL3

    val reflections = Reflections("")

    lateinit var world: World

    var inputManager = InputManager()

    var tickables: ArrayList<Tickable> = arrayListOf()

    var inputListeners: ArrayList<InputListener> = arrayListOf(
            inputManager
    )

    var startupListeners: ArrayList<StartupListener> = arrayListOf()

    val queuedWork: Queue<Runnable> = ConcurrentLinkedQueue()

    val myId: UUID = UUID.randomUUID()

    val shutdown = AtomicBoolean()

    @JvmStatic
    fun main(args: Array<String>) {
        init(args)

        thread(name = "Tick Thread") {
            while (!shutdown.get())
                loop()
        }

        while (!glfwWindowShouldClose(window)) {
            renderLoop()
        }

        cleanup()
    }

    private fun init(args: Array<String>) {
        initGLFW()
        initGL()
        initImGui()

        //TODO: change this if client, only create world when ready to avoid that overwrite
        world = WorldLoader.load(args.getOrElse(0) { "/world.json" })
        tickables.add(world)
        startupListeners.add(world)
        initInputListeners()
        initStartupListeners()

        if (args.contains("--client"))
            initClient(8080, /*args.getOrElse(args.indexOf("--address") + 1) {*/ "localhost" /*}*/)
        else
            initServer(8080)
    }

    private fun initServer(port: Int) {
        //TODO: move these
        ServerNetworkManager.addPacketCallback("login") { packet ->
            val buf = packet.buf
            val id = buf.readUUID()
            ServerNetworkManager.channels[id] = packet.ctx.channel()
            queuedWork.add(Runnable {
                val player = Player()
                player.id = id
                player.x = 3.0
                player.y = 3.0
                world.addGameObject(player)

                val rsp = Unpooled.buffer()
                rsp.writeString(world.serializer::class.java.canonicalName)
                world.serializer.toBytes(world, rsp, id)

                ServerNetworkManager.sendPacketToClient("fullSync", rsp, id)
            })
        }
        ServerNetworkManager.addPacketCallback("move") { packet ->
            val buf = packet.buf
            val id = packet.getId()
            val player = world.getGameObjectsOfType(Player::class.java).firstOrNull { it.id == id }
                    ?: return@addPacketCallback
            player.x = buf.readDouble()
            player.y = buf.readDouble()
            player.dir = buf.readDoublePair()
            player.plane = buf.readDoublePair()
        }
        ServerNetworkManager.addPacketCallback("interact") { packet ->
            world.getGameObjectsOfType(Player::class.java).firstOrNull { it.id == packet.getId() }?.interact()
        }
        thread(name = "Server Network Thread") {
            val bossGroup = NioEventLoopGroup()
            val workerGroup = NioEventLoopGroup()
            try {
                ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel::class.java)
                        .childHandler(object : ChannelInitializer<Channel>() {
                            override fun initChannel(ch: Channel) {
                                ch.pipeline().addLast(LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
                                ch.pipeline().addLast(LengthFieldPrepender(4))
                                ch.pipeline().addLast(ServerNetworkManager.get())
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .bind(port)
                        .sync()

                while (!shutdown.get()) {
                    Thread.sleep(100)
                }
            } finally {
                workerGroup.shutdownGracefully()
                bossGroup.shutdownGracefully()
            }
        }
    }

    private fun initClient(port: Int, address: String) {
        //TODO: move these, too
        ClientNetworkManager.addPacketCallback("fullSync") { packet ->
            val buf = packet.buf.copy()
            queuedWork.offer(Runnable {
                tickables.remove(world)
                startupListeners.remove(world)
                world.getGameObjectsOfType(RenderTickable::class.java).forEach {
                    RenderSystem.removeRenderTickable(it)
                }
                world = WorldLoader.getSerializer(buf.readString()).fromBytes(buf)
                world.isClient = true
                tickables.add(world)
                startupListeners.add(world)
                buf.release()
            })
        }
        ClientNetworkManager.addPacketCallback("sync") { packet ->
            val id = packet.buf.readUUID()
            val buf = packet.buf.copy()
            queuedWork.offer(Runnable {
                val name = buf.readString()
                val gObj = world.getGameObjects().find { it.id == id }
                if (gObj != null) {
                    val prop = gObj::class.memberProperties.filter { it is KMutableProperty<*> }.find { prop -> prop.name == name } as KMutableProperty1<GameObject, Any?>?
                    prop?.isAccessible = true
                    val delegate = prop?.getDelegate(gObj)
                    if (delegate is SyncedProperty<*>) {
                        delegate.setFromBytes(prop, gObj, buf)
                    }
                }
                buf.release()
            })
        }
        ClientNetworkManager.addPacketCallback("addGameObject") { packet ->
            val buf = packet.buf.copy()
            queuedWork.offer {
                world.addGameObject(buf.readGameObject())
            }
        }
        ClientNetworkManager.addPacketCallback("removeGameObject") { packet ->
            val uuid = packet.buf.readUUID()
            queuedWork.offer {
                world.removeGameObject(world.getGameObjects().find { it.id == uuid } ?: return@offer)
            }
        }

        thread(name = "Client Network Thread") {
            val workerGroup = NioEventLoopGroup()
            try {
                Bootstrap()
                        .group(workerGroup)
                        .channel(NioSocketChannel::class.java)
                        .handler(object : ChannelInitializer<Channel>() {
                            override fun initChannel(ch: Channel) {
                                ch.pipeline().addLast(LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
                                ch.pipeline().addLast(LengthFieldPrepender(4))
                                ch.pipeline().addLast(ClientNetworkManager)
                            }
                        })
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .connect(address, port)
                        .sync()

                while (!shutdown.get()) {
                    Thread.sleep(100)
                }
            } finally {
                workerGroup.shutdownGracefully()
            }
        }
    }

    private fun cleanup() {
        shutdown.set(true)
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
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
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL)
        if (window == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            glViewport(0, 0, width, height)
            windowWidth = width
            windowHeight = height
        }

        // Make the window visible
        glfwShowWindow(window)
    }

    private fun initGL() {
        GL.createCapabilities()
        glViewport(0, 0, windowWidth, windowHeight)
        // Set the clear color
        glClearColor(0.2f, 0.5f, 0.8f, 0.0f)
    }

    private fun initImGui() {
        val ctx = Context()
        ImGui.styleColorsDark()
        implGlfw = ImplGlfw(GlfwWindow.from(window), true)
        implGl3 = ImplGL3()
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

    private fun renderLoop() {
        // Clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents()

        implGl3.newFrame()
        implGlfw.newFrame()
        ImGui.newFrame()

        RenderSystem.tickRenderTickables()

        ImGui.render()
        implGl3.renderDrawData(ImGui.drawData!!)

        // Swap the color buffers
        glfwSwapBuffers(window)
    }

    private fun loop() {
        val next = System.currentTimeMillis() + 16
        //TODO: be able to schedule tasks on render and tick threads
        do {
            val runnable = queuedWork.poll()
            runnable?.run()
        } while (runnable != null)
        tickTickables()
        Thread.sleep(max(0, next - System.currentTimeMillis()))
    }
}