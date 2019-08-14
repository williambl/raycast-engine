package com.williambl.raycastengine

import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.opengl.GL11.*
import kotlin.math.abs


class Renderer() {


    fun render(world: World, player: Player) {
        val widthB = BufferUtils.createIntBuffer(1)
        val heightB = BufferUtils.createIntBuffer(1)
        glfwGetWindowSize(Main.window, widthB, heightB)
        val width = widthB[0]
        val height = heightB[0]
        println(width)
        println(height)
        println()
        println()


        for (column in 0..width) {
            val cameraX = 2 * column / width.toDouble() - 1 // x-coord in camera space

            val rayDirX = player.dir.first + player.plane.first * cameraX
            val rayDirY = player.dir.second + player.plane.second * cameraX

            //What tile are we in?
            var mapX = player.x.toInt()
            var mapY = player.y.toInt()

            //length of ray from current position to next x or y-side
            var sideDistX: Double
            var sideDistY: Double

            //Length of ray from one side to next in map
            var deltaDistX = abs(1 / rayDirX)
            var deltaDistY = abs(1 / rayDirY)
            var perpWallDist: Double

            //Direction to go in x and y
            var stepX: Int
            var stepY: Int

            var result = 0 //was a wall hit?
            var side = 0 //was the wall vertical or horizontal

            //Figure out the step direction and initial distance to a side
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

            //Loop to find where the ray hits a wall
            while(result == 0) {

                //Jump to next square
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

                //Check if ray has hit a wall
                result = try {
                    world.map[mapX][mapY]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    0
                    break
                }
            }

            perpWallDist = if (side == 0) (mapX - player.x + (1 - stepX) / 2) / rayDirX
            else (mapY - player.y + (1 - stepY) / 2) / rayDirY

            //Calculate height of line to draw on screen
            val lineHeight = (height / perpWallDist).toInt()

            //calculate lowest and highest pixel to fill in current stripe
            var bottom = (-lineHeight / 2 + height / 2)
            if (bottom < 0) bottom = 0
            var top = (lineHeight / 2 + height / 2)
            if (top >= height) top = (height - 1)

            //println("$lineHeight")

            glPushMatrix()
            glColor3f(0.0f, 1.0f * (lineHeight / height.toFloat()), 1.0f)

            glBegin(GL_QUADS)
            glVertex2i(column, bottom)
            glVertex2i(column+1, bottom)
            glVertex2i(column+1, top)
            glVertex2i(column, top)

            glEnd()
            glPopMatrix()
        }
    }
}