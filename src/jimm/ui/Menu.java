package jimm.ui;

import DrawControls.*;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.Options;
import jimm.TimerTasks;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Menu extends CanvasEx {
    /**
     * @author Rishat Shamsutdinov
     */
    public static final long TIMEOUT = 1000;

    protected class MenuItem {
        private Icon icons[];
        private String text;
        private byte action;
        private int height = -1;
        private int width = -1;

        MenuItem(String text, Icon icons[], byte action) {
            this.icons = icons;
            this.text = text;
            this.action = action;
        }

        int getHeight(FontFacade font) {
            if (height < 0) {
                int iconHeight = 0;
                if (icons != null) {
                    for (int i = icons.length - 1; i >= 0; i--) {
                        if (icons[i] != null) {
                            iconHeight = Math.max(iconHeight, icons[i].getHeight());
                        }
                    }
                }
                height = Math.max(iconHeight, Math.max(font.getFontHeight(), Options.getInt(Options.OPTION_ICONS_CANVAS))) + 1;
            }
            return height;
        }

        int getWidth(FontFacade font) {
            if (width < 0) {
                width = font.stringWidth(text) + 2;
                if (icons != null) {
                    for (int i = icons.length - 1; i >= 0; i--) {
                        if (icons[i] != null) {
                            width += icons[i].getWidth();
                        }
                    }
                    width += 3;
                }
            }
            return width;
        }
    }

    ////////////////////////////////////////////////
    private static Image alpha_m = null;
    private static int corn[] = null;

    private CanvasEx prvScreen;
    protected MenuItem items[] = new MenuItem[0];// = new Vector();
    private MenuListener listener;
    //private static final FontFacade font = new FontFacade(getSuperFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));
    private int width = 0;
    private int height = 1;

    //private Image bgdImg;

    private int topItem = 0;
    private int currItem = 0;
    private int topY = 0;
    private int x;
    //private int fX;
    //private int dX;
    private int dY;
    private byte side = -1;
    private int menuTrans;
    //private int menuScreenTrans;
    private boolean sideWasInit;
    //private boolean animated;
    //private TimerTask slide;
//	private static int ih = 0;

    static {
        try {
            alpha_m = Image.createImage("/alpha_m.png");
        } catch (Exception ignored) {
        }

        Image tmp = null;
        try {
            tmp = Image.createImage("/corn.png");
        } catch (Exception ignored) {
        }

        if (tmp != null) {
            corn = new int[tmp.getWidth() * tmp.getHeight()];
            tmp.getRGB(corn, 0, tmp.getWidth(), 0, 0, tmp.getWidth(), tmp.getHeight());
        }
    }

    public Menu(CanvasEx prvScreen, byte side) {
        this(prvScreen);
        //if (!Options.getBoolean(Options.OPTION_NEW_MSTYLE)) {
        this.side = side;
        //} else {
        //	this.side = 0;
        //}
        sideWasInit = true;
    }

    public Menu(CanvasEx c, boolean animate) {
        this(c);
        animated = !animate;
    }

    public Menu(CanvasEx prvScreen) {
        super();
        setPrvScreen(prvScreen);
        addCommandEx(JimmUI.cmdSelect, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdCancel, MENU_TYPE_RIGHT_BAR);
//#sijapp cond.if target is "MIDP2"#
//		if (Jimm.isTouch()) {
//			ih = PopUp.icons.getHeight();
//		}
//		if (ih > 0) {
//			ih++;
//		}
//#sijapp cond.end#
    }

    public void addMenuItem(String text, byte action) {
        addMenuItem(text, null, action);
    }

//	public void addMenuItem(int text, byte action) {
//		addMenuItem(ResourceBundle.getString(text), null, action);
//	}
//
//	public void addMenuItem(int text, Icon icon, byte action) {
//		addMenuItem(ResourceBundle.getString(text), action, (icon == null) ? null : new Icon[] {icon});
//	}

    public void addMenuItem(String text, Icon icon, byte action) {
        addMenuItem(text, action, (icon == null) ? null : new Icon[]{icon});
    }

//	public void addMenuItem(int text, byte action, Icon icons[]) {
//		addMenuItem(ResourceBundle.getString(text), action, icons);
//	}

    public void addMenuItem(String text, byte action, Icon icons[]) {
        String sb = ResourceBundle.getString(text);
        synchronized (items) {
            int len = items.length;
            MenuItem mi[] = new MenuItem[len + 1];
            System.arraycopy(items, 0, mi, 0, len);
            mi[len] = new MenuItem(sb, icons, action);
            items = mi;
        }
    }

    public void clear() {
        currItem = 0;
        items = new MenuItem[0];
    }

    public MenuItem[] getItems() {
        return items;
    }

    public void setCurrent(int idx) {
        currItem = idx;
        currItem = Math.max(0, Math.min(idx, items.length - 1));
    }

    public int getCurrIndex() {
        return currItem;
    }

    public void setCurrentTop(int idx) {
        topItem = idx;
        topItem = Math.max(0, Math.min(idx, items.length - 1));
    }

    public int getCurrentTop() {
        return topItem;
    }

    public void setPrvScreen(CanvasEx c) {
        prvScreen = c;
    }

    public CanvasEx getPrvScreen() {
        return prvScreen;
    }

    public byte getCurrAction() {
        return items[currItem].action;
    }

    public void setMenuListener(MenuListener listener) {
        this.listener = listener;
    }

    public MenuListener getListener() {
        return listener;
    }

    protected int correctDeltaY(int dY, int height, int topY, int scHeight) {
        return dY /*- ih*/ - 1;
    }

    private void correctHeight(int count) {
        while ((topY = NativeCanvas.getHeightEx() - height - dY) < 0) {
            height -= items[--count].getHeight(facade);
        }
    }

    protected void afterShow() {
        startRepaintTTask();
    }

    public void beforeShow() {
        if (prvScreen instanceof Menu) {
            prvScreen.beforeShow();
        }
        width = 0;
        height = 1;
        topY = 0;
        int count = items.length;
        MenuItem mi;
        for (int i = count - 1; i >= 0; i--) {
            mi = items[i];
            width = Math.max(width, mi.getWidth(facade));
            height += mi.getHeight(facade);
        }
        //height = Math.min(height, NativeCanvas.getHeightEx() - dY);
        dY = /*ih +*/ 1 + getMenuBarHeight();
        int scHeightS = NativeCanvas.getHeightEx();
        int scHeight = scHeightS;
        if (prvScreen instanceof VirtualList) {
            scHeight = ((VirtualList) prvScreen).getBottom();
            dY += (scHeightS - scHeight);
        }
        correctHeight(count);
        dY = correctDeltaY(dY, height/* + ih*/, topY, scHeight) + 1/* + ih*/;
        correctHeight(count);
        width += getScrollWidth() + 4;
        int screenWidth = NativeCanvas.getWidthEx();
        if (!sideWasInit) {
            if (prvScreen instanceof VirtualList) {
                side = ((VirtualList) prvScreen).getMenuSide();
                screenWidth = ((VirtualList) prvScreen).getWidthInternal();
            } else if (prvScreen instanceof Menu) {
                side = ((Menu) prvScreen).getMenuSide();
                if (side != 0) {
                    side = (byte) -side;
                }
            }
        }
        if (side < 0) {
            x = 0;
        } else if (side > 0) {
            x = screenWidth - width - 1;
        } else {
            x = (screenWidth - width) / 2;
        }
        x = Math.max(0, x);
        //fX = x;
        menuTrans = (Options.getInt(Options.OPTION_MENU_TRANS) * 255) / 10;
        //menuScreenTrans = (Options.getInt(Options.OPTION_MENUSCR_TRANS)*255)/10;
        checkTopItem();
        if (Options.getBoolean(Options.OPTION_ANIMATION) && !animated) {
            //animated();
            //if (side <= 0) {
            //	x = -width;
            //} else {
            //	x = screenWidth + width;
            //}
            //startSlideTTask(side <= 0 ? 20 : -20);
            lock();
        }// else {
        //	prvScreen.unlock();
        //	startRepaintTTask();
        //}
    }

    public void beforeHide() {
    }

    public void animated() {
        animated = true;
    }

    public boolean slide(CanvasEx bgd) {
        if (!animated) {
            animated();
            int tx = side * width;
            int dx = -tx;
            int ty = (height + dY) * (1 - Math.abs(side));
            int dy = -ty;
            //prvScreen.lock();
            SliderTask st = new SliderTask(this, prvScreen, this, tx, ty, dx, dy, x, topY);
            Jimm.setDisplay(st);
            unlock();
            return false;
        }
        if ((bgd != null) && (!bgd.equals(prvScreen))) {
            animated = false;
            return super.slide(bgd);
        }
        return true;
    }

    // public void run() {
    // updateX(dX);
    // }

    // private void updateX(int i) {
    // boolean destroy = false, back = false;
    // x += i;
    // if (side <= 0) {
    // if (x >= fX) {
    // x = fX;
    // destroy = true;
    // } else if (x < -width && i < 0) {
    // back = true;
    // }
    // } else {
    // if (x <= fX) {
    // x = fX;
    // destroy = true;
    // } else if (x - fX > width && i > 0) {
    // back = true;
    // }
    // }
    // invalidate();
    // if (destroy || back) {
    // if (slide != null) {
    // slide.cancel();
    // slide = null;
    // }
    // if (back) {
    // if (NativeCanvas.isActive(this) && this.equals(Jimm.getCurrentDisplay())) {
    // Jimm.getTimerRef().schedule(
    // new TimerTask() {
    // public void run() {
    // Jimm.setDisplay(prvScreen);
    // }
    // }, 5
    // );
    // }
    // } else {
    // startRepaintTTask();
    // }
    // }
    // }

    // private void startSlideTTask(int i) {
    // if (slide != null) {
    // slide.cancel();
    // slide = null;
    // }
    // dX = i;
    // slide = new TimerTask() {
    // public void run() {
    // Jimm.getDisplay().callSerially(Menu.this);
    // }
    // };
    // Jimm.getTimerRef().schedule(slide, 5, 5);
    // }

    protected void startRepaintTTask() {
        try {
            Jimm.getTimerRef().schedule(new TimerTasks(this), TIMEOUT, TIMEOUT);
        } catch (Exception ignored) {
        }
    }

    protected byte getMenuSide() {
        return side;
    }

    public int getMenuBarHeight() {
        if (prvScreen == null) {
            return 0;
        }
        return prvScreen.getMenuBarHeight();
    }

    public Image paintOnImage() {
        Image img = null;
        if (animated) {
            img = Image.createImage(width + 1, height + 1);
        } else {
            animated();
        }
        return paintOnImage(img);
    }

    public Image paintOnImage(Image img) {
        Graphics g;
        if (img == null) {
            img = Image.createImage(NativeCanvas.getWidthEx(), NativeCanvas.getHeightEx());
            g = img.getGraphics();
        } else {
            g = img.getGraphics();
            g.translate(-x, -topY);
        }
        unlock();
        menuTrans = 0;
        paint(g);
        menuTrans = (Options.getInt(Options.OPTION_MENU_TRANS) * 255) / 10;
        lock();
        return img;
    }

    public void paint(Graphics g) {
        if (getLocked()) {
            return;
        }
        drawBackgroud(g);
        paint(g, -1, -1);
    }

    private void paint(Graphics g, int curX, int curY) {
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();
        int count = items.length;
        int y = topY;
        int height = topY + this.height;
        int x;
        int itemHeight;
        //int itemWidth;
        MenuItem mi;
        g.setClip(this.x, y, width - getScrollWidth(), this.height);
        int fontHeight = facade.getFontHeight();
        for (int i = topItem; i < count; i++) {
            x = this.x + 1;
            mi = items[i];
            itemHeight = mi.getHeight(facade);
            //itemWidth = mi.getWidth(facade);
            if (ptInRect(curX, curY, this.x, y, this.x + width, y + itemHeight)) {
                currItem = i;
                break;
            }
            g.setClip(x, y + 1, width, Math.min(itemHeight, height - y));
            if (i == currItem) {
                drawGradient(g, x, y + 1, width - 1 - getScrollWidth(), itemHeight - 1, getColor(COLOR_MCURSOR), 16, -48, 0);
                g.setColor(getColor(COLOR_MDCURSOR));
                g.drawRect(x, y + 1, width - 2 - getScrollWidth(), itemHeight - 1);
            }
            x += drawIcons(g, mi.icons, x, (2 * y + itemHeight + 2) / 2) + 3;
            g.setColor(getColor(COLOR_MENU_TEXT));
            drawString(g, facade, mi.text, (2 * x/* + (width - itemWidth) * (1 - Math.abs(side))*/) / 2, (2 * y + itemHeight - fontHeight + 2) / 2, Graphics.TOP | Graphics.LEFT);

            if ((y += itemHeight) > height) {
                break;
            }
        }
        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    private int getScrollWidth() {
        if (items.length - getVisCount() == 0) {
            return 0;
        }
        return scrollerWidth + 1;
    }

    private int drawIcons(Graphics g, Icon icons[], int x, int y) {
        if (icons == null) {
            return 0;
        }
        int x0 = x;
        int count = icons.length;
        Icon icon;
        for (int i = 0; i < count; i++) {
            icon = icons[i];
            if (icon == null) {
                continue;
            }
            icon.drawInVCenter(g, x + 1, y);
            x += icon.getWidth();
        }
        return (x - x0);
    }

    private void drawBackgroud(Graphics g) {
        if (prvScreen != null) {
            prvScreen.paint(g);
        }
//        if (prvScreen instanceof VirtualList) {
//            if (bgdImg != null) {
//				int wid = NativeCanvas.getWidthEx();
//				int hei = NativeCanvas.getHeightEx();
//				for(int x = 0, y; x < wid; x += 10) {
//					for(y = 0; y < hei; y += 10) {
//						g.drawImage(bgdImg, x, y, Graphics.TOP | Graphics.LEFT);
//					}
//				}
//			}
//		}
        if (menuTrans == 0) {
            drawRect(g, getColor(COLOR_MBACK1), getColor(COLOR_MBACK2), x, topY, width, height);
        } else {
            drawAlphaRect(g, getColor(COLOR_MBACK1), x, topY, x + width, topY + height/* + ih*/, 255 - menuTrans);
        }
        drawScroller(g, topItem, topY, getVisCount(), x + width - scrollerWidth, height, items.length, true);
        g.setColor(getColor(COLOR_TEXT));
        g.drawRect(x, topY, width, height);
        //if (NativeCanvas.getCanvas() != this || ih == 0) {
        //	return;
        //}
        //Icon icon = PopUp.icons.elementAt(0);
        //if (icon != null) {
        //	icon.drawByLeftTop(g, x + 1, topY + height + 1);
        //}
        //icon = PopUp.icons.elementAt(1);
        //if (icon != null) {
        //	icon.drawByRightTop(g, x + width - 1, topY + height + 1);
        //}
    }

    /*private Image mode(int c) {
        Image image = e().a(l(), m(), n(), o());
        switch (c) {
            case 51: // '3'
                return c(image);

            case 34: // '"'
                return b(image);

            default:
                return a(image);
        }
    }

    private static javax.microedition.lcdui.Image a(javax.microedition.lcdui.Image image) {
        int i = image.getWidth();
        int k = image.getHeight();
        int i1 = alpha_m.getWidth();
        int j1 = alpha_m.getHeight();
        javax.microedition.lcdui.Graphics g = image.getGraphics();
        for (int k1 = 0; k1 < k; k1 += j1) {
            for (int l1 = 0; l1 < i; l1 += i1)
                g.drawImage(alpha_m, l1, k1, 20);
        }
        g.setColor(0xff282b2e);
        g.drawRect(0, 0, i - 1, k - 1);
        g.setColor(0xffa5a7a8);
        g.drawRect(1, 1, i - 3, k - 3);
        return image;
    }

    public static void b(int i, int k, int i1) {
        try {
            javax.microedition.lcdui.Image image = javax.microedition.lcdui.Image.createImage(k, i1);
            for (int j2 = 0; j2 < i; j2++)
                c(image);

            for (int j3 = 0; j3 < i; j3++)
                b(image);
        } catch (Throwable ignored) {
        }
    }

    private static javax.microedition.lcdui.Image b(javax.microedition.lcdui.Image image) {
        int i = image.getWidth();
        int k = image.getHeight();
        int i1 = i * k;
        int j1 = i - 2;
        int ai[] = new int[i1];
        image.getRGB(ai, 0, i, 0, 0, i, k);
        int ai1[] = new int[i * k];
        int l2 = 2;
        for (int i3 = i << 1; l2 < k - 2; i3 += i) {
            int k1 = 0x1800000;
            int l1 = 0x18000;
            int i2 = 384;
            for (int j3 = 0; j3 < 5; j3++) {
                int j2 = ai[j3 + i3];
                k1 += j2 & 0xff0000;
                l1 += j2 & 0xff00;
                i2 += j2 & 0xff;
            }
            ai1[2 + i3] = k1 >>> 4 & 0xff0000 | l1 >>> 4 & 0xff00 | i2 >>> 4 & 0xff | 0xff000000;
            int k3 = 3;
            int l3 = i3 + 5;
            for (int i4 = i3; k3 < j1; i4++) {
                int k2 = ai[l3];
                k1 += k2 & 0xff0000;
                l1 += k2 & 0xff00;
                i2 += k2 & 0xff;
                k2 = ai[i4];
                k1 -= k2 & 0xff0000;
                l1 -= k2 & 0xff00;
                i2 -= k2 & 0xff;
                ai1[k3 + i3] = k1 >>> 4 & 0xff0000 | l1 >>> 4 & 0xff00 | i2 >>> 4 & 0xff | 0xff000000;
                k3++;
                l3++;
            }
            l2++;
        }
        a(ai1, i, k, i1);
        return javax.microedition.lcdui.Image.createRGBImage(ai1, i, k, true);
    }

    private static javax.microedition.lcdui.Image c(javax.microedition.lcdui.Image image) {
        int i = image.getWidth();
        int k = image.getHeight();
        int i1 = i * k;
        int j1 = i - 2;
        int ai[] = new int[i1];
        image.getRGB(ai, 0, i, 0, 0, i, k);
        int ai1[] = new int[i * k];
        int i4 = 0;
        for (int j4 = 0; i4 < k; j4 += i) {
            int k1 = 0x100000;
            int i2 = 4096;
            int k2 = 16;
            for (int l4 = 0; l4 < 5; l4++) {
                int i3 = ai[l4 + j4];
                k1 += i3 & 0xff0000;
                i2 += i3 & 0xff00;
                k2 += i3 & 0xff;
            }
            ai1[2 + j4] = k1 >>> 3 & 0xff0000 | i2 >>> 3 & 0xff00 | k2 >>> 3 & 0xff;
            int i5 = 3;
            int k5 = j4 + 5;
            for (int j6 = j4; i5 < j1; j6++) {
                int j3 = ai[k5];
                k1 += j3 & 0xff0000;
                i2 += j3 & 0xff00;
                k2 += j3 & 0xff;
                j3 = ai[j6];
                k1 -= j3 & 0xff0000;
                i2 -= j3 & 0xff00;
                k2 -= j3 & 0xff;
                ai1[i5 + j4] = k1 >>> 3 & 0xff0000 | i2 >>> 3 & 0xff00 | k2 >>> 3 & 0xff;
                i5++;
                k5++;
            }

            i4++;
        }
        i4 = 5 * i;
        int k4 = (k - 2) * i;
        for (int j5 = 2; j5 < i - 2; j5++) {
            int l1 = 0x100000;
            int j2 = 4096;
            int l2 = 16;
            for (int l5 = 0; l5 < i4; l5 += i) {
                int k3 = ai1[j5 + l5];
                l1 += k3 & 0xff0000;
                j2 += k3 & 0xff00;
                l2 += k3 & 0xff;
            }
            ai[(i << 1) + j5] = l1 >>> 3 & 0xff0000 | j2 >>> 3 & 0xff00 | l2 >>> 3 & 0xff | 0xff000000;
            int i6 = 3 * i;
            int k6 = j5 + i4;
            for (int l6 = j5; i6 < k4; l6 += i) {
                int l3 = ai1[k6];
                l1 += l3 & 0xff0000;
                j2 += l3 & 0xff00;
                l2 += l3 & 0xff;
                l3 = ai1[l6];
                l1 -= l3 & 0xff0000;
                j2 -= l3 & 0xff00;
                l2 -= l3 & 0xff;
                ai[j5 + i6] = l1 >>> 3 & 0xff0000 | j2 >>> 3 & 0xff00 | l2 >>> 3 & 0xff | 0xff000000;
                i6 += i;
                k6 += i;
            }

        }
        a(ai, i, k, i1);
        return javax.microedition.lcdui.Image.createRGBImage(ai, i, k, true);
    }

    private static void a(int ai[], int i, int k, int i1) {
        int j1 = 9;
        int k1 = 9;
        int l1 = 9 + i;
        int i2 = (i1 - i) + 9;
        for (int k2 = i2 - i; j1 < i - 9; k2++) {
            ai[k1] = 0xff282b2e;
            ai[l1] = 0xffa5a7a8;
            ai[i2] = 0xff282b2e;
            ai[k2] = 0xffa5a7a8;
            j1++;
            k1++;
            l1++;
            i2++;
        }
        j1 = 9;
        k1 = 9 * i;
        l1 = k1 + 1;
        i2 = (k1 + i) - 1;
        for (int l2 = i2 - 1; j1 < k - 9; l2 += i) {
            ai[k1] = 0xff282b2e;
            ai[l1] = 0xffa5a7a8;
            ai[i2] = 0xff282b2e;
            ai[l2] = 0xffa5a7a8;
            j1++;
            k1 += i;
            l1 += i;
            i2 += i;
        }
        j1 = 0;
        k1 = 0;
        l1 = 0;
        for (int j2 = i1 - i; j1 < 9; j2 -= i) {
            int i3 = 0;
            for (int j3 = i - 1; i3 < 9; j3--) {
                int k3 = corn[i3 + k1];
                if ((k3 & 0xc0c0c0c0) != 0xc0c0c0c0) {
                    ai[i3 + l1] = k3;
                    ai[j3 + l1] = k3;
                    ai[i3 + j2] = k3;
                    ai[j3 + j2] = k3;
                }
                i3++;
            }
            j1++;
            k1 += 9;
            l1 += i;
        }
    }*/

//    private void b(javax.microedition.lcdui.Graphics g) {
//        int i;
//        int k;
//        synchronized (this) {
//            i = N;
//            k = O;
//        }
//        int i1 = 0;
//        for (int j1 = i; i1 < k; j1++) {
//            a(g, j1, i1);
//            i1++;
//        }
//
//    }
//
//    private void a(javax.microedition.lcdui.Graphics g, int i, int k) {
//        int i1;
//        int j1;
//        int k1;
//        int l1;
//        synchronized (this) {
//            i1 = H;
//            j1 = K;
//            k1 = M;
//            l1 = I + 2;
//        }
//        int i2 = k * R + d + 2;
//        int j2 = l();
//        int k2 = m() + i2;
//        boolean flag = i == k1;
//        g.setClip(j2, k2, j1, R + 1);
//        if (flag) {
//            int l2 = (j2 + j1) - 1;
//            int i3 = 0;
//            for (int k3 = k2; i3 < R; k3++) {
//                g.setColor(T[i3]);
//                g.drawLine(j2 + 2, k3, l2 - 2, k3);
//                i3++;
//            }
//
//            g.setColor(0xff282b2e);
//            g.drawLine(j2, k2, j2, k2 + R);
//            g.drawLine(l2, k2, l2, k2 + R);
//            g.setColor(0xffa5a7a8);
//            g.drawLine(j2 + 1, k2, j2 + 1, k2 + R);
//            g.drawLine(l2 - 1, k2, l2 - 1, k2 + R);
//        } else {
//            g.drawRegion(z, 0, i2, j1, R, 0, j2, k2, 20);
//        }
//        ud ud1 = (ud) E.elementAt(i);
//        if (ud1 != null) {
//            if (!flag)
//                g.setColor(ud1.b() ? 0xeeeeee : 0x888888);
//            else
//                g.setColor(0x222222);
//            t.a(g, 0);
//            g.setClip(j2, k2, l1, R);
//            if (b())
//                g.drawString(ud1.toString(), (j2 + l1) - 2, k2 + S, 24);
//            else
//                g.drawString(ud1.toString(), j2 + 2 + 2, k2 + S, 20);
//            java.lang.String s = ud1.a();
//            g.setClip(j2, k2, j1, R);
//            if (s.length() > 0) {
//                int l3 = k2 + (R - X.getHeight() >> 1);
//                boolean flag1 = ud1.b();
//                if (b()) {
//                    int j4 = j2 + 2 + 1;
//                    for (int l4 = s.length() - 1; l4 >= 0; l4--) {
//                        od.a(g, s.charAt(l4), flag1, j4, l3);
//                        j4 += 0 + X.getWidth();
//                    }
//
//                } else {
//                    int k4 = (j2 + j1) - 2 - 1;
//                    for (int i5 = s.length() - 1; i5 >= 0; i5--) {
//                        k4 -= X.getWidth();
//                        od.a(g, s.charAt(i5), flag1, k4, l3);
//                        k4 += 0;
//                    }
//
//                }
//            }
//        } else {
//            int j3 = j2 + (j1 - i1 >> 1);
//            int i4 = (j3 + i1) - 1;
//            k2 += R - 2 >> 1;
//            g.setColor(0xff282b2e);
//            g.drawLine(j3, k2, i4, k2);
//            k2++;
//            g.setColor(0xffa5a7a8);
//            g.drawLine(j3, k2, i4, k2);
//        }
//    }
//
//    private void c(javax.microedition.lcdui.Graphics g) {
//        int i;
//        int k;
//        int i1;
//        int j1;
//        synchronized (this) {
//            i1 = K;
//            j1 = L;
//            i = N;
//            k = O;
//        }
//        int k1 = i1 - c >> 1;
//        byte byte0 = 2;
//        int l1 = j1 - d - 2;
//        int i2 = l() + k1;
//        int j2 = m() + byte0;
//        int k2 = m() + l1;
//        a(g);
//        g.drawRegion(z, k1, ((int) (byte0)), c, d, 0, i2, j2, 20);
//        g.drawRegion(z, k1, l1, c, d, 0, i2, k2, 20);
//        if (i > 0)
//            g.drawImage(a, i2, j2, 20);
//        if (i + k < E.size())
//            g.drawImage(b, i2, k2, 20);
//    }
//
//    private final void d(javax.microedition.lcdui.Graphics g) {
//        a(g);
//        int i;
//        int k;
//        synchronized (this) {
//            i = K;
//            k = L;
//        }
//        if (C != 17)
//            g.drawImage(V, l() + i, m(), 24);
//        g.drawRegion(z, 0, 0, i, d + 2, 0, l(), m(), 20);
//        int i1 = k - (d + 2);
//        if (C != 17)
//            g.drawImage(W, l() + i, m() + k, 40);
//        g.drawRegion(z, 0, i1, i, d + 2, 0, l(), m() + i1, 20);
//    }


    private int getVisCount() {
        int size = items.length;
        int y = 0;
        int counter = 0, i;
        int topItem = this.topItem;

        if (size == 0) {
            return 0;
        }

        if (topItem < 0) {
            topItem = 0;
        } else if (topItem >= size) {
            topItem = size - 1;
        }

        for (i = topItem; i < size - 1; i++) {
            y += items[i].getHeight(facade);
            if (y > height) {
                return counter;
            }
            counter++;
        }
        y = height;
        counter = 0;
        for (i = size - 1; i >= 0; i--) {
            y -= items[i].getHeight(facade);
            if (y < 0) {
                break;
            }
            counter++;
        }
        return counter;
    }

    private void checkTopItem() {
        int size = items.length;
        int visCount = getVisCount();

        if (size == 0) {
            topItem = 0;
            return;
        }

        if (currItem >= (topItem + visCount - 1)) {
            topItem = currItem - visCount + 1;
        } else if (currItem < topItem) {
            topItem = currItem;
        }
        if ((size - topItem) <= visCount) {
            topItem = (size > visCount) ? (size - visCount) : 0;
        }
        if (topItem < 0) {
            topItem = 0;
        }
    }

    public void back() {
        if (Options.getBoolean(Options.OPTION_ANIMATION)) {
            //startSlideTTask(side <= 0 ? -20 : 20);
            int tx = 0;
            int dx = side * width;
            int ty = 0;
            int dy = (height + dY) * (1 - Math.abs(side));
            Jimm.setDisplay(new SliderTask(this, prvScreen, prvScreen, tx, ty, dx, dy, x, topY));
        } else {
            Jimm.setDisplay(prvScreen);
        }
        //bgdImg = null;
    }

    protected void clearAction() {
        //int i = 0;
        //i++;
    }

    protected void select() {
        if (listener != null) {
            listener.menuSelect(this, items[currItem].action);
        }
    }

    //#sijapp cond.if target is "MIDP2"#
    public void pointerPressed(int x, int y) {
        isDraggedWas = false;
        lastPointerYCrd = y;
        lastPointerTopItem = topItem;
// #sijapp cond.if modules_TOUCH2 is "true" #
        pointerReleased(x, y);
// #sijapp cond.end#
    }

    public void pointerDragged(int x, int y) {
        isDraggedWas = true;
        int itemCount = items.length;
        int visCount = getVisCount();
        if (itemCount == visCount) {
            return;
        }
        topItem = lastPointerTopItem + ((visCount << 1) * (-y + lastPointerYCrd)) / height;
        if (topItem < 0) topItem = 0;
        if (topItem > (itemCount - visCount)) topItem = itemCount - visCount;
        invalidate();
    }

    public void pointerReleased(int x, int y) {
        if (isDraggedWas || lastPointerYCrd == -1) {
            return;
        }
        lastPointerYCrd = -1;
        if (ptInRect(x, y, this.x, topY, this.x + width, topY + height - 1)) {
            paint(getGraphics(), x, y);
            select();
            return;
        }
        if (ptInRect(x, y, this.x + width, topY + height - 1, this.x + width / 2, NativeCanvas.getHeightEx())) {
            select();
            return;
        }
        back();
    }
//#sijapp cond.end#

    public void doKeyreaction(int keyCode, int type) {
        int ga = getExtendedGameAction(keyCode);
        if (type != KEY_RELEASED) {
            switch (ga) {
                case Canvas.UP:
                    if (--currItem < 0) {
                        currItem = items.length - 1;
                    }
                    break;

                case Canvas.DOWN:
                    currItem = ++currItem % items.length;
                    break;
            }

            switch (keyCode) {
                case Canvas.KEY_NUM1:
                case Canvas.KEY_NUM7:
                    if (type != KEY_REPEATED) {
                        currItem = (keyCode == Canvas.KEY_NUM1) ? 0 : Math.max(0, items.length - 1);
                    }
                    break;

                case Canvas.KEY_NUM3:
                case Canvas.KEY_NUM9:
                    if (type != KEY_REPEATED) {
                        int i = (keyCode == Canvas.KEY_NUM3) ? -1 : 1;
                        currItem = Math.min(items.length - 1, Math.max(topItem + getVisCount() * i, 0));
                    }
                    break;

                case Canvas.KEY_STAR:
                    Jimm.setDisplay(new Alert(null, items[currItem].text, null, null));
                    return;
            }
            checkTopItem();
            invalidate();
            return;
        }

        switch (ga) {
            case Canvas.FIRE:
            case Canvas.RIGHT:
            case KEY_CODE_LEFT_MENU:
                select();
                return;

            case Canvas.LEFT:
            case KEY_CODE_RIGHT_MENU:
            case KEY_CODE_BACK_BUTTON:
                back();
                return;

            case -8:
                clearAction();
                return;
        }
    }
}