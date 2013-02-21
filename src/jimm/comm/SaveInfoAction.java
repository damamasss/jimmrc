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

import jimm.JimmException;
import jimm.info.EditInfo;
import jimm.info.Info;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.Date;

public class SaveInfoAction extends Action {
    // Receive timeout
    private static final int TIMEOUT = 5 * 1000; // milliseconds

    //TLVs
    private static final int NICK_TLV_ID = 0x0154;
    public static final int FIRSTNAME_TLV_ID = 0x0140;
    private static final int LASTNAME_TLV_ID = 0x014A;
    private static final int EMAIL_TLV_ID = 0x015E;
    private static final int BDAY_TLV_ID = 0x023A;
    private static final int CITY_TLV_ID = 0x0190;
    private static final int HOME_PAGE_TLV_ID = 0x0213;
    private static final int ABOUT_TLV_ID = 0x0258;
    private static final int GENDER_TLV_ID = 0x017C;
    private static final int INTERESTS_TLV_ID = 0x01EA;
    private static final int WORK_NAME_TLV_ID = 0x01AE;
    private static final int WORK_POSITION_TLV_ID = 0x01C2;
    private static final int ORIGINAL_CITY_TLV_ID = 0x0320;
    private static final int CELLPHONE_TLV_ID = 0x028A;
/*

    private static final int CELLPHONE_TLV_ID  = 0x028A;
	private static final int WORK_PHONE_TLV_ID = 0x02C6;
*/

    /**
     * ************************************************************************
     */

    private String[] strData = new String[Info.UI_LAST_ID];

    // Date of init
    private Date init;

    private int packetCounter;
    private int errorCounter;

    // Constructor
    public SaveInfoAction(String[] userInfo) {
        super(false, true);
        strData = userInfo;
    }

    // Init action
    protected void init() throws JimmException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

        /* 0x0C3A */
        Util.writeWord(stream, ToIcqSrvPacket.CLI_SET_FULLINFO, false);

        /* Nick */
        Util.writeAsciizTLV(NICK_TLV_ID, stream, strData[Info.UI_NICK], false);

        /* First name */
        Util.writeAsciizTLV(FIRSTNAME_TLV_ID, stream, strData[Info.UI_FIRST_NAME], false);

        /* Last name */
        Util.writeAsciizTLV(LASTNAME_TLV_ID, stream, strData[Info.UI_LAST_NAME], false);

        /* Home Page */
        Util.writeAsciizTLV(HOME_PAGE_TLV_ID, stream, strData[Info.UI_HOME_PAGE], false);

        /* Notes */
        Util.writeAsciizTLV(ABOUT_TLV_ID, stream, strData[Info.UI_ABOUT], false);

        /* City */
        Util.writeAsciizTLV(CITY_TLV_ID, stream, strData[Info.UI_CITY], false);

        /* City birth */
        Util.writeAsciizTLV(ORIGINAL_CITY_TLV_ID, stream, strData[Info.UI_ORIG_CITY], false);

        /* Work */
        Util.writeAsciizTLV(WORK_NAME_TLV_ID, stream, strData[Info.UI_W_NAME], false);

        /* Work Adress*/
        Util.writeAsciizTLV(WORK_POSITION_TLV_ID, stream, strData[Info.UI_W_POS], false);

        /* Cell Phone*/
        Util.writeAsciizTLV(CELLPHONE_TLV_ID, stream, strData[Info.UI_CPHONE], false);

        /* Email */
        Util.writeAsciizTLV(EMAIL_TLV_ID, stream, StringConvertor.getString(strData[Info.UI_EMAIL]), false);

        /* Birthday */
        String birthday = strData[Info.UI_BDAY];
        if (birthday != null) {
            String[] bDate = Util.explode(birthday, '.');
            if (bDate.length == 3) {
                Util.writeWord(stream, BDAY_TLV_ID, false);
                Util.writeWord(stream, 6, false);
                Util.writeWord(stream, Integer.parseInt(bDate[2]), false);
                Util.writeWord(stream, Integer.parseInt(bDate[1]), false);
                Util.writeWord(stream, Integer.parseInt(bDate[0]), false);
            } else {
                Util.writeWord(stream, BDAY_TLV_ID, false);
                Util.writeWord(stream, 6, false);
                Util.writeWord(stream, 0, false);
                Util.writeWord(stream, 0, false);
                Util.writeWord(stream, 0, false);
            }
        }

        /* Gender */
        Util.writeWord(stream, GENDER_TLV_ID, false);
        Util.writeWord(stream, 1, false);
        Util.writeByte(stream, Util.stringToGender(strData[Info.UI_GENDER]));

        /* Send packet */
        ToIcqSrvPacket packet = new ToIcqSrvPacket(0, icq.getUin(), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], stream.toByteArray());
        icq.sendPacket(packet);

        /* Интересы */
        Util.writeWord(stream2, ToIcqSrvPacket.CLI_SET_FULLINFO, false);
        //Util.writeByte(stream2, 0x04);
        /* Раздел №1 */
        int cat1 = EditInfo.getIndex(strData[Info.UI_INT_CAT1]);
        String catStr1 = "";
        if (strData[Info.UI_INT_CAT1].length() > 2) catStr1 = strData[Info.UI_INT_CAT1].substring(2);
        Util.writeAsciizTLVandWord(INTERESTS_TLV_ID, stream2, catStr1, cat1 + 99, false);
        /* Раздел №2 */
        int cat2 = EditInfo.getIndex(strData[Info.UI_INT_CAT2]);
        String catStr2 = "";
        if (strData[Info.UI_INT_CAT2].length() > 2) catStr2 = strData[Info.UI_INT_CAT2].substring(2);
        Util.writeAsciizTLVandWord(INTERESTS_TLV_ID, stream2, catStr2, cat2 + 99, false);
        /* Раздел №3 */
        int cat3 = EditInfo.getIndex(strData[Info.UI_INT_CAT3]);
        String catStr3 = "";
        if (strData[Info.UI_INT_CAT3].length() > 2) catStr3 = strData[Info.UI_INT_CAT3].substring(2);
        Util.writeAsciizTLVandWord(INTERESTS_TLV_ID, stream2, catStr3, cat3 + 99, false);
        /* Раздел №4 */
        int cat4 = EditInfo.getIndex(strData[Info.UI_INT_CAT4]);
        String catStr4 = "";
        if (strData[Info.UI_INT_CAT4].length() > 2) catStr4 = strData[Info.UI_INT_CAT4].substring(2);
        Util.writeAsciizTLVandWord(INTERESTS_TLV_ID, stream2, catStr4, cat4 + 99, false);
        /* Отправка пакета*/
        ToIcqSrvPacket packet2 = new ToIcqSrvPacket(0, icq.getUin(), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], stream2.toByteArray());
        icq.sendPacket(packet2);

        /* Save date */
        this.init = new Date();
    }

    // Forwards received packet, returns true if packet was consumed
    protected boolean forward(Packet packet) throws JimmException {
        boolean consumed = false;

        // Watch out for SRV_FROMICQSRV packet
        if (packet instanceof FromIcqSrvPacket) {
            FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

            // Watch out for SRV_META packet
            if (fromIcqSrvPacket.getSubcommand() != FromIcqSrvPacket.SRV_META_SUBCMD)
                return false;

            // Get packet data
            DataInputStream stream = Util.getDataInputStream(fromIcqSrvPacket.getData(), 0);

            try {
                int type = Util.getWord(stream, false);
                switch (type) {
                    case FromIcqSrvPacket.META_SET_FULLINFO_ACK: //  full user information
                    {
                        if (stream.readByte() != 0x0A) {
                            errorCounter++;
                            break;
                        }

                        consumed = true;
                        packetCounter++;
                        break;
                    }
                }
            }
            catch (Exception e) {
                JimmException.handleExceptionEx(e);
            }
        }

        return (consumed);
    }

    // Returns true if the action is completed
    public boolean isCompleted() {
        return (packetCounter >= 1);
    }

    // Returns true if an error has occured
    public boolean isError() {
        return (this.init.getTime() + SaveInfoAction.TIMEOUT < System.currentTimeMillis()) || errorCounter > 0;
    }

    public int getProgress() {
        return packetCounter > 0 ? 100 : 0;
    }

    public void onEvent(int eventTuype) {
        switch (eventTuype) {
            case ON_COMPLETE:
            case ON_ERROR:
                //Jimm.getContactList().activate();
                //NativeCanvas.hideLPCanvas();
                break;

            default:
                super.onEvent(eventTuype);
                break;
        }
    }
}