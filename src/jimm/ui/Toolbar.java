//#sijapp cond.if modules_TOOLBAR is "true"#
package jimm.ui;

import jimm.comm.Util;
import jimm.Options;
import jimm.JimmUI;
import jimm.util.Device;
import DrawControls.Icon;
import DrawControls.ImageList;

import javax.microedition.lcdui.Graphics;

public class Toolbar {

    final class ToolbarItem {
        Icon icon;
        int x;
        int y;
        int element;

        private ToolbarItem(byte byte0) {
        }

        ToolbarItem() {
            this((byte) 0);
        }
    }

    public static final ImageList toolList = ImageList.loadFull("toolbar.png");
    private final ToolbarItem toolbarItems[] = new ToolbarItem[16];
    private int width;
    private int height;
    private int length;

    public Toolbar() {
        load();
    }

    public final void load() {
        int ints[] = Util.explodeToInt(Options.getString(Options.OPTION_TOOLBAR_HASH), ';');
        length = ints.length;
        for (int k = length - 1; k >= 0; k--) {
            ToolbarItem toolbarItem;
            if ((toolbarItem = toolbarItems[k]) == null) {
                toolbarItem = new ToolbarItem();
                toolbarItems[k] = toolbarItem;
            }
            toolbarItem.element = ints[k];
            int e = toolbarItem.element;
            toolbarItem.icon = toolList.elementAt((e > 0) ? e : -1);
        }

        Toolbar toolbar = this;
        int j = 0;
        for (int l = toolbar.length - 1; l >= 0; l--) {
            ToolbarItem toolbarItem;
            if ((toolbarItem = toolbar.toolbarItems[l]).icon != null)
                j += toolbarItem.icon.getWidth() + 4;
        }
        width = j;
        toolbar = this;
        j = 0;
        for (int i1 = toolbar.length - 1; i1 >= 0; i1--) {
            ToolbarItem toolbarItem;
            if ((toolbarItem = toolbar.toolbarItems[i1]).icon != null) {
                j = Math.max(j, toolbarItem.icon.getHeight() + 4);
            }
        }
        height = j;
    }

    public static String defaultToolbar() {
        StringBuffer stringbuffer;
        (stringbuffer = new StringBuffer()).append("12;8;1;6;19;");
        for (int i = 11; i > 0; i--) {
            stringbuffer.append("0;");
        }

        if (stringbuffer.length() != 0)
            stringbuffer.setLength(stringbuffer.length() - 1);
        return stringbuffer.toString();
    }

    public final void initCoord(int i, int j) {
        for (int k = 0; k < length; k++) {
            ToolbarItem toolbarItem;
            if ((toolbarItem = toolbarItems[k]).icon != null) {
                toolbarItem.x = i + 2;
                toolbarItem.y = j + 2;
                i += toolbarItem.icon.getWidth() + 4;
            }
        }
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final boolean pressedToolbar(int x, int y, boolean flag) {
        for (int k = length - 1; k >= 0; k--) {
            ToolbarItem toolbarItem;
            if ((toolbarItem = toolbarItems[k]).icon != null && x >= toolbarItem.x && y >= toolbarItem.y && x <= toolbarItem.x + toolbarItem.icon.getWidth() && y <= toolbarItem.y + toolbarItem.icon.getHeight()) {
                y = toolbarItem.element;
                //i = k;
                //if (flag)
                //    br.a(i);
                //else
                JimmUI.execHotKeyAction(y);
                return true;
            }
        }
        return false;
    }

    public final void paint(Graphics g) {
        int i = g.getStrokeStyle();
        int iw;
        int ih;
        g.setStrokeStyle(Graphics.DOTTED);
        for (int j = length - 1; j >= 0; j--) {
            ToolbarItem toolbarItem;
            if ((toolbarItem = toolbarItems[j]).icon != null) {
                iw = toolbarItem.icon.getWidth();
                ih = toolbarItem.icon.getHeight();
                g.drawRect(toolbarItem.x - 2, toolbarItem.y - 2, iw + 2, ih + 2);
                //toolbarItem.icon.drawByLeftTop(g, toolbarItem.x, toolbarItem.y);
                toolbarItem.icon.drawInCenter(g, toolbarItem.x + iw / 2, toolbarItem.y + ih / 2);
            }
        }
        g.setStrokeStyle(i);
    }
}
//#sijapp cond.end#