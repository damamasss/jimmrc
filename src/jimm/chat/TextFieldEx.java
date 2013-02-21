package jimm.chat;

//#sijapp cond.if modules_CLASSIC_CHAT is "true"#

import DrawControls.CanvasEx;
import DrawControls.NativeCanvas;
import DrawControls.VirtualList;
import jimm.Emotions;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.Options;
import jimm.ui.InputTextBox;
import jimm.comm.StringConvertor;
import jimm.comm.Util;
import jimm.util.FastTypeBase;
import jimm.util.Device;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

public class TextFieldEx implements Runnable {
    /**
     * @author Rishat Shamsutdinov
     */
    private final static boolean LANG_EN = true;
    private final static boolean LANG_RU = false;
    private final static byte CAPS_ON = 0;
    private final static byte CAPS_OFF = (byte) 1;
    private final static byte CAPS_AUTO = (byte) 2;
    private final static byte CAPS_NUMS = (byte) 3;
    private final static boolean UI_MODE_NONE = true;
    private final static boolean UI_MODE_SYMBOLS = false;
    private static final short CARET_BLINK_DELAY = 500;
    private static short DELAY_TIME = 700;

    private static final byte ACTION_NONE = 0;
    private static final byte ACTION_SHIFT = (byte) 1;
    private static final byte ACTION_SYMBOLS = (byte) 2;
    private static final byte ACTION_INPUT = (byte) 4;
    private static final byte ACTION_ALTWIN = (byte) 5;
    private static final byte ACTION_SMILES = (byte) 6;

    private static final byte PARSE_KEYCODE = 0;
    private static final byte PARSE_CHARS = (byte) 1;
    private static final byte PARSE_CKEY = (byte) 2;

    private static Hashtable table;
    public static boolean cKey = true;

    private final char[] SYMBOLS_CHARS = {'\u002E', '\u002C', '\'', '\u003F', '\u0021', '\u0022', '\u002D', '\u0028', '\u0029',
            '\u0040', '\u002F', '\u003A', '\u005F', '\u003B', '\u002B', '\u0026', '\u0025', '\u002A',
            '\u003D', '\u003C', '\u003E', '\u00A3', '\u20AC', '\u0024', '\u00A5', '\u00A4', '\u005B',
            '\u005D', '\u007B', '\u007D', '\\', '\u007E', '\u005E', '\u00BF', '\u00A7', '\u0023',
            '\u007C', '\n', '\u0060'};

    private StringBuffer message;
    private long lastPressedKeyTime;
    private boolean caretBlinkOn = true;
    private long lastCaretBlink = 0;
    private int lastPressedKey = 0;
    private boolean currentLang = LANG_RU;
    private byte capsStatus = CAPS_OFF;
    private byte keyCharID = 0, symbolsCharID = 0;
    private boolean uiMode = UI_MODE_NONE;
    private char currentChar = 0;
    private int cursorPos = 0;
    private static int screenWidth = 0;
    private int translationX = 0, caretLeft = 0;
    private static int inputHeight, symbolsWidth = 0, symbolsHeight = 0;
    private ChatTextList chat;
    public Vike vike;
    private TimerTask blinkTimerTask;
    private static char[] lastChars;

    public TextFieldEx(ChatTextList chat) {
        this.chat = chat;
        loadTable();
        if (lastChars == null) {
            lastChars = new char[6];
            System.arraycopy(SYMBOLS_CHARS, 0, lastChars, 0, 6);
        }
        inputHeight = CanvasEx.facade.getFontHeight();
        message = new StringBuffer();
        lastPressedKeyTime = System.currentTimeMillis();
        if (screenWidth == 0) {
            screenWidth = ChatTextList.getWidth();
        }
        if (symbolsHeight == 0 && symbolsWidth == 0) {
            symbolsWidth = inputHeight * 6 + 4;
            symbolsHeight = inputHeight * 7 + 4;
        }
    }

    private void loadTable() {
        if (table != null) {
            return;
        }
        table = new Hashtable();
        String content = Util.removeCr(Util.getStringAsStream("/keychars.txt"));
        if (content.length() <= 1) {
            return;
        }
        String[] lines = Util.explode(content, '\n');
        content = null;
        try {
            int count = lines.length;
            String line;
            for (int i = 0; i < count; i++) {
                line = lines[i] + ' ';
                if (line.trim().startsWith("//") || line.trim().length() == 0) {
                    continue;
                }
                byte state = PARSE_KEYCODE;
                int len = line.length();
                int bPos = 0;
                int keyCode = 0;
                boolean lang = LANG_RU;
                int comCount = 0;
                String ru = null;
                for (int j = 0; j < len; j++) {
                    char c = line.charAt(j);
                    switch (state) {
                        case PARSE_KEYCODE:
                            if (c == '=') {
                                try {
                                    keyCode = Integer.parseInt(line.substring(bPos, j).trim());
                                } catch (Exception ignored) {
                                }
                                if (keyCode == 0 && !line.substring(Math.max(0, bPos - 7), j).trim().equals("TIMEOUT")) {
                                    throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
                                }
                                bPos = j + 1;
                                state = PARSE_CHARS;
                            } else if (c == 'C') {
                                state = PARSE_CKEY;
                            } else if (c < '0' || c > '9') {
                                //throw (new Exception("Syntax error (keychars.txt)"));
                                bPos++;
                            }
                            break;
                        case PARSE_CHARS:
                            if (c == ' ' || c == '\n' || c == '\r' || j == len - 1) {
                                if (j == len - 1) {
                                    j++;
                                }
                                if (bPos == j) {
                                    throw (new Exception("Line(" + (i + 1) + "): syntax error (keychars.txt)"));
                                }
                                if (keyCode == 0) {
                                    try {
                                        DELAY_TIME = Short.parseShort(line.substring(bPos, j).trim());
                                        j = len;
                                        break;
                                    } catch (Exception e) {
                                        throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
                                    }
                                }
                                if (lang == LANG_RU) {
                                    ru = doChars(line.substring(bPos, j));
                                    bPos = j + 1;
                                    lang = LANG_EN;
                                } else if (lang == LANG_EN) {
                                    if (ru == null) {
                                        throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
                                    }
                                    String en = doChars(line.substring(bPos, j));
                                    table.put(new Integer(keyCode), new String[]{ru, en});
                                    j = len;
                                }
                            } else if (c == '/') {
                                if (++comCount > 1) {
                                    if (ru == null) {
                                        throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
                                    }
                                    table.put(new Integer(keyCode), new String[]{ru});
                                    j = len;
                                }
                            }
                            break;
                        case PARSE_CKEY:
                            if (c == '0') {
                                cKey = false;
                                j = len;
                            } else if (c == '/') {
                                if (++comCount > 1) {
                                    j = len;
                                }
                            }
                            break;
                    }
                }
            }
            //dos.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
//        try {
//            stream.close();
//        } catch (Exception e) {
//        }
        //System.out.println("Ok! " + DELAY_TIME);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////
    ///////////////////
    /////// T10 ////////
    ///////////////////
    ///////////////////

    //private static final Hashtable t10Base = new Hashtable();
    private static FastTypeBase fastTypeBase;
    private static boolean t10Mode = false;
    private StringBuffer currWord = new StringBuffer();
    private Vector words = new Vector();
    private Object t10ThreadObject;
    private final Object wait = new Object();
    private Vector currChars = new Vector();
    private boolean notFound;

    public FastTypeBase getBase() {
        return fastTypeBase;
    }

    private void updateCurrWord(String _chars) {
        if (_chars == null) {
            return;
        }
        String chars = null;
        int wlen = currWord.length();
        currChars.setSize(Math.max(wlen - 1, 0));
        if (_chars.length() > 0) {
            currChars.addElement(_chars);
            chars = (String) currChars.elementAt(0);
        }
        //boolean init = false;
        notFound = true;
        //System.out.println("Update 1");
        if (wlen > 0) {
            synchronized (words) {
                int count, j, k;
                Vector ws;
                String cws;
                int cwslen;
                int len = chars.length();
                boolean cont = false;
                //System.out.println("Update 2");
                for (int i = 0; i < len; i++) {
                    //ws = (Vector) t10Base.get(doKey(chars.charAt(i), wlen));
                    ws = fastTypeBase.get(chars.charAt(i), wlen);
                    if (ws == null) {
                        continue;
                    }
                    count = ws.size();
                    for (j = 0; j < count; j++) {
                        cws = Util.byteArrayToString((byte[]) ws.elementAt(j));
                        cwslen = cws.length();
                        for (k = 0; k < cwslen; k++) {
                            if (((String) currChars.elementAt(k)).indexOf(cws.charAt(k)) == -1) {
                                cont = true;
                                break;
                            }
                        }
                        if (cont) {
                            cont = false;
                            continue;
                        }
                        if (notFound) {
                            notFound = false;
                            words.removeAllElements();
                        }
                        words.addElement(ws.elementAt(j));
                    }
                }
                //System.out.println("Update 3");
            }
            if (notFound) {
                //System.out.println("Update 4");
                int s = _chars.length();
                String str = StringConvertor.toLowerCase(currWord.toString()).substring(0, wlen - 1);
                words.removeAllElements();
                for (int i = 0; i < s; i++) {
                    words.addElement(Util.stringToByteArray(str + _chars.charAt(i)));
                }
            }
        }
        //System.out.println("Update 5");
        if (words.size() > 0) {
            setCurrWord(Util.byteArrayToString((byte[]) words.elementAt(0)));
        }
        //System.out.println("Update 6");
        if (((capsStatus == CAPS_ON) || (capsStatus == CAPS_AUTO)) && (wlen > 0)) {
            currWord.setCharAt(0, StringConvertor.toUpperCase(currWord.charAt(0)));
            if (capsStatus == CAPS_ON) {
                currWord.setCharAt(wlen - 1, StringConvertor.toUpperCase(currWord.charAt(wlen - 1)));
            }
        }
        //System.out.println("Update 7");
    }

    private int place() {
        int i1 = NativeCanvas.getHeightEx() - inputHeight - 4;
        if (vike != null)
            i1 -= vike.height;
        else
            i1 -= chat.getMenuBarHeight();
        return i1;
    }

    public final boolean qwertySwitch(int x, int y) {
        if (vike.tempItem != null) {
            vike.tempItem.activity = false;
            if (vike.tempItem == vike.vikePostItem.vikeItem)
                vike.vikePostItem.vikeItem = null;
            vike.textField.g();
        }
        int vikiTop = place();
        if (y < vikiTop)
            return false;
        if (y > vikiTop + inputHeight) {
            if (vike == null)
                return false;
            if (vike.tempItem != null) {
                vike.tempItem.activity = false;
                if (vike.tempItem == vike.vikePostItem.vikeItem)
                    vike.vikePostItem.vikeItem = null;
                vike.textField.g();
            }
            vikiTop = y;
            y = x;
            x = vike.height;
            vike.vikePostItem.vikeItem = null;
            if (vike.tempItem != null)
                vike.tempItem.activity = false;
            VikeLine bs1 = null;
            int i2 = NativeCanvas.getWidthEx();
            int k2 = NativeCanvas.getHeightEx();
            int j3 = vike.lines.length - 1;
            do {
                if (j3 < 0)
                    break;
                bs1 = vike.lines[j3];
                k2 -= vike.a_int_fld;
                if (Vike.act(y, vikiTop, 0, i2, k2, vike.a_int_fld))
                    break;
                bs1 = null;
                j3--;
            } while (true);
            if (bs1 != null) {
                int k3 = bs1.a_int_fld;
                int i4 = bs1.a_vikeItem_array1d_fld.length;
                int l4 = 0;
                do {
                    if (l4 >= i4)
                        break;
                    VikeItem br1 = bs1.a_vikeItem_array1d_fld[l4];
                    if (Vike.act(y, vikiTop, k3, br1.a_int_fld, vikiTop, 0)) {
                        String item = StringConvertor.toLowerCase(br1.a_java_lang_String_fld);
                        if (item.equals("123".toLowerCase())) {
                        } else if (item.equals("SHT".toLowerCase())) {
                        } else {
                            vike.tempItem = br1;
                            //if (vike.tempItem != null)
                            //    vike.tempItem.activity = false;
                            //vike.tempItem = br1;
                            br1.activity = true;
                            vike.vikePostItem.vikeItem = br1;
                            vike.vikePostItem.c = CanvasEx.facade.stringWidth("WWW");
                            vike.vikePostItem.a_int_fld = java.lang.Math.min(i2 - vike.vikePostItem.c - 1, java.lang.Math.max(((k3 << 1) - br1.a_int_fld) / 2, 0));
                            vike.vikePostItem.d = (3 * CanvasEx.facade.getFontHeight()) / 2;
                            vike.vikePostItem.b = k2 - vike.a_int_fld - vike.vikePostItem.d / 2;
                            vike.textField.chat.setChatEx();
                            break;
                        }

                    }
                    k3 += br1.a_int_fld;
                    l4++;
                } while (true);
            }
        }
        return true;
    }

    public final boolean qwertyAction(int x, int y) {
//        if (vike.tempItem != null) {
//            vike.tempItem.activity = false;
//            if (vike.tempItem == vike.vikePostItem.vikeItem)
//                vike.vikePostItem.vikeItem = null;
//            vike.textField.g();
//        }
        jimm.Jimm.getTimerRef().schedule(new VikePostHide(vike), 100);
        int k1 = place();
        if (y < k1)
            return false;
        if (y > k1 + inputHeight) {
            if (vike == null)
                return false;
            k1 = y;
            y = x;
            x = vike.height;
            vike.vikePostItem.vikeItem = null;
            if (vike.tempItem != null)
                vike.tempItem.activity = false;
            VikeLine bs1 = null;
            int i2 = NativeCanvas.getWidthEx();
            int k2 = NativeCanvas.getHeightEx();
            int j3 = vike.lines.length - 1;
            do {
                if (j3 < 0)
                    break;
                bs1 = vike.lines[j3];
                k2 -= vike.a_int_fld;
                if (Vike.act(y, k1, 0, i2, k2, vike.a_int_fld))
                    break;
                bs1 = null;
                j3--;
            } while (true);
            if (bs1 != null) {
                int k3 = bs1.a_int_fld;
                int i4 = bs1.a_vikeItem_array1d_fld.length;
                int l4 = 0;
                do {
                    if (l4 >= i4)
                        break;
                    VikeItem br1 = bs1.a_vikeItem_array1d_fld[l4];
                    if (Vike.act(y, k1, k3, br1.a_int_fld, k1, 0)) {
                        String item = StringConvertor.toLowerCase(br1.a_java_lang_String_fld);
                        boolean flag = br1.activity;
                        boolean flag1 = true;
                        if (item.equals("lng")) {
                            Vike.a_java_lang_String_static_fld = Vike.a_java_lang_String_static_fld.equals("ru") ? "en" : "ru";
                            if (vike.currentLang != vike.symbols)
                                vike.a(Vike.a_java_lang_String_static_fld);
                        } else if (item.equals("123".toLowerCase())) {
                            if (br1.activity) {
                                br1.activity = false;
                                vike.a(Vike.a_java_lang_String_static_fld);
                            } else {
                                br1.activity = true;
                                vike.initArray((byte) 2);
                            }
                        } else if (item.equals("SHT".toLowerCase())) {
                            if (vike.sentens == 0 || vike.sentens == 1) {
                                br1.activity = !br1.activity;
                                vike.sentens = ((byte) (br1.activity ? 2 : 0));
                            } else {
                                vike.sentens = 1;
                            }
                        } else if (item.equals("BKSP".toLowerCase())) {
                            vike.textField.keyReaction(-8, CanvasEx.KEY_RELEASED, -1);
                            int j4 = (vike.textField.message.toString().replace('\u206F', '\n')).length();
                            vike.a(j4 != 0 ? item.charAt(j4 - 1) : ' ', j4 == 0);
                        } else if (item.equals("ETR".toLowerCase()))
                            vike.textField.insert("\n");
                        else if (item.equals("SPC".toLowerCase())) {
                            vike.textField.insert(" ");
                            vike.a(' ', false);
                            if (vike.currentLang == vike.symbols)
                                vike.a(Vike.a_java_lang_String_static_fld);
                            Vike.a("123", vike.linesFinal[1], false);
                        } else if (item.equals(":)".toLowerCase())) {
                            Emotions.selectEmotion(vike.textField, vike.textField.chat);
                            //vike.textField.();
                        } else if (item.equals("RGT".toLowerCase())) {
                            vike.textField.chat.rightMenuPressed();
                        } else if (item.equals("LFT".toLowerCase())) {
                            vike.textField.chat.leftMenuPressed();
                        } else if (item.equals("<-".toLowerCase())) {
                            vike.textField.keyReaction(0, CanvasEx.KEY_RELEASED, Canvas.LEFT);
                        } else if (item.equals("->".toLowerCase())) {
                            vike.textField.keyReaction(0, CanvasEx.KEY_RELEASED, Canvas.RIGHT);
                        } else if (item.equals("MDL".toLowerCase())) {
                            vike.textField.cursorPos = 0;
                            vike.textField.message.setLength(0);
                            vike.textField.afterTyping();
                        } else {
                            flag1 = false;
                            vike.textField.insert(vike.a1(item));
                            //((Vike) (i1)).a_bj_fld.b(((Vike) (i1)).a(((java.lang.String) (j1))));
                            if (vike.sentens == 2) {
                                vike.sentens = 0;
                                Vike.a("SHT", vike.linesFinal[1], false);
                            }
                        }
                        if (!br1.activity && !flag) {
                            if (vike.tempItem != null)
                                vike.tempItem.activity = false;
                            vike.tempItem = br1;
                            br1.activity = true;
                            if (!flag1) {
                                vike.vikePostItem.vikeItem = br1;
                                vike.vikePostItem.c = CanvasEx.facade.stringWidth("WWW");
                                vike.vikePostItem.a_int_fld = java.lang.Math.min(i2 - vike.vikePostItem.c - 1, java.lang.Math.max(((k3 << 1) - br1.a_int_fld) / 2, 0));
                                vike.vikePostItem.d = (3 * CanvasEx.facade.getFontHeight()) / 2;
                                vike.vikePostItem.b = k2 - vike.a_int_fld - vike.vikePostItem.d / 2;
                            }
                            //jimm.Jimm.getTimerRef().schedule(new VikePostHide(vike), 350);
                        }
                        //vike.unlock();
                        vike.textField.chat.setChatEx();
                        break;
                    }
                    k3 += br1.a_int_fld;
                    l4++;
                } while (true);
            }
            return true;
        }
        y = x;
        TextFieldEx tf = this;
        int flag = y - (tf.caretLeft + tf.translationX);
        if (flag != 0) {
            int l1 = tf.cursorPos;
            int j2;
            int l2 = j2 = ((flag) / java.lang.Math.abs(flag)) * 3;
            int l3 = tf.message.length();
            java.lang.String s = tf.message.toString();
            int i5 = 0;
            do {
                if ((l1 += l2) < 0 || l1 > l3) {
                    l1 -= l2;
                    if ((l2 -= j2) == 0)
                        break;
                }
                int k4 = CanvasEx.facade.stringWidth(s.substring(0, l1)) + translationX;
                if ((k4 = y - k4) == 0)
                    break;
                if (flag * k4 >= 0)
                    continue;
                l1 -= l2;
                if ((l2 -= j2) == 0) {
                    int i3 = i5 >= k4 ? 0 : j2;
                    l1 += i3;
                    break;
                }
                i5 = k4;
            } while (true);
            if (y <= 3)
                l1 = java.lang.Math.max(0, l1 - 1);
            else if (y >= NativeCanvas.getWidthEx() - 3)
                l1 = java.lang.Math.min(l3, l1 + 1);
            tf.cursorPos = l1;
            tf.updateCaretPosition();
        }
        return true;
    }

    private void switchWord(int i) {
        int count = words.size();
        if (count > 0) {
            int wordIdx = -1;
            byte[] bcw = Util.stringToByteArray(StringConvertor.toLowerCase(currWord.toString()));
            for (int j = count - 1; j >= 0; j--) {
                if (equals((byte[]) words.elementAt(j), bcw)) {
                    wordIdx = j;
                    break;
                }
            }
            if (wordIdx >= 0) {
                wordIdx += i;
                if (wordIdx < 0) {
                    wordIdx = count - 1;
                } else {
                    wordIdx = wordIdx % count;
                }
                setCurrWord(Util.byteArrayToString((byte[]) words.elementAt(wordIdx)));
            }
        }
    }

    private boolean findCurrWord(int i) {
        if ((cursorPos == 0 && i > 0) || i == 0) {
            return true;
        }
        char ch = message.charAt(cursorPos - Math.max(0, i));
        if (!isLetterChar(ch)) {
            return true;
        }
        synchronized (message) {
            String str = message.toString();
            if (i > 0) {
                int len = message.length();
                int bPos = cursorPos;
                for (int j = cursorPos; j < len; j++) {
                    if (isLetterChar(str.charAt(j))) {
                        bPos = j + 1;
                    } else {
                        break;
                    }
                }
                message.setLength(0);
                message.append(str.substring(0, cursorPos - 1));
                if (bPos < len) {
                    message.append(str.substring(bPos));
                }
                try {
                    setCurrWord(str.substring(cursorPos - 1, bPos));
                } catch (Exception ignored) {
                }
            } else {
                int ePos = cursorPos;
                for (int j = cursorPos - 1; j >= 0; j--) {
                    if (isLetterChar(str.charAt(j))) {
                        ePos = j;
                    } else {
                        break;
                    }
                }
                message.setLength(0);
                if (ePos > 0) {
                    message.append(str.substring(0, ePos));
                }
                if (cursorPos + 1 < str.length()) {
                    message.append(str.substring(cursorPos + 1));
                }
                try {
                    setCurrWord(str.substring(ePos, cursorPos + 1));
                } catch (Exception ignored) {
                }
            }
            int cwlen = currWord.length();
            if (cwlen > 0) {
                if (i < 0) {
                    cursorPos -= cwlen;
                }
                cursorPos -= i;
            }
            cursorPos = Math.max(0, Math.min(message.length(), cursorPos));
            //System.out.println("Len: " + currWord.length());
            updateCaretPosition();
            notifyT10(new Object());
            return false;
        }
    }

    private void fillCurrChars() {
        if (currWord.length() == 0) {
            return;
        }
        //System.out.println("=====================");
        synchronized (currChars) {
            currChars.removeAllElements();
            int idx = (currentLang == LANG_RU) ? 0 : 1;
            int len = currWord.length();
            String[] st;
            String chs;
            currChars.setSize(len);
            Enumeration chars = table.elements();
            while (chars.hasMoreElements()) {
                st = (String[]) chars.nextElement();
                chs = st[Math.min(idx, st.length - 1)];
                for (int i = 0; i < len; i++) {
                    if ((currChars.elementAt(i) == null) && (chs.indexOf(StringConvertor.toLowerCase(currWord.charAt(i))) != -1)) {
                        //System.out.println(chs);
                        currChars.setElementAt(chs, i);
                    }
                }
            }
        }
        //System.out.println("=====================");
    }

    private void insertCurrWord() {
        String str = currWord.toString();
        currWord.setLength(0);
        if ((message.length() == 0) && (!chat.getContact().uTyping())) {
            chat.getContact().beginTyping(true);
        }
        insert(str);
        if ((notFound) && (Options.getBoolean(Options.OPTION_FT_SELFTRAIN))) {
            fastTypeBase.addWord(str);
        }
        checkCap(str.charAt(0));
        words.removeAllElements();
        notifyT10("");
        updateCaretPosition();
    }

    public String getT10String() {
        return (t10Mode) ? "backlight_off" : "backlight_on";
    }

    public void onOffT10() {
        t10Mode = !t10Mode;
        if (t10Mode) {
            try {
                fastTypeBase = new FastTypeBase();
                (new Thread(this)).start();
            } catch (OutOfMemoryError oome) {
                t10Mode = false;
            }
        } else {
            //t10Base.clear();
            fastTypeBase = null;
            currWord.setLength(0);
            words.removeAllElements();
            notifyT10(null);
        }
        boolean flag = cKey;
        cKey |= vike != null;
        chat.buildMenu(false, cKey);
    }

    private void setCurrWord(String str) {
        if (currWord.length() == 0) {
            currWord.append(str);
        } else if ((currWord.length() == 1) && (str.length() == 1)) {
            char ch = str.charAt(0);
            if (capsStatus == CAPS_AUTO || capsStatus == CAPS_ON) {
                ch = StringConvertor.toUpperCase(ch);
            }
            currWord.setCharAt(0, ch);
        } else {
            char ch;
            int len = str.length();
            currWord.setLength(len);
            for (int i = 0; i < len; i++) {
                ch = currWord.charAt(i);
                if (ch != StringConvertor.toLowerCase(ch)) {
                    currWord.setCharAt(i, StringConvertor.toUpperCase(str.charAt(i)));
                } else {
                    currWord.setCharAt(i, str.charAt(i));
                }
            }
        }
        updateCaretPosition();
    }

    public void run() {
        try {
            while (t10Mode && chat.lineTyping) {
                while (t10ThreadObject == null) {
                    if (!(t10Mode && chat.lineTyping)) {
                        return;
                    }
                    waitT10();
                }
                synchronized (t10ThreadObject) {
                    boolean destroy = true;
                    if (t10ThreadObject instanceof String) {
                        //System.out.println(">>>>>>>>update");
                        updateCurrWord(t10ThreadObject.toString());
                    } else if (t10ThreadObject instanceof Integer) {
                        //System.out.println(">>>>>>>>find");
                        destroy = findCurrWord(((Integer) t10ThreadObject).intValue());
                    } else {
                        //System.out.println(">>>>>>>>fill");
                        fillCurrChars();
                    }
                    if (destroy) {
                        t10ThreadObject = null;
                    }
                }
            }
        } catch (Exception e) {
            String text = "FTThread: " + e.getMessage() + ": " + e.toString();
            if (t10ThreadObject != null) {
                text += "\n" + t10ThreadObject.toString();
            }
            System.out.println(text);
        }
    }

    private void waitT10() {
        try {
            synchronized (wait) {
                wait.wait();
            }
        } catch (Exception ignored) {
        }
    }

    private void notifyT10(Object o) {
        try {
            synchronized (wait) {
                t10ThreadObject = o;
                wait.notify();
            }
        } catch (Exception ignored) {
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String doChars(String src) {
        String result = StringConvertor.replaceStr(src.trim(), "SPACE", " ");
        result = StringConvertor.replaceStr(result, "SYMBOLS", "\t");
        result = StringConvertor.replaceStr(result, "ALTWIN", "\013");
        result = StringConvertor.replaceStr(result, "SMILES", "\f");
        return StringConvertor.replaceStr(result, "SHIFT", "\n");
    }

    public void setChat(ChatTextList chat) {
        this.chat = chat;
    }

    public ChatTextList getChat() {
        return chat;
    }

    public void beforeTyping() {
        chat.buildMenu(false, cKey);
        blinkTimerTask = new TimerTask() {
            public void run() {
                checkTimestamps();
                repaint();
            }
        };
        Jimm.getTimerRef().schedule(blinkTimerTask, 0, 250);
        updateCaretPosition();
        capsStatus = CAPS_AUTO;
        chat.setForcedSize(-1, VirtualList.getHeight() - getHeight(true) - (vike == null ? chat.getMenuBarHeight() : vike.height));
        //chat.setForcedSize(-1, chat.getHeight() - getHeight(true) - chat.getMenuBarHeight());
        if ((message.length() != 0) && (!chat.getContact().uTyping())) {
            chat.getContact().beginTyping(true);
        }
        if (Jimm.isTouch()) qwertyInit();
        chat.BeginTyping(vike == null);
        (new Thread(this)).start();
    }

    public final void qwertyInit() {
        if (vike != null) {
            return;
        }
        chat.buildMenu(false, true);
        vike = new Vike(this);
        setForcedSize();
    }

    public void afterTyping() {
        if (uiMode == UI_MODE_NONE) {
            vike = null;
            chat.lineTyping = false;
            chat.buildMenu();
            if (blinkTimerTask != null) {
                blinkTimerTask.cancel();
            }
            blinkTimerTask = null;
        } else {
            uiMode = UI_MODE_NONE;
            chat.buildMenu(false, cKey);
        }
        repaint();
        if (chat.getContact().uTyping()) {
            chat.getContact().beginTyping(false);
        }
        chat.saveCurrMessage(message.toString());
        //chat.setForcedSize(-1, chat.getHeight() - chat.getMenuBarHeight() - getHeight(false));
        chat.setForcedSize(-1, VirtualList.getHeight() - getHeight(true) - (vike == null ? chat.getMenuBarHeight() : vike.height));
    }

    public int getHeight(boolean typing) {
//		int i0 = typing ? 2 : 1;
        return (inputHeight + 4);// * i0;
    }

    public void checkTimestamps() {
        long currentTime = System.currentTimeMillis();
        if (lastCaretBlink + CARET_BLINK_DELAY < currentTime) {
            caretBlinkOn = !caretBlinkOn;
            lastCaretBlink = currentTime;
        }
        if ((currentChar != 0) && (currentTime - lastPressedKeyTime > DELAY_TIME) && !t10Mode) {
            keyCharID = 0;
            insertCurrentChar();
        }
    }

    private void updateCaretPosition() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(message.toString().substring(0, cursorPos));
        //if (t10Mode) {
        //	buffer.append(currWord.toString());
        //}
        if (!t10Mode) {
            if (currentChar != 0) {
                buffer.append(currentChar);
            }
        }
        int x = (caretLeft = CanvasEx.facade.stringWidth(buffer.toString()));
        x += CanvasEx.facade.stringWidth(currWord.toString());
        buffer = null;
        if (x + translationX <= 0) {
            if (translationX < 0) {
                while (x + translationX <= 0) {
                    translationX += screenWidth;
                }
            }
        } else if (x + translationX >= screenWidth) {
            translationX = screenWidth - 1 - x;
        } else if (x != caretLeft) {
            if ((caretLeft + translationX < 0) && (x + translationX < screenWidth) && (x - caretLeft < screenWidth)) {
                translationX += screenWidth - x - translationX;
            }
        }
        translationX = Math.min(0, translationX);
    }

    public void setString(String str) {
        message.setLength(0);
        message.append(str.replace('\n', '\u206F'));
        cursorPos = message.toString().length();
        keyCharID = 0;
        currentChar = 0;
        updateCaretPosition();
    }

    public String getString() {
        return message.toString().replace('\u206F', '\n');
    }

    public String getCapsString() {
        if (capsStatus == CAPS_NUMS) {
            return "123";
        }
        StringBuffer str = new StringBuffer();
        if (currentLang == LANG_EN) {
            if (capsStatus == CAPS_OFF) {
                str.append(" abc");
            } else if (capsStatus == CAPS_ON) {
                str.append(" ABC");
            } else if (capsStatus == CAPS_AUTO) {
                str.append(" Abc");
            }
        } else {
            if (capsStatus == CAPS_OFF) {
                str.append(' ').append('\u0430').append('\u0431').append('\u0432');
            } else if (capsStatus == CAPS_ON) {
                str.append(' ').append(StringConvertor.toUpperCase('\u0430')).append(StringConvertor.toUpperCase('\u0431')).append(StringConvertor.toUpperCase('\u0432'));
            } else if (capsStatus == CAPS_AUTO) {
                str.append(' ').append(StringConvertor.toUpperCase('\u0430')).append('\u0431').append('\u0432');
            }
        }
        return str.toString();
    }

    public void insert(String s0) {
        if (!t10Mode) {
            insertCurrentChar();
        }
        if (s0.length() > 0) {
            message.insert(cursorPos, s0.replace('\n', '\u206F'));
            //message.append(currentChar);
            cursorPos += s0.length();
        }
        updateCaretPosition();
    }

    public void paint(Graphics g) {
        int screenHeight = NativeCanvas.getHeightEx();
        int top = screenHeight - inputHeight - 4;
        if (vike != null) {
            top -= vike.height;
        } else {
            top -= chat.getMenuBarHeight();
        }
        int clipX = g.getClipX(), clipY = g.getClipY();
        int clipWidth = g.getClipWidth(), clipHeight = g.getClipHeight();
        g.setColor(0xffffff);
        g.fillRect(0, top, screenWidth, inputHeight + 4);
        g.setColor(0x000000);
        g.setClip(0, top, screenWidth, inputHeight + 4);
        g.translate(translationX, 0);

        if (message.length() > 0) {
            String s0 = message.toString().substring(0, cursorPos);
            String s1 = message.toString().substring(cursorPos);
            CanvasEx.drawString(g, CanvasEx.facade, s0, 0, top + 2 + inputHeight, Graphics.LEFT | Graphics.BOTTOM);
            if (s1.length() > 0) {
                CanvasEx.drawString(g, CanvasEx.facade, s1, t10Mode ? caretLeft + CanvasEx.facade.stringWidth(currWord.toString()) : caretLeft, top + 2 + inputHeight, Graphics.LEFT | Graphics.BOTTOM);
            }
        }
        if (t10Mode) {
            if (currWord.length() > 0) {
                g.drawLine(caretLeft, top + 2 + inputHeight, caretLeft + CanvasEx.facade.stringWidth(currWord.toString()), top + 2 + inputHeight);
                CanvasEx.drawString(g, CanvasEx.facade, currWord.toString(), caretLeft, top + 2 + inputHeight, Graphics.LEFT | Graphics.BOTTOM);
            }
        } else {
            if (currentChar != 0) {
                g.setColor(0x0000FF);
                CanvasEx.drawString(g, CanvasEx.facade, new Character(currentChar).toString(), caretLeft, top + 2 + inputHeight, Graphics.RIGHT | Graphics.BOTTOM);
            }
        }
        if (currWord.length() == 0) {
            if ((caretBlinkOn) && (chat.lineTyping) && (currentChar == 0)) {
                g.drawLine((cursorPos > 0) ? caretLeft : 1, top + 2, (cursorPos > 0) ? caretLeft : 1, top + 2 + inputHeight);
            }
        }
        g.translate(-translationX, 0);
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        if (uiMode == UI_MODE_SYMBOLS) {
            int x = screenWidth - symbolsWidth + 2, y = screenHeight - chat.getMenuBarHeight() - symbolsHeight - inputHeight + 2;
            g.setColor(0x646464);
            g.fillRect(x - 2, y - 2, symbolsWidth, symbolsHeight + inputHeight);
            g.setColor(0x000000);
            g.drawRect(x - 2, y - 2, symbolsWidth, symbolsHeight + inputHeight);
            g.setColor(0xffffff);
            g.drawRect(x, y + inputHeight, symbolsWidth - 4, symbolsHeight - 4);
            char c;
            int index;
            for (int i = 0; ; i++) {
                c = lastChars[i];
                if (i == symbolsCharID) {
                    g.setColor(0x303030);
                    g.fillRect(x, y, inputHeight, inputHeight - 1);
                    g.setColor(0xffffff);
                }
                drawChar(g, x, y, c);
                if (i == 5) break;
                x += inputHeight;
                g.drawLine(x, y + inputHeight, x, y + inputHeight + symbolsHeight - 4);
            }
            x = screenWidth - symbolsWidth + 2;
            y += inputHeight;
            g.drawLine(x, y, screenWidth - 3, y);

            for (int i = 0; i < SYMBOLS_CHARS.length;) {
                c = SYMBOLS_CHARS[i];
                if (i == symbolsCharID - 6) {
                    g.setColor(0x303030);
                    g.fillRect(x + 1, y + 1, inputHeight - 1, inputHeight - 1);
                    g.setColor(0xffffff);
                }
                drawChar(g, x, y, c);
                if (++i % 6 == 0) {
                    x = screenWidth - symbolsWidth + 2;
                    y += inputHeight;
                    g.drawLine(x, y, screenWidth - 3, y);
                } else {
                    x += inputHeight;
                }
            }
        }
    }

    private void drawChar(Graphics g, int x, int y, char c) {
        if (c == '\n') {
            g.drawLine(x + inputHeight - 2, y + 2, x + inputHeight - 2, y + 7);
            g.drawLine(x + 2, y + 7, x + inputHeight - 2, y + 7);
            g.drawLine(x + 2, y + 7, x + 5, y + 9);
            g.drawLine(x + 2, y + 7, x + 5, y + 5);
        } else {
            CanvasEx.drawString(g, CanvasEx.facade, new Character(c).toString(), x + inputHeight / 2, y + inputHeight, Graphics.BOTTOM | Graphics.HCENTER);
        }
    }

    public void repaint() {
        chat.invalidate();
    }

    private boolean isNumKey(int keyCode) {
        return (keyCode >= Canvas.KEY_NUM0) && (keyCode <= Canvas.KEY_NUM9);
    }

    private void checkCap(char ch) {
        if (capsStatus == CAPS_AUTO) {
            if (isLetterChar(ch)) {
                capsStatus = CAPS_OFF;
            }
        } else if (cursorPos > 1) {
            boolean flag = check(message.charAt(cursorPos - 2)) && (ch == ' ') && CAPS_NUMS != capsStatus;
            if (flag) {
                capsStatus = CAPS_AUTO;
            }
        }
    }

    private void switchCap() {
        if (capsStatus == CAPS_OFF) {
            capsStatus = CAPS_AUTO;
            currentChar = StringConvertor.toUpperCase(currentChar);
        } else if (capsStatus == CAPS_AUTO) {
            capsStatus = CAPS_ON;
        } else if (capsStatus == CAPS_NUMS) {
            capsStatus = CAPS_OFF;
        } else {
            if (currentLang == LANG_EN) {
                capsStatus = CAPS_NUMS;
                currentLang = LANG_RU;
            } else {
                capsStatus = CAPS_OFF;
                currentLang = LANG_EN;
            }
            insertCurrentChar();
        }
    }

    public final boolean a(char c1) {
        return cursorPos > 1 && check(message.charAt(cursorPos - 2)) && c1 == ' ';
    }

    public boolean keyReaction(int keyCode, int type, int gameAct) {
        boolean inserted = false;
        if (type == CanvasEx.KEY_RELEASED) {
            if ((gameAct == CanvasEx.KEY_CODE_RIGHT_MENU) || (gameAct == CanvasEx.KEY_CODE_LEFT_MENU)) {
                insertCurrentChar();
                chat.pressSoft(gameAct);
            } else if (gameAct == CanvasEx.KEY_CODE_BACK_BUTTON) {
                insertCurrentChar();
                afterTyping();
            } else if (uiMode == UI_MODE_NONE) {
                if ((capsStatus == CAPS_NUMS) && (isNumKey(keyCode))) {
                    currentChar = (char) keyCode;
                    insertCurrentChar();
                    lastPressedKeyTime = System.currentTimeMillis();
                    lastPressedKey = 0;
                    repaint();
                    return false;
                }
                if (t10Mode) {
                } else if ((keyCode == lastPressedKey) && (System.currentTimeMillis() - lastPressedKeyTime < DELAY_TIME)) {
                    keyCharID++;
                } else {
                    keyCharID = 0;
                    if (currentChar != 0) {
                        insertCurrentChar();
                        inserted = true;
                    }
                    updateCaretPosition();
                }
                lastPressedKey = keyCode;
                boolean ok = true;
                if ((keyCode == Canvas.KEY_STAR || keyCode == Canvas.KEY_POUND) && (currWord.length() > 0)) {     // TODO
                    byte b = 0;
                    if (StringConvertor.getString(getChars(keyCode)).indexOf(' ') < 0) {
                        if (keyCode == Canvas.KEY_STAR) {
                            b = 1;
                        } else {
                            b = -1;
                        }
                    }
                    ok = (b == 0);
                    if (!ok) {
                        switchWord(b);
                    }
                    //int i = (keyCode == Canvas.KEY_STAR) ? 1 : -1;
                    //switchWord(i);
                } else {
                    switch (writeKeyPressed(keyCode)) {
                        case ACTION_SYMBOLS:
                            uiMode = UI_MODE_SYMBOLS;
                            chat.buildMenu(true, cKey);
                            currentChar = 0;
                            break;

                        case ACTION_SHIFT:
                            switchCap();
                            break;

                        case ACTION_INPUT:
                            if (t10Mode) {
                                insertCurrentChar();
                                notifyT10(getChars(keyCode));
                                updateCaretPosition();
                            }
                            break;

                        case ACTION_ALTWIN:
                            Jimm.setPrevScreen(chat);
                            new InputTextBox(InputTextBox.EDITOR_MODE_ALT, chat.getContact().name, getString()).activate();
                            break;

                        case ACTION_SMILES:
                            Emotions.selectEmotion(this, chat);
                            break;

                        case ACTION_NONE:
                            ok = false;
                            break;
                    }
                }
                if (ok) {
                    repaint();
                    return false;
                }
            }
        }
        if (keyCode == -8) {
            keyCharID = 0;
            if (currWord.length() == 0) {
                insertCurrentChar();
                if ((message.length() > 0) && (cursorPos > 0)) {
                    message.deleteCharAt(cursorPos - 1);
                    cursorPos--;
                    boolean flag = (cursorPos == 0);
                    if (cursorPos > 1) {
                        flag |= (message.charAt(cursorPos - 1) == ' ' && check(message.charAt(cursorPos - 2)));
                    }
                    if (flag && capsStatus != CAPS_NUMS) {
                        capsStatus = CAPS_AUTO;
                    }
                }
            } else {
                currWord.setLength(currWord.length() - 1);
                if (currWord.length() > 0) {
                    notifyT10(currChars.elementAt(Math.max(currChars.size() - 2, 0)));
                } else {
                    currChars.removeAllElements();
                    words.removeAllElements();
                }
            }
            updateCaretPosition();
        } else if (keyCode == -50) {
            currentLang = (currentLang == LANG_EN) ? LANG_RU : LANG_EN;
            insertCurrentChar();
//#sijapp cond.if target is "MIDP2"#
        } else if (keyCode == -10) {
            Emotions.selectEmotion(this, chat);
//#sijapp cond.end#
        } else if (isNumKey(keyCode) && (uiMode == UI_MODE_NONE)) {
            if ((lastPressedKey == keyCode) && (System.currentTimeMillis() - lastPressedKeyTime >= 500)) {
                currentChar = (char) keyCode;
                if (currWord.length() > 0) {
                    currWord.setCharAt(currWord.length() - 1, currentChar);
                    currentChar = 0;
                } else {
                    insertCurrentChar();
                }
                lastPressedKeyTime = System.currentTimeMillis();
                lastPressedKey = 0;
            }
        } else if (gameAct == Canvas.LEFT) {
            if (uiMode == UI_MODE_SYMBOLS) {
                symbolsCharID--;
                if (symbolsCharID < 0) {
                    symbolsCharID = (byte) (SYMBOLS_CHARS.length + 5);
                }
            } else {
                if (cursorPos > 0 && !inserted && currWord.length() == 0) {
                    cursorPos--;
                    if (t10Mode) {
                        notifyT10(new Integer(-1));
                    }
                } else if (currWord.length() > 0) {
                    int len = currWord.length();
                    insertCurrWord();
                    cursorPos -= len;
                }
                updateCaretPosition();
            }
        } else if (gameAct == Canvas.RIGHT) {
            if (uiMode == UI_MODE_SYMBOLS) {
                symbolsCharID++;
                if (symbolsCharID >= SYMBOLS_CHARS.length + 6) {
                    symbolsCharID = 0;
                }
            } else {
                if (currWord.length() > 0) {
                    insertCurrWord();
                } else if (!inserted) {
                    int len = message.length();
                    if (cursorPos < len) {
                        cursorPos++;
                        if (t10Mode) {
                            notifyT10(new Integer(1));
                        }
                    } else if (t10Mode && cursorPos == len && message.charAt(len - 1) != ' ') {
                        insert(" ");
                    }
                }
                updateCaretPosition();
            }
        } else if (gameAct == Canvas.FIRE) {
            if (type == CanvasEx.KEY_RELEASED) {
                if (uiMode == UI_MODE_NONE) {
                    insertCurrentChar();
                    try {
                        JimmUI.sendMessage(getString(), chat.getContact());
                    } catch (Exception ignored) {
                    }
                    setString("");
                    afterTyping();
                } else {
                    if (symbolsCharID > 5) {
                        currentChar = SYMBOLS_CHARS[symbolsCharID - 6];
                    } else currentChar = lastChars[symbolsCharID];
                    int index = lastChars.length - 1;
                    for (int i = lastChars.length - 1; i >= 0; i--) {
                        if (lastChars[i] == currentChar) {
                            index = i;
                        }
                    }
                    for (int i = index; i > 0; i--) {
                        lastChars[i] = lastChars[i - 1];
                    }
                    lastChars[0] = currentChar;
                    if (currentChar == '\n') {
                        currentChar = '\u206F';
                    }
                    insertCurrentChar();
//					keyCharID = 0;
                    uiMode = UI_MODE_NONE;
                    chat.buildMenu(false, cKey);
                    symbolsCharID = 0;
                }
                updateCaretPosition();
            }
        } else if (gameAct == Canvas.UP) {
            if (uiMode == UI_MODE_SYMBOLS) {
                if (symbolsCharID > 5) {
                    symbolsCharID -= 6;
                } else {
                    symbolsCharID += 42;
                }
                while (symbolsCharID >= SYMBOLS_CHARS.length + 6) {
                    symbolsCharID--;
                }
            } else if (currWord.length() > 0) {
                switchWord(-1);
            } else {
                cursorPos = 0;
                updateCaretPosition();
            }
        } else if (gameAct == Canvas.DOWN) {
            if (uiMode == UI_MODE_SYMBOLS) {
                if (symbolsCharID < 42) {
                    symbolsCharID += 6;
                } else {
                    symbolsCharID -= 42;
                }
                while (symbolsCharID >= SYMBOLS_CHARS.length + 6) {
                    symbolsCharID--;
                }
            } else if (currWord.length() > 0) {
                switchWord(1);
            } else {
                cursorPos = message.length();
                updateCaretPosition();
            }
        }
        repaint();
        return false;
    }

    private byte writeKeyPressed(int keyCode) {
        lastPressedKeyTime = System.currentTimeMillis();

        String numChars = getChars(keyCode);
        if (numChars == null) {
            return ACTION_NONE;
        }
        if (keyCharID >= numChars.length()) {
            keyCharID = 0;
        }
        char ch = numChars.charAt(keyCharID);
        if (ch == '\t') {
            return ACTION_SYMBOLS;
        }
        if (ch == '\n') {
            return ACTION_SHIFT;
        }
        if (ch == '\013') {
            return ACTION_ALTWIN;
        }
        if (ch == '\f') {
            return ACTION_SMILES;
        }
        currentChar = ch;
        if ((capsStatus == CAPS_ON) || (capsStatus == CAPS_AUTO)) {
            currentChar = StringConvertor.toUpperCase(currentChar);
        }
        updateCaretPosition();
        return ACTION_INPUT;
    }

    public final void g() {
        //if (vike != null) {
        //    vike.unlock();
        //}
        chat.setChatEx();
    }

    public final void upd() {
        if (vike != null)
            vike.upd();
        //updSize();
    }

    public final void setForcedSize() {
        TextFieldEx textFieldEx = this;
        chat.setForcedSize(-1, VirtualList.getHeight() - (inputHeight + 4 + (textFieldEx.vike == null ? textFieldEx.chat.getMenuBarHeight() : textFieldEx.vike.height)));
    }

    public void screenChange() {
        screenWidth = ChatTextList.getWidth();
        upd();
    }

    private String getChars(int keyCode) {
        return getCachedChars((Object[]) table.get(new Integer(keyCode)), (currentLang == LANG_RU) ? 0 : 1);
    }

    private String getCachedChars(Object[] chars, int idx) {
        if (chars == null) {
            return null;
        }
        return (String) chars[Math.min(idx, chars.length - 1)];
    }

    private void insertCurrentChar() {
        if (currentChar == 0) {
            if (currWord.length() > 0) {
                insertCurrWord();
            }
            return;
        }
        if (currWord.length() > 0 || t10Mode) {
            if (currentChar == ' ') {
                insertCurrWord();
            } else {
                char ch = StringConvertor.toLowerCase(currentChar);
                //if (isLetterChar(ch)) {
                currWord.append(ch);
                //}
                currentChar = 0;
                return;
            }
        }
        if (message.length() == 0) {
            message.append(currentChar);
            if (!chat.getContact().uTyping()) {
                chat.getContact().beginTyping(true);
            }
        } else {
            message.insert(cursorPos, currentChar);
        }
        cursorPos++;
        checkCap(currentChar);
        currentChar = 0;
        updateCaretPosition();
    }

    private boolean isLetterChar(char ch) {
        char ch0 = StringConvertor.toLowerCase(ch);
        return (ch0 >= 'a' && ch0 <= 'z') || (ch0 >= '\u0430' && ch0 <= '\u0451') || (ch0 == '\u0451');
    }

    private boolean check(char ch) {
        return (ch == '.' || ch == '!' || ch == '?');
    }

    private boolean equals(byte[] b0, byte[] b1) {
        if (b0.length != b1.length) {
            return false;
        }
        int len = b0.length;
        for (int i = len - 1; i >= 0; i--) {
            if (b0[i] != b1[i]) {
                return false;
            }
        }
        return true;
    }
}
//#sijapp cond.end#
