package jimm.comm;

import java.util.Vector;

public class Client {

    public static final int METHOD_CAP = 1;
    public static final int METHOD_CAP_DOTS = 2;
    public static final int METHOD_DC_INT = 3;
    public static final int METHOD_DC_NUMS = 4;

    Vector caps = new Vector();// GUIDs
    Vector nocaps = new Vector();// GUIDs
    Vector methods = new Vector();// String

    int type = 0;
    int dc = 0;
    int proto = 0;

    int nameCapHash = -1;
    int index = -1;
    int count = 0;

    int dc1 = 0;
    int dc2 = 0;
    int dc3 = 0;

    String ClientName = "";

    int onImageList = -1;

    Client() {
    }

    public String getFullVersion(byte[] cap, int dwFP1, int dwFP2, int dwFP3) {
        String version = getVersion(cap, dwFP1, dwFP2, dwFP3);
        //System.out.println("ClientName = " + ClientName + " version = " + version);
        if (ClientName.indexOf("%") != -1) {
            return Util.replaceStr(ClientName, "%", version);
        }
        return ClientName + " " + version;
    }   

    public String getVersion(byte[] cap, int dwFP1, int dwFP2, int dwFP3) {
        //System.out.println("ClientName = " + ClientName + "& method = " + type);
        StringBuffer sb = new StringBuffer();
        switch (type) {
            case METHOD_CAP_DOTS:
                for (int i = 0; i < ClientID.GUIDBased.size(); i++) {
                    GUIDs guids = (GUIDs) ClientID.GUIDBased.elementAt(i);
                    if (guids.name.hashCode() == nameCapHash) {
                        int counts = cap.length / 16, j16, len;
                        GUID guid = guids.value;
                        len = guid.toByteArray().length;
                        for (int j = 0; j < counts; j++) {
                            j16 = j * 16;
                            if (guid.equals(cap, j16, len)) {
                                if (index < 0) index = 0;
                                if (count <= 0) count = 16;
                                byte[] buf = new byte[16];
                                System.arraycopy(cap, j16, buf, 0, 16);
                                for (int v = index; v < index + count; v++) {
                                    if (buf[v] > 0) {
                                        if (sb.length() > 0) {
                                            sb.append(".");
                                        }
                                        sb.append(buf[v]);
                                        return sb.toString();
                                    }
                                }
                            }
                        }
                        return "";
                    }
                }
                break;

            case METHOD_CAP:
                for (int i = 0; i < ClientID.GUIDBased.size(); i++) {
                    GUIDs guids = (GUIDs) ClientID.GUIDBased.elementAt(i);
                    if (guids.name.hashCode() == nameCapHash) {
                        int counts = cap.length / 16, j16, len;
                        GUID guid = guids.value;
                        len = guid.toByteArray().length;
                        for (int j = 0; j < counts; j++) {
                            j16 = j * 16;
                            if (guid.equals(cap, j16, len)) {
                                if (index < 0) index = 0;
                                byte[] buf = new byte[16];
                                System.arraycopy(cap, j16, buf, 0, 16);
                                return Util.byteArrayToString(buf, index, 16 - index);
                            }
                        }
                        return "";
                    }
                }
                break;

            case METHOD_DC_INT:
                int dct;
                switch (dc) {
                    case 1:
                        dct = dwFP1;
                        break;
                    case 2:
                        dct = dwFP2;
                        break;
                    case 3:
                        dct = dwFP3;
                        break;
                    default:
                        return "";
                }
                //System.out.println("ClientName = " + ClientName + "& method = METHOD_DC_INT dc = " + dc + " for" + dct);
                sb.append(dct & 0xFFFF);
                return sb.toString();


            case METHOD_DC_NUMS:
                int dct0;
                switch (dc) {
                    case 1:
                        dct0 = dwFP1;
                        break;
                    case 2:
                        dct0 = dwFP2;
                        break;
                    case 3:
                        dct0 = dwFP3;
                        break;
                    default:
                        return "";
                }
                // System.out.println("ClientName = " + ClientName + "& method = METHOD_DC_NUMS dc = " + dc + " for" + dct0);
                sb.append((dct0 >> 24) & 0xFF).append((dct0 >> 16) & 0xFF).append((dct0 >> 8) & 0xFF).append(dct0 & 0xFF);
                return sb.toString();
        }
        return "";
    }
}
