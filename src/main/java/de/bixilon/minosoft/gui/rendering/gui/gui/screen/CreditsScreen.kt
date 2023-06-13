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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.misc.event.world.handler.win.WinGameEvent
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP

@Deprecated("ToDo")
class CreditsScreen(
    guiRenderer: GUIRenderer,
) : Screen(guiRenderer) {
    private val headerElement = TextElement(guiRenderer, "Minecraft", background = null, properties = TextRenderProperties(scale = 3.0f), parent = this)
    private val textElement = TextElement(guiRenderer, "Ähm, yes. This is not yet implemented -/-\nI don't know how to make moving text in the current gui system.\nI am so sorry...", background = null, parent = this)


    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val size = size

        headerElement.render(offset + HorizontalAlignments.CENTER.getOffset(size, headerElement.size), consumer, options)
        offset.y += headerElement.size.y

        offset.y += size.y / 30
        textElement.render(offset + HorizontalAlignments.CENTER.getOffset(size, textElement.size), consumer, options)
    }

    override fun onClose() {
        super.onClose()
        guiRenderer.connection.sendPacket(ClientActionC2SP(ClientActionC2SP.ClientActions.PERFORM_RESPAWN))
    }

    companion object {

        fun register(guiRenderer: GUIRenderer) {
            guiRenderer.connection.events.listen<WinGameEvent> {
                if (!it.showCredits) {
                    return@listen
                }
                guiRenderer.gui.push(CreditsScreen(guiRenderer))
            }
        }
    }
}
