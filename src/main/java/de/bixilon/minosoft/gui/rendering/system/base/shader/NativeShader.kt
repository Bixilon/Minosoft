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

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec4.Vec4
import kotlin.math.max

interface NativeShader {
    val loaded: Boolean
    val context: RenderContext
    val defines: MutableMap<String, Any>

    val log: String

    fun load()
    fun unload()

    fun reload()

    fun use(): NativeShader {
        context.system.shader = this
        return this
    }

    fun setFloat(uniform: String, value: Float)
    fun setInt(uniform: String, value: Int)
    fun setUInt(uniform: String, value: Int)
    fun setMat4(uniform: String, mat4: Mat4)
    fun setVec2(uniform: String, vec2: Vec2)
    fun setVec3(uniform: String, vec3: Vec3)
    fun setVec4(uniform: String, vec4: Vec4)

    @Deprecated("Arrays don't exist natively")
    fun setArray(uniform: String, array: Array<*>)

    @Deprecated("Arrays don't exist natively")
    fun setIntArray(uniform: String, array: IntArray)

    @Deprecated("Arrays don't exist natively")
    fun setUIntArray(uniform: String, array: IntArray)

    @Deprecated("Arrays don't exist natively")
    fun setCollection(uniform: String, collection: Collection<*>)
    fun setRGBColor(uniform: String, color: RGBColor)
    fun setRGBAColor(uniform: String, color: RGBAColor)
    fun setBoolean(uniform: String, boolean: Boolean)
    fun setTexture(uniform: String, textureId: Int)
    fun setUniformBuffer(uniform: String, buffer: UniformBuffer)

    @Deprecated("Implicit datatype conversion")
    fun setVec3(uniform: String, vec3: Vec3d) {
        setVec3(uniform, Vec3(vec3))
    }

    @Deprecated("Explicit datatype")
    operator fun set(uniform: String, data: Any?) {
        if (data == null) return
        when (data) {
            is Array<*> -> setArray(uniform, data)
            is IntArray -> setIntArray(uniform, data)
            is Collection<*> -> setCollection(uniform, data)
            is Int -> setInt(uniform, data)
            is Float -> setFloat(uniform, data)
            is Mat4 -> setMat4(uniform, data)
            is Vec4 -> setVec4(uniform, data)
            is Vec3 -> setVec3(uniform, data)
            is Vec2 -> setVec2(uniform, data)
            is RGBColor -> setRGBColor(uniform, data)
            is RGBAColor -> setRGBAColor(uniform, data)
            is UniformBuffer -> setUniformBuffer(uniform, data)
            // ToDo: PNGTexture
            is Boolean -> setBoolean(uniform, data)
            else -> error("Don't know what todo with uniform type ${data::class.simpleName}!")
        }
    }

    operator fun set(uniform: String, data: Boolean) = setBoolean(uniform, data)
    operator fun set(uniform: String, data: Int) = setInt(uniform, data)
    operator fun set(uniform: String, data: Float) = setFloat(uniform, data)

    operator fun set(name: String, data: RGBColor) = setRGBColor(name, data)
    operator fun set(uniform: String, data: RGBAColor) = setRGBAColor(uniform, data)

    operator fun set(uniform: String, data: UniformBuffer) = setUniformBuffer(uniform, data)

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
