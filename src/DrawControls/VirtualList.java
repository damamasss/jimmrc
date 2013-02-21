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
 File: src/DrawControls/VirtualList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin
 *******************************************************************************/

package DrawControls;

import jimm.ContactList;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.Options;
import jimm.comm.XStatus;
import jimm.ui.PopUp;

import javax.microedition.lcdui.*;

//! This class is base class of owner draw list controls
/*!
    It allows you todr create list with different colors and images.
    Base class of VirtualDrawList if Canvas, so it draw itself when
    paint event is heppen. VirtualList have cursor controlled of
    user
*/

public abstract class VirtualList extends CanvasEx {
    /*! Does't show cursor at selected eitem. */
    public final static byte MODE_TEXT = 0;
    /*! Use fdotted mode of cursor. If itemd of list is selected, dotted rectangle drawn around  it*/
    public final static byte MODE_LIST = 1;

    public final static int LARGE_FONT = Font.SIZE_LARGE;
    public final static int MEDIUM_FONT = Font.SIZE_MEDIUM;
    public final static int SMALL_FONT = Font.SIZE_SMALL;

    final static protected ListItem paintedItem;
    private static Image bgimage = null;
    private static boolean skinNotNull = false;
    private static String timeString = jimm.comm.DateAndTime.getDateString(true, false);
    //private static Clock clock = new Clock();
    private final static Image nullImg = Image.createRGBImage(new int[]{0x00FFFFFF}, 1, 1, true);

    public FontFacade normalFont;
    private FontFacade boldFont;
    private FontFacade italicFont;
    private FontFacade underlinedFont;
    private VirtualListCommands vlCommands;
    public int topItem = 0;
    public int currItem = 0;
    private int lastCurrItem = 0;
    private int lastTopItem = 0;
    public boolean fullScreen = true;
    private Icon[] images = new Icon[3];
    //protected Icon cliIcon;
    private String caption;
    protected String capNick;

    protected int
            fontSize,  // Current font size of VL
            cursorMode,
            bkgrndColor,     // bk color of VL
            textColor,     // Default text color.
            capBkColor,
            capTxtColor,     // Color of caprion text
            menuBkColor,
            menuBkColor2,
            mdcursor,
            barTxtColor,
            menuCursor,
            menuTxt,
            cursorColor,
            dcursor;

    static {
        paintedItem = new ListItem();
    }

    public void setFullScreenMode(boolean value) {
        fullScreen = value;
    }

    //! Create new virtual list with default values
    public VirtualList(String capt) {
        super();
        setCaption(capt);
        cursorMode = MODE_LIST;
        fontSize = SMALL_FONT;
        createSetOfFonts();
    }

    abstract protected int getSize();

    abstract protected void get(int index, ListItem item);

    protected FontFacade getQuickFont(int style) {
        switch (style) {
            case Font.STYLE_BOLD:
                return boldFont;
            case Font.STYLE_PLAIN:
                return normalFont;
            case Font.STYLE_ITALIC:
                return italicFont;
            case Font.STYLE_UNDERLINED:
                return underlinedFont;
        }
        return (new FontFacade(getSuperFont(Font.FACE_SYSTEM, style, fontSize)));
    }

    protected int getDrawHeight() {
        return getHeightInternal() - getCapHeight() - getMenuBarHeight();
    }

    public int getDrawWidth() {
        return getWidthInternal() - scrollerWidth;
    }

    protected int getLimY() {
        return getHeightInternal();
    }

    public void setFontSize(int value) {
        if (fontSize == value) {
            return;
        }
        fontSize = value;
        createSetOfFonts();
        checkTopItem();
        invalidate();
    }

    public void setFontStyle(int value) {
        createNewFonts(value);
        checkTopItem();
        invalidate();
    }

    public static void updateTimeString() {
        timeString = jimm.comm.DateAndTime.getDateString(true, false);
        //clock.update(timeString, 0, 0, NativeCanvas.getWidthEx(), NativeCanvas.getHeightEx(), 0xff6600);
    }

    public void repaint() {
        if (isActive()) {
            invalidate();
        }
    }

    public void setCapImage(Icon image) {
        if ((images[0] == image) || (getCapHeight() == 0)) return;
        if (images[0] != null) {
            images[0] = null;
        }
        images[0] = image;
        invalidate();
    }

    public void setCapXstImage(Icon image) {
        if ((images[1] == image) || (getCapHeight() == 0)) return;
        if (images[1] != null) {
            images[1] = null;
        }
        images[1] = image;
        invalidate();
    }

    public void setCapPStImage(Icon icon) {
        if ((images[2] == icon) || (getCapHeight() == 0)) return;
        if (images[2] != null) {
            images[2] = null;
        }
        images[2] = icon;
        invalidate();
    }

    public void setVLCommands(VirtualListCommands vlCommands) {
        this.vlCommands = vlCommands;
    }

    public void setColorScheme() {
        setColors(
                CanvasEx.getColors()[CanvasEx.COLOR_CAP],
                CanvasEx.getColors()[CanvasEx.COLOR_TEXT],
                CanvasEx.getColors()[CanvasEx.COLOR_BACK],
                CanvasEx.getColors()[CanvasEx.COLOR_CURSOR],
                CanvasEx.getColors()[CanvasEx.COLOR_MBACK1],
                CanvasEx.getColors()[CanvasEx.COLOR_MCURSOR],
                CanvasEx.getColors()[CanvasEx.COLOR_DCURSOR],
                CanvasEx.getColors()[CanvasEx.COLOR_CAP],
                CanvasEx.getColors()[CanvasEx.COLOR_CAP_TEXT],
                CanvasEx.getColors()[CanvasEx.COLOR_BAR_TEXT],
                CanvasEx.getColors()[CanvasEx.COLOR_MENU_TEXT],
                CanvasEx.getColors()[CanvasEx.COLOR_MBACK1]
        );
    }


    public void setColors(int capBk, int text, int bkgrnd, int cursor, int mback1, int mcursor, int dcursor, int mdcursor,
                          int capTxt, int barTxt, int menuTxt, int mback2) {
        capBkColor = capBk;
        capTxtColor = capTxt;
        bkgrndColor = bkgrnd;
        cursorColor = cursor;
        textColor = text;
        menuBkColor = mback1;
        menuBkColor2 = mback2;
        menuCursor = mcursor;
        this.dcursor = dcursor;
        this.mdcursor = mdcursor;
        barTxtColor = barTxt;
        this.menuTxt = menuTxt;
        if (isActive()) {
            invalidate();
        }
    }

    public int getTextColor() {
        return textColor;
    }

    public void createSetOfFonts() {
        normalFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSize));
        boldFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSize));
        italicFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, fontSize));
        underlinedFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_UNDERLINED, fontSize));
    }

    private void createNewFonts(int style) {
        normalFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, style, fontSize));
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getVisCount() {
        int size = getSize();
        int y = 0;
        int counter = 0, i;
        int height = getDrawHeight();
        int topItem = this.topItem;

        if (size == 0) {
            return 0;
        }

        if (topItem < 0) topItem = 0;
        else if (topItem >= size) topItem = size - 1;
        for (i = topItem; i < size - 1; i++) {
            y += getItemHeight(i);
            if (y > height) return counter;
            counter++;
        }
        y = height;
        counter = 0;
        for (i = size - 1; i >= 0; i--) {
            y -= getItemHeight(i);
            if (y < 0) break;
            counter++;
        }
        return counter;
    }

    public void setMode(int value) {
        if (cursorMode != value) {
            cursorMode = value;
            invalidate();
        }
    }

    public boolean isActive() {
        return NativeCanvas.isActive(this);
    }

    public void activate(Alert alert) {
        activate(alert, true);
    }

    public void activate(Alert alert, boolean showMe) {
        if (!isActive() && showMe) {
            Jimm.setDisplay(this);
        }
        if (isActive()) {
            Jimm.setDisplay(new PopUp(this, alert.getString(), getDrawWidth(), 4, getCapHeight()));
        } else {
            Jimm.getDisplay().setCurrent(alert, NativeCanvas.getInst());
        }
    }

    public void activate() {
        Jimm.setDisplay(this);
    }

    public void beforeShow() {
        updateBlackout();
        timeString = jimm.comm.DateAndTime.getDateString(true, false);
        //#sijapp cond.if modules_TOOLBAR is "true"#
        if (toolbar != null) {
            int mb = getMenuBarHeight();
            int w = NativeCanvas.getWidthEx();
            int h = NativeCanvas.getHeightEx();
            int tw = toolbar.getWidth();
            //int th =  toolbar.getHeight();
            toolbar.initCoord((w - tw) / 2, h - mb);
        }
        //#sijapp cond.end#
    }

    //! Returns height of each item in list
    public int getItemHeight(int itemIndex) {
        int imgHeight = 0, fontHeight = getFontHeight() + 1;
        paintedItem.clear();
        get(itemIndex, paintedItem);
        if (paintedItem.image != null) imgHeight = paintedItem.image.getHeight();
        return (imgHeight > fontHeight) ? imgHeight + 1 : fontHeight + 1;
    }

    protected void checkCurrItem() {
        currItem = Math.max(0, Math.min(currItem, getSize() - 1));
//        if (currItem < 0) {
//            currItem = 0;
//        }
//        else if (currItem > getSize() - 1) {
//            currItem = getSize() - 1;
//        }
    }

    protected void checkTopItem() {
        int size = getSize();
        int visCount = getVisCount();
        if (size == 0) {
            topItem = 0;
            return;
        }
        if (currItem >= (topItem + visCount - 1)) {
            topItem = currItem - visCount + 1;
        } else if (currItem < topItem) {
            topItem = currItem;
        }
        if ((size - topItem) <= visCount) {
            topItem = (size > visCount) ? (size - visCount) : 0;
        }
        if (topItem < 0) {
            topItem = 0;
        }
    }

    public boolean visibleItem(int index) {
        return (index >= topItem) && (index <= (topItem + getVisCount()));
    }

    protected void storelastItemIndexes() {
        lastCurrItem = currItem;
        lastTopItem = topItem;
    }

    protected void repaintIfLastIndexesChanged() {
        if ((lastCurrItem != currItem) || (lastTopItem != topItem)) {
            invalidate();
        }
        if ((lastCurrItem != currItem) && (vlCommands != null)) {
            vlCommands.vlCursorMoved(this);
        }
    }

    protected void checkItemsAndRepaint() {
        checkCurrItem();
        checkTopItem();
        repaintIfLastIndexesChanged();
    }

    public void moveCursor(int step, boolean moveTop) {
        storelastItemIndexes();
        if ((moveTop) && (cursorMode == MODE_TEXT)) {
            topItem += step;
        }
//        if ((currItem += step) < 0) {
//            currItem = getSize() - 1;
//        }
//        currItem = currItem % getSize();
        currItem += step;
        if (step == 1) {
            if (currItem >= getSize()) {
                currItem = 0;  // перемещение курсора в самый верх
            }
        } else if (step == -1) {
            if (currItem < 0) {
                currItem = getSize() - 1; // перемещение курсора в самый низ
            }
        }
        checkCurrItem();
        checkTopItem();
        repaintIfLastIndexesChanged();
    }

    protected boolean itemSelected() {
        return executeCommand(findMenuByType(Command.OK));
    }

    public void leftMenuPressed() {
        if (leftMenu != null) {
            executeCommand(leftMenu);
        }
    }

    public void rightMenuPressed() {
        if (rightMenu != null) {
            executeCommand(rightMenu);
        }
    }

    protected boolean keyReaction(int keyCode, int type, int gameAct) {
        if (!isActive()) {
            return false;
        }

        if (type != KEY_RELEASED) {
            switch (gameAct) {
                case Canvas.DOWN:
                    moveCursor(1, false);
                    return false;

                case Canvas.UP:
                    moveCursor(-1, false);
                    return false;

//                case KEY_CODE_LEFT_MENU:
//                    break;
//
//                case KEY_CODE_RIGHT_MENU:
//                    break;

                //case Canvas.LEFT:
                //case Canvas.RIGHT:
                    //if (vlCommands != null) {
                    //    vlCommands.vlKeyPress(this, 1000004, type, gameAct);
                    //    return false;
                    //}
                    //return true;

                //case Canvas.FIRE:
                //isDraggedWas &= (type != KEY_PRESSED);
                //break;

            }

            switch (keyCode) {
                case Canvas.KEY_NUM1:
                case Canvas.KEY_NUM7:
                case Canvas.KEY_NUM3:
                case Canvas.KEY_NUM9:
                    if (type != KEY_REPEATED) {
                        hotNavigation(keyCode);
                        return false;
                    }
            }
            return true;
        }

        switch (gameAct) {
            case Canvas.FIRE:
                if ((!itemSelected()) && (vlCommands != null)) {
                    vlCommands.vlItemClicked(this);
                }
                return false;

            case KEY_CODE_LEFT_MENU:
                leftMenuPressed();
                return false;

            case KEY_CODE_RIGHT_MENU:
                rightMenuPressed();
                return false;

            case KEY_CODE_BACK_BUTTON:
                hideMenu();
                invalidate();
                return false;
        }
        invalidate();
        return true;
    }

    protected boolean checkSoftKeyPressState(Command c, int type) {
        return (KEY_PRESSED == type);
    }

    public void hotNavigation(int keyCode) {
        switch (keyCode) {
            case Canvas.KEY_NUM1:
                storelastItemIndexes();
                currItem = topItem = 0;
                checkItemsAndRepaint();
                break;

            case Canvas.KEY_NUM7:
                storelastItemIndexes();
                currItem = getSize() - 1;
                checkItemsAndRepaint();
                break;

            case Canvas.KEY_NUM3:
            case Canvas.KEY_NUM9:
                int i = (keyCode == Canvas.KEY_NUM3) ? -1 : 1;
                moveCursor(i * getVisCount(), false);
                break;
        }
    }

    private void hideMenu() {
        Command backMenu = findMenuByType(Jimm.is_smart_SE() ? Command.CANCEL : Command.BACK);
        if (backMenu != null) {
            executeCommand(backMenu);
        }
    }

    public void doKeyreaction(int keyCode, int type) {
        boolean hotKey = false;
        int gameAct = getExtendedGameAction(keyCode, fullScreen);
        hotKey |= keyReaction(keyCode, type, gameAct);
//        if (type == KEY_PRESSED) {
//            hotKey |= keyReaction(keyCode, type, gameAct);
//        } else if (type == KEY_REPEATED) {
//            hotKey |= keyReaction(keyCode, type, gameAct);
//        }

        if (hotKey) {
            if ((vlCommands != null) && (isActive())) {
                vlCommands.vlKeyPress(this, keyCode, type, gameAct);
            }
        }
    }

    public CommandListener commandListener;

    public void setCommandListener(CommandListener l) {
        commandListener = l;
    }

    protected boolean executeCommand(Command command) {
        if ((commandListener != null) && (command != null)) {
            commandListener.commandAction(command, null);
            return true;
        }
        return false;
    }

    //#sijapp cond.if target is "MIDP2"#;
    public void pointerPressed(int x, int y) {
        isDraggedWas = false;
        lastPointerYCrd = y;
        lastPointerXCrd = x;
        lastPointerTopItem = topItem;
        lastPointerTime = System.currentTimeMillis();
// #sijapp cond.if modules_TOUCH2 is "true" #
        pointerReleased(x, y);
// #sijapp cond.end#
    }

    public void pointerDragged(int x, int y) {
        if (lastPointerTopItem == -1 || lastPointerYCrd == -1) {
            return;
        }
        int itemCount = getSize();
        int visCount = getVisCount();
        if (itemCount == visCount) {
            return;
        }
        storelastItemIndexes();
        int height = getHeightInternal() - getCapHeight();
        topItem = lastPointerTopItem + ((visCount << 1) * (-y + lastPointerYCrd)) / height;
        isDraggedWas |= topItem != lastPointerTopItem;
        if (topItem < 0) {
            topItem = 0;
        }
        if (topItem > (itemCount - visCount)) {
            topItem = itemCount - visCount;
        }
        if (x > lastPointerXCrd + getDrawWidth() * 4 / 5) {
            topItem = itemCount - visCount;
        } else if (x < lastPointerXCrd - getDrawWidth() * 4 / 5) {
            topItem = 0;
        }
        repaintIfLastIndexesChanged();
    }

    public void pointerReleasedEmu(int x, int y) {
        isDraggedWas = true;
    }

    public void pointerReleased(int x, int y) {
        if (isDraggedWas/* || lastPointerYCrd == -1*/) {
            return;
        }
        byte type = DMS_CLICK;
        if (System.currentTimeMillis() - lastPointerTime > 300 && Math.abs(x - lastPointerXCrd) < 10 && Math.abs(lastPointerYCrd - lastPointerYCrd) < 10) {
            type = DMS_DBLCLICK;
        }
        paintAllOnGraphics(getGraphics(), type, x, y);
        lastPointerYCrd = -1;
    }

    protected boolean pointerPressedOnUtem(int index, int x, int y, int x1, int x2) {
        return false;
    }

    protected boolean oldDoubleClick() {
        return false;
    }

    protected void pointSelect() {
        if ((!itemSelected()) && (vlCommands != null)) {
            vlCommands.vlItemClicked(this);
        }
    }
//#sijapp cond.end#

    public void setCaption(String capt) {
        if ((capt == null) && (caption == null)) {
            return;
        }
        if ((caption != null) && (caption.equals(capt))) {
            return;
        }
        caption = capt;
        invalidate();
    }

//    private String secCaption;
//
//    public void setSecCaption(String cap) {
//        if ((secCaption != null) && (secCaption.equals(cap))) {
//            return;
//        }
//        secCaption = cap;
//        if (isActive()) {
//            invalidate();
//        }
//    }

    public String getCaption() {
        return caption;
    }

//    public void setTopItem(int index) {
//        storelastItemIndexes();
//        currItem = topItem = index;
//        checkTopItem();
//        repaintIfLastIndexesChanged();
//    }

    public void setCurrentItem(int index) {
        storelastItemIndexes();
        currItem = index;
        checkTopItem();
        repaintIfLastIndexesChanged();
    }

    public int getCurrIndex() {
        return currItem;
    }

    public int getCapHeight() {
        if (!fullScreen) {
            return 0;
        }
        int capHeight = 0;
        if (caption != null)
            capHeight = Math.max(facade.getFontHeight() + 2, Options.getInt(Options.OPTION_ICONS_CANVAS));
        if (images[0] != null) {
            int imgHeight = images[0].getHeight() + 2;
            if (capHeight < imgHeight) capHeight = imgHeight;
        }

        return capDivisor * capHeight;// - capDivisor * capHeight * (100 - anime) / 100;
    }

    protected int getCapX(int x) {
        return x;
    }

    //public static FontFacade capFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));

    public boolean drawCaption(Graphics g, int mode, int curX, int curY) {
        if (caption == null) return false;
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        if (!fullScreen) return false;
//#sijapp cond.end#
        int width = getWidthInternal();
        int height = getCapHeight();

        int _height = height / capDivisor;
        if (skinNotNull) {
            int clipX = g.getClipX();
            int clipY = g.getClipY();
            int clipWidth = g.getClipWidth();
            int clipHeight = g.getClipHeight();
            g.setClip(0, 0, width, height - 1);
            g.drawImage(bgimage, width / 2, height, Graphics.BOTTOM | Graphics.HCENTER);
            g.setClip(clipX, clipY, clipWidth, clipHeight);
        } else {
            if (capTrans == 0) {
                /*drawGradient(g, 0, 0, width, height - 1, capBkColor, height - 1, 0, -64); // более плавный градиент заголовка
                drawGlassAlpha(g, 0, 0, width, height - 1);*/
                //drawGlassRect(g, capBkColor, 0, 0, width, height - 1);
                //g.setColor(transformColorLight(capBkColor, -128));
                //g.drawLine(0, height - 1, width, height - 1);
                drawAvernageAlpha(g, 0, 0, width, height - 1, 0, 0xff, capBkColor);
            } else {
                drawAlphaGradient(g, 0, 0, width, height - 1, capBkColor, 0, -48, 255 - capTrans);
            }
        }

        g.setColor(capTxtColor);
        int x = getCapX(2 + captionShiftLeft);
        int toolIdx = -1, clipX = -1, clipY = -1, clipWidth = -1, clipHeight = -1;

        if (images[0] != null) {
            if (profileIdx >= 0 && clActive) {
                int w = facade.stringWidth(Integer.toString(profileIdx));
                drawString(g, facade, Integer.toString(profileIdx + 1), x, _height, Graphics.BOTTOM | Graphics.LEFT);
                x += w;
            }
            images[0].drawInVCenter(g, x, _height / 2);
// #sijapp cond.if target is "MIDP2" #
            if ((mode != DMS_DRAW) && (ptInRect(curX, curY, x, _height - images[0].getHeight(), x + images[0].getWidth(), _height)))
                toolIdx = 5;
// #sijapp cond.end #
            x += images[0].getWidth() + 1;
        }

        if (images[1] != null && (images[1] != XStatus.getStatusImage(XStatus.XSTATUS_NONE))) {
            images[1].drawInVCenter(g, x, _height / 2);
// #sijapp cond.if target is "MIDP2" #
            if ((mode != DMS_DRAW) && (ptInRect(curX, curY, x, _height - images[1].getHeight(), x + images[1].getWidth(), _height)))
                toolIdx = 6;
// #sijapp cond.end #
            x += images[1].getWidth() + 1;
        }
        if (images[2] != null) {
            images[2].drawInVCenter(g, x, _height / 2);
// #sijapp cond.if target is "MIDP2" #
            if ((mode != DMS_DRAW) && (ptInRect(curX, curY, x, _height - images[0].getHeight(), x + images[2].getWidth(), _height)))
                toolIdx = 8;
// #sijapp cond.end #
            x += images[2].getWidth() + 1;
        }

        //g.setColor(capTxtColor);
        //if (images[0] != null || facade.stringWidth(caption) > getWidthInternal()) {
        drawString(g, facade, caption, x, (_height - facade.getFontHeight()) / 2, Graphics.TOP | Graphics.LEFT);
        //} else {
        //    drawString(g, facade, caption, width / 2, (_height - facade.getFontHeight()) / 2, Graphics.TOP | Graphics.HCENTER);
        //}
// #sijapp cond.if modules_SOUNDS is "true"#
        if (clActive) {
            Icon capSoundImage = ContactList.getSoundPicture();
            if ((capSoundImage != null)) {
                int _hg = (capDivisor > 1) ? (height / (capDivisor - 1)) / capDivisor : 0;
                capSoundImage.drawInVCenter(g, width - 2 - captionShiftRight - capSoundImage.getWidth(), _height / 2 + _hg);
// #sijapp cond.if target is "MIDP2"#
                if ((mode != DMS_DRAW) && (ptInRect(curX, curY, width - 2 - capSoundImage.getWidth(), height - 3 - capSoundImage.getHeight(), width - 2, height - 3)))
                    toolIdx = 7;
// #sijapp cond.end#
                clipX = g.getClipX();
                clipY = g.getClipY();
                clipWidth = g.getClipWidth();
                clipHeight = g.getClipHeight();
                g.setClip(0, _height - 3, width - 2 - capSoundImage.getWidth(), _height + 3);
            }
        }
// #sijapp cond.end#
        //int xx = x;
        //x = 2 + captionShiftLeft;
        if (capDivisor == 2) {
            //if (secCaption != null) {
            //    drawString(g, capFont, secCaption, x, height - 2, Graphics.BOTTOM | Graphics.LEFT);
            //}
// #sijapp cond.if target is "MIDP2" #
            /*if (toolBar) {
                   final int ih = ContactList.menuIcons.getHeight();
                   final Icon icons[] = {
                       ContactList.menuIcons.elementAt(12),
                       ContactList.menuIcons.elementAt(1),
                       ContactList.menuIcons.elementAt(9),
                       ContactList.menuIcons.elementAt(0),
                       ContactList.menuIcons.elementAt(33)
                   };
                   int len = icons.length;
                   Icon image;
                   for (int i = 0; i < len; i++) {
                       image = icons[i];
                       if (image == null) {
                           if (i == 0) {
                               break;
                           }
                           continue;
                       }
                       int _ht = (capDivisor > 1) ?(height/(capDivisor - 1))/capDivisor : 0;
                       image.drawInVCenter(g, x, _height/2 + _ht);
                       if ((mode != DMS_DRAW) && (ptInRect(curX, curY, x, height - 3 - image.getHeight(), x + image.getWidth(), height - 3))) {
                           toolIdx = i;
                           break;
                       }
                       x += image.getWidth() + 6;
                   }
                   x += 6;
                   final int tWidth = Math.max(JimmUI.standartFAcade.stringWidth("1"), Math.max(JimmUI.standartFAcade.stringWidth("2"), JimmUI.standartFAcade.stringWidth("3"))) + 4;
                   final int tHeight = JimmUI.standartFAcade.getFontHeight();
                   if (g.getClipWidth() - x >= 3 * tWidth + 12 && profileIdx >= 0) {
                       int y = height - 3 - tHeight;
                       if (ih > tHeight) {
                           y -= (ih - tHeight) / 2;
                       }
                       for(int i = 0; i < 3; i++) {
                           int k = (profileIdx == i) ? -1 : 1;
                           g.setColor(transformColorLight(capBkColor, -32));
                           g.fillRect(x, y, tWidth, tHeight);
                           g.setColor(transformColorLight(capBkColor, k * 64));
                           g.drawLine(x, y, x + tWidth, y);
                           g.drawLine(x, y, x, y + tHeight);
                           g.setColor(transformColorLight(capBkColor, -k * 64));
                           g.drawLine(x, y + tHeight, x + tWidth, y + tHeight);
                           g.drawLine(x + tWidth, y, x + tWidth, y + tHeight);
                           g.setColor(capTxtColor);
                           drawString(g, JimmUI.standartFAcade, Integer.toString(i + 1), (2 * x + tWidth) / 2, y, Graphics.TOP | Graphics.HCENTER);
                           if ((mode != DMS_DRAW && toolIdx < 0) && (ptInRect(curX, curY, x, y, x + tWidth, y + tHeight))) {
                               toolIdx = i + 9;
                               break;
                           }
                           x += tWidth + 6;
                       }
                   }
               } else*/
// #sijapp cond.end #
//            if (capNick != null && secCaption != null) {
//                x += JimmUI.standartFAcade.stringWidth(secCaption);
//                if (cliIcon != null) {
//                    cliIcon.drawByLeftBottom(g, x, height - 2);
//                    x += cliIcon.getWidth() + 1;
//                }
//                drawString(g, capFont, capNick, x, height - 2, Graphics.BOTTOM | Graphics.LEFT);
//                capNick = null;
//            }
        }
// #sijapp cond.if target is "MIDP2" #
        if (mode != DMS_DRAW) {
            Jimm.getContactList().captionPressed(toolIdx);
        }
// #sijapp cond.end #
        if (clipX >= 0) {
            g.setClip(clipX, clipY, clipWidth, clipHeight);
        }
        return (toolIdx >= 0);
    }

    protected boolean isItemSelected(int index) {
        return ((currItem == index) && (cursorMode != MODE_TEXT));
    }

    protected void drawScroller(Graphics g, int topY, int _visCount, int menuBarHeight) {
        drawScroller(g, topY, _visCount, menuBarHeight, false);
    }

    public void drawScroller(Graphics g, int topY, int _visCount, int menuBarHeight, boolean flag) {
        drawScroller(g, topItem, topY, _visCount, getDrawWidth(), getHeightInternal() - menuBarHeight, getSize(), flag);
    }

    public static Image getBackGroundImage() {
        if (bgimage == null) {
            bgimage = nullImg;
        }
        return bgimage;
    }

    private static int skinOffset = -1;

    private int getSkinOffset() {
        if (skinOffset < 0) {
            if (getBackGroundImage() == nullImg) skinOffset = 0;
            else {
                skinOffset = (bgimage.getHeight() - getHeight() - 1) / 3;
            }
            skinOffset -= 2 * Options.getInt(Options.OPTION_SKIN_OFFSET);
        }
        return skinOffset;
    }

    static public Image setBackGroundImage(Image img) {
        skinOffset = -1;
        if ((img == null) || (img.getHeight() < 64)) {
            skinNotNull = false;
            bgimage = nullImg;
        } else {
            bgimage = img;
            skinNotNull = Options.getBoolean(Options.OPTION_BARS_FROM_SKIN) && bgimage != null;
        }
        return bgimage;
    }

    public int getFontHeight() {
        return normalFont.getFontHeight();
    }

    public String getCurrentString() {
        paintedItem.clear();
        get(currItem, paintedItem);
        return paintedItem.text;
    }

    public boolean drawItems(Graphics g, int top_y_, int fontHeight, int mode, int curX, int curY, boolean drawBackground, boolean clip) {
        int grCursorY1 = -1, grCursorY2 = -1;
        int size = getSize();
        int top_y = top_y_;// - ((size > 0) ? getItemHeight(0) * getVisCount() / 2 : 0) * (100 - anime) / 100;
        int y = top_y, itemHeight;
        int itemWidth = getDrawWidth();
        int clipX = g.getClipX(), clipY = g.getClipY();
        int clipWidth = g.getClipWidth(), clipHeight = g.getClipHeight();
        int back_width = this.getWidthInternal(), back_height = this.getHeightInternal();
        int x1 = 1, x2 = itemWidth, y1, y2;
        g.setClip(0, drawBackground ? 0 : top_y, back_width, back_height);
        if (drawBackground) {
            drawBackground(g, back_width, back_height);
            if (!clActive) {
                drawCursor(g, top_y, grCursorY1, grCursorY2, itemWidth, topItem, size, back_height, 1);
            }
        }
        if (clip) {
            g.setClip(0, top_y_, back_width, back_height - top_y/*_ - getMenuBarHeight()*/); // todo bayan
        }
        //back_height = getLimY();
        paintedItem.clear();
        for (int i = topItem; i < size; i++) {
            itemHeight = getItemHeight(i);
            y1 = y + 1;
            y2 = y + itemHeight - 1;

            if (mode == DMS_DRAW) {
                drawItemData(g, i, x1, y1, x2, y2, fontHeight);
//#sijapp cond.if target is "MIDP2"#
            } else if ((y1 < curY) && (curY < y2) && (x1 < curX) && (curX < x2)) {
                boolean notChanged = false;
                switch (mode) {
                    case DMS_CLICK:
                        notChanged = (currItem == i);
                        if (!notChanged) {
                            currItem = i;
                            checkCurrItem();
                            if (vlCommands != null) {
                                vlCommands.vlCursorMoved(this);
                            }
                            invalidate();
                            break;
                        }
                        if (oldDoubleClick()) {
                            break;
                        } else {
                            notChanged = false;
                        }

                    case DMS_DBLCLICK:
                        pointSelect();
                        break;
                }
                if (pointerPressedOnUtem(i, curX - x1, curY - y1, x1, x2) && notChanged) {
                    pointSelect();
                }
                return true;
//#sijapp cond.end#
            }
            y += itemHeight;
            if (y > back_height) {
                break;
            }
        }
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        return false;
    }

    protected void drawBackground(Graphics g, int back_width, int back_height) {
        g.setColor(bkgrndColor);
        g.fillRect(0, 0, back_width, back_height);
        if (blackOut != 0) {
            g.drawImage(getBackGroundImage(), back_width / 2, (getHeight() + getSkinOffset()) / 2, Graphics.HCENTER | Graphics.VCENTER);
            if (getBackGroundImage() != nullImg && blackOut != 255) {
                int x;
                int y;
                int width = blackOutBuf.getWidth();
                int height = blackOutBuf.getHeight();
                for (x = 0; x < back_width; x += width) {
                    for (y = 0; y < back_height; y += height) {
                        g.drawImage(blackOutBuf, x, y, Graphics.TOP | Graphics.LEFT);
                    }
                }
            }
        }
        //clock.paint(g);
    }

    protected void drawCursor(Graphics g, int top_y, int grCursorY1, int grCursorY2, int itemWidth, int topItem, int size, int height, int x) {
        //if (!NativeCanvas.getCanvas().equals(this)) {
        //    return;
        //}
        int y = top_y;
        for (int i = topItem; i < size; i++) {
            int itemHeight = getItemHeight(i);
            if (isItemSelected(i)) {
                if (grCursorY1 < 0) grCursorY1 = y + 1;
                grCursorY2 = y + itemHeight - 1;
            }
            y += itemHeight;
            if (y > height) break;
        }

        if (grCursorY1 >= 0) {
            if (cursorTrans == 0) {
                drawGradient(g, x, grCursorY1, itemWidth - 2, grCursorY2 - grCursorY1, cursorColor, 16, -48, 0);
            } else {
                drawAlphaGradient(g, x, grCursorY1, itemWidth - 2, grCursorY2 - grCursorY1, cursorColor, -48, 0, 255 - cursorTrans);
            }
            g.setColor(dcursor);
            g.drawRect(x - 1, grCursorY1 - 1, itemWidth - 1, grCursorY2 - grCursorY1 + 1);
        }
    }

    public void paintAllOnGraphics(Graphics graphics, int mode, int curX, int curY) {
        int visCount = getVisCount();
        int menuBarHeight = getMenuBarHeight();
        int y = getCapHeight();
        int bottom = getHeightInternal();

        if (mode == DMS_DRAW) {
            drawItems(graphics, y, normalFont.getFontHeight(), mode, -1, -1, true, true);
            if (NativeCanvas.getCanvas().equals(this)) {
                drawScroller(graphics, y, visCount, menuBarHeight);
            }
            drawCaption(graphics, mode, curX, curY);
            drawMenuBar(graphics, menuBarHeight, bottom, mode, -1, -1);
//#sijapp cond.if target is "MIDP2"#
        } else {
            if ((mode == DMS_CLICK) && (drawCaption(graphics, mode, curX, curY))) {
                return;
            }
            if ((mode == DMS_CLICK) && (drawMenuBar(graphics, menuBarHeight, bottom, mode, curX, curY))) {
                return;
            }
            if (drawItems(graphics, y, getFontHeight(), mode, curX, curY, false, true)) {
                return;
            }
//#sijapp cond.end#
        }
    }

    public void paint(Graphics g) {
        if (getLocked()) {
            return;
        }
        paintAllOnGraphics(g, DMS_DRAW, -1, -1);
    }

    protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight) {
        paintedItem.clear();
        get(index, paintedItem);
        int x = paintedItem.horizOffset + x1;
        int imgWidth;
        get(index, paintedItem);
        g.setColor(paintedItem.color);
        if (paintedItem.image != null) {
            imgWidth = paintedItem.image.getWidth() + 3;
            paintedItem.image.drawByLeftTop(g, x, (y1 + y2 - paintedItem.image.getHeight()) / 2);
        } else {
            imgWidth = 0;
        }
        if (paintedItem.text != null) {
            drawString(g, getQuickFont(paintedItem.fontStyle), paintedItem.text, x + imgWidth, (y1 + y2 - fontHeight) / 2, Graphics.TOP | Graphics.LEFT);
        }
    }

    private int forcedWidth = -1;
    private int forcedHeight = -1;

    public void setForcedSize(int width, int height) {
        forcedWidth = width;
        forcedHeight = height;
    }

    public int getHeightInternal() {
        return (forcedHeight < 0) ? getHeight() : forcedHeight;
    }

    public int getWidthInternal() {
        return (forcedWidth < 0) ? getWidth() : forcedWidth;
    }

    public static int getWidth() {
        return NativeCanvas.getWidthEx();
    }

    public static int getHeight() {
        return NativeCanvas.getHeightEx();
    }

    public int getBottom() {
        return getHeightInternal();
    }

    ///////////////////////////////
    //                           //
    //        EXTENDED UI        //
    //                           //
    ///////////////////////////////


    private static int menuBarTrans = 0;
    private static int capTrans = 0;
    private static int captionShiftLeft = 0;
    public static int captionShiftRight = 0;
    private static int profileIdx = -1;
    private static int blackOut = 0;
    private static Image blackOutBuf;
    private static boolean softBar;
    protected static boolean newMStyle;
    protected static boolean gradientMenuBar;
    protected static int cursorTrans = 0;
    protected int capDivisor = 1;
    protected int menuDivisor = 1;
    protected boolean clActive;


    public static void updateParams() {
        cursorTrans = (Options.getInt(Options.OPTION_CURSOR_TRANS) * 255) / 10;
        menuBarTrans = (Options.getInt(Options.OPTION_BAR_TRANS) * 255) / 10;
        capTrans = (Options.getInt(Options.OPTION_CAP_TRANS) * 255) / 10;
        blackOut = 255 - (Options.getInt(Options.OPTION_BLACKOUT) * 255) / 10;
        softBar = Options.getBoolean(Options.OPTION_SOFT_BAR);
        //toolBar = Options.getBoolean(Options.OPTION_TOOLBAR);
        //autoAnswer = Options.getBoolean(Options.OPTION_AUTO_ANSWER);
        skinNotNull = Options.getBoolean(Options.OPTION_BARS_FROM_SKIN) && (getBackGroundImage().getHeight() >= 64);
        captionShiftLeft = (!Jimm.is_phone_SE()) ? Options.getInt(Options.OPTION_CAPTION_SHIFT) : 0;
        captionShiftRight = (Jimm.is_phone_SE()) ? Options.getInt(Options.OPTION_CAPTION_SHIFT) : 0;
        gradientMenuBar = Options.getBoolean(Options.OPTION_GRADIENT_MB);
        if (!gradientMenuBar) {
            alphaBufferR = new int[]{0x00FFFFFF};
        }
        if (blackOut == 255) {
            blackOutBuf = null;
        } else {
            blackOutBuf = nullImg;
        }
    }

    private void updateBlackout() {
        if (blackOutBuf != null) {
            int width = getWidth() / 2;
            int height = getHeight() / 2;
            int buf[] = new int[width * height];
            int tc = (255 - blackOut) << 24;
            tc |= bkgrndColor;
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = tc;
            }
            blackOutBuf = Image.createRGBImage(buf, width, height, true);
        }
    }

    public static void setProfileIdx(int i) {
        profileIdx = i;
    }

    public boolean drawMenuBar(Graphics g, int height, int y2, int style, int curX, int curY) {
        return drawMenuBar(g, height, y2, style, curX, curY, timeString);
    }

    protected boolean drawMenuBar(Graphics g, int height, int y2_, int style, int curX, int curY, String timeString) {
        if (height == 0) {
            return false;
        }
        int y2 = y2_;// + height * (100 - anime) / 100;
        int _height = height;
        //#sijapp cond.if modules_TOOLBAR is "true"#
        if (toolbar != null) {
            _height -= toolbar.getHeight();
        }
        //#sijapp cond.end#
        int y1 = y2 - height;
        int _y1 = y2 - _height;
        int width = getWidthInternal();
        int layer = height / 4;
        String text;

        if (!fullScreen) {
            return false;
        }

        if (style == DMS_DRAW) {
            if (skinNotNull) {
                int clipX = g.getClipX();
                int clipY = g.getClipY();
                int clipWidth = g.getClipWidth();
                int clipHeight = g.getClipHeight();
                g.setClip(0, y1, width, height);
                g.drawImage(bgimage, width / 2, y1, Graphics.TOP | Graphics.HCENTER);
                g.setClip(clipX, clipY, clipWidth, clipHeight);
            } else {
                if (gradientMenuBar) {
                    drawTimeAlpha(g, width, height, y1, capBkColor, 255 - menuBarTrans);
                    if (menuBarTrans == 0) {
                        g.setColor(transformColorLight(capBkColor, -48));
                        g.drawLine(0, y1, width, y1);
                    }
                } else {
                    if (menuBarTrans == 0) {
                        drawAvernageAlpha(g, 0, y1, width, y2, 0xff, 0, capBkColor);
                        //drawGlassRect(g, capBkColor, 0, y1, width, y2);
                        //g.setColor(transformColorLight(capBkColor, -48));
                        //g.drawLine(0, y1, width, y1);
                    } else {
                        drawAlphaGradient(g, 0, y1, width, height, capBkColor, 0, -48, 255 - menuBarTrans);
                    }
                }
            }
        }
        g.setColor(barTxtColor);

        //#sijapp cond.if modules_TOOLBAR is "true"#
        if (toolbar != null) {
            toolbar.paint(g);
            if (style != DMS_DRAW && toolbar.pressedToolbar(curX, curY, false)) {
                return true;
            }
        }
        if (style != DMS_DRAW && clActive && (ptInRect(curX, curY, width / 3, y1, 2 * width / 3, y2))) {
            Options.changeToolbar();
            return true;
        }
        //#sijapp cond.end#

        int textY = (_y1 + y2 - facade.getFontHeight()) / 2 + 1;

        if (leftMenu != null) {
//#sijapp cond.if target is "MIDP2"#
            if ((style == DMS_CLICK) && (ptInRect(curX, curY, 0, y1, width / 2, y2))) {
                leftMenuPressed();
                invalidate();
                return true;
            }
//#sijapp cond.end#
        }
        if ((text = NativeCanvas.getLeftName()) != null) {
            //0x0077ff
            drawString(g, facade, text, layer/* + height * (100 - anime) / 100*/, textY, Graphics.TOP | Graphics.LEFT);
        }

        if (rightMenu != null) {
//#sijapp cond.if target is "MIDP2"#
            if ((style == DMS_CLICK) && (ptInRect(curX, curY, width / 2, y1, width, y2))) {
                rightMenuPressed();
                invalidate();
                return true;
            }
//#sijapp cond.end#
        }
        if ((text = NativeCanvas.getRightName()) != null) {
            //0x0077ff
            drawString(g, facade, text, width - layer - facade.stringWidth(text)/* - height * (100 - anime) / 100*/, textY, Graphics.TOP | Graphics.LEFT);
        }
        g.setColor(barTxtColor);
        drawString(g, facade, timeString, (width - facade.stringWidth(timeString)) / 2, textY, Graphics.TOP | Graphics.LEFT);
        return false;
    }

    protected Command findMenuByType(int type) {
        if ((leftMenu != null) && (leftMenu.getCommandType() == type)) {
            return leftMenu;
        }
        if ((rightMenu != null) && (rightMenu.getCommandType() == type)) {
            return rightMenu;
        }
        return null;
    }

    public int getMenuBarHeight() {
        if ((!fullScreen) || (!softBar)) {
            return 0;
        }
        int i = Math.max(facade.getFontHeight(), Options.getInt(Options.OPTION_ICONS_CANVAS)) + 2;
        //#sijapp cond.if modules_TOOLBAR is "true"#
        if (toolbar != null) {
            i += toolbar.getHeight();
        }
        //#sijapp cond.end#
        return i;
    }

    public byte getMenuSide() {
        //if (true) {
        //    return 0;
        //}
        if (leftMenu == JimmUI.cmdMenu) {
            return (byte) -1;
        }
        return (byte) 1;
    }


    protected static final byte DMS_DRAW = 0;
    protected static final byte DMS_CLICK = 1;
    protected static final byte DMS_DBLCLICK = 2;
}

