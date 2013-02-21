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
 File: src/DrawControls/TextList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/


package DrawControls;

import jimm.comm.StringConvertor;
import jimm.Options;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class TextItem {
    public Icon image;
    public String text;
    private int fontAndColor = 0;
    private int itemWidthAndHeight = 0;

    public int getHeight(int fontSize) {
        if (image != null) return image.getHeight() + 1;
        if (text == null) return 0;
        if ((itemWidthAndHeight & 0xFFFF) == 0) {
            FontFacade font = new FontFacade(CanvasEx.getSuperFont(Font.FACE_SYSTEM, getFontStyle(), fontSize));
            itemWidthAndHeight = (itemWidthAndHeight & 0xFFFF0000) | font.getFontHeight();
        }
        return itemWidthAndHeight & 0xFFFF;
    }

    public int getWidth(int fontSize) {
        if (image != null) {
            return image.getWidth() + 1;
        }
        if ((text == null) || (text.length() == 0)) {
            return 0;
        }
        if ((itemWidthAndHeight & 0xFFFF0000) == 0) {
            FontFacade font = new FontFacade(CanvasEx.getSuperFont(Font.FACE_SYSTEM, getFontStyle(), fontSize));
            itemWidthAndHeight = (itemWidthAndHeight & 0x0000FFFF) | ((font.stringWidth(text) << 16) & 0xFFFF0000);
        }
        return (itemWidthAndHeight >> 16) & 0xFFFF;
    }

    public int getColor() {
        return fontAndColor & 0xFFFFFF;
    }

    public void setColor(int value) {
        fontAndColor = (fontAndColor & 0xFF000000) | (value & 0x00FFFFFF);
    }

    public int getFontStyle() {
        return (fontAndColor >> 24) & 0xFF;
    }

    public void setFontStyle(int value) {
        fontAndColor = (fontAndColor & 0x00FFFFFF) | ((value << 24) & 0xFF000000);
    }
}

public class TextList extends VirtualList {
    private static final int BORDER = 1;

    // Vector of lines. Each line contains cols. Col can be text or image
    private Vector lines = new Vector();

    private boolean rollFlag = true;
    private boolean newStyle = false;

    // Sent messages table (id -> index)
    private Hashtable table;

    //! Construct new text list with default values of colors, font size etc...
    public TextList(String capt) { //!< Caption of list
        super(capt);
    }

    public TextList(String capt, boolean rollFlag) {
        super(capt);
        this.rollFlag = rollFlag;
    }

    public TextList(boolean newStyle, String capt) {
        super(capt);
        this.newStyle = newStyle;
    }

    // protected int getSize()
    public int getSize() {
        if (lines.isEmpty()) {
            return 0;
        }
        int size = lines.size();
        return (((TextLine) lines.lastElement()).size() == 0) ? size - 1 : size;
    }

    public TextLine getLine(int index) {
        return (TextLine) lines.elementAt(index);
    }

    protected boolean isItemSelected(int index) {
        int selIndex = getCurrIndex();
        int textIndex = (selIndex >= lines.size()) ? -1 : getLine(selIndex).bigTextIndex;
        return textIndex != -1 && (getLine(index).bigTextIndex == textIndex);
        }

    // protected void get(int index, ListItem item)
    protected void get(int index, ListItem item) {
        TextLine listItem = getLine(index);
        item.clear();
        if (listItem.size() == 0) return;

        TextItem titem = listItem.elementAt(0);
        item.text = titem.text;
        item.color = titem.getColor();
        item.fontStyle = titem.getFontStyle();
    }

    //! Remove all lines form list
    public void clear() {
        lines.removeAllElements();
        if (table != null) table.clear();
        setCurrentItem(0);
        invalidate();
    }

    //! Add new text item to list
    public void add(String text, int color) {
        internAdd(text, color, Font.STYLE_PLAIN, -1, true, '\0');
        invalidate();
    }

    private void internAdd(String text, int color, int fontStyle, int textIndex, boolean doCRLF, char last_charaster) {
        TextItem newItem = new TextItem();

        newItem.text = text;
        newItem.setColor(color);
        newItem.setFontStyle(fontStyle);

        if (lines.isEmpty()) {
            lines.addElement(new TextLine());
        }
        TextLine textLine = (TextLine) lines.lastElement();
        textLine.add(newItem);
        textLine.bigTextIndex = textIndex;
        if (doCRLF) {
            textLine.last_charaster = last_charaster;
            TextLine newLine = new TextLine();
            newLine.bigTextIndex = textIndex;
            lines.addElement(newLine);
        }
    }

    //! Add new black text item to list
    public void add(String text) {//!< Text of new item
        add(text, textColor);
    }

    public int getItemHeight(int itemIndex) {
        if (cursorMode != MODE_TEXT)
            return Math.max(Options.getInt(Options.OPTION_ICONS_CANVAS), super.getItemHeight(itemIndex));
        if (itemIndex >= lines.size()) return 1;
        return getLine(itemIndex).getHeight(getFontSize()) + 2;
    }

    private int getItemWidth(int index) {
        if (index >= lines.size()) return BORDER;
        return getLine(index).getWidth(getFontSize()) + 2;
    }

    protected int getCapX(int x) {
        if (newStyle) {
            return (getWidthInternal() - facade.stringWidth(getCaption())) / 2;
        }
        return x;
    }

    // Overrides VirtualList.drawItemData
    protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight) {
        if (cursorMode != MODE_TEXT) {
            super.drawItemData(g, index, x1, y1, x2, y2, fontHeight);
            return;
        }

        TextLine line = getLine(index);
        line.paint(newStyle ? BORDER + (getWidthInternal() - getItemWidth(index)) / 2 : BORDER, y1, g, getFontSize(), this);
    }

    protected void drawCursor(Graphics g, int top_y, int grCursorY1, int grCursorY2, int itemWidth, int topItem, int size, int height, int x) {
        if (!(newStyle))
            super.drawCursor(g, top_y, grCursorY1, grCursorY2, itemWidth, topItem, size, height, x);
        else {
            itemWidth = Math.min(itemWidth, getItemWidth(getCurrIndex()) + 2);
            super.drawCursor(g, top_y, grCursorY1, grCursorY2, itemWidth, topItem, size, height, x + (getWidthInternal() - itemWidth) / 2);
        }
    }


    // Overrides VirtualList.moveCursor
    public void moveCursor(int step, boolean moveTop) {
        int size, changeCounter = 0, currTextIndex, i, halfSize = getVisCount() / 2;

        switch (step) {
            case -1:
            case 1:
                currTextIndex = getCurrTextIndex();
                size = getSize();
                storelastItemIndexes();

                TextLine item;
                for (i = 0; i < halfSize;) {
                    currItem += step;
                    if ((currItem < 0) || (currItem >= size)) {
                        if (changeCounter != 0) currItem -= step;
                        break;
                    }
                    item = getLine(currItem);
                    if (currTextIndex != item.bigTextIndex) {
                        currTextIndex = item.bigTextIndex;
                        changeCounter++;
                        if ((changeCounter == 2) || (!visibleItem(currItem) && (i > 0))) {
                            currItem -= step;
                            break;
                        }
                    }

                    if (!visibleItem(currItem) || (changeCounter != 0)) i++;
                }

                if (!rollFlag) checkCurrItem();
                else {
                    if (currItem < 0) currItem = size - 1;
                    else if (currItem > size - 1) currItem = 0;
                }
                checkTopItem();
                repaintIfLastIndexesChanged();
                break;

            default:
                super.moveCursor(step, moveTop);
                return;
        }
    }

    public String getTextByIndex(int offset, boolean wholeText, int textIndex) {
        StringBuffer result = new StringBuffer();

        // Fills the lines
        int size = lines.size();
        TextLine line;
        for (int i = 0; i < size; i++) {
            line = getLine(i);
            if (wholeText || (textIndex == -1) || (line.bigTextIndex == textIndex)) {
                line.readText(result);
                if (line.last_charaster != '\0') {
                    if (line.last_charaster == '\n') {
                        result.append("\n");
                    } else {
                        result.append(line.last_charaster);
                    }
                }
            }
        }

        if (result.length() == 0) return null;
        String resultText = result.toString();
        int len = resultText.length();
        if (offset > len) return null;
        return resultText.substring(offset, len);
    }

    public void selectTextByIndex(int textIndex) {
        if (textIndex == -1) {
            return;
        }
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            if (getLine(i).bigTextIndex == textIndex) {
                setCurrentItem(i);
                break;
            }
        }
    }

    // Returns lines of text which were added by
    // methon addBigText in current selection
    public String getCurrText(int offset, boolean wholeText) {
        return getTextByIndex(offset, wholeText, getCurrTextIndex());
    }

    public int getCurrTextIndex() {
        int currItemIndex = getCurrIndex();
        if ((currItemIndex < 0) || (currItemIndex >= lines.size())) {
            return -1;
        }
        return getLine(currItemIndex).bigTextIndex;
    }

    public void setColors(int capBk, int text, int bkgrnd, int cursor, int mback1, int mcursor, int dcursor, int mdcursor,
                          int capTxt, int barTxt, int menuTxt, int mback2) {
        Enumeration allLines = lines.elements();
        while (allLines.hasMoreElements())
            ((TextLine) allLines.nextElement()).setItemColor(text);
        allLines = null;
        super.setColors(capBk, text, bkgrnd, cursor, mback1, mcursor, dcursor, mdcursor, capTxt, barTxt, menuTxt, mback2);
    }

    public TextList doCRLF(int blockTextIndex) {
        if (lines.size() != 0) {
            ((TextLine) lines.lastElement()).last_charaster = '\n';
        }
        TextLine newLine = new TextLine();
        newLine.bigTextIndex = blockTextIndex;
        lines.addElement(newLine);
        return this;
    }

    public void switchHeaderIcon(long id, Icon newImage, boolean removeRecord) {
        //System.out.println("Message delivered - remove == " + removeRecord);

        Long msgIdObject = new Long(id);

        if (getTable().containsKey(msgIdObject)) {
            int index = ((Integer) getTable().get(msgIdObject)).intValue();
            setIcon(newImage, index);

            if (removeRecord) {
                getTable().remove(msgIdObject);
            }
        }
    }

    private void setIcon(Icon newIcon, int index) {
        if ((index >= 0) && (index < lines.size())) {
            TextLine textLine = (TextLine) lines.elementAt(index);
            if (textLine.size() > 0) {
                TextItem item = (TextItem) textLine.items.elementAt(0);
                if (item.image != null) {
                    item.image = newIcon;
                    invalidate();
                }
            }
        }
    }

    public void addHeaderIcon(Icon image, int blockTextIndex, long id) {
        if (lines.isEmpty()) lines.addElement(new TextLine());
        TextLine textLine = (TextLine) lines.lastElement();
        textLine.bigTextIndex = blockTextIndex;

        if (textLine.size() > 0) {
            doCRLF(blockTextIndex);
            textLine = (TextLine) lines.lastElement();
        }

        TextItem newItem = new TextItem();
        newItem.image = image;
        newItem.text = "";
        textLine.add(newItem);

        // add msgTable record
        if (id != 0) {
            int lineIndex = lines.size() - 1;
            getTable().put(new Long(id), new Integer(lineIndex));
        }
    }

    private Hashtable getTable() {
        if (table == null) {
            table = new Hashtable();
        }
        return table;
    }

    public TextList addImage(Icon image, String altarnateText, int blockTextIndex) {
        if (lines.isEmpty()) lines.addElement(new TextLine());
        TextLine textLine = (TextLine) lines.lastElement();
        textLine.bigTextIndex = blockTextIndex;

        if ((textLine.getWidth(getFontSize()) + image.getWidth()) > getTextAreaWidth()) {
            doCRLF(blockTextIndex);
            textLine = (TextLine) lines.lastElement();
        }

        TextItem newItem = new TextItem();
        newItem.image = image;
        newItem.text = altarnateText;
        textLine.add(newItem);

        return this;
    }

    public TextList addImage(Icon image, String altarnateText, int imageWidth, int imageHeight, int blockTextIndex) {
        if (lines.isEmpty()) lines.addElement(new TextLine());
        TextLine textLine = (TextLine) lines.lastElement();
        textLine.bigTextIndex = blockTextIndex;

        if ((textLine.getWidth(getFontSize()) + imageWidth) > getTextAreaWidth()) {
            doCRLF(blockTextIndex);
            textLine = (TextLine) lines.lastElement();
        }

        TextItem newItem = new TextItem();
        newItem.image = image;
        newItem.text = altarnateText;
        textLine.add(newItem);
        return this;
    }

    private int getTextAreaWidth() {
        return getDrawWidth() - BORDER * 2;
    }

    public void addBigTextInternal(String text, int color, int fontStyle, int textIndex, int trueWidth) {
        /* aspro's variant as test */
        if (text.indexOf('\r') >= 0) {
            text = StringConvertor.replaceStr(text, "\r\n", "\n").replace('\r', '\n');
        }

        FontFacade font = getQuickFont(fontStyle);

        // Width of free space in last line
        int width = trueWidth;
        if (!lines.isEmpty()) {
            width -= ((TextLine) lines.lastElement()).getWidth(getFontSize());
        }
        int lineStart = 0;
        int wordStart = 0;
        int wordWidth = 0;
        int textLen = text.length();
        String substr;
        for (int i = 0; i < textLen; i++) {
            char ch = text.charAt(i);
            if ('\n' == ch) {
                substr = text.substring(lineStart, i);
                internAdd(substr, color, fontStyle, textIndex, true, '\n');
                lineStart = i + 1;
                width = trueWidth;
                wordStart = lineStart;
                wordWidth = 0;
                continue;
            }

            int w;
            w = font.charWidth(ch);
            wordWidth += w;
            width -= w;
            if (' ' == ch) {
                wordStart = i + 1;
                wordWidth = 0;
                continue;
            }
            if (width < 0) {
                if (wordStart > lineStart) {
                    substr = text.substring(lineStart, wordStart);
                    internAdd(substr, color, fontStyle, textIndex, true, '\0');
                    lineStart = wordStart;
                    width = trueWidth - wordWidth;
                    wordWidth = 0;
                } else {
                    substr = text.substring(lineStart, i);
                    internAdd(substr, color, fontStyle, textIndex, true, '\0');
                    lineStart = i;
                    width = trueWidth - w;
                    wordStart = i;
                    wordWidth = 0;
                }
                //continue;
            }
        }
        substr = text.substring(lineStart);
        if (substr.length() > 0) {
            internAdd(substr, color, fontStyle, textIndex, false, ' ');
        }
    }

    //! Add big multiline text.
    /*! Text visial width can be larger then screen width.
         Method addBigText automatically divides text to short lines
         and adds lines to text list */
    public TextList addBigText(String text, int color, int fontStyle, int textIndex) {
        if (newStyle) {
            newStyle = (getQuickFont(fontStyle).stringWidth(text) < getTextAreaWidth());
        }
        addBigTextInternal(text, color, fontStyle, textIndex, getTextAreaWidth());
        invalidate();
        return this;
    }

    static public int getLineNumbers(String s, int width, int fontSize, int fontStyle, int textColor) {
        TextList paintList = new TextList(null);
        paintList.setFontSize(fontSize);
        paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);

        return (paintList.getSize());
    }

    static public void showText(Graphics g, String s, int x, int y, int width, int height, int fontSize, int fontStyle, int textColor) {
        TextList paintList = new TextList(null);
        paintList.setFontSize(fontSize);
        paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);

        int line, textHeight = 0;
        int linesCount = paintList.getSize();
        for (line = 0; line < linesCount; line++) {
            textHeight += paintList.getLine(line).getHeight(fontSize);
        }
        int top = y + (height - textHeight) / 2;
        for (line = 0; line < linesCount; line++) {
            paintList.getLine(line).paint(x, top, g, fontSize, paintList);
            top += paintList.getLine(line).getHeight(fontSize);
        }
    }
}