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

package de.bixilon.minosoft.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

@Deprecated(forRemoval = true)
public final class Util {
    public static final char[] RANDOM_STRING_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final Gson GSON = new Gson();
    private static final Random RANDOM = new Random();
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

    public static PlayInByteBuffer decompress(byte[] bytes, PlayConnection connection) {
        return new PlayInByteBuffer(decompress(bytes), connection);
    }

    public static byte[] decompress(byte[] bytes) {
        Inflater inflater = new Inflater();
        inflater.setInput(bytes, 0, bytes.length);
        byte[] buffer = new byte[ProtocolDefinition.DEFAULT_BUFFER_SIZE];
        ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);
        try {
            while (!inflater.finished()) {
                stream.write(buffer, 0, inflater.inflate(buffer));
            }
            stream.close();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    public static byte[] compress(byte[] bytes) {
        Deflater deflater = new Deflater();
        deflater.setInput(bytes);
        deflater.finish();
        byte[] buffer = new byte[ProtocolDefinition.DEFAULT_BUFFER_SIZE];
        ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);
        while (!deflater.finished()) {
            stream.write(buffer, 0, deflater.deflate(buffer));
        }
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    public static byte[] decompressGzip(byte[] raw) throws IOException {
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(raw));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int res = 0;
        byte[] buffer = new byte[ProtocolDefinition.DEFAULT_BUFFER_SIZE];
        while (res >= 0) {
            res = gzipInputStream.read(buffer, 0, buffer.length);
            if (res > 0) {
                outputStream.write(buffer, 0, res);
            }
        }
        gzipInputStream.close();
        byte[] ret = outputStream.toByteArray();
        outputStream.close();
        return ret;
    }

    public static String readReader(BufferedReader reader, boolean closeStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(LINE_SEPARATOR);
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        if (closeStream) {
            reader.close();
        }
        return stringBuilder.toString();
    }

    public static int getJsonReaderPosition(JsonReader jsonReader) {
        try {
            return JSON_READER_POS_FIELD.getInt(jsonReader) - JSON_READER_LINE_START_FIELD.getInt(jsonReader) + 1;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException();
        }
    }

    public static Map<String, String> urlQueryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        for (String parameter : query.split("&")) {
            String[] split = parameter.split("=");
            map.put(split[0], split[1]);
        }
        return map;
    }

    public static String mapToUrlQuery(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (builder.length() != 0) {
                builder.append("&");
            }
            try {
                builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    @NotNull
    @Deprecated
    public static JsonObject readJsonFromStream(@NotNull InputStream stream, boolean close) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        if (close) {
            reader.close();
        }
        return json;
    }
}
