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
 File: src/jimm/comm/SOCKETConnection.java
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
import jimm.comm.ToIcqSrvPacket;
import jimm.comm.Util;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SOCKETConnection extends Connection implements Runnable {
    // Connection variables
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    private SocketConnection sc;
    // #sijapp cond.else#
    private StreamConnection sc;
    // #sijapp cond.end#
    private InputStream is;
    private OutputStream os;

    // FLAP sequence number counter
    //private int nextSequence;

    // ICQ sequence number counter
    private int nextIcqSequence;

    public SOCKETConnection(Icq icq) {
        this.icq = icq;
    }

    // Opens a connection to the specified host and starts the receiver thread
    public void connect(String hostAndPort) throws JimmException {
        boolean ok = false;
        try {
            // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
            sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
            // #sijapp cond.else#
            sc = (StreamConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
            // #sijapp cond.end#
            String pr = Jimm.jimm.getAppProperty("Socket-keep-alive");
            if ((pr != null) && (pr.toLowerCase().equals("true"))) {
                sc.setSocketOption(SocketConnection.KEEPALIVE, 20);
            }
            is = sc.openInputStream();
            os = sc.openOutputStream();

            inputCloseFlag = false;
            seq();
            //nextSequence = (new Random()).nextInt() % 0x0FFF;
            nextIcqSequence = 2;

            synchronized (rcvdPackets) {
                rcvdPackets.removeAllElements();
            }
            rcvThread = new Thread(this);
            rcvThread.start();
            ok = true;
        } catch (ConnectionNotFoundException e) {
            throw (new JimmException(121, 0));
        } catch (IllegalArgumentException e) {
            throw (new JimmException(122, 0));
        } catch (IOException e) {
            throw (new JimmException(120, 20));
        } catch (SecurityException e) {
            throw (new JimmException(119, 0));
        } catch (Exception e) {
            throw new JimmException(120, 2);
        } finally {
            if (!ok) closeConnection();
        }
    }

    // Sets the reconnect flag and closes the connection
    protected void closeConnection() {
        try {
            if (sc != null) {
                sc.close();
            }
        } catch (Exception e) { /* Do nothing */
        }
        sc = null;
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) { /* Do nothing */
        }
        is = null;
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) { /* Do nothing */
        }
        os = null;
    }

    // Sends the specified packet
    public void sendPacket(Packet packet) throws JimmException {
        // Throw exception if output stream is not ready
        if (os == null) {
            JimmException e = new JimmException(123, 0);
            if (!icq.reconnect(e)) {
                return;
            }
        }

        // Request lock on output stream
        synchronized (os) {
            try {
                // Set sequence numbers
                packet.setSequence(getFlapSequence());
                if (packet instanceof ToIcqSrvPacket) {
                    ((ToIcqSrvPacket) packet).setIcqSequence(nextIcqSequence++);
                }

                // Send packet and count the bytes
                byte[] outpack = packet.toByteArray();
                os.write(outpack);
                os.flush();

                Traffic.addTrafficOut(outpack.length + 51); // 51 is the overhead for each packet
                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }
            } catch (IOException e) {
                close();
                JimmException ex = new JimmException(120, 3);
                icq.reconnect(ex);
            } catch (Exception e) {
                close();
                JimmException jex = new JimmException(100, 0);
                icq.reconnect(jex);
            }
        }
    }
// #sijapp cond.if modules_FILES is "true"#

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
// #sijapp cond.end#

    // Main loop

    public void run() {
        // Required variables
        byte[] flapHeader = new byte[6];
        byte[] flapData;

        // Try
        try {
            // Check abort condition
            while (!inputCloseFlag) {
                // Read flap header
                if (Options.getBoolean(Options.OPTION_CONN_PROP)) {
                    while ((is != null) && (is.available() == 0)) {
                        if (inputCloseFlag || rcvThread == null) {
                            break;
                        }
                        Thread.sleep(250);
                    }
//					if (is == null) break;
                }
                if (!read(is, flapHeader)) break;

                // Verify flap header
                if (Util.getByte(flapHeader, 0) != 0x2A) {
                    throw (new JimmException(124, 0));
                }

                // Allocate memory for flap data
                flapData = new byte[Util.getWord(flapHeader, 4)];
                if (!read(is, flapData)) break;

                Traffic.addTrafficIn(flapData.length + flapHeader.length + 57);
                // 46 is the overhead for each packet (6 byte flap header)
                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }

                addPacket(flapHeader, flapData);
                // Notify main loop
                icq.waitNotify();
            }
        } catch (NullPointerException e) { // Construct and handle exception (only if input close flag has not been set)
            if (!inputCloseFlag) {
                JimmException f = new JimmException(120, 3);
                JimmException.handleException(f);
            }
        } catch (InterruptedException e) { /* Do nothing */
        } catch (JimmException e) {
            icq.reconnect(e);
        } catch (IOException e) { // Construct and handle exception (only if input close flag has not been set)
            if (!inputCloseFlag) {
                JimmException f = new JimmException(120, 1);
                icq.reconnect(f);
            }
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
        }
        if (!inputCloseFlag) {
            close();
            JimmException f = new JimmException(120, 10);
            icq.reconnect(f);
        }
    }
}