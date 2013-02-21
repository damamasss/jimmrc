/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/jimm/comm/DCPacket.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

// #sijapp cond.if modules_FILES is "true"#
package jimm.comm;

import jimm.JimmException;

public class DCPacket extends Packet {
    // The packet data
    byte[] data;

    // Constructor
    public DCPacket(byte[] _data) {
        data = _data;
    }

    // getDCContent(byte[] buf) => byte[]
    public byte[] getDCContent() {
        return data;
    }

    // Returns the package as byte array
    public byte[] toByteArray() {
        // Allocate memory
        byte buf[] = new byte[data.length + 2];

        // Assemble DC header
        Util.putWord(buf, 0, data.length, false); // length
        System.arraycopy(data, 0, buf, 2, data.length);

        // Return
        return (buf);
    }

    // Parses given byte array and returns a Packet object
    public static Packet parse(byte[] buf, int off, int len) throws JimmException {
        return (new DCPacket(buf));
    }

    // Parses given byte array and returns a Packet object
    public static Packet parse(byte[] buf) throws JimmException {
        return (DCPacket.parse(buf, 0, buf.length));
    }
}
// #sijapp cond.end#