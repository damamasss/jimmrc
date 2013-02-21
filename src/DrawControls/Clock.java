/*
package DrawControls;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Clock extends CanvasEx {

    private int x;
    private int y;
    private int w;
    private int h;
    private int ñolor;
    private int ñolorRect;
    private String text = "";
    private Image image;
    private int[] alphaBuff = new int[0];

    public Clock() {
    }

    public void update(String time, int x1, int y1, int x2, int y2, int color) {
        if (!text.equals(time)) {
            text = time;
        }
        int wAll = x2 - x1;
        int wChar = (wAll - 6) / time.length();
        w = wChar / 4;
        h = wChar - w;
        x = (x2 + x1) / 2 - (time.length() * (h + h / 8)) / 2;
        y = (y2 + y1) / 2 - h;
        ñolor = (color & 0xFFFFFF) | ((3 * 15) << 24);
        ñolorRect = (color & 0xFFFFFF) | ((10 * 15) << 24);
        updateImg();
    }

    public void doKeyreaction(int keyCode, int type) {
    }

    public void updateImg() {
        System.out.println("up" + " w = " +(h + h / 8) * (text.length() - 1) + w + " h = " + (h - y));
        image = Image.createImage((h + h / 8) * (text.length() - 1) + w, h);
        Graphics gi = image.getGraphics();
        gi.translate(-x, -y);
        for (int i = 0; i < text.length(); i++) {
            drawChar(gi, text.charAt(i), x + (h + h / 8) * i, y, h, w);
        }
        image = Image.createImage(image);
    }

    public void paint(Graphics g) {
        if (image != null) {
            System.out.println("cl x = " + x + " y = " + y + " w = " + image.getWidth() + " h = " + image.getHeight());
            g.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
        }
    }

    private void alpha(int len, int cl) {
        len += 2;
        synchronized (alphaBuff) {
            if (alphaBuff.length < len) {
                alphaBuff = null;
                alphaBuff = new int[len];
            }
            if (alphaBuff[0] != cl) {
                for (int i = 0; i < alphaBuff.length; i++) {
                    alphaBuff[i] = cl;
                }
            }
        }
    }

    private void drawChar(Graphics g, char ch, int x, int y, int h, int w) {
        if (ch == '1') {
            x -= w;
        }
        final int w2 = w / 2;
        final int xLeft = x;
        final int xRight = x + h;
        final int secondTop = y + h - w2;
        final int firstBottom = secondTop + w - 1;
        final int secondBottom = y + h + h + 1;
        final int k = 1;

        if ("23567890".indexOf(ch) >= 0) { // âåðõ
            drawHBar(xLeft + k, y, xRight, y + w, g);
        }
        if ("2345689".indexOf(ch) >= 0) { // ñåðåäèíà
            drawHBar(xLeft + k, secondTop, xRight, secondTop + w, g);
        }
        if ("2356890".indexOf(ch) >= 0) {// íèç
            drawHBar(xLeft + k, secondBottom - w, xRight, secondBottom, g);
        }
        if ("456890".indexOf(ch) >= 0) {// âåðõ
            drawVBar(xLeft, y + k, xLeft + w, firstBottom, g);
        }
        if ("12347890".indexOf(ch) >= 0) {// âåðõ
            drawVBar(xRight - w, y + k, xRight, firstBottom, g);
        }
        if ("2680".indexOf(ch) >= 0) {// íèç
            drawVBar(xLeft, secondTop + k, xLeft + w, secondBottom, g);
        }
        if ("134567890".indexOf(ch) >= 0) {// íèç
            drawVBar(xRight - w, secondTop + k, xRight, secondBottom, g);
        }
        if (ch == ':') {
            drawVBar(x + h / 2 - w2, y + h - 2 * w, x + h / 2 + w2, y + h, g);
            drawVBar(x + h / 2 - w2, y + h, x + h / 2 + w2, y + h + 2 * w, g);
        }
    }

    private void drawHBar(int x1, int y1, int x2, int y2, Graphics g) {
        int h = y2 - y1;
        int d = -1;
        final int half = (y1 + y2) / 2;
        for (int i = y1; i <= y2; i++) {
            if (i == y1 || i == y2) {
                drawHLine(g, x1 + h, i, x2 - h, true);
            } else {
                drawHLine(g, x1 + h, i, x2 - h, false);
            }
            if (i >= half) {
                d = 1;
            }
            h += d;
        }
    }

    private void drawVBar(int x1, int y1, int x2, int y2, Graphics g) {
        int h = x2 - x1;
        int d = -1;
        final int half = (x1 + x2) / 2;
        for (int i = x1; i <= x2; i++) {
            if (i == x1 || i == x2) {
                drawVLine(g, i, y1 + h, y2 - h, true);
            } else {
                drawVLine(g, i, y1 + h, y2 - h, false);
            }
            if (i >= half) {
                d = 1;
            }
            h += d;
        }
    }

    private void drawVLine(Graphics gr, int x1, int y1, int y2, boolean full) {
        int len = y2 - y1;
        if (full) {
            alpha(len, ñolorRect);
            gr.drawRGB(alphaBuff, 0, 1, x1, y1, 1, len, true);
        } else {
            alpha(len, ñolor);
            alphaBuff[0] = ñolorRect;
            alphaBuff[len - 1] = ñolorRect;
            gr.drawRGB(alphaBuff, 0, 1, x1, y1, 1, len, true);
            alphaBuff[0] = ñolor;
            alphaBuff[len - 1] = ñolor;
        }
    }

    private void drawHLine(Graphics gr, int x1, int y1, int x2, boolean full) {
        int len = x2 - x1;
        if (full) {
            alpha(len, ñolorRect);
            gr.drawRGB(alphaBuff, 0, len, x1, y1, len, 1, true);
        } else {
            alpha(len, ñolor);
            alphaBuff[0] = ñolorRect;
            alphaBuff[len - 1] = ñolorRect;
            gr.drawRGB(alphaBuff, 0, len, x1, y1, len, 1, true);
            alphaBuff[0] = ñolor;
            alphaBuff[len - 1] = ñolor;
        }
    }
}
*/
