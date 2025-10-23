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

package de.bixilon.minosoft.commands.parser.minecraft.resource

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.resource.location.InvalidResourceLocationError
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

@Deprecated("TODO")
class ResourceParser(
    val type: ResourceLocation,
) : ArgumentParser<Any> {
    override val examples: List<Any> = listOf("TODO")

    override fun parse(reader: CommandReader): Any {
        reader.readResult { readResourceLocation() }.let { return it.result ?: throw InvalidResourceLocationError(reader, it) }
        // TODO: validate against registry
    }

    companion object : ArgumentParserFactory<ResourceParser> {
        override val identifier = minecraft("resource")


        override fun read(buffer: PlayInByteBuffer): ResourceParser {
            val type = buffer.readResourceLocation()
            return ResourceParser(type)
        }
    }
}
