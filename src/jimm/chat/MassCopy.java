package jimm.chat;

import jimm.forms.FormEx;
import jimm.ui.ChoiceGroupEx;
import jimm.comm.DateAndTime;
import jimm.*;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.CommandListener;
import java.util.Vector;

public final class MassCopy implements CommandListener {

    class MassDate {
        boolean incoming;
        String from;
        String date;
        long time;
        String text;

        MassDate(boolean incoming, String from, String date, long time, String text) {
            this.incoming = incoming;
            this.from = from;
            this.date = date;
            this.time = time;
            this.text = text;
        }
    }

    private ChoiceGroupEx textChoise;
    private FormEx form;
    private Vector vector;

    private MassCopy(String caption) {
        textChoise = new ChoiceGroupEx(null, Choice.MULTIPLE);
        vector = new Vector();
        form = new FormEx(caption, JimmUI.cmdOk, JimmUI.cmdBack);
        form.setCommandListener(this);
    }

    public MassCopy(ChatTextList chatTextList, String name, String myname) {
        this(chatTextList.getCaption());
        Vector vct;
        int size;
        if ((size = (vct = chatTextList.getMessData()).size()) != 0) {
            size = Math.min(size, 256);
            for (int j = 0; j < size; j++) {
                MessData messData = (MessData) vct.elementAt(j);
                String text = chatTextList.getTextByIndex(messData.getOffset(), false, j);
                MassDate md = new MassDate(messData.getIncoming(), messData.getIncoming() ? name : myname, DateAndTime.getDateString(true, true, messData.getTime()), messData.getTime(), text);
                vector.addElement(md);
                textChoise.append(text, null);
            }
        }
        Jimm.setPrevScreen(chatTextList);
    }

//    public MassCopy(HistoryStorageList historyStorageList, ContactItem item) {
//        this(historyStorageList);
//        contact = item;
//    }


    public MassCopy(HistoryStorageList historyStorageList) {
        //#sijapp cond.if modules_HISTORY="true"#
        this(historyStorageList.getCaption());
        int size;
        if ((size = historyStorageList.getCounts()) != 0) {
            int offset = historyStorageList.getCurrIndex();
            if (size < 256) {
                offset = 0;
            } else if (size - offset < 256) {
                offset = size - 256;
            }
            for (int j = offset; j < size; j++) {
                CachedRecord cachedRecord = historyStorageList.getRecord(j);
                if (cachedRecord != null) {
                    MassDate md = new MassDate(cachedRecord.type == 0, cachedRecord.from, cachedRecord.date, 0, cachedRecord.text);
                    vector.addElement(md);
                    textChoise.append(cachedRecord.text, null);
                }
            }
        }
        Jimm.setPrevScreen(historyStorageList);
        //#sijapp cond.end#
    }

    public final void activate() {
        form.append(textChoise);
        Jimm.setDisplay(form);
    }

    public final void commandAction(Command command, Displayable displayable) {
        if (command.equals(JimmUI.cmdOk)) {
            boolean aflag[] = new boolean[textChoise.size()];
            if (textChoise.getSelectedFlags(aflag) != 0) {
                boolean clear = true;
                for (int i = 0; i < aflag.length; i++) {
                    if (!aflag[i]) {
                        continue;
                    }
                    MassDate md = (MassDate) vector.elementAt(i);
                    JimmUI.setClipBoardText(md.incoming, md.date, md.from, md.text, !clear);
                    clear = false;
                }
            }
        }
        Jimm.back();
    }
}