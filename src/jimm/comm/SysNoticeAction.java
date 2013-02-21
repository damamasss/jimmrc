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
 File: src/jimm/comm/SysNoticeAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class SysNoticeAction extends Action {
    // System notice
    private SystemNotice notice;

    // Constructor
    public SysNoticeAction(SystemNotice _notice) {
        super(false, true);
        this.notice = _notice;
    }

    // Init action
    protected void init() throws JimmException {
        byte[] buf;

        //	Get byte Arrys from the stuff we need the length of
        byte[] uinRaw = Util.stringToByteArray(this.notice.getSndrUin());
        byte[] reasonRaw = Util.stringToByteArray(this.notice.getReason(), true);

        // If system notice is a SYS_NOTICE_AUTHORISE
        if (this.notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHORISE) {
            // Calculate length of use date in SNAC packet loger if denyed because of the reason
            if (this.notice.isAUTH_granted()) {
                buf = new byte[1 + uinRaw.length + 1 + 4];
            } else {
                buf = new byte[1 + uinRaw.length + 1 + 2 + reasonRaw.length];
            }

            // Assemble the packet
            int marker = 0;
            Util.putByte(buf, marker, uinRaw.length);
            System.arraycopy(uinRaw, 0, buf, marker + 1, uinRaw.length);
            marker += 1 + uinRaw.length;

            // Branch for different granted or dneyed packet
            if (this.notice.isAUTH_granted()) {
                Util.putByte(buf, marker, 0x01);
                marker += 1;
                Util.putWord(buf, marker, 0x0000);
                marker += 2;
                Util.putWord(buf, marker, 0x0000);
            } else {
                Util.putByte(buf, marker, 0x00);
                marker += 1;
                Util.putWord(buf, marker, reasonRaw.length);
                marker += 2;
                System.arraycopy(reasonRaw, 0, buf, marker, reasonRaw.length);
            }

            // Send a CLI_AUTHORIZE packet
            SnacPacket packet = new SnacPacket(SnacPacket.CLI_AUTHORIZE_FAMILY,
                    SnacPacket.CLI_AUTHORIZE_COMMAND, 0x0000001A, new byte[0],
                    buf);
            icq.sendPacket(packet);
        }

        //	If system notice is a SYS_NOTICE_REQUAUTH
        else if (this.notice.getSysnotetype() == SystemNotice.SYS_NOTICE_REQUAUTH) {
            // Calculate length of use date in SNAC packet
            buf = new byte[1 + uinRaw.length + 2 + reasonRaw.length + 2];

            // Assemble the packet
            int marker = 0;
            Util.putByte(buf, marker, uinRaw.length);
            System.arraycopy(uinRaw, 0, buf, marker + 1, uinRaw.length);
            marker += 1 + uinRaw.length;
            Util.putWord(buf, marker, reasonRaw.length);
            marker += 2;
            System.arraycopy(reasonRaw, 0, buf, marker, reasonRaw.length);
            marker += reasonRaw.length;
            Util.putWord(buf, marker, 0x0000);

            // Send a CLI_REQUAUTH packet
            SnacPacket packet = new SnacPacket(SnacPacket.CLI_REQAUTH_FAMILY,
                    SnacPacket.CLI_REQAUTH_COMMAND, 0x00000018, new byte[0],
                    buf);
            icq.sendPacket(packet);
        }
    }

    // Forwards received packet, returns true if packet was consumed
    protected boolean forward(Packet packet) throws JimmException {
        return (false);
    }

    // Returns true if the action is completed
    public boolean isCompleted() {
        return (true);
    }

    // Returns true if an error has occured
    public boolean isError() {
        return (false);
    }
}