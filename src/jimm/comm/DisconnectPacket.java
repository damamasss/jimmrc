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
 File: src/jimm/comm/DisconnectPacket.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class DisconnectPacket extends Packet {
    // Packet types
    public static final int TYPE_SRV_COOKIE = 1;
    public static final int TYPE_SRV_GOODBYE = 2;
    public static final int TYPE_CLI_GOODBYE = 3;

    // UIN as string (== null for CLI_GOODBYE packets)
    protected String uin;

    // Server (!= null only for SRV_COOKIE packets)
    protected String server;

    // Cookie (!= null only for SRV_COOKIE packets)
    protected byte[] cookie;

    // Reason for disconnect as an error code (>= 0 only for SRV_DISCONNECT packets)
    protected int error;

    // Reason for disconnect as a string (!= null only for SRV_DISCONNECT packets)
    protected String description;

    // Constructs a SRV_COOKIE packet
    public DisconnectPacket(int sequence, String uin, String server, byte[] cookie) {
        //this.sequence = sequence;
        this.uin = uin;
        this.server = server;
        this.cookie = new byte[cookie.length];
        System.arraycopy(cookie, 0, this.cookie, 0, cookie.length);
        this.error = -1;
        this.description = null;
    }

    // Constructs a SRV_GOODBYE packet
    public DisconnectPacket(int sequence, int error, String description) {
        //this.sequence = sequence;
        this.uin = null;
        this.server = null;
        this.cookie = null;
        this.error = error;
        this.description = description;
    }

    // Constructs a SRV_GOODBYE packet
    public DisconnectPacket(int error, String description) {
        this(-1, error, description);
    }

    // Constructs a CLI_GOODBYE packet
    public DisconnectPacket(int sequence) {
        //this.sequence = sequence;
        this.uin = null;
        this.server = null;
        this.cookie = null;
        this.error = -1;
        this.description = null;
    }

    // Constructs a CLI_GOODBYE packet
    public DisconnectPacket() {
        this(-1);
    }

    // Returns the packet type
    public int getType() {
        if (this.uin != null) {
            return (DisconnectPacket.TYPE_SRV_COOKIE);
        } else if (this.error >= 0) {
            return (DisconnectPacket.TYPE_SRV_GOODBYE);
        } else {
            return (DisconnectPacket.TYPE_CLI_GOODBYE);
        }
    }

    // Returns the uin, or null if packet type is not SRV_COOKIE
    public String getUin() {
        if (this.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
            return this.uin;
        } else {
            return (null);
        }
    }

    // Returns the server, or null if packet type is not SRV_COOKIE
    public String getServer() {
        if (this.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
            return this.server;
        } else {
            return (null);
        }
    }

    // Returns the cookie, or null if packet type is not SRV_COOKIE
    public byte[] getCookie() {
        if (this.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
            byte[] cookie = new byte[this.cookie.length];
            System.arraycopy(this.cookie, 0, cookie, 0, this.cookie.length);
            return (cookie);
        } else {
            return (null);
        }
    }

    // Returns the error as an error code, or -1 if packet type is not SRV_GOODBYE
    public int getError() {
        if (this.getType() == DisconnectPacket.TYPE_SRV_GOODBYE) {
            return (this.error);
        } else {
            return (-1);
        }
    }

    // Returns the reason for disconnect as a string, or null if packet type is not SRV_GOODBYE
    public String getDescription() {
        if (this.getType() == DisconnectPacket.TYPE_SRV_GOODBYE) {
            return this.description;
        } else {
            return (null);
        }
    }

    // Returns the package as byte array
    public byte[] toByteArray() {
        // Get package length
        int length = 6;
        if (this.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
            length += 4 + this.uin.length();
            length += 4 + this.server.length();
            length += 4 + this.cookie.length;
        } else if (this.getType() == DisconnectPacket.TYPE_SRV_GOODBYE) {
            length += 4 + 2;
            length += 4 + this.description.length();
        }

        // Allocate memory
        byte[] buf = new byte[length];

        // Assemble FLAP header
        Util.putByte(buf, 0, 0x2A); // FLAP.ID
        Util.putByte(buf, 1, 0x04); // FLAP.CHANNEL
        Util.putWord(buf, 2, sequence); // FLAP.SEQUENCE
        Util.putWord(buf, 4, length - 6); // FLAP.LENGTH

        // Marker
        int pos = 6;

        // Assemble SRV_COOKIE
        if (this.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
            // DISCONNECT.UIN
            Util.putWord(buf, pos, 0x0001);
            Util.putWord(buf, pos + 2, this.uin.length());
            byte[] uinRaw = Util.stringToByteArray(this.uin);
            System.arraycopy(uinRaw, 0, buf, pos + 4, uinRaw.length);
            pos += 4 + uinRaw.length;

            // DISCONNECT.SERVER
            Util.putWord(buf, pos, 0x0005);
            Util.putWord(buf, pos + 2, this.server.length());
            byte[] serverRaw = Util.stringToByteArray(this.server);
            System.arraycopy(serverRaw, 0, buf, pos + 4, serverRaw.length);
            pos += 4 + serverRaw.length;

            // DISCONNECT.COOKIE
            Util.putWord(buf, pos, 0x0006);
            Util.putWord(buf, pos + 2, this.cookie.length);
            System.arraycopy(this.cookie, 0, buf, pos + 4, cookie.length);
        }

        // Assemble SRV_GOODBYE
        else if (this.getType() == DisconnectPacket.TYPE_SRV_GOODBYE) {
            // DISCONNECT.UIN
            Util.putWord(buf, pos, 0x0001);
            Util.putWord(buf, pos + 2, this.uin.length());
            byte[] uinRaw = Util.stringToByteArray(this.uin);
            System.arraycopy(uinRaw, 0, buf, pos + 4, uinRaw.length);
            pos += 4 + uinRaw.length;

            // DISCONNECT.DESCRIPTION
            Util.putWord(buf, pos, 0x0004);
            Util.putWord(buf, pos + 2, this.description.length());
            byte[] descriptionRaw = Util.stringToByteArray(this.description);
            System.arraycopy(descriptionRaw, 0, buf, pos + 4, descriptionRaw.length);

            // DISCONNECT.ERROR
            Util.putWord(buf, pos, 0x0008);
            Util.putWord(buf, pos + 2, 0x0002);
            Util.putWord(buf, pos + 4, this.error);
            pos += 4 + 2;
        }

        // Return
        return (buf);
    }

    // Parses given byte array and returns a Packet object
    public static Packet parse(byte[] buf, int off, int len) throws JimmException {
        // Get FLAP sequence number
        int flapSequence = Util.getWord(buf, off + 2);

        // Get length of FLAP data
        //int flapLength = Util.getWord(buf, off + 4);

        // Variables for all possible TLVs
        String uin = null;
        String server = null;
        byte[] cookie = null;
        int error = -1;
        String description = null;

        // Read all TLVs
        int marker = off + 6;
        byte[] tlvValue;
        while (marker < (off + len)) {
            // Get next TLV
            tlvValue = Util.getTlv(buf, marker);
            if (tlvValue == null) {
                throw (new JimmException(135, 0));
            }

            // Get type of next TLV
            int tlvType = Util.getWord(buf, marker);

            // Update markers
            marker += 4 + tlvValue.length;

            // Save value
            switch (tlvType) {
                case 0x0001: // uin
                    uin = Util.byteArrayToString(tlvValue);
                    break;
                case 0x0005: // server
                    server = Util.byteArrayToString(tlvValue);
                    break;
                case 0x0006: // cookie
                    cookie = tlvValue;
                    break;
                case 0x0008: // error
                case 0x0009: // error
                    error = Util.getWord(tlvValue, 0);
                    break;
                case 0x0004: // description
                case 0x000B: // description
                    description = Util.byteArrayToString(tlvValue);
                    break;
                default: // Do nothing on default (ignore all unknown TLVs)
            }
        }

        // CLI_GOODBYE
        if ((uin == null) && (server == null) && (cookie == null) && (error == -1) && (description == null)) {
            return (new DisconnectPacket(flapSequence));
        }

        // SRV_COOKIE
        else if ((uin != null) && (server != null) && (cookie != null) && (error == -1) && (description == null)) {
            return (new DisconnectPacket(flapSequence, uin, server, cookie));
        }

        // SRV_GOODBYYE
        else if ((server == null) && (cookie == null) && (error != -1) && (description != null)) {
            return (new DisconnectPacket(flapSequence, error, description));
        }

        // Other TLV combinations are not valid
        else {
            throw (new JimmException(135, 2));
        }
    }

    // Parses given byte array and returns a Packet object
    public static Packet parse(byte[] buf) throws JimmException {
        return (DisconnectPacket.parse(buf, 0, buf.length));
    }
}