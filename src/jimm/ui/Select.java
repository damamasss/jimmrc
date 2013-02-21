package jimm.ui;

import javax.microedition.lcdui.*;

import jimm.Jimm;
import jimm.JimmUI;
import jimm.util.ResourceBundle;
import DrawControls.NativeCanvas;
import DrawControls.TextList;

public class Select extends TextList implements CommandListener {

    public static final byte SELECT_OK = 0;
    public static final byte SELECT_NO = (byte) 1;
    public static final byte TYPE_OKCANCEL = (byte) 2;
    public static final byte TYPE_YESNO = (byte) 3;

    private int action;
    private SelectListener sListener;
    private Object prevscreen;
    private Object o;

    public Select(Object prevscreen, String cap, String text, SelectListener sListener, int action, Object o) {
        super(cap);
        addCommandEx(JimmUI.cmdYes, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdNo, MENU_TYPE_RIGHT_BAR);
        setMode(MODE_TEXT);
        setColorScheme();
        setFontSize(Font.SIZE_SMALL);
        addBigText(text, getTextColor(), Font.STYLE_PLAIN, -1);
        setCommandListener(this);
        this.sListener = sListener;
        this.action = action;
        this.o = o;
        this.prevscreen = prevscreen;
    }

    public Select(String cap, String text, SelectListener sListener, int action, Object o) {
        this(Jimm.getCurrentDisplay(), cap, text, sListener, action, o);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdYes) {
            sListener.selectAction(action, SELECT_OK, o);
        } else {
            Jimm.setDisplay(prevscreen);
        }
    }
}