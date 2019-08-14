package com.williambl.raycastengine

import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import java.nio.IntBuffer
import javax.swing.Spring.width
import kotlin.math.*


class Renderer() {

    var plane = Pair(0.0, 0.66)

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

            val rayDirX = cos(player.dir * PI / 180) + plane.first*cos(-player.dir)-plane.second*sin(player.dir) * cameraX
            val rayDirY = sin(player.dir * PI / 180) + plane.first*sin(player.dir)+plane.second*cos(-player.dir) * cameraX

            //What tile are we in?
            var mapX = player.x.toInt()
            var mapY = player.y.toInt()

            //length of ray from current position to next x or y-side
            var sideDistX: Double
            var sideDistY: Double

            //Length of ray from one side to next in map
            var deltaDistX = abs(1 / rayDirX)
            var deltaDistY = abs(1 / rayDirX)
            var perpWallDist: Double

            //Direction to go in x and y
            var stepX: Int
            var stepY: Int

            var hit = false //was a wall hit?
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
            while(!hit) {

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
                hit = try {
                    world.map[mapX][mapY]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    true
                }
            }

            perpWallDist = if (side == 0) (mapX - player.x + (1 - stepX) / 2) / rayDirX
            else (mapY - player.y + (1 - stepY) / 2) / rayDirY

            //Calculate height of line to draw on screen
            val lineHeight = (height / perpWallDist).toInt()

            //calculate lowest and highest pixel to fill in current stripe
            var drawStart = -lineHeight / 2 + height / 2
            if (drawStart < 0) drawStart = 0
            var drawEnd = lineHeight / 2 + height / 2
            if (drawEnd >= height) drawEnd = height - 1

            println("$lineHeight")
        }
    }
}