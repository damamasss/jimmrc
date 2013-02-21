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
 File: src/jimm/comm/SendMessageAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Spassky Alexander, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.*;

public class SendMessageAction extends Action {
    // Plain message
    private PlainMessage plainMsg;

    // #sijapp cond.if modules_FILES is "true"#
    // File transfer request message
    private FileTransferMessage fileTrans;
    // #sijapp cond.end#
    private int SEQ1 = 0xffff;

    // message id
    private int msgId1 = 0;
    private int msgId2 = 0;

    // msg counter variable
    private static int msgCounter = 0;

    // Constructor
    public SendMessageAction(Message msg) {
        super(false, true);
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        if (msg instanceof PlainMessage) {
            this.plainMsg = (PlainMessage) msg;
// #sijapp cond.if modules_FILES is"true"#
            this.fileTrans = null;
// #sijapp cond.end#

            // generate msgId
            msgId1 = (int) System.currentTimeMillis();
            msgId2 = getMsgCounter();
        }
// #sijapp cond.if modules_FILES is "true"#
        else if (msg instanceof FileTransferMessage) {
            this.plainMsg = null;
            this.fileTrans = (FileTransferMessage) msg;
        }
// #sijapp cond.end#
// #sijapp cond.else#
        if (msg instanceof PlainMessage) {
            this.plainMsg = (PlainMessage) msg;
        }
// #sijapp cond.end#
    }

    public long getMsgId() {
        return ((long) msgId1 << 32) + msgId2;
    }

    public static synchronized int getMsgCounter() {
        return (msgCounter++);
    }

    // Init action
    protected void init() throws JimmException {
        // Forward init request depending on message type
        SEQ1--;
        initPlainMsg();
    }

    // Init action for plain messages
    private void initPlainMsg() throws JimmException {
        // Get receiver object
        ContactItem rcvr;

// #sijapp cond.if modules_FILES is "true"#
        if (this.plainMsg != null)
            rcvr = plainMsg.getRcvr();
        else
            rcvr = fileTrans.getRcvr();
// #sijapp cond.else#
        rcvr = this.plainMsg.getRcvr();
// #sijapp cond.end#
        // What message format/encoding should we use?
        int type = 1;
        boolean utf8;
        utf8 = rcvr.hasCapability(ClientID.CAPF_UTF8_INTERNAL);
// #sijapp cond.if modules_FILES is "true"#
        if ((this.fileTrans != null) && (rcvr.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE))
        //&& rcvr.hasCapability(ClientID.CAPF_AIM_SERVERRELAY_INTERNAL))
        {
            type = 2;
        }
// #sijapp cond.end#

        if ((this.plainMsg != null) && ((this.plainMsg.getMessageType() >= Message.MESSAGE_TYPE_AWAY) &&
                (this.plainMsg.getMessageType() <= Message.MESSAGE_TYPE_FFC))) {
            type = 2;
        }

        if (/*Options.getBoolean(Options.OPTION_CHAT_IMAGE) && */icq.getProfile().getBoolean(Profile.OPTION_DELIVERY_REPORT) &&
                rcvr.hasCapability(ClientID.CAPF_AIM_SERVERRELAY_INTERNAL) &&
                rcvr.getIntValue(ContactItem.CONTACTITEM_STATUS) != ContactItem.STATUS_OFFLINE) {
            type = 2;
        }

        //////////////////////
        // Message format 1 //
        //////////////////////

        if (type == 1) {
            // Get UIN
            byte[] uinRaw = Util.stringToByteArray(rcvr.getUinString());

            // Get text
            byte[] textRaw;
            if (utf8) {
                textRaw = Util.stringToUcs2beByteArray(Util.restoreCrLf(this.plainMsg.getText()));
            } else {
                textRaw = Util.stringToByteArray(Util.restoreCrLf(this.plainMsg.getText()));
            }

            // Pack data
            byte[] buf = new byte[10 + 1 + uinRaw.length + 4 + (utf8 ? 6 : 5) + 4 + 4 + textRaw.length + 4];
            int marker = 0;
            Util.putDWord(buf, marker, msgId1); // CLI_SENDMSG.TIME
            marker += 4;
            Util.putDWord(buf, marker, msgId2); // CLI_SENDMSG.ID
            marker += 4;
            Util.putWord(buf, marker, 0x0001); // CLI_SENDMSG.FORMAT
            marker += 2;
            Util.putByte(buf, marker, uinRaw.length); // CLI_SENDMSG.UIN
            System.arraycopy(uinRaw, 0, buf, marker + 1, uinRaw.length);
            marker += 1 + uinRaw.length;
            Util.putWord(buf, marker, 0x0002); // CLI_SENDMSG.SUB_MSG_TYPE1
            Util.putWord(buf, marker + 2, (utf8 ? 6 : 5) + 4 + 4 + textRaw.length);
            marker += 4;
            Util.putWord(buf, marker, 0x0501); // SUB_MSG_TYPE1.CAPABILITIES
            if (utf8) {
                Util.putWord(buf, marker + 2, 0x0002);
                Util.putWord(buf, marker + 4, 0x0106);
                marker += 6;
            } else {
                Util.putWord(buf, marker + 2, 0x0001);
                Util.putByte(buf, marker + 4, 0x01);
                marker += 5;
            }
            Util.putWord(buf, marker, 0x0101); // SUB_MSG_TYPE1.MESSAGE
            Util.putWord(buf, marker + 2, 4 + textRaw.length);
            marker += 4;
            if (utf8) {
                Util.putDWord(buf, marker, 0x00020000); // MESSAGE.ENCODING
            } else {
                Util.putDWord(buf, marker, 0x00000000); // MESSAGE.ENCODING
            }
            marker += 4;
            System.arraycopy(textRaw, 0, buf, marker, textRaw.length); // MESSAGE.MESSAGE
            marker += textRaw.length;
            Util.putWord(buf, marker, 0x0006); // CLI_SENDMSG.UNKNOWN
            Util.putWord(buf, marker + 2, 0x0000);
            marker += 4;

            // Send packet
            SnacPacket snacPkt = new SnacPacket(SnacPacket.CLI_SENDMSG_FAMILY, SnacPacket.CLI_SENDMSG_COMMAND, 0, new byte[0], buf);
            icq.sendPacket(snacPkt);
        }

        //////////////////////
        // Message format 2 //
        //////////////////////

        else if (type == 2) {
            // System.out.println("Send TYPE 2");
            // Get UIN
            byte[] uinRaw = Util.stringToByteArray(rcvr.getUinString());

            // Get text
            byte[] textRaw;

            // Get filename if file transfer
            byte[] filenameRaw;

// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null) {

                textRaw = Util.stringToByteArray(Util.restoreCrLf(this.plainMsg.getText()), true);
                filenameRaw = new byte[0];
            } else {
                textRaw = Util.stringToByteArray(this.fileTrans.getDescription());
                filenameRaw = Util.stringToByteArray(this.fileTrans.getFilename());
            }
// #sijapp cond.else#
            textRaw = Util.stringToByteArray(Util.restoreCrLf(this.plainMsg.getText()), true);
            filenameRaw = new byte[0];
// #sijapp cond.end#

            if ((rcvr.canWin1251() || Jimm.getCurrentProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC))) {
                textRaw = Util.stringToByteArray(Util.restoreCrLf(this.plainMsg.getText()), false);
                if ((textRaw.length & 1) == 0) {
                    byte[] newTextRaw = new byte[textRaw.length + 1];
                    System.arraycopy(textRaw, 0, newTextRaw, 0, textRaw.length);
                    newTextRaw[newTextRaw.length - 1] = ' ';
                    textRaw = newTextRaw;
                }
            } else {
                textRaw = Util.stringToByteArray(Util.restoreCrLf(this.plainMsg.getText()), true);
            }

            // Set length
            // file request: 192 + UIN len + file description (no null) +
            // file name (null included)
            // normal msg: 163 + UIN len + message length;

            int p_sz = 0;

// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null) {
                p_sz = 163 + uinRaw.length + textRaw.length;
            } else {
                p_sz = 192 + uinRaw.length + textRaw.length + filenameRaw.length + 1;
            }
// #sijapp cond.else#
            p_sz = 163 + uinRaw.length + textRaw.length;
// #sijapp cond.end#

            //int tlv5len = 148;
            //int tlv11len = 108;

            // Build the packet
            byte[] buf = new byte[p_sz];
            int marker = 0;

            Util.putDWord(buf, marker, msgId1); // CLI_SENDMSG.TIME
            marker += 4;
            Util.putDWord(buf, marker, msgId2); // CLI_SENDMSG.ID
            marker += 4;
            Util.putWord(buf, marker, 0x0002); // CLI_SENDMSG.FORMAT
            marker += 2;
            Util.putByte(buf, marker, uinRaw.length); // CLI_SENDMSG.UIN
            System.arraycopy(uinRaw, 0, buf, marker + 1, uinRaw.length);
            marker += 1 + uinRaw.length;

            //-----------------TYPE2 Specific Data-------------------
            Util.putWord(buf, marker, 0x0005);
            marker += 2;

            // Length of TLV5 differs betweeen normal message and file requst
// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null) {
                Util.putWord(buf, marker, 144 + textRaw.length, true);
            } else {
                Util.putWord(buf, marker, 173 + textRaw.length + filenameRaw.length + 1);
            }
// #sijapp cond.else#
            Util.putWord(buf, marker, 144 + textRaw.length, true);
// #sijapp cond.end#
            marker += 2;

            Util.putWord(buf, marker, 0x0000);
            marker += 2;

            Util.putDWord(buf, marker, msgId1);
            marker += 4;

            Util.putDWord(buf, marker, msgId2);
            marker += 4;

            System.arraycopy(GUID.CAP_AIM_SERVERRELAY.toByteArray(), 0, buf, marker, 16);
            // SUB_MSG_TYPE2.CAPABILITY
            marker += 16;

            // Set TLV 0x0a to 0x0001
            Util.putDWord(buf, marker, 0x000a0002);
            marker += 4;
            Util.putWord(buf, marker, 0x0001);
            marker += 2;

            // Set emtpy TLV 0x0f
            Util.putDWord(buf, marker, 0x000f0000);
            marker += 4;

// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans != null) {
                // Set TLV 0x03 (IP)
                Util.putWord(buf, marker, 0x0003);
                Util.putWord(buf, marker + 2, 0x0004);
                System.arraycopy(icq.c.getLocalIP(), 0, buf, marker + 4, 4);
                marker += 8;

                // Set TLV 0x05 (port)
                Util.putWord(buf, marker, 0x0005);
                Util.putWord(buf, marker + 2, 0x0002);
                Util.putWord(buf, marker + 4, icq.c.getLocalPort());
                marker += 6;
            }
// #sijapp cond.end#
            // Set TLV 0x2711
            Util.putWord(buf, marker, 0x2711);
            marker += 2;

            // Length of TLV2711 differs betweeen normal message and file requst
// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null) {
                Util.putWord(buf, marker, 104 + textRaw.length, true);
            } else {
                Util.putWord(buf, marker, 119 + textRaw.length + filenameRaw.length + 1);
            }
// #sijapp cond.else#
            Util.putWord(buf, marker, 104 + textRaw.length, true);
// #sijapp cond.end#
            marker += 2;
            // Put 0x1b00 (unknown)
            Util.putWord(buf, marker, 0x1B00);
            marker += 2;

            // Put ICQ protocol version in LE
            Util.putWord(buf, marker, 0x0800);
            marker += 2;

            // Put capablilty (16 zero bytes)
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;

            // Put some unknown stuff
            Util.putWord(buf, marker, 0x0000);
            marker += 2;
            Util.putByte(buf, marker, 0x03);
            marker += 1;

            // Set the DC_TYPE to "normal" if we send a file transfer request
// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null)
                Util.putDWord(buf, marker, 0x00000000);
            else
                Util.putDWord(buf, marker, 0x00000004);
// #sijapp cond.else#
            Util.putDWord(buf, marker, 0x00000000);
// #sijapp cond.end#
            marker += 4;
            // Put cookie, unkown 0x0e00 and cookie again
            Util.putWord(buf, marker, SEQ1, false);
            marker += 2;
            Util.putWord(buf, marker, 0x0e, false);
            marker += 2;
            Util.putWord(buf, marker, SEQ1, false);
            marker += 2;

            // Put 12 unknown zero bytes
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;

            // Put message type 0x0001 if normal message else 0x001a for file request
// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null)
                Util.putWord(buf, marker, this.plainMsg.getMessageType(), false);
            else
                Util.putWord(buf, marker, this.fileTrans.getMessageType(), false);
// #sijapp cond.else#
            Util.putWord(buf, marker, this.plainMsg.getMessageType(), false);
// #sijapp cond.end#
            marker += 2;

            // Put contact status
            Util.putWord(buf, marker, Util.translateStatusSend(icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS)), false);
            marker += 2;

            // Put priority
/*
                int priority;

                if (rcvr.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_DND ||
					rcvr.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_OCCUPIED)
					priority = 0x02;
				else
					priority = 0x01;
*/
            Util.putWord(buf, marker, 0x02, false); // приоритет был 0x01... из за аськи 2003 изменил на два. условия выше...
            marker += 2;
            // Put message
// #sijapp cond.if modules_FILES is "true"#
            if (this.fileTrans == null) {
                // Put message length
                Util.putWord(buf, marker, textRaw.length + 1, false);
                marker += 2;

                // Put message
                System.arraycopy(textRaw, 0, buf, marker, textRaw.length); // TLV.MESSAGE
                marker += textRaw.length;
                Util.putByte(buf, marker, 0x00);
                marker++;

                // Put foreground, background color and guidlength
                Util.putDWord(buf, marker, 0x00000000);
                marker += 4;
                Util.putDWord(buf, marker, 0xFFFFFF00);
                marker += 4;
                Util.putDWord(buf, marker, 0x26000000);
                marker += 4;

                if (rcvr.canWin1251() || Jimm.getCurrentProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC)) {
                    System.arraycopy(GUID.CAP_WIN1251_GUID.toByteArray(), 0, buf, marker, 38);
                } else {
                    System.arraycopy(GUID.CAP_UTF8_GUID.toByteArray(), 0, buf, marker, 38);
                }
                // SUB_MSG_TYPE2.CAPABILITY
                marker += 38;
            } else {
                // Put message length
                Util.putWord(buf, marker, 1, false);
                marker += 2;

                // Put message (unused in file transfer request)
                Util.putByte(buf, marker, 0x00);
                marker++;
                // Put file transfer request command
                Util.putWord(buf, marker, 0x029, false);
                marker += 2;

                // Put 16 bytes of unknown binary stuff
                Util.putDWord(buf, marker, 0xf02d12d9);
                marker += 4;
                Util.putDWord(buf, marker, 0x3091d311);
                marker += 4;
                Util.putDWord(buf, marker, 0x8dd70010);
                marker += 4;
                Util.putDWord(buf, marker, 0x4b06462e);
                marker += 4;

                // Put 2 unknown zero bytes
                Util.putWord(buf, marker, 0x0000);
                marker += 2;

                // Put request type string length and string
                Util.putDWord(buf, marker, 0x0004, false);
                marker += 4;
                System.arraycopy(Util.stringToByteArray("File"), 0, buf, marker, 4);
                marker += 4;

                // Put 15 bytes of unknown binary stuff
                Util.putDWord(buf, marker, 0x00000100);
                marker += 4;
                Util.putDWord(buf, marker, 0x00010000);
                marker += 4;
                Util.putDWord(buf, marker, 0x00000000);
                marker += 4;
                Util.putWord(buf, marker, 0x0000);
                marker += 2;
                Util.putByte(buf, marker, 0x00);
                marker++;

                // Put remaining length
                Util.putDWord(buf, marker, 18 + textRaw.length + filenameRaw.length + 1, false);
                marker += 4;

                // Put description length and text
                Util.putDWord(buf, marker, textRaw.length, false);
                marker += 4;
                System.arraycopy(textRaw, 0, buf, marker, textRaw.length);
                marker += textRaw.length;

                // Put 4 unknown bytes
                Util.putDWord(buf, marker, 0x8c820222);
                marker += 4;

                // Put filename length and filename
                Util.putWord(buf, marker, filenameRaw.length + 1, false);
                marker += 2;
                System.arraycopy(filenameRaw, 0, buf, marker, filenameRaw.length);
                marker += filenameRaw.length;
                Util.putByte(buf, marker, 0x00);
                marker += 1;

                // Put total size of file transfer
                Util.putDWord(buf, marker, this.fileTrans.getSize(), false);
                marker += 4;
                // Put unknown 4 bytes
                Util.putDWord(buf, marker, 0x00008c82, false);
                marker += 4;
            }
// #sijapp cond.else#
            // Put message length
            Util.putWord(buf, marker, textRaw.length + 1, false);
            marker += 2;

            // Put message
            System.arraycopy(textRaw, 0, buf, marker, textRaw.length); // TLV.MESSAGE
            marker += textRaw.length;
            Util.putByte(buf, marker, 0x00);
            marker++;
            // Put foreground, background color and guidlength
            Util.putDWord(buf, marker, 0x00000000);
            marker += 4;
            Util.putDWord(buf, marker, 0xFFFFFF00);
            marker += 4;
            Util.putDWord(buf, marker, 0x26000000);
            marker += 4;
            if (rcvr.canWin1251() || Jimm.getCurrentProfile().getBoolean(Profile.OPTION_ECONOM_TRAFFIC)) {
                System.arraycopy(GUID.CAP_WIN1251_GUID.toByteArray(), 0, buf, marker, 38);
            } else {
                System.arraycopy(GUID.CAP_UTF8_GUID.toByteArray(), 0, buf, marker, 38);
            }
            // SUB_MSG_TYPE2.CAPABILITY
            marker += 38;
// #sijapp cond.end#

            // Put TLV 0x03
            Util.putWord(buf, marker, 0x0003, true); // CLI_SENDMSG.UNKNOWN
            marker += 2;
            Util.putWord(buf, marker, 0x0000);
            marker += 2;
            // Send packet
            SnacPacket snacPkt = new SnacPacket(SnacPacket.CLI_SENDMSG_FAMILY, SnacPacket.CLI_SENDMSG_COMMAND, 0, new byte[0], buf);
            icq.sendPacket(snacPkt);
            // System.out.println("SendMessageAction: Sent the packet");
        }

        SEQ1--;
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