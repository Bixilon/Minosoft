// Source: https://bugs.mojang.com/browse/MC-114265

// Averaging of texels for mipmap generation.

package example.jonathan2520;

public class SRGBAverager {
    private static final SRGBTable SRGB = new SRGBTable();

    public static int average(int c0, int c1, int c2, int c3) {
        if ((((c0 | c1 | c2 | c3) ^ (c0 & c1 & c2 & c3)) & 0xff000000) == 0) {
            // Alpha values are all equal. Simplifies computation somewhat. It's
            // also a reasonable fallback when all alpha values are zero, in
            // which case the resulting color would normally be undefined.
            // Defining it like this allows code that uses invisible colors for
            // whatever reason to work. Note that Minecraft's original code
            // would set the color to black; this is added functionality.

            float r = SRGB.decode(c0 & 0xff)
                    + SRGB.decode(c1 & 0xff)
                    + SRGB.decode(c2 & 0xff)
                    + SRGB.decode(c3 & 0xff);

            float g = SRGB.decode(c0 >> 8 & 0xff)
                    + SRGB.decode(c1 >> 8 & 0xff)
                    + SRGB.decode(c2 >> 8 & 0xff)
                    + SRGB.decode(c3 >> 8 & 0xff);

            float b = SRGB.decode(c0 >> 16 & 0xff)
                    + SRGB.decode(c1 >> 16 & 0xff)
                    + SRGB.decode(c2 >> 16 & 0xff)
                    + SRGB.decode(c3 >> 16 & 0xff);

            return SRGB.encode(0.25F * r)
                    | SRGB.encode(0.25F * g) << 8
                    | SRGB.encode(0.25F * b) << 16
                    | c0 & 0xff000000;
        } else {
            // The general case. Well-defined if at least one alpha value is
            // not zero. If you do try to process all zeros, you get
            // r = g = b = a = 0 which will NaN out in the division and produce
            // invisible black. You could remove the other case if you're okay
            // with that, but mind that producing or consuming a NaN causes an
            // extremely slow exception handler to be run on many CPUs.

            float a0 = c0 >>> 24;
            float a1 = c1 >>> 24;
            float a2 = c2 >>> 24;
            float a3 = c3 >>> 24;

            float r = a0 * SRGB.decode(c0 & 0xff)
                    + a1 * SRGB.decode(c1 & 0xff)
                    + a2 * SRGB.decode(c2 & 0xff)
                    + a3 * SRGB.decode(c3 & 0xff);

            float g = a0 * SRGB.decode(c0 >> 8 & 0xff)
                    + a1 * SRGB.decode(c1 >> 8 & 0xff)
                    + a2 * SRGB.decode(c2 >> 8 & 0xff)
                    + a3 * SRGB.decode(c3 >> 8 & 0xff);

            float b = a0 * SRGB.decode(c0 >> 16 & 0xff)
                    + a1 * SRGB.decode(c1 >> 16 & 0xff)
                    + a2 * SRGB.decode(c2 >> 16 & 0xff)
                    + a3 * SRGB.decode(c3 >> 16 & 0xff);

            float a = a0 + a1 + a2 + a3;

            return SRGB.encode(r / a)
                    | SRGB.encode(g / a) << 8
                    | SRGB.encode(b / a) << 16
                    | (int) (0.25F * a + 0.5F) << 24;
        }
    }
}
