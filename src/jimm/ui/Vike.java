////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//package jimm.ui;
//
//import DrawControls.NativeCanvas;
//import DrawControls.CanvasEx;
//import jimm.comm.Util;
//import jimm.comm.StringConvertor;
//import jimm.JimmUI;
//
//import javax.microedition.lcdui.Image;
//
//public class Vike {
//
//    final class bs {
//
//        br a_br_array1d_fld[];
//        int a_int_fld;
//        java.lang.String a_java_lang_String_fld;
//
//        private bs(byte byte0) {
//        }
//
//        bs() {
//            this((byte) 0);
//        }
//    }
//
//    final class br {
//
//        java.lang.String a_java_lang_String_fld;
//        int a_int_fld;
//        boolean a_boolean_fld;
//
//        private br(byte byte0) {
//        }
//
//        br() {
//            this((byte) 0);
//        }
//    }
//
//    final class bt {
//
//        br a_br_fld;
//        int a_int_fld;
//        int b;
//        int c;
//        int d;
//
//        private bt(byte byte0) {
//        }
//
//        bt() {
//            this((byte) 0);
//        }
//    }
//
//    private final String c_java_lang_String_array1d_fld[] = Util.explode("MDL <- -> RGT LFT\r123 SHT LNG SPC :) ETR BKSP", '\r');
//    private final String d_java_lang_String_array1d_fld[] = Util.explode("\u0439 \u0446 \u0443 \u043A \u0435 \u043D \u0433 \u0448 \u0449 \u0437 \u0445\r\u0444 \u044B \u0432 \u0430 \u043F \u0440 \u043E \u043B \u0434 \u0436 \u044D\r. \u044F \u0447 \u0441 \u043C \u0438 \u0442 \u044C \u0431 \u044E \u044A", '\r');
//    private final String e[] = Util.explode("@ q w e r t y u i o p\r/ a s d f g h j k l '\r! ? z x c v b n m , .", '\r');
//    final String a_java_lang_String_array1d_fld[] = Util.explode("1 2 3 4 5 6 7 8 9 0\r\u0451 - / : ; ( ) $ & @\r[ ] { } # % ^ * + =\r_ \\ | ~ < > . , ? ! ' \"", '\r');
//    final javax.microedition.lcdui.Font a_javax_microedition_lcdui_Font_fld = javax.microedition.lcdui.Font.getFont(64, 1, 16);
//    private final int c_int_fld;
//    final bs a_bs_array1d_fld[];
//    private final int a_int_array1d_fld[] = {
//            0xffffff, 0, 0x202020, 0xffffff
//    };
//    bs b_bs_array1d_fld[];
//    String b_java_lang_String_array1d_fld[];
//    static String a_java_lang_String_static_fld;
//    byte a_byte_fld;
//    int a_int_fld;
//    private javax.microedition.lcdui.Image a_javax_microedition_lcdui_Image_fld;
//    private String b_java_lang_String_fld;
//    int b_int_fld;
//    TextFieldEx a_bj_fld;
//    private String c_java_lang_String_fld;
//    private String d_java_lang_String_fld;
//    br a_br_fld;
//    final bt a_vikePostItem_fld = new bt();
//
//
//    public Vike(TextFieldEx bj1) {
//        c_int_fld = CanvasEx.facade.getFontHeight() + 4;
//        a_bs_array1d_fld = (new bs[]{
//                array(c_java_lang_String_array1d_fld[0]), array(c_java_lang_String_array1d_fld[1])
//        });
//        a_byte_fld = 0;
//        c_java_lang_String_fld = bj1.a().c();
//        d_java_lang_String_fld = bj1.a().d();
//        a_bj_fld = bj1;
//        initLang(a_java_lang_String_static_fld != null ? a_java_lang_String_static_fld : "ru");
//        if (ca.a(131)) {
//            a_byte_fld = 2;
//            Vike.a("SHT", a_bs_array1d_fld[1], true);
//        }
//        try {
//            Image vike = javax.microedition.lcdui.Image.createImage("/vike.png");
//            CanvasEx.imageToArray(a_int_array1d_fld, vike);
//        }
//        catch (Exception _ex) {
//        }
//    }
//
//    final void a(byte byte0) {
//        String as[] = null;
//        if (byte0 == 0)
//            as = d_java_lang_String_array1d_fld;
//        else if (byte0 == 1)
//            as = e;
//        else if (byte0 == 2)
//            as = a_java_lang_String_array1d_fld;
//        b_java_lang_String_array1d_fld = as;
//        init();
//    }
//
//    final void initLang(String s) {
//        a_java_lang_String_static_fld = s;
//        a(((byte) (a_java_lang_String_static_fld.equals("ru") ? 0 : 1)));
//    }
//
//    final void init() {
//        a_vikePostItem_fld.a_br_fld = null;
//        b_bs_array1d_fld = new bs[b_java_lang_String_array1d_fld.length + 2];
//        int i = (55 * NativeCanvas.getHeightEx()) / 100;
//        a_int_fld = c_int_fld;
//        b_int_fld = c_int_fld * b_bs_array1d_fld.length;
//        if (b_int_fld < i) {
//            a_int_fld = i / b_bs_array1d_fld.length;
//            b_int_fld = a_int_fld * b_bs_array1d_fld.length;
//        }
//        b_bs_array1d_fld[0] = a(a_bs_array1d_fld[0]);
//        for (int j = b_java_lang_String_array1d_fld.length - 1; j >= 0; j--)
//            b_bs_array1d_fld[j + 1] = array(b_java_lang_String_array1d_fld[j]);
//
//        b_bs_array1d_fld[b_bs_array1d_fld.length - 1] = a(a_bs_array1d_fld[1]);
//        a_bj_fld.i();
//        Vike bp1 = this;
//        String s = "/vike_" + NativeCanvas.getWidthEx() + "_";
//        s = s + (bp1.b_java_lang_String_array1d_fld != bp1.a_java_lang_String_array1d_fld ? a_java_lang_String_static_fld : "123");
//        if (!(s = s + ".png").equals(bp1.b_java_lang_String_fld)) {
//            bp1.b_java_lang_String_fld = s;
//            try {
//                bp1.a_javax_microedition_lcdui_Image_fld = javax.microedition.lcdui.Image.createImage(s);
//                return;
//            }
//            catch (java.io.IOException _ex) {
//            }
//        }
//    }
//
//    final String a(String s) {
//        if (a_byte_fld == 0)
//            return s;
//        if (b_java_lang_String_array1d_fld == d_java_lang_String_array1d_fld && s.equals("."))
//            return ",";
//        else
//            return StringConvertor.toUpperCase(s);
//    }
//
//    final void a(char c1, boolean flag) {
//        if (ca.a(131) && (a_bj_fld.a(c1) || flag) && a_byte_fld == 0) {
//            Vike.a("SHT", a_bs_array1d_fld[1], true);
//            a_byte_fld = 2;
//        }
//    }
//
//    private bs array(String s) {
//        bs bs1 = new bs();
//        df.a(s, ' ');
//        bs1.a_java_lang_String_fld = s;
//        return a(bs1);
//    }
//
//    private bs a(bs bs1) {
//        String as[];
//        int i;
//        int j = i = (as = Util.explode(bs1.a_java_lang_String_fld, ' ')).length;
//        int l = NativeCanvas.getWidthEx();
//        //bs1.a_java_lang_String_fld.indexOf("SPC");
//        boolean flag = bs1.a_br_array1d_fld == null;
//        int i1 = 0;
//        if (flag)
//            bs1.a_br_array1d_fld = new br[i];
//        for (int j1 = i - 1; j1 >= 0; j1--) {
//            br br1;
//            if (flag)
//                (br1 = bs1.a_br_array1d_fld[j1] = new br()).a_java_lang_String_fld = as[j1];
//            else
//                br1 = bs1.a_br_array1d_fld[j1];
//            String s = as[j1];
//            String s2 = null;
//            if (s.equals("LFT"))
//                s2 = c_java_lang_String_fld;
//            else if (s.equals("RGT"))
//                s2 = d_java_lang_String_fld;
//            if (s2 != null) {
//                br1.a_int_fld = JimmUI.standartFAcade.stringWidth(s2) + 16;
//                l -= br1.a_int_fld;
//                j--;
//            } else {
//                br1.a_int_fld = 0;
//            }
//        }
//
//        int k = l;
//        if (j <= 0)
//            k = 0;
//        else if (bs1.a_java_lang_String_fld.indexOf("SPC") == -1)
//            k /= j;
//        else
//            k /= j + 1;
//        for (int k1 = i - 1; k1 >= 0; k1--) {
//            br br2 = bs1.a_br_array1d_fld[k1];
//            String s1 = as[k1];
//            if (br2.a_int_fld == 0)
//                br2.a_int_fld = s1.equals("SPC") ? k << 1 : k;
//            i1 += br2.a_int_fld;
//        }
//
//        int l1 = NativeCanvas.getWidthEx() - i1;
//        bs1.a_int_fld = l1 / 2;
//        if (l1 % 2 != 0)
//            bs1.a_int_fld++;
//        return bs1;
//    }
//
//    private void a(javax.microedition.lcdui.Graphics g, int i, int j) {
//        g.fillTriangle(i, j + 6, i + 4, j + 6 + 4, i - 4, j + 6 + 4);
//        g.fillRect(i - 1, j + 6 + 4, 2, a_int_fld - 16);
//    }
//
//    private static void a(javax.microedition.lcdui.Graphics g, int i, int j, int k, int l, int i1) {
//        CanvasEx.drawGlassRect(g, i1, i, j, i + k, j + l);
//    }
//
//    protected final void a(javax.microedition.lcdui.Graphics g) {
//        int i = NativeCanvas.getWidthEx(); //    h.b(); = w
//        int j;
//        int l = j = NativeCanvas.getHeightEx();  //h.a(); = h
//        if (a_javax_microedition_lcdui_Image_fld != null)
//            g.drawImage(a_javax_microedition_lcdui_Image_fld, 0, j, 36);
//        for (int k1 = b_bs_array1d_fld.length - 1; k1 >= 0; k1--) {
//            bs bs1 = b_bs_array1d_fld[k1];
//            l -= a_int_fld;
//            int i1 = i - bs1.a_int_fld;
//            for (int l1 = bs1.a_br_array1d_fld.length - 1; l1 >= 0; l1--) {
//                br br1 = bs1.a_br_array1d_fld[l1];
//                int j1 = (((i1 -= br1.a_int_fld) << 1) + br1.a_int_fld) / 2;
//                int k = ((l << 1) + a_int_fld) / 2;
//                if (a_javax_microedition_lcdui_Image_fld == null)
//                    Vike.a(g, i1, l, br1.a_int_fld - 1, a_int_fld - 1, br1.a_boolean_fld ? a_int_array1d_fld[3] : a_int_array1d_fld[2]);
//                g.setColor(br1.a_boolean_fld ? a_int_array1d_fld[1] : a_int_array1d_fld[0]);
//                if (a_javax_microedition_lcdui_Image_fld == null)
//                    g.drawRect(i1, l, br1.a_int_fld - 1, a_int_fld - 1);
//                String s;
//                if ((s = br1.a_java_lang_String_fld).equals("SPC"))
//                    continue;
//                if (s.equals("SHT")) {
//                    if (a_byte_fld == 1) {
//                        a(g, j1 - 5, l);
//                        a(g, j1 + 5, l);
//                    } else {
//                        a(g, j1, l);
//                    }
//                    continue;
//                }
//                if (s.equals("BKSP")) {
//                    g.fillTriangle(i1 + 6, k, i1 + 6 + 4, k + 4, i1 + 6 + 4, k - 4);
//                    g.fillRect(i1 + 6 + 4, k - 1, br1.a_int_fld - 16, 3);
//                    continue;
//                }
//                if (s.equals("ETR")) {
//                    g.fillTriangle(i1 + 6, k, i1 + 6 + 4, k + 4, i1 + 6 + 4, k - 4);
//                    g.fillRect(i1 + 6 + 4, k - 1, br1.a_int_fld - 16, 3);
//                    g.fillRect((i1 + 6 + 4 + br1.a_int_fld) - 16 - 3, k - 6, 3, 5);
//                    continue;
//                }
//                if (s.equals("<-")) {
//                    g.fillTriangle(j1 - 2, k, (j1 - 2) + 4, k + 4, (j1 - 2) + 4, k - 4);
//                    continue;
//                }
//                if (s.equals("->")) {
//                    g.fillTriangle(j1 + 2, k, (j1 + 2) - 4, k + 4, (j1 + 2) - 4, k - 4);
//                    continue;
//                }
//                if (s.equals("MDL")) {
//                    g.drawLine((j1 - 4) + 1, k - 4 - 1, j1 + 4 + 1, (k + 4) - 1);
//                    g.drawLine((j1 - 4) + 1, k - 4, j1 + 4, (k + 4) - 1);
//                    g.drawLine(j1 - 4, k - 4, j1 + 4, k + 4);
//                    g.drawLine(j1 - 4 - 1, (k - 4) + 1, (j1 + 4) - 1, k + 4 + 1);
//                    g.drawLine(j1 - 4, (k - 4) + 1, (j1 + 4) - 1, k + 4);
//                    g.drawLine(j1 + 4 + 1, (k - 4) + 1, (j1 - 4) + 1, k + 4 + 1);
//                    g.drawLine(j1 + 4, (k - 4) + 1, (j1 - 4) + 1, k + 4);
//                    g.drawLine(j1 + 4, k - 4, j1 - 4, k + 4);
//                    g.drawLine((j1 + 4) - 1, k - 4 - 1, j1 - 4 - 1, (k + 4) - 1);
//                    g.drawLine((j1 + 4) - 1, k - 4, j1 - 4, (k + 4) - 1);
//                    continue;
//                }
//                if (s.equals("LNG"))
//                    s = a_java_lang_String_static_fld;
//                else if (s.equals("RGT"))
//                    s = d_java_lang_String_fld;
//                else if (s.equals("LFT"))
//                    s = c_java_lang_String_fld;
//                if (bs1 != a_bs_array1d_fld[0])
//                    s = a(s);
//                CanvasEx.drawString(g, JimmUI.standartFAcade, s, j1, (((l << 1) + a_int_fld) - JimmUI.standartFAcade.getFontHeight()) / 2, 17);
//            }
//
//        }
//
//        if (a_vikePostItem_fld.a_br_fld != null) {
//            Vike.a(g, a_vikePostItem_fld.a_int_fld, a_vikePostItem_fld.b, a_vikePostItem_fld.c, a_vikePostItem_fld.d, a_vikePostItem_fld.a_br_fld.a_boolean_fld ? a_int_array1d_fld[3] : a_int_array1d_fld[2]);
//            g.setColor(a_vikePostItem_fld.a_br_fld.a_boolean_fld ? a_int_array1d_fld[1] : a_int_array1d_fld[0]);
//            g.drawRect(a_vikePostItem_fld.a_int_fld, a_vikePostItem_fld.b, a_vikePostItem_fld.c - 1, a_vikePostItem_fld.d - 1);
//            CanvasEx.drawString(g, JimmUI.standartFAcade, a(a_vikePostItem_fld.a_br_fld.a_java_lang_String_fld), (2 * a_vikePostItem_fld.a_int_fld + a_vikePostItem_fld.c) / 2, ((2 * a_vikePostItem_fld.b + a_vikePostItem_fld.d) - JimmUI.standartFAcade.getFontHeight()) / 2, 17);
//        }
//    }
//
//    static boolean a(int i, int j, int k, int l, int i1, int j1) {
//        i -= k;
//        j -= i1;
//        return i >= 0 && j >= 0 && i <= l && j <= j1;
//    }
//
//    static void a(String s, bs bs1, boolean flag) {
//        for (int i = bs1.a_br_array1d_fld.length - 1; i >= 0; i--)
//            if (bs1.a_br_array1d_fld[i].a_java_lang_String_fld.equals(s)) {
//                bs1.a_br_array1d_fld[i].a_boolean_fld = flag;
//                return;
//            }
//
//    }
//}
////#sijapp cond.end#