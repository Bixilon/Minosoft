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

package de.bixilon.minosoft.commands.parser.minecraft.uuid

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

object UUIDParser : ArgumentParser<UUID>, ArgumentParserFactory<UUIDParser> {
    override val identifier: ResourceLocation = "minecraft:uuid".toResourceLocation()
    override val examples: List<Any> = listOf("9e6ce7c5-40d3-483e-8e5a-b6350987d65f")

    override fun parse(reader: CommandReader): UUID {
        reader.readResult { readUUID() }.let { return it.result ?: throw InvalidUUIDError(reader, it) }
    }

    fun StringReader.readUUID(): UUID? {
        val string = readWord() ?: return null

        return try {
            string.toUUID()
        } catch (ignored: Throwable) {
            null
        }
    }

    override fun read(buffer: PlayInByteBuffer) = this
}
