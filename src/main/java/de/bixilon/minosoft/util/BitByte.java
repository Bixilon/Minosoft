package de.bixilon.minosoft.util;

public class BitByte {
    public static boolean isBitSet(int in, int pos) {
        boolean bitSet;
        int mask = 1 << pos;
        bitSet = ((in & mask) == mask);
        return bitSet;
    }

    public static boolean isBitSetShort(short in, int pos) {
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

    public static byte getBitCount(short input) {
        byte ret = 0;
        for (int i = 0; i < Short.BYTES * 8; i++) { // bytes to bits
            if (isBitSetShort(input, i)) {
                ret++;
            }
        }
        return ret;
    }

}
