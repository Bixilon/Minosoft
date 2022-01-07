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

// Offers very precise sRGB encoding and decoding.

// The actual values defining sRGB are alpha = 0.055 and gamma = 2.4.

// This class works directly from that definition to take advantage of all
// available precision, unlike pre-rounded constants like 12.92 that cause a
// comparatively humongous discontinuity at a point that should be of
// differentiability class C^1.

// Stored values are chosen to speed up bulk conversion somewhat.

package example.jonathan2520;

public class SRGBCalculator {
    private final double decode_threshold;
    private final double decode_slope;
    private final double decode_multiplier;
    private final double decode_addend;
    private final double decode_exponent;
    private final double encode_threshold;
    private final double encode_slope;
    private final double encode_multiplier;
    private final double encode_addend;
    private final double encode_exponent;

    public SRGBCalculator(double gamma, double alpha) {
        encode_multiplier = alpha + 1.0;
        decode_multiplier = 1.0 / encode_multiplier;
        encode_addend = -alpha;
        decode_addend = decode_multiplier * alpha;
        encode_exponent = 1.0 / gamma;
        decode_exponent = gamma;
        decode_threshold = alpha / (gamma - 1.0);
        encode_threshold = Math.pow(gamma * decode_threshold * decode_multiplier, gamma);
        encode_slope = decode_threshold / encode_threshold;
        decode_slope = encode_threshold / decode_threshold;
    }

    public SRGBCalculator() {
        this(2.4, 0.055);
    }

    public double decode(double x) {
        if (x < decode_threshold)
            return decode_slope * x;
        else
            return Math.pow(x * decode_multiplier + decode_addend, decode_exponent);
    }

    public double encode(double x) {
        if (x < encode_threshold)
            return encode_slope * x;
        else
            return Math.pow(x, encode_exponent) * encode_multiplier + encode_addend;
    }
}
