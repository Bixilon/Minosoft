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

package de.bixilon.minosoft.commands.nodes.builder

import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.nodes.RootNode
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.suggestion.types.SuggestionType

class CommandNodeBuilder {
    var type: ArgumentNodes? = null
    var children: IntArray? = null
    var redirectNode: Int? = null
    var name: String? = null
    var parser: ArgumentParser<*>? = null
    var suggestionType: SuggestionType? = null
    var executable = false


    fun build(): CommandNode {
        return when (type) {
            ArgumentNodes.ROOT -> RootNode(this)
            ArgumentNodes.LITERAL -> LiteralNode(this)
            ArgumentNodes.ARGUMENT -> ArgumentNode(this)
            else -> throw IllegalStateException("Type not set")
        }
    }
}
