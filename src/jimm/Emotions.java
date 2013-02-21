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
 File: src/jimm/Emotions.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

//#sijapp cond.if modules_SMILES is "true" #

import DrawControls.*;
import jimm.comm.Util;
import jimm.ui.Selector;
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
import jimm.chat.TextFieldEx;
import jimm.util.Device;
//#sijapp cond.end#

import javax.microedition.lcdui.*;
import java.io.*;
import java.util.Vector;

public class Emotions implements VirtualListCommands, CommandListener {

    private static Emotions _this;
    public static ImageList images = new ImageList();
    final private static Vector findedEmotions = new Vector();
    private static boolean used;
    public static int[] selEmotionsIndexes;
    public static String[] selEmotionsWord;
    //public static String[] selEmotionsSmileNames;
    private static int[] textCorrIndexes;
    private static String[] textCorrWords;
    private static boolean[] emoFinded;
    public static int iconsSize = 0;
    protected static boolean txt = false;

    public Emotions() {
        //NativeCanvas.addAction("Emotions", ContactList.menuIcons.elementAt(3));
        used = false;
        _this = this;
        Vector textCorr = new Vector();
        Vector selEmotions = new Vector();
        InputStream stream = getClass().getResourceAsStream("/smiles.ini");
        if (stream == null) {
            stream = getClass().getResourceAsStream("/smiles.txt");
            if (stream == null) return;
            txt = true;
        }

        try {
            byte[] allStream = new byte[stream.available()];
            stream.read(allStream);
            CharImputStream cis = new CharImputStream(allStream);
            parseSmiles(cis, textCorr, selEmotions);
//#sijapp cond.if modules_GIFSMILES is "true" #
            images = new GifImageList();
            images.load("/smiles", iconsSize, iconsSize);
            if (images.size() == 0) {
                images = new ImageList();
                images.load("/smiles.png", iconsSize, iconsSize);
            }
//#sijapp cond.else #

//#sijapp cond.if modules_ANISMILES is "true" #
            images = new AniImageList();
            images.load("/smiles", iconsSize, iconsSize);
            if (images.size() == 0) {
                images = new ImageList();
                images.load("/smiles.png", iconsSize, iconsSize);
            }
//#sijapp cond.else #
            images = new ImageList();
            images.load("/smiles.png", iconsSize, iconsSize);
//#sijapp cond.end #
//#sijapp cond.end #
            if (images.size() == 0) {
                throw new Exception();
            }
            iconsSize = images.getHeight();
            //NativeCanvas.addAction("Emotions OK", ContactList.menuIcons.elementAt(3));
        } catch (Exception ignored) {
        } catch (OutOfMemoryError ignored) {
        }

        try {
            stream.close();
        } catch (Exception ignored) {
        }
        used = true;
    }

    private void parseSmiles(CharImputStream dos, Vector textCorr, Vector selEmotions) {
        try {
            boolean eof = false, clrf = false;
            StringBuffer strBuffer = new StringBuffer();
            readIS(strBuffer, dos);
            iconsSize = Integer.parseInt(strBuffer.toString());
            int currIndex = 0;
            for (; ;) {
                if (txt) {
                    readIS(strBuffer, dos);
                    //Integer currIndex = Integer.valueOf(strBuffer.toString());
                    readIS(strBuffer, dos);
                    //String smileName = strBuffer.toString();
                }
                for (int i = 0; ; i++) {
                    try {
                        clrf = readIS(strBuffer, dos);
                    } catch (EOFException eofExcept) {
                        eof = true;
                    }
                    String word = new String(strBuffer).trim();
                    if (word.length() != 0) insertTextCorr(textCorr, word, new Integer(currIndex));
                    if (i == 0) selEmotions.addElement(new Object[]{new Integer(currIndex), word});
                    if (clrf || eof) {
                        currIndex++;
                        break;
                    }
                }
                if (eof) break;
            }

            int size = selEmotions.size();
            Object[] data;
            selEmotionsIndexes = new int[size];
            selEmotionsWord = new String[size];
            //selEmotionsSmileNames = new String[size];
            for (int i = 0; i < size; i++) {
                data = (Object[]) selEmotions.elementAt(i);
                selEmotionsIndexes[i] = ((Integer) data[0]).intValue();
                selEmotionsWord[i] = (String) data[1];
                //selEmotionsSmileNames[i] = (String) data[2];
            }

            size = textCorr.size();
            textCorrWords = new String[size];
            textCorrIndexes = new int[size];
            emoFinded = new boolean[size];
            for (int i = 0; i < size; i++) {
                data = (Object[]) textCorr.elementAt(i);
                textCorrWords[i] = (String) data[0];
                textCorrIndexes[i] = ((Integer) data[1]).intValue();
            }
        } catch (Exception ignored) {
        }
    }

    // Reads simple word from stream. Returns "true" if break was found after word
    private static boolean readIS(StringBuffer buffer, CharImputStream stream) throws IOException {
        char chr;
        buffer.setLength(0);
        for (; ;) {
            chr = stream.readChar();
            if ((chr == ',') || (chr == '\n') || (chr == '\t')) break;
            if (chr >= ' ') buffer.append(chr);
        }
        return (chr == '\n');
    }

    // Add smile text and index to textCorr in decreasing order of text length
    static void insertTextCorr(Vector textCorr, String word, Integer index) {
        Object[] data = new Object[]{word, index};
        int wordLen = word.length();
        int size = textCorr.size();
        int insIndex = 0;
        Object[] cvtData;
        for (; insIndex < size; insIndex++) {
            cvtData = (Object[]) textCorr.elementAt(insIndex);
            int cvlDataWordLen = ((String) cvtData[0]).length();
            if (cvlDataWordLen <= wordLen) {
                textCorr.insertElementAt(data, insIndex);
                return;
            }
        }
        textCorr.addElement(data);
    }

    static private void findEmotionInText(String text, String emotion, int index, int startIndex, int recIndex) {
        if (!emoFinded[recIndex]) return;
        int findedIndex, len = emotion.length();
        findedIndex = text.indexOf(emotion, startIndex);
        if (findedIndex == -1) {
            emoFinded[recIndex] = false;
            return;
        }
        findedEmotions.addElement(new int[]{findedIndex, len, index});
    }

    static public void addTextWithEmotions(TextList textList, String text, int fontStyle, int textColor, int bigTextIndex) {
        if (!used || !Options.getBoolean(Options.OPTION_USE_SMILES)) {
            try {
                textList.addBigText(text, textColor, fontStyle, bigTextIndex);
            } catch (Exception e) {
                textList.addBigText(text, textColor, Font.STYLE_PLAIN, bigTextIndex);
            }
            return;
        }
        for (int i = emoFinded.length - 1; i >= 0; i--) emoFinded[i] = true;

        int startIndex = 0;
        for (; ;) {
            findedEmotions.removeAllElements();
            int size = textCorrWords.length;
            for (int i = 0; i < size; i++) {
                findEmotionInText(text, textCorrWords[i], textCorrIndexes[i], startIndex, i);
            }
            if (findedEmotions.isEmpty()) break;
            int count = findedEmotions.size();
            int minIndex = 100000, data[], minArray[] = null;
            for (int i = 0; i < count; i++) {
                data = (int[]) findedEmotions.elementAt(i);
                if (data[0] < minIndex) {
                    minIndex = data[0];
                    minArray = data;
                }
            }
            if (startIndex != minIndex) {
                try {
                    textList.addBigText(text.substring(startIndex, minIndex), textColor, fontStyle, bigTextIndex);
                } catch (Exception e) {
                    textList.addBigText(text.substring(startIndex, minIndex), textColor, Font.STYLE_PLAIN, bigTextIndex);
                }
            }
            textList.addImage(images.elementAt(minArray[2]), text.substring(minIndex, minIndex + minArray[1]), bigTextIndex);
            startIndex = minIndex + minArray[1];
        }

        int lastIndex = text.length();

        if (lastIndex != startIndex) {
            try {
                textList.addBigText(text.substring(startIndex, lastIndex), textColor, fontStyle, bigTextIndex);
            } catch (Exception e) {
                textList.addBigText(text.substring(startIndex, lastIndex), textColor, Font.STYLE_PLAIN, bigTextIndex);
            }
        }
    }

    public static String emotionText;
    private static Selector selector;
    private static int caretPos;
    private static Object lastScreen;
    private static Object object;
    private static int lastSmile = 0;

    static public void selectEmotion(Object o, Object screen) {
        lastScreen = screen;
        object = o;
        if (o instanceof TextBox) {
            caretPos = ((TextBox) o).getCaretPosition();
        }
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
        else if (o instanceof TextField) {
            caretPos = ((TextField) o).getCaretPosition();
        }
//#sijapp cond.end#
        selector = new Selector(-1, null, lastSmile);
        selector.setColorScheme();
        selector.addCommandEx(JimmUI.cmdSelect, TextList.MENU_TYPE_LEFT_BAR);
        selector.addCommandEx(JimmUI.cmdBack, TextList.MENU_TYPE_RIGHT_BAR);
        selector.setCommandListener(_this);
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//        if (o instanceof TextField) {
//            selector.pointerReleasedEmu(-1, -1);
//        }
//#sijapp cond.end#
        Jimm.setDisplay(selector);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdSelect) {
            select();
        } else if (c == JimmUI.cmdBack) {
            Jimm.setDisplay(lastScreen);
            selector = null;
        }
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
        select();
    }

    static public void select() {
        lastSmile = selector.getCurrSelectedIdx();
        StringBuffer smile = new StringBuffer();
        try {
            smile.append(' ').append(Emotions.getSelectedEmotion()).append(' ');
            if (object instanceof TextBox) {
                ((TextBox) object).insert(smile.toString(), caretPos);
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
            } else if (object instanceof TextField) {
                ((TextField) object).insert(smile.toString(), caretPos);
            } else {
                ((TextFieldEx) object).insert(smile.toString());
//#sijapp cond.end#
            }
        } catch (Exception ignored) {
        }
        object = null;
        Jimm.setDisplay(lastScreen);
        selector = null;
    }

    static public void selectFast() {
        StringBuffer smile = new StringBuffer();
        try {
            smile.append(' ').append(Emotions.getSelectedEmotion()).append(' ');
            if (object instanceof TextBox) {
                ((TextBox) object).insert(smile.toString(), caretPos);
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
            } else if (object instanceof TextField) {
                ((TextField) object).insert(smile.toString(), caretPos);
            } else {
                ((TextFieldEx) object).insert(smile.toString());
//#sijapp cond.end#
            }
//#sijapp cond.if modules_PANEL is "true"#            
            NativeCanvas.addAction(Emotions.getSelectedEmotion(), null);
//#sijapp cond.end#
        } catch (Exception ignored) {
        }
    }

    static public String getSelectedEmotion() {
        return emotionText;
    }

    static public boolean isMyOkCommand(Command command) {
        return (command == JimmUI.cmdSelect);
    }

    private class CharImputStream {
        String allStream;
        int pos = 0;

        private CharImputStream(byte[] allStream) {
            this.allStream = Util.byteArrayToString(allStream, Util.isDataUTF8(allStream));
            pos = 0;
        }

        public char readChar() throws IOException {
            if (pos >= allStream.length()) throw new EOFException();
            return allStream.charAt(pos++);
        }
    }
}
//#sijapp cond.end#