package com.williambl.raycastengine.render

import com.williambl.raycastengine.gameobject.Player

class PlayerRenderer {

    lateinit var renderableShape: TexturedRenderableShape

    val texture = RenderUtils.getOrCreateTexture("/face.png")

    //TODO: work out why this renders a little off from certain directions
    fun render(player: Player, context: RenderingContext) {
        if (!this::renderableShape.isInitialized) {
            renderableShape = TexturedRenderableShape(
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
                    RenderUtils.getOrCreateShaderProgram("flatTextured"),
                    texture
            )
        }

        val playerRelativeX = player.x - context.camera.x
        val playerRelativeY = player.y - context.camera.y

        val inv = 1.0 / (context.camera.plane.first * context.camera.dir.second - context.camera.plane.second * context.camera.dir.first)

        val playerDepth = inv * (-context.camera.plane.second * playerRelativeX + context.camera.plane.first * playerRelativeY)
        val playerCameraSpaceX = (1 + inv * (context.camera.dir.second * playerRelativeX - context.camera.dir.first * playerRelativeY)) / playerDepth

        val playerScreenSpaceX = ((context.width / 2) * (1 + playerCameraSpaceX)).toInt()

        val depth = try {
            context.zBuffer[playerScreenSpaceX]
        } catch (e: ArrayIndexOutOfBoundsException) {
            1.0
        }

        if (playerDepth < 0 || playerDepth > depth) // Do not render if player is behind camera or occluded
            return

        val playerHeight = 1.0 / playerDepth
        val playerWidth = playerHeight * (texture.width / texture.height)

        val brightness = context.worldRenderer.calculateLighting(context.world, player.x, player.y)

        val columnXMin: Float = (playerCameraSpaceX - 0.5f * playerWidth).toFloat()
        val columnXMax: Float = (playerCameraSpaceX + 0.5f * playerWidth).toFloat()
        val bottom: Float = (-playerHeight * 0.5f).toFloat()
        val top: Float = (playerHeight * 0.5f).toFloat()

        renderableShape.vertices = floatArrayOf(
                columnXMin, bottom, 0.0f, brightness.first, brightness.second, brightness.third, 0.0f, 1.0f,
                columnXMax, bottom, 0.0f, brightness.first, brightness.second, brightness.third, 1.0f, 1.0f,
                columnXMax, top, 0.0f, brightness.first, brightness.second, brightness.third, 1.0f, 0.0f,
                columnXMin, top, 0.0f, brightness.first, brightness.second, brightness.third, 0.0f, 0.0f
        )
        renderableShape.setup()
        renderableShape.render()
    }
}