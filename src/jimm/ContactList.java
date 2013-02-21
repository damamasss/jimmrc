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
 File: src/jimm/ContactList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import DrawControls.*;
import jimm.comm.OtherAction;
import jimm.comm.XStatus;
import jimm.comm.SnacPacket;
import jimm.ui.*;
import jimm.util.ResourceBundle;
import jimm.util.Device;
//import jimm.plus.NewsMailRu;
//#sijapp cond.if modules_TUNER is "true"#
import jimm.plus.TunerRadio;
//#sijapp cond.end#
//#sijapp cond.if target is "RIM"#
import net.rim.device.api.system.LED;
//#sijapp cond.end#

import javax.microedition.lcdui.*;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////////////
public class ContactList extends VirtualTree implements CommandListener, VirtualTreeCommands, VirtualListCommands, SelectListener, MenuListener {

    //private final static byte MENU_KEYLOCK = (byte) 0;
    private final static byte MENU_CONNECT = (byte) 1;
    private final static byte MENU_DISCONNECT = (byte) 2;
    private final static byte MENU_STATUS = (byte) 3;
    //#sijapp cond.if modules_SOUNDS is "true"#
    private final static byte MENU_SOUND = (byte) 4;
    //#sijapp cond.end#
    private final static byte MENU_MANAGER = (byte) 5;
    //private final static byte MENU_MYSELF = (byte) 6;
    //#sijapp cond.if modules_MAGIC_EYE is "true"#
    private final static byte MENU_MAGICEYE = (byte) 7;
    //#sijapp cond.end#
    private final static byte MENU_OPTIONS = (byte) 8;
    private final static byte MENU_MINIMIZE = (byte) 9;
    private final static byte MENU_ABOUT = (byte) 10;
    //#sijapp cond.if target is "MIDP2"#
    private final static byte MENU_SMS_OR_CALL = (byte) 11;
    //#sijapp cond.end#
    private final static byte MENU_EXIT = (byte) 12;
    private final static byte MENU_PROFILES = (byte) 13;
    private final static byte MENU_ECONOMIC = (byte) 14;
    //#sijapp cond.if modules_TUNER is "true"#
    private final static byte MENU_RADIO = (byte) 15;
    //#sijapp cond.end#
    //private final static byte MENU_UPDATESSI = (byte) 16;
    //#sijapp cond.if modules_PARTNERS is "true"#
    private final static byte MENU_PARTNERS = (byte) 17;
    //#sijapp cond.end#

    final static public int SORT_BY_NAME = 1;
    final static public int SORT_BY_STATUS = 0;


    private final static Command cmdContactMenu = new Command(ResourceBundle.getString("function"), Command.ITEM, 1);
    /* Images for icons */
    public static ImageList imageList = ImageList.loadFull("icons.png");
    public static ImageList menuIcons = ImageList.loadFull("micons.png");
    public static ImageList clientIcons = ImageList.loadFull("clicons.png");
    public static ImageList privateIcons = ImageList.loadFull("prlists.png");
    public static ImageList pstatuses = ImageList.loadFull("pstatus.png");
    public static ImageList grIcons = ImageList.loadFull("gricons.png");
    public static Icon happyIcon = ImageList.loadFull("happy.png").elementAt(0);
    public static Icon authIcon = ImageList.loadFull("auth.png").elementAt(0);
    //public static ImageList formatIcons = ImageList.load("/format.png");

    private boolean treeBuilt = false;
    private Hashtable gNodes = new Hashtable();
    private Profile profile;
    private int onlineCounter;
    private int sortType;

    /* Constructor */
    public ContactList() {
        super(null, false, true);
        setVTCommands(this);
        setVLCommands(this);
        updateParamsCL();
// #sijapp cond.if target is "MIDP2" | target is "SIEMENS2"#
        setFontSize();
// #sijapp cond.end#
        setStepSize(0);
        setCommandListener(this);
    }

    public void setProfile(Profile profile) {
        System.gc();
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }

    public boolean contain(ContactItem cItem) {
        return (cItem.getProfile() == profile);
    }

    public static void updateIcons() {
        imageList = ImageList.loadFull("icons.png");
        menuIcons = ImageList.loadFull("micons.png");
        clientIcons = ImageList.loadFull("clicons.png");
        privateIcons = ImageList.loadFull("prlists.png");
        pstatuses = ImageList.loadFull("pstatus.png");
        grIcons = ImageList.loadFull("gricons.png");
        happyIcon = ImageList.loadFull("happy.png").elementAt(0);
        authIcon = ImageList.loadFull("auth.png").elementAt(0);
        XStatus.updateIcons();
        PopUp.updateIcons();
    }

    /* *************************************************** */
    public void setFontSize() {
        int fontSize = MEDIUM_FONT;
        switch (Options.getInt(Options.OPTION_FONT_SIZE_CL)) {
            case 0:
                fontSize = SMALL_FONT;
                break;
            case 1:
                fontSize = MEDIUM_FONT;
                break;
            case 2:
                fontSize = LARGE_FONT;
                break;
        }
        try {
            setFontSize(fontSize);
        } catch (Exception e) {
            setFontSize(SMALL_FONT);
        }

//        int userStyle = Options.getInt(Options.OPTION_USER_FONT);
//        int fontStyle;
//        switch (userStyle) {
//            case 1:
//                fontStyle = Font.STYLE_BOLD;
//                break;
//            case 2:
//                fontStyle = Font.STYLE_ITALIC;
//                break;
//            case 3:
//                fontStyle = Font.STYLE_UNDERLINED;
//                break;
//            default:
//                fontStyle = Font.STYLE_PLAIN;
//                break;
//        }
//        try {
//            setFontStyle(fontStyle);
//        } catch (Exception e) {
            setFontStyle(Font.STYLE_PLAIN);
//        }
    }

    public int vtCompareNodes(TreeNode node1, TreeNode node2) {
        Object obj1 = node1.getData();
        Object obj2 = node2.getData();
        ContactListItem item1 = (ContactListItem) obj1;
        ContactListItem item2 = (ContactListItem) obj2;
        int result = 0;

        switch (sortType) {
            case SORT_BY_NAME:
                result = item1.getSortText().compareTo(item2.getSortText());
                break;
            case SORT_BY_STATUS:
            case 2:
            case 3:
                int weight1 = item1.getSortWeight();
                int weight2 = item2.getSortWeight();
                if (weight1 == weight2) {
                    result = item1.getSortText().compareTo(item2.getSortText());
                } else {
                    result = (weight1 < weight2) ? -1 : 1;
                }
                break;
        }
        if (result == 0 && item1 instanceof ContactItem && item2 instanceof ContactItem) {
            result = ((ContactItem) item1).getUinString().compareTo(((ContactItem) item2).getUinString());
        }
        return result;
    }

    public int getItemsSize() {
        return profile.getItems(Profile.ITEMS_CITEMS).size();
    }

    private ContactItem getCItem(int index) {
        return (ContactItem) getItems(Profile.ITEMS_CITEMS).elementAt(index);
    }

    public void sizeChanged(int x, int y) {
        beforeShow();
    }

    public void beforeShow() {
        super.beforeShow();
        int type1 = VirtualList.MENU_TYPE_RIGHT_BAR;
        int type2 = VirtualList.MENU_TYPE_LEFT_BAR;
        addCommandEx(JimmUI.cmdMenu, type2);
        addCommandEx(cmdContactMenu, type1);
    }

    //public void reqSSI() {
    //    jimm.comm.SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_REQROSTER_FAMILY, SnacPacket.CLI_REQROSTER_COMMAND, 0x00000000, new byte[0], new byte[0]);
    //    try {
    //        profile.getIcq().sendPacket(reply2);
    //    } catch (JimmException ignored) {
    //    }
    //}

    protected void afterShow() {
        Jimm.setPrevScreen(this);
        TimerTask tt = new TimerTask() {
            public void run() {
                lock();
                boolean exception = false;
                do {
                    try {
                        rebuild();
                    } catch (Exception e) {
                        treeBuilt = false;
                        exception = true;
                    }
                } while (exception);
                unlock();
            }
        };
        Jimm.getTimerRef().schedule(tt, 10);
    }

    public void rebuild() {
        buildTree();
        sortAll();
        updateTitle();
    }

    public void updateTree() {
        lock();
        rebuild();
        unlock();
    }

    // is called by options form when options changed
    public void optionsChanged() {
        treeBuilt = false;
    }

    // called before jimm start to connect to server
    public void beforeConnect() {
        treeBuilt = false;
        //justConnected = true;
        clear();
        setStatusesOffline(true);
    }

    public void setStatusesOffline(boolean b) {
        //System.out.println("Set offlines: " + getProfile().getUin());
        if (onlineCounter == 0 && !b) {
            return;
        }
        lock();
        onlineCounter = 0;
        profile.setStatusesOffline();
        optionsChanged();
        buildTree();
        sortAll();
        unlock();
    }

    // public void setItems(Vector contacts, Vector groups) {
    // treeBuilt = false;
    // //ContactItem cItem;
    // //unreadMessCount = 0;
    // // for (int i = getItemsSize() - 1; i >= 0; i--) {
    // // cItem = getCItem(i);
    // // updateUnreadMessCount(cItem.getUnreadMessCount());
    // // //cItem.setBooleanValue(cItem.CONTACTITEM_HAS_CHAT, getProfile().getChatHistory().chatHistoryExists(cItem.getUinString()));
    // // }
    // }

    public void sendAll(boolean only4online, String message) {
        sendAll(only4online, message, getItems(Profile.ITEMS_CITEMS));
    }

    public void sendAll(boolean only4online, String message, Vector items) {
        if (items.size() == 0) {
            Jimm.back();
            return;
        }
        boolean toCItems = (items.elementAt(0) instanceof ContactItem);
        if (toCItems) {
            ContactItem cItem;
            for (int i = items.size() - 1; i >= 0; i--) {
                cItem = (ContactItem) items.elementAt(i);
                if ((only4online) && (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE)) {
                    continue;
                }
                if (message.length() != 0) {
                    try {
                        JimmUI.sendMessage(message, cItem);
                        Thread.sleep(200);
                    } catch (Exception e) {/*Do nothing*/
                    }
                }
            }
            activate();
        } else {
            sendAll(message, items);
        }
    }

    private void sendAll(String message, Vector gItems) {
        if (gItems.size() == 0) {
            return;
        }
        ContactItem[] cItems;
        for (int i = gItems.size() - 1; i >= 0; i--) {
            cItems = profile.getItemsFromGroup(((GroupItem) gItems.elementAt(i)).getId());
            for (int j = cItems.length - 1; j >= 0; j--) {
                if (message.length() != 0) {
                    try {
                        JimmUI.sendMessage(message, cItems[j]);
                        Thread.sleep(200);
                    } catch (Exception e) {/*Do nothing*/
                    }
                }
            }
        }
        activate();
    }

    //==================================//
    //                                  //
    //    WORKING WITH CONTACTS TREE    //
    //                                  //
    //==================================//

    // Sorts the contacts and calc online counters

    synchronized private void sortAll() {
        //if (treeSorted) return;
        sortType = Options.getInt(Options.OPTION_CL_SORT_BY);
        if (Options.getBoolean(Options.OPTION_USER_GROUPS)) {
            // Sort groups
            sortNode(null);

            // Sort contacts
            GroupItem gItem;
            TreeNode groupNode;
            for (int i = getItems(Profile.ITEMS_GITEMS).size() - 1; i >= 0; i--) {
                gItem = (GroupItem) getItems(Profile.ITEMS_GITEMS).elementAt(i);
                groupNode = (TreeNode) gNodes.get(new Integer(gItem.getId()));
                sortNode(groupNode);
                calcGroupData(groupNode, gItem);
            }
        } else {
            sortNode(null);
        }
        //treeSorted = true;
    }

    // Builds contacts tree (without sorting)
    synchronized private boolean buildTree() {
        int i, gCount, cCount;
        boolean use_groups = Options.getBoolean(Options.OPTION_USER_GROUPS);
        boolean only_online = !Options.getBoolean(Options.OPTION_SHOW_OFFLINE);

        cCount = getItemsSize();
        gCount = getItems(Profile.ITEMS_GITEMS).size();
        if (treeBuilt || ((cCount == 0) && (gCount == 0))) {
            return false;
        }

        clear();
        setShowButtons(use_groups);

        // add group nodes
        gNodes.clear();

        TreeNode groupNode;
        if (use_groups) {
            GroupItem item;
            for (i = 0; i < gCount; i++) {
                item = (GroupItem) getItems(Profile.ITEMS_GITEMS).elementAt(i);
                groupNode = addNode(null, item);
                groupNode.isROT();
                gNodes.put(new Integer(item.getId()), groupNode);
            }
        }

        // add contacts
        ContactItem cItem;
        for (i = 0; i < cCount; i++) {
            cItem = getCItem(i);

            if (only_online && (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE) && (!cItem.mustBeShownAnyWay())) {
                continue;
            }

            if (use_groups) {
                groupNode = (TreeNode) gNodes.get(new Integer(cItem.getIntValue(ContactItem.CONTACTITEM_GROUP)));
                addNode(groupNode, cItem);
            } else {
                addNode(null, cItem);
            }
        }

        //treeSorted = false;
        treeBuilt = true;
        return true;
    }

    public void updateCounters() {
        onlineCounter = 0;
        GroupItem gItem;
        for (int i = getItems(Profile.ITEMS_GITEMS).size() - 1; i >= 0; i--) {
            gItem = (GroupItem) getItems(Profile.ITEMS_GITEMS).elementAt(i);
            onlineCounter += gItem.getOnlineCount();
        }
    }

    // Calculates online/total values for group
    private void calcGroupData(TreeNode groupNode, GroupItem group) {
        if ((group == null) || (groupNode == null)) {
            return;
        }

        ContactItem cItem;
        int onlineCount = 0;

        int count = groupNode.size();
        for (int i = count - 1; i >= 0; i--) {
            if (!(groupNode.elementAt(i).getData() instanceof ContactItem)) {
                continue; // T O D O: must be removed
            }
            cItem = (ContactItem) groupNode.elementAt(i).getData();
            if (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE) {
                onlineCount++;
            }
        }
        group.setCounters(onlineCount, count);
        if (Options.getBoolean(Options.OPTION_CL_HIDE_EGROUPS)) {
            addOrDeleteGroupNode(groupNode);
        }
    }

    private void addOrDeleteGroupNode(TreeNode groupNode) {
        if (groupNode == getRoot() || groupNode == null) {
            return;
        }
        if (groupNode.size() == 0) {
            removeNode(groupNode);
        } else if (!nodeIsExist(getRoot(), groupNode)) {
            addNode(groupNode);
            sortNode(null);
        }
    }

    // Must be called after any changes in contacts
    public synchronized void contactChanged(ContactItem item, boolean setCurrent, boolean needSorting) {
        if (!treeBuilt) {
            return;
        }
        if (!contain(item)) {
            return;
        }

        boolean contactExistInTree = false,
                contactExistsInList,
                wasDeleted = false,
                haveToAdd = false,
                haveToDelete = false;
        TreeNode cItemNode = null;
        int i, count, groupId;

        int status = item.getIntValue(ContactItem.CONTACTITEM_STATUS);

        String uin = item.getUinString();

        // which group id ?
        groupId = item.getIntValue(ContactItem.CONTACTITEM_GROUP);

        boolean only_online = !Options.getBoolean(Options.OPTION_SHOW_OFFLINE);

        // Whitch group node?
        TreeNode groupNode = (TreeNode) gNodes.get(new Integer(groupId));
        if (groupNode == null) {
            groupNode = getRoot();
        }

        // Does contact exists in tree?
        count = groupNode.size();
        contactExistInTree = ((cItemNode = findCNode(groupNode, uin)) != null);

        // Does contact exists in internal list?
        contactExistsInList = (getItems(Profile.ITEMS_CITEMS).indexOf(item) != -1);

        // Lock tree repainting
        lock();

        haveToAdd = contactExistsInList && !contactExistInTree;
        if (only_online && !contactExistInTree) {
            haveToAdd |= ((status != ContactItem.STATUS_OFFLINE) | item.mustBeShownAnyWay());
        }

        haveToDelete = !contactExistsInList && contactExistInTree;
        if (only_online && contactExistInTree) {
            haveToDelete |= ((status == ContactItem.STATUS_OFFLINE) && !item.mustBeShownAnyWay());
        }


        if (haveToAdd && !contactExistInTree) {
            cItemNode = addNode(groupNode, item);
            if (Options.getBoolean(Options.OPTION_CL_HIDE_EGROUPS)) {
                addOrDeleteGroupNode(groupNode);
            }
        } else if (haveToDelete) {
            removeNode(cItemNode);
            if (Options.getBoolean(Options.OPTION_CL_HIDE_EGROUPS)) {
                addOrDeleteGroupNode(groupNode);
            }
            wasDeleted = true;
        }
        boolean exception;
        do {
            exception = false;
            try {
                if (needSorting && !wasDeleted) {
                    boolean isCurrent = (getCurrentItem() == cItemNode),
                            inserted = false;

                    deleteChild(groupNode, getIndexOfChild(groupNode, cItemNode));

                    int contCount = groupNode.size();
                    sortType = Options.getInt(Options.OPTION_CL_SORT_BY);

                    // T O D O: Make binary search instead of linear before child insertion!!!
                    TreeNode testNode;
                    for (int j = 0; j < groupNode.size(); j++) {
                        testNode = groupNode.elementAt(j);
                        if ((testNode == null) || (!(testNode.getData() instanceof ContactItem))) {
                            continue;
                        }
                        if (vtCompareNodes(cItemNode, testNode) < 0) {
                            insertChild(groupNode, cItemNode, j);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) {
                        insertChild(groupNode, cItemNode, contCount);
                    }
                    if (isCurrent && !setCurrent) {
                        setCurrentItem(cItemNode);
                    }
                }
            } catch (Exception e) {
                exception = true;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        } while (exception);

        // if set current
        if (setCurrent) {
            setCurrentItem(cItemNode);
        }
        // change status for chat (if exists)
        item.setStatusImage();

        // unlock tree and repaint
        unlock();
    }

    private TreeNode findCNode(TreeNode groupNode, String uin) {
        TreeNode cItemNode;
        synchronized (groupNode) {
            Object data;
            for (int i = groupNode.size() - 1; i >= 0; i--) {
                cItemNode = groupNode.elementAt(i);
                data = cItemNode.getData();
                if (!(data instanceof ContactItem)) {
                    continue;
                }
                if (!((ContactItem) data).getUinString().equals(uin)) {
                    continue;
                }
                return cItemNode;
            }
        }
        return null;
    }

// #sijapp cond.if (target isnot "DEFAULT" & modules_SOUNDS is "true")#

    public void playOfflineNotif(ContactItem cItem) {
        if (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE) {
            return;
        }
        Notify.playSoundNotification(Notify.SOUND_TYPE_OFFLINE, cItem.getExtraValue(ContactItem.EXTRA_SOUND));
        if (!contain(cItem)) {
            return;
        }
        cItem.blinkOnline();
        if (cItem.getUinString().equals(JimmUI.getLastUin())) {
            cItem.showTicker(cItem.name + " " + ResourceBundle.getEllipsisString("offline").toLowerCase(), true);
        }
        //if (isActive()) {
        //	String text = ResourceBundle.getString("offline").toLowerCase();
        //	JimmUI.showCapText(tree, cItem.getStringValue(ContactItem.CONTACTITEM_NAME) +
        //								" " + text, TimerTasks.TYPE_FLASH);
        //}
    }
// #sijapp cond.end#

    // Privacy Lists

    public synchronized void update(ContactItem item) {
        contactChanged(item, false, false);
    }

    public void statusChanged(ContactItem cItem, boolean wasOnline, boolean nowOnline, int tolalChanges) {
        if (!contain(cItem)) {
            return;
        }
        int groupId = cItem.getIntValue(ContactItem.CONTACTITEM_GROUP);
        boolean changed = (tolalChanges != 0);
        // Calc online counters
        if (wasOnline && !nowOnline) {
            onlineCounter--;
            changed = true;
            // #sijapp cond.if modules_MAGIC_EYE is "true" #
            if (MagicEye.getBooleanValue(MagicEye.OPTION_CITEM_OFFLINE)) {
                getProfile().getMagicEye().addAction(cItem.getUinString(), "contact_offline");
            }
            // #sijapp cond.end#
        }

        if (!wasOnline && nowOnline) {
            onlineCounter++;
            changed = true;
            // #sijapp cond.if modules_MAGIC_EYE is "true" #
            if (MagicEye.getBooleanValue(MagicEye.OPTION_CITEM_ONLINE)) {
                getProfile().getMagicEye().addAction(cItem.getUinString(), "contact_online");
            }
            // #sijapp cond.end#
        }

        if (changed) {
            if (Options.getBoolean(Options.OPTION_CL_HIDE_EGROUPS)) {
                addOrDeleteGroupNode((TreeNode) gNodes.get(new Integer(groupId)));
            }
            updateTitle();
        }
    }

    //Updates the title of the list
    private void updateTitle(long traffic, boolean showHeap) {
        int idx = -1;
        if (Options.uins.size() > 1) {
            idx = getProfile().getIndex();
        }
        setProfileIdx(idx);
        StringBuffer text = new StringBuffer();
        int unreadMessCount = Profiles.getUnreadMessCount();
        char sep = '-';
        ////char traff = (getProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC))?'E':'F';
        if (unreadMessCount > 0) {
            text.append('#').append(unreadMessCount).append(' ');
        } else {
            text.append(onlineCounter).append('/').append(getItemsSize());
            //if (traffic != 0) {
            //    text.append(sep);
            //}
        }
// #sijapp cond.if modules_HISTORY is "true" #
        HistoryStorage.clearCache();
// #sijapp cond.end#
        //if (traffic != 0) {
        ////text.append(traff);
        //    text.append(traffic).append(ResourceBundle.getString("kb"));
        //}
        setCaption(text.toString());
        setCapImage(imageList.elementAt(JimmUI.getStatusImageIndex(profile.getIcq().getCurrentStatus())));
        setCapXstImage(profile.getIcq().getCurrentXStatus());
        int width = NativeCanvas.getWidthEx();
        if (width > 128) {
            if (unreadMessCount > 0) {
                setCapPStImage(imageList.elementAt(14));
            } else {
                setCapPStImage(getPStatusImage());
            }
        }
    }

    public void updateTitle(boolean showHeap) {
        updateTitle(Traffic.getSessionTraffic(), showHeap);
    }

    public void updateTitle() {
        //updateTitle(true);
        updateTitle(false);
    }

    //#sijapp cond.if target is "MIDP2"#;
    public void pointerDragged(int x, int y) {
        if (y < getCapHeight()) {
            isDraggedWas = true;
            byte off = (byte) 0;
            if (x < lastPointerXCrd - getDrawWidth() * 3 / 5) {
                off = (byte) -1;
            } else if (x > lastPointerXCrd + getDrawWidth() * 3 / 5) {
                off = (byte) 1;
            }
            if (off != 0) {
                pointCaptionDragged(off);
                lastPointerXCrd = x;
            }
            return;
        }
        super.pointerDragged(x, y);
    }

    protected boolean pointCaptionDragged(byte off) {
        int size = Options.uins.size();
        if (size <= 1) {
            return false;
        }
        int idx = getProfile().getIndex();
        if ((idx += off) < 0) {
            idx = size - 1;
        }
        idx = idx % size;
        try {
            Profiles.setProfile(idx);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
//#sijapp cond.end#;

    public synchronized void removeContact(ContactItem citem) {
        if (!contain(citem)) {
            return;
        }
        citem.resetUnreadMessages();
        String uin = citem.getUinString();
        getProfile().getChatHistory().chatHistoryDelete(citem);

        if (contain(citem)) {
            TreeNode groupNode = (TreeNode) gNodes.get(new Integer(citem.getIntValue(ContactItem.CONTACTITEM_GROUP)));
            if (groupNode == null) {
                groupNode = getRoot();
            }
            TreeNode cItemNode = findCNode(groupNode, uin);
            if (cItemNode != null) {
                removeNode(cItemNode);
            }
            getItems(Profile.ITEMS_CITEMS).removeElement(citem);
            getProfile().statusChanged(citem, citem.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE, false, -1);
            activate();
            //try {
            //	Thread.sleep(1000);
            //} catch (Exception e) {
            //}
        }

        boolean wasDeleted = citem.getIcq().delFromContactList(citem);
//#sijapp cond.if modules_HISTORY is "true" #
        if (wasDeleted) {
            HistoryStorage.clearHistory(citem);
        }
//#sijapp cond.end#
    }

    // Adds a contact list item
    public void addContactItem(ContactItem cItem) {
        if (!contain(cItem)) {
            return;
        }
        // Update visual list
        contactChanged(cItem, true, true);
        // Update online counters
        getProfile().statusChanged(cItem, false, cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE, 1);
    }

    // Adds new group
    public void addGroup(GroupItem gItem) {
        if (!Options.getBoolean(Options.OPTION_USER_GROUPS)) {
            return;
        }
        TreeNode groupNode = addNode(null, gItem);
        gNodes.put(new Integer(gItem.getId()), groupNode);
    }

    // removes existing group
    public void removeGroup(int id) {
        Integer groupId = new Integer(id);
        if (Options.getBoolean(Options.OPTION_USER_GROUPS)) {
            TreeNode node = (TreeNode) gNodes.get(groupId);
            deleteChild(getRoot(), getIndexOfChild(getRoot(), node));
            gNodes.remove(groupId);
        }
    }

    /* Adds the given message to the message queue of the contact item identified by the given UIN */
    public void addMessage(ContactItem cItem) {
        if (!contain(cItem)) {
            return;
        }
        /* Update tree */
        contactChanged(cItem, Options.getBoolean(Options.OPTION_ON_MESS_FOCUS), true);
        updateTitle();
    }

    //#sijapp cond.if target isnot "DEFAULT"#
    public void TypingHelper(String uin, boolean type) {
        if (!getProfile().getChatHistory().chatHistoryShown(uin)) {
            repaint();
        }
    }

    //public void repaintTree() {
    //	if (isActive()) {
    //		repaint();
    //	}
    //}

    public void repaintTree(Graphics g) {
        updateTitle(false);
        paint(g);
    }
//#sijapp cond.end#

// #sijapp cond.if target isnot "DEFAULT"#

    // #sijapp cond.if modules_SOUNDS is "true"#

    public boolean changeSoundMode() {
        boolean newValue = !Options.getBoolean(Options.OPTION_SILENT_MODE);
        Options.setBoolean(Options.OPTION_SILENT_MODE, newValue);
        //if (Options.getBoolean(Options.OPTION_SOUND_VIBRA)) {
        //    Options.setInt(Options.OPTION_VIBRATOR, newValue ? 1 : 0);
        //}
        Options.safe_save();
        //Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.SAVE_OPTPRO), 25);
        return newValue;
    }

    public static Icon getSoundPicture() {
        return (Options.getBoolean(Options.OPTION_SILENT_MODE) ? menuIcons.elementAt(16) : menuIcons.elementAt(15));
    }

    static private String getSoundValue() {
        return ResourceBundle.getString(Options.getBoolean(Options.OPTION_SILENT_MODE) ? "sound_on" : "sound_off");
    }
// #sijapp cond.end#
// #sijapp cond.end#

    public boolean changeEconomicMode() {
        boolean newValue = !getProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC);
        //Options.setBoolean(Options.OPTION_AECONOMIC_TRAFFIC, newValue);
        getProfile().setBoolean(Profile.OPTION_ECONOM_TRAFFIC, newValue);
        if (newValue) {
            getProfile().setBoolean(Profile.OPTION_DELIVERY_REPORT, false);
            getProfile().setBoolean(Profile.OPTION_MESS_NOTIF_TYPE, true);
//#sijapp cond.if target isnot "DEFAULT"#
            Options.setInt(Options.OPTION_TYPING_MODE, 0);
//#sijapp cond.end#
        } else {
            getProfile().setBoolean(Profile.OPTION_DELIVERY_REPORT, true);
            getProfile().setBoolean(Profile.OPTION_MESS_NOTIF_TYPE, false);
//#sijapp cond.if target isnot "DEFAULT"#
            Options.setInt(Options.OPTION_TYPING_MODE, 1);
//#sijapp cond.end#
        }
        Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.SAVE_OPTPRO), 25);
        if (getProfile().getIcq().isConnected()) {
            try {
                getProfile().getIcq().sendPacket(OtherAction.getStandartUserInfoPacket(getProfile().getIcq()));
            } catch (Exception ignored) {
            }
        }
        return newValue;
    }

    public static Icon getEconomicPicture() {
        return (Jimm.getCurrentProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC) ? menuIcons.elementAt(18) : menuIcons.elementAt(17));
    }

    public static String getEconomicValue() {
        return Jimm.getCurrentProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC) ? "econom_off" : "econom_on";
    }

    public Icon getPStatusImage() {
        return pstatuses.elementAt(getProfile().getInt(Profile.OPTION_PSTATUS) - 1);
    }

    public Icon getXStatusImage() {
        return XStatus.getStatusImage(getProfile().getInt(Profile.OPTION_XSTATUS));
    }

    public Icon getStatusImage() {
        int imageIndex = JimmUI.getStatusImageIndex(getProfile().getInt(Profile.OPTION_ONLINE_STATUS));
        return imageList.elementAt(imageIndex);
    }

    ContactItem lastChatItem = null;

    private static boolean authIconsFlag = false;
    public static boolean blinkText = false, blinkIcon = false;

    public void updateParamsCL() {
        authIconsFlag = Options.getBoolean(Options.OPTION_AUTH_ICON);
        blinkText = Options.getBoolean(Options.OPTION_ONLINE_BLINK_NICK);
        blinkIcon = Options.getBoolean(Options.OPTION_ONLINE_BLINK_ICON);
        updateParamsVT();
    }

    public void VTGetItemDrawData(TreeNode src, ListItem dst) {
        ContactListItem item = (ContactListItem) src.getData();
        dst.text = item.getText();
        dst.color = item.getTextColor();
        int imgIndex;

        if (item instanceof ContactItem) {
            ContactItem cItem = (ContactItem) item;

            if (blinkText) {
                dst.fontStyle = cItem.getFontStyle();
            }
            //else dst.fontStyle = Font.STYLE_PLAIN;
            imgIndex = item.getStatusImageIndex();
            dst.image = imageList.elementAt(imgIndex);
            dst.isMessage = (imgIndex == 14);

            dst.XStatusImg = cItem.getXStatusImage();
            //if (clientIconsFlag) {
            dst.ClientImg = cItem.getClientImage();
            //}
            //else dst.ClientImg = null;
            dst.PrivateImg = cItem.getPrivateImage();
            dst.HappyImg = cItem.getHappyImage();
            if (authIconsFlag) {
                dst.AuthImg = cItem.getAuthImage();
            }
            //else dst.AuthImg = null;
            if (imgIndex == 14) {
                dst.unreadMessCount = cItem.getUnreadMessCount();
            }
        } else {
            imgIndex = 1;
            if (!src.getExpanded()) {
                imgIndex = 0;
                dst.AuthImg = item.getAuthImage();
            }
            if (src.size() == 0) imgIndex = 2;
            dst.image = grIcons.elementAt(imgIndex);
        }
    }

    public int vtGetItemHeight() {
        return imageList.getHeight();
    }

    //<------------------------------------------------>//
    private final static byte DELETE_CHAT = 0;
    private final static byte DELETE_CONTACT = (byte) 1;

    public void selectAction(int action, int selectType, Object o) {
        if (selectType == Select.SELECT_OK) {
            switch (action) {
                case DELETE_CHAT:
                    getProfile().getChatHistory().chatHistoryDelete((ContactItem) o);
                    break;

                case DELETE_CONTACT:
                    removeContact((ContactItem) o);
                    return;
            }
        }
        //activate();
    }
    //<------------------------------------------------>//

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
        activateListOrChat();
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        TreeNode node = getCurrentItem();
        Object obj = (node == null) ? null : node.getData();
        ContactItem item = ((obj != null) && (obj instanceof ContactItem)) ? (ContactItem) obj : null;

// #sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
        if (type == KEY_RELEASED) {
            if ((keyCode != Canvas.KEY_NUM6) && (keyCode != Canvas.KEY_NUM4)) {
                int step = 1;
                if ((node != null) && (node.getROT())) {
                    //step = getItemsSize();
                    step = getVisCount();
                }
                switch (gameAct) {
                    case Canvas.RIGHT:
                        move(step);
                        return;
                    case Canvas.LEFT:
                        move(-step);
                        return;
                }
            }
        }
        if (type == KEY_RELEASED) {
// #sijapp cond.if target is "MIDP2"#
            if ((keyCode == -8) && (item != null)) {
                if (!item.getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT)) {
                    Jimm.setDisplay(new Select(ResourceBundle.getString("remove"),
                            ResourceBundle.getString("remove") + " " + item.name + "?",
                            this, DELETE_CONTACT, item
                    )
                    );
                } else {
                    Jimm.setDisplay(new Select(ResourceBundle.getString("delete_chat"),
                            ResourceBundle.getString("delete_chat") + " " + item.name + "?",
                            this, DELETE_CHAT, item
                    )
                    );
                }
                return;
            }
// #sijapp cond.end#            
        }
// #sijapp cond.end#
        if (!isActive()) {
            return;
        }
        JimmUI.execHotKey(item, keyCode, type);
    }

    // shows next or previos chat
    public String showNextPrevChat(boolean next) {
        int index = getItems(Profile.ITEMS_CITEMS).indexOf(lastChatItem);
        if (index == -1) {
            return null;
        }
        int di = next ? 1 : -1;
        int maxSize = getItemsSize();
        ContactItem cItem;

        for (int i = index + di; ; i += di) {
            if (i < 0) {
                i = maxSize - 1;
            }
            if (i >= maxSize) {
                i = 0;
            }
            if (i == index) {
                break;
            }

            cItem = getCItem(i);
            if (cItem.getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT)) {
                lastChatItem = cItem;
                cItem.activate(false);
                JimmUI.setLastUin(lastChatItem.getUinString());
                return cItem.getUinString();
            }
        }
        return null;
    }

    // private int unreadMessCount = 0;
    // // Returns number of unread messages
    // private int getUnreadMessCount() {
// //		int count = cItems.size();
// //		int result = 0;
// //		for (int i = count - 1; i >= 0; i--) result += getCItem(i).getUnreadMessCount();
    // return unreadMessCount;
    // }

    // public void updateUnreadMessCount(int i0) {
    // unreadMessCount += i0;
    // unreadMessCount = Math.max(unreadMessCount, 0);
    // }

    private Vector getItems(int type) {
        return profile.getItems(type);
    }

    // Command listener
    public void commandAction(Command c, Displayable d) {
        // Build main menu
        if (c == JimmUI.cmdMenu) {
            showMenu(true);
        } else if (c == cmdContactMenu) {
            Object node = (getCurrentItem() == null) ? null : getCurrentItem().getData();
            showFunctiontMenu(node);
//            if (node instanceof GroupItem) {
//                Jimm.setDisplay(new ManageCList(getProfile().getIcq()));
//            } else {
//                showFunctiontMenu(node);
//                if (node instanceof ContactItem || node instanceof GroupItem) {
//                    showFunctiontMenu(node);
//                } else {
//                    showFunctiontMenu(null);
//                }
//            }
//            if (node instanceof ContactItem) {
//                showContactMenu((ContactItem) node);
//            } else if (node instanceof GroupItem) {
//                Jimm.setDisplay(new ManageCList(getProfile().getIcq()));
//            } else {
//                Jimm.setDisplay(new Alert(null, ResourceBundle.getString("send_citems"), null, AlertType.INFO));
//            }
        }
    }

    public void showFunctiontMenu(Object contact) {
//        if (contact == null) {
//            return;
//        }
        Menu menu = new Menu(this, (byte) 1);
        //JimmUI.fillContactMenu(contact, menu);
        JimmUI.fillFunctionMenu(contact, menu);
        Jimm.setDisplay(menu);
    }

    public void showMenu(boolean ani) {
        try {
            Jimm.setDisplay(buildMenu(ani));
        } catch (Exception e) {
            //System.out.println("Not open menu");
        }
    }

    public Menu buildMenu(boolean animate) {
        updateTitle();
        Menu menu = new Menu(this, animate);
        menu.setPriority(getPriority() + 1);
        boolean connected = getProfile().getIcq().isConnected();
        boolean connectIsActive = (getProfile().connectionIsActive());
        if (connected) {
            //menu.addMenuItem("keylock_enable", menuIcons.elementAt(0), MENU_KEYLOCK);
            menu.addMenuItem("disconnect", menuIcons.elementAt(18), MENU_DISCONNECT);
        } else if (!connectIsActive) {
            menu.addMenuItem("connect", menuIcons.elementAt(17), MENU_CONNECT);
        }
        menu.addMenuItem("set_status", MENU_STATUS, new Icon[]{getStatusImage(), getXStatusImage(), getPStatusImage()});
//#sijapp cond.if modules_SOUNDS is "true"#
        //menu.addMenuItem(getSoundValue(), getSoundPicture(), MENU_SOUND);
//#sijapp cond.end#
        if (connected) {
            //menu.addMenuItem("manage_contact_list", menuIcons.elementAt(1), MENU_MANAGER);
            //menu.addMenuItem("myself", menuIcons.elementAt(9), MENU_MYSELF);
        }
//#sijapp cond.if modules_MAGIC_EYE is "true"#
        //if (getProfile().getBoolean(Profile.OPTION_ENABLE_MM)) {
        //    menu.addMenuItem("magic_eye", menuIcons.elementAt(33), MENU_MAGICEYE);
        //}
//#sijapp cond.end#
        //if (connected) {
        //    menu.addMenuItem("update_ssi", menuIcons.elementAt(25), MENU_UPDATESSI);
        //}
        //if (Options.uins.size() > 1) {
        menu.addMenuItem("profiles", menuIcons.elementAt(12), MENU_PROFILES);
        //}
        //if (!connectIsActive) {
        menu.addMenuItem("options_lng", menuIcons.elementAt(3), MENU_OPTIONS);
        //}
        //#sijapp cond.if modules_PARTNERS is "true"#
        menu.addMenuItem("partners", menuIcons.elementAt(10), MENU_PARTNERS);
        //#sijapp cond.end#
        menu.addMenuItem(getEconomicValue(), getEconomicPicture(), MENU_ECONOMIC);
        //#sijapp cond.if modules_TUNER is "true"#
        menu.addMenuItem("radio", XStatus.getStatusImage(14), MENU_RADIO);
        //#sijapp cond.end#
        menu.addMenuItem("about", menuIcons.elementAt(4), MENU_ABOUT);
//#sijapp cond.if target is "MIDP2" #
        if (Jimm.is_phone_SE()) {
            menu.addMenuItem("minimize", menuIcons.elementAt(27), MENU_MINIMIZE);
        }
// #sijapp cond.end #
//#sijapp cond.if target is "MIDP2"#
        if (!(Jimm.is_smart_NOKIA() || Jimm.is_phone_SE())) {
            menu.addMenuItem("make_call", XStatus.getStatusImage(14), MENU_SMS_OR_CALL);
        }
//#sijapp cond.end#
        menu.addMenuItem("exit", menuIcons.elementAt(28), MENU_EXIT);
        menu.setMenuListener(this);
        Jimm.setPrevScreen(menu);
        menu.setCurrent(lastMenuIdx);
        menu.setCurrentTop(lastMenuTop);
        return menu;
    }


    public void captionPressed(int index) {
        if (index < 0) {
            return;
        }
        switch (index) {
            case 5:
            case 6:
            case 8:
                menuSelect(null, MENU_STATUS);
                break;
//#sijapp cond.if modules_SOUNDS is "true"#
            case 7:
                menuSelect(null, MENU_SOUND);
                break;
//#sijapp cond.end#
            //case 9: case 10: case 11:
            //Profiles.setProfile(index - 9);
            //break;
            default:
                return;
        }
        repaint();
    }

//    public void menuBarPressed(int index) {
//        if (index == -1) {
//            return;
//        }
//        int keyCode;
//        TreeNode node = getCurrentItem();
//        Object obj = (node == null) ? null : node.getData();
//        ContactItem item = ((obj != null) && (obj instanceof ContactItem)) ? (ContactItem) obj : null;
//
//        switch (index) {
//            case 0:
//                keyCode = Canvas.KEY_NUM0;
//                break;
//            case 1:
//                keyCode = Canvas.KEY_NUM4;
//                break;
//            case 2:
//                keyCode = Canvas.KEY_NUM6;
//                break;
//            case 3:
//                keyCode = Canvas.KEY_STAR;
//                break;
//            case 4:
//                keyCode = Canvas.KEY_POUND;
//                break;
//            default:
//                return;
//        }
//        JimmUI.execHotKey(item, keyCode, KEY_RELEASED);
//        JimmUI.execHotKey(item, keyCode, KEY_PRESSED);
//        repaint();
//    }

    /*protected void sizeChanged(int w, int h) {
            int poles = Options.getInt(Options.OPTION_POLES);
            boolean need = ((w > h) && poles < 2) || ((w < h) && poles > 1);
            if (need) {
                Options.setInt(Options.OPTION_POLES,(w > h)?2:1);
            }
    }*/

    public boolean connect() {
        return getProfile().connect();
    }

    private int lastMenuIdx = 0;
    private int lastMenuTop = 0;

    //private static int les;

    public void menuSelect(Menu menu, byte action) {
        if (menu != null) {
            lastMenuIdx = menu.getCurrIndex();
            lastMenuTop = menu.getCurrentTop();
        }
        switch (action) {
            case MENU_CONNECT:
                beforeConnect();
                if (connect()) {
                    menu.back();
                }
                break;

            case MENU_DISCONNECT:
                setCapImage(imageList.elementAt(JimmUI.getStatusImageIndex(ContactItem.STATUS_OFFLINE)));
                getProfile().disconnect();
                menu.back();
                break;

            /* case MENU_KEYLOCK:
            Jimm.getSplashCanvasRef().lockJimm(getProfile().getIcq());
            break;*/

            case MENU_STATUS:
                (new jimm.forms.StatusesForm(menu, getProfile().getIcq())).activate();
                //Jimm.setDisplay(new StatusesMenu(this, StatusesMenu.STATE_MAIN, getProfile().getIcq()));
                break;

            case MENU_MANAGER:
                Jimm.setDisplay(new ManageCList(getProfile().getIcq()));
                break;

//            case MENU_MYSELF:
//                (new jimm.info.UserInfo()).requiestUserInfo(getProfile(), getProfile().getUin(), "");
//                break;

//#sijapp cond.if modules_SOUNDS is "true"#
            case MENU_SOUND:
                changeSoundMode();
                if (menu != null) {
                    showMenu(true);
                }
                break;
//#sijapp cond.end#

            //case MENU_UPDATESSI: // todo вылет из сети
            //    reqSSI();
            //    menu.back();
            //    break;

            case MENU_PROFILES:
                Profiles.showProfiles(this);
                break;

            case MENU_OPTIONS:
                Options.editOptions(profile.getIcq());
                break;

            //#sijapp cond.if modules_PARTNERS is "true"#
            case MENU_PARTNERS:
                Jimm.setDisplay(new TradeClass());
                break;
            //#sijapp cond.end#

            case MENU_ABOUT:
                new AboutForm(menu);
                break;

            case MENU_EXIT:
                //if (menu != null) {
                //    menu.back();
                //}
                Jimm.doExit(false);
                break;
//#sijapp cond.if modules_MAGIC_EYE is "true"#
            case MENU_MAGICEYE:
                getProfile().getMagicEye().activateEx(menu);
                break;
//#sijapp cond.end#

            case MENU_ECONOMIC:
                changeEconomicMode();
                if (menu != null) {
                    showMenu(true);
                }
                break;
            //#sijapp cond.if modules_TUNER is "true"#
            case MENU_RADIO:
                try {
                    TunerRadio.tunerStart();
                } catch (Exception ignored) {
                }
                break;
            //#sijapp cond.end#

            case MENU_MINIMIZE:
//#sijapp cond.if target is "MIDP2"#
                Jimm.setMinimized(true);
// #sijapp cond.end #
//#sijapp cond.if target is "SIEMENS2"#
//                try {
//                    if (Jimm.is_SGOLD2()) {
//                        Jimm.jimm.platformRequest("tel://NAT_CONTACTS_LIST");
//                    }
//                    Jimm.jimm.platformRequest(Jimm.strMenuCall);
//                } catch (Exception exc1) {
//                }
// #sijapp cond.end #
                break;

//#sijapp cond.if target is "MIDP2"#
            case MENU_SMS_OR_CALL:
                (new SmsOrCall()).show();
                break;
//#sijapp cond.end#
        }
    }

    private void activateListOrChat() {
        TreeNode node = getCurrentItem();

        if (node == null) {
            return;
        }
        ContactListItem item = (ContactListItem) node.getData();
        synchronized (item) {
            if (item instanceof ContactItem) {
                // Activate the contact item menu
//#sijapp cond.if target is "RIM"#
                LED.setState(LED.STATE_OFF);
//#sijapp cond.end#
                lastChatItem = (ContactItem) item;
                lastChatItem.activate();
                JimmUI.setLastUin(lastChatItem.getUinString());
            }

            if ((item instanceof GroupItem) && (node.size() > 0)) {
                setExpandFlag(node, !node.getExpanded());
            }
        }
    }
}