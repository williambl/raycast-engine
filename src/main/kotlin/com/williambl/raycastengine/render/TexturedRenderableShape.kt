package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL45.*

class TexturedRenderableShape(var vertices: FloatArray, var indices: IntArray, val shaderProgram: ShaderProgram, var texture: Texture): RenderableShape {

    val vbo: IntArray = intArrayOf(1)
    val vao: IntArray = intArrayOf(1)
    val ebo: IntArray = intArrayOf(1)

    var isSetup = false

    override fun setup() {
        if (isSetup) {
            glBindVertexArray(vao[0])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indices)
            return
        }

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
        isSetup = true
    }

    override fun render() {
        shaderProgram.use()
        texture.bind()
        glBindVertexArray(vao[0])
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }
}