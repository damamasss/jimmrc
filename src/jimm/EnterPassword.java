package jimm;
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
 File: src/jimm/comm/EnterPassword.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Denis K.
 *******************************************************************************/

import javax.microedition.lcdui.*;

public class EnterPassword implements CommandListener {

    private Object prvScreen;
    private Object nextScreen;
    private TextBox passwordTextField = new TextBox(null, null, 20, TextField.PASSWORD);

    public EnterPassword(Object ps, Object ns) {
        prvScreen = ps;
        nextScreen = ns;
        passwordTextField.setTitle(jimm.util.ResourceBundle.getString("enter_password"));
        passwordTextField.addCommand(JimmUI.cmdOk);
        passwordTextField.addCommand(JimmUI.cmdCancel);
        passwordTextField.setCommandListener(this);
    }

    public void showPasswordForm(boolean autoconnect) {
        if ((Options.getString(Options.OPTION_ENTER_PASSWORD)).length() > 0) {
            passwordTextField.setString("");
            Jimm.setDisplay(passwordTextField);
        } else {
            passReceived(autoconnect);
            //if ((Options.getString(Options.OPTION_UIN).length() == 0) || (Options.getString(Options.OPTION_PASSWORD).length() == 0)) {
            //    Options.showAccount(Jimm.getIcqRef());
            //}
        }
    }

    private void passReceived(boolean autoconnect) {
        if (autoconnect) {
            Jimm.getContactList().activate();
            Jimm.getCurrentProfile().connect();
        } else {
            if (prvScreen instanceof SplashCanvas) {
                SplashCanvas sc = (SplashCanvas) prvScreen;
                if (sc.isLocked) {
                    sc.unlockProgramm();
                    return;
                }
            }
            Jimm.setDisplay(nextScreen);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == JimmUI.cmdOk) {
            if (Options.getString(Options.OPTION_ENTER_PASSWORD).equals(passwordTextField.getString())) {
                passReceived(Options.getBoolean(Options.OPTION_AUTO_CONNECT) && !Jimm.locked());
                return;
            }
        }
        if (!Jimm.locked()) {
            Jimm.doExit(true);
        } else {
            Jimm.setDisplay(prvScreen);
        }
    }
}