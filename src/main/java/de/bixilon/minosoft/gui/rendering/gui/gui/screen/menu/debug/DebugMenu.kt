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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.debug

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.other.game.event.handlers.gamemode.GamemodeChangeEvent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.modding.event.EventInitiators

class DebugMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {
    private val connection = guiRenderer.connection

    init {
        add(TextElement(guiRenderer, "Debug options", HorizontalAlignments.CENTER, false))
        add(SpacerElement(guiRenderer, Vec2i(0, 10)))
        add(ButtonElement(guiRenderer, "Switch to next gamemode") { connection.util.sendChatMessage("/gamemode ${connection.player.gamemode.next().name.lowercase()}") })
        add(ButtonElement(guiRenderer, "Hack to next gamemode") {
            val previous = connection.player.tabListItem.gamemode
            val next = previous.next()
            connection.player.tabListItem.gamemode = next
            connection.fireEvent(GamemodeChangeEvent(connection, EventInitiators.SERVER, previous, next))
        })
        add(ButtonElement(guiRenderer, "Toggle collisions") {
            connection.player.hasCollisions = !connection.player.hasCollisions
        })
        add(ButtonElement(guiRenderer, "Fake y=100") {
            val entity = connection.player
            val position = Vec3d(entity.position.x, 100.0, entity.position.z)

            entity.position = position
            entity.previousPosition = position
        })

        add(ButtonElement(guiRenderer, "Back") { guiRenderer.gui.pop() })
    }

    companion object : GUIBuilder<LayoutedGUIElement<DebugMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<DebugMenu> {
            return LayoutedGUIElement(DebugMenu(guiRenderer))
        }
    }
}
