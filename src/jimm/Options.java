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


/*******************************************************************************
 Current record store format:

 Record #1: VERSION                 (UTF8)
 Record #2:
 STRING SIZE                        (SHOTRT)
 INTEGER SIZE                       (SHOTRT)
 BOOLEAN SIZE                       (SHOTRT)
 PASSWORD SIZE                      (SHOTRT UNSIGNED)

 STRING[] KEYS                      (UTF8)
 INTEGER[] KEYS                     (INT)
 BOOLEAN[] KEYS                     (BOOLEAN)
 STRING[] KEYS                      (UTF8 DECIPHER)
 ******************************************************************************/


package jimm;

import jimm.comm.Icq;
import jimm.comm.Util;
import jimm.forms.OptionsForm;
import jimm.util.ResourceBundle;
//#sijapp cond.if modules_TOOLBAR is "true"#
import jimm.ui.Toolbar;
//#sijapp cond.end#

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.*;
import java.util.Vector;
import java.util.Hashtable;

import DrawControls.CanvasEx;
import DrawControls.NativeCanvas;

public class Options {
    /* Accounts keys */
    public static final int OPTION_UIN = 0;
    public static final int OPTION_PASSWORD = 100;
    public static final int OPTION_MY_NICK = 1;

    // STRING
    public static final int OPTION_UI_LANGUAGE = 2;
    public static final int OPTION_SRV_HOST = 3;
    public static final int OPTION_SRV_PORT = 4;
    public static final int OPTION_CONN_ALIVE_INVTERV = 5;
    public static final int OPTION_HTTP_USER_AGENT = 6;
    public static final int OPTION_HTTP_WAP_PROFILE = 7;
    public static final int OPTION_SKIN_PATH = 8;
    public static final int OPTION_ICONS_PREFIX = 9;
    public static final int OPTION_MESS_NOTIF_FILE = 10;
    public static final int OPTION_ONLINE_NOTIF_FILE = 11;
    public static final int OPTION_OFFLINE_NOTIF_FILE = 12;
    public static final int OPTION_TYPING_NOTIF_FILE = 13;
    public static final int OPTION_CURRENCY = 14;
    public static final int OPTION_ENTER_PASSWORD = 15;
    public static final int OPTION_ANTISPAM_MSG = 16;
    public static final int OPTION_ANTISPAM_HELLO = 17;
    public static final int OPTION_ANTISPAM_ANSWER = 18;
    public static final int OPTION_PRX_SERV = 19;
    public static final int OPTION_PRX_PORT = 20;
    public static final int OPTION_AUTORETRY_COUNT = 21;
    public static final int OPTION_PRX_NAME = 22;
    public static final int OPTION_PRX_PASS = 23;
    public static final int OPTION_HOTKEYS_HASH = 24;
    public static final int OPTION_TOOLBAR_HASH = 25;
    public static final int OPTION_GFONT_PATH = 26;
    public static final int OPTION_MESSAGE_TEMPLARE = 27;

    public static final int OPTION_STRING_SIZE = 28;

    // INTEGER
    public static final int OPTIONS_CURR_ACCOUNT = 0;
    //public static final int OPTION_CONN_PROP = 1;
    public static final int OPTION_CONN_TYPE = 2;
    public static final int OPTION_RECONNECT_NUMBER = 3;
    public static final int OPTION_CLASSIC_CHAT = 4;
    public static final int OPTION_FREEZE_CLCHAT_SIZE = 5;
    public static final int OPTION_LINE_POSITION = 6;
    public static final int OPTION_CL_SORT_BY = 7;
    //public static final int OPTION_USER_FONT = 8;
    public static final int OPTION_FONT_SIZE_CL = 9;
    public static final int OPTION_FONT_SIZE_CHAT = 10;
    //public static final int OPTION_FONT_STYLE = 10;
    public static final int OPTION_SKIN = 11;
    public static final int OPTION_SKIN_OFFSET = 12;
    public static final int OPTION_VIBRO_TIME = 13;
    public static final int OPTION_CURSOR_TRANS = 14;
    public static final int OPTION_CAP_TRANS = 15;
    public static final int OPTION_BAR_TRANS = 16;
    public static final int OPTION_POPUP_TRANS = 17;
    public static final int OPTION_SPLASH_TRANS = 19;
    public static final int OPTION_BLACKOUT = 20;
    public static final int OPTION_POLES = 21;
    public static final int OPTION_MAX_TEXT_SIZE = 22;
    public static final int OPTION_CAMERA_RES = 23;
    public static final int OPTION_CAMERA_ENCODING = 24;
    public static final int OPTION_VIBRO_FRAQ = 25;
    public static final int OPTION_CAPTION_SHIFT = 26;
    public static final int OPTION_MESS_NOTIF_MODE = 27;
    public static final int OPTION_NOTIF_VOL = 28;
    public static final int OPTION_ONLINE_NOTIF_MODE = 29;
    public static final int OPTION_OFFLINE_NOTIF_MODE = 30;
    public static final int OPTION_VIBRATOR = 31;
    public static final int OPTION_TYPING_MODE = 32;
    public static final int OPTION_ONLINE_BLINK_TIME = 33;
    public static final int OPTION_COST_PER_PACKET = 34;
    public static final int OPTION_COST_PER_DAY = 35;
    public static final int OPTION_COST_PACKET_LENGTH = 36;
    public static final int OPTION_STATUS_DELAY = 37;
    public static final int OPTION_LIGHT_TIMEOUT = 38;
    public static final int OPTION_PRX_TYPE = 39;
    public static final int OPTIONS_GMT_OFFSET = 40;
    public static final int OPTIONS_LOCAL_OFFSET = 41;
    public static final int OPTION_POPUP_WIN2 = 42;
    public static final int OPTION_VISIBILITY_ID = 43;
    public static final int OPTION_ICONS_CANVAS = 44;
    public static final int OPTION_MESSAGE_TYPE = 45;
    public static final int OPTION_LUSTER = 46;
    public static final int OPTION_PANEL_ACTIVE = 47;
    public static final int OPTION_MAGIC_EYE = 48;
    public static final int OPTION_GFONTS = 49;
    public static final int OPTION_MENU_TRANS = 50;
    public static final int OPTION_MENU_STYLE = 51;
    public static final int OPTION_ANIMATION_CONST = 52;
    public static final int OPTION_SLEEP_WAKE = 53;
    public static final int OPTION_NOTIF_MESSAGE_VOL = 54;
    public static final int OPTION_NOTIF_ONLINE_VOL = 55;
    public static final int OPTION_NOTIF_OFFLINE_VOL = 56;
    public static final int OPTION_NOTIF_TYPING_VOL = 57;
    public static final int OPTION_EMOTION_SIZE = 58;
    public static final int OPTION_SBOLTUN_SLEEP = 59;

    public static final int OPTION_INTEGER_SIZE = 60;


    // BOOLEAN
    public static final int OPTION_KEEP_CONN_ALIVE = 0;
    public static final int OPTION_AUTO_CONNECT = 1;
    public static final int OPTION_SHADOW_CON = 2;
    public static final int OPTION_RECONNECT = 3;
    public static final int OPTION_AUTH_ICON = 4;
    public static final int OPTION_SAVE_TEMP_CONTACTS = 5;
    public static final int OPTION_POPUP_ONSYS = 6;
    public static final int OPTION_FT_SELFTRAIN = 7;
    public static final int OPTION_CL_HIDE_EGROUPS = 8;
    public static final int OPTION_SWAP_SEND = 9;
    public static final int OPTION_EMPTY_TITLE = 10;
    public static final int OPTION_SOFT_BAR = 11;
    public static final int OPTION_RIGHT_XTRAZ = 12;
    public static final int OPTION_TOOLBAR = 13;
    public static final int OPTION_CLIENT_ICON = 14;
    public static final int OPTION_COMBO_KEYS = 15;
    public static final int OPTION_SHOW_SIZE = 16;
    public static final int OPTION_BARS_FROM_SKIN = 17;
    public static final int OPTION_COLORS_FROM_SKIN = 18;
    public static final int OPTION_ON_MESS_FOCUS = 19;
    public static final int OPTION_AUTOLOCK = 20;
    public static final int OPTION_ANIMATION = 21;
    public static final int OPTION_BACKLIGHT = 22;
    public static final int OPTION_SHOW_OFFLINE = 23;
    public static final int OPTION_ONLINE_BLINK_ICON = 24;
    public static final int OPTION_ONLINE_BLINK_NICK = 25;
    public static final int OPTION_CP1251_HACK = 26;
    public static final int OPTION_USER_GROUPS = 27;
    public static final int OPTION_HISTORY = 28;
    public static final int OPTION_SHOW_LAST_MESS = 29;
    public static final int OPTION_CACHE_CONTACTS = 30;
    public static final int OPTION_AUTO_ANSWER = 31;
    public static final int OPTION_AUTO_XTRAZ = 32;
    public static final int OPTION_STATUS_RESTORE = 33;
    public static final int OPTION_STATUS_AUTO = 34;
    public static final int OPTION_ANTISPAM_ENABLE = 35;
    public static final int OPTION_LIGHT_MANUAL = 36;
    public static final int OPTION_USE_SMILES = 37;
    public static final int OPTION_MD5_LOGIN = 38;
    public static final int OPTION_FULL_SCREEN = 39;
    public static final int OPTION_SILENT_MODE = 40;
    public static final int OPTION_BRING_UP = 41;
    public static final int OPTION_CREEPING_LINE = 42;
    public static final int OPTION_LANG_CHANGED = 43;
    public static final int OPTION_ANTISPAM_URL = 44;
    public static final int OPTION_GRADIENT_MB = 45;
    public static final int OPTION_CONN_PROP = 46;
    public static final int OPTION_REPLAY_MESSAGE = 47;
    public static final int OPTION_ROSTER_CONNECT = 48;
    public static final int OPTION_SBOLTUN = 49;

    public static final int OPTION_BOOLEAN_SIZE = 50;

    //Hotkey Actions
    public static final int HOTKEY_NONE = 0;
    public static final int HOTKEY_INFO = 1;
    public static final int HOTKEY_NEWMSG = 2;
    public static final int HOTKEY_HISTORY = 3;
    public static final int HOTKEY_ONOFF = 4;
    public static final int HOTKEY_OPTIONS = 5;
    public static final int HOTKEY_LOCK = 6;
    public static final int HOTKEY_MINIMIZE = 7;
    public static final int HOTKEY_CLI_INFO = 8;
    public static final int HOTKEY_STATUS_MSG = 9;
    public static final int HOTKEY_SOUNDOFF = 10;
    public static final int HOTKEY_DEL_CHATS = 11;
    public static final int HOTKEY_XTRAZ_MSG = 12;
    public static final int HOTKEY_MAGIC_EYE = 13;
    public static final int HOTKEY_AUTO_ANSWER = 14;
    public static final int HOTKEY_VIBRO = 15;
    public static final int HOTKEY_FLASH = 16;
    public static final int HOTKEY_STATUS = 17;
    public static final int HOTKEY_GROUPS = 18;
    public static final int HOTKEY_CHATLIST = 19;
    public static final int HOTKEY_PROFILES = 20;
    public static final int HOTKEY_PREV_PROFILE = 21;
    public static final int HOTKEY_NEXT_PROFILE = 22;
    public static final int HOTKEY_GET_STATUS = 23;
    public static final int HOTKEY_MANAGER = 24;
    public static final int HOTKEY_CONNECT_HOT = 25;

    public static final Vector uins = new Vector();
    public static final Vector passwords = new Vector();
    public static final Vector nicks = new Vector();

    private static final Hashtable keysAct = new Hashtable();
    final public static String emptyString = "";
    public static String firstDate = "";

    static private final String optionsString[] = new String[OPTION_STRING_SIZE];
    static private final int optionsInteger[] = new int[OPTION_INTEGER_SIZE];
    static private final boolean optionsBoolean[] = new boolean[OPTION_BOOLEAN_SIZE];
    static private final String optionsPass[] = new String[1];

    private static OptionsForm optionsForm;

    public Options() {
        try {
            setDefaults();
            load();
            if (getBoolean(OPTION_LANG_CHANGED))
                setBoolean(OPTION_LANG_CHANGED, false);
        } catch (Exception e) {
            setDefaults();
        }
        ResourceBundle.setCurrUiLanguage(getString(Options.OPTION_UI_LANGUAGE));
        initAccounts();
        jimm.chat.ChatTextList.messChange();
    }

    static public void setDefaults() {
        // STRING
        setString(OPTION_UIN, emptyString);
        setString(OPTION_PASSWORD, emptyString);
        setString(OPTION_MY_NICK, "Default");
        setString(OPTION_UI_LANGUAGE, ResourceBundle.langAvailable[0]);
        setString(OPTION_SRV_HOST, "login.icq.com");//64.12.161.153 //login.oscar.aol.com 
        setString(OPTION_SRV_PORT, "5190");
        setString(OPTION_CONN_ALIVE_INVTERV, "120");
        setString(OPTION_HTTP_USER_AGENT, "unknown");
        setString(OPTION_HTTP_WAP_PROFILE, "unknown");
        setString(OPTION_SKIN_PATH, "none");
        setString(OPTION_ICONS_PREFIX, "/");
        setString(OPTION_MESS_NOTIF_FILE, "message.mp3");
        setString(OPTION_ONLINE_NOTIF_FILE, "online.mp3");
        setString(OPTION_TYPING_NOTIF_FILE, "typing.mp3");
        setString(OPTION_OFFLINE_NOTIF_FILE, "offline.mp3");
        setString(OPTION_CURRENCY, emptyString);
        setString(OPTION_ENTER_PASSWORD, emptyString);
        setString(OPTION_ANTISPAM_MSG, emptyString);
        setString(OPTION_ANTISPAM_HELLO, emptyString);
        setString(OPTION_ANTISPAM_ANSWER, emptyString);
        setString(OPTION_PRX_SERV, emptyString);
        setString(OPTION_PRX_NAME, emptyString);
        setString(OPTION_PRX_PASS, emptyString);
// #sijapp cond.if modules_PROXY is "true" #
        setString(OPTION_PRX_PORT, "1080");
        setString(OPTION_AUTORETRY_COUNT, "1");
// #sijapp cond.else #
        setString(OPTION_PRX_PORT, emptyString);
        setString(OPTION_AUTORETRY_COUNT, emptyString);
// #sijapp cond.end #
        setString(OPTION_HOTKEYS_HASH, defaultKeys());
        //#sijapp cond.if modules_TOOLBAR is "true"#
        setString(OPTION_TOOLBAR_HASH, Toolbar.defaultToolbar());
        //#sijapp cond.else#
        setString(OPTION_TOOLBAR_HASH, "1");
        //#sijapp cond.end#
        setString(OPTION_GFONT_PATH, "none");
        setString(OPTION_MESSAGE_TEMPLARE, "%PIC[b]%NICK (%HOUR:%MIN:%SEC):[/b]%BR%MSG");

        // INTEGER
        setInt(OPTIONS_CURR_ACCOUNT, 0);
        setInt(OPTION_CONN_TYPE, 0);
        setInt(OPTION_RECONNECT_NUMBER, 5);
        setInt(OPTION_CLASSIC_CHAT, 0);
        setInt(OPTION_FREEZE_CLCHAT_SIZE, -1);
        setInt(OPTION_LINE_POSITION, 0);
        setInt(OPTION_CL_SORT_BY, 0);
        //setInt(OPTION_USER_FONT, 0);
        setInt(OPTION_FONT_SIZE_CL, 0);
        setInt(OPTION_FONT_SIZE_CHAT, 0);
        //setInt(OPTION_FONT_STYLE, 0);
        setInt(OPTION_SKIN, 0);
        setInt(OPTION_SKIN_OFFSET, 0);
        setInt(OPTION_VIBRO_TIME, 75);
        setInt(OPTION_CURSOR_TRANS, 0);
        setInt(OPTION_CAP_TRANS, 0);
        setInt(OPTION_BAR_TRANS, 0);
        setInt(OPTION_POPUP_TRANS, 0);
        setInt(OPTION_SPLASH_TRANS, 0);
        setInt(OPTION_BLACKOUT, 0);
        setInt(OPTION_POLES, 1);
        setInt(OPTION_MAX_TEXT_SIZE, 2048);
        setInt(OPTION_CAMERA_RES, 0);
        setInt(OPTION_CAMERA_ENCODING, 0);
        setInt(OPTION_VIBRO_FRAQ, 100);
        setInt(OPTION_CAPTION_SHIFT, (Jimm.is_phone_NOKIA() || Jimm.is_phone_SE()) ? 20 : 0);
        setInt(OPTION_NOTIF_VOL, 100);
        setInt(OPTION_MESS_NOTIF_MODE, 0);
        setInt(OPTION_ONLINE_NOTIF_MODE, 0);
        setInt(OPTION_OFFLINE_NOTIF_MODE, 0);
        setInt(OPTION_TYPING_MODE, 1);
        setInt(OPTION_VIBRATOR, 1);
        setInt(OPTION_ONLINE_BLINK_TIME, 25);
        setInt(OPTION_COST_PER_PACKET, 0);
        setInt(OPTION_COST_PER_DAY, 0);
        setInt(OPTION_COST_PACKET_LENGTH, 1024);
        setInt(OPTION_STATUS_DELAY, 15);
        setInt(OPTION_LIGHT_TIMEOUT, 5);
        setInt(OPTION_PRX_TYPE, 0);
        setInt(OPTIONS_GMT_OFFSET, 0);
        setInt(OPTIONS_LOCAL_OFFSET, 0);
        setInt(OPTION_POPUP_WIN2, 0);
        setInt(OPTION_VISIBILITY_ID, 0);
        setInt(OPTION_ICONS_CANVAS, (Jimm.isTouch()) ? 32 : 16);
        setInt(OPTION_MESSAGE_TYPE, 0);
        setInt(OPTION_LUSTER, 1);
        setInt(OPTION_PANEL_ACTIVE, 0);
        setInt(OPTION_MAGIC_EYE, 0x1F);
        setInt(OPTION_GFONTS, 0);
        setInt(OPTION_MENU_TRANS, 0);
        setInt(OPTION_MENU_STYLE, 0);
        setInt(OPTION_ANIMATION_CONST, 5000);
        setInt(OPTION_NOTIF_MESSAGE_VOL, 100);
        setInt(OPTION_NOTIF_ONLINE_VOL, 100);
        setInt(OPTION_NOTIF_OFFLINE_VOL, 100);
        setInt(OPTION_NOTIF_TYPING_VOL, 100);
        setInt(OPTION_EMOTION_SIZE, -1);
        setInt(OPTION_SBOLTUN_SLEEP, 5);

        // BOOlEAN
        setBoolean(OPTION_KEEP_CONN_ALIVE, true);
        setBoolean(OPTION_AUTO_CONNECT, false);
        setBoolean(OPTION_SHADOW_CON, Jimm.is_phone_NOKIA());
        setBoolean(OPTION_RECONNECT, true);
        setBoolean(OPTION_AUTH_ICON, true);
        setBoolean(OPTION_SAVE_TEMP_CONTACTS, false);
        setBoolean(OPTION_POPUP_ONSYS, false);
        setBoolean(OPTION_FT_SELFTRAIN, false);
        setBoolean(OPTION_CL_HIDE_EGROUPS, false);
        setBoolean(OPTION_SWAP_SEND, false);
        setBoolean(OPTION_EMPTY_TITLE, false);
        setBoolean(OPTION_SOFT_BAR, true);
        setBoolean(OPTION_RIGHT_XTRAZ, false);
        setBoolean(OPTION_TOOLBAR, false);
        setBoolean(OPTION_CLIENT_ICON, true);
        setBoolean(OPTION_COMBO_KEYS, false);
        setBoolean(OPTION_SHOW_SIZE, false);
        setBoolean(OPTION_BARS_FROM_SKIN, false);
        setBoolean(OPTION_COLORS_FROM_SKIN, false);
        setBoolean(OPTION_ON_MESS_FOCUS, false);
        setBoolean(OPTION_AUTOLOCK, false);
        setBoolean(OPTION_ANIMATION, false);
        setBoolean(OPTION_BACKLIGHT, false);
        setBoolean(OPTION_SHOW_OFFLINE, false);
        setBoolean(OPTION_ONLINE_BLINK_ICON, true);
        setBoolean(OPTION_ONLINE_BLINK_NICK, true);
        setBoolean(OPTION_CP1251_HACK, ResourceBundle.langAvailable[0].equals("RU"));
        setBoolean(OPTION_USER_GROUPS, false);
        setBoolean(OPTION_HISTORY, false);
        setBoolean(OPTION_SHOW_LAST_MESS, false);
        setBoolean(OPTION_CACHE_CONTACTS, false);
        setBoolean(OPTION_AUTO_ANSWER, false);
        setBoolean(OPTION_AUTO_XTRAZ, false);
        setBoolean(OPTION_STATUS_RESTORE, false);
        setBoolean(OPTION_STATUS_AUTO, false);
        setBoolean(OPTION_ANTISPAM_ENABLE, false);
        setBoolean(OPTION_LIGHT_MANUAL, false);
        setBoolean(OPTION_USE_SMILES, true);
        setBoolean(OPTION_MD5_LOGIN, true);
        setBoolean(OPTION_FULL_SCREEN, true);
        setBoolean(OPTION_SILENT_MODE, false);
        setBoolean(OPTION_BRING_UP, false);
//#sijapp cond.if target="MIDP2"#
        setBoolean(OPTION_CREEPING_LINE, false);
//#sijapp cond.else#
        setBoolean(OPTION_CREEPING_LINE, true);
//#sijapp cond.end#
        setBoolean(OPTION_LANG_CHANGED, false);
        setBoolean(OPTION_ANTISPAM_URL, true);
        setBoolean(OPTION_GRADIENT_MB, false);
        setBoolean(OPTION_CONN_PROP, false);
        setBoolean(OPTION_REPLAY_MESSAGE, false);
        setBoolean(OPTION_ROSTER_CONNECT, false);
        setBoolean(OPTION_SBOLTUN, false);
//        try{
//            Class.forName("com.sonyericsson.ui.UIActivityMenu");
//            setBoolean(OPTION_ACTIVITY_MENU, true);
//        } catch (Throwable t) {
//            setBoolean(OPTION_ACTIVITY_MENU, false);
//        }
// #sijapp cond.if (target isnot "DEFAULT" & target isnot "RIM" & modules_SOUNDS is "true")#
        selectSoundType("online.", OPTION_ONLINE_NOTIF_FILE);
        selectSoundType("offline.", OPTION_OFFLINE_NOTIF_FILE);
        selectSoundType("message.", OPTION_MESS_NOTIF_FILE);
        selectSoundType("typing.", OPTION_TYPING_NOTIF_FILE);
//#sijapp cond.end#
        firstDate = jimm.comm.DateAndTime.getDateString(false, false, jimm.comm.DateAndTime.createCurrentDate(true));
    }

    static public void load() throws IOException, RecordStoreException {
        //ImageEncoder
        /* Open record store */
        RecordStore account = RecordStore.openRecordStore("options", false);

        /* Temporary variables */
        byte[] buf;
        ByteArrayInputStream bais;
        DataInputStream dis;

        /* Get version info from record store */
        buf = account.getRecord(1);
        bais = new ByteArrayInputStream(buf);
        dis = new DataInputStream(bais);
        dis.readUTF(); // version opt
        firstDate = dis.readUTF();
        ////setDefaults();

        /* Read all option key-value pairs */
        buf = account.getRecord(2);
        bais = new ByteArrayInputStream(buf);
        dis = new DataInputStream(bais);

        load(dis);
        loadKeys(keysAct);

        try {
            bais.close();
            dis.close();
        } catch (Exception ignored) {
        }

        /* Close record store */
        account.closeRecordStore();
    }

    public static int keyAction(int i) {
        Integer t;
        if ((t = (Integer) keysAct.get(new Integer(i))) == null) {
            return -1;
        } else {
            return t.intValue();
        }
    }

//    private static void addKey(int i, int j){
//        keysAct.put(new Integer(i), new Integer(j));
//    }

    // #sijapp cond.if modules_TOOLBAR is "true"#

    public static void changeToolbar() {
        boolean value = getBoolean(OPTION_TOOLBAR);
        setBoolean(OPTION_TOOLBAR, !value);
        Jimm.getContactList().updateParamsCL();
        Jimm.getContactList().rebuild();
        Jimm.getContactList().beforeShow();
    }
    // #sijapp cond.end#

    public static String defaultKeys() {
        StringBuffer sb = new StringBuffer()
                .append(NativeCanvas.KEY_NUM0).append('=').append(HOTKEY_XTRAZ_MSG).append('\n')
                .append(NativeCanvas.KEY_NUM4).append('=').append(HOTKEY_CLI_INFO).append('\n')
                .append(NativeCanvas.KEY_NUM6).append('=').append(HOTKEY_INFO).append('\n')
                .append(NativeCanvas.KEY_STAR).append('=').append(HOTKEY_CHATLIST).append('\n')
                .append(NativeCanvas.KEY_POUND).append('=').append(0).append('\n')
                .append(NativeCanvas.KEY_NUM0 + 1000).append('=').append(0).append('\n')
                .append(NativeCanvas.KEY_NUM4 + 1000).append('=').append(0).append('\n')
                .append(NativeCanvas.KEY_NUM6 + 1000).append('=').append(0).append('\n')
                .append(NativeCanvas.KEY_STAR + 1000).append('=').append(0).append('\n')
                .append(NativeCanvas.KEY_POUND + 1000).append('=').append(HOTKEY_LOCK).append('\n');

        return sb.toString();
    }

    public static String stringKeys(Hashtable hashtable) {
        java.util.Enumeration enumeration = hashtable.keys();
        StringBuffer stringbuffer = new StringBuffer();
        Integer integer;
        Integer integer1;
        for (; enumeration.hasMoreElements(); stringbuffer.append(integer.toString()).append('=').append(integer1.toString()).append('\n')) {
            integer = (Integer) enumeration.nextElement();
            integer1 = (Integer) hashtable.get(integer);
        }
        return stringbuffer.toString();
    }

    static public void loadKeys(Hashtable hashtable) {
        String[] elements;
        int len;
        if ((len = (elements = Util.explode(getString(OPTION_HOTKEYS_HASH), '\n')).length) == 0) {
            return;
        }
        hashtable.clear();
        for (int j = len - 1; j >= 0; j--) {
            String shorts;
            if ((shorts = elements[j]).trim().length() != 0) {
                String[] key_num = Util.explode(shorts, '=');
                hashtable.put(Integer.valueOf(key_num[0]), Integer.valueOf(key_num[1]));
            }
        }
    }

    /* Load option values from record store */
    static public void load(DataInputStream dis) throws IOException {
        int sizeString = dis.readShort();
        int sizeInteger = dis.readShort();
        int sizeBoolean = dis.readShort();
        int sizePass = dis.readUnsignedShort();

        for (int key = 0; key < sizeString; key++)
            if (optionsString.length > key)
                optionsString[key] = dis.readUTF();
            else
                dis.readUTF();

        for (int key = 0; key < sizeInteger; key++)
            if (optionsInteger.length > key)
                optionsInteger[key] = dis.readInt();
            else
                dis.readInt();

        for (int key = 0; key < sizeBoolean; key++)
            if (optionsBoolean.length > key)
                optionsBoolean[key] = dis.readBoolean();
            else
                dis.readBoolean();

        for (int key = 0; key < sizePass; key++) {
            int len = dis.readShort();
            byte data[] = new byte[len];
            dis.readFully(data);
            data = Util.decipherPassword(data);
            String s = Util.byteArrayToString(data, 0, data.length, true);
            //System.out.println(optionsString[0] + '\n' + s);
            if (optionsPass.length > key)
                optionsPass[key] = s;
        }
    }

    public static void initAccounts() {
        uins.removeAllElements();
        passwords.removeAllElements();
        nicks.removeAllElements();
        String[] tmp = Util.explode(optionsString[OPTION_UIN], '\t');
        for (int i = 0; i < tmp.length; i++) {
            uins.addElement(tmp[i]);
        }
        tmp = Util.explode(optionsPass[OPTION_PASSWORD - 100], '\t');
        for (int i = 0; i < tmp.length; i++) {
            passwords.addElement(tmp[i]);
        }
        tmp = Util.explode(optionsString[OPTION_MY_NICK], '\t');
        for (int i = 0; i < tmp.length; i++) {
            nicks.addElement(tmp[i]);
        }
        if (tmp.length == 0) {
            uins.addElement(emptyString);
            passwords.addElement(emptyString);
            nicks.addElement("Default");
        }
    }

    /* Save option values to record store */
    static public void save(DataOutputStream dos, boolean skipAccounts) throws IOException {
        dos.writeShort(optionsString.length);
        dos.writeShort(optionsInteger.length);
        dos.writeShort(optionsBoolean.length);
        dos.writeShort(optionsPass.length);

        for (int key = 0; key < optionsString.length; key++)
            if (optionsString[key] == null/* || (skipAccounts && (key == OPTION_UIN || key == OPTION_ENTER_PASSWORD))*/)
                dos.writeUTF(emptyString);
            else
                dos.writeUTF(optionsString[key]);

        for (int key = 0; key < optionsInteger.length; key++)
            /* if (skipAccounts && (key == OPTIONS_CURR_ACCOUNT))
                dos.writeInt(0);
            else*/
            dos.writeInt(optionsInteger[key]);

        for (int key = 0; key < optionsBoolean.length; key++)
            dos.writeBoolean(optionsBoolean[key]);

        for (int key = 0; key < optionsPass.length; key++) {
            /*if (skipAccounts && key == OPTION_PASSWORD - 100) {
                byte[] optionValue = new byte[1];
                dos.writeShort(optionValue.length);
                dos.write(optionValue);
            } else*/
            {
                String s = optionsPass[key];
                if (s == null) s = emptyString;
                byte[] optionValue = Util.stringToByteArray(s, true);
                optionValue = Util.decipherPassword(optionValue);
                dos.writeShort(optionValue.length);
                dos.write(optionValue);
            }
        }
    }

    static public void safe_save(DataOutputStream dos, boolean skipAccounts) {
        try {
            save(dos, skipAccounts);
        } catch (Exception e) {
            JimmException.handleException(new JimmException(172, 0, true));
            System.out.println("can't save the options");
        }
    }

    static public void safe_save() {
        try {
            /* Open record store */
            RecordStore account = RecordStore.openRecordStore("options", true);

            /* Add empty records if necessary */
            while (account.getNumRecords() < 3) {
                account.addRecord(null, 0, 0);
            }

            /* Temporary variables */
            byte[] buf;
            ByteArrayOutputStream baos;
            DataOutputStream dos;

            /* Add version info to record store */
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            dos.writeUTF(Jimm.getVersion());
            dos.writeUTF(firstDate);
            buf = baos.toByteArray();
            account.setRecord(1, buf, 0, buf.length);

            /* Save all option key-value pairs */
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);

            safe_save(dos, false);

            buf = baos.toByteArray();
            account.setRecord(2, buf, 0, buf.length);

            try {
                dos.close();
                baos.close();
            } catch (Exception ignored) {
            }
            /* Close record store */
            account.closeRecordStore();
        } catch (Exception e) {
            JimmException.handleException(new JimmException(172, 0, true));
        }
    }

    static public void reset_rms() throws RecordStoreException {
        String[] stores = RecordStore.listRecordStores();
        for (int i = 0; i < stores.length; i++) {
            RecordStore.deleteRecordStore(stores[i]);
        }
    }

    static public void manifest() {
        listMIDP();
// #sijapp cond.if modules_DEBUGLOG is "true"#
        String content = Util.removeCr(Util.getStringAsStream("/META-INF/MANIFEST.MF"));
        DebugLog.addText("MANIFEST:\n" + content);
// #sijapp cond.end#
    }

//    public static void listMIDP2() {
//        com.sun.midp.midletsuite.Installer installer = com.sun.midp.midletsuite.Installer.getInstaller();
//        java.lang.String appList[] = installer.list();
//        if (appList == null || appList.length == 0) {
//            java.lang.System.out.println("** No MIDlet Suites installed on phone");
//        } else {
//            label0:
//            for (int i = 0; i < appList.length; i++) {
//                com.sun.midp.midlet.MIDletSuite midletSuite = installer.getMIDletSuite(appList[i]);
//                if (midletSuite == null) {
//                    java.lang.System.out.println((i + 1) + ": suite corrupted");
//                    continue;
//                }
//                java.lang.System.out.println("[" + (i + 1) + "]");
//                java.lang.System.out.println("  Name: " + midletSuite.getProperty("MIDlet-Name"));
//                java.lang.System.out.println("  Vendor: " + midletSuite.getProperty("MIDlet-Vendor"));
//                java.lang.System.out.println("  Version: " + midletSuite.getProperty("MIDlet-Version"));
//                java.lang.String temp = midletSuite.getCA();
//                if (temp != null)
//                    java.lang.System.out.println("  Authorized by: " + temp);
//                temp = midletSuite.getProperty("MIDlet-Description");
//                if (temp != null)
//                    java.lang.System.out.println("  Description: " + temp);
//                java.lang.System.out.println("  Storage name: " + appList[i]);
//                java.lang.System.out.println("  Size: " + (midletSuite.getStorageUsed() + 1023) / 1024 + "K");
//                java.lang.System.out.println("  Installed From: " + midletSuite.getDownloadUrl());
//                java.lang.System.out.println("  MIDlets:");
//                int j = 1;
//                do {
//                    temp = midletSuite.getProperty("MIDlet-" + j);
//                    if (temp == null)
//                        continue label0;
//                    com.sun.midp.midlet.MIDletInfo midletInfo = new com.sun.midp.midlet.MIDletInfo(temp);
//                    java.lang.System.out.println("    " + midletInfo.name);
//                    j++;
//                } while (true);
//            }
//
//        }
//    }

    public static void listMIDP() {
// #sijapp cond.if modules_DEBUGLOG is "true"#
//        com.sun.midp.midlet.MIDletSuite midletSuite = com.sun.midp.midlet.Scheduler.getScheduler().getMIDletSuite();
//        DebugLog.addText("MIDlet-Name: " + midletSuite.getProperty("MIDlet-Name"));
//        DebugLog.addText("MIDlet-Vendor: " + midletSuite.getProperty("MIDlet-Vendor"));
//        DebugLog.addText("MIDlet-Version: " + midletSuite.getProperty("MIDlet-Version"));
//        DebugLog.addText("Authorized by: " + midletSuite.getCA());
//        DebugLog.addText("Description: " + midletSuite.getProperty("MIDlet-Description"));
//        DebugLog.addText("Size: " + (midletSuite.getStorageUsed() + 1023) / 1024 + " KB");
//        DebugLog.addText("Installed From: " + midletSuite.getDownloadUrl());
//        DebugLog.addText("Midlet Work StorageName: " + midletSuite.getStorageName());
//        DebugLog.addText("Midlet Work StorageRoot: " + midletSuite.getStorageRoot());
//        DebugLog.addText("Midlet Work StorageUsed: " + midletSuite.getStorageUsed());
//        DebugLog.addText("InitialMIDletClassname: " + midletSuite.getInitialMIDletClassname());
//        com.sun.midp.midlet.MIDletInfo midletInfo = new com.sun.midp.midlet.MIDletInfo(midletSuite.getProperty("MIDlet-1"));
//        DebugLog.addText("Midlet Work Name: " + midletInfo.name);
//        DebugLog.addText("Midlet Work ClassName: " + midletInfo.classname);
// #sijapp cond.end#
    }

//    public static void ManagerMIDlet(String key, String value)
//	{
//        com.sun.midp.midlet.Scheduler scheduler = com.sun.midp.midlet.Scheduler.getScheduler();
//		com.sun.midp.midlet.MIDletSuite midletSuite = scheduler.getMIDletSuite();
//		if (midletSuite == null) {
//			return;
//        }
//        midletSuite.addProperty(key, value);
//        midletSuite.saveSettings();
//	}

    public static String getIOFormat() {
        return "rcs25";
    }

    static public void startExport(DataOutputStream dos) throws Exception {
        //Jimm.getSplashCanvasRef().setMessage(ResourceBundle.getEllipsisString("export_lng"));
        Jimm.setDisplay(Jimm.getSplashCanvasRef());
        //String[] stores = getAccountRecord();
        dos.writeUTF(getIOFormat());
        dos.writeInt(RecordStore.listRecordStores().length);
        exportAll(dos);
    }

//    static public String[] getAccountRecord() throws Exception {
//        StringBuffer sb = new StringBuffer();
//        String[] stores = RecordStore.listRecordStores();
//        for (int i = 0; i < stores.length; i++) {
//            RecordStore account = RecordStore.openRecordStore(stores[i], false);
//            int rec = account.getNumRecords();
//            if (rec <= 0) {
//                continue;
//            }
//            if (rec == 1) {
//                rec++;
//            }
//            for (int j = 1; j < rec; j++) {
//                byte[] buf = account.getRecord(j);
//                if (buf.length < 1) {
//                    continue;
//                }
//                if (i > 1) {
//                    sb.append("|");
//                }
//                sb.append(stores[i]).append("&r=").append(j);
//            }
//            account.closeRecordStore();
//        }
//        return Util.explode(sb.toString(), '|');
//    }

    static public void exportAll(DataOutputStream dos) throws Exception {
        String[] stores = RecordStore.listRecordStores();
        for (int i = 0; i < stores.length; i++) {
            //System.out.println(" # 1");
            RecordStore account;
            try {
                account = RecordStore.openRecordStore(stores[i], false);
                //System.out.println(" # 2");
            } catch (Exception e) {
                //System.out.println(" # 2E");
                continue;
            }
            //System.out.println(" # 3");
            if (account.getNumRecords() <= 0) {
                //System.out.println(" # 4");
                continue;
            }
            System.out.println(" # 5");
            byte[] buf;
            dos.writeUTF(account.getName()); // ACC NAME
            //System.out.println(" # 6");
            dos.writeInt(account.getNumRecords());
            // System.out.println(" # 7");
            for (int j = 1; j <= account.getNumRecords(); j++) {
                try {
                    buf = account.getRecord(j);
                    dos.writeInt(buf.length); // DATA LEN
                    dos.write(buf); // DATA
                } catch (Exception e) {
                    //System.out.println(" # 8");
                }
            }
            Jimm.getSplashCanvasRef().setProgress((100 * i) / stores.length);
            account.closeRecordStore();
        }
    }

    static public void startImport(DataInputStream dis) throws Exception {
        //Jimm.getSplashCanvasRef().setMessage(ResourceBundle.getEllipsisString("import_lng"));
        Jimm.setDisplay(Jimm.getSplashCanvasRef());
        //System.out.println(" # 1");
        try {
            String[] stores = RecordStore.listRecordStores();
            if (stores.length > 0) {
                for (int i = 0; i < stores.length; i++) {
                    RecordStore.deleteRecordStore(stores[i]);
                }
            }
        } catch (Exception ignored) {
        }
        //System.out.println(" # 2");
        try {
            if (dis.readUTF().indexOf(getIOFormat()) == -1)
                return;
        } catch (Exception e) {
            return;
        }
        int accounts = dis.readInt();
        for (int i = 0; i < accounts; i++) {
            try {
                importAll(dis);
            } catch (Exception ignored) {
                continue;
            } finally {
                Jimm.getSplashCanvasRef().setProgress((100 * i) / accounts);
            }
        }
    }

    static public void importAll(DataInputStream dis) throws Exception {
        //System.out.println(" # 3");
        String name = dis.readUTF();
        int limit = dis.readInt();
        //System.out.println(" # 4");
        RecordStore account = RecordStore.openRecordStore(name, true);
        for (int j = 0; j < limit; j++) {
            //System.out.println(" # 5");
            byte buf[] = new byte[dis.readInt()];
            dis.read(buf);
            //System.out.println(" # 6");
            account.addRecord(buf, 0, buf.length);
            //System.out.println(" # 7");
            //account.setRecord(++rec, buf, 0, buf.length);
        }
        account.closeRecordStore();
    }

    static public void saveColorScheme(boolean forSkin) {
        String str = forSkin ? "skin_cs" : "color_scheme";
        try {
            RecordStore account = RecordStore.openRecordStore(str, true);

            byte[] buf;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int i = 0; i < CanvasEx.COLORS; i++) {
                dos.writeByte(i);
                dos.writeInt(CanvasEx.getColor(i));
            }

            buf = baos.toByteArray();
            if (account.getNumRecords() == 0) {
                account.addRecord(buf, 0, buf.length);
            } else {
                account.setRecord(1, buf, 0, buf.length);
            }
            account.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    static public void csToTxt(DataOutputStream dos) throws IOException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < CanvasEx.COLORS; i++) {
            buf.setLength(0);
            if (i > 0) {
                buf.append("\r\n");
            }
            buf.append(Integer.toHexString(CanvasEx.getColor(i) | 0x01000000).substring(1));
            //buf.append(Integer.toHexString(CanvasEx.getColor(i)& 0xffffff));
            dos.write(Util.stringToByteArray(buf.toString()));
        }
    }

    static public boolean loadColorScheme(int[] colors, boolean forSkin) {
        try {
            String str = forSkin ? "skin_cs" : "color_scheme";
            /* Open record store */
            RecordStore account = RecordStore.openRecordStore(str, false);
            if (account.getNumRecords() <= 0) {
                return false;
            }

            /* Temporary variables */
            byte[] buf = account.getRecord(1);
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            DataInputStream dis = new DataInputStream(bais);

            while (dis.available() > 0) {
                int key = dis.readUnsignedByte();
                int color = dis.readInt();
                colors[key] = color;
            }
            try {
                bais.close();
                dis.close();
            } catch (Exception ignored) {
            }

            /* Close record store */
            account.closeRecordStore();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    static public void txtToCS(DataInputStream dis) {
        try {
            int colors[] = new int[CanvasEx.COLORS];
            for (int i = 0; i < CanvasEx.COLORS; i++) {
                if (i > 0) {
                    try {
                        dis.skipBytes(2);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    byte buf[] = new byte[6];
                    dis.read(buf);
                    colors[i] = Integer.parseInt(Util.byteArrayToString(buf), 16);
                } catch (Exception e) {
                    colors[i] = 0x000000;
                }
            }
            CanvasEx.setColors(colors);
            JimmUI.setColorScheme();
        } catch (Exception ignored) {
        }
    }

    /* Option retrieval methods (no type checking!) */
    static public synchronized String getString(int key) {
        switch (key) {
            case OPTION_UIN:
                return getAccStr(uins, getInt(OPTIONS_CURR_ACCOUNT));
            case OPTION_PASSWORD:
                return getAccStr(passwords, getInt(OPTIONS_CURR_ACCOUNT));
            case OPTION_MY_NICK:
                return getAccStr(nicks, getInt(OPTIONS_CURR_ACCOUNT));
        }
        return optionsString[key];
    }

    private static String getAccStr(Vector accs, int idx) {
        if (idx < 0 || idx >= accs.size()) {
            return emptyString;
        }
        return (String) accs.elementAt(idx);
    }

    static public synchronized int getInt(int key) {
        return optionsInteger[key];
    }

    static public synchronized boolean getBoolean(int key) {
        return optionsBoolean[key];
    }


    /* Option setting methods (no type checking!) */
    static public synchronized void setString(int key, String value) {
        if (key == OPTION_PASSWORD) {
            optionsPass[key - 100] = value;
            return;
        }
        optionsString[key] = value;
        if (key == OPTION_HOTKEYS_HASH) {
            loadKeys(keysAct);
        }
    }

    static public synchronized void setInt(int key, int value) {
        optionsInteger[key] = value;
    }

    static public synchronized void setBoolean(int key, boolean value) {
        optionsBoolean[key] = value;
    }

    static public synchronized void changePassword(String pass, int acIdx) {
        if (pass == null || acIdx < 0) {
            return;
        }
        String[] passwords = Util.explode(optionsPass[OPTION_PASSWORD - 100], '\t');
        if (acIdx >= passwords.length) {
            return;
        }
        passwords[acIdx] = pass;
        int len = passwords.length;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                buf.append('\t');
            }
            buf.append(passwords[i]);
        }
        setString(OPTION_PASSWORD, buf.toString());
        safe_save();
    }

    /* Constants for connection type */
    public static final int CONN_TYPE_SOCKET = 0;
    //#sijapp cond.if modules_HTTP is "true"#
    public static final int CONN_TYPE_HTTP = 1;
    public static final int CONN_TYPE_PROXY = 2;
    //#sijapp cond.else#
    public static final int CONN_TYPE_PROXY = 1;
//#sijapp cond.end#

    static public void editOptions(Icq icq) {
        // Construct option form
        optionsForm = null;
        try {
            (optionsForm = new OptionsForm(icq)).activate();
        } catch (OutOfMemoryError oome) {
            oome.printStackTrace();
            System.gc();
        }
    }

    public static OptionsForm getOptionsForm() {
        return optionsForm;
    }

    public static void destroyForm() {
        optionsForm = null;
    }

    public static void showAccount(Icq icq) {
        JimmUI.getNative().menuSelect(null, JimmUI.FUNCTION_MENU_PROFILE_OPTIONS);
        //editOptions(icq);
        //optionsForm.menuSelect(null, OptionsForm.OPTIONS_ACCOUNT);
    }

    // #sijapp cond.if (target isnot "DEFAULT" & target isnot "RIM" & modules_SOUNDS is "true")#
    private static void selectSoundType(String name, int option) {
        boolean ok;

        /* Test existsing option */
        ok = Notify.testSoundFile(getString(option));
        if (ok) return;

        /* Test other extensions */
        String[] exts = Util.explode("mp3|wav", '|');
        String testFile;
        for (int i = 0; i < exts.length; i++) {
            testFile = name + exts[i];
            ok = Notify.testSoundFile(testFile);
            if (ok) {
                setString(option, testFile);
                break;
            }
        }
    }
//#sijapp cond.end#
}