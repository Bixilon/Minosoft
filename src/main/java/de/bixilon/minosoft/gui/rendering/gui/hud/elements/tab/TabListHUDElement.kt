/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDElement
import de.bixilon.minosoft.modding.event.events.TabListInfoChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class TabListHUDElement(hudRenderer: HUDRenderer) : HUDElement<TabListElement>(hudRenderer) {
    private val connection = renderWindow.connection
    override val layout = TabListElement(hudRenderer)


    override val layoutOffset: Vec2i
        get() = Vec2i((hudRenderer.scaledSize.x - layout.size.x) / 2, 20)

    init {
        layout.prefMaxSize = Vec2i(-1, -1)
    }


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<TabListInfoChangeEvent> {
            layout.header.text = it.header
            layout.footer.text = it.footer
        })
    }


    companion object : HUDBuilder<TabListHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:tab_list".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): TabListHUDElement {
            return TabListHUDElement(hudRenderer)
        }
    }
}
