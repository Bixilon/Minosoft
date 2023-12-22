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
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.terminal.cli.CLI

class InternalCommandClickEvent(
    val command: String,
) : ClickEvent {

    override fun onClick(guiRenderer: GUIRenderer, position: Vec2, button: MouseButtons, action: MouseActions) {
        if (button != MouseButtons.LEFT || action != MouseActions.PRESS) {
            return
        }
        CLI.execute(command)
    }

    override fun hashCode(): Int {
        return command.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is InternalCommandClickEvent) return false
        return other.command == command
    }

    companion object : ClickEventFactory<InternalCommandClickEvent> {
        override val name: String = "internal_command"

        override fun build(json: JsonObject, restricted: Boolean): InternalCommandClickEvent {
            if (restricted) throw IllegalAccessError("Using internal command is restricted")
            return InternalCommandClickEvent(json.data.toString())
        }
    }
}
