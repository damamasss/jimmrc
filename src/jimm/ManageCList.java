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
 File: src/jimm/ManageCList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import DrawControls.TextList;
import jimm.comm.Action;
import jimm.comm.Icq;
import jimm.comm.UpdateContactListAction;
import jimm.comm.Util;
import jimm.forms.FormEx;
import jimm.ui.SelectListener;
import jimm.ui.LineChoise;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;

public class ManageCList extends TextList implements CommandListener, SelectListener {
    private static final int TAG_RENAME_GROUPS = 2;
    private static final int TAG_DELETE_GROUPS = 3;
    private byte actions = -1;

    private Command sendCommand;

    private static final int STATUS_NONE = 0;
    private static final int STATUS_ADD_GROUP = 1;
    private static final int STATUS_RENAME_GROUP = 2;
    private static int status = STATUS_NONE;

    private int[] groups;
    private int lastSelected;
    public FormEx textBoxForm;
    private Icq icq;
    private TextField uinTextField;

    public ManageCList(Icq icq) {
        super(ResourceBundle.getString("manage_contact_list"));
        this.icq = icq;
        setMode(MODE_TEXT);
        JimmUI.addTextListItem(this, "search_user", ContactList.menuIcons.elementAt(2), 0, true);
        JimmUI.addTextListItem(this, "add_contact", ContactList.menuIcons.elementAt(29), 1, true);
        JimmUI.addTextListItem(this, "add_group", ContactList.menuIcons.elementAt(29), 2, true);
        JimmUI.addTextListItem(this, "rename_group", ContactList.menuIcons.elementAt(11), 3, true);
        JimmUI.addTextListItem(this, "del_group", ContactList.menuIcons.elementAt(6), 4, true);
        //String i = icq.toString();
        setColorScheme();
        setCommandListener(this);
        addCommandEx(JimmUI.cmdSelect, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        Jimm.setPrevScreen(this);
    }

    private void showTextBoxForm(String caption, String label, String text, int fieldType) {
        sendCommand = new Command(ResourceBundle.getString("send"), Command.OK, 1);
        textBoxForm = new FormEx(ResourceBundle.getString(caption), sendCommand, JimmUI.cmdCancel);
        uinTextField = new TextField(ResourceBundle.getString(label), text, 20, fieldType);
        textBoxForm.append(uinTextField);

        textBoxForm.setCommandListener(this);
        Jimm.setDisplay(textBoxForm);
    }

    public void commandAction(Command c, Displayable d) {
        if (JimmUI.isControlActive(this)) {
            if (c == JimmUI.cmdSelect) {
                CLManagementItemSelected(getCurrTextIndex());
            } else if (c == JimmUI.cmdBack) {
                //Jimm.getContactList().showMenu(false);
                Jimm.getContactList().activate();
            }
            return;
        }

        if (c == JimmUI.cmdBack) {
            Jimm.getContactList().activate();
        } else if ((c == sendCommand) && JimmUI.isControlActive(textBoxForm)) {
            Action act = null;

            switch (status) {
                case STATUS_ADD_GROUP:
                    GroupItem newGroup = new GroupItem(uinTextField.getString(), icq);
                    act = new UpdateContactListAction(newGroup, UpdateContactListAction.ACTION_ADD);
                    break;

                case STATUS_RENAME_GROUP:
                    GroupItem group = icq.getProfile().getGroupById(groups[lastSelected]);
                    group.setName(uinTextField.getString());
                    icq.getProfile().safeSave();
                    act = new UpdateContactListAction(group, UpdateContactListAction.ACTION_RENAME);
                    break;
            }

            status = STATUS_NONE;

            try {
                icq.requestAction(act);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    return;
                }
            }
            Jimm.getContactList().activate();
            icq.getProfile().addAction("wait", act);
        } else if ((c == JimmUI.cmdCancel) && JimmUI.isControlActive(textBoxForm)) {
            Jimm.getContactList().activate();
            textBoxForm = null;
        }
    }

    public void selectAction(int action, int selectType, Object o) {
        if (actions == TAG_RENAME_GROUPS) {
            String groupName = icq.getProfile().getGroupById(groups[selectType]).getName();
            lastSelected = selectType;
            showTextBoxForm("rename_group", "group_name", groupName, TextField.ANY);
        } else if (actions == TAG_DELETE_GROUPS) {
            UpdateContactListAction deleteGroupAct = new UpdateContactListAction(icq.getProfile().getGroupById(groups[selectType]), UpdateContactListAction.ACTION_DEL);
            try {
                Jimm.getContactList().activate();
                icq.requestAction(deleteGroupAct);
                icq.getProfile().addAction("wait", deleteGroupAct);
            } catch (JimmException e) {
                JimmException.handleException(e);
            }
        }
    }

    public void CLManagementItemSelected(int index) {
        switch (index) {
            case 0: /* Search for or Add User */
                Search searchf = new Search(false, icq);
                searchf.getSearchForm().activate(Search.SearchForm.ACTIV_JUST_SHOW);
                break;

            case 1: /* Add User */
                if (icq.getProfile().getGroupItems().length == 0) {
					JimmException.handleException(new JimmException(161, 0, true));
                    //Alert errorMsg = new Alert(ResourceBundle.getString("warning"), JimmException.getErrDesc(161, 0), null, AlertType.WARNING);
                    //errorMsg.setTimeout(Alert.FOREVER);
                    //Jimm.setDisplay(errorMsg);
                    return;
                }
                FormEx form = new FormEx(ResourceBundle.getString("add_contact"), JimmUI.cmdOk, JimmUI.cmdBack);
                final TextField uin = new TextField(ResourceBundle.getString("uin"), null, 12, TextField.NUMERIC);
                final TextField name = new TextField(ResourceBundle.getString("name"), null, 32, TextField.ANY);
                StringBuffer sb = new StringBuffer();
                GroupItem[] grs = icq.getProfile().getGroupItems();
                int count = grs.length;
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("|");
                    }
                    sb.append(grs[i].getName());
                }
                String[] names = Util.explode(sb.toString(), '|');
                final LineChoise groupes = new LineChoise(ResourceBundle.getString("whichgroup"), names);
                form.setCommandListener(new CommandListener() {
                    public void commandAction(Command c, Displayable d) {
                        if (c == JimmUI.cmdOk) {
                            try {
                                ContactItem cItem =
                                        new ContactItem(-1,
                                                icq.getProfile().getGroupItems()[groupes.getSelected()].getId(),
                                                uin.getString(),
                                                name.getString(),
                                                true,
                                                false,
                                                icq.getProfile()
                                        );
                                cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, true);
                                cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, ContactItem.STATUS_OFFLINE);
                                icq.addToContactList(cItem);
                            } catch (Exception e) {
                                //Jimm.getContactList().activate();
                            }
                        } else {
                            Jimm.getContactList().activate();
                        }
                    }
                });
                form.append(groupes);
                form.append(uin);
                form.append(name);
                Jimm.setDisplay(form);
                break;

            case 2: /* Add group */
                status = STATUS_ADD_GROUP;
                showTextBoxForm("add_group", "group_name", null, TextField.ANY);
                break;

            case 3: /* Rename group */
                status = STATUS_RENAME_GROUP;
                actions = TAG_RENAME_GROUPS;
                groups = JimmUI.showGroupSelector(icq.getProfile(), this, JimmUI.SHS_TYPE_ALL, -1);
                break;

            case 4: /* Delete group */
                actions = TAG_DELETE_GROUPS;
                groups = JimmUI.showGroupSelector(icq.getProfile(), this, JimmUI.SHS_TYPE_EMPTY, -1);
                break;
        }
    }
}