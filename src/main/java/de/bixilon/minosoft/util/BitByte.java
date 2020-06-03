package de.bixilon.minosoft.util;

public class BitByte {
    public static boolean isBitSet(int in, int pos) {
        boolean bitSet;
        int mask = 1 << pos;
        bitSet = ((in & mask) == mask);
        return bitSet;
    }

    public static short[] byteArrayToShortArray(byte[] readBytes) {
        short[] ret = new short[readBytes.length];
        for (int i = 0; i < readBytes.length; i++) {
            ret[0] = readBytes[0];
        }
        return ret;
    }
}
