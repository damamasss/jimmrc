/*
 * Copyright (c) 2005-2009 Sergey Tkachev http://sergetk.net
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
//#sijapp cond.if modules_GFONT="true" #
package DrawControls;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>BitmapFont allows developers to use his own fonts in mobile applications.</p>
 * <p/>
 * <p>This class includes a mixed set of methods of Graphics and Font classes.
 * It has character measurement methods from Font, such as stringWidth() and charWidth().
 * It also has text drawing methods from Graphics, such as drawString() and drawChar().
 * Text will be drawn by current Graphics color.</p>
 * <p/>
 * <p>You may create your own font using Bitmap Character Editor. This is crossplatform
 * desktop application written on Java using SWT library. It can be downloaded free from project site.</p>
 * <p/>
 * <p>Each bitmap font consists of a set of parameters such
 * as height, baseline position etc, of character code map, of array width character widthes
 * and of principal part: one or more images in PNG format store character outlines.</p>
 * <p/>
 * <p>By default all fonts are considered normal. Bold, italic and bold italic styles are generated
 * programmaticaly.</p>
 * <p/>
 * <p>This code is a part of the <a href="http://sourceforge.net/projects/mobilefonts">Mobile Fonts Project</a>.</p>
 * <p/>
 * <p>Note: if this code was useful to you, write me please. I will be proud :)</p>
 *
 * @author Sergey Tkachev <a href="http://sergetk.net">http://sergetk.net</a>
 */
public class BitmapFont {
    private final static int DEFAULT_COLOR_CACHE_CAPACITY = 5;

    private String imageName;
    private Image[] baseImages;
    private Image[] currentImages;

    private int height;
    private int baseline;
    private int xIndent;
    private int yIndent;
    private int spaceWidth;

    private int style;
    private int currentColor;
    private int charWidthIncrement = 0;
    private int imagesOffset = 0;

    private String characterMap;
    private int[] widthes, x, y, idx;

    private int colorUsageCount;
    private int[] colorUsage;
    private int[] colorCache;
    private Image[][] imageCache;

    private boolean italic;
    private boolean bold;

    private static BitmapFont defaultFont;

    /**
     * Gets the default font
     *
     * @return the default font
     */
    public static BitmapFont getDefault() {
        return defaultFont;
    }

    /**
     * Set a font as default
     *
     * @param font the font
     */
    public static void setDefault(BitmapFont font) {
        defaultFont = font;
    }

    private BitmapFont(BitmapFont font, int style) {
        this.imageName = font.imageName;
        this.currentImages = this.baseImages = font.baseImages;

        this.height = font.height;
        this.baseline = font.baseline;
        this.xIndent = font.xIndent;
        this.yIndent = font.yIndent;
        this.spaceWidth = font.spaceWidth;

        this.style = style;
        this.italic = (style & Font.STYLE_ITALIC) != 0;
        this.bold = (style & Font.STYLE_BOLD) != 0;
        this.currentColor = 0;
        this.imagesOffset = font.imagesOffset;

        this.characterMap = font.characterMap;
        this.widthes = font.widthes;
        this.x = font.x;
        this.y = font.y;
        this.idx = font.idx;

        this.colorUsageCount = font.colorUsageCount;
        this.colorUsage = font.colorUsage;
        this.colorCache = font.colorCache;
        this.imageCache = font.imageCache;

        this.charWidthIncrement = bold ? 1 : 0;
    }

    /**
     * Creates a new font from the resource.
     *
     * @param fontName the resource name
     * @noinspection SameParameterValue
     */
    public BitmapFont(String fontName) {
        this(fontName, DEFAULT_COLOR_CACHE_CAPACITY);
    }

    /**
     * Creates a new font from the resource. The capacity of the color cache defines
     * maximum size of the color cache.
     *
     * @param fontName           the resource name
     * @param colorCacheCapacity the maximum color cache size
     * @noinspection SameParameterValue
     */
    private BitmapFont(String fontName, int colorCacheCapacity) {
        this.style = Font.STYLE_PLAIN;
        this.currentColor = 0;

        try {
            InputStream input = (new Object()).getClass().getResourceAsStream(fontName);
            if (input == null) {
                throw new IOException();
            }
            DataInputStream data = new DataInputStream(input);

            imagesOffset = data.available() - 1;
            this.imageName = fontName;

            byte version = data.readByte();
            this.height = data.readByte();
            this.baseline = data.readByte();
            this.xIndent = data.readByte();
            this.yIndent = data.readByte();
            this.spaceWidth = data.readByte();

            characterMap = data.readUTF();

            int count = characterMap.length();

            // read characters widthes
            this.widthes = new int[count];
            this.x = new int[count];
            this.y = new int[count];
            this.idx = new int[count];

            for (int i = 0; i < count; i++) {
                widthes[i] = data.readByte();
            }

            // read font images
            int imagesCount = data.readByte();
            baseImages = new Image[imagesCount];

            for (int i = 0; i < imagesCount; i++) {
                int imageLength = data.readShort();
                byte[] buffer = new byte[imageLength];
                data.read(buffer, 0, imageLength);
                baseImages[i] = Image.createImage(buffer, 0, imageLength);

                imagesOffset -= imageLength + 2;
            }

            currentImages = baseImages;

            // calculate characters coordinates
            int curX = 0, curY = 0, curIdx = 0;
            int curImageWidth = currentImages[0].getWidth();
            int curImageHeight = currentImages[0].getHeight();

            for (int i = 0; i < count; i++) {
                if (widthes[i] < 0) {
                    // negative width points to another character
                    int sourceIndex = -widthes[i];
                    widthes[i] = widthes[sourceIndex];
                    x[i] = x[sourceIndex];
                    y[i] = y[sourceIndex];
                    idx[i] = idx[sourceIndex];
                } else {
                    if (curX + widthes[i] > curImageWidth) {
                        curX = 0;
                        curY += height;
                        if (curY > curImageHeight) {
                            curY = 0;
                            curIdx++;
                            curImageWidth = currentImages[curIdx].getWidth();
                            curImageHeight = currentImages[curIdx].getHeight();
                        }
                    }

                    x[i] = curX;
                    y[i] = curY;
                    idx[i] = curIdx;
                    curX += widthes[i];
                }
            }
            colorCache = new int[colorCacheCapacity];
            colorUsage = new int[colorCacheCapacity];
            imageCache = new Image[colorCacheCapacity][];

            if (defaultFont == null)
                defaultFont = this;
        } catch (IOException ignored) {
        }
    }

    /**
     * Creates a new font instance with a new style with minimal memory consumption
     *
     * @param style the style of the font
     * @return the font
     */
    public BitmapFont getFont(int style) {
        return new BitmapFont(this, style);
    }

    private void setColor(int color) {
        color &= 0x00FFFFFF;
        if (this.currentColor == color) {
            return;
        }

        this.currentColor = color;
        if (color == 0x00000000) { // new color is black
            this.currentImages = this.baseImages;
        } else {
            int cacheItemIndex = 0;
            int minUsage = Integer.MAX_VALUE;
            for (int i = 0; i < colorCache.length; i++) {
                if (colorCache[i] == color) {
                    // the color is already in the cache
                    currentImages = imageCache[i];
                    colorUsage[i] = colorUsageCount++;
                    return;
                } else if (colorCache[i] == 0) {
                    // cache item is empty
                    cacheItemIndex = i;
                    break;
                } else if (colorUsage[i] < minUsage) {
                    minUsage = colorUsage[i];
                    cacheItemIndex = i;
                }
            }
            Image[] images = getColorizedImages(this.imageName, this.imagesOffset, color);
            colorCache[cacheItemIndex] = color;
            imageCache[cacheItemIndex] = images;
            colorUsage[cacheItemIndex] = colorUsageCount++;
            this.currentImages = images;
        }
    }

    /**
     * Gets the style of the font.
     *
     * @return style
     */
    public int getStyle() {
        return this.style;
    }

    /**
     * Gets the standard height of a line of a text in this font.
     *
     * @return the height in pixels
     */
    public int getHeight() {
        return height + yIndent;
    }

    /**
     * Gets the index of the character.
     *
     * @param c the character
     * @return the index of the character
     */
    private int charIndex(char c) {
        try {
            return characterMap.indexOf(c);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     * Gets the distance from the top of the text to the text baseline.
     *
     * @return the baseline position in pixels
     */
    private int getBaselinePosition() {
        return baseline;
    }

    /**
     * Draws the specified string.
     *
     * @param g       the graphics context
     * @param text    the text to be drawn
     * @param x       the x coordinate of the anchor point
     * @param y       the y coordinate of the anchor point
     * @param anchors the anchor point for positioning of the text
     * @return the x coordinate for the next string
     */
    public int drawString(Graphics g, String text, int x, int y, int anchors) {
        return drawSubstring(g, text, 0, text.length(), x, y, anchors);
    }

    /**
     * Draws the specified substring.
     *
     * @param g       the graphics context
     * @param text    the text to be drawn
     * @param offset  the index of a first character
     * @param length  the number of characters
     * @param x       the x coordinate of the anchor point
     * @param y       the y coordinate of the anchor point
     * @param anchors the anchor point for positioning the text
     * @return the x coordinate for the next string
     */
    public int drawSubstring(Graphics g, String text, int offset, int length, int x, int y, int anchors) {
        int xx = getX(substringWidth(text, offset, length), x, anchors);
        int yy = getY(y, anchors);
        setColor(g.getColor());
        for (int i = offset; i < offset + length; i++) {
            xx = drawOneChar(g, text.charAt(i), xx, yy);
        }
        if ((style & Font.STYLE_UNDERLINED) != 0) {
            int yU = y + this.baseline + 2;
            g.drawLine(x, yU, xx - 1, yU);
        }
        return xx;
    }

    private int getX(int w, int x, int anchors) {
        if ((anchors & Graphics.RIGHT) != 0) {
            return x - w;
        } else if ((anchors & Graphics.HCENTER) != 0) {
            return x - w / 2;
        }
        return x;
    }

    private int getY(int y, int anchors) {
        if ((anchors & Graphics.BOTTOM) != 0) {
            return y - height;
        } else if ((anchors & Graphics.VCENTER) != 0) {
            return y - height / 2;
        } else if ((anchors & Graphics.BASELINE) != 0) {
            return y - this.getBaselinePosition();
        }
        return y;
    }

    /**
     * Draws the specified character.
     *
     * @param g the graphics context
     * @param c the character to be drawn
     * @param x the x coordinate of the anchor point
     * @param y the y coordinate of the anchor point
     * @return the x coordinate for the next character
     */
    public int drawChar(Graphics g, char c, int x, int y) {
        setColor(g.getColor());
        int nextX = drawOneChar(g, c, x, y);
        if ((style & Font.STYLE_UNDERLINED) != 0) {
            int yU = y + this.baseline + 2;
            g.drawLine(x, yU, nextX - 1, yU);
        }
        return nextX;
    }

    /**
     * Draws one character. It called from drawChar(), drawString() and drawSubstrung().
     *
     * @param g the graphics context
     * @param c the character to be drawn
     * @param x the x coordinate of the anchor point
     * @param y the y coordinate of the anchor point
     * @return the x coordinate for the next character
     */
    private int drawOneChar(Graphics g, char c, int x, int y) {
        // skip if it is a space
        if (c == ' ') {
            return x + this.spaceWidth + xIndent + charWidthIncrement;
        }
        int charIndex = charIndex(c);
        // draw the unknown character as a rectangle
        if (charIndex < 0) {
            int squareWidth = this.spaceWidth + xIndent + charWidthIncrement;
            g.drawRect(x, y, squareWidth - 1, height - 1);
            return x + squareWidth;
        }

        int charX = this.x[charIndex];
        int charY = this.y[charIndex];
        int cw = widthes[charIndex];
        int imageIndex = idx[charIndex];

        y += yIndent / 2;

        Image image = this.currentImages[imageIndex];

        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        int ix = x - charX;
        int iy = y - charY;

        if (!italic && !bold) {
            g.clipRect(x, y, cw, this.height);
            g.drawImage(image, ix, iy, Graphics.LEFT | Graphics.TOP);
        } else if (italic & bold) {
            int halfHeight = height / 2;
            g.clipRect(x + 1, y, cw, this.height);
            g.drawImage(image, ix + 1, iy, Graphics.LEFT | Graphics.TOP);
            g.setClip(clipX, clipY, clipWidth, clipHeight);
            g.clipRect(x + 2, y, cw, halfHeight);
            g.drawImage(image, ix + 2, iy, Graphics.LEFT | Graphics.TOP);
            g.setClip(clipX, clipY, clipWidth, clipHeight);
            g.clipRect(x, y + halfHeight, cw, height - halfHeight);
            g.drawImage(image, ix, iy, Graphics.LEFT | Graphics.TOP);
        } else if (italic) {
            int halfHeight = height / 2;
            g.clipRect(x + 1, y, cw, halfHeight);
            g.drawImage(image, ix + 1, iy, Graphics.LEFT | Graphics.TOP);
            g.setClip(clipX, clipY, clipWidth, clipHeight);
            g.clipRect(x, y + halfHeight, cw, height - halfHeight);
            g.drawImage(image, ix, iy, Graphics.LEFT | Graphics.TOP);
        } else { // just a bold
            g.clipRect(x, y, cw, this.height);
            g.drawImage(image, ix, iy, Graphics.LEFT | Graphics.TOP);
            g.setClip(clipX, clipY, clipWidth, clipHeight);
            g.clipRect(x + 1, y, cw, this.height);
            g.drawImage(image, ix + 1, iy, Graphics.LEFT | Graphics.TOP);
        }
        // restore clipping
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        return x + cw + xIndent + charWidthIncrement;
    }

    /**
     * Draws the specified characters.
     *
     * @param g       the graphics context
     * @param data    the array of characters to be drawn
     * @param offset  the start offset in the data
     * @param length  the number of characters to be drawn
     * @param x       the x coordinate of the anchor point
     * @param y       the y coordinate of the anchor point
     * @param anchors the anchor point for positioning the text
     * @return the x coordinate for the next character
     */
    public int drawChars(Graphics g, char[] data, int offset, int length, int x, int y, int anchors) {
        int xx = getX(charsWidth(data, offset, length), x, anchors);
        int yy = getY(y, anchors);
        setColor(g.getColor());
        for (int i = offset; i < offset + length; i++) {
            xx = drawOneChar(g, data[i], xx, yy);
        }
        if ((style & Font.STYLE_UNDERLINED) != 0) {
            int yU = y + this.baseline + 2;
            g.drawLine(x, yU, xx - 1, yU);
        }
        return xx;
    }

    /* ================= Character measurement functions =============== */

    /**
     * Gets the width of the specified character in this font.
     *
     * @param c the character to be measured
     * @return the width of the character
     */
    public int charWidth(char c) {
        if (c == ' ') {
            return spaceWidth + xIndent + charWidthIncrement;
        }
        int index = charIndex(c);
        if (index < 0) {
            return spaceWidth + xIndent + charWidthIncrement;
        } else {
            return widthes[index] + xIndent + charWidthIncrement;
        }
    }

    /**
     * Gets the width of the characters, starting at the specified offset
     * and for the specified number of characters (length).
     *
     * @param ch     the array of characters
     * @param offset zero-based index of a first character
     * @param length the number of characters to measure
     * @return the width in pixels
     */
    public int charsWidth(char[] ch, int offset, int length) {
        int w = 0;
        for (int i = offset; i < offset + length; i++) {
            w += charWidth(ch[i]);
        }
        return w;
    }

    /**
     * Gets the width of the string.
     *
     * @param str the String to be measured
     * @return the width in pixels
     */
    public int stringWidth(String str) {
        return substringWidth(str, 0, str.length());
    }

    /**
     * Gets the width of the substring.
     *
     * @param str    the string to be measured
     * @param offset zero-based index of a first character in the substring
     * @param length the number of characters to measure
     * @return the length of the substring
     */
    public int substringWidth(String str, int offset, int length) {
        int w = 0;
        for (int i = offset; i < offset + length; i++) {
            w += charWidth(str.charAt(i));
        }
        return w;
    }

    /* ================= Working with the PNG =============== */

    private static final String PNG_SIGNATURE = "\u0089PNG\r\n\u001A\n";

    /**
     * Loads images from the resource and replace palette chunks.
     *
     * @param name  the name of the resource containing the image data in the PNG format
     * @param color the color
     * @param skip
     * @param skip
     * @return the created image
     */
    private Image[] getColorizedImages(String name, int skip, int color) {
        InputStream inputStream = getClass().getResourceAsStream(name);
        DataInputStream dataStream = new DataInputStream(inputStream);

        Image[] images = null;

        try {
            dataStream.skip(skip);
            int imagesCount = dataStream.readByte();
            images = new Image[imagesCount];

            for (int i = 0; i < imagesCount; i++) {
                int imageLength = dataStream.readShort();
                byte[] buffer = new byte[imageLength];
                dataStream.read(buffer, 0, imageLength);

                if (!compareBytes(buffer, 0, PNG_SIGNATURE)) {
                    return null;
                }

                int paletteOffset = getChunk(buffer, 8, "PLTE");
                if (paletteOffset >= 0) {
                    colorizePalette(buffer, paletteOffset, color);
                    images[i] = Image.createImage(buffer, 0, imageLength);
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }

        return images;
    }

    /**
     * Finds the specified chunk.
     *
     * @param buffer the byte array
     * @param offset the offset of the start of the data in the array
     * @param chunk  the name of chunk (i.e. PLTE)
     * @return the offset of chunk (-1 if chunk isn't present)
     * @noinspection SameParameterValue
     */
    private int getChunk(byte[] buffer, int offset, String chunk) {
        try {
            for (; ;) {
                int dataLenght = getInt(buffer, offset);
                if (compareBytes(buffer, offset + 4, chunk)) {
                    return offset;
                } else {
                    offset += 4 + 4 + dataLenght + 4;
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    /**
     * Compare byte sequence with string
     *
     * @param buffer the byte array
     * @param offset the offset of the start of the data in the array
     * @param str    the string to compare with bytes in the buffer
     * @return true if the buffer contains the string
     */
    private boolean compareBytes(byte[] buffer, int offset, String str) {
        for (int i = 0; i < str.length(); i++) {
            if (((byte) (str.charAt(i))) != buffer[i + offset]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the integer value from the four bytes. The most signified byte go first.
     *
     * @param buffer the byte array
     * @param offset the offset of the start of the data in the array
     * @return the integer value
     */
    private int getInt(byte[] buffer, int offset) {
        int result = buffer[offset++] << 24;
        result |= (buffer[offset++] << 16) & 0x00FF0000;
        result |= (buffer[offset++] << 8) & 0x0000FF00;
        result |= buffer[offset] & 0x000000FF;

        return result;
    }

    /**
     * Set four bytes to the specified value. The most signified byte go first.
     *
     * @param buffer the byte array
     * @param offset the offset of the start of the data in the array
     * @param value  the value to set
     */
    private void setInt(byte[] buffer, int offset, int value) {
        buffer[offset++] = (byte) ((value & 0xFF000000) >>> 24);
        buffer[offset++] = (byte) ((value & 0x00FF0000) >>> 16);
        buffer[offset++] = (byte) ((value & 0x0000FF00) >>> 8);
        buffer[offset] = (byte) ((value & 0x000000FF));
    }

    /**
     * Replaces black color in the palette chunk to the specified color.
     *
     * @param buffer the byte array
     * @param offset the offset of the start of the data in the array
     * @param color  the color to replace
     */
    private void colorizePalette(byte[] buffer, int offset, int color) {
        int dataLength = getInt(buffer, offset);
        int dataOffset = offset + 8;

        int r = (color & 0x00FF0000) >>> 16;
        int g = (color & 0x0000FF00) >>> 8;
        int b = (color & 0x000000FF);

        for (int i = 0; i < dataLength / 3; i++) {
            int pR = buffer[dataOffset] & 0xFF;
            int pG = buffer[dataOffset + 1] & 0xFF;
            int pB = buffer[dataOffset + 2] & 0xFF;

            int brightness = (pR + pG + pB) / 3;

            buffer[dataOffset++] = (byte) (r + (brightness * (255 - r)) / 255); // red
            buffer[dataOffset++] = (byte) (g + (brightness * (255 - g)) / 255); // green
            buffer[dataOffset++] = (byte) (b + (brightness * (255 - b)) / 255); // blue
        }

        int crc = crc32(buffer, offset + 4, dataLength + 4);
        setInt(buffer, offset + 8 + dataLength, crc);
    }

    /* CRC32 calculations */

    private static final int CRC32_POLYNOMIAL = 0xEDB88320;

    /**
     * Calculates the CRC32 value. This functions doesn't use a table for reasons of memory saving.
     *
     * @param buffer the byte array
     * @param offset the offset of the start of the data in the array
     * @param count  the count of bytes
     * @return the CRC32 value
     */
    private static int crc32(byte buffer[], int offset, int count) {
        int crc = 0xFFFFFFFF;
        while (count-- != 0) {
            int t = (crc ^ buffer[offset++]) & 0xFF;
            for (int i = 8; i > 0; i--) {
                if ((t & 1) == 1) {
                    t = (t >>> 1) ^ CRC32_POLYNOMIAL;
                } else {
                    t >>>= 1;
                }
            }
            crc = (crc >>> 8) ^ t;
        }
        return ~crc;
    }
}
//#sijapp cond.end #