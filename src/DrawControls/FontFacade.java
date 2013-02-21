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
package DrawControls;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * <p>FontFacade allows to use native fonts and bitmaps fonts against conditions.</p>
 * <p/>
 * <p>FontFacade stores an instance of the BitmapFont or Font, and allows to draw and measure
 * characters and strings.</p>
 * <p/>
 * <p>This code is a part of the Mobile Fonts Project (http://sourceforge.net/projects/mobilefonts)</p>
 *
 * @author Sergey Tkachev <a href="http://sergetk.net">http://sergetk.net</a>
 */
public class FontFacade {
    private Object font;
    private boolean isBitmapFont;

    /**
     * Creates an instance of the FontFacade
     *
     * @param font the font. It must be an instance of Font or BitmapFont
     */
    public FontFacade(Object font) {
        setFont(font);
    }

    /**
     * Sets the font.
     *
     * @param font the font. It must be an instance of Font or BitmapFont
     */
    private void setFont(Object font) {
        this.font = font;
//#sijapp cond.if modules_GFONT="true" #
        isBitmapFont = font instanceof BitmapFont;
//#sijapp cond.else#
        isBitmapFont = false;
//#sijapp cond.end#
    }

    /**
     * Gets the current font.
     *
     * @return the font
     */
    public Object getFont() {
        return font;
    }

    /**
     * Draws one character.
     *
     * @param g       the graphics context
     * @param c       the character to be drawn
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param anchors the anchor point for positioning of the text
     */
    public void drawChar(Graphics g, char c, int x, int y, int anchors) {
        if (isBitmapFont) {
//#sijapp cond.if modules_GFONT="true" #
            ((BitmapFont) font).drawChar(g, c, x, y);
//#sijapp cond.end#
        } else if (font instanceof Font) {
            g.setFont((Font) font);
            g.drawChar(c, x, y, anchors);
        }
    }

    /**
     * Draws the string.
     *
     * @param g       the graphics context
     * @param text    the string to be drawn
     * @param x       the x coordinate of the anchor point
     * @param y       the y coordinate of the anchor point
     * @param anchors the anchor point for positioning of the text
     * @return the x coordinate for the next string
     */
    public int drawString(Graphics g, String text, int x, int y, int anchors) {
        return drawSubstring(g, text, 0, text.length(), x, y, anchors);
    }

    /**
     * Draws the substring.
     *
     * @param g       the graphics context
     * @param text    the string to be drawn
     * @param offset  zero-based index of a first character
     * @param length  the length of substring
     * @param x       the x coordinate of the anchor point
     * @param y       the y coordinate of the anchor point
     * @param anchors the anchor point for positioning of the text
     * @return the x coordinate for the next string
     * @noinspection SameParameterValue
     */
    private int drawSubstring(Graphics g, String text, int offset, int length, int x, int y, int anchors) {
        if (isBitmapFont) {
//#sijapp cond.if modules_GFONT="true" #
            return ((BitmapFont) font).drawSubstring(g, text, offset, length, x, y, anchors);
//#sijapp cond.end#
        } else if (font instanceof Font) {
            g.setFont((Font) font);
            g.drawSubstring(text, offset, length, x, y, anchors);
            return ((Font) font).substringWidth(text, length, offset);
        }
        return 0;
    }

    /**
     * Gets the character width.
     *
     * @param c the character to measure
     * @return the width in pixels
     */
    public int charWidth(char c) {
        if (isBitmapFont) {
//#sijapp cond.if modules_GFONT="true" #
            return ((BitmapFont) font).charWidth(c);
//#sijapp cond.else#
//#			return 0;
//#sijapp cond.end#
        } else {
            return ((Font) font).charWidth(c);
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
        if (isBitmapFont) {
//#sijapp cond.if modules_GFONT="true" #
            return ((BitmapFont) font).charsWidth(ch, offset, length);
//#sijapp cond.else#
//#			return 0;
//#sijapp cond.end#
        } else {
            return ((Font) font).charsWidth(ch, offset, length);
        }
    }

    /**
     * Gets the string width.
     *
     * @param str the string to width measure
     * @return the width in pixels
     */
    public int stringWidth(String str) {
        return substringWidth(str, 0, str.length());
    }

    /**
     * Gets the substring width.
     *
     * @param str    the string to measure
     * @param offset zero-based index of a first character
     * @param length the number of characters to measure
     * @return the width in pixels
     * @noinspection SameParameterValue
     */
    private int substringWidth(String str, int offset, int length) {
        if (isBitmapFont) {
//#sijapp cond.if modules_GFONT="true" #
            return ((BitmapFont) font).substringWidth(str, offset, length);
//#sijapp cond.else#
//#			return 0;
//#sijapp cond.end#
        } else {
            return ((Font) font).substringWidth(str, offset, length);
        }
    }

    /**
     * Gets the height of the font.
     *
     * @return the height in pixels
     */
    public int getFontHeight() {
        if (isBitmapFont) {
//#sijapp cond.if modules_GFONT="true" #
            return ((BitmapFont) font).getHeight();
//#sijapp cond.else#
//#			return 0;
//#sijapp cond.end#
        } else {
            return ((Font) font).getHeight();
        }
    }

    /**
     * Draws the string with a limitation by the width.
     *
     * @param g       the graphics context
     * @param text    the text to be drawn
     * @param x       the x coordinate of the anchor point
     * @param y       the y coordinate of the anchor point
     * @param width   the width limit in pixels
     * @param anchors the anchor point for positioning the text
     */
    public void drawTrimmedString(Graphics g, String text, int x, int y, int width, int anchors) {
        int textWidth = stringWidth(text);

        if (textWidth <= width) {
            drawString(g, text, x, y, anchors);
        } else {
            textWidth = 3 * charWidth('.');
            int n = 0;
            for (; n < text.length(); n++) {
                textWidth += charWidth(text.charAt(n));
                if (textWidth > width) {
                    break;
                }
            }
            x = drawString(g, text.substring(0, n) + "...", x, y, anchors);
        }
    }

    /**
     * Draws the string with a limitation by the width.
     *
     * @param g            the graphics context
     * @param outlineColor the color of text outline
     * @param text         the text to be drawn
     * @param x            the x coordinate of the anchor point
     * @param y            the y coordinate of the anchor point
     * @param anchors      the anchor point for positioning the text
     */
    public void drawOutlinedString(Graphics g, int outlineColor, String text, int x, int y, int anchors) {
        int textColor = g.getColor();
        g.setColor(outlineColor);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (x != 0 || y != 0) {
                    drawString(g, text, x + dx, y + dy, anchors);
                }
            }
        }
        g.setColor(textColor);
        drawString(g, text, x, y, anchors);
    }
}
