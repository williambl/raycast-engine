package com.williambl.raycastengine.render

import com.williambl.raycastengine.gameobject.Sprite
import org.lwjgl.opengl.GL11.*

class SpriteRenderer {

    fun render(sprite: Sprite, context: RenderingContext) {
        val spriteRelativeX = sprite.x - context.camera.x
        val spriteRelativeY = sprite.y - context.camera.y

        val inv = 1.0 / (context.camera.plane.first * context.camera.dir.second - context.camera.plane.second * context.camera.dir.first)

        val spriteCameraSpaceX = inv * (context.camera.dir.second * spriteRelativeX - context.camera.dir.first * spriteRelativeY)
        val spriteDepth = inv * (-context.camera.plane.second * spriteRelativeX + context.camera.plane.first * spriteRelativeY)

        val spriteScreenSpaceX = ((context.width / 2) * (1 + spriteCameraSpaceX / spriteDepth)).toInt()

        val depth = try {
            context.zBuffer[spriteScreenSpaceX]
        } catch (e: ArrayIndexOutOfBoundsException) {
            1.0
        }

        if (spriteDepth < 0 || spriteDepth > depth) // Do not render if sprite is behind camera or occluded
            return

        val spriteHeight = (context.height / spriteDepth).toInt()
        val spriteWidth = spriteHeight * (sprite.texture.width / sprite.texture.height)

        val brightness = context.worldRenderer.calculateLighting(context.world, sprite.x, sprite.y)

        glPushMatrix()
        glColor3d(brightness.first, brightness.second, brightness.third)

        glAlphaFunc(GL_GREATER, 0.5f)
        glEnable(GL_ALPHA_TEST)

        glEnable(GL_TEXTURE_2D)
        sprite.texture.bind()

        glBegin(GL_QUADS)
        glTexCoord2d(0.0, 1.0); glVertex2i(spriteScreenSpaceX - (spriteWidth / 2), (context.height / 2) - (spriteHeight / 2))
        glTexCoord2d(1.0, 1.0); glVertex2i(spriteScreenSpaceX + (spriteWidth / 2), (context.height / 2) - (spriteHeight / 2))
        glTexCoord2d(1.0, 0.0); glVertex2i(spriteScreenSpaceX + (spriteWidth / 2), (context.height / 2) + (spriteHeight / 2))
        glTexCoord2d(0.0, 0.0); glVertex2i(spriteScreenSpaceX - (spriteWidth / 2), (context.height / 2) + (spriteHeight / 2))

        glEnd()
        glPopMatrix()
    }
}