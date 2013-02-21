package DrawControls;

import javax.microedition.lcdui.Graphics;

/**
 * Created by IntelliJ IDEA.
 * User: Рома
 * Date: 10.02.2011
 * Time: 17:11:25
 * To change this template use File | Settings | File Templates.
 */
public class TextIcon extends Icon{

    Icon icon;
    String title;
    String general;
    String show;

    public TextIcon(Icon i, String t, String g, String s, int w, int h) {
        super(null);
        width = w;
        height = h;
        title = t;
        icon = i;
        general = g;
        show = s;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getGeneral() {
        return general;
    }

    public String getShow() {
        return show;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void drawByLeftTop(Graphics g, int x, int y) {
        //if (general == null && show == null/* && icon == null*/) {
        //    return;
        //}
//        int clipX = g.getClipX(); // todo need?
//        int clipY = g.getClipY();
//        int clipHeight = g.getClipHeight();
//        int clipWidth = g.getClipWidth();
//        g.clipRect(x, y, width, height);
        if (title != null) {
            //int x1 = x + width / 2;
            int y1 = y + height / 4 - CanvasEx.facade.getFontHeight() / 2;
            CanvasEx.drawString(g, CanvasEx.facade, title, x, y1, Graphics.TOP | Graphics.LEFT);
        }
        int off = 4;
        if (icon != null) {
            icon.drawByLeftTop(g, x, y + 3 * height / 4 - icon.realHeight() / 2);
            off+=icon.emuWidth();
        }
        if (show != null) {
            //int x1 = x + width / 2;
            int y1 = y + 3 * height / 4 - CanvasEx.facade.getFontHeight() / 2;
            CanvasEx.drawString(g, CanvasEx.facade, show, x + off, y1, Graphics.TOP | Graphics.LEFT);
        }
        //g.setClip(clipX, clipY, clipWidth, clipHeight);
    }
}
