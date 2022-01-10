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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.other.game.event.handlers.gamemode.GamemodeChangeEvent
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.ExperienceChangeEvent
import de.bixilon.minosoft.modding.event.events.SelectHotbarSlotEvent
import de.bixilon.minosoft.modding.event.events.container.ContainerRevisionChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class HotbarHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<HotbarElement>(hudRenderer) {
    private val connection = renderWindow.connection
    override lateinit var layout: HotbarElement


    override val layoutOffset: Vec2i
        get() = Vec2i((guiRenderer.scaledSize.x - layout.size.x) / 2, guiRenderer.scaledSize.y - layout.size.y)

    override fun postInit() {
        layout = HotbarElement(guiRenderer) // ToDo: The base uses image elements, that are available after init stage
        layout.prefMaxSize = Vec2i(-1, -1)

        connection.registerEvent(CallbackEventInvoker.of<ExperienceChangeEvent> {
            layout.core.experience.apply()
        })

        connection.registerEvent(CallbackEventInvoker.of<GamemodeChangeEvent> {
            layout.forceApply()
        })

        connection.registerEvent(CallbackEventInvoker.of<SelectHotbarSlotEvent> {
            layout.core.base.apply()
        })

        connection.registerEvent(CallbackEventInvoker.of<ContainerRevisionChangeEvent> {
            if (it.container != connection.player.inventory) {
                return@of
            }
            layout.core.base.apply()
            layout.offhand.apply()
        })

        connection.registerEvent(CallbackEventInvoker.of<ChatMessageReceiveEvent> {
            if (it.position != ChatTextPositions.ABOVE_HOTBAR) {
                return@of
            }
            layout.hoverText.text = it.message
            layout.hoverText.show()
        })
    }


    companion object : HUDBuilder<HotbarHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:hotbar".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): HotbarHUDElement {
            return HotbarHUDElement(hudRenderer)
        }
    }
}
