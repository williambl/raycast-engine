package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL20.glUseProgram

/**
 * A representation of an OpenGL Shader Program (i.e. a fragment and vertex shader pair).
 *
 * Do not call the constructor, use [RenderSystem.getOrCreateShaderProgram] instead.
 */
class ShaderProgram(val name: String, private val id: Int) {
    fun use() {
        glUseProgram(id)
    }
}