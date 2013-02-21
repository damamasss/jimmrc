package com.tomclaw.xmlgear;

import jimm.comm.Util;
import jimm.Options;

import java.util.Stack;
import java.util.Vector;

/**
 * XMLGear class
 * This file is distributed under zlib/libpng license
 *
 * @author Solkin I.V., TomClaw Software, 2003-2010
 */
public class XMLGear {

    public Vector items = null;
    Stack parents = null;
    private char[] fillChars = new char[]{13, 10, 32, 9};
    public char[] spaceString = new char[]{' ', ' ', ' ', ' '};
    private char[] headerBlockChars = new char[]{13, 10, 9};
    /**
     * Replace existing tags with such header
     */
    public boolean isExcludeExisting = false;

    /**
     * Constructor
     */
    public XMLGear() {
        items = new Vector();
        XMLItem xmlItem = new XMLItem(null, "xml", "", new ItemParam[]{new ItemParam("version", "1.0"), new ItemParam("encoding", "UTF-8")}, 0);
        xmlItem.itemType = 1; // Header
        items.addElement(xmlItem);
    }

    /**
     * Adding path to located absolute path
     *
     * @param absolutePath
     * @param itemHeader
     * @param itemContent
     * @param itemParams
     * @param incrRate
     * @return true if parent item found and false in opposite case
     */
    public XMLItem addItem(String absolutePath, String itemHeader,
                           String itemContent, ItemParam[] itemParams, int incrRate) {
        XMLItem parentItem = getItem(absolutePath);
        if (parentItem != null) {
            return addItem(parentItem, itemHeader,
                    itemContent, itemParams, incrRate);
        }
        return null;
    }

    /**
     * Adding item to provided parent item
     *
     * @param parentItem
     * @param itemHeader
     * @param itemContent
     * @param itemParams
     * @param incrRate
     */
    public XMLItem addItem(XMLItem parentItem, String itemHeader,
                           String itemContent, ItemParam[] itemParams, int incrRate) {
        XMLItem xmlItem = new XMLItem(parentItem, itemHeader,
                itemContent, itemParams, incrRate);
        if (isExcludeExisting) {
            /*try {
            // System.out.printlnxmlItem.getAbsolutePath() + "=="
            + getItem(xmlItem.getAbsolutePath()).itemHeader);
            } catch (Throwable ex1) {
            }*/
            if (getItem(xmlItem.getAbsolutePath()) != null) {
                // System.out.println"Item removed");
                removeItem(xmlItem.getAbsolutePath(), false);
            }
        }
        items.insertElementAt(xmlItem, items.indexOf(parentItem) + 1);
        return xmlItem;
    }

    /**
     * Removing provided item from structure
     *
     * @param xmlItem
     */
    public void removeItem(XMLItem xmlItem, boolean isWithChilds) {
        if (isWithChilds) {
            XMLItem[] xmlItems = getItems(xmlItem);
            for (int c = 0; c < xmlItems.length; c++) {
                removeItem(xmlItems[c], true);
            }
        } else {
        }
        items.removeElement(xmlItem);
    }

    /**
     * Removing item for absolute path
     *
     * @param absolutePath
     */
    public void removeItem(String absolutePath, boolean isWithChilds) {
        removeItem(getItem(absolutePath), isWithChilds);
    }

    /**
     * Removing item for provided item parentship
     *
     * @param itemParentship
     */
    public void removeItem(String[] itemParentship, boolean isWithChilds) {
        removeItem(getItem(itemParentship), isWithChilds);
    }

    /**
     * Requesting item parentship by providing absolute path
     *
     * @param absolutePath
     * @return
     */
    public String[] getItemParentship(String absolutePath) {
        if (!absolutePath.endsWith("/")) {
            /*
             * Adding slash if it's not present
             */
            absolutePath += "/";
        }
        if (absolutePath.startsWith("/")) {
            absolutePath = absolutePath.substring(1);
        }
        int slashIndex;
        Vector itemParentshipVector = new Vector();
        String[] itemParentship;
        while ((slashIndex = absolutePath.indexOf('/')) != -1) {
            /**
             * Cutting absolute path to parts
             */
            itemParentshipVector.addElement(absolutePath.substring(0, slashIndex));
            absolutePath = absolutePath.substring(slashIndex + 1, absolutePath.length());
        }
        itemParentship = new String[itemParentshipVector.size()];
        itemParentshipVector.copyInto(itemParentship);
        // System.out.printlnitemParentshipVector);
        return itemParentship;
    }

    /**
     * Renaming item header to provided headerNew
     *
     * @param absolutePath
     * @param headerNew
     * @return true if renamed successfully or false even XMLItem not found on provided path
     */
    public boolean renameItemHeader(String absolutePath, String headerNew) {
        return renameItemHeader(getItemParentship(absolutePath), headerNew);
    }

    /**
     * Renaming item header to provided headerNew
     *
     * @param headerNew
     * @return true if renamed successfully or false even XMLItem not found on provided parentship
     * @para absolutePath
     */
    public boolean renameItemHeader(String[] itemParentship, String headerNew) {
        try {
            getItem(itemParentship).itemHeader = headerNew;
            return true;
        } catch (NullPointerException ex1) {
            return false;
        }
    }

    /**
     * Looking for item by absolute path
     *
     * @param absolutePath
     * @return XMLItem or null if absolute path is invalid
     */
    public XMLItem getItem(String absolutePath) {
        return getItem(getItemParentship(absolutePath));
    }

    /**
     * Looking for item by providing item parentship
     *
     * @param itemParentship
     * @return XMLItem or null if absolute path is invalid
     */
    public XMLItem getItem(String[] itemParentship) {
        XMLItem xmlItem;
        int incrRate = 0;
        int inBlockCount = 0;
        for (int i = 0; i < items.size(); i++) {
            xmlItem = (XMLItem) items.elementAt(i);
            if (itemParentship.length <= incrRate) {
                break;
            }
            if (xmlItem.itemHeader.equals(itemParentship[incrRate])) {
                incrRate++;
                if (xmlItem.incrRate == itemParentship.length - 1) {
                    return xmlItem;
                }
                inBlockCount = 0;
            }
            inBlockCount++;
        }
        return null;
    }

    /**
     * Requesting a list of XML items in provided absolute path
     *
     * @param itemParentship
     * @return XMLItem[] - list of items in absolute path
     */
    public XMLItem[] getItems(String[] itemParentship) {
        XMLItem xmlItem;
        int incrRate = 0;
        Vector xmlItems = new Vector();
        int inBlockCount = 0;
        for (int i = 0; i < items.size(); i++) {
            xmlItem = (XMLItem) items.elementAt(i);

            if (xmlItem.itemHeader.equals(itemParentship[incrRate])) {
                incrRate++;
                if (xmlItem.incrRate == itemParentship.length - 1) {
                    while (++i < items.size()) {
                        xmlItem = ((XMLItem) items.elementAt(i));
                        if (xmlItem.incrRate < incrRate) {
                            break;
                        } else if (xmlItem.incrRate > incrRate) {
                            continue;
                        }
                        xmlItems.addElement(xmlItem);
                    }
                    XMLItem[] xmlItemsList = new XMLItem[xmlItems.size()];
                    xmlItems.copyInto(xmlItemsList);
                    return xmlItemsList;
                }
                inBlockCount = 0;
            }
            inBlockCount++;
        }
        return null;
    }

    public XMLItem[] getItems(XMLItem xmlParent) {
        XMLItem xmlItem;
        int incrRate = 0;
        Vector xmlItems = new Vector();
        int inBlockCount = 0;
        for (int i = 0; i < items.size(); i++) {
            xmlItem = (XMLItem) items.elementAt(i);

            if (xmlItem.equals(xmlParent)) {
                incrRate = xmlParent.incrRate + 1;
                // System.out.println"xmlParent found");
                while (++i < items.size()) {
                    xmlItem = ((XMLItem) items.elementAt(i));
                    if (xmlItem.incrRate < incrRate) {
                        break;
                    } else if (xmlItem.incrRate > incrRate) {
                        continue;
                    }
                    xmlItems.addElement(xmlItem);
                }
                XMLItem[] xmlItemsList = new XMLItem[xmlItems.size()];
                xmlItems.copyInto(xmlItemsList);
                return xmlItemsList;
            }
            inBlockCount++;
        }
        return null;
    }

    public String getStructureLine() {
        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < items.size(); c++) {
            XMLItem item = (XMLItem) items.elementAt(c);
            sb.append("\n\titemHeader=").append(item.itemHeader);
            if (item.parentItem != null) {
                sb.append("\n\t\tparentItem name=").append(item.parentItem.itemHeader);
            } else {
                sb.append("\n\t\tparentItem=").append(item.parentItem);
            }
            sb.append("\n\t\titemContent=").append(item.itemContent);
            sb.append("\n\t\titemParams=").append(item.itemParams.length);
            sb.append("\n\t\tincrRate=").append(item.incrRate);
            sb.append("\n\t\titemType=").append(item.itemType);
        }
        return sb.toString();
    }

    /**
     * Generating XML content
     *
     * @return XML formatted text
     */
    public String getStructure() {
        /*
         * Создание XML структуры
         */
        String xmlStructure = "";
        XMLItem xmlItem = null;
        XMLItem xmlItemParent = null;
        parents = new Stack();
        for (int c = 0; c < items.size(); c++) {
            xmlItem = (XMLItem) items.elementAt(c);
            // // System.out.printlnxmlItem.itemHeader);
            if (c > 1 && items.elementAt(c - 1) != null && xmlItem.incrRate < ((XMLItem) items.elementAt(c - 1)).incrRate) { // todo optimize
                xmlItemParent = ((XMLItem) items.elementAt(c - 1));
                //int step = xmlItemParent.incrRate - xmlItem.incrRate;
                XMLItem xmlItemParent0 = xmlItemParent;
                while (xmlItemParent0.incrRate > xmlItem.incrRate && !parents.isEmpty()) {
                    if (xmlItemParent0.parentItem != null) {
                        xmlStructure += getSpaces(xmlItemParent0.parentItem.incrRate) + "</" + xmlItemParent0.parentItem.itemHeader + ">" + "\n";
                    } else {
                        xmlStructure += getSpaces(xmlItemParent0.incrRate) + "</" + xmlItemParent0.parentItem.itemHeader + ">" + "\n";
                    }
                    xmlItemParent0 = (XMLItem) parents.lastElement();
                    parents.pop();
                }
            } else {
            }
            xmlStructure += getSpaces(xmlItem.incrRate) + "<";
            if (xmlItem.itemType == 1) {
                xmlStructure += "?";
            } else if (xmlItem.itemType == 2) {
                xmlStructure += "!";
            }
            xmlStructure += xmlItem.itemHeader;
            if (xmlItem.itemParams.length > 0) {
                xmlStructure += " ";
                ItemParam itemParam = null;
                for (int i = 0; i < xmlItem.itemParams.length; i++) {
                    itemParam = xmlItem.itemParams[i];
                    xmlStructure += itemParam.paramTitle + "=\"" + itemParam.paramValue + "\"";
                    if (i < xmlItem.itemParams.length - 1) {
                        xmlStructure += " ";
                    }
                }
            }
            if (checkFill(xmlItem.itemContent)) {
                if (c + 1 < items.size()) {
                }
                if (xmlItem.itemType == 1) {
                    xmlStructure += "?";
                } else if (c + 1 < items.size() && ((XMLItem) items.elementAt(c + 1)).incrRate > xmlItem.incrRate) {
                } else {
                    xmlStructure += "/";
                }
            }
            xmlStructure += ">";
            //} else {
            //    xmlStructure += ">";
            //}

            if (c + 1 < items.size() && ((XMLItem) items.elementAt(c + 1)).incrRate > xmlItem.incrRate) {
                if (xmlItem.itemType != 1 && xmlItem.itemType != 2) {
                    parents.addElement(xmlItem);
                }
            }

            if (!checkFill(xmlItem.itemContent)) { //todo show itemContent
                xmlStructure += xmlItem.itemContent + "</" + xmlItem.itemHeader + ">" + "\n";
            } else {
                xmlStructure += "\n";
            }
        }
        if (parents.size() > 0) {
            while (!parents.isEmpty()) {
                xmlItem = (XMLItem) parents.pop();
                xmlStructure += getSpaces(xmlItem.incrRate) + "</" + xmlItem.itemHeader + ">" + "\n";
            }
        }
        //xmlStructure = EncodingUtil.toUTF8(xmlStructure);

        return xmlStructure;
    }

    public int index0(byte[] data, byte[] data0, int off) {
        int len = data0.length;
        if (data.length < off + len) {
            return -1;
        }
        for (int i = off; i < data.length - len; i++) {
            if (equals0(data, data0, i, len)) {
                return i;
            }
        }
        return -1;
    }

    public boolean equals0(byte[] data, byte[] data0, int off, int len) {
        if (data.length < off + len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (data[i + off] != data0[i]) {
                return false;
            }
        }
        return true;
    }

    public byte[] sub0(byte[] data, int start, int fin) {
        if (fin <= start) {
            return new byte[0];
        }
        byte[] temp = new byte[fin - start];
        System.arraycopy(data, start, temp, 0, fin - start);
        return temp;
    }

    public byte[] plus0(byte[] data, byte c) {
        byte[] temp = new byte[data.length + 1];
        System.arraycopy(data, 0, temp, 0, data.length);
        temp[data.length] = c;
        return temp;
    }

//    public byte[] plus1(byte[] data, byte[] data0) {
//        byte[] temp = new byte[data.length + data0.length];
//        System.arraycopy(data, 0, temp, 0, data.length);
//        System.arraycopy(data0, data.length, temp, 0, data0.length);
//        return temp;
//    }


    /**
     * Parsing XML formatted text
     *
     * @param xmlStructure
     * StringBuffer method
     */

//    public void setStructure(String xmlStructure) {
//        //xmlStructure = EncodingUtil.getUTF8_OE(xmlStructure);
//        //byte[] xmlStructure0 = EncodingUtil.getUTF8_OE(xmlStructure).getBytes();
//        //byte[] xmlStructure0 = xmlStructure.getBytes();
//        //byte[] xmlStructure = Util.stringToByteArray(xmlStructure, Options.getBoolean(Options.OPTION_CP1251_HACK));
//        //System.out.println("lets road..");
//        items = new Vector();
//        parents = new Stack();
//
//        XMLItem xmlItem = new XMLItem();
//        char ch;
//        boolean isHeader = false;
//        StringBuffer buffer0 = new StringBuffer();
//        StringBuffer half = new StringBuffer();
//        StringBuffer secBuffer = new StringBuffer();
//        for (int c = 0; c < xmlStructure.length(); c++) {
//            ch = xmlStructure.charAt(c);
//            if (ch == '<') {
//                if (c + 4 < xmlStructure.length() && xmlStructure.indexOf("<!--") == c) {
//                    c = xmlStructure.indexOf("-->", c + 4) + 3;
//                    if (c == -1) {
//                        break;
//                    }
//                    c += 3;
//                }
//                if (buffer0.length() > 0) {
//                    /*
//                     * В буфере находится содержимое элемента
//                     */
//                    xmlItem.itemContent = buffer0.toString();
//                    buffer0.setLength(0);
//                }
//                isHeader = true;
//                continue;
//            } else if (ch == '>') {
//                if (buffer0.length() > 0) {
//                    /*
//                     * В буфере находится содержимое заголовка
//                     */
//                    ItemParam itemParam = null;
//                    boolean isFirstSpace = true;
//                    boolean isParam = false;
//                    for (int i = 0; i < buffer0.length() - 2; i++) {
//                        half.setLength(0);
//                        half.append(buffer0.toString().substring(i, i + 2));
//                        if (half.charAt(0) == ' ') {
//                            if (isFirstSpace) {
//                                /*
//                                 * Первый метод получения названия элемента
//                                 */
//                                xmlItem = new XMLItem();
//                                if (secBuffer.length() > 0) {
//                                    if (secBuffer.charAt(0) == '?') {
//                                        secBuffer.deleteCharAt(0);
//                                        xmlItem.itemHeader = secBuffer.toString();
//                                        xmlItem.itemType = 1;
//                                    } else if (secBuffer.charAt(0) == '!') {
//                                        secBuffer.deleteCharAt(0);
//                                        xmlItem.itemHeader = secBuffer.toString();
//                                        xmlItem.itemType = 2;
//                                    } else {
//                                        xmlItem.itemHeader = secBuffer.toString();
//                                    }
//                                }
//                                secBuffer.setLength(0);
//                                isFirstSpace = false;
//                                continue;
//                            }
//                            if (!isParam) {
//                                continue;
//                            }
//                        }
//                        if (itemParam == null && i == buffer0.length() - 3) {
//                            /*
//                             * Второй метод получения названия элемента
//                             */
//                            xmlItem = new XMLItem();
//                            if (secBuffer.length() > 0 & buffer0.length() > 1) {
//                                if (secBuffer.charAt(0) == '/') {
//                                    secBuffer.deleteCharAt(0);
//                                    xmlItem.itemHeader = secBuffer.toString();
//                                } else {
//                                    xmlItem.itemHeader = buffer0.toString();
//                                }
//                            }
//                            secBuffer.setLength(0);
//                            //} else if (half.startsWith("=\"")) {
//                        } else if ((half.length() > 1) ? half.charAt(0) == '=' & half.charAt(1) == '"' : half.charAt(0) == '=') {
//                            /*
//                             * Получение имени параметра
//                             * Начало
//                             */
//                            itemParam = new ItemParam();
//                            itemParam.paramTitle = secBuffer.toString();
//                            secBuffer.setLength(0);
//                            i++;
//                            isParam = true;
//                            continue;
//                            //} else if (half.startsWith("\" ") || i == buffer.length() - 3) {
//                        } else if (((half.length() > 1) ? half.charAt(0) == '"' & half.charAt(1) == ' ' : half.charAt(0) == '"') || i == buffer0.length() - 3) {
//                            /*
//                             * Получение значения параметра
//                             * Опустошение буфера
//                             */
//                            isParam = false;
//                            if (i == buffer0.length() - 3) {
//                                if (half.charAt(0) != '"') {
//                                    secBuffer.append(half.charAt(0));
//                                    if (half.charAt(1) != '"') {
//                                        secBuffer.append(half.charAt(1));
//                                    }
//                                }
//                            }
//                            itemParam.paramValue = secBuffer.toString();
//                            xmlItem.addParameter(itemParam);
//                            secBuffer.setLength(0);
//                            continue;
//                        }
//                        secBuffer.append(half.charAt(0));
//                    }
//                    if (!(buffer0.length() > 1 & (buffer0.charAt(0) == '/' || buffer0.charAt(buffer0.length() - 1) == '/'))) {
//                        /*
//                         * Элемент-заголовок, с последующим завершением
//                         * <header>
//                         *
//                         * Первый метод добавления элемента
//                         *
//                         */
//                        if (!parents.isEmpty()) {
//                            xmlItem.parentItem = (XMLItem) parents.peek();
//                        }
//                        xmlItem.incrRate = parents.size();
//                        items.addElement(xmlItem);
//                        parents.push(xmlItem);
//                    } else if (buffer0.length() > 0 & buffer0.charAt(0) == '/') {
//                        /*
//                         * Завершение элемента-заголовка
//                         * </header>
//                         */
//                        parents.pop();
//                        xmlItem = new XMLItem();
//                        if (!parents.isEmpty()) {
//                            xmlItem.parentItem = (XMLItem) parents.peek();
//                        }
//                    } else if (buffer0.length() > 0 & buffer0.charAt(buffer0.length() - 1) == '/') {
//                        /*
//                         * Элемент с самозавершением
//                         * <header param="value"/>
//                         *
//                         * Второй метод добавления элемента
//                         *
//                         */
//                        if (!parents.isEmpty()) {
//                            xmlItem.parentItem = (XMLItem) parents.peek();
//                        }
//                        xmlItem.incrRate = parents.size();
//                        if (xmlItem.itemHeader.endsWith("/")) {
//                            xmlItem.itemHeader = xmlItem.itemHeader.substring(0, xmlItem.itemHeader.length() - 1);
//                        }
//                        //// System.out.printlnxmlItem.itemHeader);
//                        items.addElement(xmlItem);
//                        xmlItem = new XMLItem();
//                    }
//                    buffer0.setLength(0);
//                }
//                isHeader = false;
//                continue;
//            }
//            if (isHeader) {
//                boolean isContinue = false;
//                for (int i = 0; i < headerBlockChars.length; i++) {
//                    if (ch == headerBlockChars[i]) {
//                        isContinue = true;
//                        break;
//                    }
//                }
//                if (isContinue) {
//                    continue;
//                }
//                buffer0.append(ch);
//            } else {
//                buffer0.append(ch);
//            }
//        }
//    }

    public void setStructure(String xmlStructure) {
        //byte[] xmlStructure0 = xmlStructure.getBytes();
        byte[] xmlStructure0 = Util.stringToByteArray(xmlStructure, Options.getBoolean(Options.OPTION_CP1251_HACK));
        items = new Vector();
        parents = new Stack();

        XMLItem xmlItem = new XMLItem();
        byte ch;
        boolean isHeader = false;
        byte[] buffer0 = new byte[0];
        for (int c = 0; c < xmlStructure0.length; c++) {
            ch = xmlStructure0[c];
            if (ch == '<') {
                if (c + 4 < xmlStructure0.length && equals0(xmlStructure0, new byte[]{'<', '!', '-', '-'}, c, 4)) {
                    c = index0(xmlStructure0, new byte[]{'-', '-', '>'}, c);
                    if (c == -1) {
                        break;
                    }
                    c += 3;
                }
                if (buffer0.length > 0) {
                    /*
                     * В буфере находится содержимое элемента
                     */
                    xmlItem.itemContent = Util.byteArrayToString(buffer0);
                    buffer0 = new byte[0];
                }
                isHeader = true;
                continue;
            } else if (ch == '>') {
                if (buffer0.length > 0) {
                    /*
                     * В буфере находится содержимое заголовка
                     */
                    ItemParam itemParam = null;
                    byte[] half;
                    byte[] secBuffer = new byte[0];
                    boolean isFirstSpace = true;
                    boolean isParam = false;
                    for (int i = 0; i < buffer0.length - 2; i++) {
                        half = new byte[2];
                        System.arraycopy(buffer0, i, half, 0, 2);
                        if (half[0] == ' ') {
                            if (isFirstSpace) {
                                /*
                                 * Первый метод получения названия элемента
                                 */
                                xmlItem = new XMLItem();
                                if (secBuffer.length > 0) {
                                    if (secBuffer[0] == '?') {
                                        secBuffer = sub0(secBuffer, 1, secBuffer.length);
                                        xmlItem.itemHeader = Util.byteArrayToString(secBuffer);
                                        xmlItem.itemType = 1;
                                    } else if (secBuffer[0] == '!') {
                                        secBuffer = sub0(buffer0, 1, buffer0.length);
                                        xmlItem.itemHeader = Util.byteArrayToString(secBuffer);
                                        xmlItem.itemType = 2;
                                    } else {
                                        xmlItem.itemHeader = Util.byteArrayToString(secBuffer);
                                    }
                                }
                                secBuffer = new byte[0];
                                isFirstSpace = false;
                                continue;
                            }
                            if (!isParam) {
                                continue;
                            }
                        }
                        if (itemParam == null && i == buffer0.length - 3) {
                            /*
                             * Второй метод получения названия элемента
                             */
                            xmlItem = new XMLItem();
                            if (secBuffer.length > 0 & buffer0.length > 1) {
                                if (secBuffer[0] == '/') {
                                    secBuffer = sub0(buffer0, 1, buffer0.length);
                                    xmlItem.itemHeader = Util.byteArrayToString(secBuffer);
                                } else {
                                    xmlItem.itemHeader = Util.byteArrayToString(buffer0);
                                }
                            }
                            secBuffer = new byte[0];
                            //} else if (half.startsWith("=\"")) {
                        } else if ((half.length > 1) ? half[0] == '=' & half[1] == '"' : half[0] == '=') {
                            /*
                             * Получение имени параметра
                             * Начало
                             */
                            itemParam = new ItemParam();
                            itemParam.paramTitle = Util.byteArrayToString(secBuffer);
                            secBuffer = new byte[0];
                            i++;
                            isParam = true;
                            continue;
                            //} else if (half.startsWith("\" ") || i == buffer.length() - 3) {
                        } else if (((half.length > 1) ? half[0] == '"' & half[1] == ' ' : half[0] == '"') || i == buffer0.length - 3) {
                            /*
                             * Получение значения параметра
                             * Опустошение буфера
                             */
                            isParam = false;
                            if (i == buffer0.length - 3) {
                                if (half[0] != '"') {
                                    secBuffer = plus0(secBuffer, half[0]);
                                    if (half[1] != '"') {
                                        secBuffer = plus0(secBuffer, half[1]);
                                    }
                                }
                            }
                            itemParam.paramValue = Util.byteArrayToString(secBuffer);
                            xmlItem.addParameter(itemParam);
                            secBuffer = new byte[0];
                            continue;
                        }
                        secBuffer = plus0(secBuffer, half[0]);
                    }
                    if (!(buffer0.length > 1 & (buffer0[0] == '/' || buffer0[buffer0.length - 1] == '/'))) {
                        /*
                         * Элемент-заголовок, с последующим завершением
                         * <header>
                         *
                         * Первый метод добавления элемента
                         *
                         */
                        if (!parents.isEmpty()) {
                            xmlItem.parentItem = (XMLItem) parents.peek();
                        }
                        xmlItem.incrRate = parents.size();
                        items.addElement(xmlItem);
                        parents.push(xmlItem);
                    } else if (buffer0.length > 0 & buffer0[0] == '/') {
                        /*
                         * Завершение элемента-заголовка
                         * </header>
                         */
                        parents.pop();
                        xmlItem = new XMLItem();
                        if (!parents.isEmpty()) {
                            xmlItem.parentItem = (XMLItem) parents.peek();
                        }
                    } else if (buffer0.length > 0 & buffer0[buffer0.length - 1] == '/') {
                        /*
                         * Элемент с самозавершением
                         * <header param="value"/>
                         *
                         * Второй метод добавления элемента
                         *
                         */
                        if (!parents.isEmpty()) {
                            xmlItem.parentItem = (XMLItem) parents.peek();
                        }
                        xmlItem.incrRate = parents.size();
                        if (xmlItem.itemHeader.endsWith("/")) {
                            xmlItem.itemHeader = xmlItem.itemHeader.substring(0, xmlItem.itemHeader.length() - 1);
                        }
                        //// System.out.printlnxmlItem.itemHeader);
                        items.addElement(xmlItem);
                        xmlItem = new XMLItem();
                    }
                    buffer0 = new byte[0];
                }
                isHeader = false;
                continue;
            }
            if (isHeader) {
                boolean isContinue = false;
                for (int i = 0; i < headerBlockChars.length; i++) {
                    if (ch == headerBlockChars[i]) {
                        isContinue = true;
                        break;
                    }
                }
                if (isContinue) {
                    continue;
                }
                buffer0 = plus0(buffer0, ch);
            } else {
                buffer0 = plus0(buffer0, ch);
            }
        }
    }

    /**
     * Generating spaces by count of parents size
     *
     * @return Spaces string
     */
    private String getSpaces() {
        return getSpaces(parents.size());
    }

    /**
     * Generating spaces by count of provided count
     *
     * @param count
     * @return Spaces string
     */
    private String getSpaces(int count) {
        String spaces = "";
        for (int t = 0; t < count; t++) {
            spaces += new String(spaceString);
        }
        return spaces;
    }

    /**
     * Checking provided text for emptyness
     *
     * @param textToCheck
     * @return true if it is empty and false in opposite case
     */
    private boolean checkFill(String textToCheck) {
        char ch;
        boolean isFill = false;
        if (textToCheck == null || textToCheck.length() == 0) {
            return true;
        }
        for (int c = 0; c < textToCheck.length(); c++) {
            ch = textToCheck.charAt(c);
            isFill = true;
            for (int i = 0; i < fillChars.length; i++) {
                if (ch == fillChars[i]) {
                    isFill = false;
                    //continue;
                }
            }
            if (!isFill) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requesting XML item in provided parentship with specified parameter
     *
     * @param itemParentship
     * @param paramTitle
     * @param paramValue
     * @return
     */
    public XMLItem getItemWithParamValue(String[] itemParentship, String paramTitle, String paramValue) {
        XMLItem[] xmlGroups = getItems(itemParentship);
        for (int c = 0; c < xmlGroups.length; c++) {
            for (int i = 0; i < xmlGroups[c].itemParams.length; i++) {
                if (xmlGroups[c].itemParams[i].paramTitle.equals(paramTitle)) {
                    if ((xmlGroups[c].itemParams[i].paramValue).equals(paramValue)) {
                        return xmlGroups[c];
                    }
                }
            }

        }
        return null;
    }

    public XMLItem[] getItemsWithHeader(String[] itemParentship, String headerTitle) {
        XMLItem xmlItem;
        int incrRate = 0;
        Vector xmlItems = new Vector();
        int inBlockCount = 0;
        for (int i = 0; i < items.size(); i++) {
            xmlItem = (XMLItem) items.elementAt(i);
//            if (xmlItem == null) {
//                System.out.println("e1");
//            }
//            if (xmlItem.itemHeader == null) {
//                System.out.println("e2");
//            }
            if (xmlItem.itemHeader.equals(itemParentship[incrRate])) {
                incrRate++;
                if (xmlItem.incrRate == itemParentship.length - 1) {
                    while (++i < items.size()) {
                        xmlItem = ((XMLItem) items.elementAt(i));
                        if (xmlItem.incrRate < incrRate) {
                            break;
                        } else if (xmlItem.incrRate > incrRate || !xmlItem.itemHeader.equals(headerTitle)) {
                            continue;
                        }
                        xmlItems.addElement(xmlItem);
                    }
                    XMLItem[] xmlItemsList = new XMLItem[xmlItems.size()];
                    xmlItems.copyInto(xmlItemsList);
                    return xmlItemsList;
                }
                inBlockCount = 0;
            }
            inBlockCount++;
        }
        return null;
    }
}
