package su.hitori.pack.util;

import su.hitori.api.Pair;

/**
 * Portable Network Graphics (PNG) utils
 */
public final class PNGUtil {

    private static final byte[] PNG_SIGNATURE = asBytes(
            137, 80, 78, 71, 13, 10, 26, 10
    );

    private PNGUtil() {}

    private static byte[] asBytes(int... ints) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) ints[i];
        }
        return bytes;
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
                ((data[offset + 1] & 0xFF) << 16) |
                ((data[offset + 2] & 0xFF) << 8) |
                (data[offset + 3] & 0xFF);
    }

    /**
     * Checks if specified byte array is data in PNG format.
     * @param data byte array to check
     * @return if data is in PNG format
     */
    public static boolean isPNGData(byte[] data) {
        if(data.length < PNG_SIGNATURE.length) return false;

        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if(data[i] != PNG_SIGNATURE[i]) return false;
        }
        return true;
    }

    /**
     * Returns PNG image dimensions stored in header
     * @param data png data
     * @return Pair containing width and height of image inside the data or null if data is not in PNG format
     */
    public static Pair<Integer, Integer> getImageDimensions(byte[] data) {
        if(!isPNGData(data)) return null;
        return Pair.of(
                readInt(data, 16),
                readInt(data, 20)
        );
    }

}
