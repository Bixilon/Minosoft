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

package de.bixilon.minosoft.data.commands.parser.entity;

import de.bixilon.minosoft.data.EntityClassMappings;
import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.entity.UnknownEntityCommandParseException;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.network.Connection;

public class ResourceLocationSelectorArgumentParser extends EntitySelectorArgumentParser {
    public static final ResourceLocationSelectorArgumentParser ENTITY_TYPE_RESOURCE_LOCATION_SELECTOR_ARGUMENT_PARSER = new ResourceLocationSelectorArgumentParser();

    @Override
    public void isParsable(Connection connection, CommandStringReader stringReader, String value) throws CommandParseException {
        if (value.startsWith("!")) {
            value = value.substring(1);
        }
        ResourceLocation resourceLocation = new ResourceLocation(value);
        if (this == ENTITY_TYPE_RESOURCE_LOCATION_SELECTOR_ARGUMENT_PARSER) {
            if (EntityClassMappings.INSTANCE.getByResourceLocation(resourceLocation) == null) {
                throw new UnknownEntityCommandParseException(stringReader, value);
            }
            return;
        }
        throw new RuntimeException();
    }
}
