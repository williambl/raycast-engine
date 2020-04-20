package com.williambl.raycastengine.render

import com.williambl.raycastengine.gameobject.Sprite

class SpriteRenderer {

    lateinit var renderableShape: TexturedRenderableShape


    fun render(sprite: Sprite, context: RenderingContext) {
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
                    RenderUtils.getAndCompileShaderProgram("flatTextured"),
                    sprite.texture
            )
        }

        val spriteRelativeX = sprite.x - context.camera.x
        val spriteRelativeY = sprite.y - context.camera.y

        val inv = 1.0 / (context.camera.plane.first * context.camera.dir.second - context.camera.plane.second * context.camera.dir.first)

        val spriteDepth = inv * (-context.camera.plane.second * spriteRelativeX + context.camera.plane.first * spriteRelativeY)
        val spriteCameraSpaceX = (1 + inv * (context.camera.dir.second * spriteRelativeX - context.camera.dir.first * spriteRelativeY))/spriteDepth

        val spriteScreenSpaceX = ((context.width / 2) * (1 + spriteCameraSpaceX)).toInt()

        val depth = try {
            context.zBuffer[spriteScreenSpaceX]
        } catch (e: ArrayIndexOutOfBoundsException) {
            1.0
        }

        if (spriteDepth < 0 || spriteDepth > depth) // Do not render if sprite is behind camera or occluded
            return

        val spriteHeight = 1.0 / spriteDepth
        val spriteWidth = spriteHeight * (sprite.texture.width / sprite.texture.height)

        val brightness = context.worldRenderer.calculateLighting(context.world, sprite.x, sprite.y)

        val columnXMin: Float = (spriteCameraSpaceX - 0.5f*spriteWidth).toFloat()
        val columnXMax: Float = (spriteCameraSpaceX + 0.5f*spriteWidth).toFloat()
        val bottom: Float = (-spriteHeight*0.5f).toFloat()
        val top: Float = (spriteHeight*0.5f).toFloat()

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