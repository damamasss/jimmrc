package jimm.ui;

import jimm.ContactItem;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.comm.Util;
import jimm.forms.FormEx;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;

public class CIForm implements CommandListener {
    /**
     * @author Rishat Shamsutdinov
     */
    private LineChoiseBoolean[] items;
    private TextField notes;
    private ContactItem contact;

    public CIForm(ContactItem ci) {
        contact = ci;
        notes = new TextField(ResourceBundle.getString("note"), contact.getStringValue(ContactItem.CONTACTITEM_NOTES), 140, TextField.ANY);
        Jimm.setDisplay(fillList(ci.getExtraValues()));
    }

    private FormEx fillList(byte values) {
        FormEx form = new FormEx(contact.name, JimmUI.cmdSave, JimmUI.cmdBack);
        form.append(notes);
        initItems(form, getNames(), toByteArray(values));
        form.removeCommand(JimmUI.cmdSave);
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
        return "watch" + "|" +
                "wait_u" + "|" +
                "sound_ext" + "|" +
                "vibration" + "|" +
                "dis_read_status" + "|" +
                "dis_read_xtraz" + "|" +
                "auto_xtraz" + "|" +
                "use_history";
    }

    private boolean[] toByteArray(byte values) {
        boolean[] result = new boolean[ContactItem.EXTRA_SIZE];
        int key;
        for (int i = 0; i < ContactItem.EXTRA_SIZE; i++) {
            key = 1 << i;
            result[i] = (values & key) != 0;
        }
        return result;
    }

    private byte getValues(boolean[] values) {
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
        //if (c != JimmUI.cmdBack) {
        boolean[] vals = new boolean[ContactItem.EXTRA_SIZE];
        useItems(vals);
        contact.setExtraValues(getValues(vals));
        contact.setStringValue(ContactItem.CONTACTITEM_NOTES, notes.getString());
        contact.getProfile().safeSave();
        //}
        Jimm.back();
    }
}