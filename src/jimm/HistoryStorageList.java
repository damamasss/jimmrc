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
 File: src/jimm/HistoryStorage.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin
 *******************************************************************************/

//#sijapp cond.if modules_HISTORY is "true" #

package jimm;

import DrawControls.*;
import jimm.comm.DateAndTime;
import jimm.comm.Util;
//#sijapp cond.if modules_FILES is "true"#
import jimm.files.FileBrowser;
import jimm.files.FileBrowserListener;
import jimm.files.FileSystem;
//#sijapp cond.end#
import jimm.forms.FormEx;
import jimm.ui.*;
import jimm.util.ResourceBundle;
import jimm.chat.MassCopy;

import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import java.io.IOException;
import java.io.OutputStream;

public class HistoryStorageList extends VirtualList implements CommandListener, VirtualListCommands, MenuListener, SelectListener
//#sijapp cond.if modules_FILES is "true"#
        , Runnable, FileBrowserListener
//#sijapp cond.end#
{

    private static final byte MENU_INFO = (byte) 0;
    private static final byte MENU_COPYTEXT = (byte) 1;
    private static final byte MENU_COPYTEXT_PLUS = (byte) 2;
    private static final byte MENU_FIND = (byte) 3;
    private static final byte MENU_EXPORT = (byte) 4;
    private static final byte MENU_EXPORTALL = (byte) 5;
    private static final byte MENU_CLEAR = (byte) 6;

    private static final byte MENU_MSGPREV = (byte) 10;
    private static final byte MENU_MSGNEXT = (byte) 11;

    private final static Command cmdBack = new Command(ResourceBundle.getString("back"), Jimm.is_smart_SE() ? Command.CANCEL : Command.BACK, 2);

    public TextList messText;
    private ContactItem cItem;
    private FormEx frmFind;
    private TextField tfldFind;
    private LineChoiseBoolean[] find;
    //private int presize; // TODO

    public HistoryStorageList() {
        super(null);
        addCommandEx(JimmUI.cmdMenu, TextList.MENU_TYPE_LEFT_BAR);
        addCommandEx(cmdBack, TextList.MENU_TYPE_RIGHT_BAR);
        setCommandListener();
        setVLCommands(this);
        setColorScheme();
    }

    public void setCommandListener() {
        setCommandListener(this);
    }

    public void vlCursorMoved(VirtualList sender) {
        if (sender == this) {
            CachedRecord record = HistoryStorage.getCachedRecord(cItem, getCurrIndex());

            if (record == null) {
                return;
            }
            setCaption(record.from + " " + record.date);
        }
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        switch (keyCode) {
            case Canvas.KEY_STAR:
                int index = getCurrIndex();
                if (index == -1) {
                    return;
                }
                CachedRecord record = HistoryStorage.getCachedRecord(cItem, index);
                if (record == null) {
                    return;
                }
                JimmUI.setClipBoardText((record.type == 0), record.date, record.from, record.text);
                break;
        }

        if ((sender == messText) && (type == KEY_PRESSED)) {
            switch (gameAct) {
                case Canvas.LEFT:
                    moveInList(-1);
                    break;
                case Canvas.RIGHT:
                    moveInList(1);
                    break;
            }
        }
    }

    public void vlItemClicked(VirtualList sender) {
        if (sender == this) {
            showMessText();
        }
    }

    private void moveInList(int offset) {
        moveCursor(offset, false);
        showMessText();
    }

//#sijapp cond.if modules_FILES is "true"#
    private boolean cp1251;
    private String exportUin;
    private String directory;

    public ContactItem getCItem() {
        return null;
    }

    public void export(String uin) {
        exportUin = uin;
        try {
            FileBrowser fb = new FileBrowser();
            fb.setListener(this);
            fb.setParameters(true);
            Jimm.setPrevScreen(this);
            fb.activate();
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
        }
    }

    public void onFileSelect(String s0) {
    }

    public void onDirectorySelect(String dir) {
        directory = dir;
        (new Thread(this)).start();
    }

    public void run() {
        if (exportUin == null) {
            startExport(null);
        } else {
            startExport(new ContactItem[]{cItem.getProfile().getItemByUIN(exportUin)});
        }
    }

    private void exportUinToStream(ContactItem item, OutputStream os) throws IOException {
        CachedRecord record;
        String uin = item.getUinString();
        int max = HistoryStorage.getRecordCount(item);
        if (max > 0) {
            String nick = (item.getStringValue(ContactItem.CONTACTITEM_NAME).length() > 0) ? item
                    .getStringValue(ContactItem.CONTACTITEM_NAME) : uin;
            //Jimm.getSplashCanvasRef().setMessage(nick);
            StringBuffer str_buf = new StringBuffer().append("\r\n").append('\t').append(ResourceBundle.getString("message_history_with")).append(nick)
                    .append(" (").append(uin).append(")\r\n").append('\t').append(ResourceBundle.getString("export_date")).append(DateAndTime.getDateString(false, true))
                    .append("\r\n\r\n");
            os.write(Util.stringToByteArray(str_buf.toString(), !cp1251));
            String curr_msg_text;
            StringBuffer msg_str_buf;
            for (int i = 0; i < max; i++) {
                record = HistoryStorage.getRecord(item, i);

                msg_str_buf = new StringBuffer();
                msg_str_buf.append("\r\n");
                if (record.type == 0) {
                    msg_str_buf.append("-------------------------------------->-\r\n ").append(nick);
                } else {
                    msg_str_buf.append("--------------------------------------<-\r\n ").append(item.getProfile().getNick());
                }
                msg_str_buf.append(" (").append(record.date).append("):\r\n");

                os.write(Util.stringToByteArray(msg_str_buf.toString(), !cp1251));
                //msg_str_buf.setLength(0);
                //msg_str_buf = null;

                curr_msg_text = record.text.trim();
                msg_str_buf = new StringBuffer(curr_msg_text.length());

                // Сохранение в историю by BeCase (экспериментально) // by XaTTaB
                int n = curr_msg_text.length();
                for (int k = 0; k < n; k++) {
                    msg_str_buf = msg_str_buf.append(curr_msg_text.charAt(k));
                }
                msg_str_buf = msg_str_buf.append('\r').append('\n');
                os.write(Util.stringToByteArray(msg_str_buf.toString(), !cp1251));
                os.flush();
                Jimm.getSplashCanvasRef().setProgress((100 * i) / max);
            }
        }
    }

    private void exportUinToFile(ContactItem item, String filename) {
        try {
            if (HistoryStorage.getRecordCount(item) > 0) {
                FileSystem file = openFile(filename);
                OutputStream os = file.openOutputStream();
                if (!cp1251) {
                    os.write(new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf});
                }
                exportUinToStream(item, os);
                os.close();
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JimmException.handleException(new JimmException(191, 0, false));
        }
    }

    public void startExport(ContactItem[] citems) {
        cp1251 = Options.getBoolean(Options.OPTION_CP1251_HACK);
        //Jimm.getSplashCanvasRef().setMessage(ResourceBundle.getEllipsisString("exporting"));
        //Jimm.getDisplay().setCurrent(Jimm.jimm.getSplashCanvasRef());
        Jimm.setDisplay(Jimm.getSplashCanvasRef());
        //Jimm.getContactList().activate();
        if (citems == null) {
            citems = cItem.getProfile().getContactItems();
        }
        for (int i = 0; i < citems.length; i++) {
            exportUinToFile(citems[i],
                    (new StringBuffer())
                            .append(directory)
                            .append("J[i]mm_hist_")
                            .append(citems[i].getUinString())
                            .append(".txt").toString()
            );
        }
        Jimm.getContactList().activate();
        //NativeCanvas.hideLPCanvas();
//		Alert ok = new Alert("", ResourceBundle.getString("export_complete"), null, AlertType.INFO);
//		ok.setTimeout(Alert.FOREVER);
//		Jimm.getDisplay().setCurrent(ok);
    }

    public FileSystem openFile(String fileName) {
        try {
            FileSystem file = FileSystem.getInstance();
            file.openFile(fileName);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            JimmException.handleException(new JimmException(191, 0, true));
            return null;
        }
    }
// #sijapp cond.end#


    public int getCounts() {
        return HistoryStorage.getRecordCount(cItem);
    }

    public CachedRecord getRecord(int i) {
        return HistoryStorage.getCachedRecord(cItem, i);
    }

    private void showMenu(boolean mstxt) {
        Menu menu = new Menu(mstxt ? (CanvasEx) messText : (CanvasEx) this);
        //if (!mstxt) {
        //    menu.addMenuItem("select", MENU_SELECT);
        //}
        menu.addMenuItem("history_info", MENU_INFO);
        menu.addMenuItem("copy_text", MENU_COPYTEXT);
        if (mstxt) {
            menu.addMenuItem("next", MENU_MSGNEXT);
            menu.addMenuItem("prev", MENU_MSGPREV);
        } else {
            menu.addMenuItem("add_to_copied_text", MENU_COPYTEXT_PLUS);
            menu.addMenuItem("find", MENU_FIND);
//#sijapp cond.if modules_FILES is "true"#
            menu.addMenuItem("export", MENU_EXPORT);
            menu.addMenuItem("exportall", MENU_EXPORTALL);
//#sijapp cond.end#
            menu.addMenuItem("clear", MENU_CLEAR);
        }
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case MENU_INFO:
                RecordStore rs = HistoryStorage.getRS();
                try {
                    Alert alert = new Alert
                            (
                                    ResourceBundle.getString("history_info"),
                                    (new StringBuffer())
                                            .append(ResourceBundle.getString("hist_cur")).append(": ").append(getSize()).append("\n")
                                            .append(ResourceBundle.getString("hist_size")).append(": ").append(rs.getSize() / 1024).append("\n")
                                            .append(ResourceBundle.getString("hist_avail")).append(": ").append(rs.getSizeAvailable() / 1024).append("\n")
                                            .toString(),
                                    null,
                                    AlertType.INFO
                            );
                    alert.setTimeout(Alert.FOREVER);
                    Jimm.setDisplay(alert);
                } catch (Exception ignored) {
                }
                return;

            case MENU_COPYTEXT:
                copyText(false);
                break;

            case MENU_COPYTEXT_PLUS:
                (new MassCopy(this)).activate();
                return;

            case MENU_FIND:
                if (frmFind == null) {
                    frmFind = new FormEx(ResourceBundle.getString("find"), JimmUI.cmdOk, JimmUI.cmdBack);
                    tfldFind = new TextField(ResourceBundle.getString("text_to_find"), "", 64, TextField.ANY);
                    find = new LineChoiseBoolean[2];
                    find[0] = new LineChoiseBoolean(ResourceBundle.getString("find_backwards"), true);
                    find[1] = new LineChoiseBoolean(ResourceBundle.getString("find_case_sensitiv"), false);
                    //chsFind = new ChoiceGroupEx(ResourceBundle.getString("option"), Choice.MULTIPLE);
                    //chsFind.append(ResourceBundle.getString("find_backwards"), null);
                    //chsFind.append(ResourceBundle.getString("find_case_sensitiv"), null);
                    //chsFind.setSelectedIndex(0, true);

                    frmFind.append(tfldFind);
                    //frmFind.append(chsFind);
                    frmFind.append(find[0]);
                    frmFind.append(find[1]);
                    frmFind.setCommandListener(this);
                }
                Jimm.setDisplay(frmFind);
                return;

//#sijapp cond.if modules_FILES is "true"#
            case MENU_EXPORT:
                export(getCurrUin());
                return;

            case MENU_EXPORTALL:
                export(null);
                return;
//#sijapp cond.end#
            //case MENU_SELECT:
            //showMessText();
            //return;

            case MENU_CLEAR:
                JimmUI.showSelector(SelectBase.getStdSelector(), this, true);
                return;

            case MENU_MSGNEXT:
                moveInList(1);
                return;

            case MENU_MSGPREV:
                moveInList(-1);
                return;
        }
        if (menu != null) {
            menu.back();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
            if (JimmUI.isControlActive(messText) || JimmUI.isControlActive(frmFind)) {
                HistoryStorage.showHistoryList(cItem);
                messText = null;
                //return;
            }
        } else if (c == cmdBack) {
            HistoryStorage.clearCache();
            messText = null;
            frmFind = null;
            Jimm.back();
        } else if (c == JimmUI.cmdMenu) {
            showMenu(JimmUI.isControlActive(messText));
        } else if (c == JimmUI.cmdOk && find != null) {
            HistoryStorage.find(cItem, tfldFind.getString(), find[1].getBooolean(), find[0].getBooolean());
        }
    }

    public void selectAction(int action, int selectType, Object o) {
        if (action == Select.SELECT_OK) {
            switch (selectType) {
                case 0: // Current
                    HistoryStorage.clearHistory(cItem);
                    break;

                case 1: // All except current
                    HistoryStorage.clear_all(cItem);
                    break;

                case 2: // All
                    HistoryStorage.clear_all(null);
                    break;
            }
            activate();
            invalidate();
        }
    }

    void showMessText() {
        try {
            if (getCurrIndex() >= getSize()) {
                return;
            }
            if (messText == null) {
                messText = new TextList(null, false);
                messText.setMode(MODE_TEXT);
                messText.addCommandEx(JimmUI.cmdMenu, TextList.MENU_TYPE_LEFT_BAR);
                messText.addCommandEx(JimmUI.cmdBack, TextList.MENU_TYPE_RIGHT_BAR);
                messText.setCommandListener(this);
                messText.setVLCommands(this);
                messText.setColorScheme();
            }

            CachedRecord record = HistoryStorage.getRecord(cItem, this.getCurrIndex());

            messText.clear();
            messText.addBigText(record.date + ":", messText.getTextColor(), Font.STYLE_BOLD, -1);
            messText.doCRLF(-1);

//#sijapp cond.if modules_SMILES is "true" #
            Emotions.addTextWithEmotions(messText, record.text, Font.STYLE_PLAIN, messText.getTextColor(), -1);
//#sijapp cond.else#
//#		messText.addBigText(record.text, messText.getTextColor(), Font.STYLE_PLAIN, -1);
//#sijapp cond.end#

            messText.doCRLF(-1);
            messText.setCaption(record.from);

            messText.activate();
            messText.repaint();
        } catch (Exception ignored) {
        }
    }

    private void copyText(boolean flag) {
        int index = getCurrIndex();
        if (index == -1) {
            return;
        }
        CachedRecord record = HistoryStorage.getCachedRecord(cItem, index);
        if (record == null) {
            return;
        }
        //if (flag) {
        //    JimmUI.setClipBoardText((record.type == 0), record.date, "\n" + record.from, record.text, true);
        //} else {
            JimmUI.setClipBoardText((record.type == 0), record.date, record.from, record.text);
        //}
    }

    boolean ready4Ani() {
        return messText != null && messText.isActive();
    }

    public String getCurrUin() {
        return (cItem == null) ? "" : cItem.getUinString();
    }

    public void setCItem(ContactItem _cItem) {
        cItem = _cItem;
    }

    protected int getSize() {
        return HistoryStorage.getRecordCount(cItem);
        //return presize;
    }

//    public void readHistory() {
//        Runnable runnable = new Runnable() {
//            public void run() {
//                for (presize = 0; presize < HistoryStorage.getRecordCount(cItem); presize++) {
//                    unlock();
//                }
//            }
//        };
//        runnable.run();
//    }

    // returns messages history list item data
    protected void get(int index, ListItem item) {
        CachedRecord record = HistoryStorage.getCachedRecord(cItem, index);
        if (record == null) {
            return;
        }
        item.text = record.shortText;
        item.color = (record.type == 0) ? getTextColor() : getColor(COLOR_CHAT);
    }
}
//#sijapp cond.end#
