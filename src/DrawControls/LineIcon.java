package DrawControls;

import jimm.ui.LineChoise;
import jimm.util.Device;

import javax.microedition.lcdui.Graphics;

public class LineIcon extends Icon {

    private static final ImageList imageListLine = ImageList.loadFull("line.png");

    private LineChoise _this;

    public LineIcon(LineChoise line, int w, int h) {
        super(null);
        width = w;
        height = h;
        setLineListener(line);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void drawByLeftTop(Graphics g, int x, int y) {
        if (_this == null) {
            return;
        }
        boolean alltext = (imageListLine == null || imageListLine.size() == 0);
        int current = _this.getSelected();
        int max = _this.getLimited();
//        int clipX = g.getClipX(); // todo need?
//        int clipY = g.getClipY();
//        int clipHeight = g.getClipHeight();
//        int clipWidth = g.getClipWidth();
//        g.clipRect(x, y, width, height);
        if (_this.getLabel() != null) {
            //int x1 = x + width / 2;
            int y1 = y + height / 4 - CanvasEx.facade.getFontHeight() / 2;
            String itm = _this.getLabel();
            CanvasEx.drawString(g, CanvasEx.facade, itm, x, y1, Graphics.TOP | Graphics.LEFT);
        }

        if (_this.getImageList() != null) {
            Icon icon = _this.getImageList().elementAt(_this.getSelected());
            if (icon != null) {
                icon.drawInCenter(g, x + width / 2, y + 3 * height / 4);
            }
        } else if (_this.getItems() != null) {
            int x1 = x + width / 2;
            int y1 = y + 3 * height / 4 - CanvasEx.facade.getFontHeight() / 2;
            String itm = _this.getItems()[_this.getSelected()];
            CanvasEx.drawString(g, CanvasEx.facade, itm, x1, y1, Graphics.TOP | Graphics.HCENTER);
        }

        if (current > 0) {
            if (!alltext) {
                Icon icon = imageListLine.elementAt(0);
                icon.drawByLeftTop(g, x, y + 3 * height / 4 - icon.realHeight() / 2);
            } else {
                CanvasEx.drawString(g, CanvasEx.facade, "<", x, y + 3 * height / 4 - CanvasEx.facade.getFontHeight() / 2, Graphics.TOP | Graphics.LEFT);
            }
        }
        if (current < max - 1) {
            if (!alltext) {
                Icon icon = imageListLine.elementAt(1);
                icon.drawByLeftTop(g, x + width - icon.emuWidth(), y + 3 * height / 4 - icon.realHeight() / 2);
            } else {
                CanvasEx.drawString(g, CanvasEx.facade, ">", x + width, y + 3 * height / 4 - CanvasEx.facade.getFontHeight() / 2, Graphics.TOP | Graphics.RIGHT);
            }
        }
        //g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    public void setLineListener(LineChoise line) {
        _this = line;
    }

    public void action(boolean up) {
        if (_this != null) {
            _this.lineAction(up);
        }
    }

    public void pPressed(int x) {
        action(x > NativeCanvas.getWidthEx() / 2);
    }
}