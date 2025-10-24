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

import de.bixilon.minosoft.gui.rendering.shader.uniform.ShaderUniform
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader

abstract class Shader(override val native: NativeShader) : AbstractShader {
    private val uniforms: MutableMap<String, ShaderUniform> = mutableMapOf()

    fun unload() {
        native.unload()
        native.context.system.shader -= this
    }

    fun load() {
        native.load()
        native.context.system.shader += this
        for (uniform in uniforms.values) {
            uniform.upload()
        }
    }

    override fun use() {
        native.context.system.shader.shader = this
    }

    fun reload() {
        native.reload()
        for (uniform in uniforms.values) {
            uniform.upload()
        }
    }

    private fun <T : ShaderUniform> T.register(): T {
        val previous = uniforms.put(name, this)

        if (previous != null) {
            throw IllegalStateException("Duplicated uniform: $name")
        }

        return this
    }

    override fun <T : ShaderUniform> uniform(uniform: T) = uniform.register()
}
