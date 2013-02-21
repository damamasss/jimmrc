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
 File: src/jimm/HistoryStorage.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin
 *******************************************************************************/

//#sijapp cond.if modules_HISTORY is "true" #

package jimm;

import jimm.comm.DateAndTime;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.rms.RecordStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Hashtable;

// History storage implementation
public class HistoryStorage {
    //===================================//
    //                                   //
    //    Data storage implementation    //
    //                                   //
    //===================================//

    static final public int CLEAR_EACH_DAY = 0;
    static final public int CLEAR_EACH_WEEK = 1;
    static final public int CLEAR_EACH_MONTH = 2;

    static final private String prefix = "hist";

    private static RecordStore recordStore;
    private static HistoryStorageList list;
    private static String currCacheUin = "";
    private static Hashtable cachedRecords;

    public HistoryStorage() {
        try {
            RecordStore.deleteRecordStore("history");
        } catch (Exception ignored) {
        }
    }

    static public synchronized void addText(ContactItem cItem, String text, byte type, String from, long time) {
/*
		Types of message:
		0 - incoming
		1 - outgouing
*/
        String uin = cItem.getUinString();
        boolean lastLine = false;
        RecordStore rs;
        if (list != null) {
            lastLine = (list.getSize() == 0) || (list.getCurrIndex() == (list.getSize() - 1));
        }
        boolean isCurrenty = currCacheUin.equals(uin);
        try {
            rs = isCurrenty ? recordStore : RecordStore.openRecordStore(getRSName(cItem), true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream das = new DataOutputStream(baos);
            das.writeByte(type);
            das.writeUTF(from);
            if (text.indexOf("<distxtr>") == 0) {
                das.writeUTF(text.substring(9));
                das.writeUTF("[Status/XTraZ]");
            } else {
                das.writeUTF(text);
                das.writeUTF(DateAndTime.getDateString(false, true, time));
            }
            byte[] buffer = baos.toByteArray();
            rs.addRecord(buffer, 0, buffer.length);
            try {
                das.close();
                baos.close();
            } catch (Exception ignored) {
            }

            if (!isCurrenty && (rs != null)) {
                rs.closeRecordStore();
                rs = null;
            }
        } catch (Exception ignored) {
        }
        if ((list != null) && list.getCurrUin().equals(uin)) {
            list.repaint();
            if (lastLine) list.setCurrentItem(list.getSize() - 1);
        }
    }

    static RecordStore getRS() {
        return recordStore;
    }

    static private String getRSName(ContactItem cItem) {
        return prefix + cItem.getProfile().getNick() + '_' + cItem.getUinString();
    }

    static private void openUINRecords(ContactItem cItem) {
        String uin = cItem.getUinString();
        if (currCacheUin.equals(uin)) {
            return;
        }
        try {
            if (recordStore != null) {
                recordStore.closeRecordStore();
                recordStore = null;
                System.gc();
            }
            recordStore = RecordStore.openRecordStore(getRSName(cItem), true);
        } catch (Exception e) {
            recordStore = null;
            return;
        }
        currCacheUin = uin;
        if (cachedRecords == null) {
            cachedRecords = new Hashtable();
        }
    }

    static public int getRecordCount(ContactItem cItem) {
        openUINRecords(cItem);
        int result;
        try {
            result = recordStore.getNumRecords();
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }

    static synchronized public CachedRecord getRecord(ContactItem cItem, int recNo) {
        openUINRecords(cItem);
        byte[] data;
        CachedRecord result = new CachedRecord();
        try {
            data = recordStore.getRecord(recNo + 1);
            result.type = data[0];
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            result.type = dis.readByte();
            result.from = dis.readUTF();
            result.text = dis.readUTF();
            result.date = dis.readUTF();
//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
//            result.contains_url = (Util.parseMessageForURL(result.text) != null);
//#sijapp cond.end#
        } catch (Exception e) {
            result.text = result.date = result.from = "error";
//#sijapp cond.if target is "SIEMENS2" | target is "MIDP2"#
//            result.contains_url = false;
//#sijapp cond.end#
            return null;
        }
        return result;
    }

//    static synchronized public void deleteCashedRecord(ContactItem cItem, int recNo) {
//        openUINRecords(cItem);
//        try {
//            recordStore.deleteRecord(recNo + 1);
//        } catch (Exception e) {
//        }
//    }

    static public CachedRecord getCachedRecord(ContactItem cItem, int recNo) {
        int maxLen = 20;
        if (cachedRecords == null) {
            openUINRecords(cItem);
        }
        CachedRecord cachedRec = (CachedRecord) cachedRecords.get(new Integer(recNo));
        if (cachedRec != null) {
            return cachedRec;
        }
        cachedRec = getRecord(cItem, recNo);
        if (cachedRec == null) {
            return null;
        }
        if (cachedRec.text.length() > maxLen) {
            cachedRec.shortText = cachedRec.text.substring(0, maxLen) + "...";
        } else {
            cachedRec.shortText = cachedRec.text;
        }
        cachedRec.shortText = cachedRec.shortText.replace('\n', ' ');
        cachedRec.shortText = cachedRec.shortText.replace('\r', ' ');
        cachedRecords.put(new Integer(recNo), cachedRec);
        return cachedRec;
    }

    static public void showHistoryList(ContactItem cItem) {
        boolean wasNull = false;
        if (list == null) {
            String caption = ResourceBundle.getString("history_lng");
            list = new HistoryStorageList();
            list.setCaption(caption);
            wasNull = true;
        }

        list.setCItem(cItem);

        list.lock();

        //if (list.getSize() != 0 && wasNull) list.setCurrentItem(list.getSize() - 1);
        
        list.unlock();
        //list.activate(list);
        Jimm.setDisplay(list);
        //list.readHistory(); //todo
        list.setCommandListener();
    }

    static public boolean ready4Ani() {
        return list != null && list.ready4Ani();
    }

    static synchronized public void clearHistory(ContactItem cItem) {
        try {
            openUINRecords(cItem);
            recordStore.closeRecordStore();
            recordStore = null;
            System.gc();
            RecordStore.deleteRecordStore(getRSName(cItem));
            if (cachedRecords != null) {
                cachedRecords.clear();
            }
            currCacheUin = "";
        } catch (Exception ignored) {
        }
    }

    static public void clearCache() {
        if (cachedRecords != null) {
            cachedRecords.clear();
            cachedRecords = null;
        }
        list = null;
        currCacheUin = "";
    }

    // Sets color scheme for history UI controls
    static public void setColorScheme() {
        if (list != null) {
            list.setColorScheme();
            if (list.messText != null) {
                list.messText.setColorScheme();
            }
        }
    }

    static synchronized private boolean find_intern(ContactItem cItem, String text, boolean case_sens, boolean back) {
        int index = list.getCurrIndex();
        if ((index < 0) || (index >= list.getSize())) {
            return false;
        }
        if (!case_sens) {
            text = text.toLowerCase();
        }
        int size = getRecordCount(cItem);

        CachedRecord record;
        String search_text;
        for (; ;) {
            if ((index < 0) || (index >= size)) {
                break;
            }
            record = getRecord(cItem, index);
            search_text = case_sens ? record.text : record.text.toLowerCase();
            if (search_text.indexOf(text) != -1) {
                list.setCurrentItem(index);
                list.activate();
                return true;
            }

            if (back) index--;
            else index++;
        }
        return false;
    }

    static void find(ContactItem cItem, String text, boolean case_sens, boolean back) {
        if (list == null) {
            return;
        }
        boolean result = find_intern(cItem, text, case_sens, back);
        if (result) {
            return;
        }
        Alert alert = new Alert
                (
                        ResourceBundle.getString("find"),
                        (new StringBuffer())
                                .append(text)
                                .append("\n")
                                .append(ResourceBundle.getString("not_found"))
                                .toString(),
                        null,
                        AlertType.INFO
                );
        alert.setTimeout(Alert.FOREVER);
        list.activate(alert);
    }

    // Clears all records for all uins
    static synchronized void clear_all(ContactItem except) {
        String exceptRMS = (except == null) ? null : getRSName(except);

        try {
            if (recordStore != null) {
                recordStore.closeRecordStore();
                recordStore = null;
                System.gc();
                currCacheUin = "";
            }
            String[] stores = RecordStore.listRecordStores();
            String store;
            for (int i = 0; i < stores.length; i++) {
                store = stores[i];
                if (store.indexOf(prefix) == -1) continue;
                if (exceptRMS != null) if (exceptRMS.equals(store)) continue;
                RecordStore.deleteRecordStore(store);
            }
        } catch (Exception ignored) {
        }
    }
}
//#sijapp cond.end#