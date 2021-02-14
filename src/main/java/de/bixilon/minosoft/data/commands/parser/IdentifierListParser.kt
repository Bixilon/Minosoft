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

import de.bixilon.minosoft.data.EntityClassMappings
import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.identifier.DimensionNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.identifier.EnchantmentNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.identifier.EntityNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.identifier.MobEffectNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.protocol.network.Connection

class IdentifierListParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun parse(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader): Any? {
        val identifier = stringReader.readModIdentifier()


        if (this == ENCHANTMENT_PARSER) {
            return connection.mapping.getEnchantment(identifier.value) ?: throw EnchantmentNotFoundCommandParseException(stringReader, identifier.key)
        }
        if (this == MOB_EFFECT_PARSER) {
            return connection.mapping.getMobEffect(identifier.value) ?: throw MobEffectNotFoundCommandParseException(stringReader, identifier.key)
        }
        if (this == DIMENSION_EFFECT_PARSER) {
            return connection.mapping.getDimension(identifier.value) ?: throw DimensionNotFoundCommandParseException(stringReader, identifier.key)
        }
        if (this == SUMMONABLE_ENTITY_PARSER) {
            // ToDo: only summonable entities, not all of them

            if (EntityClassMappings.getByIdentifier(identifier.value) == null) {
                throw EntityNotFoundCommandParseException(stringReader, identifier.key)
            }
            return null // ToDo
        }
        return null // ToDo
    }

    companion object {
        val ENCHANTMENT_PARSER = IdentifierListParser()
        val MOB_EFFECT_PARSER = IdentifierListParser()
        val DIMENSION_EFFECT_PARSER = IdentifierListParser()
        val SUMMONABLE_ENTITY_PARSER = IdentifierListParser()
    }
}
