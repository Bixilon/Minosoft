/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

public class Util {
    static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    // thanks https://www.spigotmc.org/threads/free-code-easily-convert-between-trimmed-and-full-uuids.165615

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static UUID formatUUID(String uuid) {
        return UUID.fromString(UUID_FIX.matcher(uuid.replace("-", "")).replaceAll("$1-$2-$3-$4-$5"));
    }

    public static InByteBuffer decompress(byte[] bytes, ProtocolVersion version) {
        // decompressing chunk data
        Inflater inflater = new Inflater();
        inflater.setInput(bytes, 0, bytes.length);
        byte[] result = new byte[4096];
        ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);
        try {
            while (!inflater.finished()) {
                stream.write(result, 0, inflater.inflate(result));
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new InByteBuffer(stream.toByteArray(), version);
    }

    public static byte[] compress(byte[] bytes) {
        // decompressing chunk data
        Deflater deflater = new Deflater();
        deflater.setInput(bytes, 0, bytes.length);
        byte[] result = new byte[4096];
        ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);
        while (!deflater.finished()) {
            stream.write(result, 0, deflater.deflate(result));
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
        ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        int res = 0;
        byte[] buf = new byte[1024];
        while (res >= 0) {
            res = gzipInputStream.read(buf, 0, buf.length);
            if (res > 0) {
                outputStream.write(buf, 0, res);
            }
        }
        return outputStream.toByteArray();
    }

    public static String sha1(String string) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string.getBytes(StandardCharsets.UTF_8));
            return new String(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
