package de.bixilon.minosoft.util;

public class BitByte {
    public static boolean isBitSet(int in, int pos) {
        boolean bitSet;
        int mask = 1 << pos;
        bitSet = ((in & mask) == mask);
        return bitSet;
    }

    public static byte getLow4Bits(byte input) {
        return (byte) (input & 0xF);
    }

    public static byte getHigh4Bits(byte input) {
        return (byte) ((input) >> 4 & 0xF);
    }

}
