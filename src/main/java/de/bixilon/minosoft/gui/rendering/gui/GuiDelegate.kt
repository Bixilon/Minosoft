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

package de.bixilon.minosoft.gui.rendering.gui

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import kotlin.reflect.KProperty

class GuiDelegate<T>(
    var value: T,
) {
    var rendering = value

    inline operator fun getValue(element: Element, property: KProperty<*>): T = value

    operator fun setValue(element: Element, property: KProperty<*>, value: T) {
        if (this.value == value) return

        this.value = value
        element.invalidate()
    }

    fun acknowledge(): T {
        val value = this.value
        this.rendering = value

        return value
    }


    inline fun rendering(): T = rendering
}
