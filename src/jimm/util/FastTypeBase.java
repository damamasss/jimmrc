package jimm.util;

import jimm.ContactItem;
import jimm.Jimm;
import jimm.JimmException;
import jimm.JimmUI;
import jimm.comm.StringConvertor;
import jimm.comm.Util;
//#sijapp cond.if modules_FILES is "true"#
import jimm.files.FileBrowser;
import jimm.files.FileBrowserListener;
import jimm.files.FileSystem;
//#sijapp cond.end#
import jimm.ui.Select;
import jimm.ui.SelectListener;

import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
public class FastTypeBase implements CommandListener, SelectListener
//#sijapp cond.if modules_FILES is "true"#
        , Runnable, FileBrowserListener
//#sijapp cond.end#
{
    /**
     * @author Rishat Shamsutdinov
     */
    private static final boolean MODE_ADD = false;
    private static final boolean MODE_DELETE = true;

    private final Hashtable base = new Hashtable();
    private final Vector myWords = new Vector();
    private boolean mode;

    public FastTypeBase() {
        loadMyWords();
        InputStream stream = getClass().getResourceAsStream("/ft.fat");
        if (stream == null) {
            return;
        }
        int lines = 0;
        try {
            DataInputStream dos = new DataInputStream(stream);
            String line;
            String lastLine;
            String prevWord = null;
            char ch;

            Vector words = new Vector();
            ByteArrayOutputStream baos;
            StringBuffer buf = new StringBuffer();
            while (true) {
                try {
                    baos = null;
                    baos = new ByteArrayOutputStream();
                    buf.setLength(0);
                    byte pLen = (byte) (dos.read() - 1);
                    if (pLen == -2) {
                        throw (new EOFException());
                    }
                    byte wLen = (byte) (dos.read() - pLen);
                    for (int i = 0; i < pLen; i++) {
                        buf.append(prevWord.charAt(i));
                    }
                    while (wLen-- > 0) {
                        baos.write(dos.read());
                    }
                    buf.append(Util.byteArrayToString(baos.toByteArray(), false));
                    line = StringConvertor.toLowerCase(buf.toString());
                    if (myWords.contains(line)) {
                        continue;
                    }
                    prevWord = line;
                } catch (EOFException e) {
                    if (words.size() > 0) {
                        line = " ";
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
                //ch = line.charAt(0);
                if (words.size() > 0) {
                    lastLine = Util.byteArrayToString((byte[]) words.lastElement());
                    ch = lastLine.charAt(0);
                    if (line.charAt(0) != ch || lastLine.length() != line.length()) {
                        addWordsToBase(doKey(ch, lastLine.length()), words);
                        words = null;
                        words = new Vector();
                    }
                }
                if (line.trim().length() > 0) {
                    words.addElement(Util.stringToByteArray(line));
                }
                lines++;
            }
            dos.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //System.out.println("Base: " + lines);
        try {
            stream.close();
        } catch (Exception ignored) {
        }
    }

    private Integer doKey(char ch, int len) {
        return (new Integer((len << 16) | ch));
    }

    private Vector get(String word) {
        return get(word.charAt(0), word.length());
    }

    public Vector get(char ch, int len) {
        return (Vector) base.get(doKey(ch, len));
    }

    private void loadMyWords() {
        try {
            RecordStore rs = RecordStore.openRecordStore("fasttype", false);
            int count = rs.getNumRecords();
            //myWords.setSize(count);
            byte[] buf;
            String str;
            for (int i = 0; i < count; i++) {
                buf = rs.getRecord(i + 1);
                if (buf == null) {
                    continue;
                }
                str = StringConvertor.toLowerCase(Util.byteArrayToString(buf));
                //int len = str.length();
                //if (len < 2) {
                //	continue;
                //}
                myWords.addElement(str);
                addWordToBase(str);
            }
            rs.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    private void saveMyWords() {
        try {
            RecordStore.deleteRecordStore("fasttype");
        } catch (Exception ignored) {
        }
        try {
            RecordStore rs = RecordStore.openRecordStore("fasttype", true);
            int count = myWords.size();
            byte[] buffer;
            for (int i = 0; i < count; i++) {
                buffer = Util.stringToByteArray((String) myWords.elementAt(i));
                rs.addRecord(buffer, 0, buffer.length);
            }
            rs.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    private void addWordsToBase(Integer key, Vector words) {
        Vector vect = (Vector) base.get(key);
        if (vect != null) {
            int size = vect.size();
            for (int i = 0; i < size; i++) {
                words.addElement(vect.elementAt(i));
            }
        }
        base.put(key, words);
    }

    private void addWordToBase(String str) {
        int len = str.length();
        Vector vect = get(str);
        if (vect == null) {
            vect = new Vector();
        }
        vect.addElement(Util.stringToByteArray(str));
        base.put(doKey(str.charAt(0), len), vect);
    }

    public void addWord() {
        mode = MODE_ADD;
        TextBox tb = new TextBox("", "", 450, TextField.ANY);
        tb.addCommand(JimmUI.cmdBack);
        tb.addCommand(JimmUI.cmdOk);
        tb.addCommand(JimmUI.cmdPaste);
        tb.setCommandListener(this);
        Jimm.setDisplay(tb);
    }

    public void deleteWord() {
        mode = MODE_DELETE;
        String words[] = new String[myWords.size()];
        myWords.copyInto(words);
        JimmUI.showSelector(words, this, false);
    }

    public void addWord(String word) {
        word = StringConvertor.toLowerCase(word).trim();
        if (myWords.contains(word) || word.length() < 2) {
            return;
        }
        myWords.addElement(word);
        addWordToBase(word);
        saveMyWords();
    }

    private boolean equals(byte[] b0, byte[] b1) {
        if (b0.length != b1.length) {
            return false;
        }
        for (int i = b0.length - 1; i >= 0; i--) {
            if (b0[i] != b1[i]) {
                return false;
            }
        }
        return true;
    }

    private void deleteWord(int index) {
        String word = (String) myWords.elementAt(index);
        myWords.removeElement(word);
        Vector words = get(word);
        byte bword[] = Util.stringToByteArray(word);
        for (int i = words.size() - 1; i >= 0; i--) {
            if (equals((byte[]) words.elementAt(i), bword)) {
                words.removeElementAt(i);
                break;
            }
        }
        saveMyWords();
    }

    public void commandAction(Command c, Displayable d) {
        TextBox tb = (TextBox) d;
        if (c == JimmUI.cmdOk && mode == MODE_ADD) {
            addWord(tb.getString());
        }
        if (c == JimmUI.cmdPaste) {
            tb.insert(JimmUI.getClipBoardText(false), tb.getCaretPosition());
            return;
        }
        Jimm.back();
    }

    public void selectAction(int action, int selectType, Object o) {
        if (action == Select.SELECT_OK && mode == MODE_DELETE) {
            deleteWord(selectType);
            Jimm.back();
        }
    }

    //#sijapp cond.if modules_FILES is "true"#
    private String directory;

    public void exportBase() {
        try {
            FileBrowser fb = new FileBrowser();
            fb.setListener(this);
            fb.setParameters(true);
            Jimm.setPrevScreen(jimm.chat.ChatHistory.currentChat);
            fb.activate();
        } catch (Exception ignored) {
        }
    }

    public ContactItem getCItem() {
        return null;
    }

    public void onFileSelect(String s0) {
    }

    public void onDirectorySelect(String dir) {
        directory = dir;
        (new Thread(this)).start();
    }

    public void run() {
        //Jimm.getSplashCanvasRef().setMessage(ResourceBundle.getEllipsisString("exporting"));
        Jimm.setDisplay(Jimm.getSplashCanvasRef());
        try {
            FileSystem file = FileSystem.getInstance();
            file.openFile(directory + "words_ft.txt");
            OutputStream os = file.openOutputStream();
            Enumeration words = base.elements();
            int bsize = base.size();
            int k = 1;
            StringBuffer buf = new StringBuffer();
            Vector vect;
            while (words.hasMoreElements()) {
                buf.setLength(0);
                vect = (Vector) words.nextElement();
                int size = vect.size();
                for (int i = size - 1; i >= 0; i--) {
                    buf.append(Util.byteArrayToString((byte[]) vect.elementAt(i))).append("\r\n");
                }
                os.write(Util.stringToByteArray(buf.toString()));
                os.flush();
                Jimm.getSplashCanvasRef().setProgress((100 * k) / bsize);
                k++;
            }
            os.close();
            file.close();
        } catch (Exception e) {
            JimmException.handleException(new JimmException(191, 0, false));
        }
        Jimm.getContactList().activate();
    }
//#sijapp cond.end#
}
//#sijapp cond.end#