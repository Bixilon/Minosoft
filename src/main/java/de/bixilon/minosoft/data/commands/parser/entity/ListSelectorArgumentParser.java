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

package de.bixilon.minosoft.data.commands.parser.entity;

import de.bixilon.minosoft.data.abilities.Gamemodes;
import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.entity.UnknownEnumValueCommandParseException;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;

import java.util.Arrays;
import java.util.HashSet;

public class ListSelectorArgumentParser extends EntitySelectorArgumentParser {
    public static final ListSelectorArgumentParser GAMEMODE_SELECTOR_ARGUMENT_PARSER = new ListSelectorArgumentParser(Gamemodes.values());
    public static final ListSelectorArgumentParser SORT_SELECTOR_ARGUMENT_PARSER = new ListSelectorArgumentParser("arbitrary", "furthest", "nearest", "random");

    private final HashSet<String> values;

    public ListSelectorArgumentParser(Enum<?>[] values) {
        this.values = new HashSet<>();
        for (Enum<?> enumValue : values) {
            this.values.add(enumValue.name().toLowerCase());
        }
    }

    public ListSelectorArgumentParser(String... values) {
        this.values = new HashSet<>(Arrays.asList(values));
    }

    public ListSelectorArgumentParser(HashSet<String> values) {
        this.values = values;
    }

    @Override
    public void isParsable(PlayConnection connection, CommandStringReader stringReader, String value) throws CommandParseException {
        if (value.startsWith("!")) {
            value = value.substring(1);
        }
        if (!this.values.contains(value)) {
            throw new UnknownEnumValueCommandParseException(stringReader, value);
        }
    }
}
