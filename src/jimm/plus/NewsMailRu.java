//#sijapp cond.if modules_MAILRU is "true"#
package jimm.plus;

import com.tomclaw.xmlgear.XMLGear;
import com.tomclaw.xmlgear.XMLItem;
import jimm.ui.OnlineStatus;
import jimm.util.ResourceBundle;
import jimm.JimmUI;
import jimm.Jimm;
import DrawControls.TextList;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;

/**
 * Created [01.03.2011, 16:11:24]
 * Develop by Lavlinsky Roman on 2011
 */
public final class NewsMailRu extends TextList implements CommandListener {

    public NewsMailRu() {
        super(ResourceBundle.getString("mail"), false);
        setMode(MODE_TEXT);
        setColorScheme();
        setCommandListener(this);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
    }

    public NewsMailRu fill() {
        //"http://news.mail.ru/rss/"
        //String content = Util.getStringAsStream("/news1.txt");
        addBigText("wait", getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
        String content = OnlineStatus.getStringAsHttp("http://news.mail.ru/rss/");
        if (content.length() <= 1) {
            clear();
            addBigText("fault", getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
            return this;
        }
        try {
            System.out.println(content);
            XMLGear xg = new XMLGear();
            int channel = content.indexOf("<item>");
            channel = Math.max(0, channel);
            int channel2 = content.indexOf("</channel>");
            channel2 = Math.min(Math.max(channel2, content.length()), channel2) + 10;
            System.out.println("<channel>\n" + content.substring(channel, channel2));
            xg.setStructure("<channel>\n" + content.substring(channel, channel2));
            //System.out.println(xg.getStructureLine());
            //System.out.println(xg.getStructure());
            XMLItem[] XSItem;
            XSItem = xg.getItemsWithHeader(new String[]{"channel"}, "item");
            lock();
            for (int i = 0; i < XSItem.length; i++) {
                XMLItem[] items = xg.getItems(XSItem[i]);
                for (int c = 0; c < items.length; c++) {
                    System.out.println(items[c].itemHeader);
                    System.out.println(items[c].itemContent);
                    if (items[c].itemHeader.indexOf("title") != -1) {
                        addBigText(items[c].itemContent, getColor(COLOR_TEXT), Font.STYLE_PLAIN, i).doCRLF(i);
                        System.out.println(items[c].itemContent);
                    } else if (items[c].itemHeader.indexOf("description") != -1) {
                        addBigText(items[c].itemContent, getColor(COLOR_TEXT), Font.STYLE_PLAIN, i).doCRLF(i);
                        System.out.println(items[c].itemContent);
                    }
                }
            }
            unlock();
            System.out.println(String.valueOf((XSItem != null) ? XSItem.length : -1));
        } catch (Exception e) {
            clear();
            addBigText("fault", getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
        } catch (OutOfMemoryError ignored) {
        }
        return this;
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
//            if (prvScreen != null) {
//                Jimm.setDisplay(prvScreen);
//                prvScreen = null;
//            } else {
            Jimm.getContactList().activate();
//            }
        }
    }
}
//#sijapp cond.end#