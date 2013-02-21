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
 File: src/jimm/comm/OtherAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): aspro
 *******************************************************************************/

package jimm.comm;

import jimm.*;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

public class OtherAction {

    public static Packet getUserInfoPacket(GUID[] guids) throws JimmException {
        // Send a CLI_SETUSERINFO packet
        byte[] packet = new byte[guids.length * 16 + 4];
        packet[0] = 0x00;
        packet[1] = 0x05;
        packet[2] = (byte) ((guids.length * 16) / 0x100);
        packet[3] = (byte) ((guids.length * 16) % 0x100);
        for (int i = 0; i < guids.length; i++) {
            System.arraycopy(guids[i].toByteArray(), 0, packet, i * 16 + 4, 16);
        }
        return (new SnacPacket(SnacPacket.CLI_SETUSERINFO_FAMILY, SnacPacket.CLI_SETUSERINFO_COMMAND, 0, new byte[0], packet));
    }

    private static GUID a_phone = guidModel();

    private static GUID guidModel() {
        return (new GUID(Util.stringToByteArray(getModel())));
    }

    public static String getModel() {
//            return "mi:1K810i       ";
//            return "mi:0N97-1       ";
        String mi = getModelExt();
        StringBuffer sb = new StringBuffer("mi:");
        int k = -1;
        for (int i = ClientID.integerPhones.length - 1; i >= 0; i--) {
            if (mi.indexOf(ClientID.integerPhones[i]) == 0) {
                k = i;
                i = ClientID.integerPhones[i].length();
                mi = mi.substring(i);
                break;
            }
        }
        int len = 13;
        if (Jimm.isTouch() && mi.length() <= len - 2) mi = mi + "TS";
        mi = mi.substring(0, Math.min(mi.length(), len - 1));
        if (k >= 0 || mi.length() <= len) {
            int j = len;
            if (k >= 0) {
                j--;
                sb.append(k);
            }
            sb.append(mi);
            for (j -= mi.length(); j > 0; j--) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String getModelExt() {
        String microedition = StringConvertor.getSystemProperty("microedition.platform", null);
        if (microedition == null) {
            return null;
        }
        if (Jimm.is_phone_SE()) {
            return getModelText(microedition);
        }
        if (microedition.startsWith("Alcatel")) {
            return getModelText(microedition);
        }

        String s1 = StringConvertor.getSystemProperty("com.sonyericsson.java.platform", null);
        if (s1 != null) {
            microedition = microedition + "/" + s1;
        }
        s1 = StringConvertor.getSystemProperty("device.model", null);

        String s2 = StringConvertor.getSystemProperty("device.software.version", null);

        if (microedition == null) {
            microedition = "Motorola";
        }

        if (microedition.startsWith("j2me") && s1 != null) {
            if (s1.startsWith("wtk-emulator")) {
                microedition = s1;
            }
            if (s1 != null && s2 != null) {
                microedition = "Motorola";
            } else if ((s2 = StringConvertor.getSystemProperty("microedition.hostname", null)) != null) {
                microedition = "Motorola";
                if (s1 != null)
                    s2 = s1;
                if (s2.indexOf("(none)") < 0)
                    microedition = microedition + "/" + s2;
            }
        }

        if (microedition.startsWith("Moto")) {
            if (s1 == null)
                s1 = StringConvertor.getSystemProperty("funlights.product", null);
            if (s1 != null)
                microedition = "Motorola" + s1;
        }
        if (microedition.indexOf("SIE") >= 0) {
            microedition = Util.replaceStr(StringConvertor.getSystemProperty("microedition.platform", null), "SIE", "Siemens");
        } else if (StringConvertor.getSystemProperty("com.siemens.OSVersion", null) != null) {
            microedition = "Siemens" + StringConvertor.getSystemProperty("microedition.platform", null) + "/" + StringConvertor.getSystemProperty("com.siemens.OSVersion", null);
        }

        try {
            Class.forName("com.samsung.util.Vibration");
            microedition = "Samsung";
        } catch (Throwable ignored) {
        }

        try {
            Class.forName("mmpp.media.MediaPlayer");
            microedition = "LG";
        } catch (Throwable e) {
            try {
                Class.forName("mmpp.phone.Phone");
                microedition = "LG";
            } catch (Throwable e1) {
                try {
                    Class.forName("mmpp.lang.MathFP");
                    microedition = "LG";
                } catch (Throwable e2) {
                    try {
                        Class.forName("mmpp.media.BackLight");
                        microedition = "LG";
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        return getModelText(microedition);
    }

    private static String getModelText(String platform) {
        if (platform.startsWith("Motorola") && platform.indexOf("MOTOMING_") >= 0) {
            platform = Util.replaceStr(platform, "MOTOMING_", "");
        }
        if (platform.startsWith("Nokia")) {
            platform = Util.replaceStr(platform, "XpressMusic", "XS");
            platform = Util.replaceStr(platform, "Supernova", "SN");
            platform = Util.replaceStr(platform, "Navigator", "NA");
            //platform = Util.replaceStr(platform, "c-2", "XS");
            //platform = Util.replaceStr(platform, "d-1", "XS");
            //platform = Util.replaceStr(platform, "s-1", "SN");
        }

        if (platform.indexOf('/') >= 0) {
            return platform.substring(0, platform.indexOf('/'));
        } else {
            return platform;
        }
    }

    public static Packet getStandartUserInfoPacket(Icq icq) throws JimmException {
        Vector guids = new Vector();
        int client = icq.getProfile().getInt(jimm.Profile.OPTION_CLIENT_ID) - 1;
        long fp1 = 0x00000000;
        long fp2 = 0x00000000;
        long fp3 = 0x00000000;
        int prot = 0;

        // SYSTEM CAPS START
        guids.addElement(GUID.CAP_AIM);

        if (icq.getProfile().getBoolean(Profile.OPTION_DELIVERY_REPORT)) {
            guids.addElement(GUID.CAP_AIM_SERVERRELAY);
        }

        guids.addElement(GUID.CAP_DC);
        // SYSTEM CAPS FIN

        GUID xstatus = XStatus.getStatusGUID(icq.getProfile().getInt(Profile.OPTION_XSTATUS));

        if (xstatus != null && icq.getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
            guids.addElement(GUID.CAP_XTRAZ);
            guids.addElement(xstatus);
        }

        if (client < 0) {
            byte[] jguid = Util.explodeToBytes("*Jimm ,00,00,00,00,00,00,00,00,00,00,00", ',', 16);

            byte[] jver = Util.stringToByteArray(icq.getProfile().getString(Profile.OPTION_STRING_VERSION));

            System.arraycopy(jver, 0, jguid, 5, jver.length <= 11 ? jver.length : 11);

            GUID CAP_JIMM = new GUID(jguid);

            guids.addElement(CAP_JIMM);

            if (!Options.getBoolean(Options.OPTION_CP1251_HACK)) {
                guids.addElement(GUID.CAP_UTF8);
            }

//            GUID xstatus = XStatus.getStatusGUID(icq.getProfile().getInt(Profile.OPTION_XSTATUS));
//
//            if (xstatus != null && icq.getProfile().getBoolean(Profile.OPTION_XTRAZ_ENABLE)) {
//                guids.addElement(GUID.CAP_XTRAZ);
//                guids.addElement(xstatus);
//            }

//#sijapp cond.if modules_FILES="true" #
            guids.addElement(GUID.CAP_FILE_TRANSFER);
//#sijapp cond.end #

//#sijapp cond.if target isnot "DEFAULT"#
            if (Options.getInt(Options.OPTION_TYPING_MODE) > 0) {
                guids.addElement(GUID.CAP_MTN);
            }
//#sijapp cond.end#
            fp1 = 0xFFFFFFFE;
            fp2 = 0x01012012;
            fp3 = 0xFFFFFFFE;
            prot = 9;
        } else if (client >= 0 && client < ClientID.getClients().size()) {
            Client ce = (Client) ClientID.getClients().elementAt(client);
            int size = ce.caps.size();
            GUIDs guid;
            for (int j = 0; j < ce.methods.size(); j++) {
                int hashMethod = ce.methods.elementAt(j).hashCode();
                if (hashMethod == "caps".hashCode())
                    for (int i = 0; i < size; i++) {
                        guid = ((GUIDs) ce.caps.elementAt(i));
                        guids.addElement(guid.value);
                    }
                else if (hashMethod == "caps_num".hashCode())
                    for (int i = 0; i < size; i++) {
                        guid = ((GUIDs) ce.caps.elementAt(i));
                        for (int u = 0; u < guids.size(); u++) {
                            if (guids.elementAt(i).equals(guid.value)) {
                                if (guid.num != i)
                                    guids.setElementAt(guid.value, guid.num);
                                break;
                            }
                        }
                    }
                else if (hashMethod == "proto".hashCode())
                    prot = ce.proto;
                else if (hashMethod == "dc_info1".hashCode())
                    fp1 = ce.dc1;
                else if (hashMethod == "dc_info2".hashCode())
                    fp2 = ce.dc2;
                else if (hashMethod == "dc_info3".hashCode())
                    fp3 = ce.dc3;
            }
        }
        if (a_phone != null) {
            guids.addElement(a_phone);
        }
        int status = icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS);
        switch (status) {
            case ContactItem.STATUS_CHAT:
                guids.addElement(GUID.STATUS_CHAT);
                break;
            case ContactItem.STATUS_LUNCH:
                guids.addElement(GUID.STATUS_LUNCH);
                break;
            case ContactItem.STATUS_EVIL:
                guids.addElement(GUID.STATUS_EVIL);
                break;
            case ContactItem.STATUS_DEPRESSION:
                guids.addElement(GUID.STATUS_DEPRESSION);
                break;
            case ContactItem.STATUS_HOME:
                guids.addElement(GUID.STATUS_HOME);
                break;
            case ContactItem.STATUS_WORK:
                guids.addElement(GUID.STATUS_WORK);
                break;
        }
        setClientDC(fp1, fp2, fp3, prot);
        GUID[] result = new GUID[guids.size()];
        guids.copyInto(result);
        return getUserInfoPacket(result);
    }

    public static void setClientDC(long fp1, long fp2, long fp3, int prot) {
        Util.putWord(CLI_SETSTATUS_DATA, 21, prot, true);
        Util.putDWord(CLI_SETSTATUS_DATA, 35, fp1, true);
        Util.putDWord(CLI_SETSTATUS_DATA, 39, fp2, true);
        Util.putDWord(CLI_SETSTATUS_DATA, 43, fp3, true);
    }

//    private static void setClientId(Icq icq) {
//        int client = icq.getProfile().getInt(Profile.OPTION_CLIENT_ID);
//        long fp1 = 0xFFFFFFFE;
//        long fp2 = 0x00010000;
//        long fp3 = 0xFFFFFFFE;
//        int prot = 9;
//        //String[] ver = Util.explode("###VERSION###", '.');
//        //long diver = Long.parseLong(ver[0] + ver[1], 16);
//        //prot = Options.getBoolean(Options.OPTION_DELIVERY_REPORT) ? 0x09 : 0x08;
//
//        switch (client) {
//            case 0:
//                fp1 = 0xFFFFFFFE;
//                fp2 = 0x01012012;
//                fp3 = 0xFFFFFFFE;
//                break; // Jimm
////            case 1:
////                fp1 = 0x08000907;
////                fp2 = 0x0000000E;
////                fp3 = 0x0000000F;
////                prot = 11;
////                break; // QIP 2005a
////            case 2:
////                fp1 = 0x0000115C;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 11;
////                break; // QIP 2010
////            case 3:
////                fp1 = 0x00002354;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 11;
////                break; // QIP Infium
////            case 4:
////                fp1 = 0x00000000;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 11;
////                break; // QIP PDA (Symbian)
////            case 5:
////                fp1 = 0x00000000;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 11;
////                break; // QIP PDA (Windows)
////            case 6:
////                fp1 = 0xFFFFFFFF;
////                fp2 = 0x00050005;
////                fp3 = 0xFFFFFFFF;
////                prot = 8;
////                break; // Miranda
////            case 7:
////                fp1 = 0x00000000;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 9;
////                break; // ICQ 6
////            case 8:
////                fp1 = 0xFFFFF666;
////                fp2 = 0x00000455;
////                fp3 = 0x00000000;
////                prot = 9;
////                break; // R&Q
////            case 9:
////                fp1 = 0x00000000;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 7;
////                break; // Mac ICQ
////            case 10:
////                fp1 = 0x3b75ac09;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 7;
////                break; // Trillian
////            case 11:
////                fp1 = 0x00000000;
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                break; // Pigeon
////            case 12:
////                fp1 = 0x48200903;
////                fp2 = 0x00000240;
////                fp3 = 0x48200903;
////                break; // Jimm RC
////            case 13:
////                fp1 = 0x2001de9d; // Nokia 5530
////                fp2 = 0x00000000;
////                fp3 = 0x00000000;
////                prot = 11;
////                break; // bayanICQ
////            // fp1 = 0xF7F7F7F7;  fp2 = 0x0000142C; fp3 = 0x00000000; prot =  9; // Unknown
//        }
//        Util.putWord(CLI_SETSTATUS_DATA, 21, prot, true);
//        Util.putDWord(CLI_SETSTATUS_DATA, 35, fp1, true);
//        Util.putDWord(CLI_SETSTATUS_DATA, 39, fp2, true);
//        Util.putDWord(CLI_SETSTATUS_DATA, 43, fp3, true);
//    }

    public static boolean isInvisNum = true;

    public static Packet getAuthPacket(Icq icq) throws JimmException {
        if (isInvisNum) {
            //System.out.println("It's invis num!");
            return null;
        }
        byte auth = (icq.getProfile().getBoolean(Profile.OPTION_REQ_AUTH)) ? (byte) 0x00 : (byte) 0x01;
        byte webAware = (icq.getProfile().getBoolean(Profile.OPTION_WEBAWARE)) ? (byte) 0x01 : (byte) 0x00;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Util.writeWord(stream, ToIcqSrvPacket.CLI_SET_FULLINFO, false);

        Util.writeWord(stream, 0x02F8, false);
        Util.writeWord(stream, 0x01, false);
        Util.writeByte(stream, auth);

        Util.writeWord(stream, 0x030C, false);
        Util.writeWord(stream, 0x01, false);
        Util.writeByte(stream, webAware);
        ToIcqSrvPacket packet = new ToIcqSrvPacket(0, icq.getUin(), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], stream.toByteArray());
        return packet;
    }

    public static Packet getFlagsPacket(Icq icq) throws JimmException {
        byte[] buf = new byte[6];
        Util.putWord(buf, 0, 0x04BA, false);
        Util.putDWord(buf, 2, Long.parseLong(icq.getUin()), false);
        ToIcqSrvPacket packet = new ToIcqSrvPacket(0, icq.getUin(), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], buf);
        return packet;
    }

    private static byte[] CLI_SETSTATUS_DATA =
            Util.explodeToBytes
                    (
                            "00,06,00,04," +
                                    "11,00,00,00," + // Online status
                                    "00,0C,00,25," + // TLV(C)
                                    "C0,A8,00,01," + // 192.168.0.1, cannot get own IP address
                                    "00,00,AB,CD," + // Port 43981
                                    "00," +          // Firewall
                                    "00,09," +       // Supports protocol version 8 (9 для того, чтобы подтверждение доставки работало)
                                    "00,00,00,00," +
                                    "00,00,00,50," +
                                    "00,00,00,03," +
                                    "FF,FF,FF,FE," + // Timestamp 1
                                    "00,01,00,00," + // Timestamp 2
                                    "FF,FF,FF,FE," + // Timestamp 3
                                    "00,00",
                            ',', 16
                    );

    public static Packet getStatusPacket(int status, Icq icq) throws JimmException {
        //setClientId(icq);
        int prefix = (icq.getProfile().getBoolean(Profile.OPTION_WEBAWARE)) ? 0x10010000 : 0x10000000;
        Util.putDWord(CLI_SETSTATUS_DATA, 4, prefix | status);
        return (new SnacPacket(SnacPacket.CLI_SETSTATUS_FAMILY, SnacPacket.CLI_SETSTATUS_COMMAND, 0, new byte[0], CLI_SETSTATUS_DATA));
    }

/*	public static void getAvatar(String uin) throws JimmException {
		byte[] uinRaw = Util.stringToByteArray(uin);
		byte[] buf = new byte[1+uinRaw.length+1+2+1+1+16];
		int marker = 0;
		Util.putByte(buf, marker, uin.length()); marker++;
		System.arraycopy(uinRaw, 0, buf, marker, uinRaw.length); marker+=uinRaw.length;
		Util.putByte(buf, marker, 1); marker++;
		Util.putWord(buf, marker, 0x0001); marker+=2;
		Util.putByte(buf, marker, 1); marker++;
		Util.putByte(buf, marker, (byte)10); marker++;
		System.arraycopy(new byte[16], 0, buf, marker, 16);
		icq.sendPacket(new SnacPacket(0x0010, 0x0006, 0, new byte[0], buf));
		//Alert a = new Alert("See!", "Avatar request sent", null, null);
		//a.setTimeout(a.FOREVER);
		//jimm.Jimm.setDisplay(a);
	}*/
}
