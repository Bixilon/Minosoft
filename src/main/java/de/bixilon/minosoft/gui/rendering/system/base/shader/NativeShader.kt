/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
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

    fun setFloat(uniformName: String, value: Float)
    fun setInt(uniformName: String, value: Int)
    fun setUInt(uniformName: String, value: Int)
    fun setMat4(uniformName: String, mat4: Mat4)
    fun setVec2(uniformName: String, vec2: Vec2)
    fun setVec3(uniformName: String, vec3: Vec3)
    fun setVec4(uniformName: String, vec4: Vec4)
    fun setArray(uniformName: String, array: Array<*>)
    fun setIntArray(uniformName: String, array: IntArray)
    fun setUIntArray(uniformName: String, array: IntArray)
    fun setCollection(uniformName: String, collection: Collection<*>)
    fun setRGBColor(uniformName: String, color: RGBColor)
    fun setBoolean(uniformName: String, boolean: Boolean)
    fun setTexture(uniformName: String, textureId: Int)
    fun setUniformBuffer(uniformName: String, uniformBuffer: UniformBuffer)

    fun setVec3(uniformName: String, vec3: Vec3d) {
        setVec3(uniformName, Vec3(vec3))
    }

    operator fun set(uniformName: String, data: Any?) {
        data ?: return
        when (data) {
            is Array<*> -> setArray(uniformName, data)
            is IntArray -> setIntArray(uniformName, data)
            is Collection<*> -> setCollection(uniformName, data)
            is Int -> setInt(uniformName, data)
            is Float -> setFloat(uniformName, data)
            is Mat4 -> setMat4(uniformName, data)
            is Vec4 -> setVec4(uniformName, data)
            is Vec3 -> setVec3(uniformName, data)
            is Vec2 -> setVec2(uniformName, data)
            is RGBColor -> setRGBColor(uniformName, data)
            is UniformBuffer -> setUniformBuffer(uniformName, data)
            // ToDo: PNGTexture
            is Boolean -> setBoolean(uniformName, data)
            else -> error("Don't know what todo with uniform type ${data::class.simpleName}!")
        }
    }

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
