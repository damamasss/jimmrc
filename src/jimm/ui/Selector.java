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
 File: src/jimm/util/Selector.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Denis K.
 *******************************************************************************/

package jimm.ui;

import DrawControls.*;
import jimm.*;
import jimm.comm.Util;
import jimm.comm.XStatus;
import jimm.forms.StatusesForm;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class Selector extends VirtualList implements VirtualListCommands {

    private int cols, rows, itemHeight, itemWidth, curCol, uiMode, stsIdxs[];
    public int defIdx;
    private String[] names;
    private StatusesForm stsForm;

    public final static int UI_MODE_STS = 0;
    public final static int UI_MODE_XSTS = 1;
    public final static int UI_MODE_PSTS = 2;

    public Selector(int uiMode, StatusesForm stsForm, int currIndex) {
        super(null);
        setVLCommands(this);
        this.uiMode = uiMode;
        this.stsForm = stsForm;

        setMode(MODE_TEXT);
        StringBuffer sb = new StringBuffer();

        if (uiMode == UI_MODE_STS) {
            //stsIdxs = Util.explodeToInt("7 0 4 5 2 3 13", ' ');
            stsIdxs = Util.explodeToInt("7 1 8 9 10 11 12 0 4 5 2 3 13", ' ');
//            names = Util.explode(sb.append("status_online").append("|")
//                    .append("status_away").append("|").append("status_na").append("|")
//                    .append("status_occupied").append("|").append("status_dnd").append("|")
//                    .append("status_invisible").append("|").append("status_invis_all").toString(), '|');
            names = Util.explode(sb.append("status_online").append("|")
                    .append("status_chat").append("|").append("status_evil").append("|")
                    .append("status_depression").append("|").append("status_home").append("|")
                    .append("status_work").append("|").append("status_lunch").append("|")
                    .append("status_away").append("|").append("status_na").append("|")
                    .append("status_occupied").append("|").append("status_dnd").append("|")
                    .append("status_invisible").append("|").append("status_invis_all").toString(), '|');
        } else if (uiMode == UI_MODE_PSTS) {
            names = Util.explode(sb.append("ps_visible_all").append("|")
                    .append("ps_visible_vl").append("|").append("ps_invisible_invl").append("|")
                    .append("ps_visible_cl").append("|").append("ps_invisible_all").toString(), '|');
        }

        if (stsForm != null) {
            defIdx = currIndex;
        }
        calcColsAndRows();
        setCurrSelectedIdx(currIndex);
// #sijapp cond.if modules_SMILES is "true"#
//        setCurrSelectedIdx((stsForm == null)?currIndex + cols:currIndex);
// #sijapp cond.end #       
        showCurrName();
    }

    public void calcColsAndRows() {
        int drawWidth = getDrawWidth() - 2;
        int imgHeight = getImageList().getHeight();
// #sijapp cond.if (modules_GIFSMILES is "true" | modules_ANISMILES is "true")#
        itemHeight = imgHeight + 4;
        itemHeight += 2;
        int osize = Options.getInt(Options.OPTION_EMOTION_SIZE);
        itemWidth = (stsForm == null) ? (osize > 0) ? osize : Emotions.iconsSize : itemHeight;
// #sijapp cond.else #
//#		itemHeight = imgHeight + (stsForm == null ? 2 : 4);
//#		itemWidth = itemHeight;
// #sijapp cond.end #
        cols = drawWidth / itemWidth;
        int index = getLength();
        rows = (index + cols - 1) / cols;
        itemHeight = itemWidth += ((drawWidth - (itemWidth * cols)) / cols);
    }

    private ImageList getImageList() {
        switch (uiMode) {
            case UI_MODE_STS:
                return ContactList.imageList;
            case UI_MODE_PSTS:
                return ContactList.pstatuses;
            case UI_MODE_XSTS:
                return XStatus.getXStatusImageList();
        }
//#sijapp cond.if modules_SMILES is "true"#
        return Emotions.images;
//#sijapp cond.else#
//#		return null;
//#sijapp cond.end#
    }

    public int[] colors;

//#sijapp cond.if target is "MIDP2"#
//    public void pointerPressed(int x, int y) {
//        super.pointerPressed(x,y);
//	}
//
//    public void pointerDragged(int x, int y) {
//        super.pointerDragged(x,y);
//	}
//
//	public void pointerReleased(int x, int y) {
//        super.pointerReleased(x,y);
//	}

    protected boolean pointerPressedOnUtem(int index, int x, int y, int x1, int x2) {
        int prCol = curCol;
        curCol = x / itemWidth;
        curCol = Math.min(Math.max(curCol, 0), cols - 1);
        checkCurrItem();
        showCurrName();
        invalidate();
        return (prCol == curCol);
    }

    protected boolean oldDoubleClick() {
        return true;
    }
// #sijapp cond.end#

    public void paintAllOnGraphics(Graphics graphics, int mode, int curX, int curY) {
        if (stsForm == null) {
            super.paintAllOnGraphics(graphics, mode, curX, curY);
            return;
        }
        if (mode == DMS_DRAW) {
            stsForm.paintAllOnGraphics(graphics, mode, curX, curY);
            return;
        }
        if (stsForm.drawCaption(graphics, mode, curX, curY)) {
            return;
        }
        if (stsForm.drawMenuBar(graphics, getMenuBarHeight(), getHeightInternal(), mode, curX, curY)) {
            return;
        }
        drawItems(graphics, getCapHeight(), getFontHeight(), mode, curX, curY, false, true);
    }

    protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight) {
        boolean flag = (stsForm == null);
        if (!flag) {
            flag |= (stsForm.getCurrSelector() == this);
        }
        drawItemData(g, index, x1, y1, x2, y2, flag);
    }

    public void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, boolean showCursor) {
        int xa, xb;
        int startIdx = cols * index;

        int imagesCount = getImageList().size();
        boolean isSelected = (index == getCurrIndex());
        int idx = getLength();
        xa = x1;
        Icon icon = null;
        for (int i = 0; i < cols; i++, startIdx++) {
            if (startIdx >= idx) break;

            xb = xa + itemWidth;

            if (stsForm != null) {
                if (index * cols + i == defIdx) {
                    g.setColor(getColor(COLOR_DCURSOR));
                    g.drawRect(xa, y1, itemWidth - 1, y2 - y1 - 1);
                    g.drawRect(xa - 1, y1 - 1, itemWidth + 1, y2 - y1 + 1);
                }
            }
            if (isSelected && (i == curCol)) {
                if (showCursor) {
                    if (cursorTrans == 0) {
                        drawGradient(g, xa + 1, y1 + 1, itemWidth - 2, y2 - y1 - 2, cursorColor, 16, -32, 32);
                    } else if (stsForm == null) {
                        drawAlphaGradient(g, xa + 1, y1 + 1, itemWidth - 2, y2 - y1 - 2, cursorColor, -32, 0, 255 - cursorTrans);
                    }
                    g.setColor(dcursor);
                    g.drawRect(xa, y1, itemWidth - 1, y2 - y1 - 1);
                }
            }

            if (startIdx < imagesCount) {
                int centerX = xa + itemWidth / 2;
                int centerY = (y1 + y2) / 2;
                if (stsForm == null) {
//#sijapp cond.if modules_SMILES is "true"#
                    icon = Emotions.images.elementAt(startIdx);
//#sijapp cond.end#
                } else {
                    int xstIndex = startIdx;
                    if (uiMode == UI_MODE_XSTS) {
                        xstIndex = (startIdx == 0) ? XStatus.XSTATUS_NONE - 1 : startIdx - 1;
                        icon = XStatus.getStatusImage(xstIndex);
                    } else if (uiMode == UI_MODE_STS) {
                        icon = getImageList().elementAt(stsIdxs[xstIndex]);
                    } else {
                        icon = getImageList().elementAt(xstIndex);
                    }
                }
                if (icon != null) {
                    icon.drawInCenter(g, centerX, centerY);
                }
            }
            xa = xb;
        }
    }

    protected void checkCurrItem() {
        super.checkCurrItem();
        int index = curCol + getCurrIndex() * cols;
        int idx = getLength();
        if (index >= idx) {
            curCol = (idx - 1) % cols;
        }
    }

    public void moveCursor(int step, boolean moveTop) {
        storelastItemIndexes();
        if (moveTop) {
            topItem += step;
        }
        currItem += step;
        if (step == 1) {
            if (currItem >= getSize()) {
                if (stsForm == null) {
                    currItem = 0;  // перемещение курсора в самый верх
                } else {
                    stsForm.nextSelector(true);
                }
            }
        } else if (step == -1) {
            if (currItem < 0) {
                if (stsForm == null) {
                    currItem = getSize() - 1; // перемещение курсора в самый низ
                } else {
                    stsForm.prevSelector(true);
                }
            }
        }
        checkItemsAndRepaint();
    }

    private void showCurrName() {
        if (stsForm == null) {
            setCaption(getCurrName());
        } else {
            stsForm.setCaption(getCurrName());
        }
    }

    protected int getLimY() {
        if (stsForm != null) {
            return getHeightInternal();
        }
        return super.getLimY();
    }

    public String getCurrName() {
        int selIdx = getCurrIndex() * cols + curCol;

        if (stsForm == null) {
//#sijapp cond.if modules_SMILES is "true"#
            if (selIdx < Emotions.selEmotionsWord.length) {
                Emotions.emotionText = Emotions.selEmotionsWord[selIdx];
                return Emotions.emotionText;
                //return Emotions.selEmotionsWord[selIdx];
            }
//#sijapp cond.end#
        } else {
            if (uiMode == UI_MODE_XSTS) {
                if (selIdx <= XStatus.getXStatusCount() + 1) {
                    int xstIndex = (selIdx == 0) ? XStatus.XSTATUS_NONE - 1 : selIdx - 1;
                    return XStatus.getStatusAsString(xstIndex);
                }
            } else if (uiMode == UI_MODE_STS || uiMode == UI_MODE_PSTS) {
                if (selIdx < names.length && selIdx >= 0) {
                    return ResourceBundle.getString(names[selIdx]);
                }
            }
        }
        return "";
    }

    public int getItemHeight(int itemIndex) {
        return itemHeight;
    }

    protected int getSize() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getStsIdx() {
        return stsIdxs[getCurrSelectedIdx()];
    }

    public int getLength() {
//#sijapp cond.if modules_SMILES is "true"#
        if (stsForm == null) {
            return Emotions.selEmotionsWord.length;
        }
//#sijapp cond.end#
        if (uiMode == UI_MODE_XSTS) {
            return XStatus.getXStatusCount() + 1;
        }
        if (uiMode == UI_MODE_PSTS) {
            return names.length;
        }
        return stsIdxs.length;
    }

    public int getCurrSelectedIdx() {
        return getCurrIndex() * cols + curCol;
    }

    public void setCurrSelectedIdx(int index) {
        if (index < 0) index = 0;
        else if (index >= getLength()) index = getLength() - 1;
        setCurrentItem(index / cols);
        curCol = index % cols;
        invalidate();
    }

    protected void get(int index, ListItem item) {
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        if (type == KEY_RELEASED) {
            return;
        }
        int lastCol = curCol;
        int curRow = getCurrIndex();
        int rowCount = getSize();
        switch (gameAct) {
            case Canvas.LEFT:
                if (curCol != 0) {
                    curCol--;
                } else if (curRow != 0) {
                    curCol = cols - 1;
                    curRow--;
                } else {
                    if (stsForm == null) {
                        curCol = (getLength() - 1) % cols;
                        curRow = rowCount - 1;
                    } else {
                        stsForm.prevSelector(false);
                    }
                }
                break;

            case Canvas.RIGHT:
                if (curCol < (cols - 1)) {
                    curCol++;
                } else if (curRow <= rowCount) {
                    curCol = 0;
                    curRow++;
                }
                if ((curCol + curRow * cols) > (getLength() - 1)) {
                    if (stsForm == null) {
                        curCol = 0;
                        curRow = 0;
                    } else {
                        stsForm.nextSelector(false);
                    }
                }
                break;
        }
        setCurrentItem(curRow);

        int index = curCol + getCurrIndex() * cols;
        int idx = getLength();
        if (index >= idx) curCol = (idx - 1) % cols;

        if (lastCol != curCol) {
            invalidate();
            showCurrName();
        }
    }

    public void vlCursorMoved(VirtualList sender) {
        showCurrName();
    }

    public void vlItemClicked(VirtualList sender) {
        if (stsForm != null) {
            stsForm.vlItemClicked(null);
        }
    }

    public void doKeyreaction(int keyCode, int type) {
//#sijapp cond.if modules_SMILES is "true"#
//        if ((keyCode == Canvas.KEY_STAR && type == KEY_PRESSED) && (stsForm == null)) {
//            String text = null;
//            int selIdx = getCurrIndex() * cols + curCol;
//            if (selIdx < Emotions.selEmotionsSmileNames.length) {
//                text = Emotions.selEmotionsWord[selIdx];
//            }
//            if (text != null) {
//                Jimm.setDisplay(new Alert(null, Util.removeNullChars(text).replace('\t', '\n'), null, null));
//            }
//        } else if (((keyCode == Canvas.KEY_NUM0) && (stsForm == null))) {
//            Emotions.selectFast();
//        } else
//#sijapp cond.end#
        super.doKeyreaction(keyCode, type);
    }
}