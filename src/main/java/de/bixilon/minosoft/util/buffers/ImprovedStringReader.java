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

package de.bixilon.minosoft.util.buffers;

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Pair;

public class ImprovedStringReader {
    private final String string;
    private int position;

    public ImprovedStringReader(String string) {
        this.string = string;
    }

    public Pair<String, String> readUntil(String... search) {
        Pair<String, String> ret = getUntil(search);
        position += ret.key.length() + ret.value.length();
        return ret;
    }

    /**
     * @param search If one of these string is read, end reading
     * @return key: String until first occurrence, value: Matched string
     */
    public Pair<String, String> getUntil(String... search) {
        String rest = getRest();
        String found = "";
        int lowestIndex = Integer.MAX_VALUE;
        for (String string : search) {
            int currentIndex = rest.indexOf(string);
            if (currentIndex == -1) {
                continue;
            }
            if (lowestIndex > currentIndex) {
                lowestIndex = currentIndex;
                found = string;
            }
        }
        if (lowestIndex == Integer.MAX_VALUE) {
            return new Pair<>(rest, found);
        }
        return new Pair<>(rest.substring(0, lowestIndex), found);
    }

    public String readUntilNextCommandArgument() {
        return readUntil(ProtocolDefinition.COMMAND_SEPARATOR).key;
    }

    public String getUntilNextCommandArgument() {
        return getUntil(ProtocolDefinition.COMMAND_SEPARATOR).key;
    }

    public String getString() {
        return string;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getAvailableChars() {
        return string.length() - position;
    }

    public String readChar() {
        return String.valueOf(string.charAt(position++));
    }

    public String getChar() {
        return String.valueOf(string.charAt(position));
    }

    public String getRest() {
        if (position > string.length()) {
            return "";
        }
        return string.substring(position);
    }

    public String readRest() {
        String ret = getRest();
        position = string.length();
        return ret;
    }

    public String read(int length) {
        String ret = get(length);
        position += length;
        return ret;
    }

    public String get(int length) {
        return string.substring(position, position + length);
    }

    public void skip(int length) {
        position += length;
    }

    public void skipChar() {
        position++;
    }

    public int skipSpaces() {
        int skipped = 0;
        while (getChar().equals(" ")) {
            skip(1);
            skipped++;
        }
        return skipped;
    }

    @Override
    public String toString() {
        return String.format("position=%d/%d: \"%s\"", position, string.length(), getRest());
    }
}
