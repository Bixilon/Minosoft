/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.commands.parser.minosoft.session.selector.properties

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.brigadier.bool.BooleanParser.readBoolean
import de.bixilon.minosoft.commands.parser.selector.TargetProperty
import de.bixilon.minosoft.commands.parser.selector.TargetPropertyFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ConnectedProperty(
    val connected: Boolean,
) : TargetProperty<PlaySession> {

    override fun passes(value: PlaySession): Boolean {
        val connected = value.network.connected
        return connected == this.connected
    }

    companion object : TargetPropertyFactory<PlaySession> {
        override val name: String = "connected"

        override fun read(reader: CommandReader): ConnectedProperty {
            return ConnectedProperty(reader.readBoolean() ?: throw ExpectedArgumentError(reader))
        }
    }
}
