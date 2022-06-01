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

package de.bixilon.minosoft.commands.parser.minosoft.connection.selector.properties

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.minosoft.enums.EnumParser
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates

class StateProperty(
    val state: PlayConnectionStates,
    val negated: Boolean,
) : ConnectionTargetProperty {

    override fun passes(connection: PlayConnection): Boolean {
        val state = connection.state
        if (negated) {
            return state != this.state
        }
        return state == this.state
    }

    companion object : ConnectionTargetPropertyFactory<StateProperty> {
        private val parser = EnumParser(PlayConnectionStates)
        override val name: String = "state"

        override fun read(reader: CommandReader): StateProperty {
            val (word, negated) = reader.readNegateable { parser.parse(reader) } ?: throw ExpectedArgumentError(reader)
            return StateProperty(word, negated)
        }
    }
}
