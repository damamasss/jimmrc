package jimm.ui;

import DrawControls.CanvasEx;
import DrawControls.NativeCanvas;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.TimerTasks;
import jimm.comm.Action;
import jimm.util.ResourceBundle;
import jimm.util.Device;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.util.Timer;
import java.util.TimerTask;

public class LPCanvas extends CanvasEx {
    /**
     * @author Rishat Shamsutdinov
     */

    class LPCanvasMove extends TimerTask {

        private final LPCanvas lp;
        int slowed;

        private LPCanvasMove(LPCanvas l) {
            lp = l;
        }

        public final void run() {
            if (check()) {
                lp.current++;
            }
            lp.invalidate();
//            if (needhide) {
//                lp.current+=4;
//                if (current >= 100) {
//                    NativeCanvas.hideLPCanvas();
//                    this.cancel();
//                }
//            }
        }

        private synchronized boolean check() {
            //off--;
            //if (off < 0) off = image.getWidth();
            off = (off + 1) % image.getWidth();
            boolean slow = false;
            if (current <= dist + 20) {
                slowed++;
            }
            if (slowed >= 20) {
                slowed = 0;
                slow = true;
            }
            return (dist > 0 && (current <= dist || slow));
        }
    }

    private final static Timer aniTimer = new Timer();
    private TimerTasks timerTask;
    private LPCanvasMove lpcm;

    public int current;
    int off;
    int dist;
    int h;
    private String message;
    private Image image;
    //private boolean needhide;

    public LPCanvas() {
        current = 0;
        dist = 0;
        message = null;
        try {
            image = Image.createImage("/progress.png");
        } catch (Exception ignored) {
        }
        h = getHeight();
        if (image != null) {
            lpcm = new LPCanvasMove(this);
            aniTimer.schedule(lpcm, 40, 40);
        }
    }

    public void addTimerTask(String lngStr, Action action) {
        resetTimerTask();
        listener = null;
        timerTask = new TimerTasks(action);
        setMessage(ResourceBundle.getString(lngStr));
        show();
        Jimm.getTimerRef().schedule(timerTask, 500, 500);
    }

    public void resetTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void resetAnimationask() {
        if (lpcm != null) {
            lpcm.cancel();
            lpcm = null;
        }
    }

    public Action getAction() {
        if (timerTask == null) {
            return null;
        }
        return timerTask.getAction();
    }

    public void cancel() {
        boolean b = true;
        if (listener != null) {
            listener.commandAction(JimmUI.cmdCancel, null);
            listener = null;
        } else if (timerTask != null) {
            b = timerTask.onCancel();
            timerTask = null;
        }
        if (b) {
            NativeCanvas.hideLPCanvas();
        }
    }

    public void setMessage(String str) {
        message = str;
    }

    public void setProgress(int perc, boolean line) {
        if (image == null || line) {
            current = perc;
            invalidate();
        } else {
            dist = perc;
        }
    }

    public void show() {
        unlock();
    }

    public void doKeyreaction(int keyCode, int type) {
    }

    //#sijapp cond.if target is "MIDP2"#
    public void pointerPressed(int x, int y) {
        if (y >= NativeCanvas.getHeightEx()) {
            cancel();
        }
    }
//#sijapp cond.end#

    private CommandListener listener;

    public void setCmdListener(CommandListener cLir) {
        listener = cLir;
    }

    public int getHeight() {
        int height = facade.getFontHeight();
        if (image != null) {
            height = Math.max(image.getHeight(), height);
        }
        return height;
    }

    public void paint(Graphics g) {
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();
        int height = h;
        int width = NativeCanvas.getWidthEx();
        int y = NativeCanvas.getHeightEx();
        int prwid = width * current / 100;
        g.setClip(0, y, width, height);
        drawGradient(g, 0, y, width, height, getColor(COLOR_SPLASH), 16, -48, 8);
        g.setClip(0, y, prwid, height);
        if (image != null) {
            int imgWidth = image.getWidth();
            for (int i = -imgWidth + off; i < prwid; i += imgWidth) {
                g.drawImage(image, i, y + height / 2, Graphics.VCENTER | Graphics.LEFT);
            }
//            for (int i = prwid; i > -imgWidth; i -= imgWidth) {
//                g.drawImage(image, i, y + height / 2, Graphics.VCENTER | Graphics.LEFT);
//            }
        } else {
            drawGlassRect(g, getColor(COLOR_SPL_PRGS), 0, y, prwid, y + height);
        }
        if (message != null) {
            int color = getColor(COLOR_SPL_TEXT);
            g.setColor(color);
            g.setClip(0, y, width, height);
            drawString(g, facade, message, width / 2, y + (height - facade.getFontHeight()) / 2, Graphics.TOP | Graphics.HCENTER);
        }
        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }
}