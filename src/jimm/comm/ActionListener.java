/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-06  Jimm Project

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
 File: src/jimm/comm/ActionListener.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Spassky Alexander, Igor Palkin
 *******************************************************************************/

package jimm.comm;

import jimm.*;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Vector;

public class ActionListener {
    /**
     * ************************************************************************
     */
    private Icq icq;

    public ActionListener(Icq icq) {
        this.icq = icq;
    }

    // Forwards received packet
    protected void forward(Packet packet) throws JimmException {
        // Watch out for channel 4 (Disconnect) packets
        if (packet instanceof DisconnectPacket) {
            DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
            icq.canReconnect = false;

            // Throw exception
            if (disconnectPacket.getError() == 0x0001) { // Multiple logins
                throw (new JimmException(110, 0));
            } else { // Unknown error
                throw (new JimmException(100, 0));
            }
        }
        /** *********************************************************************** */
        if (packet instanceof FromIcqSrvPacket) {
//#sijapp cond.if modules_DEBUGLOG is "true"#
            DebugLog.addText("Offline messages");
//#sijapp cond.end#
            FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;
            // Watch out for SRV_OFFLINEMSG
            if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_OFFLINEMSG_SUBCMD) {
                // Get raw data
                byte[] buf = fromIcqSrvPacket.getData();
                // Check length
                if (buf.length >= 14) {
                    // Extract UIN
                    long uinRaw = Util.getDWord(buf, 0, false);
                    String uin = String.valueOf(uinRaw);
                    // Extract date of dispatch
                    long date = DateAndTime.createLongTime(
                            Util.getWord(buf, 4, false),
                            Util.getByte(buf, 6),
                            Util.getByte(buf, 7),
                            Util.getByte(buf, 8),
                            Util.getByte(buf, 9),
                            0
                    );
                    // Get type
                    int type = Util.getWord(buf, 10, false);
                    // Get text length
                    int textLen = Util.getWord(buf, 12, false);

                    // Check length
                    if (buf.length >= 14 + textLen) {
                        String text = null;
                        // Get text
                        try {
                            text = Util.removeCr(Util.byteArrayToString(buf, 14, textLen, Util.isDataUTF8(buf, 14, textLen))); // old
                            // String text = Util.removeCr(Util.byteArrayToString(buf, 14, textLen, true)); // new
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            JimmException.handleExceptionEx(aioobe);
                        }
                        if (text == null) {
                        } else if (type == 0x0001) {
                            // Forward message to contact list
                            PlainMessage message = new PlainMessage(uin, icq.getUin(), DateAndTime.gmtTimeToLocalTime(date), text, true);
                            addMessage(message);
                        } else if (type == 0x0004) {
                            // Search for delimiter
                            int delim = text.indexOf(0xFE);
                            // Split message, if delimiter could be found
                            String urlText;
                            String url;
                            if (delim != -1) {
                                urlText = text.substring(0, delim);
                                url = text.substring(delim + 1);
                            } else {
                                urlText = text;
                                url = "";
                            }
                            // Forward message message to contact list
                            UrlMessage message = new UrlMessage(uin, icq.getUin(), DateAndTime.gmtTimeToLocalTime(date), url, urlText);
                            addMessage(message);
                        }
                    }
                }
            } else if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_DONEOFFLINEMSGS_SUBCMD) {
//#sijapp cond.if modules_DEBUGLOG is "true"#
                DebugLog.addText("Offline messages -> packet sending");
//#sijapp cond.end#
                // Send a CLI_TOICQSRV/CLI_ACKOFFLINEMSGS packet
                ToIcqSrvPacket reply = new ToIcqSrvPacket(0x00000000, icq.getUin(), ToIcqSrvPacket.CLI_ACKOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
                icq.sendPacket(reply);
            }
        }
        /** *********************************************************************** */

        // Watch out for channel 2 (SNAC) packets
        if (packet instanceof SnacPacket) {
            SnacPacket snacPacket = (SnacPacket) packet;

            // Watch out for CLI_ACKMSG_COMMAND packets
            if ((snacPacket.getFamily() == SnacPacket.CLI_ACKMSG_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.CLI_ACKMSG_COMMAND)) {
                // Get raw data
                byte[] buf = snacPacket.getData();

                // Get length of the uin (needed for skipping)
                int uinLen = Util.getByte(buf, 10);

                // Get message type
                if (buf.length > (58 + uinLen)) {
                    int msgType = Util.getWord(buf, 58 + uinLen, false);
                    // Get uin
                    String uin = Util.byteArrayToString(buf, 11, uinLen);
                    // Get msgId
                    long msgId = (Util.getDWord(buf, 0) << 32) + Util.getDWord(buf, 4);

                    icq.getProfile().getChatHistory().AckMessage(uin, msgId, true);

                    if ((msgType >= Message.MESSAGE_TYPE_AWAY) && (msgType <= Message.MESSAGE_TYPE_FFC) && icq.getProfile().getItemByUIN(uin).readStatusMess) {
                        // Create an message entry
                        int textLen = Util.getWord(buf, 64 + uinLen, false);
                        ContactItem contact = icq.getProfile().getItemByUIN(uin);
// #sijapp cond.if modules_MAGIC_EYE is "true" #
                        if (contact.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE) {
                            contact.setStatus(ContactItem.STATUS_INVISIBLE, false);
                            if (MagicEye.getBooleanValue(MagicEye.OPTION_STATUS_INVISIBLE)) {
                                icq.getProfile().getMagicEye().addAction(uin, "status_invisible");
                            }
                        }
// #sijapp cond.end #
                        icq.getProfile().getChatHistory().getChat(contact).addTextToForm("", Util.byteArrayToString(buf, 66 + uinLen, textLen, false), "", 0, true, false, ContactList.imageList.elementAt(contact.getImageIndex()), 0);
                        contact.readStatusMess = false;
                        if (Jimm.getContactList().isActive() && Jimm.getContactList().contain(contact)) {
                            contact.activate();
                        }
                    }
                }
            }

            // Typing notify
            //#sijapp cond.if target isnot "DEFAULT"#
            if ((snacPacket.getFamily() == 0x0004) && (snacPacket.getCommand() == 0x0014) && Options.getInt(Options.OPTION_TYPING_MODE) > 0) {
                byte[] p = snacPacket.getData();
                int uin_len = Util.getByte(p, 10);
                String uin = Util.byteArrayToString(p, 11, uin_len);
                int flag = Util.getWord(p, 11 + uin_len);

                if (flag == 0x0002) {
                    ContactItem cItem = icq.getProfile().BeginTyping(uin, true);
// #sijapp cond.if modules_MAGIC_EYE is "true" #
                    if ((cItem != null) && (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE)) {
                        cItem.setStatus(ContactItem.STATUS_INVISIBLE, false);
                        if (MagicEye.getBooleanValue(MagicEye.OPTION_STATUS_INVISIBLE)) {
                            icq.getProfile().getMagicEye().addAction(uin, "status_invisible");
                        }
                    }
// #sijapp cond.end #
                } else {
                    icq.getProfile().BeginTyping(uin, false);
                }
            }
            //#sijapp cond.end#

            /************************************ICQ XTraz Support*********************************/

            if (snacPacket.getFamily() == 0x0004 && snacPacket.getCommand() == 0x000b) {
                byte abyte3[];
                abyte3 = snacPacket.getData();
                int j5 = Util.getByte(abyte3, 10); //UIN LEN
                String s6 = Util.byteArrayToString(abyte3, 11, j5); //UIN
                int j1 = 11 + j5;
                j1 += 47; // (2+2+1+16+4+4+2+2+2+4+4+4)
                int k6 = Util.getWord(abyte3, j1, false); //00 1a - unknown...msg type?
                j1 += 6; // (2+2+2)
                if (k6 == 26) {
                    j1 += 3; // (2+1)
                    int j7 = Util.getWord(abyte3, j1, false); //00 4f
                    j1 += 2;
                    if (j7 != 79) { //throw (new Exception("509, 1"));
                        return;
                    }
                    j1 += 18; // (16+2)
                    long l12 = Util.getDWord(abyte3, j1, false);
                    j1 += 4;
                    String s13 = Util.byteArrayToString(abyte3, j1, (int) l12);
                    j1 = (int) ((long) j1 + l12);
                    j1 += 15; // (4+4+4+2+1)
                    if (s13 != null && s13.compareTo("Script Plug-in: Remote Notification Arrive") == 0) {
                        j1 += 8; // (4+4)
                        int l14 = (abyte3.length) - j1;
                        String s14 = Util.byteArrayToString(abyte3, j1, l14, true);
                        // System.out.println("Message is: " + s14);
                        try {
                            b(s6, s14);
                        } catch (Exception e) {
                            JimmException.handleExceptionEx(e);
                        }
                    }
                }
            }

            /***************************************************************************************/

            // Watch out for SRV_USERONLINE packets
            if (((snacPacket.getFamily() == SnacPacket.SRV_USERONLINE_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_USERONLINE_COMMAND))
                    || ((snacPacket.getFamily() == 0x0002) && (snacPacket.getCommand() == 0x0006))) {
                // DC variables
                byte[] internalIP = new byte[4];
                byte[] externalIP = new byte[4];
                int dcPort = 0;
                int dcType = -1;
                int icqProt = 0;
                int authCookie = 0;

                //boolean statusChange = true;
                boolean checkStatus = ((snacPacket.getFamily() == 0x0002) && (snacPacket.getCommand() == 0x0006));
                int dwFT1 = 0, dwFT2 = 0, dwFT3 = 0;
                int wVersion = 0;
                byte[] capabilities_old = null; // Buffer for old style capabilities (TLV 0x000D)
                byte[] capabilities_new = null; // Buffer for new style capabilities (TLV 0x0019)

                // Time variables
                int idle = -1;
                int online = -1;
                //int signon = -1;
                int regdata = -1;


                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int uinLen = Util.getByte(buf, 0);
                String uin = Util.byteArrayToString(buf, 1, uinLen);
                String moodText = null;
                int moodIcon = -1;
                // Get new status and client capabilities
                int status = checkStatus ? ContactItem.STATUS_OFFLINE : ContactItem.STATUS_ONLINE;
                //int xstatusIndex = -1;
                int marker = 1 + uinLen + 2;
                int tlvNum = Util.getWord(buf, marker);
                marker += 2;
                byte[] tlvData;
                for (int i = 0; i < tlvNum; i++) {
                    int tlvType = Util.getWord(buf, marker);
                    tlvData = Util.getTlv(buf, marker);
                    if (tlvType == 0x0006) {
                        status = (int) Util.getDWord(tlvData, 0);
                    } else if (tlvType == 0x000D) {
                        capabilities_old = tlvData;
                    } else if (tlvType == 0x0019) {
                        capabilities_new = tlvData;
                    } else if (tlvType == 0x000A) {
                        System.arraycopy(tlvData, 0, externalIP, 0, 4);
                    } else if (tlvType == 0x000c) {
                        // dcMarker
                        int dcMarker = 0;

                        // Get internal IP
                        System.arraycopy(tlvData, dcMarker, internalIP, 0, 4);
                        dcMarker += 4;

                        // Get tcp port
                        dcPort = (int) Util.getDWord(tlvData, dcMarker);
                        dcMarker += 4;

                        // Get DC type
                        dcType = Util.getByte(tlvData, dcMarker);
                        dcMarker++;

                        // Get protocol version
                        icqProt = Util.getWord(tlvData, dcMarker);
                        dcMarker += 2;

                        // Get auth cookie
                        authCookie = (int) Util.getDWord(tlvData, dcMarker);
                        dcMarker += 12;

                        // Get data for client detection
                        dwFT1 = (int) Util.getDWord(tlvData, dcMarker);
                        dcMarker += 4;
                        dwFT2 = (int) Util.getDWord(tlvData, dcMarker);
                        dcMarker += 4;
                        dwFT3 = (int) Util.getDWord(tlvData, dcMarker);

                        //statusChange = false;
                        //} else if (tlvType == 0x0003)  {
                        //	signon = (int)DateAndTime.gmtTimeToLocalTime(Util.byteArrayToLong(tlvData));
                    } else if (tlvType == 0x0004) {
                        idle = (int) Util.byteArrayToLong(tlvData) / 256;
                    } else if (tlvType == 0x000F) {
                        online = (int) Util.byteArrayToLong(tlvData);
                    } else if (tlvType == 0x0005) {
                        regdata = (int) Util.byteArrayToLong(tlvData);
                    } else if (tlvType == 0x001D) {// Icon service... and new style status message
                        int iconMarker = 0;
                        while (iconMarker < tlvData.length) {
                            int bartType = Util.getWord(tlvData, iconMarker);
                            int iconLen = Util.getByte(tlvData, iconMarker + 3);

                            final int BART_STATUS_STR = 0x0002;
                            final int BART_STATUS_ID = 0x000E;
                            if (iconLen == 0) {
                            } else if (bartType == BART_STATUS_STR) {
                                int len = Util.getWord(tlvData, iconMarker + 4);
                                int pos = iconMarker + 6;
                                moodText = Util.byteArrayToString(tlvData, pos, len, true);
                            } else if (bartType == BART_STATUS_ID) {
                                int pos = iconMarker + 4;
                                String moodIndex = Util.byteArrayToString(tlvData, pos, iconLen);
                                moodIcon = Util.strToIntDef(moodIndex.substring(7), -1);
                            }
                            iconMarker += 2 + 1 + 1 + iconLen;
                        }
                    }
                    marker += 2 + 2 + tlvData.length;
                }

                // Update contact list
                ContactItem item = icq.getProfile().getItemByUIN(uin);
                if (item != null) {
                    //xstatusIndex = item.getXStatus().getStatusIndex();
                    byte[] capsArray = new byte[0];
                    capsArray = Util.mergeCapabilities(capabilities_old, capabilities_new);
                    item.setXStatus(capsArray);

                    if (item.hasOnlyMood() && (moodIcon = XStatus.toNormal(moodIcon)) != -1) {
                        //item.setMood(moodText, XStatus.mergeMood(moodIcon));
                        item.setMood(moodText, moodIcon);
                    }
                    // Is mood
                    //item.setStatusFlags((status >> 16) & 0xFFFF);
                    item.setStatusImage();
                    int qipStatus = ClientID.detectQipStatus(capsArray);
                    item.setStatus((qipStatus != ContactItem.STATUS_NONE) ? qipStatus : Util.translateStatusReceived(status), online);
                    if (dcType != -1) {
                        item.setIPValue(ContactItem.CONTACTITEM_INTERNAL_IP, internalIP);
                        item.setIPValue(ContactItem.CONTACTITEM_EXTERNAL_IP, externalIP);
                        item.setIntValue(ContactItem.CONTACTITEM_DC_PORT, dcPort);
                        item.setIntValue(ContactItem.CONTACTITEM_DC_TYPE, dcType);
                        item.setIntValue(ContactItem.CONTACTITEM_ICQ_PROT, icqProt);
                        item.setIntValue(ContactItem.CONTACTITEM_AUTH_COOKIE, authCookie);
                    }
                    item.setIntValue(ContactItem.CONTACTITEM_SIGNON, (int) (DateAndTime.createCurrentDate(false) - online));
                    item.setIntValue(ContactItem.CONTACTITEM_REGDATA, regdata);
                    //item.setIntValue(ContactItem.CONTACTITEM_ONLINE,  online);
                    item.setIntValue(ContactItem.CONTACTITEM_IDLE, idle);
                    ClientID.detectClient0(item, dwFT1, dwFT2, dwFT3, capsArray, icqProt);
                }
                //if (checkStatus) Jimm.setDisplay(new jimm.ui.PopUp(null, item.name + '\n' + JimmUI.getStatusString(status), VirtualList.getWidth(), 2, 2));
                //if (checkStatus) System.out.println(""+Util.translateStatusReceived(status));
//#sijapp cond.if modules_DEBUGLOG is "true"#
//				DebugLog.addText("USER_ONLINE for " + uin + " (0x" + Integer.toHexString(status) + ")");
//#sijapp cond.end#
                //Util.detectUserClient(uin, dwFT1, dwFT2, dwFT3, Util.mergeCapabilities(capabilities_old, capabilities_new), icqProt, statusChange);
            }

            /** ********************************************************************* */

            // Watch out for SRV_USEROFFLINE packets
            if ((snacPacket.getFamily() == SnacPacket.SRV_USEROFFLINE_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_USEROFFLINE_COMMAND)) {
                // Get raw data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact that goes offline
                int uinLen = Util.getByte(buf, 0);
                String uin = Util.byteArrayToString(buf, 1, uinLen);

                ContactItem item = icq.getProfile().getItemByUIN(uin);
                if (item != null) {
                    long oldStatus = item.getIntValue(item.CONTACTITEM_STATUS);
                    // Update contact list
                    //RunnableImpl.callSerially(RunnableImpl.TYPE_USER_OFFLINE, uin);
//#sijapp cond.if modules_SOUNDS is "true"#
                    Jimm.getContactList().playOfflineNotif(item);
//#sijapp cond.end#
                    item.offline();
                    //if (Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE)) {
                    //	Jimm.getContactList().update(item);
                    //}
                    if (item.getExtraValue(ContactItem.EXTRA_WATCH) && oldStatus != ContactItem.STATUS_OFFLINE) {
                        icq.getProfile().getChatHistory().getChat(item).addExtraNotice("offline", ContactList.imageList.elementAt(item.getImageIndex()));
                    }
// #sijapp cond.if modules_MAGIC_EYE is "true" #
                    if (oldStatus == ContactItem.STATUS_OFFLINE /*&& !item.getUinString().equals("203738837") */ && icq.isConnected()) { // UIN  Rishat Shamsutdinov
                        if (MagicEye.getBooleanValue(MagicEye.OPTION_MAYBE_INVISIBLE)) {
                            icq.getProfile().getMagicEye().addAction(item.getUinString(), "maybe_invisible");
                        }
                    }
// #sijapp cond.end#
                    //#sijapp cond.if modules_PANEL is "true"#
                    icq.getProfile().showPopupItem(item.name, ContactList.imageList.elementAt(16), item);
                    //#sijapp cond.end#
                }
            }

            /** ********************************************************************* */

            // Watch out for SRV_ACKMSG
            else if ((snacPacket.getFamily() == SnacPacket.SRV_ACKMSG_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_ACKMSG_COMMAND)) {
                // Get data
                byte[] buf = snacPacket.getData();

                if (buf.length > 11) {
                    // Get msgId
                    long msgId = (Util.getDWord(buf, 0) << 32) + Util.getDWord(buf, 4);

                    // Get UIN of the contact changing status
                    int uinLen = Util.getByte(buf, 10);
                    String uin = Util.byteArrayToString(buf, 11, uinLen);

                    icq.getProfile().getChatHistory().AckMessage(uin, msgId, false);
                }
            }

            /* ********************************************************************** */

            // Watch out for SRV_RECVMSG
            else if ((snacPacket.getFamily() == SnacPacket.SRV_RECVMSG_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_RECVMSG_COMMAND)) {
                // Get raw data, initialize marker
                byte[] buf = snacPacket.getData();
//				System.out.println("Data(String): "+new String(buf)+"\nData(HEX): "+Util.byteArrayToHexString(buf)+"\nFamily: "+snacPacket.getFamily()+"\nCommand: "+snacPacket.getCommand()+"\n");
                int marker = 0;

                // Check length
                if (buf.length < 11) {
                    throw (new JimmException(150, 0, false));
                }

                /****************************ICQ XTraz Support****************************/
                long l3 = Util.getDWord(buf, 0, false);
                long l5 = Util.getDWord(buf, 4, false);
                /*************************************************************************/

                // Get message format
                marker += 8;
                int format = Util.getWord(buf, marker);
                marker += 2;

                // Get UIN length
                int uinLen = Util.getByte(buf, marker);
                marker += 1;

                // Check length
                if (buf.length < marker + uinLen + 4) {
                    throw (new JimmException(150, 1, false));
                }

                // Get UIN
                String uin = Util.byteArrayToString(buf, marker, uinLen);
                marker += uinLen;

                // Skip WARNING
                marker += 2;

                // Skip TLVS
                int tlvCount = Util.getWord(buf, marker);
                marker += 2;
                byte[] tlvData;
                for (int i = 0; i < tlvCount; i++) {
                    tlvData = Util.getTlv(buf, marker);
                    if (tlvData == null) {
                        throw (new JimmException(150, 2, false));
                    }

                    marker += 4 + tlvData.length;
                }

                // Get message data and initialize marker
                byte[] msgBuf;
                int tlvType;
                do {
                    msgBuf = Util.getTlv(buf, marker);
                    if (msgBuf == null) {
                        throw (new JimmException(150, 3, false));
                    }
                    tlvType = Util.getWord(buf, marker);
                    marker += 4 + msgBuf.length;
                } while ((tlvType != 0x0002) && (tlvType != 0x0005));
                int msgMarker = 0;
                //#sijapp cond.if modules_DEBUGLOG is "true" #
//				DebugLog.addText("Message Format: "+format);
                //#sijapp cond.end #
//				System.out.println("Message Format: "+format);

                //////////////////////
                // Message format 1 //
                //////////////////////
                if (format == 0x0001) {
                    // Variables for all possible TLVs
                    // byte[] capabilities = null;
                    byte[] message = null;

                    // Read all TLVs
                    byte[] tlvValue;
                    while (msgMarker < msgBuf.length) {
                        // Get next TLV
                        tlvValue = Util.getTlv(msgBuf, msgMarker);
                        if (tlvValue == null) {
                            throw (new JimmException(151, 0, false));
                        }

                        // Get type of next TLV
                        tlvType = Util.getWord(msgBuf, msgMarker);

                        // Update markers
                        msgMarker += 4 + tlvValue.length;

                        // Save value
                        switch (tlvType) {
                            case 0x0501:
                                // capabilities
                                // capabilities = tlvValue;
                                break;
                            case 0x0101:
                                // message
                                message = tlvValue;
                                break;
                            //default: // из-за этого не приходили сообщения с QIP PDA (Windows)
                            //    throw (new JimmException(151, 1, false));
                        }
                    }

                    // Process packet if at least the message TLV was present
                    if (message != null) {
                        // Check length of message
                        if (message.length < 4) {
                            throw (new JimmException(151, 2, false));
                        }

                        // Get message text
                        String text;
                        if (Util.getWord(message, 0) == 0x0002) {
                            text = Util.removeCr(Util.ucs2beByteArrayToString(message, 4, message.length - 4));
                        } else {
                            text = Util.removeCr(Util.byteArrayToString(message, 4, message.length - 4));
                        }
//						System.out.println("Incomming message: "+text);

                        // Construct object which encapsulates the received plain message
                        PlainMessage plainMsg = new PlainMessage(uin, icq.getUin(), DateAndTime.createCurrentDate(false), text, false);
                        addMessage(plainMsg);

                        ContactItem contact = icq.getProfile().getItemByUIN(uin);
//#sijapp cond.if modules_SBOLTUN is "true"#
                        if (contact != null && contact.getInvisibleId() == 0)
                            jimm.chat.ChatTextList.sBoltunInput(plainMsg.getText(), contact);
//#sijapp cond.end#
                        //if ((Options.getBoolean(Options.OPTION_AUTO_ANSWER)) && (contact != null)
                        //        && (!contact.autoAnswered) && (contact.getInvisibleId() == 0))
                        //    sendAutoMessage(contact);
                    }
                }
                //////////////////////
                // Message format 2 //
                //////////////////////
                else if (format == 0x0002) {
                    // TLV(A): Acktype 0x0000 - normal message
                    //                 0x0001 - file request / abort request
                    //                 0x0002 - file ack

                    // Check length
                    if (msgBuf.length < 10) {
                        throw (new JimmException(152, 0, false));
                    }

                    // Get and validate SUB_MSG_TYPE2.COMMAND
                    int command = Util.getWord(msgBuf, msgMarker);
                    //#sijapp cond.if modules_DEBUGLOG is "true" #
                    //DebugLog.addText(icq.getProfile().getItemByUIN(uin).getText()+"\nActionListener\nCommand:\n"+command);
                    //#sijapp cond.end #
                    if (command != 0x0000) {
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						DebugLog.addText("Non-normal command: "+command);
                        //#sijapp cond.end #
//						System.out.println("Non-normal command: "+command);
                        return; // Only normal messages are supported yet
                    }
//					System.out.println("Command is normal");
                    msgMarker += 2;

                    // Skip SUB_MSG_TYPE2.TIME and SUB_MSG_TYPE2.ID
                    msgMarker += 4 + 4;

                    // Check length
                    if (msgBuf.length < msgMarker + 16) {
                        throw (new JimmException(152, 1, false));
                    }

                    // Skip SUB_MSG_TYPE2.CAPABILITY
                    msgMarker += 16;

                    // Get message data and initialize marker
                    byte[] msg2Buf;
// #sijapp cond.if modules_FILES is "true"#
                    int ackType = -1;
                    byte[] extIP = new byte[4];
                    byte[] ip = new byte[4];
                    String port = "0";
                    int status = -1;
// #sijapp cond.end#

                    do {
                        msg2Buf = Util.getTlv(msgBuf, msgMarker);
                        if (msg2Buf == null) {
                            throw (new JimmException(152, 2, false));
                        }
                        tlvType = Util.getWord(msgBuf, msgMarker);
// #sijapp cond.if modules_FILES is "true"#
                        switch (tlvType) {
                            case 0x0003:
                                System.arraycopy(msg2Buf, 0, extIP, 0, 4);
                                break;
                            case 0x0004:
                                System.arraycopy(msg2Buf, 0, ip, 0, 4);
                                break;
                            case 0x0005:
                                port = Util.byteArrayToString(msg2Buf);
                                break;
                            case 0x000a:
                                ackType = Util.getWord(msg2Buf, 0);
                                break;
                        }
// #sijapp cond.end#
                        msgMarker += 4 + msg2Buf.length;
                    } while (tlvType != 0x2711);
                    //#sijapp cond.if modules_DEBUGLOG is "true" #
//					DebugLog.addText("Msg2Buf: "+Util.byteArrayToHexString(msg2Buf)+"\nAckType: "+ackType);
                    //#sijapp cond.end #
//					System.out.println("Msg2Buf: "+Util.byteArrayToHexString(msg2Buf));

                    int msg2Marker = 0;
//					boolean file = false;
                    //#sijapp cond.if modules_DEBUGLOG is "true" #
                    //int defsize = 2 + 2 + 16 + 3 + 4 + 2 + 2 + 2 + 12 + 2 + 2 + 2 + 2;
                    //if ((ackType == 1) && (msg2Buf.length < defsize)) {
                    //DebugLog.addText(icq.getProfile().getItemByUIN(uin).getText()+"\nActionListener\nData size:\n"+msg2Buf.length+
                    //	"\nDefault size:\n"+defsize+"\nPacket as string:\n"+Util.byteArrayToString(msg2Buf, Util.isDataUTF8(msg2Buf)));
                    //}
                    //#sijapp cond.end #
                    // Check length
//					System.out.println("Packet length: "+msg2Buf.length);
                    if (msg2Buf.length < 2 + 2 + 16 + 3 + 4 + 2 + 2 + 2 + 12 + 2 + 2 + 2 + 2) {
//						System.out.println("Packet length: "+msg2Buf.length+"\nPacket: "+Util.byteArrayToHexString(msg2Buf));
//						if (msg2Buf.length < 8) {
                        throw (new JimmException(152, 3, false));
//						}// else file = true;
                    }

//					if (file) {
//						ContactItem sender = icq.getProfile().getItemByUIN(uin);
//						sender.setIPValue (ContactItem.CONTACTITEM_INTERNAL_IP,ip);
//						sender.setIPValue (ContactItem.CONTACTITEM_EXTERNAL_IP,extIP);
//						try {
//							sender.setIntValue (ContactItem.CONTACTITEM_DC_PORT, Integer.parseInt(port));
//						} catch (Exception e) {
//							System.out.println("Port: "+port);
//							return;
//						}
//						long size = Util.getDWord(msg2Buf, 4);
//						String name = new String(msg2Buf, 8, msg2Buf.length - 9);
//						file(size, name, uin);
//						try {
//							icq.requestAction(new DirectConnectionAction(sender, size, name));
//						} catch (JimmException e) {
//							JimmException.handleException(e);
//							if (e.isCritical()) return;
//						}
//						//#sijapp cond.if modules_DEBUGLOG is "true" #
//						DebugLog.addText("It's file!");
//						//#sijapp cond.end #
//						System.out.println("It's file: "+msg2Buf.length);
//						return;
//					}
                    // Skip values up to (and including) SUB_MSG_TYPE2.UNKNOWN
                    // (before MSGTYPE)
                    msg2Marker += 2 + 2 + 16 + 3 + 4 + 2 + 2 + 2 + 12;

//					System.out.println("Get and validate message type");
                    // Get and validate message type
                    int msgType = Util.getWord(msg2Buf, msg2Marker, false);
                    msg2Marker += 2;
                    //#sijapp cond.if modules_DEBUGLOG is "true" #
//					DebugLog.addText("Message Type: "+msgType);
                    //#sijapp cond.end #
//					System.out.println("Message Type: "+msgType);
                    if (!((msgType == 0x0001) || (msgType == 0x0004) || (msgType == 0x001A) || ((msgType >= 1000) && (msgType <= 1004))))
                        return;

                    // #sijapp cond.if modules_FILES is "true"#
                    status = Util.getWord(msg2Buf, msg2Marker);
                    //#sijapp cond.if modules_DEBUGLOG is "true" #
//					jimm.DebugLog.addText("Status (from message): "+status);
                    //#sijapp cond.end #
                    // #sijapp cond.end#
                    msg2Marker += 2;

                    // Skip PRIORITY
                    msg2Marker += 2;

                    // Get length of text
                    int textLen = Util.getWord(msg2Buf, msg2Marker, false);
                    //#sijapp cond.if modules_DEBUGLOG is "true" #
//					jimm.DebugLog.addText("TextLen (from message): "+textLen);
                    //#sijapp cond.end #
                    msg2Marker += 2;

                    // Check length
                    if (!((msgType >= 1000) && (msgType <= 1004))) {
                        if (msg2Buf.length < msg2Marker + textLen + 4 + 4) {
                            throw (new JimmException(152, 4, false));
                        }
                    }

                    // Get raw text
                    byte[] rawText = new byte[textLen];
                    System.arraycopy(msg2Buf, msg2Marker, rawText, 0, textLen);
                    msg2Marker += textLen;
                    // Plain message or URL message
                    if (((msgType == 0x0001) || (msgType == 0x0004)) && (rawText.length > 1)) {
                        // Skip FOREGROUND and BACKGROUND
                        if ((msgType == 0x0001) || (msgType == 0x0004)) {
                            msg2Marker += 4 + 4;
                        }

                        // Check encoding (by checking GUID)
                        boolean isUtf8 = false;
                        if (msg2Buf.length >= msg2Marker + 4) {
                            int guidLen = (int) Util.getDWord(msg2Buf, msg2Marker, false);
                            if (guidLen == 38) {
                                if (Util.byteArrayToString(msg2Buf, msg2Marker + 4, guidLen).equals("{0946134E-4C7F-11D1-8222-444553540000}")) {
                                    isUtf8 = true;
                                }
                            }
                            msg2Marker += 4 + guidLen;
                        }

                        // Decode text and create Message object
                        Message message;
                        if (msgType == 0x0001) {
                            // Decode text
                            String text = Util.removeCr(Util.byteArrayToString(rawText, isUtf8));

                            /************************ проверка на RTF сообщение ***********************/
                            if (text.indexOf("rtf1") > 0) text = Util.DecodeRTF(text);
                            /***************************************************************************/
//						  System.out.println("Incomming message: "+text);

                            // Instantiate message object
                            message = new PlainMessage(uin, icq.getUin(), DateAndTime.createCurrentDate(false), text, false);

                        } else {
                            // Search for delimited
                            int delim = -1;
                            for (int i = 0; i < rawText.length; i++) {
                                if (rawText[i] == 0xFE) {
                                    delim = i;
                                    break;
                                }
                            }

                            // Decode text; split text first, if delimiter could
                            // be found
                            String urlText, url;
                            if (delim != -1) {
                                urlText = Util.removeCr(Util.byteArrayToString(rawText, 0, delim, isUtf8));
                                url = Util.removeCr(Util.byteArrayToString(rawText, delim + 1, rawText.length - delim - 1, isUtf8));
                            } else {
                                urlText = Util.removeCr(Util.byteArrayToString(rawText, isUtf8));
                                url = "";
                            }

                            // Instantiate UrlMessage object
                            message = new UrlMessage(uin, icq.getUin(), DateAndTime.createCurrentDate(false), url, urlText);
                        }
                        // Forward message object to contact list
                        addMessage(message);

                        ContactItem contact = icq.getProfile().getItemByUIN(uin);
//#sijapp cond.if modules_SBOLTUN is "true"#
                        if (message instanceof PlainMessage && contact != null && contact.getInvisibleId() == 0)
                            jimm.chat.ChatTextList.sBoltunInput(((PlainMessage) message).getText(), contact);
//#sijapp cond.end#
                        //if ((Options.getBoolean(Options.OPTION_AUTO_ANSWER)) && (contact != null) && (!contact.autoAnswered) && (contact.getInvisibleId() == 0))
                        //    sendAutoMessage(contact);

                        // Acknowledge message
                        if (icq.getProfile().getBoolean(Profile.OPTION_DELIVERY_REPORT) || icq.getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
                            byte[] ackBuf = new byte[10 + 1 + uinLen + 2 + 51 + 3];
                            int ackMarker = 0;
                            System.arraycopy(buf, 0, ackBuf, ackMarker, 10);
                            ackMarker += 10;
                            Util.putByte(ackBuf, ackMarker, uinLen);
                            ackMarker += 1;
                            byte[] uinRaw = Util.stringToByteArray(uin);
                            System.arraycopy(uinRaw, 0, ackBuf, ackMarker, uinRaw.length);
                            ackMarker += uinRaw.length;
                            Util.putWord(ackBuf, ackMarker, 0x0003);
                            ackMarker += 2;
                            System.arraycopy(msg2Buf, 0, ackBuf, ackMarker, 51);
                            ackMarker += 51;
                            Util.putWord(ackBuf, ackMarker, 0x0001, false);
                            ackMarker += 2;
                            Util.putByte(ackBuf, ackMarker, 0x00);
                            ackMarker += 1;
                            SnacPacket ackPacket = new SnacPacket(SnacPacket.CLI_ACKMSG_FAMILY,
                                    SnacPacket.CLI_ACKMSG_COMMAND, 0, new byte[0], ackBuf);
                            icq.sendPacket(ackPacket);
                        }
                    } else if (msgType == 0x001A) { // Extended message
                        // Check length
                        if (msg2Buf.length < msg2Marker + 2 + 18 + 4) {
                            throw (new JimmException(152, 5, false));
                        }

                        // Save current marker position
                        int extDataStart = msg2Marker;

                        // Skip EXTMSG.LEN and EXTMSG.UNKNOWN
                        msg2Marker += 2 + 18;

                        // Get length of plugin string
                        int pluginLen = (int) Util.getDWord(msg2Buf, msg2Marker, false);
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("PluginLen: "+pluginLen);
                        //#sijapp cond.end #
                        msg2Marker += 4;

                        // Check length
                        if (msg2Buf.length < msg2Marker + pluginLen + 15 + 4 + 4) {
                            throw (new JimmException(152, 6, false));
                        }

                        // Get plugin string
                        String plugin = Util.byteArrayToString(msg2Buf, msg2Marker, pluginLen);
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("Plugin: "+plugin);
                        //#sijapp cond.end #
                        msg2Marker += pluginLen;

                        // Skip EXTMSG.UNKNOWN and EXTMSG.LEN
                        msg2Marker += 15 + 4;

                        // Get length of text
                        textLen = (int) Util.getDWord(msg2Buf, msg2Marker, false);
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("TextLen (from message): "+textLen);
                        //#sijapp cond.end #
                        msg2Marker += 4;

                        // Check length
                        if (msg2Buf.length < msg2Marker + textLen) {
                            throw (new JimmException(152, 7, false));
                        }

                        // Get text
                        String text = new String();
                        if (textLen > 0) {
                            text = Util.removeCr(Util.byteArrayToString(msg2Buf, msg2Marker, textLen));
                            msg2Marker += textLen;
                        }

                        // #sijapp cond.if modules_FILES is "true"#
                        // File transfer message
//						if (ackType >= 0) System.out.println("AckType: "+ackType);
//						else System.out.println("AckType: -1");
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						if (ackType >= 0) jimm.DebugLog.addText("Plugin: "+plugin+" AckType: "+ackType);
//						else jimm.DebugLog.addText("Plugin: "+plugin+" AckType: -1");
                        //#sijapp cond.end #
                        if (plugin.equals("File") && Jimm.getCurrentDisplay() instanceof SplashCanvas) {// нужен ли канвас
                            if (ackType == 2) {
                                // Get the port we should connect to
                                port = Integer.toString(Util.getWord(msg2Buf, msg2Marker));
                                msg2Marker += 2;
                                // Skip unknwon stuff
                                msg2Marker += 2;

                                // Get filename
                                textLen = Util.getWord(msg2Buf, msg2Marker, false);
                                msg2Marker += 2;

                                // Check length
                                if (msg2Buf.length < msg2Marker + textLen) {
                                    throw (new JimmException(152, 8, false));
                                }

                                // Get text
                                String filename = Util.removeCr(Util.byteArrayToString(msg2Buf, msg2Marker, textLen));
                                msg2Marker += textLen;

                                // Get filesize
                                long filesize = Util.getDWord(msg2Buf, msg2Marker, false);
                                msg2Marker += 4;

                                // Get IP if possible
                                // Check length
                                if (msgBuf.length < +8) {
                                    throw (new JimmException(152, 9, false));
                                }

                                msg2Buf = Util.getTlv(msgBuf, msgMarker);
                                if (msg2Buf == null) {
                                    throw (new JimmException(152, 2, false));
                                }
                                tlvType = Util.getWord(msgBuf, msgMarker);
                                if (tlvType == 0x0004) System.arraycopy(msg2Buf, 0, ip, 0, 4);
                                msgMarker += 4 + msg2Buf.length;

                                ContactItem sender = icq.getProfile().getItemByUIN(uin);

                                sender.setIPValue(ContactItem.CONTACTITEM_INTERNAL_IP, ip);
                                sender.setIPValue(ContactItem.CONTACTITEM_EXTERNAL_IP, extIP);
                                sender.setIntValue(ContactItem.CONTACTITEM_DC_PORT, Integer.parseInt(port));
                                //#sijapp cond.if modules_DEBUGLOG is "true"#
//								DebugLog.addText(filename+" ("+filesize+" byte)");
                                //#sijapp cond.end#

                                DirectConnectionAction dcAct = new DirectConnectionAction(sender.getFTM());
                                try {
                                    icq.requestAction(dcAct);
                                } catch (JimmException e) {
                                    JimmException.handleException(e);
                                    if (e.isCritical()) {
                                        return;
                                    }
                                }
                                if (icq.getProfile().isCurrent()) {
                                    // Start timer (timer will activate splash screen)
                                    //NativeCanvas.getLPCanvas().setCmdListener(null);
                                    icq.getProfile().addAction("filetransfer", dcAct);
                                }
                            }
                        } else // URL message
                            // #sijapp cond.end#
                            if (plugin.equals("Send Web Page Address (URL)")) {
                                // Search for delimiter
                                int delim = text.indexOf(0xFE);

                                // Split message, if delimiter could be found
                                String urlText;
                                String url;
                                if (delim != -1) {
                                    urlText = text.substring(0, delim);
                                    url = text.substring(delim + 1);
                                } else {
                                    urlText = text;
                                    url = "";
                                }

                                // Forward message message to contact list
                                UrlMessage message = new UrlMessage(uin, icq.getUin(), DateAndTime.createCurrentDate(false), url, urlText);
                                addMessage(message);

                                // Acknowledge message
                                byte[] ackBuf = new byte[10 + 1 + uinLen + 2 + 51 + 3 + 20 + 4 + (int) pluginLen + 19 + 4
                                        + textLen];
                                int ackMarker = 0;
                                System.arraycopy(buf, 0, ackBuf, ackMarker, 10);
                                ackMarker += 10;
                                Util.putByte(ackBuf, ackMarker, uinLen);
                                ackMarker += 1;
                                byte[] uinRaw = Util.stringToByteArray(uin);
                                System.arraycopy(uinRaw, 0, ackBuf, ackMarker, uinRaw.length);
                                ackMarker += uinRaw.length;
                                Util.putWord(ackBuf, ackMarker, 0x0003);
                                ackMarker += 2;
                                System.arraycopy(msgBuf, 0, ackBuf, ackMarker, 51);
                                ackMarker += 51;
                                Util.putWord(ackBuf, ackMarker, 0x0001, false);
                                ackMarker += 2;
                                Util.putByte(ackBuf, ackMarker, 0x00);
                                ackMarker += 1;
                                System.arraycopy(msg2Buf, extDataStart, ackBuf, ackMarker, 20 + 4 + (int) pluginLen + 19
                                        + 4 + textLen);
                                SnacPacket ackPacket = new SnacPacket(SnacPacket.CLI_ACKMSG_FAMILY,
                                        SnacPacket.CLI_ACKMSG_COMMAND, 0, new byte[0], ackBuf);
                                icq.sendPacket(ackPacket);

                            } else { // Other messages
                                /***************************************ICQ XTraz Support**********************************/
                                if (plugin.equals("Script Plug-in: Remote Notification Arrive"))
                                    try {
                                        a(uin, text, msgType, l3, l5);
                                    } catch (Exception e) {
                                        JimmException.handleExceptionEx(e);
                                    }
                                /******************************************************************************************/
                            }
                    } else if (((msgType >= 1000) && (msgType <= 1004))) { // Status message requests
// #sijapp cond.if modules_MAGIC_EYE is "true" #
                        if (MagicEye.getBooleanValue(MagicEye.OPTION_READ_STATUS)) {
                            icq.getProfile().getMagicEye().addAction(uin, "read status message");
                        }
// #sijapp cond.end #
                        long currStatus = icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS);
                        if (!icq.checkInvisLevel(uin)) {
                            return;
                        }

                        ContactItem item = icq.getProfile().getItemByUIN(uin);
                        if (item != null) {
                            if (item.getExtraValue(ContactItem.EXTRA_WATCH)) {
                                icq.getProfile().getChatHistory()
                                        .getChat(item)
                                        .addExtraNotice(
                                                "read status message",
                                                ContactList.imageList.elementAt(JimmUI.getStatusImageIndex(icq.getCurrentStatus()))
                                        );
                            }
                            //#sijapp cond.if modules_PANEL is "true"#
                            icq.getProfile().showPopupItem(item.name + ' ' + ResourceBundle.getString("read status message"),
                                    ContactList.imageList.elementAt(JimmUI.getStatusImageIndex(icq.getCurrentStatus())),
                                    item
                            );
                            //#sijapp cond.end#
                            if (item.getExtraValue(ContactItem.EXTRA_STATUS)) {
                                return;
                            }
                        }

                        String statusMess = new String();

                        if ((currStatus != ContactItem.STATUS_ONLINE) && (currStatus != ContactItem.STATUS_CHAT) && (currStatus != ContactItem.STATUS_INVISIBLE) && (currStatus != ContactItem.STATUS_INVIS_ALL)) {
                            //String withName = "";
                            ContactItem itemst = icq.getProfile().getItemByUIN(uin);
                            String nick = (itemst != null) ? itemst.name : uin;
                            itemst = null;

                            if (currStatus == ContactItem.STATUS_AWAY)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_AWAY);

                            if (currStatus == ContactItem.STATUS_DND)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_DND);

                            if (currStatus == ContactItem.STATUS_NA)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_NA);

                            if (currStatus == ContactItem.STATUS_OCCUPIED)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_OCCUPIED);

                            if (currStatus == ContactItem.STATUS_EVIL)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_EVIL);

                            if (currStatus == ContactItem.STATUS_DEPRESSION)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_DEPRESSION);

                            if (currStatus == ContactItem.STATUS_HOME)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_HOME);

                            if (currStatus == ContactItem.STATUS_WORK)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_WORK);

                            if (currStatus == ContactItem.STATUS_LUNCH)
                                statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_LUNCH);

                            statusMess = Util.replaceStr(statusMess, "%NICK%", nick);
                            statusMess = Util.replaceStr(statusMess, "%TIME%", icq.getLastStatusChangeTime());
                        }

                        // Acknowledge message with away message
                        final byte[] statusMessBytes = Util.stringToByteArray(statusMess, false);

                        if (statusMessBytes.length < 1) return;

                        byte[] ackBuf = new byte[10 + 1 + uinLen + 2 + 51 + 2 + statusMessBytes.length + 1];
                        int ackMarker = 0;
                        System.arraycopy(buf, 0, ackBuf, ackMarker, 10);
                        ackMarker += 10;
                        Util.putByte(ackBuf, ackMarker, uinLen);
                        ackMarker += 1;
                        byte[] uinRaw = Util.stringToByteArray(uin);
                        System.arraycopy(uinRaw, 0, ackBuf, ackMarker, uinRaw.length);
                        ackMarker += uinRaw.length;
                        Util.putWord(ackBuf, ackMarker, 0x0003);
                        ackMarker += 2;
                        System.arraycopy(msg2Buf, 0, ackBuf, ackMarker, 51);
                        Util.putWord(ackBuf, ackMarker + 2, 0x0800);
                        ackMarker += 51;
                        Util.putWord(ackBuf, ackMarker, statusMessBytes.length + 1, false);
                        ackMarker += 2;
                        System.arraycopy(statusMessBytes, 0, ackBuf, ackMarker, statusMessBytes.length);
                        Util.putByte(ackBuf, ackMarker + statusMessBytes.length, 0x00);
                        SnacPacket ackPacket = new SnacPacket(SnacPacket.CLI_ACKMSG_FAMILY,
                                SnacPacket.CLI_ACKMSG_COMMAND, 0, new byte[0], ackBuf);

                        icq.sendPacket(ackPacket);
                    }
                } else if (format == 0x0004) { // Message format 4
                    // Check length
                    if (msgBuf.length < 8) {
                        throw (new JimmException(153, 0, false));
                    }

                    // Skip SUB_MSG_TYPE4.UIN
                    msgMarker += 4;

                    // Get SUB_MSG_TYPE4.MSGTYPE
                    int msgType = Util.getWord(msgBuf, msgMarker, false);
                    msgMarker += 2;

                    // Only plain messages and URL messagesa are supported
                    if ((msgType != 0x0001) && (msgType != 0x0004)) return;

                    // Get length of text
                    int textLen = Util.getWord(msgBuf, msgMarker, false);
                    msgMarker += 2;

                    // Check length (exact match required)
                    if (msgBuf.length != 8 + textLen) {
                        throw (new JimmException(153, 1, false));
                    }

                    // Get text
                    String text = Util.removeCr(Util.byteArrayToString(msgBuf, msgMarker, textLen));
                    msgMarker += textLen;

                    // Plain message
                    if (msgType == 0x0001) {
//                        System.out.println("Incomming message: "+text);
                        // Forward message to contact list
                        PlainMessage plainMsg = new PlainMessage(uin, icq.getUin(), DateAndTime.createCurrentDate(false), text, false);
                        addMessage(plainMsg);
                    } else if (msgType == 0x0004) { // URL message
                        // Search for delimiter
                        int delim = text.indexOf(0xFE);

                        // Split message, if delimiter could be found
                        String urlText;
                        String url;
                        if (delim != -1) {
                            urlText = text.substring(0, delim);
                            url = text.substring(delim + 1);
                        } else {
                            urlText = text;
                            url = "";
                        }

                        // Forward message message to contact list
                        UrlMessage urlMsg = new UrlMessage(uin, icq.getUin(), DateAndTime.createCurrentDate(false), url, urlText);
                        addMessage(urlMsg);
                    }
                }
            }

            //	  Watch out for SRV_ADDEDYOU
            else if ((snacPacket.getFamily() == SnacPacket.SRV_ADDEDYOU_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_ADDEDYOU_COMMAND)) {
                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int uinLen = Util.getByte(buf, 0);
                String uin = Util.byteArrayToString(buf, 1, uinLen);

                // Create a new system notice
                SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_YOUWEREADDED, icq.getProfile().getItemByUIN(uin), false, null);

                // Handle the new system notice
                addMessage(notice);
            }

            /*******************************************************************************************/
            //	  Watch out for password change confirmation
            else if ((snacPacket.getFamily() == SnacPacket.SRV_FROMICQSRV_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_FROMICQSRV_COMMAND)) {
                byte[] passrvc = snacPacket.getData();
                if (passrvc.length > 2) {
                    int Type = Util.getWord(passrvc, 0, false);
                    int Type1 = Util.getByte(passrvc, 2);
                    if ((Type == 170) && (Type1 == 10)) {
                        Alert pass_message = new Alert("", ResourceBundle.getString("change_pass_message"), null, AlertType.INFO);
                        pass_message.setTimeout(15000);
                        Jimm.setDisplay(pass_message);
                        icq.getProfile().changePassword(null);
                    } else if ((Type == 0x0104) && (Type1 == 0x0A)) {
                        OtherAction.isInvisNum = false;
                        icq.sendPacket(OtherAction.getAuthPacket(icq));
                    }
                }
            }
            /******************************************************************************************/

            /******************************************************************************************/
            //	  Watch out for CLI_ROSTERDELETE
            else if ((snacPacket.getFamily() == SnacPacket.CLI_ROSTERDELETE_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.CLI_ROSTERDELETE_COMMAND)) {
                byte[] bufdel = snacPacket.getData();
                int uinLenq = Util.getByte(bufdel, 1);
                if (Util.getByte(bufdel, 2 + uinLenq) != 0) {
                    String uinq = Util.byteArrayToString(bufdel, 2, uinLenq);
                    int nikLenq = Util.getByte(bufdel, 13 + uinLenq);
                    String nikq = Util.byteArrayToString(bufdel, 14 + uinLenq, nikLenq, true);
                    SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_DELETE, icq.getProfile().getItemByUIN(uinq), false, nikq);
                    addMessage(notice);
                }
            }
            /******************************************************************************************/

            //	  Watch out for SRV_AUTHREQ
            else if ((snacPacket.getFamily() == SnacPacket.SRV_AUTHREQ_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_AUTHREQ_COMMAND)) {
                int authMarker = 0;

                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int length = Util.getByte(buf, 0);
                authMarker += 1;
                String uin = Util.byteArrayToString(buf, authMarker, length);
                authMarker += length;

                // Get reason
                length = Util.getWord(buf, authMarker);
                authMarker += 2;
                String reason = Util.byteArrayToString(buf, authMarker, length, Util.isDataUTF8(buf, authMarker, length));

                // Create a new system notice
                SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREQ, icq.getProfile().getItemByUIN(uin), false, reason);

                // Handle the new system notice
                addMessage(notice);
            }

            //	  Watch out for SRV_AUTHREPLY
            else if ((snacPacket.getFamily() == SnacPacket.SRV_AUTHREPLY_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_AUTHREPLY_COMMAND)) {
                int authMarker = 0;
                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int length = Util.getByte(buf, 0);
                authMarker += 1;
                String uin = Util.byteArrayToString(buf, authMarker, length);
                authMarker += length;

                // Get granted boolean
                boolean granted = false;
                if (Util.getByte(buf, authMarker) == 0x01) {
                    granted = true;
                }
                authMarker += 1;

                // Get reason only of not granted
                SystemNotice notice;
                if (!granted) {
                    length = Util.getWord(buf, authMarker);
                    //String reason = Util.byteArrayToString(buf, authMarker, length + 2); // old
                    String reason = Util.byteArrayToString(buf, authMarker + 2, length, Util.isDataUTF8(buf, authMarker + 2, length));

                    // Create a new system notice
                    if (length == 0) {
                        notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREPLY, icq.getProfile().getItemByUIN(uin), granted, null);
                    } else {
                        notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREPLY, icq.getProfile().getItemByUIN(uin), granted, reason);
                    }
                } else {
                    // Create a new system notice
                    //System.out.println("Auth granted");
                    notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREPLY, icq.getProfile().getItemByUIN(uin), granted, "");
                }

                // Handle the new system notice
                addMessage(notice);
            }
            /************* UPDATE SSI ***************/
            /*else if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTER_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTER_COMMAND)) {                
                ConnectAction.roster(null, packet, icq, true); // ConnectAction.javax
            }*/

            /************* Avatars ***************/
/*			else if (snacPacket.getFamily() == 0x0010 && snacPacket.getCommand() == 0x0007) {
				byte[] buf = snacPacket.getData();
				int marker = 0;
				int uinLength = Util.getByte(buf, marker); marker++;
				byte[] uinRaw = new byte[uinLength];
				System.arraycopy(buf, marker, uinRaw, 0, uinLength); marker+=uinLength;
				marker+=2+1+1+16+1+2+1+1+16;
				int iconLength = Util.getWord(buf, marker); marker+=2;
				byte[] imgData = new byte[iconLength];
				System.arraycopy(buf, marker, imgData, 0, iconLength);
				String error = null;
				Image image = null;
				try {
					image = Image.createImage(imgData, 0, iconLength);
				} 	catch (ArrayIndexOutOfBoundsException aE) { error = "ArrayIndexOutOfBoundsException"; }
					catch (NullPointerException nE) { error = "NullPointerException"; }
					catch (IllegalArgumentException iE) { error = "IllegalArgumentException"; }
				if (error == null) {
					Alert a = new Alert("See!", error, null, null);
					a.setTimeout(a.FOREVER);
					Jimm.setDisplay(a);
				}
				RunnableImpl.addAvatar(Util.byteArrayToString(uinRaw), image);
			}*/
            /***********************************/
        }
    }

    /**
     * ******************************************* Auto Answer ********************************************
     */
//    private void sendAutoMessage(ContactItem contact) {
//        //if (contact.getInvisibleId()!=0) return;
//        if (!icq.checkInvisLevel(contact.getUinString())) return;
//        long currStatus = icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS);
//
//
//        String statusMess = new String();
//        String nick = (contact.name.length() >= 1) ? contact.name : contact.getUinString();
//
//        if (currStatus == ContactItem.STATUS_AWAY)
//            statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_AWAY);
//
//        if (currStatus == ContactItem.STATUS_DND)
//            statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_DND);
//
//        if (currStatus == ContactItem.STATUS_NA)
//            statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_NA);
//
//        if (currStatus == ContactItem.STATUS_OCCUPIED)
//            statusMess = icq.getProfile().getString(Profile.OPTION_STATUS_MESSAGE_OCCUPIED);
//
//        statusMess = Util.replaceStr(statusMess, "%NICK%", nick);
//        statusMess = Util.replaceStr(statusMess, "%TIME%", icq.getLastStatusChangeTime());
//
//        if (statusMess.length() >= 1) {
//            PlainMessage plainMsg = new PlainMessage
//                    (
//                            contact.getUinString(),
//                            contact,
//                            Message.MESSAGE_TYPE_NORM,
//                            DateAndTime.createCurrentDate(false),
//                            ResourceBundle.getString("auto_answer") + ":" + "\n" + statusMess
//                    );
//
//            SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
//            try {
//                icq.requestAction(sendMsgAct);
//            } catch (Exception e) {
//                JimmException.handleExceptionEx(e);
//            }
//        }
//        contact.autoAnswered |= true;
//        //System.out.println("AutoAnswer was sent");
//    }
    /******************************************************************************************************/

    /**
     * ******************************************* Anti-Spam ********************************************
     */
    public void addMessage(Message mess) {
        if (/*!ignoreAll(mess) && */!isSpam(mess) && !isSpamUrl(mess)) {
// #sijapp cond.if target is "MIDP2" #
            if (Options.getBoolean(Options.OPTION_BRING_UP)) {
                Jimm.setMinimized(false);
            }
// #sijapp cond.end #
            icq.getProfile().addMessage(mess, true);
//            boolean haveToBeepNow = Jimm.is_phone_SE();
//            ContactItem cItem = icq.getProfile().addMessage(mess, !haveToBeepNow);
//// #sijapp cond.if target isnot "DEFAULT"#
//            if (haveToBeepNow) {
//                boolean flag = false;
//                if (cItem != null) {
//                    flag = cItem.getExtraValue(ContactItem.EXTRA_VIBRA);
//                }
//                Notify.vibrate(flag);
//// #sijapp cond.if  modules_SOUNDS is "true"#
//                if (cItem != null) {
//                    flag = cItem.getExtraValue(ContactItem.EXTRA_SOUND);
//                }
//                Notify.playSoundNotification(Notify.SOUND_TYPE_MESSAGE, flag);
//// #sijapp cond.end#
//            }
//// #sijapp cond.end #
        }
    }

    private void sendMessage(ContactItem contact, String message) {
//		if (!icq.checkInvisLevel(contact.getUinString())) {
//		            return;
//		}
        PlainMessage plainMsg =
                new PlainMessage(contact.getUinString(), contact, Message.MESSAGE_TYPE_NORM, DateAndTime.createCurrentDate(false), message);

        SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
        try {
            icq.requestAction(sendMsgAct);
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
        }
    }

    private static Vector uins = new Vector();

    private boolean isChecked(String uin) {
        for (int i = 0; i < uins.size(); i++) {
            if (uins.elementAt(i).equals(uin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ***********************************************
     */
    private static Vector uin1 = new Vector();
    private static Vector uin2 = new Vector();

    private boolean isCheckedData(String uin) {
        for (int i = 0; i < uin1.size(); i++) {
            if (uin1.elementAt(i).equals(uin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ***********************************************
     */

    private boolean stringEquals(String s1, String s2) {
        if (s1.length() != s2.length()) {
            return false;
        }
        if (s1 == s2) {
            return true;
        }
        int size = s1.length();
        for (int i = 0; i < size; i++) {
            if (StringConvertor.toLowerCase(s1.charAt(i)) != StringConvertor.toLowerCase(s2.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /*private boolean maybeBad(String msg) {
        boolean enable = Options.getBoolean(Options.OPTION_ANTISPAM_IGNOREON);
        String [] ss = Util.explode(Options.getString(Options.OPTION_ANTISPAM_IGNOREMSG).toLowerCase(), ' ');
        String maybe = "..." + msg.toLowerCase();
        for (int i = 0; i < ss.length; i++) {
            if ((maybe.indexOf(ss[i]) != -1) && enable) {
                ss = null;
                maybe = null;
                return true;
            }
        }
        return false;
    }*/

    public boolean isSpamUrl(Message message) {
        /*boolean allstop = Options.getBoolean(Options.OPTION_ANTISPAM_ALL);
        if (allstop && null == icq.getProfile().getItemByUIN(message.getSndrUin()))
            return true;
*/
        boolean urlstop = Options.getBoolean(Options.OPTION_ANTISPAM_URL);
        if (!urlstop) return false;

        String uin = message.getSndrUin();
        ContactItem contact = icq.getProfile().getItemByUIN(uin);

        if (null != contact) return false;

        String string;
        if (message instanceof SystemNotice) {
            string = ((SystemNotice) message).getReason();
            if (string != null) {
                if (Util.parseMessageForURL(string) != null) {
//#sijapp cond.if modules_MAGIC_EYE is "true"#
                    if (string.length() <= 256) {
                        if (MagicEye.getBooleanValue(MagicEye.OPTION_ANTISPAM)) {
                            icq.getProfile().getMagicEye().addAction(uin, "antispam", string);
                        }
                    }
//#sijapp cond.end#
                    string = null;
                    return true;
                }
            }
        } else if (message instanceof PlainMessage) {
            string = ((PlainMessage) message).getText();
            if (string != null) {
                if (Util.parseMessageForURL(string) != null) {
//#sijapp cond.if modules_MAGIC_EYE is "true"#
                    if (string.length() <= 256) {
                        if (MagicEye.getBooleanValue(MagicEye.OPTION_ANTISPAM)) {
                            icq.getProfile().getMagicEye().addAction(uin, "antispam", string);
                        }
                    }
//#sijapp cond.end#
                    return true;
                }
            }
        }
        return false;
    }

//    public boolean ignoreAll(Message message) {
//        boolean allstop = Options.getBoolean(Options.OPTION_ANTISPAM_ALL);
//        if (allstop && null == icq.getProfile().getItemByUIN(message.getSndrUin())) {
//            return true;
//        }
//        return false;
//    }

    public boolean isSpam(Message message) {
        if (!Options.getBoolean(Options.OPTION_ANTISPAM_ENABLE))
            return false;

        String msg = "";
        String uin = message.getSndrUin();
        ContactItem contact = icq.getProfile().getItemByUIN(uin);

        if ((null != contact) || isChecked(uin)) {
            return false;
        }
        if (message instanceof PlainMessage) {
            msg = ((PlainMessage) message).getText();
//#sijapp cond.if modules_MAGIC_EYE is "true"#
            if (msg.length() <= 256) {
                if (MagicEye.getBooleanValue(MagicEye.OPTION_ANTISPAM)) {
                    icq.getProfile().getMagicEye().addAction(uin, "antispam", msg);
                }
            }
//#sijapp cond.end#
        }

        if (!(message instanceof PlainMessage)) {
            return true;
        }

        /************************************************/
        int d1 = 0;
        String d2;
        int d3 = 0;

        if (!isCheckedData(uin)) {
            uin1.addElement(uin);
            uin2.addElement(String.valueOf(0));
        } else {
            d1 = uin1.indexOf(uin);
            d2 = (String) uin2.elementAt(d1);
            d3 = Integer.parseInt(d2);
            d3++;
            uin2.setElementAt(String.valueOf(d3), d1);
        }
        /*************************************************/

        if (message.getOffline()) {
            return true;
        }

        String[] answers = Util.explode(Options.getString(Options.OPTION_ANTISPAM_ANSWER), '\n');
        for (int i = answers.length - 1; i >= 0; i--) {
            if (stringEquals(msg, answers[i])) {
                uins.addElement(uin);
            }
        }

        if ((d3 < 3) || isChecked(uin)) {
            contact = new ContactItem(0, 0, uin, uin, true, false, icq.getProfile());
            sendMessage(contact, Options.getString(isChecked(uin) ? Options.OPTION_ANTISPAM_HELLO : Options.OPTION_ANTISPAM_MSG));
        }
        return true;
    }

    /* xTraz (omg, is necessary to change this) */
    private void b(String s, String s1) throws Exception {
        int i;
        if ((i = s1.indexOf("<NR><RES>")) < 0)
            throw new Exception("cp #2");
        int j;
        if ((j = s1.indexOf("</RES></NR>")) < 0)
            throw new Exception("cp #3");
        String s2;
        int k;
        if ((k = (s2 = Util.DeMangleXml(s1.substring(i + 9, j))).indexOf("<val srv_id='")) < 0)
            throw new Exception("cp #6"); // Pigeon Bug

        int j2;
        if ((j2 = s2.indexOf("<title>")) < 0)
            throw new Exception("cp #9");
        int k2;
        String s3;
        if ((k2 = s2.indexOf("/title>")) < 0)
            throw new Exception("cp #10");
        if ((s3 = s2.substring(j2 + 7, k2 - 1)).length() < 1) s3 = " "; // заголовок х-статуса...
        int l2;
        if ((l2 = s2.indexOf("<desc>")) < 0)
            throw new Exception("cp #11");
        int i3;
        if ((i3 = s2.indexOf("</desc>")) < 0)
            throw new Exception("cp #12");
        String s4 = s2.substring(l2 + 6, i3); // подпись х-статуса...

        ContactItem cItem;
        if ((cItem = icq.getProfile().getItemByUIN(s)) != null) {
// #sijapp cond.if modules_MAGIC_EYE is "true" #
            if (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE) {
                cItem.setStatus(ContactItem.STATUS_INVISIBLE, false);
                if (MagicEye.getBooleanValue(MagicEye.OPTION_STATUS_INVISIBLE)) {
                    icq.getProfile().getMagicEye().addAction(s, "status_invisible");
                }
            }
// #sijapp cond.end #
            //if (!(s3 == " " && s4.length() < 1)) {
            icq.getProfile().getChatHistory().getChat(cItem).addTextToForm("", " " + s3 + " " + s4, "", 0, true, false, cItem.getXStatusImage(), 0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                JimmException.handleExceptionEx(e1);
            }
            if (cItem.openChat && Jimm.getContactList().isActive() && Jimm.getContactList().contain(cItem)) {
                cItem.activate();
            }
            cItem.openChat = false;
            //}
        }
        return;
    }

    private void a(String s, String s1, int i, long l1, long l2) throws Exception {
        ContactItem cItem = icq.getProfile().getItemByUIN(s);
        if (s1.startsWith("<N>")) {
            if (icq.getProfile().getInt(Profile.OPTION_XSTATUS) == XStatus.XSTATUS_NONE) {
                return;
            }
// #sijapp cond.if modules_MAGIC_EYE is "true" #
            if (MagicEye.getBooleanValue(MagicEye.OPTION_READ_XTRAZ)) {
                icq.getProfile().getMagicEye().addAction(s, "read xtraz");
            }
// #sijapp cond.end #
            if ((cItem != null) && (cItem.getExtraValue(ContactItem.EXTRA_WATCH))) {
                icq.getProfile().getChatHistory().getChat(cItem).addExtraNotice("read xtraz", icq.getCurrentXStatus());
            }
            String names = s;
            if (cItem != null) {
                names = cItem.name;
            }
            //#sijapp cond.if modules_PANEL is "true"#
            icq.getProfile().showPopupItem(names + ' ' + ResourceBundle.getString("read xtraz"), icq.getCurrentXStatus(), cItem);
            //#sijapp cond.end#
        }
        int j = 0;
        //long currStatus = icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS);
        if (!icq.checkInvisLevel(s)) {
            return;
        }
        int k;
        if ((k = s1.indexOf("<QUERY>")) < 0)
            throw new Exception("cp #2");
        int i1;
        if ((i1 = s1.indexOf("</QUERY>")) < 0)
            throw new Exception("cp #3");
        int j1;
        if ((j1 = s1.indexOf("<NOTIFY>")) < 0)
            throw new Exception("cp #4");
        int k1;
        if ((k1 = s1.indexOf("</NOTIFY>")) < 0)
            throw new Exception("cp #5");
        String s2;
        int i2;
        if ((i2 = (s2 = Util.DeMangleXml(s1.substring(k + 7, i1))).indexOf("<PluginID>")) < 0)
            throw new Exception("cp #6");
        int j2;
        if ((j2 = s2.indexOf("</PluginID>")) < 0)
            throw new Exception("cp #7");
        if (s2.substring(i2 + 10, j2).toLowerCase().compareTo("srvmng") != 0)
            throw new Exception("cp #8");
        String s3;
        if ((s3 = Util.DeMangleXml(s1.substring(j1 + 8, k1))).indexOf("AwayStat") < 0)
            throw new Exception("cp #9");
        int k2;
        if ((k2 = s3.indexOf("<senderId>")) < 0)
            throw new Exception("");
        int i3;
        if ((i3 = s3.indexOf("</senderId>")) < 0)
            throw new Exception("");
        if (s3.substring(k2 + 10, i3).compareTo(s) != 0)
            throw new Exception("incorrect uin");
        if (cItem != null) {
            if (cItem.getExtraValue(ContactItem.EXTRA_XTRAZ)) {
                return;
            }
            String nick = cItem.name;
            String xtrazTitle = icq.getProfile().getString(Profile.OPTION_XTRAZ_TITLE);
            xtrazTitle = Util.replaceStr(xtrazTitle, "%NICK%", nick);
            String xtrazMessage = icq.getProfile().getString(Profile.OPTION_XTRAZ_MESSAGE);
            xtrazMessage = Util.replaceStr(xtrazMessage, "%NICK%", nick);
            a(s, j, xtrazTitle, xtrazMessage, i, l1, l2); //Xtraz SM Title & Message!!
        }
        return;
    }

    private void a(String s, int i, String s1, String s2, int j, long l1, long l2) throws JimmException {
        byte abyte0[];
        String s3 = "<NR><RES>" + Util.MangleXml("<ret event='OnRemoteNotification'><srv><id>cAwaySrv</id><val srv_id='cAwaySrv'><Root><CASXtraSetAwayMessage></CASXtraSetAwayMessage><uin>" + icq.getUin() + "</uin><index>1</index><title>" + s1 + "</title><desc>" + s2 + "</desc></Root></val></srv></ret>") + "</RES></NR>";
        abyte0 = a(s, j, l1, l2, s3);
        SnacPacket SnacPacket1 = new SnacPacket(4, 11, 0L, new byte[0], abyte0);
        icq.sendPacket(SnacPacket1);
        return;
    }

    public static void a(String s, int ID, Icq icq) throws JimmException {
        SnacPacket SnacPacket1;
        String s1 = "<N><QUERY>" + Util.MangleXml("<Q><PluginID>srvMng</PluginID></Q>") + "</QUERY><NOTIFY>" + Util.MangleXml("<srv><id>cAwaySrv</id><req><id>AwayStat</id><trans>") + ID + Util.MangleXml("</trans><senderId>" + icq.getUin() + "</senderId></req></srv>") + "</NOTIFY></N>";
        byte abyte0[] = b(s, Util.getCounter(), System.currentTimeMillis(), 0L, s1);
        SnacPacket1 = new SnacPacket(4, 6, 0L, new byte[0], abyte0);
        try {
            icq.sendPacket(SnacPacket1);
        } catch (JimmException e) {
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
        }
        return;
    }

    private static byte[] a(String s, int i, long l1, long l2, String s1) {
        byte abyte0[] = new byte[0];
        int j = 0;
        try {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            (new DataOutputStream(bytearrayoutputstream)).writeUTF(s1);
            j = (abyte0 = bytearrayoutputstream.toByteArray()).length - 2;
        } catch (Exception _ex) {
            JimmException.handleExceptionEx(_ex);
        }
        int k = 0;
        byte abyte1[];
        k = a(abyte1 = new byte[s.length() + 64 + (84 + (8 + j))], 0, s, l1, l2, i, (byte) 26, (byte) 0);
        k = a(abyte1, k);
        k = b(abyte1, k);
        Util.putWord(abyte1, k, j + 4, false);
        k += 4;
        Util.putWord(abyte1, k, j, false);
        k += 4;
        if (j > 0) {
            System.arraycopy(abyte0, 2, abyte1, k, j);
        }
        return abyte1;
    }

    private static byte[] b(String s, int i, long l1, long l2, String s1) {
        int j = 0;
        int k = 92 + s1.length();
        byte abyte0[];
        Util.putDWord(abyte0 = new byte[11 + s.length() + 95 + k + 4], 0, l1, false);
        Util.putDWord(abyte0, 4, l2, false);
        Util.putWord(abyte0, 8, 2);
        Util.putByte(abyte0, 10, s.length());
        System.arraycopy(Util.stringToByteArray(s), 0, abyte0, 11, s.length());
        j = 11 + s.length();
        j = a(abyte0, j, 55 + k, l1, l2, 1);
        j = a(abyte0, j, i, 0, 256, k);
        j = a(abyte0, j);
        j = b(abyte0, j);
        Util.putWord(abyte0, j, s1.length() + 4, false);
        j += 4;
        Util.putWord(abyte0, j, s1.length(), false);
        j += 4;
        System.arraycopy(Util.stringToByteArray(s1), 0, abyte0, j, s1.length());
        j += s1.length();
        Util.putDWord(abyte0, j, 0x30000L);
        return abyte0;
    }

    private static int a(byte abyte0[], int i, int j, long l1, long l2, int k) {
        Util.putWord(abyte0, i, 5);
        i += 2;
        Util.putWord(abyte0, i, 36 + j);
        i += 2;
        Util.putWord(abyte0, i, 0);
        i += 2;
        Util.putDWord(abyte0, i, l1, false);
        i += 4;
        Util.putDWord(abyte0, i, l2, false);
        i += 4;
        Util.putDWord(abyte0, i, 0x9461349L); //some unknown stuff...
        Util.putDWord(abyte0, i + 4, 0x4c7f11d1L);
        Util.putDWord(abyte0, i + 8, 0xffffffff82224445L);
        Util.putDWord(abyte0, i + 12, 0x53540000L);
        i += 16;
        Util.putDWord(abyte0, i, 0xa0002L);
        i += 4;
        Util.putDWord(abyte0, i, k);
        i += 2;
        Util.putDWord(abyte0, i, 0xf0000L);
        return i += 4;
    }

    private static int a(byte abyte0[], int i, String s, long l1, long l2, int j, byte byte0, byte byte1) {
        Util.putDWord(abyte0, i, l1, false);
        i += 4;
        Util.putDWord(abyte0, i, l2, false);
        i += 4;
        Util.putWord(abyte0, i, 2);
        i += 2;
        Util.putByte(abyte0, i, s.length());
        i++;
        System.arraycopy(Util.stringToByteArray(s), 0, abyte0, i, s.length());
        i += s.length();
        Util.putWord(abyte0, i, 3);
        i += 2;
        Util.putWord(abyte0, i, 27, false);
        i += 2;
        Util.putWord(abyte0, i, 8);
        i++;
        Util.putDWord(abyte0, i, 0L);
        Util.putDWord(abyte0, i + 4, 0L);
        Util.putDWord(abyte0, i + 8, 0L);
        Util.putDWord(abyte0, i + 12, 0L);
        i += 16;
        Util.putDWord(abyte0, i, 3L);
        i += 4;
        Util.putDWord(abyte0, i, 4L);
        i += 4;
        Util.putWord(abyte0, i, j, false);
        i += 2;
        Util.putWord(abyte0, i, 14, false);
        i += 2;
        Util.putWord(abyte0, i, j, false);
        i += 2;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putByte(abyte0, i, byte0);
        i++;
        Util.putByte(abyte0, i, byte1);
        i++;
        Util.putWord(abyte0, i, 0, false);
        i += 2;
        Util.putWord(abyte0, i, 0);
        return i += 2;
    }

    private static int a(byte abyte0[], int i, int j, int k, int i1, int j1) {
        Util.putWord(abyte0, i, 10001);
        i += 2;
        Util.putWord(abyte0, i, 51 + j1);
        i += 2;
        Util.putWord(abyte0, i, 27, false);
        i += 2;
        Util.putByte(abyte0, i, 8);
        i++;
        Util.putDWord(abyte0, i, 0L);
        Util.putDWord(abyte0, i + 4, 0L);
        Util.putDWord(abyte0, i + 8, 0L);
        Util.putDWord(abyte0, i + 12, 0L);
        i += 16;
        Util.putDWord(abyte0, i, 3L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putWord(abyte0, i, j, false);
        i += 2;
        Util.putWord(abyte0, i, 14, false);
        i += 2;
        Util.putWord(abyte0, i, j, false);
        i += 2;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putByte(abyte0, i, 26);
        i++;
        Util.putByte(abyte0, i, 0);
        i++;
        Util.putWord(abyte0, i, k, false);
        i += 2;
        Util.putWord(abyte0, i, i1);
        return i += 2;
    }

    private static int a(byte abyte0[], int i) {
        Util.putWord(abyte0, i, 1, false);
        i += 2;
        Util.putByte(abyte0, i, 0);
        return ++i;
    }

    private static int b(byte abyte0[], int i) {
        Util.putWord(abyte0, i, 79, false);
        i += 2;
        Util.putDWord(abyte0, i, 0x3b60b3efL); // unknown stuff..
        Util.putDWord(abyte0, i + 4, 0xffffffffd82a6c45L);
        Util.putDWord(abyte0, i + 8, 0xffffffffa4e09c5aL);
        Util.putDWord(abyte0, i + 12, 0x5e67e865L);
        i += 16;
        Util.putWord(abyte0, i, 8, false);
        i += 2;
        Util.putDWord(abyte0, i, 42L, false);
        i += 4;
        System.arraycopy(Util.stringToByteArray("Script Plug-in: Remote Notification Arrive"), 0, abyte0, i, 42);
        i += 42;
        Util.putDWord(abyte0, i, 256L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putDWord(abyte0, i, 0L);
        i += 4;
        Util.putWord(abyte0, i, 0);
        i += 2;
        Util.putByte(abyte0, i, 0);
        return ++i;
    }

//	private void file(long size, String name, String uin) {
//		System.out.println("Incomming file: "+name+" ("+size+" byte)");
//		ContactItem cItem = icq.getProfile().getItemByUIN(uin);
//        addMessage(new IncommingFileMessage(uin, "Incomming file: "+name+" ("+size+" byte)"));
//        if (cItem.openChat && !Jimm.locked()) cItem.activate();
//        cItem.openChat = false;
//		try {
//			Thread.sleep(4000);
//		} catch (Exception e) {
//		}
//
//        try {
//            icq.requestAction(new FileAckAction(icq.getProfile().getItemByUIN(uin), name, size));
//        } catch (Exception e) {}
//	}
    /*******************************************************************************************************/
}