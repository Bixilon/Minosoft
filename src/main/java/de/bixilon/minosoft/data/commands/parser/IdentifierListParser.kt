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
import de.bixilon.minosoft.data.commands.parser.exceptions.EnchantmentNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.MobEffectNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.protocol.network.Connection

class IdentifierListParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun isParsable(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader) {
        val identifier = stringReader.readModIdentifier()


        if (this == ENCHANTMENT_PARSER) {
            if (!connection.mapping.doesEnchantmentExist(identifier.value)) {
                throw EnchantmentNotFoundCommandParseException(stringReader, identifier.key)
            }
            return
        }
        if (this == MOB_EFFECT_PARSER) {
            if (!connection.mapping.doesMobEffectExist(identifier.value)) {
                throw MobEffectNotFoundCommandParseException(stringReader, identifier.key)
            }
            return
        }
    }

    companion object {
        val ENCHANTMENT_PARSER = IdentifierListParser()
        val MOB_EFFECT_PARSER = IdentifierListParser()
    }
}
