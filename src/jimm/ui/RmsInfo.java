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
 File: src/jimm/MagicEye.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Lavlinskii Roman
 *******************************************************************************/

//#sijapp cond.if modules_SYSMANAGER is "true"#
package jimm.ui;

import DrawControls.TextList;
import DrawControls.CanvasEx;
import DrawControls.VirtualList;

import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import jimm.*;
import jimm.forms.FormEx;
import jimm.util.ResourceBundle;

import java.util.Vector;

public class RmsInfo extends TextList implements CommandListener, MenuListener {

    public final class MassClear implements CommandListener {

        private ChoiceGroupEx textChoise;
        private FormEx form;
        private Vector vector;

        private MassClear(String caption) {
            textChoise = new ChoiceGroupEx(null, Choice.MULTIPLE);
            vector = new Vector();
            form = new FormEx(caption, JimmUI.cmdOk, JimmUI.cmdBack);
            form.setCommandListener(this);
        }

        public MassClear() {
            this("clear_rms");
            String[] allRms = RecordStore.listRecordStores();
            int size;
            if ((size = allRms.length) != 0) {
                size = Math.min(size, 256);
                for (int j = 0; j < size; j++) {
                    vector.addElement(allRms[j]);
                    textChoise.append(allRms[j], null);
                }
            }
            activate();
        }

        public final void activate() {
            form.append(textChoise);
            Jimm.setDisplay(form);
        }

        public final void commandAction(Command command, Displayable displayable) {
            if (command.equals(JimmUI.cmdOk)) {
                boolean aflag[] = new boolean[textChoise.size()];
                if (textChoise.getSelectedFlags(aflag) != 0) {
                    String name;
                    for (int i = 0; i < aflag.length; i++) {
                        if (!aflag[i]) {
                            continue;
                        }
                        name = (String) vector.elementAt(i);
                        try {
                            RecordStore.deleteRecordStore(name);
                        } catch (RecordStoreException ignored) {
                        }
                    }
                }
            }
            new RmsInfo(prvScreen);
        }
    }

    private final static byte MENU_DELETE = (byte) 0;
    private final static byte MENU_CLEAR = (byte) 1;

    private CanvasEx prvScreen;

    public RmsInfo(CanvasEx screen) {
        super("RMS-info", false);
        prvScreen = screen;
        setMode(MODE_TEXT);
        setColorScheme();
        setCommandListener(this);
        creating();
    }

    public void creating() {
        String[] names = RecordStore.listRecordStores();
        if (names == null || names.length == 0) {
            commandAction(JimmUI.cmdBack, null);
            return;
        }
        RecordStore rs = null;
        int size;
        int all = 0;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            try {
                rs = RecordStore.openRecordStore(names[i], false);
                size = rs.getSize();
                if (i == 0) {
                    all = rs.getSizeAvailable();
                }
            } catch (RecordStoreException rse) {
                size = 0;
            } finally {
                try {
                    rs.closeRecordStore();
                } catch (Exception ignored) {
                }
            }
            sb.append("\"").append(names[i]).append("\"").append(": ").append(Integer.toString(size)).append(" bytes\n");
        }
        addBigText("Available: " + Integer.toString(all) + " bytes\n-----\n", getColor(COLOR_TEXT), Font.STYLE_BOLD, -1);
        addBigText(sb.toString(), getColor(COLOR_TEXT), Font.STYLE_BOLD, -1);
        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        activate();
    }

    private void showMenu() {
        Menu menu = new Menu(this);
        menu.addMenuItem("delete", MENU_DELETE);
        menu.addMenuItem("clear", MENU_CLEAR);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case MENU_DELETE:
                new MassClear();
                break;

            case MENU_CLEAR:
                Jimm.setDisplay(new Select(ResourceBundle.getString("reset_all"), ResourceBundle.getString("reset_rms_ask"),
                        new SelectListener() {
                            public void selectAction(int action, int selectType, Object o) {
                                resetRMS();
                            }
                        },
                        0, null));
                break;
        }
    }

    private void resetRMS() {
        Jimm.getIcqRef().disconnect();
        try {
            Options.reset_rms();
            Thread.sleep(100);
            Jimm.jimm.autorun(500);
        } catch (Exception ignored) {
        }
        //Jimm.doExit(true);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
            if (prvScreen != null) {
                Jimm.setDisplay(prvScreen);
                prvScreen = null;
            } else {
                Jimm.getContactList().activate();
            }
        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        }
    }
}
//#sijapp cond.end#