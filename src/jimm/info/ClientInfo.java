package jimm.info;

import jimm.ContactItem;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.comm.ClientID;
import jimm.comm.DateAndTime;
import jimm.comm.Util;
import jimm.ui.Menu;
import jimm.ui.MenuListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ClientInfo extends Info implements CommandListener, MenuListener {
    /**
     * @author Shamsutdinov Rishat
     */

    //private String cItemUin;
    public void fillUserInfo(String[] data) {
        addToTextList(UI_ICQ_CLIENT, data, "icq_client");
        addToTextList(UI_ICQ_VERS, data, "icq_version");
        addToTextList(UI_INT_IP, data, "Int IP");
        addToTextList(UI_EXT_IP, data, "Ext IP");
        addToTextList(UI_PORT, data, "Port");

        uiSectName = " ";
        addToTextList(UI_ONLINETIME, data, "li_online_time");
        addToTextList(UI_SIGNON, data, "li_signon_time");
        addToTextList(UI_OFFLINE_TIME, data, "li_offline_time");
        addToTextList(UI_IDLE_TIME, data, "li_idle_time");
        addToTextList(UI_REGDATA, data, "li_regdata_time");

        uiSectName = " ";
        addToTextList(UI_CAPABILITES, data, "[Capabilites]");

        uiSectName = " ";
        addToTextList(UI_MODEL_PHONE, data, "Phone");

//        uiSectName = "dc_info";
//        addToTextList(UI_ICQ_CLIENT, data, "icq_client");
//        addToTextList(UI_SIGNON, data, "li_signon_time");
//        addToTextList(UI_ONLINETIME, data, "li_online_time");
//        addToTextList(UI_OFFLINE_TIME, data, "li_offline_time");
//        addToTextList(UI_IDLE_TIME, data, "li_idle_time");
//        addToTextList(UI_REGDATA, data, "li_regdata_time");
//
//        uiSectName = "DC Information";
//        addToTextList(UI_ICQ_VERS, data, "ICQ version");
//        addToTextList(UI_INT_IP, data, "Int IP");
//        addToTextList(UI_EXT_IP, data, "Ext IP");
//        addToTextList(UI_PORT, data, "Port");
//
//        uiSectName = "mobile_info";
//        addToTextList(UI_MODEL_PHONE, data, "");
    }

    public void showClientInfo(ContactItem cItem) {
        //if (cItem != null) {
        //    cItemUin = cItem.getUinString();
        //}
        initInfoTextList(cItem.getUinString(), true, this);
        String[] clInfoData = new String[UI_LAST_ID];

        /* regdata on time */
        long regdataTime = cItem.getIntValue(ContactItem.CONTACTITEM_REGDATA);
        if (regdataTime > 0) clInfoData[UI_REGDATA] = DateAndTime.getDateString(false, false, regdataTime);

        /* sign on time */
        long signonTime = cItem.getIntValue(ContactItem.CONTACTITEM_SIGNON);
        if (signonTime > 0) clInfoData[UI_SIGNON] = DateAndTime.getDateString(false, false, signonTime);

        /* online time */
        long onlineTime = DateAndTime.createCurrentDate(false) - signonTime;//cItem.getIntValue(ContactItem.CONTACTITEM_ONLINE);
        if (onlineTime > 0 && signonTime > 0) clInfoData[UI_ONLINETIME] = DateAndTime.longitudeToString(onlineTime);

        /* Offline since */
        if ((cItem.getStringValue(ContactItem.CONTACTITEM_OFFLINETIME) != null) &&
                (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == ContactItem.STATUS_OFFLINE))
            clInfoData[UI_OFFLINE_TIME] = cItem.getStringValue(ContactItem.CONTACTITEM_OFFLINETIME);

        /* idle time */
        long idleTime = cItem.getIntValue(ContactItem.CONTACTITEM_IDLE);
        if (idleTime > 0) clInfoData[UI_IDLE_TIME] = DateAndTime.longitudeToString(idleTime);
        /* Client version */
//        int clientVers = cItem.getIntValue(ContactItem.CONTACTITEM_CLIENT);
//        if (clientVers != ClientID.CLI_NONE) clInfoData[UI_ICQ_CLIENT] = ClientID.getClientString((byte) clientVers)
//                + " " + cItem.getStringValue(ContactItem.CONTACTITEM_CLIVERSION);

        String vers = cItem.getStringValue(ContactItem.CONTACTITEM_CLIVERSION);
        if (vers != null && vers.length() > 0)
            clInfoData[UI_ICQ_CLIENT] = cItem.getStringValue(ContactItem.CONTACTITEM_CLIVERSION);
        if (cItem.capabilities != null)
            clInfoData[UI_CAPABILITES] = "\n" + ClientID.capsRead(cItem.capabilities, cItem.dc1, cItem.dc2, cItem.dc3);
        //else
        //    clInfoData[UI_CAPABILITES] = "";
        /* ICQ protocol version */
        if (cItem.getIntValue(ContactItem.CONTACTITEM_ICQ_PROT) > 0) {
            clInfoData[UI_ICQ_VERS] = Integer.toString(cItem.getIntValue(ContactItem.CONTACTITEM_ICQ_PROT));
        }
        /* Internal IP */
        clInfoData[UI_INT_IP] = Util.ipToString(cItem.getIPValue(ContactItem.CONTACTITEM_INTERNAL_IP));
        /* External IP */
        clInfoData[UI_EXT_IP] = Util.ipToString(cItem.getIPValue(ContactItem.CONTACTITEM_EXTERNAL_IP));
        /* Port */
        int port = cItem.getIntValue(ContactItem.CONTACTITEM_DC_PORT);
        if (port != 0) {
            clInfoData[UI_PORT] = Integer.toString(port);
        }
        if ((cItem.getStringValue(ContactItem.CONTACTITEM_MODEL_PHONE) != null) && cItem.getStringValue(ContactItem.CONTACTITEM_MODEL_PHONE).length() > 0)
            clInfoData[UI_MODEL_PHONE] = "\n" + cItem.getStringValue(ContactItem.CONTACTITEM_MODEL_PHONE);

        fillUserInfo(clInfoData);
        activate();
    }

    private void showMenu() {
        Menu menu = new Menu(this);
        menu.addMenuItem("copy_text", MENU_COPYTEXT);
        //menu.addMenuItem("copy_uin", MENU_COPYUIN);
        menu.addMenuItem("copy_all_text", MENU_COPY_ALLTEXT);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        menu.back();
        switch (action) {
            case MENU_COPYTEXT:
            case MENU_COPY_ALLTEXT:
                JimmUI.setClipBoardText
                        (
                                "[" + getCaption() + "]\n" + getCurrText(0, (action == MENU_COPY_ALLTEXT))
                        );
                break;

//            case MENU_COPYUIN:
//                if (cItemUin != null) {
//                    JimmUI.setClipBoardText(cItemUin);
//                }
//                break;
        }
    }

    public void commandAction(Command c, Displayable d) {
        if ((c == JimmUI.cmdCancel) || (c == JimmUI.cmdBack)) {
            Jimm.back();
        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        }
    }
}