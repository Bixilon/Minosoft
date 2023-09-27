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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements

import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDElement

interface HUDBuilder<T : HUDElement> : Identified {
    val ENABLE_KEY_BINDING_NAME: ResourceLocation?
        get() = null
    val ENABLE_KEY_BINDING: KeyBinding?
        get() = null
    val DEFAULT_ENABLED: Boolean
        get() = true

    fun register(gui: GUIRenderer) = Unit
    fun build(guiRenderer: GUIRenderer): T
}
