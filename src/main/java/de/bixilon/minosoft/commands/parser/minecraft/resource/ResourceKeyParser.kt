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

package de.bixilon.minosoft.commands.parser.minecraft.resource

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

@Deprecated("TODO")
class ResourceKeyParser(
    val registry: AbstractRegistry<*>?,
) : ArgumentParser<Any> {
    override val examples: List<Any> = listOf("TODO")

    override fun parse(reader: CommandReader): Any {
        TODO()
    }

    companion object : ArgumentParserFactory<ResourceKeyParser> {
        override val identifier: ResourceLocation = "minecraft:resource_key".toResourceLocation()


        override fun read(buffer: PlayInByteBuffer): ResourceKeyParser {
            val registryName = buffer.readResourceLocation()
            val registry = buffer.connection.registries[registryName]
            return ResourceKeyParser(registry)
        }
    }
}
