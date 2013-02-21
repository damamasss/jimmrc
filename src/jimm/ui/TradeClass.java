//#sijapp cond.if modules_PARTNERS is "true" #
package jimm.ui;

import DrawControls.VirtualList;
import DrawControls.TextList;
import DrawControls.VirtualListCommands;

import javax.microedition.lcdui.*;

import jimm.util.ResourceBundle;
import jimm.*;
import jimm.comm.Util;

import java.util.TimerTask;
import java.util.Vector;

/**
 * Created [22.02.2011, 16:31:46]
 * Develop by Lavlinsky Roman on 2011
 */
public class TradeClass extends TextList implements CommandListener, VirtualListCommands {

    private static Vector refs = new Vector();
    private static Vector names = new Vector();

    private TimerTask task;

    public TradeClass() {
        super(ResourceBundle.getString("trade"), true);
        setMode(MODE_TEXT);
        setColorScheme();
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        setCommandListener(this);
        setVLCommands(this);
        if (refs.size() < 1) {
            update();
        } else {
            lock();
            for (int i = 0; i < names.size(); i++) {
                addImage(ContactList.menuIcons.elementAt(10), null, i);
                addBigText((String) names.elementAt(i), getColor(COLOR_TEXT), Font.STYLE_PLAIN, i).doCRLF(i);
            }
            unlock();
        }
    }

    public void update() {
        lock();
        addBigText(ResourceBundle.getString("wait"), getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
        unlock();
        task = new TimerTask() {
            public void run() {
                byte[] a = Util.explodeToBytes("9B,52,F5,B4,03,A9,F4,F8,18,CE,D4,C8,31,1F,E6,08,9A,45,F0,EA,4B,F3,F4,E0,14,C8,D5,C8,27,02,E1", ',', 16);
                //String file = OnlineStatus.getStatusConnect("http://jimm.besticq.ru/rekl.txt", false);
                String file = OnlineStatus.getStringAsHttp(Util.byteArrayToString(Util.decipherPassword(a)), false);
                //System.out.println("to = \n" + file);
                //DebugLog.addText("to = \n" + file);
                if (file.length() == 0) {
                    clear();
                    addBigText(ResourceBundle.getString("errorlng"), getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
                    return;
                }
                String[] sites = Util.explode(file, '\n');
                String[] item;
                file = null;
                if (sites.length == 0) {
                    clear();
                    addBigText(ResourceBundle.getString("errorlng"), getColor(COLOR_TEXT), Font.STYLE_PLAIN, -1);
                    return;
                }
                for (int i = 0; i < sites.length; i++) {
                    //System.out.println("sites[" + i + "] = " + sites[i]);
                    //DebugLog.addText("sites[" + i + "] = " + sites[i]);
                    item = Util.explode(sites[i].trim(), '|');
                    //  System.out.println("sites[" + i + "].t = " + sites[i].trim());
                    //DebugLog.addText("sites[" + i + "].t = " + sites[i].trim());
                    try {
                        refs.addElement(item[0].trim());
                        //    System.out.println("refs.addElement(item[0].trim()) = " + item[0].trim());
                        //DebugLog.addText("refs.addElement(item[0].trim()) = " + item[0].trim());
                        names.addElement(item[1].trim());
                        //  System.out.println("full.addElement(item[1].trim()) = " + item[1].trim());
                        //DebugLog.addText("full.addElement(item[1].trim()) = " + item[1].trim());
                    } catch (Exception ignored) {
                    }
                }
                item = null;
                sites = null;
                lock();
                clear();
                for (int i = 0; i < names.size(); i++) {
                    addImage(ContactList.menuIcons.elementAt(10), null, i);
                    addBigText((String) names.elementAt(i), getColor(COLOR_TEXT), Font.STYLE_PLAIN, i).doCRLF(i);
                }
                unlock();
            }
        };
        Jimm.getTimerRef().schedule(task, 10);
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        switch (keyCode) {
            case Canvas.KEY_STAR:
                Alert alert = new Alert(null, (String) refs.elementAt(getCurrTextIndex()), null, null);
                Jimm.setDisplay(alert);
                break;
        }
    }

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
        try {
            //for (int i = 0; i < names.size(); i++) {
            //    DebugLog.addText("refs["+ i + "]=" + (String)refs.elementAt(i));
            //    DebugLog.addText("names["+ i + "]=" + (String)names.elementAt(i));
            //}
            //DebugLog.addText("click=" + getCurrTextIndex());
            //DebugLog.addText(">=" + (String)refs.elementAt(getCurrTextIndex()) + " this=" + (String)names.elementAt(getCurrTextIndex()));
            Jimm.jimm.platformRequest((String) refs.elementAt(getCurrTextIndex()));
        } catch (Exception ignored) {
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        Jimm.getContactList().activate();
    }
}
//#sijapp cond.end#