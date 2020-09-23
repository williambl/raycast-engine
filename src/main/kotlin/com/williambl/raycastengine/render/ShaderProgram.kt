package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL20.glUseProgram

class ShaderProgram(val name: String, private val id: Int) {
    fun use() {
        glUseProgram(id)
    }
}