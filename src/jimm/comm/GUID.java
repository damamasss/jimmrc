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
 File: src/jimm/comm/GUID.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): aspro
 *******************************************************************************/

package jimm.comm;

public class GUID {

    //<version from="caps_need" method="cap" index="5"/> ver.append(Util.byteArrayToString(caps_need, index, 16 - index))
    //<version from="dc_info1" method="dc_nums"/>
    //<version from="miranda_2" method="cap_dots" index="5" count="3"/> // for (i = index; i < index + count {if miranda_2[i] > 0 append(miranda_2[i]).append(".")}
    //<version from="dc_info2" method="dc_int"/> //dwFP2 & 0xFFFF
    //<version from="dc_info2" method="dc_dots"/>

    // MESSAGES
    public static final GUID CAP_UTF8_GUID = new GUID(Util.explodeToBytes("7b 30 39 34 36 31 33 34 45 2D 34 43 37 46 2D 31 31 44 31 2D 38 32 32 32 2D 34 34 34 35 35 33 35 34 30 30 30 30 7D", ' ', 16));
    public static final GUID CAP_WIN1251_GUID = new GUID(Util.explodeToBytes("7B 30 30 30 30 31 32 35 31 2D 30 30 30 30 2D 30 30 30 30 2D 30 30 30 30 2D 30 30 30 30 30 30 30 30 31 32 35 31 7D", ' ', 16));
    // QIP STATUS
    public static final GUID STATUS_DEPRESSION = new GUID(Util.explodeToBytes("b7 07 43 78 f5 0c 77 77 97 77 57 78 50 2d 05 70", ' ', 16));
    public static final GUID STATUS_CHAT = new GUID(Util.explodeToBytes("b7 07 43 78 f5 0c 77 77 97 77 57 78 50 2d 05 75", ' ', 16));
    public static final GUID STATUS_HOME = new GUID(Util.explodeToBytes("b7 07 43 78 f5 0c 77 77 97 77 57 78 50 2d 05 76", ' ', 16));
    public static final GUID STATUS_WORK = new GUID(Util.explodeToBytes("b7 07 43 78 f5 0c 77 77 97 77 57 78 50 2d 05 77", ' ', 16));
    public static final GUID STATUS_LUNCH = new GUID(Util.explodeToBytes("b7 07 43 78 f5 0c 77 77 97 77 57 78 50 2d 05 78", ' ', 16));
    public static final GUID STATUS_EVIL = new GUID(Util.explodeToBytes("b7 07 43 78 f5 0c 77 77 97 77 57 78 50 2d 05 79", ' ', 16));
    // WORKED
    public static final GUID CAP_UTF8 = new GUID(Util.explodeToBytes("09 46 13 4E 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
    public static final GUID CAP_MTN = new GUID(Util.explodeToBytes("56 3F C8 09 0B 6F 41 BD 9F 79 42 26 09 DF A2 F3", ' ', 16));
    public static final GUID CAP_AIM_SERVERRELAY = new GUID(Util.explodeToBytes("09 46 13 49 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
    // MY CLIENT
    public static final GUID CAP_DC = new GUID(Util.explodeToBytes("09 46 00 00 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
    public static final GUID CAP_AIM = new GUID(Util.explodeToBytes("09 46 13 4D 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
    public static final GUID CAP_FILE_TRANSFER = new GUID(Util.explodeToBytes("09 46 13 43 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
    public static final GUID CAP_XTRAZ = new GUID(Util.explodeToBytes("1A 09 3C 6C D7 FD 4E C5 9D 51 A6 47 4E 34 F5 A0", ' ', 16));
    public static final GUID CAP_ECONOM = new GUID(Util.explodeToBytes("4A 5B 69 5D 6D 6D 46 61 6B 65 55 54 46 38 00 00", ' ', 16));
    // MASKED
//    public static final GUID CAP_AVATAR = new GUID(Util.explodeToBytes("09 46 13 4C 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
//    public static final GUID CAP_MIRANDAIM = new GUID(Util.explodeToBytes("4D 69 72 61 6E 64 61 4D 00 08 00 1C 00 05 00 05", ' ', 16));
//    public static final GUID CAP_RICHTEXT = new GUID(Util.explodeToBytes("97 B1 27 51 24 3C 43 34 AD 22 D6 AB F7 3F 14 92", ' ', 16));
//    public static final GUID CAP_MIM = new GUID(Util.explodeToBytes("*MIM/J[i]mm,20,*PK,00,00,00", ',', 16));
//    public static final GUID CAP_QIP = new GUID(Util.explodeToBytes("56,3F,C8,09,0B,6F,41,*QIP 2005a", ',', 16));
//    public static final GUID CAP_QIP_PRTMESS = new GUID(Util.explodeToBytes("D3 D4 53 19 8B 32 40 3B AC C7 D1 A9 E2 B5 81 3E", ' ', 16));
//    public static final GUID CAP_QIPPDAWIN = new GUID(Util.explodeToBytes("56,3F,C8,09,0B,6F,41,*QIP     !", ',', 16));
//    public static final GUID CAP_QIPPDASYM = new GUID(Util.explodeToBytes("51 AD D1 90 72 04 47 3D A1 A1 49 F4 A3 97 A4 1F", ' ', 16));
//    public static final GUID CAP_QIPINFIUM = new GUID(Util.explodeToBytes("7C 73 75 02 C3 BE 4F 3E A6 9F 01 53 13 43 1E 1A", ' ', 16));
//    public static final GUID CAP_QIP2010 = new GUID(Util.explodeToBytes("7A 7B 7C 7D 7E 7F 0A 03 0B 04 01 53 13 43 1E 1A", ' ', 16));
//    public static final GUID CAP_BAYAN = new GUID(Util.explodeToBytes("*bayanICQ0.22 00 00 00 00", ' ', 16));
//    public static final GUID CAP_PIGEON = new GUID(Util.explodeToBytes("*PIGEON!,00,00,00,00,00,00,00,00,00", ',', 16));
//    public static final GUID CAP_MACICQ = new GUID(Util.explodeToBytes("DD 16 F2 02 84 E6 11 D4 90 DB 00 10 4B 9B 4B 7D", ' ', 16));
//    public static final GUID CAP_XTRAZ_CHAT = new GUID(Util.explodeToBytes("67 36 15 15 61 2D 4C 07 8F 3D BD E6 40 8E A0 41", ' ', 16));
//    public static final GUID CAP_TZERS = new GUID(Util.explodeToBytes("B2 EC 8F 16 7C 6F 45 1B BD 79 DC 58 49 78 88 B9", ' ', 16));
//    public static final GUID CAP_ICQ6 = new GUID(Util.explodeToBytes("01 38 CA 7B 76 9A 49 15 88 F2 13 FC 00 97 9E A8", ' ', 16));
//    public static final GUID CAP_AIMCHAT = new GUID(Util.explodeToBytes("74 8F 24 20 62 87 11 D1 82 22 44 45 53 54 00 00", ' ', 16));
//    public static final GUID CAP_ICQLITE = new GUID(Util.explodeToBytes("17 8C 2D 9B DA A5 45 BB 8D DB F3 BD BD 53 A1 0A", ' ', 16));
//    public static final GUID CAP_ICQLIVE_AUDIO = new GUID(Util.explodeToBytes("09 46 01 04 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16)); // Live Audio(new VoiceChat)
//    public static final GUID CAP_ICQLIVE_VIDEO = new GUID(Util.explodeToBytes("09 46 01 01 4C 7F 11 D1 82 22 44 45 53 54 00 00", ' ', 16)); // Live Video
//    public static final GUID CAP_TRILLIAN = new GUID(Util.explodeToBytes("97 B1 27 51 24 3C 43 34 AD 22 D6 AB F7 3F 14 09", ' ', 16));
//    public static final GUID CAP_TRILCRYPT = new GUID(Util.explodeToBytes("F2 E7 C7 F4 FE AD 4D FB B2 35 36 79 8B DF 00 00", ' ', 16));

//  <client par_name="ICQ 7" pic="cli_icq7.png">
//	<proto version="9"/>
//    <caps>
//        <cap par_name="smart_caps"/>
//        <cap par_name="file_transfer"/>
//        <cap par_name="live_audio"/>
//        <cap par_name="utf_messages"/>
//        <cap par_name="live_video"/>
//        <cap par_name="send_buddylist"/>
//        <cap par_name="buddy_icon"/>
//        <cap par_name="xtraz_support"/>
//        <cap par_name="chat_support"/>
//        <cap par_name="tzerz"/>
//        <cap par_name="cap_icq_lite"/>
//    </caps>
//    <no_caps>
//        <cap par_name="server_relay"/>
//    </no_caps>
    //</client>
    //
    private byte[] guid;

    public GUID(byte[] guid) {
        this.guid = guid;
    }

    public boolean equals(byte[] data) {
        if (data.length != guid.length) {
            return false;
        }
        for (int i = 0; i < guid.length; i++) {
            if (data[i] != guid[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean containsIn(byte[] guids, int off, int len) { // use xml
        if (off + len > guids.length) {
            return false;
        }
        byte[] temp = new byte[len];
        System.arraycopy(guids, off, temp, 0, len);
        return containsIn(temp, guid.length);
    }

    public boolean containsIn(byte[] guids, int len) {
        if (guids == null) {
            return false;
        }
        for (int i = 0; i < guids.length; i += len) {
            if (equals(guids, i, len)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsIn(byte[] guids) { // use xml
        return containsIn(guids, guid.length);
    }

    public boolean equals(byte[] data, int off, int len) {
        if (data.length < off + len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (data[i + off] != guid[i]) {
                return false;
            }
        }
        return true;
    }

    public byte[] toByteArray() {
        return guid;
    }
}
