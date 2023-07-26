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

package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.NamedNode
import de.bixilon.minosoft.commands.nodes.RootNode
import org.testng.Assert.assertNotNull
import org.testng.annotations.Test

@Test(groups = ["packet"])
class CommandsS2CPTest {
    private val children = CommandNode::class.java.getDeclaredField("children").apply { isAccessible = true }
    private val RootNode.children: List<CommandNode> get() = this@CommandsS2CPTest.children.get(this).unsafeCast()

    fun vanilla_op_1_19_3() {
        val packet = PacketReadingTestUtil.read("commands/vanilla_op_1_19_3", "1.19.3", constructor = ::CommandsS2CP)
        assertNotNull(packet.rootNode)
        val help = packet.rootNode!!.children.find { it.nullCast<NamedNode>()?.name == "help" }
        assertNotNull(help)
    }

    fun vanilla_op_1_15_2() {
        val packet = PacketReadingTestUtil.read("commands/vanilla_op_1_15_2", "1.15.2", constructor = ::CommandsS2CP)
        assertNotNull(packet.rootNode)
        val help = packet.rootNode!!.children.find { it.nullCast<NamedNode>()?.name == "help" }
        assertNotNull(help)
    }

    fun vanilla_op_1_20_1() {
        val packet = PacketReadingTestUtil.read("commands/vanilla_op_1_20_1", "1.20.1", constructor = ::CommandsS2CP)
        assertNotNull(packet.rootNode)
        val help = packet.rootNode!!.children.find { it.nullCast<NamedNode>()?.name == "help" }
        assertNotNull(help)
    }
}

