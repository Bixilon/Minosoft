/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.buttons

import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.observer.ObserveUtil.observer
import de.bixilon.minosoft.config.profile.delegate.types.EnumDelegate
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.util.KUtil.format
import kotlin.reflect.KMutableProperty0

class EnumDelegateButton<T : Enum<T>>(guiRenderer: GUIRenderer, key: Translatable, delegate: KMutableProperty0<T>) : DelegateButton<T>(guiRenderer, key, delegate) {
    private val observer = delegate.observer.cast<EnumDelegate<T>>()

    override fun formatValue(value: T) = value.format()

    override fun submit() {
        super.submit()
        delegate.set(observer.values.nextPort(value))
    }


    @Deprecated("crash in kutil, fixed in kutil 1.31")
    fun <X : Enum<X>> ValuesEnum<X>.nextPort(current: X): X {
        val next = current.ordinal + 1
        if (next >= VALUES.size) {
            return VALUES[0]
        }
        return VALUES[next]
    }
}
