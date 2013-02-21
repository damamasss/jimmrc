/*******************************************************************************
 ********************************************************************************
 ********************************************************************************
 File: src/DrawControls/NativeCanvas.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Shamsutdinov Rishat
 ********************************************************************************
 ********************************************************************************
 *******************************************************************************/
package DrawControls;

import jimm.Jimm;
import jimm.Options;
import jimm.SplashCanvas;
import jimm.TimerTasks;
import jimm.comm.Action;
import jimm.ui.LPCanvas;
import jimm.ui.Menu;
import jimm.ui.PopupItem;
import jimm.ui.SliderTask;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
//import javax.microedition.lcdui.game.GameCanvas;
import java.util.Timer;
import java.util.TimerTask;

public class NativeCanvas extends Canvas implements Runnable {

    private static CanvasEx canvas = null;
    private static LPCanvas lpCanvas = null;
    private static int starPressed = -1;

    private static final NativeCanvas _this = new NativeCanvas();

    static private Image bDIimage = null;

    //protected NativeCanvas() {
    //    super(false); //GameCanvas
    //}

    public static NativeCanvas getInst() {
        return _this;
    }

    public static void Repaint() {
        _this.repaint();
    }

    public static CanvasEx getCanvas() {
        return canvas;
    }

    public static LPCanvas getLPCanvas() {
        if (lpCanvas == null) {
            lpCanvas = new LPCanvas();
            boolean fl = (canvas instanceof Menu);
            if (fl) {
                canvas.lock();
                canvas.beforeShow();
            }
            if (fl) {
                canvas.unlock();
            }
        }
        return lpCanvas;
    }

    public static Action getLPAction() {
        if (lpCanvas == null) {
            return null;
        }
        return lpCanvas.getAction();
    }

    public static void hideLPCanvas() {
        if (lpCanvas == null) {
            return;
        }
        lpCanvas.resetAnimationask();
        lpCanvas = null;
        if (canvas != null && !(canvas instanceof SliderTask)) {
            canvas.beforeShow();
        }
        Repaint();
    }

    public String getKeyName(int key) {
        try {
            return super.getKeyName(key);
        } catch (Exception e) {
            return String.valueOf(key);
        }
    }

    public static String getLeftName() {
        if (canvas != null && canvas.leftMenu != null) {
            return canvas.leftMenu.getLabel();
        } else if (canvas instanceof SliderTask) {
            return ((SliderTask) canvas).getLeftName();
        }
        return null;
    }

    public static String getRightName() {
        if (canvas != null && canvas.rightMenu != null) {
            return canvas.rightMenu.getLabel();
        } else if (canvas instanceof SliderTask) {
            return ((SliderTask) canvas).getRightName();
        }
        return null;
    }

    //#sijapp cond.if modules_PANEL is "true"#
    public static void addAction(String name, Icon icon) {
        PopupItem.addItem(name, icon);
//        if (lpCanvas != null) {
//            return;
//        }
//        getLPCanvas().addTimerTask(name, icon, delay);
    }
//#sijapp cond.end#


    protected void showNotify() {
        cancelKeyRepeatTask();
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        setFullScreenMode(true);
//#sijapp cond.end#
//		updateParams();
//#sijapp cond.if modules_ANISMILES is "true" #
        AniImageList.startAnimation();
//#sijapp cond.elseif modules_GIFSMILES is "true" #
//#		GifImageList.startAnimation();
//#sijapp cond.end #
    }

    protected void hideNotify() {
        cancelKeyRepeatTask();
    }
    // для бокового экрана
    //private static Graphics gs = null;
    //private static Sprite ss = null;

    protected void sizeChanged(int w, int h) {
        if (canvas != null) {
            canvas.sizeChanged(w, h);
        }
    }

    public void paint(Graphics g) {
        if (isDoubleBuffered()) {
            paintAllOnGraphics(g);
        } else {
            try {
                if (bDIimage == null) {
                    bDIimage = Image.createImage(getWidthEx(), getHeightEx());
                }
                paintAllOnGraphics(bDIimage.getGraphics());
                g.drawImage(bDIimage, 0, 0, Graphics.TOP | Graphics.LEFT);
            } catch (Exception e) {
                System.out.println("Error in repainting grom NC: " + e.toString());
                Repaint();
            } catch (OutOfMemoryError oome) {
                System.out.println("Out of memory grom NC: " + oome.toString());
                System.gc();
                Repaint();
            }
        }
    }

//    public byte[] screenShot() {   // TODO SCREENSHOT RAW
//        int w = getWidth();
//        int h = getHeight();
//        Image b2 = Image.createImage(w, h);
//        final Graphics g = b2.getGraphics();
//        synchronized (canvas) {
//            if (canvas != null) {
//                try {
//                    canvas.paint(g);
//                    if (lpCanvas != null) {
//                        lpCanvas.paint(g);
//                    }
////#sijapp cond.if modules_PANEL is "true"#
//                    PopupItem.paintItems(g);
////#sijapp cond.end#
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            }
//        }
//        int[] source = new int[w * h];
//        b2.getRGB(source, 0, w, 0, 0, w, h);
//        byte[] out = new byte[source.length * 3];
//        int pixel;
//        for (int i = 0; i < source.length; i++) {
//            pixel = source[i];
//            for (int p = 0; p < 3; p++)
//                out[i * 3 + p] = (byte) ((pixel >> ((2-p) * 8)) & 0xFF);
//        }
//        return out;
//    }

    private void paintAllOnGraphics(Graphics g) {
        try {
            if (canvas != null) {
                canvas.paint(g);
                if (lpCanvas != null) {
                    lpCanvas.paint(g);
                }
                //#sijapp cond.if modules_PANEL is "true"#
                PopupItem.paintItems(g);
                //#sijapp cond.end#
            }
        } catch (Exception e) {
            Repaint();
        } catch (OutOfMemoryError oome) {
            System.gc();
            Repaint();
        }
    }

    private Timer repeatTimer = new Timer();
    private TimerTask slideShort;
    private TimerTask timerTask;
    private int lastKeyCode;

    protected void keyPressed(int keyCode) {
        if (isShown()) {
            //#sijapp cond.if modules_LIGHT is "true"#
            LightEx.flash(true);
            //#sijapp cond.end#
            doKeyReaction(keyCode, CanvasEx.KEY_PRESSED);
            cancelKeyRepeatTask();
            cancelSlideShort();
            lastKeyCode = keyCode;
            if (canvas != null) {
                if (canvas instanceof SliderTask)
                    ((SliderTask) canvas).getNextScreen().isPressed = true;
                else
                    canvas.isPressed = true;
            }
            timerTask = new TimerTask() {
                public void run() {
                    try {
                        Jimm.getDisplay().callSerially(NativeCanvas.this);
                    } catch (Exception ignored) {
                    }
                }
            };
            try {
                repeatTimer.schedule(timerTask, 500, 75);
            } catch (Exception ignored) {
            }
        }
    }

    public void cancelKeyRepeatTask() {
        lastKeyCode = 0;
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = null;
    }

    protected void keyReleased(int keyCode) {
        //#sijapp cond.if modules_LIGHT is "true"#
        LightEx.flash(false);
        //#sijapp cond.end#
        cancelKeyRepeatTask();
        jimm.comm.Icq.keyPressed();
        TimerTasks.keyPressed();
        startSlideShort();
        if (canvas != null && canvas.isPressed) {
            doKeyReaction(keyCode, CanvasEx.KEY_RELEASED);
            canvas.isPressed = false;
        }
    }

    protected void keyRepeated(int keyCode) {
        //#sijapp cond.if modules_LIGHT is "true"#
        LightEx.flash(true);
        //#sijapp cond.end#
        jimm.comm.Icq.keyPressed();
        TimerTasks.keyPressed();
    }

    protected void doKeyReaction(int keyCode, int type) {
        if (canvas != null) {
            if (lpCanvas != null) {
                if ((keyCode == Canvas.KEY_STAR) && (type == CanvasEx.KEY_RELEASED)) {
                    if (starPressed++ == 0) {
                        lpCanvas.cancel();
                        starPressed = -1;
                    }
                    return;
                }
                starPressed = Math.max(-1, --starPressed);
            }
            canvas.doKeyreaction(keyCode, type);
        }
    }

    public void run() {
        if (timerTask == null) {
            return;
        }
        doKeyReaction(lastKeyCode, CanvasEx.KEY_REPEATED);
    }

//#sijapp cond.if target is "MIDP2"#
    protected void pointerDragged(int x, int y) {
        if (canvas != null) {
            canvas.pointerDragged(x, y);
        }
    }

    protected void pointerPressed(int x, int y) {
        jimm.comm.Icq.keyPressed();
        TimerTasks.keyPressed();
        if (canvas != null) {
            if (lpCanvas != null) {
                lpCanvas.pointerPressed(x, y);
            }
            canvas.pointerPressed(x, y);
        }
    }

    protected void pointerReleased(int x, int y) {
        jimm.comm.Icq.keyPressed();
        TimerTasks.keyPressed();
        if (canvas != null) {
            canvas.pointerReleased(x, y);
        }
        startSlideShort();
    }
    //#sijapp cond.end#

    public int getExtendedGameAction(int code, boolean catchSoft) {
// #sijapp cond.if target isnot "SIEMENS2"#
        if (code != -10)
//#sijapp cond.end#
        {
            try {
                int gameAct = _this.getGameAction(code);
                if ((gameAct == Canvas.UP) || (gameAct == Canvas.DOWN) || (gameAct == Canvas.LEFT) || (gameAct == Canvas.RIGHT))
                    return gameAct;
                if (code == 0x5FFF0041) return Canvas.DOWN;
                if (code == 0x5FFF003F) return Canvas.UP;
            } catch (Exception ignored) {
            }
        }

        if (!catchSoft) {
            try {
                int gameAct = _this.getGameAction(code);
                if (gameAct > 0) return gameAct;
            } catch (Exception ignored) {
            }
            return 1000004;
        }
//#sijapp cond.if target is "MIDP2"#
        if (Jimm.is_phone_SE() && code == -22) {
            return 1000004;
        }
//#sijapp cond.end#
//#sijapp cond.if target_name is "Sagem_light"#
//#		int leftMenu = CanvasEx.KEY_CODE_RIGHT_MENU;
//#		int rightMenu = CanvasEx.KEY_CODE_LEFT_MENU;
//#sijapp cond.else#
        int leftMenu = CanvasEx.KEY_CODE_LEFT_MENU;
        int rightMenu = CanvasEx.KEY_CODE_RIGHT_MENU;
//#sijapp cond.end#
        int back = CanvasEx.KEY_CODE_BACK_BUTTON;
        String strCode = null;

        try {
            strCode = _this.getKeyName(code).toLowerCase();
        } catch (Exception ignored) {
        }

        if (strCode != null) {
            if ("soft1".equals(strCode) || "soft 1".equals(strCode) || "soft_1".equals(strCode)
                    || "softkey 1".equals(strCode) || "sk2(left)".equals(strCode) || strCode.startsWith("left soft")) {
                return leftMenu;
            }

            if ("soft2".equals(strCode) || "soft 2".equals(strCode) || "soft_2".equals(strCode)
                    || "softkey 4".equals(strCode) || "sk1(right)".equals(strCode) || strCode.startsWith("right soft")) {
                return rightMenu;
            }
        }

        if ((code == leftMenu) ||
//#sijapp cond.if target is "MIDP2" #
                (code == -6) ||
//#sijapp cond.end #
                (code == -21) || (code == 21) || (code == 105)
                || (code == -202) || (code == 113) || (code == 57345)
                || (code == -1)) {
            return leftMenu;
        }

        if ((code == rightMenu) ||
//#sijapp cond.if target is "MIDP2" #
                (code == -7) ||
//#sijapp cond.end #
                (code == -22) || (code == 22) || (code == 106)
                || (code == -203) || (code == 112) || (code == 57346)
                || (code == -4)) {
            return rightMenu;
        }
//#sijapp cond.if target isnot "SIEMENS2"#
        if (code == -11)
//#sijapp cond.else #
            if (code == -12)
//#sijapp cond.end #
            {
                return back;
            }

// #sijapp cond.if target isnot "SIEMENS2"#
        if (code != -10)
//#sijapp cond.end#
        {
            try {
                int gameAct = _this.getGameAction(code);
                if (gameAct > 0) return gameAct;
            } catch (Exception ignored) {
            }
        }

        return 1000004;
    }

    public static void activate(CanvasEx c) {
        _this.cancelKeyRepeatTask();

//#sijapp cond.if modules_ANISMILES is "true" #
        AniImageList.startAnimation();
//#sijapp cond.elseif modules_GIFSMILES is "true" #
//#		GifImageList.startAnimation();
//#sijapp cond.end #
        boolean animate = Options.getBoolean(Options.OPTION_ANIMATION);
        animate &= (canvas != null && !(canvas instanceof SplashCanvas || canvas instanceof SliderTask || c instanceof SliderTask || c instanceof SplashCanvas));
        animate &= (Jimm.getCurrentDisplay() instanceof CanvasEx && _this.isShown());
        animate &= (c != canvas);
        c.beforeShow();
        if (canvas != null) {
            canvas.beforeHide();
            if (c.getPriority() == -1) {
                c.setPriority(canvas.getPriority() + 1);
            }
			if (c != canvas) {
				canvas.isPressed = false;
			}
        } else {
            c.setPriority(0);
        }
        if (!animate) {
            canvas = null;
            canvas = c;
        }

        if (!(Jimm.getCurrentDisplay() instanceof CanvasEx && _this.isShown())) {
            Jimm.setDisplayable(_this);
            c.beforeShow();
        } else if (!animate) {
            Repaint();
        } else {
            if (c.slide(canvas)) {
                canvas = null;
                canvas = c;
                Repaint();
            }
        }
        canvas.afterShow();
    }

    public void setFullScreen(boolean full) {
        setFullScreenMode(full);
    }

    static public synchronized void invalidate(CanvasEx c) {
        if (c == null) {
            return;
        }
        if (canvas == c || c instanceof LPCanvas
                //#sijapp cond.if modules_PANEL is "true"#
                || c instanceof PopupItem
            //#sijapp cond.end#
                ) {
            _this.repaint();
        }
    }

    static public synchronized void invalidate(CanvasEx c, int x, int y, int w, int h) {
        if (c == null) {
            return;
        }
        if (canvas == c || c instanceof LPCanvas
                //#sijapp cond.if modules_PANEL is "true"#
                || c instanceof PopupItem
            //#sijapp cond.end#
                ) {
            _this.repaint(x, y, w, h);
        }
    }

    public static boolean isActive(Object c) {
        if ((c == null) || !(c instanceof CanvasEx)) {
            return false;
        }
        CanvasEx can = (CanvasEx) c;
        return ((canvas == can) && (_this.isShown()));
    }

    public static int getHeightEx() {
        return (lpCanvas == null) ? _this.getHeight() : _this.getHeight() - lpCanvas.getHeight();
    }

    public static int getWidthEx() {
        return _this.getWidth();
    }

//    public static boolean comboKeys(int key) {
//        int gameAction = getInst().getExtendedGameAction(key, true);
//        return (Options.getBoolean(Options.OPTION_COMBO_KEYS) &&
//                ((gameAction == Canvas.UP ||
//                        gameAction == Canvas.DOWN ||
//                        gameAction == Canvas.LEFT ||
//                        gameAction == Canvas.RIGHT ||
//                        gameAction == Canvas.FIRE ||
//                        gameAction == CanvasEx.KEY_CODE_LEFT_MENU ||
//                        gameAction == CanvasEx.KEY_CODE_RIGHT_MENU ||
//                        gameAction == CanvasEx.KEY_CODE_BACK_BUTTON ||
//                        key == Canvas.KEY_NUM3 ||
//                        key == Canvas.KEY_NUM1 ||
//                        key == Canvas.KEY_NUM7 ||
//                        key == Canvas.KEY_NUM9 ||
//                        key == Canvas.KEY_NUM0 ||
//                        key == Canvas.KEY_STAR)));
//    }

    private void startSlideShort() {
        if (canvas != null & canvas instanceof VirtualTree) {
            cancelSlideShort();
            repaint();
            String s = null;
            try {
                s = canvas.getCurrentString();
            } catch (Exception ignored) {
            }
            if (canvas.getLocked()) {
                s = null;
                canvas.setTextOff(-1);
            }
//            if (s != null & CanvasEx.getFacade().stringWidth(s) < getWidthEx()) {
//                s = null;
//            }
            if (s != null && s.length() >= 1) {
                slideShort = new SlideShort(this);
                try {
                    repeatTimer.schedule(slideShort, 2000, 100);
                } catch (IllegalStateException ignored) {
                }
            }
        }
    }

    private void cancelSlideShort() {
        if (slideShort != null) {
            slideShort.cancel();
            slideShort = null;
            if (canvas != null) {
                canvas.setTextOff(-1);
            }
        }
    }

    static void stopSlideShort(NativeCanvas nc) {
        nc.cancelSlideShort();
    }

    final class SlideShort extends TimerTask {
        private byte bytes;
        private final NativeCanvas nct;

        SlideShort(NativeCanvas nc) {
            nct = nc;
            bytes = 0;
        }

        public final void run() {
            if (NativeCanvas.getCanvas().getLocked() || bytes++ == 10) {
                NativeCanvas.stopSlideShort(nct);
            }
            if (bytes < 10) {
                NativeCanvas.getCanvas().setTextOff(1);
                nct.repaint();
                //Repaint();
            }
        }
    }
}

