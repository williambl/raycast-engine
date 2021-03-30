package com.williambl.raycastengine.render

import com.williambl.raycastengine.util.freeMemoryManaged
import com.williambl.raycastengine.util.toByteBuffer
import com.williambl.raycastengine.util.toMemoryManaged
import org.lwjgl.opengl.GL45.*
import org.lwjgl.stb.STBImage
import java.io.IOException

/**
 * A representation of a texture.
 *
 * The texture is loaded from the jar resource at [location], from any format supported by [STBImage].
 *
 * Do not call the constructor, use [RenderUtils.getOrCreateTexture] instead.
 */
class Texture(val location: String) {

    private var id: Int = -1
        get() {
            if (!isInitialised) init(); return field
        }

    var width: Int = 0
        get() {
            if (!isInitialised) init(); return field
        }
    var height: Int = 0
        get() {
            if (!isInitialised) init(); return field
        }

    var isInitialised = false

    private fun init() {
        isInitialised = true

        val imageData = try {
            this::class.java.getResourceAsStream(location).toByteBuffer().toMemoryManaged()
        } catch (e: IOException) {
            println("\"$location\" is not a valid texture.")
            return
        }

        val widthBuffer = IntArray(1)
        val heightBuffer = IntArray(1)
        val channelsBuffer = IntArray(1)

        val texture = STBImage.stbi_load_from_memory(imageData, widthBuffer, heightBuffer, channelsBuffer, 4)

        imageData.freeMemoryManaged()

        if (texture == null) {
            println("\"$location\" is not a valid texture: ${STBImage.stbi_failure_reason()}")
            return
        }

        width = widthBuffer.first()
        height = heightBuffer.first()

        id = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, id)

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texture)
    }

    fun bind() {
        if (id == -1) {
            println("\"$location\" is not a valid texture")
            return
        }
        glBindTexture(GL_TEXTURE_2D, id)
    }
}