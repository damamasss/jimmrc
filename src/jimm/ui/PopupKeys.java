package jimm.ui;

import jimm.util.ResourceBundle;
import jimm.Jimm;
import jimm.Options;

import javax.microedition.lcdui.Canvas;

import DrawControls.NativeCanvas;

public class PopupKeys extends PopUp {

    KeysCatcher kc;

    public PopupKeys(Object prvScreen, KeysCatcher kc, byte add) {
        super(prvScreen, ResourceBundle.getString(add != 2 ? add != 1 ? add != 0 ? "press_paste_key" : "press_key" : "press_del_key" : "press_copy_key"), NativeCanvas.getWidthEx(), 4, 4);
        this.kc = kc;
    }

    public void pointerReleased(int x, int y) {
        Jimm.setDisplay(prvScreen);
    }

    public void doKeyreaction(int keyCode, int type) {
        Jimm.setDisplay(prvScreen);
        kc.keyCatch(keyCode);
    }
}
