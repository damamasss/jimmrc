//#sijapp cond.if modules_TUNER is "true"#
package jimm.plus;
/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.CanvasEx;

import java.io.IOException;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.amms.control.tuner.*;

import jimm.Jimm;
import jimm.JimmUI;
import jimm.JimmException;
import jimm.ui.MenuListener;
import jimm.ui.Menu;


public final class TunerRadio implements CommandListener, MenuListener, PlayerListener {

    public static TunerRadio tuner;

    public static void tunerStart() {
        if (tuner == null) {
            tuner = new TunerRadio();
        } else {
            tuner.textList.activate();
        }
    }

    public static void tunerBack() {
        if (tuner.radioPlayer == null) {
            tuner = null;
        }
        Jimm.getContactList().activate();
    }

    private final static byte MENU_SEARCH_UP = (byte) 0;
    private final static byte MENU_SEARCH_DOWN = (byte) 1;
    private final static byte MENU_SWITCH_FM = (byte) 2;
    private final static byte MENU_SWITCH_AM = (byte) 3;
    private final static byte MENU_STOP = (byte) 4;

    final static private String msgBusy = "Sorry, busy with previous request ...";
    final static private String msgSearchUp = "Searching up";
    final static private String msgSearchDown = "Searching down";
    final static private String msgSwitchToFM = "Switching to FM";
    final static private String msgSwitchToAM = "Switching to AM";

    private Player radioPlayer;
    private TunerControl tunerControl;
    private RDSControl rdsControl;
    private int freq;
    private String modulation;
    private Thread toRun;
    private String toSay;
    private TextList textList;

    public TunerRadio() {
        freq = 910000; //91.0 MHz
        modulation = TunerControl.MODULATION_FM;
        toSay = null;
        toRun = null;
        if (radioPlayer == null) {
            initializeRadio();
        }

        textList = new TextList("tuner");
        textList.setMode(VirtualList.MODE_TEXT);
        textList.setColorScheme();
        textList.setCommandListener(this);

        textList.addBigText("start", CanvasEx.getColor(CanvasEx.COLOR_TEXT), Font.STYLE_BOLD, -1);
        textList.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
        textList.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
        textList.activate();
    }

    private void showMenu() {
        Menu menu = new Menu(textList);
        menu.addMenuItem("stop", MENU_STOP);
        menu.addMenuItem("up", MENU_SEARCH_UP);
        menu.addMenuItem("down", MENU_SEARCH_DOWN);
        menu.addMenuItem("am", MENU_SWITCH_AM);
        menu.addMenuItem("fm", MENU_SWITCH_FM);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        if (toRun != null) {
            textList.clear();
            textList.addBigText(msgBusy, CanvasEx.getColor(CanvasEx.COLOR_TEXT), Font.STYLE_BOLD, -1);
            return;
        }
        switch (action) {
            case MENU_STOP:
                closePlayer();
                break;

            case MENU_SEARCH_UP:
                toRun = new SearchUpThread();
                toSay = msgSearchUp;
                break;

            case MENU_SEARCH_DOWN:
                toRun = new SearchDownThread();
                toSay = msgSearchDown;
                break;

            case MENU_SWITCH_AM:
                toRun = new SwitchToAMThread();
                toSay = msgSwitchToAM;
                break;

            case MENU_SWITCH_FM:
                toRun = new SwitchToFMThread();
                toSay = msgSwitchToFM;
                break;

            default:
                toRun = null;
                toSay = null;
                break;
        }
        if (action != MENU_STOP) {
            startAction();
        }
        if (menu != null) {
            menu.back();
        }
    }

    public void commandAction(Command c, Displayable s) {
        if (c == JimmUI.cmdBack) {

        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        } else {
            tunerBack();
        }
    }

    private void startAction() {
        Jimm.getDisplay().callSerially(new Runnable() {
            public void run() {
                textList.clear();
                textList.addBigText(TunerRadio.this.toSay +
                        " from \n" +
                        (TunerRadio.this.freq / 10) +
                        "kHz...",
                        CanvasEx.getColor(CanvasEx.COLOR_TEXT),
                        Font.STYLE_BOLD, -1);
                TunerRadio.this.toRun.start();
            }
        });
    }

    private void closePlayer() {
        if (radioPlayer != null) {
            radioPlayer.close();
            radioPlayer = null;
        }
    }

    private void switchToFM() {
        if (tunerControl != null) {
            modulation = TunerControl.MODULATION_FM;

            try {
                freq = tunerControl.getMinFreq(modulation);
                freq = tunerControl.seek(freq, modulation, true);
            } catch (MediaException e) {
                System.out.println("Failed to switch to FM station: " + e.getMessage());
            }
        }
    }

    private void switchToAM() {
        if (tunerControl != null) {
            modulation = TunerControl.MODULATION_AM;

            try {
                freq = tunerControl.getMinFreq(modulation);
                freq = tunerControl.seek(freq, modulation, true);
            } catch (MediaException e) {
                System.out.println("Failed to switch to AM station: " + e.getMessage());
                JimmException.handleExceptionEx(e);
            }
        }
    }

    private void searchUp() {
        if (tunerControl != null) {
            try {
                freq = tunerControl.seek(freq + 1, modulation, true);
            } catch (Exception e) {
                System.out.println("Failed to search up: " + e.getMessage());
                JimmException.handleExceptionEx(e);
            }
        }
    }

    private void searchDown() {
        if (tunerControl != null) {
            try {
                freq = tunerControl.seek(freq - 1, modulation, false);
            } catch (Exception e) {
                System.out.println("Failed to search down: " + e.getMessage());
                JimmException.handleExceptionEx(e);
            }
        }
    }

    /**
     * Initializes and switched on the radio.
     */
    private void initializeRadio() {
        try {
            radioPlayer = Manager.createPlayer("capture://radio");
            radioPlayer.realize();

            radioPlayer.addPlayerListener(this);

            tunerControl = (TunerControl)
                    radioPlayer.getControl("javax.microedition.amms.control.tuner.TunerControl");

            tunerControl.setStereoMode(TunerControl.STEREO);

            // Then, let's get the RDSControl:
            rdsControl = (RDSControl)
                    radioPlayer.getControl("javax.microedition.amms.control.tuner.RDSControl");

            if (rdsControl != null) {
                //Let's turn on the automatic switching
                //to possible traffic announcements:
                try {
                    rdsControl.setAutomaticTA(true);
                } catch (MediaException mex) {
                    JimmException.handleExceptionEx(mex);
                    // ignore if feature is not supported ...
                }
            }

            radioPlayer.start();

            // Now that the radio is on let's first find a radio station
            // by seeking upwards from 91.0 MHz:
            menuSelect(null, MENU_SEARCH_UP);

        } catch (MediaException me) {
            System.out.println("Failed to initialize radio: ");
            me.printStackTrace();
            JimmException.handleExceptionEx(me);
        } catch (IOException ioe) {
            System.out.println("Failed to initialize radio: ");
            ioe.printStackTrace();
            JimmException.handleExceptionEx(ioe);
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        if (event.equals(RDSControl.RDS_NEW_DATA)) {
            updateRDSDisplay();
        }
    }

    /**
     * Shows some RDS data.
     */
    private void updateRDSDisplay() {
        if (rdsControl != null) {
            String channelName = rdsControl.getPS();
            String radioText = rdsControl.getRT();
            String programmeType = rdsControl.getPTYString(true);
            //Date date = rdsControl.getCT();
            String unit = "kHz";
            int val = freq / 10;

            String info = "station: " + channelName + ", " + radioText + "\n" +
                    "frequency: " + val + " " + unit + "\n" +
                    "modulation: " + modulation + "\n" +
                    "genre: " + programmeType + "\n"/* +
                      "local time: " + date*/;
            textList.clear();
            textList.addBigText(info, CanvasEx.getColor(CanvasEx.COLOR_TEXT), Font.STYLE_BOLD, -1);
        }
    }

    class SearchUpThread extends Thread {
        public void run() {
            TunerRadio.this.searchUp();
            TunerRadio.this.toRun = null;
        }
    }

    class SearchDownThread extends Thread {
        public void run() {
            TunerRadio.this.searchDown();
            TunerRadio.this.toRun = null;
        }
    }

    class SwitchToFMThread extends Thread {
        public void run() {
            TunerRadio.this.switchToFM();
            TunerRadio.this.toRun = null;
        }
    }

    class SwitchToAMThread extends Thread {
        public void run() {
            TunerRadio.this.switchToAM();
            TunerRadio.this.toRun = null;
        }
    }
}
//#sijapp cond.end#