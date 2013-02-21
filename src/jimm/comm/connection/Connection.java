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
 File: src/jimm/comm/Connection.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/
package jimm.comm.connection;

import jimm.JimmException;
import jimm.comm.Icq;
import jimm.comm.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Vector;

public abstract class Connection implements Runnable {
    // Disconnect flags
    protected volatile boolean inputCloseFlag;

    // Receiver thread
    protected volatile Thread rcvThread;

    protected Icq icq;

    // FLAP sequence number
    private int flapSEQ;
    //private int currSeq = 0;
    // private static final int seqVals0[] = {	5695, 23595, 23620, 23049, 0x2886, 0x2493, 23620, 23049, 2853, 17372, 1255,
    // 1796, 1657, 13606, 1930, 23918, 31234, 30120, 0x1BEA, 0x5342, 0x30CC,
    // 0x2294, 0x5697, 0x25FA, 0x3303, 0x078A, 0x0FC5, 0x25D6,
    // 0x26EE, 0x7570, 0x7F33, 0x4E94, 0x07C9, 0x7339, 0x42A8
    // };

    // Received packets
    protected Vector rcvdPackets = new Vector();

    // Opens a connection to the specified host and starts the receiver thread
    public abstract void connect(String hostAndPort) throws JimmException;

    protected abstract void closeConnection();

    // Sets the reconnect flag and closes the connection
    public void close() {
        inputCloseFlag = true;
        closeConnection();
        //Thread.yield();
        icq.waitNotify();
        rcvThread = null;
    }

    // Returns the number of packets available
    public int available() {
        synchronized (rcvdPackets) {
            return rcvdPackets.size();
        }
    }

    public boolean isConnected() {
        return !inputCloseFlag;
    }

    protected void seq() {
        flapSEQ = generateFlapSEQ();
        flapSEQ--;

        //flapSEQ = seqVals0[currSeq];
        //currSeq = ++currSeq % seqVals0.length;
    }

    private static char generateFlapSEQ() {
        Random rand = new Random();

        // new method by Joe Kucera
        long n, s = 0, i;
        do {
            n = rand.nextLong();
        } while (n <= 0);
        for (i = n; (i >>= 3) != 0; s += i) ;
        return (char) ((((0 - s) ^ (n & 0xFF)) & 7 ^ n) + 2);
    }

    // Returns and updates sequence nr
    protected int getFlapSequence() {
        return (flapSEQ++ % 0x8000);
    }

    protected void addPacket(byte[] flapHeader, byte[] flapData) {
        // Merge flap header and data and count the data
        byte[] rcvdPacket = new byte[flapHeader.length + flapData.length];
        System.arraycopy(flapHeader, 0, rcvdPacket, 0, flapHeader.length);
        System.arraycopy(flapData, 0, rcvdPacket, flapHeader.length, flapData.length);
        addPacket(rcvdPacket);
    }

    protected void addPacket(byte[] packet) {
        // Lock object and add rcvd packet to vector
        synchronized (rcvdPackets) {
            rcvdPackets.addElement(packet);
        }
    }

    // Returns the next packet, or null if no packet is available
    public Packet getPacket() throws JimmException {
        // Request lock on packet buffer and get next packet, if available
        byte[] packet;
        synchronized (this.rcvdPackets) {
            if (this.rcvdPackets.size() == 0) {
                return null;
            }
            packet = (byte[]) this.rcvdPackets.elementAt(0);
            this.rcvdPackets.removeElementAt(0);
        }
        // Parse and return packet
        return (Packet.parse(packet));
    }

    // todo IO error if read()
    protected boolean read(InputStream is, byte[] data) throws IOException {
        if (is == null) return false;
        int bReadSum = 0;
        do {
            int bRead = is.read(data, bReadSum, data.length - bReadSum);
            if (bRead == -1) return false;
            bReadSum += bRead;
        } while (bReadSum < data.length);
        return true;
    }

    // Sends the specified packet always type 5 (FLAP packet)
    public void sendPacket(Packet packet) throws JimmException {
    }

    // #sijapp cond.if modules_FILES is "true"#
    // Retun the port this connection is running on
    public int getLocalPort() {
        return (0);
    }

    // Retun the ip this connection is running on
    public byte[] getLocalIP() {
        return (new byte[4]);
    }
    // #sijapp cond.end#

    // Main loop

    public void run() {
    }
}