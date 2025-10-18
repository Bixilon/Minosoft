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

package de.bixilon.minosoft.gui.rendering.shader.uniform

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.minosoft.gui.rendering.shader.ShaderSetter
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ShaderUniform<T>(
    private val native: NativeShader,
    default: T,
    private val name: String,
    private val setter: ShaderSetter<T>,
) : ReadWriteProperty<Any, T> {
    private var value = default

    fun upload() {
        native.use()
        setter.set(native, name, value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        assert(Thread.currentThread() == native.context.thread) { "Can not call shader setters from other threads!" }
        if (value !is Mat4f && this.value == value) { // TODO: This is a hack, because mostly matrices are set unsafe (they are mutable) and the check is then always false
            return
        }
        this.value = value
        upload()
    }
}
