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
 File: src/jimm/comm/FromIcqSrvPacket.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class FromIcqSrvPacket extends SnacPacket {
    // SRV_OFFLINEMSG packet subcommand
    public static final int SRV_OFFLINEMSG_SUBCMD = 0x0041;

    // SRV_DONEOFFLINEMSGS packet subcommand
    public static final int SRV_DONEOFFLINEMSGS_SUBCMD = 0x0042;

    // SRV_META packet subcommand and types
    public static final int SRV_META_SUBCMD = 0x07DA;
    public static final int SRV_META_GENERAL_TYPE = 0x00C8;
    public static final int META_SET_FULLINFO_ACK = 0x0C3F;

    /****************************************************************************/
    /****************************************************************************/
    /**
     * ************************************************************************
     */

    // ICQ sequence number
    protected int icqSequence;

    // UIN
    protected String uin;

    // Subcommand
    protected int subcommand;

    // Constructor
    public FromIcqSrvPacket(int sequence, long reference, int snacFlags, int icqSequence, String uin, int subcommand, byte[] extData, byte[] data) {
        super(sequence, SnacPacket.SRV_FROMICQSRV_FAMILY, SnacPacket.SRV_FROMICQSRV_COMMAND, snacFlags, reference, extData, data);
        this.icqSequence = icqSequence;
        this.uin = uin;
        this.subcommand = subcommand;
    }

    // Returns the ICQ sequence number
    //public int getIcqSequence()
    //{
    //	return (this.icqSequence);
    //}

    // Sets the ICQ sequence number

    void setIcqSequence(int icqSequence) {
        this.icqSequence = icqSequence;
    }

    // Returns the UIN
    public String getUin() {
        return this.uin;
    }

    // Returns the subcommand
    public int getSubcommand() {
        return (this.subcommand);
    }

    // Returns the package as byte array
    public byte[] toByteArray() {
        // Allocate memory
        byte buf[] = new byte[6 + 10 + 14 + this.data.length + (this.extData.length > 0 ? 2 + this.extData.length : 0)];

        // Assemble FLAP header
        Util.putByte(buf, 0, 0x2A); // FLAP.ID
        Util.putByte(buf, 1, 0x02); // FLAP.CHANNEL
        Util.putWord(buf, 2, sequence); // FLAP.SEQUENCE
        Util.putWord(buf, 4, 10 + 14 + this.data.length + (this.extData.length > 0 ? 2 + this.extData.length : 0)); // FLAP.LENGTH

        // Assemble SNAC header
        Util.putWord(buf, 6, this.family); // SNAC.FAMILY
        Util.putWord(buf, 8, this.command); // SNAC.COMMAND
        Util.putWord(buf, 10, (this.extData.length > 0 ? 0x8000 : 0x0000)); // SNAC.FLAGS
        Util.putDWord(buf, 12, this.reference); // SNAC.REFERENCE;
        Util.putWord(buf, 12, this.icqSequence - 1);

        // Assemlbe SNAC.DATA
        if (this.extData.length > 0) {
            Util.putWord(buf, 16, this.extData.length);
            System.arraycopy(this.extData, 0, buf, 18, this.extData.length);
            Util.putWord(buf, 18 + this.extData.length, 0x0001);
            Util.putWord(buf, 20 + this.extData.length, 10 + this.data.length);
            Util.putWord(buf, 22 + this.extData.length, 8 + this.data.length, false); // CLI_TOICQSRV.LENGTH in Little Endian
            Util.putDWord(buf, 24 + this.extData.length, Long.parseLong(this.uin), false); // CLI_TOICQSRV.UIN in Little Endian
            Util.putWord(buf, 28 + this.extData.length, this.subcommand, false); // CLI_TOICQSRV.SUBCOMMAND in Little Endian
            Util.putWord(buf, 30 + this.extData.length, this.icqSequence, false); // CLI_TOICQSRV.SEQUENCE in Little Endian
            System.arraycopy(this.data, 0, buf, 32 + this.extData.length, this.data.length); // CLI_TOICQSRV.DATA in Little Endian
        } else {
            Util.putWord(buf, 16, 0x0001);
            Util.putWord(buf, 18, 10 + this.data.length);
            Util.putWord(buf, 20, 8 + this.data.length, false); // CLI_TOICQSRV.LENGTH in Little Endian
            Util.putDWord(buf, 22, Long.parseLong(this.uin), false); // CLI_TOICQSRV.UIN in Little Endian
            Util.putWord(buf, 26, this.subcommand, false); // CLI_TOICQSRV.SUBCOMMAND in Little Endian
            Util.putWord(buf, 28, this.icqSequence, false); // CLI_TOICQSRV.SEQUENCE in Little Endian
            System.arraycopy(this.data, 0, buf, 30, this.data.length); // CLI_TOICQSRV.DATA in Little Endian
        }

        // Return
        return (buf);
    }

    // Parses given byte array and returns a FromIcqSrvPacket object
    public static Packet parse(byte[] buf, int off, int len) throws JimmException {
        // Get FLAP sequence number
        int flapSequence = Util.getWord(buf, off + 2);

        // Get length of FLAP data
        int flapLength = Util.getWord(buf, off + 4);

        // Check length (min. 24 bytes)
        if (flapLength < 24) {
            throw (new JimmException(137, 0));
        }

        // Get SNAC flags
        int snacFlags = Util.getWord(buf, off + 10);

        // Get SNAC reference
        long snacReference = Util.getDWord(buf, off + 12);

        // Get data and extra data (if available)
        byte[] extData;
        byte[] data;
        String uin;
        int subcommand;
        int icqSequence;
        if (snacFlags == 0x8000) {
            // Check length (min. 26 bytes)
            if (flapLength < 10 + 24 + 2) {
                throw (new JimmException(137, 1));
            }

            // Get length of extra data
            int extDataLength = Util.getWord(buf, off + 16);

            // Check length (min. 26+extDataLength bytes)
            if (flapLength < 10 + 24 + 2 + extDataLength) {
                throw (new JimmException(137, 2));
            }

            // Get extra data
            extData = new byte[extDataLength];
            System.arraycopy(buf, off + 6 + 10 + 2, extData, 0, extDataLength);

            // Get uin, subcommand and icq sequence number
            uin = String.valueOf(Util.getDWord(buf, off + 6 + 10 + 6 + 2 + extDataLength, false));
            subcommand = Util.getWord(buf, off + 6 + 10 + 10 + 2 + extDataLength, false);
            icqSequence = Util.getWord(buf, off + 6 + 10 + 12 + 2 + extDataLength, false);

            // Get data
            data = new byte[flapLength - 10 - 14 - 2 - extDataLength];
            System.arraycopy(buf, off + 6 + 10 + 14 + 2 + extDataLength, data, 0, flapLength - 10 - 14 - 2 - extDataLength);
        } else {
            // Get uin, subcommand and icq sequence number
            uin = String.valueOf(Util.getDWord(buf, off + 6 + 10 + 6, false));
            subcommand = Util.getWord(buf, off + 6 + 10 + 10, false);
            icqSequence = Util.getWord(buf, off + 6 + 10 + 12, false);

            // Get extra data and data
            extData = new byte[0];
            data = new byte[flapLength - 10 - 14];
            System.arraycopy(buf, off + 6 + 10 + 14, data, 0, flapLength - 10 - 14);
        }

        // Instantiate FromIcqSrvPacket
        return (new FromIcqSrvPacket(flapSequence, snacReference, snacFlags, icqSequence, uin, subcommand, extData, data));
    }

    // Parses given byte array and returns a FromIcqSrvPacket object
    public static Packet parse(byte[] buf) throws JimmException {
        return (FromIcqSrvPacket.parse(buf, 0, buf.length));
    }
}