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
 File: src/jimm/Templates.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Igor Palkin, Arvin
 *******************************************************************************/


package jimm;

import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;
import jimm.comm.Util;
import jimm.ui.*;
import jimm.util.ResourceBundle;
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
import jimm.chat.TextFieldEx;
//#sijapp cond.end#

import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import java.util.Vector;

public class Templates implements VirtualListCommands, CommandListener, SelectListener, MenuListener {

    private static final byte MENU_NEW = (byte) 0;
    private static final byte MENU_EDIT = (byte) 1;
    private static final byte MENU_DELETE = (byte) 2;
    private static final byte MENU_CLEAR = (byte) 3;

    private static final int TMPL_CLALL = 2;
    private int caretPos;

    private Command addCommand;
    private Command editCommand;
    private TextList templateList;
    private TextBox templateTextbox;
    private Object lastScreen;
    private Object object;

    public Vector templates = new Vector();

    public Templates() {
        addCommand = new Command(ResourceBundle.getString("save"), Command.OK, 1);
        editCommand = new Command(ResourceBundle.getString("save"), Command.OK, 1);
        load();
    }

    public void selectTemplate(Object o, Object lastScreen) {
        this.lastScreen = lastScreen;
        object = o;
        if (o instanceof TextBox) {
            caretPos = ((TextBox) o).getCaretPosition();
//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2"#
        } else if (o instanceof TextField) {
            caretPos = ((TextField) o).getCaretPosition();
//#sijapp cond.end#
        }

        templateList = new TextList(null);
        templateList.setColorScheme();
        templateList.setCaption(ResourceBundle.getString("templates"));
        templateList.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
        templateList.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
        templateList.setFontSize(Font.SIZE_SMALL);
        refreshList();

        templateList.setCommandListener(this);
        templateList.setVLCommands(this);
        templateList.activate();
    }

    private void showMenu() {
        Menu menu = new Menu(templateList);
        //menu.addMenuItem("select", MENU_SELECT);
        menu.addMenuItem("add_new", MENU_NEW);
        if (templates.size() > 0) {
            menu.addMenuItem("start_edit", MENU_EDIT);
            menu.addMenuItem("delete", MENU_DELETE);
            menu.addMenuItem("clear", MENU_CLEAR);
        }
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        menu.back();
        switch (action) {
            //case MENU_SELECT:
                //select();
                //break;

            case MENU_NEW:
                templateTextbox = new TextBox(ResourceBundle.getString("new_template"), null, 1000, TextField.ANY);
                templateTextbox.addCommand(addCommand);
                templateTextbox.addCommand(JimmUI.cmdCancel);
                templateTextbox.setCommandListener(this);
                Jimm.setDisplay(templateTextbox);
                break;

            case MENU_EDIT:
                templateTextbox = new TextBox(ResourceBundle.getString("start_edit"), getTemlate(), 1000, TextField.ANY);
                templateTextbox.addCommand(editCommand);
                templateTextbox.addCommand(JimmUI.cmdCancel);
                templateTextbox.setCommandListener(this);
                Jimm.setDisplay(templateTextbox);
                break;

            case MENU_DELETE:
                templates.removeElementAt(templateList.getCurrTextIndex());
                refresh();
                break;

            case MENU_CLEAR:
                Jimm.setDisplay(new Select(ResourceBundle.getString("templates"), ResourceBundle.getString("clear") + "?", this, TMPL_CLALL, null));
                break;
        }
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
        select();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
            Jimm.setDisplay(lastScreen);
            templateList = null;
        } else if (c == addCommand) {
            String text = templateTextbox.getString();
            if (text.length() > 0) {
                templates.addElement(text);
            }
            refresh();
        } else if (c == editCommand) {
            String text = templateTextbox.getString();
            if (text.length() > 0) {
                templates.setElementAt(text, templateList.getCurrTextIndex());
            } else {
                templates.removeElementAt(templateList.getCurrTextIndex());
            }
            refresh();
        } else if (c == JimmUI.cmdCancel) {
            templateList.activate();
            templateTextbox = null;
        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        }
    }

    public void selectAction(int action, int selectType, Object o) {
        if (action != TMPL_CLALL) {
            return;
        }
        if (selectType == Select.SELECT_OK) {
            templates.removeAllElements();
            save();
            templateList.invalidate();
        }
    }

    private void select() {
        String selectedTemplate;
        if (templateList == null || templateList.getSize() == 0) {
            return;
        }
        selectedTemplate = getTemlate();
        sort();
        templateList = null;
        Jimm.setDisplay(lastScreen);
        if (selectedTemplate != null) {
            if (object instanceof TextBox) {
                ((TextBox) object).insert(selectedTemplate, caretPos);
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
            } else if (object instanceof TextField) {
                ((TextField) object).insert(selectedTemplate, caretPos);
            } else {
                ((TextFieldEx) object).insert(selectedTemplate);
//#sijapp cond.end#
            }
        }
        object = null;
    }

    private void sort() {
        String text = (String) templates.elementAt(templateList.getCurrTextIndex());
        String oldtext;
        for (int j = templateList.getCurrTextIndex(); j >= 1; j--) {
            oldtext = (String) templates.elementAt(j - 1);
            templates.setElementAt(oldtext, j);
        }
        templates.setElementAt(text, 0);
        save();
    }

    private void refreshList() {
        templateList.lock();
        templateList.clear();
        int count = templates.size();
        for (int i = 0; i < count; i++)
            templateList.addBigText((String) templates.elementAt(i), templateList.getTextColor(), Font.STYLE_PLAIN, i).doCRLF(i);
        templateList.unlock();
    }

    private void load() {
        RecordStore rms = null;
        templates.removeAllElements();
        try {
            rms = RecordStore.openRecordStore("tmpl", false);
            int size = rms.getNumRecords();
            byte[] data;
            for (int i = 1; i <= size; i++) {
                data = rms.getRecord(i);
                String str = Util.byteArrayToString(data, 0, data.length, true);
                templates.addElement(str);
            }
        } catch (Exception ignored) {
        }
        try {
            rms.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    private void save() {
        try {
            RecordStore.deleteRecordStore("tmpl");
        } catch (Exception ignored) {
        }
        if (templates.size() == 0) return;
        RecordStore rms = null;
        try {
            rms = RecordStore.openRecordStore("tmpl", true);
            int size = templates.size();
            byte[] buffer;
            String str;
            for (int i = 0; i < size; i++) {
                str = (String) templates.elementAt(i);
                buffer = Util.stringToByteArray(str, true);
                rms.addRecord(buffer, 0, buffer.length);
            }
        } catch (Exception ignored) {
        }
        try {
            rms.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    public String getTemlate() {
        return (String) templates.elementAt(templateList.getCurrTextIndex());
    }

    private void refresh() {
        save();
        refreshList();
        templateList.activate();
        templateTextbox = null;
    }

    public static void addFromChat(String text) {
        Templates templates = new Templates();
        templates.templates.addElement(text);
        templates.save();
    }
}