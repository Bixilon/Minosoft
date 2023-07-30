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

package de.bixilon.minosoft.gui.rendering.shader

import de.bixilon.minosoft.gui.rendering.shader.types.TransparentShader
import de.bixilon.minosoft.gui.rendering.shader.uniform.ShaderUniform

abstract class Shader : AbstractShader {
    private val uniforms: MutableMap<String, ShaderUniform<*>> = mutableMapOf()

    fun unload() {
        native.unload()
        native.context.system.shaders -= this
    }

    fun load() {
        if (this is TransparentShader && this.transparent) {
            native.defines["TRANSPARENT"] = " "
        }
        native.load()
        native.context.system.shaders += this
        for (uniform in uniforms.values) {
            uniform.upload()
        }
    }

    fun use() {
        native.use()
    }

    fun reload() {
        native.reload()
        for (uniform in uniforms.values) {
            uniform.forceUpload()
        }
    }


    override fun <T> uniform(name: String, default: T, type: ShaderSetter<T>): ShaderUniform<T> {
        val uniform = ShaderUniform(native, default, name, type)
        val previous = uniforms.put(name, uniform)

        if (previous != null) {
            throw IllegalStateException("Duplicated uniform: $name")
        }

        return uniform
    }
}
