/*
 * GifImageList.java
 *
 * Created on 4 Апрель 2008 г., 18:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package DrawControls;

import jimm.chat.ChatTextList;
import jimm.JimmException;

import java.io.InputStream;
import java.util.Vector;
// #sijapp cond.if modules_ANISMILES is "true" #

/**
 * @author vladimir
 */
public class AniImageList extends ImageList implements Runnable {

    //#sijapp cond.if (modules_GIFSMILES is "true" | modules_ANISMILES is "true")#
    protected static int ANI_NONE = 0;
    protected static int ANI_CHAT = 1;
    protected static int ANI_CEX = 2;
//#sijapp cond.end#

    private AniIcon[] icons;
    private Thread thread;
    private static final Object wait = new Object();

    //! Return image by index
    public Icon elementAt(int index) { //!< Index of requested image in the list
        if (index < size() && index >= 0) {
            return icons[index];
        }
        return null;
    }

    public int size() {
        return icons != null ? icons.length : 0;
    }

    /**
     * Creates a new instance of GifImageList
     */
    public AniImageList() {
    }

    private String getSmileFile(String resName, int i) {
        return resName + "/" + (i + 1) + ".png";
    }

    public void load(String resName, int w, int h) {
        //Vector tmpIcons = new Vector();
        try {
            InputStream is = getClass().getResourceAsStream(resName + "/animate.bin");
            int smileCount = is.read();

            icons = new AniIcon[smileCount];
            ImageList imgs = new ImageList();
            AniIcon icon;
            for (int smileNum = 0; smileNum < smileCount; smileNum++) {
                int imageCount = is.read();
                int frameCount = is.read();
                imgs.load(getSmileFile(resName, smileNum), imageCount);
                boolean loaded = (0 < imgs.size());
                icon = loaded ? new AniIcon(imgs.elementAt(0), frameCount) : null;
                for (int frameNum = 0; frameNum < frameCount; frameNum++) {
                    int iconIndex = is.read();
                    int delay = is.read() * TIME;
                    if (loaded) {
                        icon.addFrame(frameNum, imgs.elementAt(iconIndex), delay);
                    }
                }
                icons[smileNum] = icon;
                if (loaded) {
                    width = Math.max(width, icon.getWidth());
                    height = Math.max(height, icon.getHeight());
                }
            }
        } catch (Exception ignored) {
        }
        if (size() > 0) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private static void waitAni() {
        try {
            synchronized (wait) {
                if (firstSleep) {
                    firstSleep = false;
                    wait.wait(500L);
                } else {
                    wait.wait();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void startAnimation() {
        try {
            synchronized (wait) {
                firstSleep = true;
                wait.notify();
            }
        } catch (Exception ignored) {
        }
    }

    private long time;
    private static final int TIME = 100;
    private static boolean firstSleep = true;

    public void run() {
        time = System.currentTimeMillis();
        Object screen;
        ChatTextList chat;
        while (true) {
            try {
                Thread.sleep(TIME);
            } catch (Exception ignored) {
            }
            long newTime = System.currentTimeMillis();
            screen = jimm.Jimm.getCurrentDisplay();
            chat = jimm.chat.ChatHistory.currentChat;
            int aniWork = getAnimationWork(screen, chat);
            if (aniWork != ANI_NONE) {
                boolean update = false;
                for (int i = size() - 1; i >= 0; i--)
                    if (icons[i] != null) {
                        update |= icons[i].nextFrame(newTime - time);
                    }
                if (update) {
                    if (aniWork == ANI_CHAT) {
                        chat.invalidate();
                    } else {
                        ((CanvasEx) screen).invalidate();
                    }
                }
            }
            time = newTime;
            if (aniWork == ANI_NONE) {
                waitAni();
            }
            //screen = null;
            //chat = null;
        }
    }

    private static int getAnimationWork(Object screen, jimm.chat.ChatTextList chat) {
        if ((chat != null) && (chat.isActive())) {
            return ANI_CHAT;
        }
        if (screen != null) {
            boolean animationWorked = false;
            animationWorked |= (screen instanceof jimm.ui.Selector);
            animationWorked |= jimm.HistoryStorage.ready4Ani();
            if (screen instanceof jimm.ui.PopUp) {
                return getAnimationWork(((jimm.ui.PopUp) screen).getPrvScreen(), null);
            }
            if (screen instanceof jimm.ui.Menu) {
                return getAnimationWork(((jimm.ui.Menu) screen).getPrvScreen(), null);
            }
            if (animationWorked) {
                return ANI_CEX;
            }
        }
        return ANI_NONE;
    }
}
// #sijapp cond.end #
