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
 File: src/jimm/ChatHistory.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Artyomov Denis, Dmitry Tunin
 *******************************************************************************/

package jimm.chat;

import DrawControls.Icon;
import DrawControls.CanvasEx;
import jimm.*;
import jimm.comm.DateAndTime;

import javax.microedition.lcdui.Font;
import java.util.Enumeration;
import java.util.Hashtable;

/*************************************************/

/**
 * *********************************************
 */
class MessData {
    private long time;
    private int rowData;

    public MessData(boolean incoming, long time, int textOffset, boolean contains_url) {
        this.time = time;
        this.rowData = (textOffset & 0xFFFFFF) | (contains_url ? 0x8000000 : 0) | (incoming ? 0x4000000 : 0);
    }

    public boolean getIncoming() {
        return (rowData & 0x4000000) != 0;
    }

    public long getTime() {
        return time;
    }

    public int getOffset() {
        return (rowData & 0xFFFFFF);
    }

    //#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
    public boolean isURL() {
        return (rowData & 0x8000000) != 0;
    }
//#sijapp cond.end#
}

public class ChatHistory {
    private Hashtable historyTable;
    public static ChatTextList currentChat;
    private Profile profile;

    public ChatHistory(Profile profile) {
        this.profile = profile;
        historyTable = new Hashtable();
    }

    public synchronized void AckMessage(String uin, long msgId, boolean isUserAck) {
        if (historyTable.containsKey(uin)) {
            int imageIndex = isUserAck ? 21 : 20;
            if (imageIndex >= ContactList.imageList.size()) return;
            Icon img = ContactList.imageList.elementAt(imageIndex);

            // remove record from undelivered messages table
            boolean removeRecord = isUserAck || !profile.getBoolean(Profile.OPTION_DELIVERY_REPORT);

            ((ChatTextList) historyTable.get(uin)).switchHeaderIcon(msgId, img, removeRecord);
        }
    }

    public ChatTextList getChat(ContactItem contact) {
        if (contact == null) {
            return null;
        }
        String uin = contact.getUinString();
        if (!historyTable.containsKey(uin)) {
            newChatForm(contact, contact.name);
        }
        return getChatHistoryAt(uin);
    }

    private MessData getCurrentMessData(String uin) {
        ChatTextList list = getChatHistoryAt(uin);
        int messIndex = list.getCurrTextIndex();
        if (messIndex == -1) return null;
        MessData md = (MessData) list.getMessData().elementAt(messIndex);
        return md;
    }

    public String getCurrentMessage(String uin) {
        return getChatHistoryAt(uin).getCurrText(getCurrentMessData(uin).getOffset(), false);
    }

    public void copyText(boolean copyAll, String uin, String from) {
        ChatTextList list = getChatHistoryAt(uin);
        int messIndex = list.getCurrTextIndex();
        if (messIndex == -1) {
            return;
        }
        MessData md;
        if (copyAll) {
            //JimmUI.clearClipBoard();
            for (int i = 0; i < list.getMessData().size(); i++) {
                md = (MessData) list.getMessData().elementAt(i);
                JimmUI.setClipBoardText
                        (
                                md.getIncoming(), DateAndTime.getDateString(true, true, md.getTime()),
                                md.getIncoming() ? from : profile.getNick(), list.getTextByIndex(0, false, i)
                        );
            }
        } else {
            md = (MessData) list.getMessData().elementAt(messIndex);
            JimmUI.setClipBoardText
                    (
                            md.getIncoming(), DateAndTime.getDateString(true, true, md.getTime()),
                            md.getIncoming() ? from : profile.getNick(), getCurrentMessage(uin)
                    );
        }
    }

    public void copyText(String uin, String from, boolean add) {
        ChatTextList list = getChatHistoryAt(uin);
        int messIndex = list.getCurrTextIndex();
        if (messIndex == -1) return;
        MessData md = (MessData) list.getMessData().elementAt(messIndex);

        JimmUI.setClipBoardText
                (
                        md.getIncoming(), DateAndTime.getDateString(true, true, md.getTime()),
                        md.getIncoming() ? from : profile.getNick(), getCurrentMessage(uin), add
                );
    }

    // Returns the chat history form at the given uin
    public ChatTextList getChatHistoryAt(String uin) {
        return (historyTable.containsKey(uin)) ? (ChatTextList) historyTable.get(uin) : null;
    }

    public Hashtable getChatTable() {
        return historyTable;
    }

    final static public int DEL_TYPE_CURRENT = 1;
    final static public int DEL_TYPE_ALL_EXCEPT_CUR = 2;
    final static public int DEL_TYPE_ALL = 3;

    public void chatHistoryDelete(ContactItem cItem) {
        String uin = cItem.getUinString();
        if (currentChat == getChatHistoryAt(uin)) {
            currentChat = null;
        }
        historyTable.remove(uin);
        //cItem.deleteChat();
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        if (cItem.uTyping()) {
            cItem.beginTyping(false);
        }
//#sijapp cond.end#

        cItem.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, false);
        cItem.setIntValue(ContactItem.CONTACTITEM_AUTREQUESTS, 0);
        cItem.resetUnreadMessages();
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        if (historyTable.size() == 0) {
            ChatTextList.destroyTypingItems();
        }
//#sijapp cond.end#
    }

    // Delete the chat history for uin
    public void chatHistoryDelete(ContactItem cItem, int delType) {
        if (cItem == null && currentChat != null) {
            cItem = currentChat.getContact();
        }
        if (cItem == null) {
            return;
        }
        String uin = cItem.getUinString();
        switch (delType) {
            case DEL_TYPE_CURRENT:
                chatHistoryDelete(cItem);
                break;

            case DEL_TYPE_ALL_EXCEPT_CUR:
            case DEL_TYPE_ALL:
                Enumeration AllChats = historyTable.keys();
                String key;
                while (AllChats.hasMoreElements()) {
                    key = (String) AllChats.nextElement();
                    if ((delType == DEL_TYPE_ALL_EXCEPT_CUR) && (key.equals(uin))) {
                        continue;
                    }
                    chatHistoryDelete(profile.getItemByUIN(key));
                }
                break;
        }
    }

    // Returns if the chat history at the given number is shown
    public boolean chatHistoryShown(String uin) {
        return (historyTable.containsKey(uin)) && ((ChatTextList) historyTable.get(uin)).isActive();
    }

    // Returns true if chat history exists for this uin
    public boolean chatHistoryExists(String uin) {
        return historyTable.containsKey(uin);
    }

    public void setColorScheme() {
        Enumeration AllChats = historyTable.elements();
        while (AllChats.hasMoreElements()) {
            ((ChatTextList) AllChats.nextElement()).setColorScheme();
        }
        //AllChats = null;
    }

    // Creates a new chat form
    public void newChatForm(ContactItem contact, String name) {
        ChatTextList chatForm = new ChatTextList(name, contact);
        String uin = contact.getUinString();
        historyTable.put(uin, chatForm);
        contact.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, true);
        UpdateCaption(uin);
//#sijapp cond.if modules_HISTORY is "true" #
        fillFormHistory(contact);
//#sijapp cond.end#
        contact.setStatusImage();
    }

    // fill chat with last history lines
    //#sijapp cond.if modules_HISTORY is "true" #
    final static private int MAX_HIST_LAST_MESS = 5;

    public void fillFormHistory(ContactItem contact) {
        String name = contact.name;
        String uin = contact.getUinString();
        if (Options.getBoolean(Options.OPTION_SHOW_LAST_MESS)) {
            int recCount = HistoryStorage.getRecordCount(contact);
            if (recCount == 0) {
                return;
            }

            if (!chatHistoryExists(uin)) {
                newChatForm(contact, name);
            }
            ChatTextList chatForm = (ChatTextList) historyTable.get(uin);
            if (chatForm.getSize() != 0) {
                return;
            }

            int insSize = (recCount > MAX_HIST_LAST_MESS) ? MAX_HIST_LAST_MESS : recCount;
            CachedRecord rec;
            for (int i = recCount - insSize; i < recCount; i++) {
                try {
                    rec = HistoryStorage.getRecord(contact, i);
                    chatForm.addBigText
                            (
                                    "[" + rec.from + " " + rec.date + "]",
                                    ChatTextList.getInOutColor(rec.type == 0, false),
                                    Font.STYLE_PLAIN,
                                    -1
                            );
                    chatForm.doCRLF(-1);

                    //#sijapp cond.if modules_SMILES is "true" #
                    Emotions.addTextWithEmotions(chatForm, rec.text, Font.STYLE_PLAIN, CanvasEx.getColor(CanvasEx.COLOR_HIST_MESS), -1);
                    //#sijapp cond.else#
                    chatForm.addBigText(rec.text, CanvasEx.getColor(CanvasEx.COLOR_HIST_MESS), Font.STYLE_PLAIN, -1);
                    //#sijapp cond.end#
                    chatForm.doCRLF(-1);
                } catch (Exception ignored) {
                }
            }
        }
    }
    //#sijapp cond.end#

    public void contactRenamed(String uin, String newName) {
        ChatTextList temp = (ChatTextList) historyTable.get(uin);
        if (temp == null) {
            return;
        }
//		temp.ChatName = newName;
        UpdateCaption(uin);
    }

    public void UpdateCaption(String uin) {
        //calcCounter(uin);
        ChatTextList temp = (ChatTextList) historyTable.get(uin);
        // Calculate the title for the chatdisplay.
        String Title = (new StringBuffer())/*.append(" (").append(calcCounter(uin)).append("/")
                .append(historyTable.size()).append(")")*/.append(temp.getContact().name).toString();
        temp.setTitle(Title);
    }

    // Sets the counter for the ChatHistory
    private int calcCounter(String curUin) {
        if (curUin == null) {
            return 0;
        }
        Enumeration AllChats = historyTable.elements();
        Object chat = historyTable.get(curUin);
        int counter = 1;
        while (AllChats.hasMoreElements()) {
            if (AllChats.nextElement() == chat) {
                break;
            }
            counter++;
        }
        AllChats = null;
        return counter;
        //JimmUI.setLastUin(curUin); зачем!?
    }

    public boolean activateIfExists(ContactItem item) {
        if (item == null) {
            return false;
        }

        ChatTextList chat = getChatHistoryAt(item.getUinString());
        if (chat != null) {
            item.activate();
        }
        return (chat != null);
    }
}
