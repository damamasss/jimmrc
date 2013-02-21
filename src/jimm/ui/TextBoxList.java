//package jimm.ui;
//
////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//
//import DrawControls.*;
//import jimm.*;
//import jimm.comm.StringConvertor;
//import jimm.comm.Util;
//import jimm.comm.DateAndTime;
//import jimm.util.FastTypeBase;
//
//import javax.microedition.lcdui.*;
//import java.io.DataInputStream;
//import java.io.InputStream;
//import java.io.ByteArrayInputStream;
//import java.util.Enumeration;
//import java.util.Hashtable;
//import java.util.TimerTask;
//import java.util.Vector;
//
//public class TextBoxList extends VirtualList implements CommandListener{
//    /**
//     * @author Rishat Shamsutdinov
//     */
//    private final static boolean LANG_EN = true;
//    private final static boolean LANG_RU = false;
//    private static short DELAY_TIME = 700;
//
//    private static final byte PARSE_KEYCODE = 0;
//    private static final byte PARSE_CHARS = (byte) 1;
//    private static final byte PARSE_CKEY = (byte) 2;
//
//    private static Hashtable table;
//    public static boolean cKey = true;
//    private int heightText;
//    private Vector lines = new Vector();
//    private int currRow;
//    private char currentChar = 0;
//
//    private final char[] SYMBOLS_CHARS = {'\u002E', '\u002C', '\'', '\u003F', '\u0021', '\u0022', '\u002D', '\u0028', '\u0029',
//            '\u0040', '\u002F', '\u003A', '\u005F', '\u003B', '\u002B', '\u0026', '\u0025', '\u002A',
//            '\u003D', '\u003C', '\u003E', '\u00A3', '\u20AC', '\u0024', '\u00A5', '\u00A4', '\u005B',
//            '\u005D', '\u007B', '\u007D', '\\', '\u007E', '\u005E', '\u00BF', '\u00A7', '\u0023',
//            '\u007C', '\n', '\u0060'};
//
//    private StringBuffer message;
//    private long lastPressedKeyTime;
//    private static int inputHeight, symbolsWidth = 0, symbolsHeight = 0;
//    private static char[] lastChars;
//
//    public TextBoxList() {
//        super("");
//        setColorScheme();
//        addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
//        addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
//        setCommandListener(this);
//        loadTable();
//        if (lastChars == null) {
//            lastChars = new char[6];
//            System.arraycopy(SYMBOLS_CHARS, 0, lastChars, 0, 6);
//        }
//        message = new StringBuffer();
//        inputHeight = JimmUI.standartFAcade.getFontHeight();
//        lastPressedKeyTime = System.currentTimeMillis();
//        if (symbolsHeight == 0 && symbolsWidth == 0) {
//            symbolsWidth = inputHeight * 6 + 4;
//            symbolsHeight = inputHeight * 7 + 4;
//        }
//    }
//
//    public int getSize() {
//        return lines.size();
//    }
//
//    protected void get(int index, ListItem item) {
//    }
//
//    private void loadTable() {
//        if (table != null) {
//            return;
//        }
//        table = new Hashtable();
//        InputStream stream = this.getClass().getResourceAsStream("/keychars.txt");
//        if (stream == null) {
//            return;
//        }
//        try {
//            DataInputStream dos = new DataInputStream(stream);
//            byte[] str = new byte[dos.available()];
//            dos.read(str);
//            String content = Util.byteArrayToString(str, Util.isDataUTF8(str)).trim();
//            content = Util.removeCr(content);
//            String[] lines = Util.explode(content, '\n');
//            int count = lines.length;
//            String line;
//            for (int i = 0; i < count; i++) {
//                line = lines[i] + ' ';
//                if (line.trim().startsWith("//") || line.trim().length() == 0) {
//                    continue;
//                }
//                byte state = PARSE_KEYCODE;
//                int len = line.length();
//                int bPos = 0;
//                int keyCode = 0;
//                boolean lang = LANG_RU;
//                int comCount = 0;
//                String ru = null;
//                for (int j = 0; j < len; j++) {
//                    char c = line.charAt(j);
//                    switch (state) {
//                        case PARSE_KEYCODE:
//                            if (c == '=') {
//                                try {
//                                    keyCode = Integer.parseInt(line.substring(bPos, j).trim());
//                                } catch (Exception e) {
//                                }
//                                if (keyCode == 0 && !line.substring(Math.max(0, bPos - 7), j).trim().equals("TIMEOUT")) {
//                                    throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
//                                }
//                                bPos = j + 1;
//                                state = PARSE_CHARS;
//                            } else if (c == 'C') {
//                                state = PARSE_CKEY;
//                            } else if (c < '0' || c > '9') {
//                                bPos++;
//                            }
//                            break;
//                        case PARSE_CHARS:
//                            if (c == ' ' || c == '\n' || c == '\r' || j == len - 1) {
//                                if (j == len - 1) {
//								    j++;
//                                }
//                                if (bPos == j) {
//                                    throw (new Exception("Line(" + (i + 1) + "): syntax error (keychars.txt)"));
//                                }
//                                if (keyCode == 0) {
//                                    try {
//                                        DELAY_TIME = Short.parseShort(line.substring(bPos, j).trim());
//                                        j = len;
//                                        break;
//                                    } catch (Exception e) {
//                                        throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
//                                    }
//                                }
//                                if (lang == LANG_RU) {
//                                    ru = doChars(line.substring(bPos, j));
//                                    bPos = j + 1;
//                                    lang = LANG_EN;
//                                } else if (lang == LANG_EN) {
//                                    if (ru == null) {
//                                        throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
//                                    }
//                                    String en = doChars(line.substring(bPos, j));
//                                    table.put(new Integer(keyCode), new String[]{ru, en});
//                                    j = len;
//                                }
//                            } else if (c == '/') {
//                                if (++comCount > 1) {
//                                    if (ru == null) {
//                                        throw (new Exception("Line(" + (i + 1) + ") syntax error (keychars.txt): " + line.substring(bPos, j)));
//                                    }
//                                    table.put(new Integer(keyCode), new String[]{ru});
//                                    j = len;
//                                }
//                            }
//                            break;
//                        case PARSE_CKEY:
//                            if (c == '0') {
//                                cKey = false;
//                                j = len;
//                            } else if (c == '/') {
//                                if (++comCount > 1) {
//                                    j = len;
//                                }
//                            }
//                            break;
//                    }
//                }
//            }
//            dos.close();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        try {
//            stream.close();
//        } catch (Exception e) {
//        }
//    }
//
//    public void paintAllOnGraphics(Graphics g, int mode, int curX, int curY) {
//            if (mode == DMS_DRAW) {
//                int menuBarHeight = getMenuBarHeight();
//                int bottom = getHeightInternal();
//                g.setColor(0xffffff);
//                g.fillRect(0, 0, getWidth(), getHeight());
//                int top = getCapHeight();
//                int linesCount = lines.size();
//                if (linesCount > 0) {
//                    for (int line = 0; line < linesCount - 1; line++) {
//                        CanvasEx.drawString(g, JimmUI.standartFAcade, (String)lines.elementAt(line), 1, top, Graphics.TOP | Graphics.LEFT);
//                        top+=(inputHeight + 2);
//                    }
//                }
//                drawMenuBar(g, menuBarHeight, bottom, mode, -1, -1);
//                drawCaption(g, mode, curX, curY);
//                return;
//            }
//            super.paintAllOnGraphics(g, mode, curX, curY);
//    }
//
////    void validateCursorH(int i){
////        TextLine textLine = paintList.getLine(currItem);
////        StringBuffer stringBuffer = new StringBuffer();
////        textLine.readText(stringBuffer);
////        int sizeLine = stringBuffer.length();
////        currRow +=i;
////        if (currRow > sizeLine) {
////            validateCursorV(1);
////        }
////        if (currRow < 0) {
////            validateCursorV(-1);
////        }
////	}
////
////    void validateCursorV(int i){
////        int sizeList = paintList.getSize();
////        currItem+=i;
////        if (currItem > sizeList - 1) {
////            currItem = 0;
////        }
////        if (currItem < 0) {
////            currItem = sizeList - 1;
////        }
////        correctHorisontal();
////	}
////
////     void correctHorisontal(){
////        TextLine textLine = paintList.getLine(currItem);
////        StringBuffer stringBuffer = new StringBuffer();
////        textLine.readText(stringBuffer);
////        int sizeLine = stringBuffer.length();
////        if (currRow > sizeLine) {
////            currRow = sizeLine;
////        }
////	}
//
//    private String doChars(String src) {
//        String result = StringConvertor.replaceStr(src.trim(), "SPACE", " ");
//        result = StringConvertor.replaceStr(result, "SYMBOLS", "\t");
//        result = StringConvertor.replaceStr(result, "ALTWIN", "\013");
//        result = StringConvertor.replaceStr(result, "SMILES", "\f");
//        return StringConvertor.replaceStr(result, "SHIFT", "\n");
//    }
//
//    private boolean isNumKey(int keyCode) {
//        return (keyCode >= Canvas.KEY_NUM0) && (keyCode <= Canvas.KEY_NUM9);
//    }
//
//    protected boolean keyReaction(int keyCode, int type, int gameAct) {
//        if (isNumKey(keyCode)) {
//            currentChar = (char) keyCode;
//            insertCurrentChar();
//            return false;
//        }
//        return super.keyReaction(keyCode, type, gameAct);
//    }
//
//    private void insertCurrentChar() {
//        if (currentChar == 0) {
//            return;
//        }
//        insertText(String.valueOf(currentChar));
//        currentChar = 0;
//    }
//
//    private void insertText(String ch) {
//        for (int i = 0; i < ch.length() -1; i++) {
//            if (lines.size() < 1 || JimmUI.standartFAcade.stringWidth(message.toString() + ch.charAt(i)) > getWidth()) {
//                message.setLength(0);
//                message.append(ch);
//                lines.addElement(message.toString());
//                System.out.println("aga " + ch);
//                System.out.println("aga " + ch);
//            } else {
//                message.append(ch);
//                lines.setElementAt(message.toString(), i);
//                System.out.println("no " + ch);
//            }
//            System.out.println(lines.size() + " - it vec");
//        }
//    }
//
//    public void commandAction(Command c, Displayable d) {
//            if (c.equals(JimmUI.cmdMenu)) {
//            } else if (c.equals(JimmUI.cmdBack)) {
//                Jimm.getContactList().activate();
//            }
//        }
//}
////#sijapp cond.end#
