package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL45.*

class ColouredRenderableShape(val vertices: FloatArray, val indices: IntArray, val shaderProgram: ShaderProgram): RenderableShape {

    val vbo: IntArray = intArrayOf(1)
    val vao: IntArray = intArrayOf(1)
    val ebo: IntArray = intArrayOf(1)

    val vertexCount = indices.size

    override fun setup() {
        glGenVertexArrays(vao)
        glCreateBuffers(vbo)
        glCreateBuffers(ebo)

        glBindVertexArray(vao[0])

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0)
        glEnableVertexAttribArray(0)

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4)
        glEnableVertexAttribArray(1)

    }

    override fun render() {
        shaderProgram.use()
        glBindVertexArray(vao[0])
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }
}