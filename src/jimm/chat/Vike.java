//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
package jimm.chat;

import DrawControls.NativeCanvas;
import DrawControls.CanvasEx;
import jimm.comm.Util;
import jimm.comm.StringConvertor;
import jimm.util.Device;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

/**
 * Created [23.02.2011, 12:51:02]
 * Develop by Lavlinsky Roman on 2011
 */
public final class Vike {

    private final String utils[] = Util.explode("MDL <- -> RGT LFT\r123 SHT LNG SPC :) ETR BKSP", '\r');
    private final String russian[] = Util.explode("\u0439 \u0446 \u0443 \u043A \u0435 \u043D \u0433 \u0448 \u0449 \u0437 \u0445\r\u0444 \u044B \u0432 \u0430 \u043F \u0440 \u043E \u043B \u0434 \u0436 \u044D\r\u044F \u0447 \u0441 \u043C \u0438 \u0442 \u044C \u0431 \u044E \u044A .", '\r');
    private final String english[] = Util.explode("q w e r t y u i o p\ra s d f g h j k l /\r? z x c v b n m , .", '\r');
    final String symbols[] = Util.explode("1 2 3 4 5 6 7 8 9 0\r\u0451 - / : ; ( ) $ & @\r[ ] { } # % ^ * + =\r_ \\ | ~ < > . , ? ! ' \"", '\r');
    //private final Font b_javax_microedition_lcdui_Font_fld = Font.getFont(64, 0, c.c());
    //final Font a_javax_microedition_lcdui_Font_fld = Font.getFont(64, 1, 16);
    private final int c_int_fld;
    final VikeLine linesFinal[];
    private final int colors[] = {
            0xffffff, 0, 0x202020, 0xffffff
    };
    VikeLine lines[];
    String currentLang[];
    static String a_java_lang_String_static_fld;
    byte sentens;
    int a_int_fld;
    private Image a_javax_microedition_lcdui_Image_fld;
    private String b_java_lang_String_fld;
    int height;
    public TextFieldEx textField;
    private String c_java_lang_String_fld;
    private String d_java_lang_String_fld;
    VikeItem tempItem;
    public final VikePostItem vikePostItem = new VikePostItem();

    public Vike(TextFieldEx bj1) {
        c_int_fld = CanvasEx.facade.getFontHeight() + 4;
        linesFinal = (new VikeLine[]{
                a2(utils[0]), a2(utils[1])
        });
        sentens = 0;
        //c_java_lang_String_fld = bj1.a().c();
        //d_java_lang_String_fld = bj1.a().d();
        c_java_lang_String_fld = bj1.getChat().getLeftMenu();
        d_java_lang_String_fld = bj1.getChat().getRightMenu();
        textField = bj1;
        a(a_java_lang_String_static_fld != null ? a_java_lang_String_static_fld : "ru");
        if (/*ca.a(131)*/ true) {
            sentens = 2;
            Vike.a("SHT", linesFinal[1], true);
        }
        try {
            Image image = Image.createImage("/vike.png");
            CanvasEx.imageToArray(colors, image);
        } catch (Exception ignored) {
        }
    }

    final void initArray(byte byte0) {
        String as[] = null;
        if (byte0 == 0)
            as = russian;
        else if (byte0 == 1)
            as = english;
        else if (byte0 == 2)
            as = symbols;
        currentLang = as;
        upd();
    }

    final void a(String s) {
        a_java_lang_String_static_fld = s;
        initArray(((byte) (a_java_lang_String_static_fld.equals("ru") ? 0 : 1)));
    }

    final void upd() {
        vikePostItem.vikeItem = null;
        lines = new VikeLine[currentLang.length + 2];
        int i = (55 * NativeCanvas.getHeightEx()) / 100;
        a_int_fld = c_int_fld;
        height = c_int_fld * lines.length;
        if (height < i) {
            a_int_fld = i / lines.length;
            height = a_int_fld * lines.length;
        }
        lines[0] = a(linesFinal[0]);
        for (int j = currentLang.length - 1; j >= 0; j--)
            lines[j + 1] = a2(currentLang[j]);

        lines[lines.length - 1] = a(linesFinal[1]);
        textField.setForcedSize();
        Vike vike = this;
        String s = "/vike_" + NativeCanvas.getWidthEx() + "_";
        s = s + (vike.currentLang != vike.symbols ? a_java_lang_String_static_fld : "123");
        if (!(s = s + ".png").equals(vike.b_java_lang_String_fld)) {
            vike.b_java_lang_String_fld = s;
            try {
                vike.a_javax_microedition_lcdui_Image_fld = Image.createImage(s);
                return;
            }
            catch (java.io.IOException ignored) {
            }
        }
    }

    final String a1(String s) {
        if (sentens == 0)
            return s;
        if (currentLang == russian && s.equals("."))
            return ",";
        else
            return StringConvertor.toUpperCase(s);
    }

    final void a(char c1, boolean flag) {
        if (/*ca.a(131)*/true && (textField.a(c1) || flag) && sentens == 0) {
            Vike.a("SHT", linesFinal[1], true);
            sentens = 2;
        }
    }

    private VikeLine a2(String s) {
        VikeLine bs1 = new VikeLine();
        Util.explode(s, ' ');
        bs1.a_java_lang_String_fld = s;
        return a(bs1);
    }

    private VikeLine a(VikeLine bs1) {
        String as[];
        int i;
        int j = i = (as = Util.explode(bs1.a_java_lang_String_fld, ' ')).length;
        int l = NativeCanvas.getWidthEx();
        bs1.a_java_lang_String_fld.indexOf("SPC");
        boolean flag = bs1.a_vikeItem_array1d_fld == null;
        int i1 = 0;
        if (flag)
            bs1.a_vikeItem_array1d_fld = new VikeItem[i];
        for (int j1 = i - 1; j1 >= 0; j1--) {
            VikeItem br1;
            if (flag)
                (br1 = bs1.a_vikeItem_array1d_fld[j1] = new VikeItem()).a_java_lang_String_fld = as[j1];
            else
                br1 = bs1.a_vikeItem_array1d_fld[j1];
            String s = as[j1];
            String s2 = null;
            if (s.equals("LFT"))
                s2 = c_java_lang_String_fld;
            else if (s.equals("RGT"))
                s2 = d_java_lang_String_fld;
            if (s2 != null) {
                br1.a_int_fld = CanvasEx.facade.stringWidth(s2) + 16;
                l -= br1.a_int_fld;
                j--;
            } else {
                br1.a_int_fld = 0;
            }
        }

        int k = l;
        if (j <= 0)
            k = 0;
        else if (bs1.a_java_lang_String_fld.indexOf("SPC") == -1)
            k /= j;
        else
            k /= j + 1;
        for (int k1 = i - 1; k1 >= 0; k1--) {
            VikeItem br2 = bs1.a_vikeItem_array1d_fld[k1];
            String s1 = as[k1];
            if (br2.a_int_fld == 0)
                br2.a_int_fld = s1.equals("SPC") ? k << 1 : k;
            i1 += br2.a_int_fld;
        }

        int l1 = NativeCanvas.getWidthEx() - i1;
        bs1.a_int_fld = l1 / 2;
        if (l1 % 2 != 0)
            bs1.a_int_fld++;
        return bs1;
    }

    private void a(Graphics g, int i, int j) {
        g.fillTriangle(i, j + 6, i + 4, j + 6 + 4, i - 4, j + 6 + 4);
        g.fillRect(i - 1, j + 6 + 4, 2, a_int_fld - 16);
    }

    private static void drawGlassRect(Graphics g, int color, int x, int y, int w, int h) {
        CanvasEx.drawGlassRect(g, color, x, y, x + w, y + h);
    }

    protected final void paint(Graphics g) {
        int i = NativeCanvas.getWidthEx(); //    h.b(); = w    h.a(); = h
        int j;
        int y = j = NativeCanvas.getHeightEx();
        if (a_javax_microedition_lcdui_Image_fld != null) {
            g.drawImage(a_javax_microedition_lcdui_Image_fld, 0, j, 36);
        }
        for (int k1 = lines.length - 1; k1 >= 0; k1--) {
            VikeLine bs1 = lines[k1];
            y -= a_int_fld;
            int x = i - bs1.a_int_fld;
            for (int l1 = bs1.a_vikeItem_array1d_fld.length - 1; l1 >= 0; l1--) {
                VikeItem br1 = bs1.a_vikeItem_array1d_fld[l1];
                int j1 = (((x -= br1.a_int_fld) << 1) + br1.a_int_fld) / 2;
                int k = ((y << 1) + a_int_fld) / 2;
                if (a_javax_microedition_lcdui_Image_fld == null) {
                    //drawGlassRect(g, br1.activity ? colors[3] : colors[2], x, y, br1.a_int_fld - 1, a_int_fld - 1);
                    CanvasEx.drawGradient(g, x, y, br1.a_int_fld - 1, a_int_fld - 1, br1.activity ? colors[3] : colors[2], 16, 0, -64);
                }
                g.setColor(br1.activity ? colors[1] : colors[0]);
                if (a_javax_microedition_lcdui_Image_fld == null) {
                    g.drawRect(x, y, br1.a_int_fld - 1, a_int_fld - 1);
                }
                String s;
                if ((s = br1.a_java_lang_String_fld).equals("SPC"))
                    continue;
                if (s.equals("SHT")) {
                    if (sentens == 1) {
                        a(g, j1 - 5, y);
                        a(g, j1 + 5, y);
                    } else {
                        a(g, j1, y);
                    }
                    continue;
                }
                if (s.equals("BKSP")) {
                    g.fillTriangle(x + 6, k, x + 6 + 4, k + 4, x + 6 + 4, k - 4);
                    g.fillRect(x + 6 + 4, k - 1, br1.a_int_fld - 16, 3);
                    continue;
                }
                if (s.equals("ETR")) {
                    g.fillTriangle(x + 6, k, x + 6 + 4, k + 4, x + 6 + 4, k - 4);
                    g.fillRect(x + 6 + 4, k - 1, br1.a_int_fld - 16, 3);
                    g.fillRect((x + 6 + 4 + br1.a_int_fld) - 16 - 3, k - 6, 3, 5);
                    continue;
                }
                if (s.equals("<-")) {
                    g.fillTriangle(j1 - 2, k, (j1 - 2) + 4, k + 4, (j1 - 2) + 4, k - 4);
                    continue;
                }
                if (s.equals("->")) {
                    g.fillTriangle(j1 + 2, k, (j1 + 2) - 4, k + 4, (j1 + 2) - 4, k - 4);
                    continue;
                }
                if (s.equals("MDL")) {
                    g.drawLine((j1 - 4) + 1, k - 4 - 1, j1 + 4 + 1, (k + 4) - 1);
                    g.drawLine((j1 - 4) + 1, k - 4, j1 + 4, (k + 4) - 1);
                    g.drawLine(j1 - 4, k - 4, j1 + 4, k + 4);
                    g.drawLine(j1 - 4 - 1, (k - 4) + 1, (j1 + 4) - 1, k + 4 + 1);
                    g.drawLine(j1 - 4, (k - 4) + 1, (j1 + 4) - 1, k + 4);
                    g.drawLine(j1 + 4 + 1, (k - 4) + 1, (j1 - 4) + 1, k + 4 + 1);
                    g.drawLine(j1 + 4, (k - 4) + 1, (j1 - 4) + 1, k + 4);
                    g.drawLine(j1 + 4, k - 4, j1 - 4, k + 4);
                    g.drawLine((j1 + 4) - 1, k - 4 - 1, j1 - 4 - 1, (k + 4) - 1);
                    g.drawLine((j1 + 4) - 1, k - 4, j1 - 4, (k + 4) - 1);
                    continue;
                }
                if (s.equals("LNG"))
                    s = a_java_lang_String_static_fld;
                else if (s.equals("RGT"))
                    s = d_java_lang_String_fld;
                else if (s.equals("LFT"))
                    s = c_java_lang_String_fld;
                if (bs1 != linesFinal[0])
                    s = a1(s);
                CanvasEx.drawString(g, CanvasEx.facade, s, j1, (((y << 1) + a_int_fld) - CanvasEx.facade.getFontHeight()) / 2, Graphics.TOP | Graphics.HCENTER); 
            }

        }

        if (vikePostItem.vikeItem != null) {
            drawGlassRect(g, vikePostItem.vikeItem.activity ? colors[3] : colors[2], vikePostItem.a_int_fld, vikePostItem.b, vikePostItem.c, vikePostItem.d);
            g.setColor(vikePostItem.vikeItem.activity ? colors[1] : colors[0]);
            g.drawRect(vikePostItem.a_int_fld, vikePostItem.b, vikePostItem.c - 1, vikePostItem.d - 1);
            CanvasEx.drawString(g, CanvasEx.facade, a1(vikePostItem.vikeItem.a_java_lang_String_fld), (2 * vikePostItem.a_int_fld + vikePostItem.c) / 2, ((2 * vikePostItem.b + vikePostItem.d) - CanvasEx.facade.getFontHeight()) / 2, 17);
        }
    }

    static boolean act(int i, int j, int k, int l, int i1, int j1) {
        i -= k;
        j -= i1;
        return i >= 0 && j >= 0 && i <= l && j <= j1;
    }

    static void a(String s, VikeLine bs1, boolean flag) {
        for (int i = bs1.a_vikeItem_array1d_fld.length - 1; i >= 0; i--)
            if (bs1.a_vikeItem_array1d_fld[i].a_java_lang_String_fld.equals(s)) {
                bs1.a_vikeItem_array1d_fld[i].activity = flag;
                return;
            }

    }
}
//#sijapp cond.end#