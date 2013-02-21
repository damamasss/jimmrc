/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/jimm/Jimm.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import DrawControls.*;
import jimm.comm.Icq;
import jimm.comm.XStatus;
//#sijapp cond.if modules_FILES is "true"#
import jimm.files.FileSystem;
//#sijapp cond.end#
import jimm.ui.PopUp;
import jimm.ui.Select;
import jimm.ui.SelectListener;
import jimm.util.ResourceBundle;
import jimm.util.Device;
import jimm.chat.ChatTextList;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
//#sijapp cond.if modules_FILES is "true"#
import java.io.InputStream;
//#sijapp cond.end#
import java.util.Timer;

public class Jimm extends MIDlet implements SelectListener {

    public boolean isStarted = false;
    public static Jimm jimm;
    private static Object currentScreen = null;
    private static Object prvScreen = null;
    private static Display display;   
    private static Timer timer = new Timer();
    private static SplashCanvas sc;
    private static Device device = new Device();
    private ContactList cl;

    static public boolean isTouch() {
        return device.avaiblePhone(Device.TOUCH);
    }

    static public boolean is_phone_SE() {
        return device.avaiblePhone(Device.PHONE_SE);
    }

    static public boolean is_phone_NOKIA() {
        return device.avaiblePhone(Device.PHONE_NOKIA);
    }

    static public boolean is_smart_NOKIA() {
        return device.avaiblePhone(Device.SMART_NOKIA);
    }

    static public boolean is_smart_SE() {
        return device.avaiblePhone(Device.SMART_SE);
    }

    public void startApp() throws MIDletStateChangeException {
        if (isStarted && !is_phone_SE()) return;
        if (Jimm.jimm != null) {
            showWorkScreen();
            return;
        }
        Jimm.jimm = this;
        init();
        Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.UI_UPDATE), 30000, 30000);
        Options.manifest();
        (new EnterPassword(sc, cl)).showPasswordForm(Options.getBoolean(Options.OPTION_AUTO_CONNECT));
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        Profiles.disconnect();
        Profiles.save();
        Jimm.setDisplay(null);
        notifyDestroyed();
    }

    public void autorun(int sleep) throws MIDletStateChangeException {
        long wake;
        try {
            wake = (new java.util.Date()).getTime() + sleep;
            javax.microedition.io.PushRegistry.registerAlarm("jimm.Jimm", wake);
        } catch (Exception ignored) {
        }
        destroyApp(false);
    }

    private void init() {
        new Options();
        new JimmUI();
        Image skin = null;
//#sijapp cond.if modules_FILES is "true"#
        if (Options.getString(Options.OPTION_SKIN_PATH).indexOf('/') == 0) {
            skin = loadImageFromFS(Options.getString(Options.OPTION_SKIN_PATH));
        }
//#sijapp cond.end#
        if (skin == null) {
            try {
                skin = Image.createImage("/" + Options.getString(Options.OPTION_SKIN_PATH));
            } catch (Exception ignored) {
            } catch (OutOfMemoryError ignored) {
            }
        }
//#sijapp cond.if modules_GFONT is "true"#
        if (CanvasEx.updateFont(Options.getString(Options.OPTION_GFONT_PATH))) {
            CanvasEx.updateFont();
        }
//#sijapp cond.end#
        CanvasEx.loadCS(skin);
        DrawControls.VirtualList.setBackGroundImage(skin);
        if (!Options.getString(Options.OPTION_ICONS_PREFIX).equals("/")) {
            ContactList.updateIcons();
        }
//#sijapp cond.if modules_SBOLTUN is "true"#
        if (Options.getBoolean(Options.OPTION_SBOLTUN)) {
            ChatTextList.sBoltunInit();
        }
//#sijapp cond.end#
        //currentScreen = (sc = new SplashCanvas(ResourceBundle.getString("loading")));
        Icon.scale = Options.getInt(Options.OPTION_ICONS_CANVAS);
        //currentScreen = (sc = new SplashCanvas("CanvasEx.class"));
        currentScreen = (sc = new SplashCanvas());
        //sc.setMessage("Display.class");
        setDisplay(sc);
        Jimm.display = Display.getDisplay(this);
        sc.setProgress(15);
//#sijapp cond.if modules_SMILES is "true" #
        //sc.setMessage("Emotions.class");
        new Emotions();
        sc.setProgress(25);
// #sijapp cond.end#
//        if (Options.getBoolean(Options.OPTION_ACTIVITY_MENU)) {
//            try {new ActivityListener(this);} catch(Throwable t) {}
//        }
// #sijapp cond.if modules_HISTORY is "true" #
        //sc.setMessage("HistoryStorage.class");
        new HistoryStorage();
        sc.setProgress(30);
// #sijapp cond.end#
        //sc.setMessage("ClientID.class");
        new jimm.comm.ClientID();
        sc.setProgress(50);
        //sc.setMessage("XStatus.class");
        XStatus.init();
        sc.setProgress(60);
        /*****/
        //sc.setMessage("ContactList.class");
        cl = new ContactList();
        //sc.setMessage("Profiles.class");
        Profile defProf = new Profile(Options.getString(Options.OPTION_MY_NICK));
        Profiles.putProfile(defProf);
        cl.setProfile(defProf);
        DrawControls.VirtualList.updateParams();
        sc.setProgress(80);
        cl.beforeConnect();
        cl.rebuild();
        JimmUI.setColorScheme();
        //sc.setMessage("Profiles.class");
        sc.setProgress(100);
    }

    public static boolean locked() {
        return sc != null && sc.isLocked;
    }
        

    public static void messageAvailable() {
        if (sc == null) {
            return;
        }
        sc.availableMessages++;
        sc.invalidate();
    }

    static public void showWorkScreen() {
        if (Jimm.locked()) {
            setDisplay(getSplashCanvasRef());
        } else {
            getContactList().activate();
        }
    }

    static public void setMinimized(boolean mini) {
// #sijapp cond.if target is "MIDP2" #
        if (mini) {
            setDisplayable(null);
        } else {
            Displayable disp = getDisplay().getCurrent();
            if ((disp == null) || (!disp.isShown())) {
                showWorkScreen();
            }
        }
// #sijapp cond.end #
    }

    public static void doExit(boolean anyway) {
        if (!anyway) {
            String qest = (Profiles.getUnreadMessCount() > 0) ? ResourceBundle.getString("have_unread_mess") : ResourceBundle.getString("close_programm");
            setDisplay(new Select(getContactList(), ResourceBundle.getString("notice"), qest, jimm, 0, null));
        } else {
            try {
                jimm.destroyApp(true);
                //jimm.autorun();
            } catch (MIDletStateChangeException ignored) {
            }
        }
    }

    public void selectAction(int action, int selectType, Object o) {
        switch (selectType) {
            case Select.SELECT_OK:
                doExit(true);
                break;
        }
    }

    public static String getVersion() {
        return "###VERSION###";
    }

    public static Icq getIcqRef() {
        return getCurrentProfile().getIcq();
    }

    static public Timer getTimerRef() {
        return timer;
    }

    public static SplashCanvas getSplashCanvasRef() {
        if (sc == null) {
            sc = new SplashCanvas();
            //setStatusesToDraw(JimmUI.getStatusImageIndex(getCurrentProfile().getInt(Profile.OPTION_ONLINE_STATUS)),
                    //XStatus.getStatusImage(getCurrentProfile().getInt(Profile.OPTION_XSTATUS)));
        }
        return (sc);
    }

    public static ContactList getContactList() {
        return jimm.cl;
    }

    public static Profile getCurrentProfile() {
        return getContactList().getProfile();
    }

    static public Object getCurrentDisplay() {
        return currentScreen;
    }

    public static Display getDisplay() {
        if (display == null) {
            display = Display.getDisplay(jimm);
        }
        return display;
    }

    public static Object getPrevScreen() {
        return prvScreen;
    }

    static public synchronized void setDisplay(Object d) {
        try {
            setDisplayEx(d);
        } catch (Throwable t) {
            System.gc();
            try {
                setDisplayEx(d);
            } catch (Throwable ignored) {
            }
        }
    }

    static public synchronized void setDisplayEx(Object d) {
        if (d instanceof CanvasEx) {
            currentScreen = d;
            NativeCanvas.activate((CanvasEx) d);
            if (!(d instanceof SplashCanvas)) {
                sc = null;
            }
        } else if (d instanceof Alert && currentScreen instanceof CanvasEx) {
            Alert alert = (Alert) d;
            CanvasEx c = (CanvasEx) currentScreen;
            int y = 10;
            int x = 4;
            int h = -1;
            if (c instanceof VirtualList) {
                y = Math.max(y, ((VirtualList) c).getCapHeight());
                h = ((VirtualList) c).getHeightInternal();
            }
            if (!is_phone_SE()) {
                x = Math.max(x, Options.getInt(Options.OPTION_CAPTION_SHIFT));
            }
// #sijapp cond.if target is "MIDP2" #
            if (Options.getBoolean(Options.OPTION_BRING_UP)) {
                Jimm.setMinimized(false);
            }
// #sijapp cond.end #
            setDisplay(new PopUp(c, alert.getString(), c.getDrawWidth() - Options.getInt(Options.OPTION_CAPTION_SHIFT), h, x, y));
        } else if ((d == null) || (d instanceof Displayable)) {
            currentScreen = d;
            setDisplayable((Displayable) d);
            if (d instanceof Form) {
//#sijapp cond.if modules_ANISMILES is "true" #
                AniImageList.startAnimation();
//#sijapp cond.elseif modules_GIFSMILES is "true" #
//#				GifImageList.startAnimation();
//#sijapp cond.end #
            }
        }
    }

    static public void setDisplayable(Displayable d) {
        getDisplay().setCurrent(d);
    }

    public static void setPrevScreen(Object screen) {
        prvScreen = screen;
    }

    public static void back() {
        if (prvScreen.equals(getCurrentDisplay())) {
            setDisplay(getContactList());
        } else {
            setDisplay(getPrevScreen());
        }
    }

    // #sijapp cond.if modules_FILES="true"#
    public Image loadImageFromFS(String res) {
        Image img = null;
        FileSystem fs = null;
        InputStream InStr;
        try {
            fs = FileSystem.getInstance();
            fs.readFile(res);
            InStr = fs.openInputStream();
            img = Image.createImage(InStr);
            if (InStr != null) {
                InStr.close();
            }
        } catch (Exception ignored) {
        } catch (OutOfMemoryError ignored) {
        }
        fs.close();
        return img;
    }
    //#sijapp cond.end #    
}