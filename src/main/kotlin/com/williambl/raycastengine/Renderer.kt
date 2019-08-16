package com.williambl.raycastengine

import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.opengl.GL11.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow


class Renderer : Tickable {

    override fun tick() {
        render(Main.world, Main.player)
    }

    private fun render(world: World, player: Player) {
        val widthB = BufferUtils.createIntBuffer(1)
        val heightB = BufferUtils.createIntBuffer(1)
        glfwGetWindowSize(Main.window, widthB, heightB)
        val width = widthB[0]
        val height = heightB[0]

        renderBackground(width, height)

        renderWorld(world, player, width, height)
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

    private fun renderWorld(world: World, player: Player, width: Int, height: Int) {
        for (column in 0..width) {
            val cameraX = 2 * column / width.toDouble() - 1 // X-coord in camera space

            val rayDirX = player.dir.first + player.plane.first * cameraX
            val rayDirY = player.dir.second + player.plane.second * cameraX

            // Coords of current square
            var mapX = player.x.toInt()
            var mapY = player.y.toInt()

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
                sideDistX = (player.x - mapX) * deltaDistX
            }
            else
            {
                stepX = 1
                sideDistX = (mapX + 1.0 - player.x) * deltaDistX
            }
            if (rayDirY < 0)
            {
                stepY = -1
                sideDistY = (player.y - mapY) * deltaDistY
            }
            else
            {
                stepY = 1
                sideDistY = (mapY + 1.0 - player.y) * deltaDistY
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
            perpWallDist = if (side == 0) (mapX - player.x + (1 - stepX) / 2) / rayDirX
            else (mapY - player.y + (1 - stepY) / 2) / rayDirY

            val lineHeight = (height / perpWallDist).toInt()

            // Calculate lowest and highest pixel to fill in current column
            val bottom = (-lineHeight / 2 + height / 2)
            val top = (lineHeight / 2 + height / 2)

            // Calculate which column of texture to use
            var textureX = if (side == 0) player.y + perpWallDist * rayDirY
            else player.x + perpWallDist * rayDirX
            textureX -= floor((textureX))

            val pixelWidth = 1/world.wallTextures[result].width

            // Work out how light it should be

            var brightness = 0.0
            world.lights.forEach {
                brightness += (1 / (abs(it.x - (player.x + perpWallDist * rayDirX)).pow(2) + abs(it.y - (player.y + perpWallDist * rayDirY)).pow(2))) * it.strength
            }

            brightness = min(brightness, 1.0) // No HDR for you

            // Draw it

            glPushMatrix()
            glColor3d(brightness, brightness, brightness)
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
}