package com.williambl.raycastengine.render

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.World
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Camera
import com.williambl.raycastengine.gameobject.GameObject
import com.williambl.raycastengine.gameobject.Light
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.opengl.GL11.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow


class WorldRenderer(val world: World, val camera: Camera) : Tickable {

    override fun tick() {
        render(world, camera)
    }

    private fun render(world: World, camera: Camera) {
        val widthB = BufferUtils.createIntBuffer(1)
        val heightB = BufferUtils.createIntBuffer(1)
        glfwGetWindowSize(Main.window, widthB, heightB)
        val width = widthB[0]
        val height = heightB[0]

        val zBuffer = Array(width + 1) { 0.0 }

        val context = RenderingContext(world, camera, width, height, zBuffer, this)

        renderBackground(context)

        renderWorld(context)

        renderRenderables(context)
    }

    private fun renderBackground(context: RenderingContext) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glColor3d(context.world.floorColor.first, context.world.floorColor.second, context.world.floorColor.third)
        glBegin(GL_QUADS)
        glVertex2i(0, 0)
        glVertex2i(context.width, 0)
        glVertex2i(context.width, context.height/2)
        glVertex2i(0, context.height/2)
        glEnd()

        glBegin(GL_QUADS)
        glColor3d(context.world.skyColor.first, context.world.skyColor.second, context.world.skyColor.third)
        glVertex2i(0, context.height/2)
        glVertex2i(context.width, context.height/2)
        glVertex2i(context.width, context.height)
        glVertex2i(0, context.height)
        glEnd()

        glPopMatrix()

    }

    private fun renderWorld(context: RenderingContext) {
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

            val lineHeight = (context.height / perpWallDist).toInt()

            // Calculate lowest and highest pixel to fill in current column
            val bottom = (-lineHeight / 2 + context.height / 2)
            val top = (lineHeight / 2 + context.height / 2)

            // Calculate which column of texture to use
            var textureX = if (side == 0) camera.y + perpWallDist * rayDirY
            else camera.x + perpWallDist * rayDirX
            textureX -= floor((textureX))

            val pixelWidth = 1/world.wallTextures[result].width

            // Work out how light it should be

            val brightness = calculateLighting(world, (camera.x + perpWallDist * rayDirX), (camera.y + perpWallDist * rayDirY))

            // Write to the z-buffer
            context.zBuffer[column] = perpWallDist

            // Draw it

            glPushMatrix()
            glColor3d(brightness.first, brightness.second, brightness.third)
            glEnable(GL_TEXTURE_2D)
            world.wallTextures[result].bind()

            glBegin(GL_QUADS)
            glTexCoord2d(textureX, 1.0); glVertex2i(column, bottom)
            glTexCoord2d(textureX + pixelWidth, 1.0); glVertex2i(column + 1, bottom)
            glTexCoord2d(textureX + pixelWidth, 0.0); glVertex2i(column + 1, top)
            glTexCoord2d(textureX, 0.0); glVertex2i(column, top)

            glEnd()
            glPopMatrix()
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

    internal fun calculateLighting(world: World, x: Double, y: Double): Triple<Double, Double, Double> {
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

        return Triple(brightnessR, brightnessG, brightnessB)
    }
}