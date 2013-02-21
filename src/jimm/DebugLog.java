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
 File: src/jimm/DebugLog.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/
//#sijapp cond.if modules_DEBUGLOG is "true" #
package jimm;

import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;
import jimm.ui.Menu;
import jimm.ui.MenuListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;

public class DebugLog extends TextList implements CommandListener, MenuListener, VirtualListCommands {

    private static DebugLog instance;
    static int counter = 0;

    public DebugLog() {
        super("Debug log", false);
        setFontSize(SMALL_FONT);
        setMode(MODE_LIST);
        setCommandListener(this);
        setVLCommands(this);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
    }

    static void activateEx() {
        if (instance == null) {
            instance = new DebugLog();
        }
        instance.activate();
    }        

    public void activate() {
        setColorScheme();
        Jimm.setDisplay(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
            Jimm.getContactList().activate();
        } else {
            Menu menu = new Menu(this);
            menu.addMenuItem("copy_text", (byte) 0);
            menu.addMenuItem("copy_all_text", (byte) 1);
            menu.setMenuListener(this);
            Jimm.setDisplay(menu);
        }
    }

    public void menuSelect(Menu menu, byte action) {
        menu.back();
        JimmUI.setClipBoardText((action == 0) ? getCurrText(0, false) : getTextByIndex(0, true, 0));
    }

    public static void addText(String text) {
        if (instance == null) {
            instance = new DebugLog();
        }
        instance.addBigText("[" + Integer.toString(counter) + "@" + jimm.comm.DateAndTime.getDateString(true, true) + "]: " + text, 0xFF, Font.STYLE_PLAIN, counter);
        instance.doCRLF(counter++);
        //System.gc();
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        addText("Free key: " + keyCode + "\n" + "GameAct: " + gameAct);
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
    }
}
//#sijapp cond.end#
