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
 Author(s): Shamsutdinov Rishat
 *******************************************************************************/

// #sijapp cond.if modules_MAGIC_EYE is "true" #
package jimm;

import DrawControls.CanvasEx;
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;
import jimm.comm.DateAndTime;
import jimm.comm.Util;
import jimm.ui.Menu;
import jimm.ui.MenuListener;
import jimm.ui.LineChoiseBoolean;
import jimm.util.ResourceBundle;
import jimm.forms.FormEx;

import javax.microedition.lcdui.*;
import java.util.TimerTask;
import java.util.Vector;

public class MagicEye extends TextList implements CommandListener, MenuListener, VirtualListCommands {

    public static final byte OPTION_READ_STATUS = 1 << 0;
    public static final byte OPTION_READ_XTRAZ = 1 << 1;
    public static final byte OPTION_ANTISPAM = 1 << 2;
    public static final byte OPTION_MAYBE_INVISIBLE = 1 << 3;
    public static final byte OPTION_STATUS_INVISIBLE = 1 << 4;
    public static final byte OPTION_CITEM_ONLINE = 1 << 5;
    public static final byte OPTION_CITEM_OFFLINE = 1 << 6;

    class OptionsEye implements CommandListener {

        //private LineChoiseBoolean active;
        private LineChoiseBoolean[] items;
        private MagicEye eye;

        public OptionsEye(MagicEye magicEye) {
            eye = magicEye;
            Jimm.setDisplay(fillList(Options.getInt(Options.OPTION_MAGIC_EYE)));
        }

        private FormEx fillList(int values) {
            FormEx form = new FormEx(ResourceBundle.getString("options_lng"), JimmUI.cmdSave, JimmUI.cmdBack);
            form.removeCommand(JimmUI.cmdSave);
            initItems(form, getNames(), toByteArray(values));
            form.setCommandListener(this);
            return form;
        }

        private void initItems(FormEx form, String name, boolean[] values) {
            String[] names = Util.explode(name, '|');
            items = new LineChoiseBoolean[names.length];
            int size = Math.min(names.length, values.length);
            for (int i = 0; i < size; i++) {
                items[i] = new LineChoiseBoolean(ResourceBundle.getString(names[i]), values[i]);
                form.append(items[i]);
            }
        }

        private void useItems(boolean[] values) {
            int size = Math.min(items.length, values.length);
            for (int i = 0; i < size; i++) {
                values[i] = items[i].getBooolean();
            }
        }

        private String getNames() {
            return "read status message" + "|" + "read xtraz" + "|" + "antispam" + "|" +
                    "maybe_invisible" + "|" + "status_invisible" + "|" +
                    "contact_online" + "|" + "contact_offline";
        }

        private boolean[] toByteArray(int values) {
            int size = Util.explode(getNames(), '|').length;
            boolean[] result = new boolean[size];
            int key;
            for (int i = 0; i < size; i++) {
                key = 1 << i;
                result[i] = (values & key) != 0;
            }
            return result;
        }

        private int getValues(boolean[] values) {
            byte result = 0;
            int size = values.length;
            for (int i = 0; i < size; i++) {
                result |= boolToInt(values[i]) << i;
            }
            return result;
        }

        private int boolToInt(boolean value) {
            return (value) ? 1 : 0;
        }

        public void commandAction(Command c, Displayable d) {
            boolean[] vals = new boolean[Util.explode(getNames(), '|').length];
            useItems(vals);
            Options.setInt(Options.OPTION_MAGIC_EYE, getValues(vals));
            Options.safe_save();
            if (eye != null) {
                Jimm.setDisplay(eye);
                return;
            }
            Jimm.back();
        }
    }

    private final static byte MENU_CONTACTMENU = (byte) 0;
    private final static byte MENU_DEBUGLOG = (byte) 1;
    private final static byte MENU_OPTIONS = (byte) 2;
    private final static byte MENU_COPYTEXT = (byte) 3;
    private final static byte MENU_COPY_ALLTEXT = (byte) 4;
    private final static byte MENU_COPYUIN = (byte) 5;
    private final static byte MENU_CLEAR = (byte) 6;

    private Vector uins = new Vector();
    private int idx = 0;
    private int invisiblePackets = 0;
    //private CanvasEx prvScreen;
    private Profile profile;

    public MagicEye(Profile profile) {
        super(ResourceBundle.getString("magic_eye"));
        this.profile = profile;
        setMode(VirtualList.MODE_LIST);
        setVLCommands(this);
        setColorScheme();
    }

    public void activateEx(CanvasEx prvScreen) {
        //this.prvScreen = prvScreen;
        removeAllCommands();
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        setCommandListener(this);
        selectTextByIndex(getSize() - 1);
        activate();
    }

    public void addAction(String uin, String action, String msg) {
        registerAction(uin, action, msg);
    }

    public void addAction(String uin, String action) {
        if (action.equals("maybe_invisible")) {
            if (invisiblePackets++ == 0) {
                Jimm.getTimerRef().schedule(initTimerTask(new Object[]{uin, action}), 5000);
            }
        } else {
            registerAction(uin, action, null);
        }
    }

    private void registerAction(String uin, String laction, String msg) {
        if (!profile.getBoolean(Profile.OPTION_ENABLE_MM)) {
            return;
        }
        boolean needAddCommands = uins.size() == 0;
        uins.addElement(uin);

        if (uins.size() >= 1 && needAddCommands) {
            addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        }
        ContactItem contact = profile.getItemByUIN(uin);
        String action = ResourceBundle.getString(laction);
        int counter = idx++;
        String date = DateAndTime.getDateString(true, false);
        int color = getColor(COLOR_CAP2);
        lock();

        if (contact == null) {
            addBigText("[" + counter + "]: " + uin + " (" + date + ")\n", color, Font.STYLE_BOLD, counter);
        } else {
            addBigText("[" + counter + "]: " + contact.name + " (" + date + ")\n", color, Font.STYLE_BOLD, counter);
        }
        color = getColor(COLOR_TEXT);

        if (contact != null) {
            if ((contact.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE) && (action.equals(ResourceBundle.getString("read xtraz")) ||
                    action.equals(ResourceBundle.getString("read status message"))) && !contact.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP)) {
                addBigText(ResourceBundle.getString("status_invisible"), color, Font.STYLE_PLAIN, counter);
                contact.setStatus(ContactItem.STATUS_INVISIBLE, true);
            } else {
                addBigText(action, color, Font.STYLE_PLAIN, counter);
            }
        } else {
            addBigText(action, color, Font.STYLE_PLAIN, counter);
        }

        if (msg != null) {
            doCRLF(counter);
            addBigText(msg, color, Font.STYLE_PLAIN, counter);
        }

        doCRLF(counter);
        unlock();
    }

    public void checkToInvis(Object[] lastPacket) {
        if (invisiblePackets == 1) {
            registerAction((String) lastPacket[0], (String) lastPacket[1], null);
        }
        invisiblePackets = 0;
    }

    private TimerTask initTimerTask(final Object[] lastPacket) {
        return new TimerTask() {
            public void run() {
                checkToInvis(lastPacket);
            }
        };
    }

    private void showMenu() {
        Menu menu = new Menu(this);
        if (uins.size() > 0) {
            menu.addMenuItem("user_menu", MENU_CONTACTMENU);
        }
//#sijapp cond.if modules_DEBUGLOG is "true"#
        menu.addMenuItem("*DebugList*", MENU_DEBUGLOG);
//#sijapp cond.end#
        if (uins.size() > 0) {
            menu.addMenuItem("copy_text", MENU_COPYTEXT);
            menu.addMenuItem("copy_uin", MENU_COPYUIN);
            menu.addMenuItem("copy_all_text", MENU_COPY_ALLTEXT);
        }
        menu.addMenuItem("options_lng", MENU_OPTIONS);
        if (uins.size() > 0) {
            menu.addMenuItem("clear", MENU_CLEAR);
        }
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case MENU_CONTACTMENU:
                if (uins.size() < 1) {
                    return;
                }
                String uincm = (String) uins.elementAt(getCurrTextIndex());
                ContactItem cItemcm;
                if (profile.getItemByUIN(uincm) == null) {
                    cItemcm = profile.createTempContact(uincm);
                } else {
                    cItemcm = profile.getItemByUIN(uincm);
                }
                Menu mc = new Menu(this, (byte) 1);
                JimmUI.fillContactMenu(cItemcm, mc);
                Jimm.setPrevScreen(getVisibleObject());
                Jimm.setDisplay(mc);
                return;
            //#sijapp cond.if modules_DEBUGLOG is "true"#
            case MENU_DEBUGLOG:
                DebugLog.activateEx();
                return;
            //#sijapp cond.end#
            case MENU_OPTIONS:
                new OptionsEye(this);
                return;
            case MENU_COPYTEXT:
            case MENU_COPY_ALLTEXT:
                JimmUI.setClipBoardText(getCurrText(0, (action == MENU_COPY_ALLTEXT)));
                break;

            case MENU_COPYUIN:
                JimmUI.setClipBoardText((String) uins.elementAt(getCurrTextIndex()));
                break;

            case MENU_CLEAR:
                boolean needRemoveCommands = uins.size() > 0;
                idx = 0;
                uins = null;
                uins = new Vector();
                lock();
                if (uins.size() == 0 && needRemoveCommands) {
                    removeCommandEx(JimmUI.cmdMenu);
                }
                clear();
                setColorScheme();
                unlock();
                break;
        }

        if (menu != null) {
            menu.back();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdMenu) {
            showMenu();
        } else {
            //if (prvScreen != null) {
            //    Jimm.setDisplay(prvScreen);
            //    prvScreen = null;
            //} else
            //{
            Jimm.getContactList().activate();
            //}
        }
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
        this.menuSelect(null, MENU_CONTACTMENU);
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        if (type == VirtualList.KEY_RELEASED) {
// #sijapp cond.if target is "MIDP2"#
            if (keyCode == -8) {
                menuSelect(null, MENU_CLEAR);
            }
// #sijapp cond.end#
        }
    }

    private Object getVisibleObject() {
        return this;
    }

    public static boolean getBooleanValue(int key) {
        return (Options.getInt(Options.OPTION_MAGIC_EYE) & key) != 0;
    }
}
// #sijapp cond.end#