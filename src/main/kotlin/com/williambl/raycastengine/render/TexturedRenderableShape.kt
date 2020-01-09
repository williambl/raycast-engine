package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL45.*

class TexturedRenderableShape(var vertices: FloatArray, var indices: IntArray, val shaderProgram: Int, val texture: Texture): RenderableShape {

    val vbo: IntArray = intArrayOf(1)
    val vao: IntArray = intArrayOf(1)
    val ebo: IntArray = intArrayOf(1)

    override fun setup() {
        glGenVertexArrays(vao)
        glCreateBuffers(vbo)
        glCreateBuffers(ebo)

        glBindVertexArray(vao[0])

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW)

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
        glEnableVertexAttribArray(0)

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
        glEnableVertexAttribArray(1)

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
        glEnableVertexAttribArray(2)

    }

    override fun render() {
        glUseProgram(shaderProgram)
        texture.bind()
        glBindVertexArray(vao[0])
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }
}