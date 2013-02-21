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
 File: src/jimm/GroupItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Artyomov Denis, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import jimm.comm.Icq;
import jimm.comm.Util;

import javax.microedition.lcdui.Font;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import DrawControls.CanvasEx;

public class GroupItem implements ContactListItem {

    private int id;
    private int onlineCount, totalCount, messCout;
    private String name;

    public GroupItem(int id, String name) {
        this.id = id;
        this.name = name;
        onlineCount = totalCount = messCout = 0;
    }

    public GroupItem() {
    }

    public GroupItem(String name, Icq icq) {
        this(Util.createRandomId(icq), name);
    }

    public void setCounters(int online, int total) {
        onlineCount = online;
        totalCount = total;
    }

    public void updateCounters(int onlineInc, int totalInc) {
        onlineCount += onlineInc;
        totalCount += totalInc;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public synchronized void updateMessCount(int i0) {
        messCout += i0;
        messCout = Math.max(messCout, 0);
    }

    public DrawControls.Icon getAuthImage() {
        return (messCout > 0) ? ContactList.imageList.elementAt(14) : null;
    }

    public String getText() {
        String result;

        if ((onlineCount != 0) && Options.getBoolean(Options.OPTION_SHOW_OFFLINE)) {
            result = name + " (" + onlineCount + "/" + totalCount + ")";
        } else {
            result = name;
        }
        return result;
    }

    public int getTextColor() {
        return (totalCount + onlineCount == 0) ? CanvasEx.getColor(CanvasEx.COLOR_TEMP) : CanvasEx.getColor(CanvasEx.COLOR_TEXT);
    }

    public int getFontStyle() {
        return (onlineCount == 0) ? Font.STYLE_PLAIN : Font.STYLE_BOLD;
    }

    public int getStatusImageIndex() {
        return -1;
    }

    public int getId() {
        return (this.id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GroupItem)) return (false);
        GroupItem gi = (GroupItem) obj;
        return (this.id == gi.getId());
    }

    public void saveToStream(DataOutputStream stream) throws IOException {
        stream.writeByte(1);
        stream.writeInt(id);
        stream.writeUTF(name);
    }

    public void loadFromStream(DataInputStream stream) throws IOException {
        id = stream.readInt();
        name = stream.readUTF();
    }

    public String getSortText() {
        return name;
    }

    public int getSortWeight() {
        return 0;
    }
}