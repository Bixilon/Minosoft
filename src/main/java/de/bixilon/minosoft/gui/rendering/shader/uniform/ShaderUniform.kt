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

package de.bixilon.minosoft.gui.rendering.shader.uniform

import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KFunction3
import kotlin.reflect.KProperty

class ShaderUniform<T>(
    private val native: Shader,
    default: T,
    private val name: String,
    private val setter: KFunction3<Shader, String, T, Unit>,
) : ReadWriteProperty<Any, T> {
    private var value = default
    private var upload: Boolean = true

    fun upload() {
        if (!upload) {
            return
        }
        native.use()
        setter(native, name, value)
        upload = false
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value == value) {
            return
        }
        this.value = value
        upload = true
        if (Thread.currentThread() == native.renderWindow.thread) {
            upload()
        }
    }

}
