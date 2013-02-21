// #sijapp cond.if modules_DEBUGLOG is "true"#
package jimm.ui;

import DrawControls.ListItem;
import DrawControls.NativeCanvas;
import DrawControls.VirtualList;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.Options;
import jimm.comm.Util;
import jimm.forms.FormEx;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;

public class ColorChooser extends VirtualList implements CommandListener, MenuListener {

    static final int BORDER = 3;

    private int width; // Width and height of canvas
    private int height; // Width and height of canvas
    private int label_w; // width of "999"
    private int label_h; // height of "999"
    private int rgbColor;
    private final int radix = 16; // radix to display numbers (10 or 16)
    private static int delta = 0x10; // default increment/decrement
    private int ndx = 0; // 0 == blue, 1 == green, 2 == red
    private LineChoise stepList;
    private boolean setStep;
    private boolean fast;
    private int fastw;
    private int fasty;
    private int curfast;

    public ColorChooser(int color) {
        super(null);
        ndx = 2;

        width = NativeCanvas.getWidthEx();
        height = getHeightInternal() - getMenuBarHeight();

        label_h = facade.getFontHeight();
        label_w = facade.stringWidth("999");
        fastw = facade.charWidth('0') + 2;
        fasty = height - label_h - 7;
        //fast = !Jimm.isTouch();
        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        setCommandListener(this);
        rgbColor = color;
        setColorScheme();
    }

    protected int getSize() {
        return 0;
    }

    protected void get(int index, ListItem item) {
    }

    public void setColor(int red, int green, int blue) {
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        setColor((red << 16) | (green << 8) | blue);
    }

    public void setColor(int RGB) {
        rgbColor = RGB & 0x00ffffff;
        invalidate();
    }

    public int getColor() {
        return rgbColor;
    }

    public int getRedComponent() {
        return (rgbColor >> 16) & 0xff;
    }

    public int getGreenComponent() {
        return (rgbColor >> 8) & 0xff;
    }

    public int getBlueComponent() {
        return rgbColor & 0xff;
    }

    public void paintAllOnGraphics(Graphics g, int mode, int curX, int curY) {
        colorPaint(g, curX, curY);
        drawMenuBar(g, getMenuBarHeight(), getHeightInternal(), mode, curX, curY);
        //drawMenuItems(g, getMenuBarHeight(), getHeightInternal(), mode, curX, curY);
    }

    public boolean drawMenuBar(Graphics g, int height, int y2, int style, int curX, int curY) {
        int mb = getMenuBarHeight();
        g.setColor(bkgrndColor);
        g.fillRect(0, NativeCanvas.getHeightEx() - mb, NativeCanvas.getWidthEx(), mb);
        return super.drawMenuBar(g, height, y2, style, curX, curY);
    }

    public void setDelta(int delta) {
        if ((delta > 0) && (delta <= 128)) {
            ColorChooser.delta = delta;
        }
    }

    private String format(int num) {
        String s = Integer.toString(num, radix);
        if (s.length() >= 2) {
            return s;
        }
        return "0" + s;
    }

    protected void colorPaint(Graphics g, int curX, int curY) {
        // Scale palette cells to fit 10 across
        int p_w = width / 10;
        int p_h = (height - (BORDER * 3)) / 4;
        int usable_w = p_w * 9;
        // int usable_w = (p_w*10)/20*19;

        int sample_w = (p_w * 10) - 2;

        int sample_x = (width - usable_w / 9 * 10 + 2) / 2;
        int sample_y = 2;
        //int p_x = sample_x;
        int p_y = sample_y + p_h + 4;

        // Fill the background
        g.setColor(0xffffff);
        g.fillRect(0, 0, width, height);

        // Fill in the color sample
        g.setColor(rgbColor);
        g.fillRect(sample_x, sample_y, sample_w, p_h);
        g.setColor((ndx < 0) ? 0x000000 : 0x808080);
        g.drawRect(sample_x, sample_y, sample_w - 1, p_h - 1);

        int bars_y = p_y + p_h + BORDER;

        int bar_h = label_h + BORDER;
        int bar_w = usable_w - label_w - BORDER;

        int b_x = label_w + BORDER;
        int r_y = bars_y + BORDER;
        int g_y = r_y + bar_h;
        int b_y = g_y + bar_h;

        if (ptInRect(curX, curY, b_x, r_y, width, r_y + bar_h)) {
            //System.out.println("Red->" + (255 * curX / bar_w - b_x));
            setColor(255 * curX / bar_w - b_x, getGreenComponent(), getBlueComponent());
        } else if (ptInRect(curX, curY, b_x, g_y, width, g_y + bar_h)) {
            //System.out.println("Green");
            setColor(getRedComponent(), 255 * curX / bar_w - b_x, getBlueComponent());
        } else if (ptInRect(curX, curY, b_x, b_y, width, b_y + bar_h)) {
            //System.out.println("Blue");
            setColor(getRedComponent(), getGreenComponent(), 255 * curX / bar_w - b_x);
        }

        // Draw the color bars
        //blue
        int b_w = (bar_w * getBlueComponent()) / 255;
        g.setColor(0, 0, 255);
        g.fillRect(b_x, b_y, b_w, bar_h - BORDER);
        g.setColor((ndx == 0) ? 0x000000 : 0xa0a0ff);
        g.drawRect(b_x, b_y, bar_w - 1, bar_h - BORDER - 1);
        //green
        int g_w = (bar_w * getGreenComponent()) / 255;
        g.setColor(0, 255, 0);
        g.fillRect(b_x, g_y, g_w, bar_h - BORDER);
        g.setColor((ndx == 1) ? 0x000000 : 0xa0ffa0);
        g.drawRect(b_x, g_y, bar_w - 1, bar_h - BORDER - 1);
        //red
        int r_w = (bar_w * getRedComponent()) / 255;
        g.setColor(255, 0, 0);
        g.fillRect(b_x, r_y, r_w, bar_h - BORDER);
        g.setColor((ndx == 2) ? 0x000000 : 0xffa0a0);
        g.drawRect(b_x, r_y, bar_w - 1, bar_h - BORDER - 1);

        g.setColor(0, 0, 0);
        drawString(g, facade, format(getBlueComponent()), label_w, b_y + bar_h, Graphics.BOTTOM | Graphics.RIGHT);
        drawString(g, facade, format(getGreenComponent()), label_w, g_y + bar_h, Graphics.BOTTOM | Graphics.RIGHT);
        drawString(g, facade, format(getRedComponent()), label_w, r_y + bar_h, Graphics.BOTTOM | Graphics.RIGHT);

        int i = (width - 6 * fastw) / 2;
        String s = "00000" + Integer.toHexString(rgbColor).toUpperCase();
        s = s.substring(s.length() - 6);
        if (fast) {
            g.setColor(0xff0000);
        }
        drawString(g, facade, "[*]", 0, fasty + 4, Graphics.TOP | Graphics.LEFT);
        for (int j = 0; j < s.length(); j++) {
            g.setColor(j >= 2 ? j >= 4 ? 0x2020e0 : 0x20e020 : 0xe02020);
            drawString(g, facade, s.substring(j, j + 1), i + j * fastw, fasty + 4, Graphics.TOP | Graphics.LEFT);
            if (j == curfast) {
                g.setColor(0);
                g.fillRect(i + j * fastw, fasty + 3, fastw - 3, 2);
                g.fillRect(i + j * fastw, fasty + label_h + 3, fastw - 3, 2);
            }
        }
    }

    public void doKeyreaction(int key, int type) {
        int action = getExtendedGameAction(key);
        if (action == KEY_CODE_LEFT_MENU || action == KEY_CODE_RIGHT_MENU || action == KEY_CODE_BACK_BUTTON) {
            super.doKeyreaction(key, type);
            return;
        }
        switch (action) {
            case Canvas.FIRE:
                if (type == KEY_RELEASED) {
                    Options.getOptionsForm().colorChoosedForm(rgbColor);
                }
                return;
        }
        if (type == KEY_RELEASED) {
            return;
        }
        if (key == Canvas.KEY_STAR) {
            various();
            return;
        }
//        switch (action) {
//            case 11:
//            case Canvas.KEY_STAR: // Star
//                System.out.println("a");
//                various();
//                return;
//        }
        int dir = 0;
        switch (action) {
            case Canvas.RIGHT:
                if (!fast) {
                    dir += 1;
                } else {
                    curfast = (curfast + 1 + 6) % 6;
                }
                break;

            case Canvas.LEFT:
                if (!fast) {
                    dir -= 1;
                } else {
                    curfast = (curfast - 1 + 6) % 6;
                }
                break;

            case Canvas.DOWN:
                if (!fast) {
                    ndx -= 1;
                    if (ndx < 0) ndx = 2;
                } else {
                    int k = 0x100000 >> curfast * 4;
                    int l = 0xf00000 >> curfast * 4;
                    rgbColor = rgbColor & ~l | rgbColor - k & l;
                }
                break;

            case Canvas.UP:
                if (!fast) {
                    ndx += 1;
                } else {
                    int k = 0x100000 >> curfast * 4;
                    int l = 0xf00000 >> curfast * 4;
                    rgbColor = rgbColor & ~l | rgbColor + k & l;
                }
                break;

            default:
                return; // nothing we recognize, exit
        }

        // Limit selection to r,g,init and palette
        if (ndx < -1) {
            ndx = -1;
        }

        if (ndx > 2) {
            ndx = 0;
        }

        if (ndx >= 0) {
            int v = (rgbColor >> (ndx * 8)) & 0xff;
            v += (dir * delta);

            if (v < 0) {
                v = 0;
            }

            if (v > 255) {
                v = 255;
            }

            int mask = 0xff << (ndx * 8);
            rgbColor = (rgbColor & ~mask) | (v << (ndx * 8));
        }
        invalidate();
    }

    public void menuSelect(Menu menu, byte action) {
        menu.back();
        switch (action) {
            case 0:
                //Options.getOptionsForm().colorChoosed(rgbColor, true);
                Options.getOptionsForm().colorChoosedForm(rgbColor);
                break;

            default:
                showStepSelector();
                break;
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (setStep) {
            if (c == JimmUI.cmdSelect) {
                delta = deltas[stepList.getSelected()];
            }
            setStep = false;
            Jimm.setDisplay(this);
            return;
        }
        if (c == JimmUI.cmdMenu) {
            Menu menu = new Menu(this);
            menu.addMenuItem("Ok", (byte) 0);
            menu.addMenuItem("set_step", (byte) 1);
            menu.setMenuListener(this);
            Jimm.setDisplay(menu);
        } else {
            Jimm.back();
        }
    }

    private void showStepSelector() {
        setStep = true;
        FormEx form = new FormEx(ResourceBundle.getString("set_step"), JimmUI.cmdSelect, JimmUI.cmdBack);
        stepList = new LineChoise("", Util.explode("1" + "|" + "5" + "|" + "10" + "|" + "15" + "|" + "20" + "|" + "40", '|'));
        form.append(stepList);

        form.setCommandListener(this);
        Jimm.setDisplay(form);
    }

    private int[] deltas = {0x01, 0x05, 0x10, 0x15, 0x20, 0x40};

    private void various() {
        fast = !fast;
        invalidate();
    }
}
// #sijapp cond.end#