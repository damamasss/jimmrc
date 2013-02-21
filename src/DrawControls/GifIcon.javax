/*
 * GifIcon.java
 *
 * Created on 4 Апрель 2008 г., 19:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package DrawControls;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
// #sijapp cond.if modules_GIFSMILES is "true" #

/**
 * @author vladimir
 */
public class GifIcon extends Icon {
    private GifDecoder.GifFrame[] frames;
    private int currentFrame = 0;

    /**
     * Creates a new instance of GifIcon
     */

    protected GifIcon(Image image) {
        super(image, 0, 0, image.getWidth(), image.getHeight());
    }

    public GifIcon(GifDecoder decoder) {
        this(decoder.getImage());
        frames = decoder.getFrames();
        sleepTime = decoder.getDelay(0);
    }

    protected Image getImage() {
        return frames[currentFrame].image;
    }

    public void drawByLeftTop(Graphics g, int x, int y) {
        super.drawByLeftTop(g, x, y);
        painted = true;
    }

    private boolean painted = false;
    private long sleepTime = 0;

    boolean nextFrame(long deltaTime) {
        sleepTime -= deltaTime;
        if (sleepTime <= 0) {
            currentFrame = (currentFrame + 1) % frames.length;
            sleepTime = frames[currentFrame].delay;
            boolean needReepaint = painted;
            painted = false;
            return needReepaint;
        }
        return false;
    }

}
// #sijapp cond.end #
