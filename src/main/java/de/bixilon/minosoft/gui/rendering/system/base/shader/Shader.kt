/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.shader

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.OpenGLUniformBuffer
import de.bixilon.minosoft.util.Previous
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec4.Vec4
import kotlin.math.max

interface Shader {
    val loaded: Boolean
    val renderWindow: RenderWindow
    val resourceLocation: ResourceLocation
    val uniforms: List<String>

    val log: String

    fun load()

    fun use(): Shader {
        renderWindow.renderSystem.shader = this
        return this
    }

    fun setFloat(uniformName: String, value: Float)
    fun setInt(uniformName: String, value: Int)
    fun setMat4(uniformName: String, mat4: Mat4)
    fun setVec2(uniformName: String, vec2: Vec2)
    fun setVec3(uniformName: String, vec3: Vec3)
    fun setVec4(uniformName: String, vec4: Vec4)
    fun setArray(uniformName: String, array: Array<*>)
    fun setRGBColor(uniformName: String, color: RGBColor)
    fun setTexture(uniformName: String, textureId: Int)
    fun setUniformBuffer(uniformName: String, uniformBuffer: OpenGLUniformBuffer)

    fun setVec3(uniformName: String, vec3: Vec3d) {
        setVec3(uniformName, Vec3(vec3))
    }

    operator fun set(uniformName: String, data: Any?) {
        data ?: return
        when (data) {
            is Previous<*> -> this[uniformName] = data.value
            is Array<*> -> setArray(uniformName, data)
            is Int -> setInt(uniformName, data)
            is Float -> setFloat(uniformName, data)
            is Mat4 -> setMat4(uniformName, data)
            is Vec4 -> setVec4(uniformName, data)
            is Vec3 -> setVec3(uniformName, data)
            is Vec2 -> setVec2(uniformName, data)
            is RGBColor -> setRGBColor(uniformName, data)
            is OpenGLUniformBuffer -> setUniformBuffer(uniformName, data)
            // ToDo: PNGTexture
            else -> error("Don't know what todo with uniform type ${data::class.simpleName}!")
        }
    }

    companion object {
        val DEFAULT_DEFINES: Map<String, (renderWindow: RenderWindow) -> Any?> = mapOf(
            "ANIMATED_TEXTURE_COUNT" to {
                max(it.textureManager.staticTextures.animator.size, 1)
            }
        )
    }
}
