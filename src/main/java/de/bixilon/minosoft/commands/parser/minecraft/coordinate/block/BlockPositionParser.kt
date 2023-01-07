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

package de.bixilon.minosoft.commands.parser.minecraft.coordinate.block

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CoordinateParserUtil.readCoordinate
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object BlockPositionParser : ArgumentParser<BlockCoordinate>, ArgumentParserFactory<BlockPositionParser> {
    override val identifier: ResourceLocation = "minecraft:block_pos".toResourceLocation()
    override val examples: List<Any> = listOf("~ ~ ~", "^ ^ ^", "5 5 5")

    override fun parse(reader: CommandReader): BlockCoordinate {
        return BlockCoordinate(reader.readCoordinate(decimal = false), reader.readCoordinate(decimal = false), reader.readCoordinate(decimal = false))
    }

    override fun getSuggestions(reader: CommandReader): List<Any> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }

    override fun read(buffer: PlayInByteBuffer) = this
}
