//package jimm.util;
//
///**
// * Created [17.03.2011, 21:31:42]
// * Develop by Lavlinsky Roman on 2011
// */
//public class ImageControl /* MIDP 2.1 */ {
//
//    static boolean control = false;
//
//    static {
//        try {
//            control = Class.forName("com.sun.mmedia.ImageEncoder") != null;
//        } catch (ClassNotFoundException ignored) {
//        }
//    }
//
//    private static final byte IMAGE_RAW = 0;
//    private static final byte IMAGE_JPEG = 1;
//    private static final byte IMAGE_PNG = 2;
//
//    byte format;
//    int quality;
//    int inputWidth;
//    int inputHeight;
//    int outputLength;
//    byte inputPixels[];
//    byte outputPixels[];
//
//    public void parameter(byte inputPixels[], byte format, int inputWidth, int inputHeight, int quality) {
//        this.inputPixels = inputPixels;
//        this.format = format;
//        this.inputWidth = inputWidth;
//        this.inputHeight = inputHeight;
//        this.quality = quality;
//    }
//
//    public boolean process() {
//        switch (format) {
//            case IMAGE_RAW:
//                outputPixels = inputPixels;
//                outputLength = inputPixels.length;
//                return true;
//
//            case IMAGE_JPEG:
//                outputLength = com.sun.mmedia.ImageEncoder.RGBByteCompress(inputPixels, inputWidth, inputHeight, quality, outputPixels, (int) IMAGE_JPEG);
//                return true;
//
//            case IMAGE_PNG:
//                outputLength = com.sun.mmedia.ImageEncoder.RGBByteCompress(inputPixels, inputWidth, inputHeight, 100, outputPixels, (int) IMAGE_PNG);
//                return true;
//        }
//        return false;
//    }
//
//    public byte[] output() {
//        if (control & process()) {
//            return outputPixels;
//        }
//        return new byte[0];
//    }
//}
