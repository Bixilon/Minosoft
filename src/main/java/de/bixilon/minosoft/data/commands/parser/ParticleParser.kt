/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.exceptions.resourcelocation.ParticleNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.mappings.particle.data.BlockParticleData
import de.bixilon.minosoft.data.mappings.particle.data.DustParticleData
import de.bixilon.minosoft.data.mappings.particle.data.ItemParticleData
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.protocol.network.connection.PlayConnection

object ParticleParser : CommandParser() {

    override fun parse(connection: PlayConnection, properties: ParserProperties?, stringReader: CommandStringReader): ParticleData {
        val resourceLocation = stringReader.readResourceLocation()

        val particle = connection.registries.particleTypeRegistry[resourceLocation.value] ?: throw ParticleNotFoundCommandParseException(stringReader, resourceLocation.key)

        stringReader.skipWhitespaces()

        return when (resourceLocation.value.full) {
            "minecraft:block", "minecraft:falling_dust" -> BlockParticleData(BlockStateParser.BLOCK_STACK_PARSER.parse(connection, properties, stringReader), particle)
            "minecraft:dust" -> {
                val red = stringReader.readFloat()
                stringReader.skipWhitespaces()
                val green = stringReader.readFloat()
                stringReader.skipWhitespaces()
                val blue = stringReader.readFloat()
                stringReader.skipWhitespaces()
                val scale = stringReader.readFloat()

                DustParticleData(red, green, blue, scale, particle)
            }
            "minecraft:item" -> {
                ItemParticleData(ItemStackParser.ITEM_STACK_PARSER.parse(connection, properties, stringReader), particle)
            }
            else -> ParticleData(particle)
        }
    }
}
