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
 File: src/jimm/JimmUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Igor Palkin
 *******************************************************************************/
package jimm.info;

import jimm.Jimm;
import jimm.JimmException;
import jimm.JimmUI;
import jimm.comm.*;
import jimm.forms.FormEx;
import jimm.forms.OptionsForm;
import jimm.ui.LineChoise;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.io.ByteArrayOutputStream;

public class EditInfo implements CommandListener {

    private FormEx form;
    private Icq icq;

    private TextField _NickNameItem = new TextField(ResourceBundle.getString("nick"), null, 20, TextField.ANY);
    private TextField _FirstNameItem = new TextField(ResourceBundle.getString("firstname"), null, 20, TextField.ANY);
    private TextField _LastNameItem = new TextField(ResourceBundle.getString("lastname"), null, 20, TextField.ANY);
    private TextField _EmailItem = new TextField(ResourceBundle.getString("email"), null, 50, TextField.EMAILADDR);
    private TextField _BdayItem = new TextField(ResourceBundle.getString("birth_day"), null, 15, TextField.ANY);
    private TextField _HomePageItem = new TextField(ResourceBundle.getString("home_page"), null, 70, TextField.ANY);
    private TextField _AboutItem = new TextField(ResourceBundle.getString("notes"), null, 1024, TextField.ANY);
    private TextField _CityItem = new TextField(ResourceBundle.getString("city"), null, 50, TextField.ANY);
    private TextField _CityOrigItem = new TextField(ResourceBundle.getString("city_original"), null, 50, TextField.ANY);
    private TextField _WorkName = new TextField(ResourceBundle.getString("work_info"), null, 50, TextField.ANY);
    private TextField _WorkPosition = new TextField(ResourceBundle.getString("position"), null, 50, TextField.ANY);
    private TextField _phoneNumber = new TextField(ResourceBundle.getString("cell_phone"), null, 50, TextField.ANY);
    private LineChoise _Interests1;
    private LineChoise _Interests2;
    private LineChoise _Interests3;
    private LineChoise _Interests4;
    //private TextBox categoryValue = new TextBox(null, null, 60, TextField.ANY);
    private TextField interests1 = new TextField(null, null, 60, TextField.ANY);
    private TextField interests2 = new TextField(null, null, 60, TextField.ANY);
    private TextField interests3 = new TextField(null, null, 60, TextField.ANY);
    private TextField interests4 = new TextField(null, null, 60, TextField.ANY);
    private LineChoise _SexItem;

    //private TextField _CurrentPass = new TextField(ResourceBundle.getString("current_pass"), null, 16, TextField.PASSWORD);
    //private TextField _NewPass = new TextField(ResourceBundle.getString("new_pass"), null, 8, TextField.PASSWORD);
    //private TextField _NewPassAgain = new TextField(ResourceBundle.getString("new_pass_again"), null, 8, TextField.PASSWORD);
    //private Command _CmdChange = new Command(ResourceBundle.getString("change"), Command.ITEM, 1);

    private UserInfo _PreviousForm;
    private String[] userInfo;

    private EditInfo(boolean changePass, UserInfo currentForm, Icq icq) {
        this.icq = icq;
        String cap = changePass ? ResourceBundle.getString("change_pass") : ResourceBundle.getString("editform");
        form = new FormEx(cap, /*changePass ? _CmdChange :*/ JimmUI.cmdSave, JimmUI.cmdCancel);
        _PreviousForm = currentForm;
        if (changePass) {
            /*form.append(_CurrentPass);
            form.append(_NewPass);
            form.append(_NewPassAgain);*/
        } else {
            StringBuffer choice = new StringBuffer("---");
            _SexItem = OptionsForm.createLine(ResourceBundle.getString("gender"), "---" + "|" + "female" + "|" + "male", 0, true, true);
            for (int i = 0; i < Info.CAT_COUNT; i++) {
                choice.append('|').append(currentForm.getCategoryName(i));
            }
            String schoice = choice.toString();
            //choice = null;
            cap = ResourceBundle.getString("interests");
            _Interests1 = OptionsForm.createLine(cap, schoice, 0, true, true);
            _Interests2 = OptionsForm.createLine(cap, schoice, 0, true, true);
            _Interests3 = OptionsForm.createLine(cap, schoice, 0, true, true);
            _Interests4 = OptionsForm.createLine(cap, schoice, 0, true, true);
        }
        form.setCommandListener(this);
    }

    public static void showEditForm(String[] userInfo, UserInfo previousForm, Icq icq) {
        EditInfo editInfoForm = new EditInfo(false, previousForm, icq);
        editInfoForm.userInfo = userInfo;
        try {
            editInfoForm._SexItem.setSelected(Util.stringToGender(userInfo[Info.UI_GENDER]));
        } catch (Exception ignored) {
        }
        editInfoForm._NickNameItem.setString(userInfo[Info.UI_NICK]);
        editInfoForm._EmailItem.setString(userInfo[Info.UI_EMAIL]);
        editInfoForm._BdayItem.setString(userInfo[Info.UI_BDAY]);
        editInfoForm._FirstNameItem.setString(userInfo[Info.UI_FIRST_NAME]);
        editInfoForm._LastNameItem.setString(userInfo[Info.UI_LAST_NAME]);
        editInfoForm._HomePageItem.setString(userInfo[Info.UI_HOME_PAGE]);
        editInfoForm._AboutItem.setString(userInfo[Info.UI_ABOUT]);
        editInfoForm._CityItem.setString(userInfo[Info.UI_CITY]);
        editInfoForm._CityOrigItem.setString(userInfo[Info.UI_ORIG_CITY]);
        editInfoForm._WorkName.setString(userInfo[Info.UI_W_NAME]);
        editInfoForm._WorkPosition.setString(userInfo[Info.UI_W_POS]);
        editInfoForm._phoneNumber.setString(userInfo[Info.UI_CPHONE]);

        if (userInfo[Info.UI_INETRESTS] != null) {
            if (userInfo[Info.UI_INETRESTS].length() != 0) {
                editInfoForm._Interests1.setSelected(getIndex(userInfo[Info.UI_INT_CAT1]) + 1);
                editInfoForm._Interests2.setSelected(getIndex(userInfo[Info.UI_INT_CAT2]) + 1);
                editInfoForm._Interests3.setSelected(getIndex(userInfo[Info.UI_INT_CAT3]) + 1);
                editInfoForm._Interests4.setSelected(getIndex(userInfo[Info.UI_INT_CAT4]) + 1);

                if (userInfo[Info.UI_INT_CAT1] != null) {
                    if (userInfo[Info.UI_INT_CAT1].length() > 2) {
                        editInfoForm.interests1.setString(userInfo[Info.UI_INT_CAT1].substring(2));
                    }
                }

                if (userInfo[Info.UI_INT_CAT2] != null) {
                    if (userInfo[Info.UI_INT_CAT2].length() > 2) {
                        editInfoForm.interests2.setString(userInfo[Info.UI_INT_CAT2].substring(2));
                    }
                }

                if (userInfo[Info.UI_INT_CAT3] != null) {
                    if (userInfo[Info.UI_INT_CAT3].length() > 2) {
                        editInfoForm.interests3.setString(userInfo[Info.UI_INT_CAT3].substring(2));
                    }
                }

                if (userInfo[Info.UI_INT_CAT4] != null) {
                    if (userInfo[Info.UI_INT_CAT4].length() > 2) {
                        editInfoForm.interests4.setString(userInfo[Info.UI_INT_CAT4].substring(2));
                    }
                }
            }
        }
        editInfoForm.form.append(editInfoForm._NickNameItem);
        editInfoForm.form.append(editInfoForm._FirstNameItem);
        editInfoForm.form.append(editInfoForm._LastNameItem);
        editInfoForm.form.append(editInfoForm._SexItem);
        editInfoForm.form.append(editInfoForm._EmailItem);
        editInfoForm.form.append(editInfoForm._BdayItem);
        editInfoForm.form.append(editInfoForm._HomePageItem);
        editInfoForm.form.append(editInfoForm._AboutItem);
        editInfoForm.form.append(editInfoForm._CityItem);
        editInfoForm.form.append(editInfoForm._CityOrigItem);
        editInfoForm.form.append(editInfoForm._WorkName);
        editInfoForm.form.append(editInfoForm._WorkPosition);
        editInfoForm.form.append(editInfoForm._phoneNumber);
        editInfoForm.form.append(editInfoForm._Interests1);
        editInfoForm.form.append(editInfoForm.interests1);
        editInfoForm.form.append(editInfoForm._Interests2);
        editInfoForm.form.append(editInfoForm.interests2);
        editInfoForm.form.append(editInfoForm._Interests3);
        editInfoForm.form.append(editInfoForm.interests3);
        editInfoForm.form.append(editInfoForm._Interests4);
        editInfoForm.form.append(editInfoForm.interests4);
        Jimm.setDisplay(editInfoForm.form);
    }

    /*public static void showChangePass(UserInfo previousForm, Icq icq) {
        EditInfo changePassForm = new EditInfo(true, previousForm, icq);
        changePassForm._CurrentPass.setString(null);
        changePassForm._NewPass.setString(null);
        changePassForm._NewPassAgain.setString(null);
        Jimm.setDisplay(changePassForm.form);
    }*/

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdCancel) {
            Jimm.setDisplay(_PreviousForm);
        } else if (c == JimmUI.cmdSave) {
            userInfo[Info.UI_NICK] = StringConvertor.getString(_NickNameItem.getString());
            userInfo[Info.UI_EMAIL] = StringConvertor.getString(_EmailItem.getString());
            userInfo[Info.UI_BDAY] = StringConvertor.getString(_BdayItem.getString());
            userInfo[Info.UI_FIRST_NAME] = StringConvertor.getString(_FirstNameItem.getString());
            userInfo[Info.UI_LAST_NAME] = StringConvertor.getString(_LastNameItem.getString());
            userInfo[Info.UI_HOME_PAGE] = StringConvertor.getString(_HomePageItem.getString());
            userInfo[Info.UI_ABOUT] = StringConvertor.getString(_AboutItem.getString());
            userInfo[Info.UI_CITY] = StringConvertor.getString(_CityItem.getString());
            userInfo[Info.UI_ORIG_CITY] = StringConvertor.getString(_CityOrigItem.getString());
            userInfo[Info.UI_W_NAME] = StringConvertor.getString(_WorkName.getString());
            userInfo[Info.UI_W_POS] = StringConvertor.getString(_WorkPosition.getString());
            userInfo[Info.UI_CPHONE] = StringConvertor.getString(_phoneNumber.getString());
            userInfo[Info.UI_GENDER] = Util.genderToString(_SexItem.getSelected());

            userInfo[Info.UI_INT_CAT1] = Util.makeTwo(_Interests1.getSelected()) + interests1.getString();
            userInfo[Info.UI_INT_CAT2] = Util.makeTwo(_Interests2.getSelected()) + interests2.getString();
            userInfo[Info.UI_INT_CAT3] = Util.makeTwo(_Interests3.getSelected()) + interests3.getString();
            userInfo[Info.UI_INT_CAT4] = Util.makeTwo(_Interests4.getSelected()) + interests4.getString();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Util.writeWord(stream, ToIcqSrvPacket.CLI_SET_FULLINFO, false);

            Util.writeAsciizTLV(SaveInfoAction.FIRSTNAME_TLV_ID, stream, userInfo[Info.UI_FIRST_NAME], false);

            Jimm.getContactList().activate();
            SaveInfoAction action = new SaveInfoAction(userInfo);
            try {
                icq.requestAction(action);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    return;
                }
            }
            icq.getProfile().addAction("saveinfo", action);
        }

        /*if (c == _CmdChange) {
            String currPass = _CurrentPass.getString();
            String newPass = _NewPass.getString();
            String newPassAgain = _NewPassAgain.getString();
            int length = newPass.length();
            byte[] passRaw = Util.stringToByteArray(newPass);
            byte[] buf = new byte[length + 4 + 1];
            Util.putWord(buf, 0, 0x2e04);
            Util.putWord(buf, 2, length, false);
            System.arraycopy(passRaw, 0, buf, 4, passRaw.length);
            Util.putByte(buf, 4 + length, 0x00);

            if ((length != 0) && (newPass.equals(newPassAgain)) && (currPass.equals(icq.getPassword()))) {
                ToIcqSrvPacket reply3 = new ToIcqSrvPacket(SnacPacket.CLI_TOICQSRV_COMMAND, 0x00000000, icq.getUin(), 0x07d0, new byte[0], buf);
                try {
                    icq.sendPacket(reply3);
                } catch (JimmException e) {
                    JimmException.handleException(e);
                    if (e.isCritical()) {
                        return;
                    }
                }
                icq.getProfile().changePassword(newPass);
                Jimm.setDisplay(_PreviousForm);
            } else {
                Alert wrong_entry = new Alert("", ResourceBundle.getString("wrong_pass_entry"), null, AlertType.ERROR);
                wrong_entry.setTimeout(15000);
                Jimm.setDisplay(wrong_entry);
            }
        }*/
    }

    static public int getIndex(String str) {
        if (str == null) {
            return -1;
        }
        if (str.length() == 0) {
            return -1;
        }
        if (str.charAt(0) == '0') {
            return Integer.parseInt(str.substring(1, 2));
        }
        if (str.substring(0, 2).equals("60")) {
            return -1;
        }
        return Integer.parseInt(str.substring(0, 2));
    }
}