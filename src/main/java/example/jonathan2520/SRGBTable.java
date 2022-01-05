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

// Source: https://bugs.mojang.com/browse/MC-114265

// Offers fast sRGB encoding and decoding.

// Decoding is a straightforward table look-up.

// Encoding is a little more sophisticated. It's an exact conversion using about
// 4 kB of look-up tables that's also still quick. It relies on the fact that
// thresholds that would round to the next value have a minimum spacing of about
// 0.0003. That means that any range of 0.0003 contains at most one threshold.
// The to_int table contains the smaller value in the range. The threshold table
// contains the threshold above which the value should be one greater.

// The minimum scale value that maintains proper spacing is 255 * encode_slope
// or about 3295.4. You can get away with a little bit less like 3200, taking
// advantage of the alignment of thresholds, but it's not really worth it.

package example.jonathan2520;

public class SRGBTable {
    private final float scale;
    private final float[] to_float;
    private final float[] threshold;
    private final byte[] to_int;

    public SRGBTable() {
        this(new SRGBCalculator(), 3295.5F);
    }

    public SRGBTable(SRGBCalculator calculator, float scale) {
        this.scale = scale;
        to_float = new float[256];
        threshold = new float[256];
        to_int = new byte[(int) scale + 1];
        for (int i = 0; i < 255; ++i) {
            to_float[i] = (float) calculator.decode(i / 255.0);
            double dthresh = calculator.decode((i + 0.5) / 255.0);
            float fthresh = (float) dthresh;
            if (fthresh >= dthresh)
                fthresh = Math.nextAfter(fthresh, -1);
            threshold[i] = fthresh;
        }
        to_float[255] = 1;
        threshold[255] = Float.POSITIVE_INFINITY;
        int offset = 0;
        for (int i = 0; i < 255; ++i) {
            int up_to = (int) (threshold[i] * scale);
            build_to_int_table(offset, up_to, (byte) i);
            offset = up_to + 1;
        }
        build_to_int_table(offset, (int) scale, (byte) 255);
    }

    private void build_to_int_table(int offset, int up_to, byte value) {
        if (offset > up_to)
            throw new IllegalArgumentException("scale is too small");
        while (offset <= up_to)
            to_int[offset++] = value;
    }

    // x in [0, 255]
    public float decode(int x) {
        return to_float[x];
    }

    // x in about [-0.0003, 1.00015]: tolerates rounding error on top of [0, 1]
    public int encode(float x) {
        int index = to_int[(int) (x * scale)] & 0xff;
        if (x > threshold[index])
            ++index;
        return index;
    }
}
