//#sijapp cond.if target is "MIDP2"#
package jimm;

import jimm.util.ResourceBundle;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.*;
import javax.wireless.messaging.*;

public class SmsOrCall implements CommandListener {
    /**
     * @author Rishat Shamsutdinov
     */
    private Command cmdSendBox;
    private TextBox box;
    private String smsnum;

    private final Command cmdMakeCall = new Command(ResourceBundle.getString("make_call"), Command.ITEM, 0);
    private final Command cmdSendSms = new Command(ResourceBundle.getString("send_sms"), Command.ITEM, 1);

    public SmsOrCall() {
        box = new TextBox(ResourceBundle.getString("nr"), null, 450, TextField.PHONENUMBER);
        smsnum = "";
        box.addCommand(cmdMakeCall);
        box.addCommand(cmdSendSms);
        box.addCommand(JimmUI.cmdCancel);
        box.setCommandListener(this);
    }

    public void show() {
        Jimm.setDisplay(box);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdMakeCall) {
            act(true, box.getString());
        } else if (c == cmdSendSms) {
            smsnum = box.getString();
            box.setString(null);
            try {
                box.setConstraints(TextField.ANY);
            } catch (Exception ignored) {
            }
            box.setTitle(ResourceBundle.getString("message"));
            box.removeCommand(cmdMakeCall);
            box.removeCommand(cmdSendSms);
            box.addCommand(cmdSendBox = new Command(ResourceBundle.getString("send"), Command.OK, 1));
            return;
        }
        if (c == cmdSendBox) {
            act(false, box.getString());
        }
        Jimm.back();
    }

    private void act(boolean makeCall, String text) {
        if (makeCall) {
            try {
                Jimm.jimm.platformRequest("tel:" + text); // todo *100# - не обрабатывает
            } catch (Exception ignored) {
            }
        } else {
            try {
                MessageConnection conn;
                TextMessage mess;
                conn = (MessageConnection) Connector.open("sms://" + smsnum);
                mess = (TextMessage) conn.newMessage(MessageConnection.TEXT_MESSAGE);
                mess.setPayloadText(text);
                conn.send(mess);
                conn.close();
                //System.out.println("sms num = " + conn.numberOfSegments(mess));
            } catch (Exception ignored) {
            }
        }
    }
}
//#sijapp cond.end#