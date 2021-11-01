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
