package jimm.info;

import DrawControls.TextList;
import jimm.JimmUI;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;

public abstract class Info extends TextList {

    protected final static byte MENU_COPYTEXT = (byte) 2;
    protected final static byte MENU_COPY_ALLTEXT = (byte) 3;
    protected final static byte MENU_COPYUIN = (byte) 5;

    // Information about the user
    final public static int UI_UIN = 0;
    final public static int UI_NICK = 1;
    final public static int UI_NAME = 2;
    final public static int UI_EMAIL = 3;
    final public static int UI_CITY = 4;
    final public static int UI_STATE = 5;
    final public static int UI_PHONE = 6;
    final public static int UI_FAX = 7;
    final public static int UI_ADDR = 8;
    final public static int UI_CPHONE = 9;
    final public static int UI_AGE = 10;
    final public static int UI_GENDER = 11;
    final public static int UI_HOME_PAGE = 12;
    final public static int UI_BDAY = 13;
    final public static int UI_W_CITY = 14;
    final public static int UI_W_STATE = 15;
    final public static int UI_W_PHONE = 16;
    final public static int UI_W_FAX = 17;
    final public static int UI_W_ADDR = 18;
    final public static int UI_W_NAME = 19;
    final public static int UI_W_DEP = 20;
    final public static int UI_W_POS = 21;
    final public static int UI_ABOUT = 22;
    final public static int UI_INETRESTS = 23;
    final public static int UI_AUTH = 24;
    final public static int UI_STATUS = 25;
    final public static int UI_ICQ_CLIENT = 26;
    final public static int UI_SIGNON = 27;
    final public static int UI_ONLINETIME = 28;
    final public static int UI_IDLE_TIME = 29;
    final public static int UI_REGDATA = 30;
    final public static int UI_ICQ_VERS = 31;
    final public static int UI_INT_IP = 32;
    final public static int UI_EXT_IP = 33;
    final public static int UI_PORT = 34;
    final public static int UI_OFFLINE_TIME = 35;
    final public static int UI_UIN_LIST = 36;
    final public static int UI_FIRST_NAME = 37;
    final public static int UI_LAST_NAME = 38;
    final public static int UI_INT_CAT1 = 39;
    final public static int UI_INT_CAT2 = 40;
    final public static int UI_INT_CAT3 = 41;
    final public static int UI_INT_CAT4 = 42;
    final public static int UI_ORIG_CITY = 43;
    final public static int UI_ORIG_STATE = 44;
    final public static int UI_MODEL_PHONE = 45;
    final public static int UI_CAPABILITES = 46;

    final public static int UI_LAST_ID = 47;

    /* Interest */
    final public static int CAT_COUNT = 51;

    protected int uiBigTextIndex = 0;
    protected String uiSectName = null;

    private String[] catNames;


    public Info() {
        super(null, false);
    }

    public String getCategoryName(int i) {
        if (catNames == null) {
            catNames = Util.explode(
                    "interest_1" + "|" + "interest_2" + "|" + "interest_3" + "|" + "interest_4" + "|" + "interest_5" + "|" + "interest_6" + "|" + "interest_7" + "|" +
                            "interest_8" + "|" + "interest_9" + "|" + "interest_10" + "|" + "interest_11" + "|" + "interest_12" + "|" + "interest_13" + "|" + "interest_14" + "|" +
                            "interest_15" + "|" + "interest_16" + "|" + "interest_17" + "|" + "interest_18" + "|" + "interest_19" + "|" + "interest_20" + "|" + "interest_21" + "|" +
                            "interest_22" + "|" + "interest_23" + "|" + "interest_24" + "|" + "interest_25" + "|" + "interest_26" + "|" + "interest_27" + "|" + "interest_28" + "|" +
                            "interest_29" + "|" + "interest_30" + "|" + "interest_31" + "|" + "interest_32" + "|" + "interest_33" + "|" + "interest_34" + "|" + "interest_35" + "|" +
                            "interest_36" + "|" + "interest_37" + "|" + "interest_38" + "|" + "interest_39" + "|" + "interest_40" + "|" + "interest_41" + "|" + "interest_42" + "|" +
                            "interest_43" + "|" + "interest_44" + "|" + "interest_45" + "|" + "interest_46" + "|" + "interest_47" + "|" + "interest_48" + "|" + "interest_49" + "|" +
                            "interest_50" + "|" + "interest_51", '|'
            );
        }
        return catNames[i];
    }

    public abstract void fillUserInfo(String[] data);


    protected void addToTextList(String str, String langStr) {
        if (uiSectName != null) {
            addBigText(ResourceBundle.getString(uiSectName), getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1).doCRLF(-1);
            uiSectName = null;
        }
        if (langStr.length() > 0) {
            addBigText(ResourceBundle.getString(langStr) + ": ", getColor(COLOR_CC_TEXT), Font.STYLE_PLAIN, uiBigTextIndex);
        }
        addBigText(str, getColor(COLOR_CAT_TEXT), Font.STYLE_PLAIN, uiBigTextIndex)
                .doCRLF(uiBigTextIndex);
        uiBigTextIndex++;
    }

    protected void addToTextList(int index, String[] data, String langStr) {
        String str = data[index];
        if (str == null) return;
        if (str.length() == 0) return;

        addToTextList(str, langStr);
    }


    protected void addToTextList(int index, String[] data) {
        String str = data[index];
        if (str == null) return;
        if (str.length() <= 2) return;
        int idx = EditInfo.getIndex(str);
        if ((idx == 60) || (idx < 0)) return;

        addToTextList(str.substring(2), /*ResourceBundle.getString(*/getCategoryName(idx)/*)*/);
    }

    protected void initInfoTextList(String caption, boolean addCommands, CommandListener listener) {
        removeAllCommands();
// #sijapp cond.if target is "MOTOROLA"#
// #		setFontSize(Font.SIZE_MEDIUM);
// #sijapp cond.else#
        setFontSize(Font.SIZE_SMALL);
// #sijapp cond.end#
        setCaption(caption);
        setColorScheme();
        setMode(TextList.MODE_TEXT);
        if (addCommands) {
            addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
            addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        }
        if (listener != null) {
            setCommandListener(listener);
        }
    }

    public static TextList getInfoTextList(String caption, boolean addCommands) {
        TextList infoTextList = new TextList(null, false);
        infoTextList.setFontSize(Font.SIZE_SMALL);
        infoTextList.setCaption(caption);
        infoTextList.setColorScheme();
        infoTextList.setMode(TextList.MODE_TEXT);
        if (addCommands) {
            infoTextList.addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
            infoTextList.addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        }
        return infoTextList;
    }
}