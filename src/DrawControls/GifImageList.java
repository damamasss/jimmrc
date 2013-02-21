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

import java.util.Vector;
// #sijapp cond.if modules_GIFSMILES is "true" #

/**
 * @author vladimir
 */
public class GifImageList extends ImageList implements Runnable {

    //#sijapp cond.if (modules_GIFSMILES is "true" | modules_ANISMILES is "true")#
    protected static int ANI_NONE = 0;
    protected static int ANI_CHAT = 1;
    protected static int ANI_CEX = 2;
//#sijapp cond.end#

    private GifIcon[] icons;
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
    public GifImageList() {
    }

    private String getSmileFile(String resName, int i) {
        return resName + "/" + (i + 1) + ".gif";
//        final int delta = ((int)'z' - (int)'a') + 1;
//        final int startChar = 'a';
//        char ch1 = (char)(startChar + i % delta);
//        char ch2 = (char)(startChar + i / delta);
//        return resName + "/" + ch2 + "" + ch1 + ".gif";
    }

    private static final int MAX_ICONS = 100;

    public void load(String resName, int w, int h) {
        Vector tmpIcons = new Vector();
        try {
            GifDecoder gd;
            for (int i = 0; i < MAX_ICONS; i++) {
                gd = new GifDecoder();
                if (GifDecoder.STATUS_OK != gd.read(getSmileFile(resName, i))) {
                    break;
                }
                tmpIcons.addElement(new GifIcon(gd));
                width = Math.max(width, gd.getImage().getWidth());
                height = Math.max(height, gd.getImage().getHeight());
            }
        } catch (Exception ignored) {
        }
        //System.out.println(width + "x" + height);
        icons = new GifIcon[tmpIcons.size()];
        tmpIcons.copyInto(icons);
        if (size() > 0) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private static void waitAni() {
        try {
            synchronized (wait) {
                wait.wait();
            }
        } catch (Exception ignored) {
        }
    }

    public static void startAnimation() {
        try {
            synchronized (wait) {
                wait.notify();
            }
        } catch (Exception ignored) {
        }
    }

    private long time;
    private static final int TIME = 100;

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
        int result = ANI_NONE;
        if ((chat != null) && (chat.isActive())) {
            result = ANI_CHAT;
        } else if (screen != null) {
            boolean animationWorked = false;
            animationWorked |= (screen instanceof jimm.ui.Selector);
            animationWorked |= jimm.HistoryStorage.ready4Ani();
            animationWorked |= (screen instanceof jimm.ui.PopUp);
            animationWorked |= (screen instanceof jimm.ui.Menu);
            if (animationWorked) {
                result = ANI_CEX;
            }
        }
        return result;
    }
}
// #sijapp cond.end #
