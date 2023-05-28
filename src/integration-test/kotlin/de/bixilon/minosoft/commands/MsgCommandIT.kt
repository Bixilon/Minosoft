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

package de.bixilon.minosoft.commands

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.nodes.RootNode
import de.bixilon.minosoft.commands.parser.minecraft.message.MessageParser
import de.bixilon.minosoft.commands.parser.minecraft.target.TargetParser
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.identifier.name.NameEntityTarget
import de.bixilon.minosoft.commands.stack.CommandStack
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["command"])
class MsgCommandIT {

    private fun createNode(executor: (CommandStack) -> Unit): RootNode {
        val msg = LiteralNode("msg").addChild(
            ArgumentNode("targets", TargetParser()).addChild(
                ArgumentNode("message", MessageParser, executor = executor)
            )
        )
        return RootNode().addChild(
            msg,
            LiteralNode(name = "redirect", redirect = msg)
        ).unsafeCast()
    }

    fun basicExecution() {
        var executed = false
        val node = createNode { executed = true }

        node.execute("msg Bixilon hi there!")

        assertTrue(executed)
    }

    fun validateStack() {
        var stack: CommandStack? = null
        val node = createNode { stack = it.fork() }

        node.execute("msg Bixilon hi there!")
        assertEquals(stack!!["msg"], "msg")
        assertEquals(stack!!["targets"], NameEntityTarget("Bixilon"))
        assertEquals(stack!!["message"], "hi there!")
    }

    fun redirectExecution() {
        var executed = false
        val node = createNode { executed = true }

        node.execute("redirect Bixilon hi there!")

        assertTrue(executed)
    }

    fun redirectStack() {
        var stack: CommandStack? = null
        val node = createNode { stack = it.fork() }

        node.execute("redirect Bixilon hi there!")


        assertEquals(stack!!["msg"], "redirect")
        assertEquals(stack!!["targets"], NameEntityTarget("Bixilon"))
        assertEquals(stack!!["message"], "hi there!")
    }
}
