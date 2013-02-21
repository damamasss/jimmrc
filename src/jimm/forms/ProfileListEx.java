package jimm.forms;

import jimm.*;
import jimm.util.ResourceBundle;
import jimm.ui.Menu;
import jimm.ui.MenuListener;
import DrawControls.*;

import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.util.Vector;

/**
 * Created [16.02.2011, 21:15:08]
 * Develop by Lavlinsky Roman on 2011
 */
public class ProfileListEx extends FormEx implements CommandListener, MenuListener {

    //private Vector profiles = new Vector();

    int pushing = -1;

    public ProfileListEx() {
        super(ResourceBundle.getString("profiles"), JimmUI.cmdMenu, JimmUI.cmdBack);
        update();
        setCommandListener(this);
    }

    private void update() {
        clear();
        int len = Options.nicks.size();
        String cur = Jimm.getCurrentProfile().getNick();
        String name;
        int curIdx = 0;
        for (int i = 0; i < len; i++) {
            name = "";
            try {
                name = (String) Options.nicks.elementAt(i);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            boolean exist = Profiles.profileExist(name);
            boolean current = cur.equals(name);
            int idx = (exist) ? JimmUI.getStatusImageIndex(Profiles.getProfile(name).getIcq().getCurrentStatus()) : 6;
            if ((exist) && (Profiles.getProfile(name).getUnreadMessCount() > 0)) {
                idx = 14;
            }
            addNode(null, new FormItem(null, (current ? " *" : " ") + name, ContactList.imageList.elementAt(idx)));
            if (current) {
                curIdx = i;
            }
        }
        setCurrentItem(curIdx);
    }

    private void fillBuffers(StringBuffer sb1, StringBuffer sb2, StringBuffer sb3, int i) {
        if (sb3.length() > 0) {
            sb1.append('\t');
            sb2.append('\t');
            sb3.append('\t');
        }
        sb1.append((String) Options.uins.elementAt(i));
        sb2.append((String) Options.passwords.elementAt(i));
        sb3.append((String) Options.nicks.elementAt(i));
    }

    private void setAccountOptions(String nick, byte act) {
        switch (act) {
            case 0:
                break;
            case 1:
                if (pushing <= 0) {
                    return;
                }
                break;
            case 2:
                if (Options.nicks.size() < 2 || pushing >= Options.nicks.size() - 1) {
                    return;
                }
                break;
            case 3:
                if (pushing >= Options.nicks.size() || Options.nicks.size() <= 1 || Jimm.getCurrentProfile().getNick().equals(Options.nicks.elementAt(pushing))) {
                    return;
                }
                break;
        }
        String s1 = null;
        String s2 = null;
        String s3 = null;
        StringBuffer sb1 = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        StringBuffer sb3 = new StringBuffer();

        for (int i = 0; i < Options.nicks.size(); i++) {
            switch (act) {
                case 0: //add
                    fillBuffers(sb1, sb2, sb3, i);
                    break;
                case 1://up
                    if (i == pushing - 1) {
                        s1 = (String) Options.uins.elementAt(i);
                        s2 = (String) Options.passwords.elementAt(i);
                        s3 = (String) Options.nicks.elementAt(i);
                        fillBuffers(sb1, sb2, sb3, i + 1);
                    } else if (i == pushing) {
                        if (i != 0) {
                            sb1.append('\t');
                            sb2.append('\t');
                            sb3.append('\t');
                        }
                        sb1.append(s1);
                        sb2.append(s2);
                        sb3.append(s3);
                    } else {
                        fillBuffers(sb1, sb2, sb3, i);
                    }
                    break;
                case 2://down
                    if (i == pushing) {
                        s1 = (String) Options.uins.elementAt(i);
                        s2 = (String) Options.passwords.elementAt(i);
                        s3 = (String) Options.nicks.elementAt(i);
                        fillBuffers(sb1, sb2, sb3, i + 1);
                    } else if (i == pushing + 1) {
                        if (i != 0) {
                            sb1.append('\t');
                            sb2.append('\t');
                            sb3.append('\t');
                        }
                        sb1.append(s1);
                        sb2.append(s2);
                        sb3.append(s3);
                    } else {
                        fillBuffers(sb1, sb2, sb3, i);
                    }
                    break;
                case 3://del
                    if (pushing != i) {
                        fillBuffers(sb1, sb2, sb3, i);
                    }
                    break;
            }
        }
        if (act == 0) {
            sb1.append('\t').append("");
            sb2.append('\t').append("");
            sb3.append('\t').append(nick);
            Profiles.getProfile(nick, true);
        }
        Options.setString(Options.OPTION_UIN, sb1.toString());
        Options.setString(Options.OPTION_PASSWORD, sb2.toString());
        Options.setString(Options.OPTION_MY_NICK, sb3.toString());
        Options.initAccounts();
        Options.safe_save();
        //Profiles.update();
        update();
    }

    protected void showMenu() {
        Menu menu = new Menu(this);
        menu.addMenuItem("add_to_list", (byte) 0);
        menu.addMenuItem("up", (byte) 1);
        menu.addMenuItem("bottom", (byte) 2);
        menu.addMenuItem("delete", (byte) 3);
        menu.addMenuItem("history_lng", (byte) 4);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case 0:
                TextBox tb = new TextBox(ResourceBundle.getString("prof_name"), "", 32, TextField.ANY);
                tb.addCommand(JimmUI.cmdBack);
                tb.addCommand(JimmUI.cmdOk);
                tb.setCommandListener(this);
                Jimm.setDisplay(tb);
                return;

            case 1:
            case 2:
            case 3:
                pushing = getCurrIndex();
                setAccountOptions(null, action);
                break;

            case 4:
                String[] all = RecordStore.listRecordStores();
                StringBuffer sb = new StringBuffer();
                //String pref = "hist" + Jimm.getCurrentProfile().getNick() + '_';
                String pref = "hist" +  ((String) Options.nicks.elementAt(getCurrIndex())) + '_';
                int preflen = pref.length();
                int idx;
                for (int i = 0; i < all.length; i++) {
                    if ((idx = all[i].indexOf(pref)) != -1) {
                        if (sb.length() > 0) {
                            sb.append('\n');
                        }
                        sb.append(all[i].substring(idx + preflen));
                        RecordStore rs = null;
                        try {
                            rs = RecordStore.openRecordStore(all[i], false);
                            sb.append(": ").append(rs.getSize()).append(" byte");
                        } catch (RecordStoreException ignored) {
                        } finally {
                            try {
                                rs.closeRecordStore();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
                if (sb.length() > 0) {
                    Jimm.setDisplay(new Alert(null, sb.toString(), null, AlertType.INFO));
                    return;
                }
                break;
        }
        if (menu != null) {
            menu.back();
        } else {
            Jimm.setDisplay(this);
        }
    }

    public int vtGetItemHeight() {
        return Math.max(ContactList.imageList.getHeight() + 2, facade.getFontHeight() + 2);
    }

    public void vlItemClicked(VirtualList sender) {
        TreeNode node = getCurrentItem();
        if (node == null) {
            return;
        }
        if (Jimm.getCurrentProfile().getIndex() != getCurrIndex()) {
            Profiles.setProfile(getCurrIndex());
        } else {
            Jimm.getContactList().activate();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (NativeCanvas.getCanvas().equals(this)) {
            if (c.equals(JimmUI.cmdMenu)) {
                showMenu();
                return;
            }
            Jimm.getContactList().activate();
            return;
        }
        if (c.equals(JimmUI.cmdOk) && d != null && d instanceof TextBox) {
            TextBox textBox = (TextBox) d;
            String text = textBox.getString();
            if (text.length() > 0 && Profiles.getProfile(text) == null) {
                setAccountOptions(text, (byte) 0);
            }
        }
        Jimm.setDisplay(this);
    }
}
