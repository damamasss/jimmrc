/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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
 File: src/jimm/comm/RequestInfoAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Igor Palkin
 *******************************************************************************/


package jimm.comm;

import jimm.ContactItem;
import jimm.Jimm;
import jimm.JimmException;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class ServerListsAction extends Action {

    // Receive timeout
    private static final int TIMEOUT = 3 * 1000; // milliseconds

    public static final int VISIBLE_LIST = 0x0002;
    public static final int INVISIBLE_LIST = 0x0003;
    public static final int IGNORE_LIST = 0x000E;

    public static final int ADD_INTO_LIST = 0;
    public static final int REMOVE_FROM_LIST = 1;

    /**
     * ************************************************************************
     */

    // Date of init

    private int subaction;
    private int list;
    private ContactItem item;
    private Date init;
    private int id;

    // Constructor
    public ServerListsAction(int list, ContactItem item) {
        super(false, true);
        this.list = list;
        this.item = item;
    }

    // Init action
    public synchronized void init() throws JimmException {
        id = 0;
        switch (list) {
            case VISIBLE_LIST:
                id = item.getVisibleId();
                break;

            case INVISIBLE_LIST:
                id = item.getInvisibleId();
                break;

            case IGNORE_LIST:
                id = item.getIgnoreId();
                break;

        }

        if (id == 0) {
            id = Util.createRandomId(icq);
            subaction = ADD_INTO_LIST;
        } else {
            subaction = REMOVE_FROM_LIST;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Util.writeLenAndString(stream, item.getUinString(), true);
        Util.writeWord(stream, 0, true);
        Util.writeWord(stream, id, true);
        Util.writeWord(stream, list, true);
        Util.writeWord(stream, 0, false);

        SnacPacket packet = null;
        switch (subaction) {
            case ADD_INTO_LIST:
                packet = new SnacPacket(SnacPacket.CLI_ROSTERADD_FAMILY, SnacPacket.CLI_ROSTERADD_COMMAND, 0, new byte[0], stream.toByteArray());
                break;

            case REMOVE_FROM_LIST:
                packet = new SnacPacket(SnacPacket.CLI_ROSTERDELETE_FAMILY, SnacPacket.CLI_ROSTERDELETE_COMMAND, 0, new byte[0], stream.toByteArray());
                id = 0;
                break;
        }
        //if (packet != null) {
        icq.sendPacket(packet);
        //} else {
        //    throw new JimmException();
        //}
        init = new Date();
    }

    private int packetCounter = 0;

    // Forwards received packet, returns true if packet was consumed
    public synchronized boolean forward(Packet packet) throws JimmException {

        // Watch out for SRV_FROMICQSRV packet
        if (packet instanceof SnacPacket) {
            SnacPacket snacPacket = (SnacPacket) packet;
            if (snacPacket.getFamily() != SnacPacket.SRV_UPDATEACK_FAMILY
                    || snacPacket.getCommand() != SnacPacket.SRV_UPDATEACK_COMMAND) {
                return false;
            }
            //FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;
            byte[] data = snacPacket.getData();
            int result = Util.getWord(data, 0, false);
            if (result == 0) {
                switch (list) {
                    case VISIBLE_LIST:
                        item.setVisibleId(id);
                        break;

                    case INVISIBLE_LIST:
                        item.setInvisibleId(id);
                        break;

                    case IGNORE_LIST:
                        item.setIgnoreId(id);
                        break;
                }
                Jimm.getContactList().update(item);
            }
            packetCounter++;
            return true;
        }

        return false;
    }

    // Returns true if the action is completed
    public synchronized boolean isCompleted() {
        return (packetCounter >= 1);
    }

    // Returns true if an error has occured
    public synchronized boolean isError() {
        return (init.getTime() + TIMEOUT) < System.currentTimeMillis();
    }

    public int getProgress() {
        return packetCounter * 100;
    }
}
