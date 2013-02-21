//package com.tomclaw.xmlgear;
//
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//
//
//import java.io.ByteArrayInputStream;
//import java.io.DataInputStream;
//import java.io.IOException;
//
//
///**
// *
// * @author Игорь
// */
//public class EncodingUtil {
//
//    static byte[] UTF8_dual = new byte[]{
//        (byte) 0xD0, (byte) 0x90, (byte) 0xD0, (byte) 0x91,
//        (byte) 0xD0, (byte) 0x92, (byte) 0xD0, (byte) 0x93,
//        (byte) 0xD0, (byte) 0x94, (byte) 0xD0, (byte) 0x95,
//        (byte) 0xD0, (byte) 0x81, (byte) 0xD0, (byte) 0x96,
//        (byte) 0xD0, (byte) 0x97, (byte) 0xD0, (byte) 0x98,
//        (byte) 0xD0, (byte) 0x99, (byte) 0xD0, (byte) 0x9A,
//        (byte) 0xD0, (byte) 0x9B, (byte) 0xD0, (byte) 0x9C,
//        (byte) 0xD0, (byte) 0x9D, (byte) 0xD0, (byte) 0x9E,
//        (byte) 0xD0, (byte) 0x9F, (byte) 0xD0, (byte) 0xA0,
//        (byte) 0xD0, (byte) 0xA1, (byte) 0xD0, (byte) 0xA2,
//        (byte) 0xD0, (byte) 0xA3, (byte) 0xD0, (byte) 0xA4,
//        (byte) 0xD0, (byte) 0xA5, (byte) 0xD0, (byte) 0xA6,
//        (byte) 0xD0, (byte) 0xA7, (byte) 0xD0, (byte) 0xA8,
//        (byte) 0xD0, (byte) 0xA9, (byte) 0xD0, (byte) 0xAA,
//        (byte) 0xD0, (byte) 0xAB, (byte) 0xD0, (byte) 0xAC,
//        (byte) 0xD0, (byte) 0xAD, (byte) 0xD0, (byte) 0xAE,
//        (byte) 0xD0, (byte) 0xAF,
//        (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xB1,
//        (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xB3,
//        (byte) 0xD0, (byte) 0xB4, (byte) 0xD0, (byte) 0xB5,
//        (byte) 0xD1, (byte) 0x91, (byte) 0xD0, (byte) 0xB6,
//        (byte) 0xD0, (byte) 0xB7, (byte) 0xD0, (byte) 0xB8,
//        (byte) 0xD0, (byte) 0xB9, (byte) 0xD0, (byte) 0xBA,
//        (byte) 0xD0, (byte) 0xBB, (byte) 0xD0, (byte) 0xBC,
//        (byte) 0xD0, (byte) 0xBD, (byte) 0xD0, (byte) 0xBE,
//        (byte) 0xD0, (byte) 0xBF, (byte) 0xD1, (byte) 0x80,
//        (byte) 0xD1, (byte) 0x81, (byte) 0xD1, (byte) 0x82,
//        (byte) 0xD1, (byte) 0x83, (byte) 0xD1, (byte) 0x84,
//        (byte) 0xD1, (byte) 0x85, (byte) 0xD1, (byte) 0x86,
//        (byte) 0xD1, (byte) 0x87, (byte) 0xD1, (byte) 0x88,
//        (byte) 0xD1, (byte) 0x89, (byte) 0xD1, (byte) 0x8A,
//        (byte) 0xD1, (byte) 0x8B, (byte) 0xD1, (byte) 0x8C,
//        (byte) 0xD1, (byte) 0x8D, (byte) 0xD1, (byte) 0x8E,
//        (byte) 0xD1, (byte) 0x8F};
//    static String[] ANSI_translit = new String[]{
//        "A", "B", "V", "G",
//        "D", "E", "Jo", "Zh",
//        "Z", "I", "J", "K",
//        "L", "M", "N", "O",
//        "P", "R", "S", "T",
//        "U", "F", "H", "C",
//        "Ch", "Sh", "W", "##",
//        "Y", "\"", "Je", "Ju",
//        "Ja", "a", "b", "v",
//        "g", "d", "e", "jo",
//        "zh", "z", "i", "j",
//        "k", "l", "m", "n",
//        "o", "p", "r", "s",
//        "t", "u", "f", "h",
//        "c", "ch", "sh", "w",
//        "#", "y", "\"", "je",
//        "ju", "ja"
//    };
//    static String[] ANSI_single = new String[]{
//        "А", "Б", "В", "Г",
//        "Д", "Е", "Ё", "Ж",
//        "З", "И", "Й", "К",
//        "Л", "М", "Н", "О",
//        "П", "Р", "С", "Т",
//        "У", "Ф", "Х", "Ц",
//        "Ч", "Ш", "Щ", "Ъ",
//        "Ы", "Ь", "Э", "Ю",
//        "Я", "а", "б", "в",
//        "г", "д", "е", "ё",
//        "ж", "з", "и", "й",
//        "к", "л", "м", "н",
//        "о", "п", "р", "с",
//        "т", "у", "ф", "х",
//        "ц", "ч", "ш", "щ",
//        "ъ", "ы", "ь", "э",
//        "ю", "я"
//    };
//
//    public EncodingUtil() {
//    }
//
//    public static String getUTF8_OE(String coded) {
//        String decoded = new String();
//        char b_ch0 = 0;
//        char b_ch1 = 0;
//        boolean isFound = false;
//        for (int c = 0; c < coded.length() - 1; c++) {
//            b_ch0 = coded.charAt(c);
//            b_ch1 = coded.charAt(c + 1);
//            isFound = false;
//            for (int i = 0; i < UTF8_dual.length - 1; i += 2) {
//                if (UTF8_dual[i] == (byte) b_ch0 &&
//                        UTF8_dual[i + 1] == (byte) b_ch1) {
//                    //System.out.println((byte)b_ch0 + "; " + (byte)b_ch1);
//                    //System.out.println("> " + UTF8_dual[i] + "; " + UTF8_dual[i+1]);
//                    decoded += ANSI_single[i / 2];
//                    c += 1;
//                    //System.out.println(decoded);
//                    isFound = true;
//                    break;
//                }
//            }
//            if (!isFound) {
//                decoded += String.valueOf(b_ch0);
//            }
//        }
//        //System.out.println(decoded);
//        return decoded;
//
//    }
//
//    public static String getUTF8(String buf_string) {
//        buf_string = replace(buf_string, "Р�", String.valueOf((char) 0));
//        try {
//            byte[] buf = buf_string.getBytes();
//            int len = buf.length;
//            int off = 0;
//            byte[] buf2 = new byte[len + 2];
//            util_put16(buf2, 0, len);
//            System.arraycopy(buf, off, buf2, 2, len);
//            ByteArrayInputStream bais = new ByteArrayInputStream(buf2);
//            DataInputStream dis = new DataInputStream(bais);
//            buf_string = dis.readUTF();
//            buf_string = replace(buf_string, String.valueOf((char) 0), "И");
//        } catch (Exception ex) {
//            //Do nothing
//            System.out.println(ex.getMessage());
//        }
//        return buf_string;
//    }
//
//    public static String toUTF8(String buf_string) {
//        String utf8 = new String();
//        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
//        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
//        try {
//            dos.writeUTF(buf_string);
//            utf8 = new String(baos.toByteArray()).substring(2);
//        } catch (IOException ex) {
//            utf8 = buf_string;
//        }
//
//        return utf8;
//    }
//
//    public static String replace(String replex, String regex, String repex) {
//        replex = " " + replex + " ";
//        for (int c = 0; c < replex.length() - regex.length(); c++) {
//            if (replex.substring(c, c + regex.length()).hashCode() ==
//                    regex.hashCode()) {
//                replex = replex.substring(0, c) + repex + replex.substring(c + regex.length(), replex.length());
//                c -= regex.length() - repex.length();
//            }
//        }
//        replex = replex.substring(1, replex.length() - 1);
//        return replex;
//    }
//
//    public static int util_put16(byte[] buf, int offset, int a) {
//        buf[offset] = (byte) ((a >> 8) & 0xff);
//        buf[++offset] = (byte) (a & 0xff);
//        return 2;
//    }
//
//    public static String outBytes(String data) {
//        String output = new String();
//        for (int i = 0; i < data.length(); i++) {
//            output += String.valueOf((int) data.charAt(i)) + "; ";
//        }
//        return output;
//    }
//}
