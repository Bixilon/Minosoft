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

package de.bixilon.minosoft.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.zip.*;

public final class Util {
    public static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"); // thanks https://www.spigotmc.org/threads/free-code-easily-convert-between-trimmed-and-full-uuids.165615
    public static final String RANDOM_STRING_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random random = ThreadLocalRandom.current();

    public static UUID getUUIDFromString(String uuid) {
        if (uuid.length() == 36) {
            return UUID.fromString(uuid);
        }
        if (uuid.length() == 32) {
            return UUID.fromString(UUID_FIX.matcher(uuid.replace("-", "")).replaceAll("$1-$2-$3-$4-$5"));
        }
        throw new IllegalArgumentException(String.format("%s is not a valid UUID String", uuid));
    }

    public static InByteBuffer decompress(byte[] bytes, Connection connection) {
        return new InByteBuffer(decompress(bytes), connection);
    }

    public static byte[] decompress(byte[] bytes) {
        Inflater inflater = new Inflater();
        inflater.setInput(bytes, 0, bytes.length);
        byte[] buffer = new byte[4096];
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
        byte[] buffer = new byte[4096];
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
        byte[] buf = new byte[4096];
        while (res >= 0) {
            res = gzipInputStream.read(buf, 0, buf.length);
            if (res > 0) {
                outputStream.write(buf, 0, res);
            }
        }
        gzipInputStream.close();
        byte[] ret = outputStream.toByteArray();
        outputStream.close();
        return ret;
    }

    public static String sha1(byte[] data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            return sha1(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha1(File file) throws IOException {
        return sha1(new FileInputStream(file));
    }

    public static String sha1Gzip(File file) throws IOException {
        return sha1(new GZIPInputStream(new FileInputStream(file)));
    }

    public static String sha1(InputStream inputStream) throws IOException {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer, 0, 4096)) != -1) {
                crypt.update(buffer, 0, length);
            }
            return byteArrayToHexString(crypt.digest());
        } catch (NoSuchAlgorithmException | FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static String sha1(String string) {
        return sha1(string.getBytes(StandardCharsets.UTF_8));
    }

    public static HashMap<String, String> readTarGzFile(String fileName) throws IOException {
        File inputFile = new File(fileName);
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(inputFile)));
        HashMap<String, String> ret = new HashMap<>();
        TarArchiveEntry entry;
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            ret.put(entry.getName(), readFile(new BufferedReader(new InputStreamReader(tarArchiveInputStream)), false));
        }
        tarArchiveInputStream.close();

        return ret;
    }

    public static String readFile(BufferedReader reader, boolean closeStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        if (closeStream) {
            reader.close();
        }
        return stringBuilder.toString();
    }

    public static HashMap<String, JsonObject> readJsonTarGzFile(String fileName) throws IOException {
        File inputFile = new File(fileName);
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(inputFile)));
        HashMap<String, JsonObject> ret = new HashMap<>();
        TarArchiveEntry entry;
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            ret.put(entry.getName(), JsonParser.parseReader(new InputStreamReader(tarArchiveInputStream)).getAsJsonObject());
        }
        tarArchiveInputStream.close();

        return ret;
    }

    public static JsonObject readJsonAsset(String path) throws IOException {
        return readJsonAsset(path, Util.class);
    }

    public static JsonObject readJsonAsset(String path, Class<?> clazz) throws IOException {
        InputStreamReader reader = readAsset(path, clazz);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        return json;
    }

    public static InputStreamReader readAsset(String path, Class<?> clazz) {
        return new InputStreamReader(clazz.getResourceAsStream("/assets/" + path));
    }

    public static JsonObject readJsonFromZip(String fileName, ZipFile zipFile) throws IOException {
        InputStreamReader reader = getInputSteamFromZip(fileName, zipFile);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        return json;
    }

    public static InputStreamReader getInputSteamFromZip(String fileName, ZipFile zipFile) throws IOException {
        return new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(fileName)));
    }

    public static JsonObject readJsonFromFile(String fileName) throws IOException {
        FileReader reader = new FileReader(fileName);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        return json;
    }

    public static void downloadFile(String url, String destination) throws IOException {
        createParentFolderIfNotExist(destination);
        copyFile(getInputStreamByURL(url), new FileOutputStream(destination));
    }

    public static void downloadFileAsGz(String url, String destination) throws IOException {
        createParentFolderIfNotExist(destination);
        copyFile(getInputStreamByURL(url), new GZIPOutputStream(new FileOutputStream(destination)));
    }

    public static void copyFile(InputStream inputStream, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer, 0, 4096)) != -1) {
            output.write(buffer, 0, length);
        }
        inputStream.close();
        output.close();
    }

    public static BufferedInputStream getInputStreamByURL(String url) throws IOException {
        return new BufferedInputStream(new URL(url).openStream());
    }

    public static ThreadFactory getThreadFactory(String threadName) {
        return new ThreadFactoryBuilder().setNameFormat(threadName + "#%d").build();
    }

    public static boolean createParentFolderIfNotExist(String file) {
        return createParentFolderIfNotExist(new File(file));
    }

    public static boolean createParentFolderIfNotExist(File file) {
        return file.getParentFile().mkdirs();
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_STRING_CHARS.charAt(random.nextInt(RANDOM_STRING_CHARS.length())));
        }
        return sb.toString();
    }

    public static String getStringBetween(String search, String first, String second) {
        String result = search.substring(search.indexOf(first) + first.length());
        return result.substring(0, result.indexOf(second));
    }
}
