/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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
 File: src/jimm/comm/SystemNotice.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.ContactItem;
import jimm.util.ResourceBundle;

public class SystemNotice extends Message {
    // Types of system messages
    public static final int SYS_NOTICE_YOUWEREADDED = 1;
    public static final int SYS_NOTICE_AUTHREPLY = 2;
    public static final int SYS_NOTICE_AUTHREQ = 3;
    public static final int SYS_NOTICE_AUTHORISE = 4;
    public static final int SYS_NOTICE_REQUAUTH = 5;
    public static final int SYS_NOTICE_DELETE = 6;

    // Type of the note
    private int sysnotetype;

    // Was the Authorisation granted
    private boolean AUTH_granted;

    // What was the reason
    private String reason;

    // Constructs system notice
    public SystemNotice(int _sysnotetype, ContactItem cItem, boolean _AUTH_granted, String _reason) {
        super(DateAndTime.createCurrentDate(false), cItem.getIcq().getUin(), cItem.getUinString(), MESSAGE_TYPE_AUTO);
        sysnotetype = _sysnotetype;
        AUTH_granted = _AUTH_granted;
        reason = _reason;
    }

    // Get AUTH_granted
    public boolean isAUTH_granted() {
        return AUTH_granted;
    }

    // Get Reason
    public String getReason() {
        return reason;
    }

    // Get Sysnotetype
    public int getSysnotetype() {
        return sysnotetype;
    }

    public String getText() {
        switch (sysnotetype) {
            case SYS_NOTICE_YOUWEREADDED:
                return ResourceBundle.getString("youwereadded") + getSndrUin();
            case SYS_NOTICE_DELETE:
                return ResourceBundle.getString("user") + ' ' + getReason() + ResourceBundle.getString("removed_himself");
            case SYS_NOTICE_AUTHREQ:
                return getSndrUin() + ResourceBundle.getString("wantsyourauth") + getReason();
            case SYS_NOTICE_AUTHREPLY:
                if (isAUTH_granted()) {
                    return ResourceBundle.getString("grantedby") + getSndrUin() + ".";
                } else if (getReason() != null) {
                    return ResourceBundle.getString("denyedby") + getSndrUin() + ". "
                            + ResourceBundle.getString("reason") + ": " + getReason();
                } else {
                    return ResourceBundle.getString("denyedby") + getSndrUin() + ". "
                            + ResourceBundle.getString("noreason");
                }
        }
        return "";
    }
}