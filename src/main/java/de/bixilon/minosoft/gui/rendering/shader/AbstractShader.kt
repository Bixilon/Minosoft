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

package de.bixilon.minosoft.gui.rendering.shader

import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.shader.uniform.ShaderUniform
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

interface AbstractShader {
    val native: NativeShader

    fun <T> uniform(name: String, default: T, type: ShaderSetter<T>): ShaderUniform<T>

    fun uniform(name: String, default: Float) = uniform(name, default, NativeShader::set)
    fun uniform(name: String, default: Boolean) = uniform(name, default, NativeShader::set)

    fun uniform(name: String, default: RGBColor) = uniform(name, default, NativeShader::set)
    fun uniform(name: String, default: RGBAColor) = uniform(name, default, NativeShader::set)

    fun uniform(name: String, default: Vec2) = uniform(name, default, NativeShader::set)
    fun uniform(name: String, default: Vec3) = uniform(name, default, NativeShader::set)
    fun uniform(name: String, default: Vec4) = uniform(name, default, NativeShader::set)

    fun uniform(name: String, default: Mat4) = uniform(name, default, NativeShader::set)

    fun uniform(name: String, default: UniformBuffer) = uniform(name, default, NativeShader::set)
}
