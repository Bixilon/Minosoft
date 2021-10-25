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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.title

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.modding.event.events.title.*
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class TitleHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<TitleElement>(hudRenderer) {
    override val layout: TitleElement = TitleElement(hudRenderer)
    override val layoutOffset: Vec2i
        get() {
            val layoutOffset = Vec2i.EMPTY

            val scaledSize = hudRenderer.scaledSize

            layoutOffset.x = (scaledSize.x - layout.size.x / 2) / 2
            layoutOffset.y = (scaledSize.y - layout.title.size.y) / 2

            return layoutOffset
        }

    override fun init() {
        val connection = hudRenderer.connection

        connection.registerEvent(CallbackEventInvoker.of<TitleResetEvent> {
            layout.reset()
        })
        connection.registerEvent(CallbackEventInvoker.of<TitleHideEvent> {
            layout.hide()
        })
        connection.registerEvent(CallbackEventInvoker.of<TitleSetEvent> {
            layout.title.text = it.title
            layout.show()
        })
        connection.registerEvent(CallbackEventInvoker.of<TitleSubtitleSetEvent> {
            layout.subtitle.text = it.subtitle
            // layout.show() // non vanilla behavior
        })
        connection.registerEvent(CallbackEventInvoker.of<TitleTimesSetEvent> {
            layout.fadeInTime = it.fadeInTime * ProtocolDefinition.TICK_TIME.toLong()
            layout.stayTime = it.stayTime * ProtocolDefinition.TICK_TIME.toLong()
            layout.fadeOutTime = it.fadeOutTime * ProtocolDefinition.TICK_TIME.toLong()
        })
    }


    companion object : HUDBuilder<TitleHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:title".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): TitleHUDElement {
            return TitleHUDElement(hudRenderer)
        }
    }

}
