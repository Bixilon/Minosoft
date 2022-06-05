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

package de.bixilon.minosoft.terminal.commands

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minosoft.enums.EnumParser
import de.bixilon.minosoft.commands.stack.print.PrintTarget

object HelpCommand : Command {
    override var node: LiteralNode = LiteralNode("help", setOf("?"), executor = { it.print.printHelp() })
        .addChild(ArgumentNode("subcommand", parser = EnumParser(HelpCommands), executor = { it.print.printHelp(it["subcommand"]!!) }))

    fun PrintTarget.printHelp() {
        print("-------------- Minosoft help --------------")
    }

    fun PrintTarget.printHelp(subcommand: HelpCommands) {
        print("-------------- Minosoft help --------------")
        print("Subcommand: $subcommand")
    }

    enum class HelpCommands {
        GENERAL,
        ;

        companion object : ValuesEnum<HelpCommands> {
            override val VALUES: Array<HelpCommands> = values()
            override val NAME_MAP: Map<String, HelpCommands> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
