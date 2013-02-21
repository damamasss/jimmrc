package jimm.info;

import jimm.*;
import jimm.comm.Icq;
import jimm.comm.RequestInfoAction;
import jimm.ui.Menu;
import jimm.ui.MenuListener;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;

public class UserInfo extends Info implements CommandListener, MenuListener {
    /**
     * @author Shamsutdinov Rishat
     */
    private final static byte MENU_EDIT = (byte) 0;
    //private final static byte MENU_NEWPASS = (byte) 1;
    private final static byte MENU_UPDATENICK = (byte) -1;

    static private String[] last_user_info = new String[UI_LAST_ID];
    private ContactItem cItem;
    private int preSize;
    private Icq icq;

    public void fillUserInfo(String[] data) {
        uiSectName = "main_info";
        addToTextList(UI_UIN_LIST, data, "uin");
        addToTextList(UI_NICK, data, "nick");
        addToTextList(UI_NAME, data, "name");
        addToTextList(UI_GENDER, data, "gender");
        addToTextList(UI_AGE, data, "age");
        addToTextList(UI_EMAIL, data, "email");
        if (data[UI_AUTH] != null) {
            addToTextList(data[UI_AUTH].equals("1") ? ResourceBundle.getString("yes")
                    : ResourceBundle.getString("no"), "auth");
        }
        addToTextList(UI_BDAY, data, "birth_day");
        if ((data[UI_BDAY] != null) && (data[UI_BDAY].length() != 0) && (cItem != null))
            cItem.rememberBirthDay(data[UI_BDAY].substring(0, data[UI_BDAY].length() - 5));
        addToTextList(UI_CPHONE, data, "cell_phone");
        addToTextList(UI_HOME_PAGE, data, "home_page");
        addToTextList(UI_ABOUT, data, "notes");
        if ((data[UI_INETRESTS] != null) && (data[UI_INETRESTS].length() != 0)) uiSectName = "interests";
        addToTextList(UI_INT_CAT1, data);
        addToTextList(UI_INT_CAT2, data);
        addToTextList(UI_INT_CAT3, data);
        addToTextList(UI_INT_CAT4, data);

        if (data[UI_STATUS] != null) {
            int stat = Integer.parseInt(data[UI_STATUS]);
            int imgIndex = 0;
            if (stat == 0) {
                imgIndex = 6;
            } else if (stat == 1) {
                imgIndex = 7;
            } else if (stat == 2) {
                imgIndex = 3;
            }
            addBigText(ResourceBundle.getString("status") + ": ", getTextColor(), Font.STYLE_PLAIN, uiBigTextIndex)
                    .addImage(ContactList.imageList.elementAt(imgIndex), null, uiBigTextIndex).doCRLF(uiBigTextIndex);
            uiBigTextIndex++;
        }

        uiSectName = "home_info";
        addToTextList(UI_CITY, data, "city");
        addToTextList(UI_STATE, data, "state");
        addToTextList(UI_ADDR, data, "addr");
        addToTextList(UI_PHONE, data, "phone");
        addToTextList(UI_FAX, data, "fax");

        uiSectName = "work_info";
        addToTextList(UI_W_NAME, data, "title");
        addToTextList(UI_W_DEP, data, "depart");
        addToTextList(UI_W_POS, data, "position");
        addToTextList(UI_W_CITY, data, "city");
        addToTextList(UI_W_STATE, data, "state");
        addToTextList(UI_W_ADDR, data, "addr");
        addToTextList(UI_W_PHONE, data, "phone");
        addToTextList(UI_W_FAX, data, "fax");

        uiSectName = "city_original";
        addToTextList(UI_ORIG_CITY, data, "city");
        addToTextList(UI_ORIG_STATE, data, "state");

        if (cItem != null && cItem.getStringValue(ContactItem.CONTACTITEM_NOTES).length() > 0) {
            uiSectName = "note";
            addToTextList(cItem.getStringValue(ContactItem.CONTACTITEM_NOTES), "");
        }
    }

    public void requiestUserInfo(Profile profile, String uin, String name) {
        icq = profile.getIcq();
        cItem = profile.getItemByUIN(uin);
        initInfoTextList(uin, false, this);

        if (icq.isConnected()) {
            RequestInfoAction act = new RequestInfoAction(uin, name, this);

            addCommandEx(JimmUI.cmdCancel, MENU_TYPE_RIGHT_BAR);
            addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);

            try {
                icq.requestAction(act);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    return;
                }
            }

            add(ResourceBundle.getString("wait"));
            preSize = getSize();

            activate();
        } else {
            String[] data = new String[UI_LAST_ID];
            data[UI_NICK] = name;
            data[UI_UIN_LIST] = uin;
            preSize = getSize();
            showUserInfo(data);
            activate();
        }
        setCommandListener(this);
    }

    public void showUserInfo(String[] data) {
        last_user_info = data;
        if ((!JimmUI.isControlActive(this) && icq.isConnected()) || (getSize() != preSize)) {
            return;
        }
        lock();
        clear();
        fillUserInfo(data);
        removeCommandEx(JimmUI.cmdCancel);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        unlock();
    }

    private void showMenu() {
        Menu menu = new Menu(this);
        if (icq.getUin().equals(getCaption())) {
            menu.addMenuItem("start_edit", MENU_EDIT);
            //menu.addMenuItem("change_pass", MENU_NEWPASS);
        } else {
            menu.addMenuItem("update_nick", MENU_UPDATENICK);
        }
        menu.addMenuItem("copy_text", MENU_COPYTEXT);
        menu.addMenuItem("copy_uin", MENU_COPYUIN);
        menu.addMenuItem("copy_all_text", MENU_COPY_ALLTEXT);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case MENU_EDIT:
                try {
                    EditInfo.showEditForm(last_user_info, this, icq);
                } catch (Exception e) {
                    JimmException.handleExceptionEx(e);
                }
                return;

            /*case MENU_NEWPASS:
                try {
                    EditInfo.showChangePass(this, icq);
                } catch (Exception e) {
                    JimmException.handleException(e, true);
                }
                return;*/

            case MENU_COPYTEXT:
            case MENU_COPY_ALLTEXT:
                JimmUI.setClipBoardText
                        (
                                "[" + getCaption() + "]\n" + getCurrText(0, (action == MENU_COPY_ALLTEXT))
                        );
                break;

            case MENU_UPDATENICK:
                if (cItem != null) {
                    cItem.rename(last_user_info[UI_NICK]);
                }
                break;

            case MENU_COPYUIN:
                if (cItem != null) {
                    JimmUI.setClipBoardText(cItem.getUinString());
                }
                break;
        }
        menu.back();
    }

    public void commandAction(Command c, Displayable d) {
        if ((c == JimmUI.cmdCancel) || (c == JimmUI.cmdBack)) {
            Jimm.back();
        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        }
    }
}