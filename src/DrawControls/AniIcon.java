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
// #sijapp cond.if modules_ANISMILES is "true" #

/**
 * @author vladimir
 */
public class AniIcon extends Icon {
    private Icon[] frames;
    private int[] delays;
    private int currentFrame = 0;

    /**
     * Creates a new instance of GifIcon
     */

    public AniIcon(Icon icon, int frameCount) {
        super(icon.getImage(), 0, 0, icon.getWidth(), icon.getHeight());
        frames = new Icon[frameCount];
        delays = new int[frameCount];
    }

    protected Image getImage() {
        return frames[currentFrame].getImage();
    }

    void addFrame(int num, Icon icon, int dalay) {
        frames[num] = icon;
        delays[num] = dalay;
    }

    public void drawByLeftTop(Graphics g, int x, int y) {
        frames[currentFrame].drawByLeftTop(g, x, y);
        //frames[currentFrame].drawInVCenter(g, x, y + frames[currentFrame].getHeight()/2);
        painted = true;
    }

    public void drawInCenter(Graphics g, int x, int y) {
        frames[currentFrame].drawInCenter(g, x, y);
        painted = true;
    }

    public void drawInVCenter(Graphics g, int x, int y) {
        frames[currentFrame].drawInVCenter(g, x, y);
        painted = true;
    }

    private boolean painted = false;
    private long sleepTime = 0;

    boolean nextFrame(long deltaTime) {
        sleepTime -= deltaTime;
        if (sleepTime <= 0) {
            currentFrame = (currentFrame + 1) % frames.length;
            sleepTime = delays[currentFrame];
            boolean needReepaint = painted;
            painted = false;
            return needReepaint;
        }
        return false;
    }

}
// #sijapp cond.end #
