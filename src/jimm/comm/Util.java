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
 File: src/jimm/comm/Util.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Sergey Chernov, Andrey B. Ivlev
 *******************************************************************************/

package jimm.comm;

import jimm.ContactItem;
import jimm.GroupItem;
import jimm.Options;
import jimm.util.ResourceBundle;

import java.io.*;
import java.util.Random;
import java.util.Vector;

public class Util {
    // Arrays for new capability blowup
    private static final byte[] CAP_OLD_HEAD = explodeToBytes("09,46", ',', 16);
    private static final byte[] CAP_OLD_TAIL = explodeToBytes("4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
    // Password encryption key
    public static final byte[] PASSENC_KEY = explodeToBytes("F3,26,81,C4,39,86,DB,92,71,A3,B9,E6,53,7A,95,7C", ',', 16);

    // Online status (set values)
    public static final int SET_STATUS_AWAY = 0x0001;
    public static final int SET_STATUS_DND = 0x0013;
    public static final int SET_STATUS_NA = 0x0005;
    public static final int SET_STATUS_OCCUPIED = 0x0011;
    public static final int SET_STATUS_CHAT = 0x0020;
    public static final int SET_STATUS_INVISIBLE = 0x0100;
    public static final int SET_STATUS_EVIL = 0x3000;
    public static final int SET_STATUS_DEPRESSION = 0x4000;
    public static final int SET_STATUS_HOME = 0x5000;
    public static final int SET_STATUS_WORK = 0x6000;
    public static final int SET_STATUS_LUNCH = 0x2001;
    public static final int SET_STATUS_ONLINE = 0x0000;

    // Counter variable
    private static int counter = 0;

    public synchronized static int getCounter() {
        counter++;
        return (counter);
    }

    public static String toHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            //	look up high nibble char
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);

            //	look up low nibble char
            sb.append(hexChar[b[i] & 0x0f]);
            sb.append(" ");
            if ((i != 0) && ((i % 15) == 0)) sb.append("\n");
        }
        return sb.toString();
    }

    //	table to convert a nibble to a hex char.
    private static char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    // Extracts the byte from the buffer (buf) at position off
    public static int getByte(byte[] buf, int off) {
        int val;
        val = ((int) buf[off]) & 0x000000FF;
        return (val);
    }
//	static public boolean getByte(DataInputStream stream) throws IOException
//	{
//		return (((int)stream.readByte()) == 1);
//	}

    // Puts the specified byte (val) into the buffer (buf) at position off

    public static void putByte(byte[] buf, int off, int val) {
        buf[off] = (byte) (val & 0x000000FF);
    }

    // Extracts the word from the buffer (buf) at position off using the specified byte ordering (bigEndian)
    public static int getWord(byte[] buf, int off, boolean bigEndian) {
        int val;
        if (bigEndian) {
            val = (((int) buf[off]) << 8) & 0x0000FF00;
            val |= (((int) buf[++off])) & 0x000000FF;
        } else   // Little endian
        {
            val = (((int) buf[off])) & 0x000000FF;
            val |= (((int) buf[++off]) << 8) & 0x0000FF00;
        }
        return (val);
    }

    static public DataInputStream getDataInputStream(byte[] array, int offset) {
        return new DataInputStream(new ByteArrayInputStream(array, offset, array.length - offset));
    }

    static public int getWord(DataInputStream stream, boolean bigEndian) throws IOException {
        return bigEndian ? stream.readUnsignedShort() : ((int) stream.readByte() & 0x00FF) | (((int) stream.readByte() << 8) & 0xFF00);
    }

    static public String readAsciiz(DataInputStream stream) throws IOException {
        int len = Util.getWord(stream, false);
        if (len == 0) return "";
        byte[] buffer = new byte[len];
        stream.readFully(buffer);
        return Util.byteArrayToString(buffer);
    }

    static public void writeWord(ByteArrayOutputStream stream, int value, boolean bigEndian) {
        if (bigEndian) {
            stream.write(((value & 0xFF00) >> 8) & 0xFF);
            stream.write(value & 0xFF);
        } else {
            stream.write(value & 0xFF);
            stream.write(((value & 0xFF00) >> 8) & 0xFF);
        }
    }

    static public void writeByteArray(ByteArrayOutputStream stream, byte[] array) {
        try {
            stream.write(array);
        }
        catch (Exception e) {
            //System.out.println("Util.writeByteArray: " + e.toString());
        }
    }

    static public void writeDWord(ByteArrayOutputStream stream, int value, boolean bigEndian) {
        if (bigEndian) {
            stream.write(((value & 0xFF000000) >> 24) & 0xFF);
            stream.write(((value & 0xFF0000) >> 16) & 0xFF);
            stream.write(((value & 0xFF00) >> 8) & 0xFF);
            stream.write(value & 0xFF);
        } else {
            stream.write(value & 0xFF);
            stream.write(((value & 0xFF00) >> 8) & 0xFF);
            stream.write(((value & 0xFF0000) >> 16) & 0xFF);
            stream.write(((value & 0xFF000000) >> 24) & 0xFF);
        }
    }

    static public void writeByte(ByteArrayOutputStream stream, int value) {
        stream.write(value);
    }

    static public void writeLenAndString(ByteArrayOutputStream stream, String value, boolean utf8) {
        byte[] raw = Util.stringToByteArray(value, utf8);
        writeWord(stream, raw.length, true);
        stream.write(raw, 0, raw.length);
    }

    static public void writeLenLEAndStringAsciiz(ByteArrayOutputStream stream, String value) {
        byte[] raw = Util.stringToByteArray(value, false);
        writeWord(stream, raw.length + 1, false);
        writeByteArray(stream, raw);
        writeByte(stream, 0);
    }

    static public void writeAsciizTLV(int type, ByteArrayOutputStream stream, String value, boolean bigEndian) {
        writeWord(stream, type, bigEndian);
        byte[] raw = Util.stringToByteArray(value);
        writeWord(stream, raw.length + 3, false);
        writeWord(stream, raw.length + 1, false);
        stream.write(raw, 0, raw.length);
        stream.write(0);
    }

    static public void writeAsciizTLV(int type, ByteArrayOutputStream stream, String value) {
        writeAsciizTLV(type, stream, value, true);
    }

    static public void writeAsciizTLVandWord(int type, ByteArrayOutputStream stream, String value, int _word, boolean bigEndian) {
        writeWord(stream, type, bigEndian); // TLV.Type
        byte[] raw = Util.stringToByteArray(value); // строка в виде байтового массива
        writeWord(stream, raw.length + 5, false); // TLV.Length
        writeWord(stream, _word, false); // категория
        writeWord(stream, raw.length + 1, false); // длина строки + 1 (длина нуля)
        stream.write(raw, 0, raw.length); // строка
        stream.write(0);
    }

    static public void writeTLV(int type, ByteArrayOutputStream stream, ByteArrayOutputStream data) {
        writeTLV(type, stream, data, true);
    }

    static public void writeTLV(int type, ByteArrayOutputStream stream, ByteArrayOutputStream data, boolean bigEndian) {
        byte[] raw = data.toByteArray();
        writeWord(stream, type, bigEndian);
        writeWord(stream, raw.length, false);
        stream.write(raw, 0, raw.length);
    }

    // Extracts the word from the buffer (buf) at position off using big endian byte ordering
    public static int getWord(byte[] buf, int off) {
        return (Util.getWord(buf, off, true));
    }

    // Puts the specified word (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
    public static void putWord(byte[] buf, int off, int val, boolean bigEndian) {
        if (bigEndian) {
            buf[off] = (byte) ((val >> 8) & 0xFF);
            buf[++off] = (byte) ((val) & 0xFF);
        } else { // Little endian
            buf[off] = (byte) ((val) & 0xFF);
            buf[++off] = (byte) ((val >> 8) & 0xFF);
        }
    }

    // Puts the specified word (val) into the buffer (buf) at position off using big endian byte ordering
    public static void putWord(byte[] buf, int off, int val) {
        Util.putWord(buf, off, val, true);
    }

    // Extracts the double from the buffer (buf) at position off using the specified byte ordering (bigEndian)
    public static long getDWord(byte[] buf, int off, boolean bigEndian) {
        long val;
        if (bigEndian) {
            val = (((long) buf[off]) << 24) & 0xFF000000;
            val |= (((long) buf[++off]) << 16) & 0x00FF0000;
            val |= (((long) buf[++off]) << 8) & 0x0000FF00;
            val |= (((long) buf[++off])) & 0x000000FF;
        } else { // Little endian
            val = (((long) buf[off])) & 0x000000FF;
            val |= (((long) buf[++off]) << 8) & 0x0000FF00;
            val |= (((long) buf[++off]) << 16) & 0x00FF0000;
            val |= (((long) buf[++off]) << 24) & 0xFF000000;
        }
        return (val);
    }

    // Extracts the double from the buffer (buf) at position off using big endian byte ordering
    public static long getDWord(byte[] buf, int off) {
        return (Util.getDWord(buf, off, true));
    }

    // Puts the specified double (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
    public static void putDWord(byte[] buf, int off, long val, boolean bigEndian) {
        if (bigEndian) {
            buf[off] = (byte) ((val >> 24) & 0x00000000000000FF);
            buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
            buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
            buf[++off] = (byte) ((val) & 0x00000000000000FF);
        } else { // Little endian
            buf[off] = (byte) ((val) & 0x00000000000000FF);
            buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
            buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
            buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
        }
    }

    // Puts the specified double (val) into the buffer (buf) at position off using big endian byte ordering
    public static void putDWord(byte[] buf, int off, long val) {
        Util.putDWord(buf, off, val, true);
    }

    // getTlv(byte[] buf, int off) => byte[]
    public static byte[] getTlv(byte[] buf, int off) {
        if (off + 4 > buf.length) return (null);   // Length check (#1)
        int length = Util.getWord(buf, off + 2);
        if (off + 4 + length > buf.length) return (null);   // Length check (#2)
        byte[] value = new byte[length];
        System.arraycopy(buf, off + 4, value, 0, length);
        return (value);
    }

    public static String getStringAsStream(String s) {
        InputStream input = (new Object()).getClass().getResourceAsStream(s);
        if (input == null) {
            return "";
        }
        byte abyte0[] = null;
        try {
            DataInputStream datainputstream = new DataInputStream(input);
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            int i;
            while ((i = datainputstream.read()) != -1) {
                bytearrayoutputstream.write(i);
            }
            abyte0 = bytearrayoutputstream.toByteArray();
            bytearrayoutputstream.close();
            datainputstream.close();
            //input.close();
        } catch (Exception ignored) {
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
        if (abyte0 != null) {
            return byteArrayToString(abyte0, isDataUTF8(abyte0)).trim();
        }
        return "";
    }

    // Extracts a string from the buffer (buf) starting at position off, ending at position off+len

    public static String byteArrayToString(byte[] buf, int off, int len, boolean utf8) {
        if (buf == null) {
            return null;
        }
        // Length check
        if (buf.length < off + len) {
            return null;
        }

        // Remove \0's at the end
        while ((len > 0) && (buf[off + len - 1] == 0x00)) {
            len--;
        }

        // Read string in UCS-2BE format
        if (isDataUCS2(buf, off, len)) {
            return (ucs2beByteArrayToString(buf, off, len));
        }

        // Read string in UTF-8 format
        if (utf8) {
            return utf8beByteArrayToString(buf, off, len);
        }

        // CP1251 or default character encoding?
        if (Options.getBoolean(Options.OPTION_CP1251_HACK)) {
            return (byteArray1251ToString(buf, off, len));
        }
        return (new String(buf, off, len));
    }


    // Extracts a string from the buffer (buf) starting at position off, ending at position off+len
    public static String byteArrayToString(byte[] buf, int off, int len) {
        return (Util.byteArrayToString(buf, off, len, false));
    }

    // Converts the specified buffer (buf) to a string
    public static String byteArrayToString(byte[] buf, boolean utf8) {
        return (Util.byteArrayToString(buf, 0, buf.length, utf8));
    }

    // Converts the specified buffer (buf) to a string
    public static String byteArrayToString(byte[] buf) {
        return (Util.byteArrayToString(buf, 0, buf.length, false));
    }

    // Converts the specific 4 byte max buffer to an unsigned long
    public static long byteArrayToLong(byte[] b) {
        long l = 0;
        l |= b[0] & 0xFF;
        l <<= 8;
        l |= b[1] & 0xFF;
        l <<= 8;
        if (b.length > 3) {
            l |= b[2] & 0xFF;
            l <<= 8;
            l |= b[3] & 0xFF;
        }
        return l;
    }

    // Converts a byte array to a hex string
    public static String byteArrayToHexString(byte[] buf) {
        StringBuffer hexString = new StringBuffer(buf.length);
        String hex;
        for (int i = 0; i < buf.length; i++) {
            hex = Integer.toHexString(0x0100 + (buf[i] & 0x00FF)).substring(1);
            hexString.append(hex.length() < 2 ? "0" : "").append(hex);
        }
        return hexString.toString();
    }

    // Converts the specified string (val) to a byte array
    public static byte[] stringToByteArray(String val, boolean utf8) {
        // Write string in UTF-8 format
        if (utf8) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeUTF(val);
                byte[] raw = baos.toByteArray();
                dos.close();
                baos.close();
                byte[] result = new byte[raw.length - 2];
                System.arraycopy(raw, 2, result, 0, raw.length - 2);
                return result;
            } catch (Exception e) { // Do nothing
            }
        }

        // CP1251 or default character encoding?
        if (Options.getBoolean(Options.OPTION_CP1251_HACK)) {
            return (stringToByteArray1251(val, false));
        } else {
            return (val.getBytes());
        }

    }

    // Converts the specified string (val) to a byte array
    public static byte[] stringToByteArray(String val) {
        return (Util.stringToByteArray(val, false));
    }

    // Converts the specified string to UCS-2BE
    public static byte[] stringToUcs2beByteArray(String val) {
        byte[] ucs2be = new byte[val.length() * 2];
        for (int i = 0; i < val.length(); i++) {
            Util.putWord(ucs2be, i * 2, (int) val.charAt(i));
        }
        return (ucs2be);
    }

    // Extract a UCS-2BE string from the specified buffer (buf) starting at position off, ending at position off+len
    public static String ucs2beByteArrayToString(byte[] buf, int off, int len) {
        // Length check
        // if ((off + len > buf.length) || (buf.length % 2 != 0)) /* old variant */
        if ((off + len > buf.length) || (len % 2 != 0)) /* aspro variant */ {
            return (null);
        }

        // Convert
        StringBuffer sb = new StringBuffer();
        for (int i = off; i < off + len; i += 2) {
            sb.append((char) Util.getWord(buf, i));
        }
        return (sb.toString());
    }

    public static String utf8beByteArrayToString(byte[] buf, int off, int len) {
        String result = "";
        try {
            byte[] buf2 = new byte[len + 2];
            Util.putWord(buf2, 0, len);
            System.arraycopy(buf, off, buf2, 2, len);
            ByteArrayInputStream bais = new ByteArrayInputStream(buf2);
            DataInputStream dis = new DataInputStream(bais);
            result = dis.readUTF();
            dis.close();
            bais.close();
        } catch (Exception e) {/*Do nothing*/
        }
        return result;
    }

    public static boolean isDataUCS2(byte[] array, int start, int lenght) {
        if ((lenght & 1) != 0) return false;
        int end = start + lenght;
        byte b;
        boolean result = true;
        for (int i = start; i < end; i += 2) {
            b = array[i];
            if (b > 0 && b < 0x09) return true;
            if (b == 0 && array[i + 1] != 0) return true;
            if (b > 0x20 || b < 0x00) result = false;
        }
        return result;
    }

    // Extracts a UCS-2BE string from the specified buffer (buf)
    public static String ucs2beByteArrayToString(byte[] buf) {
        return (Util.ucs2beByteArrayToString(buf, 0, buf.length));
    }

//    public static void showBytes(byte[] data) {
//		StringBuffer buffer1 = new StringBuffer(), buffer2 = new StringBuffer();
//
//		String hex;
//		for (int i = 0; i < data.length; i++) {
//			int charaster = ((int)data[i]) & 0xFF;
//			buffer1.append(charaster < ' ' || charaster >= 128 ? '.' : (char)charaster);
//			hex = Integer.toHexString(((int)data[i]) & 0xFF);
//			buffer2.append(hex.length() == 1 ? "0" + hex : hex);
//			buffer2.append(" ");
//
//			if (((i % 16) == 15) || (i == (data.length - 1))) {
//				while (buffer2.length() < 16 * 3) {
//					buffer2.append(' ');
//				}
//
//				buffer1.setLength(0);
//				buffer2.setLength(0);
//			}
//		}
//	}

    public static boolean normalChar(char chr) {
        return (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') || (chr >= '0' && chr <= '9');
    }

    /* public static String showEasyBytes(byte[] capabilities, int radix, int off, int len) {
        byte[] data = new byte[len];
        System.arraycopy(capabilities, off, data, 0, len);
        StringBuffer buffer = new StringBuffer();
        String hex;
        boolean isUtf = Util.isDataUTF8(data);
        int count = data.length / radix, j16;

        for (int i = 0; i < count; i++) {
                int first = 0;
                j16 = i * radix;

                for (int j = j16; j < j16 + radix; j++) {
                    hex = Integer.toHexString(((int) data[j]) & 0xFF);
                    if (j == j16) {
                        buffer.append('\n');
                    }
                    buffer.append(hex.length() == 1 ? "0" + hex : hex);
                }
        }
        return buffer.toString();
    }*/

    public static String showBytesCompact(byte[] capabilities, int off, int len) {
        byte[] data = new byte[len];
        System.arraycopy(capabilities, off, data, 0, len);

        StringBuffer buffer = new StringBuffer();
        String hex;
        for (int j = 0; j < len; j++) {
            hex = Integer.toHexString(((int) data[j]) & 0xFF);
            buffer.append(hex.length() == 1 ? "0" + hex : hex);
        }
        return buffer.toString();
    }

    public static String showBytes(byte[] capabilities, int radix) {
        return showBytes(capabilities, radix, 0, capabilities.length);
    }

    public static String showBytes(byte[] capabilities, int radix, int off, int len) { // Show bytes
        byte[] data = new byte[len];
        System.arraycopy(capabilities, off, data, 0, len);

        StringBuffer buffer = new StringBuffer();
        String hex;
        String find = "";
        int count = data.length / radix, j16;
        boolean isUtf = Util.isDataUTF8(data);

        for (int i = 0; i < count; i++) {
            int first = 0;
            j16 = i * radix;
            find = Util.byteArrayToString(data, j16, radix, isUtf);

            for (int j = j16; j < j16 + radix; j++) {
                hex = Integer.toHexString(((int) data[j]) & 0xFF);
                if (j == j16) {
                    buffer.append('\n');
                    buffer.append('{');
                }
                buffer.append("0x");
                buffer.append(hex.length() == 1 ? "0" + hex : hex);
                if (j == (j16 + radix) - 1) {
                    buffer.append('}');
                } else {
                    buffer.append(',');
                }
            }
            boolean failed = false;

            for (int j = 0; j < find.length(); j++) {
                char ch = find.charAt(j);
                failed = (ch > '~');
                //if (failed) continue;
            }

            for (int j = 0; j < find.length(); j++) {
                char ch = find.charAt(j);
                //boolean dig = ((ch >= '(' && ch <= '~') || ch == ' ');
                boolean dig = ((ch >= 'A' && ch <= '_')
                        || (ch >= 'a' && ch <= 'z')
                        || (ch >= '0' && ch <= ';')
                        || (ch >= '+' && ch <= '/')
                        || (ch >= ' ' && ch <= '!'));
                switch (first) {
                    case 0:
                        if (!failed) {
                            buffer.append("// {");
                            if (dig) {
                                buffer.append(ch);
                            } else {
                                buffer.append(' ');
                            }
                            first = 1;
                        }
                        break;
                    case 1:
                        if (dig) {
                            buffer.append(ch);
                        } else {
                            buffer.append(' ');
                        }
                        if (j == find.length() - 1) {
                            buffer.append("} ");
                        }
                        break;
                }
                //if (failed) continue;
            }
        }
        find = buffer.toString();
        return find;
    }

    public static String getNumber(String find) {
        try {
            String newUin = "";
            int first = 0;

            for (int i = 0; i < find.length(); i++) {
                char ch = find.charAt(i);
                boolean dig = (ch >= '0' && ch <= '9');
                //boolean digplus = (dig || ((ch == '+') && plus));

                switch (first) {
                    case 0:
                        if (dig) {
                            first = 1;
                            newUin += ch;
                        }
                        break;
                    case 1:
                        if (dig) {
                            newUin += ch;
                        } else {
                            if (newUin.length() < 2) {
                                first = 0;
                                newUin = "";
                            } else first = 2;
                        }
                        break;
                }
            }
            find = newUin;
            long l = Long.parseLong(find);
            if (l < 10) find = "";
        } catch (Exception e) {
            find = "";
        }
        return find;
    }

    public static String getNormalText(byte[] capabilities, int radix) {
        String find = "";
        try {
            StringBuffer str = new StringBuffer();
            int count = capabilities.length / radix, j16;
            boolean isUtf = Util.isDataUTF8(capabilities);
            for (int i = 0; i < count; i++) {
                int first = 0;
                j16 = i * radix;
                find = Util.byteArrayToString(capabilities, j16, radix, isUtf);

                for (int j = 0; j < find.length(); j++) {
                    char ch = find.charAt(j);
                    boolean dig = ((ch >= '(' && ch <= '~') || ch == ' ');
                    switch (first) {
                        case 0:
                            str.append('{');
                            if (dig) {
                                str.append(ch);
                            } else {
                                str.append('*');
                            }
                            first = 1;
                            break;
                        case 1:
                            if (dig) {
                                str.append(ch);
                            } else {
                                str.append('*');
                            }
                            //if ((i % radix) == (radix - 1)) {
                            if (j == find.length() - 1) {
                                str.append("} ");
                            }
                            break;
                    }
                }
            }
            find = str.toString();
        } catch (Exception e) {
            find = "";
        }
        return find;
    }


    // Removes all CR occurences
    public static String removeCr(String val) {
        if (val.indexOf('\r') < 0) {
            return val;
        }
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < val.length(); i++) {
            char chr = val.charAt(i);
            if ((chr == 0) || (chr == '\r')) {
                continue;
            }
            result.append(chr);
        }
        return result.toString();
    }

    // Restores CRLF sequense from LF
    public static String restoreCrLf(String val) {
        StringBuffer result = new StringBuffer();
        int size = val.length();
        for (int i = 0; i < size; i++) {
            char chr = val.charAt(i);
            if (chr == '\r') continue;
            if (chr == '\n') result.append("\r\n");
            else result.append(chr);
        }
        return result.toString();
    }

    public static String removeClRfAndTabs(String val) {
        int len = val.length();
        char[] dst = new char[len];
        for (int i = 0; i < len; i++) {
            char chr = val.charAt(i);
            if ((chr == '\n') || (chr == '\r') || (chr == '\t')) chr = ' ';
            dst[i] = chr;
        }
        return new String(dst, 0, len);
    }

    public static String removeNullChars(String str) {
        if ((str == null) || (str.indexOf('\0') < 0)) {
            return str;
        }
        StringBuffer result = new StringBuffer();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) != '\0') {
                result.append(str.charAt(i));
            }
        }
        return result.toString();
    }

    public static String removeTwChars(String str) {
        if ((str == null) || (str.indexOf(' ') < 0)) {
            return str;
        }
        StringBuffer result = new StringBuffer();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) != ' ') {
                result.append(str.charAt(i));
            }
        }
        return result.toString();
    }

    // DeScramble password
    public static byte[] decipherPassword(byte[] buf) {
        byte[] ret = new byte[buf.length];
        for (int i = 0; i < buf.length; i++) {
            ret[i] = (byte) (buf[i] ^ Util.PASSENC_KEY[i % 16]);
        }
        return (ret);
    }

    // translateStatus(long status) => void
    public static int translateStatusReceived(int status) {
        if ((status & 0x0100) != 0 && (status & 0xFFFF) != 0x0100 && status != 0xFFFFFFFF) status &= 0xFFFFFEFF;

        if (status == ContactItem.STATUS_OFFLINE) return (ContactItem.STATUS_OFFLINE);
        if ((status & ContactItem.STATUS_DND) != 0) return (ContactItem.STATUS_DND);
        if ((status & ContactItem.STATUS_INVISIBLE) != 0) return (ContactItem.STATUS_INVISIBLE);
        if ((status & ContactItem.STATUS_INVIS_ALL) != 0) return (ContactItem.STATUS_INVISIBLE);
        if ((status & ContactItem.STATUS_OCCUPIED) != 0) return (ContactItem.STATUS_OCCUPIED);
        if ((status & ContactItem.STATUS_NA) != 0) return (ContactItem.STATUS_NA);
        if ((status & ContactItem.STATUS_CHAT) != 0) return (ContactItem.STATUS_CHAT);
        if ((status & ContactItem.STATUS_LUNCH) == ContactItem.STATUS_LUNCH) return (ContactItem.STATUS_LUNCH);
        if ((status & ContactItem.STATUS_EVIL) == ContactItem.STATUS_EVIL) return (ContactItem.STATUS_EVIL);
        if ((status & ContactItem.STATUS_HOME) == ContactItem.STATUS_HOME) return (ContactItem.STATUS_HOME);
        if ((status & ContactItem.STATUS_WORK) == ContactItem.STATUS_WORK) return (ContactItem.STATUS_WORK);
        if ((status & ContactItem.STATUS_AWAY) == ContactItem.STATUS_AWAY) return (ContactItem.STATUS_AWAY);
        if ((status & ContactItem.STATUS_DEPRESSION) == ContactItem.STATUS_DEPRESSION)
            return (ContactItem.STATUS_DEPRESSION);
        return (ContactItem.STATUS_ONLINE);
    }

    // Get online status set value
    public static int translateStatusSend(int status) {
        if (status == ContactItem.STATUS_AWAY) return (Util.SET_STATUS_AWAY);
        if (status == ContactItem.STATUS_CHAT) return (Util.SET_STATUS_CHAT);
        if (status == ContactItem.STATUS_DND) return (Util.SET_STATUS_DND);
        if (status == ContactItem.STATUS_INVISIBLE) return (Util.SET_STATUS_INVISIBLE);
        if (status == ContactItem.STATUS_INVIS_ALL) return (Util.SET_STATUS_INVISIBLE);
        if (status == ContactItem.STATUS_NA) return (Util.SET_STATUS_NA);
        if (status == ContactItem.STATUS_OCCUPIED) return (Util.SET_STATUS_OCCUPIED);
        if (status == ContactItem.STATUS_LUNCH) return (Util.SET_STATUS_LUNCH);
        if (status == ContactItem.STATUS_EVIL) return (Util.SET_STATUS_EVIL);
        if (status == ContactItem.STATUS_DEPRESSION) return (Util.SET_STATUS_DEPRESSION);
        if (status == ContactItem.STATUS_HOME) return (Util.SET_STATUS_HOME);
        if (status == ContactItem.STATUS_WORK) return (Util.SET_STATUS_WORK);
        return (Util.SET_STATUS_ONLINE);
    }

    public static boolean qipStatus(int status) {
        switch (status) {
            case ContactItem.STATUS_CHAT:
            case ContactItem.STATUS_LUNCH:
            case ContactItem.STATUS_EVIL:
            case ContactItem.STATUS_DEPRESSION:
            case ContactItem.STATUS_HOME:
            case ContactItem.STATUS_WORK:
                return true;
        }
        return false;
    }

    //  If the numer has only one digit add a 0
    public static String makeTwo(int number) {
        if (number < 10) {
            return ("0" + String.valueOf(number));
        }
        return (String.valueOf(number));
    }

    // Byte array IP to String
    public static String ipToString(byte[] ip) {
        if (ip == null) return null;
        StringBuffer strIP = new StringBuffer();

        for (int i = 0; i < 4; i++) {
            int tmp = (int) ip[i] & 0xFF;
            if (strIP.length() != 0) {
                strIP.append('.');
            }
            strIP.append(tmp);
        }

        return strIP.toString();
    }

    // String IP to byte array
    public static byte[] ipToByteArray(String ip) {
        byte[] arrIP = explodeToBytes(ip, '.', 10);
        return ((arrIP == null) || (arrIP.length != 4)) ? null : arrIP;
    }

    // #sijapp cond.if modules_PROXY is "true"#
    // Try to parse string IP
    public static boolean isIP(String ip) {
        boolean isTrueIp = false;
        try {
            isTrueIp = (ipToByteArray(ip) != null);
        } catch (NumberFormatException e) {
            return false;
        }
        return isTrueIp;
    }
// #sijapp cond.end #

    // Create a random id which is not used yet

    public static int createRandomId(Icq icq) {
        // Max value is probably 0x7FFF, lowest value is unknown.
        // We use range 0x1000-0x7FFF.
        // From miranda source

        int range = 0x6FFF;

        GroupItem[] gItems = icq.getProfile().getGroupItems();
        ContactItem[] cItems = icq.getProfile().getContactItems();
        int randint;
        boolean found;

        Random rand = new Random(System.currentTimeMillis());
        randint = rand.nextInt();
        if (randint < 0) randint = randint * (-1);
        randint = randint % range + 4096;

        //DebugLog.addText("rand: 0x"+Integer.toHexString(randint));

        do {
            found = false;
            for (int i = 0; i < gItems.length; i++) {
                if (gItems[i].getId() == randint) {
                    randint = rand.nextInt() + 4096 % range;
                    found = true;
                    break;
                }
            }
            if (!found)
                for (int j = 0; j < cItems.length; j++) {
                    // Privacy Lists
                    if ((cItems[j].getIntValue(ContactItem.CONTACTITEM_ID) == randint)
                            || (cItems[j].getIgnoreId() == randint)
                            || (cItems[j].getVisibleId() == randint)
                            || (cItems[j].getInvisibleId() == randint)) {
                        randint = rand.nextInt() % range + 4096;
                        found = true;
                        break;
                    }
                }
        } while (found == true);
        return randint;
    }


    public static boolean isDataUTF8(byte[] array) {
        return isDataUTF8(array, 0, array.length);
    }

    // Check is data array utf-8 string
    public static boolean isDataUTF8(byte[] array, int start, int lenght) {
        if (lenght == 0) return false;
        if (array.length < (start + lenght)) return false;

        for (int i = start, len = lenght; len > 0;) {
            int seqLen = 0;
            byte bt = array[i++];
            len--;

            if ((bt & 0xE0) == 0xC0) seqLen = 1;
            else if ((bt & 0xF0) == 0xE0) seqLen = 2;
            else if ((bt & 0xF8) == 0xF0) seqLen = 3;
            else if ((bt & 0xFC) == 0xF8) seqLen = 4;
            else if ((bt & 0xFE) == 0xFC) seqLen = 5;

            if (seqLen == 0) {
                if ((bt & 0x80) == 0x80) return false;
                else continue;
            }

            for (int j = 0; j < seqLen; j++) {
                if (len == 0) return false;
                bt = array[i++];
                if ((bt & 0xC0) != 0x80) return false;
                len--;
            }
            if (len == 0) break;
        }
        return true;
    }

    // Convert gender code to string
    static public String genderToString(int gender) {
        switch (gender) {
            case 1:
                return ResourceBundle.getString("female");
            case 2:
                return ResourceBundle.getString("male");
        }
        return "";
    }

    static public int stringToGender(String gender) {
        if (gender.equals(ResourceBundle.getString("female"))) return 1;
        if (gender.equals(ResourceBundle.getString("male"))) return 2;
        return 0;
    }

    static final int WIN1251EX_CHAR = 6;

    // Converts an Unicode string into CP1251 byte array
    public static byte[] stringToByteArray1251(String s, boolean extended) {
        byte buf[] = new byte[s.length()];
        byte bufEx[] = null;
        int exAdd = 0;
        int size = s.length();
        for (int i = 0; i < size; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case 1025:
                    buf[i] = -88;
                    break;
                case 1105:
                    buf[i] = -72;
                    break;
                /* Ukrainian CP1251 chars section */
                case 1168:
                    buf[i] = -91;
                    break;
                case 1028:
                    buf[i] = -86;
                    break;
                case 1031:
                    buf[i] = -81;
                    break;
                case 1030:
                    buf[i] = -78;
                    break;
                case 1110:
                    buf[i] = -77;
                    break;
                case 1169:
                    buf[i] = -76;
                    break;
                case 1108:
                    buf[i] = -70;
                    break;
                case 1111:
                    buf[i] = -65;
                    break;
                /* end of section */

                default:
                    if (ch >= '\u0410' && ch <= '\u044F') {
                        buf[i] = (byte) ((ch - 1040) + 192);
                    } else {
                        if (ch < 128 && !extended)
                            buf[i] = (byte) ((int) ch & 0xFF);
                        else {
                            if (ch >= 128 || ch <= WIN1251EX_CHAR) {
                                if (bufEx == null)
                                    bufEx = new byte[s.length()];
                                bufEx[i] = (byte) ((ch >> 8));
                                if ((bufEx[i] & 0xFF) >= WIN1251EX_CHAR)
                                    exAdd++;
                                if (bufEx[i] == 0) bufEx[i] = (byte) 0xff;
                                exAdd++;
                                buf[i] = (byte) ((int) ch & 0xFF);
                            } else
                                buf[i] = (byte) ((int) ch & 0xFF);
                        }
                    }
                    break;
            }
        }
        if (exAdd != 0) {
            byte[] newBuf = new byte[buf.length + exAdd];
            int i = 0, j = 0;

            while (i < buf.length) {
                if (bufEx[i] != 0) {
                    if ((bufEx[i] & 0xFF) == 0xFF) {
                        newBuf[j++] = WIN1251EX_CHAR;
                    } else {
                        if ((bufEx[i] & 0xFF) >= WIN1251EX_CHAR)
                            newBuf[j++] = WIN1251EX_CHAR + 1;
                        newBuf[j++] = bufEx[i];
                    }
                }
                newBuf[j++] = buf[i];
                i++;
            }
            return newBuf;
        }
        return buf;
    }

    // Converts an CP1251 byte array into an Unicode string
    public static String byteArray1251ToString(byte buf[], int pos, int len) {
        int end = pos + len;
        StringBuffer stringbuffer = new StringBuffer(len);
        for (int i = pos; i < end; i++) {
            int ch = buf[i] & 0xff;
            switch (ch) {
                case 168:
                    stringbuffer.append('\u0401');
                    break;
                case 184:
                    stringbuffer.append('\u0451');
                    break;
                /* Ukrainian CP1251 chars section */
                case 165:
                    stringbuffer.append('\u0490');
                    break;
                case 170:
                    stringbuffer.append('\u0404');
                    break;
                case 175:
                    stringbuffer.append('\u0407');
                    break;
                case 178:
                    stringbuffer.append('\u0406');
                    break;
                case 179:
                    stringbuffer.append('\u0456');
                    break;
                case 180:
                    stringbuffer.append('\u0491');
                    break;
                case 186:
                    stringbuffer.append('\u0454');
                    break;
                case 191:
                    stringbuffer.append('\u0457');
                    break;
                /* end of section */

                default:
                    try {
                        if (ch >= 192 && ch <= 255) {
                            stringbuffer.append((char) ((1040 + ch) - 192));
                        } else {
                            if (i < end - 1 && ch <= WIN1251EX_CHAR) {
                                i++;
                                if (ch == WIN1251EX_CHAR) ch = 0;
                                stringbuffer.append((char) ((ch << 8) + (buf[i] & 0xff)));
                            } else if (i < end - 2 && ch == (1 + WIN1251EX_CHAR)) {
                                stringbuffer.append((char) (((buf[i + 1] & 0xff) << 8) + (buf[i + 2] & 0xff)));
                                i++;
                                i++;
                            } else {
                                stringbuffer.append((char) ch);
                            }
                        }
                    }
                    catch (Exception ignored) {

                    }
                    break;
            }
        }
        return stringbuffer.toString();
    }

    private static boolean isURLChar(char chr, boolean before) {
        if (before) return ((chr >= 'A') && (chr <= 'Z')) ||
                ((chr >= 'a') && (chr <= 'z')) ||
                ((chr >= '0') && (chr <= '9'));
        return !((chr <= ' ') || (chr == '\u0022')) && ((chr & 0xFF00) == 0);
    }

    public static Vector parseMessageForURL(String msg) {
        if (msg.indexOf('.') == -1) {
            if (msg.indexOf("native://") != -1 || msg.indexOf("tel://") != -1) {
                Vector res = new Vector();
                int idx = msg.indexOf("native://");
                if (idx == -1) {
                    idx = msg.indexOf("tel://");
                }
                int sIdx = msg.indexOf(' ', idx);
                if (sIdx == -1) {
                    sIdx = msg.length();
                }
                res.addElement(msg.substring(idx, sIdx));
                return res;
            }
            return null;
        }

        Vector result = new Vector();
        int size = msg.length();
        int findIndex = 0, beginIdx, endIdx;
        for (; ;) {
            if (findIndex >= size) break;
            int ptIndex = msg.indexOf('.', findIndex);
            if (ptIndex == -1) {
                break;
            }

            for (beginIdx = ptIndex - 1; beginIdx >= 0; beginIdx--) {
                if (!isURLChar(msg.charAt(beginIdx), true)) {
                    break;
                }
            }
            for (endIdx = ptIndex + 1; endIdx < size; endIdx++) {
                if (!isURLChar(msg.charAt(endIdx), false)) {
                    break;
                }
            }
            if ((beginIdx == -1) || !isURLChar(msg.charAt(beginIdx), true)) {
                beginIdx++;
            }

            findIndex = endIdx;
            if ((ptIndex == beginIdx) || (endIdx - ptIndex < 2)) {
                continue;
            }

            result.addElement("http:\57\57" + msg.substring(beginIdx, endIdx));
        }

        return (result.size() == 0) ? null : result;
    }

    static public int strToIntDef(String str, int defValue) {
        if (str == null) return defValue;
        int result = defValue;
        try {
            result = Integer.parseInt(str);
        } catch (Exception ignored) {
        }
        return result;
    }

    static public String replaceStr(String original, String from, String to) {
        int index = original.indexOf(from);
        if (index == -1) return original;
        return original.substring(0, index) + to + original.substring(index + from.length(), original.length());
    }

    static public int extractInt(String text, int radix) {
        int len = text.length();
        if ((len & 1) != 0) {
            text = text + "0";
        }
        len = text.length();
        String item;
        int item0 = 0;
        int off = (len - 1) * 4;
        for (int i = 0; i < len; i++) {
            item = text.substring(i, i + 1);
            item0 |= (Integer.parseInt(item, radix) << off);
            off -= 4;
        }
        return item0;
    }

    static public byte[] explodeToBytesLine(String text) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int len = text.length();
        if ((len & 1) != 0) {
            text = text + "0";
        }
        len = text.length();
        String item;
        for (int i = 0; i < len; i += 2) {
            item = text.substring(i, i + 2);
            bytes.write(Integer.parseInt(item, 16));
        }
        return bytes.toByteArray();
    }

    static public byte[] explodeToBytes(String text, char serparator, int radix) {
        String[] strings = explode(text, serparator);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        String item;
        for (int i = 0; i < strings.length; i++) {
            item = strings[i];
            if (item.charAt(0) == '*') {
                for (int j = 1; j < item.length(); j++) {
                    bytes.write((byte) item.charAt(j));
                }
            } else {
                bytes.write(Integer.parseInt(item, radix));
            }
        }
        return bytes.toByteArray();
    }

    /* Divide text to array of parts using serparator charaster */
    static public String[] explode(String text, char serparator) {
        Vector tmp = new Vector();
        StringBuffer strBuf = new StringBuffer();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char chr = text.charAt(i);
            if (chr == serparator) {
                tmp.addElement(strBuf.toString());
                strBuf.delete(0, strBuf.length());
            } else {
                strBuf.append(chr);
            }
        }
        tmp.addElement(strBuf.toString());
        String[] result = new String[tmp.size()];
        tmp.copyInto(result);
        return result;
    }

    static public int[] explodeToInt(String text, char serparator) {
        Vector tmp = new Vector();
        StringBuffer strBuf = new StringBuffer();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char chr = text.charAt(i);
            if (chr == serparator) {
                tmp.addElement(new Integer(Integer.parseInt(strBuf.toString())));
                strBuf.delete(0, strBuf.length());
            } else {
                strBuf.append(chr);
            }
        }
        tmp.addElement(new Integer(Integer.parseInt(strBuf.toString())));
        int[] result = new int[tmp.size()];
        //tmp.copyInto(result);
        for (int i = tmp.size() - 1; i >= 0; i--) {
            result[i] = ((Integer) tmp.elementAt(i)).intValue();
        }
        return result;
    }

    // Merge two received capabilities into one byte array
    public static byte[] mergeCapabilities(byte[] capabilities_old, byte[] capabilities_new) {
        if (capabilities_new == null)
            return capabilities_old;
        if (capabilities_old == null)
            return capabilities_new;

        // Extend new capabilities to match with old ones
        byte[] extended_new = new byte[capabilities_new.length * 8];
        for (int i = 0; i < capabilities_new.length; i += 2) {
            System.arraycopy(CAP_OLD_HEAD, 0, extended_new, (i * 8), CAP_OLD_HEAD.length);
            System.arraycopy(capabilities_new, i, extended_new, ((i * 8) + CAP_OLD_HEAD.length), 2);
            System.arraycopy(CAP_OLD_TAIL, 0, extended_new, ((i * 8) + CAP_OLD_HEAD.length + 2), CAP_OLD_TAIL.length);
        }
        // Check for coexisting capabilities and merge
        boolean found = false;
        byte[] tmp_old;
        for (int i = 0; i < capabilities_old.length; i += 16) {
            tmp_old = new byte[16];
            System.arraycopy(capabilities_old, i, tmp_old, 0, 16);
            for (int j = 0; j < extended_new.length; j += 16) {
                byte[] tmp_new = new byte[16];
                System.arraycopy(extended_new, j, tmp_new, 0, 16);
                if (tmp_old == tmp_new) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                //System.out.println("Merge capability");
                byte[] merged = new byte[extended_new.length + 16];
                System.arraycopy(extended_new, 0, merged, 0, extended_new.length);
                System.arraycopy(tmp_old, 0, merged, extended_new.length, tmp_old.length);
                extended_new = merged;
                found = false;
            }
        }
        return extended_new;
    }

    /**
     * *************************ICQ XTraz Support*******************************
     */

    public static String DeMangleXml(String s1) {
        int i1 = 0;
        int j1 = 0;
        int k1 = 0;
        StringBuffer stringbuffer = new StringBuffer(s1.length());
        do {
            if (i1 < 0 || (i1 = s1.indexOf("&", k1)) < 0) {
                break;
            }
            stringbuffer.append(s1.substring(j1, i1));
            j1 = i1;
            if ((k1 = s1.indexOf(";", j1)) < 0) {
                break;
            }
            j1 = k1 + 1;
            String s2;
            if ((s2 = s1.substring(i1, k1 + 1)).equals("&lt;")) {
                stringbuffer.append("<");
            } else if (s2.equals("&gt;")) {
                stringbuffer.append(">");
            } else if (s2.equals("&amp;")) {
                stringbuffer.append("&");
            } else {
                stringbuffer.append(s2);
            }
        } while (true);
        if (j1 < s1.length()) {
            stringbuffer.append(s1.substring(j1));
        }
        return stringbuffer.toString();
    }

    public static String MangleXml(String s1) {
        StringBuffer stringbuffer = new StringBuffer(s1.length());
        for (int i1 = 0; i1 < s1.length(); i1++) {
            char c1;
            switch (c1 = s1.charAt(i1)) {
                case 60:
                    stringbuffer.append("&lt;");
                    break; // '<'
                case 62:
                    stringbuffer.append("&gt;");
                    break; // '>'
                case 38:
                    stringbuffer.append("&amp;");
                    break; // '&'
                default:
                    stringbuffer.append(c1);
                    break;
            }
        }
        return stringbuffer.toString();
    }

    /*****************************************************************************/

    /**
     * ************ расшифровка RTF сообщений**************
     */
    public static String DecodeRTF(String msg) {
        //System.out.println("Decoding RTF");
        StringBuffer sb = new StringBuffer();
        int nl = 0; //уровень вложенности
        int ps = 0; //текущая позиция
        int msgSz = msg.length();
        while (ps < msgSz) {
            char ch = msg.charAt(ps);
            if (ch == '{') {
                nl++;
            } else if (ch == '}') {
                nl--;
            }
            if (nl == 1) { //можно парсить текст
                ps++;
                boolean ctrl = false;
                StringBuffer ctrl2 = new StringBuffer();
                while (nl == 1) {
                    ch = msg.charAt(ps++);
                    if (ch == '{') {
                        nl++;
                        break;
                    } else if (ch == '}') {
                        nl--;
                        break;
                    } else if (ch == '\\') { //слэш
                        if (ctrl2.toString().equals("tab")) {
                            sb.append(' ');
                        }
                        ctrl2.setLength(0);
                        char ch2 = msg.charAt(ps);
                        if (ch2 == '\\') {
                            sb.append('\\');
                            ps++;
                            ctrl = false;
                        } else if (ch2 == '\'') { //нелатиница
                            char ch3 = (char) Integer.parseInt(msg.substring(ps + 1, ps + 3), 16);
                            sb.append((char) (ch3 > 127 ? ch3 == 0xA8 ? 0x401 : ch3 == 0xB8 ? 0x451 : ch3 + 0x0350 : ch3));
                            ps += 3;
                            ctrl = false;
                        } else {
                            ctrl = true;
                        }
                    } else if ((ch == ' ' || ch == '\n') && ctrl) {
                        ctrl = false;
                        if (ctrl2.toString().equals("par")) {
                            sb.append('\n');
                        }
                        ctrl2.setLength(0);
                    } else if (!ctrl && ch >= 32) {
                        sb.append(ch);
                    } else {
                        ctrl2.append(ch);
                    }
                }
            }
            ps++;
        }
        return sb.toString();
    }
    /**************************************************************************************/
}
