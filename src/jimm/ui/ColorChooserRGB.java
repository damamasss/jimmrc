//package jimm.ui;
//
//import DrawControls.*;
//
//import javax.microedition.lcdui.Image;
//import javax.microedition.lcdui.Graphics;
//import javax.microedition.lcdui.Canvas;
//
//import jimm.Options;
//import jimm.Jimm;
//import jimm.util.ResourceBundle;
//
//public class ColorChooserEx extends CanvasEx {
//
//    private static final int CUBE_SIZE = 128;
//    private static final int CUBE_STEP = 2;
//
//    CanvasEx prvScreen;
//    TextList paintList;
//    int[] cube;
//    int cubeSize = CUBE_SIZE;
//    int cubeStep = CUBE_STEP;
//    int curX;
//    int curY;
//    int x;
//    int y;
//    int w;
//    int h;
//
//    public ColorChooserEx(Object prvScreen, int x, int y) {
//        super();
//        if (!(prvScreen instanceof CanvasEx || prvScreen == null)) {
//            return;
//        }
//        if (prvScreen instanceof SliderTask) {
//            prvScreen = ((SliderTask) prvScreen).getNextScreen();
//        }
//        this.prvScreen = (CanvasEx) prvScreen;
//        this.x = x;
//        this.y = y;
//        if (NativeCanvas.getWidthEx() > CUBE_SIZE * 2) {
//            if (NativeCanvas.getHeightEx() > CUBE_SIZE * 2) {
//                cubeSize = CUBE_SIZE * 2;
//                cubeStep = 1;
//            }
//        }
//        w = cubeSize + 4;
//        h = cubeSize + 4;
//        curX = 0;
//        curY = cubeSize;
//        if (Options.getBoolean(Options.OPTION_ANIMATION)) {
//            lock();
//        }
//        //Image img = Image.createImage(cubeSize, cubeSize);
//        //img.getGraphics().drawRGB(getCube(), 0, cubeSize, 0, 0, cubeSize, cubeSize, false);
//        cube = getCube();
//    }
//
//    public void beforeHide() {
//    }
//
//    protected void afterShow() {
//        //startRepaintTTask();
//    }
//
//    public Image paintOnImage() {
//        Image img = Image.createImage(w, h);
//        Graphics g = img.getGraphics();
//        g.translate(-x, -y);
//        unlock();
//        paint(g);
//        lock();
//        return img;
//    }
//
//    public boolean slide(CanvasEx bgd) {
//        if (!animated) {
//            Jimm.setDisplay(new SliderTask(this, prvScreen, this, -w - x, 0, w + x, 0, x, y));
//            unlock();
//            return false;
//        }
//        return true;
//    }
//
//    public CanvasEx getPrvScreen() {
//        return prvScreen;
//    }
//
//    public int getY() {
//        return y;
//    }
//
//    public int getHeight() {
//        return h;
//    }
//
//    public int getWidth() {
//        return w;
//    }
//
//    public int getColor() {
//        int idx = curY * cubeSize + curX;
//        return cube[idx];
//    }
//
//    //#sijapp cond.if target is "MIDP2"#
//    public void pointerReleased(int x, int y) {
//        if (x > this.x + 2 & x < this.x + this.w - 4) {
//            if (y > this.y + 2) {
//                if (y < this.y + this.h - 5) {
//                    curX = x - this.x;
//                    curY = y - this.y;
//                    invalidate();
//                    return;
//                }
//                if (y < this.y + this.h + Math.max(facade.getFontHeight() + 4, Options.getInt(Options.OPTION_ICONS_CANVAS))) {
//                    doKeyreaction(KEY_CODE_LEFT_MENU, KEY_PRESSED);
//                    return;
//                }
//
//            }
//        }
//        doKeyreaction(KEY_CODE_RIGHT_MENU, KEY_PRESSED);
//        //super.pointerReleased(x, y);
//    }
//
////    public void pointerDragged(int x, int y) {
////    }
////
////    public void pointerReleased(int x, int y) {
////    }
//    //#sijapp cond.end#
//
//    public void paint(Graphics g) {
//        if (prvScreen != null) {
//            prvScreen.paint(g);
//        }
//        drawGlassRect(g, getColor(COLOR_CAP), x, y, x + w, y + h);
//        if (cube != null) {
//            g.drawRGB(cube, 0, cubeSize, x + 2, y + 2, cubeSize, cubeSize, false);
//        }
//        if (!NativeCanvas.getCanvas().equals(this)) {
//            return;
//        }
//        int dx = x + 2 + curX;
//        int dy = y + 2 + curY;
//        g.setColor((curY < h / 2) ? 0x000000 : 0xffffff);
//        g.drawLine(dx - 3, dy, dx + 3, dy);
//        g.drawLine(dx, dy - 3, dx, dy + 3);
//        g.drawRect(dx - 5, dy - 5, 10, 10);
//        g.setColor(0xffffff);
//        String s = "00000" + Integer.toHexString(getColor()).toUpperCase();
//        s = s.substring(s.length() - 6);
//        drawString(g, facade, s, x + w / 2, y + h - 4, Graphics.BOTTOM | Graphics.HCENTER);
//        drawGlassRect(g, getColor(COLOR_CAP), x, y + h, x + w, y + h + Math.max(facade.getFontHeight() + 4, Options.getInt(Options.OPTION_ICONS_CANVAS)));
//        g.setColor(0xffffff);
//        drawString(g, facade, ResourceBundle.getString("apply_scheme"), x + w / 2, y + h + Math.max(facade.getFontHeight() + 4, Options.getInt(Options.OPTION_ICONS_CANVAS)) / 2, Graphics.VCENTER | Graphics.HCENTER);
//    }
//
//    public int[] getCube() {
//        int w = cubeSize;
//        int h = cubeSize;
//        int[] mass = new int[w * h];
//        int color, off, uni;
//        for (int y = 0; y < h / 2; y++) {
//            int lim = 0xff;// - y*cubeStep*2;
//            int white = 0xff - y * cubeStep * 2;
//            off = y * h;
//            for (int x = 0; x < w; x++) {
//                color = 0x000000;
//                uni = w / 6;
//                if (x < uni) {
//                    color |= Math.min(white + lim, 0xff) << 16;//red
//                    color |= Math.min(white + (lim * x / uni), 0xff) << 8;//green
//                    color |= white;//blue
//                } else if (x < 2 * uni) {
//                    color |= Math.min(white + (lim * (2 * uni - x) / uni), 0xff) << 16;//red
//                    color |= Math.min(white + lim, 0xff) << 8;//green
//                    color |= white;//blue
//                } else if (x < 3 * uni) {
//                    color |= white << 16;//red
//                    color |= Math.min(white + lim, 0xff) << 8;//green
//                    color |= Math.min(white + (lim * (x - 2 * uni) / uni), 0xff);//blue
//                } else if (x < 4 * uni) {
//                    color |= white << 16;//red
//                    color |= Math.min(white + (lim * (4 * uni - x) / uni), 0xff) << 8;//green
//                    color |= Math.min(white + lim, 0xff);//blue
//                } else if (x < 5 * uni) {
//                    color |= Math.min(white + (lim * (x - 4 * uni) / uni), 0xff) << 16;//red
//                    color |= white << 8;//green
//                    color |= Math.min(white + lim, 0xff);//blue
//                } else {
//                    color |= Math.min(white + lim, 0xff) << 16;//red
//                    color |= white << 8;//green
//                    color |= Math.min(white + (lim * (w - x) / uni), 0xff);//blue
//                }
//                mass[off + x] = color;
////                for (int y = 0; y < h; y++) {
////                    mass[x + y * w] = color;
////                }
//            }
//        }
//        for (int y = h / 2; y < h; y++) {
//            int lim = Math.max(0xff - (y - h / 2) * cubeStep * 2, 0);
//            off = y * h;
//            for (int x = 0; x < w; x++) {
//                color = 0x000000;
//                uni = w / 6;
//                if (x < uni) {
//                    color |= lim << 16;//red
//                    color |= (lim * x / uni) << 8;//green
//                } else if (x < 2 * uni) {
//                    color |= (lim * (2 * uni - x) / uni) << 16;//red
//                    color |= lim << 8;//green
//                } else if (x < 3 * uni) {
//                    color |= lim << 8;//green
//                    color |= (lim * (x - 2 * uni) / uni);//blue
//                } else if (x < 4 * uni) {
//                    color |= (lim * (4 * uni - x) / uni) << 8;//green
//                    color |= lim;//blue
//                } else if (x < 5 * uni) {
//                    color |= (lim * (x - 4 * uni) / uni) << 16;//red
//                    color |= lim;//blue
//                } else {
//                    color |= lim << 16;//red
//                    color |= Math.min(lim * (w - x) / uni, 0xff);//blue
//                }
//                mass[off + x] = color;
////                for (int y = 0; y < h; y++) {
////                    mass[x + y * w] = color;
////                }
//            }
//        }
//        int gray;
//        for (int y = 0; y < 10; y++) {
//            off = y * h;
//            for (int x = 0; x < w; x++) {
//                gray = x * cubeStep;
//                color = 0x000000;
//                color |= gray << 16;//red
//                color |= gray << 8;//green
//                color |= gray;//blue
//                mass[off + x] = color;
//            }
//        }
//        return mass;
//    }
//
////    public int[] getCubeFunny() {
////        int w = cubeSize;
////        int h = cubeSize;
////        int[] mass = new int[w*h];
////        int color, off, uni;
////        for (int y = 0; y < h/2; y++) {        // todo выпуклые узоры
////            int plus = 0xff - y*cubeStep;
////            off = y * h;
////            for (int x = 0; x < w; x++) {
////                color = 0x000000;
////                uni = w/6;
////                if (x < uni) {
////                    color |= plus << 16;//red
////                    color |= (plus + plus * x / uni) << 8;//green
////                } else if (x < 2*uni) {
////                    color |= (plus + plus * (2*uni - x)/uni) << 16;//red
////                    color |= plus << 8;//green
////                } else if (x < 3*uni) {
////                    color |= plus << 8;//green
////                    color |= (plus + plus * (x - 2*uni)/uni);//blue
////                } else if (x < 4*uni) {
////                    color |= (plus + plus * (4*uni - x)/uni) << 8;//green
////                    color |= plus;//blue
////                } else if (x < 5*uni) {
////                    color |= (plus + plus * (x - 4*uni)/uni) << 16;//red
////                    color |= plus;//blue
////                } else {
////                    color |= plus << 16;//red
////                    color |= (plus + plus * (w - x)/uni);//blue
////                }
////                mass[off + x] = color;
//////                for (int y = 0; y < h; y++) {
//////                    mass[x + y * w] = color;
//////                }
////            }
////        }
////        for (int y = h/2; y < h; y++) { // todo плавные солнечные градиенты
////            int lim = 0xff - y*cubeStep;
////            off = y * h;
////            for (int x = 0; x < w; x++) {
////                color = 0x000000;
////                uni = w/6;
////                if (x < uni) {
////                    color |= lim << 16;//red
////                    color |= (lim * x / uni) << 8;//green
////                } else if (x < 2*uni) {
////                    color |= (lim * (2*uni - x)/uni) << 16;//red
////                    color |= lim << 8;//green
////                } else if (x < 3*uni) {
////                    color |= lim << 8;//green
////                    color |= (lim * (x - 2*uni)/uni);//blue
////                } else if (x < 4*uni) {
////                    color |= (lim * (4*uni - x)/uni) << 8;//green
////                    color |= lim;//blue
////                } else if (x < 5*uni) {
////                    color |= (lim * (x - 4*uni)/uni) << 16;//red
////                    color |= lim;//blue
////                } else {
////                    color |= lim << 16;//red
////                    color |= (lim * (w - x)/uni);//blue
////                }
////                mass[off + x] = color;
//////                for (int y = 0; y < h; y++) {
//////                    mass[x + y * w] = color;
//////                }
////            }
////        }
////        return mass;
////    }
//
//    long press = -1;
//
//    public void doKeyreaction(int keyCode, int type) {
//        byte off = 2;
//        if (type == KEY_PRESSED) {
//            press = System.currentTimeMillis();
//        } else if (type == KEY_REPEATED) {
//            if (System.currentTimeMillis() - press > 2500) {
//                off = 4;
//            } else if (System.currentTimeMillis() - press > 5000) {
//                off = 8;
//            }
//        }
//        int ga = getExtendedGameAction(keyCode);
//        switch (ga) {
//            case Canvas.UP:
//                curY -= off;
//                if (curY < 0) {
//                    curY = cubeSize - 1;
//                }
//                invalidate();
//                return;
//
//            case Canvas.DOWN:
//                curY += off;
//                if (curY > cubeSize - 1) {
//                    curY = 0;
//                }
//                invalidate();
//                return;
//
//            case Canvas.LEFT:
//                curX -= off;
//                if (curX < 0) {
//                    curX = cubeSize;
//                }
//                invalidate();
//                return;
//
//            case Canvas.RIGHT:
//                curX += off;
//                if (curX > cubeSize - 1) {
//                    curX = 0;
//                }
//                invalidate();
//                return;
//
//            case Canvas.FIRE:
//            case KEY_CODE_LEFT_MENU:
//                Options.getOptionsForm().colorChoosed(getColor(), false);
//                break;
//        }
//        if (Options.getBoolean(Options.OPTION_ANIMATION)) {
//            Jimm.setDisplay(new SliderTask(this, prvScreen, prvScreen, 0, 0, -w - x, 0, x, y));
//        } else {
//            Jimm.setDisplay(prvScreen);
//        }
//    }
//}
