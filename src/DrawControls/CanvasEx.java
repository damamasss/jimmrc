/*******************************************************************************
 ********************************************************************************
 ********************************************************************************
 File: src/DrawControls/VirtualList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Shamsutdinov Rishat
 ********************************************************************************
 ********************************************************************************
 *******************************************************************************/
package DrawControls;

import jimm.Jimm;
import jimm.Options;
import jimm.ui.SliderTask;
import jimm.ui.Toolbar;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import java.io.InputStream;

public abstract class CanvasEx {
    // Key event type
    public final static int KEY_PRESSED = 1;
    public final static int KEY_REPEATED = 2;
    public final static int KEY_RELEASED = 3;
    public static final int KEY_CODE_LEFT_MENU = 0x00100000;
    public static final int KEY_CODE_RIGHT_MENU = 0x00100001;
    public static final int KEY_CODE_BACK_BUTTON = 0x00100003;

    public static final int MENU_TYPE_LEFT_BAR = 1;
    public static final int MENU_TYPE_RIGHT_BAR = 2;

    protected static int[] alphaBuffer = new int[0];
    protected static int[] alphaBufferR = new int[0];
    //protected static int[] abl = new int[0];     //luster
    protected static Image bDImage = null;
    protected static int scrollerWidth;
    protected static int scrollerY1 = -1;
    protected static int scrollerY2 = -1;

    //#sijapp cond.if modules_TOOLBAR is "true"#
    protected Toolbar toolbar;
    //#sijapp cond.end#
    private boolean dontRepaint;
    protected boolean animated;
    private int priority;
    protected int textOff;
    //public int anime;
    protected long lastPointerTime;
    protected int lastPointerYCrd;
    protected int lastPointerXCrd;
    protected int lastPointerTopItem;
    protected boolean isDraggedWas;
    public boolean isPressed;
    public static FontFacade facade = new FontFacade(getSuperFont());
    public Command leftMenu;
    public Command rightMenu;

    protected CanvasEx() {
        textOff = 0;
        dontRepaint = false;
        isDraggedWas = false;
        isPressed = false;
        priority = -1;
        //anime = 100;
        lastPointerTime = 0;
        lastPointerYCrd = -1;
        lastPointerXCrd = -1;
        lastPointerTopItem = -1;
        if (scrollerWidth == 0) {
            scrollerWidth = Math.max((NativeCanvas.getWidthEx() << 1) / 100, 3);
        }
        //toolbar = null;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void beforeShow() {
    }

    protected void afterShow() {
    }

    public void beforeHide() {
        animated = false;
    }

    protected void sizeChanged(int w, int h) {
    }

    public boolean slide(CanvasEx bgd) {
        //if (bgd instanceof Menu || bgd instanceof PopUp || bgd instanceof SliderTask) {
        //	return;
        //}
        if (!animated) {
            int w = NativeCanvas.getWidthEx();
            //int h = NativeCanvas.getHeightEx();
            int i = 1;
            if (bgd.getPriority() < getPriority()) {
                i = -1;
            }
            Jimm.setDisplay(new SliderTask(this, null, this, -i * w, i * w));
            animated = true;
            return false;
        }
        return true;
    }

    public Image paintOnImage() {
        Image img = Image.createImage(NativeCanvas.getWidthEx(), NativeCanvas.getHeightEx());
        Graphics g = img.getGraphics();
        paint(g);
        return img;
    }

    public int getDrawWidth() {
        return NativeCanvas.getWidthEx();
    }

    public int getMenuBarHeight() {
        return 0;
    }

    public abstract void paint(Graphics g);

    //#sijapp cond.if target is "MIDP2"#
    public void pointerReleased(int x, int y) {
    }

    public void pointerPressed(int x, int y) {
    }

    public void pointerDragged(int x, int y) {
    }
//#sijapp cond.end#

    public abstract void doKeyreaction(int keyCode, int type);

    // Return game action or extended codes. Thanks for Aspro for source examples
    public int getExtendedGameAction(int keyCode) {
        return getExtendedGameAction(keyCode, true);
    }

    public int getExtendedGameAction(int code, boolean catchSoft) {
        return NativeCanvas.getInst().getExtendedGameAction(code, catchSoft);
    }

    public void setFullScreenMode(boolean full) {
        NativeCanvas.getInst().setFullScreen(full);
    }

    public void invalidate() {
        if (dontRepaint) {
            return;
        }
        NativeCanvas.invalidate(this);
    }

    public void invalidate(int x, int y, int w, int h) {
        if (dontRepaint) {
            return;
        }
        NativeCanvas.invalidate(this, x, y, w, h);
    }

    protected Graphics getGraphics() {
        if (bDImage == null) {
            bDImage = Image.createImage(1, 1);
        }
        return bDImage.getGraphics();
    }

    public String getCurrentString() {
        return null;
    }

//    public static void drawString(Graphics g, String str, int x, int y, int anchor) {
//        g.drawString(str, x, y, anchor);
//    }

    //#sijapp cond.if modules_GFONT="true" #
    private static BitmapFont bitmapFont = null;
    private static boolean bit = false;

    public static boolean updateFont(String file) {
        try {
            InputStream input = (new Object()).getClass().getResourceAsStream(file);
            if (input != null) {
                bitmapFont = new BitmapFont(file);
                input.close();
//                System.out.println("font ok " + file);
                return bit = true;
            }
        } catch (Exception ignored) {
        }
//        System.out.println("font no " + file);
        return bit = false;
    }

    public static void updateFont() {
        facade = new FontFacade(getSuperFont());
        //VirtualList.capFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
    }
//    static {
//        try {
//            InputStream input = new Object().getClass().getResourceAsStream("/gfont.fnt");
//            if (input != null) {
//                bitmapFont = new BitmapFont("/gfont.fnt");
//                bit = true;
//                input.close();
//            }
//        } catch (Exception e) {
//        }
//    }
//#sijapp cond.end#

    public static Object getSuperFont(int face, int style, int size) {
//#sijapp cond.if modules_GFONT="true" #
        if (bit) {
            if (style != Font.STYLE_PLAIN) {
                return bitmapFont.getFont(style);
            }
            return bitmapFont;
        }
//#sijapp cond.end#
        return Font.getFont(face, style, size);
    }

    public static Object getSuperFont() {
        return getSuperFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    }

//    public static FontFacade getFacade() {
//        return facade;
//    }

    public static void drawString(Graphics g, FontFacade font, String str, int x, int y, int anchor) {
//#sijapp cond.if modules_GFONT="true" #
//        if (!bit)
//#sijapp cond.end#
//        {
//            if (Options.getInt(Options.OPTION_FONT_STYLE) == 1) {
//                int color = g.getColor();
//                g.setColor(0x888888);
//                font.drawString(g, str, x + 1, y, anchor);
//                font.drawString(g, str, x, y + 1, anchor);
//                font.drawString(g, str, x + 1, y + 1, anchor);
//                g.setColor(color);
//            }
//        }
        font.drawString(g, str, x, y, anchor);
        //for (char c = '!'; c <= '\u044f'; c++) {
        //    Image im = Image.createImage(20, 20);
        //    Graphics g = im.getGraphics();
        //    g.drawChar(c, 0, 0, Graphics.TOP | Graphics.LEFT);
        //}
    }

    // todo arrayIn after getXtraz
    public static void drawAvernageAlpha(Graphics g, int x1, int y1, int x2, int y2, int alpha1, int alpha2, int color) {  // todo nullpointer
        int h = Math.max((y2 - y1), 2);
        int w = x2 - x1;
        int step = (alpha2 - alpha1) / h;
        synchronized (alphaBuffer) {
            alphaBuffer = new int[w * h];
            int lt, t, i, ln;
            for (int j = h - 1; j >= 0; j--) {
                //t = ((h - j) * 128 / h << 24) | color;
                lt = (h - j) * step;
                if (lt < 0) {
                    lt = alpha1 + lt;
                }
                t = lt << 24 | color;
                ln = j * w;
                try {
                    for (i = w - 1; i >= 0; i--) {
                        alphaBuffer[ln + i] = t;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //System.out.println("alphaBuffer LEN = " + alphaBuffer.length + " WXH = " + w * h + " LN = " + ln + " LN full = " + (ln + w - 1));
                    alphaBuffer = new int[w * h];
                    //e.printStackTrace();
                }
            }
            g.drawRGB(alphaBuffer, 0, w, x1, y1, w, h, true);
            alphaBuffer = new int[0];
        }
    }


    public static void drawGlassRect(Graphics gr, int color, int x1, int y1, int x2, int y2) {
        drawRect(gr, color, transformColorLight(color, -64), x1, y1, x2 - x1, y2 - y1);
        drawGlassAlpha(gr, x1, y1, x2, y2);
    }

    protected static void drawGlassAlpha(Graphics gr, int x1, int y1, int x2, int y2) {
        if (Options.getInt(Options.OPTION_LUSTER) < 1) {
            return;
        }
        int h = Math.max((y2 - y1) / 2, 2);
        int w = x2 - x1;
        synchronized (alphaBuffer) {
            alphaBuffer = new int[w * h];
            int t, i, ln;
            for (int j = h - 1; j >= 0; j--) {
//              t = ((h - j) * 128 / h << 24) | 0xFFFFFF;
//				int t = ((j * (t2 - t1) / (h - 1) + t1) << 24) | 0xFFFFFF;
                t = ((85 - (j * 50) / (h - 1)) << 24) | 0xFFFFFF;
                ln = j * w;
                for (i = w - 1; i >= 0; i--) {
                    alphaBuffer[ln + i] = t;
                }
            }
            gr.drawRGB(alphaBuffer, 0, w, x1, y1, w, h, true);
            alphaBuffer = new int[0];
        }
    }

    protected static void drawRect(Graphics g, int color1, int color2, int x1, int y1, int w, int h) {
        if (color1 == color2) {
            g.setColor(color1);
            g.fillRect(x1, y1, w, h);
            return;
        }
        int r1 = ((color1 & 0xFF0000) >> 16), g1 = ((color1 & 0x00FF00) >> 8), b1 = (color1 & 0x0000FF);
        int r2 = ((color2 & 0xFF0000) >> 16), g2 = ((color2 & 0x00FF00) >> 8), b2 = (color2 & 0x0000FF);
        int count = Math.max(Math.abs(h / 3), 8);
        int crd1, crd2;
        for (int i = count - 1; i >= 0; i--) {
            crd1 = i * h / count + y1;
            crd2 = (i + 1) * h / count + y1;
            if (crd1 == crd2) {
                continue;
            }
            g.setColor(i * (r2 - r1) / (count - 1) + r1, i * (g2 - g1) / (count - 1) + g1, i * (b2 - b1) / (count - 1) + b1);
            g.fillRect(x1, crd1, w, crd2 - crd1);
        }
    }

//    public final void Z(Graphics g, int i1, int j1, int k1, int l1)
//    {
//        int i2 = g.getClipX();
//        int j2 = g.getClipY();
//        int k2 = g.getClipWidth();
//        int l2 = g.getClipHeight();
//        if(Code(i1, j1, k1, l1, i2, j2, k2, l2)){
//            g.setClip(Math.max(i1, i2),
//                    Math.max(j1, j2),
//                    i1 >= i2 ? i2 + k2 <= i1 + k1 ? (i2 + k2) - i1 : k1 : i1 + k1 <= i2 + k2 ? (i1 + k1) - i2 : k2,
//                    j1 >= j2 ? j2 + l2 <= j1 + l1 ? (j2 + l2) - j1 : l1 : j1 + l1 <= j2 + l2 ? (j1 + l1) - j2 : l2);
//        } else {
//            g.setClip(0, 0, 0, 0);
//        }
//    }
//
//    public static boolean Code(int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2)
//    {
//        return i1 < i2 + k2 && i1 + k1 > i2 && j1 < j2 + l2 && j1 + l1 > j2;
//    }

    protected static void drawAlphaRect(Graphics g, int _color, int x1, int y1, int x2, int y2, int alpha) {
        if (alpha == 0) {
            return;
        }
        int w = x2 - x1 + 1;
        int h = y2 - y1 + 1;
        if (alpha == 255) {
            g.setColor(_color);
            g.fillRect(x1, y1 + 1, w, h - 2);
            g.fillRect(x1 + 1, y1, Math.max(1, w - 2), h);
            return;
        }
        //int wx = Math.min(32, w);
        //int tw = w;
        int count = w * h;
        synchronized (alphaBuffer) {
            alphaBuffer = new int[w * h];
            int alphaLevel = alpha << 24;
            int color = alphaLevel | _color;
            int[] hide = {0, w - 1, count - 1, count - w};
            boolean f;
            for (int i = count - 1; i >= 0; i--) {
                f = false;
                for (int j = 0; j < hide.length; j++) {
                    f |= (i == hide[j]);
                }
                alphaBuffer[i] = (f) ? 1 << 24 | _color : color;
            }
            //for (int x = x1; x < x2; x += 32){
            //g.drawRGB(alphaBuffer, 0, w, x1, y1, w, h, true);
            //tw -= 32;
            //}
            g.drawRGB(alphaBuffer, 0, w, x1, y1, w, h, true);
            alphaBuffer = new int[0];
        }
    }

//    public void drawLines(Graphics g, int color, int x1, int y1, int x2, int y2) {
//        int width = x2 - x1;
//        int height = y2 - y1;
//        synchronized (alphaBuffer) {
//            alphaBuffer = new int[width * 4];
//            int sign = ((color & 0x808080) == 0x808080 )? -40 : 40;
//            for (int k = 0; k < width; k++) {
//                int sign2 = k < width / 2 ? sign : sign * (width - k) / (width / 2) + 1;
//                int color1 = VirtualList.transformColorLight(color, sign2 / 2);
//                int color2 = VirtualList.transformColorLight(color, sign2);
//                int color3 = VirtualList.transformColorLight(color, sign2 / 8);
//                int j = k;
//                alphaBuffer[j] = color;
//                alphaBuffer[j += width] = color1;
//                alphaBuffer[j += width] = color2;
//                alphaBuffer[j += width] = color3;
//            }
//            int totalheight = height;
//            for (int j = y1; totalheight > 0; j += 4) {
//                g.drawRGB(alphaBuffer, 0, width, 0, j, width, (totalheight > 4) ? 4 : totalheight, false);
//                totalheight -= 4;
//            }
//            alphaBuffer = new int[0];
//        }
//    }

    protected final static int CONTRAST = 48;//96

    protected static void drawTimeAlpha(Graphics gr, int w, int h, int y1, int c, int a) {
        if (a == 0) {
            return;
        }
        int len;
        if (w * h + 1 == (len = alphaBufferR.length)) {
            int o = alphaBufferR[len - 1];
            if ((a << 24 | c) == o) {
                gr.drawRGB(alphaBufferR, 0, w, 0, y1, w, h, a < 255);
                if (a == 255) drawGlassAlpha(gr, 0, y1, w, y1 + h);
                return;
            }
        }
        //System.out.println("n");

        int c1 = transformColorLight(c, -32), c2 = transformColorLight(c, -102);
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int w2 = w / 2, w3 = w / 3, idx = 0;
        int color, diff;
        int r, g, b;
        synchronized (alphaBufferR) {
            alphaBufferR = new int[h * w + 1];
            for (int y = 0; y < h; y++) {
                r = y * (r2 - r1) / (h - 1) + r1;
                g = y * (g2 - g1) / (h - 1) + g1;
                b = y * (b2 - b1) / (h - 1) + b1;
                for (int x = 0; x < w; x++) {
                    diff = CONTRAST * Math.max(-Math.abs(x - w2) + w3, 0) / w3;
                    color = 0x00000000;
                    color |= a << 24;
                    color |= Math.max(Math.min(r + diff, 255), 0) << 16;
                    color |= Math.max(Math.min(g + diff, 255), 0) << 8;
                    color |= Math.max(Math.min(b + diff, 255), 0);
                    alphaBufferR[idx++] = color;
                }
            }
            alphaBufferR[idx++] = a << 24 | c;
            gr.drawRGB(alphaBufferR, 0, w, 0, y1, w, h, a != 255);
        }
        if (a == 255) {
            drawGlassAlpha(gr, 0, y1, w, y1 + h);
        }
    }


    public static void drawGradient(Graphics g, int x, int y, int w, int h, int color, int count, int light1, int light2) {
        int y1, y2;
//		int _h = h/count;
        for (int i = count - 1; i >= 0; i--) {
//			g.setColor((i * (c2 - c1) / (count - 1) + c1));
            g.setColor(transformColorLight(color, (light2 - light1) * i / (count - 1) + light1));
            y1 = y + (i * h) / count;
            y2 = y + (i * h + h) / count;
            g.fillRect(x, y1, w, y2 - y1);
        }
    }

    protected static void drawAlphaGradient(Graphics g, int x, int y, int w, int h, int color, int light1, int light2, int trans) {
        if (trans == 0) return;
        synchronized (alphaBuffer) {
            alphaBuffer = new int[w * h];
            int alphaLevel = trans << 24;
            int alphaColor, j;
            try {
                for (int i = h - 1; i >= 0; i--) {
                    alphaColor = transformColorLight(color, (light2 - light1) * i / (h - 1) + light1) | alphaLevel;
                    for (j = w - 1; j >= 0; j--) {
                        alphaBuffer[i * w + j] = alphaColor;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            g.drawRGB(alphaBuffer, 0, w, x, y, w, h, true);
            alphaBuffer = new int[0];
        }
    }

    public static void drawScroller(Graphics g, int topItem, int topY, int visCount, int x, int height, int itemCount, boolean flag) {
        boolean haveToShowScroller = ((itemCount > visCount) && (itemCount > 0));
        if (flag) {
            if (!haveToShowScroller) {
                return;
            }
            height += topY;
        }
        int color = transformColorLight(transformColorLight(getColor(COLOR_BACK), 32), -32);
        if (color == 0) {
            color = 0x808080;
        }
        g.setStrokeStyle(Graphics.SOLID);
        g.setColor(color);

        g.fillRect(x + 1, topY, scrollerWidth - 1, height - topY);
        g.setColor(transformColorLight(color, -64));
        g.drawLine(x, topY, x, height);
        if (haveToShowScroller) {
            int sliderSize = (height - topY) * visCount / itemCount;
            int colorScroller = getColor(COLOR_CAP);
            if (sliderSize < 7) {
                sliderSize = 7;
            }
            scrollerY1 = topItem * (height - sliderSize - topY) / (itemCount - visCount) + topY;
            scrollerY2 = scrollerY1 + sliderSize;
            g.setColor(colorScroller);
            g.fillRect(x + 2, scrollerY1 + 2, scrollerWidth - 3, scrollerY2 - scrollerY1 - 3);
            g.setColor(transformColorLight(colorScroller, -192));
            g.drawRect(x, scrollerY1, scrollerWidth - 1, scrollerY2 - scrollerY1 - 1);
            g.setColor(transformColorLight(colorScroller, 96));
            g.drawLine(x + 1, scrollerY1 + 1, x + 1, scrollerY2 - 2);
            g.drawLine(x + 1, scrollerY1 + 1, x + scrollerWidth - 2, scrollerY1 + 1);
        }
    }

    // change light of color
    static protected int transformColorLight(int color, int light) {
        if (light == 0) {
            return color;
        }
        int r = (color & 0xFF) + light;
        int g = ((color >> 8) & 0xFF) + light;
        int b = ((color >> 16) & 0xFF) + light;
        if (r < 0) {
            r = 0;
        } else if (r > 255) {
            r = 255;
        }
        if (g < 0) {
            g = 0;
        } else if (g > 255) {
            g = 255;
        }
        if (b < 0) {
            b = 0;
        } else if (b > 255) {
            b = 255;
        }
        return r | (g << 8) | (b << 16);
    }

    public void lock() {
        dontRepaint = true;
    }

    protected void afterUnlock() {
    }

    public void unlock() {
        dontRepaint = false;
        afterUnlock();
        invalidate();
    }

    protected boolean getLocked() {
        return dontRepaint;
    }

//    public static Image resizeImageProportional(Image sourceImage, int prcSize) {
//        if (prcSize >= 100) {
//            return sourceImage;
//        } else if (prcSize <= 0) {
//            return Image.createImage(1, 1);
//        }
//        int imageWidth = sourceImage.getWidth();
//        int imageHeight = sourceImage.getHeight();
//        int destWidth = imageWidth * prcSize / 100;
//        int destHeight = imageHeight * prcSize / 100;
//        int[] rgbData = new int[imageWidth * imageHeight];
//        int[] rgbDest = new int[destWidth * destHeight];
//        sourceImage.getRGB(rgbData, 0, imageWidth, 0, 0, imageWidth, imageHeight);
//        int currY;
//        int xx;
//        int yy = 0;
//        for (int y = 0; y < destHeight; y++) {
//            currY = y * destWidth;
//            xx = 0;
//            for (int x = 0; x < destWidth; x++) {
//                try {
//                    rgbDest[currY + x] = rgbData[(yy * 100 + xx) / 100];//(y * imageWidth + x) * 100 / prcSize
//                } catch (Throwable ex) {
//                    // System.out.println(xx + ", " + yy);
//                }
//                xx += 10000 / prcSize;
//            }
//            yy = (y * imageHeight / destHeight) * imageWidth;
//        }
//        return Image.createRGBImage(rgbDest, destWidth, destHeight, false);
//    }

    /*public static Image opaqueImage(Image sourceImage, Image __destImage, int prcSrcToDest) {
        if (prcSrcToDest > 100) {
            prcSrcToDest = 100;
        } else if (prcSrcToDest < 0) {
            prcSrcToDest = 0;
        }
        int imageWidth = sourceImage.getWidth();
        int imageHeight = sourceImage.getHeight();
        int[] rgbData = new int[imageWidth * imageHeight];
        sourceImage.getRGB(rgbData, 0, imageWidth, 0, 0, imageWidth, imageHeight);
        int[] rgbDest = new int[imageWidth * imageHeight];
        __destImage.getRGB(rgbDest, 0, imageWidth, 0, 0, imageWidth, imageHeight);
        int currY;
        int r;
        int g;
        int init;
        int currPoint;
        int destPoint;
        int prcDestToSrc = 100 - prcSrcToDest;
        for (int y = 0; y < imageHeight; y++) {
            currY = y * imageWidth;
            for (int x = 0; x < imageWidth; x++) {
                currPoint = rgbData[currY + x];
                destPoint = rgbDest[currY + x];

                init = ((currPoint & 0xff0000) * prcSrcToDest + (destPoint & 0xff0000) * prcDestToSrc) / 100 & 0xff0000;
                g = ((currPoint & 0xff00) * prcSrcToDest + (destPoint & 0xff00) * prcDestToSrc) / 100 & 0xff00;
                r = ((currPoint & 0xff) * prcSrcToDest + (destPoint & 0xff) * prcDestToSrc) / 100 & 0xff;

                rgbData[currY + x] = r | g | init;
            }
        }
        return Image.createRGBImage(rgbData, imageWidth, imageHeight, false);
    }*/

//    public static Image setAlphaToImage(Image sourceImage, int prcAlpha) {
//        int imageWidth = sourceImage.getWidth();
//        int imageHeight = sourceImage.getHeight();
//        int[] rgbData = new int[imageWidth * imageHeight];
//        sourceImage.getRGB(rgbData, 0, imageWidth, 0, 0, imageWidth, imageHeight);
//        int a;
//        int r;
//        int g;
//        int init;
//        int currPoint;
//        for (int y = 0; y < imageHeight * imageWidth; y++) {
//            currPoint = rgbData[y];
//            a = currPoint & 0xff000000;
//            r = currPoint & 0xff;
//            g = currPoint & 0xff00;
//            init = currPoint & 0xff0000;
//            if (a == 0xff000000) {
//                a = 256;
//            } else {
//                a >>= 24;
//            }
//            a -= (255 * prcAlpha) / 100;
//            if (a >= 256) {
//                a = 0xff000000;
//            } else {
//                a <<= 24;
//            }
//            rgbData[y] = a | r | g | init;
//        }
//        return Image.createRGBImage(rgbData, imageWidth, imageHeight, true);
//    }

//    public static Image smoothImage(Image sourceImage, int imageX, int imageY, int imageWidth, int imageHeight) {
//        imageWidth -= imageX;
//        imageHeight -= imageY;
//        int[] rgbData = new int[imageWidth * imageHeight];
//        sourceImage.getRGB(rgbData, 0, imageWidth, imageX, imageY, imageWidth, imageHeight);
//        int currY;
//        int prevY;
//        int nextY;
//        int r;
//        int g;
//        int init;
//        int currPoint;
//        int prevYPoint;
//        int prevXPoint;
//        int nextYPoint;
//        int nextXPoint;
//        for (int y = 1; y < imageHeight; y++) {
//            prevY = (y - 1) * imageWidth;
//            currY = y * imageWidth;
//            if (y + 1 < imageHeight) {
//                nextY = (y + 1) * imageWidth;
//            } else {
//                nextY = currY;
//            }
//            for (int x = 1; x < imageWidth - 1; x++) {
//                currPoint = rgbData[currY + x];
//                prevYPoint = rgbData[prevY + x];
//                prevXPoint = rgbData[currY + x - 1];
//                nextYPoint = rgbData[nextY + x];
//                nextXPoint = rgbData[currY + x + 1];
//                init = ((currPoint & 0xff0000) + (prevYPoint & 0xff0000) + (prevXPoint & 0xff0000) + (nextYPoint & 0xff0000) + (nextXPoint & 0xff0000)) / 5 & 0xff0000;
//                g = ((currPoint & 0xff00) + (prevYPoint & 0xff00) + (prevXPoint & 0xff00) + (nextYPoint & 0xff00) + (nextXPoint & 0xff00)) / 5 & 0xff00;
//                r = ((currPoint & 0xff) + (prevYPoint & 0xff) + (prevXPoint & 0xff) + (nextYPoint & 0xff) + (nextXPoint & 0xff)) / 5 & 0xff;
//                rgbData[currY + x] = r | g | init;
//            }
//        }
//        return Image.createRGBImage(rgbData, imageWidth, imageHeight, false);
//    }

    //public void addCommand(Command cmd) {
    //	NativeCanvas.getInst().removeCommand(cmd);
    //	NativeCanvas.cancelCmd = cmd;
    //	NativeCanvas.getInst().addCommand(cmd);
    //}

    //public void removeCommand(Command cmd) {
    //	NativeCanvas.cancelCmd = null;
    //	NativeCanvas.getInst().removeCommand(cmd);
    //}

    protected static boolean ptInRect(int ptX, int ptY, int x1, int y1, int x2, int y2) {
        return (x1 <= ptX) && (ptX < x2) && (y1 <= ptY) && (ptY < y2);
    }

    public final void setTextOff(int ints) {
        if (ints < 0) {
            if (textOff < 0) {
                textOff = 0;
                invalidate();
            }
        } else {
            textOff -= ints;
        }
    }

    //public void setCommandListener(CommandListener c) {
    //	NativeCanvas.getInst().setCommandListener(c);
    //}


    static public final int COLOR_CAP = 0;
    static public final int COLOR_TEXT = 1;
    static public final int COLOR_BACK = 2;
    static public final int COLOR_CURSOR = 3;
    static public final int COLOR_MBACK1 = 4;
    static public final int COLOR_MCURSOR = 5;
    static public final int COLOR_SPLASH = 6;
    static public final int COLOR_CHAT = 7;
    static public final int COLOR_MESS = 8;
    static public final int COLOR_YMESS = 9;
    static public final int COLOR_NICK = 10;
    static public final int COLOR_TEMP = 11;
    static public final int COLOR_DCURSOR = 12;
    static public final int COLOR_MDCURSOR = 13;
    static public final int COLOR_CAP_TEXT = 14;
    static public final int COLOR_BAR_TEXT = 15;
    static public final int COLOR_SPL_PRGS = 16;
    static public final int COLOR_YOUR_NICK = 17;
    static public final int COLOR_BLINK = 18;
    static public final int COLOR_SPL_TEXT = 19;
    static public final int COLOR_CAP2 = 20;
    static public final int COLOR_HIST_MESS = 21;
    static public final int COLOR_CC_TEXT = 22;
    static public final int COLOR_CAT_TEXT = 23;
    static public final int COLOR_FANTOM = 24;
    static public final int COLOR_MBACK2 = 25;
    static public final int COLOR_MENU_TEXT = 26;
    static public final int COLORS = 27;

    private static int[] colorScheme =
            {
                    0x990000,// COLOR_CAP = 0;
                    0x000000,// COLOR_TEXT = 1;
                    0xd0d0d0,// COLOR_BACK = 2;
                    0xb0d8f8,// COLOR_CURSOR = 3;
                    0xcccccc,// COLOR_MBACK1 = 4;
                    0xb0d8f8,// COLOR_MCURSOR = 5;
                    0x000000,// COLOR_SPLASH = 6;
                    0x0000ff,// COLOR_CHAT = 7;
                    0x000000,// COLOR_MESS = 8;
                    0x000000,// COLOR_YMESS = 9;
                    0xff0000,// COLOR_NICK = 10;
                    0x909090,// COLOR_TEMP = 11;
                    0x90b0d0,// COLOR_DCURSOR = 12;
                    0x90b0d0,// COLOR_MDCURSOR = 13;
                    0xffffff,// COLOR_CAP_TEXT = 14;
                    0xffffff,// COLOR_BAR_TEXT = 15;
                    0xff0000,// COLOR_SPL_PRGS = 16;
                    0x0000ff,// COLOR_YOUR_NICK = 17;
                    0xff0000,// COLOR_BLINK = 18;
                    0xffffff,// COLOR_SPL_TEXT = 19;
                    0x555555,// COLOR_CAP2 = 20; //M NICK
                    0x909090,// COLOR_HIST_MESS = 21;
                    0x000000,// COLOR_CC_TEXT = 22;
                    0x0000ff,// COLOR_CAT_TEXT = 23;
                    0xff0000,// COLOR_FANTOM = 24;
                    0xd0d0d0,// COLOR_MBACK2 = 25;
                    0x000000 // COLOR_MENU_TEXT = 26;
            };

    public static void imageToArray(int ai[], Image image) {
        image.getRGB(ai, 0, ai.length, 0, 0, ai.length, 1);
        for (int i = ai.length - 1; i >= 0; i--) {
            ai[i] &= 0xffffff;
        }
    }

    public static void loadCS(Image skin) {
        Image scheme = null;
        //int[] colors = new int[COLORS];

        if ((!Options.getBoolean(Options.OPTION_COLORS_FROM_SKIN)) || (skin == null)) {
            if (!Options.loadColorScheme(colorScheme, skin != null)) {
                try {
                    scheme = Image.createImage("/defcs.png");
                } catch (Exception ignored) {
                }
                if (scheme != null) {
                    imageToArray(colorScheme, scheme);
//                    scheme.getRGB(colors, 0, COLORS, 0, 0, COLORS, 1);
//                    for (int i = 0; i < COLORS; i++) {
//                        colorScheme[i] = colors[i] & 0xFFFFFF;
//                    }
                    Options.saveColorScheme(skin != null);
                }
            }
            return;
        }
        try {
            scheme = Image.createImage(skin, 0, skin.getHeight() - 1, COLORS, 1, Sprite.TRANS_NONE);
        } catch (Exception ignored) {
        }
        if (scheme == null) {
            try {
                scheme = Image.createImage("/defcs.png");
            } catch (Exception e) {
                return;
            }
        }
        try {
            imageToArray(colorScheme, scheme);
            //scheme.getRGB(colors, 0, COLORS, 0, 0, COLORS, 1);
        } catch (Exception e) {
            try {
                scheme = Image.createImage("/defcs.png");
            } catch (Exception ignored) {
            }
            imageToArray(colorScheme, scheme);
            //scheme.getRGB(colors, 0, COLORS, 0, 0, COLORS, 1);
        }
//        for (int i = 0; i < COLORS; i++) {
//            colorScheme[i] = colors[i] & 0xFFFFFF;
//        }
    }

    public static int getColor(int color) {
        return colorScheme[color];
    }

    public static int[] getColors() {
        return colorScheme;
    }

    public static void setColors(int[] colors) {
        if (colors != null) {
            colorScheme = colors;
        }
    }

    public static byte[] getColorsByte() {
        byte[] out = new byte[COLORS * 3];
        int color;
        for (int i = 0; i < COLORS; i++) {
            color = colorScheme[i];
            for (int c = 0; c < 3; c++)
                out[i * 3 + c] = (byte) ((color >> ((2-c) * 8)) & 0xFF);
        }
        return out;
    }

//            imgHeight = imgHeight * sc / 100;
//            imgWidth = imgWidth * sc / 100;
//            resImage = resizeImage(resImage, imgWidth, imgHeight, true, -1, -1);

    public static Image resizeImage(Image img, int newWidth, int newHeight, boolean useAlpha, int blockWidth, int blockHeight) {
        int width = img.getWidth();
        int width1 = width - 1;
        int height = img.getHeight();
        int height1 = height - 1;
        int height2 = height1 * width;
        int newHeight2 = newHeight * newWidth;
        int bxCnt = 0;
        int blockWidth1 = blockWidth - 1;

        int[] oldImage = new int[width * height];

        img.getRGB(oldImage, 0, width, 0, 0, width, height);

        int[] newImage = new int[newWidth * newHeight];

        int r00 = 0, g00 = 0, b00 = 0, a00 = 0, r01 = 0, g01 = 0, b01 = 0, a01 = 0, r10 = 0;
        int g10 = 0, b10 = 0, a10 = 0, r11 = 0, g11 = 0, b11 = 0, a11 = 0;
        int sxPrev = -1, syPrev = -1;
        int sx = 0;
        int xcnt = (width - newWidth) / 2;
        for (int dx = 0; dx < newWidth; dx++) {
            int sy = 0;
            int ycnt = (height - newHeight) / 2;
            for (int dy = 0; dy < newHeight2; dy += newWidth) {
                if ((sxPrev != sx) || (syPrev != sy)) {
                    int rgb = oldImage[sx + sy];
                    r00 = rgb & 0xFF;
                    g00 = (rgb >> 8) & 0xFF;
                    b00 = (rgb >> 16) & 0xFF;
                    a00 = (rgb >> 24) & 0xFF;
                    if (sy < height2) {
                        rgb = oldImage[sx + sy + width];
                        r01 = rgb & 0xFF;
                        g01 = (rgb >> 8) & 0xFF;
                        b01 = (rgb >> 16) & 0xFF;
                        a01 = (rgb >> 24) & 0xFF;
                    } else {
                        r01 = r00;
                        g01 = g00;
                        b01 = b00;
                        a01 = a00;
                    }
                    if (sx < width1) {
                        rgb = oldImage[sx + 1 + sy];
                        r10 = rgb & 0xFF;
                        g10 = (rgb >> 8) & 0xFF;
                        b10 = (rgb >> 16) & 0xFF;
                        a10 = (rgb >> 24) & 0xFF;
                    } else {
                        r10 = r00;
                        g10 = g00;
                        b10 = b00;
                        a10 = a00;
                    }
                    if (sy < height2 && sx < width1) {
                        rgb = oldImage[sx + 1 + sy + width];
                        r11 = rgb & 0xFF;
                        g11 = (rgb >> 8) & 0xFF;
                        b11 = (rgb >> 16) & 0xFF;
                        a11 = (rgb >> 24) & 0xFF;
                    } else {
                        r11 = r00;
                        g11 = g00;
                        b11 = b00;
                        a11 = a00;
                    }

                    sxPrev = sx;
                    syPrev = sy;
                }

                int cf1 = (newHeight - ycnt);
                int r1 = (r00 * cf1 + r01 * ycnt) / newHeight;
                int g1 = (g00 * cf1 + g01 * ycnt) / newHeight;
                int b1 = (b00 * cf1 + b01 * ycnt) / newHeight;
                int a1 = (a00 * cf1 + a01 * ycnt) / newHeight;
                int r2 = (r10 * cf1 + r11 * ycnt) / newHeight;
                int g2 = (g10 * cf1 + g11 * ycnt) / newHeight;
                int b2 = (b10 * cf1 + b11 * ycnt) / newHeight;
                int a2 = (a10 * cf1 + a11 * ycnt) / newHeight;

                if (blockWidth != -1) {
                    if (bxCnt == blockWidth1) {
                        r2 = r1;
                        g2 = g1;
                        b2 = b1;
                        a2 = a1;
                    }
                    if ((bxCnt == 0) && (dx != 0)) {
                        r1 = r2;
                        g1 = g2;
                        b1 = b2;
                        a1 = a2;
                    }
                }

                int cf2 = (newWidth - xcnt);
                int r = (r1 * cf2 + r2 * xcnt) / newWidth;
                int g = (g1 * cf2 + g2 * xcnt) / newWidth;
                int b = (b1 * cf2 + b2 * xcnt) / newWidth;
                int a = (a1 * cf2 + a2 * xcnt) / newWidth;

                if (r > 255) r = 255;
                if (r < 0) r = 0;
                if (g > 255) g = 255;
                if (g < 0) g = 0;
                if (b > 255) b = 255;
                if (b < 0) b = 0;
                if (a > 255) a = 255;
                if (a < 0) a = 0;

                if (!useAlpha) a = (a < 64) ? 0 : 255;

                newImage[dx + dy] = r | (g << 8) | (b << 16) | (a << 24);

                ycnt += height;
                while (ycnt > newHeight) {
                    sy += width;
                    ycnt -= newHeight;
                }
            }

            xcnt += width;
            while (xcnt >= newWidth) {
                sx++;
                xcnt -= newWidth;
            }
            if ((blockWidth != -1) && (++bxCnt == blockWidth)) bxCnt = 0;
        }

        return Image.createRGBImage(newImage, newWidth, newHeight, true);
    }

    
    static public int getInverseColor(int color) {
        int r = (color & 0xFF);
        int g = ((color >> 8) & 0xFF);
        int b = ((color >> 16) & 0xFF);
        return ((r + g + b) > 3 * 127) ? 0 : 0xFFFFFF;
    }

    public void addCommandEx(Command cmd, int type) {
        switch (type) {
            case MENU_TYPE_LEFT_BAR:
                leftMenu = cmd;
                break;

            case MENU_TYPE_RIGHT_BAR:
                rightMenu = cmd;
                break;
        }
    }

    public void removeCommandEx(Command cmd) {
        if (cmd == leftMenu) {
            leftMenu = null;
        } else if (cmd == rightMenu) {
            rightMenu = null;
        }
        invalidate();
    }

    public void removeAllCommands() {
        leftMenu = null;
        rightMenu = null;
    }
}