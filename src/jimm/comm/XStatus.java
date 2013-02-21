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
 File: src/jimm/comm/XStatus.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): aspro
 *******************************************************************************/

package jimm.comm;

import DrawControls.Icon;
import DrawControls.ImageList;
import jimm.util.ResourceBundle;
import jimm.util.Device;

import com.tomclaw.xmlgear.XMLGear;
import com.tomclaw.xmlgear.XMLItem;

public class XStatus {


    public static void init() {
        String content = Util.removeCr(Util.getStringAsStream("/xstatuses.xml"));
        try {
            if (content.length() == 0) {
                throw new Exception();
            }
            XMLGear xg = new XMLGear();
            xg.setStructure(content);
            XMLItem[] XSItem = xg.getItemsWithHeader(new String[]{"container"}, "item");
            if (XSItem == null) {
                throw new Exception();
            }
            int mood, len = XSItem.length;
            xguids = new GUID[len];
            moodToNormal = new int[len];
            XSTATUS_NONE = len + 1;
            for (int i = 0; i < len; i++) {
                XMLItem xi = XSItem[i];
                xguids[i] = new GUID(Util.explodeToBytesLine(xi.getParamValue("guid")));
                mood = -1;
                try {
                    mood = Integer.parseInt(xi.getParamValue("mood"));
                } catch (Exception ignored) {
                }
                moodToNormal[i] = mood;
            }
            content = Util.removeCr(Util.getStringAsStream("/xstatuses_string.txt"));
            if (content.length() == 0) {
                xstatus = new String[0];
                return;
            }
            String[] names = Util.explode(content, '\n');
            content = null;
            xstatus = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                xstatus[i] = extract(names[i]);
            }
        } catch (Throwable e) {
            XSTATUS_NONE = 1;
            xguids = new GUID[0];
            moodToNormal = new int[0];
            xstatus = new String[0];
        }
    }

    static String extract(String src) {
        char chr;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < src.length(); i++) {
            chr = src.charAt(i);
            if (chr >= ' ') sb.append(chr);
        }
        return sb.toString();
    }

    public static int XSTATUS_NONE;
    private static GUID[] xguids;
    private static int[] moodToNormal;
    private static String[] xstatus;
    private static ImageList imageList = ImageList.loadFull("xstatus.png");

    public XStatus() {
        index = -1;
    }

    public static int toNormal(int mood) {
        if (mood == -1) {
            return mood;
        }
        for (int i = moodToNormal.length - 1; i >= 0; i--) {
            if (moodToNormal[i] == mood) {
                return i;
            }
        }
        return -1;
    }

    public static int toMood(int xst) {
        if (xst < 0 || xst >= moodToNormal.length) {
            return -1;
        }
        return moodToNormal[xst];
    }

    public static void updateIcons() {
        imageList = ImageList.loadFull("xstatus.png");
    }

    private static int getXStatus(byte[] guid) {
        try {
            for (int i = 0; i < xguids.length; i++) {
                if (xguids[i].equals(guid)) {
                    return i;
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void setXStatus(byte[] guids) {
        index = -1;
        if (guids == null) {
            return;
        }

        byte guid[] = new byte[16];
        for (int i = 0; (i < guids.length) && (index == -1); i += 16) {
            System.arraycopy(guids, i, guid, 0, 16);
            index = getXStatus(guid);
            //System.out.println(Util.byteArrayToHexString(guid) + " as " + index);
        }
    }

    private int index;

    public static GUID getStatusGUID(int index) {
        if (index >= 0 & index < xguids.length) {
            try {
                return xguids[index];
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static Icon getStatusImage(int index) {
        if (index <= XSTATUS_NONE) {
            return imageList.elementAt(index);
        }
        return null;
    }

    public static int getStatusIndex(int index) {
        if (index >= 0) {
            return index;
        }
        return -1;
    }

    public static String getStatusAsString(int index) {
        if (index >= 0 && index < xstatus.length) {
            try {
                return xstatus[index];
            } catch (Exception ignored) {
            }
//            try {
//                return ResourceBundle.getString(xstatus[index]);
//            } catch (Exception ignored) {
//            }
        }
        return ResourceBundle.getString("xstatus_none");
    }

    public static int getXStatusCount() {
        return xguids.length;
    }

    public GUID getStatusGUID() {
        return getStatusGUID(index);
    }

    public Icon getStatusImage() {
        return getStatusImage(index);
    }

    public Icon getIcon(int mood) {
        Icon icon = getStatusImage();
        if (icon == null) {
            return imageList.elementAt(mood);
        }
        return icon;
    }

    public int getStatusIndex() {
        return getStatusIndex(index);
    }

    public String getXStatusAsString() {
        return getStatusAsString(index);
    }

    public static ImageList getXStatusImageList() {
        return imageList;
    }
}