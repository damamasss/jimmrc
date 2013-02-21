package jimm.ui;

import DrawControls.Icon;
import DrawControls.CanvasEx;
import jimm.*;
import jimm.comm.Action;
import jimm.comm.ServerListsAction;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import java.util.Vector;

public class SelectBase extends SelectList {

    public final static byte LISTENER = (byte) 0;
    public final static byte URL = (byte) 1;
    public final static byte SERVER = (byte) 2;
    private byte bytet;

    private ContactItem contact;
    private SelectListener selectListener;

    public static String[] getStdSelector() {
        return Util.explode("currect_contact" + "|" + "all_contact_except_this" + "|" + "all_contacts", '|');
    }

    public SelectBase() {
        super(null, null);
        Object d;
        if ((d = Jimm.getCurrentDisplay()) instanceof CanvasEx) {
            setPrvScreen((CanvasEx) d);
        }
    }

    public SelectBase(String[] elements, SelectListener select, boolean translateWords) {
        this();
        bytet = LISTENER;
        lock();
        selectListener = select;
        objects = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            objects[i] = elements[i];
            addMenuItem(elements[i], null, (byte) 0);
        }
        unlock();
    }

    public SelectBase(String msg) {
        this();
        bytet = URL;
        lock();
        Vector v = Util.parseMessageForURL(msg);
        objects = new Object[v.size()];
        for (int i = 0; i < v.size(); i++) {
            objects[i] = v.elementAt(i);
            addMenuItem((String) objects[i], null, (byte) 0);
        }
        unlock();
    }

    public SelectBase(ContactItem contactItem) {
        this();
        bytet = SERVER;
        lock();
        contact = contactItem;
        String visibleList = (contact.getVisibleId() == 0) ? "add_visible_list" : "rem_visible_list";
        String invisibleList = (contact.getInvisibleId() == 0) ? "add_invisible_list" : "rem_invisible_list";
        String ignoreList = (contact.getIgnoreId() == 0) ? "add_ignore_list" : "rem_ignore_list";
        Icon visibleImage = (contact.getVisibleId() == 0) ? ContactList.menuIcons.elementAt(5) : ContactList.menuIcons.elementAt(6);
        Icon invisibleImage = (contact.getInvisibleId() == 0) ? ContactList.menuIcons.elementAt(5) : ContactList.menuIcons.elementAt(6);
        Icon ignoreImage = (contact.getIgnoreId() == 0) ? ContactList.menuIcons.elementAt(5) : ContactList.menuIcons.elementAt(6);
        objects = new Object[3];
        addMenuItem((String) (objects[0] = visibleList), visibleImage, (byte) 0);
        addMenuItem((String) (objects[1] = invisibleList), invisibleImage, (byte) 0);
        addMenuItem((String) (objects[2] = ignoreList), ignoreImage, (byte) 0);
        unlock();
    }

    protected void select() {
        switch (bytet) {
            case LISTENER:
                if (selectListener != null) {
                    selectListener.selectAction(Select.SELECT_OK, getCurrIndex(), objects[getCurrIndex()]);
                }
                break;

            case URL:
                try {
                    Object object = objects[getCurrIndex()];
                    if (object instanceof String) {
                        Jimm.jimm.platformRequest(doURL((String) object));
                    }
                } catch (Exception ignored) {
                }
                break;

            case SERVER:
                int[] server = new int[]{ServerListsAction.VISIBLE_LIST, ServerListsAction.INVISIBLE_LIST, ServerListsAction.IGNORE_LIST};
                Action act = new ServerListsAction(server[getCurrIndex()], contact);
                try {
                    contact.getIcq().requestAction(act);
                } catch (JimmException e) {
                    JimmException.handleException(e);
                }
                break;
        }
        if (Jimm.getCurrentDisplay().equals(this)) {
            back();
        }
    }

    private static String doURL(String str) {
        StringBuffer url = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\n') {
                url.append(c);
            }
        }
        return url.toString();
    }
}