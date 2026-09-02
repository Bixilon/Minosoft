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

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import kotlin.reflect.KMutableProperty0

abstract class DelegateButton<T>(guiRenderer: GUIRenderer, key: Translatable, val delegate: KMutableProperty0<T>) : ButtonElement(guiRenderer, "<TBA>", onSubmit = {}) {
    private val key = ChatComponent.of(key, translator = guiRenderer.session.language)
    protected var value: T = delegate.get()
        private set

    init {
        delegate.observe(this) { value = it; updateText() }
    }

    init {
        updateText()
    }

    protected fun updateText() {
        textElement.text = BaseComponent(key, ": ", formatValue(value))
    }

    protected abstract fun formatValue(value: T): ChatComponent
}
