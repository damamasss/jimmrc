package DrawControls;

import javax.microedition.lcdui.Graphics;


public class GaugeIcon extends Icon {
    /**
     * @author Rishat Shamsutdinov
     */

    public int position;
    public int step;
    private int maxValue;
    private String label;

    public GaugeIcon(int w, int h, int pos, int max, String label) {
        super(null);
        this.label = label;
        position = 0;
        step = 0;
        maxValue = 0;
        height = h;
        position = pos;
        maxValue = max;
        updateWidth(w);
    }

    public final void updateWidth(int w) {
        width = w;
        step = (w * 10000) / (maxValue + 1);
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public int getPosition() {
        return position;
    }

    public String getLabel() {
        return label;
    }

    public final void drawByLeftTop(Graphics g, int x, int y) {
        if (label != null) {
            int y1 = y + height / 4 - CanvasEx.facade.getFontHeight() / 2;
            CanvasEx.drawString(g, CanvasEx.facade, label, x, y1, Graphics.TOP | Graphics.LEFT);
        }
        int iy = y + 3 * height / 4;
        CanvasEx.drawGlassRect(g, CanvasEx.getColor(CanvasEx.COLOR_CAP), x, iy - 2, x + width, iy + 2);
        //String value = "[" + position + "/" + maxValue + "]";
        //int l = CanvasEx.facade.stringWidth(value) / 2;
        int i1 = x + (position * step) / 10000;
        CanvasEx.drawGlassRect(g, CanvasEx.getColor(CanvasEx.COLOR_CAP), i1, y + height / 2, (i1 * 10000 + step) / 10000, y + height);
        //g.setColor(CanvasEx.getColor(CanvasEx.COLOR_TEXT));
        //CanvasEx.drawString(g, CanvasEx.facade, value, Math.max(x + l, Math.min((x + width) - l, (i1 * 10000 + step / 2) / 10000)), y + height, Graphics.BOTTOM | Graphics.HCENTER);
    }

    public final void setPosition(int ix) {
        position = java.lang.Math.max(0, java.lang.Math.min(ix, maxValue));
    }

    public void pPressed(int x) {
        setPosition(10000 * x / step);
    }
}