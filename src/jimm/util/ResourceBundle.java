/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/jimm/util/ResourceBundle.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Artyomov Denis, Rishat Shamsutdinov
 *******************************************************************************/

package jimm.util;

import jimm.comm.Util;

import java.io.*;

public class ResourceBundle {

    public static String[] langAvailable;
    static private int keys[][]; // [index, len][number]
    static private byte res[]; // + ~50000 free bytes
    //static private Hashtable resources = null;
    static private int keyse[][];
    static private byte rese[]; // + 8168 free bytes

    static {
        try {
            String string = Util.getStringAsStream("/langlist.lng");
            if (string.length() == 0) {
                langAvailable = new String[]{""};
            }
            langAvailable = Util.explode(string, ',');
        } catch (Exception ignored) {
        }
    }

    private static String currUiLanguage = ResourceBundle.langAvailable[0];

    public static String getCurrUiLanguage() {
        return (ResourceBundle.currUiLanguage);
    }

    public static void setCurrUiLanguage(String currUiLanguage) {
        if (ResourceBundle.currUiLanguage.equals(currUiLanguage)) {
            return;
        }
        for (int i = 0; i < ResourceBundle.langAvailable.length; i++) {
            if (ResourceBundle.langAvailable[i].equals(currUiLanguage)) {
                ResourceBundle.currUiLanguage = currUiLanguage;
                loadLang();
                return;
            }
        }
    }

//    static public void pre(OutputStream os) throws IOException {
//        String[] string = Util.explode(Util.getStringAsStream("/RU.lang"), '\n');
//        for (int i = 0; i < string.length; i++) {
//            //System.out.println(string[i]);
//            try {
//                //System.out.println("1 = " + string[i].indexOf('"', 1));
//                //System.out.println("2 = " + string[i].indexOf('"', 2));
//                //System.out.println("3 = " + string[i].indexOf('"', 3));
//                //System.out.println("4 = " + string[i].indexOf('"', 4));
//                int idx1 = string[i].indexOf('"', 2);
//                int idx2 = string[i].indexOf('"', 3);
//                String tS = string[i].substring(idx1 + 1, idx2) + '\n';
//                System.out.println(tS);
//                //os.write(Util.stringToByteArray(tS, false));
//            } catch (Exception e) {
//
//            }
//        }
//    }

    static private void loadLang() {
        //InputStream istream;
        try {
            //resources = new Hashtable();
            //istream = resources.getClass().getResourceAsStream("/" + ResourceBundle.currUiLanguage + ".lng");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baosError = new ByteArrayOutputStream();
            String string = Util.getStringAsStream("/" + ResourceBundle.currUiLanguage + ".lng");
            StringBuffer key = new StringBuffer();
            StringBuffer text = new StringBuffer();
            byte[] lngStr;
            int len;
            int bPos = 0;
            int bPose = 0;
            int idx;
            byte state = 0;
            int start;
            char chr;
            for (int i = 0; ; i++) {
                chr = string.charAt(i);
                if (chr == '|') {
                    keys = new int[2][Integer.parseInt(key.toString())];
                    key.setLength(0);
                    continue;
                }
                if (chr == '\n') {
                    keyse = new int[2][Integer.parseInt(key.toString())];
                    key.setLength(0);
                    start = i + 1;
                    break;
                }
                key.append(chr);
            }

            for (int i = start; i < string.length(); i++) {
                chr = string.charAt(i);
                if (chr == '=') {
                    state = 1;
                    continue;
                } else if (chr == '\n') {
                    lngStr = Util.stringToByteArray(text.toString(), true);
                    //System.out.println("char=" + i + " key=" + key.toString() + " text=" + text.toString());
                    try {
                        idx = Integer.parseInt(key.toString());
                        len = lngStr.length;
                        baos.write(lngStr, 0, len);
                        keys[0][idx] = bPos;
                        keys[1][idx] = len;
                        bPos += len;
                    } catch (NumberFormatException nfe) {
                        try {
                            idx = Integer.parseInt(key.toString().substring(key.length() - 3));
                            idx -= 100;
                            len = lngStr.length;
                            baosError.write(lngStr, 0, len);
                            keyse[0][idx] = bPose;
                            keyse[1][idx] = len;
                            bPose += len;
                        } catch (Exception ignored) {
                        }
                    }
                    state = 0;
                    key.setLength(0);
                    text.setLength(0);
                    continue;
                }

                if (state == 0) {
                    key.append(chr);
                } else if (state == 1) {
                    text.append(chr);
                }
            }
            res = baos.toByteArray();
            baos.close();
            rese = baosError.toByteArray();
            baosError.close();
        } catch (Exception ignored) {
        }
    }

//    private static Object getKey(String key) {
//        return key;
//    }
//
//    public static synchronized String getString(String key) {
//        Object o = getKey(key);
//        if (null == o) return null;
//        if (null == resources) loadLang();
//        if (null == resources) return key;
//        byte[] value = (byte[]) resources.get(o);
//        return (null == value) ? key : Util.byteArrayToString(value, true);
//    }

    public static synchronized String getString(String skey) {
        int key;
        try {
            key = Integer.parseInt(skey);
        } catch (NumberFormatException e) {
            System.out.println("#NFE " + skey);
            return skey;
        } catch (NullPointerException e) {
            return "null";
        }
        if (null == res) loadLang();
        if (null == res) return skey;
        try {
            int bPos = keys[0][key];
            int len = keys[1][key];
            return Util.byteArrayToString(res, bPos, len, true);
        } catch (Exception ignored) {
        }
        return Integer.toString(key);
    }

//    public static synchronized String getError(String key) {
//        if (null == key) return null;
//        if (null == resources) return key;
//        byte[] value = (byte[]) resources.get(key);
//        return (null == value) ? key : Util.byteArrayToString(value, true);
//    }

    public static synchronized String getError(String keyl) {
        int key;
        try {
            key = Integer.parseInt(keyl.substring(keyl.length() - 3));
            key -= 100;
        } catch (Exception e) {
            return keyl;
        }
        if (rese == null) {
            return keyl;
        }
        try {
            int bPos = keyse[0][key];
            int len = keyse[1][key];
            return Util.byteArrayToString(rese, bPos, len, true);
        } catch (Exception ignored) {
        }
        return keyl;
    }

    public static String getEllipsisString(String key) {
        return getString(key) + "...";
    }

    public static String getCRLFString(String key) {
        StringBuffer sb = new StringBuffer(getString(key));
        char chr;
        for (int i = 0; i < sb.length(); i++) {
            chr = sb.charAt(i);
            if (chr == '\t') {
                sb.setCharAt(i, '\n');
            }
        }
        return sb.toString();
    }

}