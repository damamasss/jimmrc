package com.tomclaw.xmlgear;

/**
 * XMLItem class
 * This file is distributed under zlib/libpng license
 * @author Solkin I.V., TomClaw Software, 2003-2010
 */
public class XMLItem {

    public XMLItem parentItem = null;
    public String itemHeader = null;
    public String itemContent = null;
    public ItemParam[] itemParams = null;
    public int incrRate = 0;
    public int itemType = 0;

    /**
     * Constructor
     */
    public XMLItem() {
        itemParams = new ItemParam[0];
    }

    /**
     * Constructor
     * @param parentItem
     * @param itemHeader
     * @param itemContent
     * @param itemParams
     * @param incrRate
     */
    public XMLItem(XMLItem parentItem, String itemHeader,
            String itemContent, ItemParam[] itemParams, int incrRate) {
        this.parentItem = parentItem;
        this.itemHeader = itemHeader;
        this.itemContent = itemContent;
        this.itemParams = itemParams;
        this.incrRate = incrRate;
    }

    /**
     * Adding XML item parameter by providing parameter title and parameter value
     * @param paramTitle
     * @param paramValue
     */
    public void addParameter(String paramTitle, String paramValue) {
        addParameter(new ItemParam(paramTitle, paramValue));
    }

    /**
     * Adding XML item parameter by providing ItemParam
     * @param itemParam
     */
    public void addParameter(ItemParam itemParam) {
        ItemParam[] itemParamsNext = new ItemParam[itemParams.length + 1];
        System.arraycopy(itemParams, 0, itemParamsNext, 0, itemParams.length);
        /*ItemParam[] itemParamsNext = new ItemParam[itemParams.length + 1];
        for(int c=0;c<itemParams.length;c++){
        itemParamsNext[c] = itemParams[c];
        }
        itemParamsNext[itemParams.length] = itemParam;
        this.itemParams = itemParamsNext;*/
        itemParamsNext[itemParams.length] = itemParam;
        this.itemParams = itemParamsNext;
    }

    /**
     * Requesting absolute path of item
     * @return Absolute path
     */
    public String getAbsolutePath() {
        String absolutePath = "";
        XMLItem tempParentItem;
        tempParentItem = this;
        do {
            absolutePath = "/" + tempParentItem.itemHeader + absolutePath;
            tempParentItem = tempParentItem.parentItem;
        } while (tempParentItem != null);
        return absolutePath;
    }

    public String getParamValue(String paramName) {
        String itemValue = null;
        for (int i = 0; i < itemParams.length; i++) {
            if (itemParams[i].paramTitle.equals(paramName)) {
                itemValue = itemParams[i].paramValue;
            }
        }
        return itemValue;
    }

    public void setParamValue(String paramName, String paramValue) {
        for (int i = 0; i < itemParams.length; i++) {
            if (itemParams[i].paramTitle.equals(paramName)) {
                itemParams[i].paramValue = paramValue;
                return;
            }
        }
        ItemParam[] tempParams = new ItemParam[itemParams.length + 1];
        System.arraycopy(itemParams, 0, tempParams, 0, itemParams.length);
        tempParams[itemParams.length] = new ItemParam(paramName, paramValue);
        itemParams = tempParams;
    }
}
