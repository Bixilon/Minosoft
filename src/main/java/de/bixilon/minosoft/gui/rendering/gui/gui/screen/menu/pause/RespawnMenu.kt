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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.ElementStates
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP

class RespawnMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {

    init {
        background.tint = RGBColor(0xFF, 0x00, 0x00, 0x7F)
        add(TextElement(guiRenderer, "You died!", HorizontalAlignments.CENTER, false, scale = 3.0f))
        add(SpacerElement(guiRenderer, Vec2i(0, 20)))
        if (guiRenderer.connection.world.hardcore) {
            add(TextElement(guiRenderer, "This world is hardcore, you cannot respawn!"))
        } else {
            add(ButtonElement(guiRenderer, "Respawn") { respawn() })
        }
    }

    fun respawn() {
        guiRenderer.connection.network.send(ClientActionC2SP(ClientActionC2SP.ClientActions.PERFORM_RESPAWN))
        canPop = true
        guiRenderer.gui.pop()
    }

    override fun onOpen() {
        super.onOpen()
        canPop = false
    }

    companion object : GUIBuilder<LayoutedGUIElement<RespawnMenu>> {

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<RespawnMenu> {
            return LayoutedGUIElement(RespawnMenu(guiRenderer))
        }

        fun register(guiRenderer: GUIRenderer) {
            guiRenderer.connection.player::healthCondition.observe(this) {
                if (it.hp <= 0.0f) {
                    guiRenderer.gui.open(this)
                } else {
                    val element = guiRenderer.gui[this]
                    if (element.state == ElementStates.CLOSED) {
                        return@observe
                    }
                    element.layout.canPop = true
                    guiRenderer.gui.pop(element)
                }
            }
            guiRenderer.connection.events.listen<RespawnEvent> {
                val element = guiRenderer.gui[this]
                if (element.state == ElementStates.CLOSED) {
                    return@listen
                }
                element.layout.canPop = true
                guiRenderer.gui.pop(element)
            }
        }
    }
}
