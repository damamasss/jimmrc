/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-10  Jimm Project

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
 *******************************************************************************
 File: src/jimm/Options.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis, Igor Palkin
 ******************************************************************************/

package jimm.forms;

import DrawControls.*;
import com.tomclaw.xmlgear.XMLGear;
import com.tomclaw.xmlgear.XMLItem;
import jimm.*;
import jimm.chat.ChatTextList;
import jimm.comm.DateAndTime;
import jimm.comm.Icq;
import jimm.comm.OtherAction;
import jimm.comm.Util;
import jimm.files.FileBrowser;
import jimm.files.FileBrowserListener;
import jimm.files.FileSystem;
import jimm.files.FileTransfer;
import jimm.ui.*;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

public class OptionsForm implements CommandListener, ItemStateListenerEx, MenuListener, VirtualListCommands, SelectListener
//#sijapp cond.if modules_FILES is "true"#
        , FileBrowserListener, Runnable
//#sijapp cond.end#
{

    //Class clazz = Class.forName("com.nokia.mid.ui.DeviceControl");
    //com.nokia.mid.ui.DeviceControl control = (com.nokia.mid.ui.DeviceControl)clazz.newInstance()

    private static final byte OPTIONS_COLORS = (byte) -1;
    private static final byte OPTIONS_SOUNDS = (byte) 0;
    private static final byte OPTIONS_VIBRATOR = (byte) 1;
    private static final byte OPTIONS_CHAT = (byte) 2;
    private static final byte OPTIONS_HISTORY = (byte) 3;
    private static final byte OPTIONS_SMILES = (byte) 4;
    private static final byte OPTIONS_SYSTEM = (byte) 5;
    private static final byte OPTIONS_CONTACTLIST = (byte) 6;
    private static final byte OPTIONS_VIEW = (byte) 7;
    private static final byte OPTIONS_LIGHT = (byte) 8;
    private static final byte OPTIONS_HOTKEYS = (byte) 9;
    private static final byte OPTIONS_PROXY = (byte) 10;
    private static final byte OPTIONS_NETWORK = (byte) 11;
    private static final byte OPTIONS_FAST_VIEW = (byte) 12;
    private static final byte OPTIONS_SEND_FILES = (byte) 13;
    private static final byte OPTIONS_WAIT = (byte) 14;
    private static final byte OPTIONS_TOOLBAR = (byte) 15;
    private static final byte OPTIONS_SBOLTUN = (byte) 16;
    private static final byte OPTIONS_SYNCHRONIZE = (byte) 17;

    //private static final byte COLORS_PASTE = (byte) -1;
    //private static final byte COLORS_COPY = (byte) -2;
    //private static final byte COLORS_SAVE = (byte) -3;

    private static final byte FS_IMPORT = (byte) 0;
    private static final byte FS_EXPORT = (byte) 1;

    private Icq icq;
    private Menu optionsMenu;
    private FormEx optionsForm;
    private Hashtable keys;
    private Integer copyKey = new Integer(0);
    private int currentHour;

    // #sijapp cond.if modules_TOOLBAR is "true"#
    private LineChoise[] lineToolBar;
    // #sijapp cond.end#
    private LineChoise[] lineKey;
    private LineChoise[] lineInt;
    private LineChoiseBoolean[] lineOnOff;
    //private LineChoiseBoolean[] lineOnOffIcq;
    private LineChoise languageLine;
    //private LineChoise clientIdLine;
    private LineChoise shemesLine;
    private LineChoiseBoolean graphicLine;
    //    private TextField sleepWake;
    //    private LineChoiseBoolean connectWake;
    // #sijapp cond.if modules_SMILES is "true"#
    private TextField emotionsSize;
    // #sijapp cond.end#
    private TextField srvHostTextField;
    private TextField srvPortTextField;
    private TextField autoStatusDelayTimeTextField;
    //    private TextField antispamMsgTextField;
    //    private TextField antispamAnswerTextField;
    //    private TextField antispamHelloTextField;
    private TextField connAliveIntervTextField;
    private TextField reconnectNumberTextField;
    //private TextField maxTextSizeTF;
    private TextField tfCaptionShift;
    //#sijapp cond.if modules_HTTP is "true"#
    //private TextField httpUserAgendTextField;
    //private TextField httpWAPProfileTextField;
    //#sijapp cond.end#
    //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    //private TextField tfldLineSize;
    //#sijapp cond.end#
    private TextField templMessChat;
    // #sijapp cond.if modules_SBOLTUN is "true"#
    private TextField sBoltunSleep;
// #sijapp cond.end#
    //private TextField animationConstant;

    //#sijapp cond.if target isnot "DEFAULT"#
    //#sijapp cond.if target isnot "RIM"#
    //#sijapp cond.if modules_SOUNDS is "true"#
    private TextField messageNotificationSoundfileTextField;
    //private Gauge notificationSoundVolume;
    private Gauge messageSoundVolume;
    private Gauge onlineSoundVolume;
    private Gauge offlineSoundVolume;
    private Gauge typingSoundVolume;
    //#sijapp cond.end#
    //#sijapp cond.if target is "MIDP2"#
    private Gauge vibroFraq;
    //#sijapp cond.end#
    //#sijapp cond.if modules_SOUNDS is "true"#
    private TextField onlineNotificationSoundfileTextField;
    private TextField offlineNotificationSoundfileTextField;
    private TextField typingNotificationSoundfileTextField;
    //#sijapp cond.end#
    //private TextField clientStringVersion;
    private TextField blinkOnlineTimeTextField;
    private TextField vibroTimeTextField;
    private TextField enterPasswordTextField;
    //private TextField reEnterPasswordTextField;
    // #sijapp cond.end#
    // #sijapp cond.end#
    //#sijapp cond.if modules_LIGHT is "true"#
    private TextField lightTimeout;
    private LineChoiseBoolean lightManual;
    //#sijapp cond.end#
    //#sijapp cond.if modules_PROXY is "true"#
    private TextField srvProxyHostTextField;
    private TextField srvProxyPortTextField;
    private TextField srvProxyLoginTextField;
    private TextField srvProxyPassTextField;
    private TextField connAutoRetryTextField;
    //#sijapp cond.end#
    private Gauge cursorTransValue;
    private Gauge capTransValue;
    private Gauge barTransValue;
    //private Gauge popupTransValue;
    private Gauge menuTransValue;
    private Gauge splashTransValue;
    private Gauge blackOutGauge;
    private Gauge iconCanvasGauge;
    //#sijapp cond.if modules_FILES="true"#
    private LineChoise csFs;
    private LineChoise optionsFs;
    private LineChoise extraFs;
    private LineChoise fullFs;
    private LineChoise fsEx;
    //#sijapp cond.end#

    public Image skin;
    private String[] skinsName;
    private String[] skinsLocal;
    private String[] schemeName;
    private String[] schemeLocal;
    private String[] fontName;
    private String[] fontLocal;
    public String iconsPrefix;
    // #sijapp cond.if modules_DEBUGLOG is "true"#
    //private ColorIcon[] colorIcon;
    private int[] colors;
    private TextList colorTextList;
    private int colorBuffer = -1;
// #sijapp cond.end#


    public OptionsForm(Icq icq) throws NullPointerException {
        keys = new Hashtable();
        this.icq = icq;
        if (optionsMenu != null) {
            optionsMenu = null;
        }
        optionsMenu = new Menu(NativeCanvas.getCanvas());
        //optionsMenu = new OptionsFormEx(NativeCanvas.getCanvas(), ResourceBundle.getString("options_lng"));
        Options.loadKeys(keys);
        lineInt = new LineChoise[Options.OPTION_INTEGER_SIZE];
        lineOnOff = new LineChoiseBoolean[Options.OPTION_BOOLEAN_SIZE];
        //lineOnOffIcq = new LineChoiseBoolean[7];
        Arrays array = new Arrays();
        array.init("/skins.txt");
        skinsName = array.getStrings(1);
        skinsLocal = array.getStrings(2);
        array.init("/schemes.txt");
        schemeName = array.getStrings(1);
        schemeLocal = array.getStrings(2);
        //#sijapp cond.if modules_GFONT="true"#
        fontAvaible();
        //#sijapp cond.end#
    }

    //#sijapp cond.if modules_GFONT="true"#
    private void fontAvaible() {
        String content = Util.removeCr(Util.getStringAsStream("/gfonts.xml"));
        try {
            XMLGear xg = new XMLGear();
            xg.setStructure(content);
            XMLItem[] fonts = xg.getItemsWithHeader(new String[]{"container"}, "item");
            if (fonts == null) {
                return;
            }
            int len = fonts.length;
            fontName = new String[len];
            fontLocal = new String[len];
            for (int i = 0; i < len; i++) {
                XMLItem font = fonts[i];
                fontName[i] = font.getParamValue("par_name");
                fontLocal[i] = font.getParamValue("folder");
            }
        } catch (Exception ignored) {
        }
    }
    //#sijapp cond.end#


    public void retutnFromFiles() {
        Jimm.setDisplay(optionsForm);
    }
//
//    // todo # sijapp cond . end#
//
//
//    private int transformColors(int idx) {
//        return getColorsTrans()[idx];
//    }
//
//    private int unTransformColor(int idx) {
//        int[] ixs = getColorsTrans();
//        for (int i = ixs.length - 1; i >= 0; i--) {
//            if (ixs[i] == idx) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    private int[] unTransformColors(int[] icons) {
//        int[] temp = new int[icons.length];
//        for (int i = 0; i < icons.length; i++) {
//            temp[i] = icons[unTransformColor(i)];
//        }
//        return temp;
//    }

    // #sijapp cond.if modules_DEBUGLOG is "true"#

    public void colorChoosedForm(int color) {
        int currIndex = colorTextList.getCurrTextIndex();
        colors[currIndex] = color;
        if (Options.getBoolean(Options.OPTION_COLORS_FROM_SKIN)) {
            Options.setBoolean(Options.OPTION_COLORS_FROM_SKIN, false);
            Options.safe_save();
        }
        colorTextList.switchHeaderIcon(currIndex + 1, getIcon(currIndex), false);
        Jimm.back();
    }


    private void fillColorTextList() {
        colorTextList.setColorScheme();

        colorTextList.clear();
        boolean flag = (colors == null);
        if (flag) {
            colors = new int[CanvasEx.COLORS];
        }
        String[] names = Util.explode(getColorsName(), '|');
        for (int i = 0; i < CanvasEx.COLORS; i++) {
            if (flag) {
                colors[i] = CanvasEx.getColor(i);
            }
            colorTextList.addHeaderIcon(getIcon(i), i, i + 1);
            JimmUI.addTextListItem(colorTextList, names[i], null, i, true);
        }
        colorTextList.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
        colorTextList.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
        colorTextList.setVLCommands(this);
        colorTextList.setCommandListener(this);
        colorTextList.activate();
    }

    private String getColorsName() {
        return "color_1" + "|" + "color_2" + "|" + "color_3" + "|" + "color_4" + "|" + "color_5" + "|" + "color_6" + "|"
                + "color_7" + "|" + "color_8" + "|" + "color_9" + "|" + "color_10" + "|" + "color_11" + "|" + "color_12"
                + "|" + "color_13" + "|" + "color_14" + "|" + "color_15" + "|" + "color_16" + "|" + "color_17" + "|"
                + "color_18" + "|" + "color_19" + "|" + "color_20" + "|" + "color_21" + "|" + "color_22" + "|"
                + "color_23" + "|" + "color_24" + "|" + "color_25" + "|" + "color_26" + "|" + "color_27"/* + "|"
                + "color_28" + "|" + "color_29"*/;
    }
//
//    private int[] getColorsTrans() {
//        return new int[]{0, 2, 4, 25, 3, 12, 5, 1, 8, 9, 21, 10, 17, 7, 18, 11, 24, 20, 22, 23, 14, 15, 26, 6, 19, 16, 13};
//        //return new int[]{0, 3, 12, 13, 5, 1, 8, 9, 21, 10, 17, 7, 18, 11, 24, 20, 22, 23, 14, 15, 26, 19, 2, 4, 25, 28, 6, 27, 16};  // 13 - mdcurs 25 - mb2
//        //return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28};
//    }

    //

    private Icon getIcon(int idx) {
        int height = optionsForm.getFormIconsHeight();
        height = (height > 16) ? height : 16;
        Image img = Image.createImage(height, height);
        Graphics g = img.getGraphics();
        g.setColor(CanvasEx.getInverseColor(colors[idx]));
        g.fillRect(0, 0, height, height);
        g.setColor(colors[idx]);
        g.fillRoundRect(height / 8, height / 8, height / 4 * 3, height / 4 * 3, height / 8 * 3, height / 8 * 3);
        return (new Icon(img, 0, 0, height, height));
    }

    private void colorItemSelected() {
        Jimm.setPrevScreen(colorTextList);
//        Jimm.setDisplay(new jimm.ui.ColorChooserEx(colorTextList, 4, 4));
        Jimm.setDisplay(new jimm.ui.ColorChooser(colors[colorTextList.getCurrTextIndex()]));
    }
// #sijapp cond.end#


//    public void colorChoosedForm(int color) {
//        int currIndex = optionsForm.getCurrIndex();
//        colorIcon[currIndex].setColor(color);
//        if (Options.getBoolean(Options.OPTION_COLORS_FROM_SKIN)) {
//            Options.setBoolean(Options.OPTION_COLORS_FROM_SKIN, false);
//            Options.safe_save();
//        }
//        optionsForm.activate();
//    }


    private void HotkeyFormInit() {
        clearForm();
        optionsForm.removeCommand(JimmUI.cmdSave);
        Integer integer;
        Integer integer2;
        int marker = 0;
        lineKey = new LineChoise[keys.size()];
        java.util.Enumeration enumeration = keys.keys();
        while (enumeration.hasMoreElements()) {
            integer = (java.lang.Integer) enumeration.nextElement();
            integer2 = (Integer) keys.get(integer);
            lineKey[marker] = createLine(getHotkeyName(integer.intValue()), getHotkeyActionNames(), 0, true, true);
            lineKey[marker].setSelected(integer2.intValue());
            optionsForm.append(lineKey[marker]);
            marker++;
        }
        optionsForm.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
        optionsForm.addCommand(cmdNewKey = new Command(ResourceBundle.getString("add_new"), Command.ITEM, 3));
        optionsForm.addCommand(cmdDelKey = new Command(ResourceBundle.getString("delete"), Command.ITEM, 3));
        //optionsForm.addCommand(cmdCopyKey = new Command(ResourceBundle.getString("copy_text"), Command.ITEM, 3));
        //optionsForm.addCommand(cmdPasteKey = new Command(ResourceBundle.getString("paste"), Command.ITEM, 3));
    }

    private String getHotkeyName(int key) {
        //String s1 = NativeCanvas.getInst().getKeyName((key > 500) ? (key > 1500) ? key - 2000 : key - 1000 : key); todo
        String s1 = NativeCanvas.getInst().getKeyName((key > 500) ? key - 1000 : key);
        String s2 = (s1).toLowerCase();
        if (s1.length() == 0 || s2.equals("unmapped") || s2.indexOf(" unicode") >= 0) {
            s1 = String.valueOf(key);
        }
        StringBuffer sb = new StringBuffer();
        //        if(NativeCanvas.comboKeys(key)) {
        //            sb.append("[#+] ");
        //        }
        //        sb.append(" ").append(s1);
        //if (NativeCanvas.comboKeys(key)) {
        //    sb.append("[#+]");
        //}
        //sb.append((key > 500) ? (key > 1500) ? "[CHAT] " : "[LONG] " : "" ); // todo
        sb.append((key > 500) ? "[LONG] " : "");
        sb.append(ResourceBundle.getString("ext_clhotkey")).append(": ").append(s1);
        return sb.toString();
    }

    private String getHotkeyActionNames() {
        return "ext_hotkey_action_none"
                + "|" + "info"
                + "|" + "send_message"
                + "|" + "history_lng"
                + "|" + "ext_hotkey_action_onoff"
                + "|" + "options_lng"
                + "|" + "keylock"
                + "|" + "minimize"
                + "|" + "dc_info"
                + "|" + "reqstatmsg"
                + "|" + "sound_off"
                + "|" + "delete_chats"
                + "|" + "xtraz_msg"
                + "|" + "magic_eye"
                + "|" + "auto_answer"
                + "|" + "vibration"
                + "|" + "flash"
                + "|" + "set_status"
                + "|" + "show_user_groups"
                + "|" + "chat_list"
                + "|" + "profiles"
                + "|" + "previous_profile"
                + "|" + "switch_profile"
                + "|" + "get_status"
                + "|" + "manage_contact_list"
                + "|" + "connect";
    }

    public static int getFunctionFontHeight(int size) {
        try {
            return (new FontFacade(CanvasEx.getSuperFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, size))).getFontHeight();
        } catch (Exception e) {
            return -1;
        }
    }

    private String getFunctionFontHeight() {
        StringBuffer sb = new StringBuffer()
                .append(getFunctionFontHeight(Font.SIZE_SMALL)).append('|')
                .append(getFunctionFontHeight(Font.SIZE_MEDIUM)).append('|')
                .append(getFunctionFontHeight(Font.SIZE_LARGE));

        return sb.toString();
    }

//    private void saveHotheyList() {
//        Integer integer;
//        int marker = 0;
//        java.util.Enumeration enumeration = keys.keys();
//        while (enumeration.hasMoreElements()) {
//            integer = (java.lang.Integer) enumeration.nextElement();
//            keys.put(integer, new Integer(lineKey[marker].getSelected()));
//            marker++;
//        }
//        Options.setString(Options.OPTION_HOTKEYS_HASH, Options.stringKeys(keys));
//    }

//    private void ColorChooserActivate() {
//        colorIcon = new ColorIcon[CanvasEx.COLORS];
//        String[] names = Util.explode(getColorsName(), '|');
//        for (int i = 0; i < CanvasEx.COLORS; i++) {
//            colorIcon[i] = new ColorIcon(
//                    ResourceBundle.getString(names[transformColors(i)]), CanvasEx.getColor(transformColors(i)), optionsForm.getDrawWidth(), optionsForm.vtGetItemHeight()
//            );
//            optionsForm.append(colorIcon[i]);
//        }
//    }

//    private void ColorChooserUpdate() {
//        optionsForm.clear();
//        for (int i = 0; i < CanvasEx.COLORS; i++) {
//            optionsForm.append(colorIcon[i]);
//        }
//    }

    private int lastIndex = 0;


    private Command cmdNewKey;
    private Command cmdDelKey;
    //private Command cmdCopyKey;
    //private Command cmdPasteKey;

    //private final Command cmdAddNewAccount = new Command(ResourceBundle.getString("add_new"), Command.ITEM, 3);
    //private final Command cmdDeleteAccount = new Command(ResourceBundle.getEllipsisString("delete"), Command.ITEM, 3);
    //private int currAccount;
    //private Vector uins = new Vector();
    //private Vector passwords = new Vector();
    //private Vector nicks = new Vector();

    //    public void initOptionsList() {
    //        optionsMenu.clear();
    //        optionsMenu.append(ResourceBundle.getString("profiles"), OPTIONS_ACCOUNT);
    //        optionsMenu.append("ICQ", OPTIONS_ICQ);
    //        optionsMenu.append(ResourceBundle.getString("options_network"),OPTIONS_NETWORK);
    //// #sijapp cond.if modules_PROXY is "true"#
    //        if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_PROXY) {
    //            optionsMenu.append(ResourceBundle.getString("proxy"), OPTIONS_PROXY);
    //        }
    //// #sijapp cond.end#
    //        optionsMenu.append(ResourceBundle.getString("options_interface"), OPTIONS_INTERFACE);
    //        // #sijapp cond.if modules_TOOLBAR is "true"#
    //        if (Jimm.isTouch()) {
    //            optionsMenu.append(ResourceBundle.getString("tool_bar"), OPTIONS_TOOLBAR);
    //        }
    //        // #sijapp cond.end#
    //        optionsMenu.append(ResourceBundle.getString("options_trans"), OPTIONS_TRANS);
    //        optionsMenu.append(ResourceBundle.getString("color_scheme_lng"), OPTIONS_COLOR_SCHEME);
    //        optionsMenu.append(ResourceBundle.getString("skins_cs"), OPTIONS_SKINS);
    ////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    //        optionsMenu.append(ResourceBundle.getString("cl_chat"), OPTIONS_CLCHAT);
    ////#sijapp cond.end#
    //        optionsMenu.append(ResourceBundle.getString("options_hotkeys"), OPTIONS_HOTKEYS);
    //        optionsMenu.append(ResourceBundle.getString("options_signaling"), OPTIONS_SIGNALING);
    //        optionsMenu.append(ResourceBundle.getString("antispam"), OPTIONS_ANTISPAM);
    //        optionsMenu.append(ResourceBundle.getString("misc"), OPTIONS_MISC);
    ////#sijapp cond.if modules_FILES is "true"#
    //        optionsMenu.append("import_export", OPTIONS_IMP_EXP);
    //// #sijapp cond.end#
    //        optionsMenu.setCurrentItem(lastIndex);
    //        optionsMenu.setMenuListener(this);
    //    }

//    private void readAccontsData() {
//        uins.removeAllElements();
//        passwords.removeAllElements();
//        nicks.removeAllElements();
//        copy(uins, Options.uins);
//        copy(passwords, Options.passwords);
//        copy(nicks, Options.nicks);
//        currAccount = Options.getInt(Options.OPTIONS_CURR_ACCOUNT);
//    }

//    private void copy(Vector dst, Vector src) {
//        int size = src.size();
//        for (int i = 0; i < size; i++) {
//            dst.addElement(src.elementAt(i));
//        }
//    }
//
//    private String checkUin(String value) {
//        if ((value == null) || (value.length() == 0)) {
//            return "---";
//        }
//        return value;
//    }
//
//    private void showAccountControls() {
//        int size = uins.size();
//        if (size != 1) {
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < size; i++) {
//                if (i != 0) {
//                    sb.append('|');
//                }
//                sb.append(checkUin((String) uins.elementAt(i)));
//            }
//            lineInt[Options.OPTIONS_CURR_ACCOUNT] = createLine("def_prof", sb.toString(), Options.getInt(Options.OPTIONS_CURR_ACCOUNT), false, true);
//            if (currAccount >= size) {
//                currAccount = size - 1;
//            }
//            lineInt[Options.OPTIONS_CURR_ACCOUNT].setSelected(currAccount);
//            try {
//                optionsForm.append(lineInt[Options.OPTIONS_CURR_ACCOUNT]);
//            } catch (IllegalStateException ignored) {
//            }
//        }
//
//        uinTextField = new TextField[size];
//        passwordTextField = new TextField[size];
//        nickTextField = new TextField[size];
//        TextField uinFld, passFld, nickFld;
//        String add, uin;
//        Profile pr = null;
//        for (int i = 0; i < size; i++) {
//            add = (size == 1) ? "" : "-" + (i + 1);
//
//            uin = (String) uins.elementAt(i);
//            uinFld = new TextField(ResourceBundle.getString("uin") + add, uin, 12, TextField.NUMERIC);
//            passFld = new TextField(ResourceBundle.getString("password") + add, (String) passwords.elementAt(i), 32, TextField.PASSWORD);
//            nickFld = new TextField(ResourceBundle.getString("prof_name") + add, (String) nicks.elementAt(i), 32, TextField.ANY);
//
//            if (size > 1) {
//                optionsForm.append("---");
//            }
//
//            if (isAvailableUinConnect(uin, pr) || uin.length() == 0 || uin.length() == 1) {
//                optionsForm.append(uinFld);
//                optionsForm.append(passFld);
//            }
//
//            optionsForm.append(nickFld);
//
//            uinTextField[i] = uinFld;
//            passwordTextField[i] = passFld;
//            nickTextField[i] = nickFld;
//        }
//
//        optionsForm.removeAllCommands();
//        optionsForm.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
//        optionsForm.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
//        //optionsForm.addCommand(JimmUI.cmdSave);
//        optionsForm.addCommand(cmdAddNewAccount);
//        if (getAvailableUins().length >= 1) {
//            optionsForm.addCommand(cmdDeleteAccount);
//        }
//    }
//
//    private void setAccountOptions() {
//        int size = uins.size();
//        if (size < 1) {
//            return;
//        }
//        StringBuffer uin = new StringBuffer();
//        StringBuffer pass = new StringBuffer();
//        StringBuffer nick = new StringBuffer();
//        String tmp;
//        String suin;
//
//        for (int i = 0; i < size; i++) {
//            suin = (String) uins.elementAt(i);
//            boolean cont = false;
//            if (checkUin(suin).equals("---")) {
//                cont = true;
//            }
//            for (int k = i - 1; k >= 0; k--) {
//                if (uins.elementAt(k).toString().equals(suin)) {
//                    cont = true;
//                    break;
//                }
//            }
//            if (cont) {
//                continue;
//            }
//            tmp = (String) passwords.elementAt(i);
//            if (tmp.length() > 8) {
//                tmp = tmp.substring(0, 8);
//            }
//
//            if (i > 0) {
//                uin.append('\t');
//            }
//            uin.append(suin);
//            if (i > 0) {
//                nick.append('\t');
//            }
//            nick.append(nicks.elementAt(i));
//            if (i > 0) {
//                pass.append('\t');
//            }
//            pass.append(tmp);
//        }
//        Options.setString(Options.OPTION_UIN, uin.toString());
//        Options.setString(Options.OPTION_PASSWORD, pass.toString());
//        Options.setString(Options.OPTION_MY_NICK, nick.toString());
//        if (currAccount >= size) {
//            currAccount = size - 1;
//        }
//        Options.setInt(Options.OPTIONS_CURR_ACCOUNT, currAccount);
//        Options.initAccounts();
//        Profiles.update();
//    }
//
//    private void readAccontsControls() {
//        uins.removeAllElements();
//        passwords.removeAllElements();
//        nicks.removeAllElements();
//        String uin;
//        for (int i = 0; i < uinTextField.length; i++) {
//            uin = uinTextField[i].getString();
//            if (uin != null && uin.length() > 0) {
//                uins.addElement(uinTextField[i].getString());
//                passwords.addElement(passwordTextField[i].getString());
//                nicks.addElement(nickTextField[i].getString());
//            }
//        }
//        currAccount = (lineInt[Options.OPTIONS_CURR_ACCOUNT] == null) ? 0 : lineInt[Options.OPTIONS_CURR_ACCOUNT].getSelected();
//    }

    //#sijapp cond.if modules_FILES is "true"#
    private String eiFile;

    private void reset(FileSystem fs) {
        fs.close();
        eiFile = null;
    }

    private void importFinish() {
        ChatTextList.messChange();
        Jimm.getContactList().optionsChanged();
        Jimm.getContactList().setFontSize();
//#sijapp cond.if modules_GFONT="true" #
        if (CanvasEx.updateFont(Options.getString(Options.OPTION_GFONT_PATH))) {
            CanvasEx.updateFont();
        }
//#sijapp cond.end#
    }

    public void onFileSelect(String file) {
        eiFile = file;
        (new Thread(this)).start();
    }

    public void onDirectorySelect(String directory) {
        eiFile = directory;
        (new Thread(this)).start();
    }

    public ContactItem getCItem() {
        return null;
    }
//#sijapp cond.end#

    public static String[] getResourceString(String[] input) {
        for (int i = 0; i < input.length; i++) {
            input[i] = ResourceBundle.getString(input[i]);
        }
        return input;
    }

    private String getDir(String[] src, int len) {
        StringBuffer sb = new StringBuffer();
        sb.append('/');
        for (int i = 1; i < len; i++) {
            sb.append(src[i]).append('/');
        }
        return sb.toString();
    }
//
//    private Icon getClientIcon() {
//        int idx = icq.getProfile().getInt(Profile.OPTION_CLIENT_ID);
//        if (idx >= 0 && idx < getClientIconIndex().length) {
//            return ContactList.clientIcons.elementAt(getClientIconIndex()[idx]);
//        }
//        return ContactList.menuIcons.elementAt(10);
//    }
//
//    private static int[] getClientIconIndex() {
//        return new int[]{7, 0, 14, 14, 12, 13, 1, 15, 3, 22, 4, 24, 31, 32};
//    }
//
//    public static String getClientString() {
//        return "J[i]mm|QIP 2005a|QIP 2010a|QIP Infium|QIP PDA (Symbian)|QIP PDA (Windows)|Miranda|ICQ 6|R&Q|Mac ICQ|Trillian|Pigeon|Jimm RC|BayanICQ";
//    }


//    private String[] getAvailableUins() {
//        int size = uins.size();
//        int cp = Profiles.connectedProfiles() + 1;
//        if (icq.isConnected() || icq.connectionIsActive()) {
//            cp--;
//        }
//        String items[] = new String[size - cp];
//        int j = 0;
//        String uin;
//        Profile profile = null;
//        for (int i = 0; i < size; i++) {
//            uin = (String) uins.elementAt(i);
//            if (!isAvailableUin(uin, profile)) {
//                continue;
//            }
//            items[j++] = checkUin(uin);
//        }
//        return items;
//    }
//
//    private boolean isAvailableUin(String uin, Profile profile) {
//        if ((uin.equals(icq.getUin())) || uin.length() == 0 || "---".equals(uin)) {
//            return false;
//        }
//        profile = Profiles.getProfile(uin);
//        if ((profile != null) && (profile.getIcq().isConnected() || profile.connectionIsActive())) {
//            return false;
//        }
//        return true;
////    }
//
//    private boolean isAvailableUinConnect(String uin, Profile profile) {
//        if ((uin.equals(icq.getUin()) && icq.isConnected()) || uin.length() == 0 || "---".equals(uin)) {
//            return false;
//        }
//        profile = Profiles.getProfile(uin);
//        return !((profile != null) && (profile.getIcq().isConnected() || profile.connectionIsActive()));
//    }

//    static private void addStr(ChoiceGroupEx chs, String lngStr) {
//        String[] strings = Util.explode(lngStr, '|');
//        String item;
//        for (int i = 0; i < strings.length; i++) {
//            try {
//                item = ResourceBundle.getString(strings[i]);
//            } catch (Exception e) {
//                item = strings[i];
//            }
//            chs.append(item, null);
//        }
//    }

    //    public void initOptionsList() {
    //        optionsMenu.clear();
    //        optionsMenu.append(ResourceBundle.getString("profiles"), OPTIONS_ACCOUNT);
    //        optionsMenu.append("ICQ", OPTIONS_ICQ);
    //        optionsMenu.append(ResourceBundle.getString("options_network"),OPTIONS_NETWORK);
    //// #sijapp cond.if modules_PROXY is "true"#
    //        if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_PROXY) {
    //            optionsMenu.append(ResourceBundle.getString("proxy"), OPTIONS_PROXY);
    //        }
    //// #sijapp cond.end#
    //        optionsMenu.append(ResourceBundle.getString("options_interface"), OPTIONS_INTERFACE);
    //        // #sijapp cond.if modules_TOOLBAR is "true"#
    //        if (Jimm.isTouch()) {
    //            optionsMenu.append(ResourceBundle.getString("tool_bar"), OPTIONS_TOOLBAR);
    //        }
    //        // #sijapp cond.end#
    //        optionsMenu.append(ResourceBundle.getString("options_trans"), OPTIONS_TRANS);
    //        optionsMenu.append(ResourceBundle.getString("color_scheme_lng"), OPTIONS_COLOR_SCHEME);
    //        optionsMenu.append(ResourceBundle.getString("skins_cs"), OPTIONS_SKINS);
    ////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    //        optionsMenu.append(ResourceBundle.getString("cl_chat"), OPTIONS_CLCHAT);
    ////#sijapp cond.end#
    //        optionsMenu.append(ResourceBundle.getString("options_hotkeys"), OPTIONS_HOTKEYS);
    //        optionsMenu.append(ResourceBundle.getString("options_signaling"), OPTIONS_SIGNALING);
    //        optionsMenu.append(ResourceBundle.getString("antispam"), OPTIONS_ANTISPAM);
    //        optionsMenu.append(ResourceBundle.getString("misc"), OPTIONS_MISC);
    ////#sijapp cond.if modules_FILES is "true"#
    //        optionsMenu.append("import_export", OPTIONS_IMP_EXP);
    //// #sijapp cond.end#
    //        optionsMenu.setCurrentItem(lastIndex);
    //        optionsMenu.setMenuListener(this);
    //    }

    static private LineChoise createLine(String cap, String items, int optValue) {
        return createLine(cap, items, Options.getInt(optValue), true);
    }

    static private LineChoise createLine(String cap, String items, int value, boolean translate) {
        return createLine(cap, items, value, translate, false);
    }

//    static private LineChoise createLine(String cap, String items, byte optValue, boolean translate) {
//        return createLine(cap, items, Options.getOptionsForm().icq.getProfile().getInt(optValue), translate, false);
//    }

    static public LineChoise createLine(String cap, String items, int value, boolean translate, boolean easy) {
        String[] strings = Util.explode(items, '|');
        if (translate) {
            for (int i = 0; i < strings.length; i++) {
                try {
                    strings[i] = ResourceBundle.getString(strings[i]);
                } catch (Exception ignored) {
                }
            }
        }
        LineChoise lct = new LineChoise(ResourceBundle.getString(cap), strings);
        if (!easy) {
            lct.setSelected(value);
        }
        return lct;
    }

    static public LineChoiseBoolean createLineBoo(String cap, int optValue) {
        return createLineBoo(cap, Options.getBoolean(optValue));
    }


    static public LineChoiseBoolean createLineBoo(String cap, byte optValue) {
        return createLineBoo(cap, Jimm.getCurrentProfile().getBoolean(optValue));
    }

    static public LineChoiseBoolean createLineBoo(String cap, boolean value) {
        return new LineChoiseBoolean(ResourceBundle.getString(cap), value);
    }

    public void itemStateChanged(final Object item) {
        //#sijapp cond.if modules_FILES is "true"#
        Jimm.setPrevScreen(optionsForm);
        if ((item == lineInt[Options.OPTION_SKIN]) && (lineInt[Options.OPTION_SKIN].getSelected() == 1)) {
            String[] tmp = Util.explode(Options.getString(Options.OPTION_SKIN_PATH), '/');
            String dir = (tmp.length < 2) ? null : getDir(tmp, tmp.length - 1);
            FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME, null);
            ft.startFT(dir);
        } else if ((item == graphicLine) && (graphicLine.getBooolean())) {
            String[] tmp = Util.explode(Options.getString(Options.OPTION_ICONS_PREFIX), '/');
            String dir = (tmp.length < 2) ? null : getDir(tmp, tmp.length - 2);
            FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME, null, false);
            ft.startFT(dir);
        } else if (item == csFs || item == optionsFs || item == fullFs || item == extraFs) {
            boolean dir = ((LineChoise) item).getSelected() == FS_EXPORT;
            fsEx = (LineChoise) item;
            FileBrowser fb = new FileBrowser();
            fb.setListener(this);
            fb.setParameters(dir);
            fb.activate();
        } //else
        //#sijapp cond.end#
        /* if (item instanceof ColorIcon) {
          colorItemSelected();
      } else */
//            if (uinTextField != null) {
//                int accCount = uinTextField.length;
//                if (accCount != 1) {
//                    for (int i = 0; i < accCount; i++) {
//                        if (uinTextField[i] != item) continue;
//                        lineInt[Options.OPTIONS_CURR_ACCOUNT].items[i] = checkUin(uinTextField[i].getString());
//                        //choiceCurAccount.set(i, checkUin(uinTextField[i].getString()), null);
//                        return;
//                    }
//                }
//            }
    }

    public void run() {
        OptionsForm of;
        FileSystem fs;
        InputStream inputstream;
        OutputStream outputstream;
        DataInputStream datainputstream;
        DataOutputStream dataoutputstream;
        of = this;
        fs = FileSystem.getInstance();
        inputstream = null;
        outputstream = null;
        datainputstream = null;
        dataoutputstream = null;
        try {
            if (fsEx.getSelected() == FS_IMPORT) {
                if (fsEx == csFs && eiFile.indexOf(".txt") != -1) {
                    fs.openFile(eiFile);
                    inputstream = fs.openInputStream();
                    datainputstream = new DataInputStream(inputstream);
                    Options.txtToCS(datainputstream);
                    Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
                    JimmUI.setColorScheme();
                } else if (fsEx == optionsFs && eiFile.indexOf(".opt") != -1) {
                    fs.openFile(eiFile);
                    inputstream = fs.openInputStream();
                    datainputstream = new DataInputStream(inputstream);
                    Options.load(datainputstream);
                    Options.initAccounts();
                    Options.safe_save();
                    importFinish();
                } else if (fsEx == extraFs && eiFile.indexOf(".exr") != -1) {
                    fs.openFile(eiFile);
                    inputstream = fs.openInputStream();
                    datainputstream = new DataInputStream(inputstream);
                    try {
                        icq.getProfile().loadContacts(datainputstream);
                    } catch (Exception ignored) {
                    }
                } else if (fsEx == fullFs && eiFile.indexOf(Options.getIOFormat()) != -1) {
                    // Exception 'java.io.EOFException' occurred at java.io.DataInputStream.readUnsignedShort(DataInputStream.java:256)
                    fs.openFile(eiFile);
                    inputstream = fs.openInputStream();
                    datainputstream = new DataInputStream(inputstream);
                    Options.startImport(datainputstream);
                    importFinish();
                }
                if (datainputstream != null) {
                    datainputstream.close();
                    inputstream.close();
                    of.reset(fs);
                    activate();
                    return;
                }
            }

            if (fsEx.getSelected() == FS_EXPORT) {
                if (fsEx == csFs) {
                    fs.openFile(eiFile + "color_scheme.txt");
                    outputstream = fs.openOutputStream();
                    dataoutputstream = new DataOutputStream(outputstream);
                    //dataoutputstream.write(CanvasEx.getColorsByte()); // todo color scheme raw
                    //dataoutputstream.write(NativeCanvas.getInst().screenShot()); // todo screenshot raw

                    Options.csToTxt(dataoutputstream);
/*                    dataoutputstream.close();
                    outputstream.close();
                    fs.close();
                    fs.openFile(eiFile + "about_develop_dec");
                    outputstream = fs.openOutputStream();
                    dataoutputstream = new DataOutputStream(outputstream);
                    byte dec[] = Util.decipherPassword(Util.stringToByteArray(ResourceBundle.getString("about_develop_dec")));
                    dataoutputstream.write(dec);
                    dataoutputstream.close();
                    outputstream.close();
                    fs.close();


                    fs.openFile(eiFile + "about_command_dec");
                    outputstream = fs.openOutputStream();
                    dataoutputstream = new DataOutputStream(outputstream);
                    dec = Util.decipherPassword(Util.stringToByteArray(ResourceBundle.getString("about_command_dec")));
                    dataoutputstream.write(dec);*/
                } else if (fsEx == optionsFs) {
                    fs.openFile(eiFile + "options.opt");
                    outputstream = fs.openOutputStream();
                    dataoutputstream = new DataOutputStream(outputstream);
                    //                    int[] fff = CanvasEx.getColors();
                    //                    for (int i = 0; i < fff.length; i++) {
                    //                       byte r1 = (byte)((fff[i] & 0xFF0000) >> 16), g1 = (byte)((fff[i] & 0x00FF00) >> 8), b1 = (byte)(fff[i] & 0x0000FF);
                    //                       dataoutputstream.writeByte(r1);
                    //                       dataoutputstream.writeByte(g1);
                    //                       dataoutputstream.writeByte(b1);
                    //                    }
                    Options.safe_save(dataoutputstream, true); // todo зависание приложения
                } else if (fsEx == extraFs) {
                    fs.openFile(eiFile + "extras_" + icq.getUin() + ".exr");
                    outputstream = fs.openOutputStream();
                    dataoutputstream = new DataOutputStream(outputstream);
                    icq.getProfile().saveContacts(dataoutputstream);
                } else if (fsEx == fullFs) {
                    fs.openFile(eiFile + "saved_jimm." + Options.getIOFormat());
                    outputstream = fs.openOutputStream();
                    dataoutputstream = new DataOutputStream(outputstream);
                    Options.startExport(dataoutputstream);
                }
                if (dataoutputstream != null) {
                    dataoutputstream.close();
                    outputstream.close();
                    of.reset(fs);
                    activate();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
//            if (type == VirtualList.KEY_PRESSED) {
//                switch (gameAct) {
//                    case Canvas.KEY_STAR: case 11:
//                        menuSelect(null, COLORS_COPY);
//                        break;
//
//                    case Canvas.KEY_POUND: case 12:
//                        menuSelect(null, COLORS_PASTE);
//                        break;
//                }
//            }
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
// #sijapp cond.if modules_DEBUGLOG is "true"#
        colorItemSelected();
// #sijapp cond.end#
    }

    //    static private ChoiceGroupEx createSelector(String cap, String items, int optValue) {
    //        ChoiceGroupEx chs = createSelector(cap, items);
    //        chs.setSelectedIndex(Options.getInt(optValue) % chs.size(), true);
    //        return chs;
    //    }

    //    static public ChoiceGroupEx createSelector(String cap, String items) {
    //        int choiceType = Choice.POPUP;
    //        ChoiceGroupEx chs = new ChoiceGroupEx(ResourceBundle.getString(cap), choiceType);
    //        addStr(chs, items);
    //        return chs;
    //    }

    //
    //    static private void setChecked(ChoiceGroupEx chs, String lngStr, boolean value) {
    //        addStr(chs, lngStr);
    //        chs.setSelectedIndex(chs.size() - 1, value);
    //    }

    //    static private void setChecked(ChoiceGroupEx chs, String lngStr, int optValue) {
    //        setChecked(chs, lngStr, Options.getBoolean(optValue));
    //    }
    //
    //    private Image getImage(Icon icon) {
    //        return (icon == null) ? null : icon.getImg();
    //    }

    public void menuSelect(Menu menu, byte action) {
        clearForm();
        lastIndex = optionsMenu.getCurrIndex();
        switch (action) {
// #sijapp cond.if modules_DEBUGLOG is "true"#
            case OPTIONS_COLORS:
                colorTextList = new TextList(ResourceBundle.getString("color_scheme_lng"));
                colorTextList.setMode(VirtualList.MODE_TEXT);
                fillColorTextList();
                return;
// #sijapp cond.end#

//#sijapp cond.if modules_SOUNDS is "true"#
            case OPTIONS_SOUNDS:
                messageSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.getInt(Options.OPTION_NOTIF_MESSAGE_VOL) / 10); // todo act
                messageNotificationSoundfileTextField = new TextField(ResourceBundle.getString("file"), Options.getString(Options.OPTION_MESS_NOTIF_FILE), 32, TextField.ANY);
                onlineSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.getInt(Options.OPTION_NOTIF_ONLINE_VOL) / 10);
                onlineNotificationSoundfileTextField = new TextField(ResourceBundle.getString("file"), Options.getString(Options.OPTION_ONLINE_NOTIF_FILE), 32, TextField.ANY);
                offlineSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.getInt(Options.OPTION_NOTIF_OFFLINE_VOL) / 10);
                offlineNotificationSoundfileTextField = new TextField(ResourceBundle.getString("file"), Options.getString(Options.OPTION_OFFLINE_NOTIF_FILE), 32, TextField.ANY);
                typingSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.getInt(Options.OPTION_NOTIF_TYPING_VOL) / 10);
                typingNotificationSoundfileTextField = new TextField(ResourceBundle.getString("file"), Options.getString(Options.OPTION_TYPING_NOTIF_FILE), 32, TextField.ANY);

                optionsForm.append(messageSoundVolume);
                optionsForm.append(messageNotificationSoundfileTextField);
                optionsForm.append(onlineSoundVolume);
                optionsForm.append(onlineNotificationSoundfileTextField);
                optionsForm.append(offlineSoundVolume);
                optionsForm.append(offlineNotificationSoundfileTextField);
                optionsForm.append(typingSoundVolume);
                optionsForm.append(typingNotificationSoundfileTextField);
                break;
//#sijapp cond.end#

            case OPTIONS_VIBRATOR:
                lineInt[Options.OPTION_VIBRATOR] = createLine("on_lng", "no" + "|" + "yes" + "|" + "when_locked" + "|" + "two_vibra", Options.OPTION_VIBRATOR);
                vibroTimeTextField = new TextField(ResourceBundle.getString("vibro_time"), Integer.toString(Options.getInt(Options.OPTION_VIBRO_TIME)), 5, TextField.NUMERIC);
                try {
                    if (Class.forName("com.nokia.mid.ui.DeviceControl") != null)
                        vibroFraq = new Gauge(ResourceBundle.getString("vibro_fraq"), true, 10, Options.getInt(Options.OPTION_VIBRO_FRAQ) / 10);
                } catch (ClassNotFoundException ignored) {
                }

                optionsForm.append(lineInt[Options.OPTION_VIBRATOR]);
                optionsForm.append(vibroTimeTextField);
                if (vibroFraq != null) {
                    optionsForm.append(vibroFraq);
                }
                break;

            case OPTIONS_CHAT:
                lineInt[Options.OPTION_FONT_SIZE_CHAT] = createLine("size_font", getFunctionFontHeight(), Options.getInt(Options.OPTION_FONT_SIZE_CHAT), false);
                lineOnOff[Options.OPTION_SWAP_SEND] = createLineBoo("swap_send", Options.OPTION_SWAP_SEND);
                lineOnOff[Options.OPTION_EMPTY_TITLE] = createLineBoo("empty_title", Options.OPTION_EMPTY_TITLE);
                lineOnOff[Options.OPTION_CREEPING_LINE] = createLineBoo("creeping_line", Options.OPTION_CREEPING_LINE);
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                lineInt[Options.OPTION_CLASSIC_CHAT] = createLine("cl_chat", "no" + "|" + "graphic", Options.OPTION_CLASSIC_CHAT);
                lineOnOff[Options.OPTION_FT_SELFTRAIN] = createLineBoo("ft_selftrain", Options.OPTION_FT_SELFTRAIN);
//#sijapp cond.end#
                templMessChat = new TextField(ResourceBundle.getString("message_templare"), Options.getString(Options.OPTION_MESSAGE_TEMPLARE), 100, TextField.ANY);

                optionsForm.append(lineInt[Options.OPTION_FONT_SIZE_CHAT]);
                optionsForm.append(lineOnOff[Options.OPTION_SWAP_SEND]);
                optionsForm.append(lineOnOff[Options.OPTION_EMPTY_TITLE]);
                optionsForm.append(lineOnOff[Options.OPTION_CREEPING_LINE]);
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                optionsForm.append(lineInt[Options.OPTION_CLASSIC_CHAT]);
                optionsForm.append(lineOnOff[Options.OPTION_FT_SELFTRAIN]);
//#sijapp cond.end#
                optionsForm.append(templMessChat);
                break;

//#sijapp cond.if modules_HISTORY is "true"#
            case OPTIONS_HISTORY:
                lineOnOff[Options.OPTION_HISTORY] = createLineBoo("use_history", Options.OPTION_HISTORY);
                lineOnOff[Options.OPTION_SHOW_LAST_MESS] = createLineBoo("show_prev_mess", Options.OPTION_SHOW_LAST_MESS);

                optionsForm.append(lineOnOff[Options.OPTION_HISTORY]);
                optionsForm.append(lineOnOff[Options.OPTION_SHOW_LAST_MESS]);
                break;
//#sijapp cond.end#

// #sijapp cond.if modules_SMILES is "true"#
            case OPTIONS_SMILES:
                lineOnOff[Options.OPTION_USE_SMILES] = createLineBoo("insert_emotion", Options.OPTION_USE_SMILES);
                emotionsSize = new TextField(ResourceBundle.getString("emotion_size"), String.valueOf(Options.getInt(Options.OPTION_EMOTION_SIZE)), 3, TextField.NUMERIC);

                optionsForm.append(lineOnOff[Options.OPTION_USE_SMILES]);
                optionsForm.append(emotionsSize);
                break;
// #sijapp cond.end#

            case OPTIONS_SYSTEM:
                if (ResourceBundle.langAvailable.length > 1) {
                    StringBuffer sb = new StringBuffer();
                    int langAct = 0;
                    for (int j = 0; j < ResourceBundle.langAvailable.length; j++) {
                        if (j != 0) {
                            sb.append('|');
                        }
                        sb.append(ResourceBundle.langAvailable[j]);
                        if (ResourceBundle.langAvailable[j].equals(Options.getString(Options.OPTION_UI_LANGUAGE))) {
                            langAct = j;
                        }
                    }
                    languageLine = createLine("language", sb.toString(), Options.getInt(Options.OPTION_UI_LANGUAGE), true, true);
                    languageLine.setSelected(langAct);
                    optionsForm.append(languageLine);
                }
                lineOnOff[Options.OPTION_CP1251_HACK] = createLineBoo("cp1251", Options.OPTION_CP1251_HACK);
                StringBuffer buf = new StringBuffer();
                for (int i = -12; i <= 13; i++) {
                    if (i != -12) {
                        buf.append('|');
                    }
                    buf.append("GMT");
                    if (i > 0) {
                        buf.append('+');
                    }
                    buf.append(i).append(":00");
                }
                lineInt[Options.OPTIONS_GMT_OFFSET] = createLine("time_zone", buf.toString(), Options.getInt(Options.OPTIONS_GMT_OFFSET), false, true);
                lineInt[Options.OPTIONS_GMT_OFFSET].setSelected(Options.getInt(Options.OPTIONS_GMT_OFFSET) + 12);
                buf.setLength(0);
                int[] currDateTime = DateAndTime.createDate(DateAndTime.createCurrentDate(false));
                int minutes = currDateTime[DateAndTime.TIME_MINUTE];
                int hour = currDateTime[DateAndTime.TIME_HOUR];
                for (int i = 0; i < 24; i++) {
                    if (i != 0) {
                        buf.append('|');
                    }
                    buf.append(i).append(":").append(Util.makeTwo(minutes));
                }
                lineInt[Options.OPTIONS_LOCAL_OFFSET] = createLine("local_time", buf.toString(), Options.getInt(Options.OPTIONS_LOCAL_OFFSET), false, true); // Options.getInt(2) - любое небольшое число
                lineInt[Options.OPTIONS_LOCAL_OFFSET].setSelected(hour);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                lineOnOff[Options.OPTION_BRING_UP] = createLineBoo("bring_up", Options.OPTION_BRING_UP);
                enterPasswordTextField = new TextField(ResourceBundle.getString("enter_startup_pass"), Options.getString(Options.OPTION_ENTER_PASSWORD), 20, TextField.PASSWORD);

                if (languageLine != null) {
                    optionsForm.append(languageLine);
                }
                optionsForm.append(lineOnOff[Options.OPTION_CP1251_HACK]);
                optionsForm.append(lineInt[Options.OPTIONS_GMT_OFFSET]);
                optionsForm.append(lineInt[Options.OPTIONS_LOCAL_OFFSET]);
                optionsForm.append(lineOnOff[Options.OPTION_BRING_UP]);
                optionsForm.append(enterPasswordTextField);
                break;

            case OPTIONS_CONTACTLIST:
                lineInt[Options.OPTION_FONT_SIZE_CL] = createLine("size_font", getFunctionFontHeight(), Options.getInt(Options.OPTION_FONT_SIZE_CL), false);
                lineInt[Options.OPTION_POLES] = createLine("poles", "1" + "|" + "2" + "|" + "3" + "|" + "4" + "|" + "5", Options.getInt(Options.OPTION_POLES), false);
                lineInt[Options.OPTION_POLES].setSelected(Options.getInt(Options.OPTION_POLES) - 1);
                lineInt[Options.OPTION_CL_SORT_BY] = createLine("sort_by", "sort_by_off_on" + "|" + "sort_by_name" + "|" + "sort_by_status" + "|" + "sort_by_status_chat", Options.OPTION_CL_SORT_BY);
                lineOnOff[Options.OPTION_CLIENT_ICON] = createLineBoo("client_icons", Options.OPTION_CLIENT_ICON);
                lineOnOff[Options.OPTION_RIGHT_XTRAZ] = createLineBoo("right_xtraz", Options.OPTION_RIGHT_XTRAZ);
                lineInt[Options.OPTION_TYPING_MODE] = createLine("typing_notify", "no" + "|" + "typing_display_only", Options.OPTION_TYPING_MODE);
                lineOnOff[Options.OPTION_SHOW_OFFLINE] = createLineBoo("hide_offline", Options.OPTION_SHOW_OFFLINE);
                lineOnOff[Options.OPTION_USER_GROUPS] = createLineBoo("show_user_groups", Options.OPTION_USER_GROUPS);
                lineOnOff[Options.OPTION_CL_HIDE_EGROUPS] = createLineBoo("hide_empty_groups", Options.OPTION_CL_HIDE_EGROUPS);
                lineOnOff[Options.OPTION_CACHE_CONTACTS] = createLineBoo("cache_contacts", Options.OPTION_CACHE_CONTACTS);
                lineOnOff[Options.OPTION_SAVE_TEMP_CONTACTS] = createLineBoo("save_temp_contacts", Options.OPTION_SAVE_TEMP_CONTACTS);
                lineOnOff[Options.OPTION_ON_MESS_FOCUS] = createLineBoo("on_mess_focus", Options.OPTION_ON_MESS_FOCUS);
//#sijapp cond.if modules_SOUNDS is "true"#
                lineOnOff[Options.OPTION_ONLINE_BLINK_NICK] = createLineBoo("blink_nick", Options.OPTION_ONLINE_BLINK_NICK);
                lineOnOff[Options.OPTION_ONLINE_BLINK_ICON] = createLineBoo("blink_icon", Options.OPTION_ONLINE_BLINK_ICON);
                blinkOnlineTimeTextField = new TextField(ResourceBundle.getString("blink_time"), String.valueOf(Options.getInt(Options.OPTION_ONLINE_BLINK_TIME)), 3, TextField.NUMERIC);
//#sijapp cond.end#
                tfCaptionShift = new TextField(ResourceBundle.getString("caption_shift"), String.valueOf(Options.getInt(Options.OPTION_CAPTION_SHIFT)), 2, TextField.NUMERIC);

                optionsForm.append(lineInt[Options.OPTION_FONT_SIZE_CL]);
                optionsForm.append(lineInt[Options.OPTION_POLES]);
                optionsForm.append(lineInt[Options.OPTION_CL_SORT_BY]);
                optionsForm.append(lineOnOff[Options.OPTION_CLIENT_ICON]);
                optionsForm.append(lineOnOff[Options.OPTION_RIGHT_XTRAZ]);
                optionsForm.append(lineInt[Options.OPTION_TYPING_MODE]);
                optionsForm.append(lineOnOff[Options.OPTION_SHOW_OFFLINE]);
                optionsForm.append(lineOnOff[Options.OPTION_USER_GROUPS]);
                optionsForm.append(lineOnOff[Options.OPTION_CL_HIDE_EGROUPS]);
                optionsForm.append(lineOnOff[Options.OPTION_CACHE_CONTACTS]);
                optionsForm.append(lineOnOff[Options.OPTION_SAVE_TEMP_CONTACTS]);
                optionsForm.append(lineOnOff[Options.OPTION_ON_MESS_FOCUS]);
//#sijapp cond.if modules_SOUNDS is "true"# 
                optionsForm.append(lineOnOff[Options.OPTION_ONLINE_BLINK_NICK]);
                optionsForm.append(lineOnOff[Options.OPTION_ONLINE_BLINK_ICON]);
                optionsForm.append(blinkOnlineTimeTextField);
//#sijapp cond.end#
                optionsForm.append(tfCaptionShift);
                break;

            case OPTIONS_VIEW:
                StringBuffer skins = new StringBuffer("---");
//#sijapp cond.if modules_FILES="true"#
                skins.append('|').append(ResourceBundle.getString("skins_from_fs"));
//#sijapp cond.end#
                if (skinsName != null) {
                    for (int i = 0; i < skinsName.length; i++) {
                        skins.append('|').append(skinsName[i]);
                    }
                }
                lineInt[Options.OPTION_SKIN] = createLine("skins", skins.toString(), Options.getInt(Options.OPTION_SKIN), false);
                if (Options.getString(Options.OPTION_SKIN_PATH).indexOf('/') == 0) {
                    lineInt[Options.OPTION_SKIN].setSelected(1);
                }
                lineOnOff[Options.OPTION_BARS_FROM_SKIN] = createLineBoo("bars_from_skin", Options.OPTION_BARS_FROM_SKIN);
                lineOnOff[Options.OPTION_COLORS_FROM_SKIN] = createLineBoo("color_scheme_from_skin", Options.OPTION_COLORS_FROM_SKIN);
                skins.setLength(0);
                skins.append("---");
                if (schemeName != null) {
                    for (int i = 0; i < schemeName.length; i++) {
                        skins.append('|').append(schemeName[i]);
                    }
                }
                shemesLine = createLine("color_scheme_lng", skins.toString(), 0, false, true);
                iconCanvasGauge = new Gauge(ResourceBundle.getString("icon_canvas"), true, 25, Options.getInt(Options.OPTION_ICONS_CANVAS) / 4);
//#sijapp cond.if modules_GFONT="true"#
                skins = new StringBuffer("System");
                if (fontName != null) {
                    for (int i = 0; i < fontName.length; i++) {
                        skins.append('|').append(fontName[i]);
                    }
                }
                lineInt[Options.OPTION_GFONTS] = createLine("gfonts", skins.toString(), Options.getInt(Options.OPTION_GFONTS), false);
//#sijapp cond.end#
//                lineInt[Options.OPTION_FONT_SIZE_CL] = createLine("size_font", "small" + "|" + "medium" + "|" + "large", Options.OPTION_FONT_SIZE_CL);
                lineInt[Options.OPTION_LUSTER] = createLine("luster", "no" + "|" + "panels", Options.OPTION_LUSTER);
                lineOnOff[Options.OPTION_SOFT_BAR] = createLineBoo("soft_bar", Options.OPTION_SOFT_BAR);
                lineOnOff[Options.OPTION_GRADIENT_MB] = createLineBoo("smooth_softbar", Options.OPTION_GRADIENT_MB);
                lineOnOff[Options.OPTION_ANIMATION] = createLineBoo("slidingwin", Options.OPTION_ANIMATION);
//#sijapp cond.if modules_FILES="true"#
                lineOnOff[Options.OPTION_SHOW_SIZE] = createLineBoo("show_file_size", Options.OPTION_SHOW_SIZE);
//#sijapp cond.end#
                cursorTransValue = new Gauge(ResourceBundle.getString("cursor_trans"), true, 10, Options.getInt(Options.OPTION_CURSOR_TRANS));
                capTransValue = new Gauge(ResourceBundle.getString("cap_trans"), true, 10, Options.getInt(Options.OPTION_CAP_TRANS));
                barTransValue = new Gauge(ResourceBundle.getString("menu_bar_trans"), true, 10, Options.getInt(Options.OPTION_BAR_TRANS));
                menuTransValue = new Gauge(ResourceBundle.getString("menu_trans"), true, 10, Options.getInt(Options.OPTION_MENU_TRANS));
                splashTransValue = new Gauge(ResourceBundle.getString("splash_trans"), true, 10, Options.getInt(Options.OPTION_SPLASH_TRANS));
                blackOutGauge = new Gauge(ResourceBundle.getString("blackout"), true, 10, Options.getInt(Options.OPTION_BLACKOUT));

                optionsForm.append(lineInt[Options.OPTION_SKIN]);
                optionsForm.append(lineOnOff[Options.OPTION_BARS_FROM_SKIN]);
                optionsForm.append(lineOnOff[Options.OPTION_COLORS_FROM_SKIN]);
                optionsForm.append(shemesLine);
                optionsForm.append(iconCanvasGauge);
//#sijapp cond.if modules_GFONT="true"#
                optionsForm.append(lineInt[Options.OPTION_GFONTS]);
//#sijapp cond.end#
                //optionsForm.append(lineInt[Options.OPTION_FONT_SIZE_CL]);
                optionsForm.append(lineInt[Options.OPTION_LUSTER]);
                optionsForm.append(lineOnOff[Options.OPTION_SOFT_BAR]);
                optionsForm.append(lineOnOff[Options.OPTION_GRADIENT_MB]);
                optionsForm.append(lineOnOff[Options.OPTION_ANIMATION]);
//#sijapp cond.if modules_FILES="true"#
                optionsForm.append(lineOnOff[Options.OPTION_SHOW_SIZE]);
//#sijapp cond.end#
                optionsForm.append(cursorTransValue);
                optionsForm.append(capTransValue);
                optionsForm.append(barTransValue);
                optionsForm.append(menuTransValue);
                optionsForm.append(splashTransValue);
                optionsForm.append(blackOutGauge);
                break;

//#sijapp cond.if modules_LIGHT is "true"#            
            case OPTIONS_LIGHT:
                lightTimeout = new TextField(ResourceBundle.getString("backlight_timeout"), String.valueOf(Options.getInt(Options.OPTION_LIGHT_TIMEOUT)), 3, TextField.NUMERIC);
                lightManual = createLineBoo("color_scheme_from_skin", Options.OPTION_LIGHT_MANUAL);

                optionsForm.append(lightTimeout);
                optionsForm.append(lightManual);
                break;
// #sijapp cond.end#

            case OPTIONS_HOTKEYS:
                HotkeyFormInit();
                break;

// #sijapp cond.if modules_PROXY is "true"#
            case OPTIONS_PROXY:
                lineInt[Options.OPTION_PRX_TYPE] = createLine("type_prx", "four_prx" + "|" + "five_prx" + "|" + "guess_prx", Options.OPTION_PRX_TYPE);
                srvProxyHostTextField = new TextField(ResourceBundle.getString("host_prx"), Options.getString(Options.OPTION_PRX_SERV), 32, TextField.ANY);
                srvProxyPortTextField = new TextField(ResourceBundle.getString("port_prx"), Options.getString(Options.OPTION_PRX_PORT), 5, TextField.NUMERIC);
                srvProxyLoginTextField = new TextField(ResourceBundle.getString("login_prx"), Options.getString(Options.OPTION_PRX_NAME), 32, TextField.ANY);
                srvProxyPassTextField = new TextField(ResourceBundle.getString("pass_prx"), Options.getString(Options.OPTION_PRX_PASS), 32, TextField.PASSWORD);
                connAutoRetryTextField = new TextField(ResourceBundle.getString("retry_count"), Options.getString(Options.OPTION_AUTORETRY_COUNT), 5, TextField.NUMERIC);

                optionsForm.append(lineInt[Options.OPTION_PRX_TYPE]);
                optionsForm.append(srvProxyHostTextField);
                optionsForm.append(srvProxyPortTextField);
                optionsForm.append(srvProxyLoginTextField);
                optionsForm.append(srvProxyPassTextField);
                optionsForm.append(connAutoRetryTextField);
                break;
// #sijapp cond.end#

            case OPTIONS_NETWORK:
                StringBuffer typeconn = new StringBuffer("socket");
//#sijapp cond.if modules_HTTP is "true"#
                typeconn.append('|').append("http");
//#sijapp cond.end#
// #sijapp cond.if modules_PROXY is "true"#
                typeconn.append('|').append("proxy");
// #sijapp cond.end#
                lineInt[Options.OPTION_CONN_TYPE] = createLine("conn_type", typeconn.toString(), Options.OPTION_CONN_TYPE);
                srvHostTextField = new TextField(ResourceBundle.getString("server_host"), Options.getString(Options.OPTION_SRV_HOST), 255, TextField.ANY);
                srvPortTextField = new TextField(ResourceBundle.getString("server_port"), Options.getString(Options.OPTION_SRV_PORT), 5, TextField.NUMERIC);
                lineOnOff[Options.OPTION_MD5_LOGIN] = createLineBoo("md5_login", Options.OPTION_MD5_LOGIN);
                lineOnOff[Options.OPTION_AUTO_CONNECT] = createLineBoo("auto_connect", Options.OPTION_AUTO_CONNECT);
                lineOnOff[Options.OPTION_RECONNECT] = createLineBoo("reconnect", Options.OPTION_RECONNECT);
                reconnectNumberTextField = new TextField(ResourceBundle.getString("reconnect_number"), String.valueOf(Options.getInt(Options.OPTION_RECONNECT_NUMBER)), 2, TextField.NUMERIC);
                lineOnOff[Options.OPTION_CONN_PROP] = createLineBoo("async", Options.OPTION_CONN_PROP);
                lineOnOff[Options.OPTION_SHADOW_CON] = createLineBoo("shadow_con", Options.OPTION_SHADOW_CON);
                connAliveIntervTextField = new TextField(ResourceBundle.getString("timeout_interv"), Options.getString(Options.OPTION_CONN_ALIVE_INVTERV), 4, TextField.NUMERIC);

                optionsForm.append(lineInt[Options.OPTION_CONN_TYPE]);
                optionsForm.append(srvHostTextField);
                optionsForm.append(srvPortTextField);
                optionsForm.append(lineOnOff[Options.OPTION_MD5_LOGIN]);
                optionsForm.append(lineOnOff[Options.OPTION_AUTO_CONNECT]);
                optionsForm.append(lineOnOff[Options.OPTION_RECONNECT]);
                optionsForm.append(reconnectNumberTextField);
                optionsForm.append(lineOnOff[Options.OPTION_CONN_PROP]);
                optionsForm.append(lineOnOff[Options.OPTION_SHADOW_CON]);
                optionsForm.append(connAliveIntervTextField);
                break;

//#sijapp cond.if modules_PANEL is "true"#
            case OPTIONS_FAST_VIEW:
                lineInt[Options.OPTION_PANEL_ACTIVE] = createLine("panel_active", "no" + "|" + /*"pw_forme" + "|" + "pw_curpro" + "|" + "pw_pro" + "|" +*/ "pw_all", Options.OPTION_PANEL_ACTIVE);
                optionsForm.append(lineInt[Options.OPTION_PANEL_ACTIVE]);
                break;
//#sijapp cond.end#

            case OPTIONS_SEND_FILES:
                break;

            case OPTIONS_WAIT:
                lineOnOff[Options.OPTION_STATUS_AUTO] = createLineBoo("auto_status_enable", Options.OPTION_STATUS_AUTO);
                autoStatusDelayTimeTextField = new TextField(ResourceBundle.getString("auto_status_delay"), String.valueOf(Options.getInt(Options.OPTION_STATUS_DELAY)), 3, TextField.NUMERIC);
                lineOnOff[Options.OPTION_AUTOLOCK] = createLineBoo("autolock", Options.OPTION_AUTOLOCK);
                lineOnOff[Options.OPTION_STATUS_RESTORE] = createLineBoo("auto_status_restore", Options.OPTION_STATUS_RESTORE);

                optionsForm.append(lineOnOff[Options.OPTION_STATUS_AUTO]);
                optionsForm.append(autoStatusDelayTimeTextField);
                optionsForm.append(lineOnOff[Options.OPTION_AUTOLOCK]);
                optionsForm.append(lineOnOff[Options.OPTION_STATUS_RESTORE]);
                break;

//#sijapp cond.if modules_SBOLTUN is "true"#
            case OPTIONS_SBOLTUN:
                lineOnOff[Options.OPTION_SBOLTUN] = createLineBoo("on_lng", Options.OPTION_SBOLTUN);
                sBoltunSleep = new TextField(ResourceBundle.getString("sboltun_sleep"), String.valueOf(Options.getInt(Options.OPTION_SBOLTUN_SLEEP)), 2, TextField.NUMERIC);

                optionsForm.append(lineOnOff[Options.OPTION_SBOLTUN]);
                optionsForm.append(sBoltunSleep);
                break;
//#sijapp cond.end#

//#sijapp cond.if modules_TOOLBAR is "true"#
            case OPTIONS_TOOLBAR:
                lineOnOff[Options.OPTION_TOOLBAR] = createLineBoo("tool_bar", Options.OPTION_TOOLBAR);
                optionsForm.append(lineOnOff[Options.OPTION_TOOLBAR]);
                int ints[] = Util.explodeToInt(Options.getString(Options.OPTION_TOOLBAR_HASH), ';');
                lineToolBar = new LineChoise[16];
                for (int i = 0; i < 16; i++) {
                    lineToolBar[i] = createLine("# " + i, getHotkeyActionNames(), 0, true, true);
                    try {
                        lineToolBar[i].setSelected(ints[i]);
                    } catch (Exception ignored) {
                    }
                    optionsForm.append(lineToolBar[i]);
                }
                break;
//#sijapp cond.end#

//#sijapp cond.if modules_FILES="true"#
            case OPTIONS_SYNCHRONIZE:
                graphicLine = createLineBoo("icons_from_fs", false);
                if (Options.getString(Options.OPTION_ICONS_PREFIX).equals("/")) {
                    graphicLine.setSelected(0);
                } else {
                    graphicLine.setSelected(1);
                }
                csFs = createLine("color_scheme_lng", "import_lng" + "|" + "export_lng", 0, true, true);
                optionsFs = createLine("options_lng", "import_lng" + "|" + "export_lng", 0, true, true);
                extraFs = createLine("extra_menu", "import_lng" + "|" + "export_lng", 0, true, true);
                fullFs = createLine("reset_all", "import_lng" + "|" + "export_lng", 0, true, true);

                optionsForm.append(graphicLine);
                optionsForm.append(csFs);
                optionsForm.append(optionsFs);
                optionsForm.append(extraFs);
                optionsForm.append(fullFs);
                break;
//#sijapp cond.end #

            //case OPTIONS_ACCOUNT:
            //    readAccontsData();
            //    showAccountControls();
            //    break;

//            case OPTIONS_NETWORK:
//                srvHostTextField = new TextField(ResourceBundle.getString("server_host"), Options.getString(Options.OPTION_SRV_HOST), 255, TextField.ANY);
//                srvPortTextField = new TextField(ResourceBundle.getString("server_port"), Options.getString(Options.OPTION_SRV_PORT), 5, TextField.NUMERIC);
//
//                StringBuffer typeconn = new StringBuffer().append("socket");
//                //#sijapp cond.if modules_HTTP is "true"#
//                typeconn.append('|').append("http");
//                //#sijapp cond.end#
//                // #sijapp cond.if modules_PROXY is "true"#
//                typeconn.append('|').append("proxy");
//                // #sijapp cond.end#
//                lineInt[Options.OPTION_CONN_TYPE] = createLine("conn_type", typeconn.toString(), Options.OPTION_CONN_TYPE);
//                // #sijapp cond.if modules_PROXY is "true"#
//                // #sijapp cond.else#
//                lineInt[Options.OPTION_CONN_TYPE].setSelected(Options.getInt(Options.OPTION_CONN_TYPE) % 2);
//                // #sijapp cond.end#
//
//                //keepConnAliveChoiceGroup = new ChoiceGroupEx(null, Choice.MULTIPLE);
//                //setChecked(keepConnAliveChoiceGroup, "keep_conn_alive", Options.OPTION_KEEP_CONN_ALIVE);
//                lineOnOff[Options.OPTION_KEEP_CONN_ALIVE] = createLineBoo("keep_conn_alive", Options.OPTION_KEEP_CONN_ALIVE);
//                connAliveIntervTextField = new TextField(ResourceBundle.getString("timeout_interv"), Options.getString(Options.OPTION_CONN_ALIVE_INVTERV), 4, TextField.NUMERIC);
//
//                lineOnOff[Options.OPTION_MD5_LOGIN] = createLineBoo("md5_login", Options.OPTION_MD5_LOGIN);
//                lineOnOff[Options.OPTION_CONN_PROP] = createLineBoo("async", Options.OPTION_CONN_PROP);
//                lineOnOff[Options.OPTION_AUTO_CONNECT] = createLineBoo("auto_connect", Options.OPTION_AUTO_CONNECT);
//                lineOnOff[Options.OPTION_RECONNECT] = createLineBoo("reconnect", Options.OPTION_RECONNECT);
//                // #sijapp cond.if target isnot "MOTOROLA"#
//                lineOnOff[Options.OPTION_SHADOW_CON] = createLineBoo("shadow_con", Options.OPTION_SHADOW_CON);
//                // #sijapp cond.end#
//
//                //#sijapp cond.if modules_HTTP is "true"#
//                httpUserAgendTextField = new TextField(ResourceBundle.getString("http_user_agent"), Options.getString(Options.OPTION_HTTP_USER_AGENT), 256, TextField.ANY);
//                httpWAPProfileTextField = new TextField(ResourceBundle.getString("http_wap_profile"), Options.getString(Options.OPTION_HTTP_WAP_PROFILE), 256, TextField.ANY);
//                //#sijapp cond.end#
//                reconnectNumberTextField = new TextField(ResourceBundle.getString("reconnect_number"), String.valueOf(Options.getInt(Options.OPTION_RECONNECT_NUMBER)), 2, TextField.NUMERIC);
//
//                optionsForm.append(lineInt[Options.OPTION_CONN_TYPE]);
//                optionsForm.append(lineOnOff[Options.OPTION_KEEP_CONN_ALIVE]);
//                optionsForm.append(lineOnOff[Options.OPTION_RECONNECT]);
//                optionsForm.append(lineOnOff[Options.OPTION_MD5_LOGIN]);
//                optionsForm.append(lineOnOff[Options.OPTION_AUTO_CONNECT]);
//                optionsForm.append(lineOnOff[Options.OPTION_CONN_PROP]);
//                // #sijapp cond.if target isnot "MOTOROLA"#
//                optionsForm.append(lineOnOff[Options.OPTION_SHADOW_CON]);
//                // #sijapp cond.end#
//                //#sijapp cond.if modules_HTTP is "true"#
//                optionsForm.append(httpUserAgendTextField);
//                optionsForm.append(httpWAPProfileTextField);
//                //#sijapp cond.end#
//                optionsForm.append(srvHostTextField);
//                optionsForm.append(srvPortTextField);
//                optionsForm.append(connAliveIntervTextField);
//                optionsForm.append(reconnectNumberTextField);
//                break;
//
//
//            case OPTIONS_ICQ:
//                lineOnOffIcq[Profile.OPTION_DELIVERY_REPORTX] = createLineBoo("delivery_report", Profile.OPTION_DELIVERY_REPORT);
//                lineOnOff[Options.OPTION_CACHE_CONTACTS] = createLineBoo("cache_contacts", Options.OPTION_CACHE_CONTACTS);
//                lineOnOff[Options.OPTION_AUTO_ANSWER] = createLineBoo("auto_answer", Options.OPTION_AUTO_ANSWER);
//                lineOnOff[Options.OPTION_AUTO_XTRAZ] = createLineBoo("auto_xtraz", Options.OPTION_AUTO_XTRAZ);
//                lineOnOffIcq[Profile.OPTION_XTRAZ_ENABLEX] = createLineBoo("xTraz_enable_plus", Profile.OPTION_XTRAZ_ENABLE);
//                lineOnOffIcq[Profile.OPTION_MESS_NOTIF_TYPEX] = createLineBoo("dis_out_notif", Profile.OPTION_MESS_NOTIF_TYPE);
//                lineOnOffIcq[Profile.OPTION_WEBAWAREX] = createLineBoo("web_aware", Profile.OPTION_WEBAWARE);
//                lineOnOffIcq[Profile.OPTION_REQ_AUTHX] = createLineBoo("req_auth", Profile.OPTION_REQ_AUTH);
//                //#sijapp cond.if modules_MAGIC_EYE is "true"#
//                lineOnOffIcq[Profile.OPTION_ENABLE_MMX] = createLineBoo("enable_mm", Profile.OPTION_ENABLE_MM);
//                //#sijapp cond.end#
//                clientIdLine = createLine("client_id", getClientString(), Profile.OPTION_CLIENT_ID, false);
//
//                clientStringVersion = new TextField(ResourceBundle.getString("jimm_version"), icq.getProfile().getString(Profile.OPTION_STRING_VERSION), 11, TextField.ANY);
//
//                lineOnOff[Options.OPTION_AUTOLOCK] = createLineBoo("autolock", Options.OPTION_AUTOLOCK);
//                lineOnOff[Options.OPTION_STATUS_RESTORE] = createLineBoo("auto_status_restore", Options.OPTION_STATUS_RESTORE);
//                lineOnOff[Options.OPTION_STATUS_AUTO] = createLineBoo("auto_status_enable", Options.OPTION_STATUS_AUTO);
//
//                autoStatusDelayTimeTextField = new TextField(ResourceBundle.getString("auto_status_delay"), String.valueOf(Options.getInt(Options.OPTION_STATUS_DELAY)), 3, TextField.NUMERIC);
//
//                optionsForm.append(clientIdLine);
//                optionsForm.append(lineOnOffIcq[Profile.OPTION_DELIVERY_REPORTX]);
//                optionsForm.append(lineOnOff[Options.OPTION_CACHE_CONTACTS]);
//                optionsForm.append(lineOnOff[Options.OPTION_AUTO_ANSWER]);
//                optionsForm.append(lineOnOff[Options.OPTION_AUTO_XTRAZ]);
//                optionsForm.append(lineOnOffIcq[Profile.OPTION_XTRAZ_ENABLEX]);
//                optionsForm.append(lineOnOffIcq[Profile.OPTION_MESS_NOTIF_TYPEX]);
//                optionsForm.append(lineOnOffIcq[Profile.OPTION_WEBAWAREX]);
//                optionsForm.append(lineOnOffIcq[Profile.OPTION_REQ_AUTHX]);
//                //#sijapp cond.if modules_MAGIC_EYE is "true"#
//                optionsForm.append(lineOnOffIcq[Profile.OPTION_ENABLE_MMX]);
//                //#sijapp cond.end#
//                optionsForm.append(lineOnOff[Options.OPTION_AUTOLOCK]);
//                optionsForm.append(lineOnOff[Options.OPTION_STATUS_RESTORE]);
//                optionsForm.append(lineOnOff[Options.OPTION_STATUS_AUTO]);
//                optionsForm.append(clientStringVersion);
//                optionsForm.append(autoStatusDelayTimeTextField);
//                break;
//
//            case OPTIONS_INTERFACE:
//                if (ResourceBundle.langAvailable.length > 1) {
//                    StringBuffer sb = new StringBuffer();
//                    int langAct = 0;
//                    for (int j = 0; j < ResourceBundle.langAvailable.length; j++) {
//                        if (j != 0) {
//                            sb.append('|');
//                        }
//                        sb.append(ResourceBundle.langAvailable[j]);
//                        if (ResourceBundle.langAvailable[j].equals(Options.getString(Options.OPTION_UI_LANGUAGE))) {
//                            langAct = j;
//                        }
//                    }
//                    languageLine = createLine("language", sb.toString(), Options.getInt(Options.OPTION_UI_LANGUAGE), true, true);
//                    languageLine.setSelected(langAct);
//                    optionsForm.append(languageLine);
//                }
//
//                //maxTextSizeTF = new TextField(ResourceBundle.getString("max_text_size"), String.valueOf(Options.getInt(Options.OPTION_MAX_TEXT_SIZE)), 5, TextField.NUMERIC);
//                //optionsForm.append(maxTextSizeTF);
//
//                if (Jimm.is_phone_SE()) {
//                    //#sijapp cond.if (modules_SOUNDS is "true" | modules_PANEL is "true")#
//                    tfCaptionShift = new TextField(ResourceBundle.getString("caption_shift"), String.valueOf(Options.getInt(Options.OPTION_CAPTION_SHIFT)), 2, TextField.NUMERIC);
//                    optionsForm.append(tfCaptionShift);
//                    //#sijapp cond.end#
//                } else {
//                    tfCaptionShift = new TextField(ResourceBundle.getString("caption_shift"), String.valueOf(Options.getInt(Options.OPTION_CAPTION_SHIFT)), 2, TextField.NUMERIC);
//                    optionsForm.append(tfCaptionShift);
//                }
//
//                templMessChat = new TextField(ResourceBundle.getString("message_templare"), Options.getString(Options.OPTION_MESSAGE_TEMPLARE), 100, TextField.ANY);
//                optionsForm.append(templMessChat);
//
//                //animationConstant = new TextField(ResourceBundle.getString("slidingwin_const"), String.valueOf(Options.getInt(Options.OPTION_ANIMATION_CONST)), 5, TextField.NUMERIC);
//                //optionsForm.append(animationConstant);
//
//                // CL
//                lineOnOff[Options.OPTION_USER_GROUPS] = createLineBoo("show_user_groups", Options.OPTION_USER_GROUPS);
//                lineOnOff[Options.OPTION_CL_HIDE_EGROUPS] = createLineBoo("hide_empty_groups", Options.OPTION_CL_HIDE_EGROUPS);
//                lineOnOff[Options.OPTION_CL_HIDE_OFFLINE] = createLineBoo("hide_offline", Options.OPTION_CL_HIDE_OFFLINE);
//                //lineOnOff[Options.OPTION_AUTH_ICON] = createLineBoo("auth_icon", Options.OPTION_AUTH_ICON);
//                lineOnOff[Options.OPTION_RIGHT_XTRAZ] = createLineBoo("right_xtraz", Options.OPTION_RIGHT_XTRAZ);
//                lineOnOff[Options.OPTION_SAVE_TEMP_CONTACTS] = createLineBoo("save_temp_contacts", Options.OPTION_SAVE_TEMP_CONTACTS);
//                lineOnOff[Options.OPTION_CLIENT_ICON] = createLineBoo("client_icons", Options.OPTION_CLIENT_ICON);
//                lineOnOff[Options.OPTION_ON_MESS_FOCUS] = createLineBoo("on_mess_focus", Options.OPTION_ON_MESS_FOCUS);
//
//                lineInt[Options.OPTION_CL_SORT_BY] = createLine("sort_by", "sort_by_off_on" + "|" + "sort_by_name" + "|" + "sort_by_status" + "|" + "sort_by_status_chat", Options.OPTION_CL_SORT_BY);
//
//                lineInt[Options.OPTION_POLES] = createLine("poles", "1" + "|" + "2" + "|" + "3" + "|" + "4" + "|" + "5", Options.getInt(Options.OPTION_POLES), false);
//                lineInt[Options.OPTION_POLES].setSelected(Options.getInt(Options.OPTION_POLES) - 1);
//
//                optionsForm.append(lineInt[Options.OPTION_CL_SORT_BY]);
//                optionsForm.append(lineInt[Options.OPTION_POLES]);
//                optionsForm.append(lineOnOff[Options.OPTION_USER_GROUPS]);
//                optionsForm.append(lineOnOff[Options.OPTION_CL_HIDE_EGROUPS]);
//                optionsForm.append(lineOnOff[Options.OPTION_CL_HIDE_OFFLINE]);
//                //optionsForm.append(lineOnOff[Options.OPTION_AUTH_ICON]);
//                optionsForm.append(lineOnOff[Options.OPTION_RIGHT_XTRAZ]);
//                optionsForm.append(lineOnOff[Options.OPTION_CLIENT_ICON]);
//                optionsForm.append(lineOnOff[Options.OPTION_SAVE_TEMP_CONTACTS]);
//                optionsForm.append(lineOnOff[Options.OPTION_ON_MESS_FOCUS]);
//
//                // Chat
//                // #sijapp cond.if modules_SMILES is "true"#
//                lineOnOff[Options.OPTION_USE_SMILES] = createLineBoo("insert_emotion", Options.OPTION_USE_SMILES);
//                // #sijapp cond.end#
//                //#sijapp cond.if modules_HISTORY is "true"#
//                lineOnOff[Options.OPTION_HISTORY] = createLineBoo("use_history", Options.OPTION_HISTORY);
//                lineOnOff[Options.OPTION_SHOW_LAST_MESS] = createLineBoo("show_prev_mess", Options.OPTION_SHOW_LAST_MESS);
//                //#sijapp cond.end#
//                lineOnOff[Options.OPTION_SWAP_SEND] = createLineBoo("swap_send", Options.OPTION_SWAP_SEND);
//                lineOnOff[Options.OPTION_EMPTY_TITLE] = createLineBoo("empty_title", Options.OPTION_EMPTY_TITLE);
//                lineOnOff[Options.OPTION_CP1251_HACK] = createLineBoo("cp1251", Options.OPTION_CP1251_HACK);
//
//                // #sijapp cond.if modules_SMILES is "true"#
//                optionsForm.append(lineOnOff[Options.OPTION_USE_SMILES]);
//                // #sijapp cond.end#
//                //#sijapp cond.if modules_HISTORY is "true"#
//                optionsForm.append(lineOnOff[Options.OPTION_HISTORY]);
//                optionsForm.append(lineOnOff[Options.OPTION_SHOW_LAST_MESS]);
//                //#sijapp cond.end#
//                optionsForm.append(lineOnOff[Options.OPTION_SWAP_SEND]);
//                optionsForm.append(lineOnOff[Options.OPTION_EMPTY_TITLE]);
//                optionsForm.append(lineOnOff[Options.OPTION_CP1251_HACK]);
//
//                // Texts
//                lineInt[Options.OPTION_MESSAGE_TYPE] = createLine("messages_type", "normalFont" + "|" + "transliterate" + "|" + "detransliterate", Options.OPTION_MESSAGE_TYPE);
//                lineInt[Options.OPTION_USER_FONT] = createLine("font_style", "normalFont" + "|" + "boldFont" + "|" + "italicFont" + "|" + "underlinedFont", Options.OPTION_USER_FONT);
//                lineInt[Options.OPTION_FONT_SIZE] = createLine("size_font", "small" + "|" + "medium" + "|" + "large", Options.OPTION_FONT_SIZE);
//                lineInt[Options.OPTION_FONT_STYLE] = createLine("style_font", "normalFont" + "|" + "shadow_font", Options.OPTION_FONT_STYLE);
//
//                optionsForm.append(lineInt[Options.OPTION_MESSAGE_TYPE]);
//                optionsForm.append(lineInt[Options.OPTION_USER_FONT]);
//                optionsForm.append(lineInt[Options.OPTION_FONT_SIZE]);
//                optionsForm.append(lineInt[Options.OPTION_FONT_STYLE]);
//
//                // Other
//                lineOnOff[Options.OPTION_SOFT_BAR] = createLineBoo("soft_bar", Options.OPTION_SOFT_BAR);
//                //#sijapp cond.if modules_FILES="true"#
//                lineOnOff[Options.OPTION_SHOW_SIZE] = createLineBoo("show_file_size", Options.OPTION_SHOW_SIZE);
//                //#sijapp cond.end#
//                lineOnOff[Options.OPTION_COMBO_KEYS] = createLineBoo("combo_keys", Options.OPTION_COMBO_KEYS);
//                lineInt[Options.OPTION_LUSTER] = createLine("luster", "no" + "|" + "panels" + "|" + ResourceBundle.getString("full_luster"), Options.OPTION_LUSTER);
//                //lineInt[Options.OPTION_MENU_STYLE] = createLine("option", "style_22" + "|" + "style_23" + "|" + "style_22_img", Options.OPTION_MENU_STYLE);
//                lineOnOff[Options.OPTION_GRADIENT_MB] = createLineBoo("smooth_softbar", Options.OPTION_GRADIENT_MB);
//                lineOnOff[Options.OPTION_ANIMATION] = createLineBoo("slidingwin", Options.OPTION_ANIMATION);
//
//                optionsForm.append(lineOnOff[Options.OPTION_SOFT_BAR]);
//                //#sijapp cond.if modules_FILES="true"#
//                optionsForm.append(lineOnOff[Options.OPTION_SHOW_SIZE]);
//                //#sijapp cond.end#
//                optionsForm.append(lineOnOff[Options.OPTION_COMBO_KEYS]);
//                optionsForm.append(lineInt[Options.OPTION_LUSTER]);
//                //optionsForm.append(lineInt[Options.OPTION_MENU_STYLE]);
//                optionsForm.append(lineOnOff[Options.OPTION_GRADIENT_MB]);
//                optionsForm.append(lineOnOff[Options.OPTION_ANIMATION]);
//                //#sijapp cond.if modules_LIGHT is "true"#
//                //#		lightTimeout = new TextField(ResourceBundle.getString("backlight_timeout"), String.valueOf(Options.getInt(Options.OPTION_LIGHT_TIMEOUT)), 2, TextField.NUMERIC);
//                //#		lightManual = new ChoiceGroupEx(ResourceBundle.getString("backlight_manual"), Choice.MULTIPLE);
//                //#	    setChecked(lightManual, "yes", Options.getBoolean(Options.OPTION_LIGHT_MANUAL));
//                // #sijapp cond.end#
//                //                initInterfaceOptions();
//                break;
//
//            //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//            case OPTIONS_CLCHAT:
//                lineInt[Options.OPTION_CLASSIC_CHAT] = createLine("cl_chat", "no" + "|" + /*"normalFont" + "|" +*/ "graphic", Options.OPTION_CLASSIC_CHAT); // todo убрать обычный классический чат
//                lineInt[Options.OPTION_LINE_POSITION] = createLine("line_position", "up" + "|" + "bottom", Options.OPTION_LINE_POSITION);
//                lineOnOff[Options.OPTION_FT_SELFTRAIN] = createLineBoo("ft_selftrain", Options.OPTION_FT_SELFTRAIN);
//                tfldLineSize = new TextField(ResourceBundle.getString("freeze_clchat_size"), Integer.toString(Options.getInt(Options.OPTION_FREEZE_CLCHAT_SIZE)), 3, TextField.NUMERIC);
//
//                optionsForm.append(lineInt[Options.OPTION_CLASSIC_CHAT]);
//                optionsForm.append(lineInt[Options.OPTION_LINE_POSITION]);
//                optionsForm.append(lineOnOff[Options.OPTION_FT_SELFTRAIN]);
//                optionsForm.append(tfldLineSize);
//                break;
//            //#sijapp cond.end#
//
////            case OPTIONS_TIMERS:
////                sleepWake = new TextField("sleep", "0", 6, TextField.NUMERIC);
////                connectWake = createLineBoo("connect_after", false);
////
////                optionsForm.append(sleepWake);
////                optionsForm.append(connectWake);
////                break;
//
//            //#sijapp cond.if modules_FILES="true"#
//            case OPTIONS_IMP_EXP:
//                csFs = createLine("color_scheme_lng", "import_lng" + "|" + "export_lng", 0, true, true);
//                optionsFs = createLine("options_lng", "import_lng" + "|" + "export_lng", 0, true, true);
//                extraFs = createLine("extra_menu", "import_lng" + "|" + "export_lng", 0, true, true);
//                fullFs = createLine("reset_all", "import_lng" + "|" + "export_lng", 0, true, true);
//
//                optionsForm.append(csFs);
//                optionsForm.append(optionsFs);
//                optionsForm.append(extraFs);
//                optionsForm.append(fullFs);
//                break;
//            //#sijapp cond.end #
//            // #sijapp cond.if modules_TOOLBAR is "true"#
//            case OPTIONS_TOOLBAR:
//                lineOnOff[Options.OPTION_TOOLBAR] = createLineBoo("tool_bar", Options.OPTION_TOOLBAR);
//                optionsForm.append(lineOnOff[Options.OPTION_TOOLBAR]);
//                int ints[] = Util.explodeToInt(Options.getString(Options.OPTION_TOOLBAR_HASH), ';');
//                lineToolBar = new LineChoise[16];
//                for (int i = 0; i < 16; i++) {
//                    lineToolBar[i] = createLine("# " + i, getHotkeyActionNames(), 0, true, true);
//                    try {
//                        lineToolBar[i].setSelected(ints[i]);
//                    } catch (Exception ignored) {
//                    }
//                    optionsForm.append(lineToolBar[i]);
//                }
//                break;
//            // #sijapp cond.end#
//
//            case OPTIONS_TRANS:
//                cursorTransValue = new Gauge(ResourceBundle.getString("cursor_trans"), true, 10, Options.getInt(Options.OPTION_CURSOR_TRANS));
//                capTransValue = new Gauge(ResourceBundle.getString("cap_trans"), true, 10, Options.getInt(Options.OPTION_CAP_TRANS));
//                barTransValue = new Gauge(ResourceBundle.getString("menu_bar_trans"), true, 10, Options.getInt(Options.OPTION_BAR_TRANS));
//                menuTransValue = new Gauge(ResourceBundle.getString("menu_trans"), true, 10, Options.getInt(Options.OPTION_MENU_TRANS));
//                popupTransValue = new Gauge(ResourceBundle.getString("popup_trans"), true, 10, Options.getInt(Options.OPTION_POPUP_TRANS));
//                splashTransValue = new Gauge(ResourceBundle.getString("splash_trans"), true, 10, Options.getInt(Options.OPTION_SPLASH_TRANS));
//                blackOutGauge = new Gauge(ResourceBundle.getString("blackout"), true, 10, Options.getInt(Options.OPTION_BLACKOUT));
//
//                optionsForm.append(cursorTransValue);
//                optionsForm.append(capTransValue);
//                optionsForm.append(barTransValue);
//                optionsForm.append(menuTransValue);
//                optionsForm.append(popupTransValue);
//                optionsForm.append(splashTransValue);
//                optionsForm.append(blackOutGauge);
//                break;
//
//            case OPTIONS_SKINS:
//                StringBuffer skins = new StringBuffer("---");
//                //#sijapp cond.if modules_FILES="true"#
//                skins.append('|').append(ResourceBundle.getString("skins_from_fs"));
//                //#sijapp cond.end#
//                if (skinsName != null) {
//                    for (int i = 0; i < skinsName.length; i++) {
//                        skins.append('|').append(skinsName[i]);
//                    }
//                }
//                lineInt[Options.OPTION_SKIN] = createLine("skins", skins.toString(), Options.getInt(Options.OPTION_SKIN), false);
//                if (Options.getString(Options.OPTION_SKIN_PATH).indexOf('/') == 0) {
//                    lineInt[Options.OPTION_SKIN].setSelected(1);
//                }
//                iconCanvasGauge = new Gauge(ResourceBundle.getString("icon_canvas"), true, 25, Options.getInt(Options.OPTION_ICONS_CANVAS) / 4);
//                lineOnOff[Options.OPTION_COLORS_FROM_SKIN] = createLineBoo("color_scheme_from_skin", Options.OPTION_COLORS_FROM_SKIN);
//                lineOnOff[Options.OPTION_BARS_FROM_SKIN] = createLineBoo("bars_from_skin", Options.OPTION_BARS_FROM_SKIN);
//
//                //skinsFSChGr = new ChoiceGroupEx(ResourceBundle.getString("skins_from_fs"), Choice.EXCLUSIVE);
//                //skinsFSChGr.append(ResourceBundle.getString("no"), null);
//                //skinsFSChGr.append(ResourceBundle.getString("yes"), null);
//
//                //#sijapp cond.if modules_FILES="true"#
//                graphicLine = createLineBoo("icons_from_fs", false);
//                if (Options.getString(Options.OPTION_ICONS_PREFIX).equals("/")) {
//                    graphicLine.setSelected(0);
//                } else {
//                    graphicLine.setSelected(1);
//                }
//                //#sijapp cond.end#
//
//                //#sijapp cond.if modules_GFONT="true"#
//                skins = new StringBuffer("System");
//                if (fontName != null) {
//                    for (int i = 0; i < fontName.length; i++) {
//                        skins.append('|').append(fontName[i]);
//                    }
//                }
//                lineInt[Options.OPTION_GFONTS] = createLine("gfonts", skins.toString(), Options.getInt(Options.OPTION_GFONTS), false);
//                //#sijapp cond.end#
//
//                skins = new StringBuffer("---");
//                if (schemeName != null) {
//                    for (int i = 0; i < schemeName.length; i++) {
//                        skins.append('|').append(schemeName[i]);
//                    }
//                }
//                shemesLine = createLine("color_scheme_lng", skins.toString(), 0, false, true);
//
//                optionsForm.append(lineInt[Options.OPTION_SKIN]);
//                optionsForm.append(Options.getString(Options.OPTION_SKIN_PATH));
//                optionsForm.append(iconCanvasGauge);
//                //#sijapp cond.if modules_FILES="true"#
//                optionsForm.append(graphicLine);
//                optionsForm.append(Options.getString(Options.OPTION_ICONS_PREFIX));
//                //#sijapp cond.end#
//                optionsForm.append(lineOnOff[Options.OPTION_COLORS_FROM_SKIN]);
//                optionsForm.append(lineOnOff[Options.OPTION_BARS_FROM_SKIN]);
//                //#sijapp cond.if modules_GFONT="true"#
//                optionsForm.append(lineInt[Options.OPTION_GFONTS]);
//                //#sijapp cond.end#
//                optionsForm.append(shemesLine);
//                break;
//
//            case OPTIONS_COLOR_SCHEME:
//                colorTextList = new TextList(ResourceBundle.getString("color_scheme_lng"));
//                colorTextList.setMode(VirtualList.MODE_TEXT);
//                fillColorTextList();
//                //ColorChooserActivate();
//                return;
//
//            case OPTIONS_HOTKEYS:
//                fillHotheyList();
//                break;
//
//            case OPTIONS_SIGNALING:
//                vibroTimeTextField = new TextField(null, Integer.toString(Options.getInt(Options.OPTION_VIBRO_TIME)), 5, TextField.NUMERIC);
//                // #sijapp cond.if target isnot "DEFAULT"#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                lineInt[Options.OPTION_ONLINE_NOTIF_MODE] = createLine("onl_notification", "no"/* + "|" + "beep"*/
//                        // #sijapp cond.if target isnot "RIM"#
//                        + "|" + "filesound"
//                        // #sijapp cond.end#
//                        , Options.OPTION_ONLINE_NOTIF_MODE);
//                lineInt[Options.OPTION_OFFLINE_NOTIF_MODE] = createLine("ofl_notification", "no"/* + "|" + "beep"*/
//                        // #sijapp cond.if target isnot "RIM"#
//                        + "|" + "filesound"
//                        // #sijapp cond.end#
//                        , Options.OPTION_OFFLINE_NOTIF_MODE);
//                //#sijapp cond.end#
//                // #sijapp cond.if target isnot "RIM"#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                onlineNotificationSoundfileTextField = new TextField(null, Options.getString(Options.OPTION_ONLINE_NOTIF_FILE), 32, TextField.ANY);
//                offlineNotificationSoundfileTextField = new TextField(null, Options.getString(Options.OPTION_OFFLINE_NOTIF_FILE), 32, TextField.ANY);
//
//                lineOnOff[Options.OPTION_ONLINE_BLINK_ICON] = createLineBoo("blink_icon", Options.OPTION_ONLINE_BLINK_ICON);
//                lineOnOff[Options.OPTION_ONLINE_BLINK_NICK] = createLineBoo("blink_nick", Options.OPTION_ONLINE_BLINK_NICK);
//                blinkOnlineTimeTextField = new TextField(ResourceBundle.getString("blink_time"), String.valueOf(Options.getInt(Options.OPTION_ONLINE_BLINK_TIME)), 3, TextField.NUMERIC);
//                //#sijapp cond.end#
//                // #sijapp cond.end#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                lineInt[Options.OPTION_MESS_NOTIF_MODE] = createLine("message_notification", "no"/* + "|" + "beep"*/
//                        // #sijapp cond.if target isnot "RIM"#
//                        + "|" + "filesound"
//                        // #sijapp cond.end#
//                        , Options.OPTION_MESS_NOTIF_MODE);
//                lineOnOff[Options.OPTION_REPLAY_MESSAGE] = createLineBoo("replay_message", Options.OPTION_REPLAY_MESSAGE);
//                //#sijapp cond.end#
//                // #sijapp cond.if target isnot "RIM"#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                messageNotificationSoundfileTextField = new TextField(null, Options.getString(Options.OPTION_MESS_NOTIF_FILE), 32, TextField.ANY);
//                notificationSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.getInt(Options.OPTION_NOTIF_VOL) / 10);
//                //#sijapp cond.end#
//                //#sijapp cond.if target is "MIDP2"#
//                try {
//                    if (Class.forName("com.nokia.mid.ui.DeviceControl") != null)
//                        vibroFraq = new Gauge(ResourceBundle.getString("vibro_fraq"), true, 10, Options.getInt(Options.OPTION_VIBRO_FRAQ) / 10);
//                } catch (ClassNotFoundException ignored) {
//                }
//                //#sijapp cond.end#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                typingNotificationSoundfileTextField = new TextField(null, Options.getString(Options.OPTION_TYPING_FILE), 32, TextField.ANY);
//                //#sijapp cond.end#
//                lineInt[Options.OPTION_TYPING_MODE] = createLine("typing_notify", "no" + "|" + "typing_display_only"
//                        //#sijapp cond.if modules_SOUNDS is "true"#
//                        /* + "|" + "beep"*/ + "|" + "filesound"
//                        // #sijapp cond.end#
//                        , Options.OPTION_TYPING_MODE);
//                // #sijapp cond.end#
//                lineInt[Options.OPTION_VIBRATOR] = createLine("vibration", "no" + "|" + "yes" + "|" + "when_locked" + "|" + "two_vibra", Options.OPTION_VIBRATOR);
//                // #sijapp cond.end#
//                lineInt[Options.OPTION_POPUP_WIN2] = createLine("popup_win", "no" + "|" + "pw_forme" + "|" + "pw_curpro" + "|" + "pw_pro" + "|" + "pw_all", Options.OPTION_POPUP_WIN2);
//
//                // #sijapp cond.if target isnot "DEFAULT"#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                // #sijapp cond.if target isnot "RIM"#
//                optionsForm.append(notificationSoundVolume);
//                // #sijapp cond.end#
//                //#sijapp cond.if target is "MIDP2"#
//                if (vibroFraq != null) {
//                    optionsForm.append(vibroFraq);
//                }
//                //#sijapp cond.end#
//                //#sijapp cond.end#
//                optionsForm.append(lineInt[Options.OPTION_VIBRATOR]);
//                optionsForm.append(vibroTimeTextField);
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                optionsForm.append(lineInt[Options.OPTION_MESS_NOTIF_MODE]);
//                optionsForm.append(lineOnOff[Options.OPTION_REPLAY_MESSAGE]);
//                //#sijapp cond.end#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                optionsForm.append(lineInt[Options.OPTION_ONLINE_NOTIF_MODE]);
//                //#sijapp cond.end#
//                // #sijapp cond.if target isnot "RIM"#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                optionsForm.append(lineInt[Options.OPTION_OFFLINE_NOTIF_MODE]);
//
//                optionsForm.append(lineOnOff[Options.OPTION_ONLINE_BLINK_ICON]);
//                optionsForm.append(lineOnOff[Options.OPTION_ONLINE_BLINK_NICK]);
//                optionsForm.append(blinkOnlineTimeTextField);
//                //#sijapp cond.end#
//                optionsForm.append(lineInt[Options.OPTION_TYPING_MODE]);
//                // #sijapp cond.end#
//                // #sijapp cond.end#
//                optionsForm.append(lineInt[Options.OPTION_POPUP_WIN2]);
//                //#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
//                lineOnOff[Options.OPTION_POPUP_ONSYS] = createLineBoo("popup_on_sys", Options.OPTION_POPUP_ONSYS);
//                lineOnOff[Options.OPTION_BRING_UP] = createLineBoo("bring_up", Options.OPTION_BRING_UP);
//                optionsForm.append(lineOnOff[Options.OPTION_POPUP_ONSYS]);
//                optionsForm.append(lineOnOff[Options.OPTION_BRING_UP]);
//                // #sijapp cond.end#
//                //#sijapp cond.if modules_PANEL is "true"#
//                lineInt[Options.OPTION_PANEL_ACTIVE] = createLine("panel_active", "no" + "|" + "pw_forme" + "|" + "pw_curpro" + "|" + "pw_pro" + "|" + "pw_all", Options.OPTION_PANEL_ACTIVE);
//                optionsForm.append(lineInt[Options.OPTION_PANEL_ACTIVE]);
//                //#sijapp cond.end#
//                lineOnOff[Options.OPTION_CREEPING_LINE] = createLineBoo("creeping_line", Options.OPTION_CREEPING_LINE);
//                optionsForm.append(lineOnOff[Options.OPTION_CREEPING_LINE]);
//                // #sijapp cond.if target isnot "DEFAULT"#
//                // #sijapp cond.if target isnot "RIM"#
//                //#sijapp cond.if modules_SOUNDS is "true"#
//                optionsForm.append(messageNotificationSoundfileTextField);
//                optionsForm.append(onlineNotificationSoundfileTextField);
//                optionsForm.append(offlineNotificationSoundfileTextField);
//                optionsForm.append(typingNotificationSoundfileTextField);
//                //#sijapp cond.end#
//                // #sijapp cond.end#
//                // #sijapp cond.end#
//                break;
//
////            case OPTIONS_ANTISPAM:
////                antispamMsgTextField = new TextField(ResourceBundle.getString("antispam_msg"), Options.getString(Options.OPTION_ANTISPAM_MSG), 255, TextField.ANY);
////                antispamAnswerTextField = new TextField(ResourceBundle.getString("antispam_answer"), Options.getString(Options.OPTION_ANTISPAM_ANSWER), 255, TextField.ANY);
////                antispamHelloTextField = new TextField(ResourceBundle.getString("antispam_hello"), Options.getString(Options.OPTION_ANTISPAM_HELLO), 255, TextField.ANY);
////                lineOnOff[Options.OPTION_ANTISPAM_ENABLE] = createLineBoo("antispam_enable", Options.OPTION_ANTISPAM_ENABLE);
////                lineOnOff[Options.OPTION_ANTISPAM_URL] = createLineBoo("antispam_url", Options.OPTION_ANTISPAM_URL);
////
////                optionsForm.append(lineOnOff[Options.OPTION_ANTISPAM_ENABLE]);
////                optionsForm.append(lineOnOff[Options.OPTION_ANTISPAM_URL]);
////                optionsForm.append(antispamMsgTextField);
////                optionsForm.append(antispamAnswerTextField);
////                optionsForm.append(antispamHelloTextField);
////                break;
//
//            case OPTIONS_MISC:
//                enterPasswordTextField = new TextField(ResourceBundle.getString("enter_startup_pass"), Options.getString(Options.OPTION_ENTER_PASSWORD), 20, TextField.PASSWORD);
//                reEnterPasswordTextField = new TextField(ResourceBundle.getString("reenter_startup_pass"), null, 20, TextField.PASSWORD);
//
//                StringBuffer buf = new StringBuffer();
//                for (int i = -12; i <= 13; i++) {
//                    if (i != -12) {
//                        buf.append('|');
//                    }
//                    buf.append("GMT");
//                    if (i > 0) {
//                        buf.append('+');
//                    }
//                    buf.append(i).append(":00");
//                }
//                lineInt[Options.OPTIONS_GMT_OFFSET] = createLine("time_zone", buf.toString(), Options.getInt(Options.OPTIONS_GMT_OFFSET), false, true);
//                lineInt[Options.OPTIONS_GMT_OFFSET].setSelected(Options.getInt(Options.OPTIONS_GMT_OFFSET) + 12);
//
//                buf.setLength(0);
//                int[] currDateTime = DateAndTime.createDate(DateAndTime.createCurrentDate(false));
//                int minutes = currDateTime[DateAndTime.TIME_MINUTE];
//                int hour = currDateTime[DateAndTime.TIME_HOUR];
//                for (int i = 0; i < 24; i++) {
//                    if (i != 0) {
//                        buf.append('|');
//                    }
//                    buf.append(i).append(":").append(Util.makeTwo(minutes));
//                }
//                lineInt[Options.OPTIONS_LOCAL_OFFSET] = createLine("local_time", buf.toString(), Options.getInt(Options.OPTIONS_LOCAL_OFFSET), false, true); // Options.getInt(2) - любое небольшое число
//                lineInt[Options.OPTIONS_LOCAL_OFFSET].setSelected(hour);
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(new Date());
//                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
//
//                optionsForm.append(lineInt[Options.OPTIONS_GMT_OFFSET]);
//                optionsForm.append(lineInt[Options.OPTIONS_LOCAL_OFFSET]);
//                optionsForm.append(enterPasswordTextField);
//                optionsForm.append(reEnterPasswordTextField);
//                break;
//
////            case COLORS_SAVE:
////                CanvasEx.setColors(unTransformColors(colors));
////                //JimmUI.setColors(unTransformColors(colors));
////                Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
////                JimmUI.setColorScheme();
////                activate();
////                colors = null;
////                colorTextList = null;
////                return;
//
//            case COLORS_COPY:
//                colorBuffer = colors[colorTextList.getCurrTextIndex()];
//                if (menu != null) {
//                    menu.back();
//                }
//                return;
//
//            case COLORS_PASTE:
//                int currIndex = colorTextList.getCurrTextIndex();
//                colors[currIndex] = colorBuffer;
//                colorTextList.switchHeaderIcon(currIndex + 1, getIcon(currIndex), false);
//                if (menu != null) {
//                    menu.back();
//                }
//                return;
        }
        optionsForm.removeCommand(JimmUI.cmdSave);
        Jimm.setDisplay(optionsForm);
    }

    public void commandAction(Command c, Displayable d) {
        byte needToUpdate = 0;
// #sijapp cond.if modules_DEBUGLOG is "true"#
        if (JimmUI.isControlActive(colorTextList)) {
            if (c == JimmUI.cmdBack) {
                CanvasEx.setColors(colors);
                Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
                JimmUI.setColorScheme();
                activate();
                colors = null;
                colorTextList = null;
                colorBuffer = -1;
            } else if (c == JimmUI.cmdMenu) {
                Menu menu = new Menu(colorTextList);
                menu.addMenuItem("copy_text", (byte) 1);
                menu.addMenuItem("paste", (byte) 2);
                menu.setMenuListener(new MenuListener() {
                    public void menuSelect(Menu menu, byte action) {
                        switch (action) {
                            case 1:
                                colorBuffer = colors[colorTextList.getCurrTextIndex()];
                                break;

                            case 2:
                                int currIndex = colorTextList.getCurrTextIndex();
                                colors[currIndex] = colorBuffer;
                                colorTextList.switchHeaderIcon(currIndex + 1, getIcon(currIndex), false);
                                break;
                        }
                        if (menu != null) {
                            menu.back();
                        }
                    }
                });
                Jimm.setDisplay(menu);
            }
            return;
        }
// #sijapp cond.end#

        if (c.equals(JimmUI.cmdBack)) {
            if (!NativeCanvas.getCanvas().equals(optionsForm)) {
                skin = null;
                iconsPrefix = null;
                if (optionsForm != null) {
                    optionsForm.setCommandListener(null);
                    optionsForm = null;
                }
                Options.destroyForm();
                optionsMenu.back();
                if (null != optionsMenu) {
                    optionsMenu = null;
                }
                return;

//                if (optionsMenu.getPrvScreen() instanceof Menu) {
//                    Menu newMenu = Jimm.getContactList().buildMenu(false);
//                    newMenu.beforeShow();
//                    optionsMenu.setPrvScreen(newMenu);
//                }
//                activate();
//                optionsForm = null;
            } else {
//                skin = null;
//                iconsPrefix = null;
//                if (optionsForm != null) {
//                    optionsForm.setCommandListener(null);
//                    optionsForm = null;
//                }
//                Options.destroyForm();
//                optionsMenu.back();
//                if (null != optionsMenu) {
//                    optionsMenu = null;
//                }
//                return;
                //}
                //} else if (c == JimmUI.cmdSave) {
                //boolean needrestart = false;
                //boolean wrongPass = false;
                switch (optionsMenu.getCurrAction()) {

//#sijapp cond.if modules_SOUNDS is "true"#
                    case OPTIONS_SOUNDS:
                        Options.setInt(Options.OPTION_NOTIF_MESSAGE_VOL, messageSoundVolume.getValue() * 10);
                        Options.setString(Options.OPTION_MESS_NOTIF_FILE, messageNotificationSoundfileTextField.getString());
                        Options.setInt(Options.OPTION_NOTIF_ONLINE_VOL, onlineSoundVolume.getValue() * 10);
                        Options.setString(Options.OPTION_ONLINE_NOTIF_FILE, onlineNotificationSoundfileTextField.getString());
                        Options.setInt(Options.OPTION_NOTIF_OFFLINE_VOL, offlineSoundVolume.getValue() * 10);
                        Options.setString(Options.OPTION_OFFLINE_NOTIF_FILE, offlineNotificationSoundfileTextField.getString());
                        Options.setInt(Options.OPTION_NOTIF_TYPING_VOL, typingSoundVolume.getValue() * 10);
                        Options.setString(Options.OPTION_TYPING_NOTIF_FILE, typingNotificationSoundfileTextField.getString());
                        break;
//#sijapp cond.end#

                    case OPTIONS_VIBRATOR:
                        Options.setInt(Options.OPTION_VIBRATOR, lineInt[Options.OPTION_VIBRATOR].getSelected());
                        Options.setInt(Options.OPTION_VIBRO_TIME, Integer.parseInt(vibroTimeTextField.getString()));
                        if (vibroFraq != null) {
                            Options.setInt(Options.OPTION_VIBRO_FRAQ, vibroFraq.getValue() * 10);
                        }
                        break;

                    case OPTIONS_CHAT:
                        Options.setInt(Options.OPTION_FONT_SIZE_CHAT, lineInt[Options.OPTION_FONT_SIZE_CHAT].getSelected());
                        Options.setBoolean(Options.OPTION_SWAP_SEND, lineOnOff[Options.OPTION_SWAP_SEND].getBooolean());
                        Options.setBoolean(Options.OPTION_EMPTY_TITLE, lineOnOff[Options.OPTION_EMPTY_TITLE].getBooolean());
                        Options.setBoolean(Options.OPTION_CREEPING_LINE, lineOnOff[Options.OPTION_CREEPING_LINE].getBooolean());
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                        Options.setInt(Options.OPTION_CLASSIC_CHAT, lineInt[Options.OPTION_CLASSIC_CHAT].getSelected());
                        Options.setBoolean(Options.OPTION_FT_SELFTRAIN, lineOnOff[Options.OPTION_FT_SELFTRAIN].getBooolean());
//#sijapp cond.end#
                        Options.setString(Options.OPTION_MESSAGE_TEMPLARE, templMessChat.getString());
                        ChatTextList.messChange();
                        break;

//#sijapp cond.if modules_HISTORY is "true"#
                    case OPTIONS_HISTORY:
                        Options.setBoolean(Options.OPTION_HISTORY, lineOnOff[Options.OPTION_HISTORY].getBooolean());
                        Options.setBoolean(Options.OPTION_SHOW_LAST_MESS, lineOnOff[Options.OPTION_SHOW_LAST_MESS].getBooolean());
                        break;
//#sijapp cond.end#

// #sijapp cond.if modules_SMILES is "true"#
                    case OPTIONS_SMILES:
                        Options.setBoolean(Options.OPTION_USE_SMILES, lineOnOff[Options.OPTION_USE_SMILES].getBooolean());
                        Options.setInt(Options.OPTION_EMOTION_SIZE, Integer.parseInt(emotionsSize.getString()));
                        break;
// #sijapp cond.end#

                    case OPTIONS_SYSTEM:
                        if (languageLine != null) {
                            Options.setString(Options.OPTION_UI_LANGUAGE, ResourceBundle.langAvailable[languageLine.getSelected()]);
                        }
                        Options.setBoolean(Options.OPTION_CP1251_HACK, lineOnOff[Options.OPTION_CP1251_HACK].getBooolean());
                        int timeZone = lineInt[Options.OPTIONS_GMT_OFFSET].getSelected() - 12;
                        Options.setInt(Options.OPTIONS_GMT_OFFSET, timeZone);
                        int selHour = lineInt[Options.OPTIONS_LOCAL_OFFSET].getSelected() - timeZone;
                        if (selHour < 0) {
                            selHour += 24;
                        }
                        if (selHour >= 24) {
                            selHour -= 24;
                        }
                        int localOffset = selHour - currentHour;
                        while (localOffset >= 12) {
                            localOffset -= 24;
                        }
                        while (localOffset < -12) {
                            localOffset += 24;
                        }
                        Options.setInt(Options.OPTIONS_LOCAL_OFFSET, localOffset);
                        Options.setBoolean(Options.OPTION_BRING_UP, lineOnOff[Options.OPTION_BRING_UP].getBooolean());
                        Options.setString(Options.OPTION_ENTER_PASSWORD, enterPasswordTextField.getString());
                        break;

                    case OPTIONS_CONTACTLIST:
                        Options.setInt(Options.OPTION_FONT_SIZE_CL, lineInt[Options.OPTION_FONT_SIZE_CL].getSelected());
                        Options.setInt(Options.OPTION_POLES, lineInt[Options.OPTION_POLES].getSelected() + 1);
                        Options.setInt(Options.OPTION_CL_SORT_BY, lineInt[Options.OPTION_CL_SORT_BY].getSelected());
                        Options.setBoolean(Options.OPTION_CLIENT_ICON, lineOnOff[Options.OPTION_CLIENT_ICON].getBooolean());
                        Options.setBoolean(Options.OPTION_RIGHT_XTRAZ, lineOnOff[Options.OPTION_RIGHT_XTRAZ].getBooolean());
                        if (Options.getInt(Options.OPTION_TYPING_MODE) != lineInt[Options.OPTION_TYPING_MODE].getSelected()) {
                            needToUpdate |= 1;
                        }
                        Options.setInt(Options.OPTION_TYPING_MODE, lineInt[Options.OPTION_TYPING_MODE].getSelected());
                        Options.setBoolean(Options.OPTION_SHOW_OFFLINE, lineOnOff[Options.OPTION_SHOW_OFFLINE].getBooolean());
                        Options.setBoolean(Options.OPTION_USER_GROUPS, lineOnOff[Options.OPTION_USER_GROUPS].getBooolean());
                        Options.setBoolean(Options.OPTION_CL_HIDE_EGROUPS, lineOnOff[Options.OPTION_CL_HIDE_EGROUPS].getBooolean());
                        Options.setBoolean(Options.OPTION_SAVE_TEMP_CONTACTS, lineOnOff[Options.OPTION_SAVE_TEMP_CONTACTS].getBooolean());
                        Options.setBoolean(Options.OPTION_ON_MESS_FOCUS, lineOnOff[Options.OPTION_ON_MESS_FOCUS].getBooolean());
//#sijapp cond.if modules_SOUNDS is "true"#
                        Options.setBoolean(Options.OPTION_ONLINE_BLINK_NICK, lineOnOff[Options.OPTION_ONLINE_BLINK_NICK].getBooolean());
                        Options.setBoolean(Options.OPTION_ONLINE_BLINK_ICON, lineOnOff[Options.OPTION_ONLINE_BLINK_ICON].getBooolean());
                        Options.setInt(Options.OPTION_ONLINE_BLINK_TIME, Integer.parseInt(blinkOnlineTimeTextField.getString()));
//#sijapp cond.end#
                        Options.setInt(Options.OPTION_CAPTION_SHIFT, Integer.parseInt(tfCaptionShift.getString()));
                        Jimm.getContactList().optionsChanged();
                        Jimm.getContactList().setFontSize();
                        break;

                    case OPTIONS_VIEW:
                        Options.setInt(Options.OPTION_SKIN, lineInt[Options.OPTION_SKIN].getSelected());
                        Options.setBoolean(Options.OPTION_BARS_FROM_SKIN, lineOnOff[Options.OPTION_BARS_FROM_SKIN].getBooolean());
                        Options.setBoolean(Options.OPTION_COLORS_FROM_SKIN, lineOnOff[Options.OPTION_COLORS_FROM_SKIN].getBooolean());
                        int off = 1;
                        //#sijapp cond.if modules_FILES is "true"#
                        off = 2;
                        if (lineInt[Options.OPTION_SKIN].getSelected() > 1)   ///////todo разобраться применение цветов
                            //#sijapp cond.end#
                            Options.setString(Options.OPTION_SKIN_PATH, "none");

                        if (lineInt[Options.OPTION_SKIN].getSelected() == 0) {
                            Options.setString(Options.OPTION_SKIN_PATH, "none");
                            skin = null;
                        } else if (Options.getString(Options.OPTION_SKIN_PATH).indexOf('/') == 0) {
                            if (skin == null) {
                                try {
                                    skin = Image.createImage(VirtualList.getBackGroundImage());
                                } catch (NullPointerException ignored) {
                                }
                            }
                            if ((skin != null) && (skin.getHeight() < 64)) {
                                skin = null;
                            }
                        } else {
                            try {
                                skin = Image.createImage("/" + skinsLocal[lineInt[Options.OPTION_SKIN].getSelected() - off]);
                                Options.setString(Options.OPTION_SKIN_PATH, skinsLocal[lineInt[Options.OPTION_SKIN].getSelected() - off]);
                            } catch (Exception ignored) {
                            } catch (OutOfMemoryError ignored) {
                            }
                        }
                        if (skin == null) {
                            Options.setInt(Options.OPTION_SKIN, 0);
                        }
                        CanvasEx.loadCS(skin);
                        VirtualList.setBackGroundImage(skin);
                        JimmUI.setColorScheme();
                        if (shemesLine.getSelected() > 0) {
                            InputStream streamScheme = getClass().getResourceAsStream("/" + schemeLocal[shemesLine.getSelected() - 1]);
                            if (streamScheme != null) {
                                Options.txtToCS(new DataInputStream(streamScheme));
                                Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
                                try {
                                    streamScheme.close();
                                } catch (Exception ignored) {
                                }
                            }
                        } else {
                            Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
                        }
                        Options.setInt(Options.OPTION_ICONS_CANVAS, iconCanvasGauge.getValue() * 4);
                        Icon.scale = Options.getInt(Options.OPTION_ICONS_CANVAS);Icon.scale = Options.getInt(Options.OPTION_ICONS_CANVAS);Icon.scale = Options.getInt(Options.OPTION_ICONS_CANVAS);
//#sijapp cond.if modules_GFONT="true"#
                        if (lineInt[Options.OPTION_GFONTS].getSelected() != Options.getInt(Options.OPTION_GFONTS)) {
                            Options.setString(Options.OPTION_GFONT_PATH, "none");
                            if (lineInt[Options.OPTION_GFONTS].getSelected() != 0) {
                                try {
                                    Options.setString(Options.OPTION_GFONT_PATH, "/" + fontLocal[lineInt[Options.OPTION_GFONTS].getSelected() - 1]);
                                } catch (Exception ignored) {
                                } catch (OutOfMemoryError ignored) {
                                }
                            }
                            if (!CanvasEx.updateFont(Options.getString(Options.OPTION_GFONT_PATH))) {
                                Options.setInt(Options.OPTION_GFONTS, 0);
                            } else {
                                Options.setInt(Options.OPTION_FONT_SIZE_CL, 0);
                                Options.setInt(Options.OPTION_FONT_SIZE_CHAT, 0);
                            }
                            CanvasEx.updateFont();
                            Jimm.getContactList().createSetOfFonts();
// #sijapp cond.if modules_MAGIC_EYE is "true" #
                            icq.getProfile().getMagicEye().createSetOfFonts();
//#sijapp cond.end#
                            optionsForm = new FormEx(ResourceBundle.getString("options_lng"), JimmUI.cmdSave, JimmUI.cmdBack);
                            optionsForm.setCommandListener(this);
                            optionsForm.setItemStateListener(this);
                        }
                        Options.setInt(Options.OPTION_GFONTS, lineInt[Options.OPTION_GFONTS].getSelected());
//#sijapp cond.end#
                        //Options.setInt(Options.OPTION_FONT_SIZE_CL, lineInt[Options.OPTION_FONT_SIZE_CL].getSelected());
                        Options.setInt(Options.OPTION_LUSTER, lineInt[Options.OPTION_LUSTER].getSelected());
                        Options.setBoolean(Options.OPTION_SOFT_BAR, lineOnOff[Options.OPTION_SOFT_BAR].getBooolean());
                        Options.setBoolean(Options.OPTION_GRADIENT_MB, lineOnOff[Options.OPTION_GRADIENT_MB].getBooolean());
                        Options.setBoolean(Options.OPTION_ANIMATION, lineOnOff[Options.OPTION_ANIMATION].getBooolean());
                        Options.setInt(Options.OPTION_CURSOR_TRANS, cursorTransValue.getValue());
                        Options.setInt(Options.OPTION_CAP_TRANS, capTransValue.getValue());
                        Options.setInt(Options.OPTION_BAR_TRANS, barTransValue.getValue());
                        Options.setInt(Options.OPTION_MENU_TRANS, menuTransValue.getValue());
                        Options.setInt(Options.OPTION_SPLASH_TRANS, splashTransValue.getValue());
                        Options.setInt(Options.OPTION_BLACKOUT, blackOutGauge.getValue());
                        VirtualList.updateParams();
                        iconsPrefix = null;
                        skin = null;
                        break;

//#sijapp cond.if modules_LIGHT is "true"#
                    case OPTIONS_LIGHT:
                        Options.setInt(Options.OPTION_LIGHT_TIMEOUT, Integer.parseInt(lightTimeout.getString()));
                        Options.setBoolean(Options.OPTION_LIGHT_MANUAL, lightManual.getBooolean());
                        break;
// #sijapp cond.end#

                    case OPTIONS_HOTKEYS:
                        Integer integer;
                        int marker = 0;
                        java.util.Enumeration enumeration = keys.keys();
                        while (enumeration.hasMoreElements()) {
                            integer = (java.lang.Integer) enumeration.nextElement();
                            keys.put(integer, new Integer(lineKey[marker].getSelected()));
                            marker++;
                        }
                        Options.setString(Options.OPTION_HOTKEYS_HASH, Options.stringKeys(keys));
                        break;

// #sijapp cond.if modules_PROXY is "true"#
                    case OPTIONS_PROXY:
                        Options.setInt(Options.OPTION_PRX_TYPE, lineInt[Options.OPTION_PRX_TYPE].getSelected());
                        Options.setString(Options.OPTION_PRX_SERV, srvProxyHostTextField.getString());
                        Options.setString(Options.OPTION_PRX_PORT, srvProxyPortTextField.getString());
                        Options.setString(Options.OPTION_PRX_NAME, srvProxyLoginTextField.getString());
                        Options.setString(Options.OPTION_PRX_PASS, srvProxyPassTextField.getString());
                        Options.setString(Options.OPTION_AUTORETRY_COUNT, connAutoRetryTextField.getString());
                        break;
// #sijapp cond.end#

                    case OPTIONS_NETWORK:
                        Options.setInt(Options.OPTION_CONN_TYPE, lineInt[Options.OPTION_CONN_TYPE].getSelected());
                        Options.setString(Options.OPTION_SRV_HOST, srvHostTextField.getString());
                        Options.setString(Options.OPTION_SRV_PORT, srvPortTextField.getString());
                        Options.setBoolean(Options.OPTION_MD5_LOGIN, lineOnOff[Options.OPTION_MD5_LOGIN].getBooolean());
                        Options.setBoolean(Options.OPTION_AUTO_CONNECT, lineOnOff[Options.OPTION_AUTO_CONNECT].getBooolean());
                        Options.setBoolean(Options.OPTION_RECONNECT, lineOnOff[Options.OPTION_RECONNECT].getBooolean());
                        Options.setInt(Options.OPTION_RECONNECT_NUMBER, Integer.parseInt(reconnectNumberTextField.getString()));
                        Options.setBoolean(Options.OPTION_CONN_PROP, lineOnOff[Options.OPTION_CONN_PROP].getBooolean());
                        Options.setBoolean(Options.OPTION_SHADOW_CON, lineOnOff[Options.OPTION_SHADOW_CON].getBooolean());
                        Options.setString(Options.OPTION_CONN_ALIVE_INVTERV, connAliveIntervTextField.getString());
                        break;

//#sijapp cond.if modules_PANEL is "true"#
                    case OPTIONS_FAST_VIEW:
                        Options.setInt(Options.OPTION_PANEL_ACTIVE, lineInt[Options.OPTION_PANEL_ACTIVE].getSelected());
                        break;
//#sijapp cond.end#

                    case OPTIONS_SEND_FILES:
                        break;

                    case OPTIONS_WAIT:
                        Options.setBoolean(Options.OPTION_STATUS_AUTO, lineOnOff[Options.OPTION_STATUS_AUTO].getBooolean());
                        Options.setInt(Options.OPTION_STATUS_DELAY, Integer.parseInt(autoStatusDelayTimeTextField.getString()));
                        Options.setBoolean(Options.OPTION_AUTOLOCK, lineOnOff[Options.OPTION_AUTOLOCK].getBooolean());
                        Options.setBoolean(Options.OPTION_STATUS_RESTORE, lineOnOff[Options.OPTION_STATUS_RESTORE].getBooolean());
                        Icq.delay = Options.getInt(Options.OPTION_STATUS_DELAY) * 60000;
                        break;

//#sijapp cond.if modules_SBOLTUN is "true"#
                    case OPTIONS_SBOLTUN:
                        if (Options.getBoolean(Options.OPTION_SBOLTUN) != lineOnOff[Options.OPTION_SBOLTUN].getBooolean()) {
                            Options.setBoolean(Options.OPTION_SBOLTUN, lineOnOff[Options.OPTION_SBOLTUN].getBooolean());
                            jimm.chat.ChatTextList.sBoltunInit();
                        }
                        Options.setInt(Options.OPTION_SBOLTUN_SLEEP, Integer.parseInt(sBoltunSleep.getString()));
                        break;
//#sijapp cond.end#

//#sijapp cond.if modules_TOOLBAR is "true"#
                    case OPTIONS_TOOLBAR:
                        Options.setBoolean(Options.OPTION_TOOLBAR, lineOnOff[Options.OPTION_TOOLBAR].getBooolean());
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < 16; i++) {
                            sb.append(lineToolBar[i].getSelected());
                            if (i < 15) {
                                sb.append(";");
                            }
                        }
                        Options.setString(Options.OPTION_TOOLBAR_HASH, sb.toString());
                        break;
//#sijapp cond.end#

//#sijapp cond.if modules_FILES="true"#
                    case OPTIONS_SYNCHRONIZE:
                        if (graphicLine.getBooolean()) {
                            if (iconsPrefix != null) {
                                Options.setString(Options.OPTION_ICONS_PREFIX, iconsPrefix);
                                ContactList.updateIcons();
                            }
                        } else {
                            if (!Options.getString(Options.OPTION_ICONS_PREFIX).equals("/")) {
                                Options.setString(Options.OPTION_ICONS_PREFIX, "/");
                                ContactList.updateIcons();
                            } else {
                                Options.setString(Options.OPTION_ICONS_PREFIX, "/");
                            }
                        }
                        break;
//#sijapp cond.end #

                    //case OPTIONS_ACCOUNT:
                    //    readAccontsControls();
                    //    setAccountOptions();
                    //    break;
//
//                    case OPTIONS_NETWORK:
//                        Options.setString(Options.OPTION_SRV_HOST, srvHostTextField.getString());
//                        Options.setString(Options.OPTION_SRV_PORT, srvPortTextField.getString());
//                        Options.setInt(Options.OPTION_CONN_TYPE, lineInt[Options.OPTION_CONN_TYPE].getSelected());
//                        Options.setBoolean(Options.OPTION_KEEP_CONN_ALIVE, lineOnOff[Options.OPTION_KEEP_CONN_ALIVE].getBooolean());
//                        Options.setString(Options.OPTION_CONN_ALIVE_INVTERV, connAliveIntervTextField.getString());
//                        Options.setBoolean(Options.OPTION_MD5_LOGIN, lineOnOff[Options.OPTION_MD5_LOGIN].getBooolean());
//                        Options.setBoolean(Options.OPTION_CONN_PROP, lineOnOff[Options.OPTION_CONN_PROP].getBooolean());
//                        Options.setBoolean(Options.OPTION_AUTO_CONNECT, lineOnOff[Options.OPTION_AUTO_CONNECT].getBooolean());
//                        Options.setBoolean(Options.OPTION_RECONNECT, lineOnOff[Options.OPTION_RECONNECT].getBooolean());
//// #sijapp cond.if target isnot "MOTOROLA"#
//                        Options.setBoolean(Options.OPTION_SHADOW_CON, lineOnOff[Options.OPTION_SHADOW_CON].getBooolean());
//// #sijapp cond.end#
////#sijapp cond.if modules_HTTP is "true"#
//                        Options.setString(Options.OPTION_HTTP_USER_AGENT, httpUserAgendTextField.getString());
//                        Options.setString(Options.OPTION_HTTP_WAP_PROFILE, httpWAPProfileTextField.getString());
////#sijapp cond.end#
//                        Options.setInt(Options.OPTION_RECONNECT_NUMBER, Integer.parseInt(reconnectNumberTextField.getString()));
//                        break;
//
//// #sijapp cond.if modules_PROXY is "true"#
//                    case OPTIONS_PROXY:
//                        Options.setInt(Options.OPTION_PRX_TYPE, lineInt[Options.OPTION_PRX_TYPE].getSelected());
//                        Options.setString(Options.OPTION_PRX_SERV, srvProxyHostTextField.getString());
//                        Options.setString(Options.OPTION_PRX_PORT, srvProxyPortTextField.getString());
//
//                        Options.setString(Options.OPTION_PRX_NAME, srvProxyLoginTextField.getString());
//                        Options.setString(Options.OPTION_PRX_PASS, srvProxyPassTextField.getString());
//
//                        Options.setString(Options.OPTION_AUTORETRY_COUNT, connAutoRetryTextField.getString());
//                        break;
//// #sijapp cond.end#
//
//                    case OPTIONS_ICQ:
//                        if (lineOnOffIcq[Profile.OPTION_DELIVERY_REPORTX].getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_DELIVERY_REPORT)) {
//                            needToUpdate |= 1;
//                        }
//                        icq.getProfile().setBoolean(Profile.OPTION_DELIVERY_REPORT, lineOnOffIcq[Profile.OPTION_DELIVERY_REPORTX].getBooolean());
//                        Options.setBoolean(Options.OPTION_CACHE_CONTACTS, lineOnOff[Options.OPTION_CACHE_CONTACTS].getBooolean());
//                        Options.setBoolean(Options.OPTION_AUTO_ANSWER, lineOnOff[Options.OPTION_AUTO_ANSWER].getBooolean());
//                        Options.setBoolean(Options.OPTION_AUTO_XTRAZ, lineOnOff[Options.OPTION_AUTO_XTRAZ].getBooolean());
//                        if (lineOnOffIcq[Profile.OPTION_XTRAZ_ENABLEX].getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
//                            needToUpdate |= 1;
//                        }
//                        icq.getProfile().setBoolean(Profile.OPTION_XTRAZ_ENABLE, lineOnOffIcq[Profile.OPTION_XTRAZ_ENABLEX].getBooolean());
//
//                        if (lineOnOffIcq[Profile.OPTION_MESS_NOTIF_TYPEX].getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_MESS_NOTIF_TYPE)) {
//                            needToUpdate |= 1;
//                        }
//                        icq.getProfile().setBoolean(Profile.OPTION_MESS_NOTIF_TYPE, lineOnOffIcq[Profile.OPTION_MESS_NOTIF_TYPEX].getBooolean());
//
//                        if (lineOnOffIcq[Profile.OPTION_WEBAWAREX].getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_WEBAWARE)) {
//                            needToUpdate |= 1;
//                            needToUpdate |= 1 << 8;
//                        }
//                        icq.getProfile().setBoolean(Profile.OPTION_WEBAWARE, lineOnOffIcq[Profile.OPTION_WEBAWAREX].getBooolean());
//
//                        if (lineOnOffIcq[Profile.OPTION_REQ_AUTHX].getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_REQ_AUTH)) {
//                            needToUpdate |= 1 << 8;
//                        }
//                        icq.getProfile().setBoolean(Profile.OPTION_REQ_AUTH, lineOnOffIcq[Profile.OPTION_REQ_AUTHX].getBooolean());
////#sijapp cond.if modules_MAGIC_EYE is "true"#
//                        icq.getProfile().setBoolean(Profile.OPTION_ENABLE_MM, lineOnOffIcq[Profile.OPTION_ENABLE_MMX].getBooolean());
////#sijapp cond.end#
//                        if (clientIdLine.getSelected() != icq.getProfile().getInt(Profile.OPTION_CLIENT_ID)) {
//                            needToUpdate |= 1;
//                        }
//                        icq.getProfile().setInt(Profile.OPTION_CLIENT_ID, clientIdLine.getSelected());
//
//                        if (!clientStringVersion.getString().equals(icq.getProfile().getString(Profile.OPTION_STRING_VERSION))) {
//                            needToUpdate |= 1;
//                        }
//                        icq.getProfile().setString(Profile.OPTION_STRING_VERSION, clientStringVersion.getString());
//
//                        Options.setBoolean(Options.OPTION_AUTOLOCK, lineOnOff[Options.OPTION_AUTOLOCK].getBooolean());
//                        Options.setBoolean(Options.OPTION_STATUS_RESTORE, lineOnOff[Options.OPTION_STATUS_RESTORE].getBooolean());
//                        Options.setBoolean(Options.OPTION_STATUS_AUTO, lineOnOff[Options.OPTION_STATUS_AUTO].getBooolean());
//
//                        Options.setInt(Options.OPTION_STATUS_DELAY, Integer.parseInt(autoStatusDelayTimeTextField.getString()));
//
//                        Icq.delay = Options.getInt(Options.OPTION_STATUS_DELAY) * 60000;
//                        break;
//
//                    case OPTIONS_INTERFACE:
//                        if (ResourceBundle.langAvailable.length > 1) {
//                            Options.setString(Options.OPTION_UI_LANGUAGE, ResourceBundle.langAvailable[languageLine.getSelected()]);
//                        }
//
//                        Options.setBoolean(Options.OPTION_SOFT_BAR, lineOnOff[Options.OPTION_SOFT_BAR].getBooolean());
////#sijapp cond.if target is "MIDP2"#
////#sijapp cond.end#
////#sijapp cond.if modules_FILES="true"#
//                        Options.setBoolean(Options.OPTION_SHOW_SIZE, lineOnOff[Options.OPTION_SHOW_SIZE].getBooolean());
////#sijapp cond.end#
//                        Options.setBoolean(Options.OPTION_COMBO_KEYS, lineOnOff[Options.OPTION_COMBO_KEYS].getBooolean());
//                        Options.setInt(Options.OPTION_LUSTER, lineInt[Options.OPTION_LUSTER].getSelected());
//                        //Options.setInt(Options.OPTION_MENU_STYLE, lineInt[Options.OPTION_MENU_STYLE].getSelected());
//                        Options.setBoolean(Options.OPTION_GRADIENT_MB, lineOnOff[Options.OPTION_GRADIENT_MB].getBooolean());
//                        Options.setBoolean(Options.OPTION_ANIMATION, lineOnOff[Options.OPTION_ANIMATION].getBooolean());
//
//                        int newSortMethod = lineInt[Options.OPTION_CL_SORT_BY].getSelected();
//                        //boolean hideEmpGroupsChanged = Options.getBoolean(Options.OPTION_CL_HIDE_EGROUPS);
//                        //boolean newHideOffline = lineOnOff[Options.OPTION_CL_HIDE_OFFLINE].getBooolean();
//                        boolean newUseGroups = lineOnOff[Options.OPTION_USER_GROUPS].getBooolean();
//                        //hideEmpGroupsChanged = (hideEmpGroupsChanged ^ lineOnOff[Options.OPTION_CL_HIDE_EGROUPS].getBooolean());
//                        Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, lineOnOff[Options.OPTION_CL_HIDE_OFFLINE].getBooolean());
//                        Options.setBoolean(Options.OPTION_CL_HIDE_EGROUPS, lineOnOff[Options.OPTION_CL_HIDE_EGROUPS].getBooolean());
//                        //Options.setBoolean(Options.OPTION_AUTH_ICON, lineOnOff[Options.OPTION_AUTH_ICON].getBooolean());
//                        Options.setBoolean(Options.OPTION_RIGHT_XTRAZ, lineOnOff[Options.OPTION_RIGHT_XTRAZ].getBooolean());
//                        Options.setBoolean(Options.OPTION_SAVE_TEMP_CONTACTS, lineOnOff[Options.OPTION_SAVE_TEMP_CONTACTS].getBooolean());
//                        Options.setBoolean(Options.OPTION_CLIENT_ICON, lineOnOff[Options.OPTION_CLIENT_ICON].getBooolean());
//                        Options.setBoolean(Options.OPTION_ON_MESS_FOCUS, lineOnOff[Options.OPTION_ON_MESS_FOCUS].getBooolean());
//                        Options.setInt(Options.OPTION_CL_SORT_BY, newSortMethod);
//                        Options.setBoolean(Options.OPTION_USER_GROUPS, newUseGroups);
//                        Options.setInt(Options.OPTION_POLES, lineInt[Options.OPTION_POLES].getSelected() + 1);
//                        Jimm.getContactList().optionsChanged();
//                        //if (newUseGroups != lastGroupsUsed || newHideOffline != lastHideOffline || hideEmpGroupsChanged) {
//                        //    Jimm.getContactList().optionsChanged();
//                        //}
//
//                        Options.setBoolean(Options.OPTION_SWAP_SEND, lineOnOff[Options.OPTION_SWAP_SEND].getBooolean());
//                        Options.setBoolean(Options.OPTION_EMPTY_TITLE, lineOnOff[Options.OPTION_EMPTY_TITLE].getBooolean());
//// #sijapp cond.if modules_SMILES is "true"#
//                        Options.setBoolean(Options.OPTION_USE_SMILES, lineOnOff[Options.OPTION_USE_SMILES].getBooolean());
//// #sijapp cond.end#
//// #sijapp cond.if modules_HISTORY is "true"#
//                        Options.setBoolean(Options.OPTION_HISTORY, lineOnOff[Options.OPTION_HISTORY].getBooolean());
//                        Options.setBoolean(Options.OPTION_SHOW_LAST_MESS, lineOnOff[Options.OPTION_SHOW_LAST_MESS].getBooolean());
//// #sijapp cond.end#
//                        Options.setBoolean(Options.OPTION_CP1251_HACK, lineOnOff[Options.OPTION_CP1251_HACK].getBooolean());
//
//                        Options.setInt(Options.OPTION_MESSAGE_TYPE, lineInt[Options.OPTION_MESSAGE_TYPE].getSelected());
//                        Options.setInt(Options.OPTION_USER_FONT, lineInt[Options.OPTION_USER_FONT].getSelected());
//                        Options.setInt(Options.OPTION_FONT_SIZE, lineInt[Options.OPTION_FONT_SIZE].getSelected());
//                        Options.setInt(Options.OPTION_FONT_STYLE, lineInt[Options.OPTION_FONT_STYLE].getSelected());
//                        Jimm.getContactList().setFontSize();
//
//                        //Options.setInt(Options.OPTION_MAX_TEXT_SIZE, Integer.parseInt(maxTextSizeTF.getString()));
//                        if (tfCaptionShift != null) {
//                            Options.setInt(Options.OPTION_CAPTION_SHIFT, Integer.parseInt(tfCaptionShift.getString()));
//                        }
//                        Options.setString(Options.OPTION_MESSAGE_TEMPLARE, templMessChat.getString());
//                        ChatTextList.messChange();
//                        //Options.setInt(Options.OPTION_ANIMATION_CONST, Integer.parseInt(animationConstant.getString()));
////#sijapp cond.if modules_LIGHT is "true"#
////#                Options.setInt(Options.OPTION_LIGHT_TIMEOUT, Integer.parseInt(lightTimeout.getString()));
////#                Options.setBoolean(Options.OPTION_LIGHT_MANUAL, lightManual.isSelected(0));
//// #sijapp cond.end#
//                        if (!lastUILang.equals(Options.getString(Options.OPTION_UI_LANGUAGE))) {
//                            Options.setBoolean(Options.OPTION_LANG_CHANGED, true);
//                        }
//                        break;
//
////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//                    case OPTIONS_CLCHAT:
//                        Options.setInt(Options.OPTION_CLASSIC_CHAT, lineInt[Options.OPTION_CLASSIC_CHAT].getSelected());
//                        Options.setInt(Options.OPTION_LINE_POSITION, lineInt[Options.OPTION_LINE_POSITION].getSelected());
//                        Options.setBoolean(Options.OPTION_FT_SELFTRAIN, lineOnOff[Options.OPTION_FT_SELFTRAIN].getBooolean());
//                        Options.setInt(Options.OPTION_FREEZE_CLCHAT_SIZE, Integer.parseInt(tfldLineSize.getString()));
//                        break;
////#sijapp cond.end#
//
//// #sijapp cond.if modules_TOOLBAR is "true"#
//                    case OPTIONS_TOOLBAR:
//                        Options.setBoolean(Options.OPTION_TOOLBAR, lineOnOff[Options.OPTION_TOOLBAR].getBooolean());
//                        StringBuffer sb = new StringBuffer();
//                        for (int i = 0; i < 16; i++) {
//                            sb.append(lineToolBar[i].getSelected());
//                            if (i < 15) {
//                                sb.append(";");
//                            }
//                        }
//                        Options.setString(Options.OPTION_TOOLBAR_HASH, sb.toString());
//                        break;
//// #sijapp cond.end#
//
////                    case OPTIONS_COLOR_SCHEME:
////                        CanvasEx.setColors(unTransformColors(colors));
////                        Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
////                        JimmUI.setColorScheme();
////                        returnOptionsMenu();
////                        return;
//
//                    case OPTIONS_TRANS:
//                        Options.setInt(Options.OPTION_CURSOR_TRANS, cursorTransValue.getValue());
//                        Options.setInt(Options.OPTION_CAP_TRANS, capTransValue.getValue());
//                        Options.setInt(Options.OPTION_BAR_TRANS, barTransValue.getValue());
//                        Options.setInt(Options.OPTION_MENU_TRANS, menuTransValue.getValue());
//                        Options.setInt(Options.OPTION_POPUP_TRANS, popupTransValue.getValue());
//                        Options.setInt(Options.OPTION_SPLASH_TRANS, splashTransValue.getValue());
//                        Options.setInt(Options.OPTION_BLACKOUT, blackOutGauge.getValue());
//                        VirtualList.updateParams();
//                        break;
//
//                    case OPTIONS_SKINS:
//                        //#sijapp cond.if modules_GFONT="true"#
//                        int f = Options.getInt(Options.OPTION_GFONTS);
//                        Options.setInt(Options.OPTION_GFONTS, lineInt[Options.OPTION_GFONTS].getSelected());
//                        //#sijapp cond.end#
//                        Options.setInt(Options.OPTION_ICONS_CANVAS, iconCanvasGauge.getValue() * 4);
//                        Options.setInt(Options.OPTION_SKIN, lineInt[Options.OPTION_SKIN].getSelected());
//                        Options.setBoolean(Options.OPTION_COLORS_FROM_SKIN, lineOnOff[Options.OPTION_COLORS_FROM_SKIN].getBooolean());
//                        Options.setBoolean(Options.OPTION_BARS_FROM_SKIN, lineOnOff[Options.OPTION_BARS_FROM_SKIN].getBooolean());
//
//                        int off = 1;
//                        //#sijapp cond.if modules_FILES is "true"#
//                        off = 2;
//                        //#sijapp cond.end#
//
//                        //#sijapp cond.if modules_FILES is "true"#
//                        if (lineInt[Options.OPTION_SKIN].getSelected() > 1)   ///////todo разобраться применение цветов
//                        //#sijapp cond.end#
//                        {
//                            Options.setString(Options.OPTION_SKIN_PATH, "none");
//                        }
//
//                        if (lineInt[Options.OPTION_SKIN].getSelected() == 0) {
//                            Options.setString(Options.OPTION_SKIN_PATH, "none");
//                            skin = null;
//                        } else if (Options.getString(Options.OPTION_SKIN_PATH).indexOf('/') == 0) {
//                            if (skin == null) {
//                                try {
//                                    skin = Image.createImage(VirtualList.getBackGroundImage());
//                                } catch (NullPointerException ignored) {
//                                }
//                            }
//                            if ((skin != null) && (skin.getHeight() < 64)) {
//                                skin = null;
//                            }
//                        } else {
//                            try {
//                                skin = Image.createImage("/" + skinsLocal[lineInt[Options.OPTION_SKIN].getSelected() - off]);
//                                Options.setString(Options.OPTION_SKIN_PATH, skinsLocal[lineInt[Options.OPTION_SKIN].getSelected() - off]);
//                            } catch (Exception ignored) {
//                            } catch (OutOfMemoryError ignored) {
//                            }
//                        }
//                        //---
//                        if (skin == null) {
//                            Options.setInt(Options.OPTION_SKIN, 0);
//                        }
//                        CanvasEx.loadCS(skin);
//                        VirtualList.setBackGroundImage(skin);
//                        JimmUI.setColorScheme();
//
//                        if (shemesLine.getSelected() > 0) {
//                            InputStream streamScheme = getClass().getResourceAsStream("/" + schemeLocal[shemesLine.getSelected() - 1]);
//                            if (streamScheme != null) {
//                                Options.txtToCS(new DataInputStream(streamScheme));
//                                Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
//                                try {
//                                    streamScheme.close();
//                                } catch (Exception ignored) {
//                                }
//                            }
//                        } else {
//                            Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
//                        }
//                        VirtualList.updateParams();
//                        skin = null;
//
//                        //#sijapp cond.if modules_GFONT="true"#
//                        if (f != Options.getInt(Options.OPTION_GFONTS)) {
//                            Options.setString(Options.OPTION_GFONT_PATH, "none");
//                            if (lineInt[Options.OPTION_GFONTS].getSelected() != 0) {
//                                try {
//                                    Options.setString(Options.OPTION_GFONT_PATH, "/" + fontLocal[lineInt[Options.OPTION_GFONTS].getSelected() - 1]);
//                                } catch (Exception ignored) {
//                                } catch (OutOfMemoryError ignored) {
//                                }
//                            }
//                            if (!CanvasEx.updateFont(Options.getString(Options.OPTION_GFONT_PATH))) {
//                                Options.setInt(Options.OPTION_GFONTS, 0);
//                            }
//                            CanvasEx.updateFont();
//                            Jimm.getContactList().createSetOfFonts();
//                            optionsForm = new FormEx(ResourceBundle.getString("options_lng"), JimmUI.cmdSave, JimmUI.cmdBack);
//                            optionsForm.setCommandListener(this);
//                            optionsForm.setItemStateListener(this);
//                        }
//                        //#sijapp cond.end#
//
//                        //#sijapp cond.if modules_FILES="true"#
//                        if (graphicLine.getBooolean()) {
//                            if (iconsPrefix != null) {
//                                Options.setString(Options.OPTION_ICONS_PREFIX, iconsPrefix);
//                                ContactList.updateIcons();
//                            }
//                        } else {
//                            if (!Options.getString(Options.OPTION_ICONS_PREFIX).equals("/")) {
//                                Options.setString(Options.OPTION_ICONS_PREFIX, "/");
//                                ContactList.updateIcons();
//                            } else {
//                                Options.setString(Options.OPTION_ICONS_PREFIX, "/");
//                            }
//                        }
//                        //#sijapp cond.end#
//                        iconsPrefix = null;
//                        break;
//
//                    case OPTIONS_SIGNALING:
//// #sijapp cond.if target isnot "DEFAULT"#
////#sijapp cond.if modules_SOUNDS is "true"#
//                        Options.setInt(Options.OPTION_MESS_NOTIF_MODE, lineInt[Options.OPTION_MESS_NOTIF_MODE].getSelected());
//                        Options.setBoolean(Options.OPTION_REPLAY_MESSAGE, lineOnOff[Options.OPTION_REPLAY_MESSAGE].getBooolean());
//                        Options.setInt(Options.OPTION_ONLINE_NOTIF_MODE, lineInt[Options.OPTION_ONLINE_NOTIF_MODE].getSelected());
//                        Options.setInt(Options.OPTION_OFFLINE_NOTIF_MODE, lineInt[Options.OPTION_OFFLINE_NOTIF_MODE].getSelected());
////#sijapp cond.end#
//                        if (Options.getInt(Options.OPTION_TYPING_MODE) != lineInt[Options.OPTION_TYPING_MODE].getSelected()) {
//                            needToUpdate |= 1;
//                        }
//                        Options.setInt(Options.OPTION_TYPING_MODE, lineInt[Options.OPTION_TYPING_MODE].getSelected());
//                        Options.setInt(Options.OPTION_VIBRATOR, lineInt[Options.OPTION_VIBRATOR].getSelected());
//// #sijapp cond.if target isnot "RIM"#
////#sijapp cond.if modules_SOUNDS is "true"#
//                        Options.setString(Options.OPTION_MESS_NOTIF_FILE, messageNotificationSoundfileTextField.getString());
//                        Options.setInt(Options.OPTION_NOTIF_VOL, notificationSoundVolume.getValue() * 10);
//                        Options.setString(Options.OPTION_ONLINE_NOTIF_FILE, onlineNotificationSoundfileTextField.getString());
//                        Options.setString(Options.OPTION_OFFLINE_NOTIF_FILE, offlineNotificationSoundfileTextField.getString());
//                        Options.setString(Options.OPTION_TYPING_FILE, typingNotificationSoundfileTextField.getString());
//
//                        Options.setBoolean(Options.OPTION_ONLINE_BLINK_ICON, lineOnOff[Options.OPTION_ONLINE_BLINK_ICON].getBooolean());
//                        Options.setBoolean(Options.OPTION_ONLINE_BLINK_NICK, lineOnOff[Options.OPTION_ONLINE_BLINK_NICK].getBooolean());
//                        Options.setInt(Options.OPTION_ONLINE_BLINK_TIME, Integer.parseInt(blinkOnlineTimeTextField.getString()));
////#sijapp cond.end#
////#sijapp cond.if target is "MIDP2"#
//                        if (vibroFraq != null) {
//                            Options.setInt(Options.OPTION_VIBRO_FRAQ, vibroFraq.getValue() * 10);
//                        }
////#sijapp cond.end#
//// #sijapp cond.end#
//// #sijapp cond.end#
//                        Options.setInt(Options.OPTION_VIBRO_TIME, Integer.parseInt(vibroTimeTextField.getString()));
//                        Options.setInt(Options.OPTION_POPUP_WIN2, lineInt[Options.OPTION_POPUP_WIN2].getSelected());
////#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
//                        Options.setBoolean(Options.OPTION_POPUP_ONSYS, lineOnOff[Options.OPTION_POPUP_ONSYS].getBooolean());
//                        Options.setBoolean(Options.OPTION_BRING_UP, lineOnOff[Options.OPTION_BRING_UP].getBooolean());
//                        //Options.setBoolean(Options.OPTION_SOUND_VIBRA, lineOnOff[Options.OPTION_SOUND_VIBRA].getBooolean());
//                        Options.setBoolean(Options.OPTION_CREEPING_LINE, lineOnOff[Options.OPTION_CREEPING_LINE].getBooolean());
//// #sijapp cond.end#
//                        Options.setInt(Options.OPTION_PANEL_ACTIVE, lineInt[Options.OPTION_PANEL_ACTIVE].getSelected());
//                        ContactList.updateParams();
//                        break;
//
//                    case OPTIONS_HOTKEYS:
//                        saveHotheyList();
//                        break;
//
////                    case OPTIONS_ANTISPAM:
////                        Options.setString(Options.OPTION_ANTISPAM_MSG, antispamMsgTextField.getString());
////                        Options.setString(Options.OPTION_ANTISPAM_ANSWER, antispamAnswerTextField.getString());
////                        Options.setString(Options.OPTION_ANTISPAM_HELLO, antispamHelloTextField.getString());
////                        Options.setBoolean(Options.OPTION_ANTISPAM_ENABLE, lineOnOff[Options.OPTION_ANTISPAM_ENABLE].getBooolean());
////                        Options.setBoolean(Options.OPTION_ANTISPAM_URL, lineOnOff[Options.OPTION_ANTISPAM_URL].getBooolean());
////                        break;
//
//                    case OPTIONS_MISC:
//                        if (Options.getString(Options.OPTION_ENTER_PASSWORD).equals(reEnterPasswordTextField.getString())) {
//                            Options.setString(Options.OPTION_ENTER_PASSWORD, enterPasswordTextField.getString());
//                        } //else {
//                        //    wrongPass = true;
//                        //}
//
//                        /* Set up time zone*/
//                        int timeZone = lineInt[Options.OPTIONS_GMT_OFFSET].getSelected() - 12;
//                        Options.setInt(Options.OPTIONS_GMT_OFFSET, timeZone);
//
//                        /* Translate selected time to GMT */
//                        int selHour = lineInt[Options.OPTIONS_LOCAL_OFFSET].getSelected() - timeZone;
//                        if (selHour < 0) {
//                            selHour += 24;
//                        }
//                        if (selHour >= 24) {
//                            selHour -= 24;
//                        }
//                        /* Calculate diff. between selected GMT time and phone time */
//                        int localOffset = selHour - currentHour;
//                        while (localOffset >= 12) {
//                            localOffset -= 24;
//                        }
//                        while (localOffset < -12) {
//                            localOffset += 24;
//                        }
//                        Options.setInt(Options.OPTIONS_LOCAL_OFFSET, localOffset);
//                        break;
//
////                    case OPTIONS_TIMERS:
////                        int sleep;
////                        try {
////                            sleep = Integer.parseInt(sleepWake.getString());
////                        } catch (Exception e) {
////                            sleep = 0;
////                        }
////                        if (sleep > 0) {
////                            try {
////                                Jimm.jimm.autorun(sleep);
////                                Options.setBoolean(Options.OPTION_AUTO_CONNECT, connectWake.getBooolean());
////                                Options.safe_save();
////                                return;
////                            } catch (Exception ignored) {
////                            }
////                        }
////                        returnOptionsMenu();
////                        return;
//
//                    case OPTIONS_IMP_EXP:
//                        returnOptionsMenu();
//                        return;
                }

                Options.safe_save();
                //if (optionsMenu.getCurrAction() == OPTIONS_ICQ) {
                //    icq.getProfile().saveOptions();
                //}
//            if (wrongPass) {
//                Jimm.setDisplay(new Alert(null, ResourceBundle.getString("wrong_pass_entry"), null, AlertType.ERROR));
//                return;
//            }
                returnOptionsMenu();
                if (icq.isConnected() && needToUpdate > 0) {
                    try {
                        if ((needToUpdate & 0x01) != 0) {
                            icq.sendPacket(OtherAction.getStandartUserInfoPacket(icq));
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        if ((needToUpdate & 0x01) != 0) {
                            icq.sendPacket(OtherAction.getStatusPacket(icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS), icq));
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        if ((needToUpdate & 0x10) != 0) {
                            icq.sendPacket(OtherAction.getAuthPacket(icq));
                        }
                    } catch (Exception ignored) {
                    }
                    //needToUpdate = 0;
                }
                /*if (needrestart) {
                    Jimm.setDisplay(new Select(ResourceBundle.getString("restart"), ResourceBundle.getString("need_restart"), Select.TYPE_OKCANCEL,
                        new SelectListener() {
                            public void selectAction(int action, int selectType, Object o) {
                                Jimm.doExit(true);
                            }
                        },
                        0, null));
                }*/
            }
        }
// else if (c == cmdAddNewAccount) {
//            readAccontsControls();
//            uins.addElement(Options.emptyString);
//            passwords.addElement(Options.emptyString);
//            nicks.addElement(Options.emptyString);
//            clearForm();
//            showAccountControls();
//            return;
//        }
//        if (c == cmdDeleteAccount) {
//            readAccontsControls();
//            JimmUI.showSelector(getAvailableUins(), this, false);
//            return;
//        }
        if (c == cmdNewKey) {
            PopupKeys pk = new PopupKeys(optionsForm, new KeysCatcher() {
                public void keyCatch(int i) {
                    keys.put(new Integer(i), new Integer(0));
                    keys.put(new Integer(i + 1000), new Integer(0));
                    HotkeyFormInit();
                }
            }, (byte) 0);
            Jimm.setDisplay(pk);
            return;
        }
        if (c == cmdDelKey) {
            PopupKeys pk = new PopupKeys(optionsForm, new KeysCatcher() {
                public void keyCatch(int i) {
                    keys.remove(new Integer(i));
                    keys.remove(new Integer(i + 1000));
                    HotkeyFormInit();
                }
            }, (byte) 1);
            Jimm.setDisplay(pk);
            return;
        }
//        if (c == cmdCopyKey) {
//            PopupKeys pk = new PopupKeys(optionsForm, new KeysCatcher() {
//                public void keyCatch(int i) {
//                    copyKey = (Integer) keys.get(new Integer(i));
//                }
//            }, (byte) 2);
//            Jimm.setDisplay(pk);
//            return;
//        }
//
//        if (c == cmdPasteKey) {
//            PopupKeys pk = new PopupKeys(optionsForm, new KeysCatcher() {
//                public void keyCatch(int i) {
//                    keys.put(new Integer(i), copyKey);
//                    HotkeyFormInit();
//                }
//            }, (byte) 3);
//            Jimm.setDisplay(pk);
//            return;
//        }
    }

    public void selectAction(int action, int selectType, Object o) {
//        String uin = (String) o;
//        int end = uin.length();
//        int beg = 0;
//        //while (uin.charAt(end - 1) < '0' || uin.charAt(end - 1) > '9') {end--;}
//        //while (uin.charAt(beg) < '0' || uin.charAt(beg) > '9') {beg++;}
//        for (; uin.charAt(end - 1) < '0' || uin.charAt(end - 1) > '9'; end--) ;
//        for (; uin.charAt(beg) < '0' || uin.charAt(beg) > '9'; beg++) ;
//        uin = uin.substring(beg, end);
//        readAccontsControls();
//        int index = uins.indexOf(uin);
//        uins.removeElementAt(index);
//        passwords.removeElementAt(index);
//        nicks.removeElementAt(index);
//        clearForm();
//        showAccountControls();
//        Jimm.setDisplay(optionsForm);
    }

    public void initOptionsList() {
        optionsMenu.clear();
// #sijapp cond.if modules_DEBUGLOG is "true"#
        optionsMenu.addMenuItem("options_colors", ContactList.menuIcons.elementAt(32), OPTIONS_COLORS);
// #sijapp cond.end#
        optionsMenu.addMenuItem("options_signaling", ContactList.menuIcons.elementAt(15), OPTIONS_SOUNDS);
        optionsMenu.addMenuItem("options_vibrator", ContactList.menuIcons.elementAt(15), OPTIONS_VIBRATOR);
        optionsMenu.addMenuItem("options_chat", ContactList.menuIcons.elementAt(14), OPTIONS_CHAT);
//#sijapp cond.if modules_HISTORY is "true"#
        optionsMenu.addMenuItem("options_history", ContactList.menuIcons.elementAt(14), OPTIONS_HISTORY);
//#sijapp cond.end#
// #sijapp cond.if modules_SMILES is "true"#
        optionsMenu.addMenuItem("options_smiles", ContactList.menuIcons.elementAt(14), OPTIONS_SMILES);
//#sijapp cond.end#
        optionsMenu.addMenuItem("options_system", ContactList.menuIcons.elementAt(4), OPTIONS_SYSTEM);
        optionsMenu.addMenuItem("options_contactlist", ContactList.menuIcons.elementAt(14), OPTIONS_CONTACTLIST);
        optionsMenu.addMenuItem("options_view", ContactList.menuIcons.elementAt(34), OPTIONS_VIEW);
//#sijapp cond.if modules_LIGHT is "true"#          
        optionsMenu.addMenuItem("options_light", ContactList.menuIcons.elementAt(14), OPTIONS_LIGHT);
//#sijapp cond.end#
        optionsMenu.addMenuItem("options_hotkey", ContactList.menuIcons.elementAt(22), OPTIONS_HOTKEYS);
// #sijapp cond.if modules_PROXY is "true"#
        optionsMenu.addMenuItem("options_proxy", ContactList.menuIcons.elementAt(21), OPTIONS_PROXY);
// #sijapp cond.end#        
        optionsMenu.addMenuItem("options_network", ContactList.menuIcons.elementAt(13), OPTIONS_NETWORK);
// #sijapp cond.if modules_PANEL is "true"#
        optionsMenu.addMenuItem("options_fastview", ContactList.menuIcons.elementAt(0), OPTIONS_FAST_VIEW);
// #sijapp cond.end#
        optionsMenu.addMenuItem("options_sendfiles", ContactList.menuIcons.elementAt(8), OPTIONS_SEND_FILES);
        optionsMenu.addMenuItem("options_wait", ContactList.menuIcons.elementAt(28), OPTIONS_WAIT);
// #sijapp cond.if modules_SBOLTUN is "true"#
        optionsMenu.addMenuItem("options_sboltun", ContactList.imageList.elementAt(14), OPTIONS_SBOLTUN);
// #sijapp cond.end#
// #sijapp cond.if modules_TOOLBAR is "true"#
        if (Jimm.isTouch())
            optionsMenu.addMenuItem("options_toolbar", ContactList.menuIcons.elementAt(37), OPTIONS_TOOLBAR);
// #sijapp cond.end#
        optionsMenu.addMenuItem("options_synchronize", ContactList.menuIcons.elementAt(8), OPTIONS_SYNCHRONIZE);
        optionsMenu.setCurrent(lastIndex);
        optionsMenu.setMenuListener(this);
    }

//    public void initOptionsList() {
//        optionsMenu.clear();
//        //optionsMenu.addMenuItem("profiles", ContactList.menuIcons.elementAt(12), OPTIONS_ACCOUNT);
//        optionsMenu.addMenuItem("ICQ", OPTIONS_ICQ, new Icon[]{ContactList.imageList.elementAt(7), getClientIcon()});
//        optionsMenu.addMenuItem("options_network", ContactList.menuIcons.elementAt(13), OPTIONS_NETWORK);
//        // #sijapp cond.if modules_PROXY is "true"#
//        if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_PROXY) {
//            optionsMenu.addMenuItem("proxy", ContactList.menuIcons.elementAt(21), OPTIONS_PROXY);
//        }
//        // #sijapp cond.end#
//        optionsMenu.addMenuItem("options_interface", ContactList.menuIcons.elementAt(14), OPTIONS_INTERFACE);
//        // #sijapp cond.if modules_TOOLBAR is "true"#
//        if (Jimm.isTouch()) {
//            optionsMenu.addMenuItem("tool_bar", ContactList.menuIcons.elementAt(37), OPTIONS_TOOLBAR);
//        }
//        // #sijapp cond.end#
//        optionsMenu.addMenuItem("options_trans", ContactList.menuIcons.elementAt(31), OPTIONS_TRANS);
//        optionsMenu.addMenuItem("color_scheme_lng", ContactList.menuIcons.elementAt(32), OPTIONS_COLOR_SCHEME);
//        optionsMenu.addMenuItem("skins_cs", ContactList.menuIcons.elementAt(34), OPTIONS_SKINS);
//        //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//        optionsMenu.addMenuItem("cl_chat", ContactList.imageList.elementAt(18), OPTIONS_CLCHAT);
//        //#sijapp cond.end#
//        optionsMenu.addMenuItem("options_hotkeys", ContactList.menuIcons.elementAt(22), OPTIONS_HOTKEYS);
//        optionsMenu.addMenuItem("options_signaling", ContactList.menuIcons.elementAt(15), OPTIONS_SIGNALING);
//        //optionsMenu.addMenuItem("antispam", ContactList.menuIcons.elementAt(0), OPTIONS_ANTISPAM);
//        optionsMenu.addMenuItem("misc", ContactList.menuIcons.elementAt(0), OPTIONS_MISC);
//        //#sijapp cond.if modules_FILES is "true"#
//        optionsMenu.addMenuItem("import_export", ContactList.menuIcons.elementAt(8), OPTIONS_IMP_EXP);
//        // #sijapp cond.end#
//        //optionsMenu.addMenuItem("timers", ContactList.menuIcons.elementAt(0), OPTIONS_TIMERS);
//        optionsMenu.setCurrent(lastIndex);
//        optionsMenu.setMenuListener(this);
//    }

    public void activate() {
        Jimm.setPrevScreen(Jimm.getContactList());
        initOptionsList();
        Jimm.setDisplay(optionsMenu);
    }

    private void returnOptionsMenu() {
        if (optionsMenu.getPrvScreen() instanceof Menu) {
            Menu newMenu = Jimm.getContactList().buildMenu(false);
            newMenu.beforeShow();
            optionsMenu.setPrvScreen(newMenu);
        }
        optionsMenu.animated();
        Jimm.getContactList().updateParamsCL();
        Jimm.getContactList().rebuild();
        Jimm.getContactList().beforeShow();
        activate();
    }

    private void initForm() {
        if (optionsForm != null) {
            return;
        }
        optionsForm = new FormEx(ResourceBundle.getString("options_lng"), JimmUI.cmdSave, JimmUI.cmdBack);
        optionsForm.setCommandListener(this);
        optionsForm.setItemStateListener(this);
    }

    private void clearForm() {
        initForm();
        optionsForm.removeAllCommands();
        optionsForm.deleteAll();
        optionsForm.addCommandEx(JimmUI.cmdSave, VirtualList.MENU_TYPE_LEFT_BAR);
        optionsForm.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
    }
}