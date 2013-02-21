//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
package jimm.chat;

/**
 * Created [23.02.2011, 15:01:30]
 * Develop by Lavlinsky Roman on 2011
 */
final class VikePostHide extends java.util.TimerTask {

    VikePostHide(Vike bp1) {
        a = bp1;
    }

    public final void run() {
        if (a.tempItem != null) {
            a.tempItem.activity = false;
            if (a.tempItem == a.vikePostItem.vikeItem)
                a.vikePostItem.vikeItem = null;
            a.textField.g();
        }
    }

    private final Vike a;
}
//#sijapp cond.end#
