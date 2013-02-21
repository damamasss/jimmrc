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
 File: src/jimm/comm/SysNoticeAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Denis Stanishevskiy
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class RemoveMeAction extends Action {
    private String uin;

    // Constructor
    public RemoveMeAction(String uin) {
        super(false, true);
        this.uin = uin;
    }

    // Init action
    protected void init() throws JimmException {
        byte[] buf;

        //	Get byte Arrys from the stuff we need the length of
        byte[] uinRaw = Util.stringToByteArray(this.uin);

        // Calculate length of use date in SNAC packet loger if denyed because of the reason
        buf = new byte[1 + uinRaw.length];

        // Assemble the packet
        int marker = 0;
        Util.putByte(buf, marker, uinRaw.length);
        System.arraycopy(uinRaw, 0, buf, marker + 1, uinRaw.length);

        // Send a CLI_AUTHORIZE packet
        SnacPacket packet = new SnacPacket(SnacPacket.CLI_REMOVEME_FAMILY, SnacPacket.CLI_REMOVEME_COMMAND, 0x00000003, new byte[0], buf);
        icq.sendPacket(packet);
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