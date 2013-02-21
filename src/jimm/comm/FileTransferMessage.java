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
 File: src/jimm/comm/FileTransferRequest.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

// #sijapp cond.if modules_FILES is "true"#

package jimm.comm;

import jimm.ContactItem;

import java.io.InputStream;

public class FileTransferMessage extends Message {
    // Filename
    private String filename;

    // Description
    private String description;

    // File to transfer
    InputStream fis;
    int fsize;

    // Constructs an outgoing message
    public FileTransferMessage(String sndrUin, ContactItem _rcvr, int _messageType, String _filename, String _description, InputStream is, int size) {
        super(0, null, sndrUin, _messageType);
        this.rcvr = _rcvr;
        this.filename = _filename;
        this.description = _description;
        fis = is;
        fsize = size;
        this.rcvr.setFTM(this);
    }

    // Returns the description
    public String getDescription() {
        return description;
    }

    // Returns the filename
    public String getFilename() {
        return filename;
    }

    // Returns the size of the file
    public int getSize() {
        return fsize;
    }

    // Is another segment available?
    public boolean segmentAvail(int i) {
        return (i <= (fsize / 2048));
    }

    public byte[] getFileSegmentPacket(int segment) {
        byte[] buf;
        if (segment < (fsize / 2048)) {
            buf = new byte[2049];
        } else {
            buf = new byte[(fsize % 2048) + 1];
        }

        Util.putByte(buf, 0, 0x06);

        try {
            fis.read(buf, 1, buf.length - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (buf);
    }
}
//#sijapp cond.end#