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

import javax.microedition.lcdui.Graphics;
import java.util.Vector;

public class TextLine {
    public Vector items = new Vector();
    int height = -1;
    int bigTextIndex = -1;
    char last_charaster;

    TextItem elementAt(int index) {
        return (TextItem) items.elementAt(index);
    }

    void add(TextItem item) {
        items.addElement(item);
    }

    public int getHeight(int fontSize) {
        if (height == -1) {
            height = fontSize;
            for (int i = items.size() - 1; i >= 0; i--) {
                int currHeight = elementAt(i).getHeight(fontSize);
                if (currHeight > height) {
                    height = currHeight;
                }
            }
        }
        return height;
    }

    public int getWidth(int fontSize) {
        int width = 0;
        for (int i = items.size() - 1; i >= 0; i--) {
            width += elementAt(i).getWidth(fontSize);
        }
        return width;
    }

    public void setItemColor(int value) {
        TextItem listItem;
        for (int i = items.size() - 1; i >= 0; i--) {
            listItem = elementAt(i);
            listItem.setColor(value);
        }
    }

    public void paint(int xpos, int ypos, Graphics g, int fontSize, VirtualList vl) {
        int count = items.size();
        int intemHeight = getHeight(fontSize);

        TextItem item;
        for (int i = 0; i < count; i++) {
            item = elementAt(i);
            int drawYPos = ypos + (intemHeight - item.getHeight(fontSize)) / 2;
            if (item.image != null) {
                //item.image.drawByLeftTop(g, xpos + 1, drawYPos);
                item.image.drawInVCenter(g, xpos + 1, drawYPos + item.image.getHeight() / 2);
                xpos++;
            } else if (item.text != null) {
                g.setColor(item.getColor());
                CanvasEx.drawString(g, vl.getQuickFont(item.getFontStyle()), item.text, xpos, drawYPos, Graphics.TOP | Graphics.LEFT);
            }
            xpos += item.getWidth(fontSize);
        }
    }

    public int size() {
        return items.size();
    }

    public void readText(StringBuffer buffer) {
        String text;
        for (int i = 0; i < items.size(); i++) {
            text = elementAt(i).text;
            if (text != null) {
                buffer.append(text);
            }
        }
    }
}