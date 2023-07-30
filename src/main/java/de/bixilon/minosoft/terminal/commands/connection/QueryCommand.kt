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

package de.bixilon.minosoft.terminal.commands.connection

import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes

object QueryCommand : ConnectionCommand {
    override var node = LiteralNode("query")
        .addChild(LiteralNode("health", executor = { it.health() }))
        .addChild(LiteralNode("xp", setOf("experience", "exp"), executor = { it.print.print("Experience: level §e${it.connection.player.experienceCondition.level}") }))
        .addChild(LiteralNode("dimension", executor = { it.print.print("Dimension: §e${it.connection.world.dimension.effects}") }))


    private fun CommandStack.health() {
        val health = connection.player.healthCondition
        if (health.hp == 0.0f) {
            return print.print("You are §cdead§r!")
        }
        val max = connection.player.attributes[MinecraftAttributes.MAX_HEALTH]
        print.print("Health §c${health.hp}§r/§c${max}§r, hunger=§a${health.hunger}")
    }
}
