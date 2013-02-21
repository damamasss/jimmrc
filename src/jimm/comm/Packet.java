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
 File: src/jimm/comm/Packet.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class Packet {
    // Channel constants
    public static final int CHANNEL_CONNECT = 0x01;
    public static final int CHANNEL_SNAC = 0x02;
    public static final int CHANNEL_ERROR = 0x03;
    public static final int CHANNEL_DISCONNECT = 0x04;
    public static final int CHANNEL_PING = 0x05;

    // FLAP sequence number
    protected int sequence;

    protected int flapChannel;
    protected byte[] flapData;

    // Returns the FLAP sequence number
    //public int getSequence()
    //{
    //	return (this.sequence);
    //}

    // Sets the FLAP sequence number

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    protected Packet() {
    }

    public Packet(int channel, byte[] data) {
        flapChannel = channel;
        flapData = data;
    }

    // Returns the package as byte array
    public byte[] toByteArray() {
        byte[] buf = new byte[6 + flapData.length];
        Util.putByte(buf, 0, 0x2a);
        Util.putByte(buf, 1, flapChannel);
        Util.putWord(buf, 2, sequence);
        Util.putWord(buf, 4, flapData.length);
        System.arraycopy(flapData, 0, buf, 6, flapData.length);
        return buf;
    }

    // Parses given byte array and returns a Packet object
    public static Packet parse(byte[] buf, int off, int len) throws JimmException {
        // Check length (min. 6 bytes)
// #sijapp cond.if modules_FILES is "true"#
        if (len < 2)
// #sijapp cond.else#
            if (len < 6)
// #sijapp cond.end#
            {
                throw (new JimmException(130, 0));
            }

        // Verify FLAP.ID
        if (Util.getByte(buf, off) != 0x2A) {
// #sijapp cond.if modules_FILES is "true"#
            return (DCPacket.parse(buf, off, len));
// #sijapp cond.else#
            throw (new JimmException(130, 1));
// #sijapp cond.end#
        }

        // Get and verify FLAP.CHANNEL
        int channel = Util.getByte(buf, off + 1);
        if ((channel < 1) || (channel > 5)) {
            throw (new JimmException(130, 2));
        }

        // Verify FLAP.LENGTH
        int length = Util.getWord(buf, off + 4);
        if ((length + 6) != len) {
            throw (new JimmException(130, 3));
        }

        // Parsing is done by a subclass
        switch (channel) {
            case Packet.CHANNEL_CONNECT:
                return (ConnectPacket.parse(buf, off, len));
            case Packet.CHANNEL_SNAC:
                return (SnacPacket.parse(buf, off, len));
            case Packet.CHANNEL_DISCONNECT:
                return (DisconnectPacket.parse(buf, off, len));
            default:
                return null;
        }
    }

    // Parses given byte array and returns a Packet object
    public static Packet parse(byte[] buf) throws JimmException {
        return (Packet.parse(buf, 0, buf.length));
    }
}