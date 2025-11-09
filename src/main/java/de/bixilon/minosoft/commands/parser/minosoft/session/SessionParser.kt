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

package de.bixilon.minosoft.commands.parser.minosoft.session

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser.Companion.readInt
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minosoft.session.identifier.SessionId
import de.bixilon.minosoft.commands.parser.minosoft.session.selector.properties.SessionTargetProperties
import de.bixilon.minosoft.commands.parser.selector.AbstractTarget
import de.bixilon.minosoft.commands.parser.selector.SelectorParser
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object SessionParser : SelectorParser<PlaySession>(), ArgumentParserFactory<SessionParser> {
    override val identifier = minosoft("session")
    override val properties get() = SessionTargetProperties

    override fun parseId(reader: CommandReader): AbstractTarget<PlaySession> {
        val result = reader.readResult { reader.readInt() }
        if (result.result == null) {
            throw ExpectedArgumentError(reader)
        }
        return SessionId(result.result)
    }

    override fun read(buffer: PlayInByteBuffer): SessionParser = this
}
