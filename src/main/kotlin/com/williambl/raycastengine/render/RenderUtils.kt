package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL45.*

object RenderUtils {

    fun getAndCompileShaderProgram(shaderName: String): Int {
        return createCompleteShaderProgram(
                this::class.java.getResource("/shaders/$shaderName/$shaderName.vert").readText(),
                this::class.java.getResource("/shaders/$shaderName/$shaderName.frag").readText()
        )
    }

    fun createCompleteShaderProgram(vertexShaderSrc: String, fragmentShaderSrc: String): Int {
        val vertexShader = createAndCompileShader(vertexShaderSrc, GL_VERTEX_SHADER)
        val fragmentShader = createAndCompileShader(fragmentShaderSrc, GL_FRAGMENT_SHADER)
        val shaderProgram = createAndLinkShaderProgram(vertexShader, fragmentShader)
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        return shaderProgram
    }

    fun createAndCompileShader(source: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)
        return shader
    }

    fun createAndLinkShaderProgram(vertexShader: Int, fragmentShader: Int): Int {
        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)
        return shaderProgram
    }
}