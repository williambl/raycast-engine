package com.williambl.raycastengine.render

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Camera
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.gameobject.Light
import com.williambl.raycastengine.world.DefaultWorld
import com.williambl.raycastengine.world.World
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow


class DefaultWorldRenderer(val world: DefaultWorld, val camera: Camera) : Tickable {

    lateinit var floorShape: ColouredRenderableShape
    lateinit var skyShape: ColouredRenderableShape

    val wallShapes: MutableList<TexturedRenderableShape> = mutableListOf()

    var wallShader: Int = 0

    init {
        setupGL()
    }

    override fun tick() {
        render(world, camera)
    }

    private fun setupGL() {
        floorShape = ColouredRenderableShape(
                floatArrayOf(
                        -1.0f, -1.0f, 0.0f, world.floorColor.first, world.floorColor.second, world.floorColor.third,
                        -1.0f, 0.0f, 0.0f,  world.floorColor.first, world.floorColor.second, world.floorColor.third,
                        1.0f, 0.0f, 0.0f,   world.floorColor.first, world.floorColor.second, world.floorColor.third,
                        1.0f, -1.0f, 0.0f,  world.floorColor.first, world.floorColor.second, world.floorColor.third
                ),
                intArrayOf(
                        0, 1, 2,
                        2, 3, 0
                ),
                RenderUtils.getAndCompileShaderProgram("flat")
        )
        floorShape.setup()
        skyShape = ColouredRenderableShape(
                floatArrayOf(
                        -1.0f, 0.0f, 0.0f, world.skyColor.first, world.skyColor.second, world.skyColor.third,
                        -1.0f, 1.0f, 0.0f,  world.skyColor.first, world.skyColor.second, world.skyColor.third,
                        1.0f, 1.0f, 0.0f,   world.skyColor.first, world.skyColor.second, world.skyColor.third,
                        1.0f, 0.0f, 0.0f,  world.skyColor.first, world.skyColor.second, world.skyColor.third
                ),
                intArrayOf(
                        0, 1, 2,
                        2, 3, 0
                ),
                RenderUtils.getAndCompileShaderProgram("flat")
        )
        skyShape.setup()

        wallShader = RenderUtils.getAndCompileShaderProgram("flatTextured")
    }

    private fun render(world: DefaultWorld, camera: Camera) {
        val widthB = BufferUtils.createIntBuffer(1)
        val heightB = BufferUtils.createIntBuffer(1)
        glfwGetWindowSize(Main.window, widthB, heightB)
        val width = widthB[0]
        val height = heightB[0]

        val zBuffer = Array(width + 1) { 0.0 }

        val context = RenderingContext(world, camera, width, height, zBuffer, this)

        renderBackground(context)

        renderWorld(context)

        //renderRenderables(context)
    }

    private fun renderBackground(context: RenderingContext) {
        floorShape.render()
        skyShape.render()
    }

    private fun renderWorld(context: RenderingContext) {
        if (wallShapes.size > context.width) {
            for (i in wallShapes.size..0) {
                if (i > context.width) {
                    wallShapes.removeAt(i)
                }
            }
        } else if (wallShapes.size < context.width) {
            for (i in 0..(context.width-wallShapes.size)) {
                wallShapes.add(
                        TexturedRenderableShape(
                                floatArrayOf(
                                        -1f, -1f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                                        1f, -1f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1f, 1f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
                                        -1f, 1f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f
                                ),
                                intArrayOf(
                                        0, 1, 2,
                                        2, 3, 0
                                ),
                                wallShader,
                                world.wallTextures[0]
                        )
                )
            }
        }
        for (column in 0..context.width) {
            val cameraX = 2 * column / context.width.toDouble() - 1 // X-coord in camera space

            val rayDirX = camera.dir.first + camera.plane.first * cameraX
            val rayDirY = camera.dir.second + camera.plane.second * cameraX

            // Coords of current square
            var mapX = camera.x.toInt()
            var mapY = camera.y.toInt()

            // Length of ray from current position to next X or Y wall
            // https://lodev.org/cgtutor/images/raycastdelta.gif
            var sideDistX: Double
            var sideDistY: Double

            // Length of ray from one side to next in map
            val deltaDistX = abs(1 / rayDirX)
            val deltaDistY = abs(1 / rayDirY)
            var perpWallDist: Double

            // Direction to go in X and Y
            var stepX: Int
            var stepY: Int

            var result = 0 // Result of raycast
            var side = 0 // Was the wall N-S or E-W

            // Calculate vector to next square
            if (rayDirX < 0)
            {
                stepX = -1
                sideDistX = (camera.x - mapX) * deltaDistX
            }
            else
            {
                stepX = 1
                sideDistX = (mapX + 1.0 - camera.x) * deltaDistX
            }
            if (rayDirY < 0)
            {
                stepY = -1
                sideDistY = (camera.y - mapY) * deltaDistY
            }
            else
            {
                stepY = 1
                sideDistY = (mapY + 1.0 - camera.y) * deltaDistY
            }

            // Loop to find where the ray hits a wall
            while(result == 0) {

                // Jump to next square
                if (sideDistX < sideDistY)
                {
                    sideDistX += deltaDistX
                    mapX += stepX
                    side = 0
                }
                else
                {
                    sideDistY += deltaDistY
                    mapY += stepY
                    side = 1
                }

                // Check if ray has hit a wall
                result = try {
                    world.map[mapX][mapY]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    0
                    break
                }
            }

            // Calculate distance from camera plane to wall
            perpWallDist = if (side == 0) (mapX - camera.x + (1 - stepX) / 2) / rayDirX
            else (mapY - camera.y + (1 - stepY) / 2) / rayDirY

            val lineHeight: Float = ((context.height / perpWallDist)/context.height).toFloat()

            // Calculate lowest and highest pixel to fill in current column
            val bottom: Float = -lineHeight / 1.25f
            val top: Float = lineHeight / 1.25f

            // Calculate the x-coordinate of the column in viewspace
            val columnXMin: Float = (column/context.width.toFloat())*2f - 1
            val columnXMax: Float = ((column+1)/context.width.toFloat())*2f - 1

            // Calculate which column of texture to use
            var textureX: Float = (if (side == 0) camera.y + perpWallDist * rayDirY
            else camera.x + perpWallDist * rayDirX).toFloat()
            textureX -= floor((textureX))

            val pixelWidth: Float = 1/world.wallTextures[result].width.toFloat()

            // Work out how light it should be

            val brightness = calculateLighting(world, (camera.x + perpWallDist * rayDirX), (camera.y + perpWallDist * rayDirY))

            // Write to the z-buffer
            context.zBuffer[column] = perpWallDist

            // Draw it
            val wallShape = wallShapes[column]

             wallShape.vertices = floatArrayOf(
                            columnXMin, bottom, 0.0f, brightness.first, brightness.second, brightness.third, textureX, 1.0f,
                            columnXMax, bottom, 0.0f, brightness.first, brightness.second, brightness.third, textureX+pixelWidth, 1.0f,
                            columnXMax, top, 0.0f, brightness.first, brightness.second, brightness.third, textureX+pixelWidth, 0.0f,
                            columnXMin, top, 0.0f, brightness.first, brightness.second, brightness.third, textureX, 0.0f
                    )
            wallShape.texture = world.wallTextures[result]
            wallShape.setup()
            wallShape.render()

        }
    }

    private fun renderRenderables(context: RenderingContext) {
        val renderables = context.world.getGameObjectsOfType(Renderable::class.java).map { it as Renderable<GameObject> } as ArrayList

        renderables.sortBy {
            abs((it as GameObject).x - camera.x).pow(2) + abs((it as GameObject).y - camera.y).pow(2)
        }
        renderables.reverse()

        renderables.forEach {
            it.getRenderer().invoke(it as GameObject, context)
        }
    }

    internal fun calculateLighting(world: World, x: Double, y: Double): Triple<Float, Float, Float> {
        var brightnessR = 0.0
        var brightnessG = 0.0
        var brightnessB = 0.0
        world.getGameObjectsOfType(Light::class.java).forEach {
            brightnessR += (1 / ((it.x - x).pow(2) + (it.y - y).pow(2))) * it.strength.first
            brightnessG += (1 / ((it.x - x).pow(2) + (it.y - y).pow(2))) * it.strength.second
            brightnessB += (1 / ((it.x - x).pow(2) + (it.y - y).pow(2))) * it.strength.third
        }

        // No HDR for you
        brightnessR = min(brightnessR, 1.0)
        brightnessG = min(brightnessG, 1.0)
        brightnessB = min(brightnessB, 1.0)

        return Triple(brightnessR.toFloat(), brightnessG.toFloat(), brightnessB.toFloat())
    }
}