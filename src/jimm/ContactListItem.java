/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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
 File: src/jimm/ContactListItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Artyomov Denis
 *******************************************************************************/

package jimm;

public interface ContactListItem {
    // Checks whether some other object is equal to this one
    public abstract boolean equals(Object obj);

    public String getText();

    public int getTextColor();

    public int getFontStyle();

    public int getStatusImageIndex();

    public DrawControls.Icon getAuthImage();

    // Returns text used for sorting in contact list
    public String getSortText();

    public int getSortWeight();
}