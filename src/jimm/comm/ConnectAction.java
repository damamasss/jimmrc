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
 File: src/jimm/comm/ConnectAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.*;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Vector;

public class ConnectAction extends Action {
    // Action states
    public static final int STATE_ERROR = -2;
    public static final int STATE_INIT = -1;
    public static final int STATE_INIT_DONE = 0;
    public static final int STATE_AUTHKEY_REQUESTED = 1;
    public static final int STATE_CLI_IDENT_SENT = 2;
    public static final int STATE_CLI_DISCONNECT_SENT = 3;
    public static final int STATE_CLI_COOKIE_SENT = 4;
    public static final int STATE_CLI_CAPS_SENT = 5;
    public static final int STATE_CLI_CHECKROSTER_SENT = 6;
    public static final int STATE_CLI_STATUS_INFO_SENT = 7;
    public static final int STATE_CLI_REQOFFLINEMSGS_SENT = 8;
    //public static final int STATE_CLI_ACKOFFLINEMSGS_SENT = 9;

    // Privacy Lists
    private Hashtable ignoreList = new Hashtable();
    private Hashtable invisibleList = new Hashtable();
    private Hashtable visibleList = new Hashtable();

    // CLI_SETICBM packet data
    public final byte[] CLI_SETICBM_DATA = Util.explodeToBytes("0,0,0,0,0,0B,1F,40,3,E7,3,E7,0,0,0,0", ',', 16);

    // CLI_READY packet data
    public final byte[] CLI_READY_DATA = Util.explodeToBytes
            (
                    "00,22,00,01,01,10,16,4f," +
                            "00,01,00,04,01,10,16,4f," +
                            "00,13,00,04,01,10,16,4f," +
                            "00,02,00,01,01,10,16,4f," +
                            "00,03,00,01,01,10,16,4f," +
                            "00,15,00,01,01,10,16,4f," +
                            "00,04,00,01,01,10,16,4f," +
                            "00,06,00,01,01,10,16,4f," +
                            "00,09,00,01,01,10,16,4f," +
                            "00,0a,00,01,01,10,16,4f," +
                            "00,0b,00,01,01,10,16,4f",
                    ',', 16
            );

    private final short[] FAMILIES_AND_VER_LIST = {
            0x0022, 0x0001,
            0x0001, 0x0004,
            0x0013, 0x0004,
            0x0002, 0x0001,
            0x0003, 0x0001,
            0x0015, 0x0001,
            0x0004, 0x0001,
            0x0006, 0x0001,
            0x0009, 0x0001,
            0x000a, 0x0001,
            0x000b, 0x0001,
    };

    // Timeout
    public static final int TIME_OUT = 30 * 1000; // 25 seconds
    public int TIMEOUT = 30 * 1000; // milliseconds //25 seconds

    /**
     * **********************************************************************
     */

    // UIN
    private String uin;

    // Password
    private String password;

    // Server host
    private String srvHost;

    // Server port
    private String srvPort;

    // Action state
    private int state;

    // Last activity
    //    private Date lastActivity = new Date();
    private long lastActivity = 0;
    private boolean active = false;
    private boolean cancel = false;
    private boolean forceReconnect = false;

    // Temporary variables
    private String server;
    private byte[] cookie;
    private boolean srvReplyRosterRcvd;
    private Vector cItems = new Vector();
    private Vector gItems = new Vector();
    private int timestamp;
    private int count;

    // Constructor
    public ConnectAction(String uin, String password, String srvHost, String srvPort) {
        super(true, false);
        this.uin = uin;
        this.password = password;
        this.srvHost = srvHost;
        this.srvPort = srvPort;
    }

    // Returns the UID
    public String getUin() {
        return this.uin;
    }

    // Returns the password
    public String getPassword() {
        return this.password;
    }

    // Returns the server host
    public String getSrvHost() {
        return this.srvHost;
    }

    // Returns the server port
    public String getSrvPort() {
        return this.srvPort;
    }

    // Init action
    protected void init() throws JimmException {
        if (cancel) {
            return;
        }
        this.state = STATE_INIT;
        this.lastActivity = System.currentTimeMillis();

        connect(this.srvHost + ":" + this.srvPort);

        // Set STATE_INIT
        this.state = STATE_INIT_DONE;

        // Update activity timestamp
        this.lastActivity = System.currentTimeMillis();
    }

    private void connect(String server) throws JimmException {
        if (cancel) {
            return;
        }
        int retry = 1;
//#sijapp cond.if modules_PROXY is "true"#
        try {
            retry = Integer.parseInt(Options.getString(Options.OPTION_AUTORETRY_COUNT));
            retry = (retry > 0) ? retry : 1;
        } catch (NumberFormatException e) {
            retry = 1;
        }
//#sijapp cond.end#
        // Open connection
        for (int i = 0; i < retry; i++) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            lastActivity = System.currentTimeMillis();
            if (cancel) {
                return;
            }
            try {
                icq.c.connect(server);
                return;
            } catch (JimmException e) {
                if (icq.c != null) {
                    icq.c.close();
                }
                if (i >= (retry - 1) || ((this.lastActivity + this.TIMEOUT) < System.currentTimeMillis())) {
                    this.state = STATE_ERROR;
                    throw (e);
                }
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    // Forwards received packet, returns true if packet has been consumed
    protected boolean forward(Packet packet) throws JimmException {
        if (cancel) {
            return false;
        }

        // Set activity flag
        this.active = true;

        // Catch JimmExceptions
        try {
            // Flag indicates whether packet has been consumed or not
            boolean consumed = false;

            // Watch out for STATE_INIT_DONE
            if (this.state == STATE_INIT_DONE) {
                // Watch out for SRV_CLI_HELLO packet
                if (packet instanceof ConnectPacket) {
                    ConnectPacket connectPacket = (ConnectPacket) packet;
                    if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO) {
                        if (Options.getBoolean(Options.OPTION_MD5_LOGIN)) {
                            icq.sendPacket(new ConnectPacket());
                            byte[] buf = new byte[4 + this.uin.length()];
                            Util.putWord(buf, 0, 0x0001);
                            Util.putWord(buf, 2, this.uin.length());
                            byte[] uinRaw = Util.stringToByteArray(this.uin);
                            System.arraycopy(uinRaw, 0, buf, 4, uinRaw.length);
                            icq.sendPacket(new SnacPacket(0x0017, 0x0006, 0, new byte[0], buf));
                        } else {
                            // Send a CLI_IDENT packet as reply
                            ConnectPacket reply = new ConnectPacket(this.uin, this.password);
                            icq.sendPacket(reply);
                        }

                        // Move to next state
                        this.state = !Options.getBoolean(Options.OPTION_MD5_LOGIN) ? STATE_CLI_IDENT_SENT :
                                STATE_AUTHKEY_REQUESTED;

                        // Packet has been consumed
                        consumed = true;
                    }
                }
            } else if (state == STATE_AUTHKEY_REQUESTED) {
                if (packet instanceof SnacPacket) {
                    SnacPacket snacPacket = (SnacPacket) packet;
                    if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0007)) {
                        byte[] rbuf = snacPacket.getData();
                        int len = Util.getWord(rbuf, 0);
                        byte[] authkey = new byte[len];
                        //if (Options.getBoolean(Options.OPTION_REQ_AUTH)) {
                        //	authkey = new byte[1];
                        //} else authkey = new byte[0];
                        System.arraycopy(rbuf, 2, authkey, 0, len);
                        rbuf = null;
                        byte[] buf = new byte[2 + 2 + this.uin.length() + 2 + 2 + 16];
                        int marker = 0;
                        Util.putWord(buf, marker, 0x0001);
                        marker += 2;
                        Util.putWord(buf, marker, this.uin.length());
                        marker += 2;
                        byte[] uinRaw = Util.stringToByteArray(this.uin);
                        System.arraycopy(uinRaw, 0, buf, marker, uinRaw.length);
                        marker += uinRaw.length;
                        Util.putWord(buf, marker, 0x0025);
                        marker += 2;
                        Util.putWord(buf, marker, 0x0010);
                        marker += 2;
                        IcqMD5 icqMd5 = new IcqMD5();
                        byte[] md5buf = new byte[authkey.length + this.password.length() + icqMd5.AIM_MD5_STRING.length];
                        int md5marker = 0;
                        System.arraycopy(authkey, 0, md5buf, md5marker, authkey.length);
                        md5marker += authkey.length;
                        byte[] passwordRaw = Util.stringToByteArray(this.password);
                        System.arraycopy(passwordRaw, 0, md5buf, md5marker, passwordRaw.length);
                        md5marker += passwordRaw.length;
                        System.arraycopy(icqMd5.AIM_MD5_STRING, 0, md5buf, md5marker, icqMd5.AIM_MD5_STRING.length);
                        byte[] hash = icqMd5.calculateMD5(md5buf);
                        System.arraycopy(hash, 0, buf, marker, 16);
                        icq.sendPacket(new SnacPacket(0x0017, 0x0002, 0, new byte[0], buf));
                        state = STATE_CLI_IDENT_SENT;
                    } else {
                        throw new JimmException(100, 0);
                    }
                }
                consumed = true;
            }
            // Watch out for STATE_CLI_IDENT_SENT
            else if (this.state == STATE_CLI_IDENT_SENT) {
                int errcode = -1;
                if (Options.getBoolean(Options.OPTION_MD5_LOGIN)) {
                    if (packet instanceof SnacPacket) {
                        SnacPacket snacPacket = (SnacPacket) packet;
                        if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0003)) {
                            byte[] buf = snacPacket.getData();
                            int marker = 0;
                            byte[] tlvData;
                            while (marker < buf.length) {
                                tlvData = Util.getTlv(buf, marker);
                                int tlvType = Util.getWord(buf, marker);
                                marker += 4 + tlvData.length;
                                switch (tlvType) {
                                    case 0x0008:
                                        errcode = Util.getWord(tlvData, 0);
                                        break;
                                    case 0x0005:
                                        this.server = Util.byteArrayToString(tlvData);
                                        break;
                                    case 0x0006:
                                        this.cookie = tlvData;
                                        break;
                                }
                            }
                        }
                    } else if (packet instanceof DisconnectPacket) {
                        consumed = true;
                    }
                } else {
                    // watch out for channel 4 packet
                    if (packet instanceof DisconnectPacket) {
                        DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
                        // Watch out for SRV_COOKIE packet
                        if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
                            // Save cookie
                            this.cookie = disconnectPacket.getCookie();
                            this.server = disconnectPacket.getServer();
                        }
                        // Watch out for SRV_GOODBYE packet
                        else if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_GOODBYE)
                            errcode = disconnectPacket.getError();
                        consumed = true;
                    }
                }

                if (errcode != -1) {
                    int toThrow = 100;
                    icq.canReconnect = false;
                    switch (errcode) {
                        // Multiple logins
                        case 0x0001:
                            toThrow = 110;
                            break;
                        // Bad password
                        case 0x0004:
                        case 0x0005:
                            toThrow = 111;
                            break;
                        // Non-existant UIN
                        case 0x0007:
                        case 0x0008:
                            toThrow = 112;
                            break;
                        // Too many clients from same IP
                        case 0x0015:
                        case 0x0016:
                            toThrow = 113;
                            break;
                        // Rate exceeded
                        case 0x0018:
                        case 0x001d:
                            toThrow = 114;
                            break;
                    }
                    throw new JimmException(toThrow, errcode);
                }

                if (consumed & (this.server != null) & (this.cookie != null)) {
                    // Close connection (only if not HTTP Connection)
//#sijapp cond.if modules_HTTP is "true"#
//				if ((icq.c != null) && (!(icq.c instanceof jimm.comm.connection.HTTPConnection)))
//#sijapp cond.else#
                    if (icq.c != null)
//#sijapp cond.end#
                    {
                        icq.c.close();
                        // #sijapp cond.if target is "DEFAULT" | target is "MIDP2"#
                        //if (Options.getBoolean(Options.OPTION_SHADOW_CON)) try {
                        // Wait the given time before starting the
                        // new connection
                        //	Thread.sleep(2000);
                        //} catch (InterruptedException e) {
                        //}
                        // #sijapp cond.end#
                        // Open connection
                        connect(server);
                    }
                    // Move to next state
                    this.state = STATE_CLI_DISCONNECT_SENT;
                }

            }

            // Watch out for STATE_CLI_DISCONNECT_SENT
            else if (this.state == STATE_CLI_DISCONNECT_SENT) {

                // Watch out for SRV_HELLO packet
                if (packet instanceof ConnectPacket) {
                    ConnectPacket connectPacket = (ConnectPacket) packet;
                    if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO) {
                        // Send a CLI_COOKIE packet as reply
                        ConnectPacket reply = new ConnectPacket(this.cookie);
                        icq.sendPacket(reply);

                        // Move to next state
                        this.state = STATE_CLI_COOKIE_SENT;

                        // Packet has been consumed
                        consumed = true;
                    }
                }
            }
            // Watch out for STATE_CLI_COOKIE_SENT
            else if (this.state == STATE_CLI_COOKIE_SENT) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                for (int i = 0; i < FAMILIES_AND_VER_LIST.length; i++) {
                    Util.writeWord(stream, FAMILIES_AND_VER_LIST[i], true);
                }

                icq.sendPacket
                        (
                                new SnacPacket
                                        (
                                                SnacPacket.SERVICE_FAMILY,
                                                SnacPacket.CLI_FAMILIES_COMMAND,
                                                0x00000000,
                                                new byte[0],
                                                stream.toByteArray()
                                        )
                        );

                // Move to next state
                state = STATE_CLI_CAPS_SENT;
            } else if (this.state == STATE_CLI_CAPS_SENT) {
                SnacPacket reqp = new SnacPacket
                        (
                                SnacPacket.SERVICE_FAMILY,
                                SnacPacket.CLI_REQINFO_COMMAND,
                                SnacPacket.CLI_REQINFO_COMMAND,
                                new byte[0], new byte[0]
                        );
                icq.sendPacket(reqp);

                byte[] rdata = new byte[6];
                Util.putDWord(rdata, 0, 0x000B0002);
                Util.putWord(rdata, 4, 0x000F);
                reqp = new SnacPacket
                        (
                                SnacPacket.ROSTER_FAMILY,
                                SnacPacket.CLI_REQLISTS_COMMAND,
                                SnacPacket.CLI_REQLISTS_COMMAND,
                                new byte[0], rdata
                        );
                icq.sendPacket(reqp);

                // Send a CLI_REQROSTER or
                // CLI_CHECKROSTER packet
                long versionId1 = icq.getProfile().getSsiListLastChangeTime();
                int versionId2 = icq.getProfile().getSsiNumberOfItems();
                if (((versionId1 == -1) && (versionId2 == -1)) || (icq.getProfile().getItemsSize() == 0)) {
                    SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_REQROSTER_FAMILY, SnacPacket.CLI_REQROSTER_COMMAND, 0x00000000, new byte[0], new byte[0]);
                    icq.sendPacket(reply2);
                } else {
                    byte[] data = new byte[6];
                    Util.putDWord(data, 0, versionId1);
                    Util.putWord(data, 4, versionId2);
                    SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_CHECKROSTER_FAMILY, SnacPacket.CLI_CHECKROSTER_COMMAND, 0x00000000, new byte[0], data);
                    icq.sendPacket(reply2);
                }

                reqp = new SnacPacket
                        (
                                SnacPacket.LOCATION_FAMILY,
                                SnacPacket.CLI_REQLOCATION_COMMAND,
                                SnacPacket.CLI_REQLOCATION_COMMAND,
                                new byte[0], new byte[0]
                        );
                icq.sendPacket(reqp);

                rdata = new byte[6];
                Util.putDWord(rdata, 0, 0x00050002);
                Util.putWord(rdata, 4, 0x0003);
                reqp = new SnacPacket
                        (
                                SnacPacket.CONTACT_FAMILY,
                                SnacPacket.CLI_REQBUDDY_COMMAND,
                                SnacPacket.CLI_REQBUDDY_COMMAND,
                                new byte[0], rdata
                        );
                icq.sendPacket(reqp);

                reqp = new SnacPacket
                        (
                                SnacPacket.ICBM_FAMILY,
                                SnacPacket.CLI_REQICBM_COMMAND,
                                SnacPacket.CLI_REQICBM_COMMAND,
                                new byte[0], new byte[0]
                        );
                icq.sendPacket(reqp);

                reqp = new SnacPacket
                        (
                                SnacPacket.BOS_FAMILY,
                                SnacPacket.CLI_REQBOS_COMMAND,
                                SnacPacket.CLI_REQBOS_COMMAND,
                                new byte[0], new byte[0]
                        );
                icq.sendPacket(reqp);

                // Move to next state
                this.state = STATE_CLI_CHECKROSTER_SENT;
            }

            // Watch out for STATE_CLI_CHECKROSTER_SENT
            else if (this.state == STATE_CLI_CHECKROSTER_SENT) {
                // if Options.getBoolean(Options.OPTION_DOWNLOAD_CL);
                // Watch out for SNAC packet
                if (packet instanceof SnacPacket) {
                    SnacPacket snacPacket = (SnacPacket) packet;

                    // Watch out for
                    // SRV_REPLYROSTEROK
                    if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTEROK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTEROK_COMMAND)) {
                        this.srvReplyRosterRcvd = true;

                        // Packet has been consumed
                        consumed = true;
                    }
                    // watch out for SRV_REPLYROSTER
                    // packet
                    else if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTER_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTER_COMMAND)) {
                        if (snacPacket.getFlags() != 1) this.srvReplyRosterRcvd = true;

                        // Get data
                        byte[] buf = snacPacket.getData();
                        int marker = 0;

                        // Check length
                        if (buf.length < 3) {
                            throw (new JimmException(115, 0));
                        }

                        // Skip
                        // SRV_REPLYROSTER.UNKNOWN
                        marker += 1;

                        // Iterate through all
                        // items
                        count = Util.getWord(buf, marker);
                        marker += 2;

                        ContactItem item;
                        GroupItem grp;
                        for (int i = 0; i < count; i++) {
                            // Check length
                            if (buf.length < marker + 2) {
                                throw (new JimmException(115, 1));
                            }

                            // Get name length
                            int nameLen = Util.getWord(buf, marker);
                            marker += 2;

                            // Check length
                            if (buf.length < marker + nameLen + 2 + 2 + 2 + 2) {
                                throw (new JimmException(115, 2));
                            }

                            // Get name
                            String name = Util.byteArrayToString(buf, marker, nameLen, Util.isDataUTF8(buf, marker, nameLen));
                            marker += nameLen;

                            // Get group, id and type
                            int group = Util.getWord(buf, marker);
                            int id = Util.getWord(buf, marker + 2);
                            int type = Util.getWord(buf, marker + 4);
                            marker += 6;

                            // Get length of the following TLVs
                            int len = Util.getWord(buf, marker);
                            marker += 2;

                            // Check length
                            if (buf.length < marker + len) {
                                throw (new JimmException(115, 3));
                            }

                            // Normal contact
                            if ((type == 0x0000) || ((type == 0x0019 || type == 0x001B) && Options.getBoolean(Options.OPTION_CACHE_CONTACTS))) {
                                // ByteArrayOutputStream serverData = new ByteArrayOutputStream();

                                // Get nick
                                String nick = new String(name);

                                boolean noAuth = false;
                                byte[] tlvData;
                                while (len > 0) {
                                    tlvData = Util.getTlv(buf, marker);
                                    if (tlvData == null) {
                                        throw (new JimmException(115, 4));
                                    }
                                    int tlvType = Util.getWord(buf, marker);
                                    if (tlvType == 0x0131) {
                                        nick = Util.byteArrayToString(tlvData, true);
                                    } else if (tlvType == 0x0066) {
                                        noAuth = true;
                                    }

                                    //else if (tlvType == 0x006D) /* Server-side additional data */
                                    //{
                                    //	Util.writeWord(serverData, tlvType, true);
                                    //	Util.writeWord(serverData, tlvData.length, true);
                                    //	Util.writeByteArray(serverData, tlvData);
                                    //
                                    //	Util.showBytes(serverData.toByteArray());
                                    //}

                                    len -= 4;
                                    len -= tlvData.length;
                                    marker += 4 + tlvData.length;
                                }
                                if (len != 0) {
                                    throw (new JimmException(115, 5));
                                }

                                // Add this contact item to the vector
                                try {
                                    boolean cache = (type == 0x0019 || type == 0x001B);
                                    item = icq.getProfile().getItemByUIN(name);
                                    if (item == null) {
                                        item = new ContactItem(icq.getProfile());
                                    } else if (cache) {
                                        nick = item.getText();
                                    }
                                    item.init(id, group, name, nick, noAuth, !cache);
                                    if (cache) {
                                        item.setBooleanValue(ContactItem.CONTACTITEM_PHANTOM, true);
                                    }
                                    item.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, icq.getProfile().getChatHistory().chatHistoryExists(name));
                                    // if (serverData.size() != 0) item.ssData = serverData.toByteArray();

//                                    // Privacy Lists
//                                    Integer iid = (Integer) invisibleList.get(name);
//                                    item.setInvisibleId((iid != null) ? iid.intValue() : 0);
//
//                                    Integer vid = (Integer) visibleList.get(name);
//                                    item.setVisibleId((vid != null) ? vid.intValue() : 0);
//
//                                    Integer xid = (Integer) ignoreList.get(name);
//                                    item.setIgnoreId((xid != null) ? xid.intValue() : 0);

                                    cItems.addElement(item);
                                } catch (Exception e) {
                                    JimmException.handleExceptionEx(e);
                                    // Contact with wrong uin was received
                                }
                            } else if (type == 0x0001) {
                                // Skip TLVs
                                marker += len;

                                // Add this group item to the vector
                                if (group != 0x0000) {
                                    grp = icq.getProfile().getGroupById(group);
                                    if (grp == null) {
                                        grp = new GroupItem(group, name);
                                    }
                                    grp.setName(name);
                                    gItems.addElement(grp);
                                }
                            }

                            // Privacy Lists
                            // Permit record ("Allow" list in AIM, and "Visible" list in ICQ)
                            else if (type == 0x0002) {
                                marker += len;
                                visibleList.put(name, new Integer(id));
                            }
                            // Deny record ("Block" list in AIM, and "Invisible" list in ICQ)
                            else if (type == 0x0003) {
                                marker += len;
                                invisibleList.put(name, new Integer(id));
                            }
                            // Ignore list record.
                            else if (type == 0x000E) {
                                marker += len;
                                ignoreList.put(name, new Integer(id));
                            }

                            // My visibility settings
                            else if (type == 0x0004) {
                                byte[] tlvData;
                                while (len > 0) {
                                    tlvData = Util.getTlv(buf, marker);
                                    if (tlvData == null) {
                                        throw (new JimmException(115, 110));
                                    }
                                    int tlvType = Util.getWord(buf, marker);

                                    if (tlvType == 0x00CA) {
                                        Options.setInt(Options.OPTION_VISIBILITY_ID, (int) id);
                                    }

                                    len -= 4;
                                    len -= tlvData.length;
                                    marker += 4 + tlvData.length;
                                }
                                if (len != 0) {
                                    throw (new JimmException(115, 111));
                                }
                            }
                            // All other item types
                            else {
                                // Skip TLVs
                                marker += len;
                            }
                        }

                        // Check length
                        if (buf.length != marker + 4) {
                            throw (new JimmException(115, 6));
                        }

                        // Get timestamp
                        timestamp = (int) Util.getDWord(buf, marker);

                        // Update contact list
                        //if (Options.getBoolean(Options.OPTION_UPDATE_CL) || Options.getBoolean(Options.OPTION_UPDATE_CL_FC))
                        //{
                        //Options.setBoolean(Options.OPTION_UPDATE_CL_FC, false);
                        //ContactListItem[] itemsAsArray = new ContactListItem[items.size()];
                        //items.copyInto(itemsAsArray);
                        //Jimm.getContactList().update(snacPacket.getFlags(), timestamp, count, itemsAsArray);
                        //}
                        // Packet has been consumed
                        consumed = true;
                    }

                    // Check if all required packets have been received
                    if (this.srvReplyRosterRcvd) {
                        if (Options.getBoolean(Options.OPTION_SAVE_TEMP_CONTACTS)) {
                            String[] uins = icq.getProfile().getTempContacts();
                            for (int i = uins.length - 1; i >= 0; i--) {
                                cItems.addElement(icq.getProfile().getItemByUIN(uins[i]));
                            }
                            this.lastActivity = System.currentTimeMillis();
                        }
                        icq.getProfile().setItems(timestamp, count, cItems, gItems);

                        for (int i = 0; i < cItems.size(); i++) {
                            ContactItem cItem = (ContactItem) cItems.elementAt(i);
                            String uin = cItem.getUinString();
                            Integer iid = (Integer) invisibleList.get(uin);
                            cItem.setInvisibleId((iid != null) ? iid.intValue() : 0);
                            Integer vid = (Integer) visibleList.get(uin);
                            cItem.setVisibleId((vid != null) ? vid.intValue() : 0);
                            Integer xid = (Integer) ignoreList.get(uin);
                            cItem.setIgnoreId((xid != null) ? xid.intValue() : 0);
                        }

                        // Send a CLI_ROSTERACK packet
                        SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_ROSTERACK_FAMILY, SnacPacket.CLI_ROSTERACK_COMMAND, 0x00000000, new byte[0], new byte[0]);
                        icq.sendPacket(reply1);

                        // Send a CLI_SETUSERINFO packet
                        // Set version information to this packet in our capability
                        icq.sendPacket(OtherAction.getStandartUserInfoPacket(icq));
                        //OtherAction.setWebAware();

                        // Send a CLI_SETICBM packet
                        SnacPacket reply0;

                        // If typing notify is on, we send full caps..with typing
                        byte[] tmp_packet;

                        //#sijapp cond.if target isnot "DEFAULT"#
                        if (Options.getInt(Options.OPTION_TYPING_MODE) > 0) {
                            reply0 = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY, SnacPacket.CLI_SETICBM_COMMAND, 0x00000000, new byte[0], CLI_SETICBM_DATA);
                        } else {
                            //#sijapp cond.end#
                            tmp_packet = CLI_SETICBM_DATA;
                            tmp_packet[5] = 0x03;
                            reply0 = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY, SnacPacket.CLI_SETICBM_COMMAND, 0x00000000, new byte[0], tmp_packet);
                            //#sijapp cond.if target isnot "DEFAULT"#
                        }
                        //#sijapp cond.end#
                        icq.sendPacket(reply0);

                        int onlineStatusOpt = icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS);
                        int onlineStatus = Util.translateStatusSend(onlineStatusOpt);
                        int pstatus = icq.getProfile().getInt(Profile.OPTION_PSTATUS);
                        int visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
                        byte[] buf = new byte[15];
                        byte bCode = 0;
                        int cmd = SnacPacket.CLI_ROSTERUPDATE_COMMAND;
                        if (visibilityItemId == 0) {
                            Options.setInt(Options.OPTION_VISIBILITY_ID, Util.createRandomId(icq));
                            cmd = SnacPacket.CLI_ROSTERADD_COMMAND;
                        }
                        visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
                        if (visibilityItemId != 0) {
                            // Build packet for privacy setting changing
                            int marker = 0;
                            boolean needRestore = true;

                            if (onlineStatus == Util.SET_STATUS_INVISIBLE) {
                                pstatus = (onlineStatusOpt == ContactItem.STATUS_INVIS_ALL) ? (byte) 2 : (byte) 3;
                                needRestore = false;
                            }
                            Util.putWord(buf, marker, 0);
                            marker += 2; // name (null)
                            Util.putWord(buf, marker, 0);
                            marker += 2; // GroupID
                            Util.putWord(buf, marker, visibilityItemId);
                            marker += 2; // EntryID
                            Util.putWord(buf, marker, 4);
                            marker += 2; // EntryType
                            Util.putWord(buf, marker, 5);
                            marker += 2; // Length in bytes of following TLV
                            Util.putWord(buf, marker, 0xCA);
                            marker += 2; // TLV Type
                            Util.putWord(buf, marker, 1);
                            marker += 2; // TLV Length
                            Util.putByte(buf, marker, (needRestore) ? (byte) icq.transformVisId(pstatus) : (byte) pstatus);             // TLV Value
                            icq.sendPacket(new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY, cmd, buf));
                        }
                        icq.sendPacket(OtherAction.getStatusPacket(icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS), icq));
                        icq.setMood();

                        // Send to server sequence of unuthoruzed contacts to see their statuses
//						String[] noauth = Jimm.getContactList().getUnauthAndTempContacts();
//						if (noauth.length > 0) icq.addLocalContacts(noauth);

                        this.state = STATE_CLI_STATUS_INFO_SENT;
                        this.lastActivity = System.currentTimeMillis();
                    }
                }
            } else if (this.state == STATE_CLI_STATUS_INFO_SENT) {
                // Send a CLI_READY packet
                SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_READY_FAMILY, SnacPacket.CLI_READY_COMMAND, 0x00000000, new byte[0], CLI_READY_DATA);
                icq.sendPacket(reply2);

                // Send a CLI_TOICQSRV/CLI_REQOFFLINEMSGS packet
                ToIcqSrvPacket reply3 = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_REQOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
                icq.sendPacket(reply3);

                // Move to STATE_CONNECTED
                icq.setConnected();
                // Packet has been consumed
                consumed = true;

                // Move to next state
                this.state = STATE_CLI_REQOFFLINEMSGS_SENT;
                // } else if (this.state == STATE_CLI_REQOFFLINEMSGS_SENT) {
                // if (packet instanceof SnacPacket) {
                // SnacPacket snPacket = (SnacPacket)packet;

                // // Error after requesting offline messages?
                // if ((snPacket.getFamily() == 0x0015) && (snPacket.getCommand() == 0x0001)) {
// // 						System.out.println("Error after requesting offline messages");
                // // Move to next state
                // this.state = STATE_CLI_ACKOFFLINEMSGS_SENT;
                // // Move to STATE_CONNECTED
                // icq.setConnected();
                // // Packet has been consumed
                // consumed = true;
                // }
                // }
                // if (packet instanceof FromIcqSrvPacket && !consumed) {
                // FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

// //					System.out.println(Integer.toHexString(fromIcqSrvPacket.getSubcommand()));
                // // Watch out for SRV_OFFLINEMSG
                // if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_OFFLINEMSG_SUBCMD) {
                // // Get raw data
                // byte[] buf = fromIcqSrvPacket.getData();

                // // Check length
// //						System.out.println(""+buf.length);
                // if (buf.length < 14) {
                // return false;
                // }

                // // Extract UIN
                // long uinRaw = Util.getDWord(buf, 0, false);

                // String uin = String.valueOf(uinRaw);

                // // Extract date of dispatch
                // long date = DateAndTime.createLongTime
                // (
                // Util.getWord(buf, 4, false),
                // Util.getByte(buf, 6),
                // Util.getByte(buf, 7),
                // Util.getByte(buf, 8),
                // Util.getByte(buf, 9),
                // 0
                // );

                // // Get type
                // int type = Util.getWord(buf, 10, false);

                // // Get text length
                // int textLen = Util.getWord(buf, 12, false);

                // // Check length
                // // if (buf.length != 14 + textLen) { throw (new JimmException(116, 1)); }

                // // Check length
                // if (buf.length >= 14 + textLen) {
                // String text = null;
                // // Get text
                // try {
                // text = Util.removeCr(Util.byteArrayToString(buf, 14, textLen, Util.isDataUTF8(buf, 14, textLen))); // old
                // // String text = Util.removeCr(Util.byteArrayToString(buf, 14, textLen, true)); // new
                // } catch (ArrayIndexOutOfBoundsException aioobe) {
                // }

                // if (text == null) {
                // } else if (type == 0x0001) {
                // // Forward message to contact list
                // PlainMessage message = new PlainMessage(uin, this.uin, DateAndTime.gmtTimeToLocalTime(date), text, true);
                // icq.getActListener().addMessage(message);
                // } else if (type == 0x0004) {
                // // Search for delimiter
                // int delim = text.indexOf(0xFE);

                // // Split message, if delimiter could be found
                // String urlText;
                // String url;
                // if (delim != -1) {
                // urlText = text.substring(0, delim);
                // url = text.substring(delim + 1);
                // } else {
                // urlText = text;
                // url = "";
                // }

                // // Forward message message to contact list
                // UrlMessage message = new UrlMessage(uin, this.uin, DateAndTime.gmtTimeToLocalTime(date), url, urlText);
                // icq.getActListener().addMessage(message);
                // }
                // }

                // // Packet has been consumed
                // consumed = true;
                // } else if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_DONEOFFLINEMSGS_SUBCMD) {
                // // Send a CLI_TOICQSRV/CLI_ACKOFFLINEMSGS packet
                // ToIcqSrvPacket reply = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_ACKOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
                // icq.sendPacket(reply);

                // // Move to next state
                // this.state = STATE_CLI_ACKOFFLINEMSGS_SENT;

                // // Move to STATE_CONNECTED
                // icq.setConnected();

                // // Packet has been consumed
                // consumed = true;
                // }
                // }
            }

            // Update activity timestamp and reset activity flag
            this.lastActivity = System.currentTimeMillis();
            this.active = false;

            // Return consumption flag
            return (consumed);
        } catch (JimmException e) {
            // Update activity timestamp and reset activity flag
            this.lastActivity = System.currentTimeMillis();
            this.active = false;

            // Set error state if exception is critical
            if (e.isCritical()) {
                this.state = STATE_ERROR;
            }

            // Forward exception
            throw (e);
        }
    }

    // Returns true if the action is completed
    public boolean isCompleted() {
        return (this.state == STATE_CLI_REQOFFLINEMSGS_SENT || cancel);
    }

    // Returns true if an error has occured
    public boolean isError() {
        if (cancel || forceReconnect) {
            return false;
        }
        if ((this.state != STATE_ERROR) && (!this.active) && (this.lastActivity + this.TIMEOUT < System.currentTimeMillis())) {
            icq.getProfile().actionCompleted(this, true);
            this.state = STATE_ERROR;
            JimmException e = new JimmException(118, 0);
            icq.reconnect(e);
            forceReconnect = true;
        }
        return (this.state == STATE_ERROR);
    }

    // Returns a number between 0 and 100 (inclusive) which indicates the current progress
    public int getProgress() {
        switch (this.state) {
            case STATE_INIT_DONE:
                return 15;
            case STATE_AUTHKEY_REQUESTED:
                return 25;
            case STATE_CLI_IDENT_SENT:
                return 38;
            case STATE_CLI_DISCONNECT_SENT:
                return 50;
            case STATE_CLI_COOKIE_SENT:
            case STATE_CLI_CAPS_SENT:
                return 63;
            case STATE_CLI_CHECKROSTER_SENT:// case STATE_CLI_STATUS_INFO_SENT:
                return 75;
            case STATE_CLI_STATUS_INFO_SENT://case STATE_CLI_REQOFFLINEMSGS_SENT:
                return 98;
            case STATE_CLI_REQOFFLINEMSGS_SENT://case STATE_CLI_ACKOFFLINEMSGS_SENT:
                return 100;
            default:
                return (0);
        }
    }

    public String getProgressMsg() {
        switch (this.state) {
            case STATE_INIT_DONE:
                return srvHost;
            case STATE_AUTHKEY_REQUESTED:
                //return "Connecting...25%";
            case STATE_CLI_IDENT_SENT:
                //return "Connecting...38%";
            case STATE_CLI_DISCONNECT_SENT:
                //return "Connecting...50%";
            case STATE_CLI_COOKIE_SENT:
            case STATE_CLI_CAPS_SENT:
                //return "Connecting...63%";
            case STATE_CLI_CHECKROSTER_SENT:// case STATE_CLI_STATUS_INFO_SENT:
                //return "Connecting...75%";
            case STATE_CLI_STATUS_INFO_SENT://case STATE_CLI_REQOFFLINEMSGS_SENT:
                //return "Connecting...98%";
            case STATE_CLI_REQOFFLINEMSGS_SENT://case STATE_CLI_ACKOFFLINEMSGS_SENT:
                return "";
            //return (new StringBuffer(ResourceBundle.getString("connecting", ResourceBundle.FLAG_ELLIPSIS)))
            //            .append(getProgress()).append('%').toString();
            //return ResourceBundle.getString("connecting", ResourceBundle.FLAG_ELLIPSIS);
            //return "Are you READY!?)";
            default:
                return (srvHost);
        }
    }

    public void onEvent(int eventType) {
        switch (eventType) {
            case ON_COMPLETE:
                Object obj = Jimm.getCurrentDisplay();
                if (obj instanceof jimm.ui.ProfileList) {
                    ((jimm.ui.ProfileList) obj).update();
                }
                if (icq.getProfile().isCurrent()) {
                    Jimm.getContactList().updateTree();
//                    RequestInfoAction act = new RequestInfoAction(icq.getUin(), "", null);
//                    try{
//                        icq.requestAction(act);
//                    } catch (JimmException e) {}
                }
                try {
                    icq.sendPacket(OtherAction.getFlagsPacket(icq));
                } catch (JimmException e) {
                }
                //TimerTasks.setStatusTimer(); //203738837
                break;

            case ON_CANCEL:
                if (cancel) {
                    break;
                }
                cancel = true;
                icq.reconnect_attempts = 0;
                icq.disconnect();
                break;
        }
    }
}