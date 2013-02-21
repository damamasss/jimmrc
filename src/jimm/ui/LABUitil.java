//package jimm.ui;
//
///**
// * Created [02.03.2011, 0:02:36]
// * Develop by Lavlinsky Roman on 2011
// */
//public final class LABUitil {
//
//    double L, A, B;
//
//    public LABUitil() {
//    }
//
//    // TODO Natural Logarithm and Euler's
//    double pow(double a, double b) {
//        boolean gt1 = (Math.sqrt((a - 1) * (a - 1)) > 1);
//        int oc = -1;
//        int iter = 20;
//        double p, x, x2, sumX, sumY;
//        if ((b - Math.floor(b)) == 0) {
//            p = a;
//            for (int i = 1; i < b; i++) p *= a;
//            return p;
//        }
//        x = (gt1) ? (a / (a - 1)) : (a - 1);
//        sumX = (gt1) ? (1 / x) : x;
//        for (int i = 2; i < iter; i++) {
//            p = x;
//            for (int j = 1; j < i; j++) p *= x;
//            double xTemp = (gt1) ? (1 / (i * p)) : (p / i);
//            sumX = (gt1) ? (sumX + xTemp) : (sumX + (xTemp * oc));
//            oc *= -1;
//        }
//        x2 = b * sumX;
//        sumY = 1 + x2;
//        for (int i = 2; i <= iter; i++) {
//            p = x2;
//            for (int j = 1; j < i; j++) p *= x2;
//            int yTemp = 2;
//            for (int j = i; j > 2; j--) yTemp *= j;
//            sumY += p / yTemp;
//        }
//        return sumY;
//    }
//
//    public int ConvertLabToRGB() {
//        double vy = (L + 16.0) / 116.0;
//        double vx = A / 500.0 + vy;
//        double vz = vy - B / 200.0;
//
//        System.out.println("1LAB" + vy);
//        System.out.println("1LAB" + vx);
//        System.out.println("1LAB" + vz);
//
//        double vx3 = vx * vx * vx;
//        double vy3 = vy * vy * vy;
//        double vz3 = vz * vz * vz;
//
//        System.out.println("2LAB" + vx3);
//        System.out.println("2LAB" + vy3);
//        System.out.println("2LAB" + vz3);
//
//        if (vy3 > 0.008856)
//            vy = vy3;
//        else
//            vy = (vy - 16.0 / 116.0) / 7.787;
//
//        if (vx3 > 0.008856)
//            vx = vx3;
//        else
//            vx = (vx - 16.0 / 116.0) / 7.787;
//
//        if (vz3 > 0.008856)
//            vz = vz3;
//        else
//            vz = (vz - 16.0 / 116.0) / 7.787;
//
//        System.out.println("3LAB" + vy);
//        System.out.println("3LAB" + vx);
//        System.out.println("3LAB  " + vz);
//
//        vx *= 0.95047; //use white = D65
//        vz *= 1.08883;
//
//        double vr = (vx * 3.2406 + vy * -1.5372 + vz * -0.4986);
//        double vg = (vx * -0.9689 + vy * 1.8758 + vz * 0.0415);
//        double vb = (vx * 0.0557 + vy * -0.2040 + vz * 1.0570);
//
//        System.out.println("4LAB  " + vr);
//        System.out.println("4LAB  " + vg);
//        System.out.println("4LAB  " + vb);
//
//
//        if (vr > 0.0031308)
//            vr = (1.055 * pow(vr, (1.0 / 2.4)) - 0.055);
//        else
//            vr = 12.92 * vr;
//
//        if (vg > 0.0031308)
//            vg = (1.055 * pow(vg, (1.0 / 2.4)) - 0.055);
//        else
//            vg = 12.92 * vg;
//
//        if (vb > 0.0031308)
//            vb = (1.055 * pow(vb, (1.0 / 2.4)) - 0.055);
//        else
//            vb = 12.92 * vb;
//
//        vr *= 255;
//        vg *= 255;
//        vb *= 255;
//
//        System.out.println("5LAB  " + vr);
//        System.out.println("5LAB  " + vg);
//        System.out.println("5LAB  " + vb);
//
//        return (int) (((long) vr & 0xFF) << 16 | ((long) vg & 0xFF) << 8 | ((long) vb) & 0xFF);
//    }
//
//    public void ConvertRGBToLab(int rgb) {
//        int ir = (rgb >> 16) & 0xff;
//        int ig = (rgb >> 8) & 0xff;
//        int ib = (rgb) & 0xff;
//
//
//        double fr = (ir) / 255.0;
//        double fg = (ig) / 255.0;
//        double fb = (ib) / 255.0;
//
//        if (fr > 0.04045)
//            fr = pow((fr + 0.055) / 1.055, 2.4);
//        else
//            fr = fr / 12.92;
//
//        if (fg > 0.04045)
//            fg = pow((fg + 0.055) / 1.055, 2.4);
//        else
//            fg = fg / 12.92;
//
//        if (fb > 0.04045)
//            fb = pow((fb + 0.055) / 1.055, 2.4);
//        else
//            fb = fb / 12.92;
//
//        // Use white = D65
//        double x = fr * 0.4124 + fg * 0.3576 + fb * 0.1805;
//        double y = fr * 0.2126 + fg * 0.7152 + fb * 0.0722;
//        double z = fr * 0.0193 + fg * 0.1192 + fb * 0.9505;
//
//        double vx = x / 0.95047;
//        double vy = y;
//        double vz = z / 1.08883;
//
//        if (vx > 0.008856)
//            vx = pow(vx, 0.3333);
//        else
//            vx = (7.787 * vx) + (16.0 / 116.0);
//
//        if (vy > 0.008856)
//            vy = pow(vy, 0.3333);
//        else
//            vy = (7.787 * vy) + (16.0 / 116.0);
//
//        if (vz > 0.008856)
//            vz = pow(vz, 0.3333);
//        else
//            vz = (7.787 * vz) + (16.0 / 116.0);
//
//        L = Math.min(Math.max(116.0 * vy - 16.0, 0), 100);
//        A = 500.0 * (vx - vy);
//        B = 200.0 * (vy - vz);
//
//        System.out.println("LAB  " + L);
//        System.out.println("LAB  " + A);
//        System.out.println("LAB  " + B);
//    }
//}
