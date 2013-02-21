package jimm.ui;

import java.util.*;
import javax.microedition.lcdui.*;

import DrawControls.*;
import jimm.*;

public class SliderTask extends CanvasEx implements Runnable {
    /**
     * @author Rishat Shamsutdinov
     */
    public static final int PERIOD = 7;
    public static final int COUNT = 9;

    private final static Timer sliderTimer = new Timer();

    private TimerTask slider;
    private CanvasEx bgdScreen;
    private Image scrImg;
    private CanvasEx nextScreen;
    private int shiftX, shiftY, transY, transX;
    private int count = COUNT;
    private boolean stop;
    private int ftx, fty, x, y;

    public SliderTask(CanvasEx screen, CanvasEx bgdScreen, CanvasEx nextScreen, int tx, int ty, int dx, int dy, int x, int y) {
        try {
            scrImg = screen.paintOnImage();
        } catch (OutOfMemoryError oome) {
            count = 0;
        }
        this.bgdScreen = bgdScreen;
        this.nextScreen = nextScreen;
        if (nextScreen instanceof ContactList && screen != nextScreen) {
            dx += dx / COUNT;
            dy += dy / COUNT;
        }
        shiftX = (-dx / COUNT);
        shiftY = (-dy / COUNT);
        transX = tx;
        transY = ty;
        ftx = tx + dx;
        fty = ty + dy;
        this.x = x;
        this.y = y;
    }

    public SliderTask(CanvasEx screen, CanvasEx bgdScreen, CanvasEx nextScreen, int tx, int dx) {
        this(screen, bgdScreen, nextScreen, tx, 0, dx, 0, 0, 0);
    }

    public CanvasEx getNextScreen() {
        return nextScreen;
    }

    public String getLeftName() {
        if (bgdScreen != null && bgdScreen.leftMenu != null) {
            return bgdScreen.leftMenu.getLabel();
        }
        return null;
    }

    public String getRightName() {
        if (bgdScreen != null && bgdScreen.rightMenu != null) {
            return bgdScreen.rightMenu.getLabel();
        }
        return null;
    }

    private synchronized void startSliding() {
        if (slider != null) {
            return;
        }
        slider = new TimerTask() {
            public void run() {
                Jimm.getDisplay().callSerially(SliderTask.this);
            }
        };
        sliderTimer.schedule(slider, PERIOD, PERIOD);
//        lock();
//        transX -= shiftX * COUNT / 2;
//        transY -= shiftY * COUNT / 2;
//        shiftX = shiftX / 2;
//        shiftY = shiftY / 2;
//        unlock();
    }

    private synchronized boolean check() {
        //System.out.println("TRS: "+transX+"; "+transY + " -> " + count);
        return (transX * (transX - shiftX) < 0 || transY * (transY - shiftY) < 0 || count-- <= 0);
    }

    public void run() {
        lock();
        if (check()) {
            transX = ftx;
            transY = fty;
            stop = true;
            if (slider != null) {
                slider.cancel();
                slider = null;
            }
        } else {
            transX -= (shiftX);
            transY -= (shiftY);
//            if (shiftX > 1) shiftX--;
//            else if (shiftX < -1) shiftX++;
//            if (shiftY > 1) shiftY--;
//            else if (shiftY < -1) shiftY++;
        }
        unlock();
    }

    private void switchScreen() {
        Jimm.setDisplay(nextScreen);
        nextScreen.unlock();
    }

    public void beforeShow() {
        startSliding();
    }

    public void paint(Graphics g) {
        if (bgdScreen != null) {
            bgdScreen.paint(g);
        }
        if (scrImg != null) {
            g.drawImage(scrImg, x + transX, y + transY, Graphics.LEFT | Graphics.TOP);
        }
        if (stop) {
            stop = false;
            switchScreen();
        }
    }

    public void doKeyreaction(int keyCode, int type) {
    }
}