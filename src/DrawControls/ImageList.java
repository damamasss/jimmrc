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
 File: src/DrawControls/ImageList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Andreas Rossbacher
 *******************************************************************************/

package DrawControls;

import jimm.Options;

import javax.microedition.lcdui.Image;
import java.io.IOException;
import java.util.Vector;

//! Class for dividing one big image to several small with equal size
/*!
    This class allow you to reduce images number, stored at res folder.
    It can be uses only if all images have equal height and width.
    For example, if you want use 10 images with size 16 x 16, you can
    store one 160 x 16 image and divide it with help of this class

    \par Example
    \code
    ImageList images = new ImageList();
    images.load("/big160x16.png", 16);

    // now you can retrive second image:
    Image img1 = images.iconAt(1);

    \endcode
*/

public class ImageList {

    private Icon[] icons;
    protected int width = 0;
    protected int height = 0;

    //! Return image by index
    public Icon elementAt(int index) { //!< Index of requested image in the list
        if ((index >= 0) && (index < size())) {
            return icons[index];
        }
        return null;
    }

    //! Add image by icon
    public void addElementAt(Icon icon) {
        Vector tmp = new Vector();
        for (int i = 0; i < icons.length; i++) {
            tmp.addElement(icons[i]);
        }

        tmp.addElement(icon);
        icons = null;
        icons = new Icon[tmp.size()];
        tmp.copyInto(icons);
    }

    //! Return number of stored images
    public int size() {
        return icons != null ? icons.length : 0;
    }

    //! Return width of each image
    public int getWidth() {
        return width;
    }

    //! Return hright of each image
    public int getHeight() {
        return Math.max(height, Icon.scale);
    }

    public void load(String resName, int count) throws IOException {
        Image resImage = Image.createImage(resName);
        int imgHeight = resImage.getHeight();
        int imgWidth = resImage.getWidth();
        width = imgWidth / count;
        height = imgHeight;

        Vector tmpIcons = new Vector();
        Icon icon;
        for (int y = 0; y < imgHeight; y += height) {
            for (int x = 0; x < imgWidth; x += width) {
                icon = new Icon(resImage, x, y, width, height);
                tmpIcons.addElement(icon);
            }
        }
        icons = new Icon[tmpIcons.size()];
        tmpIcons.copyInto(icons);
    }

    //! Load and divide big image to several small and store it in object
    public void load(String resName, int width, int height) throws IOException {
//#sijapp cond.if modules_FILES is "true"#
        Image resImage = (resName.lastIndexOf('/') == 0) ? Image.createImage(resName) : jimm.Jimm.jimm.loadImageFromFS(resName);
//#sijapp cond.else#
//#		Image resImage = Image.createImage(resName);
//#sijapp cond.end#
        if (resImage == null) {
            throw new IOException();
        }
        int imgHeight = resImage.getHeight();
        int imgWidth = resImage.getWidth();

        if (width == -1) {
            width = Math.min(imgHeight, imgWidth);
        }
        if (height == -1) {
            height = imgHeight;
        }

        this.width = width;
        this.height = height;

        Vector tmpIcons = new Vector();
        Icon icon;
        for (int y = 0; y < imgHeight; y += height) {
            for (int x = 0; x < imgWidth; x += width) {
                icon = new Icon(resImage, x, y, width, height);
                tmpIcons.addElement(icon);
            }
        }
        icons = new Icon[tmpIcons.size()];
        tmpIcons.copyInto(icons);
    }

/*    public void reload(String resName, int height) throws IOException {
//#sijapp cond.if modules_FILES is "true"#
        Image resImage = (resName.lastIndexOf('/') == 0) ? Image.createImage(resName) : jimm.Jimm.jimm.loadImageFromFS(resName);
//#sijapp cond.else#
//#		Image resImage = Image.createImage(resName);
//#sijapp cond.end#
        if (resImage == null) {
            throw new IOException();
        }
        int imgHeight = resImage.getHeight();
        int imgWidth = resImage.getWidth();
        int step = 100*height/imgHeight;
        imgWidth = step*imgWidth/100;
        try{
            resImage = CanvasEx.resizeImage(resImage, imgWidth, imgHeight, true, -1, -1);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError oome) {
            oome.printStackTrace();
        }

        if (width == -1) {
            width = Math.min(imgHeight, imgWidth);
        }
        if (height == -1) {
            height = imgHeight;
        }

        this.width = width;
        this.height = height;

        Vector tmpIcons = new Vector();
        Icon icon;
        for (int y = 0; y < imgHeight; y += height) {
            for (int x = 0; x < imgWidth; x += width) {
                icon = new Icon(resImage, x, y,width, height);
                tmpIcons.addElement(icon);
            }
        }
        icons = new Icon[tmpIcons.size()];
        tmpIcons.copyInto(icons);
    }*/

    public void init(Icon i) {
        icons = new Icon[]{i};
    }

    static public ImageList load(String resName) {
        ImageList icons = new ImageList();
        try {
            icons.load(resName, -1, -1);
        } catch (Exception ignored) {
        }
        return icons;
    }

    public static ImageList loadFull(String path) {
        String prefix = jimm.Options.getString(jimm.Options.OPTION_ICONS_PREFIX);
        ImageList imageList = load(prefix + path);
        if (!prefix.equals("/")) {
            if (imageList.size() == 0) {
                imageList = ImageList.load("/" + path);
            }
        }
        return imageList;
    }

//    static public Image loadImage(String resName) {
//        try {
//            return Image.createImage(resName);
//        } catch (Exception e) {}
//        return null;
//    }
}