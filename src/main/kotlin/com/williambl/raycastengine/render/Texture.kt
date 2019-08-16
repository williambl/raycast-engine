package com.williambl.raycastengine.render

import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.opengl.GL11.*
import java.io.IOException
import java.nio.ByteBuffer


class Texture(val location: String) {

    private val id: Int

    val width: Int
    val height: Int

    init {
        val decoder = try {
            PNGDecoder(this::class.java.getResourceAsStream(location))
        } catch (e: IOException) {
            println("\"$location\" is not a valid texture, skipping")
            null
        } catch (e: NullPointerException) {
            println("\"$location\" is not a valid texture, skipping")
            null
        }

        if (decoder != null) {
            width = decoder.width
            height = decoder.height

            val buffer = ByteBuffer.allocateDirect(4 * width * height)
            decoder.decode(buffer, width * 4, PNGDecoder.Format.RGBA)
            buffer.flip()

            glEnable(GL_TEXTURE_2D)

            id = glGenTextures()

            glBindTexture(GL_TEXTURE_2D, id)

            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        } else {
            id = -1
            width = -1
            height = -1
        }
    }

    fun bind() {
        if (id == -1) {
            println("\"$location\" is not a valid texture")
            return
        }
        glBindTexture(GL_TEXTURE_2D, id)
    }
}