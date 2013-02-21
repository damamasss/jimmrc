package jimm.ui;

import DrawControls.Icon;
import DrawControls.CanvasEx;
import jimm.ContactItem;
import jimm.ContactList;
import jimm.Jimm;
import jimm.Profile;
import jimm.chat.ChatTextList;

import java.util.Enumeration;
import java.util.Hashtable;

public class ChatList extends SelectList {
    /**
     * @author Rishat Shamsutdinov
     */
    public ChatList(CanvasEx prvScr, Profile profile) {
        super(prvScr, null);
        Hashtable chatTable = profile.getChatHistory().getChatTable();
        int size = chatTable.size();
        if (size == 0) {
            return;
        }
        ContactItem chats[] = new ContactItem[size];
        Enumeration chatsEnu = chatTable.elements();
        Icon status, xstatus;
        for (int i = 0; i < size; i++) {
            chats[i] = ((ChatTextList) chatsEnu.nextElement()).getContact();
            status = ContactList.imageList.elementAt(chats[i].getImageIndexWithMess());
            xstatus = chats[i].getXStatusImage();
            if (status != null) {
                if (xstatus != null) {
                    addMenuItem(chats[i].getText(), (byte) 0, new Icon[]{status, xstatus});
                } else {
                    addMenuItem(chats[i].getText(), status, (byte) 0);
                }
            } else if (xstatus != null) {
                addMenuItem(chats[i].getText(), xstatus, (byte) 0);
            } else {
                addMenuItem(chats[i].getText(), (byte) 0);
            }
        }
        objects = chats;
        Jimm.setDisplay(this);
    }

    protected void select() {
        ((ContactItem) objects[getCurrIndex()]).activate();
    }

    protected void clearAction() {
        Jimm.getCurrentProfile().getChatHistory().chatHistoryDelete((ContactItem) objects[getCurrIndex()]);
        if (Jimm.getCurrentProfile().getChatHistory().getChatTable().size() == 0) {
            if (getPrvScreen() instanceof ContactList)
                back();
            else
                Jimm.setDisplay(Jimm.getContactList());
        } else {
            if (getPrvScreen() instanceof ContactList)
                new ChatList(getPrvScreen(), Jimm.getCurrentProfile());
            else
                new ChatList(Jimm.getContactList(), Jimm.getCurrentProfile());
        }
    }
}