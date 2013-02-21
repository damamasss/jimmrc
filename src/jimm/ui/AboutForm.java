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
 File: src/jimm/AbuutForm.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Lavlinskii Roman
 *******************************************************************************/

package jimm.ui;

import DrawControls.CanvasEx;
import DrawControls.TextList;
import jimm.*;
import jimm.comm.Util;
import jimm.comm.StringConvertor;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.util.TimerTask;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AboutForm extends TextList implements CommandListener, MenuListener {

//    final class FaqForm extends TextList implements CommandListener, VirtualListCommands {
//        byte level;
//        String level0;
//        String level1;
//        int levelZero = 0;
//        int levelOne = 0;
//
//        public FaqForm() {
//            super(ResourceBundle.getString("FAQ"), false);
//            setMode(MODE_TEXT);
//            setColorScheme();
//            setCommandListener(this);
//            setVLCommands(this);
//            addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
//        }
//
//        String doGet() {
//            return Util.getStringAsStream("/faq.ini");
//            //return OnlineStatus.getStringAsHttp("http://jimm.besticq.ru/faq.php", false);
//        }
//
//        String[] doTrain() {
//            if (ini.getHeaders().length == 0) ini.setStructure(doGet(), false);
//            String[] items = new String[0];
//            switch (level) {
//                case 0:
//                    items = ini.getHeaders();
//                    //System.out.println(items.length);
//                    break;
//
//                case 1:
//                    items = ini.getItems(level0);
//                    //System.out.println(items.length + " " + level0);
//                    break;
//
//                case 2:
//                    items = new String[]{ini.getValue(level1, level0)};
//                    //System.out.println(items.length + " " + level0 + " " + level1);
//                    break;
//            }
//            return items;
//        }
//
//        void doFill() {
//            String[] items;
//            int font, color;
//            clear();
//            addBigText(ResourceBundle.getString("wait"), getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
//            if ((items = doTrain()).length > 0) {
//                if (level > 1)
//                    font = Font.STYLE_PLAIN;
//                else
//                    font = Font.STYLE_BOLD;
//                color = getColor(COLOR_TEXT);
//                lock();
//                clear();
//                for (int i = 0; i < items.length; i++) {
//                    addBigText(items[i], color, font, i).doCRLF(i);
//                }
//                switch (level) {
//                    case 0:
//                        selectTextByIndex(levelZero);
//                        break;
//
//                    case 1:
//                        selectTextByIndex(levelOne);
//                        break;
//                }
//                unlock();
//            }
//        }
//
//        public void vlItemClicked(VirtualList sender) {
//            switch (level) {
//                case 0:
//                    levelZero = getCurrTextIndex();
//                    level0 = ini.getHeaders()[levelZero];
//                    break;
//                case 1:
//                    levelOne = getCurrTextIndex();
//                    level1 = ini.getItems(level0)[levelOne];
//                    break;
//            }
//            level++;
//            if (level > 2) {
//                level = 2;
//            } else {
//                doFill();
//            }
//        }
//
//        public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
//        }
//
//        public void vlCursorMoved(VirtualList sender) {
//        }
//
//        public void commandAction(Command c, Displayable d) {
//            level--;
//            if (level < 0) {
//                Jimm.setDisplay(AboutForm.this);
//            } else {
//                doFill();
//            }
//        }
//    }

    private final static byte MENU_INFO = (byte) 0;
    private final static byte MENU_UPDATE = (byte) 1;
    private final static byte MENU_FAQ = (byte) 2;
    private final static byte MENU_SYSTEM = (byte) 3;
    private final static byte MENU_RMS = (byte) 4;
    private final static byte MENU_DOWNLOAD = (byte) 5;

    private final static byte MENU_DOWNLOAD_SITE = (byte) 10;
    private final static byte MENU_DOWNLOAD_ONLINE = (byte) 11;
    private final static byte MENU_DOWNLOAD_FORUM = (byte) 12;
    private final static byte MENU_DOWNLOAD_EVANGELIE = (byte) 13;

    private final static byte ABOUT = (byte) -1;
    private final static byte FAQ = (byte) -2;
    private final static byte UPDATE = (byte) -3;

    private static boolean connecting = false;
    public static String version = "";
    private static String faq = "";
    private static String info = "";
    private CanvasEx prvScreen;
    private Command cmdUpdate;
    private TimerTask timerTask;
    //private FaqForm faqForm;
    //private static INIGear ini = new INIGear();

    private byte state;

    public AboutForm(CanvasEx screen) {
        super(ResourceBundle.getString("about"), false);
        setMode(MODE_TEXT);
        setColorScheme();
        setCommandListener(this);
        prvScreen = screen;
        state = -1;
        doActivate();
    }

    public void doActivate() {
        StringBuffer stack = new StringBuffer();
        //stack/*.append("jimm.besticq.ru").append('\n')*/.append("\n");
        //addBigText(stack.toString(), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
        //stack.setLength(0);

        stack.append(ResourceBundle.getString("about_develop")).append(':').append("\n");
        addBigText(stack.toString(), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
        stack.setLength(0);

        //stack.append(ResourceBundle.getString("about_develop_dec")).append('\n').append('\n');
        stack.append(Util.byteArrayToString(Util.decipherPassword(Util.explodeToBytes("38 C6 63 2F D1 6B 2A 78 99 4A 99 36 BD 96 75 91", ' ', 16))))
                .append('\n').append('\n');
        addBigText(stack.toString(), getColor(COLOR_CAT_TEXT), Font.STYLE_PLAIN, -1);
        stack.setLength(0);

        stack.append(ResourceBundle.getString("about_command")).append(':').append('\n');
        addBigText(stack.toString(), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
        stack.setLength(0);

        //stack.append(ResourceBundle.getString("about_command_dec")).append('\n').append('\n');
        stack.append(Util.byteArrayToString(Util.decipherPassword(Util.explodeToBytes("27 C6 6C 36 D7 6A 28 B2 59 43 5D " +
                "0A BB 97 B5 8D 13 CF 73 24 10 AA FB 59 9F 40 5B 0E BE 9F 78 96 1D 06 41 29 DD 76 3E 6C 5D 83 61 06 BF " +
                "8B 66 8E 17 CE 6C 2A DB 75 FB 42 99 5B 59 14 A0 56 B5 B7 1B CB 70 28 D9 6F 3E 62 82 83 75 06 BE 89 68 " +
                "97 0D 0A A1 14 D7 77 2A 73 91 54 5C 16 A0 5A 55 91 17 D6 64 24 C8 75 F7 B2 A3 5D 54 0E BE 89 B5 B8 1F " +
                "CE 73 34 D1 78 F7 B2 BE 43 52 0C BB 97 66 5C 3B C5 6F 34 C7 AA FB 52 81 51 5C 0A BD 98 66 5C 37 C3 6C " +
                "2C C8 75 F7 B2 A0 4D 52 0C BB 97 66 5C 3B C5 6F 34 C7 AA FB 47 91 51 4B 06 B2 89 B9 5C 23 C6 65 2C C8 74 28", ' ', 16))))
                .append('\n').append('\n');
        addBigText(stack.toString(), getColor(COLOR_CAT_TEXT), Font.STYLE_PLAIN, -1);
        stack.setLength(0);

        stack.append(ResourceBundle.getString("traffic_out")).append(':').append(' ');
        addBigText(stack.toString(), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
        stack.setLength(0);

        stack.append(Traffic.getSessionTrafficOut()).append('k').append('b').append('\n');
        addBigText(stack.toString(), getColor(COLOR_CAT_TEXT), Font.STYLE_PLAIN, -1);
        stack.setLength(0);

        stack.append(ResourceBundle.getString("traffic_in")).append(':').append(' ');
        addBigText(stack.toString(), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
        stack.setLength(0);

        stack.append(Traffic.getSessionTrafficIn()).append('k').append('b').append('\n').append('\n');
        addBigText(stack.toString(), getColor(COLOR_CAT_TEXT), Font.STYLE_PLAIN, -1);
        stack.setLength(0);

        stack.append(ResourceBundle.getString("latest_ver")).append(':').append(' ');
        addBigText(stack.toString(), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
        stack.setLength(0);

        stack.append(version);
        addBigText(stack.toString(), getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);

        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);

        Jimm.setDisplay(this);

        doUpdate(false);
        // http://c.waplog.net/339896.cnt - ievangelie.ru
        // http://imtop.ru/279/small.png - jimm.besticq.ru
        //http://mrdark.ru/di/check.php?id=74961791&uid=381433601 -  проверка номера на бан
        //http://mrdark.ru/di/getversion.php?v=v0.80 kNa7E&d=02.04.2010 - если версия не совпадает с новой, пишется существенное исправление
        //http://mrdark.ru/di/faq.php
        //http://mrdark.ru/di/pda/load.php
    }

    public void doUpdate(boolean anyway) {
//        if (state == FAQ) {
//            if (faqForm == null) {
//                faqForm = new FaqForm();
//            }
//            faqForm.level = 0;
//            Jimm.setDisplay(faqForm);
//            faqForm.doFill();
//            return;
//        }
        if (state == ABOUT) {
            if (version.length() == 0) {
                if (timerTask != null) {
                    timerTask.cancel();
                    timerTask = null;
                }
                timerTask = new TimerTask() {
                    public void run() {
                        connecting = true;
                        //String midp = StringConvertor.getSystemProperty("microedition.platform", "none");
                        //String locale = StringConvertor.getSystemProperty("microedition.locale", "none");
                        //String encode = StringConvertor.getSystemProperty("microedition.encoding", "none");
                        String date = "###DATE###";
                        String cldc = StringConvertor.getSystemProperty("microedition.configuration", "none");
                        String profiles = StringConvertor.getSystemProperty("microedition.profiles", "none");
                        String screen = Integer.toString(DrawControls.NativeCanvas.getWidthEx()) + "x" + Integer.toString(DrawControls.NativeCanvas.getHeightEx()) + "x" + Jimm.getDisplay().numColors();
                        String touch = (Jimm.isTouch()) ? "touch_screen" : "easy_screen";
                        String setup = Util.explode(Options.firstDate, ' ')[0];

                        StringBuffer sb = new StringBuffer();

                        sb.append("http://jimm.besticq.ru/last.ls")
                                .append("?date=").append(date)
                                .append("&cldc=").append(cldc)
                                .append("&prof=").append(profiles)
                                .append("&screen=").append(screen)
                                .append("&touch=").append(touch)
                                .append("&setup=").append(setup);

//                        for (int i = 0; i < sb.length(); i++) {
//                            char c = sb.charAt(i);
//                            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
//                                sb.setCharAt(i, '_');
//                            }
//                        }
//#sijapp cond.if modules_DEBUGLOG is "true"#
//                        DebugLog.addText("last = " + sb.toString());
//                        JimmUI.setClipBoardText(sb.toString());
//#sijapp cond.end#
                        version = OnlineStatus.getStringAsHttp(sb.toString(), true);
                        addBigText('\n' + version, getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
                        connecting = false;
                        doUpdate(true);
                    }
                };
                Jimm.getTimerRef().schedule(timerTask, 1500);
            }
            return;
        }
        lock();
        clear();
        removeAllCommands();
        String stack = (state == FAQ) ? faq : info;

        if (stack.length() > 0)
            addBigText(stack, getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
        else
            addBigText(ResourceBundle.getString("wait"), getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);

        if (stack.length() == 0 && !anyway && !connecting) {
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            timerTask = new TimerTask() {
                public void run() {
                    connecting = true;
                    if (state == FAQ)
                        faq = OnlineStatus.getStringAsHttp("http://jimm.besticq.ru/faq.php", true);
                    else
                        info = OnlineStatus.getStringAsHttp("http://jimm.besticq.ru/last.lst", true);
                    connecting = false;
                    doUpdate(true);
                }
            };
            Jimm.getTimerRef().schedule(timerTask, 50);
        }
        addCommandEx((cmdUpdate = new Command(ResourceBundle.getString("vers_update"), Command.ITEM, 1)), MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        unlock();
    }

    private void showMenu() {
        Menu menu = new Menu(this);
        menu.addMenuItem("about_cmd", MENU_INFO);
        menu.addMenuItem("new_functions", MENU_UPDATE);
        menu.addMenuItem("help", MENU_FAQ);
//#sijapp cond.if modules_SYSMANAGER is "true"#
        menu.addMenuItem("options_system", MENU_SYSTEM);
        menu.addMenuItem("RMS", MENU_RMS);
//#sijapp cond.end#
        menu.addMenuItem("urls", MENU_DOWNLOAD);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    private void showNativeMenu() {
        Menu menu = new Menu(this);
        menu.addMenuItem("vers_download", MENU_DOWNLOAD_SITE);
        menu.addMenuItem("vers_download_online", MENU_DOWNLOAD_ONLINE);
        menu.addMenuItem("forum", MENU_DOWNLOAD_FORUM);
        menu.addMenuItem("religion", MENU_DOWNLOAD_EVANGELIE);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    private static String[] getAdress() {
        return Util.explode("jimm.besticq.ru|online.besticq.ru/jimm|forum.besticq.ru|ievangelie.ru", '|');

    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case MENU_INFO:
                Jimm.setDisplay(new Alert(null, ResourceBundle.getCRLFString("about_popup"), null, AlertType.INFO));
                return;

            case MENU_UPDATE:
                state = UPDATE;
                doUpdate(false);
                break;

            case MENU_FAQ:
                state = FAQ;
                doUpdate(false);
                break;

            //#sijapp cond.if modules_SYSMANAGER is "true"#
            case MENU_SYSTEM:
                new SystemManager(null);
                return;

            case MENU_RMS:
                new RmsInfo(null);
                return;
            //#sijapp cond.end#                        

            case MENU_DOWNLOAD:
                showNativeMenu();
                break;

            default:
                try {
                    Jimm.jimm.platformRequest("http://" + getAdress()[action - 10]);
                } catch (Exception ignored) {
                }
                break;
        }
        if (menu != null && action != MENU_DOWNLOAD/* && action != MENU_FAQ*/) {
            menu.back();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
            if (prvScreen != null) {
                Jimm.setDisplay(prvScreen);
                prvScreen = null;
            } else {
                Jimm.getContactList().activate();
            }
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
        } else if (c == cmdUpdate) {
            if (state == FAQ) faq = "";
            if (state == UPDATE) info = "";
            doUpdate(false);
        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        }
    }
}