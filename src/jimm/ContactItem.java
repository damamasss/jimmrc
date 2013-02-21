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
 File: src/jimm/ContactItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import DrawControls.CanvasEx;
import DrawControls.Icon;
import DrawControls.NativeCanvas;
import DrawControls.VirtualList;
import jimm.chat.ChatTextList;
import jimm.comm.*;
//#sijapp cond.if modules_FILES="true"#
import jimm.files.FileTransfer;
//#sijapp cond.end#
import jimm.ui.PopUp;
import jimm.ui.SliderTask;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/* T O D : remove UI code to ChatHistory */
public class ContactItem implements ContactListItem {
    /* Status (all are mutual exclusive) */
    public static final int STATUS_AWAY = 0x00000001;
    public static final int STATUS_DND = 0x00000002;
    public static final int STATUS_NA = 0x00000004;
    public static final int STATUS_OCCUPIED = 0x00000010;
    public static final int STATUS_CHAT = 0x00000020;
    public static final int STATUS_INVISIBLE = 0x00000100;
    public static final int STATUS_INVIS_ALL = 0x00000200;
    public static final int STATUS_EVIL = 0x00003000;
    public static final int STATUS_DEPRESSION = 0x00004000;
    public static final int STATUS_HOME = 0x00005000;
    public static final int STATUS_WORK = 0x00006000;
    public static final int STATUS_LUNCH = 0x00002001;
    public static final int STATUS_OFFLINE = 0xFFFFFFFF;
    public static final int STATUS_NONE = 0x10000000;
    public static final int STATUS_ONLINE = 0x00000000;

    /* No capability */
    public static final int CAP_NO_INTERNAL = 0x00000000;

    /* Message types */
    public static final int MESSAGE_PLAIN = 1;
    public static final int MESSAGE_URL = 2;
    public static final int MESSAGE_SYS_NOTICE = 3;
    public static final int MESSAGE_AUTH_REQUEST = 4;

    private int idAndGropup,
            caps,
            idle,
            booleanValues,
            messCounters = 0,
            unreadMessCount = 0;

    private byte extraValues = 0;

    private int typeAndClientId,
            portAndProt,
            intIP,
            extIP,
            authCookie;

    private int regdata,
            signOn,
            status,
            statusImageIndex,
            clientImageIndex;

    private String clientVersion,
            offlineTime,
            lowerText,
            modelPhone;

    private String notes = "";

    public String name, uinString;

    private Profile profile;

    public boolean readXtraz,
            autoAnswered,
            openChat,
            readStatusMess,
            moodChanged;

    public byte[] capabilities = null;

    public int protocol;

    public int dc1,
            dc2,
            dc3;


    public void setStringValue(int key, String value) {
        switch (key) {
            case CONTACTITEM_UIN:
                uinString = value;
                return;
            case CONTACTITEM_NAME:
                name = value;
                lowerText = null;
                return;
            case CONTACTITEM_CLIVERSION:
                clientVersion = value;
                return;
            case CONTACTITEM_OFFLINETIME:
                offlineTime = value;
                return;
            case CONTACTITEM_MODEL_PHONE:
                modelPhone = value;
                return;
            case CONTACTITEM_NOTES:
                notes = value;
        }
    }

    public String getStringValue(int key) {
        switch (key) {
            case CONTACTITEM_UIN:
                return uinString;
            case CONTACTITEM_NAME:
                return name;
            case CONTACTITEM_CLIVERSION:
                return clientVersion;
            case CONTACTITEM_OFFLINETIME:
                return offlineTime;
            case CONTACTITEM_MODEL_PHONE:
                return modelPhone;
            case CONTACTITEM_NOTES:
                return notes;
        }
        return null;
    }


    public String getSortText() {
        return getLowerText();
    }

    public int getSortWeight() {
        int status = getIntValue(CONTACTITEM_STATUS);

        if (isMessageAvailable(MESSAGE_PLAIN)) {
            return 1;
        }
        if ((getBooleanValue(CONTACTITEM_IS_TEMP) || getBooleanValue(CONTACTITEM_PHANTOM)) && (status == STATUS_OFFLINE)) {
            return 20;
        }

        if (Options.getInt(Options.OPTION_CL_SORT_BY) > 1) {
            if (Options.getInt(Options.OPTION_CL_SORT_BY) == 3
                    && getBooleanValue(CONTACTITEM_HAS_CHAT)
                    && status != STATUS_OFFLINE) {
                return 2;
            }
            if (status == STATUS_CHAT) {
                return 3;
            }
            if (status == STATUS_ONLINE) {
                return 4;
            }
            if (status == STATUS_INVISIBLE) {
                return 5;
            }
            if (status == STATUS_HOME) {
                return 6;
            }
            if (status == STATUS_WORK) {
                return 7;
            }
            if (status == STATUS_EVIL) {
                return 8;
            }
            if (status == STATUS_DEPRESSION) {
                return 9;
            }
            if (status == STATUS_OCCUPIED) {
                return 10;
            }
            if (status == STATUS_DND) {
                return 11;
            }
            if (status == STATUS_LUNCH) {
                return 12;
            }
            if (status == STATUS_AWAY) {
                return 13;
            }
            if (status == STATUS_NA) {
                return 14;
            }
        } else if (status != STATUS_OFFLINE) {
            return 5;
        }

        return 15;
    }


    public void setIntValue(int key, int value) {
        switch (key) {
            case CONTACTITEM_ID:
                idAndGropup = (idAndGropup & 0x0000FFFF) | (value << 16);
                return;

            case CONTACTITEM_GROUP:
                idAndGropup = (idAndGropup & 0xFFFF0000) | value;
                return;

            case CONTACTITEM_PLAINMESSAGES:
                messCounters = (messCounters & 0x00FFFFFF) | (value << 24);
                return;

            case CONTACTITEM_URLMESSAGES:
                messCounters = (messCounters & 0xFF00FFFF) | (value << 16);
                return;

            case CONTACTITEM_SYSNOTICES:
                messCounters = (messCounters & 0xFFFF00FF) | (value << 8);
                return;

            case CONTACTITEM_AUTREQUESTS:
                messCounters = (messCounters & 0xFFFFFF00) | value;
                return;

            case CONTACTITEM_IDLE:
                idle = value;
                return;
            case CONTACTITEM_CAPABILITIES:
                caps = value;
                return;
            case CONTACTITEM_STATUS:
                status = value;
                statusImageIndex = JimmUI.getStatusImageIndex(value);
                return;
            case CONTACTITEM_DC_TYPE:
                typeAndClientId = (typeAndClientId & 0xff) | ((value & 0xff) << 8);
                return;
            case CONTACTITEM_ICQ_PROT:
                portAndProt = (portAndProt & 0xffff0000) | (value & 0xffff);
                return;
            case CONTACTITEM_DC_PORT:
                portAndProt = (portAndProt & 0xffff) | ((value & 0xffff) << 16);
                return;
            case CONTACTITEM_CLIENT_IMAGE:
                typeAndClientId = (typeAndClientId & 0xff00) | (value & 0xff);
                clientImageIndex = (typeAndClientId & 0xff) - 1;
                return;
            case CONTACTITEM_AUTH_COOKIE:
                authCookie = value;
                return;
            case CONTACTITEM_SIGNON:
                signOn = value;
                return;
            case CONTACTITEM_REGDATA:
                regdata = value;
                return;
        }
    }

    public int getIntValue(int key) {
        switch (key) {
            case CONTACTITEM_ID:
                return ((idAndGropup & 0xFFFF0000) >> 16) & 0xFFFF;
            case CONTACTITEM_GROUP:
                return (idAndGropup & 0x0000FFFF);
            case CONTACTITEM_PLAINMESSAGES:
                return ((messCounters & 0xFF000000) >> 24) & 0xFF;
            case CONTACTITEM_URLMESSAGES:
                return ((messCounters & 0x00FF0000) >> 16) & 0xFF;
            case CONTACTITEM_SYSNOTICES:
                return ((messCounters & 0x0000FF00) >> 8) & 0xFF;
            case CONTACTITEM_AUTREQUESTS:
                return (messCounters & 0x000000FF);
            case CONTACTITEM_IDLE:
                return idle;
            case CONTACTITEM_CAPABILITIES:
                return caps;
            case CONTACTITEM_STATUS:
                return status;
            case CONTACTITEM_DC_TYPE:
                return ((typeAndClientId & 0xff00) >> 8) & 0xFF;
            case CONTACTITEM_ICQ_PROT:
                return portAndProt & 0xffff;
            case CONTACTITEM_DC_PORT:
                return ((portAndProt & 0xffff0000) >> 16) & 0xFFFF;
            case CONTACTITEM_CLIENT_IMAGE:
                return typeAndClientId & 0xff;
            case CONTACTITEM_AUTH_COOKIE:
                return authCookie;
            case CONTACTITEM_SIGNON:
                return signOn;
            case CONTACTITEM_REGDATA:
                return regdata;
        }
        return 0;
    }


    public void setBooleanValue(int key, boolean value) {
        booleanValues = (booleanValues & (~key)) | (value ? key : 0x00000000);
    }

    public boolean getBooleanValue(int key) {
        return (booleanValues & key) != 0;
    }

    public byte getExtraValues() {
        return extraValues;
    }

    public void setExtraValues(byte vals) {
        extraValues = vals;
    }

    public boolean getExtraValue(int key) {
        return (extraValues & key) != 0;
    }


    public static byte[] longIPToByteAray(int value) {
        if (value == 0) return null;
        return new byte[]
                {
                        (byte) (value & 0x000000FF),
                        (byte) ((value & 0x0000FF00) >> 8),
                        (byte) ((value & 0x00FF0000) >> 16),
                        (byte) ((value & 0xFF000000) >> 24)
                };
    }

    public static int arrayToLongIP(byte[] array) {
        if ((array == null) || (array.length < 4)) return 0;
        return (int) array[0] & 0xFF |
                (((int) array[1] & 0xFF) << 8) |
                (((int) array[2] & 0xFF) << 16) |
                (((int) array[3] & 0xFF) << 24);
    }

    public void setIPValue(int key, byte[] value) {
        switch (key) {
            case CONTACTITEM_INTERNAL_IP:
                intIP = arrayToLongIP(value);
                break;
            case CONTACTITEM_EXTERNAL_IP:
                extIP = arrayToLongIP(value);
                break;
        }
    }

    public byte[] getIPValue(int key) {
        switch (key) {
            case CONTACTITEM_INTERNAL_IP:
                return longIPToByteAray(intIP);
            case CONTACTITEM_EXTERNAL_IP:
                return longIPToByteAray(extIP);
        }
        return null;
    }

    private String birthDay;

    public boolean rememberBirthDay(String BDay) {
        birthDay = BDay;
        getProfile().safeSave();
        updateHappyFlag();
        return true;
    }

    public void saveToStreamExtra(DataOutputStream stream) throws IOException {
        stream.writeByte(0);
        stream.writeInt(getUIN());
        stream.writeByte(extraValues);
    }

    public void saveToStream(DataOutputStream stream) throws IOException {
        if (!getBooleanValue(CONTACTITEM_PHANTOM)) {
            stream.writeByte(0);
            stream.writeInt(idAndGropup);
            stream.writeByte(booleanValues & (CONTACTITEM_IS_TEMP | CONTACTITEM_NO_AUTH));
            stream.writeByte(extraValues);
            stream.writeInt(getIntValue(CONTACTITEM_PLAINMESSAGES));
            stream.writeInt(getUIN());
            stream.writeUTF(name);
            stream.writeUTF(StringConvertor.getString(birthDay));
            stream.writeUTF(notes);

            // Privacy lists
            stream.writeInt(getVisibleId());
            stream.writeInt(getInvisibleId());
            stream.writeInt(getIgnoreId());
        }
    }

    public void loadFromStream(DataInputStream stream) throws IOException {
        idAndGropup = stream.readInt();
        booleanValues = stream.readByte();
        extraValues = stream.readByte();
//#sijapp cond.if modules_HISTORY is "true"#
        if (Options.getBoolean(Options.OPTION_SHOW_LAST_MESS)) setIntValue(CONTACTITEM_PLAINMESSAGES, stream.readInt());
        else
//#sijapp cond.end#
            stream.readInt();
        unreadMessCount = getIntValue(CONTACTITEM_PLAINMESSAGES);
        uinString = Integer.toString(stream.readInt());
        name = stream.readUTF();
        birthDay = stream.readUTF();
        notes = stream.readUTF();
        updateHappyFlag();

        // Privacy lists
        setVisibleId(stream.readInt());
        setInvisibleId(stream.readInt());
        setIgnoreId(stream.readInt());
    }

    /* Extra keys */
    public static final int EXTRA_WATCH = 1 << 0;
    public static final int EXTRA_WAIT = 1 << 1;
    public static final int EXTRA_SOUND = 1 << 2;
    public static final int EXTRA_VIBRA = 1 << 3;
    public static final int EXTRA_STATUS = 1 << 4;
    public static final int EXTRA_XTRAZ = 1 << 5;
    public static final int EXTRA_AUTOX = 1 << 6;
    public static final int EXTRA_HIST = 1 << 7;

    public static final int EXTRA_SIZE = 8;

    /* Variable keys */
    public static final int CONTACTITEM_UIN = 0;      /* String */
    public static final int CONTACTITEM_NAME = 1;      /* String */
    public static final int CONTACTITEM_CLIVERSION = 2;      /* String  */
    public static final int CONTACTITEM_OFFLINETIME = 3;      /* String  */
    public static final int CONTACTITEM_MODEL_PHONE = 4;      /* String  */
    public static final int CONTACTITEM_NOTES = 5;      /* String  */

    public static final int CONTACTITEM_ID = 0;     /* Integer */
    public static final int CONTACTITEM_GROUP = 1;     /* Integer */
    public static final int CONTACTITEM_PLAINMESSAGES = 2;     /* Integer */
    public static final int CONTACTITEM_URLMESSAGES = 3;     /* Integer */
    public static final int CONTACTITEM_SYSNOTICES = 4;     /* Integer */
    public static final int CONTACTITEM_AUTREQUESTS = 5;     /* Integer */
    public static final int CONTACTITEM_IDLE = 6;     /* Integer */
    public static final int CONTACTITEM_STATUS = 7;    /* Integer */
    public static final int CONTACTITEM_SIGNON = 8;    /* Integer */
    public static final int CONTACTITEM_REGDATA = 9;    /* Integer */
    public static final int CONTACTITEM_AUTH_COOKIE = 10;    /* Integer */
    public static final int CONTACTITEM_DC_TYPE = 11;     /* Integer */
    public static final int CONTACTITEM_ICQ_PROT = 12;     /* Integer */
    public static final int CONTACTITEM_DC_PORT = 13;     /* Integer */
    public static final int CONTACTITEM_CAPABILITIES = 14;     /* Integer */
    public static final int CONTACTITEM_CLIENT_IMAGE = 15;     /* Integer */

    public static final int CONTACTITEM_ADDED = 1 << 0; /* Boolean */
    public static final int CONTACTITEM_NO_AUTH = 1 << 1; /* Boolean */
    public static final int CONTACTITEM_IS_TEMP = 1 << 2; /* Boolean */
    public static final int CONTACTITEM_HAS_CHAT = 1 << 3; /* Boolean */
    public static final int CONTACTITEM_PHANTOM = 1 << 4; /* Boolean */
    public static final int CONTACTITEM_CAN1215 = 1 << 5; /* Boolean */

    public static final int CONTACTITEM_INTERNAL_IP = 225;    /* IP address */
    public static final int CONTACTITEM_EXTERNAL_IP = 226;    /* IP address */


    // #sijapp cond.if modules_FILES is "true"#
    /* DC values */
    private FileTransferMessage ftm;
    private FileTransfer ft;
//  #sijapp cond.end#

    public void init() {
        setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, false);
        setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, false);
        setBooleanValue(ContactItem.CONTACTITEM_PHANTOM, false);
        setBooleanValue(ContactItem.CONTACTITEM_CAN1215, false);
        setIntValue(ContactItem.CONTACTITEM_STATUS, STATUS_OFFLINE);
        setIntValue(ContactItem.CONTACTITEM_CAPABILITIES, 0);
//		setIntValue(ContactItem.CONTACTITEM_PLAINMESSAGES, 0);
//		setIntValue(ContactItem.CONTACTITEM_URLMESSAGES, 0);
//		setIntValue(ContactItem.CONTACTITEM_SYSNOTICES, 0);
//		setIntValue(ContactItem.CONTACTITEM_AUTREQUESTS, 0);
        setIPValue(ContactItem.CONTACTITEM_INTERNAL_IP, new byte[4]);
        setIPValue(ContactItem.CONTACTITEM_EXTERNAL_IP, new byte[4]);
        setIntValue(ContactItem.CONTACTITEM_DC_PORT, 0);
        setIntValue(ContactItem.CONTACTITEM_DC_TYPE, 0);
        setIntValue(ContactItem.CONTACTITEM_ICQ_PROT, 0);
        setIntValue(ContactItem.CONTACTITEM_AUTH_COOKIE, 0);
// #sijapp cond.if modules_FILES is "true"#
        this.ft = null;
// #sijapp cond.end#
        setIntValue(ContactItem.CONTACTITEM_SIGNON, -1);
        setIntValue(ContactItem.CONTACTITEM_REGDATA, -1);
        //online = -1;
        setIntValue(ContactItem.CONTACTITEM_IDLE, -1);
        setIntValue(ContactItem.CONTACTITEM_CLIENT_IMAGE, 0);
        setStringValue(ContactItem.CONTACTITEM_CLIVERSION, "");
        setStringValue(ContactItem.CONTACTITEM_OFFLINETIME, "");
        setStringValue(ContactItem.CONTACTITEM_MODEL_PHONE, "");
        //if (notes == null) {
        //    setStringValue(ContactItem.CONTACTITEM_NOTES, "");
        //}
    }

    public void init(int id, int group, String uin, String name, boolean noAuth, boolean added) {
//		birthDay = "null";

        if (id == -1) {
            setIntValue(ContactItem.CONTACTITEM_ID, Util.createRandomId(getIcq()));
        } else {
            setIntValue(ContactItem.CONTACTITEM_ID, id);
        }

        setIntValue(ContactItem.CONTACTITEM_GROUP, group);
        setStringValue(ContactItem.CONTACTITEM_UIN, uin);
        setStringValue(ContactItem.CONTACTITEM_NAME, name);
        setBooleanValue(ContactItem.CONTACTITEM_NO_AUTH, noAuth);
        setBooleanValue(ContactItem.CONTACTITEM_ADDED, added);
        init();
    }

    /* Constructor for an existing contact item */
    public ContactItem(int id, int group, String uin, String name, boolean noAuth, boolean added, Profile profile) {
        this.profile = profile;
        this.init(id, group, uin, name, noAuth, added);
    }

    public ContactItem(Profile profile) {
        this.profile = profile;
    }

    public Icq getIcq() {
        return getProfile().getIcq();
    }

    public Profile getProfile() {
        return profile;
    }

    /* Returns true if client supports given capability */
    public boolean hasCapability(int capability) {
        return ((capability & this.caps) != 0x00000000);
    }

    /* Adds a capability by its CAPF value */
    public void addCapability(int capability) {
        this.caps |= capability;
    }

    public String getLowerText() {
        if (lowerText == null) {
            lowerText = name.toLowerCase();
            if (lowerText.equals(name)) lowerText = name; // to decrease memory usage
        }
        return lowerText;
    }

    /* Returns font color for contact name */
    public int getTextColor() {
        if (getBooleanValue(CONTACTITEM_PHANTOM)) return CanvasEx.getColor(CanvasEx.COLOR_FANTOM);
        if (getBooleanValue(CONTACTITEM_IS_TEMP)) return CanvasEx.getColor(CanvasEx.COLOR_TEMP);
        if ((blinking) && (ContactList.blinkText)) return CanvasEx.getColor(CanvasEx.COLOR_BLINK);
        return getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT)
                ? CanvasEx.getColor(CanvasEx.COLOR_CHAT) : CanvasEx.getColor(CanvasEx.COLOR_TEXT);
    }

    /* Returns font style for contact name */
    public int getFontStyle() {
        if (blinking) return Font.STYLE_BOLD;
        return getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT) ? Font.STYLE_BOLD : Font.STYLE_PLAIN;
        //return Font.STYLE_PLAIN;
    }

    public int getUIN() {
        return Integer.parseInt(uinString);
    }

    public String getUinString() {
        return uinString;
    }

    public int getImageIndex() {
        return statusImageIndex;
    }

    /* Returns image index for contact */
    public int getStatusImageIndex() {
//#sijapp cond.if target isnot "DEFAULT"#
        if (typing) return 18;
        if ((blinking) && (ContactList.blinkIcon)) return 19;
//#sijapp cond.end#
        if (isMessageAvailable(MESSAGE_PLAIN)) return 14;
        if (isMessageAvailable(MESSAGE_URL)) return 15;
        if (isMessageAvailable(MESSAGE_AUTH_REQUEST)) return 16;
        if (isMessageAvailable(MESSAGE_SYS_NOTICE)) return 17;

        return statusImageIndex;
    }

    public int getImageIndexWithMess() {
        if (isMessageAvailable(MESSAGE_PLAIN)) {
            return 14;
        }
        return getImageIndex();
    }

    public String getText() {
        return name;
    }


    // #sijapp cond.if modules_FILES is "true"#
    /* Returns the fileTransfer Object of this contact */
    public FileTransfer getFT() {
        return this.ft;
    }

    /* Set the FileTransferMessage of this contact */
    public void setFTM(FileTransferMessage _ftm) {
        this.ftm = _ftm;
    }

    /* Returns the FileTransferMessage of this contact */
    public FileTransferMessage getFTM() {
        return this.ftm;
    }
//  #sijapp cond.end#

    public boolean canWin1251() {
        return getBooleanValue(ContactItem.CONTACTITEM_CAN1215);
    }

    /* Returns true if contact must be shown even user offline and "hide offline" is on */
    public boolean mustBeShownAnyWay() {
        return (getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT)) ||
                (getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP)) ||
                (getBooleanValue(ContactItem.CONTACTITEM_PHANTOM));
    }

    /* Returns total count of all unread messages (messages, sys notices, urls, auths) */
    public int getUnreadMessCount() {
        return unreadMessCount;
    }

    /* Returns true if the next available message is a message of given type
        Returns false if no message at all is available, or if the next available
        message is of another type */
    public boolean isMessageAvailable(int type) {
        switch (type) {
            case MESSAGE_PLAIN:
                return (this.getIntValue(ContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
            case MESSAGE_URL:
                return (this.getIntValue(ContactItem.CONTACTITEM_URLMESSAGES) > 0);
            case MESSAGE_SYS_NOTICE:
                return (this.getIntValue(ContactItem.CONTACTITEM_SYSNOTICES) > 0);
            case MESSAGE_AUTH_REQUEST:
                return (this.getIntValue(ContactItem.CONTACTITEM_AUTREQUESTS) > 0);
        }
        return (this.getIntValue(ContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
    }

    /* Increases the mesage count */
    public void increaseMessageCount(int type) {
        unreadMessCount++;
        getProfile().updateUnreadMessCount();
        // if (Jimm.getContactList().contain(this)) {
        // Jimm.getContactList().updateUnreadMessCount(1);
        // }
        GroupItem group = getProfile().getGroupById(getIntValue(CONTACTITEM_GROUP));
        if (group != null) {
            group.updateMessCount(1);
        }
        switch (type) {
            case MESSAGE_PLAIN:
                setIntValue(CONTACTITEM_PLAINMESSAGES, getIntValue(CONTACTITEM_PLAINMESSAGES) + 1);
                break;
            case MESSAGE_URL:
                setIntValue(CONTACTITEM_URLMESSAGES, getIntValue(CONTACTITEM_URLMESSAGES) + 1);
                break;
            case MESSAGE_SYS_NOTICE:
                setIntValue(CONTACTITEM_SYSNOTICES, getIntValue(CONTACTITEM_SYSNOTICES) + 1);
                break;
            case MESSAGE_AUTH_REQUEST:
                setIntValue(CONTACTITEM_AUTREQUESTS, getIntValue(CONTACTITEM_AUTREQUESTS) + 1);
                break;
        }
    }

    public void resetUnreadMessages() {
        int changeCount = -unreadMessCount + getIntValue(CONTACTITEM_AUTREQUESTS);
        unreadMessCount = 0;
        setIntValue(CONTACTITEM_URLMESSAGES, 0);
        setIntValue(CONTACTITEM_SYSNOTICES, 0);
        setIntValue(CONTACTITEM_PLAINMESSAGES, 0);

        getProfile().updateUnreadMessCount();
        // if (Jimm.getContactList().contain(this)) {
        // Jimm.getContactList().updateUnreadMessCount(changeCount);
        // }
        GroupItem group = getProfile().getGroupById(getIntValue(CONTACTITEM_GROUP));
        if (group != null) {
            group.updateMessCount(changeCount);
        }

        Jimm.getContactList().contactChanged(this, false, true);
    }

    /* Checks whether some other object is equal to this one */
    public boolean equals(Object obj) {
        if (!(obj instanceof ContactItem)) {
            return (false);
        }
        ContactItem ci = (ContactItem) obj;
        return (this.getUinString().equals(ci.getUinString()) && (this.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) == ci.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP)));
    }

    //#sijapp cond.if target isnot "DEFAULT"#
    private boolean typing = false;

    public void BeginTyping(boolean type) {
        typing = type;
        setStatusImage();
    }

    //#sijapp cond.if modules_CLASSIC_CHAT is "true"#
    private boolean uTyping = false;

    public boolean uTyping() {
        return uTyping;
    }

    //#sijapp cond.end#
    public void beginTyping(boolean typing) {
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        uTyping = typing;
//#sijapp cond.end#
//#sijapp cond.if target isnot "DEFAULT"#
        if ((Options.getInt(Options.OPTION_TYPING_MODE) > 0) /*&& ((caps & ClientID.CAPF_TYPING) != 0) */&& getIcq().checkInvisLevel(getUinString()))
            try {
                getIcq().beginTyping(getUinString(), typing);
            } catch (JimmException ignored) {
            } catch (Exception ignored) {
            }
//#sijapp cond.end#
    }

    private boolean blinking;
    private int blinkOnlineNumber;
    private Timer blinkTimer;

    void blinkOnline() {
        if (blinkOnlineNumber > 0) {
            return;
        }
        if (blinkTimer != null) {
            return;
        }
        if (!ContactList.blinkText && !ContactList.blinkIcon) {
            return;
        }
        blinkOnlineNumber = Options.getInt(Options.OPTION_ONLINE_BLINK_TIME) * 2 + 1;
        (blinkTimer = new Timer()).schedule(new Blinking(this), 500, 500);
    }

    class Blinking extends TimerTask {
        private final ContactItem citem;

        Blinking(ContactItem o1) {
            citem = o1;
        }

        public final void run() {
            ContactItem item = citem;
            if ((blinkOnlineNumber > 0) || (blinking)) {
                blinking = !blinking;
                blinkOnlineNumber--;
                if (Jimm.getContactList().contain(item)) {
                    Jimm.getDisplay().callSerially(new RunnableImpl(RunnableImpl.TYPE_REPAINT_TREE));
                }
            } else {
                if (blinkTimer != null) {
                    blinkTimer.cancel();
                }
                blinkTimer = null;
            }
        }
    }

    //#sijapp cond.end#
    /**
     * ************************************************************************
     */
    public void setOfflineStatus() {
//#sijapp cond.if target isnot "DEFAULT"#
        typing = false;
//#sijapp cond.end#
        setIntValue(CONTACTITEM_STATUS, STATUS_OFFLINE);
        setIntValue(CONTACTITEM_CLIENT_IMAGE, 0);
        setXStatus(new byte[0]);
    }

    public void offline() {
        setStatus(STATUS_OFFLINE, false);
        setIPValue(ContactItem.CONTACTITEM_INTERNAL_IP, null);
        setIPValue(ContactItem.CONTACTITEM_EXTERNAL_IP, null);
        setIntValue(ContactItem.CONTACTITEM_DC_PORT, 0);
        setIntValue(ContactItem.CONTACTITEM_DC_TYPE, 0);
        setIntValue(ContactItem.CONTACTITEM_ICQ_PROT, 0);
        setIntValue(ContactItem.CONTACTITEM_AUTH_COOKIE, 0);
        setIntValue(ContactItem.CONTACTITEM_SIGNON, -1);
        setIntValue(ContactItem.CONTACTITEM_REGDATA, -1);
        setIntValue(ContactItem.CONTACTITEM_IDLE, -1);
        capabilities = null;
    }

    public void setStatus(int value, boolean checkStatus) {
        setStatus(value, 21);
    }

    public void setStatus(int value, int onlineTime) {
        boolean statusChanged = (value != status);
        boolean wasOnline = (status != STATUS_OFFLINE);
        boolean nowOnline = (value != STATUS_OFFLINE);
        if (!nowOnline) {
            setStringValue(CONTACTITEM_OFFLINETIME, DateAndTime.getDateString(false, false));
//#sijapp cond.if target isnot "DEFAULT"#
            BeginTyping(false);
//#sijapp cond.end#
            setIntValue(CONTACTITEM_CAPABILITIES, 0);
            //setIntValue(CONTACTITEM_CLIENT, ClientID.CLI_NONE);  // statusnone
            setStringValue(CONTACTITEM_MODEL_PHONE, "");  // statusnone
            setXStatus(new byte[0]);
            //contactChanged(cItem, false, true);
        }
        setIntValue(CONTACTITEM_STATUS, value);
        if (statusChanged) {
            String[] strs = JimmUI.getStatusesStrings();
            if (getExtraValue(ContactItem.EXTRA_WATCH) && nowOnline && wasOnline) {
                getProfile().getChatHistory().getChat(this).addExtraNotice("status_changed", ResourceBundle.getString(strs[statusImageIndex]), ContactList.imageList.elementAt(getImageIndex()));
            }
            //#sijapp cond.if modules_PANEL is "true"#
            String status = strs[statusImageIndex];
            if (status.indexOf(".") == 0) {
                status = "offline";
            }
            getProfile().showPopupItem(name + ' ' + ResourceBundle.getString("status_changed") + ' ' + ResourceBundle.getString(status), ContactList.imageList.elementAt(getImageIndex()), this);
            // #sijapp cond.end#
            getProfile().statusChanged(this, wasOnline, nowOnline, 0);
            Jimm.getContactList().contactChanged(this, false, (wasOnline && !nowOnline) || (!wasOnline && nowOnline) || (statusChanged && Options.getInt(Options.OPTION_CL_SORT_BY) != ContactList.SORT_BY_NAME));
        }

        if (nowOnline && !wasOnline) {
// #sijapp cond.if (target isnot "DEFAULT" & modules_SOUNDS is "true")#
            //System.out.println("Online: " + onlineTime);
            if (onlineTime < 21) {
                Notify.playSoundNotification(Notify.SOUND_TYPE_ONLINE, getExtraValue(ContactItem.EXTRA_SOUND));
                blinkOnline();
            }
// #sijapp cond.end#
            if (getExtraValue(ContactItem.EXTRA_WATCH)) {
                getProfile().getChatHistory().getChat(this).addExtraNotice("status_online", ContactList.imageList.elementAt(getImageIndex()));
            }
            if (getExtraValue(ContactItem.EXTRA_WAIT)) {
                Alert alert = new Alert(null, name + " " + ResourceBundle.getString("status_online").toLowerCase() + "!", null, null);
                alert.setTimeout(Alert.FOREVER);
                Jimm.setDisplay(alert);
            }
        }
    }

    // Получение картинки ICQ-клиента
    public Icon getClientImage() {
        return ContactList.clientIcons.elementAt(clientImageIndex);
    }

    private boolean isHappy = false;

    public void updateHappyFlag() {
        isHappy = birthDay != null && DateAndTime.currentDate.equals(birthDay);
    }

    // Получение шарика радости
    public Icon getHappyImage() {
        return isHappy ? ContactList.happyIcon : null;
    }

    // Получение иконки авторизации
    public Icon getAuthImage() {
        return (getBooleanValue(CONTACTITEM_NO_AUTH)) ? ContactList.authIcon : null;
    }

    // Получение картинки приватного списка для контакта
    public Icon getPrivateImage() {
        int index = -1;
        if (ignoreId != 0) index = 2;
        else if (visibleId != 0) index = 1;
        else if (invisibleId != 0) index = 0;

        return ContactList.privateIcons.elementAt(index);
    }

    /* Sets new contact name */
    public void rename(String newName) {
        if ((newName == null) || (newName.length() == 0)) return;
        name = newName;
        lowerText = null;
        try {
            /* Save ContactList */
            getProfile().safeSave();

            /* Try to save ContactList to server */
            if (!getBooleanValue(CONTACTITEM_IS_TEMP)) {
                UpdateContactListAction action = new UpdateContactListAction(this, UpdateContactListAction.ACTION_RENAME);
                getIcq().requestAction(action);
            }
        } catch (JimmException je) {
            if (je.isCritical()) {
                return;
            }
        } catch (Exception e) { /* Do nothing */
        }

        Jimm.getContactList().contactChanged(this, true, true);
        getProfile().getChatHistory().contactRenamed(getUinString(), name);
    }

    private int lastXStatus = -2;

    /* Activates the contact item menu */
    public void activate() {
        activate(true);
    }

    public void activate(boolean initText) {
        String currentUin = getStringValue(CONTACTITEM_UIN);
        resetUnreadMessages();
        ChatTextList chat = getProfile().getChatHistory().getChatHistoryAt(currentUin);
        boolean flag = false;
        if (chat == null) {
//#sijapp cond.if modules_HISTORY is "true"#
            flag |= Options.getBoolean(Options.OPTION_SHOW_LAST_MESS) && (HistoryStorage.getRecordCount(this) > 0);
//#sijapp cond.end#
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
            flag |= (Options.getInt(Options.OPTION_CLASSIC_CHAT) > 0);
//#sijapp cond.end#
        }
        if (flag) {
            getProfile().getChatHistory().newChatForm(this, this.name);
            chat = getProfile().getChatHistory().getChatHistoryAt(currentUin);
        }
        if (chat != null) {
            chat.activate(initText, false, this);
            readXStatus(false);
        } else {
            JimmUI.writeMessage(this, null).activate();
            //Jimm.getContactList().showFunctiontMenu(this);
        }
        setStatusImage();
    }

    private void readXStatus(boolean checkChat) {
        ChatTextList chat = getProfile().getChatHistory().getChatHistoryAt(getUinString());
        checkChat = ((!checkChat) || (chat != null) && (chat.isActive()));
        boolean request = (Options.getBoolean(Options.OPTION_AUTO_XTRAZ) || getExtraValue(EXTRA_AUTOX));
        request &= (readXtraz && checkChat);
        if (request) {
            boolean xStChanged = (xstatus.getStatusIndex() >= 0 && lastXStatus != xstatus.getStatusIndex());
            if (xStChanged || moodChanged) {
                requestXStatusText();
            }
            if (xStChanged) {
                lastXStatus = xstatus.getStatusIndex();
            }
        }
    }

    public void requestXStatusText() {
        readXtraz = false;
        openChat = true;
        if (moodText != null) {
            getProfile().getChatHistory().getChat(this).addTextToForm("", " " + moodText, "", 0, true, false, getXStatusImage(), 0);
            activate();
            return;
        }
        try {
            ActionListener.a(getUinString(), 0, getIcq());
        } catch (Exception ignored) {
        }
    }

    /**
     * ************************************************************************
     */

    /* Shows popup window with text of received message */
    public void showPopupWindow(String uin, String name, String text) {
        if (Jimm.locked()) {
            return;
        }
        boolean haveToShow = false;
        boolean chatVisible = getProfile().getChatHistory().chatHistoryShown(uin);
        boolean uinEquals = uin.equals(JimmUI.getLastUin());
        if (uinEquals) {
            showTicker(text, false);
        }
        int pwType = Options.getInt(Options.OPTION_POPUP_WIN2);
        switch (pwType) {
            case 0:
                return;
            case 1:
                haveToShow = (!chatVisible) && uinEquals;
                break;
            case 3:
                haveToShow = true;
                break;
            case 2:
            case 4:
                haveToShow = (!chatVisible) || (!uinEquals);
                break;
        }
        haveToShow &= (pwType != 2 || Jimm.getContactList().contain(this));
        haveToShow &= (pwType != 3 || !Jimm.getContactList().contain(this));

        if (!haveToShow) {
            return;
        }
        String textToAdd = "[" + name + "]\n" + text;
        Displayable d = Jimm.getDisplay().getCurrent();
        boolean onsys = Options.getBoolean(Options.OPTION_POPUP_ONSYS);
        boolean notrans = (Options.getInt(Options.OPTION_POPUP_TRANS) == 0);
        if (d instanceof Alert) {
            Alert currAlert = (Alert) d;
            if (currAlert.getImage() != null) {
                currAlert.setImage(null);
            }
            if (onsys && notrans) {
                int w = NativeCanvas.getWidthEx();
                PopUp pp = new PopUp(null, textToAdd, w, 2, 2);
                w = pp.width;
                int h = pp.getHeight();
                Image image = Image.createImage(w, h);//Всплывать поверх системных
                Graphics g = image.getGraphics();
                g.setColor(0xffffff);
                g.fillRect(0, 0, w, h);
                g.translate(-pp.x + 2, -pp.y + 2);
                pp.paint(g);
                currAlert.setImage(image);
            } else {
                currAlert.setString(currAlert.getString() + "\n" + textToAdd);
            }
            return;
        }
        if (d instanceof NativeCanvas) {
            CanvasEx c = NativeCanvas.getCanvas();
            int x = 4;
            int y, width;
            if (c instanceof SliderTask) {
                c = ((SliderTask) c).getNextScreen();
            }
            if (c instanceof VirtualList) {
                y = ((VirtualList) c).getCapHeight();
                width = c.getDrawWidth();
            } else if (c instanceof PopUp) {
                y = ((PopUp) c).getY();
                width = ((PopUp) c).getWidth();
            } else {
                y = 10;
                width = VirtualList.getWidth();
            }
            Jimm.setDisplay(new PopUp(c, textToAdd, width, x, y));
            return;
        }

// #sijapp cond.if target is "MIDP2"#
        String oldText = null;
        if (d instanceof TextBox) {
            oldText = ((TextBox) d).getString();
        }
// #sijapp cond.end#
        //System.out.println("o0");
        Alert alert;
        if (onsys && notrans) {
            int w = NativeCanvas.getWidthEx();
            PopUp pp = new PopUp(null, textToAdd, w, 2, 2);
            w = pp.width;
            int h = pp.getHeight();
            Image image = Image.createImage(w, h);//Всплывать поверх системных
            Graphics g = image.getGraphics();
            g.setColor(0xffffff);
            g.fillRect(0, 0, w, h);
            g.translate(-pp.x + 2, -pp.y + 2);
            pp.paint(g);
            alert = new Alert(null, null, image, null);
        } else {
            alert = new Alert(ResourceBundle.getString("message"), textToAdd, null, null);
        }
        alert.setTimeout(Alert.FOREVER);
        Jimm.getDisplay().setCurrent(alert, d);
// #sijapp cond.if target is "MIDP2"#
        if (d instanceof TextBox && (StringConvertor.getString(((TextBox) d).getString()).length() == 0) && (oldText != null)) {
            ((TextBox) d).setString(oldText);
        }
// #sijapp cond.end#
    }

    protected void showTicker(String text, boolean exclusive) {
        Object dp = Jimm.getCurrentDisplay();
        if (dp instanceof TextBox && Options.getBoolean(Options.OPTION_CREEPING_LINE) && text != null) {
            try {
                TextBox d = (TextBox) dp;
                String oldText = d.getString();
                Ticker oldTicker = d.getTicker();
                if (oldTicker == null || exclusive) {
                    d.setTicker(new Ticker(ititTickerText(text)));
                } else {
                    d.setTicker(new Ticker(ititTickerText(text + " | " + oldTicker.getString())));
                }
                if (StringConvertor.getString(d.getString()).length() == 0) {
                    d.setString(oldText);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static String ititTickerText(String text) {
        return text.substring(0, Math.min(256, text.length()));
    }

    public void checkStatus() {
        String uin = getUinString();
        int length = uin.length();
        byte[] uinRaw = Util.stringToByteArray(uin);
        byte[] buf = new byte[length + 4 + 1];
        Util.putWord(buf, 0, 0x0000);
        Util.putWord(buf, 2, 0x0005);
        Util.putByte(buf, 4, length);
        System.arraycopy(uinRaw, 0, buf, 5, uinRaw.length);
        SnacPacket packet = new SnacPacket(0x0002, 0x0015, 0, new byte[0], buf);

        try {
            getIcq().sendPacket(packet);
        } catch (JimmException ignored) {
        }
        Jimm.getContactList().activate();
    }

    public void setStatusImage() {
        int imgIndex;

//#sijapp cond.if target isnot "DEFAULT"#
        imgIndex = typing ? 18 : JimmUI.getStatusImageIndex(getIntValue(CONTACTITEM_STATUS));
//#sijapp cond.else#
//#		imgIndex = JimmUI.getStatusImageIndex(getIntValue(CONTACTITEM_STATUS));
//#sijapp cond.end#

        if (Jimm.locked()) {
            //Jimm.setStatusesToDraw(imgIndex, getXStatusImage());
            //Jimm.getSplashCanvasRef().setMessage(getStringValue(CONTACTITEM_NAME));
            Jimm.getSplashCanvasRef().invalidate();
            Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.SC_RESET_TEXT_AND_IMG), 15000);
            return;
        }

        ChatTextList chatList = getProfile().getChatHistory().getChatHistoryAt(getStringValue(CONTACTITEM_UIN));

        if (chatList != null) {
            chatList.setImage(ContactList.imageList.elementAt(imgIndex));
            chatList.setXstImage(getXStatusImage());
        }
    }

    private jimm.comm.XStatus xstatus = new jimm.comm.XStatus();

    public void setXStatus(byte[] capa) {
        setMood(null, -1);
        boolean changed = (!hasOnlyMood()) && (!xstatus.getStatusGUID().equals(capa, 0, Math.max(capa.length, 1)));
        changed |= (hasOnlyMood() && capa.length > 0);
        if (changed && capa.length > 0) {
            lastXStatus = -2;
        }
        readXtraz = true;
        readXStatus(true);
        getXStatus().setXStatus(capa);
        if (getExtraValue(ContactItem.EXTRA_WATCH) && capa.length > 0 && changed) {
            getProfile().getChatHistory().getChat(this).addExtraNotice("xtraz_changed", getXStatus().getXStatusAsString(), getXStatusImage());
        }
        //#sijapp cond.if modules_PANEL is "true"#
        //if (capa.length > 0 && changed) {
        //    getProfile().addTimeAction(name + ' ' + ResourceBundle.getString("xtraz_changed") + ' ' + getXStatus().getXStatusAsString(), getXStatusImage());
        //}
        // #sijapp cond.end#
    }

    private String moodText;
    private int moodIcon = -1;

    public void setMood(String text, int icon) {
        moodChanged = (icon != -1 && text != null && moodIcon != icon && !text.equals(moodText));
        moodText = text;
        moodIcon = icon;
    }

    public boolean hasMood() {
        return (moodIcon != -1);
    }

    public boolean hasOnlyMood() {
        return (getXStatus().getStatusGUID() == null);
    }

    public Icon getXStatusImage() {
        return xstatus.getIcon(moodIcon);
    }

    public jimm.comm.XStatus getXStatus() {
        return xstatus;
    }

    // Privacy Lists
    private int ignoreId;
    private int visibleId;
    private int invisibleId;

    public int getIgnoreId() {
        //DebugLog.addText ("Ignore ID = " + ignoreId);
        return ignoreId;
    }

    public void setIgnoreId(int id) {
        ignoreId = id;
    }

    public int getVisibleId() {
        //DebugLog.addText ("Visibility = " + visibleId);
        return visibleId;
    }

    public void setVisibleId(int id) {
        visibleId = id;
    }

    public int getInvisibleId() {
        //DebugLog.addText ("Invisibility = " + invisibleId);
        return invisibleId;
    }

    public void setInvisibleId(int id) {
        invisibleId = id;
    }
}