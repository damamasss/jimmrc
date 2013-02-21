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
 File: src/jimm/comm/SearchAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;
import jimm.Options;
import jimm.Search;

import java.io.ByteArrayOutputStream;

public class SearchAction extends Action {
    // States of search mission
    public static final int STATE_ERROR = -1;
    public static final int STATE_UIN_SEARCH_SENT = 1;
    public static final int STATE_EMAIL_SEARCH_SENT = 2;
    public static final int STATE_WHITEPAGES_SEARCH_SENT = 3;
    public static final int STATE_FIRSTRESULT_RECEIVED = 4;
    public static final int STATE_LASTRESULT_RECEIVED = 5;
    public static final int STATE_SEARCH_FINISHED = 6;
    public static final int STATE_SEARCH_CANCELED = 7;

    // TLVs used in LE format
    public static final int TLV_TYPE_UIN = 0x3601; // long (4 byte)
    public static final int TLV_TYPE_NICK = 0x5401; // String (2 byte length + string)
    public static final int TLV_TYPE_FIRSTNAME = 0x4001; // String (2 byte length + string)
    public static final int TLV_TYPE_LASTNAME = 0x4A01; // String (2 byte length + string)
    public static final int TLV_TYPE_EMAIL = 0x5E01; // String (2 byte length + string + 1 byte email code)
    public static final int TLV_TYPE_CITY = 0x9001; // String (2 byte length + string)
    public static final int TLV_TYPE_KEYWORD = 0x2602; // String (2 byte length + string)
    public static final int TLV_TYPE_GENDER = 0x7C01; // UINT8 (1 byte: 1 - female, 2 - male)
    public static final int TLV_TYPE_ONLYONLINE = 0x3002; // UINT8 (1 byte:  1 - search online, 0 - search all)
    public static final int TLV_TYPE_AGE = 0x6801; //

    // Search action was called by
    public static final int CALLED_BY_SEARCHUSER = 0;
    public static final int CALLED_BY_ADDUSER = 1;

    // Timeout
    public static final int TIMEOUT = 60 * 1000; // milliseconds

    /**
     * ************************************************************************
     */

    // Action state
    private int state;

    // Search object as container for request and results
    private String[] search;

    private Search cont;

    // Last activity
    private long lastActivity = System.currentTimeMillis();

    // "---", "13-17", "18-22", "23-29", "30-39", "40-49", "50-59", "> 60"
    private final int[] ages = Util.explodeToInt("0,99,13,17,18,22,23,29,30,39,40,49,50,59,60,99", ',');

    public SearchAction(Search cont, String[] search, int _calledBy) {
        super(false, true);
        this.search = search;
        this.cont = cont;
    }

    // Init action
    protected void init() throws JimmException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (int i = search.length - 1; i >= 0; i--) {
            if (search[i] == null) {
                search[i] = Options.emptyString;
            }
        }

        Util.writeWord(buffer, 0x5f05, true);

        // UIN
        if (search[Search.UIN].length() != 0) {
            Util.writeWord(buffer, TLV_TYPE_UIN, true);
            Util.writeWord(buffer, 0x0004, false);
            Util.writeDWord(buffer, Integer.parseInt(search[Search.UIN]), false);
        }

        // NICK
        if (search[Search.NICK].length() != 0)
            Util.writeAsciizTLV(TLV_TYPE_NICK, buffer, search[Search.NICK]);

        // First name
        if (search[Search.FIRST_NAME].length() != 0)
            Util.writeAsciizTLV(TLV_TYPE_FIRSTNAME, buffer, search[Search.FIRST_NAME]);

        // Last name
        if (search[Search.LAST_NAME].length() != 0)
            Util.writeAsciizTLV(TLV_TYPE_LASTNAME, buffer, search[Search.LAST_NAME]);

        // email
        if (search[Search.EMAIL].length() != 0)
            Util.writeAsciizTLV(TLV_TYPE_EMAIL, buffer, search[Search.EMAIL]);

        // City
        if (search[Search.CITY].length() != 0)
            Util.writeAsciizTLV(TLV_TYPE_CITY, buffer, search[Search.CITY]);

        // Keyword
        if (search[Search.KEYWORD].length() != 0)
            Util.writeAsciizTLV(TLV_TYPE_KEYWORD, buffer, search[Search.KEYWORD]);

        // Age (user enter age as "minAge-maxAge", "-maxAge", "minAge-" or "age")
        int ageIndex = 0;
        if (!Options.emptyString.equals(search[Search.AGE])) {
            ageIndex = Util.strToIntDef(search[Search.AGE], 0);
        }
        if (ageIndex != 0) {
            Util.writeWord(buffer, 0x6801, true);
            Util.writeWord(buffer, 4, false);
            Util.writeWord(buffer, ages[2 * ageIndex], false);
            Util.writeWord(buffer, ages[2 * ageIndex + 1], false);
        }

        // Gender
        int gender = 0;
        if (!Options.emptyString.equals(search[Search.GENDER])) {
            gender = Util.strToIntDef(search[Search.GENDER], 0);  
        }
        if (gender != 0) {
            Util.writeWord(buffer, TLV_TYPE_GENDER, true);
            Util.writeWord(buffer, 1, false);
            Util.writeByte(buffer, gender);
        }

        // Only online
        Util.writeWord(buffer, TLV_TYPE_ONLYONLINE, true);
        Util.writeWord(buffer, 1, false);
        Util.writeByte(buffer, search[Search.ONLY_ONLINE].equals("1") ? 1 : 0);

        ToIcqSrvPacket packet = new ToIcqSrvPacket(SnacPacket.CLI_TOICQSRV_COMMAND, 0x0002, icq.getUin(), 0x07D0, new byte[0], buffer.toByteArray());
        icq.sendPacket(packet);

        this.state = STATE_UIN_SEARCH_SENT;
    }

    // Forwards received packet, returns true if packet was consumed
    protected synchronized boolean forward(Packet packet) throws JimmException {
        // Flag indicates whether packet has been consumed or not
        boolean consumed = false;

        if (this.state == STATE_UIN_SEARCH_SENT || this.state == STATE_FIRSTRESULT_RECEIVED) {
            // Watch out for SRV_FROMICQSRV packet type
            if (packet instanceof FromIcqSrvPacket) {
                FromIcqSrvPacket fromIcqServerPacket = (FromIcqSrvPacket) packet;

                int marker = 0;

                byte[] data = fromIcqServerPacket.getData();

                if (Util.getWord(data, marker) == 0xa401) this.state = STATE_FIRSTRESULT_RECEIVED;
                if (Util.getWord(data, marker) == 0xae01) this.state = STATE_LASTRESULT_RECEIVED;

                marker += 2;
                if (Util.getByte(data, marker) == 0x0A) {
                    // Get UIN
                    marker += 3;
                    long uin = Util.getDWord(data, marker, false);

                    // Get nick
                    marker += 4;

                    String[] strings = new String[4];

                    // Get the strings
                    // #0 Nick
                    // #1 Firstname
                    // #2 Lastname
                    // #3 EMail
                    for (int i = 0; i < 4; i++) {
                        strings[i] = Util.byteArrayToString(data, marker + 2, Util.getWord(data, marker, false));
                        marker += 2 + Util.getWord(data, marker, false);
                    }

                    // Get auth flag
                    String auth = (Util.getByte(data, marker) == 0) ? "1" : "0";
                    marker += 1;

                    // Get status
                    int status = Util.getWord(data, marker, false);
                    marker += 2;

                    // Get gender
                    String gender = Util.genderToString(Util.getByte(data, marker));
                    marker += 1;

                    // Get age
                    int age = Util.getWord(data, marker, false);

                    cont.addResult(String.valueOf(uin), strings[0], strings[1] + " " + strings[2], strings[3], auth, status, gender, age);

                    if (this.state == STATE_LASTRESULT_RECEIVED) {
                        marker += 2;
                        long foundleft = Util.getDWord(data, marker, false);
                        this.state = STATE_SEARCH_FINISHED;
                    }
                } else {
                    this.state = ConnectAction.STATE_ERROR;
                }
            }
        }

        // Update activity timestamp
        this.lastActivity = System.currentTimeMillis();

        return consumed;
    }

    public void onEvent(int eventType) {
        switch (eventType) {
            case ON_COMPLETE:
                //NativeCanvas.hideLPCanvas();
                cont.getSearchForm().activate(Search.SearchForm.ACTIV_SHOW_RESULTS);
                break;

            case ON_ERROR:
                //NativeCanvas.hideLPCanvas();
                if (this.state == STATE_FIRSTRESULT_RECEIVED) {
                    JimmException.handleException(new JimmException(159, 0, true));
                } else {
                    cont.getSearchForm().activate(Search.SearchForm.ACTIV_SHOW_NORESULTS);
                }
                break;

            case ON_CANCEL:
                cont.getSearchForm().activate(Search.SearchForm.ACTIV_JUST_SHOW);
                state = STATE_SEARCH_CANCELED;
                break;
        }
    }

    // Returns true if the action is completed
    public boolean isCompleted() {
        return (state == STATE_SEARCH_FINISHED) || (state == STATE_SEARCH_CANCELED);
    }

    // Returns true if an error has occured
    public synchronized boolean isError() {
        return (state == ConnectAction.STATE_ERROR) || ((lastActivity + SearchAction.TIMEOUT) < System.currentTimeMillis());
    }
}