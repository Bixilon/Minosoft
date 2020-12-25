/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.UnknownInventorySlotCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.protocol.network.Connection

class ItemSlotParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun isParsable(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader) {
        val slot = stringReader.readUnquotedString()

        if (!SLOTS.contains(slot)) {
            throw UnknownInventorySlotCommandParseException(stringReader, slot)
        }

    }

    companion object {
        private val SLOTS = HashSet<String>()

        init {

            for (i in 0 until 54) {
                SLOTS.add("container.$i")
            }
            for (i in 0 until 9) {
                SLOTS.add("hotbar.$i")
            }
            for (i in 0 until 27) {
                SLOTS.add("inventory." + (9 + i))
            }
            for (i in 0 until 27) {
                SLOTS.add("enderchest." + (200 + i))
            }
            for (i in 0 until 8) {
                SLOTS.add("villager." + (300 + i))
            }
            for (i in 0 until 15) {
                SLOTS.add("horse." + (500 + i))
            }
            SLOTS.addAll(setOf("weapon", "weapon.mainhand", "weapon.offhand", "armor.head", "armor.chest", "armor.legs", "armor.feet", "horse.saddle", "horse.armor", "horse.chest"))
        }

        val ITEM_SLOT_PARSER = ItemSlotParser()
    }
}
