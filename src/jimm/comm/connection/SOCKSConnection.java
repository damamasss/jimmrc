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
 File: src/jimm/comm/SOCKSConnection.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Manuel Linsmayer, Andreas Rossbacher, tamerlan311
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
import java.util.Vector;

// #sijapp cond.if modules_PROXY is "true"#
public class SOCKSConnection extends Connection implements Runnable {

    private final byte[] SOCKS5_HELLO =
            {(byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x02}; // version 05: 1) noauth 2) login/passwod

    // Connection variables
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    private SocketConnection sc;
    // #sijapp cond.else#
    private StreamConnection sc;
    // #sijapp cond.end#
    private InputStream is;
    private OutputStream os;

    private boolean is_connected = false;

    // FLAP sequence number counter
    //private int nextSequence;

    // ICQ sequence number counter
    private int nextIcqSequence;

    public SOCKSConnection(Icq icq) {
        this.icq = icq;
    }

    // Tries to resolve given host IP
    private synchronized String ResolveIP(String host, String port) {
        if (Util.isIP(host)) return host;
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        SocketConnection c;

        try {
            c = (SocketConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE);
            String ip = c.getAddress();

            try {
                c.close();
            } catch (Exception e) { /* Do nothing */
            } finally {
                c = null;
            }

            return ip;
        }
        catch (Exception e) {
            return "0.0.0.0";
        }
        // #sijapp cond.else#
        return "0.0.0.0";
        // #sijapp cond.end#
    }


    // Build socks CONNECT request
    private byte[] socks_connect_request(byte sver, String host, String port) {
        byte[] dest;
        int len;
        if (sver == 5) {
            len = 7 + host.length();
            dest = Util.stringToByteArray(host);
        } else {
            len = 9;
            String ip = ResolveIP(host, port);
            dest = Util.ipToByteArray(ip);
        }

        byte[] buf = new byte[len];
        buf[0] = sver; // Version of Socks Protocol
        buf[1] = 0x01; // CMD CONNECT

        if (sver == 5) {
            buf[2] = 0x00; // Reserved
            buf[3] = 0x03; // DomenName
            Util.putByte(buf, 4, dest.length); // lenght of domain
            System.arraycopy(dest, 0, buf, 5, dest.length); //copy domain name
            Util.putWord(buf, 5 + dest.length, Integer.parseInt(port)); // set port
        } else {
            Util.putWord(buf, 2, Integer.parseInt(port));
            System.arraycopy(dest, 0, buf, 4, dest.length); //copy ip
            buf[8] = 0x00; // end of packet
        }
        return buf;
    }

    // Build socks5 AUTHORIZE request
    private byte[] socks5_authorize_request(String login, String pass) {
        byte[] buf = new byte[3 + login.length() + pass.length()];

        Util.putByte(buf, 0, 0x01);
        Util.putByte(buf, 1, login.length());
        Util.putByte(buf, login.length() + 2, pass.length());
        byte[] blogin = Util.stringToByteArray(login);
        byte[] bpass = Util.stringToByteArray(pass);
        System.arraycopy(blogin, 0, buf, 2, blogin.length);
        System.arraycopy(bpass, 0, buf, blogin.length + 3, bpass.length);

        return buf;
    }

    // Opens a connection to the specified host and starts the receiver
    // thread
    public void connect(String hostAndPort) throws JimmException {
        int mode = Options.getInt(Options.OPTION_PRX_TYPE);
        is_connected = false;
        String host = "";
        String port = "";

        if (mode != 0) {
            int sep = 0;
            for (int i = 0; i < hostAndPort.length(); i++) {
                if (hostAndPort.charAt(i) == ':') {
                    sep = i;
                    break;
                }
            }
            // Get Host and Port
            host = hostAndPort.substring(0, sep);
            port = hostAndPort.substring(sep + 1);
        }
        try {
            switch (mode) {
                case 0:
                    connect_socks((byte) 4, host, port);
                    break;
                case 1:
                    connect_socks((byte) 5, host, port);
                    break;
                case 2:
                    // Try better first
                    try {
                        connect_socks((byte) 5, host, port);
                    } catch (Exception e) {
                        // Do nothing
                    }
                    // If not succeeded, then try socks4
                    if (!is_connected) {
                        stream_close();
                        try {
                            // Wait the given time
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // Do nothing
                        }
                        connect_socks((byte) 4, host, port);
                    }
                    break;
            }

            inputCloseFlag = false;
            rcvThread = new Thread(this);
            rcvThread.start();
            //nextSequence = (new Random()).nextInt() % 0x0FFF;
            seq();
            nextIcqSequence = 2;
        } catch (JimmException e) {
            throw (e);
        }
    }

    // Attempts to connect through socks4 or socks5
    private synchronized void connect_socks(byte sver, String host, String port) throws JimmException {
        String proxy_host = Options.getString(Options.OPTION_PRX_SERV);
        String proxy_port = Options.getString(Options.OPTION_PRX_PORT);
        String proxy_login = Options.getString(Options.OPTION_PRX_NAME);
        String proxy_pass = Options.getString(Options.OPTION_PRX_PASS);
        int i = 0;
        int ver = 0;
        int meth = 0;
        byte[] buf;

        try {
            // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
            sc = (SocketConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
            // #sijapp cond.else#
            sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
            // #sijapp cond.end#
            is = sc.openInputStream();
            os = sc.openOutputStream();

            // If version 5 - need to authorisation
            if (sver == 5) {
                os.write(SOCKS5_HELLO);
                os.flush();

                // Wait for responce
                while (is.available() == 0 && i < 50) try {
                    // Wait the given time
                    i++;
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Do nothing
                }
                if (is.available() == 0) {
                    throw (new JimmException(118, 226));
                }

                // Read reply
                ver = is.read();
                meth = is.read();

                //System.out.println("Hello Response ver:"+ver+" ans:"+meth);
                // Plain text authorisation
                if (ver == 0x05 && meth == 0x02) {
                    os.write(socks5_authorize_request(proxy_login, proxy_pass));
                    os.flush();

                    // Wait for responce
                    while (is.available() == 0 && i < 50) try {
                        // Wait the given time
                        i++;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Do nothing
                    }
                    if (is.available() == 0) {
                        throw (new JimmException(118, 227));
                    }

                    // Read reply
                    ver = is.read();
                    meth = is.read();

                    //System.out.println("Auth ansver ver:"+ver+" ans:"+meth);
                    if (meth != 0) // if ansver not NULL then authorisation Faled
                        throw (new JimmException(227, meth));
                }
                // if not Proxy without authorisation
                // Something bad happened :'(
                else if (ver != 0x05 || meth != 0x00)
                    throw (new JimmException(226, 0));
            }

            os.write(socks_connect_request(sver, host, port));
            os.flush();

            // Wait for responce
            while (is.available() == 0 && i < 50) try {
                // Wait the given time
                i++;
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                // Do nothing
            }
            if (is.available() == 0) {
                throw (new JimmException(118, 226));
            }

            // process response from server////
            ver = is.read();    // version
            meth = is.read();    // Ansver CODE

            //System.out.println("Last Ansver ver:"+ver+" ans:"+meth);
            if (ver == 0x05)        // if Socks5
                switch (meth) {
                    case 0x00:      // ALL OK
                        meth = is.read();//is.skip(1);
                        meth = is.read();
                        if (meth == 1) {
                            //is.skip(6); on Motorola ROKR E2 this metod skip() fail - skiped
                            buf = new byte[6];
                            is.read(buf);
                            // end is.skip(6);
                        } else {
                            int len = is.read();
                            //is.skip(len+2);
                            buf = new byte[len + 2];
                            is.read(buf);
                            //end is.skip(len+2);
                        }
                        break;
                    default:    //Error
                        throw (new JimmException(226, meth));
                }
            else    // if Socks4
                switch (meth) {
                    case 0x5A:    // Granted
                        //is.skip(6); on Motorola ROKR E2 this metod skip() fail - skiped
                        buf = new byte[6];
                        is.read(buf);
                        // end is.skip(6)
                        break;
                    default:    // Error conect to Proxy
                        throw (new JimmException(226, meth));
                }
            is_connected = true;
        } catch (ConnectionNotFoundException e) {
            throw (new JimmException(121, 226));
        } catch (IllegalArgumentException e) {
            throw (new JimmException(122, 226));
        } catch (IOException e) {
            throw (new JimmException(120, 226));
        }
    }

    // Sets the reconnect flag and closes the connection
    protected void closeConnection() {
//		inputCloseFlag = true;

        stream_close();

//		Thread.yield();
    }

    // Close input and output streams
    private void stream_close() {
        try {
            is.close();
        } catch (Exception e) {
            /* Do nothing */
        } finally {
            is = null;
        }

        try {
            os.close();
        } catch (Exception e) {
            /* Do nothing */
        } finally {
            os = null;
        }

        try {
            sc.close();
        } catch (Exception e) { /* Do nothing */
        } finally {
            sc = null;
        }
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

            // Set sequence numbers
            packet.setSequence(getFlapSequence());
            if (packet instanceof ToIcqSrvPacket) {
                ((ToIcqSrvPacket) packet).setIcqSequence(nextIcqSequence++);
            }

            // Send packet and count the bytes
            try {
                byte[] outpack = packet.toByteArray();
                os.write(outpack);
                os.flush();

                Traffic.addTrafficOut(outpack.length + 51); // 51 is the overhead for each packet
                if (Jimm.getContactList().isActive()) {
                    Jimm.getContactList().updateTitle();
                }

            } catch (IOException e) {
                close();
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

        // Reset packet buffer
        synchronized (this) {
            rcvdPackets = new Vector();
        }

        // Try
        try {
            // Check abort condition
            while (!inputCloseFlag) {
                // Read flap header
                if (Options.getBoolean(Options.OPTION_CONN_PROP)) {
                    while ((is != null) && (is.available() == 0)) Thread.sleep(250);
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
                icq.waitNotify();
            }
        }
        // Catch communication exception
        catch (NullPointerException e) {
            // Construct and handle exception (only if input close flag has not been set)
            if (!inputCloseFlag) {
                JimmException f = new JimmException(120, 3);
                JimmException.handleException(f);
            }

            // Reset input close flag
            inputCloseFlag = false;
        }
        // Catch InterruptedException
        catch (InterruptedException e) { /* Do nothing */
        }
        // Catch JimmException
        catch (JimmException e) {
            // Handle exception
            JimmException.handleException(e);
        }
        // Catch IO exception
        catch (IOException e) {
            // Construct and handle exception (only if input close flag has not been set)
            if (!inputCloseFlag) {
                JimmException f = new JimmException(120, 1);
                JimmException.handleException(f);
            }

            // Reset input close flag
            inputCloseFlag = false;
        }
        close();
    }
}

// #sijapp cond.end #