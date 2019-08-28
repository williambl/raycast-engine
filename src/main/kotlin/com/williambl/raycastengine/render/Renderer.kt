package com.williambl.raycastengine.render

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.World
import com.williambl.raycastengine.events.Tickable
import com.williambl.raycastengine.gameobject.Camera
import com.williambl.raycastengine.gameobject.Light
import com.williambl.raycastengine.gameobject.Sprite
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.opengl.GL11.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow


class Renderer(val world: World, val camera: Camera) : Tickable {

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

        renderBackground(width, height)

        renderWorld(world, camera, width, height, zBuffer)

        renderSprites(world, camera, width, height, zBuffer)
    }

    private fun renderBackground(width: Int, height: Int) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glColor3d(0.6, 0.6, 0.6)
        glBegin(GL_QUADS)
        glVertex2i(0, 0)
        glVertex2i(width, 0)
        glVertex2i(width, height/2)
        glVertex2i(0, height/2)
        glEnd()

        glColor3d(0.1, 0.1, 0.1)
        glVertex2i(0, height/2)
        glVertex2i(width, height/2)
        glVertex2i(width, height)
        glVertex2i(0, height)
        glEnd()

        glPopMatrix()

    }

    private fun renderWorld(world: World, camera: Camera, width: Int, height: Int, zBuffer: Array<Double>) {
        for (column in 0..width) {
            val cameraX = 2 * column / width.toDouble() - 1 // X-coord in camera space

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

            val lineHeight = (height / perpWallDist).toInt()

            // Calculate lowest and highest pixel to fill in current column
            val bottom = (-lineHeight / 2 + height / 2)
            val top = (lineHeight / 2 + height / 2)

            // Calculate which column of texture to use
            var textureX = if (side == 0) camera.y + perpWallDist * rayDirY
            else camera.x + perpWallDist * rayDirX
            textureX -= floor((textureX))

            val pixelWidth = 1/world.wallTextures[result].width

            // Work out how light it should be

            val brightness = calculateLighting(world, (camera.x + perpWallDist * rayDirX), (camera.y + perpWallDist * rayDirY))

            // Write to the z-buffer
            zBuffer[column] = perpWallDist

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

    private fun renderSprites(world: World, camera: Camera, width: Int, height: Int, zBuffer: Array<Double>) {
        val sprites = world.getGameObjectsOfType(Sprite::class.java) as ArrayList

        sprites.sortBy {
            abs(it.x - camera.x).pow(2) + abs(it.y - camera.y).pow(2)
        }
        sprites.reverse()

        sprites.forEach {
            val spriteRelativeX = it.x - camera.x
            val spriteRelativeY = it.y - camera.y

            val inv = 1.0 / (camera.plane.first * camera.dir.second - camera.plane.second * camera.dir.first)

            val spriteCameraSpaceX = inv * (camera.dir.second * spriteRelativeX - camera.dir.first * spriteRelativeY)
            val spriteDepth = inv * (-camera.plane.second * spriteRelativeX + camera.plane.first * spriteRelativeY)

            val spriteScreenSpaceX = ((width / 2) * (1 + spriteCameraSpaceX / spriteDepth)).toInt()

            val depth = try {
                zBuffer[spriteScreenSpaceX]
            } catch (e: ArrayIndexOutOfBoundsException) {
                0.0
            }

            if (spriteDepth < 0 || spriteDepth > depth) // Do not render if sprite is behind camera or occluded
                return@forEach

            val spriteHeight = (height / spriteDepth).toInt()
            val spriteWidth = spriteHeight * (it.texture.width / it.texture.height)

            val brightness = calculateLighting(world, it.x, it.y)

            glPushMatrix()
            glColor3d(brightness.first, brightness.second, brightness.third)

            glAlphaFunc(GL_GREATER, 0.5f)
            glEnable(GL_ALPHA_TEST)

            glEnable(GL_TEXTURE_2D)
            it.texture.bind()

            glBegin(GL_QUADS)
            glTexCoord2d(0.0, 1.0); glVertex2i(spriteScreenSpaceX - (spriteWidth / 2), (height / 2) - (spriteHeight / 2))
            glTexCoord2d(1.0, 1.0); glVertex2i(spriteScreenSpaceX + (spriteWidth / 2), (height / 2) - (spriteHeight / 2))
            glTexCoord2d(1.0, 0.0); glVertex2i(spriteScreenSpaceX + (spriteWidth / 2), (height / 2) + (spriteHeight / 2))
            glTexCoord2d(0.0, 0.0); glVertex2i(spriteScreenSpaceX - (spriteWidth / 2), (height / 2) + (spriteHeight / 2))

            glEnd()
            glPopMatrix()
        }
    }

    private fun calculateLighting(world: World, x: Double, y: Double): Triple<Double, Double, Double> {
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