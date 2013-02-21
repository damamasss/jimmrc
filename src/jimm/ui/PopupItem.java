//#sijapp cond.if modules_PANEL is "true"#
package jimm.ui;

import DrawControls.*;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

import jimm.Options;

import java.util.Timer;
import java.util.TimerTask;

public class PopupItem extends CanvasEx {

    private int x;
    private int y;
    private int height;
    private int width;
    public TextList paintList;
    private Icon icon;
    private int cback;
    private int trans;

    public PopupItem(int y, String mess, Icon icon) {
        this.y = y;
        this.icon = icon;
        height = NativeCanvas.getHeightEx();
        width = 80 * NativeCanvas.getWidthEx() / 100;
        cback = getColor(COLOR_CAP);
        int ctext = getInverseColor(cback);
        trans = 255 - (Options.getInt(Options.OPTION_POPUP_TRANS) * 255 / 10);
        paintList = new TextList(null);
        paintList.setFontSize(Font.SIZE_SMALL);
        paintList.addBigTextInternal(mess, ctext, Font.STYLE_PLAIN, -1, width);
        paintList.setColors(0, ctext, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        height = Math.min(facade.getFontHeight() * paintList.getSize() + 1, Math.max(facade.getFontHeight() + 2, 30 * height / 100));
        int linesCount = paintList.getSize();
        for (int line = 0, textHeight = 0; line < linesCount; line++) {
            textHeight += paintList.getLine(line).getHeight(Font.SIZE_SMALL);
            if (textHeight > height - 3) {
                break;
            }
        }
        if (paintList.getSize() > 0) {
            width = 1;
            for (int i = paintList.getSize() - 1; i >= 0; i--) {
                width = Math.max(width, paintList.getLine(i).getWidth(Font.SIZE_SMALL));
            }
        }
        x = NativeCanvas.getWidthEx() - width - 1;

    }

    public int getHeight() {
        return height;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

//    public boolean slide(CanvasEx bgd) {
//        if (!animated) {
//            Jimm.setDisplay(new SliderTask(this, (CanvasEx)Jimm.getCurrentDisplay(), (CanvasEx)Jimm.getCurrentDisplay(), 10));
//            return false;
//        }
//        return true;
//    }

    public void paint(Graphics g) {
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();
        int iconw = (icon != null) ? icon.getWidth() : 0;
        drawAlphaRect(g, cback, x - iconw, y, x + width, y + height, trans);
        g.setClip(x, y, width, height);
        int top = y;
        int linesCount = paintList.getSize();
        for (int line = 0; line < linesCount; line++) {
            paintList.getLine(line).paint(x, top, g, Font.SIZE_SMALL, paintList);
            top += paintList.getLine(line).getHeight(Font.SIZE_SMALL);
            if (top >= y + height) {
                break;
            }
        }
        if (icon != null) {
            g.setClip(x - iconw - 1, y, iconw + 1, height);
            icon.drawByLeftTop(g, x - iconw - 1, y);
            g.setStrokeStyle(Graphics.DOTTED);
            g.drawLine(x - 1, y, x - 1, height);
            g.setStrokeStyle(Graphics.SOLID);
        }
        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    public void doKeyreaction(int keyCode, int type) {
    }

    //#sijapp cond.if target is "MIDP2"#
    public void pointerPressed(int x, int y) {
    }
//#sijapp cond.end#

    private static PopupItem[] actions = new PopupItem[0];
    private static Timer timer = new Timer();

    public static void addItem(PopupItem pc) {
        synchronized (actions) {
            int size = actions.length;
            PopupItem[] pct = new PopupItem[size + 1];
            if (size > 0) {
                System.arraycopy(actions, 0, pct, 0, size);
            }
            pct[size] = pc;
            actions = pct;
        }
    }

    public static void removeItem() {
        synchronized (actions) {
            int size = actions.length - 1;
            PopupItem[] pct = new PopupItem[size];
            if (size == 0) {
                actions = pct;
                return;
            }
            System.arraycopy(actions, 1, pct, 0, size);
            int y = 0;
            for (int i = 0; i < size; i++) {
                pct[i].setY(y);
                y += pct[i].getHeight();
            }
            actions = pct;
        }
    }

    public static int getSize() {
        synchronized (actions) {
            return actions.length;
        }
    }

    public static void addItem(String name, Icon icon) {
        int y = 0;
        final int curr = getSize();
        if (curr > 0) {
            synchronized (actions) {
                for (int i = 0; i < curr; i++) {
                    y += (actions[i]).getHeight();
                }
            }
        }
        if (y > NativeCanvas.getHeightEx()) {
            return;
        }
        PopupItem item = new PopupItem(y, name, icon);
        addItem(item);
        NativeCanvas.Repaint();
        timer.schedule(
                new TimerTask() {
                    public void run() {
                        removeItem();
                        NativeCanvas.Repaint();
                    }
                }
                , 5000);
    }

    public static void paintItems(Graphics g) {
        if (getSize() > 0) {
            synchronized (actions) {
                for (int i = 0; i < getSize(); i++) {
                    (actions[i]).paint(g);
                }
            }
        }
    }
}
//#sijapp cond.end#