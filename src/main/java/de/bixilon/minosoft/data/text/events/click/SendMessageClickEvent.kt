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

package de.bixilon.minosoft.data.text.events.click

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.factory.name.MultiNameFactory
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.confirmation.SendMessageDialog
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons

class SendMessageClickEvent(
    val message: String,
) : ClickEvent {

    override fun onClick(guiRenderer: GUIRenderer, position: Vec2, button: MouseButtons, action: MouseActions) {
        if (button != MouseButtons.LEFT || action != MouseActions.PRESS) {
            return
        }
        if (!guiRenderer.connection.profiles.gui.confirmation.sendMessage) {
            guiRenderer.connection.util.typeChat(message)
            return
        }
        val dialog = SendMessageDialog(guiRenderer, message)
        dialog.show()
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SendMessageClickEvent) return false
        return other.message == message
    }

    companion object : ClickEventFactory<SendMessageClickEvent>, MultiNameFactory<SendMessageClickEvent> {
        override val name: String = "send_message"
        override val aliases: Set<String> = setOf("run_command")

        override fun build(json: JsonObject, restricted: Boolean): SendMessageClickEvent {
            return SendMessageClickEvent(json.data.toString())
        }
    }
}
