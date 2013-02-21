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
 File: src/jimm/comm/Icq.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import DrawControls.Icon;
import jimm.*;
//#sijapp cond.if modules_HTTP is "true"#
import jimm.comm.connection.HTTPConnection;
//#sijapp cond.end#
// #sijapp cond.if modules_FILES is "true"#
import jimm.comm.connection.PeerConnection;
// #sijapp cond.end#
import jimm.comm.connection.SOCKETConnection;
// #sijapp cond.if modules_PROXY is "true"#
import jimm.comm.connection.SOCKSConnection;
// #sijapp cond.end#

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.Vector;

public class Icq implements Runnable {
    public static final byte[] MTN_PACKET_BEGIN = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01
    };
    // State constants
    // private static final int STATE_NOT_CONNECTED = 0;
    // private static final int STATE_CONNECTED = 1;

    // Current state
    private boolean connected = false;

    // Requested actions
    private Vector reqAction = new Vector();

    // Thread
    private volatile Thread thread;

    private String lastStatusChangeTime;

    public Icq(Profile profile) {
        this.profile = profile;
    }

    private Profile profile;

    public Profile getProfile() {
        return profile;
    }

    public String getUin() {
        return getProfile().getUin();
    }

    public String getPassword() {
        return getProfile().getPassword();
    }

    // public boolean containAction(Action act) {
    // if (act == null || actAction == null) {
    // return false;
    // }
    // return actAction.contains(act);
    // }

    // Request an action

    public void requestAction(Action act) throws JimmException {
        // Set reference to this ICQ object for callbacks
        act.setIcq(this);

        // Look whether action is executable at the moment
        if (!act.isExecutable()) {
            throw (new JimmException(140, 0));
        }

        // Queue requested action
        synchronized (reqAction) {
            reqAction.addElement(act);
        }

        // Connect?
        if (act instanceof ConnectAction) {
            // Create new thread and start
            thread = null;
            thread = new Thread(this);
            thread.start();
        }
        waitNotify();
    }

    public void sendPacket(Packet packet) throws JimmException {
        if (c == null) {
            JimmException je = new JimmException(141, 0);
            reconnect(je);
        } else if (packet != null) {
            c.sendPacket(packet);
        }
    }

    // Sends to server client-side contacts
    // public void addLocalContacts(String[] uins) {
    // if ((uins == null) || (uins.length == 0)) {
    // return;
    // }
    // int len = 0, i;
    // for (i = 0; i < uins.length; i++) {
    // len += (1+uins[i].length());
    // }
    // byte[] buf = new byte[len];

    // int position = 0;
    // for (i = 0; i < uins.length; i++) {
    // byte[] rowUin = Util.stringToByteArray(uins[i]);
    // buf[position++] = (byte)rowUin.length;
    // System.arraycopy(rowUin, 0, buf, position, rowUin.length);
    // position += rowUin.length;
    // }

    // try {
    // sendPacket(new SnacPacket(0x0003, 0x0004, 0, new byte[0], buf));
    // } catch (JimmException e) {
    // JimmException.handleException(e);
    // }
    // }

    // public void removeLocalContact(String uin) {
    // byte[] buf = new byte[1 + uin.length()];
    // Util.putByte(buf, 0, uin.length());
    // System.arraycopy(uin.getBytes(), 0, buf, 1, uin.length());
    // try {
    // sendPacket(new SnacPacket(0x0003, 0x0005, 0, new byte[0], buf));
    // } catch (JimmException e) {
    // JimmException.handleException(e);
    // }
    // }

    // Adds a ContactItem to the server saved contact list

    public synchronized void addToContactList(ContactItem cItem) {
        // Request contact item adding
        UpdateContactListAction act = new UpdateContactListAction(cItem, UpdateContactListAction.ACTION_ADD);

        try {
            requestAction(act);
        } catch (JimmException e) {
            JimmException.handleException(e);
            if (e.isCritical()) {
                return;
            }
        }

        // Start timer
        Jimm.getContactList().activate();
        getProfile().addAction("wait", act);
        // System.out.println("start addContact");
    }

    //Random server
    public String getSrvHost() {
        String servers = Options.getString(Options.OPTION_SRV_HOST).replace('\n', ' ');
        String[] serverList = Util.explode(servers, ' ');
        String server = Util.replaceStr(serverList[0], "\r", "");
        return server;
    }

    //Random server. Reset to next server
    public void nextSrvHost() {
        String servers = Options.getString(Options.OPTION_SRV_HOST);
        char delim = (servers.indexOf(' ') < 0) ? '\n' : ' ';
        String[] serverList = Util.explode(servers, delim);
        if (serverList.length < 2) {
            return;
        }
        StringBuffer newSrvs = new StringBuffer();
        for (int i = 1; i < serverList.length; i++) {
            newSrvs.append(serverList[i]);
            newSrvs.append(delim);
        }
        newSrvs.append(serverList[0]);
        Options.setString(Options.OPTION_SRV_HOST, newSrvs.toString());
    }

    public void connect() {
        connect(true);
    }

    // Connects to the ICQ network
    private void connect(boolean reset) {
        String uin = getUin();
        String pass = getPassword();
        canReconnect = true;
// #sijapp cond.if target isnot "MOTOROLA"#
        if (Options.getBoolean(Options.OPTION_SHADOW_CON) && reset) {
            // Make the shadow connection for Nokia 6230 of other devices if needed
            ContentConnection ctemp = null;
            DataInputStream istemp = null;
            try {
                //String url = "http://shadow.jimm.org/";
                //String url = "http://shadow.mrdark.ru/";
                String url = "http://c.jimm.im:8051/fake";
                ctemp = (ContentConnection) Connector.open(url);

                istemp = ctemp.openDataInputStream();
            } catch (Exception e) {
                JimmException.handleExceptionEx(e);
            }
        }
// #sijapp cond.end#

        // Connect
        ConnectAction act = new ConnectAction(uin, pass, getSrvHost(), Options.getString(Options.OPTION_SRV_PORT));
        try {
            requestAction(act);
        } catch (JimmException e) {
            if (!reconnect(e)) {
                canReconnect = false;
            }
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
            disconnect();
// #sijapp cond.if modules_DEBUGLOG is "true"#
//			DebugLog.addText("Error in Icq.connect()\n"+e.getMessage());
// #sijapp cond.end#
        }

        //Jimm.setStatusesToDraw(jimm.JimmUI.getStatusImageIndex(getProfile().getInt(Profile.OPTION_ONLINE_STATUS)),
                //XStatus.getStatusImage(getProfile().getInt(Profile.OPTION_XSTATUS))
        //);

        // Start timer
        getProfile().addAction(getSrvHost(), act);

        lastStatusChangeTime = DateAndTime.getDateString(true, false);
    }

    public void disconnect() {
        disconnect(true);
    }

    /* Disconnects from the ICQ network */
    public void disconnect(boolean reset) {
        if (reset) {
            canReconnect = false;
        }
        /* Disconnect */
        if (c != null) {
            c.close();
        }
        resetServerCon();
// #sijapp cond.if modules_FILES is "true"#
        resetPeerCon();
// #sijapp cond.end#

        /* Reset all contacts offine */
        waitNotify();
        if (reset) {
            if (stChanged > 0) {
                getProfile().setInt(Profile.OPTION_ONLINE_STATUS, oldStatus);
                getProfile().saveOptions();
                stChanged = 0;
                statusChanged = (byte) Math.max(statusChanged - 1, 0);
            }
            Object obj = Jimm.getCurrentDisplay();
            if (obj instanceof jimm.ui.ProfileList) {
                ((jimm.ui.ProfileList) obj).update();
            }
            if (profile.isCurrent()) {
                Jimm.getContactList().setStatusesOffline(false);
            } else {
                profile.setStatusesOffline();
            }
        }
    }

    // Dels a ContactItem to the server saved contact list
    public synchronized boolean delFromContactList(ContactItem cItem) {
        // Check whether contact item is temporary
        if (cItem.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) || cItem.getBooleanValue(ContactItem.CONTACTITEM_PHANTOM)) {
            // Remove this temporary contact item
            //removeLocalContact(cItem.getUinString());
            profile.removeContactItem(cItem);

            // Activate contact list
            //Jimm.getContactList().activate();
        }
        if (!cItem.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP)) {
            // Request contact item removal
            UpdateContactListAction act2 = new UpdateContactListAction(cItem, UpdateContactListAction.ACTION_DEL);
            try {
                requestAction(act2);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    return false;
                }
            }
            //getProfile().addAction("wait", act2);
        }
        return true;
    }

    //#sijapp cond.if target isnot "DEFAULT"#
    public void beginTyping(String uin, boolean isTyping) throws JimmException {
        if (!getProfile().getBoolean(Profile.OPTION_MESS_NOTIF_TYPE)) {
            byte[] uinRaw = Util.stringToByteArray(uin);
            int tempBuffLen = MTN_PACKET_BEGIN.length + 1 + uinRaw.length + 2;
            int marker = 0;
            byte[] tempBuff = new byte[tempBuffLen];
            System.arraycopy(MTN_PACKET_BEGIN, 0, tempBuff, marker, MTN_PACKET_BEGIN.length);
            marker += MTN_PACKET_BEGIN.length;
            Util.putByte(tempBuff, marker, uinRaw.length);
            marker += 1;
            System.arraycopy(uinRaw, 0, tempBuff, marker, uinRaw.length);
            marker += uinRaw.length;
            Util.putWord(tempBuff, marker, ((isTyping) ? (0x0002) : (0x0000)));
            marker += 2;
            // Send packet
            SnacPacket snacPkt = new SnacPacket(0x0004, 0x0014, 0x00000000, new byte[0], tempBuff);
            sendPacket(snacPkt);
        }
    }
    //#sijapp cond.end#

    // Checks whether the comm. subsystem is in STATE_NOT_CONNECTED

    public boolean isNotConnected() {
        return !connected;
    }

    // Puts the comm. subsystem into STATE_NOT_CONNECTED
    protected void setNotConnected() {
        connected = false;
    }

    public boolean connectionIsActive() {
        return c != null && (canReconnect && !isConnected());
    }

    // Checks whether the comm. subsystem is in STATE_CONNECTED
    public boolean isConnected() {
        return connected;
    }

    // Puts the comm. subsystem into STATE_CONNECTED
    protected void setConnected() {
        //String servers = Options.getString(Options.OPTION_SRV_HOST).replace('\n', ' ');
        reconnect_attempts = /*Util.explode(servers, ' ').length * */Options.getInt(Options.OPTION_RECONNECT_NUMBER);
        connected = true;
    }

    public ActionListener getActListener() {
        return actListener;
    }

    // Resets the comm. subsystem
    public void resetServerCon() {
        // Stop thread
        thread = null;
//#sijapp cond.if modules_DEBUGLOG is "true" #
//		jimm.DebugLog.addText("Trying to stop the thread.");
//#sijapp cond.end #
        c = null;
//#sijapp cond.if modules_DEBUGLOG is "true" #
//		jimm.DebugLog.addText("Connection destroyed.");
//#sijapp cond.end #

        // Wake up thread in order to complete
        //waitNotify();

        // Reset all variables
        connected = false;

        // Delete all actions
        if (actAction != null) {
            actAction.removeAllElements();
            getProfile().removeAllActions();
        }
        if (reqAction != null) {
            reqAction.removeAllElements();
        }
    }

    // #sijapp cond.if modules_FILES is "true"#
    // Resets the comm. subsystem
    public void resetPeerCon() {
        // Close connection
        peerC = null;
    }
// #sijapp cond.end#

    /** *********************************************************************** */
    /** *********************************************************************** */
    /**
     * **********************************************************************
     */

    // Wait object
    private final Object wait = new Object();

    // Connection to the ICQ server
    public jimm.comm.connection.Connection c;

    // #sijapp cond.if modules_FILES is "true"#
    // Connection to peer
    PeerConnection peerC;
// #sijapp cond.end#

    // All currently active actions
    private Vector actAction;

    // Action listener
    private ActionListener actListener;

    public int reconnect_attempts;


    private Packet pingPacket = null;
    private long prevPingTime = 0;
    private long keepAliveInterv = 0;

    private void ping() {
        if (!Options.getBoolean(Options.OPTION_KEEP_CONN_ALIVE)) {
            return;
        }
        if (!isConnected()) {
            return;
        }

        if (pingPacket == null) {
            pingPacket = new Packet(5, new byte[0]);
            keepAliveInterv = Util.strToIntDef(Options.getString(Options.OPTION_CONN_ALIVE_INVTERV), 120);
            keepAliveInterv = Math.max(keepAliveInterv, 1) * 1000;
            prevPingTime = System.currentTimeMillis();
        }

        long time = System.currentTimeMillis();
        if (time > (prevPingTime + keepAliveInterv)) {
            prevPingTime = time;
            try {
                sendPacket(pingPacket);
            } catch (JimmException ignored) {
            }
        }
    }

    // todo null pointer if connect
    private boolean analysisPackets() throws JimmException, Exception {
        boolean dcPacketAvailable = false;
        Action newAction;
        synchronized (reqAction) {
            if (reqAction.size() > 0) {
                if ((actAction.size() == 1) && (((Action) actAction.elementAt(0)).isExclusive())) {
                    newAction = null;
                } else {
                    newAction = (Action) reqAction.elementAt(0);
                    if (((actAction.size() > 0) && (newAction.isExclusive())) || (!newAction.isExecutable())) {
                        newAction = null;
                    } else {
                        reqAction.removeElementAt(0);
                    }
                }
            } else {
                newAction = null;
            }
        }

        boolean analysised = false;
        if (newAction != null) {
            newAction.init();
            if (!(newAction.isCompleted() || newAction.isError())) {
                actAction.addElement(newAction);
            }
            analysised = true;
        }

        // Set dcPacketAvailable to true if the peerC is not null and there is an packet waiting
// #sijapp cond.if modules_FILES is "true"#
        if (peerC != null) {
            dcPacketAvailable = peerC.available() > 0;
        }
// #sijapp cond.end#
        if (c.available() > 0) {
            Packet packet;
            while ((c.available() > 0) || dcPacketAvailable) {
                packet = null;
                if (c.available() > 0) packet = c.getPacket();
// #sijapp cond.if modules_FILES is "true"#
                else if (dcPacketAvailable) packet = peerC.getPacket();
// #sijapp cond.end#
                analysisPacket(packet);
            }
            analysised = true;
            Action action;
            // Remove completed actions
            for (int i = actAction.size() - 1; i >= 0; i--) {
                action = (Action) actAction.elementAt(i);
                if (action.isCompleted() || action.isError()) {
                    actAction.removeElementAt(i);
                }
            }
        }
        return analysised;
    }

    private void analysisPacket(Packet packet) throws JimmException {
        if (packet == null) {
            return;
        }
        // Forward received packet to all active actions and to the  action listener
        Action action;
        for (int i = actAction.size() - 1; i >= 0; i--) {
            try {
                action = (Action) actAction.elementAt(i);
                if (action.isCompleted() || action.isError()) {
                    continue;
                }
                if (action.forward(packet)) {
                    return;
                }
            } catch (JimmException e) {
                throw e;
            } catch (Exception e) {
                JimmException.handleExceptionEx(e);
            }
        }
        try {
            actListener.forward(packet);
        } catch (JimmException e) {
            throw e;
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
//#sijapp cond.if modules_DEBUGLOG is "true" #
//			jimm.DebugLog.addText("Error in ActionListener: "+e.getMessage());
//#sijapp cond.end #
        }
    }

    private boolean newActionsIsAvailable() {
        try {
            return analysisPackets();
        } catch (JimmException e) {
            reconnect(e);
            return false;
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
//#sijapp cond.if modules_DEBUGLOG is "true" #
//#			String str = new String();
//#			if (c == null) str = "\nConnection destroyed. Dont worry ;)";
//#			jimm.DebugLog.addText("Error in Icq's thread: "+e.toString()+" "+e.getClass().getName()+str);
//#sijapp cond.end #
            //disconnect();
            return false;
        }
    }

    private void sleep(long ms) {
        try {
            synchronized (wait) {
                wait.wait(ms);
            }
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
        }
    }

    public void waitNotify() {
        try {
            synchronized (wait) {
                wait.notify();
            }
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
        }
    }

    public void run() {
        // Instantiate connections
        if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_SOCKET) {
            c = new SOCKETConnection(this);
//#sijapp cond.if modules_HTTP is "true"#
        } else if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_HTTP) {
            c = new HTTPConnection(this);
//#sijapp cond.end#
// #sijapp cond.if modules_PROXY is "true"#
        } else if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_PROXY) {
            c = new SOCKSConnection(this);
// #sijapp cond.end#
        }
        // Instantiate active actions vector
        actAction = new Vector();
        // Instantiate action listener
        actListener = new ActionListener(this);
        prevPingTime = System.currentTimeMillis();
        try {
            do {
                if (Thread.currentThread() != thread) {
                    break;
                }
                if (!newActionsIsAvailable()) {
                    ping();
                    sleep(250);
                }
                if (isConnected()) {
                    autoStatus();
                }
            } while ((c != null) && (c.isConnected()));
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
//			System.out.println(e.toString());
        }
        if (Thread.currentThread() == thread) {
            disconnect();
        }
//#sijapp cond.if modules_DEBUGLOG is "true" #
//		jimm.DebugLog.addText("Thread has been stopped.");
//#sijapp cond.end #
    }

    public volatile boolean canReconnect = false;

    public synchronized boolean reconnect(JimmException e) {
        int errCode = e.getErrCode();
        if (e.isCritical()) {
            if ((reconnect_attempts-- > 0) && (Options.getBoolean(Options.OPTION_RECONNECT)) && (canReconnect) &&
                    (errCode < 110 || errCode > 117) && (errCode != 127) && (errCode != 119)) {

                disconnect(false);
                if (profile.isCurrent()) {
                    Jimm.getContactList().beforeConnect();
                }
                nextSrvHost();
                connect(false);
                return true;
            } else {
                //#sijapp cond.if modules_DEBUGLOG is "true"#
                jimm.DebugLog.addText("Reconnection failed.\nReconnect attempts: " + reconnect_attempts +
                        "\nCan to reconnect: " + canReconnect + "\nError code: " + errCode +
                        "\nOption: " + Options.getBoolean(Options.OPTION_RECONNECT)
                );
                //#sijapp cond.end#
            }
//#sijapp cond.if modules_FILES is "true"#
            if (!e.isPeer()) {
                disconnect();
            } else {
                resetPeerCon();
            }
//#sijapp cond.else#
            disconnect();
//#sijapp cond.end#
            //Jimm.setStatusesToDraw(JimmUI.getStatusImageIndex(ContactItem.STATUS_OFFLINE), null);
        }

        JimmException.handleException(e);
        return false;
    }

    /**************************************************************************/
    /**************************************************************************/
    /**
     * **********************************************************************
     */


    public int getCurrentStatus() {
        return isConnected() ? getProfile().getInt(Profile.OPTION_ONLINE_STATUS) : ContactItem.STATUS_OFFLINE;
    }

    public Icon getCurrentXStatus() {
        return XStatus.getStatusImage(getProfile().getInt(Profile.OPTION_XSTATUS));
    }

    public void setOnlineStatus(int status) throws JimmException {
        // Convert online status
        int onlineStatus = Util.translateStatusSend(status);
        boolean qipStatus = Util.qipStatus(status);

        int visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
        byte[] buf = new byte[15];
        //byte bCode = 0;
        if (visibilityItemId != 0) {
            if (onlineStatus == Util.SET_STATUS_INVISIBLE)
                setPrivateStatus((status == ContactItem.STATUS_INVIS_ALL) ? (byte) 2 : (byte) 3);
        }

        // Send a CLI_SETSTATUS packet
        if (!qipStatus) {
            sendPacket(OtherAction.getStatusPacket(status, this));
        }

        // Change privacy setting according to new status
        if (visibilityItemId != 0 && onlineStatus != Util.SET_STATUS_INVISIBLE) {
            SnacPacket reply2post = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
                    SnacPacket.CLI_ROSTERUPDATE_COMMAND,
                    SnacPacket.CLI_ROSTERUPDATE_COMMAND,
                    new byte[0],
                    buf);
            sendPacket(reply2post);
        }

        // Save new online status
        getProfile().setInt(Profile.OPTION_ONLINE_STATUS, status);
        getProfile().saveOptions();
        //if (qipStatusUpd) {
        sendPacket(OtherAction.getStandartUserInfoPacket(this));
        //}
        // reset autoAnswered flag for all contacts
        profile.resetAutoAnsweredFlag();

        lastStatusChangeTime = DateAndTime.getDateString(true, false);
    }

    //PSTATUS_ALL = 0x01
    //PSTATUS_VISIBLE_ONLY = 0x03
    //PSTATUS_NOT_INVISIBLE = 0x04
    //PSTATUS_CL_ONLY = 0x05
    //PSTATUS_NONE = 0x02

    private static final int[] psToNormal = {0, 1, 3, 4, 5, 2};

    public int transformVisId(int i) {
        if (i < 0 || i >= psToNormal.length) {
            return 1;
        }
        return psToNormal[i];

        //switch (i) {
        //    case 2: case 3: case 4: return (i + 1);
        //    case 5: return 2;
        //}
        // return i;
    }

    private int returnTransformVisId(int ps) {
        for (int i = psToNormal.length - 1; i >= 0; i--) {
            if (psToNormal[i] == ps) {
                return i;
            }
        }
        return 1;

//         switch (i){
//             case 3: case 4: case 5: return (i - 1);
//             case 2: return 5;
//         }
//         return i;
    }

    public void setPrivateStatus(byte status) throws JimmException {
        setPrivateStatus(status, false);
    }

    public void setPrivateStatus(byte status, boolean needReturn) throws JimmException {
        int visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
        byte[] buf = new byte[15];
        int marker = 0;
        if (isConnected()) {
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
            Util.putByte(buf, marker, status);             // TLV Value

            SnacPacket reply2pre = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
                    SnacPacket.CLI_ROSTERUPDATE_COMMAND,
                    0,//SnacPacket.CLI_ROSTERUPDATE_COMMAND,
                    new byte[0],
                    buf);
            sendPacket(reply2pre);
        }
        getProfile().setInt(Profile.OPTION_PSTATUS, (needReturn) ? returnTransformVisId((int) status) : (int) status);
        getProfile().saveOptions();
    }

    public void setXStatus(int xstIdx, String title, String desc) {
        getProfile().setString(Profile.OPTION_XTRAZ_TITLE, title);
        getProfile().setString(Profile.OPTION_XTRAZ_MESSAGE, desc);
        getProfile().setInt(Profile.OPTION_XSTATUS, xstIdx);
        getProfile().saveOptions();
        if (isConnected()) {
            try {
                if (getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
                    sendPacket(OtherAction.getStandartUserInfoPacket(this));
                }
                setMood(XStatus.toMood(xstIdx), title + ' ' + desc);
            } catch (JimmException e) {
                JimmException.handleException(e);
            }
        }
    }

    public void setMood(int moodIndex, String moodText) throws JimmException {
        //int moodIdx = XStatus.mergeMoodXs(moodIndex);
        int moodIdx = moodIndex;
        String mood = (moodIdx < 0) ? "" : ("icqmood" + moodIdx);
        moodText = (moodText.length() < 250) ? moodText : (moodText.substring(0, 250 - 5) + "[...]");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /* xStatus */
        byte[] moodArr = Util.stringToByteArray(mood, true);
        byte[] moodTextArr = Util.stringToByteArray(moodText, true);
        int tlvLen = (2 + 1 + 1 + moodArr.length) + (2 + 1 + 1 + 2 + moodTextArr.length + 2);
        // TLV (0x1D)
        Util.writeWord(baos, 0x1D, true);
        Util.writeWord(baos, tlvLen, true);
        // subTLV 0x0002 (icq mood)
        Util.writeWord(baos, 0x0002, true);
        Util.writeByte(baos, 0x04);
        Util.writeByte(baos, 2 + moodTextArr.length + 2);
        Util.writeLenAndString(baos, moodText, true);
        Util.writeWord(baos, 0x0000, true);
        // subTLV 0x000E (icq mood)
        Util.writeWord(baos, 0x000E, true);
        Util.writeLenAndString(baos, mood, true);

        SnacPacket snacPkt = new SnacPacket(SnacPacket.SERVICE_FAMILY, SnacPacket.CLI_SETSTATUS_COMMAND, baos.toByteArray());
        sendPacket(snacPkt);
    }

    public void setMood() throws JimmException {
        String text = "";
        int xst = getProfile().getInt(Profile.OPTION_XSTATUS);
        if (xst == XStatus.XSTATUS_NONE) {
            xst = -1;
        }
        if (xst != -1) {
            text = getProfile().getString(Profile.OPTION_XTRAZ_TITLE) + ' ' + getProfile().getString(Profile.OPTION_XTRAZ_MESSAGE);
        }
        setMood(XStatus.toMood(xst), text);
        //setMood(XStatus.toMood(xst), text);
    }

    public boolean checkInvisLevel(String uin) {
        int pstatus = getProfile().getInt(Profile.OPTION_PSTATUS);
        int newpstatus = transformVisId(pstatus);
        //System.out.println(" " + pstatus + " " + newpstatus);
        if (newpstatus == 1) return true;
        if (newpstatus == 2) return false;
        ContactItem cItem = getProfile().getItemByUIN(uin);
        if (cItem == null) return false;
        if (newpstatus == 3)
            return (cItem.getVisibleId() != 0 && cItem.getInvisibleId() == 0 && cItem.getIgnoreId() == 0);
        if (newpstatus == 4) return (cItem.getInvisibleId() == 0 && cItem.getIgnoreId() == 0);
        return (!cItem.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) || cItem.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH));
    }

    public String getLastStatusChangeTime() {
        return lastStatusChangeTime;
    }

    ///////////////////
    //// Auto-status ////
    ///////////////////
    private int oldStatus;
    public static int delay = Options.getInt(Options.OPTION_STATUS_DELAY) * 60000;
    private static long lastKeyPressedTime = 0;
    private static long lastPlayTime = 0;
    private static byte statusChanged = 0;
    private byte stChanged = 0;

    private synchronized void autoStatus() {
        long systemTime = System.currentTimeMillis();
        boolean replay = Options.getBoolean(Options.OPTION_REPLAY_MESSAGE);
        if (replay & systemTime - lastKeyPressedTime > 300000 & systemTime - lastPlayTime > 150000 & Profiles.getUnreadMessCount() > 0) {
            Notify.playSoundNotification(Notify.SOUND_TYPE_MESSAGE, false);
            lastPlayTime = System.currentTimeMillis();
        }

        if (Options.getBoolean(Options.OPTION_STATUS_AUTO)) {
            if (systemTime - lastKeyPressedTime < (stChanged + 1) * delay || stChanged == 2) {
                return;
            }
            //System.out.println("Change status: " + stChanged);
            switch (getCurrentStatus()) {
                case ContactItem.STATUS_ONLINE:
                case ContactItem.STATUS_CHAT:
                case ContactItem.STATUS_DND:
                case ContactItem.STATUS_OCCUPIED:
                case ContactItem.STATUS_EVIL:
                case ContactItem.STATUS_DEPRESSION:
                case ContactItem.STATUS_WORK:
                case ContactItem.STATUS_HOME:
                case ContactItem.STATUS_LUNCH:
                    oldStatus = getCurrentStatus();
                    statusChange(ContactItem.STATUS_AWAY);
                    statusChanged++;
                    stChanged++;
                    break;
                case ContactItem.STATUS_AWAY:
                    if (stChanged == 1) {
                        statusChange(ContactItem.STATUS_NA);
                        stChanged++;
                    }
                    break;
            }
        }
//        if (Options.getInt(Options.OPTION_XSTATUS_AUTO) > 0) {
//            if (System.currentTimeMillis() - lastKeyPressedTime < (stChanged + 1) * delay || stChanged == 2) {
//				return;
//			}
//            int newXstatus =  Options.getInt(Options.OPTION_XSTATUS_AUTO);
//            xStatusChange(xStatusChangeTrans[newXstatus]);
//        }
    }

    private void statusChange(int status) {
        try {
            setOnlineStatus(status);
            Jimm.getContactList().updateTitle();
        } catch (JimmException e) {
            JimmException.handleException(e);
        }
    }

    public void restoreStatus() {
        if ((stChanged > 0) && (Options.getBoolean(Options.OPTION_STATUS_RESTORE))) {
            statusChange(oldStatus);
            stChanged = 0;
            statusChanged = (byte) Math.max(statusChanged - 1, 0);
        }
    }

//    private static int[] xStatusChangeTrans = {0, 11, 18, 8, 13, 21, 29};
//
//    private void xStatusChange(int xstatus) {
//        String title = XStatus.getStatusAsString(xstatus);
//        getProfile().setString(Profile.OPTION_XTRAZ_TITLE, title);
//		getProfile().setInt(Profile.OPTION_XSTATUS, xstatus);
//		getProfile().saveOptions();
//        Jimm.setStatusesToDraw( JimmUI.getStatusImageIndex(getProfile().getInt(Profile.OPTION_ONLINE_STATUS)),
//							   XStatus.getStatusImage(getProfile().getInt(Profile.OPTION_XSTATUS)) );
//		if (isConnected()) {
//			try {
//				if (getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
//					sendPacket(OtherAction.getStandartUserInfoPacket(this));
//				}
//			} catch (JimmException e) {
//
//			}
//		}
//	}

    public void sendProcessBuddy(int mode, String name, int id, int groupId, int buddyType) throws JimmException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Util.writeLenAndString(buffer, name, true);
        Util.writeWord(buffer, groupId, true);
        Util.writeWord(buffer, id, true);
        Util.writeWord(buffer, buddyType, true);
        Util.writeWord(buffer, 0, true);
        int command;
        switch (mode) {
            case 0:
                command = SnacPacket.CLI_ROSTERADD_COMMAND;
                break;

            case 1:
                command = SnacPacket.CLI_ROSTERDELETE_COMMAND;
                break;

            default:
                return;
        }
        SnacPacket packet = new SnacPacket(0x0013, command, Util.getCounter(), new byte[0], buffer.toByteArray());
        sendPacket(packet);
    }

    public static void keyPressed() {
        if ((Jimm.locked()) && (Jimm.getCurrentDisplay() instanceof SplashCanvas)) {
            return;
        }
        lastKeyPressedTime = System.currentTimeMillis();
        if (statusChanged > 0) {
            Profiles.restoreStatuses();
        }
    }
}
