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
 File: src/jimm/comm/Action.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public abstract class Action {
    final static public int ON_COMPLETE = 1;
    final static public int ON_CANCEL = 2;
    final static public int ON_ERROR = 3;

    // ICQ object
    protected Icq icq;

    private boolean exclusive, executableConnected;

    protected Action(boolean exclusive, boolean executableConnected) {
        this.exclusive = exclusive;
        this.executableConnected = executableConnected;
    }

    // Set ICQ object
    protected void setIcq(Icq icq) {
        this.icq = icq;
    }

    public Icq getIcq() {
        return icq;
    }

    // Returns true if the action can be performed
    final public boolean isExecutable() {
        if (executableConnected) return icq.isConnected();
        return icq.isNotConnected();
    }

    // Returns true if this is an exclusive command
    final public boolean isExclusive() {
        return exclusive;
    }

    // Init action
    protected abstract void init() throws JimmException;

    // Forwards received packet, returns true if packet was consumed
    protected abstract boolean forward(Packet packet) throws JimmException;

    // Returns true if the action is completed
    public abstract boolean isCompleted();

    // Returns ture if an error has occured
    public abstract boolean isError();

    // Returns a number between 0 and 100 (inclusive) which indicates the progress
    public int getProgress() {
        if (this.isCompleted()) return (100);
        else return (0);
    }

    public String getProgressMsg() {
        return null;
    }

    public void onEvent(int eventType) {
        if (eventType == ON_CANCEL) {
            //NativeCanvas.hideLPCanvas();
            jimm.Jimm.back();
        }
    }
}
