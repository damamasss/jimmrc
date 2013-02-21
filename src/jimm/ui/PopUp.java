package jimm.ui;

import javax.microedition.lcdui.*;

import jimm.*;
import jimm.util.Device;
import jimm.comm.Util;
import DrawControls.*;

public class PopUp extends CanvasEx {
    /**
     * @author Rishat Shamsutdinov
     */
    private static final long TIMEOUT = 1000;

    public static ImageList icons = ImageList.loadFull("picons.png");

    public CanvasEx prvScreen;
    public TextList paintList;
    private int fontSize;
    public int width;
    private int screenWidth;
    private int height;
    public int x;
    public int y;
    public int topLine = 0;
    public int visCount = 0;

    public PopUp(Object prvScreen, String str, int width, int x, int y) {
        this(prvScreen, str, width, -1, x, y);
    }

    public PopUp(Object prvScreen, String str, int width, int height, int x, int y) {
        super();
        if (!(prvScreen instanceof CanvasEx || prvScreen == null) || (str == null) || (str.length() == 0)) {
            return;
        }
        if (prvScreen instanceof SliderTask) {
            prvScreen = ((SliderTask) prvScreen).getNextScreen();
        }
        fontSize = Font.SIZE_SMALL;
        this.prvScreen = (CanvasEx) prvScreen;
        screenWidth = width;
        this.x = x + 2;
        this.y = y + 2;

        this.width = 95 * width / 100;
        this.width -= 4;

        paintList = new TextList(null);
        paintList.setFontSize(fontSize);
        paintList.addBigTextInternal(str, getInverseColor(getInverseColor(COLOR_BACK)), Font.STYLE_PLAIN, -1, this.width);

        if (height == -1) {
            height = NativeCanvas.getHeightEx();
        }
        this.height = Math.min(getFontHeight() * paintList.getSize() + 4, Math.max(getFontHeight() + 2, 30 * height / 100));
        int linesCount = paintList.getSize();
        visCount = linesCount;
        for (int line = 0, textHeight = 0; line < linesCount; line++) {
            textHeight += paintList.getLine(line).getHeight(fontSize);
            if (textHeight > this.height - 3) {
                visCount = line;
                break;
            }
        }
        if (paintList.getSize() > 0) {
            this.width = 0;
            for (int i = paintList.getSize() - 1; i >= 0; i--) {
                this.width = Math.max(this.width, paintList.getLine(i).getWidth(fontSize));
            }
        }
        if (visCount < paintList.getSize()) {
            this.width += scrollerWidth;
        }
        addCommandEx(JimmUI.cmdYes, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdNo, MENU_TYPE_RIGHT_BAR);
        if (Options.getBoolean(Options.OPTION_ANIMATION)) {
            lock();
        }
    }

    private int getFontHeight() {
        return facade.getFontHeight();
    }

    public static void updateIcons() {
        icons = ImageList.loadFull("picons.png");
    }

    public void beforeHide() {
    }

    protected void afterShow() {
        startRepaintTTask();
    }

    public Image paintOnImage() {
        Image img = Image.createImage(width + 5, height + icons.getHeight() + 2);
        Graphics g = img.getGraphics();
        g.translate(-x + 2, -y + 2);
        unlock();
        paint(g);
        lock();
        return img;
    }

    public boolean slide(CanvasEx bgd) {
        if (!animated) {
            Jimm.setDisplay(new SliderTask(this, prvScreen, this, -width - x + 2, 0, width + x - 2, 0, x - 2, y - 2));
            unlock();
            return false;
        }
        return true;
    }

    private void startRepaintTTask() {
        try {
            Jimm.getTimerRef().schedule(new TimerTasks(this), TIMEOUT, TIMEOUT);
        } catch (Exception ignored) {
        }
    }

    public CanvasEx getPrvScreen() {
        return prvScreen;
    }

    public int getY() {
        return y - 2;
    }

    public int getHeight() {
        return (height + 3 + icons.getHeight());
    }

    public int getWidth() {
        return screenWidth;
    }

    //#sijapp cond.if target is "MIDP2"#
    public void pointerPressed(int x, int y) {
// #sijapp cond.if modules_TOUCH2 is "true" #
        pointerReleased(x, y);
// #sijapp cond.end#
    }

    public void pointerDragged(int x, int y) {
        if (lastPointerTopItem == -1) {
            return;
        }
        int itemCount = paintList.getSize();
        if (itemCount == visCount) {
            return;
        }
        topLine = lastPointerTopItem + itemCount * (y - lastPointerYCrd) / (height - 3);
        if (topLine < 0) {
            topLine = 0;
        }
        if (topLine > (itemCount - visCount)) {
            topLine = itemCount - visCount;
        }
        invalidate();
    }

    public void pointerReleased(int x, int y) {
        if ((visCount < paintList.getSize()) && (x >= this.x + width - scrollerWidth - 1)) {
            if ((scrollerY1 <= y) && (y < scrollerY2)) {
                lastPointerYCrd = y;
                lastPointerTopItem = topLine;
                return;
            }
        }
        if (ptInRect(x, y, this.x, this.y, this.x + width, this.y + height)) {
            doKeyreaction(KEY_CODE_LEFT_MENU, KEY_RELEASED);
            return;
        }
        Icon icon = icons.elementAt(0);
        if (icon != null) {
            if (ptInRect(x, y, this.x - 1, this.y + height, this.x + icon.getWidth() + 1, this.y + height + icon.getHeight())) {
                doKeyreaction(KEY_CODE_LEFT_MENU, KEY_RELEASED);
                return;
            }
        }
        icon = icons.elementAt(1);
        if (icon != null) {
            if (ptInRect(x, y, this.x + width - icon.getWidth() - 1, this.y + height, this.x + width + 1, this.y + height + icon.getHeight())) {
                doKeyreaction(KEY_CODE_RIGHT_MENU, KEY_RELEASED);
                return;
            }
        }
    }
    //#sijapp cond.end#

    public void paint0(Graphics g) {
        int color = getInverseColor(getColor(COLOR_BACK));
        drawGlassRect(g, getColor(COLOR_CAP), x - 2, y - 2, x + width + 3, y + height + 1 + icons.getHeight());
        g.setColor(color);
        g.fillRect(x, y, width + 1, height - 3);
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();
        g.setClip(x, y, width + 1, height - 3);
        paintList.setColors(0, getInverseColor(color), color, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        int top = y + 1;
        int linesCount = paintList.getSize();
        int height = top + this.height;
        for (int line = topLine; line < linesCount; line++) {
            paintList.getLine(line).paint(x + 1, top, g, fontSize, paintList);
            top += paintList.getLine(line).getHeight(fontSize);
            if (top >= height) {
                break;
            }
        }
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        Icon icon = icons.elementAt(0);
        if (icon != null) {
            icon.drawInVCenter(g, x, y + this.height + icon.getHeight() / 2);
        }
        icon = icons.elementAt(1);
        if (icon != null) {
            icon.drawInVCenter(g, x + width - icon.getWidth(), y + this.height + icon.getHeight() / 2);
        }
    }

    public void paint(Graphics g) {
        if (prvScreen != null) {
            prvScreen.paint(g);
        }
        //int color = getInverseColor(getColor(COLOR_BACK));
        //drawGlassRect(g, getColor(COLOR_CAP), x - 2, y - 2, x + width + 3, y + height + 1 + icons.getHeight());
        drawAvernageAlpha(g, x - 2, y - 2, x + width + 3, y + height + 1 + icons.getHeight(), 0, 0xff, getColor(COLOR_CAP));
        //g.setColor(color);
        //g.fillRect(x, y, width + 1, height - 3);
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();
        g.setClip(x, y, width + 1, height - 3);
        paintList.setColors(0, getInverseColor(getColor(COLOR_CAP)), getColor(COLOR_CAP), 0, 0, 0, 0, 0, 0, 0, 0, 0);

        int top = y + 1;
        int linesCount = paintList.getSize();
        int height = top + this.height;
        for (int line = topLine; line < linesCount; line++) {
            paintList.getLine(line).paint(x + 1, top, g, fontSize, paintList);
            top += paintList.getLine(line).getHeight(fontSize);
            if (top >= height) {
                break;
            }
        }
        drawScroller(g, topLine, y, visCount, x + width - scrollerWidth + 1, this.height - 3, linesCount, true);
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        if (NativeCanvas.getCanvas() != this) {
            return;
        }
        //drawGlassRect(g, 0x333333, x, y + this.height, x + width/2 - 1, y + this.height + icons.getHeight() - 1);// todo knopki
        //drawGlassRect(g, 0x333333, x + width/2 + 1, y + this.height, x + width, y + this.height + icons.getHeight() - 1);
        Icon icon = icons.elementAt(0);
        if (icon != null) {
            icon.drawInVCenter(g, x, y + this.height + icon.getHeight() / 2);
        }
        icon = icons.elementAt(1);
        if (icon != null) {
            icon.drawInVCenter(g, x + width - icon.getWidth(), y + this.height + icon.getHeight() / 2);
        }
    }

    public void doKeyreaction(int keyCode, int type) {
        if (type != KEY_RELEASED) {
            return;
        }

        int ga = getExtendedGameAction(keyCode);
        switch (ga) {
            case Canvas.UP:
                topLine = Math.max(0, topLine -= visCount);
                break;

            case Canvas.DOWN:
                if (visCount < paintList.getSize()) {
                    topLine = Math.min(topLine += visCount, paintList.getSize() - visCount);
                }
                break;

            case KEY_CODE_RIGHT_MENU:
                if (prvScreen instanceof PopUp) {
                    Jimm.setDisplay(prvScreen);
                    prvScreen.doKeyreaction(KEY_CODE_RIGHT_MENU, KEY_RELEASED);
                    return;
                }

            case KEY_CODE_LEFT_MENU:
            case KEY_CODE_BACK_BUTTON:
            case Canvas.FIRE:
                if (Options.getBoolean(Options.OPTION_ANIMATION)) {
                    Jimm.setDisplay(new SliderTask(this, prvScreen, prvScreen, 0, 0, -width - x + 2, 0, x - 2, y - 2));
                } else {
                    Jimm.setDisplay(prvScreen);
                }
                return;
        }

        switch (keyCode) {
            case Canvas.KEY_POUND:
                if (prvScreen instanceof PopUp) {
                    Jimm.setDisplay(prvScreen);
                    prvScreen.doKeyreaction(KEY_CODE_RIGHT_MENU, KEY_RELEASED);
                    return;
                }

            case Canvas.KEY_STAR:
                if (Options.getBoolean(Options.OPTION_ANIMATION)) {
                    Jimm.setDisplay(new SliderTask(this, prvScreen, prvScreen, 0, 0, -width - x + 2, 0, x - 2, y - 2));
                } else {
                    Jimm.setDisplay(prvScreen);
                }
                return;
        }
        invalidate();
    }
}