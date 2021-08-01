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

package de.bixilon.minosoft.data.commands.parser.minosoft;

import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.CommandParser;
import de.bixilon.minosoft.data.commands.parser.exceptions.BlankStringCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.minosoft.InvalidVersionCommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.registries.versions.Version;
import de.bixilon.minosoft.data.registries.versions.Versions;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;

import javax.annotation.Nullable;

public class VersionParser extends CommandParser {
    public static final VersionParser VERSION_PARSER = new VersionParser();

    @Override
    public Object parse(PlayConnection connection, @Nullable ParserProperties properties, CommandStringReader stringReader) throws CommandParseException {
        String rawVersionName = stringReader.readString();
        if (rawVersionName.isBlank()) {
            throw new BlankStringCommandParseException(stringReader, rawVersionName);
        }
        Version version = Versions.getVersionByName(rawVersionName);
        if (version == null) {
            throw new InvalidVersionCommandParseException(stringReader, rawVersionName);
        }
        return version;
    }
}
