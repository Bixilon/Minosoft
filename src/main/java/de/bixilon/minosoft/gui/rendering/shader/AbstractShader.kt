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

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.shader.uniform.AnyShaderUniform
import de.bixilon.minosoft.gui.rendering.shader.uniform.ShaderUniform
import de.bixilon.minosoft.gui.rendering.shader.uniform.color.RGBAColorShaderUniform
import de.bixilon.minosoft.gui.rendering.shader.uniform.color.RGBColorShaderUniform
import de.bixilon.minosoft.gui.rendering.shader.uniform.mat.Mat4fShaderUniform
import de.bixilon.minosoft.gui.rendering.shader.uniform.primitive.FloatShaderUniform
import de.bixilon.minosoft.gui.rendering.shader.uniform.vec.Vec3fShaderUniform
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader

interface AbstractShader {
    val native: NativeShader

    fun <T : ShaderUniform> uniform(uniform: T): T
    fun <T> uniform(name: String, default: T, type: ShaderSetter<T>) = uniform(AnyShaderUniform(native, default, name, type))

    fun uniform(name: String, default: Float) = uniform(FloatShaderUniform(native, default, name))
    fun uniform(name: String, default: Boolean) = uniform(name, default, NativeShader::set)

    fun uniform(name: String, default: RGBColor) = uniform(RGBColorShaderUniform(native, default, name))
    fun uniform(name: String, default: RGBAColor) = uniform(RGBAColorShaderUniform(native, default, name))

    fun uniform(name: String, default: Vec2f) = uniform(name, default, NativeShader::set)
    fun uniform(name: String, default: Vec3f) = uniform(Vec3fShaderUniform(native, default, name))
    fun uniform(name: String, default: Vec4f) = uniform(name, default, NativeShader::set)

    fun uniform(name: String, default: Mat4f) = uniform(Mat4fShaderUniform(native, default, name))

    fun uniform(name: String, default: UniformBuffer) = uniform(name, default, NativeShader::set)
}
