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
 File: src/jimm/comm/HTTPConnection.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/
package jimm.comm.connection;

import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.Traffic;
import jimm.comm.DisconnectPacket;
import jimm.comm.Icq;
import jimm.comm.Packet;
import jimm.comm.Util;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

//#sijapp cond.if modules_HTTP is "true"#
public class HTTPConnection extends Connection implements Runnable {
    // Connection variables
    private HttpConnection hcm; // Connection for monitor URLs (receiving)
    private HttpConnection hcd; // Connection for data URLSs (sending)
    private InputStream ism;
    private OutputStream osd;

    // URL for the monitor thread
    private String monitorURL;

    // HTTP Connection sequence
    private int seq;

    // HTTP Connection session ID
    private String sid;

    // IP and port of HTTP Proxy Server to connect to
    private String proxy_host;
    private int proxy_port;

    // Counter for the connections to the http proxy server
    private int connSeq;

    public HTTPConnection(Icq icq) {
        this.icq = icq;
        seq = 0;
        connSeq = 0;
        monitorURL = "http://http.proxy.icq.com/hello";
    }

    // Opens a connection to the specified host and starts the receiver thread
    public synchronized void connect(String hostAndPort) throws JimmException {
        try {
            connSeq++;
            // If this is the first connection initialize the connection with the proxy
            if (connSeq == 1) {
                this.inputCloseFlag = false;
                this.rcvThread = new Thread(this);
                this.rcvThread.start();
                // Wait the the finished init will notify us
                this.wait();
            }

            // Extract host and port from combined String (we need port as int value)
            String icqserver_host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
            int icqserver_port = Integer.parseInt(hostAndPort.substring(hostAndPort.indexOf(":") + 1));
            // System.out.println("Connect via "+proxy_host+":"+proxy_port+" to: "+icqserver_host+" "+icqserver_port);
            // Send anser packet with connect to real server (via proxy)
            byte[] packet = new byte[icqserver_host.length() + 4];
            Util.putWord(packet, 0, icqserver_host.length());
            System.arraycopy(Util.stringToByteArray(icqserver_host), 0, packet, 2, icqserver_host.length());
            Util.putWord(packet, 2 + icqserver_host.length(), icqserver_port);

            this.sendPacket(null, packet, 0x003, connSeq);

            // If this was not the first connection to the ICQ server close the previous
            if (connSeq != 1) {
                DisconnectPacket reply = new DisconnectPacket();
                this.sendPacket(reply, null, 0x0005, connSeq - 1);
                this.sendPacket(null, new byte[0], 0x0006, connSeq - 1);
            }

        } catch (IllegalArgumentException e) {
            throw (new JimmException(127, 0));
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    // Sets the reconnect flag and closes the connection
    public synchronized void closeConnection() {
        try {
            this.ism.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            this.ism = null;
        }

        try {
            this.osd.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            this.osd = null;
        }

        try {
            this.hcm.close();
            this.hcd.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            this.hcm = null;
            this.hcd = null;
        }
    }

    /**
     * **************************************************************************
     * ****************************************************************************
     * <p/>
     * Sends and gets packets wraped in http requeste from ICQ http proxy server.
     * Packets to send and receive look like this:
     * <p/>
     * WORD	Size	Size of the upcoming packet
     * WORD	Version	Version of the ICQ Proxy Protocol (always 0x0443)
     * WORD	Type	Type of the upcoming packet must be one of these:
     * 0x0002	Reply on server hello
     * 0x0003	Loginrequest to ICQ server
     * 0x0004	Reply to login
     * 0x0005  FLAP packet
     * 0x0006  Close connection
     * 0x0007	Close connection reply
     * DWORD	Unkn	0x00000000
     * WORD	Unkn	0x0000
     * WORD	ConnSq	Number of connection the packet is for
     * ...		Data	Data of the packet (Size - 14 bytes)
     * <p/>
     * ****************************************************************************
     * ***************************************************************************
     */
    // Sends the specific packet (with the possibility of setting the packet type
    public void sendPacket(Packet packet, byte[] rawData, int type, int connCount) throws JimmException {
        // Set the connection parameters
        try {
            this.hcd = (HttpConnection) Connector.open("http://" + proxy_host + ":" + proxy_port + "/data?sid=" + sid + "&seq=" + seq, Connector.READ_WRITE);
            this.hcd.setRequestProperty("User-Agent", Options.getString(Options.OPTION_HTTP_USER_AGENT));
            this.hcd.setRequestProperty("x-wap-profile", Options.getString(Options.OPTION_HTTP_WAP_PROFILE));
            this.hcd.setRequestProperty("Cache-Control", "no-store no-cache");
            this.hcd.setRequestProperty("Pragma", "no-cache");
            this.hcd.setRequestMethod(HttpConnection.POST);
            this.osd = this.hcd.openOutputStream();
        } catch (IOException e) {
            this.close();
        }

        // Throw exception if output stream is not ready
        if (this.osd == null) {
            throw (new JimmException(128, 0, true));
        }

        // Request lock on output stream
        synchronized (this.osd) {
            // Send packet and count the bytes
            try {
                byte[] outpack;
                // Add http header (it has 14 bytes)
                if (rawData == null) {
                    rawData = packet.toByteArray();
                    outpack = new byte[14 + rawData.length];
                }

                outpack = new byte[14 + rawData.length];
                Util.putWord(outpack, 0, rawData.length + 12); // Length
                Util.putWord(outpack, 2, 0x0443); // Version
                Util.putWord(outpack, 4, type);
                Util.putDWord(outpack, 6, 0x00000000); // Unknown
                Util.putDWord(outpack, 10, connCount);
                // The "real" data
                System.arraycopy(rawData, 0, outpack, 14, rawData.length);
                // System.out.println("Sent: "+outpack.length+" init");
                this.osd.write(outpack);
                // this.osd.flush();

                // Send the data
                if (hcd.getResponseCode() != HttpConnection.HTTP_OK)
                    this.close();
                else
                    seq++;

                try {
                    this.osd.close();
                    this.hcd.close();
                } catch (Exception e) {
                    // Do nothing
                } finally {
                    this.osd = null;
                    this.hcd = null;
                }

                // 40 is the overhead for each packet (TCP/IP)
                // 190 is the ca. overhead for the HTTP header
                // 14 bytes is the overhead for ICQ HTTP data header
                // 170 bytes is the ca. overhead of the HTTP/1.1 200 OK
                Traffic.addTrafficOut(outpack.length + 40 + 190 + 14 + 170);
                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }
                // System.out.println(" ");
            } catch (IOException e) {
                this.close();
            }
        }
    }

    // Sends the specified packet always type 5 (FLAP packet)
    public void sendPacket(Packet packet) throws JimmException {
        this.sendPacket(packet, null, 0x0005, connSeq);
    }

    // Main loop
    public void run() {
        // Required variables
        byte[] length = new byte[2];
        byte[] httpPacket;
        byte[] packet = new byte[0];
        int flapMarker = 0;

        int bRead, bReadSum;
        int bReadSumRequest = 0;

        // Reset packet buffer
        synchronized (this) {
            this.rcvdPackets = new Vector();
        }

        // Try
        try {
            // Check abort condition
            while (!this.inputCloseFlag) {
                // Set connection parameters
                this.hcm = (HttpConnection) Connector.open(monitorURL, Connector.READ_WRITE);
                this.hcm.setRequestProperty("User-Agent", Options.getString(Options.OPTION_HTTP_USER_AGENT));
                this.hcm.setRequestProperty("x-wap-profile", Options.getString(Options.OPTION_HTTP_WAP_PROFILE));
                this.hcm.setRequestProperty("Cache-Control", "no-store no-cache");
                this.hcm.setRequestProperty("Pragma", "no-cache");
                this.hcm.setRequestMethod(HttpConnection.GET);
                this.ism = this.hcm.openInputStream();
                if (hcm.getResponseCode() != HttpConnection.HTTP_OK) throw new IOException();
                // Read flap header
                bReadSumRequest = 0;

                do {
                    bReadSum = 0;
                    // Read HTTP packet length information
                    do {
                        bRead = ism.read(length, bReadSum, length.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                        bReadSumRequest += bRead;
                    } while (bReadSum < length.length);
                    if (bRead == -1) break;
                    // Allocate memory for packet data
                    httpPacket = new byte[Util.getWord(length, 0)];
                    bReadSum = 0;

                    // Read HTTP packet data
                    do {
                        bRead = ism.read(httpPacket, bReadSum, httpPacket.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                        bReadSumRequest += bRead;
                    } while (bReadSum < httpPacket.length);
                    if (bRead == -1) break;

                    // Only process type 5 (flap) packets
                    if (Util.getWord(httpPacket, 2) == 0x0005) {
                        // Packet has 12 bytes header and could contain more than one FLAP
                        int contBytes = 12;
                        while (contBytes < httpPacket.length) {

                            // Verify flap header only if we are sure there is a start
                            if (flapMarker == 0) {
                                if (Util.getByte(httpPacket, contBytes) != 0x2A) {
                                    throw (new JimmException(124, 0));
                                }
                                // Copy flap packet data from http packet
                                packet = new byte[Util.getWord(httpPacket, contBytes + 4) + 6];
                            }
                            // Read packet data form httpPacket to packet
                            // Packet contains the end of the flap packet
                            if (httpPacket.length - contBytes >= (packet.length - flapMarker)) {
                                System.arraycopy(httpPacket, contBytes, packet, flapMarker, (packet.length - flapMarker));
                                contBytes += (packet.length - flapMarker);
                                flapMarker = packet.length;
                            }
                            // Packet does not contain the end of the flap packet
                            else {
                                System.arraycopy(httpPacket, contBytes, packet, flapMarker, httpPacket.length - contBytes);
                                flapMarker += (httpPacket.length - contBytes);
                                contBytes += httpPacket.length - contBytes;
                            }
                            // If all the bytes from a flap packet have been read add that packet to the queue
                            if (flapMarker == packet.length) {
                                // Lock object and add rcvd packet to vector
                                synchronized (this.rcvdPackets) {
                                    this.rcvdPackets.addElement(packet);
                                }
                                flapMarker = 0;
                            }
                        }

                        icq.waitNotify();
                    } else if (Util.getWord(httpPacket, 2) == 0x0007) {
                        // Construct and handle exception if we get a close rep for the connection we are
                        // currently using
                        if (Util.getWord(httpPacket, 10) == connSeq)
                            throw new JimmException(221, 0); // сервер закрывает?
                    } else if (Util.getWord(httpPacket, 2) == 0x0002) {
                        synchronized (this) {
                            // Init answer from proxy set sid and proxy_host and proxy_port
                            byte[] temp = new byte[16];
                            System.arraycopy(httpPacket, 10, temp, 0, 16);
                            sid = Util.byteArrayToHexString(temp);
                            // Get IP of proxy
                            byte[] ip = new byte[Util.getWord(httpPacket, 26)];
                            System.arraycopy(httpPacket, 28, ip, 0, ip.length);
                            this.proxy_host = Util.byteArrayToString(ip);

                            // Get port for proxy
                            this.proxy_port = Util.getWord(httpPacket, 28 + ip.length);

                            // Set monitor URL to non init value
                            monitorURL = "http://" + proxy_host + ":" + proxy_port + "/monitor?sid=" + sid;

                            this.notify();
                        }
                    }
                } while (bReadSumRequest < hcm.getLength());


                // This is not accurate for http connection
                // 42 is the overhead for each packet (2 byte packet length) (TCP IP)
                // 185 is the overhead for each monitor packet HTTP HEADER
                // 175 is the overhead for each HTTP/1.1 200 OK answer header
                // ICQ HTTP data header is counted in bReadSum
                Traffic.addTrafficIn(bReadSumRequest + 42 + 185 + 175);

                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }


                try {
                    this.ism.close();
                    this.hcm.close();
                } catch (Exception e) {
                    // Do nothing
                } finally {
                    this.ism = null;
                    this.hcm = null;
                }
            }
        }
        // Catch communication exception
        catch (NullPointerException e) {
            if (!this.inputCloseFlag) {
                // Construct and handle exception
                JimmException f = new JimmException(125, 3);
                JimmException.handleException(f);
            } else { /* Do nothing */
            }
        }
        // Catch JimmException
        catch (JimmException e) {

            // Handle exception
            JimmException.handleException(e);

        }
        // Catch IO exception
        catch (IOException e) {
            if (!this.inputCloseFlag) {
                // Construct and handle exception
                JimmException f = new JimmException(125, 1);
                JimmException.handleException(f);
            } else { /* Do nothing */
            }
        }
    }
}
//#sijapp cond.end#