//package DrawControls;
//
//import javax.microedition.lcdui.Graphics;
//
//public class StringIcon extends Icon {
//
//    private String string;
//    private byte act;
//
//    public StringIcon(String t, int w, int h, byte a) {
//        super(null);
//        width = w;
//        height = h;
//        string = t;
//        act = a;
//    }
//
//    public int getWidth() {
//        return width;
//    }
//
//    public int getHeight() {
//        return height;
//    }
//
//    public String getString() {
//        return string;
//    }
//
//    public byte getAct() {
//        return act;
//    }
//
//    public void drawByLeftTop(Graphics g, int x, int y) {
////        int clipX = g.getClipX();
////        int clipY = g.getClipY();
////        int clipHeight = g.getClipHeight();
////        int clipWidth = g.getClipWidth();
////        g.clipRect(x, y, width, height);
////        if (icon != null) {
////            int xi = x + (width - icon.getWidth()) / 2;
////            icon.drawInVCenter(g, xi, (2 * y + icon.getHeight() + 2) / 2);
////        }
//        if (string != null) {
//            int x1 = x + width / 2;
//            int y1 = y + height / 2 - CanvasEx.facade.getFontHeight() / 2;
//            CanvasEx.drawString(g, CanvasEx.facade, string, x1, y1, Graphics.TOP | Graphics.HCENTER);
//        }
//        //g.setClip(clipX, clipY, clipWidth, clipHeight);
//    }
//}