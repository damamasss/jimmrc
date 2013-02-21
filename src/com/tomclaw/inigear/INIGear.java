///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.tomclaw.inigear;
//
//import jimm.comm.Util;
//
//import java.util.Vector;
//
//import com.tomclaw.xmlgear.XMLGear;
//
///**
// * Solkin Igor Viktorovich, TomClaw Software, 2003-2010
// * http://www.tomclaw.com/
// *
// * @author Игорь
// */
//public class INIGear {
//
//    public Vector iniItems = new Vector();
//    public boolean isFirstIndex = false;
//
//    public INIGear() {
//    }
//
//    public INIGear(boolean isFirstIndex) {
//        this.isFirstIndex = isFirstIndex;
//    }
//
//    public void addHeader(String headerName) {
//        if (this.isExist(headerName, null)) {
//            return;
//        }
//        INIItem iniItem = new INIItem(headerName);
//        iniItems.addElement(iniItem);
//    }
//
//    public void addItem(String itemName, String itemValue, String headerName) {
//        if (this.isExist(headerName, itemName)) {
//            this.setItemValue(itemName, itemValue, headerName);
//            return;
//        }
//        if (!this.isExist(headerName, null)) {
//            this.addHeader(headerName);
//        }
//        INIItem iniItem = null;
//        boolean isFound = false;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.itemName == null && iniItem.itemValue == null) {
//                if (iniItem.headerName.hashCode() == headerName.hashCode()) {
//                    isFound = true;
//                } else {
//                    if (isFound) {
//                        iniItems.insertElementAt(new INIItem(headerName, itemName, itemValue), c);
//                        break;
//                    }
//                }
//            }
//            if (c == (iniItems.size() - 1) && isFound) {
//                iniItems.addElement(new INIItem(headerName, itemName, itemValue));
//                break;
//            }
//        }
//    }
//
//    public void renameHeader(String headerOldName, String headerNewName) {
//        if (!this.isExist(headerOldName, null)) {
//            this.addHeader(headerNewName);
//            return;
//        }
//        INIItem iniItem = null;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.headerName.hashCode() == headerOldName.hashCode()
//                    && iniItem.itemName == null && iniItem.itemValue == null) {
//                iniItem.headerName = headerNewName;
//                iniItems.setElementAt(iniItem, c);
//            }
//        }
//    }
//
//    public void renameItem(String itemOldName, String itemNewName, String headerName) {
//        if (!this.isExist(headerName, itemOldName)) {
//            if (!this.isExist(headerName, null)) {
//                this.addHeader(headerName);
//            }
//            this.addItem(itemNewName, "", headerName);
//            return;
//        }
//        INIItem iniItem = null;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.headerName.hashCode() == headerName.hashCode() && iniItem.itemName != null) {
//                if (iniItem.itemName.hashCode() == itemOldName.hashCode()) {
//                    iniItem.itemName = itemNewName;
//                    iniItems.setElementAt(iniItem, c);
//                }
//            }
//        }
//    }
//
//    public void setItemValue(String itemName, String itemValue, String headerName) {
//        if (!this.isExist(headerName, itemName)) {
//            if (!this.isExist(headerName, null)) {
//                this.addHeader(headerName);
//            }
//            this.addItem(itemName, itemValue, headerName);
//            return;
//        }
//        INIItem iniItem = null;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.headerName.hashCode() == headerName.hashCode()
//                    && iniItem.itemName != null) {
//                if (iniItem.itemName.hashCode() == itemName.hashCode()) {
//                    iniItem.itemValue = itemValue;
//                    iniItems.setElementAt(iniItem, c);
//                }
//            }
//        }
//    }
//
//    public void deleteHeader(String headerName, boolean isWithItems) {
//        INIItem iniItem = null;
//        boolean isFound = false;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.itemName == null && iniItem.itemValue == null) {
//                if (iniItem.headerName.hashCode() == headerName.hashCode()) {
//                    isFound = true;
//                    iniItems.removeElementAt(c);
//                    c--;
//                    if (!isWithItems) {
//                        break;
//                    }
//                } else {
//                    if (isFound) {
//                        break;
//                    }
//                }
//            } else {
//                if (isFound) {
//                    iniItems.removeElementAt(c);
//                    c--;
//                }
//            }
//        }
//    }
//
//    public void deleteItem(String itemName, String headerName) {
//        INIItem iniItem = null;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.headerName.hashCode() == headerName.hashCode()
//                    && iniItem.itemName != null) {
//                if (iniItem.itemName.hashCode() == itemName.hashCode()) {
//                    iniItems.removeElementAt(c);
//                }
//            }
//        }
//    }
//
//    public boolean isExist(String headerName, String itemName) {
//        boolean isExist = false;
//        INIItem iniItem = null;
//        if (itemName != null) {
//            if (itemName.length() == 0) {
//                itemName = null;
//            }
//        }
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.headerName.hashCode() == headerName.hashCode()) {
//                if (iniItem.itemName == null) {
//                    if (itemName == null) {
//                        isExist = true;
//                        break;
//                    }
//                } else {
//                    if (itemName != null) {
//                        if (iniItem.itemName.hashCode() == itemName.hashCode()) {
//                            isExist = true;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        return isExist;
//    }
//
//    public String[] getHeaders() {
//        Vector headers = new Vector();
//        INIItem iniItem = null;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.itemName == null) {
//                headers.addElement(iniItem);
//            }
//        }
//        String headersEnum[] = new String[headers.size()];
//        for (int c = 0; c < headers.size(); c++) {
//            headersEnum[c] = ((INIItem) headers.elementAt(c)).headerName;
//        }
//        return headersEnum;
//    }
//
//    public String[] getItems(String headerName) {
//        return getItems(headerName, true);
//    }
//
//    public String[] getItems(String headerName, boolean isFullCompare) {
//        Vector items = new Vector();
//        INIItem iniItem = null;
//        boolean isFound = false;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.itemName == null && iniItem.itemValue == null) {
//                // Logger.outMessage(headerName+" {"+iniItem.headerName+"} "+headerName.startsWith(iniItem.headerName));
//                if ((isFullCompare && iniItem.headerName.equals(headerName)) || (!isFullCompare && headerName.startsWith(iniItem.headerName))) {
//                    // Logger.outMessage(headerName+" {"+iniItem.headerName+"} "+headerName.startsWith(iniItem.headerName));
//                    isFound = true;
//                } else {
//                    if (isFound) {
//                        break;
//                    }
//                }
//            } else {
//                if (isFound) {
//                    items.addElement(iniItem);
//                }
//            }
//        }
//        String itemsEnum[] = new String[items.size()];
//        for (int c = 0; c < items.size(); c++) {
//            itemsEnum[c] = ((INIItem) items.elementAt(c)).itemName;
//        }
//        return itemsEnum;
//    }
//
//    public String getValue(String itemName, String headerName) {
//        return getValue(itemName, headerName, true);
//    }
//
//    public String getValue(String itemName, String headerName, boolean isFullCompare) {
//        String itemValue = null;
//        INIItem iniItem = null;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (((isFullCompare && iniItem.headerName.equals(headerName)) || (!isFullCompare && headerName.startsWith(iniItem.headerName)))
//                    && iniItem.itemName != null) {
//                if (iniItem.itemName.hashCode() == itemName.hashCode()) {
//                    itemValue = iniItem.itemValue;
//                    break;
//                }
//            }
//        }
//        return itemValue;
//    }
//
//    public byte[] getStructure() {
//        String iniStructure = new String();
//        INIItem iniItem;
//        for (int c = 0; c < iniItems.size(); c++) {
//            iniItem = (INIItem) iniItems.elementAt(c);
//            if (iniItem.itemName == null) {
//                iniStructure += "[" + iniItem.headerName + "]\n";
//            } else {
//                iniStructure += iniItem.itemName + "="
//                        + iniItem.itemValue + "\n";
//            }
//        }
//        return Util.stringToByteArray(iniStructure, true);
//    }
//
//
//    public int index0(char[] data, char[] data0, int off) {
//        int len = data0.length;
//        if (data.length < off + len) {
//            return -1;
//        }
//        for (int i = off; i < data.length - len; i++) {
//            if (equals0(data, data0, i, len)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    public int lastIndex0(char[] data, char[] data0, int off) {
//        int len = data0.length;
//        if (data.length < off + len) {
//            return -1;
//        }
//        for (int i = data.length - len - 1; i >= off; i--) {
//            if (equals0(data, data0, i, len)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    public boolean equals0(char[] data, char[] data0, int off, int len) {
//        if (data.length < off + len) {
//            return false;
//        }
//        for (int i = 0; i < len; i++) {
//            if (data[i + off] != data0[i]) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public char[] sub0(char[] data, int start, int fin) {
//        if (fin <= start) {
//            return new char[0];
//        }
//        char[] temp = new char[fin - start];
//        System.arraycopy(data, start, temp, 0, fin - start);
//        return temp;
//    }
//
//    public char[] plus0(char[] data, char c) {
//        char[] temp = new char[data.length + 1];
//        System.arraycopy(data, 0, temp, 0, data.length);
//        temp[data.length] = c;
//        return temp;
//    }
//
//    public void setStructure(String structure) {
//        setStructure(structure, true);
//    }
//
//    public void setStructure(String structure, boolean n) {
//        char[] text = structure.toCharArray();
//        char crlf = (n) ? 10 : ';';
//        text[text.length - 1] = crlf;
//        iniItems = new Vector();
//        INIItem iniItem = null;
//        char ch;
//        char[] buffer = new char[0];
//        String prevHeader = "";
//        for (int c = 0; c < text.length; c++) {
//            ch = text[c];
//            if (ch == 13) {
//                continue;
//            }
//            if (!n && ch == 10 && buffer.length == 0) {
//                continue;
//            }
//            if (ch == crlf) {
//                if (buffer.length <= 1) {
//                    continue;
//                }
//                if (buffer.length > 0 && buffer[0] == '[' && buffer[buffer.length - 1] == ']') {
//                    iniItem = new INIItem(new String(sub0(buffer, 1, buffer.length - 1)));
//                    prevHeader = iniItem.headerName;
//                } else {
//                    int equivIndex;
//                    if (isFirstIndex) {
//                        equivIndex = index0(buffer, new char[]{'='}, 0);
//                    } else {
//                        equivIndex = lastIndex0(buffer, new char[]{'='}, 0);
//                    }
//                    if (equivIndex > 0) {
//                        iniItem = new INIItem(prevHeader, new String(sub0(buffer, 0, equivIndex)), new String(sub0(buffer, equivIndex + 1, buffer.length)));
//                    }
//                }
//                iniItems.addElement(iniItem);
//                buffer = new char[0];
//                continue;
//            }
//            buffer = plus0(buffer, ch);
//        }
//    }
//}
