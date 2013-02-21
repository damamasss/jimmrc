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
 File: src/jimm/JimmException.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm;

import jimm.util.ResourceBundle;
import jimm.comm.DateAndTime;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

public class JimmException extends Exception {

    private boolean critical = true;
    private boolean displayMsg = true;
    private int _ErrCode = 0;
    private boolean peer = false;

    public JimmException(int errCode, int extErrCode) {
        super(JimmException.getErrDesc(errCode, extErrCode));
        _ErrCode = errCode;
        peer = false;
    }


    public JimmException(int errCode, int extErrCode, boolean msg) {
        super(JimmException.getErrDesc(errCode, extErrCode));
        _ErrCode = errCode;
        critical = false;
        displayMsg = msg;
    }

    public static String getErrDesc(int errCode, int extErrCode) {
        String errDesc = ResourceBundle.getError("error_" + errCode);
        int ext = errDesc.indexOf("EXT");
        if (ext != -1) return (errDesc.substring(0, ext) + extErrCode + errDesc.substring(ext + 3));
        return errDesc;
    }

    public int getErrCode() {
        return _ErrCode;
    }

//#sijapp cond.if modules_FILES is "true"#
    public JimmException(int errCode, int extErrCode, boolean displayMsg, boolean peer) {
        super(JimmException.getErrDesc(errCode, extErrCode));
        this._ErrCode = errCode;
        this.critical = false;
        this.displayMsg = displayMsg;
        this.peer = peer;
    }
//#sijapp cond.end#


    public boolean isDisplayMsg() {
//#sijapp cond.if modules_DEBUGLOG is "true" #
//#		return _ErrCode < 300;
//#sijapp cond.else#
        return displayMsg;
//#sijapp cond.end#
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isPeer() {
        return peer;
    }

    public synchronized static void handleException(JimmException je) {
        if (je.isDisplayMsg()) {
            Alert errorMsg = new Alert(ResourceBundle.getString("errorlng"), "[" + DateAndTime.getDateString(false, true) + "]: " + je.getMessage(), null, AlertType.ERROR);
            errorMsg.setTimeout(Alert.FOREVER);
            Jimm.setDisplay(errorMsg);
        }
// #sijapp cond.if modules_DEBUGLOG is "true"#
        System.out.println('#');
        je.printStackTrace();
// #sijapp cond.end#        
    }

    public synchronized static void handleExceptionEx(Exception e) {
// #sijapp cond.if modules_DEBUGLOG is "true"#
        DebugLog.addText(e.toString());
        // e.toString() + '\n' + e.getMessage() + '\n' + e.getClass()//todo test
        System.out.println('#');
        e.printStackTrace();
// #sijapp cond.end#
    }
}