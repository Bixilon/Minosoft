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

package de.bixilon.minosoft.gui.rendering.system.dummy.shader

import glm_.mat4x4.Mat4
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import glm_.vec4.Vec4
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader

class DummyNativeShader(
    override val context: RenderContext,
) : NativeShader {
    override val loaded: Boolean = true
    override val defines: MutableMap<String, Any> = mutableMapOf()
    override val log: String = ""

    override fun load() = Unit

    override fun unload() = Unit

    override fun reload() = Unit

    override fun setFloat(uniform: String, value: Float) = Unit

    override fun setInt(uniform: String, value: Int) = Unit

    override fun setUInt(uniform: String, value: Int) = Unit

    override fun setMat4(uniform: String, mat4: Mat4f) = Unit

    override fun setVec2(uniform: String, vec2: Vec2f) = Unit

    override fun setVec3(uniform: String, vec3: Vec3f) = Unit

    override fun setVec4(uniform: String, vec4: Vec4f) = Unit

    override fun setRGBColor(uniform: String, color: RGBColor) = Unit
    override fun setRGBAColor(uniform: String, color: RGBAColor) = Unit

    override fun setBoolean(uniform: String, boolean: Boolean) = Unit

    override fun setTexture(uniform: String, textureId: Int) = Unit

    override fun setUniformBuffer(uniform: String, buffer: UniformBuffer) = Unit
}
