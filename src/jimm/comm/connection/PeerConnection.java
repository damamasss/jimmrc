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
 File: src/jimm/comm/PeerConnection.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/
package jimm.comm.connection;

import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.Traffic;
import jimm.comm.Icq;
import jimm.comm.Packet;
import jimm.comm.Util;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

// #sijapp cond.if modules_FILES is "true"#
public class PeerConnection implements Runnable {
    // Connection variables
    private SocketConnection sc;
    private InputStream is;
    private OutputStream os;

    private Icq icq;

    // Disconnect flags
    private volatile boolean inputCloseFlag;

    // Receiver thread
    private volatile Thread rcvThread;

    // Received packets
    private Vector rcvdPackets;

    public PeerConnection(Icq icq) {
        this.icq = icq;
    }

    // Opens a connection to the specified host and starts the receiver
    // thread
    public void connect(String hostAndPort) throws JimmException {
        try {
            //#sijapp cond.if modules_DEBUGLOG is "true" #
//			jimm.DebugLog.addText("PeerConn: "+hostAndPort);
            //#sijapp cond.end #

            //try {
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            jimm.DebugLog.addText("PeerConnecting...");
            //#sijapp cond.end #
            this.sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE, true);
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            jimm.DebugLog.addText("PeerConnected");
            //#sijapp cond.end #
            //} catch (Exception e) {
            //	e.printStackTrace();
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            //	jimm.DebugLog.addText("PeerConnEr: "+e.getMessage());
            //#sijapp cond.end #
            //}
            //try {
            this.is = this.sc.openInputStream();
            //} catch (Exception e) {
            //	e.printStackTrace();
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            //	jimm.DebugLog.addText("OpenInputEr: "+e.getMessage());
            //#sijapp cond.end #
            //}
            //try {
            this.os = this.sc.openOutputStream();
            //} catch (Exception e) {
            //	e.printStackTrace();
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            //	jimm.DebugLog.addText("OpenOutoutEr: "+e.getMessage());
            //#sijapp cond.end #
            //}
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            jimm.DebugLog.addText("Next: \"run\"");
            //#sijapp cond.end #

            this.inputCloseFlag = false;

            this.rcvThread = new Thread(this);
            this.rcvThread.start();

        } catch (ConnectionNotFoundException e) {
            throw (new JimmException(126, 0, true, true));
        } catch (IllegalArgumentException e) {
            throw (new JimmException(127, 0, true, true));
        } catch (IOException e) {
            throw (new JimmException(125, 0, true, true));
        } catch (Exception e) {
            throw (new JimmException(173, 0, true, true));
        }
    }

    // Sets the reconnect flag and closes the connection
    public synchronized void close() {
        this.inputCloseFlag = true;

        try {
            this.is.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            this.is = null;
        }

        try {
            this.os.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            this.os = null;
        }

        try {
            this.sc.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            this.sc = null;
        }

        Thread.yield();
    }

    // Returns the number of packets available
    public synchronized int available() {
        if (this.rcvdPackets == null) {
            return (0);
        } else {
            return (this.rcvdPackets.size());
        }
    }

    // Returns the next packet, or null if no packet is available
    public Packet getPacket() throws JimmException {
        // Request lock on packet buffer and get next packet, if available
        byte[] packet;
        synchronized (this.rcvdPackets) {
            if (this.rcvdPackets.size() == 0) {
                return (null);
            }
            packet = (byte[]) this.rcvdPackets.elementAt(0);
            this.rcvdPackets.removeElementAt(0);
        }

        // Parse and return packet
        return (Packet.parse(packet));
    }

    // Sends the specified packet
    public void sendPacket(Packet packet) throws JimmException {
        // Throw exception if output stream is not ready
        if (this.os == null) {
            throw (new JimmException(128, 0, true, true));
        }

        // Request lock on output stream
        synchronized (this.os) {

            // Send packet and count the bytes
            try {
                byte[] outpack = packet.toByteArray();
                this.os.write(outpack);
                this.os.flush();
                // System.out.println("Peer packet sent length: "+outpack.length);
                //#sijapp cond.if modules_DEBUGLOG is "true" #
//				jimm.DebugLog.addText("Peer packet sent length: "+outpack.length);
                //#sijapp cond.end #


                // 51 is the overhead for each packet
                Traffic.addTrafficOut(outpack.length + 51);
                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }

            } catch (IOException e) {
                this.close();
            }
        }
    }

    // Retun the port this connection is running on
    public int getLocalPort() {
        try {
            return (this.sc.getLocalPort());
        } catch (IOException e) {
            return (0);
        }
    }

    // Retun the ip this connection is running on
    public byte[] getLocalIP() {
        try {
            return (Util.ipToByteArray(this.sc.getLocalAddress()));
        } catch (IOException e) {
            return (new byte[4]);
        }
    }

    // Main loop
    public void run() {
        //#sijapp cond.if modules_DEBUGLOG is "true" #
        jimm.DebugLog.addText("\"run\" started");
        //#sijapp cond.end #
        // Required variables
        byte[] dcLength = new byte[2];
        byte[] rcvdPacket;
        int bRead = -1, bReadSum;
        boolean needBreak = false;

        // Reset packet buffer
        //synchronized (this.rcvdPackets)
        //{
        this.rcvdPackets = new Vector();
        //}

        // Try
        try {
            // Check abort condition
            while (!this.inputCloseFlag) {

                // Read flap header
                bReadSum = 0;
                if (Options.getBoolean(Options.OPTION_CONN_PROP)) {
                    try {
                        while ((is != null) && (is.available() == 0)) {
                            Thread.sleep(250);
                        }
                    } catch (Exception ignored) {
                    }
                }
                do {
                    try {
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("Doing: bRead; "+"breadSum: "+bReadSum);
                        //#sijapp cond.end #
                        bRead = this.is.read(dcLength, bReadSum, dcLength.length - bReadSum);
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("Done: bRead; "+"breadSum: "+bReadSum);
                        //#sijapp cond.end #
                    } catch (Exception e) {
                        e.printStackTrace();
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("bRead1: "+e.getMessage());
                        //#sijapp cond.end #
                        needBreak = true;
                        break;
                    }
                    if (bRead == -1) break;
                    bReadSum += bRead;
                } while (bReadSum < dcLength.length);
                if (bRead == -1) break;
                if (needBreak) break;

                // Allocate memory for flap data
                rcvdPacket = new byte[Util.getWord(dcLength, 0, false)];

                // Read flap data
                bReadSum = 0;
                do {
                    try {
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("Doing: bRead2; "+"breadSum2: "+bReadSum);
                        //#sijapp cond.end #
                        bRead = this.is.read(rcvdPacket, bReadSum, rcvdPacket.length - bReadSum);
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("Done: bRead2; "+"breadSum2: "+bReadSum);
                        //#sijapp cond.end #
                    } catch (Exception e) {
                        e.printStackTrace();
                        //#sijapp cond.if modules_DEBUGLOG is "true" #
//						jimm.DebugLog.addText("bRead2: "+e.getMessage());
                        //#sijapp cond.end #
                        needBreak = true;
                        break;
                    }
                    if (bRead == -1) break;
                    bReadSum += bRead;
                } while (bReadSum < rcvdPacket.length);
                if (bRead == -1) break;
                if (needBreak) break;


                Traffic.addTrafficIn(bReadSum + 53);

                // 42 is the overhead for each packet (2 byte packet length)
                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }


                // Lock object and add rcvd packet to vector
                synchronized (this.rcvdPackets) {
                    this.rcvdPackets.addElement(rcvdPacket);
                }
                icq.waitNotify();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            //#sijapp cond.if modules_DEBUGLOG is "true" #
//			jimm.DebugLog.addText("NPE: "+e.getMessage());
            //#sijapp cond.end #
            if (!this.inputCloseFlag) {
                // Construct and handle exception
                JimmException f = new JimmException(125, 3, true, true);
                JimmException.handleException(f);
            } else { /* Do nothing */
            }
        } catch (Exception e) {
            e.printStackTrace();
            //#sijapp cond.if modules_DEBUGLOG is "true" #
            //#sijapp cond.end #
//			jimm.DebugLog.addText("???\n"+e.getMessage());
        }
    }
}
// #sijapp cond.end#