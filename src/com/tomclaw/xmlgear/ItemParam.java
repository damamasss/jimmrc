package com.tomclaw.xmlgear;

/**
 * ItemParam class
 * This file is distributed under zlib/libpng license
 * @author Solkin I.V., TomClaw Software, 2003-2010
 */
public class ItemParam {

    public String paramTitle = null;
    public String paramValue = null;

    /**
     * Creates an empty parameter
     */
    public ItemParam(){
    }

    /**
     * Create parameter by providing paramTitle and paramValue
     * @param paramTitle
     * @param paramValue
     */
    public ItemParam(String paramTitle, String paramValue){
        this.paramTitle = paramTitle;
        this.paramValue = paramValue;
    }

}
