/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/DrawControls/Icon.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Andreas Rossbacher
 *******************************************************************************/

package DrawControls;


import jimm.Options;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Icon {
    public static int scale;

    private Image image;
    private int x = 0;
    private int y = 0;
    protected int width = 0;
    protected int height = 0;

    public Icon(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Icon(Image image) {
        this(image, 0, 0, (image == null) ? 0 : image.getWidth(), (image == null) ? 0 : image.getHeight());
    }


    protected Image getImage() {
        return image;
    }

    //! Return width of each image
    public int getWidth() {
        return Math.max(width, scale);
    }

    public int emuWidth() {
        return width;
    }

    //! Return hright of each image
    public int getHeight() {
        return Math.max(height, scale);
    }

    public int realHeight() {
        return height;
    }

    public void drawByLeftTop(Graphics g, int x, int y) {
        if (getImage() == null) {
            return;
        }
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipHeight = g.getClipHeight();
        int clipWidth = g.getClipWidth();
        int iy = y - this.y;
        int ix = x - this.x;
        g.clipRect(x, y, width, height);
        g.drawImage(getImage(), ix, iy, Graphics.TOP | Graphics.LEFT);
        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    public void drawInCenter(Graphics g, int x, int y) {
        drawByLeftTop(g, x - width / 2, y - height / 2);
    }

    public void drawInVCenter(Graphics g, int x, int y) {
        drawByLeftTop(g, x + ((getWidth() - width) / 2), y - height / 2);
    }

    public void drawByRightBottom(Graphics g, int x, int y) {
        drawByLeftTop(g, x - width, y - height);
    }

    public void drawByRightTop(Graphics g, int x, int y) {
        drawByLeftTop(g, x - width, y);
    }

    public void drawByLeftBottom(Graphics g, int x, int y) {
        drawByLeftTop(g, x + ((getWidth() - width) / 2), y - height);
    }

    public void drawInCenterByRight(Graphics g, int x, int y) {
        drawByLeftTop(g, x - getWidth() / 2, y - height / 2);
    }

    public Image getImg() {
        Image result = null;
        try {
            Image image = Image.createImage(this.image, x, y, width, height, javax.microedition.lcdui.game.Sprite.TRANS_NONE);
            if (image != null) {
                result = Image.createImage(image);
            }
        } catch (Exception ignored) {
        }
        return result;
    }
}