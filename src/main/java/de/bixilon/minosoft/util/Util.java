package de.bixilon.minosoft.util;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Util {
    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
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

    public static InByteBuffer decompress(byte[] bytes) {
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
        return new InByteBuffer(stream.toByteArray());
    }
}
