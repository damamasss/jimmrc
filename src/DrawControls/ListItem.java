/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/DrawControls/ListItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package DrawControls;

import javax.microedition.lcdui.Font;

/*! Data for list item */
/*! All members of class are made as public in order to easy access */

public class ListItem {
    public String text;
    public Icon image;
    public Icon XStatusImg;
    public Icon ClientImg;
    public Icon PrivateImg;
    public Icon HappyImg;
    public Icon AuthImg;
    public int fontStyle;
    public int color;
    public int horizOffset;
    //public int imageIndex;
    public boolean isMessage;
    public boolean needBottom;
    public int unreadMessCount;

    ListItem() {
        fontStyle = Font.STYLE_PLAIN;
    }

    //! Set all member to default values
    public void clear() {
        image = null;
        XStatusImg = null;
        ClientImg = null;
        PrivateImg = null;
        HappyImg = null;
        AuthImg = null;
        text = null;
        color = 0;
        horizOffset = 0;
        //imageIndex = -1;
        fontStyle = Font.STYLE_PLAIN;
        isMessage = false;
        needBottom = false;
        unreadMessCount = 0;
    }
}