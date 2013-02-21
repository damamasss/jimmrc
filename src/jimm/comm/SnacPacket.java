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
 File: src/jimm/comm/SnacPacket.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class SnacPacket extends Packet {
    /**************************/
    /* Family 0x0001: SERVICE */
    /**
     * **********************
     */
    public static final int SERVICE_FAMILY = 0x0001;

    // CLI_READY packet packet family and command
    public static final int CLI_READY_FAMILY = 0x0001;
    public static final int CLI_READY_COMMAND = 0x0002;

    // SRV_FAMILIES packet family and command
    public static final int SRV_FAMILIES_FAMILY = 0x0001;
    public static final int SRV_FAMILIES_COMMAND = 0x0003;

    // CLI_RATESREQUEST packet family and command
    public static final int CLI_RATESREQUEST_FAMILY = 0x0001;
    public static final int CLI_RATESREQUEST_COMMAND = 0x0006;

    // SRV_RATES packet family and command
    public static final int SRV_RATES_FAMILY = 0x0001;
    public static final int SRV_RATES_COMMAND = 0x0007;

    // CLI_ACKRATES packet family and command
    public static final int CLI_ACKRATES_FAMILY = 0x0001;
    public static final int CLI_ACKRATES_COMMAND = 0x0008;

    // CLI_REQINFO packet family and command
    public static final int CLI_REQINFO_FAMILY = 0x0001;
    public static final int CLI_REQINFO_COMMAND = 0x000E;

    // SRV_REPLYINFO packet family and command
    public static final int SRV_REPLYINFO_FAMILY = 0x0001;
    public static final int SRV_REPLYINFO_COMMAND = 0x000F;

    // SRV_MOTD packet family and command
    public static final int SRV_MOTD_FAMILY = 0x0001;
    public static final int SRV_MOTD_COMMAND = 0x0013;

    // CLI_FAMILIES packet family and command
    public static final int CLI_FAMILIES_FAMILY = 0x0001;
    public static final int CLI_FAMILIES_COMMAND = 0x0017;

    // SRV_FAMILIES2 packet family and command
    public static final int SRV_FAMILIES2_FAMILY = 0x0001;
    public static final int SRV_FAMILIES2_COMMAND = 0x0018;

    // CLI_SETSTATUS packet packet family and command
    public static final int CLI_SETSTATUS_FAMILY = 0x0001;
    public static final int CLI_SETSTATUS_COMMAND = 0x001E;

    /***************************/
    /* Family 0x0002: LOCATION */
    /**
     * ***********************
     */

    public static final int LOCATION_FAMILY = 0x0002;

    // CLI_REQLOCATION packet family and command
    public static final int CLI_REQLOCATION_FAMILY = 0x0002;
    public static final int CLI_REQLOCATION_COMMAND = 0x0002;

    // SRV_REPLYLOCATION packet family and command
    public static final int SRV_REPLYLOCATION_FAMILY = 0x0002;
    public static final int SRV_REPLYLOCATION_COMMAND = 0x0003;

    // CLI_SETUSERINFO packet family and command
    public static final int CLI_SETUSERINFO_FAMILY = 0x0002;
    public static final int CLI_SETUSERINFO_COMMAND = 0x0004;

    /**************************/
    /* Family 0x0003: CONTACT */
    /**
     * **********************
     */

    public static final int CONTACT_FAMILY = 0x0003;

    // CLI_REQBUDDY packet family and command
    public static final int CLI_REQBUDDY_FAMILY = 0x0003;
    public static final int CLI_REQBUDDY_COMMAND = 0x0002;

    // SRV_REPLYBUDDY packet family and command
    public static final int SRV_REPLYBUDDY_FAMILY = 0x0003;
    public static final int SRV_REPLYBUDDY_COMMAND = 0x0003;

    // SRV_REPLYBUDDY packet family and command
    public static final int CLI_BUDDYLIST_ADD_FAMILY = 0x0003;
    public static final int CLI_BUDDYLIST_ADD_COMMAND = 0x0004;

    // SRV_REPLYBUDDY packet family and command
    public static final int CLI_BUDDYLIST_REMOVE_FAMILY = 0x0003;
    public static final int CLI_BUDDYLIST_REMOVE_COMMAND = 0x0005;

    // SRV_USERONLINE packet family and command
    public static final int SRV_USERONLINE_FAMILY = 0x0003;
    public static final int SRV_USERONLINE_COMMAND = 0x000B;

    // SRV_USEROFFLINE packet family and command
    public static final int SRV_USEROFFLINE_FAMILY = 0x0003;
    public static final int SRV_USEROFFLINE_COMMAND = 0x000C;

    /***********************/
    /* Family 0x0004: ICBM */
    /**
     * *******************
     */

    public static final int ICBM_FAMILY = 0x0004;

    // CLI_SETICBM packet packet family and command
    public static final int CLI_SETICBM_FAMILY = 0x0004;
    public static final int CLI_SETICBM_COMMAND = 0x0002;

    // CLI_REQICBM packet family and command
    public static final int CLI_REQICBM_FAMILY = 0x0004;
    public static final int CLI_REQICBM_COMMAND = 0x0004;

    // SRV_REPLYICBM packet family and command
    public static final int SRV_REPLYICBM_FAMILY = 0x0004;
    public static final int SRV_REPLYICBM_COMMAND = 0x0005;

    // CLI_SENDMSG packet family and command
    public static final int CLI_SENDMSG_FAMILY = 0x0004;
    public static final int CLI_SENDMSG_COMMAND = 0x0006;

    // SRV_RECVMSG packet family and command
    public static final int SRV_RECVMSG_FAMILY = 0x0004;
    public static final int SRV_RECVMSG_COMMAND = 0x0007;

    // CLI_ACKMSG packet family and command
    public static final int CLI_ACKMSG_FAMILY = 0x0004;
    public static final int CLI_ACKMSG_COMMAND = 0x000B;

    // SRV_ACKMSG packet family and command
    public static final int SRV_ACKMSG_FAMILY = 0x0004;
    public static final int SRV_ACKMSG_COMMAND = 0x000C;

    /**********************/
    /* Family 0x0009: BOS */
    /**
     * ******************
     */
    public static final int BOS_FAMILY = 0x0009;

    // CLI_REQBOS packet family and command
    public static final int CLI_REQBOS_FAMILY = 0x0009;
    public static final int CLI_REQBOS_COMMAND = 0x0002;

    // SRV_REPLYBOS packet family and command
    public static final int SRV_REPLYBOS_FAMILY = 0x0009;
    public static final int SRV_REPLYBOS_COMMAND = 0x0003;

    /***************************/
    /* Family 0x000B: INTERVAL */
    /***************************/

    // Nothing

    /*************************/
    /* Family 0x0013: ROSTER */
    /**
     * *********************
     */

    public static final int ROSTER_FAMILY = 0x0013;

    // CLI_REQLISTS packet family and command
    public static final int CLI_REQLISTS_FAMILY = 0x0013;
    public static final int CLI_REQLISTS_COMMAND = 0x0002;

    // SRV_REPLYLISTS packet family and command
    public static final int SRV_REPLYLISTS_FAMILY = 0x0013;
    public static final int SRV_REPLYLISTS_COMMAND = 0x0003;

    // CLI_REQROSTER packet family and command
    public static final int CLI_REQROSTER_FAMILY = 0x0013;
    public static final int CLI_REQROSTER_COMMAND = 0x0004;

    // CLI_CHECKROSTER packet family and command
    public static final int CLI_CHECKROSTER_FAMILY = 0x0013;
    public static final int CLI_CHECKROSTER_COMMAND = 0x0005;

    // SRV_REPLYROSTER packet family and command
    public static final int SRV_REPLYROSTER_FAMILY = 0x0013;
    public static final int SRV_REPLYROSTER_COMMAND = 0x0006;

    // CLI_ROSTERACK packet family and command
    public static final int CLI_ROSTERACK_FAMILY = 0x0013;
    public static final int CLI_ROSTERACK_COMMAND = 0x0007;

    // CLI_ROSTERADD packet family and command
    public static final int CLI_ROSTERADD_FAMILY = 0x0013;
    public static final int CLI_ROSTERADD_COMMAND = 0x0008;

    // CLI_ROSTERUPDATE packet family and command
    public static final int CLI_ROSTERUPDATE_FAMILY = 0x0013;
    public static final int CLI_ROSTERUPDATE_COMMAND = 0x0009;

    // CLI_ROSTERDELETE packet family and command
    public static final int CLI_ROSTERDELETE_FAMILY = 0x0013;
    public static final int CLI_ROSTERDELETE_COMMAND = 0x000A;

    // SRV_UPDATEACK packet family and command
    public static final int SRV_UPDATEACK_FAMILY = 0x0013;
    public static final int SRV_UPDATEACK_COMMAND = 0x000E;

    // SRV_REPLYROSTEROK packet family and command
    public static final int SRV_REPLYROSTEROK_FAMILY = 0x0013;
    public static final int SRV_REPLYROSTEROK_COMMAND = 0x000F;

    // CLI_ADDSTART packet family and command
    public static final int CLI_ADDSTART_FAMILY = 0x0013;
    public static final int CLI_ADDSTART_COMMAND = 0x0011;

    // CLI_ADDEND packet family and command
    public static final int CLI_ADDEND_FAMILY = 0x0013;
    public static final int CLI_ADDEND_COMMAND = 0x0012;

    // CLI_REMOVEME packet family and command
    public static final int CLI_REMOVEME_FAMILY = 0x0013;
    public static final int CLI_REMOVEME_COMMAND = 0x0016;

    // CLI_REQAUTH packet family and command
    public static final int CLI_REQAUTH_FAMILY = 0x0013;
    public static final int CLI_REQAUTH_COMMAND = 0x0018;

    // SRV_AUTHREQ packet family and command
    public static final int SRV_AUTHREQ_FAMILY = 0x0013;
    public static final int SRV_AUTHREQ_COMMAND = 0x0019;

    // CLI_AUTHORIZE packet family and command
    public static final int CLI_AUTHORIZE_FAMILY = 0x0013;
    public static final int CLI_AUTHORIZE_COMMAND = 0x001A;

    // SRV_AUTHREPLY packet family and command
    public static final int SRV_AUTHREPLY_FAMILY = 0x0013;
    public static final int SRV_AUTHREPLY_COMMAND = 0x001B;

    // SRV_ADDEDYOU packet family and command
    public static final int SRV_ADDEDYOU_FAMILY = 0x0013;
    public static final int SRV_ADDEDYOU_COMMAND = 0x001C;

    /**************************/
    /* Family 0x0015: OLD ICQ */
    /**
     * **********************
     */

    // SRV_TOICQERR packet family and command
    public static final int SRV_TOICQERR_FAMILY = 0x0015;
    public static final int SRV_TOICQERR_COMMAND = 0x0001;

    // CLI_TOICQSRV packet family and command
    public static final int CLI_TOICQSRV_FAMILY = 0x0015;
    public static final int CLI_TOICQSRV_COMMAND = 0x0002;

    // SRV_FROMICQSRV packet family and command
    public static final int SRV_FROMICQSRV_FAMILY = 0x0015;
    public static final int SRV_FROMICQSRV_COMMAND = 0x0003;

    /****************************************************************************/
    /****************************************************************************/
    /**
     * ************************************************************************
     */

    // The family this SNAC packet belongs to
    protected int family;

    // The command to perform
    protected int command;

    // The snac flags
    protected int snacFlags;

    // Reference number
    protected long reference;

    // Extra data (empty array if not available)
    protected byte[] extData;

    // Data
    protected byte[] data;

    // Constructor
    public SnacPacket(int sequence, int family, int command, int snacFlags, long reference, byte[] extData, byte[] data) {
        //this.sequence = sequence;
        this.family = family;
        this.command = command;
        this.snacFlags = snacFlags;
        this.reference = reference;
        this.extData = extData;
        this.data = data;
    }

    // Constructor
    public SnacPacket(int family, int command, long reference, byte[] extData, byte[] data) {
        this(-1, family, command, 0, reference, extData, data);
    }

    public SnacPacket(int family, int command, int snacFlags, byte[] data) {
        this(-1, family, command, snacFlags, 0, new byte[0], data);
    }

    public SnacPacket(int family, int command, byte[] data) {
        this(-1, family, command, 0, 0, new byte[0], data);
    }

    // Returns the family this SNAC packet belongs to
    public int getFamily() {
        return (this.family);
    }

    // Returns the command to perform
    public int getCommand() {
        return (this.command);
    }

    // Returns the snacFlags
    public int getFlags() {
        return (snacFlags);
    }

    // Returns the reference number
    public long getReference() {
        return (this.reference);
    }

    // Returns a copy of the extra data (empty array if not available)
    public byte[] getExtData() {
        byte[] extData = new byte[this.extData.length];
        System.arraycopy(this.extData, 0, extData, 0, extData.length);
        return (extData);
    }

    // Returns a copy of the data
    public byte[] getData() {
        byte[] data = new byte[this.data.length];
        System.arraycopy(this.data, 0, data, 0, data.length);
        return (data);
    }

    // Returns the packet as byte array
    public byte[] toByteArray() {
        // Allocate memory
        byte buf[] = new byte[6 + 10 + data.length + (this.extData.length > 0 ? 2 + this.extData.length : 0)];

        // Assemble FLAP header
        Util.putByte(buf, 0, 0x2A);   // FLAP.ID
        Util.putByte(buf, 1, 0x02);   // FLAP.CHANNEL
        Util.putWord(buf, 2, sequence);   // FLAP.SEQUENCE
        Util.putWord(buf, 4, 10 + this.data.length + (this.extData.length > 0 ? 2 + this.extData.length : 0));   // FLAP.LENGTH

        // Assemble SNAC header
        Util.putWord(buf, 6, this.family);   // SNAC.FAMILY
        Util.putWord(buf, 8, this.command);   // SNAC.COMMAND
        Util.putWord(buf, 10, (this.extData.length > 0 ? 0x8000 : 0x0000));   // SNAC.FLAGS
        Util.putDWord(buf, 12, this.reference);   // SNAC.REFERENCE;

        // Assemlbe SNAC.DATA
        if (this.extData.length > 0) {
            Util.putWord(buf, 16, this.extData.length);
            System.arraycopy(this.extData, 0, buf, 18, this.extData.length);
            System.arraycopy(this.data, 0, buf, 18 + this.extData.length, this.data.length);
        } else {
            System.arraycopy(this.data, 0, buf, 16, this.data.length);
        }

        // Return
        return (buf);
    }

    // Parses given byte array and returns a SnacPacket object
    public static Packet parse(byte[] buf, int off, int len) throws JimmException {
        // Get FLAP sequence number
        int flapSequence = Util.getWord(buf, off + 2);

        // Get length of FLAP data
        int flapLength = Util.getWord(buf, off + 4);

        // Check length (min. 10 bytes)
        if (flapLength < 10) {
            throw (new JimmException(133, 0));
        }

        // Get SNAC family
        int snacFamily = Util.getWord(buf, off + 6);

        // Get SNAC command
        int snacCommand = Util.getWord(buf, off + 8);

        // Look for CLI_TOICQSRV packet
        if ((snacFamily == SnacPacket.CLI_TOICQSRV_FAMILY) && (snacCommand == SnacPacket.CLI_TOICQSRV_COMMAND)) {
            return (ToIcqSrvPacket.parse(buf, off, len));
        }
        // Look for SRV_FROMICQSRV packet
        else if ((snacFamily == SnacPacket.SRV_FROMICQSRV_FAMILY) && (snacCommand == SnacPacket.SRV_FROMICQSRV_COMMAND)) {
            return (FromIcqSrvPacket.parse(buf, off, len));
        }

        // Get SNAC flags
        int snacFlags = Util.getWord(buf, off + 10);

        // Get SNAC reference
        long snacReference = Util.getDWord(buf, off + 12);

        // Get SNAC data and extra data (if available)
        byte[] extData;
        byte[] data;
        if (snacFlags == 0x8000) {
            // Check length (min. 12 bytes)
            if (flapLength < 10 + 2) {
                throw (new JimmException(133, 1));
            }

            // Get length of extra data
            int extDataLength = Util.getWord(buf, off + 16);

            // Check length (min. 12+extDataLength bytes)
            if (flapLength < 10 + 2 + extDataLength) {
                throw (new JimmException(133, 2));
            }

            // Get extra data
            extData = new byte[extDataLength];
            System.arraycopy(buf, off + 6 + 10 + 2, extData, 0, extDataLength);

            // Get SNAC data
            data = new byte[flapLength - 10 - 2 - extDataLength];
            System.arraycopy(buf, off + 6 + 10 + 2 + extDataLength, data, 0, flapLength - 10 - 2 - extDataLength);
        } else {
            // Get SNAC data
            extData = new byte[0];
            data = new byte[flapLength - 10];
            System.arraycopy(buf, off + 16, data, 0, flapLength - 10);
        }

        // Instantiate SnacPacket
        return (new SnacPacket(flapSequence, snacFamily, snacCommand, snacFlags, snacReference, extData, data));
    }

    // Parses given byte array and returns a SnacPacket object
    public static Packet parse(byte[] buf) throws JimmException {
        return (SnacPacket.parse(buf, 0, buf.length));
    }
}