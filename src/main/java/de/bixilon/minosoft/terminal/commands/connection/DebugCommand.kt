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

package de.bixilon.minosoft.terminal.commands.connection

import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.brigadier.bool.BooleanParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.util.KUtil.format

object DebugCommand : ConnectionCommand {
    override var node = LiteralNode("debug")
        .addChild(LiteralNode("allowFly", executor = { it.fly() }, allowArguments = true).addChild(ArgumentNode("value", BooleanParser, executable = true)))
        .addChild(LiteralNode("network").addChild(
            LiteralNode("detach", executor = { it.connection.network.detach(); it.connection.util.sendDebugMessage("Now you are alone on the wire...") }),
        ))


    private fun CommandStack.fly() {
        val value: Boolean = this["value"] ?: !connection.player.abilities.allowFly
        val abilities = connection.player.abilities
        if (abilities.allowFly == value) {
            print.print("Allow fly is already set to ${value.format()}§r!")
        } else {
            connection.player.abilities = abilities.copy(allowFly = value)
            print.print("Allow fly set to ${value.format()}§r!")
        }
    }
}
