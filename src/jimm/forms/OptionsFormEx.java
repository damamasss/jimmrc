//package jimm.forms;
//
//import jimm.JimmUI;
//import jimm.Jimm;
//import jimm.Options;
//import jimm.ui.MenuListener;
//import jimm.ui.SliderTask;
//
//import javax.microedition.lcdui.*;
//
//import DrawControls.*;
//
//public class OptionsFormEx extends FormEx implements CommandListener, VirtualListCommands {
//
//    private MenuListener menuListener;
//    private CanvasEx prevScreen;
//
//    public OptionsFormEx(CanvasEx screen, String cap) {
//        super(cap, JimmUI.cmdOk, JimmUI.cmdBack);
//        removeCommand(JimmUI.cmdOk);
//        prevScreen = screen;
//        setCommandListener(this);
//        setVLCommands(this);
//    }
//
//    public CanvasEx getPrvScreen() {
//        return null;
//    }
//
//    public void setPrvScreen(CanvasEx screen) {
//        prevScreen = screen;
//    }
//
//    public void back() {
//        //System.out.println("back");
//        CanvasEx screen = prevScreen;
//        if (screen == null) {
//            screen = Jimm.getContactList();
//        }
//        //super.slide(screen);
//        if (Options.getBoolean(Options.OPTION_ANIMATION)) {
//            Jimm.setDisplay(new SliderTask(this, screen, screen, 0, 0, NativeCanvas.getHeightEx(), 0, 0, 0));
//        } else {
//            Jimm.setDisplay(prevScreen);
//        }
//    }
//
//    public void setMenuListener(MenuListener ml) {
//        menuListener = ml;
//    }
//
//    public void append(String item, byte act) {
//        addNode(null, new FormItem(item, null, new StringIcon(item, getDrawWidth() * 19 / 20, vtGetItemHeight(), act)));
//    }
//
//    public byte getCurrAction() {
//        TreeNode node = getCurrentItem();
//        if (node == null) {
//            return -1;
//        }
//        Object data = node.getData();
//        if (data == null) {
//            return -1;
//        }
//        if (data instanceof FormItem) {
//            FormItem fi = (FormItem) data;
//             if (fi.item instanceof String) {
//                StringIcon si = (StringIcon) fi.icon;
//                return si.getAct();
//             }
//        }
//        return -1;
//    }
//
//    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
//        super.vlKeyPress(sender, keyCode, type, gameAct);
//    }
//
//    public void vlCursorMoved(VirtualList sender) {
//    }
//
//    public void vlItemClicked(VirtualList sender) {
//        TreeNode node = getCurrentItem();
//        if (node == null) {
//            return;
//        }
//        Object data = node.getData();
//        if (data == null) {
//            return;
//        }
//        if (data instanceof FormItem) {
//            FormItem fi = (FormItem) data;
//             if (fi.item instanceof String) {
//                StringIcon si = (StringIcon) fi.icon;
//                //System.out.println("si.getAct = " + si.getAct());
//                if (menuListener != null) {
//                    menuListener.menuSelect(null, si.getAct());
//                }
//            }
//        }
//    }
//
//    public void commandAction(Command c, Displayable d) {
//        if (c.equals(JimmUI.cmdBack)) {
//            back();
//        }
//    }
//}
