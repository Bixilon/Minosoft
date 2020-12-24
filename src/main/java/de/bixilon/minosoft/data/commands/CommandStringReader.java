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

package de.bixilon.minosoft.data.commands;

import de.bixilon.minosoft.data.commands.parser.exceptions.BooleanCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.InvalidIdentifierCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.StringCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.DoubleCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.FloatCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.IntegerCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.LongCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.properties.BadPropertyMapCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.properties.DuplicatedPropertyKeyCommandParseException;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class CommandStringReader {
    private final String string;
    private int cursor;

    public CommandStringReader(String string) {
        this.string = string;
    }

    public CommandStringReader(CommandStringReader stringReader) {
        this.string = stringReader.string;
        this.cursor = stringReader.cursor;
    }

    public static boolean isCharNumeric(char c) {
        return ((c >= '0' && c <= '9') || c == '-' || c == '.');
    }

    public int getCursor() {
        return this.cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public int getRemainingLength() {
        return this.string.length() - this.cursor;
    }

    public int getTotalLength() {
        return this.string.length();
    }

    public String getRead() {
        return this.string.substring(0, this.cursor);
    }

    public String getRemaining() {
        return this.string.substring(this.cursor);
    }

    public boolean canRead(int length) {
        return this.string.length() - (this.cursor + length) >= 0;
    }

    public boolean canRead() {
        return canRead(1);
    }

    public char peek() {
        return this.string.charAt(this.cursor);
    }

    public char read() {
        return this.string.charAt(this.cursor++);
    }

    public void skip(int length) {
        this.cursor += length;
    }

    public void skip() {
        this.cursor++;
    }

    /**
     * @return The number of skipped whitespaces
     */
    public int skipWhitespaces() {
        int skipped = 0;
        while (canRead() && Character.isWhitespace(peek())) {
            skip();
            skipped++;
        }
        return skipped;
    }

    public String readUnquotedString() {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char next = peek();
            if ((next >= '0' && next <= '9') || (next >= 'A' && next <= 'Z') || (next >= 'a' && next <= 'z') || next == '.' || next == '+' || next == '-' || next == '_') {
                builder.append(next);
                skip();
                continue;
            }
            break;
        }
        return builder.toString();
    }

    public String readQuotedString() throws StringCommandParseException {
        if (!canRead() || !peekExpected('"', '\'')) {
            throw new StringCommandParseException(this, String.valueOf(peek()), "String is not quoted!");
        }
        return readStringUntil(read());
    }

    public String readNumericString() {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char next = peek();
            if (isCharNumeric(next)) {
                builder.append(next);
                skip();
                continue;
            }
            break;
        }
        return builder.toString();
    }

    public Pair<String, String> readProperty() throws StringCommandParseException, BadPropertyMapCommandParseException {
        skipWhitespaces();
        StringBuilder builder = new StringBuilder();
        String key = readString();
        skipWhitespaces();
        if (read() != '=') {
            throw new BadPropertyMapCommandParseException(this, key, "Not a property string!");
        }
        skipWhitespaces();
        String value = readString();
        skipWhitespaces();
        return new Pair<>(key, value);
    }

    public Map<String, String> readProperties() throws StringCommandParseException, DuplicatedPropertyKeyCommandParseException, BadPropertyMapCommandParseException {
        Map<String, String> ret = new HashMap<>();
        if (peek() != '[') {
            throw new BadPropertyMapCommandParseException(this, String.valueOf(peek()), "Not a property map!");
        }
        skip();
        if (peek() == ']') {
            return ret;
        }
        Pair<String, String> property = readProperty();
        ret.put(property.getKey(), property.getValue());
        while (peek() == ',') {
            property = readProperty();
            if (ret.containsKey(property.getKey())) {
                throw new DuplicatedPropertyKeyCommandParseException(this, property.getKey());
            }
            ret.put(property.getKey(), property.getValue());
        }
        if (peek() != ']') {
            throw new BadPropertyMapCommandParseException(this, String.valueOf(peek()), "Bad property map ending!");
        }
        skip();

        return ret;
    }

    public Pair<String, ModIdentifier> readModIdentifier() throws InvalidIdentifierCommandParseException {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char next = peek();
            if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'z') || next == '.' || next == '-' || next == '_' || next == ':' || next == '/') {
                builder.append(next);
                skip();
                continue;
            }
            break;
        }
        String identifier = builder.toString();
        try {
            return new Pair<>(identifier, ModIdentifier.getIdentifier(builder.toString()));
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdentifierCommandParseException(this, identifier, exception);
        }
    }

    public String readString() throws StringCommandParseException {
        if (!canRead()) {
            return "";
        }
        if (peekExpected('"', '\'')) {
            return readQuotedString();
        }
        return readUnquotedString();
    }

    public boolean peekExpected(char... expected) {
        char next = peek();
        for (char c : expected) {
            if (next == c) {
                return true;
            }
        }
        return false;
    }

    private String readStringUntil(boolean requiresTerminator, char terminator) throws StringCommandParseException {
        return readStringUntil(requiresTerminator, new char[]{terminator}).getKey();
    }

    private Pair<String, Character> readStringUntil(boolean requiresTerminator, char... terminators) throws StringCommandParseException {
        StringBuilder builder = new StringBuilder();
        boolean isNextCharEscaped = false;
        while (canRead()) {
            char read = read();
            if (read == '\\') {
                isNextCharEscaped = true;
                continue;
            }
            if (isNextCharEscaped) {
                builder.append(read);
                continue;
            }

            for (char terminator : terminators) {
                if (read == terminator) {
                    return new Pair<>(builder.toString(), terminator);
                }
            }
            builder.append(read);
        }
        if (requiresTerminator) {
            throw new StringCommandParseException(this, builder.toString(), "Terminator(s) not found in string!");
        }
        return new Pair<>(builder.toString(), (char) 0);
    }

    public boolean readBoolean() throws BooleanCommandParseException, StringCommandParseException {
        String read = readString();
        if (read.equals("true")) {
            return true;
        }
        if (read.equals("false")) {
            return true;
        }
        throw new BooleanCommandParseException(this, read);
    }

    public int readInt() throws IntegerCommandParseException {
        String numericString = readNumericString();
        try {
            return Integer.parseInt(numericString);
        } catch (NumberFormatException exception) {
            throw new IntegerCommandParseException(this, numericString, exception);
        }
    }

    public long readLong() throws LongCommandParseException {
        String numericString = readNumericString();
        try {
            return Long.parseLong(numericString);
        } catch (NumberFormatException exception) {
            throw new LongCommandParseException(this, numericString, exception);
        }
    }

    public float readFloat() throws FloatCommandParseException {
        String numericString = readNumericString();
        try {
            return Float.parseFloat(numericString);
        } catch (NumberFormatException exception) {
            throw new FloatCommandParseException(this, numericString, exception);
        }
    }

    public double readDouble() throws DoubleCommandParseException {
        String numericString = readNumericString();
        try {
            return Double.parseDouble(numericString);
        } catch (NumberFormatException exception) {
            throw new DoubleCommandParseException(this, numericString, exception);
        }
    }

    public Pair<String, Character> readStringUntilOrEnd(char... terminators) {
        try {
            return readStringUntil(false, terminators);
        } catch (StringCommandParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Pair<String, Character> readStringUntil(char... terminators) throws StringCommandParseException {
        return readStringUntil(true, terminators);
    }

    public String readStringUntilOrEnd(char terminator) {
        try {
            return readStringUntil(false, terminator);
        } catch (StringCommandParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String readStringUntil(char terminator) throws StringCommandParseException {
        return readStringUntil(true, terminator);
    }

    public boolean readExpected(char... expected) {
        boolean ret = peekExpected(expected);
        skip();
        return ret;
    }

    public String readRemaining() {
        String ret = this.string.substring(this.cursor);
        this.cursor = this.string.length() + 1;
        return ret;
    }

    public String peekRemaining() {
        return this.string.substring(this.cursor);
    }

    public String getString() {
        return this.string;
    }

    @Override
    public String toString() {
        return String.format("position=%d/%d: \"%s\"", this.cursor, this.string.length(), peekRemaining());
    }
}
