/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.commands.session

import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minecraft.resource.ResourceParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP

object ItemCommand : SessionCommand {
    override var node = LiteralNode("item")
        .addChild(LiteralNode("create").addChild(ArgumentNode("type", ResourceParser(minecraft("item")), executor = { it.create() })))


    private fun CommandStack.create() {
        val name = this.get<ResourceLocation>("type")!!
        val type = session.registries.item[name] ?: throw IllegalArgumentException("Item type not found: $name")

        if (session.player.gamemode != Gamemodes.CREATIVE) throw IllegalStateException("Not in creative mode!")

        val stack = ItemStack(type, count = if (type is StackableItem) type.maxStackSize else 1)

        val changes = session.player.items.inventory.add(stack).unsafeCommit()
        for ((slotId, stack) in changes) {
            session.connection.send(ItemStackCreateC2SP(slotId, stack))
        }
    }
}
