/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import kotlin.math.max

interface NativeShader {
    val loaded: Boolean
    val context: RenderContext
    val defines: MutableMap<String, Any>

    fun load()
    fun unload()

    fun reload()

    fun setBoolean(uniform: String, boolean: Boolean)
    fun setFloat(uniform: String, value: Float)

    fun setInt(uniform: String, value: Int)
    fun setUInt(uniform: String, value: Int)

    fun setMat4f(uniform: String, mat4: Mat4f)

    fun setVec2f(uniform: String, vec2: Vec2f)
    fun setVec3f(uniform: String, vec3: Vec3f)
    fun setVec4f(uniform: String, vec4: Vec4f)

    fun setRGBColor(uniform: String, color: RGBColor)
    fun setRGBAColor(uniform: String, color: RGBAColor)

    fun setTexture(uniform: String, textureId: Int)

    fun setUniformBuffer(uniform: String, buffer: UniformBuffer)

    operator fun set(uniform: String, value: Boolean) = setBoolean(uniform, value)
    operator fun set(uniform: String, value: Float) = setFloat(uniform, value)

    operator fun set(uniform: String, mat4: Mat4f) = setMat4f(uniform, mat4)

    operator fun set(uniform: String, vec2: Vec2f) = setVec2f(uniform, vec2)
    operator fun set(uniform: String, vec3: Vec3f) = setVec3f(uniform, vec3)
    operator fun set(uniform: String, vec4: Vec4f) = setVec4f(uniform, vec4)

    operator fun set(name: String, value: RGBColor) = setRGBColor(name, value)
    operator fun set(uniform: String, value: RGBAColor) = setRGBAColor(uniform, value)

    operator fun set(uniform: String, value: UniformBuffer) = setUniformBuffer(uniform, value)

    companion object {
        val DEFAULT_DEFINES: Map<String, (context: RenderContext) -> Any?> = mapOf(
            "ANIMATED_TEXTURE_COUNT" to {
                max(it.textures.static.animator.size, 1)
            }
        )

        fun ResourceLocation.shader(): ResourceLocation {
            return ResourceLocation(namespace, "rendering/shader/${path.replace("(\\w+)\\.\\w+".toRegex(), "$1")}/${path.split("/").last()}")
        }
    }
}
