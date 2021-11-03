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

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.modding.event.events.TabListEntryChangeEvent
import de.bixilon.minosoft.modding.event.events.TabListInfoChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class TabListHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<TabListElement>(hudRenderer), Drawable {
    private val connection = renderWindow.connection
    override val layout = TabListElement(hudRenderer)

    override val layoutOffset: Vec2i
        get() = Vec2i((hudRenderer.scaledSize.x - layout.size.x) / 2, 20)

    init {
        enabled = false
        layout.prefMaxSize = Vec2i(-1, -1)
    }


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<TabListInfoChangeEvent> {
            layout.header.text = it.header
            layout.footer.text = it.footer
        })
        connection.registerEvent(CallbackEventInvoker.of<TabListEntryChangeEvent> {
            for ((uuid, entry) in it.items) {
                if (entry.remove) {
                    layout.remove(uuid)
                    continue
                }
                layout.update(uuid)
            }
        })

        // ToDo: Also check team changes, scoreboard changes, etc
    }

    override fun draw() {
        // check if content was changed, and we need to re-prepare before drawing
        if (layout.needsApply) {
            layout.forceApply()
        }
    }


    companion object : HUDBuilder<TabListHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:tab_list".toResourceLocation()
        override val ENABLE_KEY_BINDING_NAME: ResourceLocation = "minosoft:enable_tab_list".toResourceLocation()
        override val ENABLE_KEY_BINDING: KeyBinding = KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_TAB),
            ),
        )

        override fun build(hudRenderer: HUDRenderer): TabListHUDElement {
            return TabListHUDElement(hudRenderer)
        }
    }
}
