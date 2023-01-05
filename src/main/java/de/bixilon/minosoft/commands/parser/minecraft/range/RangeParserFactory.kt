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

package de.bixilon.minosoft.commands.parser.minecraft.range

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.range._float.FloatRangeParser
import de.bixilon.minosoft.commands.parser.minecraft.range._int.IntRangeParser
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation


object RangeParserFactory : ArgumentParserFactory<ArgumentParser<*>> {
    override val identifier: ResourceLocation = "minosoft:range".toResourceLocation()

    override fun read(buffer: PlayInByteBuffer): ArgumentParser<*> {
        val decimals = buffer.readBoolean()
        return if (decimals) {
            FloatRangeParser.read(buffer)
        } else {
            IntRangeParser.read(buffer)
        }
    }


    fun <T> CommandReader.readRange(reader: CommandReader.() -> T): Pair<T?, T?>? {
        if (!canPeek()) {
            return null
        }
        val first = reader(this) ?: return null
        if (!canPeek()) {
            return Pair(first, null)
        }
        if (peekNext() != '.'.code || peekNext(pointer + 1) != '.'.code) {
            return null
        }
        readNext()
        readNext()

        val second = reader(this) ?: return null

        return Pair(first, second)
    }
}
