/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-06  Jimm Project

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
 File: src/jimm/Traffic.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Andreas Rossbacher
 *******************************************************************************/
package jimm;

public class Traffic {
    static private long input;
    static private long output;

    static public long getSessionTraffic() {
        return ((input + output) / 1024);
    }

//    static public long getSessionTrafficByte() {
//        return (input + output);
//    }

    static public void addTrafficIn(int bytes) {
        input += bytes;
    }

    static public void addTrafficOut(int bytes) {
        output += bytes;
    }

    static public String getSessionTrafficIn() {
        return (getSessionTrafficString(input));
    }

    static public String getSessionTrafficOut() {
        return (getSessionTrafficString(output));
    }

    static public String getSessionTrafficString(long input) {
        String out;
//        try{
//            float f = (float)input / 1024; // use cldc 1.1 & modp 2.1
//            out = String.valueOf(f);
//            if (out.length() > 4) {
//                out = out.substring(0, 4);
//            }
//        } catch (Throwable tw) {
        String s = Long.toString(input * 100 / 1024);
        if (s.length() <= 2) {
            return "0,00";
        }
        out = (new StringBuffer()).append(s.substring(0, s.length() - 2)).append(',').append(s.substring(s.length() - 2)).toString();
        //}
        return out;
    }
}
