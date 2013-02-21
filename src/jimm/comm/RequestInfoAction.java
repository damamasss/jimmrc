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
 Author(s): Manuel Linsmayer
 *******************************************************************************/

package jimm.comm;

import jimm.ContactItem;
import jimm.JimmException;
import jimm.info.Info;
import jimm.info.UserInfo;

import java.io.DataInputStream;
import java.util.Date;

public class RequestInfoAction extends Action {
    // Receive timeout
    private static final int TIMEOUT = 10 * 1000; // milliseconds

    private boolean infoShown;

    private UserInfo userInfo;

//	private boolean showInfoText = true;

    /**
     * ************************************************************************
     */

    private String[] strData = new String[Info.UI_LAST_ID];

    // Date of init
    private Date init;

    private int packetCounter;

    private String existingNick;

    // Constructor
    public RequestInfoAction(String uin, String nick, UserInfo userInfo) {
        super(false, true);
        existingNick = nick;
        infoShown = false;
        packetCounter = 0;
        strData[Info.UI_UIN] = uin;
        this.userInfo = userInfo;
    }

    // Init action
    protected void init() throws JimmException {
        // Send a CLI_METAREQINFO packet
        byte[] buf = new byte[6];
        Util.putWord(buf, 0, ToIcqSrvPacket.CLI_META_REQMOREINFO_TYPE, false);
        Util.putDWord(buf, 2, Long.parseLong(strData[Info.UI_UIN]), false);
        ToIcqSrvPacket packet = new ToIcqSrvPacket(0, icq.getUin(), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], buf);
        icq.sendPacket(packet);

        // Save date
        this.init = new Date();
    }

    // Forwards received packet, returns true if packet was consumed
    protected synchronized boolean forward(Packet packet) throws JimmException {
        boolean consumed = false;

        //boolean authFlag = false, webAwareFlag = false;

        // Watch out for SRV_FROMICQSRV packet
        if (packet instanceof FromIcqSrvPacket) {
            FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

            // Watch out for SRV_META packet
            if (fromIcqSrvPacket.getSubcommand() != FromIcqSrvPacket.SRV_META_SUBCMD) {
                return false;
            }

            // Get packet data
            DataInputStream stream = Util.getDataInputStream(fromIcqSrvPacket.getData(), 0);

            // Watch out for SRV_METAGENERAL packet
            try {
                int type = Util.getWord(stream, false);
                stream.readByte(); // Success byte

                switch (type) {
                    case FromIcqSrvPacket.SRV_META_GENERAL_TYPE: // basic user information
                    {
                        strData[Info.UI_NICK] = Util.readAsciiz(stream); // nickname
                        // first name + last name
                        String fistName = Util.readAsciiz(stream);
                        String lastName = Util.readAsciiz(stream);
                        strData[Info.UI_FIRST_NAME] = fistName;
                        strData[Info.UI_LAST_NAME] = lastName;
                        if ((fistName.length() != 0) || (lastName.length() != 0))
                            strData[Info.UI_NAME] = fistName + " " + lastName;
                        strData[Info.UI_EMAIL] = Util.readAsciiz(stream); // email
                        strData[Info.UI_CITY] = Util.readAsciiz(stream); // home city
                        strData[Info.UI_STATE] = Util.readAsciiz(stream); // home state
                        strData[Info.UI_PHONE] = Util.readAsciiz(stream); // home phone
                        strData[Info.UI_FAX] = Util.readAsciiz(stream); // home fax
                        strData[Info.UI_ADDR] = Util.readAsciiz(stream); // home address
                        strData[Info.UI_CPHONE] = Util.readAsciiz(stream); // cell phone

//                        if (strData[Info.UI_UIN].equals(icq.getUin())) {
//							Util.readAsciiz(stream); // home zip code
//							Util.getWord(stream, false); // home country code
//							byte[] buffer = new byte[3];
//							stream.readFully(buffer); // GMT offset | auth flag | web aware flag
//                            icq.getProfile().setBoolean(Profile.OPTION_REQ_AUTH, Util.getByte(buffer, 1) == (byte)0);
//							icq.getProfile().setBoolean(Profile.OPTION_WEBAWARE, Util.getByte(buffer, 2) != (byte)0);
//							infoShown = true;
//							if (strData[Info.UI_NICK].length() > 0){
//								icq.getProfile().setNick(strData[Info.UI_NICK]);
//							}
//						}
                        packetCounter++;
                        consumed = true;
                        break;
                    }

                    case 0x00DC: // more user information
                    {
                        int age = Util.getWord(stream, false);
                        strData[Info.UI_AGE] = (age != 0) ? Integer.toString(age) : "";
                        strData[Info.UI_GENDER] = Util.genderToString(stream.readByte());
                        strData[Info.UI_HOME_PAGE] = Util.readAsciiz(stream);
                        int year = Util.getWord(stream, false);
                        int mon = stream.readByte();
                        int day = stream.readByte();
                        strData[Info.UI_BDAY] = (year != 0) ? day + "." + mon + "." + year : "";
                        stream.readByte(); // foreing language 1
                        stream.readByte(); // foreing language 2
                        stream.readByte(); // foreing language 3
                        Util.getWord(stream, false); // unknown
                        strData[Info.UI_ORIG_CITY] = Util.readAsciiz(stream); // original city
                        strData[Info.UI_ORIG_STATE] = Util.readAsciiz(stream); // original state
                        packetCounter++;
                        consumed = true;
                        break;
                    }

                    case 0x00D2: // work user information
                    {
                        for (int i = Info.UI_W_CITY; i <= Info.UI_W_ADDR; i++)
                            strData[i] = Util.readAsciiz(stream); // city - address
                        Util.readAsciiz(stream); // work zip code
                        Util.getWord(stream, false); // work country code
                        strData[Info.UI_W_NAME] = Util.readAsciiz(stream); // work company
                        strData[Info.UI_W_DEP] = Util.readAsciiz(stream); // work department
                        strData[Info.UI_W_POS] = Util.readAsciiz(stream); // work position
                        packetCounter++;
                        consumed = true;
                        break;
                    }

                    case 0x00E6: // user about information
                    {
                        strData[Info.UI_ABOUT] = Util.readAsciiz(stream); // notes string
                        packetCounter++;
                        consumed = true;
                        break;
                    }

                    case 0x00F0: // user interests information
                    {
                        //StringBuffer sb = new StringBuffer();
                        int counter = Math.min(stream.readByte(), 4);
                        String item;
                        for (int i = 0; i < counter; i++) {
                            int category = 0;
                            item = "";
                            try {
                                category = Util.getWord(stream, false) - 100;
                                item = Util.readAsciiz(stream);
                            } catch (Exception e) {
                                JimmException.handleExceptionEx(e);
                            }
                            if (item.trim().length() == 0 || category < 0) {
                                strData[Info.UI_INT_CAT1 + i] = "60";
                                continue;
                            }
                            strData[Info.UI_INT_CAT1 + i] = Util.makeTwo(category) + item;
                        }
                        strData[Info.UI_INETRESTS] = "not_null";
                        packetCounter++;
                        consumed = true;
                        break;
                    }

                    case 0x00FA: // end snac
                    {
                        packetCounter++;
                        consumed = true;
                        break;
                    }
                }
            } catch (Exception e) {
                JimmException.handleExceptionEx(e);
                System.out.println(e.getMessage());
            }

            // is completed?
            if (isCompleted()) {
                if (!infoShown) {
                    //RunnableImpl.callSerially(RunnableImpl.TYPE_SHOW_USER_INFO, (Object)strData);
                    userInfo.showUserInfo(strData);
                    tryToChangeName();
                    infoShown = true;
                }
            }
        }

        return (consumed);
    }

    // Rename contact if its name consists of digits
    private void tryToChangeName() {
        if (strData[Info.UI_UIN].equals(existingNick)) {
            ContactItem item = icq.getProfile().getItemByUIN(strData[Info.UI_UIN]);
            if (item != null) item.rename(strData[Info.UI_NICK]);
        }
    }

    // Returns true if the action is completed
    public synchronized boolean isCompleted() {
        return (packetCounter >= 5);
    }

    // Returns true if an error has occured
    public synchronized boolean isError() {
        return (this.init.getTime() + RequestInfoAction.TIMEOUT < System.currentTimeMillis());
    }
}