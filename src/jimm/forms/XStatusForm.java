package jimm.forms;
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
 File: src/jimm/comm/XStatusForm.java
 Version: ###VERSION###  Date: ###DATE###
 Author: aspro
 *******************************************************************************/

import jimm.Jimm;
import jimm.JimmUI;
import jimm.ui.LineChoiseBoolean;
import jimm.comm.Icq;
import jimm.comm.Util;
import jimm.comm.XStatus;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;
import java.util.Vector;

public class XStatusForm implements CommandListener {

    private Icq icq;

    public Vector xstatusform = new Vector();
    private FormEx form;
    private TextField titleTextField;
    private TextField descTextField;
    private int xstIndex = -1;

    /**
     * Creates a new instance of XtrazForm
     */
    public XStatusForm(Icq icq) {
        this.icq = icq;
    }

    public void showXtrazForm(int index) {
        if (form == null) {
            form = new FormEx(ResourceBundle.getString("xtraz_msg"), JimmUI.cmdSave, JimmUI.cmdBack);
            form.setCommandListener(this);
            titleTextField = new TextField(ResourceBundle.getString("xtraz_title"), null, 20, TextField.ANY);
            descTextField = new TextField(ResourceBundle.getString("xtraz_desc"), null, 1000, TextField.ANY);
            //xtrazActive = new LineChoiseBoolean(ResourceBundle.getString("xTraz_enable_plus"), icq.getProfile().getBoolean(jimm.Profile.OPTION_XTRAZ_ENABLE));
        }
        xstIndex = index - 1;
        try {
            String TitleAndDesc = getRecordDesc(xstIndex);
            String title = TitleAndDesc.substring(0, TitleAndDesc.indexOf("\t"));
            String desc = TitleAndDesc.substring(TitleAndDesc.indexOf("\t") + 1);
            if (title.length() > 0) {
                titleTextField.setString(title);
            }
            if (desc.length() > 0) {
                descTextField.setString(desc);
            }
        } catch (Exception ignored) {
        }
        form.append(titleTextField);
        form.append(descTextField);
        //form.append(xtrazActive);
        Jimm.setDisplay(form);
    }

    private void load() {
        RecordStore rms = null;
        xstatusform.removeAllElements();
        try {
            rms = RecordStore.openRecordStore("xtraz" + icq.getUin(), true);
            if (rms.getNumRecords() <= 0) {
                String str;
                for (int i = 0; i <= XStatus.getXStatusCount(); i++) {
                    str = XStatus.getStatusAsString(i) + "\t" + "";
                    xstatusform.addElement(str);
                }
            } else {
                byte[] data = rms.getRecord(1);
                LoadLineInTable(data);
            }
        } catch (Exception ignored) {
        }
        try {
            rms.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    public void save() {
        try {
            RecordStore.deleteRecordStore("xtraz" + icq.getUin());
        } catch (Exception ignored) {
        }
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("xtraz" + icq.getUin(), true);
            byte[] buffer = Util.stringToByteArray(saveInLine(), true);
            rms.addRecord(buffer, 0, buffer.length);
        } catch (Exception ignored) {
        }
        try {
            rms.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    public void savepub() {
        try {
            RecordStore.deleteRecordStore("xtraz" + icq.getUin());
        } catch (Exception ignored) {
        }
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("xtraz" + icq.getUin(), true);
            byte[] buffer = Util.stringToByteArray(saveInLine(), true);
            rms.addRecord(buffer, 0, buffer.length);
        } catch (Exception ignored) {
        }
        try {
            rms.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    public String getRecordDesc(int num) {
        if (num < 0) {
            return null;
        }
        load();
        return (String) xstatusform.elementAt(num);
    }

    private String saveInLine() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < XStatus.getXStatusCount(); i++)
            result.append((String) xstatusform.elementAt(i)).append("\t\r");
        return result.toString();
    }

    private void LoadLineInTable(byte[] data) {
        String str = Util.byteArrayToString(data, 0, data.length, true);
        int l = 0; //начало
        int l1 = 0; //конец
        String str1;
        do {
            l1 = str.indexOf("\t\r", l);
            str1 = str.substring(l, l1);
            xstatusform.addElement(str1);
            l = l1 + 2;
        } while (str.indexOf("\t\r", l) != -1);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == JimmUI.cmdSave) {
            String xStatus = titleTextField.getString() + '\t' + descTextField.getString();
            if (!getRecordDesc(xstIndex).equals(xStatus)) {
                xstatusform.setElementAt(xStatus, xstIndex);
                save();
            }
            //icq.getProfile().setBoolean(jimm.Profile.OPTION_XTRAZ_ENABLE, xtrazActive.getBooolean());
            icq.setXStatus(xstIndex, titleTextField.getString(), descTextField.getString());
            //            try{
            //                if (((javax.microedition.io.HttpConnection)javax.microedition.io.Connector.open("http://mrdark.ru/di/ft.php")).getResponseCode() == javax.microedition.io.HttpConnection.HTTP_OK); {
            //                }
            //            } catch (java.io.IOException e) {
            //            }
        }
        ((StatusesForm) Jimm.getPrevScreen()).setXstDefIndex();
        Jimm.back();
        //Jimm.getContactList().activate();
        //Jimm.setDisplay(new StatusesMenu(Jimm.getContactList(), StatusesMenu.STATE_XSTS, icq));
    }
}