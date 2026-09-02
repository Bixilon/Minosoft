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

import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import kotlin.reflect.KMutableProperty0

class BooleanDelegateButton(guiRenderer: GUIRenderer, key: Translatable, delegate: KMutableProperty0<Boolean>) : DelegateButton<Boolean>(guiRenderer, key, delegate) {

    override fun formatValue(value: Boolean) = if (value) TextComponent("ON").color(ChatColors.GREEN) else TextComponent("OFF").color(ChatColors.RED) // TODO: i18n

    override fun submit() {
        super.submit()
        delegate.set(!value)
    }
}
