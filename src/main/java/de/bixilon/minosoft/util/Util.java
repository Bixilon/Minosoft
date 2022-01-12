/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Field;

@Deprecated(forRemoval = true)
public final class Util {
    public static final Gson GSON = new Gson();
    private static final Field JSON_READER_POS_FIELD;
    private static final Field JSON_READER_LINE_START_FIELD;

    static {
        new JsonReader(new StringReader(""));
        Class<?> jsonReadClass = JsonReader.class;
        try {
            JSON_READER_POS_FIELD = jsonReadClass.getDeclaredField("pos");
            JSON_READER_POS_FIELD.setAccessible(true);
            JSON_READER_LINE_START_FIELD = jsonReadClass.getDeclaredField("lineStart");
            JSON_READER_LINE_START_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static int getJsonReaderPosition(JsonReader jsonReader) {
        try {
            return JSON_READER_POS_FIELD.getInt(jsonReader) - JSON_READER_LINE_START_FIELD.getInt(jsonReader) + 1;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException();
        }
    }
}
