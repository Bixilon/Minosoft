// Averaging of texels for mipmap generation.

package example.jonathan2520;

public class SRGBAverager {
    private static final SRGBTable srgb = new SRGBTable();

    public static int average(int c0, int c1, int c2, int c3) {
        if ((((c0 | c1 | c2 | c3) ^ (c0 & c1 & c2 & c3)) & 0xff000000) == 0) {
            // Alpha values are all equal. Simplifies computation somewhat. It's
            // also a reasonable fallback when all alpha values are zero, in
            // which case the resulting color would normally be undefined.
            // Defining it like this allows code that uses invisible colors for
            // whatever reason to work. Note that Minecraft's original code
            // would set the color to black; this is added functionality.

            float r = srgb.decode(c0 & 0xff)
                    + srgb.decode(c1 & 0xff)
                    + srgb.decode(c2 & 0xff)
                    + srgb.decode(c3 & 0xff);

            float g = srgb.decode(c0 >> 8 & 0xff)
                    + srgb.decode(c1 >> 8 & 0xff)
                    + srgb.decode(c2 >> 8 & 0xff)
                    + srgb.decode(c3 >> 8 & 0xff);

            float b = srgb.decode(c0 >> 16 & 0xff)
                    + srgb.decode(c1 >> 16 & 0xff)
                    + srgb.decode(c2 >> 16 & 0xff)
                    + srgb.decode(c3 >> 16 & 0xff);

            return srgb.encode(0.25F * r)
                    | srgb.encode(0.25F * g) << 8
                    | srgb.encode(0.25F * b) << 16
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

            float r = a0 * srgb.decode(c0 & 0xff)
                    + a1 * srgb.decode(c1 & 0xff)
                    + a2 * srgb.decode(c2 & 0xff)
                    + a3 * srgb.decode(c3 & 0xff);

            float g = a0 * srgb.decode(c0 >> 8 & 0xff)
                    + a1 * srgb.decode(c1 >> 8 & 0xff)
                    + a2 * srgb.decode(c2 >> 8 & 0xff)
                    + a3 * srgb.decode(c3 >> 8 & 0xff);

            float b = a0 * srgb.decode(c0 >> 16 & 0xff)
                    + a1 * srgb.decode(c1 >> 16 & 0xff)
                    + a2 * srgb.decode(c2 >> 16 & 0xff)
                    + a3 * srgb.decode(c3 >> 16 & 0xff);

            float a = a0 + a1 + a2 + a3;

            return srgb.encode(r / a)
                    | srgb.encode(g / a) << 8
                    | srgb.encode(b / a) << 16
                    | (int) (0.25F * a + 0.5F) << 24;
        }
    }
}
