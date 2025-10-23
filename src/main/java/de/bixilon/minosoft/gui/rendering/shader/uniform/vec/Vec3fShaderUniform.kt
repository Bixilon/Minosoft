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

package de.bixilon.minosoft.gui.rendering.shader.uniform.vec

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.shader.AbstractShader
import de.bixilon.minosoft.gui.rendering.shader.uniform.ShaderUniform
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import kotlin.reflect.KProperty

class Vec3fShaderUniform(
    native: AbstractShader,
    default: Vec3f,
    name: String,
) : ShaderUniform(native, name) {
    private var value = default

    override fun upload() {
        super.upload()
        shader.native.setVec3f(name, value)
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): Vec3f {
        return value
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: Vec3f) {
        assert(Thread.currentThread() == shader.native.context.thread) { "Can not call shader setters from other threads!" }
        if (this.value == value) return
        this.value = value
        upload()
    }
}
