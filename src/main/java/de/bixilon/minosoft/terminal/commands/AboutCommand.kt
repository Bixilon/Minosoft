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

import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.stack.print.PrintTarget
import de.bixilon.minosoft.properties.MinosoftProperties

object AboutCommand : Command {
    override var node: LiteralNode = LiteralNode("about", setOf("version"), executor = { it.print.printAbout() })

    private fun PrintTarget.printAbout() {
        print("-------------- Minosoft --------------")
        print("This is minosoft version §e${MinosoftProperties.general.name}")
        val git = MinosoftProperties.git
        if (git == null) {
            print("Sadly git version information is not available.")
        } else {
            print("This version was built from §e${git.branch}§r on top of §e${git.commit}")
        }

        print("This software was mainly created by §eMoritz Zwerger§r (https://bixilon.de)")
        print("and is licensed under the §eGPL3§r license.")

        print("The source code is available under https://gitlab.bixilon.de/bixilon/minosoft.")
        print("Feel free to §econtribute§r, §efork§r and §esubmit§r a pull request!")
    }
}
