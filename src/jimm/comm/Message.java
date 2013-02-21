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
 File: src/jimm/comm/Message.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.ContactItem;

public abstract class Message {
    // Static variables for message type;
    public static final int MESSAGE_TYPE_AUTO = 0x0000;
    public static final int MESSAGE_TYPE_NORM = 0x0001;
    public static final int MESSAGE_TYPE_EXTENDED = 0x001a;
    public static final int MESSAGE_TYPE_AWAY = 0x03e8;
    public static final int MESSAGE_TYPE_OCC = 0x03e9;
    public static final int MESSAGE_TYPE_NA = 0x03ea;
    public static final int MESSAGE_TYPE_DND = 0x03eb;
    public static final int MESSAGE_TYPE_FFC = 0x03ec;
    public static final int MESSAGE_TYPE_EVIL = 0x03eb;
    public static final int MESSAGE_TYPE_DEPRESSION = 0x03eb;
    public static final int MESSAGE_TYPE_HOME = 0x03ec;
    public static final int MESSAGE_TYPE_WORK = 0x03e9;
    public static final int MESSAGE_TYPE_LUNCH = 0x03e8;

    // Message type
    private int messageType;

    protected boolean offline;

    // Senders UIN (set for both incoming and outgoing messages)
    private String sndrUin;

    // Receivers UIN (set only for incoming messages)
    private String rcvrUin;

    // Receiver object (set only for outgoing messages)
    protected ContactItem rcvr;

    // Date of dispatch
    private long newDate;

    protected Message(long date, String rcvrUin, String sndrUin, int messageType) {
        newDate = date;
        this.rcvrUin = rcvrUin;
        this.sndrUin = sndrUin;
        this.messageType = messageType;
    }

    // Returns the senders UIN
    public String getSndrUin() {
        return this.sndrUin;
    }

    // Returns the receivers UIN
    public String getRcvrUin() {
        if (this.rcvrUin != null) {
            return this.rcvrUin;
        } else {
            return (this.rcvr.getUinString());
        }
    }

    // Returns the message type
    public int getMessageType() {
        return (this.messageType);
    }

    // Returns the receiver
    public ContactItem getRcvr() {
        return (this.rcvr);
    }

    public boolean getOffline() {
        return offline;
    }

    public long getNewDate() {
        return newDate;
    }
}