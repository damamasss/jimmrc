package DrawControls;

import jimm.ui.LineChoise;

import javax.microedition.lcdui.Graphics;

/**
 * Created [03.03.2011, 14:20:08]
 * Develop by Lavlinsky Roman on 2011
 */
public class ColorIcon extends Icon {

    private String head;
    private int color;

    public ColorIcon(String head, int color, int w, int h) {
        super(null);
        width = w;
        height = h;
        this.head = head;
        this.color = color;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getHeader() {
        return head;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void drawByLeftTop(Graphics g, int x, int y) {
        if (head != null) {
            int y1 = y + height / 2 - CanvasEx.facade.getFontHeight() / 2;
            CanvasEx.drawString(g, CanvasEx.facade, head, x + height + 2, y1, Graphics.TOP | Graphics.LEFT);
        }
        CanvasEx.drawGradient(g, x, y, height, height, CanvasEx.getInverseColor(color), 16, 16, -32);
        CanvasEx.drawGradient(g, x + 2, y + 2, height - 4, height - 4, color, 16, 0, -64);
    }
}
