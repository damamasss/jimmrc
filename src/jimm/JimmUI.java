/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-06  Jimm Project

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
 File: src/jimm/JimmUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import DrawControls.*;
import jimm.chat.ChatHistory;
import jimm.chat.ChatTextList;
import jimm.comm.*;
//#sijapp cond.if modules_FILES is "true"#
import jimm.files.FileTransfer;
//#sijapp cond.end#
import jimm.forms.FormEx;
import jimm.forms.StatusesForm;
import jimm.forms.OptionsForm;
import jimm.info.ClientInfo;
import jimm.info.UserInfo;
import jimm.ui.*;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class JimmUI implements CommandListener, SelectListener, MenuListener {

    public final static Command cmdOk = new Command(ResourceBundle.getString("ok"), Command.OK, 1);
    public final static Command cmdYes = new Command(ResourceBundle.getString("yes"), Command.OK, 1);
    public final static Command cmdSend = new Command(ResourceBundle.getString("send"), Command.OK, 1);
    public static Command cmdSelect = new Command(ResourceBundle.getString("select"), Command.OK, 1);
    public final static Command cmdNo = new Command(ResourceBundle.getString("no"), Command.CANCEL, 2);
    public final static Command cmdCancel = new Command(ResourceBundle.getString("cancel"), Jimm.is_smart_SE() ? Command.CANCEL : Command.BACK, 9);
    public final static Command cmdBack = new Command(ResourceBundle.getString("back"), Jimm.is_smart_SE() ? Command.CANCEL : Command.BACK, 2);
    public final static Command cmdMenu = new Command(ResourceBundle.getString("option"), Command.ITEM, 4);
    public static final Command cmdSave = new Command(ResourceBundle.getString("save"), Command.SCREEN, 1);
    public final static Command transliterateCommand = new Command(ResourceBundle.getString("transliterate"), Command.ITEM, 3);
    public final static Command detransliterateCommand = new Command(ResourceBundle.getString("detransliterate"), Command.ITEM, 3);
    public final static Command cmdClear = new Command(ResourceBundle.getString("clear"), Command.ITEM, 8);
    public final static Command cmdQuote = new Command(ResourceBundle.getString("quote"), Command.ITEM, 6);
    public final static Command cmdPaste = new Command(ResourceBundle.getString("paste"), Command.ITEM, 6);
    public final static Command cmdInsertEmo = new Command(ResourceBundle.getString("insert_emotion"), Command.ITEM, 2);
    public final static Command cmdInsTemplate = new Command(ResourceBundle.getString("templates"), Command.ITEM, 4);
    public final static Command cmdSendAll = new Command(ResourceBundle.getString("massive_send"), Command.ITEM, 6);
    public final static Command cmdNext = new Command(ResourceBundle.getString("next"), Command.ITEM, 5);
    public final static Command cmdPrev = new Command(ResourceBundle.getString("prev"), Command.ITEM, 5);

    static private JimmUI jimmui;

    public JimmUI() {
        jimmui = this;
        if (NativeCanvas.getWidthEx() <= 176) {
            cmdSelect = new Command(ResourceBundle.getString("select_alt"), Command.OK, 1);
        }
    }

    public static JimmUI getNative() {
        return jimmui;
    }

    public void commandAction(Command c, Displayable d) {
        if (isControlActive(massList)) {
            if (massUIMode == MASS_MODE) {
                if (c == cmdBack) {
                    Jimm.back();
                    textMessReceiver.beginTyping(false);
                    massList = null;
                    sendAllList = null;
                } else if (c == cmdSelect) {
                    checkType = sendAllList.getSelectedIndex();
                    sendAllList = null;
                    if (checkType < 2) {
                        sendAll(checkType == 1);
                        cItems = null;
                    } else {
                        massUIMode = MASS_ITEMS;
                        showItemsCheck();
                    }
                }
            } else {
                if (c == cmdBack) {
                    Jimm.back();
                    textMessReceiver.beginTyping(false);
                    checkList = null;
                    massList = null;
                } else if (c == cmdSelect) {
                    send2CheckedItems();
//                    Menu m = new Menu(massList);
//                    m.addMenuItem("send", (byte)0);
//                    m.addMenuItem("select_all", (byte)-1);
//                    m.addMenuItem("select_none", (byte)-2);
//                    m.setMenuListener(new MenuListener() {
//                        public void menuSelect(Menu menu, byte action) {
//                            switch (action) {
//                                case 0:
//                                    send2CheckedItems();
//                                    return;
//                                case -1:
//                                    if (checkList != null) {
//                                        for (int i = 0; i < checkList.size(); i++) {
//                                            checkList.setSelectedIndex(i, true);
//                                        }
//                                    }
//                                    break;
//                                case -2:
//                                    if (checkList != null) {
//                                        for (int i = 0; i < checkList.size(); i++) {
//                                            checkList.setSelectedIndex(i, false);
//                                        }
//                                    }
//                                    break;
//                            }
//                            massList.clear();
//                            massList.append(checkList);
//                            if (menu != null) {
//                                menu.back();
//                            }
//                        }
//                    });
//                    Jimm.setDisplay(m);
                }
            }
        }
    }

    /**
     * *************************Massive send*******************************
     */

    private static final boolean MASS_MODE = false;
    private static final boolean MASS_ITEMS = true;

    private static FormEx massList;
    private static ChoiceGroupEx sendAllList;
    private static ChoiceGroupEx checkList;
    private static boolean massUIMode = MASS_MODE;
    private static String text2MassSend;
    private int checkType = -1;
    private Vector cItems;

    public static void showMassiveSendList(String text) {
        text2MassSend = text;
        massUIMode = MASS_MODE;
        massList = new FormEx(ResourceBundle.getString("massive_send"), cmdSelect, cmdBack);
        sendAllList = new ChoiceGroupEx(null, Choice.EXCLUSIVE);
        sendAllList.append(ResourceBundle.getString("send_all"), null);
        sendAllList.append(ResourceBundle.getString("send_only_online"), null);
        sendAllList.append(ResourceBundle.getString("send_citems"), null);
        sendAllList.append(ResourceBundle.getString("send_gitems"), null);
        massList.append(sendAllList);
        massList.setCommandListener(jimmui);
        Jimm.setDisplay(massList);
    }

    private void showItemsCheck() {
        massList = new FormEx(ResourceBundle.getString("send_check"), cmdSelect, cmdBack);
        checkList = new ChoiceGroupEx(null, Choice.MULTIPLE);
        cItems = Jimm.getCurrentProfile().getItems(checkType);
        int size = cItems.size();
        ContactListItem item;
        Icon icon;
        for (int i = 0; i < size; i++) {
            item = (ContactListItem) cItems.elementAt(i);
            icon = ContactList.imageList.elementAt(item.getStatusImageIndex());
            checkList.append(item.getText(), (icon == null) ? null : icon.getImg());
        }
        massList.append(checkList);
        massList.setCommandListener(this);
        Jimm.setDisplay(massList);
    }

    private void send2CheckedItems() {
        int size = checkList.size();
        Vector result = new Vector();
        for (int i = 0; i < size; i++) {
            if (checkList.isSelected(i)) {
                result.addElement(cItems.elementAt(i));
            }
        }
        StringBuffer messText = new StringBuffer();
        messText.append(text2MassSend);
        Jimm.getContactList().sendAll(false, messText.toString(), result);
        checkList = null;
        sendAllList = null;
        cItems = null;
        text2MassSend = null;
        massList = null;
    }

    private void sendAll(boolean only4online) {
        Jimm.getContactList().sendAll(only4online, text2MassSend);
        massList = null;
    }

    /****************************Massive send end****************************/


    /**
     * *****************************Clipboard******************************
     */

    static private StringBuffer[] stringBuffers = {new StringBuffer(), new StringBuffer()};   // 0 - text, 1 - quote
    //static private StringBuffer clipBoardText = new StringBuffer();
    //static private StringBuffer clipBoardQute = new StringBuffer();

    static private String insertQuotingChars(String text, String qChars) {
        StringBuffer result = new StringBuffer(text.length() + 20);
        int size = text.length();
        boolean wasNewLine = true;
        for (int i = 0; i < size; i++) {
            char chr = text.charAt(i);
            if (wasNewLine) result.append(qChars);
            result.append(chr);
            wasNewLine = (chr == '\n');
        }
        return result.toString();
    }

    static public boolean clipBoardIsEmpty() {
        boolean empty = true;
        for (int i = 0; i < 2; i++) {
            empty &= (stringBuffers[i].length() < 1);
        }
        return empty;
    }

    static public String getClipBoardText(boolean quote) {
        return stringBuffers[(quote ? 1 : 0)].toString();
    }

    static public String prepareClipBoardQute(String clipBoardText, String clipBoardHeader, boolean clipBoardIncoming, int buffer) {
        if (buffer < 1) {
            return clipBoardText;
        }
        StringBuffer sb = new StringBuffer();
        String quoteChars = clipBoardIncoming ? "> " : "< ";
//        try{
//            //quoteChars = clipBoardIncoming ? "\u203A " : "\u2039 ";
//            quoteChars = clipBoardIncoming ? "\u00BB " : "\u00AB ";  //
//        } catch (Exception e) {
//            quoteChars = clipBoardIncoming ? "> " : "< ";
//        }
        // >> = >>>
        // >> = (1010 = 10100) на указанное число
        //
        // todo (new StringBuilder()).append("\"").append(s).append("\"").toString();
        // Nick [25.10.2010 14:51] "Привет. Что делаешь" - попробовать
        //
        // > Nick [25.10.2010 14:51]
        // > Привет. Что делаешь
        //
        // Nick [25.10.2010 14:51]
        // "Привет. Что делаешь"

        if (clipBoardHeader != null) {
            sb.append(quoteChars);
            sb.append('[').append(clipBoardHeader.trim()).append(']').append('\n');
        }
        if (clipBoardText != null) {
            sb.append(insertQuotingChars(clipBoardText, quoteChars));
        }
        return sb.toString();
    }

    static private String getHeader(String date, String from) {
        StringBuffer clipBoardHeader = new StringBuffer();
        if (date != null && !date.equals("<no_date>")) {
            clipBoardHeader.append(from).append(' ').append(date);
        } else {
            clipBoardHeader.append(from);
        }
        return clipBoardHeader.toString();
    }

    static public void setClipBoardText(String text) {
        setClipBoardText(text, false);
    }

    static public void setClipBoardText(String text, boolean add) {
        if (!add) clearClipBoard();
        for (int i = 0; i < 2; i++) {
            stringBuffers[i].append(prepareClipBoardQute(text, null, true, i));
        }
    }

    static public void setClipBoardText(boolean incoming, String date, String from, String text) {
        setClipBoardText(incoming, date, from, text, false);
    }

    static public void setClipBoardText(boolean incoming, String date, String from, String text, boolean add) {
        if (!add) clearClipBoard();
        for (int i = 0; i < 2; i++) {
            stringBuffers[i].append(prepareClipBoardQute(text, getHeader(date, from), incoming, i));
        }
    }

    public static void clearClipBoard() {
        for (int i = 0; i < 2; i++) {
            stringBuffers[i] = new StringBuffer();
        }
    }

    /******************************Clipboard end*****************************/

    /**
     * ***************************Color scheme*****************************
     */

//    static public void setColorScheme(VirtualList virtualList) {
//        if (virtualList == null) {
//            return;
//        }
//        virtualList.setColors(
//                CanvasEx.getColors()[CanvasEx.COLOR_CAP],
//                CanvasEx.getColors()[CanvasEx.COLOR_TEXT],
//                CanvasEx.getColors()[CanvasEx.COLOR_BACK],
//                CanvasEx.getColors()[CanvasEx.COLOR_CURSOR],
//                CanvasEx.getColors()[CanvasEx.COLOR_MBACK1],
//                CanvasEx.getColors()[CanvasEx.COLOR_MCURSOR],
//                CanvasEx.getColors()[CanvasEx.COLOR_DCURSOR],
//                CanvasEx.getColors()[CanvasEx.COLOR_SCROLL],
//                CanvasEx.getColors()[CanvasEx.COLOR_CAP_TEXT],
//                CanvasEx.getColors()[CanvasEx.COLOR_BAR_TEXT],
//                CanvasEx.getColors()[CanvasEx.COLOR_MENU_TEXT],
//                CanvasEx.getColors()[CanvasEx.COLOR_SYSTEM]
//        );
//    }
    static public void setColorScheme() {
// #sijapp cond.if modules_HISTORY is "true" #
        HistoryStorage.setColorScheme();
// #sijapp cond.end#
        Jimm.getContactList().setColorScheme();
        Profiles.setColorSchemes();
    }

    /**************************Color scheme end******************************/

    /**
     * ****************************Hotkeys*********************************
     */

    private static long lockPressedTime = -1;
    //#sijapp cond.if target is "MIDP2"#
    private static boolean lightsOff;
// #sijapp cond.end#

    static public void execHotKey(ContactItem cItem, int keyCode, int type) {
        //System.out.println("# execHotKey");
        execHotKeyAction((type != CanvasEx.KEY_PRESSED) ? Options.keyAction((type != CanvasEx.KEY_REPEATED) ? keyCode : keyCode + 1000) : -1, cItem, type);
    }

    static public void execHotKeyAction(int actionNum) {
        TreeNode node = Jimm.getContactList().getCurrentItem();
        Object obj = (node == null) ? null : node.getData();
        ContactItem item = ((obj != null) && (obj instanceof ContactItem)) ? (ContactItem) obj : null;
        lockPressedTime = System.currentTimeMillis() + 1000;
        execHotKeyAction(actionNum, item, VirtualList.KEY_RELEASED);
    }

    static private void execHotKeyAction(int actionNum, ContactItem item, int keyType) {
        if (keyType == CanvasEx.KEY_PRESSED) {
            lockPressedTime = System.currentTimeMillis();
            return;
        }
        if (keyType == CanvasEx.KEY_REPEATED) {
            if ((System.currentTimeMillis() - lockPressedTime) <= 900) {
                return;
            }
            lockPressedTime = -1;
            NativeCanvas.getCanvas().isPressed = false;
        }

        switch (actionNum) {
            case Options.HOTKEY_INFO:
                if (item != null) {
                    (new UserInfo()).requiestUserInfo(item.getProfile(), item.getUinString(), item.name);
                    clciContactMenu = item;
                }
                break;

            case Options.HOTKEY_NEWMSG:
                if ((item != null) && !(item.getProfile().connectionIsActive())) {
                    writeMessage(item, null).activate();
                }
                break;

// #sijapp cond.if modules_HISTORY is "true" #
            case Options.HOTKEY_HISTORY:
                if (item != null) {
                    HistoryStorage.showHistoryList(item);
                }
                break;
// #sijapp cond.end#

            case Options.HOTKEY_ONOFF:
                boolean hide = Options.getBoolean(Options.OPTION_SHOW_OFFLINE);
                Options.setBoolean(Options.OPTION_SHOW_OFFLINE, !hide);
                Options.safe_save();
                Jimm.getContactList().optionsChanged();
                Jimm.getContactList().rebuild();
                break;

            case Options.HOTKEY_OPTIONS:
                Options.editOptions(Jimm.getIcqRef());
                break;

            case Options.HOTKEY_LOCK:
                Jimm.getSplashCanvasRef().lockProgramm();
                break;

            case Options.HOTKEY_MINIMIZE:
                Jimm.setMinimized(true);
                break;

            case Options.HOTKEY_CLI_INFO:
                if (item != null) {
                    (new ClientInfo()).showClientInfo(item);
                }
                break;

            case Options.HOTKEY_STATUS_MSG:
                requestStatusMess(item);
                break;

// #sijapp cond.if modules_SOUNDS is "true"#
            case Options.HOTKEY_SOUNDOFF:
                Jimm.getContactList().changeSoundMode();
                Jimm.getContactList().activate();
                break;
// #sijapp cond.end#

            case Options.HOTKEY_DEL_CHATS:
                Jimm.getCurrentProfile().getChatHistory().chatHistoryDelete(null, ChatHistory.DEL_TYPE_ALL);
                Jimm.getContactList().activate();
                break;

            case Options.HOTKEY_XTRAZ_MSG:
                if (item != null) {
                    item.requestXStatusText();
                }
                break;

// #sijapp cond.if modules_MAGIC_EYE is "true" #
            case Options.HOTKEY_MAGIC_EYE:
                Jimm.getCurrentProfile().getMagicEye().activateEx(null);
                break;
//#sijapp cond.end#

            case Options.HOTKEY_AUTO_ANSWER:
                boolean flag = Options.getBoolean(Options.OPTION_AUTO_ANSWER);
                Options.setBoolean(Options.OPTION_AUTO_ANSWER, !flag);
                Options.safe_save();
                String texts = ResourceBundle.getString("auto_is_on");
                if (flag) texts = ResourceBundle.getString("auto_is_off");
                Jimm.setDisplay(new Alert(null, texts, null, AlertType.INFO));
                break;

            case Options.HOTKEY_VIBRO:
                int type = Options.getInt(Options.OPTION_VIBRATOR);
                if (type < 1) {
                    Options.setInt(Options.OPTION_VIBRATOR, 1);
                } else {
                    Options.setInt(Options.OPTION_VIBRATOR, 0);
                }
                Options.safe_save();
                String text;
                if (type > 0) {
                    text = ResourceBundle.getString("vibro_is_off");
                } else {
                    text = ResourceBundle.getString("vibro_is_on");
                }
                Jimm.setDisplay(new Alert(null, text, null, AlertType.INFO));
                break;

//#sijapp cond.if target is "MIDP2"#
            case Options.HOTKEY_FLASH:
                setLights(lightsOff ? 0x64 : 0x00);
                lightsOff = !(lightsOff || Options.getBoolean(Options.OPTION_COMBO_KEYS));
                break;
// #sijapp cond.end#

            case Options.HOTKEY_STATUS:
                //Jimm.setDisplay(new StatusesMenu(Jimm.getContactList(), StatusesMenu.STATE_MAIN, Jimm.getIcqRef()));
                (new StatusesForm(null, Jimm.getIcqRef())).activate();
                break;

            case Options.HOTKEY_GROUPS:
                boolean grs = Options.getBoolean(Options.OPTION_USER_GROUPS);
                Options.setBoolean(Options.OPTION_USER_GROUPS, !grs);
                Options.safe_save();
                Jimm.getContactList().optionsChanged();
                Jimm.getContactList().activate();
                break;

            case Options.HOTKEY_CHATLIST:
                ContactList clc = Jimm.getContactList();
                new ChatList(clc, clc.getProfile());
                break;

            case Options.HOTKEY_PROFILES:
                Profiles.showProfiles(Jimm.getContactList());
                break;

            case Options.HOTKEY_PREV_PROFILE:
                int i = -1;
                if (Options.uins.size() > 1) {
                    i = Jimm.getCurrentProfile().getIndex() - 1;
                    if (i < 0) {
                        i = Options.uins.size() - 1;
                    }
                }
                Profiles.setProfile(i);
                break;

            case Options.HOTKEY_NEXT_PROFILE:
                int j = -1;
                if (Options.uins.size() > 1) {
                    j = Jimm.getCurrentProfile().getIndex() + 1;
                    j %= Options.uins.size();
                }
                Profiles.setProfile(j);
                break;

            case Options.HOTKEY_GET_STATUS:
                Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.GET_STATUS), 10);
                break;

            case Options.HOTKEY_MANAGER:
                Icq icq = Jimm.getIcqRef();
                if (icq.isConnected()) {
                    Jimm.setDisplay(new ManageCList(icq));
                }
                //else {
                //    Jimm.setDisplay(new Alert(null, ResourceBundle.getString("connect"), null, AlertType.INFO));
                //}
                break;

            case Options.HOTKEY_CONNECT_HOT:
                ContactList cl = Jimm.getContactList();
                Profile profile = cl.getProfile();
                if (!profile.connectionIsActive() && !profile.getIcq().isConnected()) {
                    cl.beforeConnect();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                    }
                    if (cl.connect()) {
                        break;
                    }
                }
                break;
        }
    }

    /*****************************Hotkeys end*********************************/

    /**
     * **************************Selectors**********************************
     */

    private static String lastUin;

    static public void showSelector(String[] elements, SelectListener listener, boolean translateWords) {
        SelectBase base = new SelectBase(elements, listener, translateWords);
        Jimm.setDisplay(base);
    }

    static public String getLastUin() {
        return lastUin;
    }

    static public void setLastUin(String uin) {
        lastUin = uin;
    }

    public final static long[] statuses = {
            ContactItem.STATUS_AWAY,
            ContactItem.STATUS_CHAT,
            ContactItem.STATUS_DND,
            ContactItem.STATUS_INVISIBLE,
            ContactItem.STATUS_NA,
            ContactItem.STATUS_OCCUPIED,
            ContactItem.STATUS_OFFLINE,
            ContactItem.STATUS_ONLINE,
            ContactItem.STATUS_EVIL,
            ContactItem.STATUS_DEPRESSION,
            ContactItem.STATUS_HOME,
            ContactItem.STATUS_WORK,
            ContactItem.STATUS_LUNCH,
            ContactItem.STATUS_INVIS_ALL,
    };

    private static int getStatusIndex(long status) {
        for (int i = statuses.length - 1; i >= 0; i--) {
            if (statuses[i] == status) {
                return i;
            }
        }
        return -1;
    }

    public static int getStatusImageIndex(long status) {
        //int index = getStatusIndex(status);
        //return index;
        return getStatusIndex(status);
    }

    public static String[] getStatusesStrings() {
        return Util.explode(
                new StringBuffer().append("status_away").append("|")
                        .append("status_chat").append("|")
                        .append("status_dnd").append("|")
                        .append("status_invisible").append("|")
                        .append("status_na").append("|")
                        .append("status_occupied").append("|")
                        .append(".").append("|")
                        .append("status_online").append("|")
                        .append("status_evil").append("|")
                        .append("status_depression").append("|")
                        .append("status_home").append("|")
                        .append("status_work").append("|")
                        .append("status_lunch").append("|")
                        .append("status_invis_all").toString()
                , '|'
        );
    }

    public static String getStatusString(long status) {
        int index = getStatusIndex(status);
        return (index == -1) ? null : ResourceBundle.getString(getStatusesStrings()[index]);
    }

    public static int groupIds[] = null;
    public static final int SHS_TYPE_ALL = -0;
    public static final int SHS_TYPE_EMPTY = 1;

    public static int[] showGroupSelector(Profile profile, SelectListener listener, int type, int excludeGroupId) {
        GroupItem[] groups = profile.getGroupItems();
        String[] groupNamesTmp = new String[groups.length];
        int[] groupIdsTmp = new int[groups.length];

        int index = 0;
        ContactItem[] cItems;
        for (int i = 0; i < groups.length; i++) {
            int groupId = groups[i].getId();
            if (groupId == excludeGroupId) {
                continue;
            }
            switch (type) {
                case SHS_TYPE_EMPTY:
                    cItems = profile.getItemsFromGroup(groupId);
                    if (cItems.length != 0) {
                        continue;
                    }
                    break;
            }

            groupIdsTmp[index] = groupId;
            groupNamesTmp[index] = groups[i].getName();
            index++;
        }

        if (index == 0) {
            Alert alert = new Alert("", ResourceBundle.getString("no_availible_groups"), null, AlertType.INFO);
            alert.setTimeout(Alert.FOREVER);
            Jimm.setDisplay(alert);
            return null;
        }
        String[] groupNames = new String[index];
        int[] groupIds = new int[index];
        System.arraycopy(groupIdsTmp, 0, groupIds, 0, index);
        System.arraycopy(groupNamesTmp, 0, groupNames, 0, index);
        showSelector(groupNames, listener, false);
        return groupIds;
    }

    public static void addTextListItem(TextList list, String text, Icon image, int value, boolean translate, boolean doCRLF) {
        if (image != null) {
            list.addImage(image, null, value);
        }
        String textToAdd = translate ? ResourceBundle.getString(text) : text;

        list.addBigText(textToAdd, list.getTextColor(), Font.STYLE_PLAIN, value);
        if (doCRLF) {
            list.doCRLF(value);
        }
    }

    public static void addTextListItem(TextList list, String text, Icon image, int value, boolean translate) {
        addTextListItem(list, text, image, value, translate, true);
    }

    static public boolean isControlActive(VirtualList list) {
        return list != null && list.isActive();
    }

    /***************************Selectors end*********************************/

    /**
     * *******************Text editor for messages**************************
     */

    private static ContactItem textMessReceiver;

    public static InputTextBox writeMessage(ContactItem receiver, String initText) {
        //if (InputTextBox.isShownEx()) {
        //    return;
        //}
        String title = (Options.getBoolean(Options.OPTION_EMPTY_TITLE)) ? null : receiver.name;
        textMessReceiver = receiver;
        lastUin = receiver.getUinString();
        receiver.beginTyping(true);
        return new InputTextBox(InputTextBox.EDITOR_MODE_MESSAGE, title, initText);
    }

    public static void authOperation(String reasonText) {
        SystemNotice notice = null;
        switch (authType) {
            case AUTH_TYPE_DENY:
                notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, authContactItem, false, reasonText);
                break;
            case AUTH_TYPE_REQ_AUTH:
                notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, authContactItem, false, reasonText);
                break;
        }
        SysNoticeAction sysNotAct = new SysNoticeAction(notice);
        UpdateContactListAction updateAct = new UpdateContactListAction(authContactItem, UpdateContactListAction.ACTION_REQ_AUTH);
        try {
            authContactItem.getIcq().requestAction(sysNotAct);
            if (authContactItem.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP)) {
                authContactItem.getIcq().requestAction(updateAct);
            }
        } catch (JimmException e) {
            JimmException.handleException(e);
            if (e.isCritical()) {
                return;
            }
        }
        authContactItem.setIntValue(ContactItem.CONTACTITEM_AUTREQUESTS, 0);
        if (!authContactItem.getProfile().getChatHistory().activateIfExists(authContactItem)) {
            Jimm.back();
        }
    }

    public static void hideTextBox(boolean message) {
        Jimm.back();
        if (message) {
            textMessReceiver.beginTyping(false);
        } else {
            authContactItem = null;
        }
    }

    public static void sendMessage(String text) throws Exception {
        sendMessage(text, textMessReceiver);
        boolean activated = textMessReceiver.getProfile().getChatHistory().activateIfExists(textMessReceiver);
        if (!activated) {
            Jimm.back();
        }
    }

    public static void sendMessage(String text, ContactItem textMessReceiver) throws NullPointerException {
        if (text == null) {
            textMessReceiver.beginTyping(false);
            return;
        }
        switch (Options.getInt(Options.OPTION_MESSAGE_TYPE)) {
            case 1:
                text = (new StringConvertor()).transliterate(text);
                break;
            case 2:
                text = (new StringConvertor()).detransliterate(text);
                break;
        }
        int length = text.length();
        if (length == 0) {
            textMessReceiver.beginTyping(false);
            return;
        }
        String text2send;
        PlainMessage plainMsg;
        SendMessageAction sendMsgAct;
        for (int i = 0; i < length; i += 1024) {
            text2send = text.substring(i, Math.min(i + 1024, length));
            plainMsg = new PlainMessage(textMessReceiver.getIcq().getUin(), textMessReceiver,
                    Message.MESSAGE_TYPE_NORM, DateAndTime.createCurrentDate(false), text2send);
            sendMsgAct = new SendMessageAction(plainMsg);
            long msgId = sendMsgAct.getMsgId();
            try {
                textMessReceiver.getIcq().requestAction(sendMsgAct);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    return;
                }
            }
            textMessReceiver.getProfile().getChatHistory().getChat(textMessReceiver).addMyMessage(text2send, plainMsg.getNewDate(), msgId);
//#sijapp cond.if modules_HISTORY is "true" #
            if (Options.getBoolean(Options.OPTION_HISTORY) || textMessReceiver.getExtraValue(ContactItem.EXTRA_HIST)) {
                HistoryStorage.addText(textMessReceiver, text2send, (byte) 1, textMessReceiver.getProfile().getNick(), plainMsg.getNewDate());
            }
//#sijapp cond.end#
            if (i + 1024 < length) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
            }
        }
        textMessReceiver.beginTyping(false);
        textMessReceiver.autoAnswered |= true;
    }

//    static class TextListSens extends TextList {
//        TextListSens(String cap) {
//            super(cap);
//        }
//
//        public void doKeyreaction(int keyCode, int type) {
//            if (Canvas.KEY_NUM1 <= keyCode && keyCode <= Canvas.KEY_NUM9) {
//                if (keyCode - Canvas.KEY_NUM1 < getSize() - 1) {
//                    currItem = keyCode - Canvas.KEY_NUM1;
//                    pointSelect();
//                    return;
//                }
//            }
//            super.doKeyreaction(keyCode, type);
//        }
//    }

    public static void activateMessMenu(String cap, String strings[], Icon icons[], final InputListener inputListener, final Object back) {
        final TextList list = new TextList(cap) {
            public void doKeyreaction(int keyCode, int type) {
                if (Canvas.KEY_NUM1 <= keyCode && keyCode <= Canvas.KEY_NUM9) {
                    if (keyCode - Canvas.KEY_NUM1 < getSize() - 1) {
                        setCurrentItem(keyCode - Canvas.KEY_NUM1);
                        pointSelect();
                        return;
                    }
                }
                super.doKeyreaction(keyCode, type);
            }
        };
        list.setMode(TextList.MODE_TEXT);
        for (int i = 0; i < strings.length; i++) {
            addTextListItem(list, strings[i], icons == null ? ContactList.authIcon : icons[i], i, false);
        }
        list.setColorScheme();
        list.addCommandEx(cmdOk, VirtualList.MENU_TYPE_LEFT_BAR);
        list.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_RIGHT_BAR);
        list.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.equals(cmdOk)) {
                    if (inputListener != null) {
                        inputListener.action(list.getCurrIndex(), back);
                    }
                } else {
                    Jimm.setDisplay(back);
                }
            }
        });
        list.setVLCommands(new VirtualListCommands() { // проверить, активно ли нажатие на центральную клавишу при наличии этого листенера

            public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
//                if (Canvas.KEY_NUM1 <= keyCode && keyCode <= Canvas.KEY_NUM9) {
//                    if (inputListener != null) {
//                        inputListener.action(keyCode - 49, back);
//                    }
//                }
            }

            public void vlCursorMoved(VirtualList sender) {
            }

            public void vlItemClicked(VirtualList sender) {
                if (inputListener != null) {
                    inputListener.action(sender.getCurrIndex(), back);
                }
            }
        });
        list.activate();
    }

    /********************Text editor for messages end*************************/

    /**
     * *******************************URLs**********************************
     */

    public static void gotoURL(String msg) {
        SelectBase base = new SelectBase(msg);
        Jimm.setDisplay(base);
    }

    /********************************URLs end*********************************/

    /**
     * ***************************Auth Message******************************
     */

    private static int authType;
    private static ContactItem authContactItem;

    public static final int AUTH_TYPE_DENY = 10001;
    public static final int AUTH_TYPE_REQ_AUTH = 10002;


    public static void authMessage(int authType, ContactItem contactItem, String caption, String text) {
        JimmUI.authType = authType;
        authContactItem = contactItem;
        new InputTextBox(InputTextBox.EDITOR_MODE_AUTH_MESSAGE, ResourceBundle.getString(caption), ResourceBundle.getString(text)).activate();
    }

    /****************************Auth Message end*****************************/

    /**
     * ***************************Contact Menu******************************
     */

    private static final byte USER_MENU_MESSAGE = (byte) 1;
    private static final byte USER_MENU_STATUS_MESSAGE = (byte) 2;
    private static final byte USER_MENU_REQU_AUTH = (byte) 3;
    // #sijapp cond.if modules_FILES is "true"#
    private static final byte USER_MENU_FILE_TRANS = (byte) 4;
    // #sijapp cond.if target isnot "MOTOROLA" #
    private static final byte USER_MENU_CAM_TRANS = (byte) 5;
    // #sijapp cond.end#
    // #sijapp cond.end#
    private static final byte USER_MENU_SCHEME_TRANS = (byte) 6;
    private static final byte USER_MENU_USER_REMOVE = (byte) 7;
    private static final byte USER_MENU_REMOVE_ME = (byte) 8;
    private static final byte USER_MENU_RENAME = (byte) 9;
    // #sijapp cond.if modules_HISTORY is "true"#
    private static final byte USER_MENU_HISTORY = (byte) 10;
    // #sijapp cond.end#
    private static final byte USER_MENU_LOCAL_INFO = (byte) 11;
    private static final byte USER_MENU_USER_INFO = (byte) 12;
    private static final byte USER_MENU_COPY_UIN = (byte) 13;
    private static final byte USER_MENU_MOVE = (byte) 14;
    private static final byte USER_MENU_LIST_OPERATION = (byte) 15;
    private static final byte USER_MENU_XTRAZ_MESSAGE = (byte) 16;
    private static final byte USER_MENU_ADD = (byte) 17;
    private static final byte USER_MENU_CHECK_STATUS = (byte) 18;
    private static final byte USER_MENU_EXTRA = (byte) 19;

    private static ContactItem clciContactMenu;
    private static int clciGroupMenu;

    public static void fillContactMenu(ContactItem contact, Menu tlContactMenu) {
        clciContactMenu = contact;
        long status = contact.getIntValue(ContactItem.CONTACTITEM_STATUS);
        boolean isConnected = contact.getIcq().isConnected();
        boolean connectIsActive = contact.getProfile().connectionIsActive();
        boolean isbest = contact.canWin1251();
        if (!connectIsActive) {
            tlContactMenu.addMenuItem("send_message", ContactList.imageList.elementAt(14), USER_MENU_MESSAGE);
        }
        if (isConnected) {
            if (contact.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH)) {
                tlContactMenu.addMenuItem("requauth", ContactList.imageList.elementAt(16), USER_MENU_REQU_AUTH);
            }

            if ((contact.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) && !contact.getBooleanValue(ContactItem.CONTACTITEM_ADDED))
                    || contact.getBooleanValue(ContactItem.CONTACTITEM_PHANTOM)) {
                tlContactMenu.addMenuItem("user_add", ContactList.menuIcons.elementAt(29), USER_MENU_ADD);
            }
        }
        if (status != ContactItem.STATUS_OFFLINE) {
//#sijapp cond.if modules_FILES is "true"#
            tlContactMenu.addMenuItem("ft_name", ContactList.menuIcons.elementAt(8), USER_MENU_FILE_TRANS);
//#sijapp cond.if target isnot "MOTOROLA"#
            tlContactMenu.addMenuItem("ft_cam", ContactList.menuIcons.elementAt(23), USER_MENU_CAM_TRANS);
//#sijapp cond.end#
//#sijapp cond.end#
            if (isbest) {
                tlContactMenu.addMenuItem("send_scheme", ContactList.menuIcons.elementAt(32), USER_MENU_SCHEME_TRANS);
            }
        }
        if ((status != ContactItem.STATUS_ONLINE) && (status != ContactItem.STATUS_OFFLINE) && (status != ContactItem.STATUS_INVISIBLE)) {
            tlContactMenu.addMenuItem("reqstatmsg", ContactList.imageList.elementAt(contact.getImageIndex()), USER_MENU_STATUS_MESSAGE);
        }
        if (contact.getXStatus().getStatusIndex() != -1 || contact.hasMood()) {
            tlContactMenu.addMenuItem("xtraz_msg", contact.getXStatusImage(), USER_MENU_XTRAZ_MESSAGE);
        }
        tlContactMenu.addMenuItem("info", ContactList.menuIcons.elementAt(9), USER_MENU_USER_INFO);
        tlContactMenu.addMenuItem("dc_info", ContactList.menuIcons.elementAt(10), USER_MENU_LOCAL_INFO);
        tlContactMenu.addMenuItem("copy_uin", ContactList.menuIcons.elementAt(19), USER_MENU_COPY_UIN);
//#sijapp cond.if modules_HISTORY is "true" #
        tlContactMenu.addMenuItem("history_lng", ContactList.menuIcons.elementAt(7), USER_MENU_HISTORY);
//#sijapp cond.end#
        //tlContactMenu.addMenuItem("extra_menu", ContactList.menuIcons.elementAt(35), USER_MENU_EXTRA);
        tlContactMenu.addMenuItem("options_lng", ContactList.menuIcons.elementAt(35), USER_MENU_EXTRA);
        if (isConnected && (contact.getProfile().getGroupItems().length > 1) && (!contact.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH)) && (!contact.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP))) {
            tlContactMenu.addMenuItem("move_to_group", ContactList.menuIcons.elementAt(1), USER_MENU_MOVE);
        }
        if (isConnected) {
            tlContactMenu.addMenuItem("server_lists", ContactList.menuIcons.elementAt(31), USER_MENU_LIST_OPERATION);
            tlContactMenu.addMenuItem("remove_me", ContactList.menuIcons.elementAt(30), USER_MENU_REMOVE_ME);
        }
        tlContactMenu.addMenuItem("rename", ContactList.menuIcons.elementAt(11), USER_MENU_RENAME);
        if (isConnected) {
            tlContactMenu.addMenuItem("remove", ContactList.menuIcons.elementAt(6), USER_MENU_USER_REMOVE);
        }
        tlContactMenu.setMenuListener(jimmui);
    }

    private static void showListsOperation() {
        SelectBase base = new SelectBase(clciContactMenu);
        Jimm.setDisplay(base);
    }

    public void menuSelect(Menu menu, byte action) {
        if (!(Jimm.getPrevScreen() instanceof Form) && Jimm.getPrevScreen() != null) {
            Jimm.setDisplay(menu);
        }
        switch (action) {
            case USER_MENU_MESSAGE:
                writeMessage(clciContactMenu, null).activate();
                break;

            case USER_MENU_REQU_AUTH:
                JimmUI.authMessage(JimmUI.AUTH_TYPE_REQ_AUTH, clciContactMenu, "requauth", "plsauthme"/* + Options.getString(Options.OPTION_UIN)*/);
                break;

            case USER_MENU_STATUS_MESSAGE:
                if (clciContactMenu.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE) {
                    requestStatusMess(clciContactMenu);
                }
                menu.back();
                break;

            case USER_MENU_CHECK_STATUS:
                clciContactMenu.checkStatus();
                break;

            case USER_MENU_XTRAZ_MESSAGE:
                clciContactMenu.requestXStatusText();
                menu.back();
                break;

            case USER_MENU_EXTRA:
                new CIForm(clciContactMenu);
                break;

//#sijapp cond.if modules_FILES is "true"#
            case USER_MENU_FILE_TRANS: {
                FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME, clciContactMenu);
                ft.startFT();
            }
            break;

//#sijapp cond.if target isnot "MOTOROLA" #
            case USER_MENU_CAM_TRANS: {
                FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_CAMERA_SNAPSHOT, clciContactMenu);
                ft.startFT();
            }
            break;
//#sijapp cond.end#
//#sijapp cond.end#
            case USER_MENU_SCHEME_TRANS:
                PlainMessage plainMsg = new PlainMessage(clciContactMenu.getIcq().getUin(), clciContactMenu,
                        Message.MESSAGE_TYPE_NORM, DateAndTime.createCurrentDate(false), ChatTextList.colorSchemeSend());
                SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
                try {
                    clciContactMenu.getIcq().requestAction(sendMsgAct);
                } catch (Exception ignored) {
                }
                menu.back();
                break;

            case USER_MENU_COPY_UIN:
                JimmUI.setClipBoardText(clciContactMenu.getUinString());
                menu.back();
                break;

            case USER_MENU_MOVE:
                groupIds = JimmUI.showGroupSelector
                        (
                                clciContactMenu.getProfile(),
                                this,
                                JimmUI.SHS_TYPE_ALL,
                                clciContactMenu.getIntValue(ContactItem.CONTACTITEM_GROUP)
                        );
                break;

            case USER_MENU_USER_REMOVE:
                Jimm.setDisplay(new Select(ResourceBundle.getString("remove"),
                        ResourceBundle.getString("remove") + " " + clciContactMenu.name + "?",
                        jimmui, DELETE_CONTACT, clciContactMenu));
                break;

            case USER_MENU_REMOVE_ME:
                Jimm.setDisplay(new Select(ResourceBundle.getString("remove_me"),
                        ResourceBundle.getString("remove_me_from") + clciContactMenu.name + "?",
                        jimmui, DELETE_ME, clciContactMenu));
                break;

            case USER_MENU_RENAME:
                new InputTextBox(InputTextBox.EDITOR_MODE_RENAME, ResourceBundle.getString("rename"), clciContactMenu.name).activate();
                break;

            case USER_MENU_USER_INFO:
                (new UserInfo()).requiestUserInfo(clciContactMenu.getProfile(), clciContactMenu.getUinString(), clciContactMenu.name);
                break;

            case USER_MENU_LOCAL_INFO:
                (new ClientInfo()).showClientInfo(clciContactMenu);
                break;

//#sijapp cond.if modules_HISTORY is "true" #
            case USER_MENU_HISTORY:
                HistoryStorage.showHistoryList(clciContactMenu);
                break;
//#sijapp cond.end#

            case USER_MENU_LIST_OPERATION:
                showListsOperation();
                break;

            case USER_MENU_ADD:
                Search search = new Search(true, clciContactMenu.getIcq());
                String data[] = new String[Search.LAST_INDEX];
                data[Search.UIN] = clciContactMenu.getUinString();
                SearchAction act = new SearchAction(search, data, SearchAction.CALLED_BY_ADDUSER);
                try {
                    clciContactMenu.getIcq().requestAction(act);
                } catch (JimmException e) {
                    JimmException.handleException(e);
                }
                clciContactMenu.getProfile().addAction("wait", act);
                break;

            case FUNCTION_MENU_CONTACT:
                Menu menu2 = new Menu(Jimm.getContactList(), (byte) 0);
                fillContactMenu(clciContactMenu, menu2);
                Jimm.setDisplay(menu2);
                break;

            case FUNCTION_MENU_GROUP:
                Menu menu3 = new Menu(Jimm.getContactList(), (byte) 0);
                fillGroupMenu(menu3);
                Jimm.setDisplay(menu3);
                break;

            case FUNCTION_MENU_FIND_CONTACT:
                Search searchf = new Search(false, Jimm.getIcqRef());
                searchf.getSearchForm().activate(Search.SearchForm.ACTIV_JUST_SHOW);
                break;

            case FUNCTION_MENU_ADD_CONTACT: {
                final Icq icq = Jimm.getIcqRef();
                if (icq.getProfile().getGroupItems().length == 0) {
                    JimmException.handleException(new JimmException(161, 0, true));
                    //Alert errorMsg = new Alert(ResourceBundle.getString("warning"), JimmException.getErrDesc(161, 0), null, AlertType.WARNING);
                    //errorMsg.setTimeout(Alert.FOREVER);
                    //Jimm.setDisplay(errorMsg);
                    return;
                }
                FormEx form = new FormEx(ResourceBundle.getString("add_contact"), JimmUI.cmdOk, JimmUI.cmdBack);
                final TextField uin = new TextField(ResourceBundle.getString("uin"), null, 12, TextField.NUMERIC);
                final TextField name = new TextField(ResourceBundle.getString("name"), null, 32, TextField.ANY);
                StringBuffer sb = new StringBuffer();
                GroupItem[] grs = icq.getProfile().getGroupItems();
                int count = grs.length;
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("|");
                    }
                    sb.append(grs[i].getName());
                }
                String[] names = Util.explode(sb.toString(), '|');
                final LineChoise groupes = new LineChoise(ResourceBundle.getString("whichgroup"), names);
                form.setCommandListener(new CommandListener() {
                    public void commandAction(Command c, Displayable d) {
                        if (c == JimmUI.cmdOk) {
                            try {
                                ContactItem cItem =
                                        new ContactItem(-1,
                                                icq.getProfile().getGroupItems()[groupes.getSelected()].getId(),
                                                uin.getString(),
                                                name.getString(),
                                                true,
                                                false,
                                                icq.getProfile()
                                        );
                                cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, true);
                                cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, ContactItem.STATUS_OFFLINE);
                                icq.addToContactList(cItem);
                            } catch (Exception ignored) {
                            }
                        } else {
                            Jimm.getContactList().activate();
                        }
                    }
                });
                form.append(groupes);
                form.append(uin);
                form.append(name);
                Jimm.setDisplay(form);
                break;
            }

            case FUNCTION_MENU_ADD_GROUP: {
                final Icq icq = Jimm.getIcqRef();
                FormEx gForm = new FormEx(ResourceBundle.getString("add_group"), cmdOk, cmdCancel);
                final TextField gTextField = new TextField(ResourceBundle.getString("group_name"), null, 20, TextField.ANY);
                gForm.append(gTextField);

                gForm.setCommandListener(new CommandListener() {
                    public void commandAction(Command c, Displayable d) {
                        if (c == JimmUI.cmdOk) {
                            GroupItem newGroup = new GroupItem(gTextField.getString(), icq);
                            Action act = new UpdateContactListAction(newGroup, UpdateContactListAction.ACTION_ADD);
                            try {
                                icq.requestAction(act);
                            } catch (JimmException e) {
                                JimmException.handleException(e);
                                if (e.isCritical()) {
                                    return;
                                }
                            }
                            Jimm.getContactList().activate();
                            icq.getProfile().addAction("wait", act);
                            return;
                        }
                        Jimm.getContactList().activate();
                    }
                });
                Jimm.setDisplay(gForm);
                break;
            }

            case FUNCTION_MENU_MYSELF:
                try {
                    Profile profile = Jimm.getCurrentProfile();
                    (new jimm.info.UserInfo()).requiestUserInfo(profile, profile.getUin(), "");
                } catch (Exception ignored) {
                }
                break;

            case FUNCTION_MENU_ANTISPAM: {
                FormEx options = new FormEx(ResourceBundle.getString("antispam"), JimmUI.cmdOk, JimmUI.cmdBack);
                final TextField antispamMsgTextField = new TextField(ResourceBundle.getString("antispam_msg"), Options.getString(Options.OPTION_ANTISPAM_MSG), 255, TextField.ANY);
                final TextField antispamAnswerTextField = new TextField(ResourceBundle.getString("antispam_answer"), Options.getString(Options.OPTION_ANTISPAM_ANSWER), 255, TextField.ANY);
                final TextField antispamHelloTextField = new TextField(ResourceBundle.getString("antispam_hello"), Options.getString(Options.OPTION_ANTISPAM_HELLO), 255, TextField.ANY);
                final LineChoiseBoolean on = OptionsForm.createLineBoo("antispam_enable", Options.OPTION_ANTISPAM_ENABLE);
                final LineChoiseBoolean http = OptionsForm.createLineBoo("antispam_url", Options.OPTION_ANTISPAM_URL);

                options.setCommandListener(new CommandListener() {
                    public void commandAction(Command c, Displayable d) {
                        //if (c == JimmUI.cmdOk) {
                        Options.setString(Options.OPTION_ANTISPAM_MSG, antispamMsgTextField.getString());
                        Options.setString(Options.OPTION_ANTISPAM_ANSWER, antispamAnswerTextField.getString());
                        Options.setString(Options.OPTION_ANTISPAM_HELLO, antispamHelloTextField.getString());
                        Options.setBoolean(Options.OPTION_ANTISPAM_ENABLE, on.getBooolean());
                        Options.setBoolean(Options.OPTION_ANTISPAM_URL, http.getBooolean());
                        Options.safe_save();
                        //}
                        Jimm.getContactList().activate();
                    }
                });
                options.append(antispamMsgTextField);
                options.append(antispamAnswerTextField);
                options.append(antispamHelloTextField);
                options.append(on);
                options.append(http);
                Jimm.setDisplay(options);
                break;
            }

            case FUNCTION_MENU_PROFILE_OPTIONS: {
                FormEx profile = new FormEx(ResourceBundle.getString("pw_curpro"), JimmUI.cmdOk, JimmUI.cmdBack);
                profile.removeCommand(JimmUI.cmdOk);
                final Icq icq = Jimm.getIcqRef();
                //final TextField uin = new TextField(ResourceBundle.getString("UIN"), Jimm.getCurrentProfile().getUin(), 12, TextField.NUMERIC);
                final TextField uin = new TextField(ResourceBundle.getString("UIN"), Jimm.getCurrentProfile().getUin(), 32, TextField.ANY);
                final TextField pass = new TextField(ResourceBundle.getString("password"), Jimm.getCurrentProfile().getPassword(), 11, TextField.PASSWORD);
                final LineChoiseBoolean report = OptionsForm.createLineBoo("delivery_report", Profile.OPTION_DELIVERY_REPORT);
                final LineChoiseBoolean xtraz = OptionsForm.createLineBoo("xTraz_enable_plus", Profile.OPTION_XTRAZ_ENABLE);
                final LineChoiseBoolean notyfy_print = OptionsForm.createLineBoo("dis_out_notif", Profile.OPTION_MESS_NOTIF_TYPE);
                final LineChoiseBoolean webAware = OptionsForm.createLineBoo("web_aware", Profile.OPTION_WEBAWARE);
                final LineChoiseBoolean auth = OptionsForm.createLineBoo("req_auth", Profile.OPTION_REQ_AUTH);
                //#sijapp cond.if modules_MAGIC_EYE is "true"#
                final LineChoiseBoolean mm = OptionsForm.createLineBoo("magic_eye", Profile.OPTION_ENABLE_MM);
                //#sijapp cond.end#
                final LineChoise clientIdLine = OptionsForm.createLine("client_id", ClientID.getClientString(), icq.getProfile().getInt(Profile.OPTION_CLIENT_ID), false, true);
                clientIdLine.setSelected(icq.getProfile().getInt(Profile.OPTION_CLIENT_ID));
                //clientIdLine = createLine("client_id", getClientString(), Profile.OPTION_CLIENT_ID, false);
                //clientStringVersion = new TextField(ResourceBundle.getString("jimm_version"), icq.getProfile().getString(Profile.OPTION_STRING_VERSION), 11, TextField.ANY);

                profile.setCommandListener(new CommandListener() {
                    public void commandAction(Command c, Displayable d) {
                        //if (c == JimmUI.cmdOk) {
                        if (uin.getString() != null && uin.getString().length() > 0) {
                            int acc = Jimm.getCurrentProfile().getIndex();
                            StringBuffer sb1 = new StringBuffer();
                            StringBuffer sb2 = new StringBuffer();
                            for (int i = 0; i < Options.nicks.size(); i++) {
                                if (sb1.length() > 0) {
                                    sb1.append('\t');
                                    sb2.append('\t');
                                }
                                if (i == acc) {
                                    sb1.append(uin.getString());
                                    sb2.append(pass.getString());
                                } else {
                                    sb1.append((String) Options.uins.elementAt(i));
                                    sb2.append((String) Options.passwords.elementAt(i));
                                }
                            }
                            Options.setString(Options.OPTION_UIN, sb1.toString());
                            Options.setString(Options.OPTION_PASSWORD, sb2.toString());
                            sb1 = null;
                            sb2 = null;
                            Options.safe_save();
                            Options.initAccounts();
                        }
                        int needToUpdate = 0;
                        if (report.getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_DELIVERY_REPORT)) {
                            needToUpdate |= 1;
                        }
                        icq.getProfile().setBoolean(Profile.OPTION_DELIVERY_REPORT, report.getBooolean());
                        if (xtraz.getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
                            needToUpdate |= 1;
                        }
                        icq.getProfile().setBoolean(Profile.OPTION_XTRAZ_ENABLE, xtraz.getBooolean());
                        if (notyfy_print.getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_MESS_NOTIF_TYPE)) {
                            needToUpdate |= 1;
                        }
                        icq.getProfile().setBoolean(Profile.OPTION_MESS_NOTIF_TYPE, notyfy_print.getBooolean());
                        if (webAware.getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_WEBAWARE)) {
                            needToUpdate |= 1;
                            needToUpdate |= 1 << 8;
                        }
                        icq.getProfile().setBoolean(Profile.OPTION_WEBAWARE, webAware.getBooolean());
                        if (auth.getBooolean() != icq.getProfile().getBoolean(Profile.OPTION_REQ_AUTH)) {
                            needToUpdate |= 1 << 8;
                        }
                        icq.getProfile().setBoolean(Profile.OPTION_REQ_AUTH, auth.getBooolean());
//#sijapp cond.if modules_MAGIC_EYE is "true"#
                        icq.getProfile().setBoolean(Profile.OPTION_ENABLE_MM, mm.getBooolean());
//#sijapp cond.end#
                        if (clientIdLine.getSelected() != icq.getProfile().getInt(Profile.OPTION_CLIENT_ID)) {
                            needToUpdate |= 1;
                        }
                        icq.getProfile().setInt(Profile.OPTION_CLIENT_ID, clientIdLine.getSelected());
//                            if (clientIdLine.getSelected() != icq.getProfile().getInt(Profile.OPTION_CLIENT_ID)) {
//                                needToUpdate |= 1;
//                            }
//                            icq.getProfile().setInt(Profile.OPTION_CLIENT_ID, clientIdLine.getSelected());
//                            if (!clientStringVersion.getString().equals(icq.getProfile().getString(Profile.OPTION_STRING_VERSION))) {
//                                needToUpdate |= 1;
//                            }
//                            icq.getProfile().setString(Profile.OPTION_STRING_VERSION, clientStringVersion.getString());
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
                        }
                        icq.getProfile().saveOptions();
                        //}
                        Jimm.getContactList().activate();
                    }
                });
                profile.append(uin);
                profile.append(pass);
                profile.append(report);
                profile.append(xtraz);
                profile.append(notyfy_print);
                profile.append(webAware);
                profile.append(auth);
                //#sijapp cond.if modules_MAGIC_EYE is "true"#
                profile.append(mm);
                //#sijapp cond.end#
                profile.append(clientIdLine);
                Jimm.setDisplay(profile);
                break;
            }
//#sijapp cond.if modules_MAGIC_EYE is "true"#
            case FUNCTION_MENU_MAGIC_EYE:
                if (Jimm.getCurrentProfile().getBoolean(Profile.OPTION_ENABLE_MM)) {
                    Jimm.getCurrentProfile().getMagicEye().activateEx(Jimm.getContactList());
                }
                break;
//#sijapp cond.end#
        }
    }

    private static final byte FUNCTION_MENU_CONTACT = (byte) 100;
    private static final byte FUNCTION_MENU_GROUP = (byte) 101;
    private static final byte FUNCTION_MENU_FIND_CONTACT = (byte) 102;
    private static final byte FUNCTION_MENU_ADD_CONTACT = (byte) 103;
    private static final byte FUNCTION_MENU_ADD_GROUP = (byte) 104;
    private static final byte FUNCTION_MENU_MYSELF = (byte) 105;
    private static final byte FUNCTION_MENU_ANTISPAM = (byte) 106;
    public static final byte FUNCTION_MENU_PROFILE_OPTIONS = (byte) 107;
    private static final byte FUNCTION_MENU_MAGIC_EYE = (byte) 108;

    public static void fillFunctionMenu(Object contact, Menu tlContactMenu) {
        if (contact instanceof ContactItem) {
            clciContactMenu = (ContactItem) contact;
            tlContactMenu.addMenuItem("user_menu", ContactList.menuIcons.elementAt(10), FUNCTION_MENU_CONTACT);
        } else if (contact instanceof GroupItem) {
            clciGroupMenu = ((GroupItem) contact).getId();
            tlContactMenu.addMenuItem("group_menu", ContactList.menuIcons.elementAt(10), FUNCTION_MENU_GROUP);
        }
        tlContactMenu.addMenuItem("search_user", ContactList.menuIcons.elementAt(2), FUNCTION_MENU_FIND_CONTACT);
        tlContactMenu.addMenuItem("add_contact", ContactList.menuIcons.elementAt(29), FUNCTION_MENU_ADD_CONTACT);
        tlContactMenu.addMenuItem("add_group", ContactList.menuIcons.elementAt(29), FUNCTION_MENU_ADD_GROUP);
        tlContactMenu.addMenuItem("myself", ContactList.menuIcons.elementAt(9), FUNCTION_MENU_MYSELF);
        tlContactMenu.addMenuItem("antispam", ContactList.menuIcons.elementAt(0), FUNCTION_MENU_ANTISPAM);
        tlContactMenu.addMenuItem("options_lng", ContactList.imageList.elementAt(7), FUNCTION_MENU_PROFILE_OPTIONS);
//#sijapp cond.if modules_MAGIC_EYE is "true"#
        tlContactMenu.addMenuItem("magic_eye", ContactList.menuIcons.elementAt(33), FUNCTION_MENU_MAGIC_EYE);
//#sijapp cond.end#        
        tlContactMenu.setMenuListener(jimmui);
    }

    public static void fillGroupMenu(Menu tlContactMenu) {
        tlContactMenu.addMenuItem("rename", ContactList.menuIcons.elementAt(11), (byte) 1);
        if (Jimm.getIcqRef().isConnected()) {
            tlContactMenu.addMenuItem("clear", ContactList.menuIcons.elementAt(6), (byte) 2);
            tlContactMenu.addMenuItem("remove", ContactList.menuIcons.elementAt(6), (byte) 3);
        }
        tlContactMenu.setMenuListener(new MenuListener() {
            public void menuSelect(Menu menu, byte action) {
                switch (action) {
                    case 1:
                        TextBox tb = new TextBox(ResourceBundle.getString("rename"), "", 32, TextField.ANY);
                        tb.addCommand(JimmUI.cmdBack);
                        tb.addCommand(JimmUI.cmdOk);
                        tb.setCommandListener(new CommandListener() {
                            public void commandAction(Command c, Displayable d) {
                                if (c.equals(cmdOk)) {
                                    TextBox textBox = (TextBox) d;
                                    String text = textBox.getString();
                                    if (text.length() > 0) {
                                        GroupItem group = Jimm.getCurrentProfile().getGroupById(clciGroupMenu);
                                        group.setName(text);
                                        Jimm.getIcqRef().getProfile().safeSave();
                                        UpdateContactListAction act = new UpdateContactListAction(group, UpdateContactListAction.ACTION_RENAME);
                                        try {
                                            Jimm.getIcqRef().requestAction(act);
                                            Jimm.getIcqRef().getProfile().addAction("wait", act);
                                        } catch (JimmException e) {
                                            JimmException.handleException(e);
                                        }
                                    }
                                }
                                Jimm.getContactList().activate();
                            }
                        });
                        Jimm.setDisplay(tb);
                        return;

                    case 2:
                        ContactItem[] contactItems = Jimm.getCurrentProfile().getItemsFromGroup(clciGroupMenu);
//                        Icq icq = Jimm.getIcqRef();
//                        try {                    // todo
//                            icq.sendPacket(new SnacPacket(SnacPacket.CLI_ADDSTART_FAMILY, SnacPacket.CLI_ADDSTART_COMMAND, SnacPacket.CLI_ADDSTART_COMMAND, new byte[0], new byte[0]));
//                        } catch (JimmException e) {
//                        }
                        Jimm.getContactList().lock();
                        for (int i = 0; i < contactItems.length; i++) {
                            Jimm.getContactList().removeContact(contactItems[i]);
                        }
                        Jimm.getContactList().unlock();
//                        try {
//                            icq.sendPacket(new SnacPacket(SnacPacket.CLI_ADDEND_COMMAND, SnacPacket.CLI_ADDEND_COMMAND, SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]));
//                        } catch (JimmException e) {
//                        }
                        break;

                    case 3:
                        Jimm.setDisplay(new Select(ResourceBundle.getString("remove"),
                                ResourceBundle.getString("remove") + " " + Jimm.getCurrentProfile().getGroupById(clciGroupMenu).getName() + "?",
                                new SelectListener() {
                                    public void selectAction(int action, int selectType, Object o) {
                                        UpdateContactListAction deleteGroupAct = new UpdateContactListAction(Jimm.getCurrentProfile().getGroupById(clciGroupMenu), UpdateContactListAction.ACTION_DEL);
                                        try {
                                            Jimm.getIcqRef().requestAction(deleteGroupAct);
                                            Jimm.getIcqRef().getProfile().addAction("wait", deleteGroupAct);
                                        } catch (JimmException e) {
                                            JimmException.handleException(e);
                                        }
                                    }
                                }, -1, null));
                        return;
                }
                menu.back();
            }
        });
    }

    public static void requestStatusMess(ContactItem item) {
        if (item == null) {
            return;
        }
        long status = item.getIntValue(ContactItem.CONTACTITEM_STATUS);
        if (!(status == ContactItem.STATUS_INVISIBLE)) {
            int msgType = Message.MESSAGE_TYPE_AWAY;
            if (status == ContactItem.STATUS_OCCUPIED) {
                msgType = Message.MESSAGE_TYPE_OCC;
            } else if (status == ContactItem.STATUS_DND) {
                msgType = Message.MESSAGE_TYPE_DND;
            } else if (status == ContactItem.STATUS_CHAT) {
                msgType = Message.MESSAGE_TYPE_FFC;
            } else if (status == ContactItem.STATUS_NA) {
                msgType = Message.MESSAGE_TYPE_NA;
            } else if (status == ContactItem.STATUS_EVIL) {
                msgType = Message.MESSAGE_TYPE_EVIL;
            } else if (status == ContactItem.STATUS_DEPRESSION) {
                msgType = Message.MESSAGE_TYPE_DEPRESSION;
            } else if (status == ContactItem.STATUS_HOME) {
                msgType = Message.MESSAGE_TYPE_HOME;
            } else if (status == ContactItem.STATUS_WORK) {
                msgType = Message.MESSAGE_TYPE_WORK;
            } else if (status == ContactItem.STATUS_LUNCH) {
                msgType = Message.MESSAGE_TYPE_LUNCH;
            }
            item.readStatusMess = true;
            PlainMessage awayReq = new PlainMessage(item.getIcq().getUin(), item, msgType, DateAndTime.createCurrentDate(false), "");
            SendMessageAction act = new SendMessageAction(awayReq);
            try {
                item.getIcq().requestAction(act);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    //return;
                }
            }
        }
    }

    /****************************Contact Menu end*****************************/

    /**
     * **************************Delete Contact*****************************
     */

    private final static byte DELETE_CONTACT = (byte) 0;
    private final static byte DELETE_ME = (byte) 1;

    public void selectAction(int action, int selectType, Object o) {
        if (groupIds != null) {
            GroupItem old = clciContactMenu.getProfile().getGroupById(clciContactMenu.getIntValue(ContactItem.CONTACTITEM_GROUP));
            GroupItem nEw = clciContactMenu.getProfile().getGroupById(groupIds[selectType]);
            UpdateContactListAction uact = new UpdateContactListAction(clciContactMenu, old, nEw, UpdateContactListAction.ACTION_MOVE);
            try {
                clciContactMenu.getIcq().requestAction(uact);
                clciContactMenu.getProfile().addAction("wait", uact);
            } catch (JimmException e) {
                JimmException.handleException(e);
            }
            groupIds = null;
            return;
        }
        switch (action) {
            case DELETE_CONTACT:
                Jimm.getContactList().removeContact((ContactItem) o);
                break;

            case DELETE_ME:
                clciContactMenu = (ContactItem) o;
                menuRemoveMeSelected();
                break;
        }
    }

    private static void menuRemoveMeSelected() {
        RemoveMeAction remAct = new RemoveMeAction(clciContactMenu.getUinString());
        try {
            clciContactMenu.getIcq().requestAction(remAct);
        } catch (JimmException e) {
            JimmException.handleException(e);
            if (e.isCritical()) {
                return;
            }
        }
        Jimm.getContactList().activate();
    }

    public static void menuRenameSelected(String newName) {
        Jimm.getContactList().activate();
        if ((newName == null) || (newName.length() < 0)) {
            return;
        }
        clciContactMenu.rename(newName);
    }

    public static void setLights(int light) {
        boolean control = false;
        try {
            control = Class.forName("com.nokia.mid.ui.DeviceControl") != null;
        } catch (ClassNotFoundException ignored) {
        }
        if (control) {
            com.nokia.mid.ui.DeviceControl.setLights(0x00, light);
        }
    }

}
