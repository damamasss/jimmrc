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
 File: src/jimm/ChatTextList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Artyomov Denis, Dmitry Tunin
 *******************************************************************************/

package jimm.chat;

import DrawControls.*;
import jimm.*;
import jimm.comm.*;
import jimm.ui.*;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class ChatTextList extends TextList implements VirtualListCommands, CommandListener, MenuListener {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"# ===>
    //private static ChatItem chatItem;
    private static ChatMenu chatMenu;
    public static TextFieldEx textLine;
    public boolean lineTyping;

    //private Ticker currTicker;
    private String currMessage = "";
//#sijapp cond.end# <===

//#sijapp cond.if modules_SBOLTUN is "true"#
    public static sBoltun sBoltun;
//#sijapp cond.end#

    // UI modes
    final public static int UI_MODE_NONE = 0;
    final public static int UI_MODE_DEL_CHAT = 1;

    private final static byte MENU_SMILES = (byte) 100;
    private final static byte MENU_TEMPLARES = (byte) 101;
    private final static byte MENU_GRANTAUTH = (byte) 102;
    private final static byte MENU_DENYAUTH = (byte) 103;
    private final static byte MENU_QOUTE = (byte) 104;
    private final static byte MENU_PASTE = (byte) 105;
    private final static byte MENU_FILE_TRANS = (byte) 106;
    private final static byte MENU_INFO = (byte) 107;
    private final static byte MENU_DCINFO = (byte) 108;
    private final static byte MENU_XSTATUS = (byte) 109;
    private final static byte MENU_STATUS = (byte) 110;
    private final static byte MENU_COPYTEXT = (byte) 111;
    private final static byte MENU_ADDTOCOPIED = (byte) 112;
    private final static byte MENU_HISTORY = (byte) 113;
    private final static byte MENU_APPLYSCHEME = (byte) 114;
    private final static byte MENU_GOTOURL = (byte) 115;
    private final static byte MENU_DELETECHAT = (byte) 116;
    private final static byte MENU_TO_TEMPLARE = (byte) 117;

    private final static byte LINE_ALTWIN = (byte) -1;
    private final static byte LINE_CANCEL = (byte) -2;
    private final static byte LINE_CLEAR = (byte) -3;
    private final static byte LINE_DETRANS = (byte) -4;
    private final static byte LINE_EMO = (byte) -5;
    private final static byte LINE_FASTYPE = (byte) -6;
    private final static byte LINE_MASSEND = (byte) -7;
    private final static byte LINE_PASTE = (byte) -8;
    private final static byte LINE_QOUTE = (byte) -9;
    private final static byte LINE_SEND = (byte) -10;
    private final static byte LINE_TEMPL = (byte) -11;
    private final static byte LINE_TRANS = (byte) -12;

    private final static byte FAT_ONOFF = -33;
    private final static byte FAT_ADD_WORD = -34;
    private final static byte FAT_EXPORT = -35;
    private final static byte FAT_DELETE_WORD = -36;

    /* Message reply command */
    static Command cmdCloseChat;
    static Vector messTemplare = new Vector();
    public String ChatName;
    private ContactItem contact;
    private Vector messData = new Vector();
    private int messTotalCounter = 0;

    public ChatTextList(String name, ContactItem contact) {
        super(name, false);
        if (cmdCloseChat == null) {
            if (NativeCanvas.getWidthEx() <= 128) {
                cmdCloseChat = JimmUI.cmdBack;
            } else {
                cmdCloseChat = new Command(ResourceBundle.getString("close"), Jimm.is_smart_SE() ? Command.CANCEL : Command.BACK, 2);
            }
        }
        ChatName = name;
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        //_this = this;
        //if (Options.getInt(Options.OPTION_CLASSIC_CHAT) == 1) {
        //    initChatItem();
        //} else
        if (Options.getInt(Options.OPTION_CLASSIC_CHAT) == 1) {
            initTextLine();
        }
//#sijapp cond.end#

        setMode(MODE_TEXT);
        switch (Options.getInt(Options.OPTION_FONT_SIZE_CHAT)) {
            case 0:
                setFontSize(SMALL_FONT);
                break;
            case 1:
                setFontSize(MEDIUM_FONT);
                break;
            case 2:
                setFontSize(LARGE_FONT);
                break;
        }

        setContact(contact);
        setColorScheme();
        setVLCommands(this);
        buildMenu();
    }

    //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//    private void initChatItem() {
//        if (chatItem == null) {
//            textLine = null;
//            lineTyping = false;
//
//            chatItem = new ChatItem(ChatName);
//            chatItem.addCommand(JimmUI.cmdMenu);
//            chatItem.addCommand(cmdCloseChat);
//        }
//        setForcedSize(chatItem.getMinContentWidth(), chatItem.getMinContentHeight());
//    }

    private void initTextLine() {
        if (textLine == null) {
            //chatItem = null;
            textLine = new TextFieldEx(this);
        }
        //setForcedSize(-1, getHeight() - textLine.getHeight(lineTyping) - getMenuBarHeight());
        setForcedSize(-1, getHeight() - textLine.getHeight(lineTyping) - (textLine.vike == null ? getMenuBarHeight() : textLine.vike.height));
    }

    public void saveCurrMessage(String message) {
        currMessage = message;
    }

    static void destroyTypingItems() {
        //chatItem = null;
        textLine = null;
    }
//#sijapp cond.end#

    public ContactItem getContact() {
        return contact;
    }

    public void setContact(ContactItem contact) {
        this.contact = contact;
    }

    public void showMenu() {
        Menu menu = new Menu(this);
//        menu.addMenuItem("user_menu", MENU_CONTACTMENU);
//        if (contact.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH)) {
//            menu.addMenuItem("requauth", MENU_REQAUTH);
//        }
//        if (contact.isMessageAvailable(ContactItem.MESSAGE_AUTH_REQUEST)) {
//            menu.addMenuItem("grant", MENU_GRANTAUTH);
//            menu.addMenuItem("deny", MENU_DENYAUTH);
//        }
//        menu.addMenuItem("copy_text", MENU_COPYTEXT);
//        //menu.addMenuItem("copy_all_text", MENU_COPYALLTEXT);
//        menu.addMenuItem("add_to_copied_text", MENU_ADDTOCOPIED);
//        if (!JimmUI.clipBoardIsEmpty()) {
//            menu.addMenuItem("paste", MENU_PASTE);
//            menu.addMenuItem("quote", MENU_QOUTE);
//        }
//        if ((contact.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) || contact.getBooleanValue(ContactItem.CONTACTITEM_PHANTOM)) &&
//                !contact.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH)) {
//            menu.addMenuItem("add_to_list", MENU_ADDUSR);
//        }
//        int index = getCurrTextIndex();
//        MessData md;
////#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
//        if (index != -1) {
//            md = (MessData) getMessData().elementAt(index);
//            String str = getCurrText(md.getOffset(), false);
//            if (isScheme(str)) {
//                menu.addMenuItem("apply_scheme", MENU_APPLYSCHEME);
//            }
////            if (Util.getNumber(str).length() > 4) {
////                menu.addMenuItem("copy_uin", MENU_COPYNUM);
////            }
////            if (Util.getNumber(str).length() > 4) {
////                menu.addMenuItem("make_call", MENU_CALLNUM);
////            }
//        }
////#sijapp cond.end#
////#sijapp cond.if modules_HISTORY is "true" #
//        boolean fl = !(Options.getBoolean(Options.OPTION_HISTORY) || contact.getExtraValue(ContactItem.EXTRA_HIST));
//        if (index != -1) {
//            md = (MessData) getMessData().elementAt(index);
//            fl |= (!md.isURL() && md.getTime() == 0);
//        }
//        if (fl) {
//            menu.addMenuItem("add_to_history", MENU_ADDTOHISTORY);
//        }
////#sijapp cond.end#
////#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
//        if (index != -1) {
//            md = (MessData) getMessData().elementAt(index);
//            if (md.isURL()) {
//                menu.addMenuItem("goto_url", MENU_GOTOURL);
//            }
//        }
////#sijapp cond.end#
//        menu.addMenuItem("delete_chat", MENU_DELETECHAT);
//        menu.setMenuListener(this);

//#sijapp cond.if modules_SMILES is "true" #
        menu.addMenuItem("insert_emotion", MENU_SMILES);
//#sijapp cond.end#
        menu.addMenuItem("message_templare", MENU_TEMPLARES);
        menu.addMenuItem("to_message_templare", MENU_TO_TEMPLARE);
//#sijapp cond.if modules_FILES is "true"#
        if (contact.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE) {
            menu.addMenuItem("ft_name", MENU_FILE_TRANS);
        }
//#sijapp cond.end#
        if (contact.isMessageAvailable(ContactItem.MESSAGE_AUTH_REQUEST)) {
            menu.addMenuItem("grant", MENU_GRANTAUTH);
            menu.addMenuItem("deny", MENU_DENYAUTH);
        }
        if (!JimmUI.clipBoardIsEmpty()) {
            menu.addMenuItem("paste", MENU_PASTE);
            menu.addMenuItem("quote", MENU_QOUTE);
        }
        menu.addMenuItem("info", MENU_INFO);
        menu.addMenuItem("dc_info", MENU_DCINFO);
        if (contact.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE) {
            menu.addMenuItem("reqstatmsg", MENU_STATUS);
        }
        if (contact.getXStatus().getStatusIndex() != -1 || contact.hasMood()) {
            menu.addMenuItem("xtraz_msg", MENU_XSTATUS);
        }
        menu.addMenuItem("copy_text", MENU_COPYTEXT);
        menu.addMenuItem("add_to_copied_text", MENU_ADDTOCOPIED);
//#sijapp cond.if modules_HISTORY is "true" #
        menu.addMenuItem("history_lng", MENU_HISTORY);
//#sijapp cond.end#
//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
        int index = getCurrTextIndex();
        MessData md;
        if (index != -1) {
            md = (MessData) getMessData().elementAt(index);
            if (md.isURL()) {
                menu.addMenuItem("goto_url", MENU_GOTOURL);
            }
            String str = getCurrText(md.getOffset(), false);
            if (isScheme(str)) {
                menu.addMenuItem("apply_scheme", MENU_APPLYSCHEME);
            }
        }
//#sijapp cond.end#
        menu.addMenuItem("delete_chat", MENU_DELETECHAT);
        menu.setMenuListener(this);

//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//        if (chatItem != null) {
//            chatMenu = new ChatMenu(menu);
//            chatMenu.setMenuListener(menu.getListener());
//            chatMenu.beforeShow();
//            return;
//        }
//#sijapp cond.end#
        Jimm.setDisplay(menu);
    }

    public void buildMenu() {
        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        addCommandEx(cmdCloseChat, MENU_TYPE_RIGHT_BAR);
        setCommandListener(this);
    }
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#

    public int getBottom() {
        if (textLine == null) {
            return getHeightInternal();
        }
        return super.getBottom() + getMenuBarHeight();
    }

    public final String getLeftMenu() {
        if (leftMenu == null)
            return "";
        else
            return leftMenu.getLabel();
    }

    public final String getRightMenu() {
        if (rightMenu == null)
            return "";
        else
            return rightMenu.getLabel();
    }

    public void buildMenu(boolean light, boolean cKey) {
        int left = MENU_TYPE_LEFT_BAR;
        int right = MENU_TYPE_RIGHT_BAR;
        if (light) {
            addCommandEx(JimmUI.cmdSelect, left);
            addCommandEx(JimmUI.cmdBack, right);
        } else {
            if (cKey) {
                addCommandEx(JimmUI.cmdMenu, right);
                addCommandEx(JimmUI.cmdSend, left);
            } else {
                addCommandEx(JimmUI.cmdMenu, left);
                addCommandEx(JimmUI.cmdClear, right);
            }
        }

        setCommandListener(this);
    }

    private void showLineMenu(boolean cKey) {
        Menu menu = new Menu(this);
        if (!cKey) {
            menu.addMenuItem("send", LINE_SEND);
        }
//#sijapp cond.if modules_SMILES is "true" #
        menu.addMenuItem("insert_emotion", LINE_EMO);
//#sijapp cond.end#
        menu.addMenuItem("alt_window", LINE_ALTWIN);
        menu.addMenuItem("FastType", LINE_FASTYPE);
        if (!JimmUI.clipBoardIsEmpty()) {
            menu.addMenuItem("paste", LINE_PASTE);
            menu.addMenuItem("quote", LINE_QOUTE);
        }
        menu.addMenuItem("transliterate", LINE_TRANS);
        menu.addMenuItem("detransliterate", LINE_DETRANS);
        menu.addMenuItem("massive_send", LINE_MASSEND);
        menu.addMenuItem("templates", LINE_TEMPL);
        menu.addMenuItem("clear", LINE_CLEAR);
        menu.addMenuItem("cancel", LINE_CANCEL);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }
//#sijapp cond.end#

    public boolean isActive() {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        //if (chatItem != null) {
        //    return chatItem.isShown(this);
        //}
//#sijapp cond.end#
        return super.isActive();
    }

//    private Object getVisibleObject() {
////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//        //if (chatItem != null) return chatItem.getForm();
////#sijapp cond.end#
//        return this;
//    }

    public void invalidate() {
        if (getLocked()) return;
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        //if (chatItem != null) chatItem.updateContents(this);
        //else
//#sijapp cond.end#
        super.invalidate();
    }

    public void beforeShow() {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        hideMenu();
        int clchType = Options.getInt(Options.OPTION_CLASSIC_CHAT);
        //if (clchType == 1) {
        //    if (chatItem == null) {
        //        initChatItem();
        //    }
        //} else
        if (clchType == 1) {
            setFullScreenMode(true);
            initTextLine();
        }
//#sijapp cond.end#
    }

    public final void setChatEx() {
        //if (chatItem != null) {
        //     chatItem.setChat(this);
        //} else {
        invalidate();
        //}
    }

    //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    void hideMenu() {
        chatMenu = null;
    }

    public void paint(Graphics g) {
        g.setColor(getColor(COLOR_BACK));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
        if (textLine != null) {
            textLine.paint(g);
            if (textLine.vike != null) {
                textLine.vike.paint(g);
            }
        }
        if (chatMenu != null) {
            chatMenu.paint(g);
        }
    }

    protected void drawScroller(Graphics g, int topY, int visCount, int menuBarHeight) {
        super.drawScroller(g, topY, visCount, (textLine == null) ? menuBarHeight : 0);
    }

    public boolean drawMenuBar(Graphics g, int height, int bottom, int style, int curX, int curY) {
        if (lineTyping && textLine != null && textLine.vike != null) {
            return false;
        }
        if (lineTyping) {
            return super.drawMenuBar(g, height, getHeight(), style, curX, curY, textLine.getCapsString());
        }
        return super.drawMenuBar(g, height, (textLine == null) ? bottom : getHeight(), style, curX, curY);
    }

    boolean pressSoft(Command c) {
        if (leftMenu == c) {
            super.keyReaction(0, KEY_RELEASED, KEY_CODE_LEFT_MENU);
            return true;
        } else if (rightMenu == c) {
            super.keyReaction(0, KEY_RELEASED, KEY_CODE_RIGHT_MENU);
            return true;
        }
        return false;
    }

    public void pressSoft(int gameAct) {
        super.keyReaction(0, KEY_RELEASED, gameAct);
    }

    protected boolean checkSoftKeyPressState(Command c, int type) {
        if (!lineTyping || c != JimmUI.cmdClear) {
            return super.checkSoftKeyPressState(c, type);
        }
        return (type != KEY_RELEASED);
    }

    public final void beforeHide() {
        if (lineTyping)
            saveCurrMessage(textLine.getString());
        super.beforeHide();
    }

    protected final int getDrawHeight() {
        if (!lineTyping)
            return super.getDrawHeight();
        else
            return VirtualList.getHeight() - getCapHeight() - ((textLine != null) ? (textLine.vike != null) ? textLine.vike.height : 0 : 0);
    }

    public final void afterShow() {
        if (lineTyping)
            textLine.upd();
        super.afterShow();
    }

    public final void sizeChanged(int w, int h) { // todo сделать обновление ширины всех сообщений

        //DebugLog.addText("size changed w=" + w + " h=" + h);
        //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        if (textLine != null) {
            textLine.screenChange();
            textLine.setForcedSize();
        }
        //#sijapp cond.end#

//        boolean flag = false;
//        if (getWidthInternal() != w && w > 0) {
//            flag = true;
//        }
//        setForcedSize(w, h);
//
//        if (flag) {
//            super.beforeShow();
//            beforeShow();
//            afterShow();
//            //if (textLine != null && lineTyping) {
//            //    textLine.beforeTyping();
//            //}
//            //TextFieldEx.newWidth();
//            //if (textLine != null && textLine.vike != null) {
//            //    textLine.vike.upd();
//            //}
//        }
        checkCurrItem();
        checkTopItem();
    }

    public void setForcedSize(int width, int height) {
        super.setForcedSize(width, height);
        checkCurrItem();
        checkTopItem();
    }
//#sijapp cond.end#

    public void commandAction(Command c, Displayable d) {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        if (contact.uTyping()) {
            contact.beginTyping(false);
        }
        if ((lineTyping) && (textLine != null)) {
            if (c == JimmUI.cmdBack) {
                textLine.afterTyping();
            } else if (c == JimmUI.cmdClear) {
                textLine.keyReaction(-8, KEY_RELEASED, 0);
            } else if (c == JimmUI.cmdMenu) {
                showLineMenu(TextFieldEx.cKey);
            } else if (c == JimmUI.cmdSend) {
                try {
                    JimmUI.sendMessage(textLine.getString(), contact);
                } catch (Exception ignored) {
                }
                textLine.setString("");
                textLine.afterTyping();
            } else if (c == JimmUI.cmdSelect) {
                textLine.keyReaction(0, KEY_RELEASED, Canvas.FIRE);
            }
        } else
//#sijapp cond.end#
            if (c == cmdCloseChat) {
                contact.resetUnreadMessages();
                Jimm.getContactList().activate();
            } else if (c == JimmUI.cmdMenu) {
                showMenu();
            }
    }

    public void menuSelect(Menu menu, byte action) {
        if (/*action != MENU_CONTACTMENU && */action != MENU_GOTOURL && action != MENU_DELETECHAT) {
            menu.back();
        }
        switch (action) {
            case MENU_DELETECHAT:
                JimmUI.showSelector(SelectBase.getStdSelector(), new SelectListener() {
                    public void selectAction(int action, int selectType, Object o) {
                        int delType = -1;
                        switch (selectType) {
                            case 0:
                                delType = ChatHistory.DEL_TYPE_CURRENT;
                                break;
                            case 1:
                                delType = ChatHistory.DEL_TYPE_ALL_EXCEPT_CUR;
                                break;
                            case 2:
                                delType = ChatHistory.DEL_TYPE_ALL;
                                break;
                        }
                        contact.getProfile().getChatHistory().chatHistoryDelete(contact, delType);
                        Jimm.getContactList().activate();
                    }
                }, true);
                break;

            case MENU_ADDTOCOPIED:
                (new MassCopy(this, contact.name, contact.getProfile().getNick())).activate();
                break;

            case MENU_COPYTEXT:
                contact.getProfile().getChatHistory().copyText(/*action == MENU_COPYALLTEXT*/false, contact.getUinString(), contact.name);
                break;

            case MENU_QOUTE:
            case MENU_PASTE:
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                if (textLine != null) {
                    textLine.insert(JimmUI.getClipBoardText(action == MENU_QOUTE));
                    lineTyping |= true;
                    textLine.beforeTyping();
                    //} else if (chatItem != null) {
                    //    chatItem.insert(JimmUI.getClipBoardText(action == MENU_QOUTE), chatItem.getCaretPosition());
                    //    chatItem.activateTextField();
                } else
//#sijapp cond.end#
                    JimmUI.writeMessage(contact, JimmUI.getClipBoardText(action == MENU_QOUTE)).activate();
                break;

//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
            case MENU_GOTOURL:
                JimmUI.gotoURL(getCurrText(0, false));
                break;
//#sijapp cond.end#

//            case MENU_ADDUSR:
//                Search search = new Search(true, contact.getIcq());
//                String data[] = new String[Search.LAST_INDEX];
//                data[Search.UIN] = contact.getUinString();
//                SearchAction act = new SearchAction(search, data, SearchAction.CALLED_BY_ADDUSER);
//                try {
//                    contact.getIcq().requestAction(act);
//                } catch (JimmException e) {
//                    JimmException.handleException(e);
//                }
//                contact.getProfile().addAction("wait", act);
//                break;

            case MENU_APPLYSCHEME:
                int idx = getCurrTextIndex();
                MessData md = (MessData) getMessData().elementAt(idx);
                String txt = getCurrText(md.getOffset(), false);
                if (txt == null) {
                    return;
                }
                colorSchemeApply(txt);
                Options.saveColorScheme(VirtualList.getBackGroundImage().getHeight() > 64);
                break;

//            case MENU_COPYNUM:
//                MessData mdcopy = (MessData) getMessData().elementAt(getCurrTextIndex());
//                String txtcopy = getCurrText(mdcopy.getOffset(), false);
//                JimmUI.setClipBoardText(Util.getNumber(txtcopy));
//                break;

//           case MENU_CALLNUM:
//                try {
//                    MessData mdnum = (MessData) getMessData().elementAt(getCurrTextIndex());
//                    String txtnum = getCurrText(mdnum.getOffset(), false);
//                    Jimm.jimm.platformRequest("tel:" + Util.getNumber(txtnum));
//                } catch (Exception e) {}
//                break;

//#sijapp cond.if modules_HISTORY is "true" #
            case MENU_HISTORY:
                HistoryStorage.showHistoryList(contact);
                break;
//            case MENU_ADDTOHISTORY:
//                //(new MassCopy(this, contact)).activate();
//                int textIndex = getCurrTextIndex();
//                MessData hdata = (MessData) getMessData().elementAt(textIndex);
//                String text = getCurrText(hdata.getOffset(), false);
//                if (text == null) {
//                    return;
//                }
//                HistoryStorage.addText(contact, (hdata.getTime() == 0) ? "<distxtr>" + text : text, hdata.getIncoming() ? 0 : (byte) 1,
//                        hdata.getIncoming() ? contact.name : Options.getString(Options.OPTION_MY_NICK), hdata.getTime());
//                break;
//#sijapp cond.end#

            case MENU_GRANTAUTH:
                contact.setIntValue(ContactItem.CONTACTITEM_AUTREQUESTS, 0);
                SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, contact, true, "");
                SysNoticeAction sysNotAct = new SysNoticeAction(notice);
                try {
                    contact.getIcq().requestAction(sysNotAct);
                } catch (JimmException e) {
                    JimmException.handleException(e);
                }
                updateMessIcon();
                break;

            case MENU_DENYAUTH:
                contact.setIntValue(ContactItem.CONTACTITEM_AUTREQUESTS, 0);
                JimmUI.authMessage(JimmUI.AUTH_TYPE_DENY, contact, "reason", null);
                updateMessIcon();
                break;

//            case MENU_REQAUTH:
//                JimmUI.authMessage(JimmUI.AUTH_TYPE_REQ_AUTH, contact, "requauth", "plsauthme");
//                break;

//            case MENU_CONTACTMENU:
//                Menu m = new Menu(this, (byte) 1);
//                JimmUI.fillContactMenu(contact, m);
//                Jimm.setPrevScreen(getVisibleObject());
////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//                if (chatItem != null) {
//                    hideMenu();
//                    chatMenu = new ChatMenu(m);
//                    chatMenu.setMenuListener(m.getListener());
//                    chatMenu.beforeShow();
//                    return;
//                }
////#sijapp cond.end#
//                //Jimm.setDisplay(this);
//                Jimm.setDisplay(m);
//                break;

            case MENU_DCINFO:
                (new jimm.info.ClientInfo()).showClientInfo(contact);
                break;

            case MENU_INFO:
                (new jimm.info.UserInfo()).requiestUserInfo(contact.getProfile(), contact.getUinString(), contact.name);
                break;
            //#sijapp cond.if modules_FILES is "true"#
            case MENU_FILE_TRANS:
                jimm.files.FileTransfer ft = new jimm.files.FileTransfer(jimm.files.FileTransfer.FT_TYPE_FILE_BY_NAME, contact);
                ft.startFT();
                break;
            //#sijapp cond.end#

            //#sijapp cond.if modules_SMILES is "true" #
            case MENU_SMILES:
                //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                if (textLine != null) {
                    Emotions.selectEmotion(textLine, this);
                } else
                    //#sijapp cond.end#
                    JimmUI.writeMessage(contact, null).commandAction(JimmUI.cmdInsertEmo, null);
                //new InputTextBox(InputTextBox.EDITOR_MODE_MESSAGE, (Options.getBoolean(Options.OPTION_EMPTY_TITLE)) ? null : (contact != null) ? contact.name : null, "").commandAction(JimmUI.cmdInsertEmo, null);
                break;
            //#sijapp cond.end#

            case MENU_TEMPLARES:
                //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                if (textLine != null) {
                    (new Templates()).selectTemplate(textLine, this);
                } else
                    //#sijapp cond.end#
                    JimmUI.writeMessage(contact, null).commandAction(JimmUI.cmdInsTemplate, null);
                //new InputTextBox(InputTextBox.EDITOR_MODE_MESSAGE, (Options.getBoolean(Options.OPTION_EMPTY_TITLE)) ? null : (contact != null) ? contact.name : null, "").commandAction(JimmUI.cmdInsTemplate, null);
                break;

            case MENU_TO_TEMPLARE:
                String base = Util.removeCr(contact.getProfile().getChatHistory().getCurrentMessage(contact.getUinString()).trim().replace('\n', ' '));
                Templates.addFromChat(base);
                break;

            case MENU_STATUS:
                JimmUI.requestStatusMess(contact);
                break;

            case MENU_XSTATUS:
                contact.requestXStatusText();
                break;


//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
            case LINE_SEND:
                try {
                    JimmUI.sendMessage(textLine.getString(), contact);
                } catch (Exception ignored) {
                }
                textLine.setString("");
                textLine.afterTyping();
                break;

            case LINE_CANCEL:
                textLine.afterTyping();
                break;

            case LINE_TRANS:
                textLine.setString((new StringConvertor()).transliterate(textLine.getString()));
                break;

            case LINE_DETRANS:
                textLine.setString((new StringConvertor()).detransliterate(textLine.getString()));
                break;
//#sijapp cond.if modules_SMILES is "true" #
            case LINE_EMO:
                Emotions.selectEmotion(textLine, this);
                break;
//#sijapp cond.end#
            case LINE_TEMPL:
                (new Templates()).selectTemplate(textLine, this);
                break;

            case LINE_CLEAR:
                textLine.setString("");
                break;

            case LINE_QOUTE:
            case LINE_PASTE:
                if (!JimmUI.clipBoardIsEmpty()) {
                    textLine.insert(JimmUI.getClipBoardText(action == LINE_QOUTE));
                }
                break;

            case LINE_MASSEND:
                JimmUI.showMassiveSendList(textLine.getString());
                break;

            case LINE_ALTWIN:
                Jimm.setPrevScreen(this);
                new InputTextBox(InputTextBox.EDITOR_MODE_ALT, contact.name, textLine.getString()).activate();
                break;

            case LINE_FASTYPE:
                //textLine.onOffT10();
                menu.lock();
                menu.clear();
                menu.addMenuItem(ResourceBundle.getString(textLine.getT10String()), FAT_ONOFF);
                if (textLine.getBase() != null) {
                    menu.addMenuItem("add_to_list", FAT_ADD_WORD);
                    menu.addMenuItem("delete", FAT_DELETE_WORD);
                    //#sijapp cond.if modules_FILES is "true"#
                    menu.addMenuItem("export", FAT_EXPORT);
                    //#sijapp cond.end#
                }
                menu.unlock();
                Jimm.setDisplay(menu);
                break;

            case FAT_ONOFF:
                textLine.onOffT10();
                break;

            case FAT_ADD_WORD:
                Jimm.setPrevScreen(this);
                textLine.getBase().addWord();
                break;

            case FAT_DELETE_WORD:
                Jimm.setPrevScreen(this);
                textLine.getBase().deleteWord();
                break;
//#sijapp cond.if modules_FILES is "true"#
            case FAT_EXPORT:
                textLine.getBase().exportBase();
                break;
//#sijapp cond.end#
//#sijapp cond.end#
        }
    }

    static int getInOutColor(boolean incoming, boolean head) {
        int color;
        if (!head) {
            color = incoming ? getColor(COLOR_MESS) : getColor(COLOR_YMESS);
        } else {
            color = incoming ? getColor(COLOR_NICK) : getColor(COLOR_YOUR_NICK);
        }
        return color;
    }

    public Vector getMessData() {
        return messData;
    }

    //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    public void doKeyreaction(int keyCode, int type) {
        if ((lineTyping) && (textLine != null)) {
            textLine.keyReaction(keyCode, type, getExtendedGameAction(keyCode));
        } else if (chatMenu != null) {
            chatMenu.doKeyreaction(keyCode, type);
        } else {
            super.doKeyreaction(keyCode, type);
        }
    }

    //#sijapp cond.end#
    protected boolean keyReaction(int keyCode, int type, int gameAct) {
        return (keyCode == Canvas.KEY_STAR) || (keyCode == Canvas.KEY_POUND) || (keyCode == Canvas.KEY_NUM0) || super.keyReaction(keyCode, type, gameAct);
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void setImage(Icon img) {
        setCapImage(img);
        //#sijapp cond.if target="SIEMENS2"#
        //((com.siemens.mp.lcdui.Displayable)form).setHeadlineIcon(img.getImg());
        //#sijapp cond.end#
    }

    public void setXstImage(Icon img) {
        setCapXstImage(img);
    }

    public void vlItemClicked(VirtualList sender) {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        //if (chatItem != null) {
        //    chatItem.activateTextField();
        //    return;
        //} else
        if (textLine != null) {
            if (!lineTyping) {
                lineTyping |= true;
                textLine.beforeTyping();
            }
        } else
//#sijapp cond.end#
            JimmUI.writeMessage(contact, null).activate();
    }

    //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    public void pointerReleased(int x, int y) {
        if (y < getCapHeight()) {
            new ChatList(this, contact.getProfile());
            return;
        }
        if (Options.getInt(Options.OPTION_CLASSIC_CHAT) != 0) {
            int qwertyTop = getHeight();
            if (textLine != null && textLine.vike != null) {
                qwertyTop -= textLine.vike.height;
            } else {
                qwertyTop -= getMenuBarHeight();
            }
            int textLineTop = qwertyTop - textLine.getHeight(lineTyping);
            if (!lineTyping && textLine != null && y > textLineTop && y < qwertyTop) {
                lineTyping |= true;
                textLine.beforeTyping();
                return;
            }
            if (lineTyping && textLine != null) {
                if (textLine.vike != null) {
                    //if (y > getMenuBarHeight()) {
                    //    super.pointerReleased(x, y);
                    //}
                    if (y > qwertyTop) {
                        textLine.qwertyAction(x, y);
                    } else if (y > textLineTop) {
                        Jimm.setPrevScreen(this);
                        new InputTextBox(InputTextBox.EDITOR_MODE_ALT, contact.name, textLine.getString()).activate();
                    } else {
                        textLine.afterTyping();
                    }
                } else {
                    if (y > getMenuBarHeight()) {
                        super.pointerReleased(x, y);
                    }
                    if (y > textLineTop) {
                        Jimm.setPrevScreen(this);
                        new InputTextBox(InputTextBox.EDITOR_MODE_ALT, contact.name, textLine.getString()).activate();
                    } else {
                        textLine.afterTyping();
                    }
                }
                return;
            }
        }
        super.pointerReleased(x, y);
    }


    public void pointerPressed(int x, int y) {
        if (Options.getInt(Options.OPTION_CLASSIC_CHAT) != 0) {
            /*isDraggedWas = true;
            int qwertyTop = getHeight();
            if (textLine != null && textLine.vike != null) {
                qwertyTop -= textLine.vike.height;
            } else {
                qwertyTop -= getMenuBarHeight();
            }*/
            //DebugLog.addText("vis=" + getVisCount() + " size=" + getSize() + " getHeightInternal=" + getHeightInternal() + "getDrawHeight" + getDrawHeight());
            if (lineTyping && textLine != null && textLine.vike != null && textLine.qwertySwitch(x, y)/* && y > qwertyTop*/) {
                return;
            }
        }
        super.pointerPressed(x, y);
    }

    public void pointerDragged(int x, int y) {
        if (Options.getInt(Options.OPTION_CLASSIC_CHAT) != 0) {
            /*isDraggedWas = true;
            int qwertyTop = getHeight();
            if (textLine != null && textLine.vike != null) {
                qwertyTop -= textLine.vike.height;
            } else {
                qwertyTop -= getMenuBarHeight();
            }*/
            if (lineTyping && textLine != null && textLine.vike != null && textLine.qwertySwitch(x, y)/* && y > qwertyTop*/) {
                return;
            }
        }
        super.pointerDragged(x, y);
    }

    public boolean drawCaption(Graphics g, int mode, int curX, int curY) {
        if (lineTyping && textLine != null && textLine.vike != null) {
            return false;
        }
        return super.drawCaption(g, mode, curX, curY);
    }
//#sijapp cond.end#

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        if (type == KEY_RELEASED) {
            switch (keyCode) {
                // #sijapp cond.if target is "MIDP2"#
                case -8:
                    JimmUI.showSelector(SelectBase.getStdSelector(), new SelectListener() {
                        public void selectAction(int action, int selectType, Object o) {
                            int delType = -1;
                            switch (selectType) {
                                case 0:
                                    delType = ChatHistory.DEL_TYPE_CURRENT;
                                    break;
                                case 1:
                                    delType = ChatHistory.DEL_TYPE_ALL_EXCEPT_CUR;
                                    break;
                                case 2:
                                    delType = ChatHistory.DEL_TYPE_ALL;
                                    break;
                            }
                            contact.getProfile().getChatHistory().chatHistoryDelete(contact, delType);
                            Jimm.getContactList().activate();
                        }
                    }, true);
                    return;
                // #sijapp cond.end#

                case Canvas.KEY_NUM0:
                    contact.getProfile().getChatHistory().copyText(contact.getUinString(), contact.name, true);
                    return;

                case Canvas.KEY_STAR:
                    contact.getProfile().getChatHistory().copyText(false, contact.getUinString(), contact.name);
                    return;

                case Canvas.KEY_POUND:
                    if (!JimmUI.clipBoardIsEmpty()) {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
                        if (textLine != null) {
                            textLine.insert(JimmUI.getClipBoardText(true));//textLine.insert(JimmUI.getClipBoardText(false));
                            lineTyping |= true;
                            textLine.beforeTyping();
                            //} else if (chatItem != null) {
                            //    chatItem.insert(JimmUI.getClipBoardText(false), chatItem.getCaretPosition());
                            //    chatItem.activateTextField();
                        } else
//#sijapp cond.end#
                            JimmUI.writeMessage(contact, JimmUI.getClipBoardText(false)).activate();
                    }
                    return;
            }
        }

        try {
            if (type == KEY_PRESSED) {
                String currUin;

                switch (gameAct) {
                    case Canvas.LEFT:
                        currUin = Jimm.getContactList().showNextPrevChat(false);
                        //ChatHistory.calcCounter(currUin);
                        contact.getProfile().getChatHistory().UpdateCaption(currUin);
                        return;

                    case Canvas.RIGHT:
                        currUin = Jimm.getContactList().showNextPrevChat(true);
                        //ChatHistory.calcCounter(currUin);
                        contact.getProfile().getChatHistory().UpdateCaption(currUin);
                        return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    //#sijapp cond.if target isnot "DEFAULT"#
    public void BeginTyping(boolean typing) {
////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//        if (chatItem != null) {
////#sijapp cond.if target is "MIDP2"#
//            if (Jimm.is_phone_SE()) {
//                repaint();
//                return;
//            }
//// #sijapp cond.end#
//            currTicker = typing ? new Ticker(ResourceBundle.getString("typing")) : null;
//            //if (isActive() && Options.getInt(Options.OPTION_CLASSIC_CHAT) == 1) {
//                //chatItem.getForm().setTicker(currTicker);
//            //}
//        } else
//// #sijapp cond.end#
        repaint();
    }
//#sijapp cond.end#

    public synchronized void addExtraNotice(String laction, String text, Icon icon) {
        String action = ResourceBundle.getString(laction);
        addExtraNotice(action + " " + text, icon);
    }

    public synchronized void addExtraNotice(String action, Icon icon) {
        if (!isActive()) {
            contact.increaseMessageCount(ContactItem.MESSAGE_SYS_NOTICE);
        }
        long time = DateAndTime.createCurrentDate(false);
        action = ResourceBundle.getString(action);
        String att = ResourceBundle.getString("notice");
//#sijapp cond.if modules_HISTORY is "true" #
        if (Options.getBoolean(Options.OPTION_HISTORY) || contact.getExtraValue(ContactItem.EXTRA_HIST)) {
            HistoryStorage.addText(contact, action, (byte) 0, att, time);
        }
//#sijapp cond.end#
        //System.out.println("att = " + att);
        //System.out.println("action = " + action);
        addTextToForm(att, action, "", time, true, false, icon, 0);
    }

    // "%ICON%[b]%NICK% (%H%:%M%:%S%):[/b]%CRLF%%MSG%;
    // %PIC<b>%NICK (%HOUR:%MIN:%SEC):</b>%BR%MSG
    // "[b][i]->[/i][o]<-[/o][/b] :%MSG%"
    // "[b][i]Mne pishut[/i][o]Ya pishu[/o][/b] :%MSG%"
    // %PIC[b]%NICK (%HOUR:%MIN:%SEC):[/b]%BR%MSG

    public static void messChange() {
        messTemplare = explodes(Options.getString(Options.OPTION_MESSAGE_TEMPLARE));
    }

    private static Vector explodes(String text) {
        String[] table = Util.explode("[B],[/B],[I],[/I],[O],[/O],%PIC,%NICK,%HOUR,%MIN,%SEC,%BR,%MSG", ',');
        Vector tmp = new Vector();
        StringBuffer strBuf = new StringBuffer();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            strBuf.append(text.charAt(i));
            int j = equalTable(strBuf.toString(), table);
            //System.out.println("equalTable " + strBuf.toString() + " = " + j);
            if (j >= 0) {
                if (strBuf.length() > 0) {
                    tmp.addElement(strBuf.toString().substring(0, strBuf.length() - table[j].length()));
                }
                //System.out.println(strBuf.toString().substring(0, strBuf.length() - table[j].length()));
                tmp.addElement(new Integer(j));
                //System.out.println(j);
                strBuf.delete(0, strBuf.length());
            }
        }
        if (tmp.size() == 0) {
            tmp.addElement(strBuf.toString());
        }
        return tmp;
    }

    static int equalTable(String src, String[] table) {
        if (src == null || src.length() == 0) {
            return -1;
        }
        String temp = src.toUpperCase();
        for (int i = 0; i < table.length; i++) {
            if (temp.indexOf(table[i]) >= 0) {
                return i;
            }
        }
        return -1;
    }

    // todo nullpointer if getXtraz
    public void addTextToForm(String from, String message, String url, long time, boolean incoming, boolean offline, Icon image, long msgId) {
        lock();
        int texOffset = 0;
        boolean xTraz = (time == 0);
        synchronized (messData) {
            boolean inc = incoming;
            int font = Font.STYLE_PLAIN;
            String[] fullTime = Util.explode(DateAndTime.getDateString(!offline, true, time), ':');
            for (int i = 0; i < messTemplare.size(); i++) {
                Object item = messTemplare.elementAt(i);   // %NICK%
                if (messTemplare.elementAt(i) instanceof Integer) {
                    Integer integer = (Integer) item;
                    int act = integer.intValue();
                    if (act > 5 | act < 2)
                        if (inc ^ incoming | ((act != 12 & act != 6) & xTraz))
                            continue;

                    switch (act) {
                        case 0:
                            font = Font.STYLE_BOLD;
                            break;
                        case 1:
                            font = Font.STYLE_PLAIN;
                            break;
                        case 2:
                            inc = true;
                            break;
                        case 4:
                            inc = false;
                            break;
                        case 3:
                        case 5:
                            inc = incoming;
                            break;
                        case 6:
                            if (image != null) {
                                addHeaderIcon(image, messTotalCounter, msgId);
                            }
                            break;
                        case 7:
                            if (from != null) {
                                addText(from, getInOutColor(incoming, true), messTotalCounter, font, false);
                            }
                            break;
                        case 8:
                        case 9:
                        case 10:
                            try {
                                addText(fullTime[act - 8], getInOutColor(incoming, true), messTotalCounter, font, false);
                            } catch (Exception ignored) {
                            }
                            break;
                        case 11:
                            doCRLF(messTotalCounter);
                            break;
                        case 12:
                            String txt = getTextByIndex(0, false, messTotalCounter);
                            texOffset = 0;
                            if (txt != null) {
                                texOffset = (getTextByIndex(0, false, messTotalCounter)).length();
                            }
                            int fontEx = font;
                            if (font != Font.STYLE_BOLD) {
                                if (xTraz) {
                                    fontEx = Font.STYLE_BOLD;
                                } else {
                                    fontEx = getFontStyle();
                                }
                            }
                            addText(message, getInOutColor(incoming, xTraz), messTotalCounter, fontEx, true);
                            break;
                    }
                } else if (messTemplare.elementAt(i) instanceof String) {
                    if (inc ^ incoming || xTraz)
                        continue;

                    String string = (String) item;
                    addText(string, getInOutColor(incoming, true), messTotalCounter, font, false);
                }
            }
            boolean contains_url = false;
            if (Util.parseMessageForURL(message) != null || url.length() > 0) {
                contains_url = true;
            }
            getMessData().addElement(new MessData(incoming, time, texOffset, contains_url));
        }
        messTotalCounter++;

        int messIndex = getCurrTextIndex();
        if (!incoming || messIndex >= (getMessData().size() - 2)) {
            setCurrentItem(getSize() - 1);
            selectTextByIndex(messTotalCounter - 1);
        }
        unlock();
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        //if (chatItem != null) {
        //    chatItem.updateContents(this);
        //}
//#sijapp cond.end#
    }

    private void addText(String text, int color, int messTotalCounter, int fontStyle, boolean doCRLF) {
//#sijapp cond.if modules_SMILES is "true" #
        if (doCRLF) {
            Emotions.addTextWithEmotions(this, text, fontStyle, color, messTotalCounter);
        } else
//#sijapp cond.end#
            addBigText(text, color, fontStyle, messTotalCounter);

        if (doCRLF) {
            doCRLF(messTotalCounter);
        }
    }

    //#sijapp cond.if modules_SBOLTUN is "true"#
    static public void sBoltunInit() {
        if (sBoltun == null && Options.getBoolean(Options.OPTION_SBOLTUN)) {
            sBoltun = new sBoltun();
            sBoltun.parseMind();
        }
    }

    public static void sBoltunInput(String inpmessage, ContactItem item) {
        if (sBoltun != null && Options.getBoolean(Options.OPTION_SBOLTUN)) {
            sBoltun.input(inpmessage, item);
        }
    }
//#sijapp cond.end#

    /* Adds a message to the message display */

    public void addMessage(Message message) {
        Icon image;
        String uin = contact.getUinString();

        boolean offline = message.getOffline();
        boolean visible = isActive();

        if ((!visible) && (ChatHistory.currentChat != null) && (ChatHistory.currentChat.isActive() && Jimm.getContactList().contain(contact))) {
            ChatHistory.currentChat.setCapPStImage(ContactList.imageList.elementAt(14));
        }

        if (message instanceof PlainMessage) {
            PlainMessage plainMsg = (PlainMessage) message;
            image = ContactList.imageList.elementAt(14);
            if (!visible) {
                contact.increaseMessageCount(ContactItem.MESSAGE_PLAIN);
            }
            addTextToForm(contact.name, plainMsg.getText(), "", plainMsg.getNewDate(), true, offline, image, 0);
//#sijapp cond.if modules_HISTORY is "true" #
            if (Options.getBoolean(Options.OPTION_HISTORY) || contact.getExtraValue(ContactItem.EXTRA_HIST)) {
                HistoryStorage.addText(contact, plainMsg.getText(), (byte) 0, contact.name, plainMsg.getNewDate());
            }
//#sijapp cond.end#

            if (!offline) {
                //#sijapp cond.if modules_PANEL is "true"#
                if (!contact.getProfile().getChatHistory().chatHistoryShown(uin)) {
                    contact.getProfile().showPopupItem(
                            contact.getStringValue(ContactItem.CONTACTITEM_NAME) + ": " + plainMsg.getText(),
                            ContactList.imageList.elementAt(14),
                            contact
                    );
                }
                //#sijapp cond.end#
                contact.showPopupWindow(uin, contact.getStringValue(ContactItem.CONTACTITEM_NAME), plainMsg.getText());
            }

        } else if (message instanceof UrlMessage) {
            UrlMessage urlMsg = (UrlMessage) message;
            image = ContactList.imageList.elementAt(14);
            if (!visible) contact.increaseMessageCount(ContactItem.MESSAGE_URL);
            addTextToForm(contact.name, urlMsg.getText(), urlMsg.getUrl(), urlMsg.getNewDate(), false, offline, image, 0);
        } else if (message instanceof SystemNotice) {
            SystemNotice notice = (SystemNotice) message;
            if (!visible) {
                contact.increaseMessageCount(ContactItem.MESSAGE_SYS_NOTICE);
            }
            image = ContactList.imageList.elementAt(17);

            if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ) {
                contact.increaseMessageCount(ContactItem.MESSAGE_AUTH_REQUEST);
                image = ContactList.imageList.elementAt(16);
            } else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY) {
                if (notice.isAUTH_granted()) {
                    contact.setBooleanValue(ContactItem.CONTACTITEM_NO_AUTH, false);
                    contact.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, false);
                } else {
                    image = ContactList.imageList.elementAt(16);
                }
            }
            if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_DELETE) {
                contact.setBooleanValue(ContactItem.CONTACTITEM_NO_AUTH, false);
                contact.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, true);
                contact.setBooleanValue(ContactItem.CONTACTITEM_ADDED, false);
                contact.setIntValue(ContactItem.CONTACTITEM_STATUS, ContactItem.STATUS_OFFLINE);
            }
            addTextToForm(ResourceBundle.getString("sysnotice"), notice.getText(), "", notice.getNewDate(), false, offline, image, 0);
        }
        contact.getProfile().getChatHistory().UpdateCaption(uin);
    }

    public void addMyMessage(String message, long time, long msgId) {
        Icon image = ContactList.imageList.elementAt(14);
        addTextToForm(contact.getProfile().getNick(), message, "", time, false, false, image, msgId);
    }

    private int getFontStyle() {
//        switch (Options.getInt(Options.OPTION_USER_FONT)) {
//            case 1:
//                return Font.STYLE_BOLD;
//            case 2:
//                return Font.STYLE_ITALIC;
//            case 3:
//                return Font.STYLE_UNDERLINED;
//        }
        return Font.STYLE_PLAIN;
    }

    private void updateMessIcon() {
        if (contact.getProfile().getUnreadMessCount() == 0) {
            setCapPStImage(null);
        } else {
            setCapPStImage(ContactList.imageList.elementAt(14));
        }
    }

    public void activate(boolean initChat, boolean resetText, ContactItem contact) {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        hideMenu();
        int clchType = Options.getInt(Options.OPTION_CLASSIC_CHAT);
//        if (clchType == 1) {
//            if (chatItem == null) {
//                initChatItem();
//            }
//            chatItem.setChat(this);
//            contact.getProfile().getChatHistory().UpdateCaption(contact.getUinString());
//            chatItem.getForm().setTitle(ChatName);
//            chatItem.getForm().setTicker(currTicker);
//            chatItem.setString(currMessage);
//            setFullScreenMode(false);
//            chatItem.updateChatHeight();
//            chatItem.updateContents(this);
//
//            Jimm.setPrevScreen(chatItem.getForm());
//            Jimm.setDisplay(chatItem.getForm());
//            ChatHistory.currentChat = this;
//
//            if (initChat) {
//                chatItem.activateTextField();
//            }
//            if (resetText) {
//                chatItem.setString("");
//            }
//        } else
        if (clchType == 1) {
            setFullScreenMode(true);
            initTextLine();
            textLine.setChat(this);
            textLine.setString(currMessage);
//			if ((!lineTyping) && initChat) {
//				lineTyping |= true;
//				textLine.beforeTyping();
//			}
            activateEx();
        } else {
            //if (chatItem != null) {
            //    chatItem = null;
            //}
            if (textLine != null) {
                textLine.vike = null;
                textLine = null;
            }
            lineTyping = false;
            setForcedSize(-1, -1);
            setFullScreenMode(true);
            activateEx();
        }
//#sijapp cond.else#
//#		activateEx();
//#sijapp cond.end#
        updateMessIcon();
    }

    private void activateEx() {
        contact.getProfile().getChatHistory().UpdateCaption(contact.getUinString());
        setCommandListener(this);
        Jimm.setPrevScreen(this);
        ChatHistory.currentChat = this;
        super.activate();
    }

    public void setTitle(String newName) {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        ChatName = newName;
        //if ((chatItem != null) && (isActive())) {
        //    chatItem.getForm().setTitle(newName);
        //    return;
        //}
//#sijapp cond.end#
        setCaption(newName);
    }

    /**
     * ***************************************************************
     */
    /* ONLINE COLOR SHEMES SEND */
    private static String colorThemeText() {
        return "%COLOR%SCHEME%";
    }

    private static boolean isScheme(String text) {
        return text.indexOf(colorThemeText()) != -1;
    }

    private static void colorSchemeApply(String text) {
        int idx = text.indexOf(colorThemeText()) + colorThemeText().length() + 1;
        String string = text.substring(idx, text.length());
        int position;
        try {
            position = 0;
            int colors[] = new int[COLORS];
            for (int i = 0; i < COLORS; i++) {
                if (i > 0) {
                    position++;
                }
                try {
                    colors[i] = Integer.parseInt(string.substring(position, position + 6), 16);
                } catch (Exception e) {
                    colors[i] = 0x000000;
                }
                position += 6;
            }
            setColors(colors);
            JimmUI.setColorScheme();
        } catch (Exception ignored) {
        }
    }

    public static String colorSchemeSend() {
        StringBuffer buf = new StringBuffer();
        StringBuffer bufFinal = new StringBuffer(colorThemeText() + "\n");
        for (int i = 0; i < COLORS; i++) {
            buf.setLength(0);
            if (i > 0) {
                buf.append("\n");
            }
            buf.append(Integer.toHexString(getColor(i) | 0x01000000).substring(1));
            bufFinal.append(buf.toString());
        }
        return bufFinal.toString();
    }
    /*******************************************************************/
}