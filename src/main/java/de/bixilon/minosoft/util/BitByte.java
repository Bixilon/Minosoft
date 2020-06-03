package de.bixilon.minosoft.util;

public class BitByte {
    public static boolean isBitSet(byte in, int pos) {
        boolean bitSet;
        int mask = 1 << pos;
        bitSet = ((in & mask) == mask);
        return bitSet;
    }
}
