/*
 * Minosoft
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

public final class BitByte {
    public static boolean isBitSet(int in, int pos) {
        int mask = 1 << pos;
        return ((in & mask) == mask);
    }

    public static boolean isBitMask(int in, int mask) {
        return ((in & mask) == mask);
    }

    public static byte getBitCount(short input) {
        byte ret = 0;
        for (byte i = 0; i < Short.BYTES * 8; i++) { // bytes to bits
            if (isBitSetShort(input, i)) {
                ret++;
            }
        }
        return ret;
    }

    public static boolean isBitSetShort(short in, int pos) {
        int mask = 1 << pos;
        return ((in & mask) == mask);
    }

    public static short byteToUShort(byte b) {
        return (short) (b & 0xFF);
    }
}
