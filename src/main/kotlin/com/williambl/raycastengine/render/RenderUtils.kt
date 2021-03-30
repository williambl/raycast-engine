package com.williambl.raycastengine.render

import org.lwjgl.opengl.GL45.*

object RenderUtils {

    private val textures: MutableMap<String, Texture> = mutableMapOf()
    private val shaders: MutableMap<String, ShaderProgram> = mutableMapOf()

    /**
     * Loads a [ShaderProgram] from a resource named [shaderName].
     *
     * Shader programs are in the following format:
     * ```
     * shaders
     * ├── shaderName
     * │   ├── shaderName.frag
     * │   └── shaderName.vert
     * ```
     */
    fun getOrCreateShaderProgram(shaderName: String): ShaderProgram {
        return shaders.getOrPut(shaderName) {
            createCompleteShaderProgram(
                this::class.java.getResource("/shaders/$shaderName/$shaderName.vert").readText(),
                this::class.java.getResource("/shaders/$shaderName/$shaderName.frag").readText(),
                shaderName
        )}
    }

    /**
     * Loads a [Texture] from the jar resource at [location].
     *
     * The texture file can be in any format supported by [org.lwjgl.stb.STBImage].
     */
    fun getOrCreateTexture(location: String): Texture {
        return textures.getOrPut(location) { Texture(location) }
    }

    private fun createCompleteShaderProgram(vertexShaderSrc: String, fragmentShaderSrc: String, name: String): ShaderProgram {
        val vertexId = compileShader(vertexShaderSrc, GL_VERTEX_SHADER)
        val fragmentId = compileShader(fragmentShaderSrc, GL_FRAGMENT_SHADER)
        val shaderProgramId = createAndLinkShaderProgram(vertexId, fragmentId)
        glDeleteShader(vertexId)
        glDeleteShader(fragmentId)
        return ShaderProgram(name, shaderProgramId)
    }

    private fun compileShader(source: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)
        return shader
    }

    private fun createAndLinkShaderProgram(vertexShader: Int, fragmentShader: Int): Int {
        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)
        return shaderProgram
    }
}