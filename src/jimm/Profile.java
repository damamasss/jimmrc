package jimm;

import DrawControls.Icon;
import DrawControls.NativeCanvas;
import jimm.chat.ChatHistory;
import jimm.chat.ChatTextList;
import jimm.comm.*;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Profile {
    /**
     * @author Rishat Shamsutdinov
     */
    public static final byte ITEMS_CITEMS = (byte) 2;
    public static final byte ITEMS_GITEMS = (byte) 1;

    public static final byte OPTION_ONLINE_STATUS = 0; // max - 0xFFFFFFFF;
    public static final byte OPTION_PSTATUS = 1; // max - 8
    public static final byte OPTION_XSTATUS = 2; // max - 64
    public static final byte OPTION_CLIENT_ID = 3; // max - 32

    public static final byte OPTION_WEBAWARE = 1 << 0;
    public static final byte OPTION_REQ_AUTH = 1 << 1;
    public static final byte OPTION_ENABLE_MM = 1 << 2;
    public static final byte OPTION_XTRAZ_ENABLE = 1 << 3;
    public static final byte OPTION_DELIVERY_REPORT = 1 << 4;
    public static final byte OPTION_MESS_NOTIF_TYPE = 1 << 5;
    public static final byte OPTION_ECONOM_TRAFFIC = 1 << 6;

    public static final byte OPTION_STATUS_MESSAGE_AWAY = 0; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_DND = 1; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_NA = 2; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_OCCUPIED = 3; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_EVIL = 4; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_DEPRESSION = 5; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_HOME = 6; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_WORK = 7; /* String  */
    public static final byte OPTION_STATUS_MESSAGE_LUNCH = 8; /* String  */
    public static final byte OPTION_XTRAZ_TITLE = 9; /* String  */
    public static final byte OPTION_XTRAZ_MESSAGE = 10; /* String  */
    public static final byte OPTION_STRING_VERSION = 11; /* String  */

    private int booleanValues;
    private long intValues;
    private final String statStrings[] = new String[12];

    private Icq icq;
    private final Hashtable actions = new Hashtable();
    // #sijapp cond.if modules_MAGIC_EYE is "true" #
    private MagicEye magicEye;
    // #sijapp cond.end#
    private ChatHistory chatHistory;
    private String uin;
    private String nick;
    //private String password;

    /* Version id numbers */
    private int ssiListLastChangeTime = -1;
    private int ssiNumberOfItems = 0;

    private Vector cItems;
    private Vector gItems;
    private int unreadMessCount = 0;

    public Profile(String nick) {
        //DebugLog.addText("Create profile: " + uin);
        //this.uin = uin;
        this.nick = nick;
        //this.password = password;
        try {
            load();
        } catch (Exception ignored) {
        }
        try {
            loadOptions();
        } catch (Exception e) {
            //jimm.DebugLog.addText("Loading exception: " + e.toString() + " " + e.getMessage());
            setInt(OPTION_ONLINE_STATUS, ContactItem.STATUS_ONLINE);
            setInt(OPTION_CLIENT_ID, 0);
            setInt(OPTION_PSTATUS, 3);
            setInt(OPTION_XSTATUS, XStatus.XSTATUS_NONE);
            booleanValues = 0x0C; //001100
            setString(OPTION_STATUS_MESSAGE_AWAY, ResourceBundle.getString("status_message_text_away"));
            setString(OPTION_STATUS_MESSAGE_DND, ResourceBundle.getString("status_message_text_dnd"));
            setString(OPTION_STATUS_MESSAGE_NA, ResourceBundle.getString("status_message_text_na"));
            setString(OPTION_STATUS_MESSAGE_OCCUPIED, ResourceBundle.getString("status_message_text_occ"));
            setString(OPTION_STATUS_MESSAGE_EVIL, ResourceBundle.getString("status_message_text_evil"));
            setString(OPTION_STATUS_MESSAGE_DEPRESSION, ResourceBundle.getString("status_message_text_depress"));
            setString(OPTION_STATUS_MESSAGE_HOME, ResourceBundle.getString("status_message_text_home"));
            setString(OPTION_STATUS_MESSAGE_WORK, ResourceBundle.getString("status_message_text_work"));
            setString(OPTION_STATUS_MESSAGE_LUNCH, ResourceBundle.getString("status_message_text_lunch"));
            setString(OPTION_STRING_VERSION, "###VERSION###");
        }
    }

    public void tryToDestroy(String nextNick) {
        if (nick.equals(nextNick)) {
            return;
        }
        //NativeCanvas.hideLPCanvas();
        if ((chatHistory != null) && (chatHistory.getChatTable().size() == 0)) {
            chatHistory = null;
        }
// #sijapp cond.if modules_MAGIC_EYE is "true" #
        if ((magicEye != null) && (magicEye.getSize() == 0)) {
            magicEye = null;
        }
// #sijapp cond.end#
        if ((icq != null && !connectionIsActive()) && (!icq.isConnected())) {
            icq.disconnect(false);
            icq = null;
            //System.out.println("[" + getUin() + "]Destroying icq ...");
        }
        boolean empty = (icq == null && chatHistory == null);
// #sijapp cond.if modules_MAGIC_EYE is "true" #
        empty = (empty && magicEye == null);
// #sijapp cond.end#
        if (getUnreadMessCount() == 0 && empty) {
            //System.out.println("[" + getUin() + "]Destroying profile ...");
            Profiles.removeProfile(getNick());
        }
    }

    public void activate(String prevNick) {
        if (nick.equals(prevNick)) {
            return;
        }
        Jimm.getContactList().setProfile(this);
        NativeCanvas.hideLPCanvas();
        Jimm.getContactList().optionsChanged();
        if (getIcq().isConnected()) {
            Jimm.getContactList().clear();
        } else {
            Jimm.getContactList().beforeConnect();
        }
        Jimm.getContactList().rebuild();
        Jimm.getContactList().updateCounters();
        Jimm.getContactList().activate();
        if (actions.size() > 0) {
            showAction(actions.keys().nextElement());
        }
    }

    public boolean isCurrent() {
        return (Jimm.getCurrentProfile() == this);
    }

    public void addAction(String str, Action act) {
        actions.put(act, new Object[]{str, act});
        if (isCurrent()) {
            showAction(act);
        }
    }

    public void addAction(String str, int prg, Object key) {
        NativeCanvas.hideLPCanvas();
        addAction(str, prg, null, key);
    }

    public void addAction(String str, int prg, CommandListener listener, Object key) {
        actions.put(key, new Object[]{str, new Integer(prg), listener});
        if (isCurrent()) {
            showAction(key);
        }
    }

    public void addAction(String str, int prg, CommandListener listener, Object key, boolean line) {
        actions.put(key, new Object[]{str, new Integer(prg), listener, new Boolean(line)});
        if (isCurrent()) {
            showAction(key);
        }
    }

    private void showAction(Object key) {
        if (actions.size() == 0 || key == null) {
            return;
        }
        synchronized (actions) {
            Object action[] = (Object[]) actions.get(key);
            int len = action.length;
            if (len == 2) {
                NativeCanvas.getLPCanvas().addTimerTask((String) action[0], (Action) action[1]);
            } else if (len == 3) {
                NativeCanvas.getLPCanvas().setCmdListener((CommandListener) action[2]);
                NativeCanvas.getLPCanvas().setMessage((String) action[0]);
                NativeCanvas.getLPCanvas().setProgress(((Integer) action[1]).intValue(), false);
            } else if (len == 4) {
                NativeCanvas.getLPCanvas().setCmdListener((CommandListener) action[2]);
                NativeCanvas.getLPCanvas().setMessage((String) action[0]);
                NativeCanvas.getLPCanvas().setProgress(((Integer) action[1]).intValue(), ((Boolean) action[3]).booleanValue());
            }
        }
    }

    //#sijapp cond.if modules_PANEL is "true"#
    public void showPopupItem(String name, Icon icon, ContactItem contact) {
        if (Jimm.locked()) {
            return;
        }
//        boolean haveToShow = false;
//        boolean chatVisible = getChatHistory().chatHistoryShown(uin);
//        boolean uinEquals = uin.equals(JimmUI.getLastUin());
//        int pwType = Options.getInt(Options.OPTION_PANEL_ACTIVE);
//        switch (pwType) {
//            case 0:
//                return;
//            case 1:
//                haveToShow = (!chatVisible) && uinEquals;
//                break;
//            case 3:
//                haveToShow = true;
//                break;
//            case 2:
//            case 4:
//                haveToShow = !chatVisible || !uinEquals;
//                break;
//        }
//        if (contact != null) {
//            haveToShow &= (pwType != 2 || Jimm.getContactList().contain(contact));
//            haveToShow &= (pwType != 3 || !Jimm.getContactList().contain(contact));
//        }
//        haveToShow &= (Jimm.getDisplay().getCurrent() instanceof NativeCanvas);
//
//        if (!haveToShow) {
//            return;
//        }
        if (Options.getInt(Options.OPTION_PANEL_ACTIVE) == 0) return;
        NativeCanvas.addAction(name, icon);
    }
    //#sijapp cond.end#

    public void actionCompleted(Object key) {
        actionCompleted(key, false);
    }

    public void actionCompleted(Object key, boolean destroy) {
        if (key == null) {
            return;
        }
        boolean current = (isCurrent() && (key.equals(NativeCanvas.getLPAction()) || !destroy));
        if (current) {
            if (destroy) {
                NativeCanvas.getLPCanvas().resetTimerTask();
            } else {
                //NativeCanvas.getLPCanvas().stopMove();
                NativeCanvas.hideLPCanvas();
            }
        }
        //System.out.println("[" + getUin() + "]Destroying action ...");
        actions.remove(key);
        if (current && actions.size() > 0) {
            showAction(actions.keys().nextElement());
        }
    }

    public void removeAllActions() {
        actions.clear();
        NativeCanvas.getLPCanvas().resetTimerTask();
        NativeCanvas.hideLPCanvas();
    }

    public boolean connectionIsActive() {
        return icq != null && icq.connectionIsActive();
    }

    public String getNick() {
        if (nick.length() == 0) {
            nick = Options.getString(Options.OPTION_MY_NICK);
            //return ((String) Options.nicks.elementAt(getIndex()));
        }
        return nick;
    }

//    public void setNick(String n) {
//        nick = n;
//    }

    public String getUin() {
        if (uin == null || uin.length() == 0 /*|| !uin.equals(Options.getString(Options.OPTION_UIN))*/) {
            //uin = Options.getString(Options.OPTION_UIN);
            uin = (String) Options.uins.elementAt(getIndex());
        }
        return uin;
    }

    public String getPassword() {
        return ((String) Options.passwords.elementAt(getIndex()));
    }

    public int getIndex() {
        return Options.nicks.indexOf(getNick());
    }

    private String newPass;

    public void changePassword(String pass) {
        if (pass != null) {
            newPass = pass;
            //return;
        }
        if (newPass == null) {
            return;
        }
        Options.changePassword(newPass, getIndex());
        newPass = null;
    }

    // #sijapp cond.if modules_MAGIC_EYE is "true" #
    public MagicEye getMagicEye() {
        if (magicEye == null) {
            magicEye = new MagicEye(this);
        }
        return magicEye;
    }
// #sijapp cond.end#

    public ChatHistory getChatHistory() {
        if (chatHistory == null) {
            chatHistory = new ChatHistory(this);
        }
        return chatHistory;
    }

    public void setColorSchemes() {
        if (chatHistory != null) {
            chatHistory.setColorScheme();
        }
// #sijapp cond.if modules_MAGIC_EYE is "true" #
        if (magicEye != null) {
            magicEye.setColorScheme();
        }
// #sijapp cond.end #
    }

    public void loadContacts(DataInputStream dis) throws Exception {
        if (cItems == null) return;
        ssiListLastChangeTime = dis.readInt();
        ssiNumberOfItems = dis.readUnsignedShort();
        int count = ssiNumberOfItems;
        while (count > 0) {
            byte type = dis.readByte();
            if (type == 0) {
                int uin = dis.readInt();
                for (int i = 0; i < cItems.size(); i++) {
                    ContactItem ci = (ContactItem) cItems.elementAt(i);
                    if (ci.getUIN() == uin) {
                        ci.setExtraValues(dis.readByte());
                        break;
                    }
                }
            }
            count--;
        }
        safeSave();
    }

    public void load() throws Exception {
        if (cItems != null) {
            return;
        }
        cItems = new Vector();
        gItems = new Vector();

        String[] recordStores = RecordStore.listRecordStores();
        boolean exist = false;
        for (int i = recordStores.length - 1; i >= 0; i--) {
            if (recordStores[i].equals("contactlist_" + nick)) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            throw (new Exception());
        }

        RecordStore cl = RecordStore.openRecordStore("contactlist_" + nick, false);

        try {
            byte[] buf;
            ByteArrayInputStream bais;
            DataInputStream dis;

            buf = cl.getRecord(1);
            bais = new ByteArrayInputStream(buf);
            dis = new DataInputStream(bais);
            if (!(dis.readUTF().equals(Jimm.getVersion()))) {
                throw (new IOException());
            }
            try {
                bais.close();
                dis.close();
            } catch (Exception ignored) {
            }
            buf = cl.getRecord(2);
            bais = new ByteArrayInputStream(buf);
            dis = new DataInputStream(bais);
            ssiListLastChangeTime = dis.readInt();
            ssiNumberOfItems = dis.readUnsignedShort();

            int marker = 3;

            ContactItem ci;
            GroupItem gi;
            while (marker <= cl.getNumRecords()) {
                try {
                    bais.close();
                    dis.close();
                } catch (Exception ignored) {
                }
                buf = cl.getRecord(marker++);
                bais = new ByteArrayInputStream(buf);
                dis = new DataInputStream(bais);

                while (dis.available() > 0) {
                    byte type = dis.readByte();
                    if (type == 0) {
                        ci = new ContactItem(this);
                        ci.loadFromStream(dis);
                        cItems.addElement(ci);
                    } else if (type == 1) {
                        gi = new GroupItem();
                        gi.loadFromStream(dis);
                        gItems.addElement(gi);
                    }
                }
            }
            try {
                bais.close();
                dis.close();
            } catch (Exception ignored) {
            }
        } finally {
            cl.closeRecordStore();
        }
        updateHappyFlags();
    }

    public void safeSave() {
        saveOptions();
        try {
            save();
        } catch (Exception ignored) {
        }
    }

    public void saveContacts(DataOutputStream dos) throws IOException, RecordStoreException {
        dos.writeInt(ssiListLastChangeTime);
        dos.writeShort((short) ssiNumberOfItems);
        int cItemsCount = cItems.size();
        for (int i = 0; i < cItemsCount; i++) {
            getCItem(i).saveToStreamExtra(dos);
        }
    }

    private void save() throws IOException, RecordStoreException {
        try {
            RecordStore.deleteRecordStore("contactlist_" + nick);
        } catch (RecordStoreNotFoundException ignored) {
        }
        //System.out.println("!!!");            
        RecordStore cl = RecordStore.openRecordStore("contactlist_" + nick, true);

        ByteArrayOutputStream baos;
        DataOutputStream dos;
        byte[] buf;

        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        dos.writeUTF(Jimm.getVersion());
        buf = baos.toByteArray();
        cl.addRecord(buf, 0, buf.length);

        baos.reset();
        dos.writeInt(ssiListLastChangeTime);
        dos.writeShort((short) ssiNumberOfItems);
        buf = baos.toByteArray();
        cl.addRecord(buf, 0, buf.length);

        baos.reset();


        int cItemsCount = cItems.size();
        int totalCount = cItemsCount + gItems.size();
        GroupItem gItem;
        for (int i = 0; i < totalCount; i++) {
            if (i < cItemsCount) {
                getCItem(i).saveToStream(dos);
            } else {
                gItem = (GroupItem) gItems.elementAt(i - cItemsCount);
                gItem.saveToStream(dos);
            }

            // Start new record if it exceeds 4000 bytes
            if ((baos.size() >= 4000) || (i == totalCount - 1)) {
                buf = baos.toByteArray();
                cl.addRecord(buf, 0, buf.length);
                baos.reset();
            }
        }
        cl.closeRecordStore();
    }

    public int getSsiListLastChangeTime() {
        return ssiListLastChangeTime;
    }

    public int getSsiNumberOfItems() {
        return ssiNumberOfItems;
    }

    public Icq getIcq() {
        if (icq == null) {
            icq = new Icq(this);
        }
        return icq;
    }

    public void disconnect() {
        if (icq != null) {
            icq.disconnect();
        }
    }

    public boolean connect() {
        if ((getUin().length() == 0) || (getPassword().length() == 0)) {
            Options.showAccount(getIcq());
            return false;
        }
        getIcq().reconnect_attempts = Options.getInt(Options.OPTION_RECONNECT_NUMBER);
        getIcq().connect();
        return true;
    }

    public void setItems(int timestamp, int numberOfItems, Vector contacts, Vector groups) {
        ssiListLastChangeTime = timestamp;
        ssiNumberOfItems = numberOfItems;
        cItems = contacts;
        gItems = groups;
        if (isCurrent()) {
            Jimm.getContactList().optionsChanged();
        }
    }

    public int getUnreadMessCount() {
        return unreadMessCount;
    }

    public void updateUnreadMessCount() {
        int count = getItemsSize();
        unreadMessCount = 0;
        for (int i = count - 1; i >= 0; i--) {
            unreadMessCount += getCItem(i).getUnreadMessCount();
        }
        Object obj = Jimm.getCurrentDisplay();
        if (obj instanceof jimm.ui.ProfileList) {
            ((jimm.ui.ProfileList) obj).update();
        }
    }

    private ContactItem getCItem(int index) {
        return (ContactItem) cItems.elementAt(index);
    }

    public ContactItem getItemByUIN(String uin) {
        int uinInt = Integer.parseInt(uin);
        ContactItem citem;
        for (int i = cItems.size() - 1; i >= 0; i--) {
            citem = getCItem(i);
            if (citem.getUIN() == uinInt) {
                return citem;
            }
        }
        return null;
    }

    public GroupItem getGroupById(int id) {
        synchronized (gItems) {
            GroupItem group;
            for (int i = gItems.size() - 1; i >= 0; i--) {
                group = (GroupItem) gItems.elementAt(i);
                if (group.getId() == id) {
                    return group;
                }
            }
        }
        return null;
    }

    synchronized public ContactItem[] getContactItems() {
        ContactItem[] cItems_ = new ContactItem[cItems.size()];
        cItems.copyInto(cItems_);
        return (cItems_);
    }


    public GroupItem[] getGroupItems() {
        synchronized (this) {
            GroupItem[] gItems_ = new GroupItem[gItems.size()];
            gItems.copyInto(gItems_);
            return (gItems_);
        }
    }

    public String[] getTempContacts() {
        Vector data = new Vector();
        for (int i = cItems.size() - 1; i >= 0; i--) {
            if (getCItem(i).getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) && !getCItem(i).getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH)) {
                data.addElement(getCItem(i).getUinString());
            }
        }
        String result[] = new String[data.size()];
        data.copyInto(result);
        return result;
    }

    public ContactItem[] getItemsFromGroup(int groupId) {
        Vector vect = new Vector();
        ContactItem cItem;
        for (int i = cItems.size() - 1; i >= 0; i--) {
            cItem = getCItem(i);
            if (cItem.getIntValue(ContactItem.CONTACTITEM_GROUP) == groupId) {
                vect.addElement(cItem);
            }
        }

        ContactItem[] result = new ContactItem[vect.size()];
        vect.copyInto(result);

        return result;
    }

    public Vector getItems(int type) {
        return (type == ITEMS_CITEMS) ? cItems : gItems;
    }

    public int getItemsSize() {
        return cItems.size();
    }

    public synchronized ContactItem createTempContact(String uin) {
        ContactItem cItem = getItemByUIN(uin);
        if (cItem != null) {
            return cItem;
        }

        try {
            cItem = new ContactItem(0, 0, uin, uin, false, false, this);
        } catch (Exception e) {
            return null; // Message from non-icq contact
        }
        cItems.addElement(cItem);
        cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, true);
        return cItem;
    }

    public synchronized void removeContactItem(ContactItem cItem) {
        cItems.removeElement(cItem);
        safeSave();
    }

    public synchronized void addContactItemPrc(ContactItem cItem) {
        ContactItem oldItem = getItemByUIN(cItem.getUinString());
        int lastUnknownStatus = ContactItem.STATUS_NONE;
        if (oldItem != null) {
            removeContactItem(oldItem);
            lastUnknownStatus = oldItem.getIntValue(ContactItem.CONTACTITEM_STATUS);
        }

        cItems.addElement(cItem);
        cItem.setBooleanValue(ContactItem.CONTACTITEM_ADDED, true);
        cItem.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, getChatHistory().chatHistoryExists(cItem.getUinString()));
        if (cItem.getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT)) {
            getChatHistory().getChatHistoryAt(cItem.getUinString()).setContact(cItem);
        }

        if (lastUnknownStatus != ContactItem.STATUS_NONE) {
            cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, lastUnknownStatus);
            //lastUnknownStatus = ContactItem.STATUS_NONE;
        }
        safeSave();
    }

    public synchronized void addContactItem(ContactItem cItem) {
        if (!cItem.getBooleanValue(ContactItem.CONTACTITEM_ADDED)) {
            addContactItemPrc(cItem);
        }
        Jimm.getContactList().addContactItem(cItem);
    }

    public void addGroup(GroupItem gItem) {
        synchronized (gItems) {
            gItems.addElement(gItem);
            safeSave();
        }
        if (isCurrent()) {
            Jimm.getContactList().addGroup(gItem);
        }
    }

    public void removeGroup(GroupItem gItem) {
        synchronized (this) {
            gItems.removeElement(gItem);
            safeSave();
        }
        if (isCurrent()) {
            Jimm.getContactList().removeGroup(gItem.getId());
        }
    }

    public synchronized ContactItem addMessage(Message message, boolean haveToBeep) {
        String uin = message.getSndrUin();
        ContactItem cItem = getItemByUIN(uin);
        if (cItem == null) {
            cItem = createTempContact(uin);
        }
        if (cItem == null) {
            return null;
        }
        try {
            getChatHistory().getChat(cItem).addMessage(message);
        } catch (Exception ignored) {
        }
        Jimm.getContactList().addMessage(cItem);

        Jimm.messageAvailable();

// #sijapp cond.if target isnot "DEFAULT"#
        if (haveToBeep) {
            Notify.vibrate(cItem.getExtraValue(ContactItem.EXTRA_VIBRA));
// #sijapp cond.if  modules_SOUNDS is "true"#
            int status = getInt(OPTION_ONLINE_STATUS);
            //if (status == ContactItem.STATUS_NA || status == ContactItem.STATUS_OCCUPIED) {
            if (status != ContactItem.STATUS_NA && status != ContactItem.STATUS_OCCUPIED) {
                Notify.playSoundNotification(Notify.SOUND_TYPE_MESSAGE, cItem.getExtraValue(ContactItem.EXTRA_SOUND));
            }
// #sijapp cond.end#
        }
// #sijapp cond.end #
        cItem.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, true);
        return cItem;
    }

    public void statusChanged(ContactItem cItem, boolean wasOnline, boolean nowOnline, int totalChanges) {
        int groupId = cItem.getIntValue(ContactItem.CONTACTITEM_GROUP);
        GroupItem group = getGroupById(groupId);
        if (wasOnline && !nowOnline) {
            if (group != null) {
                group.updateCounters(-1, 0);
            }
        }
        if (!wasOnline && nowOnline) {
            if (group != null) {
                group.updateCounters(1, 0);
            }
        }
        if (group != null) {
            group.updateCounters(0, totalChanges);
        }
        Jimm.getContactList().statusChanged(cItem, wasOnline, nowOnline, totalChanges);
    }

    public ContactItem BeginTyping(String uin, boolean type) {
        ContactItem item = getItemByUIN(uin);
        if (item == null) {
            if (!Options.getBoolean(Options.OPTION_ANTISPAM_ENABLE)) {
                item = createTempContact(uin);
            }
        }

        if (item == null) {
            return null;
        }
        if (!item.hasCapability(ClientID.CAPF_TYPING)) {
            item.addCapability(ClientID.CAPF_TYPING);
        }
        item.BeginTyping(type);
//#sijapp cond.if modules_SOUNDS is "true"#
        if (type) {
            Notify.playSoundNotification(Notify.SOUND_TYPE_TYPING, item.getExtraValue(ContactItem.EXTRA_SOUND));
        }
//#sijapp cond.end#
        //#sijapp cond.if modules_PANEL is "true"#
        if (type) {
            showPopupItem(item.name, ContactList.imageList.elementAt(18), item);
        }
        //#sijapp cond.end#
        if (isCurrent()) {
            ChatTextList chat = getChatHistory().getChatHistoryAt(uin);
            if (chat != null) {
                chat.BeginTyping(type);
            }
            Jimm.getContactList().TypingHelper(uin, type);
        }
        return item;
    }

    public void resetAutoAnsweredFlag() {
        long currStatus = getInt(OPTION_ONLINE_STATUS);
        ContactItem item;
        for (int i = cItems.size() - 1; i >= 0; i--) {
            item = getCItem(i);
            item.autoAnswered = !((currStatus == ContactItem.STATUS_NA) || (currStatus == ContactItem.STATUS_OCCUPIED) ||
                    (currStatus == ContactItem.STATUS_DND) || (currStatus == ContactItem.STATUS_AWAY));
        }
    }

    public void setStatusesOffline() {
        ContactItem item;
        for (int i = getItemsSize() - 1; i >= 0; i--) {
            item = getCItem(i);
            item.setOfflineStatus();
        }
        for (int i = getItems(Profile.ITEMS_GITEMS).size() - 1; i >= 0; i--) {
            ((GroupItem) getItems(Profile.ITEMS_GITEMS).elementAt(i)).setCounters(0, 0);
        }
    }

    public synchronized void updateHappyFlags() {
        for (int i = getItemsSize() - 1; i >= 0; i--) {
            getCItem(i).updateHappyFlag();
        }
    }

    //////////////
    /// Options ///
    //////////////
    public synchronized int getInt(byte key) {
        switch (key) {
            case OPTION_ONLINE_STATUS:
                return (int) (intValues);
            case OPTION_XSTATUS:
                return (int) ((intValues >> 32) & 0xFF);
            case OPTION_PSTATUS:
                return (int) ((intValues >> 40) & 0xFF);
            case OPTION_CLIENT_ID:
                return (int) ((intValues >> 48) & 0xFF);
        }
        return 0;
    }

    public synchronized boolean getBoolean(byte key) {
        return ((booleanValues & key) != 0);
    }

    public synchronized String getString(byte key) {
        return StringConvertor.getString(statStrings[key]);
    }

    public synchronized void setInt(byte key, int ivalue) {
        long value = ivalue;
        //System.out.println("Before["+ key + "]: 0x" + Long.toString(intValues, 16));
        switch (key) {
            case OPTION_ONLINE_STATUS:
                intValues = (intValues & 0xFFFFFFFF00000000L) | value;
                break;
            case OPTION_XSTATUS:
                intValues = (intValues & 0xFFFFFF00FFFFFFFFL) | ((value & 0xFF) << 32);
                break;
            case OPTION_PSTATUS:
                intValues = (intValues & 0xFFFF00FFFFFFFFFFL) | ((value & 0xFF) << 40);
                break;
            case OPTION_CLIENT_ID:
                intValues = (intValues & 0xFF00FFFFFFFFFFFFL) | ((value & 0xFF) << 48);
                break;
        }
        //System.out.println("After[" + key + "]: 0x" + Long.toString(intValues, 16));
    }

    public synchronized void setBoolean(byte key, boolean value) {
        booleanValues = (booleanValues & (~key)) | (value ? key : 0x00000000);
    }

    public synchronized void setString(byte key, String value) {
        statStrings[key] = value;
    }

    public void saveOptions() {
        try {
            RecordStore rs = RecordStore.openRecordStore("optpro_" + getNick(), true);
            byte[] buf;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(intValues);
            dos.writeInt(booleanValues);
            int len = statStrings.length;
            for (int i = 0; i < len; i++) {
                dos.writeUTF(StringConvertor.getString(statStrings[i]));
            }
            buf = baos.toByteArray();
            if (rs.getNumRecords() == 0) {
                rs.addRecord(buf, 0, buf.length);
            } else {
                rs.setRecord(1, buf, 0, buf.length);
            }
            rs.closeRecordStore();
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
            //jimm.DebugLog.addText("Saving exception: " + e.toString() + " " + e.getMessage());
        }
    }

    private void loadOptions() throws Exception {
        RecordStore rs = RecordStore.openRecordStore("optpro_" + getNick(), false);
        byte[] buf = rs.getRecord(1);
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        DataInputStream dis = new DataInputStream(bais);

        intValues = dis.readLong();
        booleanValues = dis.readInt();
        int i = 0;
        while (dis.available() > 0) {
            statStrings[i++] = dis.readUTF();
        }
        try {
            bais.close();
            dis.close();
        } catch (Exception ignored) {
        }
        rs.closeRecordStore();
    }
}