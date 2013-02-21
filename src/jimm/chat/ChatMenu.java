package jimm.chat;

import jimm.ui.Menu;

//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
class ChatMenu extends Menu {
    /**
     * @author Rishat Shamsutdinov
     */
    private ChatTextList chat;

    ChatMenu(Menu menu) {
        super(null, false);
        this.chat = (ChatTextList) menu.getPrvScreen();
        MenuItem mi[] = menu.getItems();
        int size = mi.length;
        items = new MenuItem[size];
        System.arraycopy(mi, 0, items, 0, size);
    }

    public void back() {
        chat.hideMenu();
    }

    protected int correctDeltaY(int dY, int height, int topY, int scHeight) {
        return dY - super.correctDeltaY(dY, height, topY, scHeight) + scHeight - chat.getBottom();
    }

    protected void startRepaintTTask() {
    }

    public void invalidate() {
        chat.invalidate();
    }

    public void doKeyreaction(int keyCode, int type) {
        if (type == KEY_RELEASED) {
            return;
        }
        super.doKeyreaction(keyCode, type);
    }
}
//#sijapp cond.end#