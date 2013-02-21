/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2005-06  Jimm Project

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
 File: src/jimm/RunnableImpl.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Rishat Shamsutdinov
 *******************************************************************************/

package jimm;

import DrawControls.NativeCanvas;
import jimm.ui.Menu;
import jimm.ui.PopUp;


public class RunnableImpl implements Runnable {

    final static public byte TYPE_REPAINT_TREE = 0;

    private byte type;

    RunnableImpl(byte type) {
        this.type = type;
    }

    public void run() {
        switch (type) {
            case TYPE_REPAINT_TREE:
                if (clIsVisible(Jimm.getCurrentDisplay())) {
                    NativeCanvas.Repaint();
                }
                break;
        }
    }

    private boolean clIsVisible(Object o) {
        if (o instanceof Menu) {
            return clIsVisible(((Menu) o).getPrvScreen());
        }
        if (o instanceof PopUp) {
            return clIsVisible(((PopUp) o).getPrvScreen());
        }
        return (o instanceof ContactList);
    }
}