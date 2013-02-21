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
 File: src/jimm/Search.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm;

import DrawControls.*;
import jimm.comm.Icq;
import jimm.comm.SearchAction;
import jimm.comm.Util;
import jimm.forms.FormEx;
import jimm.info.Info;
import jimm.info.UserInfo;
import jimm.ui.*;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class Search {
    private SearchForm searchForm;

    private boolean liteVersion;
    final public static int UIN = 0;
    final public static int NICK = 1;
    final public static int FIRST_NAME = 2;
    final public static int LAST_NAME = 3;
    final public static int EMAIL = 4;
    final public static int CITY = 5;
    final public static int KEYWORD = 6;
    final public static int GENDER = 7;
    final public static int ONLY_ONLINE = 8;
    final public static int AGE = 9;
    final public static int LAST_INDEX = 10;

    private Object prvScreen;
    private Icq icq;

    private Vector results;

    public Search(boolean liteVersion, Icq icq) {
        this.icq = icq;
        this.results = new Vector();
        this.liteVersion = liteVersion;
        this.prvScreen = Jimm.getCurrentDisplay();
    }

    public void addResult(String uin, String nick, String name, String email, String auth, int status, String gender, int age) {
        String[] resultData = new String[Info.UI_LAST_ID];

        resultData[Info.UI_UIN_LIST] = uin;
        resultData[Info.UI_NICK] = nick;
        resultData[Info.UI_NAME] = name;
        resultData[Info.UI_EMAIL] = email;
        resultData[Info.UI_AUTH] = auth;
        resultData[Info.UI_STATUS] = Integer.toString(status);
        resultData[Info.UI_GENDER] = gender;
        resultData[Info.UI_AGE] = Integer.toString(age);

        this.results.addElement(resultData);
    }

    public String[] getResult(int nr) {
        return (String[]) results.elementAt(nr);
    }

    public int size() {
        return results.size();
    }

    public SearchForm getSearchForm() {
        if (searchForm == null) {
            searchForm = new SearchForm();
        }
        return searchForm;
    }

    /* Class for the search forms */
    public class SearchForm implements CommandListener, VirtualListCommands, MenuListener {
        private final static byte MENU_ADDUSR = (byte) 0;
        private final static byte MENU_INFO = (byte) 1;
        private final static byte MENU_NEXTUSR = (byte) 2;
        private final static byte MENU_PREVUSR = (byte) 3;
        private final static byte MENU_SENDMSG = (byte) 4;

        private Command searchCommand;
        private Command addCommand;
        private FormEx searchForm;
        private UserInfo screen;
        //private ChoiceGroupEx groupList;
        private LineChoise groups;
        private TextField uinSearchTextBox;
        private TextField nickSearchTextBox;
        private TextField firstnameSearchTextBox;
        private TextField lastnameSearchTextBox;
        private TextField emailSearchTextBox;
        private TextField citySearchTextBox;
        private TextField keywordSearchTextBox;
        //private ChoiceGroupEx chgrAge;
        private LineChoise age;
        //private ChoiceGroupEx gender;
        private LineChoise gender;
        //private ChoiceGroupEx onlyOnline;
        private LineChoiseBoolean onlyOnline;
        int selectedIndex;

        public SearchForm() {
            searchCommand = new Command(ResourceBundle.getString("user_search"), Command.OK, 1);
            addCommand = new Command(ResourceBundle.getString("add_to_list"), Command.ITEM, 1);

            searchForm = new FormEx(ResourceBundle.getString("search_user"), searchCommand, JimmUI.cmdBack);

            uinSearchTextBox = new TextField(ResourceBundle.getString("uin"), "", 32, TextField.NUMERIC);
            nickSearchTextBox = new TextField(ResourceBundle.getString("nick"), "", 32, TextField.ANY);
            firstnameSearchTextBox = new TextField(ResourceBundle.getString("firstname"), "", 32, TextField.ANY);
            lastnameSearchTextBox = new TextField(ResourceBundle.getString("lastname"), "", 32, TextField.ANY);
            emailSearchTextBox = new TextField(ResourceBundle.getString("email"), "", 32, TextField.EMAILADDR);
            citySearchTextBox = new TextField(ResourceBundle.getString("city"), "", 32, TextField.ANY);
            keywordSearchTextBox = new TextField(ResourceBundle.getString("keyword"), "", 32, TextField.ANY);

            //chgrAge = new ChoiceGroupEx(ResourceBundle.getString("age"), ChoiceGroup.EXCLUSIVE, Util.explode("---|13-17|18-22|23-29|30-39|40-49|50-59|> 60", '|'), null);
            age = new LineChoise(ResourceBundle.getString("age"), Util.explode("---|13-17|18-22|23-29|30-39|40-49|50-59|> 60", '|'));

            gender = new LineChoise(ResourceBundle.getString("gender"),
                    new String[]{
                            ResourceBundle.getString("female_male"),
                            ResourceBundle.getString("female"),
                            ResourceBundle.getString("male")
                    }
            );
            //gender = new ChoiceGroupEx(ResourceBundle.getString("gender"), Choice.EXCLUSIVE);
            //gender.append(ResourceBundle.getString("female_male"), null);
            //gender.append(ResourceBundle.getString("female"), null);
            //gender.append(ResourceBundle.getString("male"), null);
            onlyOnline = new LineChoiseBoolean(ResourceBundle.getString("only_online"), false);
            //onlyOnline = new ChoiceGroupEx("", Choice.MULTIPLE);
            //onlyOnline.append(ResourceBundle.getString("only_online"), null);


            searchForm.append(uinSearchTextBox);
            searchForm.append(nickSearchTextBox);
            searchForm.append(firstnameSearchTextBox);
            searchForm.append(lastnameSearchTextBox);
            searchForm.append(citySearchTextBox);
            searchForm.append(emailSearchTextBox);
            searchForm.append(keywordSearchTextBox);
            //searchForm.append(chgrAge);
            searchForm.append(gender);
            searchForm.append(age);
            searchForm.append(onlyOnline);
            searchForm.setCommandListener(this);

            screen = new UserInfo();
            screen.setMode(TextList.MODE_TEXT);
            screen.setColorScheme();
            screen.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
            screen.setCommandListener(this);
            screen.setVLCommands(this);
        }

        static final public int ACTIV_SHOW_RESULTS = 1;
        static final public int ACTIV_JUST_SHOW = 2;
        static final public int ACTIV_SHOW_NORESULTS = 3;

        public void activate(int type) {
            switch (type) {
                case ACTIV_SHOW_RESULTS:
                    drawResultScreen(selectedIndex);
                    Jimm.setDisplay(screen);
                    break;

                case ACTIV_JUST_SHOW:
                    Jimm.setDisplay(searchForm);
                    break;

                case ACTIV_SHOW_NORESULTS:
                    Alert alert = new Alert(null, ResourceBundle.getString("no_results"), null, null);
                    alert.setTimeout(Alert.FOREVER);
                    searchForm.activate(alert);
                    break;
            }
        }

        public void drawResultScreen(int n) {
            screen.clear();
            if (Search.this.size() > 0) {
                screen.lock();
                screen.fillUserInfo(getResult(n));
                screen.setCaption(ResourceBundle.getString("results") + " " + Integer.toString(n + 1) + "/" + Integer.toString(Search.this.size()));
                screen.unlock();
            } else {
                screen.lock();
                screen.setCaption(ResourceBundle.getString("results") + " 0/0");
                screen.addBigText(ResourceBundle.getString("no_results") + ": ", 0x0, Font.STYLE_BOLD, -1);
                screen.unlock();
            }
            screen.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
            screen.setCommandListener(this);
        }

        public void nextOrPrev(boolean next) {
            if (next) {
                selectedIndex = (selectedIndex + 1) % Search.this.size();
                this.activate(Search.SearchForm.ACTIV_SHOW_RESULTS);
            } else {
                if (selectedIndex == 0) {
                    selectedIndex = Search.this.size() - 1;
                } else {
                    selectedIndex = (selectedIndex - 1) % Search.this.size();
                }
                this.activate(Search.SearchForm.ACTIV_SHOW_RESULTS);
            }
        }

        private void back() {
            /*if (!(NativeCanvas.getLPAction() instanceof SearchAction)) {
                searchForm = null;
            }*/
            Jimm.setDisplay(prvScreen);
        }

        public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
            if (type == CanvasEx.KEY_PRESSED) {
                switch (gameAct) {
                    case Canvas.LEFT:
                        nextOrPrev(false);
                        break;

                    case Canvas.RIGHT:
                        nextOrPrev(true);
                        break;
                }
            }
        }

        public void vlCursorMoved(VirtualList sender) {
        }

        public void vlItemClicked(VirtualList sender) {
        }

        private void showMenu() {
            Menu menu = new Menu(screen);
            if (Search.this.size() > 1) {
                menu.addMenuItem("prev", MENU_PREVUSR);
                menu.addMenuItem("next", MENU_NEXTUSR);
            }
            menu.addMenuItem("add_to_list", MENU_ADDUSR);
            menu.addMenuItem("send_message", MENU_SENDMSG);
            menu.addMenuItem("info", MENU_INFO);
            menu.setMenuListener(this);
            Jimm.setDisplay(menu);
        }

        public void menuSelect(Menu menu, byte action) {
            switch (action) {
                case MENU_NEXTUSR:
                case MENU_PREVUSR:
                    nextOrPrev(action == MENU_NEXTUSR);
                    break;

                case MENU_ADDUSR:
                    if (icq.getProfile().getGroupItems().length == 0) {
						JimmException.handleException(new JimmException(161, 0, true));
                        //Alert errorMsg = new Alert(ResourceBundle.getString("warning"), JimmException.getErrDesc(161, 0), null, AlertType.WARNING);
                        //errorMsg.setTimeout(Alert.FOREVER);
                        //Jimm.setDisplay(errorMsg);
                    } else {
                        //groupList = new ChoiceGroupEx("", Choice.EXCLUSIVE);
                        FormEx form = new FormEx(ResourceBundle.getString("whichgroup"), addCommand, JimmUI.cmdBack);
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
                        groups = new LineChoise(ResourceBundle.getString("whichgroup"), names);
                        form.setCommandListener(this);
                        //form.append(groupList);
                        form.append(groups);
                        Jimm.setDisplay(form);
                    }
                    break;

                case MENU_SENDMSG:
                    String[] resultData0 = getResult(selectedIndex);
                    ContactItem cItem = icq.getProfile().createTempContact(resultData0[Info.UI_UIN_LIST]);
                    cItem.setStringValue(ContactItem.CONTACTITEM_NAME, resultData0[Info.UI_NICK]);
                    JimmUI.writeMessage(cItem, null).activate();
                    break;

                case MENU_INFO:
                    String[] resultData1 = getResult(selectedIndex);
                    Jimm.setPrevScreen(screen);
                    (new UserInfo()).requiestUserInfo(icq.getProfile(), resultData1[Info.UI_UIN_LIST], resultData1[Info.UI_NICK]);
                    break;
            }
        }

        public void commandAction(Command c, Displayable d) {
            if (c == JimmUI.cmdBack) {
                if (JimmUI.isControlActive(screen) && !liteVersion) {
                    activate(Search.SearchForm.ACTIV_JUST_SHOW);
                } else if ((this.groups != null) && (d == null)) {
                    Object o = Jimm.getCurrentDisplay();
                    if (o instanceof FormEx && !o.equals(searchForm)) {
                        if (c == addCommand) {
                            String[] resultData = getResult(selectedIndex);
                            ContactItem cItem =
                                    new ContactItem(-1,
                                            icq.getProfile().getGroupItems()[groups.getSelected()].getId(),
                                            resultData[Info.UI_UIN_LIST],
                                            resultData[Info.UI_NICK],
                                            resultData[Info.UI_AUTH].equals("1"),
                                            false,
                                            icq.getProfile()
                                    );
                            cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, resultData[Info.UI_AUTH].equals("1"));//было true....//вернул true (вроде из-за того был баг с добалением/авторизацией)
                            cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, ContactItem.STATUS_OFFLINE);
                            icq.addToContactList(cItem);
                        } else {
                            Jimm.setDisplay(screen);
                        }
                    } else {
                        back();
                    }
                } else {
                    back();
                }
            } else if (c == JimmUI.cmdMenu) {
                showMenu();
            } else if (c == searchCommand) {
                selectedIndex = 0;

                String[] data = new String[Search.LAST_INDEX];

                data[Search.UIN] = uinSearchTextBox.getString();
                data[Search.NICK] = nickSearchTextBox.getString();
                data[Search.FIRST_NAME] = firstnameSearchTextBox.getString();
                data[Search.LAST_NAME] = lastnameSearchTextBox.getString();
                data[Search.EMAIL] = emailSearchTextBox.getString();
                data[Search.CITY] = citySearchTextBox.getString();
                data[Search.KEYWORD] = keywordSearchTextBox.getString();
                data[Search.GENDER] = Integer.toString(gender.getSelected());
                data[Search.ONLY_ONLINE] = onlyOnline.getBooolean() ? "1" : "0";
                //data[Search.AGE] = Integer.toString(chgrAge.getSelectedIndex());
                data[Search.AGE] = Integer.toString(age.getSelected());

                SearchAction act = new SearchAction(Search.this, data, SearchAction.CALLED_BY_SEARCHUSER);
                try {
                    icq.requestAction(act);
                } catch (JimmException e) {
                    JimmException.handleException(e);
                    if (e.isCritical()) {
                        return;
                    }
                }
                results.removeAllElements();
                icq.getProfile().addAction("wait", act);
            } else if (c == addCommand) {
                String[] resultData = getResult(selectedIndex);
                ContactItem cItem = new ContactItem(-1, icq.getProfile().getGroupItems()[groups.getSelected()].getId(),
                        resultData[Info.UI_UIN_LIST], resultData[Info.UI_NICK], resultData[Info.UI_AUTH].equals("1"), false, icq.getProfile());
                cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, resultData[Info.UI_AUTH].equals("1"));//было true....//вернул true (вроде из-за того был баг с добалением/авторизацией)
                cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, ContactItem.STATUS_OFFLINE);
                icq.addToContactList(cItem);
            }
        }
    }
}
