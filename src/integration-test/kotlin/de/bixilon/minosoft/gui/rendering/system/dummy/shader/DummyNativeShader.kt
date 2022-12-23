/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.dummy.shader

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader

class DummyNativeShader(
    override val renderWindow: RenderWindow,
) : NativeShader {
    override val loaded: Boolean = true
    override val defines: MutableMap<String, Any> = mutableMapOf()
    override val log: String = ""

    override fun load() = Unit

    override fun unload() = Unit

    override fun reload() = Unit

    override fun setFloat(uniformName: String, value: Float) = Unit

    override fun setInt(uniformName: String, value: Int) = Unit

    override fun setUInt(uniformName: String, value: Int) = Unit

    override fun setMat4(uniformName: String, mat4: Mat4) = Unit

    override fun setVec2(uniformName: String, vec2: Vec2) = Unit

    override fun setVec3(uniformName: String, vec3: Vec3) = Unit

    override fun setVec4(uniformName: String, vec4: Vec4) = Unit

    override fun setArray(uniformName: String, array: Array<*>) = Unit

    override fun setIntArray(uniformName: String, array: IntArray) = Unit

    override fun setUIntArray(uniformName: String, array: IntArray) = Unit

    override fun setCollection(uniformName: String, collection: Collection<*>) = Unit

    override fun setRGBColor(uniformName: String, color: RGBColor) = Unit

    override fun setBoolean(uniformName: String, boolean: Boolean) = Unit

    override fun setTexture(uniformName: String, textureId: Int) = Unit

    override fun setUniformBuffer(uniformName: String, uniformBuffer: UniformBuffer) = Unit
}
