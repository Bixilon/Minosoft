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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import de.bixilon.minosoft.data.commands.parser.exceptions.BooleanCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.InvalidJSONCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.StringCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.identifier.InvalidIdentifierCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.nbt.CompoundTagBadFormatCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.nbt.ListTagBadFormatCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.DoubleCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.FloatCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.IntegerCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.LongCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.properties.BadPropertyMapCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.properties.DuplicatedPropertyKeyCommandParseException;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.nbt.tag.*;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class CommandStringReader {
    private static final Pattern NBT_PATTERN_INT = Pattern.compile("[-+]?(0|[1-9][0-9]*)");
    private static final Pattern NBT_PATTERN_BYTE = Pattern.compile(NBT_PATTERN_INT.pattern() + "b");
    private static final Pattern NBT_PATTERN_SHORT = Pattern.compile(NBT_PATTERN_INT.pattern() + "s");
    private static final Pattern NBT_PATTERN_LONG = Pattern.compile(NBT_PATTERN_INT.pattern() + "l");
    private static final Pattern NBT_PATTERN_FLOAT = Pattern.compile(NBT_PATTERN_INT.pattern() + "([.][0-9]*)?f");
    private static final Pattern NBT_PATTERN_DOUBLE = Pattern.compile(NBT_PATTERN_INT.pattern() + "([.][0-9]*)?d?");
    private static final Gson GSON = new Gson();

    private final String string;
    private int cursor;

    public CommandStringReader(@NotNull String string) {
        this.string = string;
    }

    public CommandStringReader(@NotNull CommandStringReader stringReader) {
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
        int nextLength = this.cursor + length;
        if (nextLength > this.string.length() || nextLength < 0) {
            throw new IllegalStateException("Nothing to skip!");
        }
        this.cursor = nextLength;
    }

    public void skip() {
        skip(1);
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

    @NotNull
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

    @NotNull
    public String readQuotedString() throws StringCommandParseException {
        if (!canRead() || !peekExpected('"', '\'')) {
            throw new StringCommandParseException(this, String.valueOf(peek()), "String is not quoted!");
        }
        return readStringUntil(read());
    }

    @NotNull
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

    @NotNull
    public Pair<String, String> readProperty() throws StringCommandParseException, BadPropertyMapCommandParseException {
        skipWhitespaces();
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

    @NotNull
    public Map<String, String> readProperties() throws StringCommandParseException, DuplicatedPropertyKeyCommandParseException, BadPropertyMapCommandParseException {
        if (peek() != '[') {
            throw new BadPropertyMapCommandParseException(this, String.valueOf(peek()), "Not a property map!");
        }
        Map<String, String> ret = new HashMap<>();
        skip();
        skipWhitespaces();
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

    @NotNull
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

    @NotNull
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

    @NotNull
    private String readStringUntil(boolean requiresTerminator, char terminator) throws StringCommandParseException {
        return readStringUntil(requiresTerminator, new char[]{terminator}).getKey();
    }

    @NotNull
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
                isNextCharEscaped = false;
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

    @NotNull
    public Pair<String, Character> readStringUntilOrEnd(char... terminators) {
        try {
            return readStringUntil(false, terminators);
        } catch (StringCommandParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public Pair<String, Character> readStringUntil(char... terminators) throws StringCommandParseException {
        return readStringUntil(true, terminators);
    }

    @NotNull
    public String readStringUntilOrEnd(char terminator) {
        try {
            return readStringUntil(false, terminator);
        } catch (StringCommandParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public String readStringUntil(char terminator) throws StringCommandParseException {
        return readStringUntil(true, terminator);
    }

    public ListTag readNBTListTag() throws ListTagBadFormatCommandParseException, StringCommandParseException, CompoundTagBadFormatCommandParseException {
        skipWhitespaces();
        if (peek() != '[') {
            throw new ListTagBadFormatCommandParseException(this, String.valueOf(peek()), "Not a list tag!");
        }
        skip();
        ListTag listTag = new ListTag();
        skipWhitespaces();
        if (peek() == ']') {
            skip();
            return listTag;
        }
        while (canRead()) {
            int start = getCursor();
            try {
                listTag.addTag(readNBTTag());
            } catch (IllegalArgumentException exception) {
                throw new ListTagBadFormatCommandParseException(this, this.string.substring(start, getCursor()), exception);
            }
            skipWhitespaces();
            if (peek() == ',') {
                skip();
                skipWhitespaces();
            }
            if (peek() == ']') {
                skip();
                return listTag;
            }
        }
        throw new ListTagBadFormatCommandParseException(this, String.valueOf(peek()), "No closing tag!");
    }

    public NBTTag readNBTTag() throws StringCommandParseException, ListTagBadFormatCommandParseException, CompoundTagBadFormatCommandParseException {
        // ToDo: Array tags
        skipWhitespaces();
        if (peek() == '[') {
            return readNBTListTag();
        }
        if (peek() == '{') {
            return readNBTCompoundTag();
        }
        String data = readString().toLowerCase(Locale.ROOT);
        if (data.equals("true")) {
            return new ByteTag(true);
        }
        if (data.equals("false")) {
            return new ByteTag(false);
        }
        try {
            if (NBT_PATTERN_BYTE.matcher(data).matches()) {
                return new ByteTag(Byte.parseByte(data.substring(0, data.length() - 1)));
            }
            if (NBT_PATTERN_SHORT.matcher(data).matches()) {
                return new ShortTag(Short.parseShort(data.substring(0, data.length() - 1)));
            }
            if (NBT_PATTERN_LONG.matcher(data).matches()) {
                return new LongTag(Long.parseLong(data.substring(0, data.length() - 1)));
            }
            if (NBT_PATTERN_INT.matcher(data).matches()) {
                return new IntTag(Integer.parseInt(data));
            }
            if (NBT_PATTERN_FLOAT.matcher(data).matches()) {
                return new FloatTag(Float.parseFloat(data.substring(0, data.length() - 1)));
            }
            if (NBT_PATTERN_DOUBLE.matcher(data).matches()) {
                return new DoubleTag(Double.parseDouble(data.endsWith("d") ? data.substring(0, data.length() - 1) : data));
            }
        } catch (NumberFormatException ignored) {
        }

        return new StringTag(data);
    }

    public CompoundTag readNBTCompoundTag() throws CompoundTagBadFormatCommandParseException, StringCommandParseException, ListTagBadFormatCommandParseException {
        if (peek() != '{') {
            throw new CompoundTagBadFormatCommandParseException(this, String.valueOf(peek()), "Not a compound tag!");
        }
        CompoundTag compoundTag = new CompoundTag();
        skip();
        skipWhitespaces();
        if (peek() == '}') {
            return compoundTag;
        }
        while (canRead()) {
            String key = readString();
            skipWhitespaces();
            if (peek() != ':') {
                throw new CompoundTagBadFormatCommandParseException(this, String.valueOf(peek()), "Invalid map char!");
            }
            skip();
            NBTTag value = readNBTTag();
            compoundTag.writeTag(key, value);
            skipWhitespaces();
            char nextChar = read();
            if (nextChar == '}') {
                return compoundTag;
            } else {
                skipWhitespaces();
                if (nextChar != ',') {
                    throw new CompoundTagBadFormatCommandParseException(this, String.valueOf(nextChar), "Invalid map char!");
                }
            }
        }
        throw new CompoundTagBadFormatCommandParseException(this, String.valueOf(peek()), "No closing tag!");
    }


    public JsonElement readJson() throws InvalidJSONCommandParseException {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(getRemaining()));
            jsonReader.setLenient(false);
            JsonObject json = GSON.fromJson(jsonReader, JsonObject.class);

            skip(Util.getJsonReaderPosition(jsonReader) - 1);
            return json;
        } catch (JsonParseException exception) {
            throw new InvalidJSONCommandParseException(this, String.valueOf(peek()), exception);
        }
    }

    public boolean readExpected(char... expected) {
        boolean ret = peekExpected(expected);
        skip();
        return ret;
    }

    @NotNull
    public String readRemaining() {
        String ret = this.string.substring(this.cursor);
        this.cursor = this.string.length();
        return ret;
    }

    @NotNull
    public String peekRemaining() {
        return this.string.substring(this.cursor);
    }

    @NotNull
    public String getString() {
        return this.string;
    }

    @Override
    public String toString() {
        if (canRead()) {
            return String.format("position=%d/%d: \"%s\"", this.cursor, this.string.length(), peekRemaining());
        }
        return String.format("position=%d/%d", this.cursor, this.string.length());
    }
}
